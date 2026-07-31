package library.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class KValueTwoSATTest {

    @Test
    public void testBasic() {
        // 3 variables, k = 4
        KValueTwoSAT sat = new KValueTwoSAT(3, 4);
        int x = 0;
        int y = 1;
        int z = 2;

        sat.forceValue(x, 2);
        sat.forceValue(y, 1);
        sat.forceValue(z, 3);

        int[] res = sat.solve();
        assertNotNull(res);
        assertArrayEquals(new int[]{2, 1, 3}, res);
    }

    @Test
    public void testDifferenceLeq() {
        // x - y <= 1 with k = 5
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 5);
            int x = 0;
            int y = 1;

            sat.forceDifferenceLeq(x, y, 1);
            sat.forceValue(x, 4);

            int[] res = sat.solve();
            assertNotNull(res);
            // x = 4 implies y >= 3, so y can be 3 or 4
            assertTrue(res[1] >= 3);
            assertTrue(res[0] - res[1] <= 1);
        }

        // x - y <= 1 is violated when x = 4, y = 2
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 5);
            int x = 0;
            int y = 1;

            sat.forceDifferenceLeq(x, y, 1);
            sat.forceValue(x, 4);
            sat.forceValue(y, 2);

            int[] res = sat.solve();
            assertNull(res);
        }
    }

    @Test
    public void testSumLeq() {
        // x + y <= 3 with k = 4 (values 0, 1, 2, 3)
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.forceSumLeq(x, y, 3);
            sat.forceValue(x, 3);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(3, res[0]);
            assertEquals(0, res[1]);
        }

        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.forceSumLeq(x, y, 3);
            sat.forceValue(x, 2);
            sat.forceValue(y, 2);

            int[] res = sat.solve();
            assertNull(res);
        }
    }

    @Test
    public void testSumGeq() {
        // x + y >= 4 with k = 4 (values 0, 1, 2, 3)
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.forceSumGeq(x, y, 4);
            sat.forceValue(x, 1);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(1, res[0]);
            assertEquals(3, res[1]);
        }

        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.forceSumGeq(x, y, 4);
            sat.forceValue(x, 1);
            sat.forceValue(y, 2);

            int[] res = sat.solve();
            assertNull(res);
        }
    }

    @Test
    public void testEq() {
        KValueTwoSAT sat = new KValueTwoSAT(2, 5);
        int x = 0;
        int y = 1;

        sat.forceEq(x, y);
        sat.forceValue(x, 3);

        int[] res = sat.solve();
        assertNotNull(res);
        assertEquals(3, res[0]);
        assertEquals(3, res[1]);
    }

    @Test
    public void testBoundaryK1() {
        KValueTwoSAT sat = new KValueTwoSAT(2, 1);

        // k = 1 means variables can only be 0
        int[] res = sat.solve();
        assertNotNull(res);
        assertArrayEquals(new int[]{0, 0}, res);
    }

    @Test
    public void testUnderlyingTwoSATAccess() {
        KValueTwoSAT sat = new KValueTwoSAT(1, 3);
        int x = 0;
        assertNotNull(sat.getTwoSAT());
        // verify node threshold validation
        assertThrows(IllegalArgumentException.class, () -> sat.node(x, -1));
        assertThrows(IllegalArgumentException.class, () -> sat.node(x, 3));
        assertThrows(IllegalArgumentException.class, () -> sat.node(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> sat.node(1, 1));
    }

    @Test
    public void testConditionalImplications() {
        // [x >= 2] => [y >= 3] with k = 4
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.ifGeThenGe(x, 2, y, 3);
            sat.forceValue(x, 2);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(3, res[1]);
        }

        // [x >= 2] => [y < 1] with k = 4
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.ifGeThenLt(x, 2, y, 1);
            sat.forceValue(x, 2);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(0, res[1]);
        }

        // [x < 2] => [y >= 3] with k = 4
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.ifLtThenGe(x, 2, y, 3);
            sat.forceValue(x, 1);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(3, res[1]);
        }

        // [x < 2] => [y < 1] with k = 4
        {
            KValueTwoSAT sat = new KValueTwoSAT(2, 4);
            int x = 0;
            int y = 1;

            sat.ifLtThenLt(x, 2, y, 1);
            sat.forceValue(x, 1);

            int[] res = sat.solve();
            assertNotNull(res);
            assertEquals(0, res[1]);
        }
    }

    @Test
    public void testForbidInterval() {
        // x in [1, 2] is forbidden with k = 4. Allowed values are 0, 3.
        {
            KValueTwoSAT sat = new KValueTwoSAT(1, 4);
            int x = 0;

            sat.forbidInterval(x, 1, 2);

            // try to set x = 1, should fail
            {
                KValueTwoSAT s = new KValueTwoSAT(1, 4);
                int xv = 0;
                s.forbidInterval(xv, 1, 2);
                s.forceValue(xv, 1);
                assertNull(s.solve());
            }

            // try to set x = 2, should fail
            {
                KValueTwoSAT s = new KValueTwoSAT(1, 4);
                int xv = 0;
                s.forbidInterval(xv, 1, 2);
                s.forceValue(xv, 2);
                assertNull(s.solve());
            }

            // try to set x = 0, should succeed
            {
                KValueTwoSAT s = new KValueTwoSAT(1, 4);
                int xv = 0;
                s.forbidInterval(xv, 1, 2);
                s.forceValue(xv, 0);
                assertNotNull(s.solve());
            }

            // try to set x = 3, should succeed
            {
                KValueTwoSAT s = new KValueTwoSAT(1, 4);
                int xv = 0;
                s.forbidInterval(xv, 1, 2);
                s.forceValue(xv, 3);
                assertNotNull(s.solve());
            }
        }
    }

    interface Constraint {
        boolean check(int[] val);
    }

    @Test
    public void testRandomStress() {
        Random rnd = new Random(42);
        for (int tCase = 0; tCase < 10000; tCase++) {
            int n = rnd.nextInt(4) + 2; // 2 to 5 variables
            int k = rnd.nextInt(5) + 2; // 2 to 6 domain size
            KValueTwoSAT sat = new KValueTwoSAT(n, k);

            List<Constraint> constraints = new ArrayList<>();
            int numConstraints = rnd.nextInt(5) + 1;
            for (int c = 0; c < numConstraints; c++) {
                int type = rnd.nextInt(10);
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                while (v == u) {
                    v = rnd.nextInt(n);
                }

                if (type == 0) {
                    // forceValue
                    int val = rnd.nextInt(k);
                    sat.forceValue(u, val);
                    final int fu = u;
                    final int fval = val;
                    constraints.add(arr -> arr[fu] == fval);
                } else if (type == 1) {
                    // forceDifferenceLeq
                    int d = rnd.nextInt(2 * k) - k; // [-k, k]
                    sat.forceDifferenceLeq(u, v, d);
                    final int fu = u;
                    final int fv = v;
                    final int fd = d;
                    constraints.add(arr -> arr[fu] - arr[fv] <= fd);
                } else if (type == 2) {
                    // forceSumLeq
                    int sumVal = rnd.nextInt(2 * k);
                    sat.forceSumLeq(u, v, sumVal);
                    final int fu = u;
                    final int fv = v;
                    final int fsum = sumVal;
                    constraints.add(arr -> arr[fu] + arr[fv] <= fsum);
                } else if (type == 3) {
                    // forceSumGeq
                    int sumVal = rnd.nextInt(2 * k);
                    sat.forceSumGeq(u, v, sumVal);
                    final int fu = u;
                    final int fv = v;
                    final int fsum = sumVal;
                    constraints.add(arr -> arr[fu] + arr[fv] >= fsum);
                } else if (type == 4) {
                    // forceEq
                    sat.forceEq(u, v);
                    final int fu = u;
                    final int fv = v;
                    constraints.add(arr -> arr[fu] == arr[fv]);
                } else if (type == 5) {
                    // ifGeThenGe
                    int a = rnd.nextInt(k + 2) - 1; // [-1, k]
                    int b = rnd.nextInt(k + 2) - 1;
                    sat.ifGeThenGe(u, a, v, b);
                    final int fu = u;
                    final int fv = v;
                    final int fa = a;
                    final int fb = b;
                    constraints.add(arr -> !(arr[fu] >= fa) || (arr[fv] >= fb));
                } else if (type == 6) {
                    // ifGeThenLt
                    int a = rnd.nextInt(k + 2) - 1;
                    int b = rnd.nextInt(k + 2) - 1;
                    sat.ifGeThenLt(u, a, v, b);
                    final int fu = u;
                    final int fv = v;
                    final int fa = a;
                    final int fb = b;
                    constraints.add(arr -> !(arr[fu] >= fa) || (arr[fv] < fb));
                } else if (type == 7) {
                    // ifLtThenGe
                    int a = rnd.nextInt(k + 2) - 1;
                    int b = rnd.nextInt(k + 2) - 1;
                    sat.ifLtThenGe(u, a, v, b);
                    final int fu = u;
                    final int fv = v;
                    final int fa = a;
                    final int fb = b;
                    constraints.add(arr -> !(arr[fu] < fa) || (arr[fv] >= fb));
                } else if (type == 8) {
                    // ifLtThenLt
                    int a = rnd.nextInt(k + 2) - 1;
                    int b = rnd.nextInt(k + 2) - 1;
                    sat.ifLtThenLt(u, a, v, b);
                    final int fu = u;
                    final int fv = v;
                    final int fa = a;
                    final int fb = b;
                    constraints.add(arr -> !(arr[fu] < fa) || (arr[fv] < fb));
                } else {
                    // forbidInterval
                    int l = rnd.nextInt(k + 2) - 1;
                    int r = rnd.nextInt(k + 2) - 1;
                    sat.forbidInterval(u, l, r);
                    final int fu = u;
                    final int fl = l;
                    final int fr = r;
                    constraints.add(arr -> (fl > fr) || (arr[fu] < fl || arr[fu] > fr));
                }
            }

            int[] restored = sat.solve();
            if (restored != null) {
                // Verify restored values satisfy all constraints
                for (int i = 0; i < n; i++) {
                    assertTrue(restored[i] >= 0 && restored[i] < k, "Value out of bounds");
                }
                for (Constraint cons : constraints) {
                    assertTrue(cons.check(restored), "Constraint not satisfied");
                }
            } else {
                // Verify by brute force that no satisfying assignment exists
                boolean anySatisfies = bruteForceCheck(n, k, constraints);
                assertFalse(anySatisfies, "Solver returned null, but a satisfying assignment exists!");
            }
        }
    }

    private boolean bruteForceCheck(int n, int k, List<Constraint> constraints) {
        int[] current = new int[n];
        return dfs(0, n, k, current, constraints);
    }

    private boolean dfs(int idx, int n, int k, int[] current, List<Constraint> constraints) {
        if (idx == n) {
            for (Constraint cons : constraints) {
                if (!cons.check(current)) {
                    return false;
                }
            }
            return true;
        }
        for (int v = 0; v < k; v++) {
            current[idx] = v;
            if (dfs(idx + 1, n, k, current, constraints)) {
                return true;
            }
        }
        return false;
    }
}
