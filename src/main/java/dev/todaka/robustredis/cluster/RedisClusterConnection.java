package dev.todaka.robustredis.cluster;

import dev.todaka.robustredis.NodeConnection;
import dev.todaka.robustredis.RedisCommand;
import dev.todaka.robustredis.RedisCommands;
import dev.todaka.robustredis.RedisResponse;
import dev.todaka.robustredis.connection.RedisURI;

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
