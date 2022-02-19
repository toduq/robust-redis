package dev.todaka.robustredis;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.emptyList;

public interface CommandDispatcher {
    default CompletableFuture<RedisResponse> dispatchCommand(String command) {
        return dispatchCommand(new RedisCommand(command, emptyList(), emptyList()));
    }

    default CompletableFuture<RedisResponse> dispatchCommand(String command, List<String> keys) {
        return dispatchCommand(new RedisCommand(command, keys, emptyList()));
    }

    default CompletableFuture<RedisResponse> dispatchCommand(String command, List<String> keys, List<String> args) {
        return dispatchCommand(new RedisCommand(command, keys, args));
    }

    CompletableFuture<RedisResponse> dispatchCommand(RedisCommand redisCommand);
}
