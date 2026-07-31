package library.util.segtree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SegTreeClearTest {

    @Test
    public void testSegTreeClear() {
        SegTree<Long> seg = new SegTree<>(10, Long::sum, 0L);
        seg.set(3, 10L);
        seg.set(5, 20L);
        assertEquals(30L, seg.prodAll());

        seg.clear();
        assertEquals(0L, seg.prodAll());
        for (int i = 0; i < 10; i++) {
            assertEquals(0L, seg.get(i));
        }
    }

    @Test
    public void testSegTreelongClear() {
        SegTreelong seg = new SegTreelong(10, Long::sum, 0L);
        seg.set(3, 10L);
        seg.set(5, 20L);
        assertEquals(30L, seg.prodAll());

        seg.clear();
        assertEquals(0L, seg.prodAll());
        for (int i = 0; i < 10; i++) {
            assertEquals(0L, seg.get(i));
        }
    }

    @Test
    public void testLazySegTreeClear() {
        LazySegTree<Long, Long> seg = new LazySegTree<>(10, new LazySegTreeStrategy<Long, Long>() {
            @Override public Long identityX() { return 0L; }
            @Override public Long mergeX(Long a, Long b) { return a + b; }
            @Override public Long identityA() { return 0L; }
            @Override public Long mergeA(Long newer, Long older) { return newer + older; }
            @Override public Long mergeAX(Long a, Long x) { return a + x; }
        });

        seg.set(3, 5L);
        assertEquals(5L, seg.foldAll());

        seg.clear();
        assertEquals(0L, seg.foldAll());
        for (int i = 0; i < 10; i++) {
            assertEquals(0L, seg.get(i));
        }
    }

    @Test
    public void testLazySegTreelonglongClear() {
        LazySegTreelonglong seg = new LazySegTreelonglong(10, new LazySegTreeStrategy_longlong() {
            @Override public long identityX() { return 0L; }
            @Override public long mergeX(long a, long b) { return a + b; }
            @Override public long identityA() { return 0L; }
            @Override public long mergeA(long newer, long older) { return newer + older; }
            @Override public long mergeAX(long a, long x) { return a + x; }
        });

        seg.set(3, 5L);
        assertEquals(5L, seg.fold(0, 10));

        seg.clear();
        assertEquals(0L, seg.fold(0, 10));
        for (int i = 0; i < 10; i++) {
            assertEquals(0L, seg.get(i));
        }
    }
}
