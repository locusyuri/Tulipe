package org.fleur.srcbackend.pojo.vo

/**
 * 查询 SQL 的结果：只返回 rows。
 */
data class SqlQueryResult(
    val sqlType: String = "SELECT",
    val rows: List<Map<String, Any?>>,
)

