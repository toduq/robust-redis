package dev.todaka.robustredis.connection

data class RedisURI(
    val host: String,
    val port: Int,
    val password: String? = null,
)
