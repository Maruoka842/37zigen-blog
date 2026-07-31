package library.util.seq;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationTest {

    @Test
    public void testSimplifyPermutationSubgroupIdentity() {
        int n = 3;
        List<int[]> perm = new ArrayList<>();
        perm.add(new int[]{0, 1, 2});
        List<int[][]> res = Permutation.simplifyPermutationSubgroup(n, perm, true);

        // Only identity should be present in each orbit set
        for (int f = 1; f < n; f++) {
            assertEquals(1, res.get(f).length);
            assertArrayEquals(new int[]{0, 1, 2}, res.get(f)[0]);
        }
    }

    @Test
    public void testSimplifyPermutationSubgroupCyclic() {
        int n = 3;
        List<int[]> perm = new ArrayList<>();
        perm.add(new int[]{1, 2, 0}); // (0 1 2)
        List<int[][]> res = Permutation.simplifyPermutationSubgroup(n, perm, true);

        // GroupElement size is 3. Orbits:
        // f=2: can move 2 to 0, 1, 2. So size 3.
        // f=1: after fixing 2, only identity remains. So size 1.
        assertEquals(3, res.get(2).length);
        assertEquals(1, res.get(1).length);

        long totalSize = 1;
        for (int f = 1; f < n; f++) {
            totalSize *= res.get(f).length;
        }
        assertEquals(3, totalSize);
    }

    @Test
    public void testSimplifyPermutationSubgroupSymmetric() {
        int n = 3;
        List<int[]> perm = new ArrayList<>();
        perm.add(new int[]{1, 0, 2}); // (0 1)
        perm.add(new int[]{0, 2, 1}); // (1 2)
        List<int[][]> res = Permutation.simplifyPermutationSubgroup(n, perm, true);

        // S3 size is 6.
        // f=2: can move 2 to 0, 1, 2. size 3.
        // f=1: can move 1 to 0, 1. size 2.
        assertEquals(3, res.get(2).length);
        assertEquals(2, res.get(1).length);

        long totalSize = 1;
        for (int f = 1; f < n; f++) {
            totalSize *= res.get(f).length;
        }
        assertEquals(6, totalSize);
    }

    @Test
    public void testSimplifyPermutationSubgroupLarge() {
        int n = 5;
        List<int[]> perm = new ArrayList<>();
        perm.add(new int[]{1, 2, 3, 4, 0}); // cycle (0 1 2 3 4)
        perm.add(new int[]{1, 0, 2, 3, 4}); // swap (0 1)
        List<int[][]> res = Permutation.simplifyPermutationSubgroup(n, perm, true);

        // S5 size is 120.
        long totalSize = 1;
        for (int f = 1; f < n; f++) {
            totalSize *= res.get(f).length;
        }
        assertEquals(120, totalSize);
    }
}
