package dev.todaka.robustredis;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class RespParser {
    private final Deque<byte[]> pendingTokens = new ArrayDeque<>();
    private final Deque<TokenType> nextTokenType = new ArrayDeque<>();
    private int bulkStringLen = -1;

    public RedisResponse tryParse(ByteBuf buf) {
        tokenize(buf);
        return parse();
    }

    private void tokenize(ByteBuf buf) {
        while (true) {
            if (nextTokenType.isEmpty()) {
                nextTokenType.addLast(TokenType.TYPE);
            }

            switch (nextTokenType.getFirst()) {
                case TYPE: {
                    if (buf.readableBytes() < 1) {
                        return;
                    }
                    final var token = enqueueNBytes(buf, 1);
                    // next expectation
                    switch (token[0]) {
                        case '+':
                        case '-':
                        case ':': {
                            nextTokenType.addLast(TokenType.TILL_NEWLINE);
                            nextTokenType.addLast(TokenType.NEW_LINE);
                            break;
                        }
                        case '$': {
                            nextTokenType.addLast(TokenType.BULK_STRING_LEN);
                            nextTokenType.addLast(TokenType.NEW_LINE);
                            nextTokenType.addLast(TokenType.BULK_STRING);
                            nextTokenType.addLast(TokenType.NEW_LINE);
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("unknown response type found : " + token[0]);
                        }
                    }
                    break;
                }
                case NEW_LINE: {
                    if (buf.readableBytes() < 2) {
                        return;
                    }
                    final var token = enqueueNBytes(buf, 2);
                    if (token[0] != '\r' || token[1] != '\n') {
                        throw new IllegalArgumentException(
                                "new line expected, but found other binary : [" + token[0] + "," + token[1] + "]");
                    }
                    break;
                }
                case TILL_NEWLINE: {
                    int index = buf.bytesBefore((byte) '\r');
                    if (index == -1) {
                        return;
                    }
                    enqueueNBytes(buf, index);
                    break;
                }
                case BULK_STRING_LEN: {
                    int index = buf.bytesBefore((byte) '\r');
                    if (index == -1) {
                        return;
                    }
                    final var token = enqueueNBytes(buf, index);
                    bulkStringLen = Integer.parseInt(new String(token, StandardCharsets.US_ASCII));
                    if (bulkStringLen == -1) {
                        // null bulk string, no bulk string body and new line.
                        nextTokenType.removeFirst();
                        nextTokenType.removeFirst();
                    }
                    break;
                }
                case BULK_STRING: {
                    if (buf.readableBytes() < bulkStringLen) {
                        return;
                    }
                    enqueueNBytes(buf, bulkStringLen);
                    break;
                }
            }
            nextTokenType.removeFirst();
        }
    }

    private byte[] enqueueNBytes(ByteBuf buf, int len) {
        byte[] token = new byte[len];
        buf.readBytes(token);
        pendingTokens.addLast(token);
        return token;
    }

    private RedisResponse parse() {
        if (pendingTokens.size() < 3) {
            return null;
        }
        final var iter = pendingTokens.iterator();
        final var type = iter.next();
        switch (type[0]) {
            case '+': {
                final var body = new String(iter.next(), StandardCharsets.UTF_8);
                consumeNPendingTokens(3);
                return new RedisResponse.StringResponse(body);
            }
            case '-': {
                final var body = new String(iter.next(), StandardCharsets.UTF_8);
                consumeNPendingTokens(3);
                return new RedisResponse.ErrorResponse(body);
            }
            case ':': {
                final var body = new String(iter.next(), StandardCharsets.UTF_8);
                consumeNPendingTokens(3);
                return new RedisResponse.LongResponse(Long.parseLong(body));
            }
            case '$': {
                final var len = Long.parseLong(new String(iter.next(), StandardCharsets.UTF_8));
                if (len == -1) {
                    // null bulk string
                    consumeNPendingTokens(3);
                    return new RedisResponse.NullResponse();
                } else {
                    if (pendingTokens.size() < 5) {
                        return null;
                    }
                    iter.next();
                    final var body = new String(iter.next(), StandardCharsets.UTF_8);
                    consumeNPendingTokens(5);
                    return new RedisResponse.StringResponse(body);
                }
            }
        }
        return null;
    }

    private void consumeNPendingTokens(int len) {
        for (int i = 0; i < len; i++) {
            pendingTokens.removeFirst();
        }
    }

    private enum TokenType {
        TYPE,
        NEW_LINE,
        TILL_NEWLINE,
        BULK_STRING_LEN,
        BULK_STRING,
    }
}
