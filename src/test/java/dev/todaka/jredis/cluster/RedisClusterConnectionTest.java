package dev.todaka.jredis.cluster;

import dev.todaka.jredis.RedisResponse;
import dev.todaka.jredis.connection.RedisURI;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisClusterConnectionTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        final var conn = new RedisClusterConnection(new RedisURI("127.0.0.1", 45401));
        for (int i = 0; i < 100; i++) {
            assertThat(conn.exists("non_exists_key_" + i).get())
                    .isEqualTo(new RedisResponse.LongResponse(0));
        }
    }
}
