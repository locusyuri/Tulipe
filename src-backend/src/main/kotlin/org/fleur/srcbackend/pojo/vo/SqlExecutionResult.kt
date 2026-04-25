package org.fleur.srcbackend.pojo.vo

/**
 * 兼容旧 /data-source/execute 入口的结果对象。
 *
 * 新接口已拆分为 SqlQueryResult 与 SqlMutationResult。
 */
data class SqlExecutionResult(
    val sqlType: String,
    val rows: List<Map<String, Any?>>? = null,
    val affectedRows: Int? = null,
)