package org.fleur.srcbackend.pojo.enums

enum class DbType(
    val code: String,
    val driverClassName: String,
) {
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("postgresql", "org.postgresql.Driver");

    fun buildJdbcUrl(host: String, port: Int, database: String): String {
        return when (this) {
            MYSQL -> "jdbc:mysql://$host:$port/$database"
            POSTGRESQL -> "jdbc:postgresql://$host:$port/$database"
        }
    }

    companion object {
        // 统一处理别名，避免在 Service 里散落字符串判断。
        fun from(input: String): DbType {
            return when (input.trim().lowercase()) {
                "mysql" -> MYSQL
                "postgresql", "postgres", "pg", "pgs" -> POSTGRESQL
                else -> throw IllegalArgumentException("暂不支持的数据库类型: $input")
            }
        }
    }
}

