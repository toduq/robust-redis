package dev.todaka.robustredis.protocol

import dev.todaka.robustredis.exception.RedisProtocolException
import io.netty.buffer.ByteBuf

class RespParser {
    private val pendingTokens = ArrayDeque<ByteArray>()
    private val nextTokenType = ArrayDeque<TokenType>()
    private var bulkStringLen = -1

    fun tryParse(buf: ByteBuf): RedisResponse? {
        tokenize(buf)
        return parse()
    }

    private fun tokenize(buf: ByteBuf) {
        while (true) {
            if (nextTokenType.isEmpty()) {
                nextTokenType.addLast(TokenType.TYPE)
            }
            when (nextTokenType.first()) {
                TokenType.TYPE -> {
                    if (buf.readableBytes() < 1) {
                        return
                    }
                    val token = enqueueNBytes(buf, 1)
                    when (token[0].toInt().toChar()) {
                        '+', '-', ':' -> {
                            nextTokenType.addLast(TokenType.TILL_NEWLINE)
                            nextTokenType.addLast(TokenType.NEW_LINE)
                        }

                        '$' -> {
                            nextTokenType.addLast(TokenType.BULK_STRING_LEN)
                            nextTokenType.addLast(TokenType.NEW_LINE)
                            nextTokenType.addLast(TokenType.BULK_STRING)
                            nextTokenType.addLast(TokenType.NEW_LINE)
                        }

                        else -> {
                            throw RedisProtocolException("unknown response type found : " + token[0])
                        }
                    }
                }

                TokenType.NEW_LINE -> {
                    if (buf.readableBytes() < 2) {
                        return
                    }
                    val token = enqueueNBytes(buf, 2)
                    if (token[0] != '\r'.code.toByte() || token[1] != '\n'.code.toByte()) {
                        throw RedisProtocolException(
                            "new line expected, but found other binary : [" + token[0] + "," + token[1] + "]"
                        )
                    }
                }

                TokenType.TILL_NEWLINE -> {
                    val index = buf.bytesBefore('\r'.code.toByte())
                    if (index == -1) {
                        return
                    }
                    enqueueNBytes(buf, index)
                }

                TokenType.BULK_STRING_LEN -> {
                    val index = buf.bytesBefore('\r'.code.toByte())
                    if (index == -1) {
                        return
                    }
                    val token = enqueueNBytes(buf, index)
                    bulkStringLen = String(token, Charsets.US_ASCII).toInt()
                    if (bulkStringLen == -1) {
                        // null bulk string, no bulk string body and new line.
                        nextTokenType.removeFirst()
                        nextTokenType.removeFirst()
                    }
                }

                TokenType.BULK_STRING -> {
                    if (buf.readableBytes() < bulkStringLen) {
                        return
                    }
                    enqueueNBytes(buf, bulkStringLen)
                }
            }
            nextTokenType.removeFirst()
        }
    }

    private fun enqueueNBytes(buf: ByteBuf, len: Int): ByteArray {
        val token = ByteArray(len)
        buf.readBytes(token)
        pendingTokens.addLast(token)
        return token
    }

    private fun parse(): RedisResponse? {
        if (pendingTokens.size < 3) {
            return null
        }
        val iter = pendingTokens.iterator()
        val type = iter.next()
        when (type[0].toInt().toChar()) {
            '+' -> {
                val body = String(iter.next(), Charsets.UTF_8)
                consumeNPendingTokens(3)
                return StringResponse(body)
            }

            '-' -> {
                val body = String(iter.next(), Charsets.UTF_8)
                consumeNPendingTokens(3)
                return ErrorResponse(body)
            }

            ':' -> {
                val body = String(iter.next(), Charsets.UTF_8)
                consumeNPendingTokens(3)
                return LongResponse(body.toLong())
            }

            '$' -> {
                val len = String(iter.next(), Charsets.UTF_8).toLong()
                return if (len == -1L) {
                    // null bulk string
                    consumeNPendingTokens(3)
                    NullResponse
                } else {
                    if (pendingTokens.size < 5) {
                        return null
                    }
                    iter.next()
                    val body = String(iter.next(), Charsets.UTF_8)
                    consumeNPendingTokens(5)
                    StringResponse(body)
                }
            }
        }
        return null
    }

    private fun consumeNPendingTokens(len: Int) {
        for (i in 0..<len) {
            pendingTokens.removeFirst()
        }
    }

    private enum class TokenType {
        TYPE,
        NEW_LINE,
        TILL_NEWLINE,
        BULK_STRING_LEN,
        BULK_STRING
    }
}
