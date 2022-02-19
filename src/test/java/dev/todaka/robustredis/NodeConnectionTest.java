package dev.todaka.robustredis;

import dev.todaka.robustredis.connection.RedisURI;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeConnectionTest {
    @Test
    public void test() throws InterruptedException, ExecutionException {
        try (var nodeConn = NodeConnection.connect(new RedisURI("127.0.0.1", 10001))) {
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
            assertThat(nodeConn.exists("abc").get()).isEqualTo(new RedisResponse.LongResponse(0));
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
        }
    }

    @Test
    public void testConnectionTimeout() {
        final var started = System.currentTimeMillis();
        try (var nodeConn = NodeConnection.connect(new RedisURI("10.255.255.254", 10002))) {
            nodeConn.ping();
        } catch (Exception e) {
            final var elapsed = System.currentTimeMillis() - started;
            assertThat(1000 < elapsed && elapsed < 3000).isTrue();
        }
    }
}
