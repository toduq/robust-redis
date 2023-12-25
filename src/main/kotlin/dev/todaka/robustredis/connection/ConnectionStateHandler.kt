package dev.todaka.robustredis.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.net.SocketAddress
import java.util.concurrent.CompletableFuture

class ConnectionStateHandler(
    private val channelReadyFuture: CompletableFuture<Channel>,
) : ChannelDuplexHandler() {
    override fun channelActive(ctx: ChannelHandlerContext) {
        println("on channelActive")
        if (!channelReadyFuture.isDone) {
            channelReadyFuture.complete(ctx.channel())
        }
        ctx.fireChannelRegistered()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("on channelInactive")
        ctx.fireChannelInactive()
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

    override fun bind(ctx: ChannelHandlerContext?, localAddress: SocketAddress?, promise: ChannelPromise?) {
        println("on bind")
        super.bind(ctx, localAddress, promise)
    }

    override fun connect(
        ctx: ChannelHandlerContext?,
        remoteAddress: SocketAddress?,
        localAddress: SocketAddress?,
        promise: ChannelPromise?
    ) {
        println("on connect")
        super.connect(ctx, remoteAddress, localAddress, promise)
    }

    override fun disconnect(ctx: ChannelHandlerContext?, promise: ChannelPromise?) {
        println("on disconnect")
        super.disconnect(ctx, promise)
    }

    override fun close(ctx: ChannelHandlerContext?, promise: ChannelPromise?) {
        println("on close")
        super.close(ctx, promise)
    }

    override fun deregister(ctx: ChannelHandlerContext?, promise: ChannelPromise?) {
        println("on deregister")
        super.deregister(ctx, promise)
    }

    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        println("on channelRegistered")
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        println("on channelUnregistered")
        super.channelUnregistered(ctx)
    }
}
