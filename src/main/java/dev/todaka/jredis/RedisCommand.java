package dev.todaka.jredis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;

public class RedisCommand {
    final String request;
    final CompletableFuture<RedisResponse> response;

    public RedisCommand(String request) {
        this.request = request;
        this.response = new CompletableFuture<>();
    }

    public ByteBuf toByteBuf() {
        return Unpooled.copiedBuffer(request, CharsetUtil.UTF_8);
    }
}
