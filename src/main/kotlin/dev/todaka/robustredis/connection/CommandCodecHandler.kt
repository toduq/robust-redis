package dev.todaka.robustredis.connection

import dev.todaka.robustredis.model.RedisCommand
import dev.todaka.robustredis.resp.*
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.util.*

class CommandCodecHandler : ChannelDuplexHandler() {
    private val commandQueue = ArrayDeque<RedisCommand<*>>()
    private val respParser: RespParser = RespParser()
    private lateinit var buf: ByteBuf

    override fun channelActive(ctx: ChannelHandlerContext) {
        buf = ctx.alloc().buffer()
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        buf.release()
        ctx.fireChannelInactive()

        val exception = RuntimeException("connection closed")
        commandQueue.forEach {
            if (!it.commandOutput.completableFuture.isDone) {
                it.commandOutput.completableFuture.completeExceptionally(exception)
            }
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        println("on write")
        val redisCommand = msg as RedisCommand<*>
        commandQueue.addLast(redisCommand)
        val buf = ctx.alloc().buffer()
        redisCommand.writeToByteBuf(buf)
        ctx.write(buf, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        println("on channelRead")
        val msgBuf = msg as ByteBuf
        buf.writeBytes(msgBuf)
        msgBuf.release()
        while (true) {
            val resp = respParser.tryParse(buf) ?: break
            val command = commandQueue.removeFirst()
            when (resp) {
                is StringResponse -> command.commandOutput.resolveString(resp.body)

                is LongResponse -> command.commandOutput.resolveLong(resp.body)
                
                is NullResponse -> command.commandOutput.resolveNull()

                is ErrorResponse -> command.commandOutput.reject(resp.body)
            }
        }
    }
}
