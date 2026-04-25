package org.fleur.srcbackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.fleur.srcbackend.pojo.entity.Connection
import org.fleur.srcbackend.result.TulipeResult
import org.fleur.srcbackend.service.DataSourceService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/data-source")
@Tag(name = "Data Source", description = "数据源连接管理接口")
class DataSourceController(
    private val dataSourceService: DataSourceService,
) {

    @PostMapping("/connect")
    @Operation(summary = "测试并保存连接", description = "测试数据库连接可用性，成功后持久化连接信息并返回 connectionId")
    // 该接口用于测试连接，成功后会返回并持久化 connectionId。
    fun connect(@RequestBody request: Connection): TulipeResult<Long> {
        val connectionId = dataSourceService.connect(request)
        return TulipeResult.success(connectionId)
    }
}