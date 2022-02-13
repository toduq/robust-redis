package dev.todaka.jredis;

import java.util.concurrent.CompletableFuture;

public interface CommandDispatcher {
    CompletableFuture<RedisResponse> dispatchCommand(String command);
}
