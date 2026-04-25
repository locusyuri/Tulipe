package org.fleur.srcbackend.pojo.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class DbType(
    val code: String,
    val driverClassName: String,
) {
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("postgresql", "org.postgresql.Driver");

    // MySQL 可以直接连到 server；PostgreSQL 则需要明确指定 database。
    fun buildJdbcUrl(host: String, port: Int, database: String? = null): String {
        return when (this) {
            MYSQL -> "jdbc:mysql://$host:$port/"
            POSTGRESQL -> {
                require(!database.isNullOrBlank()) { "PostgreSQL 连接必须提供 database" }
                "jdbc:postgresql://$host:$port/$database"
            }
        }
    }

    @JsonValue
    fun toJsonValue(): String = code

    companion object {
        // 统一处理别名，避免在 Service 里散落字符串判断。
        @JsonCreator
        @JvmStatic
        fun from(input: String): DbType {
            return when (input.trim().lowercase()) {
                "mysql" -> MYSQL
                "postgresql", "postgres", "pg", "pgs" -> POSTGRESQL
                else -> throw IllegalArgumentException("暂不支持的数据库类型: $input")
            }
        }
    }
}
