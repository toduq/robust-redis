package dev.todaka.robustredis.cluster

import dev.todaka.robustredis.cluster.CRC16.crc16
import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI
import dev.todaka.robustredis.exception.RedisInvalidClusterException
import dev.todaka.robustredis.model.RedisCommand
import java.util.concurrent.CompletableFuture

class ClusterRouter(views: List<ClusterNodeView>) {
    /**
     * SlotId to RedisURI mapping built by CLUSTER NODES response.
     * Contains exactly 16,384 entries.
     */
    private val slotIdToNode: List<RedisURI>

    /**
     * RedisURI to NodeConnection mapping.
     */
    private val connectionPool: MutableMap<RedisURI, CompletableFuture<NodeConnection>> = HashMap()

    init {
        val uris = views.flatMap { view ->
            view.slots.flatMap { slot ->
                (slot.from..slot.to).map { slotId -> slotId to RedisURI(view.ip, view.port) }
            }
        }
        if (uris.size != 16384) {
            throw RedisInvalidClusterException("Invalid CLUSTER NODES response")
        }

        slotIdToNode = uris.sortedBy { it.first }.map { it.second }
    }

    fun findOrEstablishConnection(command: RedisCommand<*>): CompletableFuture<NodeConnection> {
        val slot = command.commandInput.keys?.firstOrNull()
            ?.let { crc16(it.toByteArray(Charsets.UTF_8)) % 16384 }
            ?: 0 // use slot = 0 for command without key

        val uri = slotIdToNode[slot]
        synchronized(connectionPool) {
            return connectionPool.computeIfAbsent(uri) {
                NodeConnection.connectAsync(uri)
            }
        }
    }
}
