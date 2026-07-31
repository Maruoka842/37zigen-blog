package library.util.algebra.instance;

public interface EuclideanDomainElement<X extends EuclideanDomainElement<X>> extends UFDElement<X> {
	X div(X a);
	X mod(X a);
	long norm();
}
