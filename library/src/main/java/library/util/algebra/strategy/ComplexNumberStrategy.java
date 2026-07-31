package library.util.algebra.strategy;

import library.util.algebra.instance.impl.ComplexNumber;

/**
 * 複素数体 C 上の代数的構造。
 */
public enum ComplexNumberStrategy implements FieldStrategy<ComplexNumber>, ExactDivRingStrategy<ComplexNumber> {
	STRATEGY;

	@Override
	public ComplexNumber zero() {
		return ComplexNumber.ZERO;
	}

	@Override
	public ComplexNumber one() {
		return ComplexNumber.ONE;
	}

	@Override
	public ComplexNumber add(ComplexNumber a, ComplexNumber b) {
		return a.add(b);
	}

	@Override
	public ComplexNumber mul(ComplexNumber a, ComplexNumber b) {
		return a.mul(b);
	}

	@Override
	public ComplexNumber neg(ComplexNumber a) {
		return a.neg();
	}

	@Override
	public ComplexNumber inv(ComplexNumber a) {
		return a.inv();
	}

	@Override
	public ComplexNumber div(ComplexNumber a, ComplexNumber b) {
		return a.div(b);
	}

	@Override
	public ComplexNumber mod(ComplexNumber a, ComplexNumber b) {
		return a.mod(b);
	}

	@Override
	public long norm(ComplexNumber a) {
		return a.norm();
	}

	@Override
	public boolean equals(ComplexNumber a, ComplexNumber b) {
		return a.equals(b);
	}

	@Override
	public ComplexNumber exactDiv(ComplexNumber a, ComplexNumber b) {
		return a.exactDiv(b);
	}

	@Override
	public ComplexNumber canonicalUnit(ComplexNumber a) {
		if (a.isZero()) return ComplexNumber.ONE;
		return a;
	}
}
