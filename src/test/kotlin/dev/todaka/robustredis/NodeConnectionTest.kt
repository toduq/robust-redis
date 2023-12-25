package dev.todaka.robustredis

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class NodeConnectionTest : AbstractContainerBaseTest() {
    @Test
    fun test() {
        NodeConnection.connect(redisUri()).use { nodeConn ->
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
            // sample address
            NodeConnection.connect(RedisURI("192.0.2.0", 10002)).use { nodeConn -> nodeConn.ping() }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - started
            assertThat(elapsed in 1001..2999).isTrue()
        }
    }
}
