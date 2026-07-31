package library.util.collections;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/***
 * lenがa.lengthに比べて小さくなっても配列を取り直さない。
 * @param <T>
 */
public class LongDeque implements Iterable<Long> {
	long[] a = new long[16];
	int head = 0;
	int tail = 0;
	int len = 0;
	//[head, tail)に値を持つ。
	public LongDeque() {
		
	}
	
	public long peekFirst() {
		if (len == 0) throw new NoSuchElementException();
		return a[head];
	}
		
	public long peekLast() {
		if (len == 0) throw new NoSuchElementException();
		return a[(tail - 1) & (a.length - 1)];
	}
	
	public void addFirst(long v) {
		if (len == a.length) resize(2 * len);
		head = (head - 1) & (a.length - 1);
		a[head] = v;
		len++;
	}
	
	public void addLast(long v) {
		if (len == a.length) resize(2 * len);
		a[tail] = v;
		tail = (tail + 1) & (a.length - 1);
		++len;
	}
	
	public long pollFirst() {
		if (len == 0) throw new NoSuchElementException();
		long ret = a[head];
		head = (head + 1) & (a.length - 1);
		len--;
		return ret;
	}
	
	public long pollLast() {
		if (len == 0) throw new NoSuchElementException();
		long ret = a[(tail - 1) & (a.length  - 1)];
		tail = (tail - 1) & (a.length - 1);
		len--;
		return ret;
	}
	
	public boolean isEmpty() {
		return len == 0;
	}
	
	public boolean isNonEmpty() {
		return len != 0;
	}
	
	public long get(int id) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException("get("+id+")は添え字"+0+"以上"+(len-1)+"以下に違反");
		return a[(head + id) & (a.length - 1)];
	}
	
	public void set(int id, long value) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException();
		a[(head + id) & (a.length - 1)] = value;
	}
	
	void resize(int size) {
		long[] na = new long[size];
		for (int i = 0; i < len; i++) {
			na[i] = a[(head + i) & (a.length - 1)];
		}
		head = 0;
		tail = len;
		a = na;
	}
	
	public int size() {
		return len;
	}

	public void clear() {
		head = tail = len = 0;
	}
	
	public long[] toArray() {
		long[]ret=new long[len];
		for (int i = 0; i < len; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
    @Override
    public PrimitiveIterator.OfLong iterator() {
		return new PrimitiveIterator.OfLong() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < len;
			}

			@Override
			public long nextLong() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(idx++);
			}
		};
    }

	public LongDeque copy() {
		LongDeque ret = new LongDeque();
		ret.resize(Math.max(16, Integer.highestOneBit(Math.max(1, len - 1)) << 1));
		for (int i = 0; i < len; i++) {
			ret.addLast(get(i));
		}
		return ret;
	}

	/**
	 * 内部状態を文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "LongDeque { elements: " + java.util.Arrays.toString(toArray()) + " }";
	}

	/**
	 * 内部状態を標準出力に出力する。
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

	/**
	 * このデックと別のオブジェクトの同値性を判定します。
	 * 全ての要素が順序を含めて一致する場合に同値とみなします。
	 *
	 * <p>計算量: $O(N)$（$N$ はデックの要素数）</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongDeque)) return false;
		LongDeque other = (LongDeque) obj;
		if (this.len != other.len) return false;
		for (int i = 0; i < len; i++) {
			if (this.get(i) != other.get(i)) return false;
		}
		return true;
	}

	/**
	 * このデックのハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$（$N$ はデックの要素数）</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		int result = 1;
		for (int i = 0; i < len; i++) {
			result = 31 * result + Long.hashCode(get(i));
		}
		return result;
	}
}


