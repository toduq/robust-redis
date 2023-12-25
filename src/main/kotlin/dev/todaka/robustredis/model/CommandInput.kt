package dev.todaka.robustredis.model

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil

data class CommandInput(
    val name: CommandName,
    val keys: List<String>? = null,
    val args: List<String>? = null,
) {
    fun writeToByteBuf(buf: ByteBuf) {
        val len = 1 + (keys?.size ?: 0) + (args?.size ?: 0)
        ByteBufUtil.writeAscii(buf, "*$len\r\n")
        writeBulkString(buf, name.toString())
        if (keys != null) {
            for (key in keys) {
                writeBulkString(buf, key)
            }
        }
        if (args != null) {
            for (arg in args) {
                writeBulkString(buf, arg)
            }
        }
    }

    private fun writeBulkString(buf: ByteBuf, str: String) {
        val len = ByteBufUtil.utf8Bytes(str)
        ByteBufUtil.writeAscii(buf, "$$len\r\n")
        ByteBufUtil.writeUtf8(buf, str)
        ByteBufUtil.writeAscii(buf, "\r\n")
    }
}
