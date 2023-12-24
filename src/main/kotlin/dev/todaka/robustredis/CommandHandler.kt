package dev.todaka.robustredis

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.util.*
import java.util.concurrent.CompletableFuture

class CommandHandler(
    private val channelReadyFuture: CompletableFuture<Channel>,
) : ChannelDuplexHandler() {

    private val commandQueue = ArrayDeque<RedisCommand<*>>()
    private val respParser: RespParser = RespParser()
    private lateinit var buf: ByteBuf

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("on channelActive")
        buf = ctx.alloc().buffer()
        if (!channelReadyFuture.isDone) {
            channelReadyFuture.complete(ctx.channel())
        }
        ctx.fireChannelRegistered()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        buf.release()
        ctx.fireChannelUnregistered()
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val redisCommand: RedisCommand<*> = msg as RedisCommand<*>
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
            val resp: RedisResponse = respParser.tryParse(buf) ?: break
            val command: RedisCommand<*> = commandQueue.removeFirst()
            when (resp) {
                is StringResponse -> command.commandOutput.setResult(resp.body)

                is LongResponse -> command.commandOutput.setResult(resp.body)

                is ErrorResponse -> command.commandOutput.setError(resp.body)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("on exceptionCaught")
        if (!channelReadyFuture.isDone) {
            channelReadyFuture.completeExceptionally(cause)
        }
        cause.printStackTrace()
        ctx.close()
    }
}
