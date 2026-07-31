package library.util.algebra.strategy;

public class PolynomialEuclideanStrategy<T> extends PolynomialRingStrategy<T> implements EuclideanDomainStrategy<T[]> {
	private final FieldStrategy<T> field;

	public PolynomialEuclideanStrategy(FieldStrategy<T> field) {
		super(field);
		this.field = field;
	}

	@Override
	public T[] div(T[] a, T[] b) {
		return divmod(a, b)[0];
	}

	@Override
	public T[] mod(T[] a, T[] b) {
		return divmod(a, b)[1];
	}

	@Override
	public long norm(T[] a) {
		return trim(a).length;
	}

	@Override
	public T[] canonicalUnit(T[] a) {
		T[] ta = trim(a);
		if (ta.length == 0) return one();
		T[] res = createArray(1);
		res[0] = ta[ta.length - 1];
		return res;
	}

	private T[][] divmod(T[] a, T[] b) {
		T[] ta = trim(a);
		T[] tb = trim(b);
		if (tb.length == 0) throw new ArithmeticException("Division by zero");
		if (ta.length < tb.length) {
			T[][] res = (T[][]) java.lang.reflect.Array.newInstance(clazz, 2, 0);
			res[0] = zero();
			res[1] = ta;
			return res;
		}
		T[] q = createArray(ta.length - tb.length + 1);
		T[] r = ta.clone();
		T invLeading = field.inv(tb[tb.length - 1]);
		for (int i = ta.length - 1; i >= tb.length - 1; i--) {
			q[i - tb.length + 1] = field.mul(r[i], invLeading);
			T factor = q[i - tb.length + 1];
			for (int j = 0; j < tb.length; j++) {
				r[i - tb.length + 1 + j] = field.sub(r[i - tb.length + 1 + j], field.mul(factor, tb[j]));
			}
		}
		T[][] res = (T[][]) java.lang.reflect.Array.newInstance(clazz, 2, 0);
		res[0] = trim(q);
		res[1] = trim(r);
		return res;
	}
}
