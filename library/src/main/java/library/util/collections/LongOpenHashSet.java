package library.util.collections;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class LongOpenHashSet {
	
	private static final int defaultCapacity = 16;
	private int size = 0;
	private long[] keys;
	private boolean[] deleted;
	private int mask = defaultCapacity - 1;
	private static final double defaultLoadFactor = 0.75;
	boolean containsZero=false;
	final static long nullKey=0;
	
	@SuppressWarnings("unchecked")
	public LongOpenHashSet() {
		keys = new long[defaultCapacity];
		deleted = new boolean[defaultCapacity];
	}
	
	
	private int hash(long key) {
		return Long.hashCode(key) & mask;
	}
	
	void resize(int newCapacity) {
		var oldKeys = keys;
		keys = new long[newCapacity];
		deleted = new boolean[newCapacity];
		mask = newCapacity - 1;
		size = 0;
		for (int i = 0; i < oldKeys.length; i++) {
			if(oldKeys[i] != nullKey) {
				add(oldKeys[i]);
			}
		}
	}
	
	public void add(long key) {
		if(key==nullKey) {
			if (!containsZero) {
				containsZero=true;
			}
			return;
		}
		if (size + 1 > keys.length * defaultLoadFactor) resize(keys.length * 2);
		int start = hash(key);
		int i = start;
		int firstDeleted = -1;
		do {
			if (keys[i] == nullKey) {
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
			} else if (keys[i]==key) {
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
		
	public boolean contains(long key) {
		if(key==nullKey) return containsZero;
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == nullKey && !deleted[i]) return false;
			if (keys[i]==key) return true;
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
	public boolean remove(long key) {
		if (key == nullKey) {
			if (containsZero) {
				containsZero = false;
				return true;
			}
			return false;
		}
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == nullKey && !deleted[i]) return false;
			if (keys[i] == key) {
				keys[i] = nullKey;
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
	
	public void forEach(LongConsumer f) {
		if(containsZero)f.accept(nullKey);;
		for (int i = 0; i < keys.length; i++) {
	        if (keys[i] != nullKey && !deleted[i]) {
	            f.accept(keys[i]);
	        }
	    }
	}

	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

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
		if (!(o instanceof LongOpenHashSet)) return false;
		LongOpenHashSet other = (LongOpenHashSet) o;
		int thisTotal = this.size + (this.containsZero ? 1 : 0);
		int otherTotal = other.size + (other.containsZero ? 1 : 0);
		if (thisTotal != otherTotal) return false;
		if (this.containsZero && !other.containsZero) return false;
		for (int i = 0; i < this.keys.length; i++) {
			if (this.keys[i] != nullKey && !this.deleted[i]) {
				if (!other.contains(this.keys[i])) return false;
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
		if (containsZero) {
			h += Long.hashCode(nullKey);
		}
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != nullKey && !deleted[i]) {
				h += Long.hashCode(keys[i]);
			}
		}
		return h;
	}

	/**
	 * 内部状態を文字列として表現します。
	 *
	 * <p>計算量: $O(N)$（$N$ は格納されている要素数）</p>
	 *
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		if (size == 0 && !containsZero) return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		if (containsZero) {
			sb.append("0");
			first = false;
		}
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != nullKey && !deleted[i]) {
				if (!first) sb.append(", ");
				first = false;
				sb.append(keys[i]);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * デバッグ用にセットの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
}
