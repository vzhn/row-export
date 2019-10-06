package me.vzhilin.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

import java.util.*;

public class TopologicalSort<V> {
    private final HashMultimap<V, V> outcomingEdges = HashMultimap.create();
    private final HashMultimap<V, V> incomingEdges = HashMultimap.create();

    public void put(V from, V to) {
        outcomingEdges.put(from, to);
        incomingEdges.put(to, from);
    }

    private void remove(V from, V to) {
        outcomingEdges.remove(from, to);
        incomingEdges.remove(to, from);
    }

    public List<V> topoSort() {
        List<V> result = new ArrayList<>();
        Queue<V> noIncomingEdges = new LinkedList<>(Sets.difference(outcomingEdges.keySet(), incomingEdges.keySet()));
        HashSet<V> temp = new HashSet<>();
        while (!noIncomingEdges.isEmpty()) {
            V from = noIncomingEdges.poll();
            result.add(from);
            temp.clear();
            temp.addAll(outcomingEdges.get(from));
            for (V to: temp) {
                remove(from, to);
                if (incomingEdges.get(to).isEmpty()) {
                    noIncomingEdges.add(to);
                }
            }
        }
        if (!incomingEdges.isEmpty()) {
            throw new RuntimeException("cycles detected");
        }
        return result;
    }
}
