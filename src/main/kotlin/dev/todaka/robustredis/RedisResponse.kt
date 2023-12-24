package dev.todaka.robustredis


interface RedisResponse

data class StringResponse(val body: String) : RedisResponse

data class ErrorResponse(val body: String) : RedisResponse

data class LongResponse(val body: Long) : RedisResponse

object NullResponse : RedisResponse
