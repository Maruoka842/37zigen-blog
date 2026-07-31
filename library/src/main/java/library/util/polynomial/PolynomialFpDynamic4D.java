package library.util.polynomial;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import library.util.Itertools;
import java.util.Random;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.MathUtils;
import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.PolynomialEuclideanStrategy;
import library.util.algebra.strategy.UFDStrategy;
import library.util.linalg.MatrixUtilsFp;

/**
 * 4変数の多項式（係数は mod p）
 */
public class PolynomialFpDynamic4D implements UFDStrategy<long[][][][]>, ExactDivRingStrategy<long[][][][]> {
	/**
	 * 最適な分解結果を保持するレコード。
	 * f = phConst * phFactors - qConst * qFactors の関係を満たす。
	 * @param phConst ph部分の定数倍
	 * @param phFactors ph部分の既約因子
	 * @param qConst q部分の定数倍
	 * @param qFactors q部分の既約因子
	 * @param score Σ(項数-1) で計算される複雑度スコア
	 */
	public record BestDecomposition4D(long phConst, Factor[] phFactors, long qConst, Factor[] qFactors, long score) {}
	public final long mod;
	private final Fp mo;
	private final PolynomialFpDynamic poly1d;
	private final PolynomialFpDynamic3D poly3d;

	/** 998244353 = 119×2^23+1, 原始根3 */
	public static final PolynomialFpDynamic4D MOD998244353 = new PolynomialFpDynamic4D(
			PolynomialFpDynamic3D.MOD998244353);
	/** 469762049 = 7×2^26+1, 原始根3 */
	public static final PolynomialFpDynamic4D MOD469762049 = new PolynomialFpDynamic4D(
			PolynomialFpDynamic3D.MOD469762049);
	/** 167772161 = 5×2^25+1, 原始根3 */
	public static final PolynomialFpDynamic4D MOD167772161 = new PolynomialFpDynamic4D(
			PolynomialFpDynamic3D.MOD167772161);
	/** 754974721 = 45×2^24+1, 原始根11 */
	public static final PolynomialFpDynamic4D MOD754974721 = new PolynomialFpDynamic4D(
			PolynomialFpDynamic3D.MOD754974721);
	/** 1004535809 = 479×2^21+1, 原始根3 */
	public static final PolynomialFpDynamic4D MOD1004535809 = new PolynomialFpDynamic4D(
			PolynomialFpDynamic3D.MOD1004535809);

	public PolynomialFpDynamic4D(long mod) {
		this.mod = mod;
		this.mo = new Fp(mod);
		this.poly1d = PolynomialFpDynamic.of(mod);
		this.poly3d = new PolynomialFpDynamic3D(PolynomialFpDynamic2D.of(this.poly1d));
	}

	public PolynomialFpDynamic4D(PolynomialFpDynamic poly1d) {
		this.mod = poly1d.mod;
		this.mo = new Fp(mod);
		this.poly1d = poly1d;
		this.poly3d = new PolynomialFpDynamic3D(PolynomialFpDynamic2D.of(this.poly1d));
	}

	public PolynomialFpDynamic4D(PolynomialFpDynamic3D poly3d) {
		this.mod = poly3d.mod;
		this.mo = new Fp(mod);
		this.poly1d = poly3d.poly1d;
		this.poly3d = poly3d;
	}

	@Override
	public long[][][][] zero() { return new long[0][0][0][0]; }

	@Override
	public long[][][][] one() { return new long[][][][] {{{{1}}}}; }

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][][] x() {
		long[][][][] ret = new long[2][1][1][1];
		ret[1][0][0][0] = 1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][][] y() {
		long[][][][] ret = new long[1][2][1][1];
		ret[0][1][0][0] = 1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][][] z() {
		long[][][][] ret = new long[1][1][2][1];
		ret[0][0][1][0] = 1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][][] w() {
		long[][][][] ret = new long[1][1][1][2];
		ret[0][0][0][1] = 1;
		return ret;
	}

	@Override
	public long[][][][] add(long[][][][] a, long[][][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0, l = 0, k = 0;
		for (long[][][] r3 : a) {
			m = Math.max(m, r3.length);
			for (long[][] r2 : r3) {
				l = Math.max(l, r2.length);
				for (long[] r1 : r2)
					k = Math.max(k, r1.length);
			}
		}
		for (long[][][] r3 : b) {
			m = Math.max(m, r3.length);
			for (long[][] r2 : r3) {
				l = Math.max(l, r2.length);
				for (long[] r1 : r2)
					k = Math.max(k, r1.length);
			}
		}
		long[][][][] c = new long[n][m][l][k];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				for (int p = 0; p < l; p++)
					for (int q = 0; q < k; q++) {
						long v1 = getSafe(a, i, j, p, q);
						long v2 = getSafe(b, i, j, p, q);
						c[i][j][p][q] = (v1 + v2) % mod;
					}
		return c;
	}

	@Override
	public long[][][][] sub(long[][][][] a, long[][][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0, l = 0, k = 0;
		for (long[][][] r3 : a) {
			m = Math.max(m, r3.length);
			for (long[][] r2 : r3) {
				l = Math.max(l, r2.length);
				for (long[] r1 : r2)
					k = Math.max(k, r1.length);
			}
		}
		for (long[][][] r3 : b) {
			m = Math.max(m, r3.length);
			for (long[][] r2 : r3) {
				l = Math.max(l, r2.length);
				for (long[] r1 : r2)
					k = Math.max(k, r1.length);
			}
		}
		long[][][][] c = new long[n][m][l][k];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				for (int p = 0; p < l; p++)
					for (int q = 0; q < k; q++) {
						long v1 = getSafe(a, i, j, p, q);
						long v2 = getSafe(b, i, j, p, q);
						c[i][j][p][q] = (v1 - v2 + mod) % mod;
					}
		return c;
	}

	@Override
	public long[][][][] neg(long[][][][] a) {
		return sub(zero(), a);
	}

	public long[][][][] mulNaive(long[][][][] a, long[][][][] b) {
		if (a.length == 0 || b.length == 0) return new long[0][0][0][0];
		ArrayList<int[]> al = new ArrayList<>(); ArrayList<Long> av = new ArrayList<>();
		int ma = 0, la = 0, ka = 0;
		for (int i = 0; i < a.length; i++) {
			ma = Math.max(ma, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				la = Math.max(la, a[i][j].length);
				for (int k = 0; k < a[i][j].length; k++) {
					ka = Math.max(ka, a[i][j][k].length);
					for (int l = 0; l < a[i][j][k].length; l++)
						if (a[i][j][k][l] != 0) { al.add(new int[]{i,j,k,l}); av.add(a[i][j][k][l]); }
				}
			}
		}
		ArrayList<int[]> bl = new ArrayList<>(); ArrayList<Long> bv = new ArrayList<>();
		int mb = 0, lb = 0, kb = 0;
		for (int i = 0; i < b.length; i++) {
			mb = Math.max(mb, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				lb = Math.max(lb, b[i][j].length);
				for (int k = 0; k < b[i][j].length; k++) {
					kb = Math.max(kb, b[i][j][k].length);
					for (int l = 0; l < b[i][j][k].length; l++)
						if (b[i][j][k][l] != 0) { bl.add(new int[]{i,j,k,l}); bv.add(b[i][j][k][l]); }
				}
			}
		}
		if (av.isEmpty() || bv.isEmpty()) return new long[0][0][0][0];
		long[][][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1][ka + kb - 1];
		for (int k = 0; k < av.size(); k++) {
			long v = av.get(k); int[] p1 = al.get(k);
			for (int l = 0; l < bv.size(); l++) {
				int[] p2 = bl.get(l);
				c[p1[0]+p2[0]][p1[1]+p2[1]][p1[2]+p2[2]][p1[3]+p2[3]] = (c[p1[0]+p2[0]][p1[1]+p2[1]][p1[2]+p2[2]][p1[3]+p2[3]] + v * bv.get(l)) % mod;
			}
		}
		return c;
	}

	public long[][][][] mul(long[][][][] a, long b) {
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = poly3d.mul(a[i], b);
		}
		return res;
	}

	@Override
	public long[][][][] mul(long[][][][] a, long[][][][] b) {
		if (!poly1d.isNTTFriendly)
			return mulNaive(a, b);
		if (a.length == 0 || b.length == 0)
			return new long[0][0][0][0];
		int ma = 0, la = 0, ka = 0;
		for (long[][][] r3 : a) {
			ma = Math.max(ma, r3.length);
			for (long[][] r2 : r3) {
				la = Math.max(la, r2.length);
				for (long[] r1 : r2)
					ka = Math.max(ka, r1.length);
			}
		}
		int mb = 0, lb = 0, kb = 0;
		for (long[][][] r3 : b) {
			mb = Math.max(mb, r3.length);
			for (long[][] r2 : r3) {
				lb = Math.max(lb, r2.length);
				for (long[] r1 : r2)
					kb = Math.max(kb, r1.length);
			}
		}
		if (ma == 0 || la == 0 || ka == 0 || mb == 0 || lb == 0 || kb == 0)
			return new long[0][0][0][0];
		int strideJ = ma + mb - 1;
		int strideP = la + lb - 1;
		int strideQ = ka + kb - 1;
		long[] fa = new long[a.length * strideJ * strideP * strideQ];
		long[] fb = new long[b.length * strideJ * strideP * strideQ];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				for (int p = 0; p < a[i][j].length; p++)
					for (int q = 0; q < a[i][j][p].length; q++)
						fa[i * strideJ * strideP * strideQ + j * strideP * strideQ + p * strideQ + q] = a[i][j][p][q];
		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++)
				for (int p = 0; p < b[i][j].length; p++)
					for (int q = 0; q < b[i][j][p].length; q++)
						fb[i * strideJ * strideP * strideQ + j * strideP * strideQ + p * strideQ + q] = b[i][j][p][q];
		long[] res = poly1d.mul(fa, fb);
		long[][][][] c = new long[a.length + b.length - 1][strideJ][strideP][strideQ];
		for (int i = 0; i < res.length; i++) {
			int idxI = i / (strideJ * strideP * strideQ);
			int rem = i % (strideJ * strideP * strideQ);
			int idxJ = rem / (strideP * strideQ);
			rem %= (strideP * strideQ);
			int idxP = rem / strideQ;
			int idxQ = rem % strideQ;
			if (idxI < c.length && idxJ < c[idxI].length && idxP < c[idxI][idxJ].length
					&& idxQ < c[idxI][idxJ][idxP].length)
				c[idxI][idxJ][idxP][idxQ] = res[i];
		}
		return c;
	}

	@Override
	public boolean equals(long[][][][] a, long[][][][] b) {
		return Arrays.deepEquals(resize(a), resize(b));
	}

	/**
	 * 4変数多項式を Kronecker 置換により 1変数多項式に変換する。
	 * 各変数の次数に応じて適切な重み（混合基数）を掛けて 1変数の次数に写像する。
	 * 写像: x=t^(sy*sz*sw), y=t^(sz*sw), z=t^sw, w=t
	 *
	 * @param a 多項式
	 * @param sy y変数の重み（yの最大次数+1）
	 * @param sz z変数の重み（zの最大次数+1）
	 * @param sw w変数の重み（wの最大次数+1）
	 * @return 1変数多項式の係数配列
	 */
	private long[] flattenKronecker(long[][][][] a, int sy, int sz, int sw) {
		a = resize(a);
		if (degX(a) == -1) return poly1d.zero();
		long[] res = new long[((degX(a) * sy + degY(a)) * sz + degZ(a)) * sw + degW(a) + 1];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				for (int k = 0; k < a[i][j].length; k++)
					for (int l = 0; l < a[i][j][k].length; l++)
						if (a[i][j][k][l] != 0) res[((i * sy + j) * sz + k) * sw + l] = a[i][j][k][l];
		return poly1d.resize(res);
	}

	/**
	 * Kronecker 置換された 1変数多項式を 4変数多項式に戻す（逆写像）。
	 *
	 * @param a 1変数多項式の係数配列
	 * @param sy y変数の重み
	 * @param sz z変数の重み
	 * @param sw w変数の重み
	 * @param maxX x変数の最大次数
	 * @param maxY y変数の最大次数
	 * @param maxZ z変数の最大次数
	 * @param maxW w変数の最大次数
	 * @return 4変数多項式。次数制限を超える場合は null
	 */
	private long[][][][] unflattenKronecker(long[] a, int sy, int sz, int sw, int maxX, int maxY, int maxZ, int maxW) {
		a = poly1d.resize(a);
		if (a.length == 0) return zero();
		long[][][][] res = new long[maxX + 1][maxY + 1][maxZ + 1][maxW + 1];
		for (int idx = 0; idx < a.length; idx++) {
			if (a[idx] == 0) continue;
			int x = idx / (sy * sz * sw), rem = idx % (sy * sz * sw);
			int y = rem / (sz * sw);
			rem %= sz * sw;
			int z = rem / sw, w = rem % sw;
			if (x > maxX || y > maxY || z > maxZ || w > maxW) return null;
			res[x][y][z][w] = a[idx];
		}
		return resize(res);
	}

	/**
	 * x に関する次数を返す。
	 * @param a 多項式
	 * @return x の次数。0多項式の場合は -1
	 */
	public int degX(long[][][][] a) {
		for (int i = a.length - 1; i >= 0; i--)
			if (poly3d.degX(a[i]) != -1) return i;
		return -1;
	}

	/**
	 * 多項式 f が 0 かを判定する。
	 * @param f 多項式
	 * @return f が 0 なら true
	 *
	 * <p>計算量: O(degX f * degY f * degZ f * degW f)
	 */
	public boolean isZero(long[][][][] f) {
		return degX(f) == -1;
	}

	/**
	 * y に関する次数を返す。
	 */
	public int degY(long[][][][] a) {
		int res = -1;
		for (long[][][] b : a) res = Math.max(res, poly3d.degX(b));
		return res;
	}

	/**
	 * z に関する次数を返す。
	 */
	public int degZ(long[][][][] a) {
		int res = -1;
		for (long[][][] b : a) res = Math.max(res, poly3d.degY(b));
		return res;
	}

	/**
	 * w に関する次数を返す。
	 */
	public int degW(long[][][][] a) {
		int res = -1;
		for (long[][][] b : a) res = Math.max(res, poly3d.degZ(b));
		return res;
	}

	/** 全次数を返す。計算量: O(項数) */
	public int totalDegree(long[][][][] a) {
		int res = -1;
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < a[i][j].length; k++) {
					for (int l = 0; l < a[i][j][k].length; l++) {
						if (a[i][j][k][l] != 0) res = Math.max(res, i + j + k + l);
					}
				}
			}
		}
		return res;
	}

	/** 項数を返す。計算量: O(項数) */
	public int countTerms(long[][][][] a) {
		int res = 0;
		for (long[][][] b : a) {
			for (long[][] c : b) {
				for (long[] d : c) {
					for (long val : d) {
						if (val != 0) res++;
					}
				}
			}
		}
		return res;
	}

	/**
	 * 多項式 f を点 (x, y, z, w) で評価する。
	 * @param f 多項式
	 * @param x 評価点 x
	 * @param y 評価点 y
	 * @param z 評価点 z
	 * @param w 評価点 w
	 * @return f(x, y, z, w)
	 */
	public long eval(long[][][][] f, long x, long y, long z, long w) {
		long res = 0;
		for (int i = f.length - 1; i >= 0; i--) {
			res = (res * x + poly3d.eval(f[i], y, z, w)) % mod;
		}
		return res;
	}

	/**
	 * primitiveな多項式の既約性を判定する。
	 * primitiveでない多項式を与えた場合の動作は保証されない。
	 * ヒルベルトの既約性定理に基づき、ランダムな1変数への射影を用いて判定する。
	 * trueならば既約。falseならば不明。
	 * @param f 判定対象の多項式
	 * @return 既約であることが確定の場合 true
	 */
	public boolean isIrreducibleHeuristicForPrimitive(long[][][][] f) {
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0 && degW(f) <= 0) return false;
		f = monic(f);
		int dx = degX(f);
		Random rnd = new Random(0);
		for (int t = 0; t < 5; t++) {
			long cy = poly1d.fp.reduce(rnd.nextLong());
			long cz = poly1d.fp.reduce(rnd.nextLong());
			long cw = poly1d.fp.reduce(rnd.nextLong());
			long[] f_xyzw = new long[dx + 1];
			for (int i = 0; i <= dx; i++) {
				f_xyzw[i] = poly3d.eval(f[i], cy, cz, cw);
			}
			if (poly1d.deg(f_xyzw) != dx) continue;
			if (poly1d.isIrreducible(f_xyzw)) return true;
		}
		return false;
	}

	/**
	 * 多項式の末尾の零項を取り除き、サイズを正規化する。
	 * @param a 多項式
	 * @return 正規化された多項式
	 */
	public long[][][][] resize(long[][][][] a) {
		int dx = degX(a);
		if (dx == -1) return zero();
		long[][][][] res = new long[dx + 1][][][];
		for (int i = 0; i <= dx; i++) {
			res[i] = poly3d.resize(a[i]);
		}
		return res;
	}

	/**
	 * 多項式の辞書順最大の主係数を返す。
	 * @param a 多項式
	 * @return 主係数
	 */
	public long lead(long[][][][] a) {
		int dx = degX(a);
		if (dx == -1) return 0;
		return poly3d.lead(a[dx]);
	}

	/**
	 * 多項式を monic（主係数が 1）にする。
	 * @param a 多項式
	 * @return monic 化された多項式
	 */
	public long[][][][] monic(long[][][][] a) {
		a = resize(a);
		long leadVal = lead(a);
		if (leadVal == 0) return a;
		long inv = mo.inv(leadVal);

		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][][];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = new long[a[i][j].length][];
				for (int k = 0; k < a[i][j].length; k++) {
					res[i][j][k] = new long[a[i][j][k].length];
					for (int l = 0; l < a[i][j][k].length; l++)
						res[i][j][k][l] = a[i][j][k][l] * inv % mod;
				}
			}
		}
		return res;
	}

	/**
	 * x に関する content（係数多項式の GCD）を計算する。
	 * @param a 多項式
	 * @return content（3変数多項式）
	 */
	public long[][][] contentX(long[][][][] a) {
		long[][][] g = poly3d.zero();
		for (long[][][] mat : a)
			g = poly3d.gcd(g, mat);
		return g;
	}

	/**
	 * 多項式の各係数を指定した 3変数多項式 p で割る。
	 * @param a 多項式
	 * @param p 割る多項式
	 * @return 各係数を p で割った多項式
	 */
	public long[][][][] lexdivByPolyYZW(long[][][][] a, long[][][] p) {
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly3d.lexdiv(a[i], p);
		return res;
	}

	/**
	 * 多項式の各係数に指定した 3変数多項式 p を掛ける。
	 * @param a 多項式
	 * @param p 掛ける多項式
	 * @return 各係数に p を掛けた多項式
	 */
	public long[][][][] mulByPoly3D(long[][][][] a, long[][][] p) {
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly3d.mul(a[i], p);
		return res;
	}

	/**
	 * 2つの多項式の最大公約数 (GCD) を計算する。
	 * 結果は monic（主係数が 1）になるとは限りませんが、monic 化した方がいいでしょう。
	 *
	 * @param a 多項式1
	 * @param b 多項式2
	 * @return monic な GCD
	 */
	@Override
	public long[][][][] gcd(long[][][][] a, long[][][][] b) {
		a = resize(a);
		b = resize(b);
		if (degX(a) == -1)
			return monic(b);
		if (degX(b) == -1)
			return monic(a);

		long[][][] contA = contentX(a);
		long[][][] contB = contentX(b);
		long[][][] gCont = poly3d.gcd(contA, contB);

		long[][][][] primA = lexdivByPolyYZW(a, contA);
		long[][][][] primB = lexdivByPolyYZW(b, contB);

		long[][][][] resPrim = gcdZippel(primA, primB);
		if (resPrim == null) {
			var field = new FractionFieldStrategy<>(poly3d);
			var strategy = new PolynomialEuclideanStrategy<>(field);

			FractionFieldElement<long[][][]>[] fA = toFractionArray(primA);
			FractionFieldElement<long[][][]>[] fB = toFractionArray(primB);
			FractionFieldElement<long[][][]>[] fG = strategy.gcd(fA, fB);

			resPrim = fromFractionArray(fG);
		}
		return monic(mulByPoly3D(resPrim, gCont));
	}

	/**
	 * Zippelの確率的モジュラーGCDアルゴリズムを用いてGCDを計算する。
	 */
	public long[][][][] gcdZippel(long[][][][] f, long[][][][] g) {
		f = resize(f);
		g = resize(g);
		if (degX(f) == -1) return monic(g);
		if (degX(g) == -1) return monic(f);
		if (degX(f) < degX(g)) { long[][][][] t = f; f = g; g = t; }

		long[][][] lcf = f[degX(f)];
		long[][][] lcg = g[degX(g)];
		
		Random rnd = new Random(0);
		for (int attempt = 0; attempt < 20; attempt++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			long rz = poly1d.fp.reduce(rnd.nextLong());
			long rw = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz, rw) == 0 || poly3d.eval(lcg, ry, rz, rw) == 0) continue;
			long[] f_r = new long[f.length];
			for (int i = 0; i < f.length; i++) f_r[i] = poly3d.eval(f[i], ry, rz, rw);
			long[] g_r = new long[g.length];
			for (int i = 0; i < g.length; i++) g_r[i] = poly3d.eval(g[i], ry, rz, rw);

			long[] gcd_r = poly1d.gcd(f_r, g_r);
			int[] skeleton = new int[gcd_r.length];
			int skelSize = 0;
			for (int i = 0; i < gcd_r.length; i++) if (gcd_r[i] != 0) skeleton[skelSize++] = i;
			skeleton = Arrays.copyOf(skeleton, skelSize);

			long[][][][] G = new long[skeleton[skelSize - 1] + 1][][][];
			boolean success = true;
			for (int i : skeleton) {
				G[i] = interpolateZippel(f, g, i, ry, rz, rw, gcd_r[i], skeleton);
				if (G[i] == null) { success = false; break; }
			}
			if (!success) continue;

			long[][][][] G_full = new long[G.length][][][];
			for (int i = 0; i < G.length; i++) G_full[i] = G[i] == null ? poly3d.zero() : G[i];
			long[][][][] res = resize(G_full);
			try {
				if (isDivisible(f, res) && isDivisible(g, res)) return monic(res);
			} catch (Exception e) {}
		}
		return null;
	}


	/**
	 * 求める最大公約数を G(x, y, z, w) = gcd(f, g) とし、その x の i 次の係数を G_i(y, z, w) とする。
	 * 係数多項式 G_i(y, z, w) を Zippel のスパース補間アルゴリズムを用いて復元する。
	 * 事前に得られた 1変数の GCD から、G_i が非ゼロとなる次数（スケルトン）が既知であることを利用し、
	 * 少ない評価点数で効率的に多変数多項式を補間する。
	 *
	 * @param f 入力多項式 f
	 * @param g 入力多項式 g
	 * @param xIdx 補間対象とする x の次数 i
	 * @param ry 起点となる評価点 y
	 * @param rz 起点となる評価点 z
	 * @param rw 起点となる評価点 w
	 * @param targetVal 既知の値 G_i(ry, rz, rw)
	 * @param skeleton GCD のスケルトン。特定の評価点 (ry, rz, rw) において
	 *                 gcd(f(x, ry, rz, rw), g(x, ry, rz, rw)) を計算した際に、
	 *                 係数が非ゼロとなる x の次数の集合。
	 * @return 補間された 3変数多項式 G_i(y, z, w)。補間に失敗した場合は null
	 *
	 * <p>計算量: O(degW * interpolateYZwithFixedW)
	 */
	private long[][][] interpolateZippel(long[][][][] f, long[][][][] g, int xIdx, long ry, long rz, long rw, long targetVal, int[] skeleton) {
		long[][][] lcf = f[degX(f)];
		long[][][] lcg = g[degX(g)];
		Random rnd = new Random(0);

		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<Long> valuesY = new ArrayList<>();
		pointsY.add(ry);
		valuesY.add(targetVal);

		long[] currentCoeffPolyY = {targetVal};
		int retryY = 0;
		while (true) {
			if (++retryY > 100) return null;
			long ry2 = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry2, rz, rw) == 0 || poly3d.eval(lcg, ry2, rz, rw) == 0) {
				continue;
			}

			long[] f_ry2 = new long[f.length];
			for (int j = 0; j < f.length; j++) f_ry2[j] = poly3d.eval(f[j], ry2, rz, rw);
			long[] g_ry2 = new long[g.length];
			for (int j = 0; j < g.length; j++) g_ry2[j] = poly3d.eval(g[j], ry2, rz, rw);

			long[] gcd_ry2 = poly1d.gcd(f_ry2, g_ry2);
			if (poly1d.deg(gcd_ry2) != skeleton[skeleton.length-1]) {
				continue;
			}

			pointsY.add(ry2);
			valuesY.add(xIdx < gcd_ry2.length ? gcd_ry2[xIdx] : 0L);

			long[] p_arr = new long[pointsY.size()];
			long[] v_arr = new long[valuesY.size()];
			for(int j=0; j<pointsY.size(); j++) { p_arr[j] = pointsY.get(j); v_arr[j] = valuesY.get(j); }
			long[] nextCoeffPolyY = poly1d.interpolate(p_arr, v_arr);

			if (Arrays.equals(nextCoeffPolyY, currentCoeffPolyY)) break;
			currentCoeffPolyY = nextCoeffPolyY;
			if (pointsY.size() > degY(f) + 1) break;
		}

		ArrayList<Long> pointsZ = new ArrayList<>();
		ArrayList<long[]> valuesZ = new ArrayList<>();
		pointsZ.add(rz);
		valuesZ.add(currentCoeffPolyY);

		long[][] currentCoeffPolyYZ = {currentCoeffPolyY};
		int retryZ = 0;
		while (true) {
			if (++retryZ > 100) return null;
			long rz2 = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz2, rw) == 0 || poly3d.eval(lcg, ry, rz2, rw) == 0) {
				continue;
			}

			long[] coeffY_rz2 = interpolateYwithFixedZW(f, g, xIdx, rz2, rw, skeleton);
			if (coeffY_rz2 == null) {
				continue;
			}

			pointsZ.add(rz2);
			valuesZ.add(coeffY_rz2);

			long[] p_arr = new long[pointsZ.size()];
			for(int j=0; j<pointsZ.size(); j++) p_arr[j] = pointsZ.get(j);

			int maxDegY = 0;
			for (long[] v : valuesZ) maxDegY = Math.max(maxDegY, v.length - 1);
			long[][] nextCoeffPolyYZ = new long[maxDegY + 1][];
			for (int j = 0; j <= maxDegY; j++) {
				long[] zValues = new long[pointsZ.size()];
				for (int k = 0; k < pointsZ.size(); k++) zValues[k] = (j < valuesZ.get(k).length) ? valuesZ.get(k)[j] : 0L;
				nextCoeffPolyYZ[j] = poly1d.interpolate(p_arr, zValues);
			}

			if (poly3d.poly2d.equals(nextCoeffPolyYZ, currentCoeffPolyYZ)) break;
			currentCoeffPolyYZ = nextCoeffPolyYZ;
			if (pointsZ.size() > degZ(f) + 1) break;
		}

		ArrayList<Long> pointsW = new ArrayList<>();
		ArrayList<long[][]> valuesW = new ArrayList<>();
		pointsW.add(rw);
		valuesW.add(currentCoeffPolyYZ);

		long[][][] currentCoeffPolyYZW = {currentCoeffPolyYZ};
		int retryW = 0;
		while (true) {
			if (++retryW > 100) return null;
			long rw2 = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz, rw2) == 0 || poly3d.eval(lcg, ry, rz, rw2) == 0) {
				continue;
			}

			long[][] coeffYZ_rw2 = interpolateYZwithFixedW(f, g, xIdx, rw2, skeleton);
			if (coeffYZ_rw2 == null) {
				continue;
			}

			pointsW.add(rw2);
			valuesW.add(coeffYZ_rw2);

			long[] p_arr = new long[pointsW.size()];
			for(int j=0; j<pointsW.size(); j++) p_arr[j] = pointsW.get(j);

			int maxDegY = 0, maxDegZ = 0;
			for (long[][] v : valuesW) {
				maxDegY = Math.max(maxDegY, v.length - 1);
				for (long[] row : v) maxDegZ = Math.max(maxDegZ, row.length - 1);
			}
			long[][][] nextCoeffPolyYZW = new long[maxDegY + 1][maxDegZ + 1][];
			for (int j = 0; j <= maxDegY; j++) {
				for (int k = 0; k <= maxDegZ; k++) {
					long[] wValues = new long[pointsW.size()];
					for (int l = 0; l < pointsW.size(); l++) {
						wValues[l] = (j < valuesW.get(l).length && k < valuesW.get(l)[j].length) ? valuesW.get(l)[j][k] : 0L;
					}
					nextCoeffPolyYZW[j][k] = poly1d.interpolate(p_arr, wValues);
				}
			}

			if (poly3d.equals(nextCoeffPolyYZW, currentCoeffPolyYZW)) break;
			currentCoeffPolyYZW = nextCoeffPolyYZW;
			if (pointsW.size() > degW(f) + 1) break;
		}
		return currentCoeffPolyYZW;
	}

	private long[][] interpolateYZwithFixedW(long[][][][] f, long[][][][] g, int xIdx, long rw, int[] skeleton) {
		long[][][] lcf = f[degX(f)];
		long[][][] lcg = g[degX(g)];
		Random rnd = new Random(0);
		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<long[]> valuesY = new ArrayList<>();

		long ry0 = poly1d.fp.reduce(rnd.nextLong());
		int retryW0 = 0;
		long rz_avoid = rnd.nextLong(1, mod);
		while (poly3d.eval(lcf, ry0, rz_avoid, rw) == 0 || poly3d.eval(lcg, ry0, rz_avoid, rw) == 0) {
			if (++retryW0 > 100) return null;
			ry0 = poly1d.fp.reduce(rnd.nextLong());
			rz_avoid = rnd.nextLong(1, mod);
		}

		long[][] currentCoeffPolyYZ = null;
		for (int t=0; t < degY(f) + 2; t++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz_avoid, rw) == 0 || poly3d.eval(lcg, ry, rz_avoid, rw) == 0) {
				continue;
			}

			long[] coeffZ_ry = interpolateZwithFixedYW(f, g, xIdx, ry, rw, skeleton);
			if (coeffZ_ry == null) {
				continue;
			}

			pointsY.add(ry);
			valuesY.add(coeffZ_ry);

			long[] p_arr = new long[pointsY.size()];
			for(int j=0; j<pointsY.size(); j++) p_arr[j] = pointsY.get(j);

			int maxDegZ = 0;
			for (long[] v : valuesY) maxDegZ = Math.max(maxDegZ, v.length - 1);
			long[][] nextCoeffPolyYZ = new long[pointsY.size()][maxDegZ + 1];
			for (int k = 0; k <= maxDegZ; k++) {
				long[] yValues = new long[pointsY.size()];
				for (int l = 0; l < pointsY.size(); l++) yValues[l] = (k < valuesY.get(l).length) ? valuesY.get(l)[k] : 0L;
				long[] interp = poly1d.interpolate(p_arr, yValues);
				for (int l = 0; l < interp.length; l++) nextCoeffPolyYZ[l][k] = interp[l];
			}

			if (currentCoeffPolyYZ != null && poly3d.poly2d.equals(nextCoeffPolyYZ, currentCoeffPolyYZ)) return currentCoeffPolyYZ;
			currentCoeffPolyYZ = nextCoeffPolyYZ;
		}
		return currentCoeffPolyYZ;
	}

	/**
	 * 求める最大公約数を G(x, y, z, w) = gcd(f, g) とし、その x の i 次の係数を G_i(y, z, w) とする。
	 * y と w をそれぞれ ry, rw に固定した状態で、1変数多項式 G_i(ry, z, rw) を補間する。
	 *
	 * @param f 入力多項式 f
	 * @param g 入力多項式 g
	 * @param xIdx 補間対象とする x の次数 i
	 * @param ry 固定する y の値
	 * @param rw 固定する w の値
	 * @param skeleton GCD のスケルトン。特定の評価点において 1変数 GCD の係数が非ゼロとなる x の次数の集合。
	 * @return 補間された 1変数多項式 G_i(z)。補間に失敗した場合は null
	 */
	private long[] interpolateZwithFixedYW(long[][][][] f, long[][][][] g, int xIdx, long ry, long rw, int[] skeleton) {
		long[][][] lcf = f[degX(f)];
		long[][][] lcg = g[degX(g)];
		Random rnd = new Random(0);
		ArrayList<Long> pointsZ = new ArrayList<>();
		ArrayList<Long> valuesZ = new ArrayList<>();

		long[] currentCoeffPolyZ = null;
		for (int t=0; t < degZ(f) + 2; t++) {
			long rz = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz, rw) == 0 || poly3d.eval(lcg, ry, rz, rw) == 0) {
				continue;
			}

			long[] f_r = new long[f.length];
			for (int i = 0; i < f.length; i++) f_r[i] = poly3d.eval(f[i], ry, rz, rw);
			long[] g_r = new long[g.length];
			for (int i = 0; i < g.length; i++) g_r[i] = poly3d.eval(g[i], ry, rz, rw);

			long[] gcd_r = poly1d.gcd(f_r, g_r);
			if (poly1d.deg(gcd_r) != skeleton[skeleton.length-1]) {
				continue;
			}

			pointsZ.add(rz);
			valuesZ.add(xIdx < gcd_r.length ? gcd_r[xIdx] : 0L);

			long[] p_arr = new long[pointsZ.size()];
			long[] v_arr = new long[valuesZ.size()];
			for(int j=0; j<pointsZ.size(); j++) { p_arr[j] = pointsZ.get(j); v_arr[j] = valuesZ.get(j); }
			long[] nextCoeffPolyZ = poly1d.interpolate(p_arr, v_arr);

			if (currentCoeffPolyZ != null && Arrays.equals(nextCoeffPolyZ, currentCoeffPolyZ)) return currentCoeffPolyZ;
			currentCoeffPolyZ = nextCoeffPolyZ;
		}
		return currentCoeffPolyZ;
	}
	
	/**
	 * 求める最大公約数を G(x, y, z, w) = gcd(f, g) とし、その x の i 次の係数を G_i(y, z, w) とする。
	 * z と w をそれぞれ rz, rw に固定した状態で、1変数多項式 G_i(y, rz, rw) を補間する。
	 *
	 * @param f 入力多項式 f
	 * @param g 入力多項式 g
	 * @param xIdx 補間対象とする x の次数 i
	 * @param rz 固定する z の値
	 * @param rw 固定する w の値
	 * @param skeleton GCD のスケルトン。特定の評価点において 1変数 GCD の係数が非ゼロとなる x の次数の集合。
	 * @return 補間された 1変数多項式 G_i(y)。補間に失敗した場合は null
	 */
	private long[] interpolateYwithFixedZW(long[][][][] f, long[][][][] g, int xIdx, long rz, long rw, int[] skeleton) {
		long[][][] lcf = f[degX(f)];
		long[][][] lcg = g[degX(g)];
		Random rnd = new Random(0);
		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<Long> valuesY = new ArrayList<>();

		long[] currentCoeffPolyY = null;
		for (int t=0; t < degY(f) + 2; t++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			if (poly3d.eval(lcf, ry, rz, rw) == 0 || poly3d.eval(lcg, ry, rz, rw) == 0) {
				continue;
			}

			long[] f_ry = new long[f.length];
			for (int i = 0; i < f.length; i++) f_ry[i] = poly3d.eval(f[i], ry, rz, rw);
			long[] g_ry = new long[g.length];
			for (int i = 0; i < g.length; i++) g_ry[i] = poly3d.eval(g[i], ry, rz, rw);

			long[] gcd_ry = poly1d.gcd(f_ry, g_ry);
			if (poly1d.deg(gcd_ry) != skeleton[skeleton.length-1]) continue;

			pointsY.add(ry);
			valuesY.add(xIdx < gcd_ry.length ? gcd_ry[xIdx] : 0L);

			long[] p_arr = new long[pointsY.size()];
			long[] v_arr = new long[valuesY.size()];
			for(int j=0; j<pointsY.size(); j++) { p_arr[j] = pointsY.get(j); v_arr[j] = valuesY.get(j); }
			long[] nextCoeffPolyY = poly1d.interpolate(p_arr, v_arr);

			if (currentCoeffPolyY != null && Arrays.equals(nextCoeffPolyY, currentCoeffPolyY)) return currentCoeffPolyY;
			currentCoeffPolyY = nextCoeffPolyY;
		}
		return currentCoeffPolyY;
	}

	private boolean isDivisible(long[][][][] a, long[][][][] b) {
		if (degX(b) == -1) return false;
		try {
			long[][][][] q = lexdiv(a, b);
			return equals(mul(b, q), a);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 多項式を有理関数係数の 1変数多項式（Fraction の配列）に変換する。
	 */
	private FractionFieldElement<long[][][]>[] toFractionArray(long[][][][] a) {
		@SuppressWarnings("unchecked")
		FractionFieldElement<long[][][]>[] res = new FractionFieldElement[a.length];
		var field = new FractionFieldStrategy<>(poly3d);
		for (int i = 0; i < a.length; i++)
			res[i] = field.of(poly3d.resize(a[i]), poly3d.one());
		return res;
	}

	/**
	 * 有理関数係数の 1変数多項式を、元の多項式環の表現（原始多項式）に変換する。
	 */
	private long[][][][] fromFractionArray(FractionFieldElement<long[][][]>[] a) {
		// 分母の最小公倍数を掛けて整式に戻す
		long[][][] commonDen = poly3d.one();
		for (var f : a) {
			long[][][] g = poly3d.gcd(commonDen, f.den());
			commonDen = poly3d.mul(commonDen, poly3d.lexdiv(f.den(), g));
		}
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly3d.mul(a[i].num(), poly3d.lexdiv(commonDen, a[i].den()));
		// 原始多項式にする（content で割る）
		long[][][] c = contentX(res);
		return lexdivByPolyYZW(res, c);
	}

	/**
	 * 多項式の除算を行い、商を返す。
	 * a が b で割り切れることが保証されている必要がある。
	 * 内部で Kronecker 置換を用いて 1変数多項式の除算に帰着させている。
	 *
	 * @param a 被除数
	 * @param b 除数
	 * @return 商 a / b
	 * @throws ArithmeticException 0多項式で割ろうとした場合
	 */
	@Override
	public long[][][][] exactDiv(long[][][][] a, long[][][][] b) {
		return lexdiv(a, b);
	}

	/**
	 * 辞書式順序 (x > y > z > w) に基づく多項式の除算を行い、商と余りを返す。
	 * Kronecker 置換を用いて 1 変数多項式の除算に帰着させることで、一意な商と余りを得る。
	 */
	public DivModResult lexdivmod(long[][][][] a, long[][][][] b) {
		a = resize(a); b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		int dxA = degX(a);
		if (dxA < dxB) return new DivModResult(zero(), a);
		int dyA = degY(a), dyB = degY(b);
		int dzA = degZ(a), dzB = degZ(b);
		int dwA = degW(a), dwB = degW(b);
		int sy = Math.max(dyA, dyB) + (dxA - dxB + 1) * dyB + 1;
		int sz = Math.max(dzA, dzB) + (dxA - dxB + 1) * dzB + 1;
		int sw = Math.max(dwA, dwB) + (dxA - dxB + 1) * dwB + 1;
		long[] fa = flattenKronecker(a, sy, sz, sw);
		long[] fb = flattenKronecker(b, sy, sz, sw);
		PolynomialFpDynamic.DivModResult res = poly1d.divmod(fa, fb);
		long[][][][] q = unflattenKronecker(res.q, sy, sz, sw, Math.max(0, dxA - dxB), sy - 1, sz - 1, sw - 1);
		long[][][][] r = unflattenKronecker(res.r, sy, sz, sw, dxB, sy - 1, sz - 1, sw - 1);
		return new DivModResult(q, r);
	}

	/** 辞書式順序に基づく商を返す。 */
	public long[][][][] lexdiv(long[][][][] a, long[][][][] b) {
		a = resize(a);
		b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		if (dxB == 0 && degY(b) == 0 && degZ(b) == 0 && degW(b) == 0) {
			long inv = mo.inv(b[0][0][0][0]);
			long[][][][] res = new long[a.length][][][];
			for (int i = 0; i < a.length; i++) {
				res[i] = new long[a[i].length][][];
				for (int j = 0; j < a[i].length; j++) {
					res[i][j] = new long[a[i][j].length][];
					for (int k = 0; k < a[i][j].length; k++) res[i][j][k] = poly1d.mul(a[i][j][k], inv);
				}
			}
			return resize(res);
		}
		return lexdivmod(a, b).q;
	}

	/** 辞書式順序に基づく余りを返す。 */
	public long[][][][] lexmod(long[][][][] a, long[][][][] b) {
		return lexdivmod(a, b).r;
	}

	public static class DivModResult {
		public long[][][][] q;
		public long[][][][] r;
		public DivModResult(long[][][][] q, long[][][][] r) {
			this.q = q;
			this.r = r;
		}
	}

	/**
	 * 多項式の因子とその重複度を保持するクラス。
	 */
	public static class Factor {
		/** 既約因子 */
		public long[][][][] factor;
		/** 重複度 */
		public int multiplicity;
		public Factor(long[][][][] factor, int multiplicity) {
			this.factor = factor;
			this.multiplicity = multiplicity;
		}
	}

	/**
	 * 多項式の因数分解結果を保持するレコード。
	 * @param leadingCoeff 主係数
	 * @param factors 既約因子の配列（すべて monic）
	 */
	public record FactorResult4D(long leadingCoeff, Factor[] factors) {}

	/**
	 * Wang の主係数トリックの前進変換結果を保持する。
	 * @param leadingCoeff {@code L(y,z,w)=[x^d]F(x,y,z,w)}
	 * @param factorCount 評価点で得た因子数 {@code k}
	 * @param mainDegree {@code d=deg_x F}
	 * @param monicPolynomial {@code F^*(X,y,z,w)=L(y,z,w)^{d-1}F(X/L(y,z,w),y,z,w)}
	 */
	public record WangTransform4D(long[][][] leadingCoeff, int factorCount, int mainDegree, long[][][][] monicPolynomial) {}

	/**
	 * 平方因子分解 (square-free decomposition) を行う。
	 * <p>
	 * SymPy の dmp_sqf_list と同様に、x を主変数とみなした Yun 法を用いる。
	 * 係数環（y,z,w の3変数多項式）側の content も再帰的に分解し、同じ重複度の因子をマージする。
	 * </p>
	 *
	 * @param inputf 分解対象多項式
	 * @return (既約とは限らない) square-free 因子と重複度
	 */
	public Factor[] squareFreeDecomposition(long[][][][] inputf) {
		long[][][][] f = resize(inputf);
		if (degX(f) == -1) return new Factor[0];

		ArrayList<Factor> result = new ArrayList<>();

		// x に関する primitive part を取り出す
		long[][][] content = contentX(f);
		long[][][][] prim = lexdivByPolyYZW(f, content);

		// primitive part の square-free decomposition (Yun)
		ArrayList<Factor> primSqf = squareFreePrimitiveByYun(prim);
		result.addAll(primSqf);

		// content 側は「完全因数分解」ではなく square-free decomposition を再帰する
		PolynomialFpDynamic3D.SquareFreeFactor[] contentSqf = poly3d.squareFreeDecomposition(content);
		for (PolynomialFpDynamic3D.SquareFreeFactor cf : contentSqf) {
			long[][][][] lifted = new long[][][][] { cf.factor };
			boolean merged = false;
			for (Factor ex : result) {
				if (ex.multiplicity == cf.multiplicity) {
					ex.factor = resize(mul(ex.factor, lifted));
					merged = true;
					break;
				}
			}
			if (!merged) result.add(new Factor(resize(lifted), cf.multiplicity));
		}

		result.removeIf(e -> degX(e.factor) == -1);
		result.sort((a, b) -> Integer.compare(a.multiplicity, b.multiplicity));
		return result.toArray(new Factor[0]);
	}


	/**
	 * x-primitive な多項式に対する Yun 法。
	 */
	private ArrayList<Factor> squareFreePrimitiveByYun(long[][][][] primitive) {
		ArrayList<Factor> out = new ArrayList<>();
		long[][][][] f = monic(primitive);
		if (degX(f) <= 0) {
			if (degX(f) == 0 && degY(f) == 0 && degZ(f) == 0 && degW(f) == 0 && f[0][0][0][0] != 1) {
				// 定数のみ（monic化済みなので通常は 1）
				out.add(new Factor(f, 1));
			}
			return out;
		}

		long[][][][] fp = diffX(f);
		long[][][][] g = gcd(f, fp);
		long[][][][] p = lexdiv(f, g);
		long[][][][] q = lexdiv(fp, g);

		for (int i = 1; ; i++) {
			long[][][][] dp = diffX(p);
			long[][][][] h = sub(q, dp);
			h = resize(h);
			if (degX(h) == -1) {
				if (degX(p) > 0) out.add(new Factor(monic(p), i));
				break;
			}
			long[][][][] gi = gcd(p, h);
			if (degX(gi) > 0) out.add(new Factor(monic(gi), i));
			p = lexdiv(p, gi);
			q = lexdiv(h, gi);
		}
		return out;
	}

	/**
	 * x に関する形式微分。
	 */
	private long[][][][] diffX(long[][][][] a) {
		a = resize(a);
		if (a.length <= 1) return zero();
		long[][][][] res = new long[a.length - 1][][][];
		for (int i = 1; i < a.length; i++) {
			long coef = i % mod;
			res[i - 1] = poly3d.mul(a[i], coef);
		}
		return resize(res);
	}


	/**
	 * 多項式を既約分解する。
	 *
	 * @param inputf 因数分解する多項式
	 * @return 既約因子の配列
	 */
	public FactorResult4D factor(long[][][][] inputf) {
		inputf = resize(inputf);
		if (degX(inputf) == -1) return new FactorResult4D(0, new Factor[0]);
		long coef = lead(inputf);
		if (degX(inputf) == 0 && degY(inputf) == 0 && degZ(inputf) == 0 && degW(inputf) == 0) return new FactorResult4D(coef, new Factor[0]);

		ArrayList<Factor> ret = new ArrayList<>();
		long[][][] content = contentX(inputf);
		PolynomialFpDynamic3D.FactorResult3D contentFR = poly3d.factor(content);
		for (PolynomialFpDynamic3D.Factor f3d : contentFR.factors()) {
			ret.add(new Factor(new long[][][][] { f3d.factor }, f3d.multiplicity));
		}

		long[][][][] primitive = lexdivByPolyYZW(inputf, content);
		if (degX(primitive) > 0 && isIrreducibleHeuristicForPrimitive(primitive)) {
			ret.add(new Factor(monic(primitive), 1));
			return new FactorResult4D(coef, ret.toArray(new Factor[ret.size()]));
		}
		ArrayList<Factor> sqfFactors = squareFreePrimitiveByYun(primitive);
		for (Factor sqf : sqfFactors) {
			Factor[] factors = factorByWang(sqf.factor);
			for (Factor f : factors) {
				ret.add(new Factor(f.factor, f.multiplicity * sqf.multiplicity));
			}
		}
		return new FactorResult4D(coef, ret.toArray(new Factor[ret.size()]));
	}

	/**
	 * Wang の主係数トリックにより {@code F(x,y,z,w)} を {@code X} に関して monic にする。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code F != 0}, {@code k >= 0}, {@code L(y,z,w)=[x^d]F != 0}。</li>
	 * <li>事後条件: {@code F^*=sum_{0<=i<d} [x^i]F L^{d-1-i}X^i + X^d}。</li>
	 * <li>事後条件: {@code [X^d]F^*=1}。</li>
	 * <li>副作用: なし。返値は入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code F=0} または {@code k<0} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 入力が係数配列表現でない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(d * (Pow3D + Mul3D))。
	 */
	public WangTransform4D wangTrickForward(long[][][][] F, int k) {
		if (k < 0) throw new IllegalArgumentException("factor count must be non-negative");
		F = resize(F);
		int d = degX(F);
		if (d == -1) throw new IllegalArgumentException("zero polynomial has no leading coefficient");
		long[][][] L = poly3d.resize(F[d]);
		long[][][][] monicF = new long[d + 1][][][];
		for (int i = 0; i < d; i++) {
			int exponent = d - 1 - i;
			long[][][] scale = exponent == 0 ? poly3d.one() : powFull3D(L, exponent);
			monicF[i] = poly3d.mul(F[i], scale);
		}
		monicF[d] = poly3d.one();
		return new WangTransform4D(ArrayUtils.copy(L), k, d, resize(monicF));
	}

	/**
	 * Wang の主係数トリックで monic 化された因子を逆変換し、{@code x} に関する原始部分を返す。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code L(y,z,w) != 0}, {@code monicFactors != null}。</li>
	 * <li>事後条件: 返値 {@code H_i=pp_x(G_i(Lx,y,z,w))}。</li>
	 * <li>副作用: なし。返値は入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code L=0} または {@code monicFactors=null} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 係数が content で割り切れない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(s * (d_i * Mul3D + Gcd3D))。
	 */
	public Factor[] wangTrickBackward(long[][][] L, Factor[] monicFactors) {
		L = poly3d.resize(L);
		if (poly3d.degX(L) == -1) throw new IllegalArgumentException("leading coefficient must be non-zero");
		if (monicFactors == null) throw new IllegalArgumentException("monicFactors must not be null");
		Factor[] res = new Factor[monicFactors.length];
		for (int idx = 0; idx < monicFactors.length; idx++) res[idx] = new Factor(monic(primitivePartX(substituteWangX(monicFactors[idx].factor, L))), monicFactors[idx].multiplicity);
		return res;
	}

	/** {@code a^n} を3変数多項式として返す。未テスト。計算量: O(log n * Mul3D) */
	private long[][][] powFull3D(long[][][] a, int n) {
		long[][][] ret = poly3d.one(), b = poly3d.resize(a);
		while (n != 0) {
			if ((n & 1) != 0) ret = poly3d.mul(ret, b);
			n >>= 1;
			if (n != 0) b = poly3d.mul(b, b);
		}
		return ret;
	}

	/** {@code G(L(y,z,w)x,y,z,w)} を返す。未テスト。計算量: O(d * Mul3D) */
	private long[][][][] substituteWangX(long[][][][] G, long[][][] L) {
		G = resize(G);
		int d = degX(G);
		if (d == -1) return zero();
		long[][][][] res = new long[d + 1][][][];
		long[][][] pow = poly3d.one();
		for (int i = 0; i <= d; i++) {
			res[i] = poly3d.mul(G[i], pow);
			pow = poly3d.mul(pow, L);
		}
		return resize(res);
	}

	/** {@code pp_x(a)=a/content_x(a)} を返す。未テスト。計算量: O(Gcd3D * deg_x a + Div3D * deg_x a) */
	private long[][][][] primitivePartX(long[][][][] a) {
		a = resize(a);
		if (degX(a) == -1) return zero();
		long[][][] content = contentX(a);
		if (poly3d.degX(content) == -1 || poly3d.equals(content, poly3d.one())) return resize(a);
		return resize(lexdivByPolyYZW(a, content));
	}

	/** Wang の主係数トリックとHensel持ち上げで primitive かつ square-free な多項式を既約分解する。未テスト。計算量: O(Factor3D + Lift4D) */
	public Factor[] factorByWang(long[][][][] inputf) {
		long[][][][] f = primitivePartX(inputf);
		int dx = degX(f);
		if (dx == -1) return new Factor[0];
		if (dx <= 0) {
			PolynomialFpDynamic3D.FactorResult3D fr = poly3d.factor(f[0]);
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) res[i] = new Factor(new long[][][][] { fr.factors()[i].factor }, fr.factors()[i].multiplicity);
			return res;
		}
		if (degW(f) <= 0) {
			PolynomialFpDynamic3D.FactorResult3D fr = poly3d.factor(evalW(f, 0));
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) res[i] = new Factor(embedPoly3DAtDegW(fr.factors()[i].factor, 0, 1), fr.factors()[i].multiplicity);
			return res;
		}
		WangTransform4D transformed = wangTrickForward(f, 0);
		long[][][][] monicF = transformed.monicPolynomial();
		long a = findGoodEvaluationPointW(monicF);
		PolynomialFpDynamic3D.FactorResult3D fr = poly3d.factor(evalW(monicF, a));
		transformed = wangTrickForward(f, fr.factors().length);
		Factor[] lifted = liftFactors(transformed.monicPolynomial(), fr.factors(), a);
		Factor[] recombined = recombine(transformed.monicPolynomial(), lifted);
		return wangTrickBackward(transformed.leadingCoeff(), recombined);
	}

	private Factor[] recombine(long[][][][] f, Factor[] lifted) {
		if (lifted.length <= 1) return lifted;
		ArrayList<long[][][][]> currentFactors = new ArrayList<>();
		for (Factor fact : lifted) currentFactors.add(fact.factor);

		ArrayList<long[][][][]> result = new ArrayList<>();
		long[][][][] remainingF = f;

		for (int sz = 1; sz <= currentFactors.size() / 2; sz++) {
			boolean[] used = new boolean[currentFactors.size()];
			for (int[] indices : Itertools.combinations(currentFactors.size(), sz)) {
				boolean anyUsed = false;
				for (int idx : indices) if (used[idx]) anyUsed = true;
				if (anyUsed) continue;

				long[][][][] prod = one();
				for (int idx : indices) prod = mul(prod, currentFactors.get(idx));
				if (degX(prod) <= degX(remainingF) && degY(prod) <= degY(remainingF) && degZ(prod) <= degZ(remainingF) && degW(prod) <= degW(remainingF) && isDivisible(remainingF, prod)) {
					result.add(prod);
					remainingF = lexdiv(remainingF, prod);
					for (int idx : indices) used[idx] = true;
				}
			}
			ArrayList<long[][][][]> nextFactors = new ArrayList<>();
			for (int i = 0; i < currentFactors.size(); i++) if (!used[i]) nextFactors.add(currentFactors.get(i));
			currentFactors = nextFactors;
		}
		if (degX(remainingF) > 0 || degY(remainingF) > 0 || degZ(remainingF) > 0 || degW(remainingF) > 0) result.add(remainingF);

		Factor[] res = new Factor[result.size()];
		for (int i = 0; i < result.size(); i++) res[i] = new Factor(result.get(i), 1);
		return res;
	}

	


	/** {@code f(x,y,z,valW)} を返す。未テスト。計算量: O(項数) */
	private long[][][] evalW(long[][][][] f, long valW) {
		f = resize(f);
		long[][][] res = new long[f.length][][];
		for (int i = 0; i < f.length; i++) {
			res[i] = new long[f[i].length][];
			for (int j = 0; j < f[i].length; j++) {
				res[i][j] = new long[f[i][j].length];
				for (int k = 0; k < f[i][j].length; k++) res[i][j][k] = poly1d.eval(f[i][j][k], valW);
			}
		}
		return poly3d.resize(res);
	}

	/** Hensel 持ち上げに適した w 評価点を返す。未テスト。計算量: O(mod候補 * Factor1D) */
	private long findGoodEvaluationPointW(long[][][][] f) {
		int dx = degX(f);
		long[][][] lc = f[dx];
		long bestA = -1;
		int minFactors = Integer.MAX_VALUE;
		int found = 0;
		Random rnd=new Random(0);
		for (int i=0; i < Math.min(mod, 1000); i++) {
			long a=rnd.nextLong(1,mod);
			long[][] lca2d = poly3d.evalZ(lc, a);
			if (poly3d.poly2d.degX(lca2d) == -1) continue;
			long[][][] fa3d = evalW(f, a);
			long[] fa1d = null;
			outer: for (long b = 0; b < Math.min(mod, 100); b++) {
				long[] lcab1d = poly3d.poly2d.evalY(lca2d, b);
				if (poly1d.deg(lcab1d) == -1) continue;
				long[][] fb2d = poly3d.evalZ(fa3d, b);
				for (long c = 0; c < Math.min(mod, 100); c++) {
					if (poly1d.eval(lcab1d, c) == 0) continue;
					long[] fc1d = poly3d.poly2d.evalY(fb2d, c);
					if (poly1d.isSquareFree(fc1d)) {
						fa1d = fc1d;
						break outer;
					}
				}
			}
			if (fa1d != null) {
				int factors = poly1d.factor(fa1d).factors().length;
				if (factors < minFactors) {
					minFactors = factors;
					bestA = a;
				}
				if (++found >= 3) break;
			}
		}
		if (bestA != -1) return bestA;
		throw new ArithmeticException("good w evaluation point not found");
	}

	/** {@code f(x,y,z,w+c)} を返す。未テスト。計算量: O(dx * dy * dz * Shift1D(degW f)) */
	private long[][][][] shiftW(long[][][][] f, long c) {
		f = resize(f);
		long[][][][] res = new long[f.length][][][];
		for (int i = 0; i < f.length; i++) {
			res[i] = new long[f[i].length][][];
			for (int j = 0; j < f[i].length; j++) {
				res[i][j] = new long[f[i][j].length][];
				for (int k = 0; k < f[i][j].length; k++) res[i][j][k] = poly1d.taylorShift(f[i][j][k], poly1d.fp.reduce(c));
			}
		}
		return resize(res);
	}

	/** {@code a} の w 次数を {@code nw} 未満へ切り詰める。未テスト。計算量: O(項数) */
	private long[][][][] truncateW(long[][][][] a, int nw) {
		a = resize(a);
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][][];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = new long[a[i][j].length][];
				for (int k = 0; k < a[i][j].length; k++) res[i][j][k] = Arrays.copyOf(a[i][j][k], Math.min(nw, a[i][j][k].length));
			}
		}
		return resize(res);
	}

	/** {@code [w^k]a(x,y,z,w)} を返す。未テスト。計算量: O(dx * dy * dz) */
	private long[][][] coeffW(long[][][][] a, int w) {
		a = resize(a);
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = new long[a[i][j].length];
				for (int k = 0; k < a[i][j].length; k++) if (w < a[i][j][k].length) res[i][j][k] = a[i][j][k][w];
			}
		}
		return poly3d.resize(res);
	}

	/** {@code a(x,y,z)} を {@code w^k a(x,y,z)} として4変数多項式に埋め込む。未テスト。計算量: O(項数) */
	private long[][][][] embedPoly3DAtDegW(long[][][] a, int k, int nw) {
		a = poly3d.resize(a);
		if (poly3d.degX(a) == -1) return zero();
		long[][][][] res = new long[a.length][][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][][];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = new long[a[i][j].length][nw];
				for (int z = 0; z < a[i][j].length; z++) res[i][j][z][k] = a[i][j][z];
			}
		}
		return resize(res);
	}

	/** {@code f(x,y,z,valW)} の互いに素な3変数因子分解から {@code f(x,y,z,w)} の因子をHensel持ち上げで復元する。未テスト。計算量: O((degW(f)+1) * (Mul4D * factors.length + LinearSolve)) */
	public Factor[] liftFactors(long[][][][] f, PolynomialFpDynamic3D.Factor[] factors, long valW) {
		f = resize(f);
		int nw = Math.max(1, degW(f) + 1);
		long[][][][] shiftedF = truncateW(shiftW(f, valW), nw);
		int r = factors.length;
		if (r == 0) return new Factor[0];
		long[][][][][] lifted = new long[r][][][][];
		for (int i = 0; i < r; i++) lifted[i] = embedPoly3DAtDegW(factors[i].factor, 0, nw);
		for (int k = 1; k < nw; k++) {
			long[][][][] prod = one();
			for (int i = 0; i < r; i++) prod = truncateW(mul(prod, lifted[i]), k + 1);
			long[][][] err = poly3d.resize(poly3d.sub(coeffW(shiftedF, k), coeffW(prod, k)));
			long[][][][] corr3D = henselCorrections3D(err, factors);
			
			for (int i = 0; i < r; i++) lifted[i] = truncateW(add(lifted[i], embedPoly3DAtDegW(corr3D[i], k, nw)), nw);
		}
		Factor[] res = new Factor[r];
		long back = poly1d.fp.reduce(-valW);
		for (int i = 0; i < r; i++) res[i] = new Factor(resize(shiftW(lifted[i], back)), factors[i].multiplicity);
		return res;
	}

	
	/** {@code err=sum_i c_i prod_{j!=i} factors[j]} を満たす3変数補正を返す。計算量: O(U^3 + U * Mul3D) */
	private long[][][][] henselCorrections3D(long[][][] err, PolynomialFpDynamic3D.Factor[] factors) {
		if (poly3d.degX(err)==-1) return new long[factors.length][0][0][0];
		int r = factors.length;
		// Q = prod_i factors[i]
		long[][][] Q = poly3d.one();
		for (PolynomialFpDynamic3D.Factor factor : factors) Q = poly3d.mul(Q, factor.factor);
		
		// cofactors[i] = prod_{i ≠ j} factors[j]
		long[][][][] cofactors = new long[r][][][];
		
		// 各iにおけるc_iのx, y, zのサイズ（最大次数 + 1）を格納する配列
		int[] xBounds = new int[r];
		int[] yBounds = new int[r];
		int[] zBounds = new int[r];
		int[] offset = new int[r];
		int vars = 0;
		
		int maxX = Math.max(poly3d.degX(err), 0);
		int maxY = Math.max(poly3d.degY(err), 0);
		int maxZ = Math.max(poly3d.degZ(err), 0);
		for (int i = 0; i < r; i++) {
			cofactors[i] = poly3d.lexdiv(Q, factors[i].factor);
			
			xBounds[i] = Math.max(Math.max(0, poly3d.degX(err)) - Math.max(0, poly3d.degX(Q)) + Math.max(0, poly3d.degX(factors[i].factor)), 0) + 1;
			yBounds[i] = Math.max(Math.max(0, poly3d.degY(err)) - Math.max(0, poly3d.degY(Q)) + Math.max(0, poly3d.degY(factors[i].factor)), 0) + 1;
			zBounds[i] = Math.max(Math.max(0, poly3d.degZ(err)) - Math.max(0, poly3d.degZ(Q)) + Math.max(0, poly3d.degZ(factors[i].factor)), 0) + 1;
			maxX = Math.max(maxX, xBounds[i] + Math.max(poly3d.degX(cofactors[i]), 0));
			maxY = Math.max(maxY, yBounds[i] + Math.max(poly3d.degY(cofactors[i]), 0));
			maxZ = Math.max(maxZ, zBounds[i] + Math.max(poly3d.degZ(cofactors[i]), 0));
			offset[i] = vars;
			vars += xBounds[i] * yBounds[i] * zBounds[i];
		}
		
		long[][] matA = new long[(maxX + 1) * (maxY + 1) * (maxZ + 1)][vars];
		long[] vecB = new long[(maxX + 1) * (maxY + 1) * (maxZ + 1)];
		
		// 行列の係数埋め込み
		for (int i = 0; i < r; i++) {
			for (int ux = 0; ux < xBounds[i]; ux++) {
				for (int uy = 0; uy < yBounds[i]; uy++) {
					for (int uz = 0; uz < zBounds[i]; uz++) {
						int col = offset[i] + (ux * yBounds[i] + uy) * zBounds[i] + uz;
						
						for (int cx = 0; cx < cofactors[i].length; cx++) {
							for (int cy = 0; cy < cofactors[i][cx].length; cy++) {
								for (int cz = 0; cz < cofactors[i][cx][cy].length; cz++) {
									long v = cofactors[i][cx][cy][cz];
									if (v == 0) continue;
									int row = ((ux + cx) * (maxY + 1) + uy + cy) * (maxZ + 1) + uz + cz;
									matA[row][col] = (matA[row][col] + v) % mod;
								}
							}
						}
					}
				}
			}
		}
		
		// err の埋め込み
		for (int x = 0; x < err.length; x++) {
			for (int y = 0; y < err[x].length; y++) {
				for (int z = 0; z < err[x][y].length; z++) {
					vecB[(x * (maxY + 1) + y) * (maxZ + 1) + z] = err[x][y][z];
				}
			}
		}
		
		long[][] res = MatrixUtilsFp.solveLinearEquation(matA, vecB, mod);
		if (res == null) throw new ArithmeticException("linear correction solve failed");
		long[] sol = res[0];
		long[][][][] corr = new long[r][][][];
		
		// 解の復元
		for (int i = 0; i < r; i++) {
			corr[i] = new long[xBounds[i]][yBounds[i]][zBounds[i]];
			for (int ux = 0; ux < xBounds[i]; ux++) {
				for (int uy = 0; uy < yBounds[i]; uy++) {
					for (int uz = 0; uz < zBounds[i]; uz++) {
						corr[i][ux][uy][uz] = sol[offset[i] + (ux * yBounds[i] + uy) * zBounds[i] + uz];
					}
				}
			}
			corr[i] = poly3d.resize(corr[i]);
		}
		
		return corr;
	}
	
	
	void tr(Object...out) {System.out.println(Arrays.deepToString(out));}

	/**
	 * Kronecker置換を用いて多項式を既約分解する。
	 * @param inputf 因数分解対象の多項式
	 * @return 既約因子の配列
	 */
	public Factor[] factorByKronecker(long[][][][] inputf) {
		long[][][][] f = monic(inputf);
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0 && degW(f) <= 0) return new Factor[0];
		ArrayList<long[][][][]> raw = new ArrayList<>();
		factorDfs(f, raw);
		ArrayList<Factor> ret = new ArrayList<>();
		for (long[][][][] g : raw) {
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

	/**
	 * 因数分解の再帰本体。真の因子を見つけ、リストに追加していく。
	 * @param f 分解対象の多項式（monic）
	 * @param out 見つかった既約因子を格納するリスト
	 */
	private void factorDfs(long[][][][] f, ArrayList<long[][][][]> out) {
		f = monic(f);
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0 && degW(f) <= 0) return;
		long[][][][] d = findWangEezFactor(f);
		if (d == null) {
			out.add(f);
			return;
		}
		long[][][][] q = lexdiv(f, d);
		if (!equals(mul(d, q), f) || equals(d, one()) || equals(q, one())) {
			out.add(f);
			return;
		}
		factorDfs(d, out);
		factorDfs(q, out);
	}

	/**
	 * Kronecker 置換を用いて真の因子を1つ探索する。
	 * @param f 分解対象'の多項式
	 * @return 見つかった真の因子。見つからない（既約である）場合は null
	 */
	private long[][][][] findWangEezFactor(long[][][][] f) {
		int sw = Math.max(1, degW(f) + 1), sz = Math.max(1, degZ(f) + 1), sy = Math.max(1, degY(f) + 1);
		long[] flat = flattenKronecker(f, sy, sz, sw);
		PolynomialFpDynamic.Factor[] fs = poly1d.factor(flat).factors();
		ArrayList<long[]> factors = new ArrayList<>();
		for (PolynomialFpDynamic.Factor e : fs)
			for (int i = 0; i < e.multiplicity; i++)
				factors.add(e.factor);
		if (factors.size() <= 1) return null;
		boolean[] used = new boolean[factors.size()];
		// 因子の組み合わせを小さい方から試す
		for (int szSubset = 1; szSubset <= factors.size() / 2; szSubset++) {
			long[][][][] d = findFactorSubsetDfs(f, factors, used, 0, szSubset, new long[] {1}, sy, sz, sw);
			if (d != null) return d;
		}
		return null;
	}

	/**
	 * 1変数因子の部分集合の積から多変数多項式の因子を探索する DFS。
	 * @param f 分解対象の多項式
	 * @param factors 1変数因子のリスト
	 * @param used 使用済みフラグ
	 * @param from 探索開始インデックス
	 * @param need あといくつ因子を選択するか
	 * @param prod 現在選択されている因子の積（1変数）
	 * @param sy Kronecker 置換の重み y
	 * @param sz Kronecker 置換の重み z
	 * @param sw Kronecker 置換の重み w
	 * @return 見つかった真の因子。見つからない場合は null
	 */
	private long[][][][] findFactorSubsetDfs(long[][][][] f, ArrayList<long[]> factors, boolean[] used, int from, int need,
			long[] prod, int sy, int sz, int sw) {
		if (need == 0) {
			long[][][][] candidate = unflattenKronecker(prod, sy, sz, sw, degX(f), degY(f), degZ(f), degW(f));
			if (candidate == null || degX(candidate) <= 0 && degY(candidate) <= 0 && degZ(candidate) <= 0 && degW(candidate) <= 0)
				return null;
			candidate = monic(candidate);
			if (equals(candidate, f)) return null;
			try {
				long[][][][] q = lexdiv(f, candidate);
				if (q != null && equals(mul(candidate, q), f)) return candidate;
			} catch (RuntimeException e) {
				return null;
			}
			return null;
		}
		for (int i = from; i <= factors.size() - need; i++) {
			if (used[i]) continue;
			used[i] = true;
			long[][][][] res = findFactorSubsetDfs(f, factors, used, i + 1, need - 1, poly1d.mul(prod, factors.get(i)),
					sy, sz, sw);
			used[i] = false;
			if (res != null) return res;
		}
		return null;
	}

	private long getSafe(long[][][][] F, int i, int j, int p, int q) {
		if (i < 0 || i >= F.length)
			return 0;
		if (j < 0 || j >= F[i].length)
			return 0;
		if (p < 0 || p >= F[i][j].length)
			return 0;
		if (q < 0 || q >= F[i][j][p].length)
			return 0;
		return F[i][j][p][q];
	}

	/**
	 * Buchbergerのアルゴリズムを用いてグレブナー基底を計算する。
	 * 単項式順序は辞書式順序 (x > y > z > w) を使用する。
	 * @param F 多項式の集合
	 * @return グレブナー基底
	 */
	public List<long[][][][]> groebnerBasis(List<long[][][][]> F) {
		List<long[][][][]> G = new ArrayList<>();
		for (long[][][][] f : F) {
			long[][][][] res = normalForm(f, G);
			if (degX(res) != -1) G.add(monic(res));
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			int n = G.size();
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					long[][][][] s = sPolynomial(G.get(i), G.get(j));
					long[][][][] res = normalForm(s, G);
					if (degX(res) != -1) {
						G.add(monic(res));
						changed = true;
						break;
					}
				}
				if (changed) break;
			}
		}
		return G;
	}

	/**
	 * 既約グレブナー基底（reduced Gröbner basis）を計算する。
	 * @param F 多項式の集合
	 * @return 既約グレブナー基底
	 */
	public List<long[][][][]> reducedGroebnerBasis(List<long[][][][]> F) {
		List<long[][][][]> G = groebnerBasis(F);
		List<long[][][][]> G2 = new ArrayList<>();
		for (int i = 0; i < G.size(); i++) {
			long[][][][] f = G.get(i);
			boolean redundant = false;
			Term4D ltF = leadTerm(f);
			for (int j = 0; j < G.size(); j++) {
				if (i == j) continue;
				Term4D ltG = leadTerm(G.get(j));
				if (ltF.dx() >= ltG.dx() && ltF.dy() >= ltG.dy() && ltF.dz() >= ltG.dz() && ltF.dw() >= ltG.dw()) {
					redundant = true;
					break;
				}
			}
			if (!redundant) G2.add(f);
		}
		List<long[][][][]> G3 = new ArrayList<>();
		for (int i = 0; i < G2.size(); i++) {
			List<long[][][][]> other = new ArrayList<>(G2);
			long[][][][] f = other.remove(i);
			G3.add(monic(normalForm(f, other)));
		}
		return G3;
	}

	/**
	 * 多項式 f を多項式集合 G で簡約化し、その剰余（正規形）を返す。
	 * @param f 簡約化される多項式
	 * @param G 多項式集合
	 * @return 正規形
	 */
	public long[][][][] normalForm(long[][][][] f, List<long[][][][]> G) {
		long[][][][] r = zero();
		long[][][][] p = resize(f);
		while (degX(p) != -1) {
			boolean divided = false;
			Term4D ltP = leadTerm(p);
			for (long[][][][] g : G) {
				Term4D ltG = leadTerm(g);
				if (ltP.dx() >= ltG.dx() && ltP.dy() >= ltG.dy() && ltP.dz() >= ltG.dz() && ltP.dw() >= ltG.dw()) {
					long val = mo.mul(ltP.v(), mo.inv(ltG.v()));
					p = subMulTerm(p, g, val, ltP.dx() - ltG.dx(), ltP.dy() - ltG.dy(), ltP.dz() - ltG.dz(), ltP.dw() - ltG.dw());
					divided = true;
					break;
				}
			}
			if (!divided) {
				r = addTerm(r, ltP.v(), ltP.dx(), ltP.dy(), ltP.dz(), ltP.dw());
				p = removeLeadTerm(p);
			}
		}
		return resize(r);
	}

	/**
	 * 2つの多項式 f, g の S-多項式を計算する。
	 * @param f 多項式1
	 * @param g 多項式2
	 * @return S-多項式
	 */
	public long[][][][] sPolynomial(long[][][][] f, long[][][][] g) {
		Term4D ltF = leadTerm(f);
		Term4D ltG = leadTerm(g);
		int mx = Math.max(ltF.dx(), ltG.dx());
		int my = Math.max(ltF.dy(), ltG.dy());
		int mz = Math.max(ltF.dz(), ltG.dz());
		int mw = Math.max(ltF.dw(), ltG.dw());
		long[][][][] tF = mulTerm(f, mo.inv(ltF.v()), mx - ltF.dx(), my - ltF.dy(), mz - ltF.dz(), mw - ltF.dw());
		long[][][][] tG = mulTerm(g, mo.inv(ltG.v()), mx - ltG.dx(), my - ltG.dy(), mz - ltG.dz(), mw - ltG.dw());
		return resize(sub(tF, tG));
	}

	/**
	 * 多項式の辞書順最大の主項を返す。
	 * @param a 多項式
	 * @return 主項の情報
	 */
	public Term4D leadTerm(long[][][][] a) {
		int dx = degX(a);
		if (dx == -1) return new Term4D(-1, -1, -1, -1, 0);
		long[][][] lead3D = a[dx];
		int dy = poly3d.degX(lead3D);
		int dz = poly3d.poly2d.degX(lead3D[dy]);
		int dw = poly3d.poly1d.deg(lead3D[dy][dz]);
		return new Term4D(dx, dy, dz, dw, lead3D[dy][dz][dw]);
	}

	/** {@code a - b * v * x^dx * y^dy * z^dz * w^dw} を返す。 */
	public long[][][][] subMulTerm(long[][][][] a, long[][][][] b, long v, int dx, int dy, int dz, int dw) {
		int nx = Math.max(a.length, b.length + dx);
		int my = 0, lz = 0, kw = 0;
		for (long[][][] r3 : a) {
			my = Math.max(my, r3.length);
			for (long[][] r2 : r3) {
				lz = Math.max(lz, r2.length);
				for (long[] r1 : r2) kw = Math.max(kw, r1.length);
			}
		}
		for (long[][][] r3 : b) {
			my = Math.max(my, r3.length + dy);
			for (long[][] r2 : r3) {
				lz = Math.max(lz, r2.length + dz);
				for (long[] r1 : r2) kw = Math.max(kw, r1.length + dw);
			}
		}
		long[][][][] c = new long[nx][my][lz][kw];
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < my; j++)
				for (int p = 0; p < lz; p++)
					for (int q = 0; q < kw; q++) {
						long va = getSafe(a, i, j, p, q);
						long vb = getSafe(b, i - dx, j - dy, p - dz, q - dw);
						c[i][j][p][q] = (va - mo.mul(vb, v) + mod) % mod;
					}
		return resize(c);
	}

	/** {@code a + v * x^dx * y^dy * z^dz * w^dw} を返す。 */
	public long[][][][] addTerm(long[][][][] a, long v, int dx, int dy, int dz, int dw) {
		int nx = Math.max(a.length, dx + 1);
		int my = 0, lz = 0, kw = 0;
		for (long[][][] r3 : a) {
			my = Math.max(my, r3.length);
			for (long[][] r2 : r3) {
				lz = Math.max(lz, r2.length);
				for (long[] r1 : r2) kw = Math.max(kw, r1.length);
			}
		}
		my = Math.max(my, dy + 1);
		lz = Math.max(lz, dz + 1);
		kw = Math.max(kw, dw + 1);
		long[][][][] c = new long[nx][my][lz][kw];
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < my; j++)
				for (int p = 0; p < lz; p++)
					for (int q = 0; q < kw; q++) {
						long va = getSafe(a, i, j, p, q);
						if (i == dx && j == dy && p == dz && q == dw) va = (va + v) % mod;
						c[i][j][p][q] = va;
					}
		return resize(c);
	}

	/** 主項を取り除いた多項式を返す。 */
	public long[][][][] removeLeadTerm(long[][][][] a) {
		Term4D lt = leadTerm(a);
		if (lt.dx() == -1) return a;
		long[][][][] c = resize(a);
		c[lt.dx()][lt.dy()][lt.dz()][lt.dw()] = 0;
		return resize(c);
	}

	/** {@code a * v * x^dx * y^dy * z^dz * w^dw} を返す。 */
	public long[][][][] mulTerm(long[][][][] a, long v, int dx, int dy, int dz, int dw) {
		if (degX(a) == -1) return zero();
		int nx = a.length + dx;
		int my = 0, lz = 0, kw = 0;
		for (long[][][] r3 : a) {
			my = Math.max(my, r3.length);
			for (long[][] r2 : r3) {
				lz = Math.max(lz, r2.length);
				for (long[] r1 : r2) kw = Math.max(kw, r1.length);
			}
		}
		my += dy; lz += dz; kw += dw;
		long[][][][] c = new long[nx][my][lz][kw];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				for (int k = 0; k < a[i][j].length; k++)
					for (int l = 0; l < a[i][j][k].length; l++)
						c[i + dx][j + dy][k + dz][l + dw] = mo.mul(a[i][j][k][l], v);
		return resize(c);
	}

	public long[][][][] invNaive(long[][][][] a) {
		if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0 || a[0][0][0][0] == 0)
			throw new AssertionError();
		int m0 = 0, l0 = 0, k0 = 0;
		for (long[][][] r3 : a) {
			m0 = Math.max(m0, r3.length);
			for (long[][] r2 : r3) {
				l0 = Math.max(l0, r2.length);
				for (long[] r1 : r2)
					k0 = Math.max(k0, r1.length);
			}
		}
		long[][][][] b = new long[a.length][m0][l0][k0];
		long inva0000 = mo.inv(a[0][0][0][0]);
		b[0][0][0][0] = inva0000;
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				for (int p = 0; p < b[i][j].length; p++) {
					for (int q = 0; q < b[i][j][p].length; q++) {
						if (i == 0 && j == 0 && p == 0 && q == 0)
							continue;
						for (int i2 = 0; i2 <= i; i2++) {
							if (i2 >= a.length)
								continue;
							for (int j2 = 0; j2 <= j; j2++) {
								if (j2 >= a[i2].length)
									continue;
								for (int p2 = 0; p2 <= p; p2++) {
									if (p2 >= a[i2][j2].length)
										continue;
									for (int q2 = 0; q2 <= q; q2++) {
										if (i2 == 0 && j2 == 0 && p2 == 0 && q2 == 0)
											continue;
										if (q2 >= a[i2][j2][p2].length)
											continue;
										b[i][j][p][q] = (b[i][j][p][q]
												- a[i2][j2][p2][q2] * b[i - i2][j - j2][p - p2][q - q2] % mod + mod)
												% mod;
									}
								}
							}
						}
						b[i][j][p][q] = b[i][j][p][q] * inva0000 % mod;
					}
				}
			}
		}
		return b;
	}

	/**
	 * 1/f の指定された項の係数を効率的に計算するための最適な分解 f = ph - q を求める。
	 * 複雑度スコア Σ(a_i-1) を最小化する分解を探索する。ここで a_i は p, h, q の各既約因子の項数である (p, h, q の因子は相異なるという仮定でスコア計算）。
	 * f[0][0][0][0]=1を仮定
	 * @param inputf 多項式 f
	 * @return 最適な分解結果
	 */
	public BestDecomposition4D findBestDecomposition(long[][][][] inputf) {
		if(inputf[0][0][0][0]!=1)throw new AssertionError();
		long[][][][] f = resize(inputf);
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						f[i][j][k][l]%=mod;
						if(f[i][j][k][l]<0)f[i][j][k][l]+=mod;
					}
				}
			}
		}
		if (degX(f) == -1) return new BestDecomposition4D(0, new Factor[0], 0, new Factor[0], 0);
		// f の非ゼロ項を抽出する（定数項以外）
		ArrayList<int[]> fTerms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (i == 0 && j == 0 && k == 0 && l == 0) continue;
						if (f[i][j][k][l] != 0) {
							fTerms.add(new int[] {i, j, k, l, (int) f[i][j][k][l]});
						}
					}
				}
			}
		}
		ArrayList<long[][][][]> candidatesP = new ArrayList<>();
		enumerateSparsePolynomialsFromF(Math.max(1, f.length), Math.max(1, f[0].length), Math.max(1, f[0][0].length), Math.max(1, f[0][0][0].length), fTerms, 1, candidatesP);
		ArrayList<BestCandidate4D> scoredP = new ArrayList<>();
		for (long[][][][] p : candidatesP) {
			ArrayList<int[]> pTerms = new ArrayList<>();
			for (int i = 0; i < p.length; i++) for (int j = 0; j < p[i].length; j++) for (int k = 0; k < p[i][j].length; k++) for (int l = 0; l < p[i][j][k].length; l++)
				if (p[i][j][k][l] != 0) pTerms.add(new int[] {i, j, k, l, (int) p[i][j][k][l]});
			scoredP.add(new BestCandidate4D(p, pTerms, scoreFactors(factor(p).factors())));
		}
		BestDecomposition4D best = computeDecompositionResult(one(), f, f);
		scoredP.sort((x, y) -> Long.compare(x.score, y.score));
		for (var sh : scoredP) {
			for (var sp : scoredP) {
				if (best.score != -1 && sh.score + sp.score >= best.score) continue;
				BestDecomposition4D current = computeDecompositionResult(sp.poly, sh.poly, f);
				if (best.score == -1 || current.score < best.score) best = current;
			}
		}
		return best;
	}

	private record BestCandidate4D(long[][][][] poly, ArrayList<int[]> terms, long score) {}

	private long scoreFactors(Factor[] factors) {
		long s = 0;
		for (Factor f : factors) s += countTerms(f.factor) - 1;
		return s;
	}

	private void enumerateSparsePolynomialsFromF(int dx, int dy, int dz, int dw, ArrayList<int[]> fTerms, long constTerm, ArrayList<long[][][][]> out) {
		// 0項 (1)
		long[][][][] res0 = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)][Math.max(1, dw)];
		res0[0][0][0][0] = constTerm;
		out.add(resize(res0));

		// 1項 (1 + term_i)
		for (int i = 0; i < fTerms.size(); i++) {
			int[] t = fTerms.get(i);
			long[][][][] res = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)][Math.max(1, dw)];
			res[0][0][0][0] = constTerm;
			if(t[0] < res.length && t[1] < res[t[0]].length && t[2] < res[t[0]][t[1]].length && t[3] < res[t[0]][t[1]][t[2]].length) res[t[0]][t[1]][t[2]][t[3]] = t[4];
			out.add(resize(res));
		}

		// 2項 (1 + term_i + term_j)
		for (int i = 0; i < fTerms.size(); i++) {
			for (int j = i + 1; j < fTerms.size(); j++) {
				int[] t1 = fTerms.get(i);
				int[] t2 = fTerms.get(j);
				long[][][][] res = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)][Math.max(1, dw)];
				res[0][0][0][0] = constTerm;
					if(t1[0] < res.length && t1[1] < res[t1[0]].length && t1[2] < res[t1[0]][t1[1]].length && t1[3] < res[t1[0]][t1[1]][t1[2]].length) res[t1[0]][t1[1]][t1[2]][t1[3]] = t1[4];
				if(t2[0] < res.length && t2[1] < res[t2[0]].length && t2[2] < res[t2[0]][t2[1]].length && t2[3] < res[t2[0]][t2[1]][t2[2]].length) res[t2[0]][t2[1]][t2[2]][t2[3]] = t2[4];
				out.add(resize(res));
			}
		}
	}

	private BestDecomposition4D computeDecompositionResult(long[][][][] p, long[][][][] h, long[][][][] f) {
		long[][][][] ph = mul(p, h);
		long[][][][] q = sub(ph, f);
		Factor[] qFactors = factor(q).factors();
		ArrayList<Factor> phFactorsList = new ArrayList<>();
		if(equals(p, h)) {
			phFactorsList.add(new Factor(p, 2));
		} else {
			phFactorsList.add(new Factor(p, 1));
			phFactorsList.add(new Factor(h, 1));
		}
		Factor[] phFactors = phFactorsList.toArray(new Factor[0]);

		long phConst = (lead(p) * lead(h)) % mod;
		long qConst = lead(q);

		ArrayList<long[][][][]> distinctIrred = new ArrayList<>();
		for (Factor ef : phFactors) {
			long[][][][] mf = monic(ef.factor);
			boolean exists = false;
			for (long[][][][] prev : distinctIrred) if (equals(prev, mf)) exists = true;
			if (!exists) distinctIrred.add(mf);
		}
		for (Factor ef : qFactors) {
			long[][][][] mf = monic(ef.factor);
			boolean exists = false;
			for (long[][][][] prev : distinctIrred) if (equals(prev, mf)) exists = true;
			if (!exists) distinctIrred.add(mf);
		}
		long score = 0;
		for (long[][][][] irred : distinctIrred) {
			score += countTerms(irred) - 1;
		}
		return new BestDecomposition4D(phConst, phFactors, qConst, qFactors, score);
	}

	/**
	 * 4変数の多項式の項を表すレコード。
	 * @param dx xの次数
	 * @param dy yの次数
	 * @param dz zの次数
	 * @param dw wの次数
	 * @param v 係数
	 */
	public record Term4D(int dx, int dy, int dz, int dw, long v) {}

	public long[][][][] sparseMul(long[][][][] a, long[][][][] sparsePoly) {
		ArrayList<Term4D> terms = new ArrayList<>();
		int maxDx = 0, maxDy = 0, maxDz = 0, maxDw = 0;
		for (int i = 0; i < sparsePoly.length; i++) {
			for (int j = 0; j < sparsePoly[i].length; j++) {
				for (int k = 0; k < sparsePoly[i][j].length; k++) {
					for (int l = 0; l < sparsePoly[i][j][k].length; l++) {
						if (sparsePoly[i][j][k][l] != 0) {
							terms.add(new Term4D(i, j, k, l, poly1d.fp.reduce(sparsePoly[i][j][k][l])));
							maxDx = Math.max(maxDx, i);
							maxDy = Math.max(maxDy, j);
							maxDz = Math.max(maxDz, k);
							maxDw = Math.max(maxDw, l);
						}
					}
				}
			}
		}
		int ni = a.length, nj = 0, nk = 0, nl = 0;
		for (long[][][] row : a) {
			nj = Math.max(nj, row.length);
			for (long[][] col : row) {
				nk = Math.max(nk, col.length);
				for (long[] depth : col)
					nl = Math.max(nl, depth.length);
			}
		}
		if (ni == 0 || terms.isEmpty()) return zero();
		long[][][][] b = new long[ni + maxDx][nj + maxDy][nk + maxDz][nl + maxDw];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				for (int k = 0; k < b[i][j].length; k++) {
					for (int l = 0; l < b[i][j][k].length; l++) {
						for (Term4D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
							if (pi >= 0 && pi < a.length && pj >= 0 && pj < a[pi].length && pk >= 0 && pk < a[pi][pj].length && pl >= 0 && pl < a[pi][pj][pk].length) {
								b[i][j][k][l] = (b[i][j][k][l] + t.v * a[pi][pj][pk][pl]) % mod;
							}
						}
					}
				}
			}
		}
		return b;
	}

	public long[][][][] sparseExp(long[][][][] f, int ni, int nj, int nk, int nl) {
		if (ni <= 0 || nj <= 0 || nk <= 0 || nl <= 0)
			return zero();
		if (f.length > 0 && f[0].length > 0 && f[0][0].length > 0 && f[0][0][0].length > 0 && poly1d.fp.reduce(f[0][0][0][0]) != 0)
			throw new ArithmeticException("f[0][0][0][0] must be zero");
		ArrayList<Term4D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (f[i][j][k][l] != 0)
							terms.add(new Term4D(i, j, k, l, poly1d.fp.reduce(f[i][j][k][l])));
					}
				}
			}
		}
		long[][][][] g = new long[ni][nj][nk][nl];
		g[0][0][0][0] = 1;
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						if (i == 0 && j == 0 && k == 0 && l == 0)
							continue;
						long tmp = 0;
						if (i > 0) {
							for (Term4D t : terms) {
								int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pi >= 0 && pj >= 0 && pk >= 0 && pl >= 0 && pi < ni && pj < nj && pk < nk && pl < nl) {
									tmp = (tmp + t.dx * t.v % mod * g[pi][pj][pk][pl]) % mod;
								}
							}
							g[i][j][k][l] = tmp * MathUtils.modInv(i, mod) % mod;
						} else if (j > 0) {
							for (Term4D t : terms) {
								if (t.dx != 0)
									continue;
								int pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pj >= 0 && pk >= 0 && pl >= 0 && pj < nj && pk < nk && pl < nl) {
									tmp = (tmp + t.dy * t.v % mod * g[0][pj][pk][pl]) % mod;
								}
							}
							g[i][j][k][l] = tmp * MathUtils.modInv(j, mod) % mod;
						} else if (k > 0) {
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0)
									continue;
								int pk = k - t.dz, pl = l - t.dw;
								if (pk >= 0 && pl >= 0 && pk < nk && pl < nl) {
									tmp = (tmp + t.dz * t.v % mod * g[0][0][pk][pl]) % mod;
								}
							}
							g[i][j][k][l] = tmp * MathUtils.modInv(k, mod) % mod;
						} else {
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0 || t.dz != 0)
									continue;
								int pl = l - t.dw;
								if (pl >= 0 && pl < nl) {
									tmp = (tmp + t.dw * t.v % mod * g[0][0][0][pl]) % mod;
								}
							}
							g[i][j][k][l] = tmp * MathUtils.modInv(l, mod) % mod;
						}
					}
				}
			}
		}
		return g;
	}

	public long[][][][] sparsePow(long[][][][] f, int ni, int nj, int nk, int nl, long p) {
		if (ni <= 0 || nj <= 0 || nk <= 0 || nl <= 0)
			return zero();
		if (p == 0) {
			long[][][][] res = new long[ni][nj][nk][nl];
			res[0][0][0][0] = 1;
			return res;
		}
		int d0i = -1, d0j = -1, d0k = -1, d0l = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (f[i][j][k][l] != 0) {
							d0i = i;
							d0j = j;
							d0k = k;
							d0l = l;
							break outer;
						}
					}
				}
			}
		}
		if (d0i == -1)
			return new long[ni][nj][nk][nl];
		if ((d0i > 0 && (ni - 1) / d0i < p) || (d0j > 0 && (nj - 1) / d0j < p) || (d0k > 0 && (nk - 1) / d0k < p) || (d0l > 0 && (nl - 1) / d0l < p))
			return new long[ni][nj][nk][nl];
		int bi = (int) (d0i * p), bj = (int) (d0j * p), bk = (int) (d0k * p), bl = (int) (d0l * p);
		if (bi >= ni || bj >= nj || bk >= nk || bl >= nl)
			return new long[ni][nj][nk][nl];
		ArrayList<Term4D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (i == d0i && j == d0j && k == d0k && l == d0l)
							continue;
						if (f[i][j][k][l] != 0)
							terms.add(new Term4D(i - d0i, j - d0j, k - d0k, l - d0l, poly1d.fp.reduce(f[i][j][k][l])));
					}
				}
			}
		}
		long[][][][] res = new long[ni][nj][nk][nl];
		res[bi][bj][bk][bl] = MathUtils.modPow(poly1d.fp.reduce(f[d0i][d0j][d0k][d0l]), p % (mod - 1), mod);
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0i][d0j][d0k][d0l]), mod);
		long pMod = p % mod;
		for (int i = bi; i < ni; i++) {
			for (int j = bj; j < nj; j++) {
				for (int k = bk; k < nk; k++) {
					for (int l = bl; l < nl; l++) {
						if (i == bi && j == bj && k == bk && l == bl)
							continue;
						long tmp = 0;
						if (i > bi) {
							int n = i - bi;
							for (Term4D t : terms) {
								int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pi >= bi && pj >= bj && pk >= bk && pl >= bl && pi < ni && pj < nj && pk < nk && pl < nl) {
									long val = ((pMod + 1) * t.dx % mod - n + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[pi][pj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
						} else if (j > bj) {
							int m = j - bj;
							for (Term4D t : terms) {
								if (t.dx != 0)
									continue;
								int pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pj >= bj && pk >= bk && pl >= bl && pj < nj && pk < nk && pl < nl) {
									long val = ((pMod + 1) * t.dy % mod - m + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][pj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
						} else if (k > bk) {
							int n2 = k - bk;
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0)
									continue;
								int pk = k - t.dz, pl = l - t.dw;
								if (pk >= bk && pl >= bl && pk < nk && pl < nl) {
									long val = ((pMod + 1) * t.dz % mod - n2 + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][bj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(n2, mod) % mod;
						} else {
							int m2 = l - bl;
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0 || t.dz != 0)
									continue;
								int pl = l - t.dw;
								if (pl >= bl && pl < nl) {
									long val = ((pMod + 1) * t.dw % mod - m2 + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][bj][bk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(m2, mod) % mod;
						}
					}
				}
			}
		}
		return res;
	}

	public long[][][][] sparseSqrt(long[][][][] f, int ni, int nj, int nk, int nl) {
		if (ni <= 0 || nj <= 0 || nk <= 0 || nl <= 0)
			return zero();
		int d0i = -1, d0j = -1, d0k = -1, d0l = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (f[i][j][k][l] != 0) {
							d0i = i;
							d0j = j;
							d0k = k;
							d0l = l;
							break outer;
						}
					}
				}
			}
		}
		if (d0i == -1)
			return new long[ni][nj][nk][nl];
		if (d0i % 2 != 0 || d0j % 2 != 0 || d0k % 2 != 0 || d0l % 2 != 0)
			return null;
		long sqrt0 = MathUtils.modKthRoot(poly1d.fp.reduce(f[d0i][d0j][d0k][d0l]), 2, mod);
		if (sqrt0 == -1)
			return null;
		if (sqrt0 * 2 > mod)
			sqrt0 = mod - sqrt0;
		int bi = d0i / 2, bj = d0j / 2, bk = d0k / 2, bl = d0l / 2;
		if (bi >= ni || bj >= nj || bk >= nk || bl >= nl)
			return new long[ni][nj][nk][nl];
		ArrayList<Term4D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (i == d0i && j == d0j && k == d0k && l == d0l)
							continue;
						if (f[i][j][k][l] != 0)
							terms.add(new Term4D(i - d0i, j - d0j, k - d0k, l - d0l, poly1d.fp.reduce(f[i][j][k][l])));
					}
				}
			}
		}
		long[][][][] res = new long[ni][nj][nk][nl];
		res[bi][bj][bk][bl] = sqrt0;
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0i][d0j][d0k][d0l]), mod);
		long kMod = MathUtils.modInv(2, mod);
		for (int i = bi; i < ni; i++) {
			for (int j = bj; j < nj; j++) {
				for (int k = bk; k < nk; k++) {
					for (int l = bl; l < nl; l++) {
						if (i == bi && j == bj && k == bk && l == bl)
							continue;
						long tmp = 0;
						if (i > bi) {
							int n = i - bi;
							for (Term4D t : terms) {
								int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pi >= bi && pj >= bj && pk >= bk && pl >= bl && pi < ni && pj < nj && pk < nk && pl < nl) {
									long val = ((kMod + 1) * t.dx % mod - n + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[pi][pj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
						} else if (j > bj) {
							int m = j - bj;
							for (Term4D t : terms) {
								if (t.dx != 0)
									continue;
								int pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pj >= bj && pk >= bk && pl >= bl && pj < nj && pk < nk && pl < nl) {
									long val = ((kMod + 1) * t.dy % mod - m + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][pj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
						} else if (k > bk) {
							int n2 = k - bk;
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0)
									continue;
								int pk = k - t.dz, pl = l - t.dw;
								if (pk >= bk && pl >= bl && pk < nk && pl < nl) {
									long val = ((kMod + 1) * t.dz % mod - n2 + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][bj][pk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(n2, mod) % mod;
						} else {
							int m2 = l - bl;
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0 || t.dz != 0)
									continue;
								int pl = l - t.dw;
								if (pl >= bl && pl < nl) {
									long val = ((kMod + 1) * t.dw % mod - m2 + mod) % mod;
									tmp = (tmp + val * t.v % mod * res[bi][bj][bk][pl]) % mod;
								}
							}
							res[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(m2, mod) % mod;
						}
					}
				}
			}
		}
		return res;
	}

	public long[][][][] sparseInv(long[][][][] f, int ni, int nj, int nk, int nl) {
		if (ni <= 0 || nj <= 0 || nk <= 0 || nl <= 0)
			return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0].length == 0 || f[0][0][0].length == 0 || f[0][0][0][0] == 0)
			throw new ArithmeticException("f[0][0][0][0] must be non-zero");
		ArrayList<Term4D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (i == 0 && j == 0 && k == 0 && l == 0)
							continue;
						if (f[i][j][k][l] != 0)
							terms.add(new Term4D(i, j, k, l, poly1d.fp.reduce(f[i][j][k][l])));
					}
				}
			}
		}
		long[][][][] res = new long[ni][nj][nk][nl];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0][0][0]), mod);
		res[0][0][0][0] = inv0;
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						if (i == 0 && j == 0 && k == 0 && l == 0)
							continue;
						long tmp = 0;
						for (Term4D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
							if (pi >= 0 && pj >= 0 && pk >= 0 && pl >= 0 && pi < ni && pj < nj && pk < nk && pl < nl) {
								tmp = (tmp + t.v * res[pi][pj][pk][pl]) % mod;
							}
						}
						res[i][j][k][l] = tmp == 0 ? 0 : (mod - tmp) % mod * inv0 % mod;
					}
				}
			}
		}
		return res;
	}

	public long[][][][] sparseInv(long[][][][] a) {
		int ni = a.length, nj = 0, nk = 0, nl = 0;
		for (long[][][] row : a) {
			nj = Math.max(nj, row.length);
			for (long[][] col : row) {
				nk = Math.max(nk, col.length);
				for (long[] depth : col)
					nl = Math.max(nl, depth.length);
			}
		}
		return sparseInv(a, ni, nj, nk, nl);
	}

	/**
	 * 多項式を式として表示する。
	 * @param label ラベル
	 * @param arr 多項式の係数配列
	 *
	 * <p>計算量: O(NMLK)
	 * <p>未テスト
	 */
	public void printPolyAsExpr(String label, long[][][][] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x", "y", "z", "w" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				for (int k = 0; k < arr[i][j].length; k++) {
					for (int l = 0; l < arr[i][j][k].length; l++) {
						long coeff = mo.reduce(arr[i][j][k][l]);
						if (coeff == 0) continue;

						if (!isFirst) {
							sb.append(" + ");
						}

						StringBuilder varPart = new StringBuilder();
						int[] powers = { i, j, k, l };
						for (int v = 0; v < 4; v++) {
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
			}
		}

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}

	public long[][][][] sparseLog(long[][][][] f, int ni, int nj, int nk, int nl) {
		if (ni <= 0 || nj <= 0 || nk <= 0 || nl <= 0)
			return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0].length == 0 || f[0][0][0].length == 0 || f[0][0][0][0] == 0)
			throw new ArithmeticException("f[0][0][0][0] must be non-zero");
		ArrayList<Term4D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					for (int l = 0; l < f[i][j][k].length; l++) {
						if (f[i][j][k][l] != 0)
							terms.add(new Term4D(i, j, k, l, poly1d.fp.reduce(f[i][j][k][l])));
					}
				}
			}
		}
		long[][][][] g = new long[ni][nj][nk][nl];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0][0][0]), mod);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						if (i == 0 && j == 0 && k == 0 && l == 0)
							continue;
						long tmp = 0;
						if (i > 0) {
							for (Term4D t : terms) {
								int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pi >= 0 && pj >= 0 && pk >= 0 && pl >= 0 && pi < ni && pj < nj && pk < nk && pl < nl) {
									if (i == t.dx && j == t.dy && k == t.dz && l == t.dw)
										tmp = (tmp + i * t.v) % mod;
									else
										tmp = (tmp + mod - (i - t.dx) * t.v % mod * g[pi][pj][pk][pl] % mod) % mod;
								}
							}
							g[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(i, mod) % mod;
						} else if (j > 0) {
							for (Term4D t : terms) {
								if (t.dx != 0)
									continue;
								int pj = j - t.dy, pk = k - t.dz, pl = l - t.dw;
								if (pj >= 0 && pk >= 0 && pl >= 0 && pj < nj && pk < nk && pl < nl) {
									if (j == t.dy && k == t.dz && l == t.dw)
										tmp = (tmp + j * t.v) % mod;
									else
										tmp = (tmp + mod - (j - t.dy) * t.v % mod * g[0][pj][pk][pl] % mod) % mod;
								}
							}
							g[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(j, mod) % mod;
						} else if (k > 0) {
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0)
									continue;
								int pk = k - t.dz, pl = l - t.dw;
								if (pk >= 0 && pl >= 0 && pk < nk && pl < nl) {
									if (k == t.dz && l == t.dw)
										tmp = (tmp + k * t.v) % mod;
									else
										tmp = (tmp + mod - (k - t.dz) * t.v % mod * g[0][0][pk][pl] % mod) % mod;
								}
							}
							g[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(k, mod) % mod;
						} else {
							for (Term4D t : terms) {
								if (t.dx != 0 || t.dy != 0 || t.dz != 0)
									continue;
								int pl = l - t.dw;
								if (pl >= 0 && pl < nl) {
									if (l == t.dw)
										tmp = (tmp + l * t.v) % mod;
									else
										tmp = (tmp + mod - (l - t.dw) * t.v % mod * g[0][0][0][pl] % mod) % mod;
								}
							}
							g[i][j][k][l] = tmp * inv0 % mod * MathUtils.modInv(l, mod) % mod;
						}
					}
				}
			}
		}
		return g;
	}
}
