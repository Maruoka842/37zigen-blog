package library.util.collections;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/***
 * lenはa.length以下で、かつ長さが変わっても要素を取りこぼさない。
 */
public class DoubleDeque implements Iterable<Double> {
	double[] a = new double[16];
	int head = 0;
	int tail = 0;
	int len = 0;

	// [head, tail) に要素を持つ。
	public DoubleDeque() {
	}

	public double peekFirst() {
		if (len == 0) throw new NoSuchElementException();
		return a[head];
	}

	public double peekLast() {
		if (len == 0) throw new NoSuchElementException();
		return a[(tail - 1) & (a.length - 1)];
	}

	public void addFirst(double v) {
		if (len == a.length) resize(2 * len);
		head = (head - 1) & (a.length - 1);
		a[head] = v;
		len++;
	}

	public void addLast(double v) {
		if (len == a.length) resize(2 * len);
		a[tail] = v;
		tail = (tail + 1) & (a.length - 1);
		len++;
	}

	public double pollFirst() {
		if (len == 0) throw new NoSuchElementException();
		double ret = a[head];
		head = (head + 1) & (a.length - 1);
		len--;
		return ret;
	}

	public double pollLast() {
		if (len == 0) throw new NoSuchElementException();
		double ret = a[(tail - 1) & (a.length - 1)];
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

	public double get(int id) {
		if (id < 0 || id >= len) {
			throw new IndexOutOfBoundsException("get(" + id + ") is out of range [0, " + (len - 1) + "]");
		}
		return a[(head + id) & (a.length - 1)];
	}

	public void set(int id, double value) {
		if (id < 0 || id >= len) throw new IndexOutOfBoundsException();
		a[(head + id) & (a.length - 1)] = value;
	}

	void resize(int size) {
		double[] na = new double[size];
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

	public double[] toArray() {
		double[] ret = new double[len];
		for (int i = 0; i < len; i++) {
			ret[i] = get(i);
		}
		return ret;
	}

	@Override
	public PrimitiveIterator.OfDouble iterator() {
		return new PrimitiveIterator.OfDouble() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < len;
			}

			@Override
			public double nextDouble() {
				if (!hasNext()) throw new NoSuchElementException();
				return get(idx++);
			}
		};
	}

	public DoubleDeque copy() {
		DoubleDeque ret = new DoubleDeque();
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
		if (!(obj instanceof DoubleDeque)) return false;
		DoubleDeque other = (DoubleDeque) obj;
		if (this.len != other.len) return false;
		for (int i = 0; i < len; i++) {
			if (Double.compare(this.get(i), other.get(i)) != 0) return false;
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
			result = 31 * result + Double.hashCode(get(i));
		}
		return result;
	}
}
