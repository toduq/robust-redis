package dev.todaka.robustredis;

import dev.todaka.robustredis.protocol.*;

import java.util.concurrent.CompletableFuture;

/**
 * See https://redis.io/commands
 */
public interface RedisCommands extends CommandDispatcher {
    // https://redis.io/commands#cluster

    default CompletableFuture<String> clusterNodes() {
        final var input = new CommandInput(CommandName.CLUSTER).addArg("NODES");
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }

    // https://redis.io/commands#connection

    default CompletableFuture<String> echo(String message) {
        final var input = new CommandInput(CommandName.ECHO).addArg(message);
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }

    default CompletableFuture<String> ping() {
        final var input = new CommandInput(CommandName.PING);
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }

    // https://redis.io/commands#generic

    default CompletableFuture<String> del(String key) {
        final var input = new CommandInput(CommandName.DEL).addKey(key);
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }

    default CompletableFuture<Long> exists(String key) {
        final var input = new CommandInput(CommandName.EXISTS).addKey(key);
        return dispatchCommand(new RedisCommand<>(input, new LongCommandOutput()));
    }

    // https://redis.io/commands#string

    default CompletableFuture<Long> decr(String key) {
        final var input = new CommandInput(CommandName.DECR).addKey(key);
        return dispatchCommand(new RedisCommand<>(input, new LongCommandOutput()));
    }

    default CompletableFuture<String> get(String key) {
        final var input = new CommandInput(CommandName.GET).addKey(key);
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }

    default CompletableFuture<Long> incr(String key) {
        final var input = new CommandInput(CommandName.INCR).addKey(key);
        return dispatchCommand(new RedisCommand<>(input, new LongCommandOutput()));
    }

    default CompletableFuture<String> set(String key, String value) {
        final var input = new CommandInput(CommandName.SET).addKey(key).addArg(value);
        return dispatchCommand(new RedisCommand<>(input, new StringCommandOutput()));
    }
}
