package library.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.Edge;

public class MaxMinTest {

    @Test
    public void testEmptyAndSingleNode() {
        // N = 0
        long[][] res0 = MaxMin.asps(new long[0][0]);
        assertEquals(0, res0.length);

        // N = 1
        long[][] cost1 = new long[][]{{Long.MAX_VALUE}};
        long[][] res1 = MaxMin.asps(cost1);
        assertEquals(1, res1.length);
        assertEquals(Long.MAX_VALUE, res1[0][0]);
    }

    @Test
    public void testSmallHandCraftedMaxMin() {
        // A small 3-node graph: 0 -> 1 with cost 10, 1 -> 2 with cost 5, 0 -> 2 with cost 3
        long[][] cost = new long[][] {
            {Long.MAX_VALUE, 10, 3},
            {Long.MIN_VALUE, Long.MAX_VALUE, 5},
            {Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE}
        };

        long[][] res = MaxMin.asps(cost);
        // Expected max bottleneck path from 0 to 2 is 0 -> 1 -> 2 with min(10, 5) = 5 (better than 0 -> 2 with cost 3)
        assertEquals(10, res[0][1]);
        assertEquals(5, res[1][2]);
        assertEquals(5, res[0][2]);
        assertEquals(Long.MIN_VALUE, res[2][0]); // unreachable
    }

    @Test
    public void testStressMaxMin() {
        Random rand = new Random(42);
        int numIterations = 100;

        for (int iter = 0; iter < numIterations; iter++) {
            int N = rand.nextInt(30) + 1; // 1 to 30 nodes
            double edgeProb = rand.nextDouble() * 0.5 + 0.1; // 10% to 60% density

            long[][] adjMaxMin = new long[N][N];
            long UNREACHABLE_MAXMIN = Long.MIN_VALUE;

            for (int i = 0; i < N; i++) {
                Arrays.fill(adjMaxMin[i], UNREACHABLE_MAXMIN);
                adjMaxMin[i][i] = Long.MAX_VALUE;
            }

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == j) continue;
                    if (rand.nextDouble() < edgeProb) {
                        long cost = rand.nextInt(100) + 1;
                        adjMaxMin[i][j] = cost;
                    }
                }
            }

            // Reference Floyd-Warshall for Max-Min
            long[][] expectedMaxMin = new long[N][N];
            for (int i = 0; i < N; i++) {
                System.arraycopy(adjMaxMin[i], 0, expectedMaxMin[i], 0, N);
            }
            for (int k = 0; k < N; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        long pathCost = Math.min(expectedMaxMin[i][k], expectedMaxMin[k][j]);
                        expectedMaxMin[i][j] = Math.max(expectedMaxMin[i][j], pathCost);
                    }
                }
            }

            // Test maxMin
            long[][] actualMaxMinFromAdj = MaxMin.asps(adjMaxMin);
            assertArrayEquals(expectedMaxMin, actualMaxMinFromAdj);
        }
    }

    @Test
    public void testBooleanProduct() {
        boolean[][] a = {
            {true, false, true},
            {false, true, false}
        };
        boolean[][] b = {
            {false, true},
            {true, false},
            {true, true}
        };
        boolean[][] expected = {
            {true, true},  // (T and F) or (F and T) or (T and T) = T, (T and T) or (F and F) or (T and T) = T
            {true, false}  // (F and F) or (T and T) or (F and T) = T, (F and T) or (T and F) or (F and T) = F
        };
        boolean[][] res = MaxMin.booleanProduct(a, b);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(expected[i], res[i]);
        }
    }

    @Test
    public void testMaxMinProduct() {
        long[][] a = {
            {10, 3, 5},
            {2, 8, 4}
        };
        long[][] b = {
            {4, 7},
            {6, 1},
            {9, 5}
        };
        // Expected product C_ij = max_k min(A_ik, B_kj)
        long[][] expected = {
            {5, 7},
            {6, 4}
        };
        long[][] res = MaxMin.mul(a, b);
        for (int i = 0; i < res.length; i++) {
            assertArrayEquals(expected[i], res[i]);
        }
    }

    @Test
    public void testMulNaiveCorrectness() {
        Random rand = new Random(42);
        int numIterations = 50;
        for (int iter = 0; iter < numIterations; iter++) {
            int n = rand.nextInt(40) + 1;
            int m = rand.nextInt(40) + 1;
            int kCols = rand.nextInt(40) + 1;

            long[][] a = new long[n][m];
            long[][] b = new long[m][kCols];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    a[i][j] = rand.nextBoolean() ? Long.MIN_VALUE : rand.nextInt(100);
                }
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < kCols; j++) {
                    b[i][j] = rand.nextBoolean() ? Long.MIN_VALUE : rand.nextInt(100);
                }
            }

            long[][] resNaive = MaxMin.mulNaive(a, b);
            long[][] resMul = MaxMin.mul(a, b);

            for (int i = 0; i < n; i++) {
                assertArrayEquals(resNaive[i], resMul[i]);
            }
        }
    }

    @Test
    public void runMatrixMulBenchmark() {
        Random rand = new Random(42);
        int[] testSizes = {10, 30, 50, 70, 100, 120};
        System.out.println("----- STARTING MATRIX MUL BENCHMARK -----");

        for (int dim : testSizes) {
            long[][] a = new long[dim][dim];
            long[][] b = new long[dim][dim];

            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    a[i][j] = rand.nextBoolean() ? Long.MIN_VALUE : rand.nextInt(100);
                    b[i][j] = rand.nextBoolean() ? Long.MIN_VALUE : rand.nextInt(100);
                }
            }

            // Warmup
            for (int i = 0; i < 20; i++) {
                MaxMin.mulNaive(a, b);
                // We bypass dynamic branching check to benchmark both actual full-algorithm runs
                runFullAlgorithmMul(a, b);
            }

            int runs = 100;
            if (dim >= 100) runs = 30;

            // Naive
            long startNaive = System.nanoTime();
            for (int i = 0; i < runs; i++) {
                MaxMin.mulNaive(a, b);
            }
            long endNaive = System.nanoTime();
            double avgNaive = (endNaive - startNaive) / (double) runs / 1_000_000.0;

            // Full Bitset/Event-driven
            long startBitset = System.nanoTime();
            for (int i = 0; i < runs; i++) {
                runFullAlgorithmMul(a, b);
            }
            long endBitset = System.nanoTime();
            double avgBitset = (endBitset - startBitset) / (double) runs / 1_000_000.0;

            System.out.printf("Dim = %3d | Naive: %7.4f ms | Bitset-Event: %7.4f ms | Ratio (Bitset/Naive): %5.2f%n",
                    dim, avgNaive, avgBitset, avgBitset / avgNaive);
        }
        System.out.println("----- END MATRIX MUL BENCHMARK -----");
    }

    // Helper to force execution of the non-naive bitset algorithm
    private long[][] runFullAlgorithmMul(long[][] a, long[][] b) {
        int n = a.length;
        int m = a[0].length;
        int kCols = b[0].length;
        long[][] c = new long[n][kCols];

        int numA = n * m;
        int numB = m * kCols;
        int totalEvents = numA + numB;
        long[] allVals = new long[totalEvents];
        int ptr = 0;
        for (int i = 0; i < n; i++) {
            long[] rowA = a[i];
            for (int k = 0; k < m; k++) {
                allVals[ptr++] = rowA[k];
            }
        }
        for (int k = 0; k < m; k++) {
            long[] rowB = b[k];
            for (int j = 0; j < kCols; j++) {
                allVals[ptr++] = rowB[j];
            }
        }
        Arrays.sort(allVals);

        int uniqueCount = 0;
        for (int i = 0; i < totalEvents; i++) {
            if (i == 0 || allVals[i] != allVals[i - 1]) {
                uniqueCount++;
			}
		}
		long[] ascUniqueVals = new long[uniqueCount];
		int uPtr = 0;
		for (int i = 0; i < totalEvents; i++) {
			if (i == 0 || allVals[i] != allVals[i - 1]) {
				ascUniqueVals[uPtr++] = allVals[i];
			}
		}

		long[] uniqueVals = new long[uniqueCount];
		for (int i = 0; i < uniqueCount; i++) {
			uniqueVals[i] = ascUniqueVals[uniqueCount - 1 - i];
		}

		int[] countA = new int[uniqueCount];
		int[] countB = new int[uniqueCount];
		int[] ranksA = new int[numA];
		int[] ranksB = new int[numB];

		int ptrA = 0;
		for (int i = 0; i < n; i++) {
			long[] rowA = a[i];
			for (int k = 0; k < m; k++) {
				int ascRank = Arrays.binarySearch(ascUniqueVals, rowA[k]);
				int descRank = uniqueCount - 1 - ascRank;
				ranksA[ptrA++] = descRank;
				countA[descRank]++;
			}
		}

		int ptrB = 0;
		for (int k = 0; k < m; k++) {
			long[] rowB = b[k];
			for (int j = 0; j < kCols; j++) {
				int ascRank = Arrays.binarySearch(ascUniqueVals, rowB[j]);
				int descRank = uniqueCount - 1 - ascRank;
				ranksB[ptrB++] = descRank;
				countB[descRank]++;
			}
		}

		int[] offsetA = new int[uniqueCount + 1];
		int[] offsetB = new int[uniqueCount + 1];
		for (int r = 0; r < uniqueCount; r++) {
			offsetA[r + 1] = offsetA[r] + countA[r];
			offsetB[r + 1] = offsetB[r] + countB[r];
		}

		long[] packedA = new long[numA];
		long[] packedB = new long[numB];

		int[] curA = offsetA.clone();
		int[] curB = offsetB.clone();

		ptrA = 0;
		for (int i = 0; i < n; i++) {
			for (int k = 0; k < m; k++) {
				int descRank = ranksA[ptrA++];
				packedA[curA[descRank]++] = ((long) i << 32) | k;
			}
		}

		ptrB = 0;
		for (int k = 0; k < m; k++) {
			for (int j = 0; j < kCols; j++) {
				int descRank = ranksB[ptrB++];
				packedB[curB[descRank]++] = ((long) k << 32) | j;
			}
		}

		int LN = (n + 63) / 64;
		int LK = (kCols + 63) / 64;

		long[][] colA = new long[m][LN];
		long[][] rowB = new long[m][LK];
		long[][] rowRes = new long[n][LK];
		long[][] colRes = new long[kCols][LN];

		for (int i = 0; i < n; i++) {
			Arrays.fill(c[i], Long.MIN_VALUE);
		}

		for (int r = 0; r < uniqueCount; r++) {
			long v = uniqueVals[r];

			int startA = offsetA[r];
			int endA = offsetA[r + 1];
			for (int idx = startA; idx < endA; idx++) {
				long packed = packedA[idx];
				int i = (int) (packed >>> 32);
				int k = (int) (packed & 0xFFFFFFFFL);

				colA[k][i >>> 6] |= (1L << (i & 63));

				long[] rowResI = rowRes[i];
				long[] rowBK = rowB[k];
				for (int w = 0; w < LK; w++) {
					long old = rowResI[w];
					long rowVal = rowBK[w];
					long diff = rowVal & ~old;
					if (diff != 0) {
						rowResI[w] = old | rowVal;
						long tempDiff = diff;
						while (tempDiff != 0) {
							long lsb = tempDiff & -tempDiff;
							int localJ = Long.numberOfTrailingZeros(lsb);
							int j = (w << 6) | localJ;
							c[i][j] = v;
							colRes[j][i >>> 6] |= (1L << (i & 63));
							tempDiff ^= lsb;
						}
					}
				}
			}

			int startB = offsetB[r];
			int endB = offsetB[r + 1];
			for (int idx = startB; idx < endB; idx++) {
				long packed = packedB[idx];
				int k = (int) (packed >>> 32);
				int j = (int) (packed & 0xFFFFFFFFL);

				rowB[k][j >>> 6] |= (1L << (j & 63));

				long[] colResJ = colRes[j];
				long[] colAK = colA[k];
				for (int w = 0; w < LN; w++) {
					long old = colResJ[w];
					long colVal = colAK[w];
					long diff = colVal & ~old;
					if (diff != 0) {
						colResJ[w] = old | colVal;
						long tempDiff = diff;
						while (tempDiff != 0) {
							long lsb = tempDiff & -tempDiff;
							int localI = Long.numberOfTrailingZeros(lsb);
							int i = (w << 6) | localI;
							c[i][j] = v;
							rowRes[i][j >>> 6] |= (1L << (j & 63));
							tempDiff ^= lsb;
						}
					}
				}
			}
		}

		return c;
    }

    @Test
    public void runBenchmark() {
        Random rand = new Random(42);
        int[] testNs = {10, 20, 30, 60, 100, 200};
        System.out.println("----- STARTING BENCHMARK -----");

        for (int N : testNs) {
            double edgeProb = 0.3; // moderate density
            long[][] adjMaxMin = new long[N][N];
            long UNREACHABLE_MAXMIN = Long.MIN_VALUE;
            for (int i = 0; i < N; i++) {
                Arrays.fill(adjMaxMin[i], UNREACHABLE_MAXMIN);
                adjMaxMin[i][i] = Long.MAX_VALUE;
            }

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == j) continue;
                    if (rand.nextDouble() < edgeProb) {
                        long cost = rand.nextInt(100) + 1;
                        adjMaxMin[i][j] = cost;
                    }
                }
            }

            // Warmup
            for (int i = 0; i < 50; i++) {
                runFloydWarshallMaxMin(adjMaxMin);
                runBitsetMaxMin(adjMaxMin);
            }

            // Benchmark Floyd-Warshall
            int runs = 100;
            long startFW = System.nanoTime();
            for (int i = 0; i < runs; i++) {
                runFloydWarshallMaxMin(adjMaxMin);
            }
            long endFW = System.nanoTime();
            double avgFW = (endFW - startFW) / (double) runs / 1_000_000.0;

            // Benchmark Bitset
            long startBS = System.nanoTime();
            for (int i = 0; i < runs; i++) {
                runBitsetMaxMin(adjMaxMin);
            }
            long endBS = System.nanoTime();
            double avgBS = (endBS - startBS) / (double) runs / 1_000_000.0;

            System.out.printf("N = %3d | Floyd-Warshall: %7.4f ms | Bitset: %7.4f ms | Ratio (BS/FW): %5.2f%n",
                    N, avgFW, avgBS, avgBS / avgFW);
        }
        System.out.println("----- END BENCHMARK -----");
    }

    private long[][] runFloydWarshallMaxMin(long[][] adj) {
        return MaxMin.warshalFloyd(adj);
    }

    private long[][] runBitsetMaxMin(long[][] adj) {
        return MaxMin.asps(adj);
    }
}
