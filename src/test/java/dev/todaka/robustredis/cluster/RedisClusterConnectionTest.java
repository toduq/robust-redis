package dev.todaka.robustredis.cluster;

import dev.todaka.robustredis.connection.RedisURI;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisClusterConnectionTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        final var conn = new RedisClusterConnection(new RedisURI("127.0.0.1", 11001));
        for (int i = 0; i < 100; i++) {
            assertThat(conn.exists("non_exists_key_" + i).get()).isEqualTo(0L);
        }
    }
}
