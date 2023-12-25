package dev.todaka.robustredis.connection

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.exception.RedisAlreadyClosedException
import dev.todaka.robustredis.exception.RedisInitializationCanceledException
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
import java.util.concurrent.atomic.AtomicReference

/**
 * 単一のnodeに対してコマンドを送信するクラス
 *
 * 指定されたRedisURIに対してconnectionを確立し、コマンドを送信する。
 * closeされるか、コマンドの送信に失敗するとconnectionは切断される。
 *
 * 再接続やcommandのretry、handshake等の処理は行わない。
 * handshakeは、connectAsyncのCompletableFutureを使って呼び出し側で行うこと。
 */
class NodeConnection : AutoCloseable, RedisCommands {
    val status = AtomicReference(ConnectionStatus.INITIALIZING)
    private lateinit var workerGroup: EventLoopGroup
    private lateinit var channel: Channel

    override fun close() {
        println("on close NodeConnection")
        status.set(ConnectionStatus.CLOSED)
        if (this::channel.isInitialized) {
            channel.close()
            channel.closeFuture()?.sync()
        }
        if (this::workerGroup.isInitialized) {
            workerGroup.shutdownGracefully()?.sync()
        }
    }

    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        if (status.get() == ConnectionStatus.ACTIVE) {
            channel.writeAndFlush(redisCommand)
        } else {
            redisCommand.commandOutput.completableFuture.completeExceptionally(
                RedisAlreadyClosedException("connection already closed")
            )
        }

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
            val channelActivatedFuture = CompletableFuture<Channel>()
            val channelClosedFuture = CompletableFuture<Void>()

            // on connected
            channelActivatedFuture.whenComplete { channel, throwable ->
                if (throwable != null) {
                    nodeConnectionReadyFuture.completeExceptionally(throwable)
                } else {
                    conn.channel = channel
                    conn.status.set(ConnectionStatus.ACTIVE)
                    nodeConnectionReadyFuture.complete(conn)
                }
            }

            // on closed (either by close() or network error)
            channelClosedFuture.whenComplete { _, _ ->
                conn.status.set(ConnectionStatus.CLOSED)
            }

            bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    // Write(Outbound)時は、上から下に呼び出される。
                    // Read(Inbound)時は、下から上に呼び出される。
                    // See: [ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html)
                    ch.pipeline()
                        .addLast(CommandCodecHandler())
                        .addLast(ConnectionStateHandler(channelActivatedFuture, channelClosedFuture))
                }
            })

            val connectFuture = bootstrap.connect(endpoint.host, endpoint.port)
            connectFuture.addListener {
                if (!connectFuture.isSuccess) {
                    conn.status.set(ConnectionStatus.CLOSED)
                    if (connectFuture.cause() != null) {
                        // Completed with failure
                        nodeConnectionReadyFuture.completeExceptionally(connectFuture.cause())
                    } else {
                        // Completed by cancellation
                        nodeConnectionReadyFuture.completeExceptionally(
                            RedisInitializationCanceledException("initialization canceled")
                        )
                    }
                }
            }

            return nodeConnectionReadyFuture
        }
    }
}

enum class ConnectionStatus {
    INITIALIZING,
    ACTIVE,
    CLOSED,
}
