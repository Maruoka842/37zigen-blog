package library.util.collections;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.IntFunction;

public class TreeMultiSet<E> {
    private final TreeMap<E, Long> map;
    /** 重複を含めた総要素数 */
    private long size = 0;
    
    public TreeMultiSet() {
    	map = new TreeMap<>();
	}
    
    public TreeMultiSet(Comparator<? super E> comp) {
    	map = new TreeMap<>(comp);
    }
    
    /***
     * int配列から構築するコンストラクタ
     */
    public static TreeMultiSet<Integer> create(int[] array) {
    	TreeMultiSet<Integer> set=new TreeMultiSet<Integer>();
    	for (int v : array) {
    		set.add(v);
    	}
    	return set;
    }
    
    /***
     * repeatは負（削除）でもよいが、操作後の個数が負になるならerror
     * @param element
     * @param repeat
     */
    public void add(E element, long repeat) {
    	if (repeat == 0) return;
    	size += repeat;
    	long num = map.getOrDefault(element, 0L) + repeat;
    	if (num < 0) throw new AssertionError();
    	if (num == 0) map.remove(element);
    	else map.put(element, num);
    }
    
	public void add(E element) {
		add(element, 1);
	}

    /**
     * elementを一つ削除する
     * @param element
     */
    public boolean remove(E element) {
    	return remove(element, 1);
    }
    
    /**
     * もともと入っていた要素数がrepeat未満の時はfalse
     * @param element
     * @param repeat
     * @return
     */
    public boolean remove(E element, long repeat) {
    	long num = map.getOrDefault(element, 0L);
    	boolean ret = num >= repeat;
    	repeat = Math.min(repeat, num);
    	size -= repeat;
    	if (num == repeat) map.remove(element);
    	else map.put(element, num - repeat);
    	return ret;
    }
    
    public Set<Entry<E, Long>> entrySet() {
    	return map.entrySet();
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
    
    public void addAll(TreeMultiSet<E> set) {
    	for (var es : set.entrySet()) {
    		add(es.getKey(),es.getValue());
    	}
    }
    
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    public E pollFirst() {
    	var e = map.firstKey();
    	remove(e);
    	return e;
    }

    public E pollLast() {
    	var e = map.lastKey();
    	remove(e);
    	return e;
    }
    
    public E peekFirst() {
    	var e = map.firstKey();
    	return e;
    }
    
    public E peekLast() {
    	var e = map.lastKey();
    	return e;
    }
    
    public E lower(E v) {
    	return map.lowerKey(v);
    }
    
    public E higher(E v) {
    	return map.higherKey(v);
    }
    
    public E floor(E v) {
    	return map.floorKey(v);
    }
    
    public E ceil(E v) {
    	return map.ceilingKey(v);
    }
    
    public Map.Entry<E, Long> peekLastEntry() {
    	return map.lastEntry();
    }
    
    
    /**
     * 重複を含めた全要素を配列として返す。
     *
     * @param generator 配列生成関数
     * @return 要素配列
     */
    public E[] toArray(IntFunction<E[]> generator) {
    	int sz = 0;
    	for (var es : map.entrySet()) sz += es.getValue();
    	E[] arr = generator.apply(sz);
    	int idx = 0;
    	for (var es : map.entrySet()) {
    		for (int i = 0; i < es.getValue(); i++) {
				arr[idx++] = es.getKey();
			}
    	}
    	return arr;
    }
    
    public long getValue(E element) {
    	return map.getOrDefault(element, 0L);
    }

    public boolean contains(E element) {
    	return map.containsKey(element);
    }

    /**
     * このマルチセットと別のオブジェクトの同値性を判定します。
     * 各要素の出現回数が一致する場合に同値とみなします。
     *
     * <p>計算量: $O(N)$（$N$ は要素の種類数）</p>
     *
     * @param obj 比較対象のオブジェクト
     * @return 同値であれば true, そうでなければ false
     */
    // 未テスト
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TreeMultiSet)) return false;
        TreeMultiSet<?> other = (TreeMultiSet<?>) obj;
        return this.map.equals(other.map);
    }

    /**
     * このマルチセットのハッシュコードを計算します。
     *
     * <p>計算量: $O(N)$（$N$ は要素の種類数）</p>
     *
     * @return ハッシュコード
     */
    // 未テスト
    @Override
    public int hashCode() {
        return map.hashCode();
    }
    
    /**
     * 集合の状態を文字列として表す。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし。</li>
     *   <li>計算量: $O(N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * @return 集合の状態を表す文字列
     */
    // 未テスト
    @Override
    public String toString() {
    	if (isEmpty()) {
		return "空集合";
    	} else {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
    		for (var entry : entrySet()) {
			if (!first) sb.append("\n");
			sb.append(entry.getKey()).append(" が ").append(entry.getValue()).append("個");
			first = false;
    		}
		return sb.toString();
    	}
    }

    /**
     * 集合の状態を標準出力に出力する。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし。</li>
     *   <li>副作用: 標準出力への出力。</li>
     *   <li>計算量: $O(N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     */
    // 未テスト
    public void dump() {
	System.out.println(toString());
    }
}
