package org.fleur.srcbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SrcBackendApplication

fun main(args: Array<String>) {
    runApplication<SrcBackendApplication>(*args)
}
