package dev.todaka.jredis;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeConnectionTest {
    @Test
    public void test() throws InterruptedException, ExecutionException {
        try (var nodeConn = new NodeConnection()) {
            nodeConn.connect().get();
            assertThat(nodeConn.ping().get()).isEqualTo("PONG");
            assertThat(nodeConn.ping().get()).isEqualTo("PONG");
            assertThat(nodeConn.exists("abc").get()).isEqualTo("0");
            assertThat(nodeConn.ping().get()).isEqualTo("PONG");
        }
    }
}
