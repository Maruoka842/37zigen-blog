package library.util.collections;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class OpenHashMap<Key, Value> {
	public static final class Entry<K, V> {
	    /** エントリのキー。 */
	    public final K key;
	    /** エントリの値。 */
	    public final V value;

	    Entry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }
	    
	    public K getKey() {
	    	return key;
	    }
	    
	    public V getValue() {
	    	return  value;
	    }
	}
	
	public Iterable<Entry<Key, Value>> entrySet() {
	    return () -> new java.util.Iterator<>() {
	        int idx = 0;

	        @Override
	        public boolean hasNext() {
	            while (idx < keys.length) {
	                if (keys[idx] != null && !deleted[idx]) return true;
	                idx++;
	            }
	            return false;
	        }

	        @Override
	        public Entry<Key, Value> next() {
	            if (!hasNext()) throw new java.util.NoSuchElementException();
	            return new Entry<>(keys[idx], values[idx++]);
	        }
	    };
	}
	
	
	/** 初期バケット数。 */
	private static final int defaultCapacity = 16;
	/** 現在格納されているキー数。 */
	private int size = 0;
	/** キーを保持する開番地法テーブル。 */
	private Key[] keys;
	/** {@link #keys} と同じ添字に対応する値のテーブル。 */
	private Value[] values;
	/** {@code true} なら同じ添字が削除済み墓石であることを表す。 */
	private boolean[] deleted;
	/** バケット添字へのビットマスク。 */
	private int mask = defaultCapacity - 1;
	/** リサイズを開始する負荷率の上限。 */
	private static final double defaultLoadFactor = 0.5;
	/** キーのハッシュ値と同値性を定める戦略。 */
	private Hash.Strategy<Key> strategy;
	
	@SuppressWarnings("unchecked")
	public OpenHashMap() {
		this(Hash.defaultStrategy());
	}
	
	@SuppressWarnings("unchecked")
	public OpenHashMap(Hash.Strategy<Key> strategy) {
		keys = (Key[]) new Object[defaultCapacity];
		values = (Value[]) new Object[defaultCapacity];
		deleted = new boolean[defaultCapacity];
		this.strategy = strategy;
	}
	
	
	private int hash(Key key) {
		return strategy.hashCode(key) & mask;
	}
	
	void resize(int newCapacity) {
		var oldKeys = keys;
		var oldValues = values;
		keys = (Key[]) new Object[newCapacity];
		values = (Value[]) new Object[newCapacity];
		deleted = new boolean[newCapacity];
		mask = newCapacity - 1;
		size = 0;
		for (int i = 0; i < oldKeys.length; i++) {
			if(oldKeys[i] != null) {
				put(oldKeys[i], oldValues[i]);
			}
		}
	}
	
	public void put(Key key, Value value) {
		if (key == null) throw new AssertionError();
		if (size + 1 > keys.length * defaultLoadFactor) resize(keys.length * 2);
		int start = hash(key);
		int i = start;
		int firstDeleted = -1;
		do {
			if (keys[i] == null) {
				if (deleted[i]) {
					if (firstDeleted == -1) firstDeleted = i;
				} else {
					int insert = firstDeleted == -1 ? i : firstDeleted;
					keys[insert] = key;
					values[insert] = value;
					deleted[insert] = false;
					++size;
					return;
				}
			} else if (strategy.equals(keys[i], key)) {
				values[i] = value;
				return;
			}
			i = (i + 1) & mask;
		} while (i != start);
		if (firstDeleted != -1) {
			keys[firstDeleted] = key;
			values[firstDeleted] = value;
			deleted[firstDeleted] = false;
			++size;
			return;
		}
		throw new AssertionError();
	}
		
	public boolean containsKey(Key key) {
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null) {
				if (!deleted[i]) return false;
			} else if (strategy.equals(keys[i], key)) return true;
			i = (i + 1) & mask;
		} while (i != start);
		return false;
	}
	
	public Value get(Key key) {
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null) {
				if (!deleted[i]) throw new AssertionError("存在しない要素をgetしようとしている");
			} else if (strategy.equals(keys[i], key)) return values[i];
			i = (i + 1) & mask;
		} while (i != start);
		throw new AssertionError();
	}
	
	public Value getOrDefaultValue(Key key, Value defaultValue) {
		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null) {
				if (!deleted[i]) return defaultValue;
			} else if (strategy.equals(keys[i], key)) return values[i];
			i = (i + 1) & mask;
		} while (i != start);
		return defaultValue;
	}
	
	/**
	 * 契約: 事前状態を {@code old} とする。{@code key ∈ dom(old)} なら、
	 * 事後状態で {@code this[key] = remappingFunction(old[key], value)} かつ戻り値は {@code this[key]}。
	 * {@code key ∉ dom(old)} なら、事後状態で {@code this[key] = value} かつ戻り値は {@code value}。
	 * 計算量: 期待 {@code O(1)}、最悪 {@code O(n)}。
	 * @param key 更新または挿入するキー。事前条件: {@code key != null}。
	 * @param value {@code key ∉ dom(old)} のとき挿入する値、または {@code remappingFunction} の第2引数
	 * @param remappingFunction {@code key ∈ dom(old)} のとき適用する関数
	 * @return 事後状態の {@code this[key]}
	 */
    public Value merge(Key key, Value value,
            BiFunction<? super Value, ? super Value, ? extends Value> remappingFunction) {
		if (key == null) throw new AssertionError();
		int start = hash(key);
		int i = start;
		int firstDeleted = -1;
		do {
			if (keys[i] == null) {
				if (deleted[i]) {
					if (firstDeleted == -1) firstDeleted = i;
				} else {
					int insert = firstDeleted == -1 ? i : firstDeleted;
					keys[insert] = key;
					values[insert] = value;
					deleted[insert] = false;
					++size;
					if (size > keys.length * defaultLoadFactor) resize(keys.length * 2);
					return value;
				}
			} else if (strategy.equals(keys[i], key)) {
				values[i]=remappingFunction.apply(values[i], value);
				return values[i];
			}
			i = (i + 1) & mask;
		} while (i != start);
		if (firstDeleted != -1) {
			keys[firstDeleted] = key;
			values[firstDeleted] = value;
			deleted[firstDeleted] = false;
			++size;
			if (size > keys.length * defaultLoadFactor) resize(keys.length * 2);
			return value;
		}
		throw new AssertionError();
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
	
	public void forEach(BiConsumer<? super Key, ? super Value> f) {
	    for (int i = 0; i < keys.length; i++) {
	        if (keys[i] != null && !deleted[i]) {
	            f.accept(keys[i], values[i]);
	        }
	    }
	}
	
	/**
	 * 契約: {@code old = this[key]} が存在するなら、事後状態で {@code key ∉ dom(this)} かつ戻り値は {@code old}。
	 * {@code key ∉ dom(this)} なら状態を変更せず {@code null} を返す。
	 * 計算量: 期待 {@code O(1)}、最悪 {@code O(n)}。
	 * @param key 削除対象キー。事前条件: {@code key != null}。
	 * @return 削除前の {@code this[key]}、存在しなければ {@code null}
	 */
	// 未テスト
	public Value remove(Key key) {
		if (key == null) throw new AssertionError();

		int start = hash(key);
		int i = start;
		do {
			if (keys[i] == null && !deleted[i]) return null; // 見つからない

			if (keys[i] != null && !deleted[i] && strategy.equals(keys[i], key)) {
				Value old = values[i];
				keys[i] = null;
				values[i] = null;
				deleted[i] = true; // tombstone
				--size;
				return old;
			}

			i = (i + 1) & mask;
		} while (i != start);

		return null;
	}
	
	public void clear() {
		//https://atcoder.jp/contests/abc379/submissions/72663503
		size = 0;
		Arrays.fill(keys, null);
		Arrays.fill(deleted, false);
	}
	
	/**
	 * このマップと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpenHashMap)) return false;
		OpenHashMap<?, ?> other = (OpenHashMap<?, ?>) o;
		if (this.size() != other.size()) return false;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && !deleted[i]) {
				@SuppressWarnings("unchecked")
				OpenHashMap<Object, Object> otherTyped = (OpenHashMap<Object, Object>) other;
				if (!otherTyped.containsKey(keys[i])) return false;
				Object thisValue = values[i];
				Object otherValue = otherTyped.get(keys[i]);
				if (thisValue == null) {
					if (otherValue != null) return false;
				} else {
					if (!thisValue.equals(otherValue)) return false;
				}
			}
		}
		return true;
	}

	/**
	 * このマップのハッシュコード値を返します。
	 * @return ハッシュコード値
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null && !deleted[i]) {
				int kh = strategy.hashCode(keys[i]);
				int vh = (values[i] == null) ? 0 : values[i].hashCode();
				h += kh ^ vh;
			}
		}
		return h;
	}

	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
