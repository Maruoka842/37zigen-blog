package library.util.linalg;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PrefixLinearBasisOverF2 の動作確認を行うユニットテスト。
 */
public class PrefixLinearBasisOverF2Test {

    @Test
    public void testBasic() {
        long[] a = {1, 2, 4, 8};
        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        assertEquals(4, plb.size());

        // [0, 4) should have basis [8, 4, 2, 1]
        long[] basis = plb.basis(0, 4);
        assertArrayEquals(new long[]{8, 4, 2, 1}, basis);

        // maxXor for [0, 4) should be 15
        assertEquals(15, plb.maxXor(0, 4));

        // query for [1, 3) -> {2, 4}
        long[] basisSub = plb.basis(1, 3);
        assertArrayEquals(new long[]{4, 2}, basisSub);
        assertEquals(6, plb.maxXor(1, 3));
    }

    @Test
    public void testDependent() {
        long[] a = {3, 5, 6}; // 3, 5, 3^5=6 -> linearly dependent
        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        assertEquals(3, plb.size());
        assertEquals(2, plb.rank(0, 3)); // basis size should be 2, not 3
        assertEquals(6, plb.maxXor(0, 3)); // Max possible XOR is 6

        assertTrue(plb.contains(0, 3, 3));
        assertTrue(plb.contains(0, 3, 5));
        assertTrue(plb.contains(0, 3, 6));
        assertTrue(plb.contains(0, 3, 0));
        assertFalse(plb.contains(0, 3, 4));
    }

    @Test
    public void testEmptyAndSingle() {
        long[] a = {42};
        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        assertEquals(1, plb.size());
        assertEquals(0, plb.basis(0, 0).length);
        assertEquals(0, plb.maxXor(0, 0));
        assertTrue(plb.contains(0, 0, 0));
        assertFalse(plb.contains(0, 0, 42));

        assertEquals(1, plb.basis(0, 1).length);
        assertEquals(42, plb.maxXor(0, 1));
        assertTrue(plb.contains(0, 1, 42));
    }

    @Test
    public void testReduce() {
        long[] a = {3, 5, 6}; // basis: {3, 5} -> (binary 011 and 101)
        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        // x = 7 (111). Since 5 (101) is in basis, 7 ^ 5 = 2. Since 3 (011) is in basis, 2 ^ 3 = 1. Output should be 1.
        assertEquals(1, plb.reduce(0, 3, 7));
        assertEquals(0, plb.reduce(0, 3, 6)); // 6 can be fully reduced to 0 because 3^5 = 6
        assertEquals(4, plb.reduce(0, 1, 7)); // base is {3}. 7^3 = 4. 4 cannot be reduced further.
    }

    @Test
    public void testUnsignedMaxXor() {
        // 1L << 63 has 63rd bit set, which is negative in signed representation
        long val1 = 1L << 63;
        long val2 = 123456789L;
        long[] a = {val1, val2};
        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        long maxPlb = plb.maxXor(0, 2);
        long maxExpected = val1 ^ val2; // since both are independent and the 63rd bit should be kept for unsigned max
        assertEquals(maxExpected, maxPlb);
    }

    @Test
    public void testStressAgainstNaive() {
        Random rand = new Random(42);
        int N = 100;
        long[] a = new long[N];
        for (int i = 0; i < N; i++) {
            a[i] = rand.nextLong(); // full 64-bit including sign/63rd bit
        }

        PrefixLinearBasisOverF2 plb = new PrefixLinearBasisOverF2(a);

        for (int step = 0; step < 1000; step++) {
            int l = rand.nextInt(N);
            int r = rand.nextInt(N - l) + l + 1; // [l, r)

            long[] basis = plb.basis(l, r);
            verifyBasisCorrectness(a, l, r, basis);

            // test maxXor
            long maxPlb = plb.maxXor(l, r);
            long maxNaive = maxXorNaive(basis);
            assertEquals(maxNaive, maxPlb);

            // test contains and reduce
            for (int k = 0; k < 10; k++) {
                long x = rand.nextLong();
                boolean containsPlb = plb.contains(l, r, x);
                boolean containsNaive = containsNaive(basis, x);
                assertEquals(containsNaive, containsPlb);

                long reducedPlb = plb.reduce(l, r, x);
                long reducedNaive = reduceNaive(basis, x);
                assertEquals(reducedNaive, reducedPlb);
            }
        }
    }

    private void verifyBasisCorrectness(long[] a, int l, int r, long[] basis) {
        // 1. All elements in basis are linearly independent
        for (int i = 0; i < basis.length; i++) {
            long val = basis[i];
            long x = val;
            for (int j = 0; j < basis.length; j++) {
                if (i != j && Long.compareUnsigned(x ^ basis[j], x) < 0) {
                    x ^= basis[j];
                }
            }
            assertNotEquals(0, x);
        }

        // 2. All elements in a[l..r) can be represented by basis
        for (int i = l; i < r; i++) {
            assertTrue(containsNaive(basis, a[i]), "basis should contain " + a[i]);
        }

        // 3. Size is minimal (meaning size of basis = rank of a[l..r))
        // We can check that the number of elements in basis matches the rank found by a simple greedy algorithm on a[l..r)
        long[] naiveBasis = getNaiveBasis(a, l, r);
        assertEquals(naiveBasis.length, basis.length);
    }

    private long[] getNaiveBasis(long[] a, int l, int r) {
        long[] b = new long[64];
        int count = 0;
        for (int i = l; i < r; i++) {
            long val = a[i];
            for (int bit = 63; bit >= 0; bit--) {
                if (((val >>> bit) & 1) == 1) {
                    if (b[bit] == 0) {
                        b[bit] = val;
                        count++;
                        break;
                    }
                    val ^= b[bit];
                }
            }
        }
        long[] res = new long[count];
        int idx = 0;
        for (int bit = 63; bit >= 0; bit--) {
            if (b[bit] != 0) {
                res[idx++] = b[bit];
            }
        }
        return res;
    }

    private boolean containsNaive(long[] basis, long x) {
        for (long b : basis) {
            if (Long.compareUnsigned(x ^ b, x) < 0) {
                x ^= b;
            }
        }
        return x == 0;
    }

    private long reduceNaive(long[] basis, long x) {
        for (long b : basis) {
            if (Long.compareUnsigned(x ^ b, x) < 0) {
                x ^= b;
            }
        }
        return x;
    }

    private long maxXorNaive(long[] basis) {
        long res = 0;
        for (long b : basis) {
            if (Long.compareUnsigned(res ^ b, res) > 0) {
                res ^= b;
            }
        }
        return res;
    }
}
