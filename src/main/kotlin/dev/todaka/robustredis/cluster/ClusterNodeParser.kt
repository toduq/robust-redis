package dev.todaka.robustredis.cluster

/**
 * See https://redis.io/commands/cluster-nodes
 *
 * The CLUSTER NODES response format is as follows.
 * `<id> <ip:port@cport> <flags> <master> <ping-sent> <pong-recv> <config-epoch> <link-state> <slot> <slot> ... <slot>`
 **/
object ClusterNodesParser {
    fun parse(clusterNodes: String): List<ClusterNodeView> {
        val lines = clusterNodes.trim().split("\n")
        return lines.map { line ->
            val columns = line.trim().split(" ")
            val slots = columns.subList(8, columns.size).map { slot ->
                if (slot.contains("-")) {
                    ClusterNodeView.Slot(slot.split("-")[0].toInt(), slot.split("-")[1].toInt())
                } else {
                    val slotInt = slot.toInt()
                    ClusterNodeView.Slot(slotInt, slotInt)
                }
            }
            ClusterNodeView(
                id = columns[0],
                ip = columns[1].split(":")[0],
                port = columns[1].split(":")[1].split("@")[0].toInt(),
                flags = HashSet(columns[2].split(",")),
                master = columns[3],
                linkState = columns[7],
                slots = slots,
            )
        }
    }
}
