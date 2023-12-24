package dev.todaka.robustredis

fun main() {
    NodeConnection.connect(RedisURI("localhost", 6379)).use { connection ->
        println(connection.ping().get())
    }
}
