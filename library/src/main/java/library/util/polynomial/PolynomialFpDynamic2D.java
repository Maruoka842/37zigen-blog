package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import library.util.Itertools;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.MathUtils;
import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.PolynomialEuclideanStrategy;
import library.util.algebra.strategy.UFDStrategy;
import library.util.linalg.MatrixUtilsZ;
import library.util.linalg.MatrixUtilsFp;

public class PolynomialFpDynamic2D implements UFDStrategy<long[][]>, ExactDivRingStrategy<long[][]> {
	/**
	 * 最適な分解結果を保持するレコード。
	 * f = phConst * phFactors - qConst * qFactors の関係を満たす。
	 * @param phConst ph部分の定数倍
	 * @param phFactors ph部分の既約因子
	 * @param qConst q部分の定数倍
	 * @param qFactors q部分の既約因子
	 * @param score Σ(項数-1) で計算される複雑度スコア
	 */
	public record BestDecomposition2D(long phConst, Factor[] phFactors, long qConst, Factor[] qFactors, long score) {}
	public final long mod;
	Fp fp;
	final PolynomialFpDynamic poly1d;

	/** 998244353 = 119×2^23+1, 原始根3 */
	public static final PolynomialFpDynamic2D MOD998244353 = new PolynomialFpDynamic2D(PolynomialFpDynamic.MOD998244353);
	/** 469762049 = 7×2^26+1, 原始根3 */
	public static final PolynomialFpDynamic2D MOD469762049 = new PolynomialFpDynamic2D(PolynomialFpDynamic.MOD469762049);
	/** 167772161 = 5×2^25+1, 原始根3 */
	public static final PolynomialFpDynamic2D MOD167772161 = new PolynomialFpDynamic2D(PolynomialFpDynamic.MOD167772161);
	/** 754974721 = 45×2^24+1, 原始根11 */
	public static final PolynomialFpDynamic2D MOD754974721 = new PolynomialFpDynamic2D(PolynomialFpDynamic.MOD754974721);
	/** 1004535809 = 479×2^21+1, 原始根3 */
	public static final PolynomialFpDynamic2D MOD1004535809 = new PolynomialFpDynamic2D(PolynomialFpDynamic.MOD1004535809);

	public static PolynomialFpDynamic2D of(long mod) {
		if (mod == 998244353L) return MOD998244353;
		if (mod == 469762049L) return MOD469762049;
		if (mod == 167772161L) return MOD167772161;
		if (mod == 754974721L) return MOD754974721;
		if (mod == 1004535809L) return MOD1004535809;
		return new PolynomialFpDynamic2D(mod);
	}

	public static PolynomialFpDynamic2D of(PolynomialFpDynamic poly1d) {
		if (poly1d == PolynomialFpDynamic.MOD998244353) return MOD998244353;
		if (poly1d == PolynomialFpDynamic.MOD469762049) return MOD469762049;
		if (poly1d == PolynomialFpDynamic.MOD167772161) return MOD167772161;
		if (poly1d == PolynomialFpDynamic.MOD754974721) return MOD754974721;
		if (poly1d == PolynomialFpDynamic.MOD1004535809) return MOD1004535809;
		return new PolynomialFpDynamic2D(poly1d);
	}

	private PolynomialFpDynamic2D(long mod) {
		this.mod = mod;
		fp = new Fp(mod);
		poly1d = PolynomialFpDynamic.of(mod);
	}

	private PolynomialFpDynamic2D(PolynomialFpDynamic poly1d) {
		this.mod = poly1d.mod;
		fp = new Fp(mod);
		this.poly1d = poly1d;
	}

	/**
	 * https://atcoder.jp/contests/abc214/submissions/71958167
	 */
	public long[][] sparseMul(long[][] a, long[][] sparsePoly) {
		ArrayList<Integer> degXs = new ArrayList<>();
		ArrayList<Integer> degYs = new ArrayList<>();
		ArrayList<Long> coefs = new ArrayList<>();
		int maxDegX = 0, maxDegY = 0;
		for (int i = 0; i < sparsePoly.length; i++)
			for (int j = 0; j < sparsePoly[i].length; j++)
				if (sparsePoly[i][j] != 0) {
					degXs.add(i); degYs.add(j); coefs.add(sparsePoly[i][j]);
					maxDegX = Math.max(maxDegX, i); maxDegY = Math.max(maxDegY, j);
				}
		int m0 = 0;
		for (long[] row : a) m0 = Math.max(m0, row.length);
		long[][] b = new long[a.length + maxDegX][m0 + maxDegY];
		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++)
				for (int k = 0; k < coefs.size(); ++k) {
					int dx = degXs.get(k), dy = degYs.get(k);
					if (i - dx >= 0 && i - dx < a.length && j - dy >= 0 && j - dy < a[i - dx].length)
						b[i][j] = (b[i][j] + coefs.get(k) * a[i - dx][j - dy]) % mod;
				}
		return b;
	}
	
	public long[][] sparseExp(long[][] f, int nx, int ny) {
		if (nx <= 0 || ny <= 0) return zero();
		if (f.length > 0 && f[0].length > 0 && poly1d.fp.reduce(f[0][0]) != 0) throw new ArithmeticException("f[0][0] must be zero");
		ArrayList<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (f[i][j] != 0) terms.add(new Term2D(i, j, poly1d.fp.reduce(f[i][j])));
			}
		}
		long[][] g = new long[nx][ny];
		g[0][0] = 1;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				if (i == 0 && j == 0) continue;
				long tmp = 0;
				if (i > 0) {
					for (Term2D t : terms) {
						int pi = i - t.dx, pj = j - t.dy;
						if (pi >= 0 && pj >= 0 && pi < nx && pj < ny) {
							tmp = (tmp + t.dx * t.v % mod * g[pi][pj]) % mod;
						}
					}
					g[i][j] = tmp * MathUtils.modInv(i, mod) % mod;
				} else {
					for (Term2D t : terms) {
						if (t.dx != 0) continue;
						int pj = j - t.dy;
						if (pj >= 0 && pj < ny) {
							tmp = (tmp + t.dy * t.v % mod * g[0][pj]) % mod;
						}
					}
					g[i][j] = tmp * MathUtils.modInv(j, mod) % mod;
				}
			}
		}
		return g;
	}

	public long[][] sparsePow(long[][] f, int nx, int ny, long k) {
		if (nx <= 0 || ny <= 0) return zero();
		if (k == 0) { long[][] res = new long[nx][ny]; res[0][0] = 1; return res; }
		int d0x = -1, d0y = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (f[i][j] != 0) { d0x = i; d0y = j; break outer; }
			}
		}
		if (d0x == -1) return new long[nx][ny];
		if ((d0x > 0 && (nx - 1) / d0x < k) || (d0y > 0 && (ny - 1) / d0y < k)) return new long[nx][ny];
		int bx = (int) (d0x * k), by = (int) (d0y * k);
		if (bx >= nx || by >= ny) return new long[nx][ny];
		ArrayList<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (i == d0x && j == d0y) continue;
				if (f[i][j] != 0) terms.add(new Term2D(i - d0x, j - d0y, poly1d.fp.reduce(f[i][j])));
			}
		}
		long[][] res = new long[nx][ny];
		res[bx][by] = MathUtils.modPow(poly1d.fp.reduce(f[d0x][d0y]), k % (mod - 1), mod);
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0x][d0y]), mod);
		long kMod = k % mod;
		for (int i = bx; i < nx; i++) {
			for (int j = by; j < ny; j++) {
				if (i == bx && j == by) continue;
				long tmp = 0;
				if (i > bx) {
					int n = i - bx;
					for (Term2D t : terms) {
						int pi = i - t.dx, pj = j - t.dy;
						if (pi >= bx && pj >= by && pi < nx && pj < ny) {
							long val = ((kMod + 1) * t.dx % mod - n + mod) % mod;
							tmp = (tmp + val * t.v % mod * res[pi][pj]) % mod;
						}
					}
					res[i][j] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
				} else {
					int m = j - by;
					for (Term2D t : terms) {
						if (t.dx != 0) continue;
						int pj = j - t.dy;
						if (pj >= by && pj < ny) {
							long val = ((kMod + 1) * t.dy % mod - m + mod) % mod;
							tmp = (tmp + val * t.v % mod * res[i][pj]) % mod;
						}
					}
					res[i][j] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
				}
			}
		}
		return res;
	}

	public long[][] sparseSqrt(long[][] f, int nx, int ny) {
		if (nx <= 0 || ny <= 0) return zero();
		int d0x = -1, d0y = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (f[i][j] != 0) { d0x = i; d0y = j; break outer; }
			}
		}
		if (d0x == -1) return new long[nx][ny];
		if (d0x % 2 != 0 || d0y % 2 != 0) return null;
		long sqrt0 = MathUtils.modKthRoot(poly1d.fp.reduce(f[d0x][d0y]), 2, mod);
		if (sqrt0 == -1) return null;
		if (sqrt0 * 2 > mod) sqrt0 = mod - sqrt0;
		int bx = d0x / 2, by = d0y / 2;
		if (bx >= nx || by >= ny) return new long[nx][ny];
		ArrayList<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (i == d0x && j == d0y) continue;
				if (f[i][j] != 0) terms.add(new Term2D(i - d0x, j - d0y, poly1d.fp.reduce(f[i][j])));
			}
		}
		long[][] res = new long[nx][ny];
		res[bx][by] = sqrt0;
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0x][d0y]), mod);
		long kMod = MathUtils.modInv(2, mod);
		for (int i = bx; i < nx; i++) {
			for (int j = by; j < ny; j++) {
				if (i == bx && j == by) continue;
				long tmp = 0;
				if (i > bx) {
					int n = i - bx;
					for (Term2D t : terms) {
						int pi = i - t.dx, pj = j - t.dy;
						if (pi >= bx && pj >= by && pi < nx && pj < ny) {
							long val = ((kMod + 1) * t.dx % mod - n + mod) % mod;
							tmp = (tmp + val * t.v % mod * res[pi][pj]) % mod;
						}
					}
					res[i][j] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
				} else {
					int m = j - by;
					for (Term2D t : terms) {
						if (t.dx != 0) continue;
						int pj = j - t.dy;
						if (pj >= by && pj < ny) {
							long val = ((kMod + 1) * t.dy % mod - m + mod) % mod;
							tmp = (tmp + val * t.v % mod * res[i][pj]) % mod;
						}
					}
					res[i][j] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
				}
			}
		}
		return res;
	}

	public long[][] sparseInv(long[][] a) {
		if (a.length == 0 || a[0].length == 0 || a[0][0] == 0) throw new AssertionError();
		ArrayList<Integer> degXs = new ArrayList<>();
		ArrayList<Integer> degYs = new ArrayList<>();
		ArrayList<Long> coefs = new ArrayList<>();
		int m0 = 0;
		for (int i = 0; i < a.length; i++) {
			m0 = Math.max(m0, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				if (i == 0 && j == 0) continue;
				if (a[i][j] != 0) { degXs.add(i); degYs.add(j); coefs.add(a[i][j]); }
			}
		}
		long[][] b = new long[a.length][m0];
		long constInv = fp.inv(mod - a[0][0]);
		b[0][0] = fp.inv(a[0][0]);
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < b[i].length; j++) {
				if (i == 0 && j == 0) continue;
				for (int k = 0; k < coefs.size(); ++k) {
					int dx = degXs.get(k), dy = degYs.get(k);
					if (i - dx >= 0 && j - dy >= 0 && i - dx < b.length && j - dy < b[i - dx].length)
						b[i][j] = (b[i][j] + coefs.get(k) * b[i - dx][j - dy]) % mod;
				}
				b[i][j] = constInv * b[i][j] % mod;
			}
		return b;
	}

	public long[][] inv(long[][] a) {
		if (a.length == 0 || a[0].length == 0 || a[0][0] == 0) throw new AssertionError();
		int m0 = 0;
		for (long[] row : a) m0 = Math.max(m0, row.length);
		long[][] b = new long[a.length][m0];
		long inva00 = fp.inv(a[0][0]);
		b[0][0] = inva00;
		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++) {
				if (i == 0 && j == 0) continue;
				for (int i2 = 0; i2 <= i; i2++) {
					if (i2 >= a.length) continue;
					for (int j2 = 0; j2 <= j; j2++) {
						if (i2 == 0 && j2 == 0) continue;
						if (j2 >= a[i2].length) continue;
						b[i][j] = (b[i][j] - a[i2][j2] * b[i - i2][j - j2] % mod + mod) % mod;
					}
				}
				b[i][j] = b[i][j] * inva00 % mod;
			}
		return b;
	}

	public long[][] mulNaive(long[][] a, long[][] b) {
		int m0 = 0, m1 = 0;
		for (long[] row : a) m0 = Math.max(m0, row.length);
		for (long[] row : b) m1 = Math.max(m1, row.length);
		if (a.length == 0 || b.length == 0 || m0 == 0 || m1 == 0) return zero();
		ArrayList<Integer> ai = new ArrayList<>(), aj = new ArrayList<>();
		ArrayList<Long> av = new ArrayList<>();
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				if (a[i][j] != 0) { ai.add(i); aj.add(j); av.add(a[i][j]); }
		ArrayList<Integer> bi = new ArrayList<>(), bj = new ArrayList<>();
		ArrayList<Long> bv = new ArrayList<>();
		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++)
				if (b[i][j] != 0) { bi.add(i); bj.add(j); bv.add(b[i][j]); }
		long[][] c = new long[a.length + b.length - 1][m0 + m1 - 1];
		for (int k = 0; k < av.size(); k++) {
			long v = av.get(k);
			int r = ai.get(k), s = aj.get(k);
			for (int l = 0; l < bv.size(); l++) {
				c[r + bi.get(l)][s + bj.get(l)] = (c[r + bi.get(l)][s + bj.get(l)] + v * bv.get(l)) % mod;
			}
		}
		return c;
	}

	@Override
	public long[][] zero() { return new long[0][0]; }

	@Override
	public long[][] one() { return new long[][] {{1}}; }

	/**
	 * 未テスト
	 * @return
	 */
	public long[][] x() {
		return new long[][] { { 0 }, { 1 } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][] y() {
		return new long[][] { { 0, 1 } };
	}

	@Override
	public long[][] add(long[][] a, long[][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		for (long[] row : a) m = Math.max(m, row.length);
		for (long[] row : b) m = Math.max(m, row.length);
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				long va = (i < a.length && j < a[i].length) ? a[i][j] : 0;
				long vb = (i < b.length && j < b[i].length) ? b[i][j] : 0;
				c[i][j] = poly1d.addMod(va, vb);
			}
		}
		return c;
	}

	@Override
	public long[][] sub(long[][] a, long[][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		for (long[] row : a) m = Math.max(m, row.length);
		for (long[] row : b) m = Math.max(m, row.length);
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				long va = (i < a.length && j < a[i].length) ? a[i][j] : 0;
				long vb = (i < b.length && j < b[i].length) ? b[i][j] : 0;
				c[i][j] = poly1d.subMod(va, vb);
			}
		}
		return c;
	}

	@Override
	public long[][] neg(long[][] a) {
		long[][] c = new long[a.length][];
		for (int i = 0; i < a.length; i++) {
			c[i] = new long[a[i].length];
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] != 0) c[i][j] = mod - a[i][j];
			}
		}
		return c;
	}

	@Override
	public boolean equals(long[][] a, long[][] b) {
		int nx = Math.max(a.length, b.length);
		for (int i = 0; i < nx; i++) {
			long[] ra = i < a.length ? a[i] : new long[0];
			long[] rb = i < b.length ? b[i] : new long[0];
			int ny = Math.max(ra.length, rb.length);
			for (int j = 0; j < ny; j++) {
				long va = j < ra.length ? ra[j] : 0;
				long vb = j < rb.length ? rb[j] : 0;
				if (va != vb) return false;
			}
		}
		return true;
	}

	@Override
	public long[][] exactDiv(long[][] a, long[][] b) {
		return lexdiv(a, b);
	}

	/**
	 * 辞書式順序 (x > y) に基づく多項式の除算を行い、商と余りを返す。
	 * Kronecker 置換を用いて 1 変数多項式の除算に帰着させることで、一意な商と余りを得る。
	 */
	public DivModResult lexdivmod(long[][] a, long[][] b) {
		a = resize(a); b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		int dxA = degX(a);
		if (dxA < dxB) return new DivModResult(zero(), a);
		int dyA = degY(a), dyB = degY(b);
		int stride = Math.max(dyA, dyB) + (dxA - dxB + 1) * dyB + 1;
		long[] fa = flattenKronecker(a, stride);
		long[] fb = flattenKronecker(b, stride);
		PolynomialFpDynamic.DivModResult res = poly1d.divmod(fa, fb);
		long[][] q = unflattenKronecker(res.q, stride, Math.max(0, dxA - dxB), stride - 1);
		long[][] r = unflattenKronecker(res.r, stride, dxB, stride - 1);
		return new DivModResult(q, r);
	}

	/** 辞書式順序に基づく商を返す。 */
	public long[][] lexdiv(long[][] a, long[][] b) {
		a = resize(a); b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		if (dxB == 0 && poly1d.deg(b[0]) == 0) {
			long inv = fp.inv(b[0][0]);
			long[][] res = new long[a.length][];
			for (int i = 0; i < a.length; i++) res[i] = poly1d.mul(a[i], inv);
			return resize(res);
		}
		return lexdivmod(a, b).q;
	}

	/** 辞書式順序に基づく余りを返す。 */
	public long[][] lexmod(long[][] a, long[][] b) {
		return lexdivmod(a, b).r;
	}

	public long[][] mul(long[][] a, long b) {
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) {
			res[i] = poly1d.mul(a[i], b);
		}
		return res;
	}

	@Override
	public long[][] mul(long[][] a, long[][] b) {
		int m0 = 0, m1 = 0;
		for (long[] row : a) m0 = Math.max(m0, row.length);
		for (long[] row : b) m1 = Math.max(m1, row.length);
		if (a.length == 0 || b.length == 0 || m0 == 0 || m1 == 0) return zero();
		if (!poly1d.isNTTFriendly || Math.min(1L * a.length * m0, 1L * b.length * m1) < 16)
			return mulNaive(a, b);
		int stride = m0 + m1 - 1;
		long[] f = new long[stride * a.length];
		long[] g = new long[stride * b.length];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++) f[i * stride + j] = a[i][j];
		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++) g[i * stride + j] = b[i][j];
		long[] h = poly1d.mul(f, g);
		long[][] c = new long[a.length + b.length - 1][stride];
		for (int i = 0; i < h.length; i++) if (h[i] != 0) c[i / stride][i % stride] = h[i];
		return c;
	}

	/**
	 * [x^n] f(x,y)/g(x,y)
	 * https://noshi91.hatenablog.com/entry/2024/03/16/224034
	 */
	public long[] fixingXofRational(long[][] f, long[][] g, int n) {
		int m0 = 0;
		for (long[] row : g) m0 = Math.max(m0, row.length);
		if (g.length == 0 || m0 == 0 || g[0].length == 0 || g[0][0] != 1) throw new AssertionError();
		while (n != 0) {
			long[][] negatedG = ArrayUtils.copy(g);
			for (int i = 0; i < negatedG.length; i++)
				for (int j = 0; j < negatedG[i].length; j++)
					if (i % 2 == 1) negatedG[i][j] = negatedG[i][j] * (mod - 1) % mod;
			f = mul(f, negatedG);
			g = mul(g, negatedG);
			int mf = 0, mg = 0;
			for (long[] row : f) mf = Math.max(mf, row.length);
			for (long[] row : g) mg = Math.max(mg, row.length);
			long[][] nf = new long[Math.min(n / 2 + 1, (f.length + 1) / 2)][(mf + 1) / 2];
			long[][] ng = new long[Math.min(n / 2 + 1, (g.length + 1) / 2)][(mg + 1) / 2];
			for (int i = (int) (n % 2); i < f.length && i / 2 < nf.length; i += 2) nf[i / 2] = f[i];
			for (int i = 0; i < g.length && i / 2 < ng.length; i += 2) ng[i / 2] = g[i];
			f = nf; g = ng; n /= 2;
		}
		return f.length > 0 ? f[0] : new long[0];
	}

	public int degX(long[][] a) {
		for (int i = a.length - 1; i >= 0; i--) {
			if (poly1d.deg(a[i]) != -1) return i;
		}
		return -1;
	}

	/**
	 * 多項式 f が 0 かを判定する。
	 * @param f 多項式
	 * @return f が 0 なら true
	 *
	 * <p>計算量: O(degX f * degY f)
	 */
	public boolean isZero(long[][] f) {
		return degX(f) == -1;
	}

	public long[][] resize(long[][] a) {
		int dx = degX(a);
		if (dx == -1) return new long[0][0];
		long[][] res = new long[dx + 1][];
		for (int i = 0; i <= dx; i++) {
			res[i] = a[i] == null ? poly1d.zero() : poly1d.resize(a[i]);
		}
		return res;
	}

	/**
	 * 多項式 f を点 (x, y) で評価する。
	 * @param f 多項式
	 * @param x 評価点 x
	 * @param y 評価点 y
	 * @return f(x, y)
	 */
	public long eval(long[][] f, long x, long y) {
		long res = 0;
		for (int i = f.length - 1; i >= 0; i--) {
			long val = f[i] == null ? 0 : poly1d.eval(f[i], y);
			res = (res * x + val) % mod;
		}
		return res;
	}

	/**
	 * primitiveな多項式の既約性を判定する。
	 * primitiveでない多項式を与えた場合の動作は保証されない。
	 * ランダムな1変数への射影を用いて判定する。
	 * @param f 判定対象の多項式
	 * @return trueならば既約。falseならば不定
	 */
	public boolean isIrreducibleHeuristicForPrimitive(long[][] f) {
		if (degX(f) <= 0 && degY(f) <= 0) return false;
		f = monic(f);
		int dx = degX(f);
		Random rnd = new Random(0);
		for (int t = 0; t < 5; t++) {
			long c = poly1d.fp.reduce(rnd.nextLong());
			long[] f_xc = new long[dx + 1];
			for (int i = 0; i <= dx; i++) {
				f_xc[i] = f[i] == null ? 0 : poly1d.eval(f[i], c);
			}
			if (poly1d.deg(f_xc) != dx) continue;
			if (poly1d.isIrreducible(f_xc)) return true;
		}
		return false;
	}

	/**
	 * 多項式の辞書順最大の主係数を返す。
	 * @param a 多項式
	 * @return 主係数
	 */
	public long lead(long[][] a) {
		int dx = degX(a);
		if (dx == -1) return 0;
		if (a[dx] == null) return 0;
		int dy = poly1d.deg(a[dx]);
		return a[dx][dy];
	}

	/**
	 * 多項式の辞書順最大の主項を返す。
	 * @param a 多項式
	 * @return 主項
	 */
	public Term2D leadTerm(long[][] a) {
		int dx = degX(a);
		if (dx == -1) return new Term2D(-1, -1, 0);
		int dy = poly1d.deg(a[dx]);
		return new Term2D(dx, dy, a[dx][dy]);
	}

	/**
	 * 多項式 a から c * x^dx * y^dy * b を引く。
	 * @param a 多項式 a
	 * @param b 多項式 b
	 * @param c 係数 c
	 * @param dx x の指数
	 * @param dy y の指数
	 * @return a - c * x^dx * y^dy * b
	 */
	public long[][] subMulTerm(long[][] a, long[][] b, long c, int dx, int dy) {
		if (c == 0) return a;
		int nx = Math.max(a.length, b.length + dx);
		int ny = 0;
		for (long[] row : a) ny = Math.max(ny, row.length);
		for (long[] row : b) ny = Math.max(ny, row.length + dy);

		long[][] res = new long[nx][ny];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = a[i][j];
			}
		}
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				if (b[i][j] == 0) continue;
				long val = b[i][j] * c % mod;
				res[i + dx][j + dy] = (res[i + dx][j + dy] - val + mod) % mod;
			}
		}
		return resize(res);
	}

	/**
	 * 多項式 f を多項式集合 G で簡約した正規形（剰余）を返す。
	 * ここで簡約とは、f のある項が G のある要素 g の主項 LT(g) で割り切れるとき、
	 * その項を消去するように f から g の定数倍を引く操作を指す。
	 * この操作を繰り返して得られる、どの項も G のどの要素の主項でも割り切れない多項式が正規形である。
	 * @param f 多項式 f
	 * @param G 簡約に用いる多項式の集合
	 * @return f の正規形
	 */
	public long[][] normalForm(long[][] f, List<long[][]> G) {
		long[][] p = resize(f);
		long[][] r = zero();
		while (degX(p) != -1) {
			Term2D ltP = leadTerm(p);
			boolean divisionOccurred = false;
			for (long[][] g : G) {
				Term2D ltG = leadTerm(g);
				if (ltG.dx != -1 && ltP.dx >= ltG.dx && ltP.dy >= ltG.dy) {
					long c = ltP.v * MathUtils.modInv(ltG.v, mod) % mod;
					p = subMulTerm(p, g, c, ltP.dx - ltG.dx, ltP.dy - ltG.dy);
					divisionOccurred = true;
					break;
				}
			}
			if (!divisionOccurred) {
				r = addTerm(r, ltP.v, ltP.dx, ltP.dy);
				p = removeLeadTerm(p);
			}
		}
		return r;
	}

	/**
	 * 2つの多項式 f, g の S-多項式を返す。
	 * S-多項式 S(f, g) は、f と g の主項 LT(f), LT(g) の最小公倍単項式を L = lcm(LM(f), LM(g)) とするとき、
	 * S(f, g) = (L/LT(f)) * f - (L/LT(g)) * g で定義される。
	 * @param f 多項式 f
	 * @param g 多項式 g
	 * @return S(f, g)
	 */
	public long[][] sPolynomial(long[][] f, long[][] g) {
		Term2D ltF = leadTerm(f);
		Term2D ltG = leadTerm(g);
		if (ltF.dx == -1 || ltG.dx == -1) return zero();
		int dx = Math.max(ltF.dx, ltG.dx);
		int dy = Math.max(ltF.dy, ltG.dy);

		long invF = MathUtils.modInv(ltF.v, mod);
		long invG = MathUtils.modInv(ltG.v, mod);

		long[][] termF = mulTerm(f, invF, dx - ltF.dx, dy - ltF.dy);
		long[][] termG = mulTerm(g, invG, dx - ltG.dx, dy - ltG.dy);
		return sub(termF, termG);
	}

	/**
	 * 多項式集合 F が生成するイデアル I のグレブナー基底を返す。
	 * グレブナー基底 G とは、I の任意の要素 f の主項 LT(f) が G のいずれかの要素 g の主項 LT(g) で割り切れるような I の生成系のことである。
	 * @param F 多項式集合 F
	 * @return グレブナー基底
	 */
	public List<long[][]> groebnerBasis(List<long[][]> F) {
		ArrayList<long[][]> G = new ArrayList<>();
		for (long[][] f : F) {
			long[][] res = normalForm(f, G);
			if (degX(res) != -1) G.add(monic(res));
		}

		ArrayList<int[]> pairs = new ArrayList<>();
		for (int i = 0; i < G.size(); i++) {
			for (int j = i + 1; j < G.size(); j++) {
				pairs.add(new int[] {i, j});
			}
		}

		while (!pairs.isEmpty()) {
			int[] pair = pairs.remove(pairs.size() - 1);
			long[][] s = sPolynomial(G.get(pair[0]), G.get(pair[1]));
			long[][] h = normalForm(s, G);
			if (degX(h) != -1) {
				h = monic(h);
				int nextIdx = G.size();
				for (int i = 0; i < G.size(); i++) {
					pairs.add(new int[] {i, nextIdx});
				}
				G.add(h);
			}
		}
		return G;
	}

	/**
	 * 多項式集合 F が生成するイデアルの簡約グレブナー基底を返す。
	 * @param F 多項式集合 F
	 * @return 簡約グレブナー基底
	 */
	public List<long[][]> reducedGroebnerBasis(List<long[][]> F) {
		List<long[][]> G = groebnerBasis(F);
		ArrayList<long[][]> minimalG = new ArrayList<>();
		for (int i = 0; i < G.size(); i++) {
			Term2D ltI = leadTerm(G.get(i));
			boolean redundant = false;
			for (int j = 0; j < G.size(); j++) {
				if (i == j) continue;
				Term2D ltJ = leadTerm(G.get(j));
				if (ltI.dx >= ltJ.dx && ltI.dy >= ltJ.dy) {
					redundant = true;
					break;
				}
			}
			if (!redundant) minimalG.add(monic(G.get(i)));
		}

		ArrayList<long[][]> reducedG = new ArrayList<>();
		for (int i = 0; i < minimalG.size(); i++) {
			long[][] f = minimalG.get(i);
			ArrayList<long[][]> others = new ArrayList<>();
			for (int j = 0; j < minimalG.size(); j++) {
				if (i != j) others.add(minimalG.get(j));
			}
			reducedG.add(normalForm(f, others));
		}
		reducedG.sort((a, b) -> {
			Term2D ltA = leadTerm(a);
			Term2D ltB = leadTerm(b);
			if (ltA.dx != ltB.dx) return Integer.compare(ltB.dx, ltA.dx);
			return Integer.compare(ltB.dy, ltA.dy);
		});
		return reducedG;
	}

	/**
	 * 多項式 a に項 v * x^dx * y^dy を加える。
	 * @param a 多項式 a
	 * @param v 係数 v
	 * @param dx x の指数
	 * @param dy y の指数
	 * @return a + v * x^dx * y^dy
	 */
	public long[][] addTerm(long[][] a, long v, int dx, int dy) {
		if (v == 0) return a;
		int nx = Math.max(a.length, dx + 1);
		int ny = Math.max(0, dy + 1);
		for (long[] row : a) ny = Math.max(ny, row.length);

		long[][] res = new long[nx][ny];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = a[i][j];
			}
		}
		res[dx][dy] = (res[dx][dy] + v) % mod;
		return resize(res);
	}

	/**
	 * 多項式 a から主項を取り除く。
	 * @param a 多項式 a
	 * @return 主項を取り除いた多項式
	 */
	public long[][] removeLeadTerm(long[][] a) {
		Term2D lt = leadTerm(a);
		if (lt.dx == -1) return zero();
		return subMulTerm(a, one(), lt.v, lt.dx, lt.dy);
	}

	/**
	 * 多項式 a に単項 c * x^dx * y^dy を掛ける。
	 * @param a 多項式 a
	 * @param c 係数 c
	 * @param dx x の指数
	 * @param dy y の指数
	 * @return c * x^dx * y^dy * a
	 */
	public long[][] mulTerm(long[][] a, long c, int dx, int dy) {
		if (c == 0) return zero();
		int nx = a.length + dx;
		int ny = 0;
		for (long[] row : a) ny = Math.max(ny, row.length + dy);
		long[][] res = new long[nx][ny];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				res[i + dx][j + dy] = a[i][j] * c % mod;
			}
		}
		return resize(res);
	}

	/**
	 * 多項式を monic（主係数が 1）にする。
	 * @param a 多項式
	 * @return monic 化された多項式
	 */
	public long[][] monic(long[][] a) {
		a = resize(a);
		long leadVal = lead(a);
		if (leadVal == 0) return a;
		long inv = fp.inv(leadVal);
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) {
				res[i] = poly1d.zero();
				continue;
			}
			res[i] = new long[a[i].length];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = a[i][j] * inv % mod;
			}
		}
		return res;
	}
	
	/**
	 *  多変数多項式を x に関する多項式とみたときの「係数部分のgcd」を計算する。
	 * @param a
	 * @return
	 */
	public long[] contentX(long[][] a) {
		long[] g = poly1d.zero();
		for (long[] row : a)
			if (row != null) g = poly1d.gcd(g, row);
		return g;
	}
	/**
	 * x に関する多項式を {@code p}(y) で割る。
	 * @param a
	 * @param p
	 * @return
	 */
	public long[][] lexdivByPolyY(long[][] a, long[] p) {
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++)
			res[i] = a[i] == null ? poly1d.zero() : poly1d.div(a[i], p);
		return res;
	}

	public long[][] mulByPolyY(long[][] a, long[] p) {
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++)
			res[i] = a[i] == null ? poly1d.zero() : poly1d.mul(a[i], p);
		return res;
	}

	public static class DivModResult {
		public long[][] q;
		public long[][] r;
		public DivModResult(long[][] q, long[][] r) {
			this.q = q;
			this.r = r;
		}
	}

	/**
	 * 多項式 f を多項式集合 G で簡約した結果（正規形と余因子）を保持するレコード。
	 * f = \sum cofactors[i] * G[i] + remainder の関係を満たす。
	 * @param remainder 剰余（正規形）
	 * @param cofactors 各生成多項式にかかる係数多項式の配列
	 */
	public record NormalFormResult(long[][] remainder, long[][][] cofactors) {}

	/**
	 * 多項式を $q$ ごとの成分で保持する形式。
	 * $T^{n+1} = \oplus T^{n+1}_q$ の元を表す。
	 */
	public static class LairezForm {
		public final java.util.TreeMap<Integer, long[][]> map = new java.util.TreeMap<>();

		public LairezForm() {}
		public LairezForm(int q, long[][] a) { map.put(q, a); }

		public void add(int q, long[][] a, PolynomialFpDynamic2D poly) {
			if (poly.isZero(a)) return;
			map.put(q, poly.add(map.getOrDefault(q, poly.zero()), a));
			if (poly.isZero(map.get(q))) map.remove(q);
		}

		public LairezForm copy() {
			LairezForm res = new LairezForm();
			for (var e : map.entrySet()) res.map.put(e.getKey(), e.getValue());
			return res;
		}

		public int maxQ() { return map.isEmpty() ? -1 : map.lastKey(); }
	}

	/**
	 * 多項式 f を多項式集合 G で簡約した正規形と、その過程で得られた余因子を返す。
	 * 複変数の除算アルゴリズムに基づき、f = \sum cofactors[i] * G[i] + remainder を満たす一組を計算する。
	 *
	 * <p>事前条件: G の各多項式の主係数が 0 でないこと。
	 * <p>事後条件: f = \sum G[i] * result.cofactors[i] + result.remainder。result.remainder のどの項も G の主項で割り切れない。
	 * <p>副作用: なし。
	 * <p>計算量: O(degX f * degY f * |G| * M(deg))
	 * <p>破壊的変更: なし。
	 * <p>参照共有・所有権: 返値は新規配列。
	 * <p>例外・未定義条件: mod が素数でない場合、逆元が存在せず異常終了する可能性がある。
	 * @param f 多項式 f
	 * @param G 簡約に用いる多項式のリスト
	 * @return 正規形と余因子のレコード
	 */
	public NormalFormResult normalFormWithCofactors(long[][] f, List<long[][]> G) {
		long[][] p = resize(f);
		long[][] r = zero();
		int m = G.size();
		long[][][] cofactors = new long[m][][];
		for (int i = 0; i < m; i++) cofactors[i] = zero();

		while (degX(p) != -1) {
			Term2D ltP = leadTerm(p);
			boolean divisionOccurred = false;
			for (int i = 0; i < m; i++) {
				long[][] g = G.get(i);
				Term2D ltG = leadTerm(g);
				if (ltG.dx != -1 && ltP.dx >= ltG.dx && ltP.dy >= ltG.dy) {
					long inv = MathUtils.modInv(ltG.v, mod);
					long c = ltP.v * inv % mod;
					int dx = ltP.dx - ltG.dx;
					int dy = ltP.dy - ltG.dy;
					p = subMulTerm(p, g, c, dx, dy);
					cofactors[i] = addTerm(cofactors[i], c, dx, dy);
					divisionOccurred = true;
					break;
				}
			}
			if (!divisionOccurred) {
				r = addTerm(r, ltP.v, ltP.dx, ltP.dy);
				p = removeLeadTerm(p);
			}
		}
		return new NormalFormResult(r, cofactors);
	}

	/**
	 * 多項式の因子とその重複度を保持するクラス。
	 */
	public static class Factor {
		/** 既約因子 */
		public long[][] factor;
		/** 重複度 */
		public int multiplicity;
		public Factor(long[][] factor, int multiplicity) {
			this.factor = factor;
			this.multiplicity = multiplicity;
		}
	}


	/**
	 * Wang の主係数トリックの前進変換結果を保持する。
	 * @param leadingCoeff {@code L(y)=[x^d]F(x,y)}
	 * @param factorCount 評価点で得た1変数因子数 {@code k}
	 * @param mainDegree {@code d=deg_x F}
	 * @param monicPolynomial {@code F^*(X,y)=L(y)^{d-1}F(X/L(y),y)\in F_p[y][X]}
	 */
	public record WangTransform2D(long[] leadingCoeff, int factorCount, int mainDegree, long[][] monicPolynomial) {}

	/**
	 * 多項式の因数分解結果を保持するレコード。
	 * @param leadingCoeff 主係数
	 * @param factors 既約因子の配列（すべて monic）
	 */
	public record FactorResult2D(long leadingCoeff, Factor[] factors) {}

	public static class SquareFreeFactor {
		public long[][] factor;
		public int multiplicity;
		public SquareFreeFactor(long[][] factor, int multiplicity) {
			this.factor = factor;
			this.multiplicity = multiplicity;
		}
	}
	
	public SquareFreeFactor[] squareFreeDecomposition(long[][] inputf) {
		long[][] f = resize(inputf);
		if (degX(f) == -1) return new SquareFreeFactor[0];
		ArrayList<SquareFreeFactor> result = new ArrayList<>();
		long[] content = contentX(f);
		long[][] prim = lexdivByPolyY(f, content);
		result.addAll(squareFreePrimitiveByYun(prim));
		PolynomialFpDynamic.SquareFreeFactor[] sqf1d = poly1d.factorSquareFree(content);
		for (PolynomialFpDynamic.SquareFreeFactor cf : sqf1d) {
			long[][] lifted = new long[][] { cf.factor };
			boolean merged = false;
			for (SquareFreeFactor ex : result) {
				if (ex.multiplicity == cf.multiplicity) {
					ex.factor = resize(mul(ex.factor, lifted));
					merged = true;
					break;
				}
			}
			if (!merged) result.add(new SquareFreeFactor(resize(lifted), cf.multiplicity));
		}
		result.removeIf(e -> degX(e.factor) == -1);
		result.sort((a, b) -> Integer.compare(a.multiplicity, b.multiplicity));
		return result.toArray(new SquareFreeFactor[0]);
	}

	/**
	 * {@code primitive}は主変数xに関して、コンテンツが取り除かれている(primitive)とする。
	 * @param primitive
	 * @return
	 */
	private ArrayList<SquareFreeFactor> squareFreePrimitiveByYun(long[][] primitive) {
		ArrayList<SquareFreeFactor> out = new ArrayList<>();
		long[][] f = monic(primitive);
		if (degX(f) <= 0) return out;
		long[][] fp = diffX(f);
		long[][] g = gcd(f, fp);
		long[][] p = lexdiv(f, g);
		long[][] q = lexdiv(fp, g);
		for (int i = 1; ; i++) {
			long[][] h = resize(sub(q, diffX(p)));
			if (degX(h) == -1) {
				if (degX(p) > 0) out.add(new SquareFreeFactor(monic(p), i));
				break;
			}
			long[][] gi = gcd(p, h);
			if (degX(gi) > 0) out.add(new SquareFreeFactor(monic(gi), i));
			p = lexdiv(p, gi);
			q = lexdiv(h, gi);
		}
		return out;
	}

	public long[][] diffX(long[][] a) {
		a = resize(a);
		if (a.length <= 1) return zero();
		long[][] res = new long[a.length - 1][];
		for (int i = 1; i < a.length; i++) res[i - 1] = poly1d.mul(a[i], i % mod);
		return resize(res);
	}

	/**
	 * Wang の主係数トリックにより {@code F(x,y)} を {@code X} に関して monic にする。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code F != 0}, {@code k >= 0}, {@code d=deg_x F >= 0}, {@code L(y)=[x^d]F(x,y) != 0}。</li>
	 * <li>事後条件: 返値 {@code T} は {@code T.leadingCoeff=L}, {@code T.factorCount=k}, {@code T.mainDegree=d} を満たす。</li>
	 * <li>事後条件: {@code T.monicPolynomial = F^*(X,y)} は<br>
	 * {@code F^*(X,y)=sum_{0<=i<d} [x^i]F(x,y) L(y)^{d-1-i} X^i + X^d}。</li>
	 * <li>事後条件: {@code [X^d]F^*(X,y)=1}。</li>
	 * <li>副作用: なし。返値は新規配列を所有し、入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code F=0} または {@code k<0} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: {@code F} が {@code F_p[y][x]} の正規化済み係数配列でない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(d * Pow1D(deg_y L, d) + d * Mul1D)。
	 */
	public WangTransform2D wangTrickForward(long[][] F, int k) {
		if (k < 0) throw new IllegalArgumentException("factor count must be non-negative");
		F = resize(F);
		int d = degX(F);
		if (d == -1) throw new IllegalArgumentException("zero polynomial has no leading coefficient");
		long[] L = poly1d.resize(F[d]);
		long[][] monicF = new long[d + 1][];
		for (int i = 0; i < d; i++) {
			long[] coeff = i < F.length ? poly1d.resize(F[i]) : poly1d.zero();
			int exponent = d - 1 - i;
			long[] scale = exponent == 0 ? poly1d.one() : poly1d.powFull(L, exponent);
			monicF[i] = poly1d.mul(coeff, scale);
		}
		monicF[d] = poly1d.one();
		return new WangTransform2D(Arrays.copyOf(L, L.length), k, d, resize(monicF));
	}

	/**
	 * Wang の主係数トリックで monic 化された因子を逆変換し、{@code x} に関する原始部分を返す。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code L(y) != 0}, {@code monicFactors != null}。</li>
	 * <li>事前条件: 各 {@code G_i(X,y)} は {@code F_p[y][X]} の多項式である。</li>
	 * <li>事後条件: 返値の各因子 {@code H_i(x,y)} は<br>
	 * {@code H_i = pp_x(G_i(L(y)x,y))} を満たす。ただし {@code pp_x(P)=P/content_x(P)}。</li>
	 * <li>事後条件: {@code content_x(H_i)=1}。ただし零因子は零のまま返す。</li>
	 * <li>副作用: なし。返値と各 {@link Factor#factor} は新規配列を所有し、入力因子配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code L=0} または {@code monicFactors=null} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: {@code G_i(Lx,y)} が {@code content_x} で割り切れない内部表現を持つ場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(s * (d_i * Pow1D(deg_y L, d_i) + d_i * Mul1D + Gcd1D))。
	 */
	public Factor[] wangTrickBackward(long[] L, Factor[] monicFactors) {
		L = poly1d.resize(L);
		if (poly1d.deg(L) == -1) throw new IllegalArgumentException("leading coefficient must be non-zero");
		if (monicFactors == null) throw new IllegalArgumentException("monicFactors must not be null");
		Factor[] res = new Factor[monicFactors.length];
		for (int idx = 0; idx < monicFactors.length; idx++) {
			Factor factor = monicFactors[idx];
			long[][] substituted = substituteWangX(factor.factor, L);
			long[][] primitive = primitivePartX(substituted);
			res[idx] = new Factor(monic(primitive), factor.multiplicity);
		}
		return res;
	}

	/**
	 * {@code G(X,y)} に {@code X=L(y)x} を代入する。未テスト。
	 * 計算量: O(d * Pow1D(deg_y L, d) + d * Mul1D)
	 */
	private long[][] substituteWangX(long[][] G, long[] L) {
		G = resize(G);
		int d = degX(G);
		if (d == -1) return zero();
		long[][] res = new long[d + 1][];
		long[] pow = poly1d.one();
		for (int i = 0; i <= d; i++) {
			res[i] = poly1d.mul(G[i], pow);
			pow = poly1d.mul(pow, L);
		}
		return resize(res);
	}

	/**
	 * {@code pp_x(a)=a/content_x(a)} を返す。未テスト。
	 * 計算量: O(Gcd1D * deg_x a + Div1D * deg_x a)
	 */
	private long[][] primitivePartX(long[][] a) {
		a = resize(a);
		if (degX(a) == -1) return zero();
		long[] content = contentX(a);
		if (poly1d.deg(content) <= 0) return resize(a);
		return resize(lexdivByPolyY(a, content));
	}

	/**
	 * {@code f(x, valY)} の互いに素な1変数因子分解から、{@code f(x, y)} の因子をHensel持ち上げで復元する。未テスト。
	 * {@code f} は primitive かつ square-free かつ monic かつ相異なる素因子が {@code y=valY} で同じ素因子につぶれないことを仮定する。
	 * 計算量: O((degY(f) + 1) * (factors.length * (ExtGCD1D + Mul1D) + factors.length * Mul2D))
	 */
	public Factor[] liftFactors(long[][] f, PolynomialFpDynamic.Factor[] factors, long valY) {
		f = resize(f);
		int ny = Math.max(1, degY(f) + 1);

		// f(x, y) = sum a_i(x) (y-valY)^i = sum a_i(x) t^i と置き換え、t-adic に ny 次未満だけ扱う。
		long[][] shiftedF = truncateY(shiftY(f, valY), ny);

		int r = factors.length;
		if (r == 0) return new Factor[0];

		// 初期値は f(x, valY) の因数分解 mod t。
		long[][][] lifted = new long[r][][];
		for (int i = 0; i < r; i++) {
			lifted[i] = embedPolyXAtDegY(factors[i].factor, 0);
		}

		for (int k = 1; k < ny; k++) {
			// 既に mod t^k まで一致
			long[][] prod = one();
			for (int i = 0; i < r; i++) prod = truncateY(mul(prod, lifted[i]), k + 1);
			long[] err = poly1d.resize(poly1d.sub(coeffY(shiftedF, k), coeffY(prod, k)));
			// shiftedF　-　Πlifted = err * t^k
			// Π(lifted　+　corr_i t^k)と更新するとして、
			// err = sum_i corr_i Π_{j ≠ i} factor_i
			// err / Πfactor_i = sum_i corr_i / factors_i
			long[][] corr1D = henselCorrections1D(err, factors);
			long[][][] corr2D = new long[r][][];
			for (int i = 0; i < r; i++) corr2D[i] = embedPolyXAtDegY(corr1D[i], k);
			//lifted[i]={f(x,t) mod t^{k+1} のi番目の素因数}
			for (int i = 0; i < r; i++) lifted[i] = truncateY(add(lifted[i], corr2D[i]), ny);
		}

		Factor[] res = new Factor[r];
		long back = poly1d.fp.reduce(-valY);
		for (int i = 0; i < r; i++) res[i] = new Factor(resize(shiftY(lifted[i], back)), factors[i].multiplicity);
		return res;
	}
	
	/**
	 * {@code err = sum_i c_i prod_{j != i} factors[j]} を満たす {@code c_i} を返す。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code factors} は相異なる monic な1変数多項式で、任意の {@code i != j} について {@code gcd(f_i,f_j)=1}。</li>
	 * <li>事後条件: {@code Q=prod_i f_i} とすると {@code err = qQ + r} の {@code qQ} を {@code c_0} に吸収し、
	 * {@code err = sum_i c_i Q/f_i}。</li>
	 * <li>事後条件: {@code i>0} なら {@code deg c_i < deg f_i}。</li>
	 * <li>副作用: なし。返値と各行は新規配列を所有し、入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code factors.length=0} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 因子が互いに素でない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(r * (Mul1D + Div1D + ExtGCD1D))。
	 */
	private long[][] henselCorrections1D(long[] err, PolynomialFpDynamic.Factor[] factors) {
		int r = factors.length;
		if (r == 0) throw new IllegalArgumentException("factors must be non-empty");
		long[] Q = poly1d.one();
		for (PolynomialFpDynamic.Factor factor : factors) {
			if (factor.multiplicity != 1) throw new ArithmeticException("lift expects square-free specialized factors");
			Q = poly1d.mul(Q, factor.factor);
		}
		PolynomialFpDynamic.DivModResult dm = poly1d.divmod(err, Q);
		long[] rem = dm.r;
		long[][] corr = new long[r][];
		for (int i = 0; i < r; i++) {
			long[] fi = factors[i].factor;
			long[] Qi = poly1d.div(Q, fi);
			long[] invQi = poly1d.extgcd(poly1d.mod(Qi, fi), fi).x();
			corr[i] = poly1d.mod(poly1d.mul(poly1d.mod(rem, fi), invQi), fi);
		}
		if (poly1d.deg(dm.q) != -1) corr[0] = poly1d.add(corr[0], poly1d.mul(dm.q, factors[0].factor));
		return corr;
	}

	/**
	 * Wang の主係数トリックとHensel持ち上げで primitive かつ square-free な多項式を既約分解する。
	 * @param inputf 因数分解対象 {@code F(x,y)}
	 * @return {@code pp_x} により復元された既約因子列
	 */
	public Factor[] factorByWang(long[][] inputf) {
		long[][] f = primitivePartX(inputf);
		int dx = degX(f);
		if (dx == -1) {
			return new Factor[0];
		}
		if (dx <= 0) {
			PolynomialFpDynamic.FactorResult fr = poly1d.factor(f[0]);
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) {
				res[i] = new Factor(new long[][] { fr.factors()[i].factor }, fr.factors()[i].multiplicity);
			}
			return res;
		}
		if (degY(f) <= 0) {
			PolynomialFpDynamic.FactorResult fr = poly1d.factor(evalY(monic(f), 0));
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) {
				res[i] = new Factor(embedPolyXAtDegY(fr.factors()[i].factor, 0), fr.factors()[i].multiplicity);
			}
			return res;
		}
		WangTransform2D transformed = wangTrickForward(f, 0);
		long[][] monicF = transformed.monicPolynomial();
		long a = findGoodEvaluationPoint(monicF);
		long[] fa = evalY(monicF, a);
		PolynomialFpDynamic.FactorResult fr = poly1d.factor(fa);
		transformed = wangTrickForward(f, fr.factors().length);
		monicF = transformed.monicPolynomial();
		Factor[] lifted = liftFactors(monicF, fr.factors(), a);
		Factor[] recombined = recombine(transformed.monicPolynomial(), lifted);
		return wangTrickBackward(transformed.leadingCoeff(), recombined);
	}

	private Factor[] recombine(long[][] f, Factor[] lifted) {
		if (lifted.length <= 1) return lifted;
		ArrayList<long[][]> currentFactors = new ArrayList<>();
		for (Factor fact : lifted) currentFactors.add(fact.factor);

		ArrayList<long[][]> result = new ArrayList<>();
		long[][] remainingF = f;

		for (int sz = 1; sz <= currentFactors.size() / 2; sz++) {
			boolean[] used = new boolean[currentFactors.size()];
			for (int[] indices : Itertools.combinations(currentFactors.size(), sz)) {
				boolean anyUsed = false;
				for (int idx : indices) if (used[idx]) anyUsed = true;
				if (anyUsed) continue;

				long[][] prod = one();
				for (int idx : indices) prod = mul(prod, currentFactors.get(idx));
				if (degX(prod) <= degX(remainingF) && degY(prod) <= degY(remainingF) && isDivisible(remainingF, prod)) {
					result.add(prod);
					remainingF = lexdiv(remainingF, prod);
					for (int idx : indices) used[idx] = true;
				}
			}
			ArrayList<long[][]> nextFactors = new ArrayList<>();
			for (int i = 0; i < currentFactors.size(); i++) if (!used[i]) nextFactors.add(currentFactors.get(i));
			currentFactors = nextFactors;
		}
		if (degX(remainingF) > 0 || degY(remainingF) > 0) result.add(remainingF);

		Factor[] res = new Factor[result.size()];
		for (int i = 0; i < result.size(); i++) res[i] = new Factor(result.get(i), 1);
		return res;
	}
	
	private long findGoodEvaluationPoint(long[][] f) {
		int dx = degX(f);
		long[] lc = f[dx];
		for (long a = 0; a < Math.min(mod, 1000); a++) {
			if (poly1d.eval(lc, a) == 0) continue;
			long[] fa = evalY(f, a);
			if (poly1d.isSquareFree(fa)) return a;
		}
		throw new ArithmeticException("good evaluation point not found");
	}
	

	/** {@code f(x, y+c)} を返す。未テスト。計算量: O(dx * Shift1D(degY f)) */
	private long[][] shiftY(long[][] f, long c) {
		f = resize(f);
		long[][] res = new long[f.length][];
		for (int i = 0; i < f.length; i++) {
			res[i] = poly1d.taylorShift(f[i], poly1d.fp.reduce(c));
		}
		return resize(res);
	}

	/** {@code a} の y 次数を {@code ny} 未満へ切り詰める。未テスト。計算量: O(項数) */
	private long[][] truncateY(long[][] a, int ny) {
		a = resize(a);
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) res[i] = Arrays.copyOf(a[i], Math.min(ny, a[i].length));
		return resize(res);
	}

	/** {@code [y^k] a(x,y)} を返す。未テスト。 */
	private long[] coeffY(long[][] a, int k) {
		a = resize(a);
		long[] res = new long[a.length];
		for (int i = 0; i < a.length; i++) if (k < a[i].length) res[i] = a[i][k];
		return poly1d.resize(res);
	}

	/** {@code a(x)} を {@code y^k a(x)} として2変数多項式に埋め込む。未テスト。計算量: O(deg a) */
	private long[][] embedPolyXAtDegY(long[] a, int k) {
		a = poly1d.resize(a);
		if (poly1d.deg(a) == -1) return zero();
		long[][] res = new long[a.length][k+1];
		for (int i = 0; i < a.length; i++) res[i][k] = a[i];
		return resize(res);
	}
	
	void tr(Object...out) {System.out.println(Arrays.deepToString(out));}
	
	/**
	 * 多項式を既約分解する。
	 * Kronecker置換を用いて1変数多項式の因数分解に帰着させる。
	 * @param inputf 因数分解対象の多項式
	 * @return 既約因子の配列
	 */
	public FactorResult2D factor(long[][] inputf) {
		inputf = resize(inputf);
		if (degX(inputf) == -1) return new FactorResult2D(0, new Factor[0]);
		long[] content = contentX(inputf);
		long coef = lead(inputf);
		if (degX(inputf) == 0 && poly1d.deg(inputf[0]) == 0) return new FactorResult2D(coef, new Factor[0]);
		ArrayList<Factor> ret = new ArrayList<>();
		PolynomialFpDynamic.FactorResult contentFR = poly1d.factor(content);
		for (PolynomialFpDynamic.Factor f1d : contentFR.factors()) {
			ret.add(new Factor(new long[][] { f1d.factor }, f1d.multiplicity));
		}

		long[][] primitive = lexdivByPolyY(inputf, content);
		if (degX(primitive) > 0 && isIrreducibleHeuristicForPrimitive(primitive)) {
			ret.add(new Factor(monic(primitive), 1));
			return new FactorResult2D(coef, ret.toArray(new Factor[ret.size()]));
		}
		ArrayList<SquareFreeFactor> sqfFactors = squareFreePrimitiveByYun(primitive);
		for (SquareFreeFactor sqf : sqfFactors) {
			Factor[] factors = factorByWang(sqf.factor);
			for (Factor f : factors) {
				ret.add(new Factor(f.factor, f.multiplicity * sqf.multiplicity));
			}
		}
		return new FactorResult2D(coef, ret.toArray(new Factor[ret.size()]));
	}


	/**
	 * Kronecker置換を用いて多項式を既約分解する。
	 * @param inputf 因数分解対象の多項式
	 * @return 既約因子の配列
	 */
	public Factor[] factorByKronecker(long[][] inputf) {
		long[][] f = monic(inputf);
		if (degX(f) <= 0 && degY(f) <= 0) return new Factor[0];
		ArrayList<long[][]> raw = new ArrayList<>();
		factorDfsByKronecker(f, raw);
		ArrayList<Factor> ret = new ArrayList<>();
		for (long[][] g : raw) {
			g = monic(g);
			boolean merged = false;
			for (Factor e : ret) {
				if (equals(e.factor, g)) {
					e.multiplicity++;
					merged = true;
					break;
				}
			}
			if (!merged) ret.add(new Factor(g, 1));
		}
		return ret.toArray(new Factor[ret.size()]);
	}

	/** Kroneckerの再帰分解本体。計算量: O(2^s * Mul2D + Factor1D(D)) */
	private void factorDfsByKronecker(long[][] f, ArrayList<long[][]> out) {
		f = monic(f);
		if (degX(f) <= 0 && degY(f) <= 0) return;
		long[][] d = findFactorByKronecker(f);
		if (d == null) {
			out.add(f);
			return;
		}
		long[][] q = lexdiv(f, d);
		if (!equals(mul(d, q), f) || equals(d, one()) || equals(q, one())) {
			out.add(f);
			return;
		}
		factorDfsByKronecker(d, out);
		factorDfsByKronecker(q, out);
	}

	/** 1変数に落とした因子の積から、元の多項式を割る真の因子を探す。計算量: O(2^s * (Mul1D + Div2D + Mul2D)) */
	private long[][] findFactorByKronecker(long[][] f) {
		int sx = Math.max(1, degY(f) + 1);
		long[] flat = flattenKronecker(f, sx);
		PolynomialFpDynamic.Factor[] fs = poly1d.factor(flat).factors();
		ArrayList<long[]> factors = new ArrayList<>();
		for (PolynomialFpDynamic.Factor e : fs)
			for (int i = 0; i < e.multiplicity; i++)
				factors.add(e.factor);
		if (factors.size() <= 1) return null;
		boolean[] used = new boolean[factors.size()];
		for (int sz = 1; sz <= factors.size() / 2; sz++) {
			long[][] d = findFactorSubsetDfs(f, factors, used, 0, sz, new long[] {1}, sx);
			if (d != null) return d;
		}
		return null;
	}

	/** 指定サイズの1変数因子部分集合を試す。計算量: O(C(s,k) * (Mul1D + Div2D + Mul2D)) */
	private long[][] findFactorSubsetDfs(long[][] f, ArrayList<long[]> factors, boolean[] used, int from, int need, long[] prod, int sx) {
		if (need == 0) {
			long[][] candidate = unflattenKronecker(prod, sx, degX(f), degY(f));
			if (candidate == null || degX(candidate) <= 0 && degY(candidate) <= 0) return null;
			candidate = monic(candidate);
			if (equals(candidate, f)) return null;
			try {
				long[][] q = lexdiv(f, candidate);
				if (equals(mul(candidate, q), f)) return candidate;
			} catch (RuntimeException e) {
				return null;
			}
			return null;
		}
		for (int i = from; i <= factors.size() - need; i++) {
			if (used[i]) continue;
			used[i] = true;
			long[][] res = findFactorSubsetDfs(f, factors, used, i + 1, need - 1, poly1d.mul(prod, factors.get(i)), sx);
			used[i] = false;
			if (res != null) return res;
		}
		return null;
	}

	/** 2変数多項式を x=t^sx, y=t として詰める。計算量: O(項数) */
	private long[] flattenKronecker(long[][] a, int sx) {
		a = resize(a);
		if (degX(a) == -1) return poly1d.zero();
		long[] res = new long[degX(a) * sx + degY(a) + 1];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				if (a[i][j] != 0) res[i * sx + j] = a[i][j];
		return poly1d.resize(res);
	}

	/** Kronecker 置換した候補を2変数へ戻す。計算量: O(次数) */
	private long[][] unflattenKronecker(long[] a, int sx, int maxX, int maxY) {
		a = poly1d.resize(a);
		if (a.length == 0) return zero();
		long[][] res = new long[maxX + 1][maxY + 1];
		for (int idx = 0; idx < a.length; idx++) {
			if (a[idx] == 0) continue;
			int x = idx / sx, y = idx % sx;
			if (x > maxX || y > maxY) return null;
			res[x][y] = a[idx];
		}
		return resize(res);
	}

	/**
	 * 多項式 a(x, y) を y について微分する。
	 * @param a 多項式
	 * @return ∂a/∂y
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public long[][] diffY(long[][] a) {
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) res[i] = poly1d.differentiate(a[i]);
		return resize(res);
	}

	/**
	 * 多項式 a(x, y) を x について repeat 回微分する。
	 * @param a 多項式
	 * @param repeat 微分回数
	 * @return ∂^{repeat}a/∂x^{repeat}
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public long[][] diffX(long[][] a, int repeat) {
		if (repeat < 0) throw new IllegalArgumentException("repeat must be non-negative");
		if (repeat == 0) return ArrayUtils.copy(a);
		a = resize(a);
		if (repeat >= a.length) return zero();
		long[][] res = new long[a.length - repeat][];
		for (int i = repeat; i < a.length; i++) {
			res[i - repeat] = poly1d.mul(a[i], poly1d.fp.perm(i, repeat));
		}
		return resize(res);
	}

	/**
	 * 多項式 a(x, y) を y について repeat 回微分する。
	 * @param a 多項式
	 * @param repeat 微分回数
	 * @return ∂^{repeat}a/∂y^{repeat}
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public long[][] diffY(long[][] a, int repeat) {
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) res[i] = poly1d.diff(a[i], repeat);
		return resize(res);
	}

	/**
	 * 多項式 a(x, y) を x = x_val で評価した 1 変数多項式 a(x_val, y) を返す。
	 * @param a 多項式
	 * @param x_val 評価値
	 * @return a(x_val, y)
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public long[] evalX(long[][] a, long x_val) {
		long[] res = poly1d.zero();
		x_val = poly1d.fp.reduce(x_val);
		for (int i = a.length - 1; i >= 0; i--) {
			res = poly1d.add(poly1d.mul(res, x_val), a[i]);
		}
		return res;
	}

	/**
	 * 多項式 a(x, y) を y = y_val で評価した 1 変数多項式 a(x, y_val) を返す。
	 * @param a 多項式
	 * @param y_val 評価値
	 * @return a(x, y_val)
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public long[] evalY(long[][] a, long y_val) {
		long[] res = new long[a.length];
		for (int i = 0; i < a.length; i++) {
			res[i] = poly1d.eval(a[i], y_val);
		}
		return poly1d.resize(res);
	}

	/** yの次数を返す。計算量: O(項数) */
	public int degY(long[][] a) {
		int res = -1;
		for (long[] row : a) res = Math.max(res, poly1d.deg(row));
		return res;
	}

	/** 各単項式の総次数の最大値。計算量: O(項数) */
	public int totalDegree(long[][] a) {
		int res = -1;
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] != 0) res = Math.max(res, i + j);
			}
		}
		return res;
	}

	/** 項数を返す。計算量: O(項数) */
	public int countTerms(long[][] a) {
		int res = 0;
		for (long[] row : a) {
			for (long val : row) {
				if (val != 0) res++;
			}
		}
		return res;
	}

	/** monic な gcd を返す。計算量: O(degX^3 * M(degY)) 程度 */
	@Override
	public long[][] gcd(long[][] a, long[][] b) {
		a = resize(a); b = resize(b);
		if (degX(a) == -1) return monic(b);
		if (degX(b) == -1) return monic(a);

		long[] contA = contentX(a);
		long[] contB = contentX(b);
		long[] gCont = poly1d.gcd(contA, contB);

		long[][] primA = lexdivByPolyY(a, contA);
		long[][] primB = lexdivByPolyY(b, contB);

		long[][] resPrim = gcdZippel(primA, primB);
		if (resPrim == null) {
			var field = new FractionFieldStrategy<>(poly1d);
			var strategy = new PolynomialEuclideanStrategy<>(field);

			FractionFieldElement<long[]>[] fA = toFractionArray(primA);
			FractionFieldElement<long[]>[] fB = toFractionArray(primB);
			FractionFieldElement<long[]>[] fG = strategy.gcd(fA, fB);

			resPrim = fromFractionArray(fG);
		}
		return monic(mulByPolyY(resPrim, gCont));
	}

	/**
	 * Zippelの確率的モジュラーGCDアルゴリズムを用いてGCDを計算する。
	 */
	private long[][] gcdZippel(long[][] f, long[][] g) {
		f = resize(f);
		g = resize(g);
		if (degX(f) == -1) return monic(g);
		if (degX(g) == -1) return monic(f);
		if (degX(f) < degX(g)) { long[][] t = f; f = g; g = t; }

		long[] lcf = f[degX(f)];
		long[] lcg = g[degX(g)];

		Random rnd = new Random(0);
		for (int attempt = 0; attempt < 20; attempt++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			if (poly1d.eval(lcf, ry) == 0 || poly1d.eval(lcg, ry) == 0) continue;

			long[] f_r = new long[f.length];
			for (int i = 0; i < f.length; i++) f_r[i] = poly1d.eval(f[i], ry);
			long[] g_r = new long[g.length];
			for (int i = 0; i < g.length; i++) g_r[i] = poly1d.eval(g[i], ry);

			long[] gcd_r = poly1d.gcd(f_r, g_r);
			int skelDeg = poly1d.deg(gcd_r);
			if (skelDeg == -1) continue;

			long[][] G = new long[skelDeg + 1][];
			boolean success = true;
			for (int i = 0; i <= skelDeg; i++) {
				if (gcd_r[i] == 0) { G[i] = new long[0]; continue; }
				G[i] = interpolateZippel(f, g, i, ry, gcd_r[i], skelDeg);
				if (G[i] == null) { success = false; break; }
			}
			if (!success) continue;

			long[][] res = resize(G);
			try {
				if (isDivisible(f, res) && isDivisible(g, res)) return monic(res);
			} catch (Exception e) {}
		}
		return null;
	}

	private long[] interpolateZippel(long[][] f, long[][] g, int xIdx, long ry, long targetVal, int skelDeg) {
		long[] lcf = f[degX(f)];
		long[] lcg = g[degX(g)];
		Random rnd = new Random(0);

		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<Long> valuesY = new ArrayList<>();
		pointsY.add(ry);
		valuesY.add(targetVal);

		long[] currentCoeffPolyY = {targetVal};
		for (int t = 0; t < degY(f) + 2; t++) {
			long ry2 = poly1d.fp.reduce(rnd.nextLong());
			if (poly1d.eval(lcf, ry2) == 0 || poly1d.eval(lcg, ry2) == 0) continue;

			long[] f_ry2 = new long[f.length];
			for (int j = 0; j < f.length; j++) f_ry2[j] = poly1d.eval(f[j], ry2);
			long[] g_ry2 = new long[g.length];
			for (int j = 0; j < g.length; j++) g_ry2[j] = poly1d.eval(g[j], ry2);

			long[] gcd_ry2 = poly1d.gcd(f_ry2, g_ry2);
			if (poly1d.deg(gcd_ry2) != skelDeg) continue;

			pointsY.add(ry2);
			valuesY.add(xIdx < gcd_ry2.length ? gcd_ry2[xIdx] : 0L);

			long[] p_arr = new long[pointsY.size()];
			long[] v_arr = new long[valuesY.size()];
			for(int j=0; j<pointsY.size(); j++) { p_arr[j] = pointsY.get(j); v_arr[j] = valuesY.get(j); }
			long[] nextCoeffPolyY = poly1d.interpolate(p_arr, v_arr);

			if (Arrays.equals(nextCoeffPolyY, currentCoeffPolyY)) return currentCoeffPolyY;
			currentCoeffPolyY = nextCoeffPolyY;
			if (pointsY.size() > degY(f) + 1) break;
		}
		return currentCoeffPolyY;
	}

	private boolean isDivisible(long[][] a, long[][] b) {
		if (degX(b) == -1) return false;
		try {
			long[][] q = lexdiv(a, b);
			return equals(mul(b, q), a);
		} catch (Exception e) {
			return false;
		}
	}

	private FractionFieldElement<long[]>[] toFractionArray(long[][] a) {
		@SuppressWarnings("unchecked")
		FractionFieldElement<long[]>[] res = new FractionFieldElement[a.length];
		var field = new FractionFieldStrategy<>(poly1d);
		for (int i = 0; i < a.length; i++) res[i] = field.of(poly1d.resize(a[i]), poly1d.one());
		return res;
	}

	private long[][] fromFractionArray(FractionFieldElement<long[]>[] a) {
		long[] commonDen = new long[]{1};
		for (var f : a) {
			long[] g = poly1d.gcd(commonDen, f.den());
			commonDen = poly1d.mul(commonDen, poly1d.div(f.den(), g));
		}
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) res[i] = poly1d.mul(a[i].num(), poly1d.div(commonDen, a[i].den()));
		return res;
	}

	/**
	 * 1/f の指定された項の係数を効率的に計算するための最適な分解 f = ph - q を求める。
	 * 複雑度スコア Σ(a_i-1) を最小化する分解を探索する。ここで a_i は p, h, q の各既約因子の項数である。
	 * f[0][0]=1を仮定
	 * @param inputf 多項式 f
	 * @return 最適な分解結果
	 */
	public BestDecomposition2D findBestDecomposition(long[][] inputf) {
		if(inputf[0][0]!=1)throw new AssertionError();
		long[][] f = resize(inputf);
		if (degX(f) == -1) return new BestDecomposition2D(0, new Factor[0], 0, new Factor[0], 0);

		// f の非ゼロ項を抽出する（定数項以外）
		ArrayList<int[]> fTerms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (i == 0 && j == 0) continue;
				if (f[i][j] != 0) {
					fTerms.add(new int[] {i, j, (int) f[i][j]});
				}
			}
		}

		ArrayList<long[][]> candidatesP = new ArrayList<>();
		enumerateSparsePolynomialsFromF(Math.max(1, f.length), Math.max(1, f[0].length), fTerms, 1, candidatesP);
		ArrayList<BestCandidate2D> scoredP = new ArrayList<>();
		for (long[][] p : candidatesP) {
			ArrayList<int[]> pTerms = new ArrayList<>();
			for (int i = 0; i < p.length; i++) for (int j = 0; j < p[i].length; j++) if (p[i][j] != 0) pTerms.add(new int[] {i, j, (int) p[i][j]});
			scoredP.add(new BestCandidate2D(p, pTerms, scoreFactors(factor(p).factors())));
		}

		BestDecomposition2D best = computeDecompositionResult(one(), f, f);

		scoredP.sort((x, y) -> Long.compare(x.score, y.score));
		for (var sh : scoredP) {
			for (var sp : scoredP) {
				if (best.score != -1 && sh.score + sp.score >= best.score) continue;
				BestDecomposition2D current = computeDecompositionResult(sp.poly, sh.poly, f);
				if (best.score == -1 || current.score < best.score) best = current;
			}
		}
		return best;
	}

	private record BestCandidate2D(long[][] poly, ArrayList<int[]> terms, long score) {}

	private long scoreFactors(Factor[] factors) {
		long s = 0;
		for (Factor f : factors) s += countTerms(f.factor) - 1;
		return s;
	}

	private void enumerateSparsePolynomialsFromF(int dx, int dy, ArrayList<int[]> fTerms, long constTerm, ArrayList<long[][]> out) {
		// 0項 (1)
		long[][] res0 = new long[Math.max(1, dx)][Math.max(1, dy)];
		res0[0][0] = constTerm;
		out.add(resize(res0));

		// 1項 (1 + term_i)
		for (int i = 0; i < fTerms.size(); i++) {
			int[] t = fTerms.get(i);
			long[][] res = new long[Math.max(1, dx)][Math.max(1, dy)];
			res[0][0] = constTerm;
			if(t[0] < res.length && t[1] < res[t[0]].length) res[t[0]][t[1]] = t[2];
			out.add(resize(res));
		}

		// 2項 (1 + term_i + term_j)
		for (int i = 0; i < fTerms.size(); i++) {
			for (int j = i + 1; j < fTerms.size(); j++) {
				int[] t1 = fTerms.get(i);
				int[] t2 = fTerms.get(j);
				long[][] res = new long[Math.max(1, dx)][Math.max(1, dy)];
				res[0][0] = constTerm;
				if(t1[0] < res.length && t1[1] < res[t1[0]].length) res[t1[0]][t1[1]] = t1[2];
				if(t2[0] < res.length && t2[1] < res[t2[0]].length) res[t2[0]][t2[1]] = t2[2];
				out.add(resize(res));
			}
		}
	}

	private BestDecomposition2D computeDecompositionResult(long[][] p, long[][] h, long[][] f) {
		long[][] ph = mul(p, h);
		long[][] q = sub(ph, f);
		Factor[] pFactors = factor(p).factors();
		Factor[] hFactors = factor(h).factors();
		Factor[] qFactors = factor(q).factors();

		ArrayList<Factor> phFactorsList = new ArrayList<>();
		for (Factor fct : pFactors) phFactorsList.add(fct);
		for (Factor fct : hFactors) phFactorsList.add(fct);
		Factor[] phFactors = phFactorsList.toArray(new Factor[0]);

		long phConst = (lead(p) * lead(h)) % mod;
		long qConst = lead(q);

		ArrayList<long[][]> distinctIrred = new ArrayList<>();
		for (Factor ef : phFactors) {
			long[][] mf = monic(ef.factor);
			boolean exists = false;
			for (long[][] prev : distinctIrred) if (equals(prev, mf)) exists = true;
			if (!exists) distinctIrred.add(mf);
		}
		for (Factor ef : qFactors) {
			long[][] mf = monic(ef.factor);
			boolean exists = false;
			for (long[][] prev : distinctIrred) if (equals(prev, mf)) exists = true;
			if (!exists) distinctIrred.add(mf);
		}
		long score = 0;
		for (long[][] irred : distinctIrred) {
			score += countTerms(irred) - 1;
		}
		return new BestDecomposition2D(phConst, phFactors, qConst, qFactors, score);
	}
	
	
	public record Term2D(int dx, int dy, long v) {}
	
	public long[][] sparseInv(long[][] f, int nx, int ny) {
		if (nx <= 0 || ny <= 0) return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0] == 0) throw new ArithmeticException("f[0][0] must be non-zero");
		ArrayList<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (i == 0 && j == 0) continue;
				if (f[i][j] != 0) terms.add(new Term2D(i, j, poly1d.fp.reduce(f[i][j])));
			}
		}
		long[][] res = new long[nx][ny];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0]), mod);
		res[0][0] = inv0;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				if (i == 0 && j == 0) continue;
				long tmp = 0;
				for (Term2D t : terms) {
					int pi = i - t.dx, pj = j - t.dy;
					if (pi >= 0 && pj >= 0 && pi < nx && pj < ny) {
						tmp = (tmp + t.v * res[pi][pj]) % mod;
					}
				}
				res[i][j] = tmp == 0 ? 0 : (mod - tmp) % mod * inv0 % mod;
			}
		}
		return res;
	}

	public long[][] sparseLog(long[][] f, int nx, int ny) {
		if (nx <= 0 || ny <= 0) return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0] == 0) throw new ArithmeticException("f[0][0] must be non-zero");
		ArrayList<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				if (f[i][j] != 0) terms.add(new Term2D(i, j, poly1d.fp.reduce(f[i][j])));
			}
		}
		long[][] g = new long[nx][ny];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0]), mod);
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				if (i == 0 && j == 0) continue;
				long tmp = 0;
				if (i > 0) {
					for (Term2D t : terms) {
						int pi = i - t.dx, pj = j - t.dy;
						if (pi >= 0 && pj >= 0 && pi < nx && pj < ny) {
							if (i == t.dx && j == t.dy) {
								tmp = (tmp + i * t.v) % mod;
							} else {
								tmp = (tmp + mod - (i - t.dx) * t.v % mod * g[i - t.dx][j - t.dy] % mod) % mod;
							}
						}
					}
					g[i][j] = tmp * inv0 % mod * MathUtils.modInv(i, mod) % mod;
				} else {
					for (Term2D t : terms) {
						if (t.dx != 0) continue;
						int pj = j - t.dy;
						if (pj >= 0 && pj < ny) {
							if (j == t.dy) {
								tmp = (tmp + j * t.v) % mod;
							} else {
								tmp = (tmp + mod - (j - t.dy) * t.v % mod * g[0][j - t.dy] % mod) % mod;
							}
						}
					}
					g[i][j] = tmp * inv0 % mod * MathUtils.modInv(j, mod) % mod;
				}
			}
		}
		return g;
	}

	/**
	 * 多項式を式として表示する。
	 * @param label ラベル
	 * @param arr 多項式の係数配列
	 *
	 * <p>計算量: O(NM)
	 * <p>未テスト
	 */
	public void printPolyAsExpr(String label, long[][] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x", "y" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				long coeff = fp.reduce(arr[i][j]);
				if (coeff == 0) continue;

				if (!isFirst) {
					sb.append(" + ");
				}

				StringBuilder varPart = new StringBuilder();
				int[] powers = { i, j };
				for (int v = 0; v < 2; v++) {
					if (powers[v] > 0) {
						varPart.append(vars[v]);
						if (powers[v] > 1) {
							varPart.append("^").append(powers[v]);
						}
						varPart.append(" ");
					}
				}

				if (varPart.length() == 0) {
					sb.append(coeff);
				} else {
					if (coeff != 1) {
						sb.append(coeff).append("*");
					}
					sb.append(varPart.toString().trim());
				}

				isFirst = false;
			}
		}

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}

	/**
	 * x に関する終結式 Res_x(f, g) を計算する。
	 * 多点評価・補間を用いて 1 変数多項式の終結式計算に帰着させる。
	 * @param f 多項式 f
	 * @param g 多項式 g
	 * @return Res_x(f, g) (y の多項式)
	 */
	public long[] res_x(long[][] f, long[][] g) {
		f = resize(f);
		g = resize(g);
		int n = degX(f);
		int m = degX(g);
		if (n == -1 || m == -1) return poly1d.zero();
		if (n == 0) return poly1d.powFull(f[0], m);
		if (m == 0) return poly1d.powFull(g[0], n);

		int dyF = degY(f);
		int dyG = degY(g);
		int D = n * dyG + m * dyF;

		// 評価点の選定。lc(f) と lc(g) が 0 にならない点を選ぶ。
		long[] lcf = f[n];
		long[] lcg = g[m];

		long[] evalPoints = new long[D + 1];
		int count = 0;
		for (long y = 0; count < D + 1; y++) {
			if (y >= mod) throw new ArithmeticException("mod is too small for resultant calculation via interpolation");
			if (poly1d.eval(lcf, y) != 0 && poly1d.eval(lcg, y) != 0) {
				evalPoints[count++] = y;
			}
		}

		// 全ての係数多項式を evalPoints で多点評価
		long[][] evalF = new long[n + 1][];
		for (int i = 0; i <= n; i++) {
			evalF[i] = poly1d.multipointEval(f[i], evalPoints);
		}
		long[][] evalG = new long[m + 1][];
		for (int j = 0; j <= m; j++) {
			evalG[j] = poly1d.multipointEval(g[j], evalPoints);
		}

		long[] values = new long[D + 1];
		for (int k = 0; k < D + 1; k++) {
			long[] fk = new long[n + 1];
			for (int i = 0; i <= n; i++) fk[i] = evalF[i][k];
			long[] gk = new long[m + 1];
			for (int j = 0; j <= m; j++) gk[j] = evalG[j][k];
			values[k] = poly1d.resultant(fk, gk);
		}

		return poly1d.resize(poly1d.interpolate(evalPoints, values));
	}

	/**
	 * 低次数の2変数多項式 P, Q を用いて F(x, y) = \prod_{i \ge 1} P(x, x^i y) / Q(x, x^i y) \pmod{x^{nx} y^{ny}} を計算する。
	 * @param P 多項式 P(x, z) の係数配列
	 * @param Q 多項式 Q(x, z) の係数配列
	 * @param nx x の最大次数 + 1
	 * @param ny y の最大次数 + 1
	 * @return F(x, y) \pmod{x^{nx} y^{ny}} の係数配列
	 *
	 * <p>計算量: O(nx \cdot ny \cdot (\text{terms in } P + \text{terms in } Q))
	 * // 未テスト
	 */
	public long[][] infiniteProductPdivQ(long[][] P, long[][] Q, int nx, int ny) {
		//https://atcoder.jp/contests/abc221/submissions/77329884
		if (nx <= 0 || ny <= 0) return zero();
		// Q(0,0) = P(0,0) = 1 が仮定されていることを確認し、さもなくばエラー
		if (P == null || P.length == 0 || P[0] == null || P[0].length == 0 || poly1d.fp.reduce(P[0][0]) != 1) {
			throw new IllegalArgumentException("P[0][0] must be congruent to 1 modulo the prime");
		}
		if (Q == null || Q.length == 0 || Q[0] == null || Q[0].length == 0 || poly1d.fp.reduce(Q[0][0]) != 1) {
			throw new IllegalArgumentException("Q[0][0] must be congruent to 1 modulo the prime");
		}
		// P(x, 0) == Q(x, 0) であることを確認し、さもなくばエラー
		int maxLen = Math.max(P.length, Q.length);
		for (int r = 0; r < maxLen; r++) {
			long p_val = (r < P.length && P[r] != null && P[r].length > 0) ? poly1d.fp.reduce(P[r][0]) : 0;
			long q_val = (r < Q.length && Q[r] != null && Q[r].length > 0) ? poly1d.fp.reduce(Q[r][0]) : 0;
			if (p_val != q_val) {
				throw new IllegalArgumentException("P(x, 0) and Q(x, 0) must be equal modulo the prime to ensure convergence at y=0");
			}
		}

		long[][] f = new long[nx][ny];
		f[0][0] = 1; // 初期条件：y=0 で F(x,0) = 1 より f_0,0 = 1, その他の f_n,0 = 0 (n >= 1)

		ArrayList<Term2D> pTerms = new ArrayList<>();
		for (int r = 0; r < P.length; r++) {
			if (P[r] == null) continue;
			for (int s = 0; s < P[r].length; s++) {
				long p_val = poly1d.fp.reduce(P[r][s]);
				if (p_val != 0) {
					pTerms.add(new Term2D(r, s, p_val));
				}
			}
		}

		ArrayList<Term2D> qTerms = new ArrayList<>();
		for (int r = 0; r < Q.length; r++) {
			if (Q[r] == null) continue;
			for (int s = 0; s < Q[r].length; s++) {
				if (r == 0 && s == 0) continue; // (0,0) を除く
				long q_val = poly1d.fp.reduce(Q[r][s]);
				if (q_val != 0) {
					qTerms.add(new Term2D(r, s, q_val));
				}
			}
		}

		// 動的計画法による係数決定：
		// ここで、各多項式の係数を以下のように表す：
		//   F(x, y) = \sum_{n, m} f_{n, m} x^n y^m
		//   P(x, xy) = \sum_{r, s} p_{r, s} x^(r+s) y^s
		//   Q(x, xy) = \sum_{r, s} q_{r, s} x^(r+s) y^s
		//
		// 関数方程式 Q(x, xy) F(x, y) = P(x, xy) F(x, xy) より、
		// 両辺の [x^n y^m] の係数を比較する。
		// LHS [x^n y^m] Q(x, xy) F(x, y) は、
		//   \sum_{r, s} q_{r,s} f_{n-r-s, m-s} = f_{n,m} + \sum_{(r,s) != (0,0)} q_{r,s} f_{n-r-s, m-s}
		// RHS [x^n y^m] P(x, xy) F(x, xy) は、
		//   \sum_{r, s} p_{r,s} f_{n-r-m, m-s}
		// 従って、以下の漸化式が得られる：
		//   f_{n,m} = \sum_{r, s} p_{r, s} f_{n-r-m, m-s} - \sum_{(r, s) != (0,0)} q_{r, s} f_{n-r-s, m-s}
		for (int n = 1; n < nx; n++) {
			for (int m = 1; m <= Math.min(n, ny - 1); m++) {
				long val = 0;
				// 第1項： \sum_{r, s} p_{r, s} f_{n-r-m, m-s}
				for (int i = 0; i < pTerms.size(); i++) {
					Term2D t = pTerms.get(i);
					int r = t.dx;
					int s = t.dy;
					long p_val = t.v;
					int mk = m - s;
					if (mk < 0) continue;
					int nm = n - r - m;
					if (nm >= mk && nm >= 0) {
						val = (val + p_val * f[nm][mk]) % mod;
					}
				}
				// 第2項： - \sum_{(r, s) \ne (0,0)} q_{r, s} f_{n-r-s, m-s}
				for (int i = 0; i < qTerms.size(); i++) {
					Term2D t = qTerms.get(i);
					int r = t.dx;
					int s = t.dy;
					long q_val = t.v;
					int mk = m - s;
					if (mk < 0) continue;
					int nk = n - r - s;
					// x次数 >= y次数 (nk >= mk) かつ境界条件 (nk >= 0)
					if (nk >= mk && nk >= 0) {
						val = (val + (mod - q_val) * f[nk][mk]) % mod;
					}
				}
				f[n][m] = val;
			}
		}
		return f;
	}

	/**
	 * 多項式 f(x, y) の n 累乗 f(x, y)^n を、打ち切りなしで計算する。
	 * バイナリエクスポネシエーション（繰り返し二乗法）を用いて計算する。
	 * // 未テスト
	 * @param f 多項式
	 * @param n 指数（非負整数）
	 * @return f(x, y)^n
	 *
	 * <p>計算量: O(D_x \cdot D_y \cdot \log n) （D_x, D_y は結果の最高次数）
	 */
	public long[][] powFull(long[][] f, long n) {
		if (n < 0) throw new IllegalArgumentException("exponent must be non-negative");
		if (n == 0) return one();
		if (n == 1) return ArrayUtils.copy(f);
		int dx = degX(f);
		if (dx == -1) return zero();
		long[][] ret = one();
		long[][] b = ArrayUtils.copy(f);
		while (n != 0) {
			if (n % 2 == 1) {
				ret = mul(ret, b);
			}
			n /= 2;
			if (n == 0) break;
			b = mul(b, b);
		}
		return ret;
	}

	/**
	 * 与えられたすべての2D多項式の積を計算する。
	 * // 未テスト
	 * @param f 2D多項式の配列
	 * @return すべて of 2D多項式の積
	 *
	 * <p>計算量: O(N \log^2 N) （N は総次数）
	 */
	public long[][] mulAll(long[][][] f) {
		if (f.length == 0) return one();
		long[][][] copy = new long[f.length][][];
		for (int i = 0; i < f.length; i++) {
			copy[i] = resize(f[i]);
		}
		Arrays.sort(copy, (a, b) -> {
			if (a.length != b.length) {
				return Integer.compare(a.length, b.length);
			}
			for (int i = 0; i < a.length; i++) {
				int cmp = Arrays.compare(a[i], b[i]);
				if (cmp != 0) return cmp;
			}
			return 0;
		});
		java.util.Queue<long[][]> pq = new java.util.ArrayDeque<>();
		for (int i = 0; i < copy.length; ) {
			int j = i;
			while (j < copy.length && equals(copy[i], copy[j])) j++;
			int count = j - i;
			if (count == 1) {
				pq.add(copy[i]);
			} else {
				pq.add(powFull(copy[i], count));
			}
			i = j;
		}
		while (pq.size() >= 2) {
			pq.add(mul(pq.poll(), pq.poll()));
		}
		return pq.peek();
	}

	/**
	 * 与えられたすべての2D多項式の積を計算する。
	 * // 未テスト
	 * @param f 2D多項式のリスト
	 * @return すべての2D多項式の積
	 *
	 * <p>計算量: O(N \log^2 N) （N は総次数）
	 */
	public long[][] mulAll(List<long[][]> f) {
		return mulAll(f.toArray(new long[0][][]));
	}

	/**
	 * 多項式の非ゼロ項のリストを返す。
	 * @param p 2D多項式
	 * @return 非ゼロ項のリスト
	 */
	public List<Term2D> getTerms(long[][] p) {
		List<Term2D> terms = new ArrayList<>();
		for (int i = 0; i < p.length; i++) {
			if (p[i] == null) continue;
			for (int j = 0; j < p[i].length; j++) {
				long v = fp.reduce(p[i][j]);
				if (v != 0) {
					terms.add(new Term2D(i, j, v));
				}
			}
		}
		return terms;
	}


	private interface EvalFactor {
		LinForm form();
	}

	/**
	 * 1次形式 a * t + b * i + c を表現する。
	 */
	private record LinForm(int dt, int di, int c) {
	}
	/**
	 * sign=+1分子 -1分母 : 1/form! or form!
	 */
	private record Fact(LinForm form, int sign) implements EvalFactor {}
	/**
	 * base^form
	 */
	private record Pow(LinForm form, long base) implements EvalFactor {}

	/**
	 * 分類結果（Case A, B, C）を保持する。
	 */
	private static class ClassificationResult {
		boolean possible;
		List<EvalFactor> tOnly = new ArrayList<>();
		List<EvalFactor> iMinusTOnly = new ArrayList<>();
		List<EvalFactor> iOnly = new ArrayList<>();
	}

	/**
	 * 因子リストをCase A, B, Cに分類する。
	 * @param allFactors すべての因子リスト
	 * @param S_m パラメータのステップ数
	 * @param s_sign 符号パラメータ
	 * @return 分類結果
	 */
	private ClassificationResult tryClassify(List<EvalFactor> allFactors, long S_m, long s_sign) {
		ClassificationResult res = new ClassificationResult();
		long mult = S_m * s_sign;
		res.possible = true;
		for (EvalFactor fe : allFactors) {
			long tc = fe.form().dt;
			long ic = fe.form().di;
			if (tc == 0) {
				res.iOnly.add(fe);
			} else if (tc == mult && ic == 0) {
				res.tOnly.add(fe);
			} else if (tc == -mult && ic == mult) {
				if (S_m > 1) {
					res.possible = false;
					break;
				}
				res.iMinusTOnly.add(fe);
			} else {
				res.possible = false;
				break;
			}
		}
		return res;
	}

	/**
	 * クラス1: 2項式 x 2項式 x 2項式 g = G1 * G2 * G3 について、
	 * 各 i = 0, 1, ..., N に対して [x^(alpha * i + beta) y^(gamma * i + delta)] g^i を
	 * 1次元畳み込みにできないか試みて、できる場合は O(N log N) 時間で列挙する。
	 *　無理な場合はO(N^2)。
	 * @param G1 二項式因子1
	 * @param G2 二項式因子2
	 * @param G3 二項式因子3
	 * @param alpha パラメータ
	 * @param beta パラメータ
	 * @param gamma パラメータ
	 * @param delta パラメータ
	 * @param N 求める上限
	 * @return c_0, c_1, ..., c_N
	 */
	public long[] enumerateCoefficientsClass1(
		long[][] G1, long[][] G2, long[][] G3,
		int alpha, int beta, int gamma, int delta, int N
	) {
		//https://atcoder.jp/contests/abc289/submissions/77774367
		List<Term2D> terms1 = getTerms(G1);
		List<Term2D> terms2 = getTerms(G2);
		List<Term2D> terms3 = getTerms(G3);

		if (terms1.size() != 2 || terms2.size() != 2 || terms3.size() != 2) {
			throw new AssertionError();
		}
		Term2D P1 = terms1.get(0);
		Term2D Q1 = terms1.get(1);
		Term2D P2 = terms2.get(0);
		Term2D Q2 = terms2.get(1);
		Term2D P3 = terms3.get(0);
		Term2D Q3 = terms3.get(1);

		//  ((P1+Q1)(P2+Q2)(P3+Q3))^i
		// =(P1)^j1 Q1^(i-j1) (P2)^j2 Q2^(i-j2) (P3)^j3 Q3^(i-j3)
		// X: j1*(a_P1 - a_Q1) + j2*(a_P2 - a_Q2) + j3*(a_P3 - a_Q3) + i*(a_Q1 + a_Q2 + a_Q3 - alpha) = beta
		// Y: j1*(b_P1 - b_Q1) + j2*(b_P2 - b_Q2) + j3*(b_P3 - b_Q3) + i*(b_Q1 + b_Q2 + b_Q3 - gamma) = delta
		long[][] A = {
			{ P1.dx - Q1.dx, P2.dx - Q2.dx, P3.dx - Q3.dx, Q1.dx + Q2.dx + Q3.dx - alpha },
			{ P1.dy - Q1.dy, P2.dy - Q2.dy, P3.dy - Q3.dy, Q1.dy + Q2.dy + Q3.dy - gamma }
		};
		long[] b_arr = { beta, delta };
		
		// 未知数のベクトルは (j1, j2, j3, i)^T
		
		long[][] eqSol = library.util.linalg.MatrixUtilsZ.linearEquationOnZ(A, b_arr);
		if (eqSol == null) {
			return new long[N + 1];
		}
		long[] part = eqSol[0];//特殊解
		long[] v1 = eqSol.length >= 2 ? eqSol[1] : new long[4];//基底
		long[] v2 = eqSol.length >= 3 ? eqSol[2] : new long[4];//基底

		long c1 = v1[3];
		long c2 = v2[3];
		if (c1 == 0 && c2 == 0) {//iが動かない
			throw new AssertionError();
		}

		// B = [c1   c2  ]
		//     [v10  v20 ]
		//     [v11  v21 ]
		//     [v12  v22 ]
		// として (i, j1, j2, j3)^T = B (t, u)^T 
		// 列エルミート標準形 H = BV は、第一行目が [g, 0] (ただし g = gcd(c1, c2)) となるため、
		// H[k+1][0] = B_k, H[k+1][1] = D_k を直接得ることができる。
		long[][] B = {
			{ c1, c2 },
			{ v1[0], v2[0] },
			{ v1[1], v2[1] },
			{ v1[2], v2[2] }
		};
		MatrixUtilsZ.HermiteResult hnfRes = MatrixUtilsZ.columnHermiteNormalForm(B);
		long[][] H = hnfRes.H();
		long gcdVal = H[0][0];
		boolean classifiable = true;
		for (int k = 0; k < 3; k++) {
			if (H[k + 1][0] % gcdVal != 0) {
				classifiable = false;
			}
		}
		
		long[] D = new long[3];
		long[] E = new long[3];
		long[] F = new long[3];
		long S_m = MathUtils.gcd(H[1][1], H[2][1], H[3][1]);

		if (S_m == 0) { // j_k = E[k]i + F[k]
			for (int k = 0; k < 3; k++) {
				D[k] = H[k + 1][1];
				E[k] = H[k + 1][0] / gcdVal;
				F[k] = part[k] - part[3] * E[k];
			}
			List<EvalFactor> allFactors = new ArrayList<>();
			for (int r = 0; r < 3; r++) {
				allFactors.add(new Fact(new LinForm(0, 1, 0), 1));
			}
			for (int k = 0; k < 3; k++) {
				allFactors.add(new Fact(new LinForm((int) D[k], (int) E[k], (int) F[k]), -1));
				allFactors.add(new Fact(new LinForm((int) -D[k], (int) (1 - E[k]), (int) -F[k]), -1));
			}
			long[] mP_coeffs = { P1.v, P2.v, P3.v };
			long[] mQ_coeffs = { Q1.v, Q2.v, Q3.v };
			for (int k = 0; k < 3; k++) {
				allFactors.add(new Pow(new LinForm((int) D[k], (int) E[k], (int) F[k]), mP_coeffs[k]));
				allFactors.add(new Pow(new LinForm((int) -D[k], (int) (1 - E[k]), (int) -F[k]), mQ_coeffs[k]));
			}

			long[] ans = new long[N + 1];
			for (int i = 0; i <= N; i++) {
				if ((i - part[3]) % gcdVal != 0) continue;
				if (1L * alpha * i + beta < 0 || 1L * gamma * i + delta < 0) continue;
				long val = 1;
				boolean valid = true;
				for (EvalFactor fe : allFactors) {
					long arg = fe.form().di * i + fe.form().c;
					if (fe instanceof Fact fact) {
						if (arg < 0) {
							valid = false;
							break;
						}
						long f_val = fact.sign() == 1 ? fp.fac((int) arg) : fp.ifac((int) arg);
						val = val * f_val % poly1d.mod;
					} else if (fe instanceof Pow pow) {
						long p_val = MathUtils.modPow(pow.base(), (arg % (poly1d.mod - 1) + poly1d.mod - 1) % (poly1d.mod - 1), poly1d.mod);
						val = val * p_val % poly1d.mod;
					}
				}
				if (valid) {
					ans[i] = val;
				}
			}
			return ans;
		}

		ClassificationResult classRes = null;
		List<EvalFactor> allFactors = null;

		for (int flip = 0; flip <= 1; flip++) {
			if (flip == 1) {
				// D の符号を反転して試す。
				// これにより、i - t 型だけでなく i + t 型の畳み込みも自然に試みることができる。
				for (int k = 0; k < 3; k++) {
					H[k + 1][1] = -H[k + 1][1];
				}
			}
			// i = part[3] + g t'
			// j_k = part[k] + H[k+1][0] t' + D[k] u
			// で t' を消去すると
			// j_k = E[k]i + D[k]u + F[k]
			for (int k = 0; k < 3; k++) {
				D[k] = H[k + 1][1];
				E[k] = H[k + 1][0] / gcdVal;
				F[k] = part[k] - part[3] * E[k];
			}
			classifiable = true;
			S_m = MathUtils.gcd(D[0], D[1], D[2]);
			for (int k = 0; k < 3; k++) {
				if (D[k] != 0 && Math.abs(D[k] / S_m) != 1) {
					classifiable = false;
					break;
				}
			}

			if (classifiable) {
				allFactors = new ArrayList<>();
				// 分子のi!^3
				for (int r = 0; r < 3; r++) {
					allFactors.add(new Fact(new LinForm(0, 1, 0), 1));
				}
				// Three denominator factorials j_k! : form is (D_arr[k]*t + E_arr[k]*i + F_arr[k])
				// Three denominator factorials (i-j_k)! : form is (-D_arr[k]*t + (1-E_arr[k])*i - F_arr[k])
				for (int k = 0; k < 3; k++) {
					allFactors.add(new Fact(new LinForm((int) D[k], (int) E[k], (int) F[k]), -1));
					allFactors.add(new Fact(new LinForm((int) -D[k], (int) (1 - E[k]), (int) -F[k]), -1));
				}
				// Base power terms
				long[] mP_coeffs = { P1.v, P2.v, P3.v };
				long[] mQ_coeffs = { Q1.v, Q2.v, Q3.v };
				for (int k = 0; k < 3; k++) {
					allFactors.add(new Pow(new LinForm((int) D[k], (int) E[k], (int) F[k]), mP_coeffs[k]));
					allFactors.add(new Pow(new LinForm((int) -D[k], (int) (1 - E[k]), (int) -F[k]), mQ_coeffs[k]));
				}

				classRes = tryClassify(allFactors, S_m, 1);
				if (!classRes.possible) {
					classRes = tryClassify(allFactors, S_m, -1);
				}
				if (classRes.possible) {
					break;
				}
			}
		}
		if (!classifiable || !classRes.possible) {
			// Fallback to O(N^2) direct summation over u
			
			// i = part[3] + g t
			// j_k = part[k] + H[k+1][0] t + H[k][1] u
			System.err.println("Fallback to O(N^2).");
			long[] ans = new long[N + 1];
			for (int i = 0; i <= N; i++) {
				if ((i - part[3]) % gcdVal != 0) continue;
				if (1L * alpha * i + beta < 0 || 1L * gamma * i + delta < 0) continue;
				long t = (i - part[3]) / gcdVal;

				long minU = Long.MIN_VALUE;
				long maxU = Long.MAX_VALUE;
				boolean possibleI = true;

				for (int k = 0; k < 3; k++) {
					long tc = H[k + 1][1];
					long const_val = H[k + 1][0] * t + part[k];
					
					// {i choose j_k} != 0 より
					// 0 <= j_k <= i
					// 0 <= tc * u + const_val <= i
					if (tc > 0) {
						// u >= -const_val / tc
						minU = Math.max(minU, Math.ceilDiv(-const_val, tc));
						// u <= (i - const_val) / tc
						maxU = Math.min(maxU, Math.floorDiv(i - const_val, tc));
					} else if (tc < 0) {
						// u <= const_val / -tc
						maxU = Math.min(maxU, Math.floorDiv(-const_val, tc));
						// u >= (const_val - i) / absTc
						minU = Math.max(minU, Math.ceilDiv(i - const_val, tc));
					} else {
						if (const_val < 0 || const_val > i) {
							possibleI = false;
							break;
						}
					}
				}

				if (!possibleI || minU > maxU) continue;

				long sum = 0;
				for (long u = minU; u <= maxU; u++) {
					long val = 1;
					boolean valid = true;

					// Numerator factorials i! (three times)
					long f_i = fp.fac(i);
					val = val * f_i % poly1d.mod;
					val = val * f_i % poly1d.mod;
					val = val * f_i % poly1d.mod;

					for (int k = 0; k < 3; k++) {
						long jk = H[k + 1][1] * u + H[k + 1][0] * t + part[k];
						long i_minus_jk = i - jk;

						if (jk < 0 || i_minus_jk < 0) {
							valid = false;
							break;
						}
						// Denominator factorials jk! and (i - jk)!
						val = val * fp.ifac((int) jk) % poly1d.mod;
						val = val * fp.ifac((int) i_minus_jk) % poly1d.mod;

						// Base power terms P_k^jk and Q_k^(i-jk)
						long baseP = k == 0 ? P1.v : (k == 1 ? P2.v : P3.v);
						long baseQ = k == 0 ? Q1.v : (k == 1 ? Q2.v : Q3.v);

						long powP = fp.pow(baseP, jk);
						long powQ = fp.pow(baseQ, i_minus_jk);

						val = val * powP % poly1d.mod * powQ % poly1d.mod;
					}

					if (valid) {
						sum = (sum + val) % poly1d.mod;
					}
				}
				ans[i] = sum;
			}
			return ans;
		}

		long[] ans = new long[N + 1];

		// Group by parity
		for (int parity = 0; parity < S_m; parity++) {
			int maxJ = (int)Math.floorDiv(N - parity, S_m);
			if (maxJ < 0) continue;

			long[] polyA = new long[2 * maxJ + 1];
			long[] polyB = new long[2 * maxJ + 1];

			for (int T = 0; T <= 2 * maxJ; T++) {
				long t = T - maxJ;
				long val = 1;
				boolean valid = true;
				for (EvalFactor fe : classRes.tOnly) {
					long arg = fe.form().dt * t + fe.form().c;
					if (fe instanceof Fact fact) {
						if (arg < 0) {
							valid = false;
							break;
						}
						long f_val = fact.sign() == 1 ? fp.fac((int) arg) : fp.ifac((int) arg);
						val = val * f_val % poly1d.mod;
					} else if (fe instanceof Pow pow) {
						long p_val = fp.pow(pow.base(), arg);
						val = val * p_val % poly1d.mod;
					}
				}
				polyA[T] = valid ? val : 0;
			}

			for (int u = 0; u <= 2 * maxJ; u++) {
				long val = 1;
				boolean valid = true;
				for (EvalFactor fe : classRes.iMinusTOnly) {
					long arg = (-fe.form().dt) * (S_m * u + parity) + fe.form().c;
					if (fe instanceof Fact fact) {
						if (arg < 0) {
							valid = false;
							break;
						}
						long f_val = fact.sign() == 1 ? fp.fac((int) arg) : fp.ifac((int) arg);
						val = val * f_val % poly1d.mod;
					} else if (fe instanceof Pow pow) {
						long p_val = fp.pow(pow.base(), arg);
						val = val * p_val % poly1d.mod;
					}
				}
				polyB[u] = valid ? val : 0;
			}

			long[] polyH = poly1d.mul(polyA, polyB);

			for (int j = 0; j <= maxJ; j++) {
				int i = (int) (S_m * j + parity);
				if (i > N) continue;
				if ((i - part[3]) % gcdVal != 0) continue;
				if (1L * alpha * i + beta < 0 || 1L * gamma * i + delta < 0) continue;

				long prefactor = 1;
				boolean valid = true;
				for (EvalFactor fe : classRes.iOnly) {
					long arg = fe.form().di * i + fe.form().c;
					if (fe instanceof Fact fact) {
						if (arg < 0) {
							valid = false;
							break;
						}
						long f_val = fact.sign() == 1 ? fp.fac((int) arg) : fp.ifac((int) arg);
						prefactor = prefactor * f_val % poly1d.mod;
					} else if (fe instanceof Pow pow) {
						long p_val = fp.pow(pow.base(), arg);
						prefactor = prefactor * p_val % poly1d.mod;
					}
				}

				if (valid) {
					long val_H = polyH[j + maxJ];
					ans[i] = val_H * prefactor % poly1d.mod;
				}
			}
		}

		return ans;
	}

}
