package dev.todaka.robustredis

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil

class CommandInput(val command: CommandName) {
    var keys: List<String>? = null
    var args: List<String>? = null

    fun addKey(key: String): CommandInput {
        keys = listOf(key)
        return this
    }

    fun addKey(keys: List<String>?): CommandInput {
        this.keys = keys
        return this
    }

    fun addArg(arg: String): CommandInput {
        args = listOf(arg)
        return this
    }

    fun addArg(args: List<String>?): CommandInput {
        this.args = args
        return this
    }

    fun writeToByteBuf(buf: ByteBuf) {
        val len = 1 + (if (keys != null) keys!!.size else 0) + if (args != null) args!!.size else 0
        ByteBufUtil.writeAscii(buf, "*$len\r\n")
        writeBulkString(buf, command.toString())
        if (keys != null) {
            for (key in keys!!) {
                writeBulkString(buf, key)
            }
        }
        if (args != null) {
            for (arg in args!!) {
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
