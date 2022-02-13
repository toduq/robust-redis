package dev.todaka.jredis;

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

    public CompletableFuture<Void> connect(String host, int port) {
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);

        final var channelReadyFuture = new CompletableFuture<Channel>();
        final var connectionReadyFuture = new CompletableFuture<Void>();
        channelReadyFuture.whenComplete((channel, throwable) -> {
            if (throwable != null) {
                connectionReadyFuture.completeExceptionally(throwable);
                return;
            }
            this.channel = channel;
            connectionReadyFuture.complete(null);
        });

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new CommandHandler(channelReadyFuture));
            }
        });

        b.connect(host, port);
        return connectionReadyFuture;
    }

    @Override
    public void close() throws InterruptedException {
        System.out.println("close");
        channel.close();
        channel.closeFuture().sync();
        workerGroup.shutdownGracefully().sync();
    }

    public CompletableFuture<RedisResponse> dispatchCommand(String command) {
        System.out.println("execute " + command);
        final var redisCommand = new RedisCommand(command + "\r\n");
        channel.writeAndFlush(redisCommand);
        return redisCommand.response;
    }
}
