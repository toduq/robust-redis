package dev.todaka.robustredis.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class CommandInput {
    private final CommandName command;
    private List<String> keys = null;
    private List<String> args = null;

    public CommandInput(CommandName command) {
        this.command = command;
    }

    public CommandInput addKey(String key) {
        keys = Collections.singletonList(key);
        return this;
    }

    public CommandInput addKey(List<String> keys) {
        this.keys = keys;
        return this;
    }

    public CommandInput addArg(String arg) {
        args = Collections.singletonList(arg);
        return this;
    }

    public CommandInput addArg(List<String> args) {
        this.args = args;
        return this;
    }

    public void writeToByteBuf(ByteBuf buf) {
        final var len = 1 + (keys != null ? keys.size() : 0) + (args != null ? args.size() : 0);
        ByteBufUtil.writeAscii(buf, '*' + Integer.toString(len) + "\r\n");
        writeBulkString(buf, command.toString());
        if (keys != null) {
            for (final var key : keys) {
                writeBulkString(buf, key);
            }
        }
        if (args != null) {
            for (final var arg : args) {
                writeBulkString(buf, arg);
            }
        }
    }

    private void writeBulkString(ByteBuf buf, String str) {
        final var len = ByteBufUtil.utf8Bytes(str);
        ByteBufUtil.writeAscii(buf, '$' + Integer.toString(len) + "\r\n");
        ByteBufUtil.writeUtf8(buf, str);
        ByteBufUtil.writeAscii(buf, "\r\n");
    }
}
