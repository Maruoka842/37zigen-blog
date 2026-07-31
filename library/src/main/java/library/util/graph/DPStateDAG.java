package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import library.util.collections.HashStrategies;
import library.util.collections.OpenHashMap;

public class DPStateDAG {
    public static class Edge {
        public final int[] from;
        public final int[] to;
        public final long cost;
        public final String type;

        public Edge(int[] from, int[] to, long cost, String type) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.type = type;
        }

        @Override
        public String toString() {
            return java.util.Arrays.toString(from) + " -> " + java.util.Arrays.toString(to)
                    + " cost=" + cost + " type=" + type;
        }
    }

    public static class PathResult {
        public final boolean reachable;
        public final long cost;
        public final List<Edge> path;

        private PathResult(boolean reachable, long cost, List<Edge> path) {
            this.reachable = reachable;
            this.cost = cost;
            this.path = path;
        }

        public static PathResult unreachable() {
            return new PathResult(false, Long.MAX_VALUE, List.of());
        }

        public static PathResult of(long cost, List<Edge> path) {
            return new PathResult(true, cost, path);
        }
        
        public void dump() {
            if (!reachable) {
                System.out.println("--- Path Result: UNREACHABLE ---");
                return;
            }
            System.out.println("--- Path Result: REACHABLE ---");
            System.out.println("Total Cost: " + cost);
            System.out.println("Steps: " + path.size());
            for (int i = 0; i < path.size(); i++) {
                Edge e = path.get(i);
                System.out.printf("  [%2d] %s -> %s (cost: %d, type: %s)%n", 
                    i, 
                    Arrays.toString(e.from), 
                    Arrays.toString(e.to), 
                    e.cost, 
                    e.type);
            }
            System.out.println("------------------------------");
        }
    }

    private final OpenHashMap<int[], List<Edge>> adj = new OpenHashMap<>(HashStrategies.INT_ARRAY);
    private final OpenHashMap<int[], Integer> indegree = new OpenHashMap<>(HashStrategies.INT_ARRAY);

    public void addVertex(int[] v) {
        ensureVertex(v);
    }

    public void addEdge(int[] from, int[] to, long cost, String type) {
        ensureVertex(from);
        ensureVertex(to);
        adj.get(from).add(new Edge(from, to, cost, type));
        indegree.put(to, indegree.get(to) + 1);
    }

    private void ensureVertex(int[] v) {
        if (!adj.containsKey(v)) {
            adj.put(v, new ArrayList<>());
        }
        if (!indegree.containsKey(v)) {
            indegree.put(v, 0);
        }
    }

    public PathResult shortestPathBetween(int[] from, int[] to) {
        if (!adj.containsKey(from) || !adj.containsKey(to)) {
            return PathResult.unreachable();
        }

        List<int[]> topo = topologicalOrder();

        OpenHashMap<int[], Long> dist = new OpenHashMap<>(HashStrategies.INT_ARRAY);
        OpenHashMap<int[], Edge> prev = new OpenHashMap<>(HashStrategies.INT_ARRAY);
        dist.put(from, 0L);

        for (int[] v : topo) {
        	if (!dist.containsKey(v)) {
        		continue;
            }
            long base = dist.get(v);
            for (Edge e : adj.getOrDefaultValue(v, List.of())) {
                long cand = base + e.cost;
                if (!dist.containsKey(e.to) || cand < dist.get(e.to)) {
                    dist.put(e.to, cand);
                    prev.put(e.to, e);
                }
            }
        }

        if (!dist.containsKey(to)) {
            return PathResult.unreachable();
        }

        List<Edge> path = new ArrayList<>();
        int[] cur = to;
        while (!Objects.deepEquals(cur, from)) {
            Edge e = prev.get(cur);
            path.add(e);
            cur = e.from;
        }
        Collections.reverse(path);

        return PathResult.of(dist.get(to), path);
    }

    private List<int[]> topologicalOrder() {
        OpenHashMap<int[], Integer> indeg = new OpenHashMap<>(HashStrategies.INT_ARRAY);
        indegree.forEach(indeg::put);

        ArrayDeque<int[]> q = new ArrayDeque<>();
        indeg.forEach((v, d) -> {
            if (d == 0) {
                q.addLast(v);
            }
        });

        List<int[]> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int[] v = q.removeFirst();
            order.add(v);
            for (Edge e : adj.getOrDefaultValue(v, List.of())) {
                int d = indeg.get(e.to) - 1;
                indeg.put(e.to, d);
                if (d == 0) {
                    q.addLast(e.to);
                }
            }
        }
        return order;
    }
}
