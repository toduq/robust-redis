package dev.todaka.jredis.cluster;

import dev.todaka.jredis.NodeConnection;
import dev.todaka.jredis.RedisCommand;
import dev.todaka.jredis.RedisCommands;
import dev.todaka.jredis.RedisResponse;
import dev.todaka.jredis.connection.RedisURI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RedisClusterConnection implements RedisCommands {
    private final ClusterRouter clusterRouter;

    public RedisClusterConnection(RedisURI initialEndpoint) throws InterruptedException, ExecutionException {
        try (final var conn = NodeConnection.connect(initialEndpoint)) {
            final var clusterNodes = (RedisResponse.StringResponse) conn.clusterNodes().get();
            final var views = ClusterNodesParser.parse(clusterNodes.getBody());
            clusterRouter = new ClusterRouter(views);
        }
    }

    @Override
    public CompletableFuture<RedisResponse> dispatchCommand(RedisCommand redisCommand) {
        return clusterRouter
                .findOrEstablishConnection(redisCommand)
                .thenCompose(conn -> conn.dispatchCommand(redisCommand));
    }
}
