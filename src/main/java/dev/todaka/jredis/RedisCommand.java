package dev.todaka.jredis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RedisCommand {
    public final String command;
    public final List<String> keys;
    public final List<String> args;
    public final CompletableFuture<RedisResponse> response;

    public RedisCommand(String command) {
        this(command, Collections.emptyList(), Collections.emptyList());
    }

    public RedisCommand(String command, List<String> keys) {
        this(command, keys, Collections.emptyList());
    }

    public RedisCommand(String command, List<String> keys, List<String> args) {
        this.command = command;
        this.keys = keys;
        this.args = args;
        this.response = new CompletableFuture<>();
    }

    public void writeToByteBuf(ByteBuf buf) {
        final var len = 1 + keys.size() + args.size();
        ByteBufUtil.writeAscii(buf, '*' + Integer.toString(len) + "\r\n");
        writeBulkString(buf, command);
        for (final var key : keys) {
            writeBulkString(buf, key);
        }
        for (final var arg : args) {
            writeBulkString(buf, arg);
        }
    }

    private void writeBulkString(ByteBuf buf, String str) {
        final var len = ByteBufUtil.utf8Bytes(str);
        ByteBufUtil.writeAscii(buf, '$' + Integer.toString(len) + "\r\n");
        ByteBufUtil.writeUtf8(buf, str);
        ByteBufUtil.writeAscii(buf, "\r\n");
    }
}
