package org.fleur.srcbackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.fleur.srcbackend.result.TulipeResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Test", description = "测试与联通性检查接口")
class TestController {
    @GetMapping("/api/hello")
    @Operation(summary = "健康问候", description = "返回基础运行信息，用于调试后端可达性")
    fun hello(): TulipeResult<String> {
        return TulipeResult.success(data = "Hello from GraalVM Native Backend!")
    }
}