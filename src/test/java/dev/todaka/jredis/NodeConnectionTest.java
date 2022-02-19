package dev.todaka.jredis;

import dev.todaka.jredis.connection.RedisURI;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeConnectionTest {
    @Test
    public void test() throws InterruptedException, ExecutionException {
        try (var nodeConn = new NodeConnection()) {
            nodeConn.connect(new RedisURI("127.0.0.1", 10001)).get();
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
            assertThat(nodeConn.exists("abc").get()).isEqualTo(new RedisResponse.LongResponse(0));
            assertThat(nodeConn.ping().get()).isEqualTo(new RedisResponse.StringResponse("PONG"));
        }
    }

    @Test
    @Disabled
    @Timeout(5)
    public void testConnectionTimeout() throws InterruptedException, ExecutionException {
        try (var nodeConn = new NodeConnection()) {
            nodeConn.connect(new RedisURI("127.0.0.1", 10002)).get();
        }
    }
}
