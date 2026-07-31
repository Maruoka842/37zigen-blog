package library.util.algebra.instance.impl;

import java.util.Objects;
import library.util.algebra.instance.ExactDivRingElement;
import library.util.algebra.instance.FieldElement;
import library.util.algebra.strategy.ComplexNumberStrategy;

/**
 * 複素数を表すレコード。
 * @param re 実部
 * @param im 虚部
 */
public record ComplexNumber(double re, double im) implements FieldElement<ComplexNumber>, ExactDivRingElement<ComplexNumber> {

	public static final ComplexNumber ZERO = new ComplexNumber(0, 0);
	public static final ComplexNumber ONE = new ComplexNumber(1, 0);
	public static final ComplexNumber I = new ComplexNumber(0, 1);

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumberStrategy parent() {
		return ComplexNumberStrategy.STRATEGY;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber self() {
		return this;
	}

	/**
	 * 極座標形式から複素数を生成する。
	 * 計算量: O(1)。
	 * @param r 絶対値
	 * @param theta 偏角
	 * @return 複素数
	 */
	public static ComplexNumber polar(double r, double theta) {
		return new ComplexNumber(r * Math.cos(theta), r * Math.sin(theta));
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber add(ComplexNumber o) {
		return new ComplexNumber(re + o.re, im + o.im);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber sub(ComplexNumber o) {
		return new ComplexNumber(re - o.re, im - o.im);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber mul(ComplexNumber o) {
		return new ComplexNumber(re * o.re - im * o.im, re * o.im + im * o.re);
	}

	/**
	 * 計算量: O(1)。
	 */
	public ComplexNumber mul(double d) {
		return new ComplexNumber(re * d, im * d);
	}

	/**
	 * 計算量: O(1)。
	 */
	public ComplexNumber div(double d) {
		return new ComplexNumber(re / d, im / d);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber div(ComplexNumber o) {
		double d = o.absSq();
		return new ComplexNumber((re * o.re + im * o.im) / d, (im * o.re - re * o.im) / d);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber inv() {
		double d = absSq();
		return new ComplexNumber(re / d, -im / d);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber neg() {
		return new ComplexNumber(-re, -im);
	}

	/**
	 * 共役複素数を返す。
	 * 計算量: O(1)。
	 * @return 共役複素数
	 */
	public ComplexNumber conj() {
		return new ComplexNumber(re, -im);
	}

	/**
	 * 絶対値を返す。
	 * 計算量: O(1)。
	 * @return 絶対値
	 */
	public double abs() {
		return Math.hypot(re, im);
	}

	/**
	 * 絶対値の 2 乗を返す。
	 * 計算量: O(1)。
	 * @return 絶対値の 2 乗
	 */
	public double absSq() {
		return re * re + im * im;
	}

	/**
	 * 偏角を返す。
	 * 計算量: O(1)。
	 * @return 偏角 (-PI to PI)
	 */
	public double arg() {
		return Math.atan2(im, re);
	}

	/**
	 * 指数関数 exp(this) を返す。
	 * 計算量: O(1)。
	 * @return exp(this)
	 */
	public ComplexNumber exp() {
		double e = Math.exp(re);
		return new ComplexNumber(e * Math.cos(im), e * Math.sin(im));
	}

	/**
	 * 対数関数 log(this) を返す。
	 * 計算量: O(1)。
	 * @return log(this)
	 */
	public ComplexNumber log() {
		return new ComplexNumber(Math.log(abs()), arg());
	}

	/**
	 * 冪乗 this^n を返す。
	 * 計算量: O(1)。
	 * @param n 指数
	 * @return this^n
	 */
	public ComplexNumber pow(double n) {
		return polar(Math.pow(abs(), n), arg() * n);
	}

	/**
	 * 複素数冪 this^o を返す。
	 * 計算量: O(1)。
	 * @param o 指数
	 * @return this^o
	 */
	public ComplexNumber pow(ComplexNumber o) {
		if (isZero()) return o.isZero() ? ONE : ZERO;
		return o.mul(log()).exp();
	}

	/**
	 * 平方根 sqrt(this) を返す。
	 * 計算量: O(1)。
	 * @return sqrt(this)
	 */
	public ComplexNumber sqrt() {
		return pow(0.5);
	}

	/**
	 * 計算量: O(1)。
	 */
	public boolean isZero() {
		return re == 0 && im == 0;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber one() {
		return ONE;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber zero() {
		return ZERO;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber exactDiv(ComplexNumber o) {
		return div(o);
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber mod(ComplexNumber o) {
		if (o.isZero()) throw new ArithmeticException("Division by zero");
		return ZERO;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public long norm() {
		return isZero() ? 0 : 1;
	}

	/**
	 * 計算量: O(1)。
	 */
	@Override
	public ComplexNumber gcd(ComplexNumber o) {
		if (isZero() && o.isZero()) return ZERO;
		return ONE;
	}

	@Override
	public String toString() {
		return "(" + re + ", " + im + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ComplexNumber that)) return false;
		return Double.compare(that.re, re) == 0 && Double.compare(that.im, im) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(re, im);
	}
}
