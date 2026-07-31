package library.util.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MyPriorityQueue<T> extends PriorityQueue<T> {
    private static final long serialVersionUID = 1L;

    /** デフォルトコンストラクタ: 配列型は Arrays.compare、その他は Comparable */
    @SuppressWarnings("unchecked")
    public MyPriorityQueue() {
        super((x, y) -> defaultCompare(x, y));
    }

    /** 初期容量指定コンストラクタ */
    public MyPriorityQueue(int initialCapacity) {
        super(initialCapacity, (x, y) -> defaultCompare(x, y));
    }

    /** Comparator 指定コンストラクタ */
    public MyPriorityQueue(Comparator<? super T> comparator) {
        super(comparator);
    }

    /** デフォルト比較: 配列型なら Arrays.compare、その他は Comparable */
    @SuppressWarnings("unchecked")
    private static <T> int defaultCompare(T x, T y) {
        if (x instanceof int[] xi && y instanceof int[] yi) return Arrays.compare(xi, yi);
        if (x instanceof long[] xl && y instanceof long[] yl) return Arrays.compare(xl, yl);
        if (x instanceof double[] xd && y instanceof double[] yd) return Arrays.compare(xd, yd);
        if (x instanceof char[] xc && y instanceof char[] yc) return Arrays.compare(xc, yc);
        if (x instanceof Comparable cx && y instanceof Comparable cy) return cx.compareTo(cy);
        throw new IllegalArgumentException("Elements not comparable: " +
                x.getClass() + ", " + y.getClass());
    }
}
