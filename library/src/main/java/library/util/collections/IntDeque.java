package library.util.collections;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/***
 * lenがa.lengthに比べて小さくなっても配列を取り直さない。
 * @param <T>
 */
public class IntDeque implements Iterable<Integer> {
	@SuppressWarnings("unchecked")
	int[] a = new int[16];
	int head = 0;
	int tail = 0;
	int len = 0;
	//[head, tail)に値を持つ。
	public IntDeque() {
		
	}
	
	public int peekFirst() {
		if (len == 0) throw new NoSuchElementException();
		return a[head];
	}
		
	public int peekLast() {
		if (len == 0) throw new NoSuchElementException();
		return a[(tail - 1) & (a.length - 1)];
	}
	
	public void addFirst(int v) {
		if (len == a.length) resize(2 * len);
		head = (head - 1) & (a.length - 1);
		a[head] = v;
		len++;
	}
	
	public void addLast(int v) {
		if (len == a.length) resize(2 * len);
		a[tail] = v;
		tail = (tail + 1) & (a.length - 1);
		++len;
	}
	
	public int pollFirst() {
		if (len == 0) throw new NoSuchElementException();
		int ret = a[head];
		head = (head + 1) & (a.length - 1);
		len--;
		return ret;
	}
	
	public int pollLast() {
		if (len == 0) throw new NoSuchElementException();
		int ret = a[(tail - 1) & (a.length  - 1)];
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
	
	public int get(int id) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException("get("+id+")は添え字"+0+"以上"+(len-1)+"以下に違反");
		return a[(head + id) & (a.length - 1)];
	}
	
	public void set(int id, int value) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException();
		a[(head + id) & (a.length - 1)] = value;
	}
	
	void resize(int size) {
		@SuppressWarnings("unchecked")
		int[] na = new int[size];
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
	
	public int[] toArray() {
		int[]ret=new int[len];
		for (int i = 0; i < len; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
	public long[] tolongArray() {
		long[]ret=new long[len];
		for (int i = 0; i < len; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
	
    @Override
    public PrimitiveIterator.OfInt iterator() {
		return new PrimitiveIterator.OfInt() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < len;
			}

			@Override
			public int nextInt() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(idx++);
			}
		};
    }
    
    
	public IntDeque copy() {
		IntDeque ret = new IntDeque();
		ret.resize(Math.max(16, Integer.highestOneBit(Math.max(1, len - 1)) << 1));
		for (int i = 0; i < len; i++) {
			ret.addLast(get(i));
		}
		return ret;
	}

	/**
	 * デックの内容を表す文字列を返す。
	 * @return デック内容の文字列
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < len; i++) {
			sb.append(get(i));
			if (i < len - 1) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * デックの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
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
		if (!(obj instanceof IntDeque)) return false;
		IntDeque other = (IntDeque) obj;
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
			result = 31 * result + Integer.hashCode(get(i));
		}
		return result;
	}
}


