package dev.todaka.robustredis

data class RedisURI(
    val host: String,
    val port: Int,
    val password: String? = null,
)
