package dev.todaka.robustredis.connection

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import dev.todaka.robustredis.AbstractContainerBaseTest
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
class NodeConnectionTest : AbstractContainerBaseTest() {
    @Test
    fun test() {
        NodeConnection.connectAsync(standaloneRedisUri()).get().use { conn ->
            assertThat(conn.ping().get()).isEqualTo("PONG")
            assertThat(conn.ping().get()).isEqualTo("PONG")
            assertThat(conn.echo("HELLO").get()).isEqualTo("HELLO")
            assertThat(conn.exists("abc").get()).isEqualTo(0L)
            assertThat(conn.ping().get()).isEqualTo("PONG")
        }
    }

    @Test
    fun testIncrAndDecr() {
        val key = UUID.randomUUID().toString()
        NodeConnection.connect(standaloneRedisUri()).use { conn ->
            assertThat(conn.exists(key).get()).isEqualTo(0L)
            assertThat(conn.incr(key).get()).isEqualTo(1L)
            assertThat(conn.incr(key).get()).isEqualTo(2L)
            assertThat(conn.decr(key).get()).isEqualTo(1L)
            assertThat(conn.get(key).get()).isEqualTo("1")
            assertThat(conn.set(key, "3").get()).isEqualTo("OK")
            assertThat(conn.get(key).get()).isEqualTo("3")
            assertThat(conn.del(key).get()).isEqualTo(1L)
            assertThat(conn.get(key).get()).isEqualTo(null)
        }
    }

    @Test
    fun testConnectionTimeout() {
        val started = System.currentTimeMillis()
        try {
            // sample address
            NodeConnection.connect(RedisURI("192.0.2.0", 10002)).use { conn -> conn.ping() }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - started
            assertThat(elapsed in 1001..2999).isTrue()
        }
    }
}
