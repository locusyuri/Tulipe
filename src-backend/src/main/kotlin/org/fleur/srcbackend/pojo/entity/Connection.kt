package org.fleur.srcbackend.pojo.entity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Connection(
    // 用户自定义连接名称，用于列表展示与快速识别。
    val name: String,
    // 用户分组信息，用于在前端按业务场景分类。
    val group: String,
    // 数据库类型标识，决定 config 应该反序列化为哪一种具体配置。
    val dbType: String,
    // 多态配置对象，按 type 反序列化为具体子类型。
    val config: ConnectionConfig,
)


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MysqlConnectionConfig::class, name = "mysql"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "postgresql"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "postgres"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "pg"),
    JsonSubTypes.Type(value = PostgreSqlConnectionConfig::class, name = "pgs"),
)
sealed interface ConnectionConfig {
    val type: String
}

sealed interface JdbcConnectionConfig : ConnectionConfig {
    // JDBC 通用连接字段：所有基于 JDBC 的数据库都共用这些信息。
    val host: String
    val port: Int
    val username: String
    val password: String
}

data class MysqlConnectionConfig(
    override val type: String = "mysql",
    override val host: String,
    override val port: Int,
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

data class PostgreSqlConnectionConfig(
    override val type: String = "postgresql",
    override val host: String,
    override val port: Int,
    // PostgreSQL 需要在连接阶段指定默认 database，所以这个字段只放在 PG 专属配置里。
    val database: String,
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

