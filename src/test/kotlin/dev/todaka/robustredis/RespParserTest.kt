package dev.todaka.robustredis

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Test

class RespParserTest {
    @Test
    fun testSingleLine() {
        val respParser = RespParser()
        val buf = Unpooled.buffer()
        ByteBufUtil.writeUtf8(buf, "+PO")
        assertThat(respParser.tryParse(buf)).isEqualTo(null)
        ByteBufUtil.writeUtf8(buf, "NG\r")
        assertThat(respParser.tryParse(buf)).isEqualTo(null)
        ByteBufUtil.writeUtf8(buf, "\n-Error\r\n")
        assertThat(respParser.tryParse(buf)).isEqualTo(StringResponse("PONG"))
        assertThat(respParser.tryParse(buf)).isEqualTo(ErrorResponse("Error"))
        assertThat(respParser.tryParse(buf)).isEqualTo(null)
    }

    @Test
    fun testBulkString() {
        val respParser = RespParser()
        val buf = Unpooled.buffer()
        ByteBufUtil.writeUtf8(buf, "$12\r\nHello, World\r\n")
        ByteBufUtil.writeUtf8(buf, "+PONG\r\n")
        ByteBufUtil.writeUtf8(buf, "$13\r\nHello, Redis!\r\n")
        assertThat(respParser.tryParse(buf)).isEqualTo(StringResponse("Hello, World"))
        assertThat(respParser.tryParse(buf)).isEqualTo(StringResponse("PONG"))
        assertThat(respParser.tryParse(buf)).isEqualTo(StringResponse("Hello, Redis!"))
        assertThat(respParser.tryParse(buf)).isEqualTo(null)
    }
}
