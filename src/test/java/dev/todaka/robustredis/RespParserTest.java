package dev.todaka.robustredis;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RespParserTest {
    @Test
    public void testSingleLine() {
        final var respParser = new RespParser();
        final var buf = Unpooled.buffer();
        ByteBufUtil.writeUtf8(buf, "+PO");
        assertThat(respParser.tryParse(buf)).isEqualTo(null);
        ByteBufUtil.writeUtf8(buf, "NG\r");
        assertThat(respParser.tryParse(buf)).isEqualTo(null);
        ByteBufUtil.writeUtf8(buf, "\n-Error\r\n");
        assertThat(respParser.tryParse(buf)).isEqualTo(new RedisResponse.StringResponse("PONG"));
        assertThat(respParser.tryParse(buf)).isEqualTo(new RedisResponse.ErrorResponse("Error"));
        assertThat(respParser.tryParse(buf)).isEqualTo(null);
    }

    @Test
    public void testBulkString() {
        final var respParser = new RespParser();
        final var buf = Unpooled.buffer();
        ByteBufUtil.writeUtf8(buf, "$12\r\nHello, World\r\n");
        ByteBufUtil.writeUtf8(buf, "+PONG\r\n");
        ByteBufUtil.writeUtf8(buf, "$13\r\nHello, Redis!\r\n");
        assertThat(respParser.tryParse(buf)).isEqualTo(new RedisResponse.StringResponse("Hello, World"));
        assertThat(respParser.tryParse(buf)).isEqualTo(new RedisResponse.StringResponse("PONG"));
        assertThat(respParser.tryParse(buf)).isEqualTo(new RedisResponse.StringResponse("Hello, Redis!"));
        assertThat(respParser.tryParse(buf)).isEqualTo(null);
    }
}
