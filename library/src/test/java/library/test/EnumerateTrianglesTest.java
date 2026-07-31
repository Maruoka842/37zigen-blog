package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import library.util.graph.Graph;
import library.util.graph.Graphs;
import org.junit.jupiter.api.Test;

public class EnumerateTrianglesTest {

    @Test
    public void testCompleteGraphK3() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        List<String> triangles = new ArrayList<>();
        Graphs.enumerateTriangles(g, (u, v, w) -> {
            List<Integer> list = new ArrayList<>();
            list.add(u); list.add(v); list.add(w);
            Collections.sort(list);
            triangles.add(list.toString());
        });
        assertEquals(1, triangles.size());
        assertEquals("[0, 1, 2]", triangles.get(0));
    }

    @Test
    public void testCompleteGraphK4() {
        int N = 4;
        Graph g = new Graph(N);
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                g.addEdge(i, j);
            }
        }
        List<String> triangles = new ArrayList<>();
        Graphs.enumerateTriangles(g, (u, v, w) -> {
            List<Integer> list = new ArrayList<>();
            list.add(u); list.add(v); list.add(w);
            Collections.sort(list);
            triangles.add(list.toString());
        });
        Collections.sort(triangles);
        // K4 has 4C3 = 4 triangles
        assertEquals(4, triangles.size());
        assertEquals("[0, 1, 2]", triangles.get(0));
        assertEquals("[0, 1, 3]", triangles.get(1));
        assertEquals("[0, 2, 3]", triangles.get(2));
        assertEquals("[1, 2, 3]", triangles.get(3));
    }

    @Test
    public void testCycleC4() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 0);
        List<String> triangles = new ArrayList<>();
        Graphs.enumerateTriangles(g, (u, v, w) -> triangles.add("" + u + v + w));
        assertEquals(0, triangles.size());
    }

    @Test
    public void testTwoTrianglesWithCommonEdge() {
        // 0-1, 1-2, 2-0 (triangle 1)
        // 0-1, 1-3, 3-0 (triangle 2)
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        g.addEdge(1, 3);
        g.addEdge(3, 0);
        List<String> triangles = new ArrayList<>();
        Graphs.enumerateTriangles(g, (u, v, w) -> {
            List<Integer> list = new ArrayList<>();
            list.add(u); list.add(v); list.add(w);
            Collections.sort(list);
            triangles.add(list.toString());
        });
        Collections.sort(triangles);
        assertEquals(2, triangles.size());
        assertEquals("[0, 1, 2]", triangles.get(0));
        assertEquals("[0, 1, 3]", triangles.get(1));
    }
}
