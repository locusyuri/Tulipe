package org.fleur.srcbackend.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PreDestroy
import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.pojo.entity.JdbcConnectionConfig
import org.fleur.srcbackend.pojo.entity.MysqlConnectionConfig
import org.fleur.srcbackend.pojo.entity.PostgreSqlConnectionConfig
import org.fleur.srcbackend.pojo.enums.DbType
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

@Component
class ConnectionPoolManager {

    private val dataSourceCache = ConcurrentHashMap<String, HikariDataSource>()

    /**
     * 基于连接配置懒加载连接池。
     */
    fun getOrCreate(request: Connection): HikariDataSource {
        val key = buildPoolKey(request)
        return dataSourceCache.computeIfAbsent(key) { createDataSource(request) }
    }

    private fun createDataSource(request: Connection): HikariDataSource {
        val dbType = DbType.from(request.dbType)
        val config = request.config as? JdbcConnectionConfig
            ?: throw IllegalArgumentException("当前仅支持 JDBC 连接配置")

        val resolvedDbType = DbType.from(config.type)
        require(dbType == resolvedDbType) {
            "dbType 与 config.type 不一致: ${request.dbType} / ${config.type}"
        }

        val jdbcUrl = when (dbType) {
            DbType.MYSQL -> {
                val mysqlConfig = config as? MysqlConnectionConfig
                    ?: throw IllegalArgumentException("mysql 类型必须使用 MysqlConnectionConfig")
                dbType.buildJdbcUrl(mysqlConfig.host, mysqlConfig.port)
            }

            DbType.POSTGRESQL -> {
                val postgresConfig = config as? PostgreSqlConnectionConfig
                    ?: throw IllegalArgumentException("postgresql 类型必须使用 PostgreSqlConnectionConfig")
                dbType.buildJdbcUrl(postgresConfig.host, postgresConfig.port, postgresConfig.database)
            }
        }

        // 连接池参数先采用保守默认值，后续可按负载做配置化。
        val hikariConfig = HikariConfig().apply {
            driverClassName = dbType.driverClassName
            this.jdbcUrl = jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = 10
            minimumIdle = 1
            connectionTimeout = 5_000
            validationTimeout = 2_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
            poolName = "conn-pool-${dbType.code}-${shortDigest(jdbcUrl)}"
            connectionTestQuery = "SELECT 1"
        }

        return HikariDataSource(hikariConfig)
    }

    /**
     * 通过不可逆摘要构造缓存 key，避免直接暴露明文密码。
     */
    private fun buildPoolKey(request: Connection): String {
        val base = when (val config = request.config) {
            is MysqlConnectionConfig -> {
                "mysql|${config.host}|${config.port}|${config.username}|${config.password}"
            }

            is PostgreSqlConnectionConfig -> {
                "postgresql|${config.host}|${config.port}|${config.database}|${config.username}|${config.password}"
            }
        }
        return sha256(base)
    }

    private fun shortDigest(value: String): String = sha256(value).take(8)

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @PreDestroy
    fun closeAll() {
        dataSourceCache.values.forEach { it.close() }
        dataSourceCache.clear()
    }
}


