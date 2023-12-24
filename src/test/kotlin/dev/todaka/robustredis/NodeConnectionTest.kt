package dev.todaka.robustredis

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class NodeConnectionTest {
    @Test
    fun test() {
        NodeConnection.connect(RedisURI("127.0.0.1", 6379)).use { nodeConn ->
            assertThat(nodeConn.ping().get()).isEqualTo("PONG")
            assertThat(nodeConn.ping().get()).isEqualTo("PONG")
            assertThat(nodeConn.exists("abc").get()).isEqualTo(0L)
            assertThat(nodeConn.ping().get()).isEqualTo("PONG")
        }
    }

    @Test
    fun testConnectionTimeout() {
        val started = System.currentTimeMillis()
        try {
            NodeConnection.connect(RedisURI("10.255.255.254", 10002)).use { nodeConn -> nodeConn.ping() }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - started
            assertThat(elapsed in 1001..2999).isTrue()
        }
    }
}
