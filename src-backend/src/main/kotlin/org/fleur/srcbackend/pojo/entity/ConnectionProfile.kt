package org.fleur.srcbackend.pojo.entity

/**
 * SQLite 中用于持久化用户连接信息的实体。
 *
 * 说明：
 * - 该实体对应 connection_profile 表。
 * - MySQL 不需要 database_name 时，落库使用空字符串占位。
 * - passwordCiphertext 字段当前先沿用表结构名称，后续可替换为真正的加密实现。
 */
data class ConnectionProfile(
    val id: Long? = null,
    val profileName: String,
    val dbType: String,
    val host: String,
    val port: Int,
    val databaseName: String,
    val username: String,
    val passwordCiphertext: String?,
    val extraJson: String?,
)

