package dev.todaka.robustredis.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext

/**
 * Channelの接続状態を管理するためのHandler
 *
 * Nettyのeventの呼び出し順序は以下の通り
 *
 * ```
 * - handlerAdded
 * - channelRegistered
 * - connect
 * - channelActive : 接続成功時のみ
 *
 * - channelRead : 読み込み時
 * - channelReadComplete : 読み込み完了時のみ
 * - exceptionCaught : 読み込み失敗時のみ
 *
 * - channelInactive : 接続成功している時の close 時のみ
 * - channelUnregistered
 * - handlerRemoved
 * ```
 */
class ConnectionStateHandler(
    private val eventListener: ChannelStateListener,
) : ChannelDuplexHandler() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        eventListener.onAdded(ctx)
        super.handlerAdded(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        eventListener.onReady(ctx.channel())
        super.channelActive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        if (evt is ClosedReason) {
            ctx.close() // (NodeConnectionと重複しているので、もしかしたらいらないかも)
            eventListener.onClosed(evt)
        }
        super.userEventTriggered(ctx, evt)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.pipeline().fireUserEventTriggered(ClosedReason.Network(cause))
    }
}

interface ChannelStateListener {
    /** 接続開始前に、必ず1度呼ばれる */
    fun onAdded(ctx: ChannelHandlerContext)

    /** 接続に成功した場合、最大1回呼ばれる */
    fun onReady(channel: Channel)

    /** close開始後に、必ず1度呼ばれる */
    fun onClosed(reason: ClosedReason)
}

sealed class ClosedReason {
    data object ManuallyClosed : ClosedReason()
    data class Network(val cause: Throwable) : ClosedReason()
    data class Initialization(val cause: Throwable) : ClosedReason()
}
