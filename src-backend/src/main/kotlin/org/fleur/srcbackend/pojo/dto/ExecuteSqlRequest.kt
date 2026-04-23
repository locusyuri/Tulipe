package org.fleur.srcbackend.pojo.dto

import org.fleur.srcbackend.pojo.entity.Connection

/**
 * 执行 SQL 请求：携带目标连接配置与待执行 SQL。
 */
data class ExecuteSqlRequest(
    val connection: Connection,
    val sql: String,
)

