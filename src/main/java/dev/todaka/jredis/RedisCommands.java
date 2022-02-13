package dev.todaka.jredis;

import java.util.concurrent.CompletableFuture;

/**
 * See https://redis.io/commands
 */
public interface RedisCommands extends CommandDispatcher {
    // === Connection Section ===

    default CompletableFuture<RedisResponse> ping() {
        return dispatchCommand("PING");
    }

    default CompletableFuture<RedisResponse> echo(String message) {
        return dispatchCommand("ECHO " + message);
    }

    // === Keys Section ===

    default CompletableFuture<RedisResponse> set(String key, String value) {
        return dispatchCommand("SET " + key + " " + value);
    }

    default CompletableFuture<RedisResponse> get(String key) {
        return dispatchCommand("GET " + key);
    }

    default CompletableFuture<RedisResponse> exists(String key) {
        return dispatchCommand("EXISTS " + key);
    }

    default CompletableFuture<RedisResponse> del(String key) {
        return dispatchCommand("DEL " + key);
    }

    // === Cluster Section ===

    default CompletableFuture<RedisResponse> clusterNodes() {
        return dispatchCommand("CLUSTER NODES");
    }
}
