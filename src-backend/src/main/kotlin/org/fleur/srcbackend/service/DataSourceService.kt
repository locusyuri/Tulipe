package org.fleur.srcbackend.service

import org.fleur.srcbackend.pojo.dto.ExecuteSqlRequest
import org.fleur.srcbackend.pojo.vo.SqlExecutionResult
import org.fleur.srcbackend.pojo.entity.ConnectionProfile
import org.fleur.srcbackend.pojo.entity.JdbcConnectionConfig
import org.fleur.srcbackend.pojo.entity.MysqlConnectionConfig
import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.pojo.entity.PostgreSqlConnectionConfig
import org.fleur.srcbackend.pojo.enums.DbType
import org.fleur.srcbackend.repository.ConnectionProfileRepository
import com.fasterxml.jackson.databind.ObjectMapper
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

    fun executeSql(request: ExecuteSqlRequest): SqlExecutionResult {
        require(request.sql.isNotBlank()) { "sql 不能为空" }

        val profile = connectionProfileRepository.findById(request.connectionId)
            ?: throw IllegalArgumentException("连接不存在: ${request.connectionId}")

        val sql = request.sql.trim()
        val sqlType = resolveSqlType(sql)
        val connection = toConnection(profile)
        val jdbcTemplate = JdbcTemplate(connectionPoolManager.getOrCreate(connection))

        return when (sqlType) {
            "SELECT" -> {
                val rows = jdbcTemplate.queryForList(sql)
                SqlExecutionResult(sqlType = sqlType, rows = rows)
            }

            "UPDATE", "INSERT", "DELETE" -> {
                val affectedRows = jdbcTemplate.update(sql)
                SqlExecutionResult(sqlType = sqlType, affectedRows = affectedRows)
            }

            else -> throw IllegalArgumentException("暂不支持的 SQL 类型: $sqlType")
        }
    }

    // 通过首个关键字识别语句类型，首版仅开放常用读写语句。
    private fun resolveSqlType(sql: String): String {
        return sql.substringBefore(" ").uppercase()
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
