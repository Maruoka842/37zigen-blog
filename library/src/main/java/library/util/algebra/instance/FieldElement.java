package library.util.algebra.instance;

public interface FieldElement<X extends FieldElement<X>> extends EuclideanDomainElement<X> {
	X inv();

	@Override
	default X pow(long n) {
		if (n < 0) return inv().pow(-n);
		return EuclideanDomainElement.super.pow(n);
	}
}
