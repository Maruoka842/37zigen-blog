package library.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.graph.LinearMatroidParity;
import library.util.graph.LinearMatroidParity.VectorPair;
import library.util.linalg.MatrixUtilsFp;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class LinearMatroidParityTest {

    @Test
    public void testEmpty() {
        long mod = 998244353;
        assertEquals(0, LinearMatroidParity.size(new VectorPair[0], mod));
        boolean[] res = LinearMatroidParity.solve(new VectorPair[0], mod);
        assertEquals(0, res.length);
    }

    @Test
    public void testSimple() {
        long mod = 998244353;
        // 2D space, 1 pair of unit vectors
        VectorPair p1 = new VectorPair(new long[]{1, 0}, new long[]{0, 1});
        VectorPair[] bcs = {p1};

        assertEquals(1, LinearMatroidParity.size(bcs, mod));
        boolean[] res = LinearMatroidParity.solve(bcs, mod);
        assertArrayEquals(new boolean[]{true}, res);
    }

    @Test
    public void testDependent() {
        long mod = 998244353;
        // 2D space, 2 identical pairs
        VectorPair p1 = new VectorPair(new long[]{1, 0}, new long[]{0, 1});
        VectorPair p2 = new VectorPair(new long[]{1, 0}, new long[]{0, 1});
        VectorPair[] bcs = {p1, p2};

        assertEquals(1, LinearMatroidParity.size(bcs, mod));
        boolean[] res = LinearMatroidParity.solve(bcs, mod);
        // Lexicographically smallest choice that gives max matching
        // Choice {p1: false, p2: true} is lexicographically smaller than {p1: true, p2: false} if we consider the binary vector.
        // Wait, lexicographically smallest binary vector means we want as many 0s as possible at the beginning?
        // Let's check the implementation logic.
        // try_erase(i) is called for i from 0 to m-1.
        // If it can erase i without reducing rank, it sets ret[i] = false.
        // So for {p1, p2}, it tries to erase p1. p2 alone has rank 2, so p1 is erased.
        // Then it tries to erase p2. But without p1 and p2, rank is 0. So it keeps p2.
        // Result should be {false, true}.
        assertArrayEquals(new boolean[]{false, true}, res);
    }

    @Test
    public void test3D() {
        long mod = 998244353;
        // 3D space, should be padded to 4D internally.
        VectorPair p1 = new VectorPair(new long[]{1, 0, 0}, new long[]{0, 1, 0});
        VectorPair p2 = new VectorPair(new long[]{0, 0, 1}, new long[]{1, 1, 1});
        VectorPair[] bcs = {p1, p2};

        // rank of {p1, p2} in 3D.
        // b1=(1,0,0), c1=(0,1,0), b2=(0,0,1), c2=(1,1,1)
        // These 4 vectors are: (1,0,0), (0,1,0), (0,0,1), (1,1,1)
        // (1,1,1) = (1,0,0) + (0,1,0) + (0,0,1).
        // So they span 3D space.
        // Maximum parity matching size is floor(rank/2) in general? No, it's about pairs.
        // A matching of size 1 uses 2 vectors. A matching of size 2 uses 4 vectors.
        // Here we have 4 vectors spanning 3D space. Any 3 are independent.
        // But we must pick pairs.
        // {p1} is independent (rank 2).
        // {p2} is independent (rank 2).
        // {p1, p2} rank is 3.
        // Linear Matroid Parity: find max number of pairs such that their union is independent.
        // Union of {p1, p2} has rank 3, but there are 4 vectors. So they are dependent.
        // Max size is 1.

        assertEquals(1, LinearMatroidParity.size(bcs, mod));
        boolean[] res = LinearMatroidParity.solve(bcs, mod);
        // tries to erase p1. p2 has rank 2. 1 == 1, so p1 erased.
        // result {false, true}
        assertArrayEquals(new boolean[]{false, true}, res);
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        long mod = 998244353;
        int numTrials = 100;

        for (int t = 0; t < numTrials; t++) {
            int r = rnd.nextInt(10) + 2;
            int m = rnd.nextInt(15) + 1;
            VectorPair[] bcs = new VectorPair[m];
            for (int i = 0; i < m; i++) {
                long[] b = new long[r];
                long[] c = new long[r];
                for (int j = 0; j < r; j++) {
                    b[j] = rnd.nextLong(mod);
                    c[j] = rnd.nextLong(mod);
                }
                bcs[i] = new VectorPair(b, c);
            }

            int expectedSize = LinearMatroidParity.size(bcs, mod, rnd.nextLong());
            boolean[] solved = LinearMatroidParity.solve(bcs, mod, rnd.nextLong());
            int count = 0;
            for (boolean b : solved) if (b) count++;
            assertEquals(expectedSize, count, "Matching size mismatch in trial " + t);
            
            // Verify independence
            List<long[]> vectors = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                if (solved[i]) {
                    vectors.add(bcs[i].b());
                    vectors.add(bcs[i].c());
                }
            }
            if (count > 0) {
                long[][] mat = new long[vectors.size()][r];
                for (int i = 0; i < vectors.size(); i++) {
                    mat[i] = vectors.get(i);
                }
                int rank = MatrixUtilsFp.rank(mat, mod);
                assertEquals(2 * count, rank, "Matching not independent in trial " + t);
            }
        }
    }
}
