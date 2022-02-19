package dev.todaka.robustredis;

import dev.todaka.robustredis.connection.RedisURI;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NodeConnection implements AutoCloseable, RedisCommands {
    private EventLoopGroup workerGroup;
    private Channel channel;

    private NodeConnection() {
    }

    public static NodeConnection connect(RedisURI endpoint) {
        try {
            return connectAsync(endpoint).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("failed to connect", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("failed to connect", e);
        }
    }

    public static CompletableFuture<NodeConnection> connectAsync(RedisURI endpoint) {
        final var conn = new NodeConnection();

        conn.workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap()
                .group(conn.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .option(ChannelOption.SO_KEEPALIVE, true);

        final var nodeConnectionReadyFuture = new CompletableFuture<NodeConnection>();

        final var handlerActivatedFuture = new CompletableFuture<Channel>();
        handlerActivatedFuture.whenComplete((channel, throwable) -> {
            if (throwable != null) {
                nodeConnectionReadyFuture.completeExceptionally(throwable);
                return;
            }
            conn.channel = channel;
            nodeConnectionReadyFuture.complete(conn);
        });

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new CommandHandler(handlerActivatedFuture));
            }
        });

        final var connectFuture = b.connect(endpoint.getHost(), endpoint.getPort());
        connectFuture.addListener((_void) -> {
            if (!connectFuture.isSuccess()) {
                if (connectFuture.cause() != null) {
                    // Completed with failure
                    nodeConnectionReadyFuture.completeExceptionally(connectFuture.cause());
                } else {
                    // Completed by cancellation
                    nodeConnectionReadyFuture.completeExceptionally(new RuntimeException("initialization canceled"));
                }
            }
        });
        return nodeConnectionReadyFuture;
    }

    @Override
    public void close() throws InterruptedException {
        System.out.println("close");
        if (channel != null) {
            channel.close();
            channel.closeFuture().sync();
        }
        workerGroup.shutdownGracefully().sync();
    }

    public CompletableFuture<RedisResponse> dispatchCommand(RedisCommand redisCommand) {
        // System.out.println("execute " + redisCommand.command);
        channel.writeAndFlush(redisCommand);
        return redisCommand.response;
    }
}
