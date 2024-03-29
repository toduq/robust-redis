package dev.todaka.robustredis.connection

import dev.todaka.robustredis.exception.RedisConnectionClosedException
import dev.todaka.robustredis.model.RedisCommand
import dev.todaka.robustredis.protocol.*
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.util.concurrent.ConcurrentLinkedQueue

class CommandCodecHandler : ChannelDuplexHandler() {
    private val commandQueue = ConcurrentLinkedQueue<RedisCommand<*>>()
    private val respParser: RespParser = RespParser()
    private lateinit var buf: ByteBuf

    override fun channelActive(ctx: ChannelHandlerContext) {
        buf = ctx.alloc().buffer()
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        buf.release()
        ctx.fireChannelInactive()

        val exception = RedisConnectionClosedException("connection is closed while sending command")
        commandQueue.forEach {
            if (!it.commandOutput.completableFuture.isDone) {
                it.commandOutput.completableFuture.completeExceptionally(exception)
            }
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val redisCommand = msg as RedisCommand<*>
        println("on write ${redisCommand.commandInput.name}")
        commandQueue.add(redisCommand)
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
            val command = commandQueue.remove()
            when (resp) {
                is StringResponse -> command.commandOutput.resolveString(resp.body)

                is LongResponse -> command.commandOutput.resolveLong(resp.body)

                is NullResponse -> command.commandOutput.resolveNull()

                is ErrorResponse -> command.commandOutput.reject(resp.body)
            }
        }
    }
}
