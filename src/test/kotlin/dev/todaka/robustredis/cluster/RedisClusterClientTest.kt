package dev.todaka.robustredis.cluster

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.todaka.robustredis.AbstractContainerBaseTest
import dev.todaka.robustredis.connection.NodeConnection
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test

class RedisClusterClientTest : AbstractContainerBaseTest() {
    @Test
    fun test() {
        // Wait for cluster to be ready
        await().until {
            NodeConnection.connect(clusterRedisUri()).use { conn ->
                val clusterNodes = conn.clusterNodes().get()
                val views = ClusterNodesParser.parse(clusterNodes)
                views.size == 6
            }
        }
        val conn = RedisClusterClient(
            clusterRedisUri(),
            clusterViewTranslator = { views ->
                views.map { view ->
                    view.copy(
                        ip = redisClusterContainer.host,
                        port = redisClusterContainer.getMappedPort(view.port),
                    )
                }
            }
        )
        for (i in 0..99) {
            assertThat(conn.exists("non_exists_key_$i").get()).isEqualTo(0L)
        }
    }
}
