package org.fleur.srcbackend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/api/hello")
    fun hello(): Map<String, String> {
        return mapOf(
            "message" to "Hello from GraalVM Native Backend!",
            "status" to "ok",
            "runtime" to System.getProperty("java.vendor")
        )
    }
}
