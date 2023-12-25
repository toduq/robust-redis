package dev.todaka.robustredis.cluster

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.cluster.ClusterNodesParser.parse
import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI
import dev.todaka.robustredis.model.RedisCommand
import java.util.concurrent.CompletableFuture

// TODO: close
class RedisClusterConnection(
    initialEndpoint: RedisURI,
    /** Docker環境などでRedisが認識しているportと実際に接続するportが異なる場合などに、それを変換するための仕組み */
    private val clusterViewTranslator: (view: List<ClusterNodeView>) -> List<ClusterNodeView> = { it },
) : RedisCommands {
    private var clusterRouter: ClusterRouter

    init {
        NodeConnection.connect(initialEndpoint).use { conn ->
            val clusterNodes = conn.clusterNodes().get()
            val view = parse(clusterNodes)
            clusterRouter = ClusterRouter(clusterViewTranslator(view))
        }
    }

    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        return clusterRouter
            .findOrEstablishConnection(redisCommand)
            .thenCompose { it.dispatchCommand(redisCommand) }
    }
}
