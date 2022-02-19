package dev.todaka.robustredis;

import java.util.concurrent.CompletableFuture;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * See https://redis.io/commands
 */
public interface RedisCommands extends CommandDispatcher {
    // === Connection Section ===

    default CompletableFuture<RedisResponse> ping() {
        return dispatchCommand("PING");
    }

    default CompletableFuture<RedisResponse> echo(String message) {
        return dispatchCommand("ECHO", emptyList(), singletonList(message));
    }

    // === Keys Section ===

    default CompletableFuture<RedisResponse> set(String key, String value) {
        return dispatchCommand("SET", singletonList(key), singletonList(value));
    }

    default CompletableFuture<RedisResponse> get(String key) {
        return dispatchCommand("GET", singletonList(key));
    }

    default CompletableFuture<RedisResponse> exists(String key) {
        return dispatchCommand("EXISTS", singletonList(key));
    }

    default CompletableFuture<RedisResponse> del(String key) {
        return dispatchCommand("DEL", singletonList(key));
    }

    default CompletableFuture<RedisResponse> incr(String key) {
        return dispatchCommand("INCR", singletonList(key));
    }

    // === Cluster Section ===

    default CompletableFuture<RedisResponse> clusterNodes() {
        return dispatchCommand("CLUSTER", singletonList("NODES"));
    }
}
