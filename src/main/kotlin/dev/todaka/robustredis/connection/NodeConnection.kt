package dev.todaka.robustredis.connection

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.exception.RedisAlreadyClosedException
import dev.todaka.robustredis.exception.RedisInitializationException
import dev.todaka.robustredis.model.RedisCommand
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
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
    private val status = AtomicReference(ConnectionStatus.INITIALIZING)
    private lateinit var workerGroup: EventLoopGroup
    private lateinit var ctx: ChannelHandlerContext
    private lateinit var channel: Channel

    override fun close() {
        println("on close NodeConnection")
        ctx.pipeline().fireUserEventTriggered(ClosedReason.ManuallyClosed)
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

    inner class NodeConnectionListener(
        private val connectionReadyFuture: CompletableFuture<NodeConnection>,
    ) : ChannelStateListener {
        override fun onAdded(ctx: ChannelHandlerContext) {
            this@NodeConnection.ctx = ctx
        }

        override fun onReady(channel: Channel) {
            this@NodeConnection.channel = channel
            status.set(ConnectionStatus.ACTIVE)
            connectionReadyFuture.complete(this@NodeConnection)
        }

        override fun onClosed(reason: ClosedReason) {
            status.set(ConnectionStatus.CLOSED)
            if (!connectionReadyFuture.isDone) {
                val cause = when (reason) {
                    is ClosedReason.Initialization -> reason.cause
                    is ClosedReason.Network -> reason.cause
                    is ClosedReason.ManuallyClosed -> null
                }
                connectionReadyFuture.completeExceptionally(
                    RedisInitializationException("connection closed before ready", cause)
                )
            }
            if (this@NodeConnection::channel.isInitialized) {
                channel.close()
                channel.closeFuture()?.sync()
            }
            if (this@NodeConnection::workerGroup.isInitialized) {
                workerGroup.shutdownGracefully()?.sync()
            }
        }
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

            val connectionReadyFuture = CompletableFuture<NodeConnection>()
            bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    // Write(Outbound)時は、上から下に呼び出される。
                    // Read(Inbound)時は、下から上に呼び出される。
                    // See: [ChannelPipeline](https://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html)
                    ch.pipeline()
                        .addLast(CommandCodecHandler())
                        .addLast(ConnectionStateHandler(conn.NodeConnectionListener(connectionReadyFuture)))
                }
            })

            val connectFuture = bootstrap.connect(endpoint.host, endpoint.port)
            connectFuture.addListener {
                if (!connectFuture.isSuccess) {
                    val reason = ClosedReason.Initialization(connectFuture.cause())
                    conn.ctx.pipeline().fireUserEventTriggered(reason)
                }
            }

            return connectionReadyFuture
        }
    }
}

enum class ConnectionStatus {
    INITIALIZING,
    ACTIVE,
    CLOSED,
}
