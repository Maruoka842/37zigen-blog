package library.util.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/***
 * tailがa.lengthに比べて小さくなっても配列を取り直さない。
 * @param <T>
 */
public class IntArrayList implements Iterable<Integer> {
	@SuppressWarnings("unchecked")
	int defaultCapacity = 16;
	int[] a;
	public int tail = 0;
	public IntArrayList() {
		a = new int[defaultCapacity];
	}
	
	public IntArrayList(int initialCapacity) {
		a = new int[initialCapacity];
	}
	
	public void clear() {
		tail = 0;
	}
	
	public int peekFirst() {
		return a[0];
	}
	
	public int peekLast() {
		return a[tail - 1];
	}
	
	
	public int peekLastOrDefault(int defaultValue) {
		if (tail == 0) return defaultValue;
		else return a[tail - 1];
	}
	
	public void add(int v) {
		if (tail == a.length) resize(2 * a.length);
		a[tail] = v;
		tail++;
	}

	public void addAll(IntArrayList list) {
		ensureCapacity(tail + list.tail);
		System.arraycopy(list.a, 0, a, tail, list.tail);
		tail += list.tail;
	}

	public void addAll(int[] values) {
		ensureCapacity(tail + values.length);
		System.arraycopy(values, 0, a, tail, values.length);
		tail += values.length;
	}
	
	public int pollLast() {
		if (tail == 0) throw new NoSuchElementException();
		int ret = a[tail - 1];
		tail--;
		return ret;
	}
	
	public boolean isEmpty() {
		return tail == 0;
	}
	
	public boolean isNonEmpty() {
		return tail != 0;
	}
	
	public int get(int id) {
		if (id < 0 || id >= tail) throw new IndexOutOfBoundsException("get("+id+")は添え字"+0+"以上"+(tail-1)+"以下に違反");
		return a[id];
	}
	
	public int getOrDefault(int id, int defaultValue) {
		if (id < 0 || id >= tail) return defaultValue;
		return a[id];
	}
	
	public void set(int id, int value) {
		if (id < 0 || id >= tail) throw new IndexOutOfBoundsException();
		a[id] = value;
	}
	
	void resize(int size) {
		a = Arrays.copyOf(a, size);
	}

	void ensureCapacity(int capacity) {
		if (capacity <= a.length) return;
		int size = a.length;
		while (size < capacity) size *= 2;
		resize(size);
	}
	
	public int size() {
		return tail;
	}
	
	public int[] toArray() {
		return Arrays.copyOf(a, tail);
	}
	
	public long[] toLongArray() {
		long[]ret=new long[tail];
		for (int i = 0; i < tail; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
	public void swap(int i, int j) {
		if(i==j)return;
		var tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
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
	
    @Override
    public PrimitiveIterator.OfInt iterator() {
		return new PrimitiveIterator.OfInt() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < tail;
			}

			@Override
			public int nextInt() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(idx++);
			}
		};
    }
	
    
    public void sort() {
    	Arrays.sort(a, 0, tail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntArrayList that)) return false;
        if (tail != that.tail) return false;
        for (int i = 0; i < tail; i++) {
            if (a[i] != that.a[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < tail; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }

    @Override
	public String toString() {
	return Arrays.toString(toArray());
	}

	/**
	 * IntArrayListの現在の状態をコピーした新しいインスタンスを返す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return IntArrayListのコピー
	 */
	public IntArrayList copy() {
		IntArrayList ret = new IntArrayList(a.length);
		ret.tail = this.tail;
		ret.defaultCapacity = this.defaultCapacity;
		System.arraycopy(this.a, 0, ret.a, 0, this.a.length);
		return ret;
	}

	/**
	 * リストの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
}


