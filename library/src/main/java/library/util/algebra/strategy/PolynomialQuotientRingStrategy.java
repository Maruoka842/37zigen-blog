package library.util.algebra.strategy;

import java.util.Arrays;

public class PolynomialQuotientRingStrategy<T> implements CommutativeRingStrategy<T[]> {
	private final PolynomialEuclideanStrategy<T> polyBase;
	private final T[] modPoly;

	public PolynomialQuotientRingStrategy(FieldStrategy<T> field, T[] modPoly) {
		this.polyBase = new PolynomialEuclideanStrategy<>(field);
		this.modPoly = polyBase.trim(modPoly);
		if (this.modPoly.length == 0) throw new IllegalArgumentException("Modulus cannot be zero");
	}

	@Override
	public T[] zero() {
		return polyBase.zero();
	}

	@Override
	public T[] one() {
		return polyBase.mod(polyBase.one(), modPoly);
	}

	@Override
	public T[] add(T[] a, T[] b) {
		return polyBase.mod(polyBase.add(a, b), modPoly);
	}

	@Override
	public T[] sub(T[] a, T[] b) {
		return polyBase.mod(polyBase.sub(a, b), modPoly);
	}

	@Override
	public T[] mul(T[] a, T[] b) {
		return polyBase.mod(polyBase.mul(a, b), modPoly);
	}

	@Override
	public T[] neg(T[] a) {
		return polyBase.mod(polyBase.neg(a), modPoly);
	}

	@Override
	public boolean equals(T[] a, T[] b) {
		return polyBase.equals(polyBase.mod(a, modPoly), polyBase.mod(b, modPoly));
	}
}
