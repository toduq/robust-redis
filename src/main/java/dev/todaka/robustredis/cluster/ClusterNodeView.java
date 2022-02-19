package dev.todaka.robustredis.cluster;

import lombok.Value;

import java.util.List;
import java.util.Set;

/**
 * Represents CLUSTER NODES response line.
 */
@Value
public class ClusterNodeView {
    String id;
    String ip;
    int port;
    Set<String> flags;
    String master;
    String linkState;
    List<Slot> slots;

    @Value
    public static class Slot {
        int from;
        int to;
    }
}
