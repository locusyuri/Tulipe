package org.fleur.srcbackend.controller

import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.result.TulipeResult
import org.fleur.srcbackend.service.DataSourceService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/data-source")
class DataSourceController(
    private val dataSourceService: DataSourceService,
) {

    @PostMapping("/connect")
    // 该接口用于测试连接，成功后会返回并持久化 connectionId。
    fun connect(@RequestBody request: Connection): TulipeResult<Long> {
        val connectionId = dataSourceService.connect(request)
        return TulipeResult.success(connectionId)
    }
}