package org.fleur.srcbackend.service

import org.fleur.srcbackend.pojo.dto.ExecuteSqlRequest
import org.fleur.srcbackend.pojo.vo.SqlExecutionResult
import org.fleur.srcbackend.pojo.entity.Connection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class DataSourceService(
    private val connectionPoolManager: ConnectionPoolManager,
) {

    fun connect(request: Connection) {
        val jdbcTemplate = JdbcTemplate(connectionPoolManager.getOrCreate(request))
        // 用最小探活 SQL 验证连接可用性。
        val ok = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
        require(ok == 1) { "连接测试失败" }
    }

    fun executeSql(request: ExecuteSqlRequest): SqlExecutionResult {
        require(request.sql.isNotBlank()) { "sql 不能为空" }

        val sql = request.sql.trim()
        val sqlType = resolveSqlType(sql)
        val jdbcTemplate = JdbcTemplate(connectionPoolManager.getOrCreate(request.connection))

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
}
