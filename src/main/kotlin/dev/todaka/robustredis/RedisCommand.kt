package dev.todaka.robustredis

import io.netty.buffer.ByteBuf

data class RedisCommand<R>(
    val commandInput: CommandInput,
    val commandOutput: CommandOutput<R>
) {
    fun writeToByteBuf(buf: ByteBuf) {
        commandInput.writeToByteBuf(buf)
    }

    val firstKey: String?
        get() = commandInput.keys?.get(0)
}
