package library.util.algebra.strategy;

import library.util.algebra.instance.impl.HurwitzInt;

/**
 * フルヴィッツ整数環上の代数的構造。
 */
public class HurwitzIntStrategy implements RingStrategy<HurwitzInt> {
	@Override
	public HurwitzInt zero() {
		return HurwitzInt.ZERO;
	}

	@Override
	public HurwitzInt one() {
		return HurwitzInt.ONE;
	}

	@Override
	public HurwitzInt add(HurwitzInt a, HurwitzInt b) {
		return a.add(b);
	}

	@Override
	public HurwitzInt mul(HurwitzInt a, HurwitzInt b) {
		return a.mul(b);
	}

	@Override
	public HurwitzInt neg(HurwitzInt a) {
		return a.neg();
	}

	@Override
	public boolean equals(HurwitzInt a, HurwitzInt b) {
		return a.equals(b);
	}
}
