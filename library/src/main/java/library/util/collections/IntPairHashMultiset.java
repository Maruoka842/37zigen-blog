package library.util.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import library.util.Ints;

public class IntPairHashMultiset {
    private final IntPairHashMap<Long> map;
    /** 重複を含めた総要素数 */
    private long size = 0;

    public IntPairHashMultiset() {
        map = new IntPairHashMap<>();
    }

    /***
     * repeatは負（削除）でもよいが、操作後の個数が負になるならerror
     * @param a
     * @param b
     * @param repeat
     */
    public void add(int a, int b, long repeat) {
        if (repeat == 0) return;
        size += repeat;
        long num = map.getOrDefault(a, b, 0L) + repeat;
        if (num < 0) throw new AssertionError();
        if (num == 0) map.remove(a, b);
        else map.put(a, b, num);
    }

    public void add(int a, int b) {
        add(a, b, 1);
    }

    /**
     * (a, b)を一つ削除する
     * @param a
     * @param b
     */
    public boolean remove(int a, int b) {
        return remove(a, b, 1);
    }

    /**
     * もともと入っていた要素数がrepeat未満の時はfalse
     * @param a
     * @param b
     * @param repeat
     * @return
     */
    public boolean remove(int a, int b, long repeat) {
        long num = map.getOrDefault(a, b, 0L);
        boolean ret = num >= repeat;
        repeat = Math.min(repeat, num);
        size -= repeat;
        if (num == repeat) map.remove(a, b);
        else map.put(a, b, num - repeat);
        return ret;
    }

    /**
     * keyがpackされた(a, b), valueが要素の個数
     * @return
     */
    public Set<Map.Entry<Long, Long>> entrySet() {
        return map.entrySet();
    }

    /**
     * a, b, valueを展開したentryのリスト
     * @return
     */
    public List<Entry> entryList() {
        List<Entry> ret = new ArrayList<>();
        for (var e : map.entrySet()) {
            ret.add(new Entry(Ints.unpack(e.getKey(), true), Ints.unpack(e.getKey(), false), e.getValue()));
        }
        return ret;
    }

    /**
     * 重複を込めたサイズを返す
     * @return
     */
    public long size() {
        return size;
    }

    public int numberDistinctElements() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public long count(int a, int b) {
        return map.getOrDefault(a, b, 0L);
    }

    public boolean contains(int a, int b) {
        return map.containsKey(a, b);
    }

    public void clear() {
        map.clear();
        size = 0;
    }

    /**
     * このマルチセットと指定されたオブジェクトの同値性を判定します。
     *
     * <p>計算量: $O(N)$（$N$ は格納されている異なり数）</p>
     *
     * @param obj 比較対象のオブジェクト
     * @return 同値であれば true, そうでなければ false
     */
    // 未テスト
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntPairHashMultiset)) return false;
        IntPairHashMultiset other = (IntPairHashMultiset) obj;
        return this.size == other.size && this.map.equals(other.map);
    }

    /**
     * このマルチセットのハッシュコードを返します。
     *
     * <p>計算量: $O(N)$（$N$ は格納されている異なり数）</p>
     *
     * @return ハッシュコード
     */
    // 未テスト
    @Override
    public int hashCode() {
        return java.util.Objects.hash(map, size);
    }

    /**
     * 内部状態を文字列として表現します。
     *
     * <p>計算量: $O(N)$（$N$ は格納されている異なり数）</p>
     *
     * @return 内部状態の文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (var entry : entryList()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("(").append(entry.a).append(", ").append(entry.b).append("):").append(entry.value);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * デバッグ用に集合の内容を標準出力に出力する。
     *
     * 未テスト
     * @complexity O(N) (N は異なる要素数)
     */
    public void dump() {
        System.out.println(toString());
    }

    public IntPairHashMap<Long> getHashMap() {
        return map;
    }

    public static class Entry {
        public final int a;
        public final int b;
        public final long value;

        public Entry(int a, int b, long value) {
            this.a = a;
            this.b = b;
            this.value = value;
        }
    }
}
