package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import library.util.collections.OpenHashMap;

public class ZerosOnesRewritingStressTest {

    @Test
    public void stressTest() {
        Random rnd = new Random(42);
        for (int n = 1; n <= 8; n++) {
            for (int k = 1; k <= n; k++) {
                ImplicitDigraph<int[]> graph = ReconfigurationProblems.zerosOnesRewriting(n, k);

                for (int iter = 0; iter < 5; iter++) {
                    int[] start = new int[n];
                    for (int i = 0; i < n; i++) start[i] = rnd.nextInt(2);

                    OpenHashMap<int[], Integer> bfsDists = graph.bfsDistances(start);

                    for (int i = 0; i < (1 << n); i++) {
                        int[] target = new int[n];
                        for (int j = 0; j < n; j++) target[j] = (i >> j) & 1;

                        boolean reachable = bfsDists.containsKey(target);
                        assertEquals(reachable, graph.onPath(start, target),
                            "onPath fail: n=" + n + ", k=" + k + ", start=" + Arrays.toString(start) + ", target=" + Arrays.toString(target));

                        if ((k == 1 || k == 2) && reachable) {
                            assertEquals((int)bfsDists.get(target), graph.dist(start, target),
                                "dist fail: n=" + n + ", k=" + k + ", start=" + Arrays.toString(start) + ", target=" + Arrays.toString(target));
                        }
                    }
                }
            }
        }
    }
}
