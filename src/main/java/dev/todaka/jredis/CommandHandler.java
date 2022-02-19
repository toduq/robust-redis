package dev.todaka.jredis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CommandHandler extends ChannelDuplexHandler {
    private final CompletableFuture<Channel> channelReadyFuture;
    private final Deque<RedisCommand> commandQueue = new ArrayDeque<>();
    private final RespParser respParser = new RespParser();
    private ByteBuf buf;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channelActive");
        buf = ctx.alloc().buffer();
        if (!channelReadyFuture.isDone()) {
            channelReadyFuture.complete(ctx.channel());
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        buf.release();
        ctx.fireChannelUnregistered();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        final RedisCommand redisCommand = (RedisCommand) msg;
        commandQueue.addLast(redisCommand);
        final var buf = ctx.alloc().buffer();
        redisCommand.writeToByteBuf(buf);
        ctx.write(buf, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("channelRead");
        final ByteBuf _msg = (ByteBuf) msg;
        buf.writeBytes(_msg);
        _msg.release();

        while (true) {
            final RedisResponse resp = respParser.tryParse(buf);
            if (resp == null) break;
            final RedisCommand command = commandQueue.removeFirst();
            command.response.complete(resp);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("exceptionCaught");
        if (!channelReadyFuture.isDone()) {
            channelReadyFuture.completeExceptionally(cause);
        }
        cause.printStackTrace();
        ctx.close();
    }
}
