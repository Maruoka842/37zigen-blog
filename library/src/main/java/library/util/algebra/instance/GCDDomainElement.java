package library.util.algebra.instance;

public interface GCDDomainElement<X extends GCDDomainElement<X>> extends IntegralDomainElement<X> {
	X gcd(X a);
}
