package org.fleur.srcbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.connection-pool")
class ConnectionPoolProperties {
    // 动态连接池最大连接数。
    var maximumPoolSize: Int = 10

    // 最小空闲连接数。
    var minimumIdle: Int = 1

    // 获取连接超时时间（毫秒）。
    var connectionTimeout: Long = 5_000

    // 连接有效性校验超时时间（毫秒）。
    var validationTimeout: Long = 2_000

    // 空闲连接最大存活时间（毫秒）。
    var idleTimeout: Long = 600_000

    // 连接最大生命周期（毫秒）。
    var maxLifetime: Long = 1_800_000

    // 连接探活 SQL。
    var connectionTestQuery: String = "SELECT 1"
}

