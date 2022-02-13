package dev.todaka.jredis.cluster;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterNodesParserTest {
    @Test
    public void test() {
        final var response =
                "0fee9f95a7bae82e11867a428bcccfe3f0542b36 127.0.0.1:45402@55402 master - 0 1644737192153 2 connected 5461-10922\r\n" +
                        "2fa310eb607645d34e7f05d19137098f49864f4e 127.0.0.1:45403@55403 master - 0 1644737192153 3 connected 10923-16383\r\n" +
                        "52aa3c1ced7a0381237ed1111dc556485cde4854 127.0.0.1:45401@55401 myself,master - 0 1644737191000 1 connected 0-5460\r\n";

        final var expected = List.of(
                new ClusterNodeView(
                        "0fee9f95a7bae82e11867a428bcccfe3f0542b36",
                        "127.0.0.1",
                        45402,
                        new HashSet<>(List.of("master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(5461, 10922))
                ),
                new ClusterNodeView(
                        "2fa310eb607645d34e7f05d19137098f49864f4e",
                        "127.0.0.1",
                        45403,
                        new HashSet<>(List.of("master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(10923, 16383))
                ),
                new ClusterNodeView(
                        "52aa3c1ced7a0381237ed1111dc556485cde4854",
                        "127.0.0.1",
                        45401,
                        new HashSet<>(List.of("myself", "master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(0, 5460))
                )
        );
        final var actual = ClusterNodesParser.parse(response);
        assertThat(actual).isEqualTo(expected);
    }
}
