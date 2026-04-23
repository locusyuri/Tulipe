package org.fleur.srcbackend.pojo.vo

/**
 * SQL 执行结果：查询语句返回 rows，更新语句返回 affectedRows。
 */
data class SqlExecutionResult(
    val sqlType: String,
    val rows: List<Map<String, Any?>>? = null,
    val affectedRows: Int? = null,
)