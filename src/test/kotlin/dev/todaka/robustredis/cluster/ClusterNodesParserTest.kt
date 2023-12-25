package dev.todaka.robustredis.cluster

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.todaka.robustredis.cluster.ClusterNodesParser.parse
import org.junit.jupiter.api.Test

class ClusterNodesParserTest {
    @Test
    fun test() {
        val response = """
            ef667db6be793874f1e4706416281ac4d17077f7 127.0.0.1:11006@21006 slave a2243c4e2511bb2f118fcb83f3637eda3e091c32 0 1645262298713 3 connected
            8a6c9f48f25e81b8345927075b77a3185522f6ff 127.0.0.1:11002@21002 myself,master - 0 1645262298000 2 connected 5461-10922
            8e659ad1ea699db55586086e19e131c49c5475a6 127.0.0.1:11005@21005 slave 8a6c9f48f25e81b8345927075b77a3185522f6ff 0 1645262298508 2 connected
            a2243c4e2511bb2f118fcb83f3637eda3e091c32 127.0.0.1:11003@21003 master - 0 1645262298508 3 connected 10923-16383
            07dc0acc3acc59d821459d0f73299eee8e13e230 127.0.0.1:11001@21001 master - 0 1645262298713 1 connected 0-5460
            5c2e5bf28c5e19de5ded3f1899f1ad3da56853c9 127.0.0.1:11004@21004 slave 07dc0acc3acc59d821459d0f73299eee8e13e230 0 1645262298508 1 connected
        """.trimIndent()
        val expected = listOf(
            ClusterNodeView(
                id = "ef667db6be793874f1e4706416281ac4d17077f7",
                ip = "127.0.0.1",
                port = 11006,
                flags = hashSetOf("slave"),
                master = "a2243c4e2511bb2f118fcb83f3637eda3e091c32",
                linkState = "connected",
                slots = listOf(),
            ),
            ClusterNodeView(
                id = "8a6c9f48f25e81b8345927075b77a3185522f6ff",
                ip = "127.0.0.1",
                port = 11002,
                flags = hashSetOf("myself", "master"),
                master = "-",
                linkState = "connected",
                slots = listOf(ClusterNodeView.Slot(5461, 10922)),
            ),
            ClusterNodeView(
                id = "8e659ad1ea699db55586086e19e131c49c5475a6",
                ip = "127.0.0.1",
                port = 11005,
                flags = hashSetOf("slave"),
                master = "8a6c9f48f25e81b8345927075b77a3185522f6ff",
                linkState = "connected",
                slots = listOf(),
            ),
            ClusterNodeView(
                id = "a2243c4e2511bb2f118fcb83f3637eda3e091c32",
                ip = "127.0.0.1",
                port = 11003,
                flags = hashSetOf("master"),
                master = "-",
                linkState = "connected",
                slots = listOf(ClusterNodeView.Slot(10923, 16383)),
            ),
            ClusterNodeView(
                id = "07dc0acc3acc59d821459d0f73299eee8e13e230",
                ip = "127.0.0.1",
                port = 11001,
                flags = hashSetOf("master"),
                master = "-",
                linkState = "connected",
                slots = listOf(ClusterNodeView.Slot(0, 5460)),
            ),
            ClusterNodeView(
                id = "5c2e5bf28c5e19de5ded3f1899f1ad3da56853c9",
                ip = "127.0.0.1",
                port = 11004,
                flags = hashSetOf("slave"),
                master = "07dc0acc3acc59d821459d0f73299eee8e13e230",
                linkState = "connected",
                slots = listOf(),
            )
        )
        assertThat(parse(response)).isEqualTo(expected)
    }
}
