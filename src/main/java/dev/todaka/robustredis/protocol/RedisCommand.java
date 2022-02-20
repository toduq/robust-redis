package dev.todaka.robustredis.protocol;

import io.netty.buffer.ByteBuf;

public class RedisCommand<R> {
    public final CommandInput commandInput;
    public final CommandOutput<R> commandOutput;

    public RedisCommand(CommandInput commandInput, CommandOutput<R> commandOutput) {
        this.commandInput = commandInput;
        this.commandOutput = commandOutput;
    }

    public void writeToByteBuf(ByteBuf buf) {
        commandInput.writeToByteBuf(buf);
    }

    public String getFirstKey() {
        return commandInput.getKeys().get(0);
    }
}
