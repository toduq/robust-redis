package dev.todaka.robustredis.performance;

import dev.todaka.robustredis.NodeConnection;
import dev.todaka.robustredis.connection.RedisURI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RedisStandalonePerformanceTest {
    @Test
    public void test() throws InterruptedException, ExecutionException {
        try (var nodeConn = NodeConnection.connect(new RedisURI("127.0.0.1", 10001))) {
            final var futures = new ArrayList<CompletableFuture<Long>>();
            for (int i = 0; i < 100_000; i++) {
                futures.add(nodeConn.incr(Long.toString(System.currentTimeMillis())));
            }
            for (CompletableFuture<Long> future : futures) {
                future.get();
            }
        }
    }
}
