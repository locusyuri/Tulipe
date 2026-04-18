package org.fleur.srcbackend.service

import org.fleur.srcbackend.pojo.dto.ConnectionRequest
import org.fleur.srcbackend.pojo.enums.DbType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Service

@Service
class DataSourceService {

    fun connect(request: ConnectionRequest): Map<String, Any> {
        // 把前端入参统一解析成枚举，避免字符串分支散落在业务代码里。
        val dbType = DbType.from(request.dbType)
        val jdbcUrl = dbType.buildJdbcUrl(request.host, request.port, request.database)

        val dataSource = DriverManagerDataSource().apply {
            setDriverClassName(dbType.driverClassName)
            url = jdbcUrl
            username = request.username
            password = request.password
        }

        val jdbcTemplate = JdbcTemplate(dataSource)
        // 用最小探活 SQL 验证连接可用性。
        val ok = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)

        return mapOf(
            "connected" to (ok == 1),
            "dbType" to dbType.code,
            "url" to jdbcUrl,
        )
    }
}
