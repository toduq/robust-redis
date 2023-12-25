package dev.todaka.robustredis.standalone

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI
import dev.todaka.robustredis.exception.RedisAlreadyClosedException
import dev.todaka.robustredis.model.RedisCommand
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

/**
 * Standalone Redis向けのClient
 *
 * - NodeConnectionが切れたら自動的に再接続する
 *   - 再接続時、送信中のcommandは実行されたかが分からないため、安全にretryすることはできない。そのため全てrejectする。
 */
class RedisStandaloneClient(
    private val endpoint: RedisURI
) : AutoCloseable, RedisCommands {
    private val status = AtomicReference<StandaloneClientStatus>(StandaloneClientStatus.Waiting)
    private val waitingCommand = ArrayDeque<RedisCommand<*>>()

    init {
        // TODO: connectとconnectAsyncを用意する
        connect()
    }

    private fun connect() {
        val future = synchronized(status) {
            if (status.get() is StandaloneClientStatus.Connecting) {
                return
            }
            val f = NodeConnection.connectAsync(endpoint)
            status.set(StandaloneClientStatus.Connecting(f))
            f
        }

        future.thenAccept { conn ->
            synchronized(status) {
                status.set(StandaloneClientStatus.Active(conn))
            }
            while (waitingCommand.isNotEmpty()) {
                // TODO: この間に切断されたらどうする？
                conn.dispatchCommand(waitingCommand.removeFirst())
            }
        }
    }

    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        when (val it = status.get()) {
            is StandaloneClientStatus.Connecting -> {
                waitingCommand.addLast(redisCommand)
            }

            is StandaloneClientStatus.Active -> {
                try {
                    it.conn.dispatchCommand(redisCommand)
                } catch (e: RedisAlreadyClosedException) {
                    connect() // TODO: あってる？
                    waitingCommand.addLast(redisCommand)
                }
            }

            StandaloneClientStatus.Waiting -> TODO()
        }

        return redisCommand.commandOutput.completableFuture
    }

    override fun close() {
        when (val it = status.get()) {
            is StandaloneClientStatus.Connecting -> {
                it.future.thenAccept(NodeConnection::close)
            }

            is StandaloneClientStatus.Active -> {
                it.conn.close()
            }

            StandaloneClientStatus.Waiting -> TODO()
        }
    }
}

sealed interface StandaloneClientStatus {
    /** 初期状態 */
    data object Waiting : StandaloneClientStatus

    /** 接続待ち */
    data class Connecting(val future: CompletableFuture<NodeConnection>) : StandaloneClientStatus

    /** 接続済み */
    data class Active(val conn: NodeConnection) : StandaloneClientStatus
}
