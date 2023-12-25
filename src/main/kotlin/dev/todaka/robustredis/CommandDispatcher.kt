package dev.todaka.robustredis

import java.util.concurrent.CompletableFuture

interface CommandDispatcher {
    fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R>
}
