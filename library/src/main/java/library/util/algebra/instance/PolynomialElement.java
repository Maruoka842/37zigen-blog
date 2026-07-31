package library.util.algebra.instance;

import library.util.algebra.strategy.IntegralDomainStrategy;

public interface PolynomialElement<T, SELF extends PolynomialElement<T, SELF>> extends IntegralDomainElement<SELF> {
	IntegralDomainStrategy<T[]> strategy();
}
