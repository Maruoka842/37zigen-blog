package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class TwoPointerMethodTest {

    @Test
    public void testCountGeqLong() {
        // Simple manual tests
        long[] u = {1, 3, 5};
        long[] v = {2, 4, 6};

        // binary search path: u.length = 3, v.length = 3. 3 * log(3) approx 3 * 2 = 6, 3 + 3 = 6.
        // Let's force binary search by making one array very small and one very large, and vice versa.

        // Equal size
        assertEquals(9, TwoPointerMethod.countGeq(u, v, 3)); // all pairs sum >= 3
        assertEquals(6, TwoPointerMethod.countGeq(u, v, 6)); // (1,6), (3,4), (3,6), (5,2), (5,4), (5,6) etc. 5+2=7 >= 6, 5+4=9 >= 6, 5+6=11 >= 6.
        assertEquals(0, TwoPointerMethod.countGeq(u, v, 12)); // none sum >= 12 (max is 5+6=11)

        // Assertions for sortedness
        long[] unsorted = {3, 1, 5};
        assertThrows(AssertionError.class, () -> TwoPointerMethod.countGeq(unsorted, v, 5));
        assertThrows(AssertionError.class, () -> TwoPointerMethod.countGeq(u, unsorted, 5));
    }

    @Test
    public void testCountGeqDouble() {
        double[] u = {1.5, 3.5, 5.5};
        double[] v = {2.0, 4.0, 6.0};

        assertEquals(9, TwoPointerMethod.countGeq(u, v, 3.0));
        assertEquals(6, TwoPointerMethod.countGeq(u, v, 7.5)); // (3.5, 4.0)=7.5, (3.5, 6.0)=9.5, (5.5, 2.0)=7.5, (5.5, 4.0)=9.5, (5.5, 6.0)=11.5, (1.5, 6.0)=7.5 -> 6 pairs
        assertEquals(0, TwoPointerMethod.countGeq(u, v, 12.0));

        double[] unsorted = {3.0, 1.0, 5.0};
        assertThrows(AssertionError.class, () -> TwoPointerMethod.countGeq(unsorted, v, 5.0));
        assertThrows(AssertionError.class, () -> TwoPointerMethod.countGeq(u, unsorted, 5.0));
    }

    @Test
    public void testCountGeqLongStressAndComplexityPaths() {
        Random rng = new Random(42);

        // Test varying relative sizes
        int[] sizes = {0, 1, 5, 10, 100, 1000};
        for (int lenA : sizes) {
            for (int lenB : sizes) {
                long[] u = new long[lenA];
                long[] v = new long[lenB];
                for (int i = 0; i < lenA; i++) u[i] = rng.nextInt(200) - 100;
                for (int i = 0; i < lenB; i++) v[i] = rng.nextInt(200) - 100;
                Arrays.sort(u);
                Arrays.sort(v);

                long x = rng.nextInt(200) - 100;

                // Compare optimized countGeq against a naive O(N*M) implementation
                long expected = 0;
                for (long ui : u) {
                    for (long vi : v) {
                        if (ui + vi >= x) {
                            expected++;
                        }
                    }
                }

                long actual = TwoPointerMethod.countGeq(u, v, x);
                assertEquals(expected, actual, "Failed for sizes u=" + lenA + ", v=" + lenB + " with target " + x);
            }
        }
    }

    @Test
    public void testCountGeqDoubleStressAndComplexityPaths() {
        Random rng = new Random(43);

        int[] sizes = {0, 1, 5, 10, 100, 1000};
        for (int lenA : sizes) {
            for (int lenB : sizes) {
                double[] u = new double[lenA];
                double[] v = new double[lenB];
                for (int i = 0; i < lenA; i++) u[i] = rng.nextDouble() * 200 - 100;
                for (int i = 0; i < lenB; i++) v[i] = rng.nextDouble() * 200 - 100;
                Arrays.sort(u);
                Arrays.sort(v);

                double x = rng.nextDouble() * 200 - 100;

                long expected = 0;
                for (double ui : u) {
                    for (double vi : v) {
                        if (ui + vi >= x) {
                            expected++;
                        }
                    }
                }

                long actual = TwoPointerMethod.countGeq(u, v, x);
                assertEquals(expected, actual, "Failed for sizes u=" + lenA + ", v=" + lenB + " with target " + x);
            }
        }
    }
}
