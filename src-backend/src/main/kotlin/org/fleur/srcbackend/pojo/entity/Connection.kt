package org.fleur.srcbackend.pojo.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class ConnectionRequest(
    // 用户自定义连接名称。
    val name: String,
    // 用户分组信息。
    val group: String,
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
    val host: String
    val port: Int
    val database: String
    val username: String
    val password: String
}

data class MysqlConnectionConfig(
    override val type: String = "mysql",
    override val host: String,
    override val port: Int,
    override val database: String,
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

data class PostgreSqlConnectionConfig(
    override val type: String = "postgresql",
    override val host: String,
    override val port: Int,
    override val database: String,
    override val username: String,
    override val password: String,
) : JdbcConnectionConfig

