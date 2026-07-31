package library.util.algebra.strategy;

import java.util.Arrays;

public class PolynomialRingStrategy<T> implements IntegralDomainStrategy<T[]>, ExactDivRingStrategy<T[]> {
	protected final CommutativeRingStrategy<T> base;
	protected final Class<T> clazz;

	@SuppressWarnings("unchecked")
	public PolynomialRingStrategy(CommutativeRingStrategy<T> base) {
		this.base = base;
		this.clazz = (Class<T>) base.zero().getClass();
	}

	protected T[] createArray(int n) {
		@SuppressWarnings("unchecked")
		T[] arr = (T[]) java.lang.reflect.Array.newInstance(clazz, n);
		Arrays.fill(arr, base.zero());
		return arr;
	}

	protected T[] trim(T[] a) {
		int n = a.length;
		while (n > 0 && base.equals(a[n - 1], base.zero())) n--;
		if (n == a.length) return a;
		return Arrays.copyOf(a, n);
	}

	@Override
	public T[] zero() {
		return createArray(0);
	}

	@Override
	public T[] one() {
		T[] res = createArray(1);
		res[0] = base.one();
		return res;
	}

	@Override
	public T[] add(T[] a, T[] b) {
		T[] res = createArray(Math.max(a.length, b.length));
		for (int i = 0; i < res.length; i++) {
			T va = i < a.length ? a[i] : base.zero();
			T vb = i < b.length ? b[i] : base.zero();
			res[i] = base.add(va, vb);
		}
		return trim(res);
	}

	@Override
	public T[] sub(T[] a, T[] b) {
		T[] res = createArray(Math.max(a.length, b.length));
		for (int i = 0; i < res.length; i++) {
			T va = i < a.length ? a[i] : base.zero();
			T vb = i < b.length ? b[i] : base.zero();
			res[i] = base.sub(va, vb);
		}
		return trim(res);
	}

	@Override
	public T[] mul(T[] a, T[] b) {
		if (a.length == 0 || b.length == 0) return zero();
		T[] res = createArray(a.length + b.length - 1);
		for (int i = 0; i < a.length; i++) {
			if (base.equals(a[i], base.zero())) continue;
			for (int j = 0; j < b.length; j++) {
				res[i + j] = base.add(res[i + j], base.mul(a[i], b[j]));
			}
		}
		return trim(res);
	}

	@Override
	public T[] neg(T[] a) {
		T[] res = createArray(a.length);
		for (int i = 0; i < a.length; i++) {
			res[i] = base.neg(a[i]);
		}
		return res;
	}

	@Override
	public boolean equals(T[] a, T[] b) {
		T[] ta = trim(a);
		T[] tb = trim(b);
		if (ta.length != tb.length) return false;
		for (int i = 0; i < ta.length; i++) {
			if (!base.equals(ta[i], tb[i])) return false;
		}
		return true;
	}

	@Override
	public T[] exactDiv(T[] a, T[] b) {
		T[] ta = trim(a);
		T[] tb = trim(b);
		if (tb.length == 0) throw new ArithmeticException("Division by zero");
		if (ta.length < tb.length) {
			if (ta.length == 0) return zero();
			throw new ArithmeticException("Not exactly divisible");
		}
		if (!(base instanceof FieldStrategy)) {
			// Naive division for ExactDivRing. Assumes leading coefficient of b is a unit or divides others.
			// This is limited but enough for many cases.
			T[] res = createArray(ta.length - tb.length + 1);
			T[] rem = ta.clone();
			if (!(base instanceof ExactDivRingStrategy)) throw new UnsupportedOperationException("Base must be ExactDivRingStrategy");
			ExactDivRingStrategy<T> ex = (ExactDivRingStrategy<T>) base;
			for (int i = ta.length - 1; i >= tb.length - 1; i--) {
				res[i - tb.length + 1] = ex.exactDiv(rem[i], tb[tb.length - 1]);
				T q = res[i - tb.length + 1];
				for (int j = 0; j < tb.length; j++) {
					rem[i - tb.length + 1 + j] = base.sub(rem[i - tb.length + 1 + j], base.mul(q, tb[j]));
				}
			}
			for (T r : rem) if (!base.equals(r, base.zero())) throw new ArithmeticException("Not exactly divisible");
			return trim(res);
		}
		// If Field, we can use PolynomialEuclideanStrategy.div
		return new PolynomialEuclideanStrategy<>((FieldStrategy<T>) base).div(a, b);
	}
}
