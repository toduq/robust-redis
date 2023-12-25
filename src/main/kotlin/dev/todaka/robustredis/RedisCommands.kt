package dev.todaka.robustredis

import dev.todaka.robustredis.model.*
import java.util.concurrent.CompletableFuture

interface CommandDispatcher {
    fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R>
}

interface RedisCommands : CommandDispatcher {
    // https://redis.io/commands#cluster
    fun clusterNodes(): CompletableFuture<String> {
        val input = CommandInput(CommandName.CLUSTER, args = listOf("NODES"))
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    // https://redis.io/commands#connection
    fun echo(message: String): CompletableFuture<String> {
        val input = CommandInput(CommandName.ECHO, args = listOf(message))
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    fun ping(): CompletableFuture<String> {
        val input = CommandInput(CommandName.PING)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    // https://redis.io/commands#generic
    fun del(key: String): CompletableFuture<Long> {
        val input = CommandInput(CommandName.DEL, keys = listOf(key))
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    fun exists(key: String): CompletableFuture<Long> {
        val input = CommandInput(CommandName.EXISTS, keys = listOf(key))
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    // https://redis.io/commands#string
    fun decr(key: String): CompletableFuture<Long> {
        val input = CommandInput(CommandName.DECR, keys = listOf(key))
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    fun get(key: String): CompletableFuture<String?> {
        val input = CommandInput(CommandName.GET, keys = listOf(key))
        return dispatchCommand(RedisCommand(input, NullableStringCommandOutput()))
    }

    fun incr(key: String): CompletableFuture<Long> {
        val input = CommandInput(CommandName.INCR, keys = listOf(key))
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    fun set(key: String, value: String): CompletableFuture<String> {
        val input = CommandInput(CommandName.SET, keys = listOf(key), args = listOf(value))
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }
}
