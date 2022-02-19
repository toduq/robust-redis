package dev.todaka.jredis.cluster;

import dev.todaka.jredis.NodeConnection;
import dev.todaka.jredis.RedisCommand;
import dev.todaka.jredis.RedisRouter;
import dev.todaka.jredis.connection.RedisURI;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClusterRouter implements RedisRouter {
    /**
     * SlotId to RedisURI mapping built by CLUSTER NODES response.
     * Contains exactly 16,384 entries.
     */
    private List<RedisURI> slotIdToNode;

    /**
     * RedisURI to NodeConnection mapping.
     */
    private final Map<RedisURI, CompletableFuture<NodeConnection>> connectionPool = new HashMap<>();

    public ClusterRouter(List<ClusterNodeView> views) {
        final var newSlotIdToNode = new ArrayList<RedisURI>(Collections.nCopies(16384, null));
        for (final var view : views) {
            final var uri = new RedisURI(view.getIp(), view.getPort());
            for (final var slotRange : view.getSlots()) {
                for (int s = slotRange.getFrom(); s <= slotRange.getTo(); s++) {
                    newSlotIdToNode.set(s, uri);
                }
            }
        }
        slotIdToNode = newSlotIdToNode;
    }

    public CompletableFuture<NodeConnection> findOrEstablishConnection(RedisCommand command) {
        int slot;
        if (command.keys.isEmpty()) {
            // use slot = 0 for command without key
            slot = 0;
        } else {
            slot = CRC16.crc16(command.keys.get(0).getBytes(StandardCharsets.UTF_8)) % 16384;
        }
        final RedisURI uri = slotIdToNode.get(slot);

        synchronized (connectionPool) {
            return connectionPool.computeIfAbsent(
                    uri, notUsed -> NodeConnection.connectAsync(uri));
        }
    }
}
