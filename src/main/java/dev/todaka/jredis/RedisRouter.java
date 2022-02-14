package dev.todaka.jredis;

import java.util.concurrent.CompletableFuture;

public interface RedisRouter {
    CompletableFuture<NodeConnection> findOrEstablishConnection(RedisCommand command);
}
