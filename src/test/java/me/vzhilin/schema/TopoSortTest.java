package me.vzhilin.schema;

import com.google.common.collect.Lists;
import me.vzhilin.util.TopologicalSort;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public final class TopoSortTest {
    @Test
    public void simple() {
        TopologicalSort<Integer> g = new TopologicalSort<>();
        g.put(1, 2);
        g.put(2, 3);
        g.put(3, 4);
        g.put(4, 5);

        g.put(1, 5);
        g.put(2, 4);
        g.put(3, 5);

        assertThat(g.topoSort(), equalTo(Lists.newArrayList(1, 2, 3, 4, 5)));
    }
}
