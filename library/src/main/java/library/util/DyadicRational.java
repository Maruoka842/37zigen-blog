package library.util;

/**
 * dyadic rational（2進有理数）を表すクラス。
 *
 * <p>値 V の数学的定義：
 * V = a + b / 2^M
 * ここで、a および b は整数（long）であり、M = 61 である。
 *
 * 【不変条件（Invariants）】
 * 任意の正規化された DyadicRational インスタンスにおいて、以下の条件が厳格に維持される：
 * 0 <= b < 2^M
 *
 * したがって、負の数や減算、符号反転などの結果も、常に 0 <= b < 2^M を満たすように正規化される。
 * 例えば、負の有理数 -1.25 は -2 + 0.75 として表現され、a = -2, b = 0.75 * 2^M となる（b は常に非負）。
 * すべてのコンストラクタ、ファクトリメソッド、および演算処理（add, subtract, negate, simplest）は
 * この不変条件を厳密に維持するように実装されている。</p>
 */
// 未テスト
public class DyadicRational implements Comparable<DyadicRational> {
	/** 整数部分（任意の値、正負およびゼロを許容） */
	public final long a;
	/** 分子部分（不変条件 0 <= b < 2^M を常に満たす） */
	public final long b;
	/** 分母の指数、2^M（M = 61） */
	public static final int M = 61;
	
	public static final DyadicRational ZERO=new DyadicRational(0);

	/**
	 * 整数から DyadicRational を構築する。
	 * @param a 整数値
	 */
	public DyadicRational(long a) {
		this.a = a;
		this.b = 0;
	}

	/**
	 * 整数部分と分子部分から DyadicRational を構築する。
	 * @param a 整数部分
	 * @param b 分子部分 (0 <= b < 2^M)
	 */
	public DyadicRational(long a, long b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * x + y / z から DyadicRational を構築する。
	 * @param x 整数部分
	 * @param y 分子
	 * @param z 分母（2のべき乗など）
	 */
	public DyadicRational(long x, long y, long z) {
		long q = Math.floorDiv(y, z);
		long r = Math.floorMod(y, z);
		this.a = x + q;
		this.b = r * ((1L << M) / z);
	}

	/**
	 * 整数部分 a と分子部分 b から DyadicRational を構築する。
	 * 範囲外の b（正規化が必要な値）は、不変条件 0 <= b < 2^M を満たすように正規化される。
	 * @param a 整数部分
	 * @param b 分子部分
	 * @return DyadicRational インスタンス
	 */
	public static DyadicRational fromAb(long a, long b) {
		long limit = 1L << M;
		if (b >= limit) {
			a++;
			b -= limit;
		} else if (b < 0) {
			a--;
			b += limit;
		}
		return new DyadicRational(a, b);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DyadicRational)) return false;
		DyadicRational other = (DyadicRational) o;
		return a == other.a && b == other.b;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(a, b);
	}

	@Override
	public int compareTo(DyadicRational other) {
		if (a != other.a) {
			return Long.compare(a, other.a);
		}
		return Long.compare(b, other.b);
	}

	/**
	 * 加算を行う。
	 * @param other 足す数
	 * @return 計算結果
	 */
	public DyadicRational add(DyadicRational other) {
		return fromAb(a + other.a, b + other.b);
	}

	/**
	 * 減算を行う。
	 * @param other 引く数
	 * @return 計算結果
	 */
	public DyadicRational subtract(DyadicRational other) {
		return fromAb(a - other.a, b - other.b);
	}

	/**
	 * 符号反転を行う。
	 * @return 計算結果
	 */
	public DyadicRational negate() {
		return fromAb(-a, -b);
	}

	/**
	 * 正の無限大を表す。
	 * @return 正の無限大
	 */
	public static DyadicRational infinity() {
		return fromAb(1L << M, 0);
	}

	/**
	 * 負の無限大を表す。
	 * @return 負の無限大
	 */
	public static DyadicRational negInfinity() {
		return fromAb(-(1L << M), 0);
	}

	/**
	 * 2つの dyadic rational の間にある最も単純な dyadic rational を計算する。
	 * @param x 下限
	 * @param y 上限
	 * @param include_x x を含むかどうか
	 * @param include_y y を含むかどうか
	 * @return 最も単純な dyadic rational
	 */
	public static DyadicRational simplest(DyadicRational x, DyadicRational y, boolean include_x, boolean include_y) {
		if (include_x && !x.equals(negInfinity())) {
			x = x.subtract(fromAb(0, 2));
		}
		if (include_y && !y.equals(infinity())) {
			y = y.add(fromAb(0, 2));
		}
		if (x.compareTo(y) >= 0) {
			throw new IllegalArgumentException("x must be strictly less than y");
		}
		if (y.a < 0) {
			return simplest(y.negate(), x.negate(), false, false).negate();
		}

		long limit = 1L << M;
		{
			long l = x.a + 1;
			long r = (y.b == 0 ? y.a - 1 : y.a);
			if (l <= 0 && 0 <= r) return new DyadicRational(0);
			if (l <= r && 0 <= l) return new DyadicRational(l);
			if (l <= r && r <= 0) return new DyadicRational(r);
		}

		long l = x.b + 1;
		long r = (y.b == 0 ? limit - 1 : y.b - 1);
		if (l == r) return fromAb(x.a, l);
		int k = 63 - Long.numberOfLeadingZeros(l ^ r);
		r &= ~((1L << k) - 1);
		return fromAb(x.a, r);
	}

	@Override
	public String toString() {
		long x = a;
		long y = b;
		long z = 1L << M;
		while (y % 2 == 0 && z % 2 == 0 && z > 1) {
			y /= 2;
			z /= 2;
		}
		y += x * z;
		return y + "/" + z;
	}

	/**
	 * double値に変換する。
	 * @return double値
	 */
	public double toDouble() {
		return a + (double) b / (1L << M);
	}
}
