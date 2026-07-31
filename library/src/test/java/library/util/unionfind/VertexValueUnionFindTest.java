package library.util.unionfind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VertexValueUnionFindTest {

    @Test
    public void testBasic() {
        VertexValueUnionFind<Integer> uf = new VertexValueUnionFind<>(5, Integer::sum, 0);
        assertEquals(5, uf.numberConnectedComponents());

        uf.set(0, 10);
        uf.set(1, 20);
        assertEquals(10, uf.getVertexValue(0));
        assertEquals(20, uf.getVertexValue(1));

        uf.union(0, 1);
        assertEquals(4, uf.numberConnectedComponents());
        assertEquals(30, uf.getVertexValue(0));
        assertEquals(30, uf.getVertexValue(1));

        uf.mulRight(0, 5);
        assertEquals(35, uf.getVertexValue(0));
        assertEquals(35, uf.getVertexValue(1));
    }

    @Test
    public void testCopy() {
        VertexValueUnionFind<Integer> uf = new VertexValueUnionFind<>(10, Integer::sum, 0);
        uf.set(0, 1);
        uf.set(1, 2);
        uf.set(2, 4);
        uf.set(3, 8);
        uf.union(0, 1);
        uf.union(2, 3);
        uf.union(0, 2);

        // At this point {0, 1, 2, 3} are connected, value = 1+2+4+8 = 15
        assertEquals(15, uf.getVertexValue(0));
        assertEquals(7, uf.numberConnectedComponents());

        VertexValueUnionFind<Integer> copy = uf.copy();
        assertEquals(uf.n(), copy.n());
        assertEquals(uf.numberConnectedComponents(), copy.numberConnectedComponents());
        for (int i = 0; i < uf.n(); i++) {
            assertEquals(uf.root(i), copy.root(i));
            assertEquals(uf.getVertexValue(i), copy.getVertexValue(i));
        }

        // Verify independence
        copy.union(4, 5);
        assertTrue(copy.equiv(4, 5));
        assertFalse(uf.equiv(4, 5));
        assertEquals(uf.numberConnectedComponents() - 1, copy.numberConnectedComponents());

        copy.set(6, 100);
        assertEquals(100, (int)copy.getVertexValue(6));
        assertEquals(0, (int)uf.getVertexValue(6));
    }

    @Test
    public void testNumberConnectedComponentsBug() {
        VertexValueUnionFind<Integer> uf = new VertexValueUnionFind<>(5, Integer::sum, 0);
        assertEquals(5, uf.numberConnectedComponents(), "numberConnectedComponents should be initialized to n");
    }

    @Test
    public void testToString() {
        VertexValueUnionFind<Integer> uf = new VertexValueUnionFind<>(5, Integer::sum, 0);
        assertEquals("{0}(0){1}(0){2}(0){3}(0){4}(0)", uf.toString());

        uf.set(0, 10);
        uf.set(1, 20);
        assertEquals("{0}(10){1}(20){2}(0){3}(0){4}(0)", uf.toString());

        uf.union(0, 1);
        assertEquals("{0, 1}(30){2}(0){3}(0){4}(0)", uf.toString());
    }
}
