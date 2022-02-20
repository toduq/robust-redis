package dev.todaka.robustredis.protocol;

import java.util.concurrent.CompletableFuture;

public abstract class CommandOutput<R> {
    protected CompletableFuture<R> future = new CompletableFuture<>();

    public CompletableFuture<R> getCompletableFuture() {
        return future;
    }
    
    public void setResult(String result) {
        future.completeExceptionally(new UnsupportedOperationException(
                "setResult(String) is not supported for this command"));
    }

    public void setResult(long result) {
        future.completeExceptionally(new UnsupportedOperationException(
                "setResult(long) is not supported for this command"));
    }

    public void setError(String error) {
        future.completeExceptionally(new RuntimeException(error));
    }
}
