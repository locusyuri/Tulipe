package org.fleur.srcbackend.pojo.dto

/**
 * 执行 SQL 请求：携带连接主键与待执行 SQL。
 */
data class ExecuteSqlRequest(
    val connectionId: Long,
    val sql: String,
)

