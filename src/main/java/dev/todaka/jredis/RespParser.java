package dev.todaka.jredis;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class RespParser {
    private final Deque<RespLine> lineQueue = new ArrayDeque<>();

    public String tryParse(ByteBuf buf) {
        tryEnqueueLines(buf);

        final var firstLine = lineQueue.peekFirst();
        if (firstLine == null) {
            return null;
        }

        if (firstLine.type == '$') {
            if (lineQueue.size() < 2) {
                return null;
            }
            lineQueue.removeFirst();
            return lineQueue.removeFirst().content;
        }
        return lineQueue.removeFirst().content;
    }

    private void tryEnqueueLines(ByteBuf buf) {
        while (true) {
            if (!buf.isReadable()) {
                break;
            }

            RespLine respLine;

            // bulk string 2nd line
            if (!lineQueue.isEmpty() && lineQueue.getLast().type == '$') {
                final var stringLen = Integer.parseInt(lineQueue.getLast().content);
                respLine = readBulkStringSecondLine(buf, stringLen);
            } else {
                final var type = buf.getByte(buf.readerIndex());
                switch (type) {
                    case '+':
                    case '-':
                    case ':':
                    case '$': {
                        respLine = readOneLine(buf);
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("unknown response type found : " + type);
                }
            }
            if (respLine != null) {
                lineQueue.addLast(respLine);
            } else {
                break;
            }
        }
    }

    private RespLine readOneLine(ByteBuf buf) {
        int index = buf.bytesBefore((byte) '\n');
        if (index == -1) {
            return null;
        }
        final var type = buf.getByte(buf.readerIndex());
        final var line = buf.readBytes(index + 1).toString(StandardCharsets.UTF_8);
        return new RespLine(type, line.substring(1, line.length() - 2));
    }

    private RespLine readBulkStringSecondLine(ByteBuf buf, int len) {
        if (buf.readableBytes() < len + 2) {
            return null;
        }
        final var line = buf.readBytes(len + 2).toString(StandardCharsets.UTF_8);
        return new RespLine((byte) 'b', line.substring(0, line.length() - 2));
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
