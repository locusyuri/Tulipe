package org.fleur.srcbackend.result


/**
 * @param success 是否成功
 * @param message 提示信息
 * @param data 返回数据
 */
data class TulipeResult<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
){
    companion object{
        /**
         * 响应成功
         */
        fun <T> success(data: T): TulipeResult<T> = TulipeResult(true, "success", data)

        /**
         * 响应成功 (无数据)
         */
        fun <T> success(): TulipeResult<T> = TulipeResult(true, "success", null)

        /**
         * 响应失败
         */
        fun <T> failure(message: String): TulipeResult<T> = TulipeResult(false, message, null)
    }
}

