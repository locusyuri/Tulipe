package org.fleur.srcbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.fleur.srcbackend.pojo.dto.ExecuteSqlRequest
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

    fun executeQuery(request: ExecuteSqlRequest): SqlQueryResult {
        val sql = normalizeSql(request.sql)
        requireSqlType(sql, setOf("SELECT"))

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val rows = jdbcTemplate.queryForList(sql)
        return SqlQueryResult(rows = rows)
    }

    fun executeUpdate(request: ExecuteSqlRequest): SqlMutationResult {
        val sql = normalizeSql(request.sql)
        val sqlType = requireSqlType(sql, setOf("INSERT", "UPDATE"))

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql)
        return SqlMutationResult(sqlType = sqlType, affectedRows = affectedRows)
    }

    fun executeDelete(request: ExecuteSqlRequest): SqlMutationResult {
        val sql = normalizeSql(request.sql)
        val sqlType = requireSqlType(sql, setOf("DELETE"))

        val jdbcTemplate = loadJdbcTemplate(request.connectionId)
        val affectedRows = jdbcTemplate.update(sql)
        return SqlMutationResult(sqlType = sqlType, affectedRows = affectedRows)
    }

    /**
     * 兼容旧的统一执行入口，方便前端迁移到 query/update/delete 的拆分接口。
     */
    @Deprecated("Use executeQuery/executeUpdate/executeDelete")
    fun executeSql(request: ExecuteSqlRequest): SqlExecutionResult {
        require(request.sql.isNotBlank()) { "sql 不能为空" }

        val sql = normalizeSql(request.sql)
        val sqlType = resolveSqlType(sql)
        val jdbcTemplate = loadJdbcTemplate(request.connectionId)

        return when (sqlType) {
            "SELECT" -> SqlExecutionResult(sqlType = sqlType, rows = jdbcTemplate.queryForList(sql))
            "INSERT", "UPDATE", "DELETE" -> SqlExecutionResult(sqlType = sqlType, affectedRows = jdbcTemplate.update(sql))
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
