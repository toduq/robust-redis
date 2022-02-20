package dev.todaka.robustredis.cluster;

import dev.todaka.robustredis.NodeConnection;
import dev.todaka.robustredis.RedisCommands;
import dev.todaka.robustredis.connection.RedisURI;
import dev.todaka.robustredis.protocol.RedisCommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RedisClusterConnection implements RedisCommands {
    private final ClusterRouter clusterRouter;

    public RedisClusterConnection(RedisURI initialEndpoint) throws InterruptedException, ExecutionException {
        try (final var conn = NodeConnection.connect(initialEndpoint)) {
            final var clusterNodes = conn.clusterNodes().get();
            final var views = ClusterNodesParser.parse(clusterNodes);
            clusterRouter = new ClusterRouter(views);
        }
    }

    @Override
    public <R> CompletableFuture<R> dispatchCommand(RedisCommand<R> redisCommand) {
        return clusterRouter
                .findOrEstablishConnection(redisCommand)
                .thenCompose(conn -> conn.dispatchCommand(redisCommand));
    }
}
