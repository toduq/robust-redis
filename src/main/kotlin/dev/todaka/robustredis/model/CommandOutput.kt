package dev.todaka.robustredis.model

import dev.todaka.robustredis.exception.RedisProtocolException
import java.util.concurrent.CompletableFuture

abstract class CommandOutput<R> {
    val completableFuture = CompletableFuture<R>()

    open fun resolveString(result: String) =
        unsupported("resolveString is not supported for this command")

    open fun resolveLong(result: Long) =
        unsupported("resolveLong is not supported for this command")

    open fun resolveNull() =
        unsupported("resolveNull is not supported for this command")

    open fun reject(error: String) {
        completableFuture.completeExceptionally(RuntimeException(error))
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun unsupported(message: String) {
        completableFuture.completeExceptionally(RedisProtocolException(message))
    }
}

class StringCommandOutput : CommandOutput<String>() {
    override fun resolveString(result: String) {
        this.completableFuture.complete(result)
    }
}

class NullableStringCommandOutput : CommandOutput<String?>() {
    override fun resolveString(result: String) {
        this.completableFuture.complete(result)
    }

    override fun resolveNull() {
        this.completableFuture.complete(null)
    }
}

class LongCommandOutput : CommandOutput<Long>() {
    override fun resolveLong(result: Long) {
        this.completableFuture.complete(result)
    }
}
