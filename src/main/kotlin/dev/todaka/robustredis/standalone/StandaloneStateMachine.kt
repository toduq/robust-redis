package dev.todaka.robustredis.standalone

import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.exception.RedisStateMachineException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class StandaloneStateMachine(
    /** this must be single thread for caller thread safety */
    private val executor: ScheduledExecutorService = defaultExecutor,
) {
    private var status: StandaloneClientStatus = StandaloneClientStatus.Initializing

    /** 何回呼んでも良い */
    fun connect(
        createConn: () -> CompletableFuture<NodeConnection>,
        onActive: () -> Unit,
    ) {
        executor.submit {
            closeConn()
            if (status is StandaloneClientStatus.Initializing || status is StandaloneClientStatus.Active) {
                val future = createConn()
                status = StandaloneClientStatus.Connecting(future)
                future.whenComplete { _, _ -> println("whenComplete") }
                future.thenAcceptAsync(
                    {
                        status = StandaloneClientStatus.Active(it)
                        onActive()
                    },
                    executor,
                )
            }
        }
    }

    fun close() {
        executor.submit {
            closeConn()
            status = StandaloneClientStatus.Closed(null)
        }
    }

    fun fetchActiveConnection(): NodeConnection? {
        return when (val it = status) {
            is StandaloneClientStatus.Initializing -> null

            is StandaloneClientStatus.Connecting -> null

            is StandaloneClientStatus.Active -> it.conn

            is StandaloneClientStatus.Closed -> throw RedisStateMachineException("already closed")
        }
    }

    private fun closeConn() {
        when (val it = status) {
            is StandaloneClientStatus.Active -> it.conn.close()

            is StandaloneClientStatus.Connecting -> it.future.thenAccept { it.close() }

            else -> {}
        }
    }

    companion object {
        private val defaultExecutor = Executors.newSingleThreadScheduledExecutor()
    }
}

sealed interface StandaloneClientStatus {
    /** 初期状態 */
    data object Initializing : StandaloneClientStatus

    /** 接続待ち */
    data class Connecting(val future: CompletableFuture<NodeConnection>) : StandaloneClientStatus

    /** 接続済み */
    data class Active(val conn: NodeConnection) : StandaloneClientStatus

    /** 終了状態 */
    data class Closed(val cause: Throwable?) : StandaloneClientStatus
}
