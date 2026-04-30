package org.fleur.srcbackend.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PreDestroy
import org.fleur.srcbackend.config.ConnectionPoolProperties
import org.fleur.srcbackend.pojo.dto.Connection
import org.fleur.srcbackend.pojo.dto.JdbcConnectionConfig
import org.fleur.srcbackend.pojo.dto.MysqlConnectionConfig
import org.fleur.srcbackend.pojo.dto.PostgreSqlConnectionConfig
import org.fleur.srcbackend.pojo.enums.DbType
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

@Component
class ConnectionPoolManager(
    private val poolProperties: ConnectionPoolProperties,
) {

    private val dataSourceCache = ConcurrentHashMap<String, HikariDataSource>()

    /**
     * 基于连接配置懒加载连接池。
     */
    fun getOrCreate(request: Connection): HikariDataSource {
        val key = buildPoolKey(request)
        return dataSourceCache.computeIfAbsent(key) { createDataSource(request) }
    }

    private fun createDataSource(request: Connection): HikariDataSource {
        val config = request.config as? JdbcConnectionConfig
            ?: throw IllegalArgumentException("当前仅支持 JDBC 连接配置")
        val dbType = config.type

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
            maximumPoolSize = poolProperties.maximumPoolSize
            minimumIdle = poolProperties.minimumIdle
            connectionTimeout = poolProperties.connectionTimeout
            validationTimeout = poolProperties.validationTimeout
            idleTimeout = poolProperties.idleTimeout
            maxLifetime = poolProperties.maxLifetime
            poolName = "conn-pool-${dbType.code}-${shortDigest(jdbcUrl)}"
            connectionTestQuery = poolProperties.connectionTestQuery
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
