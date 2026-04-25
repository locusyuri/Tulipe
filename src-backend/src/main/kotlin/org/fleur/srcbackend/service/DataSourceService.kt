package org.fleur.srcbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.fleur.srcbackend.pojo.dto.DdlColumn
import org.fleur.srcbackend.pojo.dto.ExecuteSqlRequest
import org.fleur.srcbackend.pojo.dto.SqlFilter
import org.fleur.srcbackend.pojo.dto.SqlOrderBy
import org.fleur.srcbackend.pojo.dto.StructuredDdlRequest
import org.fleur.srcbackend.pojo.dto.StructuredDeleteRequest
import org.fleur.srcbackend.pojo.dto.StructuredInsertRequest
import org.fleur.srcbackend.pojo.dto.StructuredQueryRequest
import org.fleur.srcbackend.pojo.dto.StructuredUpdateRequest
import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.pojo.entity.ConnectionProfile
import org.fleur.srcbackend.pojo.entity.JdbcConnectionConfig
import org.fleur.srcbackend.pojo.entity.MysqlConnectionConfig
import org.fleur.srcbackend.pojo.entity.PostgreSqlConnectionConfig
import org.fleur.srcbackend.pojo.enums.DbType
import org.fleur.srcbackend.pojo.vo.SqlExecutionResult
import org.fleur.srcbackend.pojo.vo.SqlMutationResult
import org.fleur.srcbackend.pojo.vo.SqlQueryResult
import org.fleur.srcbackend.repository.ConnectionProfileRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class DataSourceService(
    private val connectionPoolManager: ConnectionPoolManager,
    private val connectionProfileRepository: ConnectionProfileRepository,
    private val objectMapper: ObjectMapper,
) {

    fun connect(request: Connection): Long {
        // 外层 dbType 与内层 config.type 必须一致，避免请求语义冲突。
        val outerDbType = DbType.from(request.dbType)
        val innerDbType = DbType.from(request.config.type)
        require(outerDbType == innerDbType) {
            "dbType 与 config.type 不一致: ${request.dbType} / ${request.config.type}"
        }

        val jdbcTemplate = JdbcTemplate(connectionPoolManager.getOrCreate(request))
        // 用最小探活 SQL 验证连接可用性。
        val ok = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
        require(ok == 1) { "连接测试失败" }

        // 连接校验通过后立即落库，便于后续通过 connectionId 进行管理和执行。
        val profile = toConnectionProfile(request, outerDbType)
        return connectionProfileRepository.save(profile)
    }

    fun executeQuery(request: StructuredQueryRequest): SqlQueryResult {
        require(request.page >= 1) { "page 必须 >= 1" }
        require(request.pageSize in 1..1000) { "pageSize 必须在 1..1000 之间" }

        val table = sanitizeTableName(request.table)
        val selectedColumns = if (request.columns.isEmpty()) {
            "*"
        } else {
            request.columns.joinToString(", ") { sanitizeColumnName(it) }
        }

        val whereClause = buildWhereClause(request.filters)
        val orderClause = buildOrderByClause(request.orderBy)
        val offset = (request.page - 1) * request.pageSize

        val sql = buildString {
            append("SELECT ")
            append(selectedColumns)
            append(" FROM ")
            append(table)
            append(whereClause.sql)
            append(orderClause)
            append(" LIMIT ? OFFSET ?")
        }

        val args = whereClause.args.toMutableList().apply {
            add(request.pageSize)
            add(offset)
        }

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val rows = jdbcTemplate.queryForList(sql, *args.toTypedArray())
        return SqlQueryResult(rows = rows)
    }

    fun executeInsert(request: StructuredInsertRequest): SqlMutationResult {
        val table = sanitizeTableName(request.table)
        require(request.values.isNotEmpty()) { "values 不能为空" }

        val entries = request.values.entries.toList()
        val columns = entries.joinToString(", ") { sanitizeColumnName(it.key) }
        val placeholders = List(entries.size) { "?" }.joinToString(", ")
        val sql = "INSERT INTO $table ($columns) VALUES ($placeholders)"
        val args = entries.map { it.value }

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql, *args.toTypedArray())
        return SqlMutationResult(sqlType = "INSERT", affectedRows = affectedRows)
    }

    fun executeUpdate(request: StructuredUpdateRequest): SqlMutationResult {
        val table = sanitizeTableName(request.table)
        require(request.setValues.isNotEmpty()) { "setValues 不能为空" }
        require(request.filters.isNotEmpty()) { "UPDATE 必须提供 filters，避免误更新全表" }

        val setEntries = request.setValues.entries.toList()
        val setClause = setEntries.joinToString(", ") { "${sanitizeColumnName(it.key)} = ?" }
        val whereClause = buildWhereClause(request.filters)
        val sql = "UPDATE $table SET $setClause${whereClause.sql}"

        val args = setEntries.map { it.value }.toMutableList().apply {
            addAll(whereClause.args)
        }

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql, *args.toTypedArray())
        return SqlMutationResult(sqlType = "UPDATE", affectedRows = affectedRows)
    }

    fun executeDelete(request: StructuredDeleteRequest): SqlMutationResult {
        val table = sanitizeTableName(request.table)
        require(request.filters.isNotEmpty()) { "DELETE 必须提供 filters，避免误删全表" }

        val whereClause = buildWhereClause(request.filters)
        val sql = "DELETE FROM $table${whereClause.sql}"

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql, *whereClause.args.toTypedArray())
        return SqlMutationResult(sqlType = "DELETE", affectedRows = affectedRows)
    }

    fun executeDdl(request: StructuredDdlRequest): SqlMutationResult {
        val sqlType = request.action.trim().uppercase(Locale.ROOT)
        val table = sanitizeTableName(request.table)

        val sql = when (sqlType) {
            "CREATE" -> buildCreateTableSql(table, request.columns)
            "DROP" -> "DROP TABLE $table"
            "TRUNCATE" -> "TRUNCATE TABLE $table"
            else -> throw IllegalArgumentException("结构化 DDL 当前仅支持 CREATE / DROP / TRUNCATE")
        }

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql)
        return SqlMutationResult(sqlType = sqlType, affectedRows = affectedRows)
    }

    /**
     * 兼容旧的统一执行入口，方便前端迁移到 query/update/delete 的拆分接口。
     */
    @Deprecated("Use executeQuery/executeUpdate/executeDelete/executeDdl")
    fun executeSql(request: ExecuteSqlRequest): SqlExecutionResult {
        require(request.sql.isNotBlank()) { "sql 不能为空" }

        val sql = normalizeSql(request.sql)
        val sqlType = resolveSqlType(sql)
        val jdbcTemplate = loadJdbcTemplate(request.connectionId)

        return when (sqlType) {
            "SELECT" -> SqlExecutionResult(sqlType = sqlType, rows = jdbcTemplate.queryForList(sql))
            "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP", "TRUNCATE" -> {
                SqlExecutionResult(sqlType = sqlType, affectedRows = jdbcTemplate.update(sql))
            }
            else -> throw IllegalArgumentException("暂不支持的 SQL 类型: $sqlType")
        }
    }

    // 通过首个关键字识别语句类型，首版仅开放常用读写语句。
    private fun resolveSqlType(sql: String): String {
        return sql.substringBefore(" ").uppercase()
    }

    // 校验 SQL 类型是否命中当前接口允许的范围。
    private fun requireSqlType(sql: String, allowedTypes: Set<String>): String {
        val sqlType = resolveSqlType(sql)
        require(sqlType in allowedTypes) {
            "当前接口不支持的 SQL 类型: $sqlType，允许类型: ${allowedTypes.joinToString(", ")}"
        }
        return sqlType
    }

    // 统一按 connectionId 取出连接配置并创建 JdbcTemplate，避免各接口重复查库和拼接连接池。
    private fun loadJdbcTemplate(connectionId: Long): JdbcTemplate {
        val profile = connectionProfileRepository.findById(connectionId)
            ?: throw IllegalArgumentException("连接不存在: $connectionId")
        val connection = toConnection(profile)
        return JdbcTemplate(connectionPoolManager.getOrCreate(connection))
    }

    private fun normalizeSql(sql: String): String {
        require(sql.isNotBlank()) { "sql 不能为空" }
        return sql.trim()
    }

    // 仅允许字母、数字、下划线和 schema.table 形式，避免标识符注入。
    private fun sanitizeTableName(table: String): String {
        val value = table.trim()
        val segmentPattern = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
        require(value.isNotBlank()) { "table 不能为空" }
        val segments = value.split(".")
        require(segments.all { segmentPattern.matches(it) }) { "非法表名: $table" }
        return value
    }

    // 字段名只允许常规标识符；保留 * 用于查询全部列。
    private fun sanitizeColumnName(column: String): String {
        val value = column.trim()
        if (value == "*") {
            return value
        }
        val pattern = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
        require(pattern.matches(value)) { "非法字段名: $column" }
        return value
    }

    private fun buildWhereClause(filters: List<SqlFilter>): BuiltSql {
        if (filters.isEmpty()) {
            return BuiltSql(sql = "", args = emptyList())
        }

        val parts = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        filters.forEach { filter ->
            val field = sanitizeColumnName(filter.field)
            val op = filter.operator.trim().lowercase(Locale.ROOT)

            when (op) {
                "eq" -> {
                    parts += "$field = ?"
                    args += filter.value
                }

                "ne" -> {
                    parts += "$field <> ?"
                    args += filter.value
                }

                "gt" -> {
                    parts += "$field > ?"
                    args += filter.value
                }

                "gte" -> {
                    parts += "$field >= ?"
                    args += filter.value
                }

                "lt" -> {
                    parts += "$field < ?"
                    args += filter.value
                }

                "lte" -> {
                    parts += "$field <= ?"
                    args += filter.value
                }

                "like" -> {
                    parts += "$field LIKE ?"
                    args += filter.value
                }

                "in" -> {
                    require(filter.values.isNotEmpty()) { "IN 操作需要 values" }
                    val placeholders = List(filter.values.size) { "?" }.joinToString(", ")
                    parts += "$field IN ($placeholders)"
                    args.addAll(filter.values)
                }

                "is_null" -> parts += "$field IS NULL"
                "is_not_null" -> parts += "$field IS NOT NULL"
                else -> throw IllegalArgumentException("不支持的过滤操作符: ${filter.operator}")
            }
        }

        return BuiltSql(sql = " WHERE ${parts.joinToString(" AND ")}", args = args)
    }

    private fun buildOrderByClause(orderBy: List<SqlOrderBy>): String {
        if (orderBy.isEmpty()) {
            return ""
        }

        val expression = orderBy.joinToString(", ") { item ->
            val field = sanitizeColumnName(item.field)
            val direction = item.direction.trim().uppercase(Locale.ROOT)
            require(direction == "ASC" || direction == "DESC") { "排序方向仅支持 ASC / DESC" }
            "$field $direction"
        }
        return " ORDER BY $expression"
    }

    private fun buildCreateTableSql(table: String, columns: List<DdlColumn>): String {
        require(columns.isNotEmpty()) { "CREATE TABLE 需要 columns 定义" }

        val definitions = columns.map { column ->
            val name = sanitizeColumnName(column.name)
            val type = sanitizeColumnType(column.type)
            buildString {
                append(name)
                append(" ")
                append(type)
                if (!column.nullable) {
                    append(" NOT NULL")
                }
                if (column.primaryKey) {
                    append(" PRIMARY KEY")
                }
                column.defaultValue?.takeIf { it.isNotBlank() }?.let {
                    append(" DEFAULT ")
                    append(it)
                }
            }
        }

        return "CREATE TABLE $table (${definitions.joinToString(", ")})"
    }

    private fun sanitizeColumnType(type: String): String {
        val value = type.trim()
        require(value.isNotBlank()) { "列类型不能为空" }
        require(!value.contains(";")) { "列类型不能包含分号" }
        return value
    }

    private data class BuiltSql(
        val sql: String,
        val args: List<Any?>,
    )

    private fun toConnectionProfile(request: Connection, dbType: DbType): ConnectionProfile {
        val config = request.config as? JdbcConnectionConfig
            ?: throw IllegalArgumentException("当前仅支持 JDBC 连接配置")

        val databaseName = when (config) {
            is MysqlConnectionConfig -> ""
            is PostgreSqlConnectionConfig -> config.database
        }

        // 这里先保存必要信息与扩展信息；group 等富信息放入 extra_json，后续可再扩展独立列。
        val extraJson = objectMapper.writeValueAsString(
            mapOf(
                "group" to request.group,
                "config" to request.config,
            )
        )

        return ConnectionProfile(
            profileName = request.name,
            dbType = dbType.code,
            host = config.host,
            port = config.port,
            databaseName = databaseName,
            username = config.username,
            passwordCiphertext = config.password,
            extraJson = extraJson,
        )
    }

    private fun toConnection(profile: ConnectionProfile): Connection {
        val dbType = DbType.from(profile.dbType)
        val password = profile.passwordCiphertext
            ?: throw IllegalArgumentException("连接缺少密码信息，无法执行 SQL")

        val group = runCatching {
            profile.extraJson
                ?.let { objectMapper.readTree(it).path("group").asText("") }
                .orEmpty()
        }.getOrDefault("")

        val config = when (dbType) {
            DbType.MYSQL -> MysqlConnectionConfig(
                host = profile.host,
                port = profile.port,
                username = profile.username,
                password = password,
            )

            DbType.POSTGRESQL -> PostgreSqlConnectionConfig(
                host = profile.host,
                port = profile.port,
                database = profile.databaseName,
                username = profile.username,
                password = password,
            )
        }

        return Connection(
            name = profile.profileName,
            group = group,
            dbType = dbType.code,
            config = config,
        )
    }
}
