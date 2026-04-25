package org.fleur.srcbackend.pojo.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.fleur.srcbackend.pojo.enums.DbType

/**
 * 数据库连接信息，用于后续的查询和操作。
 */
data class Connection(
    // 用户自定义连接名称，用于列表展示与快速识别。
    val name: String,
    // 用户分组信息，用于在前端按业务场景分类。
    val group: String,
    // 多态配置对象，按 type 反序列化为具体子类型。
    val config: ConnectionConfig,
)

/**
 * 数据库连接配置的多态基类，根据 type 字段动态反序列化为具体类型。
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MysqlConnectionConfig::class, name = "mysql"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "postgresql"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "postgres"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "pg"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "pgs"),
)
sealed interface ConnectionConfig {
    val type: DbType
}

/**
 * JDBC 连接配置的基类，所有基于 JDBC 的数据库都继承这个接口。
 */
sealed interface JdbcConnectionConfig : ConnectionConfig {
    // JDBC 通用连接字段：所有基于 JDBC 的数据库都共用这些信息。
    val host: String
    val port: Int
    val username: String
    val password: String
}

/**
 * MySQL 连接配置。
 */
data class MysqlConnectionConfig(
    override val type: DbType = DbType.MYSQL,
    override val host: String,
    override val port: Int,
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

/**
 * PostgreSQL 连接配置。
 */
data class PostgreSqlConnectionConfig(
    override val type: DbType = DbType.POSTGRESQL,
    override val host: String,
    override val port: Int,

    val database: String, // PostgreSQL 需要在连接阶段指定默认 database，所以这个字段只放在 PG 专属配置里。
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

