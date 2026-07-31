package library.util.poset;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class BooleanLatticeTest {
    @Test
    public void testExpConsistency() {
        long mod = 998244353;
        int n = 4;
        long[] a = new long[1 << n];
        Random rnd = new Random(42);
        for (int i = 1; i < (1 << n); i++) {
            a[i] = rnd.nextInt((int) mod);
        }

        long[] resNew = BooleanLattice.exp(a, mod);
        long[] resNaive = BooleanLattice.expNaive(a, mod);

        assertArrayEquals(resNaive, resNew, "New exp should match naive exp");
    }

    @Test
    public void testExpSmall() {
        long mod = 998244353;
        int n = 2;
        long[] a = new long[1 << n];
        Random rnd = new Random(43);
        for (int i = 1; i < (1 << n); i++) {
            a[i] = rnd.nextInt((int) mod);
        }

        long[] resNew = BooleanLattice.exp(a, mod);
        long[] resNaive = BooleanLattice.expNaive(a, mod);

        assertArrayEquals(resNaive, resNew);
    }
}
