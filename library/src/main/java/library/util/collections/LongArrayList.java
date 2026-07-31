package library.util.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/***
 * lenがa.lengthに比べて小さくなっても配列を取り直さない。
 */
public class LongArrayList implements Iterable<Long> {
	@SuppressWarnings("unchecked")
	long[] a;
	int tail = 0;
	int len = 0;
	int capacity = 16;
	
	public LongArrayList() {
		a = new long[capacity];
	}
	
	public LongArrayList(int capacity) {
		this.capacity = capacity;
		a = new long[capacity];
	}
	
	public long peekFirst() {
		if (len == 0) throw new NoSuchElementException();
		return a[0];
	}
		
	public long peekLast() {
		if (len == 0) throw new NoSuchElementException();
		return a[tail - 1];
	}
	
	public void add(long v) {
		if (len == a.length) resize(2 * len);
		a[tail] = v;
		tail++;
		++len;
	}
	
	public void clear() {
		tail = 0;
		len = 0;
	}
	
	public long pollLast() {
		if (len == 0) throw new NoSuchElementException();
		long ret = a[tail - 1];
		tail--;
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
		return a[id];
	}
	
	public void set(int id, long value) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException();
		a[id] = value;
	}
	
	void resize(int size) {
		a = Arrays.copyOf(a, size);
		tail = len;
	}
	
	public int size() {
		return len;
	}
	
	public void reverse() {
		int s = 0;
		int t = tail - 1;
		while (s < t) {
			{
				var tmp = a[s];
				a[s] = a[t];
				a[t] = tmp;
			}
			++s;--t;
		}
	}
	
    public void sort() {
    	Arrays.sort(a, 0, tail);
    }
    
	public long[] toArray() {
		return Arrays.copyOf(a, len);
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

	/**
	 * LongArrayListの現在の状態をコピーした新しいインスタンスを返す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return LongArrayListのコピー
	 */
	public LongArrayList copy() {
		LongArrayList ret = new LongArrayList(a.length);
		ret.tail = this.tail;
		ret.len = this.len;
		ret.capacity = this.capacity;
		System.arraycopy(this.a, 0, ret.a, 0, this.a.length);
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
		return "LongArrayList { elements: " + java.util.Arrays.toString(toArray()) + " }";
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
}


