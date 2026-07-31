package library.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.Edge;

public class MinMaxTest {


    @Test
    public void testStressMinMax() {
        Random rand = new Random(42);
        int numIterations = 100;

        for (int iter = 0; iter < numIterations; iter++) {
            int N = rand.nextInt(30) + 1; // 1 to 30 nodes
            double edgeProb = rand.nextDouble() * 0.5 + 0.1; // 10% to 60% density

            long[][] adjMinMax = new long[N][N];
            long UNREACHABLE_MINMAX = Long.MAX_VALUE / 3;

            for (int i = 0; i < N; i++) {
                Arrays.fill(adjMinMax[i], UNREACHABLE_MINMAX);
                adjMinMax[i][i] = 0;
            }

            List<Edge> edges = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == j) continue;
                    if (rand.nextDouble() < edgeProb) {
                        long cost = rand.nextInt(100) + 1;
                        adjMinMax[i][j] = cost;
                        edges.add(new Edge(i, j, cost));
                    }
                }
            }

            // Reference Floyd-Warshall for Min-Max
            long[][] expectedMinMax = new long[N][N];
            for (int i = 0; i < N; i++) {
                System.arraycopy(adjMinMax[i], 0, expectedMinMax[i], 0, N);
            }
            for (int k = 0; k < N; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        long pathCost = Math.max(expectedMinMax[i][k], expectedMinMax[k][j]);
                        expectedMinMax[i][j] = Math.min(expectedMinMax[i][j], pathCost);
                    }
                }
            }

            // Test minMax
            long[][] actualMinMaxFromAdj = MinMax.asps(adjMinMax);
            assertArrayEquals(expectedMinMax, actualMinMaxFromAdj);
        }
    }
}