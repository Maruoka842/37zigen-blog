package library.util.algebra.strategy;

import java.util.Arrays;

public class TruncatedPolynomialRingStrategy<T> implements CommutativeRingStrategy<T[]> {
	private final PolynomialRingStrategy<T> polyBase;
	private final CommutativeRingStrategy<T> base;
	private final int n;

	public TruncatedPolynomialRingStrategy(CommutativeRingStrategy<T> base, int n) {
		this.polyBase = new PolynomialRingStrategy<>(base);
		this.base = base;
		this.n = n;
	}

	private T[] truncate(T[] a) {
		if (a.length <= n) return a;
		return Arrays.copyOf(a, n);
	}

	@Override
	public T[] zero() {
		return polyBase.zero();
	}

	@Override
	public T[] one() {
		return truncate(polyBase.one());
	}

	@Override
	public T[] add(T[] a, T[] b) {
		T[] res = polyBase.createArray(Math.min(n, Math.max(a.length, b.length)));
		for (int i = 0; i < res.length; i++) {
			T va = i < a.length ? a[i] : base.zero();
			T vb = i < b.length ? b[i] : base.zero();
			res[i] = base.add(va, vb);
		}
		return polyBase.trim(res);
	}

	@Override
	public T[] sub(T[] a, T[] b) {
		T[] res = polyBase.createArray(Math.min(n, Math.max(a.length, b.length)));
		for (int i = 0; i < res.length; i++) {
			T va = i < a.length ? a[i] : base.zero();
			T vb = i < b.length ? b[i] : base.zero();
			res[i] = base.sub(va, vb);
		}
		return polyBase.trim(res);
	}

	@Override
	public T[] mul(T[] a, T[] b) {
		T[] res = polyBase.createArray(Math.min(a.length + b.length - 1, n));
		for (int i = 0; i < a.length; i++) {
			if (base.equals(a[i], base.zero())) continue;
			for (int j = 0; j < Math.min(b.length, n - i); j++) {
				res[i + j] = base.add(res[i + j], base.mul(a[i], b[j]));
			}
		}
		return polyBase.trim(res);
	}

	@Override
	public T[] neg(T[] a) {
		T[] res = polyBase.createArray(a.length);
		for (int i = 0; i < a.length; i++) {
			res[i] = base.neg(a[i]);
		}
		return res;
	}

	@Override
	public boolean equals(T[] a, T[] b) {
		int len = Math.max(a.length, b.length);
		for (int i = 0; i < Math.min(len, n); i++) {
			T va = i < a.length ? a[i] : base.zero();
			T vb = i < b.length ? b[i] : base.zero();
			if (!base.equals(va, vb)) return false;
		}
		return true;
	}
}
