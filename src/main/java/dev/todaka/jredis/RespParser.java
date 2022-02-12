package dev.todaka.jredis;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class RespParser {
    private final Deque<RespLine> lineQueue = new ArrayDeque<>();

    public String tryParse(ByteBuf buf) {
        tryEnqueueLines(buf);

        if (lineQueue.isEmpty()) {
            return null;
        }
        return lineQueue.removeFirst().content;
    }

    private void tryEnqueueLines(ByteBuf buf) {
        while (true) {
            if (buf.readableBytes() < 3) {
                break;
            }
            final var type = buf.getByte(buf.readerIndex());

            RespLine respLine;
            switch (type) {
                case '+':
                case '-':
                case ':': {
                    respLine = readUntilNewLine(buf);
                    break;
                }
                default:
                    throw new IllegalArgumentException("unknown response type found : " + type);
            }
            if (respLine != null) {
                lineQueue.addLast(respLine);
            } else {
                break;
            }
        }
    }

    private RespLine readUntilNewLine(ByteBuf buf) {
        int index = buf.bytesBefore((byte) '\n');
        if (index == -1) {
            return null;
        }
        final var type = buf.getByte(buf.readerIndex());
        final var line = buf.readBytes(index + 1).toString(StandardCharsets.UTF_8);
        return new RespLine(type, line.substring(1, line.length() - 2));
    }

    private static class RespLine {
        final byte type;
        final String content;

        public RespLine(byte type, String content) {
            this.type = type;
            this.content = content;
        }
    }
}
