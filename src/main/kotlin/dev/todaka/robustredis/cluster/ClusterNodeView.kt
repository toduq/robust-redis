package dev.todaka.robustredis.cluster

data class ClusterNodeView(
    val id: String,
    val ip: String,
    val port: Int,
    val flags: Set<String>,
    val master: String,
    val linkState: String,
    val slots: List<Slot>,
) {
    data class Slot(
        val from: Int,
        val to: Int,
    )
}
