package dev.todaka.robustredis.standalone

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.connection.ConnectionStatus
import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI
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
    private val status = AtomicReference(Status.INITIALIZING)
    private var nodeConnection: NodeConnection
    private val waitingCommand = ArrayDeque<RedisCommand<*>>()

    init {
        nodeConnection = NodeConnection.connect(endpoint)
        status.set(Status.ACTIVE)
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        when (nodeConnection.status.get()) {
            ConnectionStatus.INITIALIZING -> {
                waitingCommand.addLast(redisCommand)
            }

            ConnectionStatus.ACTIVE -> {
                nodeConnection.dispatchCommand(redisCommand)
            }

            ConnectionStatus.CLOSED -> {
                synchronized(nodeConnection) {
                    nodeConnection = NodeConnection.connectAsync(endpoint).whenComplete { conn, ex ->
                        if (ex != null) {
                            redisCommand.commandOutput.completableFuture.completeExceptionally(ex)
                        } else {
                            conn.dispatchCommand(redisCommand)
                        }
                    }.get()
                }
                waitingCommand.addLast(redisCommand)
            }
        }

        return redisCommand.commandOutput.completableFuture
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    enum class Status {
        INITIALIZING,
        ACTIVE,
        RECONNECTING,
        CLOSED,
    }
}
