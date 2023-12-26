package dev.todaka.robustredis.standalone

import dev.todaka.robustredis.RedisCommands
import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI
import dev.todaka.robustredis.exception.RedisAlreadyClosedException
import dev.todaka.robustredis.model.RedisCommand
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Standalone Redis向けのClient
 *
 * - NodeConnectionが切れたら自動的に再接続する
 *   - 再接続時、送信中のcommandは実行されたかが分からないため、安全にretryすることはできない。そのため全てrejectする。
 */
class RedisStandaloneClient(
    private val endpoint: RedisURI
) : AutoCloseable, RedisCommands {
    private val status = StandaloneStateMachine()
    private val waitingCommand = ConcurrentLinkedDeque<RedisCommand<*>>()

    init {
        // TODO: CompletableFutureを返す
        connect()
    }

    override fun <R> dispatchCommand(redisCommand: RedisCommand<R>): CompletableFuture<R> {
        println("dispatchCommand")
        val activeConnection = status.fetchActiveConnection()
        if (activeConnection == null) {
            waitingCommand.addLast(redisCommand)
        } else {
            try {
                activeConnection.dispatchCommand(redisCommand)
            } catch (e: RedisAlreadyClosedException) {
                connect()
                waitingCommand.addLast(redisCommand)
            }
        }

        return redisCommand.commandOutput.completableFuture
    }

    private fun connect() {
        println("on connect")
        status.connect(
            createConn = { NodeConnection.connectAsync(endpoint) },
            onActive = {
                println("onActive")
                while (waitingCommand.isNotEmpty()) {
                    val command = waitingCommand.removeFirst()
                    dispatchCommand(command)
                }
            },
        )
    }

    override fun close() {
        status.close()
    }
}
