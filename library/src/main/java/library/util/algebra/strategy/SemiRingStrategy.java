package library.util.algebra.strategy;

import java.util.Objects;

public interface SemiRingStrategy<T> {
	T zero();
	T one();
	T add(T a, T b);
	T mul(T a, T b);
	boolean equals(T a, T b);

	/**
	 * a^n を返す。
	 * 事前条件: n >= 0。
	 * 事後条件: 戻り値 = a^n。
	 * 副作用: なし。
	 * 計算量: O(log n * mul)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 戻り値は新規作成されるか、a または one() と共有される。
	 * 例外・未定義条件: n < 0 のとき未定義。
	 * @param a 底
	 * @param n 指数
	 * @return a^n
	 */
	default T pow(T a, long n) {
		if (n < 0) throw new IllegalArgumentException("Exponent must be non-negative");
		T res = one();
		T base = a;
		while (n > 0) {
			if ((n & 1) == 1) res = mul(res, base);
			base = mul(base, base);
			n >>= 1;
		}
		return res;
	}
	
	default boolean isZero(T a) {
		return equals(zero(), a);
	}
	
	default boolean isOne(T a) {
		return equals(one(), a);
	}

	/**
	 * a のハッシュ値を返す。
	 * equals(a, b) なら hashCode(a) == hashCode(b) である必要がある。
	 * @param a 対象。
	 * @return ハッシュ値。
	 */
	default int hashCode(T a) {
		return Objects.hashCode(a);
	}
}
