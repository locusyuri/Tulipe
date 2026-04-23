package org.fleur.srcbackend.service

import org.fleur.srcbackend.pojo.entity.JdbcConnectionConfig
import org.fleur.srcbackend.pojo.entity.MysqlConnectionConfig
import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.pojo.entity.PostgreSqlConnectionConfig
import org.fleur.srcbackend.pojo.enums.DbType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Service

@Service
class DataSourceService {

    fun connect(request: Connection) {
        // 外层 dbType 与内层 config.type 必须一致，避免请求语义冲突。
        val outerDbType = DbType.from(request.dbType)
        val innerDbType = DbType.from(request.config.type)
        require(outerDbType == innerDbType) {
            "dbType 与 config.type 不一致: ${request.dbType} / ${request.config.type}"
        }

        // 目前只支持 JDBC 子类型（MySQL/PostgreSQL），其他类型后续扩展。
        val config = request.config as? JdbcConnectionConfig
            ?: throw IllegalArgumentException("当前仅支持 JDBC 连接配置")

        // MySQL 与 PostgreSQL 共用 JDBC 连接能力，但 PostgreSQL 需要额外指定 database。
        val jdbcUrl = when (outerDbType) {
            DbType.MYSQL -> {
                val mysqlConfig = config as? MysqlConnectionConfig
                    ?: throw IllegalArgumentException("mysql 类型必须使用 MysqlConnectionConfig")
                outerDbType.buildJdbcUrl(mysqlConfig.host, mysqlConfig.port)
            }
            DbType.POSTGRESQL -> {
                val postgresConfig = config as? PostgreSqlConnectionConfig
                    ?: throw IllegalArgumentException("postgresql 类型必须使用 PostgreSqlConnectionConfig")
                outerDbType.buildJdbcUrl(postgresConfig.host, postgresConfig.port, postgresConfig.database)
            }
        }

        val dataSource = DriverManagerDataSource().apply {
            setDriverClassName(outerDbType.driverClassName)
            url = jdbcUrl
            username = config.username
            password = config.password
        }

        val jdbcTemplate = JdbcTemplate(dataSource)
        // 用最小探活 SQL 验证连接可用性。
        val ok = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
        require(ok == 1) { "连接测试失败" }
    }
}
