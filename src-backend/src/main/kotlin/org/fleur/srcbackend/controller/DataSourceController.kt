package org.fleur.srcbackend.controller

import org.fleur.srcbackend.pojo.dto.ConnectionRequest
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
    fun connect(@RequestBody request: ConnectionRequest): TulipeResult<Map<String, Any>> {
        val result = dataSourceService.connect(request)
        return TulipeResult.success(result)
    }
}