package dev.todaka.robustredis


sealed interface RedisResponse

data class StringResponse(val body: String) : RedisResponse

data class ErrorResponse(val body: String) : RedisResponse

data class LongResponse(val body: Long) : RedisResponse

data object NullResponse : RedisResponse
