package org.fleur.srcbackend.pojo.vo

/**
 * 写入类 SQL 的结果：只返回影响行数。
 */
data class SqlMutationResult(
    val sqlType: String,
    val affectedRows: Int,
)

