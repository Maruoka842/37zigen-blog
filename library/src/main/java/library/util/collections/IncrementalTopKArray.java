package library.util.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

/**
 * インクリメンタルに最大値を降順に維持。
 */
public class IncrementalTopKArray<T> implements Iterable<T> {
//https://atcoder.jp/contests/abc447/submissions/73770589
//https://atcoder.jp/contests/abc345/submissions/74247226
	/**
	 * 内部でデータを保持する配列
	 */
	T[] data;

	/**
	 * 現在格納されている要素の数
	 */
	int size = 0;

	/**
	 * 要素の順序を決定する比較器
	 */
	Comparator<? super T> comp;

	/**
	 * 要素が同一であるかを判定する述語
	 */
	BiPredicate<? super T, ? super T> equals;

	/**
	 * 重複を排除して管理するかどうかを表すフラグ
	 */
	final boolean distinct;
	
	/**
	 * 指定されたサイズ、比較器、等価判定器、重複排除フラグで初期化する。
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param k 最大要素数
	 * @param comp 比較器
	 * @param equals 等価判定器
	 * @param distinct 重複を排除（一意にする）する場合は true、排除しない場合は false
	 */
	// 未テスト
	@SuppressWarnings("unchecked")
	public IncrementalTopKArray(int k, Comparator<T> comp, BiPredicate<? super T, ? super T> equals, boolean distinct) {
		this.data = (T[]) new Object[k];
		if (comp == null) throw new NullPointerException();
		this.comp = comp;
		this.equals = equals;
		this.distinct = distinct;
	}

	/**
	 * 指定されたサイズ、重複排除フラグで初期化する。自然順序付けおよび Object::equals を使用する。
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param k 最大要素数
	 * @param distinct 重複を排除（一意にする）する場合は true、排除しない場合は false
	 */
	// 未テスト
	@SuppressWarnings("unchecked")
	public IncrementalTopKArray(int k, boolean distinct) {
		this(k, (Comparator<T>) Comparator.naturalOrder(), Object::equals, distinct);
	}

	@SuppressWarnings("unchecked")
	public IncrementalTopKArray(int k, Comparator<T> comp, BiPredicate<? super T, ? super T> equals) {
		this(k, comp, equals, true);
	}

	@SuppressWarnings("unchecked")
	public IncrementalTopKArray(int k) {
		this(k, true);
	}

	/**
	 * 要素を追加する。
	 * 重複を排除する設定（distinct = true）の場合、同一と判定された要素が既に存在すれば、
	 * 新しい要素がより大きい場合のみ置換して順序を維持する。
	 * 重複を排除しない設定（distinct = false）の場合、重複を許容して適切な位置に挿入する。
	 * いずれの場合も最大要素数 k を超えた分は保持されない。
	 * <ul>
	 *   <li>計算量: O(k)</li>
	 * </ul>
	 *
	 * @param v 追加する要素
	 * @return 要素が追加または更新された場合は true、変化がなかった場合は false
	 */
	public boolean add(T v) {
		if (v == null) throw new NullPointerException("value is null");
		if (distinct) {
			for (int i = 0; i < size; i++) {
				if (equals.test(v, data[i])) {
					if (comp.compare(v, data[i]) > 0) {
						data[i] = v;
						for (int j = i; j >= 1; j--) {
							if (comp.compare(data[j-1], data[j]) < 0) {
								{
									var tmp = data[j-1];
									data[j-1] = data[j];
									data[j] = tmp;
								}
							} else {
								return true;
							}
						}
						return true;
					} else {
						return false;
					}
				}
			}
		}
		for (int i = 0; i < data.length; i++) {
			if (data[i] == null || comp.compare(v, data[i]) > 0) {
				for (int j = data.length - 1; j >= i + 1; j--) {
					data[j] = data[j - 1];
				}
				data[i] = v;
				if (size < data.length) ++size;
				return true;
			}
		}
		return false;
	}

	/**
	 * 最大全体の値を返す。要素が0個のときは {@link NoSuchElementException} を投げる。
	 */
	public T max() {
		if (size == 0) throw new NoSuchElementException();
		return data[0];
	}

	/**
	 * 2番目の最大値を返す。要素が2未満のときは {@link NoSuchElementException} を投げる。
	 */
	public T secondMax() {
		if (size < 2) throw new NoSuchElementException();
		return data[1];
	}

	/**
	 * 要素が存在すれば最大全体の値、なければ defaultValue を返す。
	 */
	public T maxOrDefault(T defaultValue) {
		return size >= 1 ? data[0]: defaultValue;
	}

	/**
	 * 要素が2個以上あれば2番目の最大値、なければ defaultValue を返す。
	 */
	public T secondMaxOrDefault(T defaultValue) {
		return size >= 2 ? data[1] : defaultValue;
	}
	
	/**
	 *  * 指定した値 {@code v} を 1 個削除したと仮定した場合の最大値を返す。
	 * @param v
	 * @param defaultValue
	 * @return
	 */
	public T maxIfRemovedOrDefault(T v, T defaultValue) {
		if(size==0)return defaultValue;
		if(v == null) return data[0];
		if(size==1) {
			if(comp.compare(v, data[0])==0 && equals.test(v, data[0])) return defaultValue;
			else return data[0];
		}
		if (comp.compare(v, data[0])==0 && equals.test(v, data[0])) return data[1];
		else return data[0];
	}

	public int size() { return size; }
	public boolean isEmpty() { return size == 0; }
	public void clear() { 
		Arrays.fill(data, null);
		size = 0;
	}
	
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < size;
			}

			@Override
			public T next() {
				if (!hasNext()) throw new NoSuchElementException();
				return data[idx++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(k)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("IncrementalTopKArray { data: " + java.util.Arrays.toString(java.util.Arrays.copyOf(data, size)) + ", size: " + size + " }");
	}
}
