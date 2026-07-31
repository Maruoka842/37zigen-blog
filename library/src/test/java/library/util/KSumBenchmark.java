package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class KSumBenchmark {

    public static int[] threeSumFFT(long[] a, long target) {
        int n = a.length;
        if (n < 3) return null;

        long min = a[0];
        long max = a[0];
        for (int i = 1; i < n; i++) {
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }

        long R = max - min;
        long T = target - 3 * min;
        if (T < 0 || T > 3 * R) return null;

        int X = (int) T;
        long[] f = new long[X + 1];
        for (int i = 0; i < n; i++) {
            long val = a[i] - min;
            if (val <= X) {
                f[(int) val]++;
            }
        }
        for (int i = 0; i < f.length; i++) {
            f[i] = Math.min(f[i], 10);
        }
        long[] ff = library.util.polynomial.PolynomialFp.squared(f);

        library.util.collections.IntArrayList[] list = new library.util.collections.IntArrayList[X + 1];
        for (int i = 0; i < list.length; i++) {
			list[i] = new library.util.collections.IntArrayList();
        }
        for (int i = 0; i < n; i++) {
            long val = a[i] - min;
            if (val <= X) {
                list[(int) val].add(i);
            }
        }

        for (int i = n - 1; i >= 0; --i) {
            long valA = a[i] - min;
            if (valA > X) continue;
            int val = (int) valA;
            list[val].pollLast();
            int v = X - val;
            if (v < 0 || v >= ff.length || ff[v] == 0) continue;
            long cnt = ff[v];
            if (v >= val) {
                cnt -= 2 * f[v - val];
                if (v == 2 * val) {
                    cnt++;
                    cnt -= (f[val] - 1);
                } else {
                    if (v % 2 == 0) {
                        cnt -= f[v / 2];
                    }
                }
            } else {
                if (v % 2 == 0) {
                    cnt -= f[v / 2];
                }
            }
            if (cnt != 0) {
                // v = A[j] + A[k]
                for (int j = 0; j < i; j++) {
                    long valJ = a[j] - min;
                    if (valJ > v) continue;
                    int u = (int) (v - valJ);
                    if (u >= 0 && u < list.length && !list[u].isEmpty()) {
                        for (int kIdx = 0; kIdx < list[u].size(); kIdx++) {
                            int k = list[u].get(kIdx);
                            if (k != j && k != i) {
                                int[] ans = {j, k, i};
                                Arrays.sort(ans);
                                return ans;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void runBenchmark() {
        System.out.println("=== KSum.threeSum Template-based FFT Benchmark ===");
        Random rng = new Random(42);

        // Warm up
        for (int i = 0; i < 20; i++) {
            long[] test = new long[100];
            for (int j = 0; j < 100; j++) test[j] = rng.nextInt(200);
            threeSumFFT(test, 300);
        }

        // Test very large inputs (N = 10^6, R = 10^6)
        int N = 1000000;
        int R = 1000000;
        long[] a = new long[N];
        for (int i = 0; i < N; i++) {
            a[i] = rng.nextInt(R);
        }
        // pick three elements and sum them to form a guaranteed target
        long target = a[10] + a[500000] + a[999999];

        System.out.println("Running template-based threeSumFFT on N=10^6, R=10^6...");
        long t0 = System.nanoTime();
        int[] res = threeSumFFT(a, target);
        long t1 = System.nanoTime();
        double timeMs = (t1 - t0) / 1e6;

        assertNotNull(res);
        assertEquals(target, a[res[0]] + a[res[1]] + a[res[2]]);
        System.out.printf("Success! Found solution %s in %.3f ms%n", Arrays.toString(res), timeMs);
    }
}
