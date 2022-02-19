package dev.todaka.robustredis;

import java.util.concurrent.CompletableFuture;

public interface RedisRouter {
    CompletableFuture<NodeConnection> findOrEstablishConnection(RedisCommand command);
}
