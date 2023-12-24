package dev.todaka.robustredis

import java.util.concurrent.CompletableFuture


interface RedisCommands : CommandDispatcher {
    // https://redis.io/commands#cluster
    fun clusterNodes(): CompletableFuture<String> {
        val input = CommandInput(CommandName.CLUSTER).addArg("NODES")
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    // https://redis.io/commands#connection
    fun echo(message: String?): CompletableFuture<String> {
        val input = CommandInput(CommandName.ECHO).addArg(message!!)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    fun ping(): CompletableFuture<String> {
        val input = CommandInput(CommandName.PING)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    // https://redis.io/commands#generic
    fun del(key: String?): CompletableFuture<String> {
        val input = CommandInput(CommandName.DEL).addKey(key!!)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    fun exists(key: String?): CompletableFuture<Long> {
        val input = CommandInput(CommandName.EXISTS).addKey(key!!)
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    // https://redis.io/commands#string
    fun decr(key: String?): CompletableFuture<Long> {
        val input = CommandInput(CommandName.DECR).addKey(key!!)
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    operator fun get(key: String?): CompletableFuture<String> {
        val input = CommandInput(CommandName.GET).addKey(key!!)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }

    fun incr(key: String?): CompletableFuture<Long> {
        val input = CommandInput(CommandName.INCR).addKey(key!!)
        return dispatchCommand(RedisCommand(input, LongCommandOutput()))
    }

    operator fun set(key: String?, value: String?): CompletableFuture<String> {
        val input = CommandInput(CommandName.SET).addKey(key!!).addArg(value!!)
        return dispatchCommand(RedisCommand(input, StringCommandOutput()))
    }
}
