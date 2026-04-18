package org.fleur.srcbackend.exception

import org.fleur.srcbackend.result.TulipeResult
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): TulipeResult<Nothing> {
        return TulipeResult.failure(ex.message ?: "参数错误")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): TulipeResult<Nothing> {
        return TulipeResult.failure(ex.message ?: "服务器异常")
    }
}

