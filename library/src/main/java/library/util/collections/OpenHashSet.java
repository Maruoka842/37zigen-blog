package library.util.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

public class OpenHashSet<Key> implements Iterable<Key> {
	
	private static final int defaultCapacity = 16;
	private int size = 0;
	private Key[] keys;
	private boolean[] deleted;
	private int mask = defaultCapacity - 1;
	private static final double defaultLoadFactor = 0.75;
	private Hash.Strategy<Key> strategy;
	
	@SuppressWarnings("unchecked")
	public OpenHashSet() {
		this(Hash.defaultStrategy());
	}
	
	@SuppressWarnings("unchecked")
	public OpenHashSet(Hash.Strategy<Key> strategy) {
		keys = (Key[]) new Object[defaultCapacity];
		deleted = new boolean[defaultCapacity];
		this.strategy = strategy;
	}
	
	
	@SuppressWarnings("unchecked")
	public OpenHashSet(Hash.Strategy<Key> strategy, int initialCapacity) {
		int capacity = Integer.highestOneBit(initialCapacity)*2;
		this.mask = capacity - 1;
		keys = (Key[]) new Object[capacity];
		deleted = new boolean[capacity];
		this.strategy = strategy;
	}
	
	private int hash(Key key) {
		return strategy.hashCode(key) & mask;
	}
	
	void resize(int newCapacity) {
		var oldKeys = keys;
		keys = (Key[]) new Object[newCapacity];
		deleted = new boolean[newCapacity];
		mask = newCapacity - 1;
		size = 0;
		for (int i = 0; i < oldKeys.length; i++) {
			if(oldKeys[i] != null) {
				add(oldKeys[i]);
			}
		}
	}
	
	public void add(Key key) {
		if (key == null) throw new AssertionError();
		if (size + 1 > keys.length * defaultLoadFactor) resize(keys.length * 2);
		int start = hash(key);
		int i = start;
		int firstDeleted = -1;
		do {
			if (keys[i] == null) {
				if (!deleted[i]) {
					int target = firstDeleted != -1 ? firstDeleted : i;
					keys[target] = key;
					deleted[target] = false;
					++size;
					return;
				} else {
					if (firstDeleted == -1) {
						firstDeleted = i;
					}
				}
			} else if (strategy.equals(keys[i], key)) {
				return;
			}
			i = (i + 1) & mask;
		} while (i != start);
		if (firstDeleted != -1) {
			keys[firstDeleted] = key;
			deleted[firstDeleted] = false;
			++size;
			return;
		}
		throw new AssertionError();
	}
		
	public boolean contains(Key key) {
		if (key == null) return false;
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null && !deleted[i]) return false;
			if (keys[i] != null && strategy.equals(keys[i], key)) return true;
			i = (i + 1) & mask;
		} while (i != start);
		return false;
	}

	/**
	 * 指定された要素を集合から削除します。
	 *
	 * <p>計算量: 平均 O(1)</p>
	 * // 未テスト
	 *
	 * @param key 削除する要素
	 * @return 要素が削除された場合は true、そうでない場合は false
	 */
	public boolean remove(Key key) {
		if (key == null) return false;
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null && !deleted[i]) return false;
			if (keys[i] != null && strategy.equals(keys[i], key)) {
				keys[i] = null;
				deleted[i] = true;
				--size;
				return true;
			}
			i = (i + 1) & mask;
		} while (i != start);
		return false;
	}

	public boolean isEmpty() {
		return size == 0;
	}
	
	
	
	public boolean isNonEmpty() {
		return size != 0;
	}
	
	public int size() {
		return size;
	}
	
	public void forEach(Consumer<? super Key> f) {
	    for (int i = 0; i < keys.length; i++) {
	        if (keys[i] != null && !deleted[i]) {
	            f.accept(keys[i]);
	        }
	    }
	}

	/**
	 * 集合のすべての要素に対して累積演算を行います。空集合の場合はnullを返します。
	 * 未テスト。
	 *
	 * @param accumulator 累積演算を行う二項演算子
	 * @return 累積結果、または集合が空の場合はnull
	 *
	 * 計算量: O(C) (C はハッシュセットの内部配列の容量)
	 */
	public Key fold(BinaryOperator<Key> accumulator) {
		Key res = null;
		boolean first = true;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && !deleted[i]) {
				if (first) {
					res = keys[i];
					first = false;
				} else {
					res = accumulator.apply(res, keys[i]);
				}
			}
		}
		return res;
	}
	
	@Override
	public Iterator<Key> iterator() {
		return new Iterator<>() {
			int id=0;
			
			@Override
			public boolean hasNext() {
				while (id < keys.length) {
					if (keys[id] != null && !deleted[id]) return true;
					++id;
				}
				return false;
			}
			
			@Override
			public Key next() {
				if (!hasNext()) throw new NoSuchElementException();
				return keys[id++];
			}
			
			
		};
		
		
	}

	
	/**
	 * このセットと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpenHashSet)) return false;
		OpenHashSet<?> other = (OpenHashSet<?>) o;
		if (this.size() != other.size()) return false;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && !deleted[i]) {
				@SuppressWarnings("unchecked")
				boolean contains = ((OpenHashSet<Object>) other).contains(keys[i]);
				if (!contains) return false;
			}
		}
		return true;
	}

	/**
	 * このセットのハッシュコード値を返します。
	 * @return ハッシュコード値
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && !deleted[i]) {
				h += strategy.hashCode(keys[i]);
			}
		}
		return h;
	}

	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
