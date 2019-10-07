package me.vzhilin.cli;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.vzhilin.db.Row;
import me.vzhilin.util.TopologicalSort;

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
