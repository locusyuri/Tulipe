package org.fleur.srcbackend.pojo.dto

data class ConnectionRequest(
    val dbType: String,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
)

