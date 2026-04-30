package org.fleur.srcbackend.pojo.dto

/**
 * 结构化查询请求：前端传递表、字段、条件等信息，由后端负责拼接参数化 SQL。
 */
data class StructuredQueryRequest(
    val connectionId: Long,
    val table: String,
    val columns: List<String> = emptyList(),
    val filters: List<SqlFilter> = emptyList(),
    val orderBy: List<SqlOrderBy> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 100,
)

/**
 * 结构化插入请求。
 */
data class StructuredInsertRequest(
    val connectionId: Long,
    val table: String,
    val values: Map<String, Any?>,
)

/**
 * 结构化更新请求。
 */
data class StructuredUpdateRequest(
    val connectionId: Long,
    val table: String,
    val setValues: Map<String, Any?>,
    val filters: List<SqlFilter> = emptyList(),
)

/**
 * 结构化删除请求。
 */
data class StructuredDeleteRequest(
    val connectionId: Long,
    val table: String,
    val filters: List<SqlFilter> = emptyList(),
)

/**
 * 结构化 DDL 请求：当前先支持 CREATE / DROP / TRUNCATE。
 */
data class StructuredDdlRequest(
    val connectionId: Long,
    val action: String,
    val table: String,
    val columns: List<DdlColumn> = emptyList(),
)

/**
 * 过滤条件，operator 取值：eq/ne/gt/gte/lt/lte/like/in/is_null/is_not_null。
 */
data class SqlFilter(
    val field: String,
    val operator: String = "eq",
    val value: Any? = null,
    val values: List<Any?> = emptyList(),
)

/**
 * 排序字段，direction 取值：ASC / DESC。
 */
data class SqlOrderBy(
    val field: String,
    val direction: String = "ASC",
)

/**
 * CREATE TABLE 场景下的列定义。
 */
data class DdlColumn(
    val name: String,
    val type: String,
    val nullable: Boolean = true,
    val primaryKey: Boolean = false,
    val defaultValue: String? = null,
)

