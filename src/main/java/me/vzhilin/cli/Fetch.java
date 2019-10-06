package me.vzhilin.cli;

import me.vzhilin.db.Row;
import me.vzhilin.util.TopologicalSort;

import java.util.*;

public final class Fetch {
    public List<Row> fetch(List<Row> start) {
        TopologicalSort<Row> topologicalSort = new TopologicalSort<>();

        Set<Row> processed = new HashSet<>();
        Queue<Row> queue = new LinkedList<>(start);
        while (!queue.isEmpty()) {
            Row next = queue.poll();
            for (Row r: next.forwardReferences().values()) {
                topologicalSort.put(r, next);
                if (processed.add(r)) {
                    queue.add(r);
                }
            }
        }

        return topologicalSort.topoSort();
    }
}
