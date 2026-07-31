package library.util.segtree;

import library.util.monoid.BinaryInversionState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryInversionTest {

    @Test
    public void testBasic() {
        int N = 5;
        var seg = SegTreeFactory.flip_inversionCount(N);
        int[] A = {0, 1, 0, 0, 1};
        // 0 1 0 0 1
        // Inversions: (1, 0) at indices (1, 2) and (1, 3). Total 2.
        for (int i = 0; i < N; i++) {
            seg.set(i, BinaryInversionState.of(A[i]));
        }

        assertEquals(2, seg.fold(0, 5).c10);

        // Flip [0, 3): 1 0 1 0 1
        // (1, 0) pairs: (0, 1), (0, 3), (2, 3). Total 3.
        seg.act(0, 3, true);
        assertEquals(3, seg.fold(0, 5).c10);

        // Flip [2, 5): 1 0 0 1 0
        // (1, 0) pairs: (0, 1), (0, 2), (0, 4), (3, 4). Total 4.
        seg.act(2, 5, true);
        assertEquals(4, seg.fold(0, 5).c10);
    }

    @Test
    public void testRandom() {
        int N = 100;
        var seg = SegTreeFactory.flip_inversionCount(N);
        int[] A = new int[N];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < N; i++) {
            A[i] = rnd.nextInt(2);
            seg.set(i, BinaryInversionState.of(A[i]));
        }

        for (int q = 0; q < 100; q++) {
            int L = rnd.nextInt(N);
            int R = rnd.nextInt(N - L) + L + 1;
            if (rnd.nextBoolean()) {
                // flip
                seg.act(L, R, true);
                for (int i = L; i < R; i++) A[i] = 1 - A[i];
            } else {
                // query
                long expected = 0;
                long c1 = 0;
                for (int i = L; i < R; i++) {
                    if (A[i] == 0) expected += c1;
                    else c1++;
                }
                assertEquals(expected, seg.fold(L, R).c10);
            }
        }
    }
}
