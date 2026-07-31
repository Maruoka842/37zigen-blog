package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KValueMinCutTest {

    @Test
    public void testSingleVariablePositive() {
        int k = 4;
        KValueMinCut kmc = new KValueMinCut(1, k);

        int[] cost = {10, 5, 20, 15};
        kmc.addCost(0, cost);

        long val = kmc.minCutValue();
        assertEquals(5, val);

        int[] ans = kmc.restoreValues();
        assertEquals(1, ans[0]);
    }

    @Test
    public void testSingleVariableNegative() {
        int k = 4;
        KValueMinCut kmc = new KValueMinCut(1, k);

        int[] cost = {-10, -5, -20, -15};
        kmc.addCost(0, cost);

        long val = kmc.minCutValue();
        assertEquals(-20, val);

        int[] ans = kmc.restoreValues();
        assertEquals(2, ans[0]);
    }

    @Test
    public void testTwoVariablesWithConstraint() {
        int k = 3;
        KValueMinCut kmc = new KValueMinCut(2, k);

        int[] costX = {10, 30, 20};
        int[] costY = {40, 10, 50};

        kmc.addCost(0, costX);
        kmc.addCost(1, costY);

        // y - x <= -1 <=> x - y >= 1
        kmc.forceDifferenceLeq(1, 0, -1);

        long val = kmc.minCutValue();
        assertEquals(30, val);

        int[] ans = kmc.restoreValues();
        assertEquals(2, ans[0]); // x (index 0) should be 2
        assertEquals(1, ans[1]); // y (index 1) should be 1
    }

    @Test
    public void testKEqualsOne() {
        int k = 1;
        KValueMinCut kmc = new KValueMinCut(1, k);

        int[] cost = {42};
        kmc.addCost(0, cost);

        long val = kmc.minCutValue();
        assertEquals(42, val);

        int[] ans = kmc.restoreValues();
        assertEquals(0, ans[0]);
    }

    @Test
    public void testForceValueWithAddCost() {
        int k = 4;
        KValueMinCut kmc = new KValueMinCut(1, k);

        int[] cost = {10, 5, 20, 15};
        kmc.addCost(0, cost);
        kmc.forceValue(0, 2);

        long val = kmc.minCutValue();
        assertEquals(20, val);

        int[] ans = kmc.restoreValues();
        assertEquals(2, ans[0]);
    }

    @Test
    public void testAddCostPerUnit() {
        int k = 4;
        KValueMinCut kmc = new KValueMinCut(1, k);
        kmc.addCostPerUnit(0, 10);

        long val = kmc.minCutValue();
        assertEquals(0, val); // values = 0, cost is 0

        int[] ans = kmc.restoreValues();
        assertEquals(0, ans[0]);
    }

    @Test
    public void testValidation() {
        KValueMinCut kmc = new KValueMinCut(2, 3);

        // Invalid variable index
        assertThrows(IllegalArgumentException.class, () -> kmc.addCost(-1, new int[]{1, 2, 3}));
        assertThrows(IllegalArgumentException.class, () -> kmc.addCost(2, new int[]{1, 2, 3}));

        // Invalid cost array length
        assertThrows(IllegalArgumentException.class, () -> kmc.addCost(0, new int[]{1, 2}));

        // Null cost
        assertThrows(NullPointerException.class, () -> kmc.addCost(0, null));
    }
}
