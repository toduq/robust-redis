package dev.todaka.robustredis

enum class CommandName {
    // https://redis.io/commands#cluster
    CLUSTER,

    // https://redis.io/commands#connection
    ECHO,
    PING,

    // https://redis.io/commands#generic
    DEL,
    EXISTS,

    // https://redis.io/commands#string
    DECR,
    GET,
    INCR,
    SET
}
