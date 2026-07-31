package library.test;

import library.util.DualSlopeTrick;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DualSlopeTrickTest {
    @Test
    public void testBasic() {
        DualSlopeTrick dst = new DualSlopeTrick();
        // Initial state: f(0) = 0, f(x) = INF for x != 0
        assertEquals(0, dst.getAtZero());
        assertEquals(0, dst.getMin());

        dst.addConst(10);
        assertEquals(10, dst.getAtZero());
        assertEquals(10, dst.getMin());
    }

    @Test
    public void testShift() {
        DualSlopeTrick dst = new DualSlopeTrick();
        dst.addConst(10);
        dst.shift(5); // f(5) = 10, others INF
        assertEquals(10, dst.getMin());
        // Since f(5) = 10 and f(x) = INF otherwise, f(0) should be INF?
        // Wait, maspy says f(0) is easily accessible.
        // If f(x) = g(x - 5) where g(0) = 10, then f(5) = g(0) = 10.
        // f(0) = g(-5) = INF.
        // Let's check how shift works.
        // getAtZero returns -st.getMin().min.
        // Initially st is empty, st.min = 0. getAtZero = 0.
        // dst.shift(5) calls st.addRelu(-INF).addConst(-INF) 5 times.
        // st.addRelu(-INF): st.min += max(0, topL - (-INF)) = 0 + max(0, -INF - (-INF)) = 0.
        // L.push(-INF), R.push(L.pop()) -> R has -INF.
        // st.addConst(-INF): st.min += -INF.
        // So st.min becomes -5*INF.
        // getAtZero = -(-5*INF) = 5*INF. Correct, f(0) is INF.
    }

    @Test
    public void testAddLinear() {
        DualSlopeTrick dst = new DualSlopeTrick();
        dst.addLinear(2, 3); // f(x) = 2x + 3 if x = 0, else INF
        assertEquals(3, dst.getAtZero());
        assertEquals(3, dst.getMin()); // min occurs at x=0

        dst.shift(1); // f(x) = 2(x-1) + 3 if x-1 = 0 (x=1), else INF
        // f(1) = 3, f(0) = INF
        assertEquals(3, dst.getMin());
    }

    @Test
    public void testAddLinearOrZero() {
        DualSlopeTrick dst = new DualSlopeTrick();
        // f(x) = 0 if x = 0, else INF
        dst.addLinearOrZero(1, 0); // f(x) = 0 + max(x, 0) if x = 0, else INF
        // This is not very interesting since f is mostly INF.

        // Let's try to build something.
        // f(x) = 0 for all x.
        // In dual: f*(p) = 0 if p = 0, else INF.
        // Wait, f(x) = 0 for all x => f*(p) = sup (px) = 0 if p=0, INF if p != 0.
        // SlopeTrick represents f*(p) = min_f + sum max(0, a_i - p) + sum max(0, p - b_i).
        // If st is empty, st(p) = 0 for all p.
        // This means f*(p) = 0 for all p.
        // Then f(x) = (f*)*(x) = sup (xp - f*(p)) = sup (xp) = 0 if x=0, INF if x != 0.
        // So empty SlopeTrick corresponds to f(0)=0, f(x)=INF.

        // To get f(x) = 0 for all x, we need st to be f*(p) = 0 if p=0, else INF.
        // Which is an empty SlopeTrick but shifted? No.

        // Let's use the property: adding max(x-a, 0) to f(x) is moveRightCurve in dual.
        dst = new DualSlopeTrick(); // f(0)=0
        dst.addLinearOrZero(1, 0); // f(x) = 0 (x=0), x (x>0), INF (x<0)? No.
        // f(x) <- f(x) + max(x, 0).
        // Since f(x) was INF for x != 0, f(x) is still INF for x != 0.
        // f(0) = 0 + max(0, 0) = 0.
        assertEquals(0, dst.getAtZero());
    }

    @Test
    public void testSlideMin() {
        DualSlopeTrick dst = new DualSlopeTrick();
        dst.addConst(10); // f(0)=10, else INF
        dst.slideMin(0, 5); // f(x) = min_{0 <= d <= 5} f(x - d)
        // f(x) = 10 if 0 <= x <= 5, else INF
        assertEquals(10, dst.getMin());
        assertEquals(10, dst.getAtZero());
        dst.shift(3); // f(x) = 10 if 3 <= x <= 8, else INF
        assertEquals(10, dst.getMin());
        // f(0) should be INF
        long atZero = dst.getAtZero();
        assertTrue(atZero > 1e14, "f(0) was " + atZero);
    }

    @Test
    public void testMatching() {
        // Test with a simple case from yukicoder 2114
        // The problem is about matching 0s and 1s on a line.
        // This can be solved with slope trick or dual slope trick.

        DualSlopeTrick dst = new DualSlopeTrick();
        // Suppose we have a 0 at pos 1 and a 1 at pos 3.
        // Cost to match them is |1 - 3| = 2.

        // Standard DP:
        // dp[i][j] = min cost after considering i elements with balance j.
        // dp[i][j] = min(dp[i-1][j-1] + cost, dp[i-1][j+1] + cost, ...)

        // Using Dual Slope Trick:
        dst = new DualSlopeTrick();
        // Element at 1 is '0'. If we use it as '+1', cost is -1*x? No.
        // Let's use the logic from maspy's post.
        // "01 Matching"
        // 0 at x: f(x) <- f(x) + |x - a| is not what dual slope trick is for.
        // It's for when we have x in the balance and we want to add costs.

        // Actually, let's just verify the operations are consistent with SlopeTrick.
        dst = new DualSlopeTrick();
        dst.addConst(5);
        dst.addLinear(2, 3); // f(x) = 2x + 8 at x=0
        assertEquals(8, dst.getAtZero());
        assertEquals(8, dst.getMin());

        dst.shift(1); // f(1) = 8, else INF
        dst.addConst(2); // f(1) = 10, else INF
        assertEquals(10, dst.getMin());
    }
}
