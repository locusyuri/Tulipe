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
    // 该接口仅用于测试连接，成功时不返回业务数据。
    fun connect(@RequestBody request: Connection): TulipeResult<Nothing> {
        dataSourceService.connect(request)
        return TulipeResult.success()
    }
}