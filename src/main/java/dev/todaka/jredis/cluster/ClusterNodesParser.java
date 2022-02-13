package dev.todaka.jredis.cluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See https://redis.io/commands/cluster-nodes
 *
 * The CLUSTER NODES response format is as follows.
 * <id> <ip:port@cport> <flags> <master> <ping-sent> <pong-recv> <config-epoch> <link-state> <slot> <slot> ... <slot>
 */
public class ClusterNodesParser {
    public static List<ClusterNodeView> parse(String clusterNodes) {
        return Arrays
                .stream(clusterNodes.trim().split("\r\n"))
                .map(line -> {
                    final var columns = line.split(" ");
                    final var slots = Arrays
                            .asList(columns)
                            .subList(8, columns.length)
                            .stream()
                            .map(slot -> {
                                if (slot.contains("-")) {
                                    return new ClusterNodeView.Slot(
                                            Integer.parseInt(slot.split("-")[0]),
                                            Integer.parseInt(slot.split("-")[1])
                                    );
                                } else {
                                    final var slotInt = Integer.parseInt(slot);
                                    return new ClusterNodeView.Slot(slotInt, slotInt);
                                }
                            })
                            .collect(Collectors.toList());
                    return new ClusterNodeView(
                            columns[0],
                            columns[1].split(":")[0],
                            Integer.parseInt(columns[1].split(":")[1].split("@")[0]),
                            new HashSet<>(Arrays.asList(columns[2].split(","))),
                            columns[3],
                            columns[7],
                            slots
                    );
                })
                .collect(Collectors.toList());
    }
}
