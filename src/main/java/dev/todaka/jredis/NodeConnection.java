package dev.todaka.jredis;

import dev.todaka.jredis.connection.RedisURI;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

public class NodeConnection implements AutoCloseable, RedisCommands {
    private EventLoopGroup workerGroup;
    private Channel channel;

    public CompletableFuture<NodeConnection> connect(RedisURI endpoint) {
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);

        final var channelReadyFuture = new CompletableFuture<Channel>();
        final var connectionReadyFuture = new CompletableFuture<NodeConnection>();
        channelReadyFuture.whenComplete((channel, throwable) -> {
            if (throwable != null) {
                connectionReadyFuture.completeExceptionally(throwable);
                return;
            }
            this.channel = channel;
            connectionReadyFuture.complete(this);
        });

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new CommandHandler(channelReadyFuture));
            }
        });

        b.connect(endpoint.getHost(), endpoint.getPort());
        return connectionReadyFuture;
    }

    @Override
    public void close() throws InterruptedException {
        System.out.println("close");
        channel.close();
        channel.closeFuture().sync();
        workerGroup.shutdownGracefully().sync();
    }

    public CompletableFuture<RedisResponse> dispatchCommand(RedisCommand redisCommand) {
        System.out.println("execute " + redisCommand.command);
        channel.writeAndFlush(redisCommand);
        return redisCommand.response;
    }
}
