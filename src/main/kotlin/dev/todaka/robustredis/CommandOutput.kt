package dev.todaka.robustredis

import java.util.concurrent.CompletableFuture

abstract class CommandOutput<R> {
    val completableFuture = CompletableFuture<R>()

    open fun setResult(result: String) {
        completableFuture.completeExceptionally(
            UnsupportedOperationException(
                "setResult(String) is not supported for this command"
            )
        )
    }

    open fun setResult(result: Long) {
        completableFuture.completeExceptionally(
            UnsupportedOperationException(
                "setResult(long) is not supported for this command"
            )
        )
    }

    open fun setError(error: String) {
        completableFuture.completeExceptionally(RuntimeException(error))
    }
}

class StringCommandOutput : CommandOutput<String>() {
    override fun setResult(result: String) {
        this.completableFuture.complete(result)
    }
}


class LongCommandOutput : CommandOutput<Long>() {
    override fun setResult(result: Long) {
        this.completableFuture.complete(result)
    }
}
