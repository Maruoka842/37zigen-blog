package library.util.algebra.strategy;

import library.util.algebra.instance.impl.GaussInt;

/**
 * ガウス整数環 Z[i] 上のユークリッド整域としての代数的構造。
 */
public class GaussIntStrategy implements EuclideanDomainStrategy<GaussInt>, ExactDivRingStrategy<GaussInt> {
	@Override
	public GaussInt zero() {
		return GaussInt.ZERO;
	}

	@Override
	public GaussInt one() {
		return GaussInt.ONE;
	}

	@Override
	public GaussInt add(GaussInt a, GaussInt b) {
		return a.add(b);
	}

	@Override
	public GaussInt mul(GaussInt a, GaussInt b) {
		return a.mul(b);
	}

	@Override
	public GaussInt neg(GaussInt a) {
		return a.neg();
	}

	@Override
	public boolean equals(GaussInt a, GaussInt b) {
		return a.equals(b);
	}

	@Override
	public GaussInt div(GaussInt a, GaussInt b) {
		return a.div(b);
	}

	@Override
	public GaussInt mod(GaussInt a, GaussInt b) {
		return a.rem(b);
	}

	@Override
	public long norm(GaussInt a) {
		return a.norm();
	}

	@Override
	public GaussInt exactDiv(GaussInt a, GaussInt b) {
		return a.div(b);
	}

	@Override
	public GaussInt canonicalUnit(GaussInt a) {
		if (a.isZero()) return GaussInt.ONE;
		GaussInt cur = a;
		GaussInt u = GaussInt.ONE;
		for (int i = 0; i < 4; i++) {
			if (cur.re() > 0 && cur.im() >= 0) return u;
			cur = cur.muli();
			u = u.muli();
		}
		return GaussInt.ONE;
	}
}
