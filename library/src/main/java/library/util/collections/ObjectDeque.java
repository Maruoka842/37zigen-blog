package library.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/***
 * lenがa.lengthに比べて小さくなっても配列を取り直さない。
 * @param <T>
 */
public class ObjectDeque<E> implements Iterable<E> {
	@SuppressWarnings("unchecked")
	E[] a = (E[]) new Object[16];
	int head = 0;
	int tail = 0;
	int len = 0;
	//[head, tail)に値を持つ。
	public ObjectDeque() {
		
	}
	
	public E peekFirst() {
		if (len == 0) throw new NoSuchElementException();
		return a[head];
	}
		
	public E peekLast() {
		if (len == 0) throw new NoSuchElementException();
		return a[(tail - 1) & (a.length - 1)];
	}
	
	public void addFirst(E v) {
		if (len == a.length) resize(2 * len);
		head = (head - 1) & (a.length - 1);
		a[head] = v;
		len++;
	}
	
	public void addLast(E v) {
		if (len == a.length) resize(2 * len);
		a[tail] = v;
		tail = (tail + 1) & (a.length - 1);
		++len;
	}
	
	public E pollFirst() {
		if (len == 0) throw new NoSuchElementException();
		E ret = a[head];
		head = (head + 1) & (a.length - 1);
		len--;
		return ret;
	}
	
	public E pollLast() {
		if (len == 0) throw new NoSuchElementException();
		E ret = a[(tail - 1) & (a.length  - 1)];
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
	
	public E get(int id) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException("get("+id+")は添え字"+0+"以上"+(len-1)+"以下に違反");
		return a[(head + id) & (a.length - 1)];
	}
	
	public void set(int id, E value) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException();
		a[(head + id) & (a.length - 1)] = value;
	}
	
	void resize(int size) {
		@SuppressWarnings("unchecked")
		E[] na = (E[]) new Object[size];
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
	
	public Object[] toArray() {
		Object[]ret= new Object[len];
		for (int i = 0; i < len; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
	public ObjectDeque<E> copy() {
		ObjectDeque<E> ret = new ObjectDeque<>();
		ret.resize(Math.max(16, Integer.highestOneBit(Math.max(1, len - 1)) << 1));
		for (int i = 0; i < len; i++) {
			ret.addLast(get(i));
		}
		return ret;
	}
	
	public E[] toArray(IntFunction<E[]> gen) {
		E[]ret=gen.apply(len);
		for (int i = 0; i < len; i++) {
			ret[i]=get(i);
		}
		return ret;
	}
	
	
    @Override
    public Iterator<E> iterator() {
		return new Iterator<>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < len;
			}

			@Override
			public E next() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(idx++);
			}
		};
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
		if (!(obj instanceof ObjectDeque)) return false;
		ObjectDeque<?> other = (ObjectDeque<?>) obj;
		if (this.len != other.len) return false;
		for (int i = 0; i < len; i++) {
			if (!java.util.Objects.equals(this.get(i), other.get(i))) return false;
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
			E val = get(i);
			result = 31 * result + (val == null ? 0 : val.hashCode());
		}
		return result;
	}
}


