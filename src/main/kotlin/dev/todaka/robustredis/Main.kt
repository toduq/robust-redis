package dev.todaka.robustredis

import dev.todaka.robustredis.connection.NodeConnection
import dev.todaka.robustredis.connection.RedisURI

fun main() {
    NodeConnection.connect(RedisURI("localhost", 6379)).use { connection ->
        println(connection.ping().get())
    }
}
