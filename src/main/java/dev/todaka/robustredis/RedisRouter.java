package dev.todaka.robustredis;

import dev.todaka.robustredis.protocol.RedisCommand;

import java.util.concurrent.CompletableFuture;

public interface RedisRouter {
    CompletableFuture<NodeConnection> findOrEstablishConnection(RedisCommand<?> command);
}
