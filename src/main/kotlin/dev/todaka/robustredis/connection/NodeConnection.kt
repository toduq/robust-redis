package dev.todaka.robustredis.connection

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.model.RedisCommand
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.CompletableFuture

/**
 * 単一のnodeに対してコマンドを送信するクラス
 *
 * 指定されたRedisURIに対してconnectionを確立し、コマンドを送信する。
 * closeされるか、コマンドの送信に失敗するとconnectionは切断される。
 */
class NodeConnection : AutoCloseable, RedisCommands {
    private lateinit var workerGroup: EventLoopGroup
    private lateinit var channel: Channel

    override fun close() {
        println("on close NodeConnection")
        if (this::channel.isInitialized) {
            channel.close()
            channel.closeFuture()?.sync()
        }
        if (this::workerGroup.isInitialized) {
            workerGroup.shutdownGracefully()?.sync()
        }
    }

    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        channel.writeAndFlush(redisCommand)
        return redisCommand.commandOutput.completableFuture
    }

    companion object {
        fun connect(endpoint: RedisURI): NodeConnection {
            return connectAsync(endpoint).get()
        }

        fun connectAsync(endpoint: RedisURI): CompletableFuture<NodeConnection> {
            val conn = NodeConnection().also {
                it.workerGroup = NioEventLoopGroup()
            }

            val bootstrap = Bootstrap()
                .group(conn.workerGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .option(ChannelOption.SO_KEEPALIVE, true)

            val nodeConnectionReadyFuture = CompletableFuture<NodeConnection>()
            val handlerActivatedFuture = CompletableFuture<Channel>()
            handlerActivatedFuture.whenComplete { channel, throwable ->
                if (throwable != null) {
                    nodeConnectionReadyFuture.completeExceptionally(throwable)
                } else {
                    conn.channel = channel
                    nodeConnectionReadyFuture.complete(conn)
                }
            }
            bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(CommandCodecHandler())
                        .addLast(ConnectionStateHandler(handlerActivatedFuture))
                }
            })

            val connectFuture = bootstrap.connect(endpoint.host, endpoint.port)
            connectFuture.addListener {
                if (!connectFuture.isSuccess) {
                    if (connectFuture.cause() != null) {
                        // Completed with failure
                        nodeConnectionReadyFuture.completeExceptionally(connectFuture.cause())
                    } else {
                        // Completed by cancellation
                        nodeConnectionReadyFuture.completeExceptionally(RuntimeException("initialization canceled"))
                    }
                }
            }

            return nodeConnectionReadyFuture
        }
    }
}
