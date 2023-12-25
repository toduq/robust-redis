package dev.todaka.robustredis.standalone

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.todaka.robustredis.AbstractContainerBaseTest
import org.junit.jupiter.api.Test
import java.util.*

class RedisStandaloneClientTest : AbstractContainerBaseTest() {
    @Test
    fun test() {
        RedisStandaloneClient(standaloneRedisUri()).use { client ->
            assertThat(client.ping().get()).isEqualTo("PONG")
            assertThat(client.ping().get()).isEqualTo("PONG")
            assertThat(client.echo("HELLO").get()).isEqualTo("HELLO")
            assertThat(client.exists("abc").get()).isEqualTo(0L)
            assertThat(client.ping().get()).isEqualTo("PONG")
        }
    }

    @Test
    fun testIncrAndDecr() {
        val key = UUID.randomUUID().toString()
        RedisStandaloneClient(standaloneRedisUri()).use { conn ->
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
}
