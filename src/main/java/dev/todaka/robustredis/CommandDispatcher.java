package dev.todaka.robustredis;

import dev.todaka.robustredis.protocol.RedisCommand;

import java.util.concurrent.CompletableFuture;

public interface CommandDispatcher {
    <R> CompletableFuture<R> dispatchCommand(RedisCommand<R> redisCommand);
}
