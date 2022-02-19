package dev.todaka.robustredis.cluster;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterNodesParserTest {
    @Test
    public void test() {
        final var response = "ef667db6be793874f1e4706416281ac4d17077f7 127.0.0.1:11006@21006 slave a2243c4e2511bb2f118fcb83f3637eda3e091c32 0 1645262298713 3 connected\n" +
                "8a6c9f48f25e81b8345927075b77a3185522f6ff 127.0.0.1:11002@21002 myself,master - 0 1645262298000 2 connected 5461-10922\n" +
                "8e659ad1ea699db55586086e19e131c49c5475a6 127.0.0.1:11005@21005 slave 8a6c9f48f25e81b8345927075b77a3185522f6ff 0 1645262298508 2 connected\n" +
                "a2243c4e2511bb2f118fcb83f3637eda3e091c32 127.0.0.1:11003@21003 master - 0 1645262298508 3 connected 10923-16383\n" +
                "07dc0acc3acc59d821459d0f73299eee8e13e230 127.0.0.1:11001@21001 master - 0 1645262298713 1 connected 0-5460\n" +
                "5c2e5bf28c5e19de5ded3f1899f1ad3da56853c9 127.0.0.1:11004@21004 slave 07dc0acc3acc59d821459d0f73299eee8e13e230 0 1645262298508 1 connected\n";

        final var expected = List.of(
                new ClusterNodeView(
                        "ef667db6be793874f1e4706416281ac4d17077f7",
                        "127.0.0.1",
                        11006,
                        new HashSet<>(List.of("slave")),
                        "a2243c4e2511bb2f118fcb83f3637eda3e091c32",
                        "connected",
                        List.of()
                ),
                new ClusterNodeView(
                        "8a6c9f48f25e81b8345927075b77a3185522f6ff",
                        "127.0.0.1",
                        11002,
                        new HashSet<>(List.of("myself", "master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(5461, 10922))
                ),
                new ClusterNodeView(
                        "8e659ad1ea699db55586086e19e131c49c5475a6",
                        "127.0.0.1",
                        11005,
                        new HashSet<>(List.of("slave")),
                        "8a6c9f48f25e81b8345927075b77a3185522f6ff",
                        "connected",
                        List.of()
                ),
                new ClusterNodeView(
                        "a2243c4e2511bb2f118fcb83f3637eda3e091c32",
                        "127.0.0.1",
                        11003,
                        new HashSet<>(List.of("master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(10923, 16383))
                ),
                new ClusterNodeView(
                        "07dc0acc3acc59d821459d0f73299eee8e13e230",
                        "127.0.0.1",
                        11001,
                        new HashSet<>(List.of("master")),
                        "-",
                        "connected",
                        List.of(new ClusterNodeView.Slot(0, 5460))
                ),
                new ClusterNodeView(
                        "5c2e5bf28c5e19de5ded3f1899f1ad3da56853c9",
                        "127.0.0.1",
                        11004,
                        new HashSet<>(List.of("slave")),
                        "07dc0acc3acc59d821459d0f73299eee8e13e230",
                        "connected",
                        List.of()
                )
        );
        final var actual = ClusterNodesParser.parse(response);
        assertThat(actual).isEqualTo(expected);
    }
}
