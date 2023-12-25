package dev.todaka.robustredis.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import java.util.concurrent.CompletableFuture

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
    private val channelReadyFuture: CompletableFuture<Channel>,
    private val channelClosedFuture: CompletableFuture<Void>,
) : ChannelDuplexHandler() {
    @Volatile
    private var lastException: Throwable? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        if (!channelReadyFuture.isDone) {
            channelReadyFuture.complete(ctx.channel())
        }
        ctx.fireChannelRegistered()
    }

    /**
     * channelInactiveは、connectionが確立された後(channelActiveが呼ばれたケース)にのみ呼び出されるが、
     * channelUnregisteredは、connectionの確立に失敗しても呼び出される。
     */
    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        if (!channelClosedFuture.isDone) {
            lastException
                ?.let { channelClosedFuture.completeExceptionally(it) }
                ?: channelClosedFuture.complete(null)
        }
        super.channelUnregistered(ctx)
    }

    @Suppress("OVERRIDE_DEPRECATION") // ChannelInboundHandlerAdapterのexceptionCaughtはdeprecatedではない
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (!channelReadyFuture.isDone) {
            channelReadyFuture.completeExceptionally(cause)
        }
        lastException = cause

        // 何か起きたらすぐにconnectionを閉じる
        ctx.close()
    }
}
