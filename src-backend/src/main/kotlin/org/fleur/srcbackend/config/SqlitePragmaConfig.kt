package org.fleur.srcbackend.config

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class SqlitePragmaConfig {

    @Bean
    fun sqlitePragmaInitializer(jdbcTemplate: JdbcTemplate): ApplicationRunner = ApplicationRunner {
        // 统一在连接建立后设置 SQLite 关键参数，提升并发稳定性。
        jdbcTemplate.execute("PRAGMA journal_mode=WAL")
        jdbcTemplate.execute("PRAGMA busy_timeout=5000")
        jdbcTemplate.execute("PRAGMA foreign_keys=ON")
        jdbcTemplate.execute("PRAGMA synchronous=NORMAL")
    }
}
