package library.util.algebra.strategy;

/**
 * 整域 (Integral Domain) を表す。
 * 1. 可換環である (Commutative Ring)
 * 2. 1 != 0 である
 * 3. 零因子を持たない (No zero divisors)
 */
public interface IntegralDomainStrategy<T> extends CommutativeRingStrategy<T> {
}
