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
import library.util.geometry.LongPoint;
import library.util.linalg.MatrixUtilsFp;

public class PolynomialFpDynamic3D implements UFDStrategy<long[][][]>, ExactDivRingStrategy<long[][][]> {
	/**
	 * 最適な分解結果を保持するレコード。
	 * f = phConst * phFactors - qConst * qFactors の関係を満たす。
	 * @param phConst ph部分の定数倍
	 * @param pFactors ph部分の既約因子
	 * @param qConst q部分の定数倍
	 * @param qFactors q部分の既約因子
	 * @param score Σ(項数-1) で計算される複雑度スコア
	 */
	public record BestDecomposition3D(long phConst, Factor[] pFactors, long qConst, Factor[] qFactors, long score) {}
	public final long mod;
	Fp fp;
	final PolynomialFpDynamic poly1d;
	final PolynomialFpDynamic2D poly2d;

	/** 998244353 = 119×2^23+1, 原始根3 */
	public static final PolynomialFpDynamic3D MOD998244353 = new PolynomialFpDynamic3D(
			PolynomialFpDynamic2D.MOD998244353);
	/** 469762049 = 7×2^26+1, 原始根3 */
	public static final PolynomialFpDynamic3D MOD469762049 = new PolynomialFpDynamic3D(
			PolynomialFpDynamic2D.MOD469762049);
	/** 167772161 = 5×2^25+1, 原始根3 */
	public static final PolynomialFpDynamic3D MOD167772161 = new PolynomialFpDynamic3D(
			PolynomialFpDynamic2D.MOD167772161);
	/** 754974721 = 45×2^24+1, 原始根11 */
	public static final PolynomialFpDynamic3D MOD754974721 = new PolynomialFpDynamic3D(
			PolynomialFpDynamic2D.MOD754974721);
	/** 1004535809 = 479×2^21+1, 原始根3 */
	public static final PolynomialFpDynamic3D MOD1004535809 = new PolynomialFpDynamic3D(
			PolynomialFpDynamic2D.MOD1004535809);

	public PolynomialFpDynamic3D(long mod) {
		this.mod = mod;
		fp = new Fp(mod);
		poly1d = PolynomialFpDynamic.of(mod);
		poly2d = PolynomialFpDynamic2D.of(poly1d);
	}

	public PolynomialFpDynamic3D(PolynomialFpDynamic2D poly2d) {
		this.mod = poly2d.mod;
		this.fp = poly2d.fp;
		this.poly1d = poly2d.poly1d;
		this.poly2d = poly2d;
	}

	@Override
	public long[][][] zero() {
		return new long[0][0][0];
	}

	@Override
	public long[][][] one() {
		return new long[][][] { { { 1 } } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][] x() {
		long[][][] ret = new long[2][1][1];
		ret[1][0][0] = 1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][] y() {
		long[][][] ret = new long[1][2][1];
		ret[0][1][0] = 1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public long[][][] z() {
		long[][][] ret = new long[1][1][2];
		ret[0][0][1] = 1;
		return ret;
	}

	@Override
	public long[][][] add(long[][][] a, long[][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0, l = 0;
		for (long[][] mat : a) {
			m = Math.max(m, mat.length);
			for (long[] row : mat)
				l = Math.max(l, row.length);
		}
		for (long[][] mat : b) {
			m = Math.max(m, mat.length);
			for (long[] row : mat)
				l = Math.max(l, row.length);
		}
		long[][][] c = new long[n][m][l];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				for (int k = 0; k < l; k++) {
					long va = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					long vb = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : 0;
					c[i][j][k] = fp.add(va, vb);
				}
		return c;
	}

	@Override
	public long[][][] sub(long[][][] a, long[][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0, l = 0;
		for (long[][] mat : a) {
			m = Math.max(m, mat.length);
			for (long[] row : mat)
				l = Math.max(l, row.length);
		}
		for (long[][] mat : b) {
			m = Math.max(m, mat.length);
			for (long[] row : mat)
				l = Math.max(l, row.length);
		}
		long[][][] c = new long[n][m][l];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				for (int k = 0; k < l; k++) {
					long va = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					long vb = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : 0;
					c[i][j][k] = fp.sub(va, vb);
				}
		return c;
	}

	@Override
	public long[][][] neg(long[][][] a) {
		return sub(zero(), a);
	}

	@Override
	public boolean equals(long[][][] a, long[][][] b) {
		return Arrays.deepEquals(resize(a), resize(b));
	}

	@Override
	public long[][][] exactDiv(long[][][] a, long[][][] b) {
		return lexdiv(a, b);
	}

	/**
	 * 辞書式順序 (x > y > z) に基づく多項式の除算を行い、商と余りを返す。
	 * Kronecker 置換を用いて 1 変数多項式の除算に帰着させることで、一意な商と余りを得る。
	 */
	public DivModResult lexdivmod(long[][][] a, long[][][] b) {
		a = resize(a); b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		int dxA = degX(a);
		if (dxA < dxB) return new DivModResult(zero(), a);
		int dyA = degY(a), dyB = degY(b);
		int dzA = degZ(a), dzB = degZ(b);
		int sy = Math.max(dyA, dyB) + (dxA - dxB + 1) * dyB + 1;
		int sz = Math.max(dzA, dzB) + (dxA - dxB + 1) * dzB + 1;
		long[] fa = flattenKronecker(a, sy, sz);
		long[] fb = flattenKronecker(b, sy, sz);
		PolynomialFpDynamic.DivModResult res = poly1d.divmod(fa, fb);
		long[][][] q = unflattenKronecker(res.q, sy, sz, Math.max(0, dxA - dxB), sy - 1, sz - 1);
		long[][][] r = unflattenKronecker(res.r, sy, sz, dxB, sy - 1, sz - 1);
		return new DivModResult(q, r);
	}

	/** 辞書式順序に基づく商を返す。 */
	public long[][][] lexdiv(long[][][] a, long[][][] b) {
		a = resize(a);
		b = resize(b);
		int dxB = degX(b);
		if (dxB == -1) throw new ArithmeticException("division by zero polynomial");
		if (dxB == 0 && poly2d.degX(b[0]) == 0 && poly1d.deg(b[0][0]) == 0) {
			long inv = fp.inv(b[0][0][0]);
			long[][][] resDiv = new long[a.length][][];
			for (int i = 0; i < a.length; i++)
				resDiv[i] = poly2d.mulByPolyY(a[i], new long[] { inv });
			return resize(resDiv);
		}
		return lexdivmod(a, b).q;
	}

	/** 辞書式順序に基づく余りを返す。 */
	public long[][][] lexmod(long[][][] a, long[][][] b) {
		return lexdivmod(a, b).r;
	}

	public long[][][] mulNaive(long[][][] a, long[][][] b) {
		if (a.length == 0 || b.length == 0) return zero();
		ArrayList<int[]> al = new ArrayList<>(); ArrayList<Long> av = new ArrayList<>();
		int ma = 0, la = 0;
		for (int i = 0; i < a.length; i++) {
			ma = Math.max(ma, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				la = Math.max(la, a[i][j].length);
				for (int k = 0; k < a[i][j].length; k++)
					if (a[i][j][k] != 0) { al.add(new int[]{i,j,k}); av.add(a[i][j][k]); }
			}
		}
		ArrayList<int[]> bl = new ArrayList<>(); ArrayList<Long> bv = new ArrayList<>();
		int mb = 0, lb = 0;
		for (int i = 0; i < b.length; i++) {
			mb = Math.max(mb, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				lb = Math.max(lb, b[i][j].length);
				for (int k = 0; k < b[i][j].length; k++)
					if (b[i][j][k] != 0) { bl.add(new int[]{i,j,k}); bv.add(b[i][j][k]); }
			}
		}
		if (av.isEmpty() || bv.isEmpty()) return zero();
		long[][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1];
		for (int k = 0; k < av.size(); k++) {
			long v = av.get(k); int[] p1 = al.get(k);
			for (int l = 0; l < bv.size(); l++) {
				int[] p2 = bl.get(l);
				c[p1[0]+p2[0]][p1[1]+p2[1]][p1[2]+p2[2]] = (c[p1[0]+p2[0]][p1[1]+p2[1]][p1[2]+p2[2]] + v * bv.get(l)) % mod;
			}
		}
		return c;
	}

	public long[][][] mul(long[][][] a, long b) {
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = poly2d.mul(a[i], b);
		}
		return res;
	}

	@Override
	public long[][][] mul(long[][][] a, long[][][] b) {
		return mulNaive(a, b);
	}
	
	public int degX(long[][][] a) {
		for (int i = a.length - 1; i >= 0; i--)
			if (poly2d.degX(a[i]) != -1)
				return i;
		return -1;
	}

	/**
	 * 多項式 f が 0 かを判定する。
	 * @param f 多項式
	 * @return f が 0 なら true
	 *
	 * <p>計算量: O(degX f * degY f * degZ f)
	 */
	public boolean isZero(long[][][] f) {
		return degX(f) == -1;
	}
	
	public long[][][] resize(long[][][] a) {
		int dx = degX(a);
		if (dx == -1)
			return zero();
		long[][][] r = new long[dx + 1][][];
		for (int i = 0; i <= dx; i++)
			r[i] = poly2d.resize(a[i]);
		return r;
	}
	
	/**
	 * 多項式の辞書順最大の主係数を返す。
	 * @param a 多項式
	 * @return 主係数
	 */
	public long lead(long[][][] a) {
		int dx = degX(a);
		if (dx == -1) return 0;
		long[][] leadMat = a[dx];
		int dy = poly2d.degX(leadMat);
		if (dy == -1) return 0;
		int dz = poly1d.deg(leadMat[dy]);
		if (dz == -1) return 0;
		return leadMat[dy][dz];
	}

	/**
	 * 多項式を monic（主係数が 1）にする。
	 * @param a 多項式
	 * @return monic 化された多項式
	 */
	public long[][][] monic(long[][][] a) {
		a = resize(a);
		long leadVal = lead(a);
		if (leadVal == 0) return a;
		long inv = fp.inv(leadVal);
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][];
			for (int j = 0; j < a[i].length; j++) {
				res[i][j] = new long[a[i][j].length];
				for (int k = 0; k < a[i][j].length; k++)
					res[i][j][k] = a[i][j][k] * inv % mod;
			}
		}
		return res;
	}

	/**
	 * 多項式 f を点 (x, y, z) で評価する。
	 * @param f 多項式
	 * @param x 評価点 x
	 * @param y 評価点 y
	 * @param z 評価点 z
	 * @return f(x, y, z)
	 */
	public long eval(long[][][] f, long x, long y, long z) {
		long res = 0;
		for (int i = f.length - 1; i >= 0; i--) {
			res = (res * x + poly2d.eval(f[i], y, z)) % mod;
		}
		return res;
	}

	/**
	 * 全次数を返す。計算量: O(項数)
	 */
	public int totalDegree(long[][][] a) {
		int res = -1;
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < a[i][j].length; k++) {
					if (a[i][j][k] != 0) res = Math.max(res, i + j + k);
				}
			}
		}
		return res;
	}

	/**
	 * 項数を返す。計算量: O(項数)
	 */
	public int countTerms(long[][][] a) {
		int res = 0;
		for (long[][] mat : a) {
			for (long[] row : mat) {
				for (long val : row) {
					if (val != 0) res++;
				}
			}
		}
		return res;
	}

	/**
	 * primitiveな多項式の既約性を判定する。
	 * primitiveでない多項式を与えた場合の動作は保証されない。
	 * ヒルベルトの既約性定理に基づき、ランダムな1変数への射影を用いて判定する。
	 * @param f 判定対象の多項式
	 * @return 既約である可能性が高い場合は true
	 */
	public boolean isIrreducibleHeuristicForPrimitive(long[][][] f) {
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0) return false;
		f = monic(f);
		int dx = degX(f);
		Random rnd = new Random();
		for (int t = 0; t < 5; t++) {
			long cy = Math.floorMod(rnd.nextLong(), mod);
			long cz = Math.floorMod(rnd.nextLong(), mod);
			long[] f_xyz = new long[dx + 1];
			for (int i = 0; i <= dx; i++) {
				f_xyz[i] = poly2d.eval(f[i], cy, cz);
			}
			if (poly1d.deg(f_xyz) != dx) continue;
			if (poly1d.isIrreducible(f_xyz)) return true;
		}
		return false;
	}

	/**
	 * xの係数のgcd
	 * @param a
	 * @return
	 */
	public long[][] contentX(long[][][] a) {
		long[][] g = poly2d.zero();
		for (long[][] mat : a)
			g = poly2d.gcd(g, mat);
		return g;
	}

	/**
	 * pはy,zの多項式
	 * @param a
	 * @param p
	 * @return
	 */
	public long[][][] lexdivByPolyYZ(long[][][] a, long[][] p) {
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly2d.lexdiv(a[i], p);
		return res;
	}

	public long[][][] mulByPoly2D(long[][][] a, long[][] p) {
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly2d.mul(a[i], p);
		return res;
	}

	/** {@code f(x,y,z+c)} を返す。未テスト。計算量: O(dx * dy * Shift1D(degZ f)) */
	private long[][][] shiftZ(long[][][] f, long c) {
		f = resize(f);
		long[][][] res = new long[f.length][][];
		for (int i = 0; i < f.length; i++) {
			res[i] = new long[f[i].length][];
			for (int j = 0; j < f[i].length; j++) res[i][j] = poly1d.taylorShift(f[i][j], poly1d.fp.reduce(c));
		}
		return resize(res);
	}

	/** {@code a} の z 次数を {@code nz} 未満へ切り詰める。未テスト。計算量: O(項数) */
	private long[][][] truncateZ(long[][][] a, int nz) {
		a = resize(a);
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][];
			for (int j = 0; j < a[i].length; j++) res[i][j] = Arrays.copyOf(a[i][j], Math.min(nz, a[i][j].length));
		}
		return resize(res);
	}

	/** {@code [z^k] a(x,y,z)} を返す。未テスト。計算量: O(dx * dy) */
	private long[][] coeffZ(long[][][] a, int k) {
		a = resize(a);
		long[][] res = new long[a.length][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length];
			for (int j = 0; j < a[i].length; j++) if (k < a[i][j].length) res[i][j] = a[i][j][k];
		}
		return poly2d.resize(res);
	}

	public static class DivModResult {
		public long[][][] q;
		public long[][][] r;
		public DivModResult(long[][][] q, long[][][] r) {
			this.q = q;
			this.r = r;
		}
	}

	/**
	 * 多項式の因子とその重複度を保持するクラス。
	 */
	public static class Factor {
		/** 既約因子 */
		public long[][][] factor;
		/** 重複度 */
		public int multiplicity;
		public Factor(long[][][] factor, int multiplicity) {
			this.factor = factor;
			this.multiplicity = multiplicity;
		}
	}

	/**
	 * 多項式の因数分解結果を保持するレコード。
	 * @param leadingCoeff 主係数
	 * @param factors 既約因子の配列（すべて monic）
	 */
	public record FactorResult3D(long leadingCoeff, Factor[] factors) {}

	/**
	 * Wang の主係数トリックの前進変換結果を保持する。
	 * @param leadingCoeff {@code L(y,z)=[x^d]F(x,y,z)}
	 * @param factorCount 評価点で得た因子数 {@code k}
	 * @param mainDegree {@code d=deg_x F}
	 * @param monicPolynomial {@code F^*(X,y,z)=L(y,z)^{d-1}F(X/L(y,z),y,z)}
	 */
	public record WangTransform3D(long[][] leadingCoeff, int factorCount, int mainDegree, long[][][] monicPolynomial) {}

	public static class SquareFreeFactor {
		public long[][][] factor;
		public int multiplicity;
		public SquareFreeFactor(long[][][] factor, int multiplicity) {
			this.factor = factor;
			this.multiplicity = multiplicity;
		}
	}

	public SquareFreeFactor[] squareFreeDecomposition(long[][][] inputf) {
		long[][][] f = resize(inputf);
		if (degX(f) == -1) return new SquareFreeFactor[0];
		ArrayList<SquareFreeFactor> result = new ArrayList<>();
		long[][] content = contentX(f);
		long[][][] prim = lexdivByPolyYZ(f, content);
		result.addAll(squareFreePrimitiveByYun(prim));
		PolynomialFpDynamic2D.SquareFreeFactor[] contentSqf = poly2d.squareFreeDecomposition(content);
		for (PolynomialFpDynamic2D.SquareFreeFactor cf : contentSqf) {
			long[][][] lifted = new long[][][] { cf.factor };
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

	private ArrayList<SquareFreeFactor> squareFreePrimitiveByYun(long[][][] primitive) {
		ArrayList<SquareFreeFactor> out = new ArrayList<>();
		long[][][] f = monic(primitive);
		if (degX(f) <= 0) return out;
		long[][][] fp = diffX(f);
		long[][][] g = gcd(f, fp);
		long[][][] p = lexdiv(f, g);
		long[][][] q = lexdiv(fp, g);
		for (int i = 1; ; i++) {
			long[][][] h = resize(sub(q, diffX(p)));
			if (degX(h) == -1) {
				if (degX(p) > 0) out.add(new SquareFreeFactor(monic(p), i));
				break;
			}
			long[][][] gi = gcd(p, h);
			if (degX(gi) > 0) out.add(new SquareFreeFactor(monic(gi), i));
			p = lexdiv(p, gi);
			q = lexdiv(h, gi);
		}
		return out;
	}

	private long[][][] diffX(long[][][] a) {
		a = resize(a);
		if (a.length <= 1) return zero();
		long[][][] res = new long[a.length - 1][][];
		for (int i = 1; i < a.length; i++) res[i - 1] = poly2d.mul(a[i], i % mod);
		return resize(res);
	}

	/**
	 * 多項式を既約分解する。
	 * Kronecker置換を用いて1変数多項式の因数分解に帰着させる。
	 * @param inputf 因数分解対象の多項式
	 * @return 既約因子の配列
	 */
	public FactorResult3D factor(long[][][] inputf) {
		inputf = resize(inputf);
		if (degX(inputf) == -1) return new FactorResult3D(0, new Factor[0]);
		long coef = lead(inputf);
		if (degX(inputf) == 0 && degY(inputf) == 0 && degZ(inputf) == 0) return new FactorResult3D(coef, new Factor[0]);

		ArrayList<Factor> ret = new ArrayList<>();
		long[][] content = contentX(inputf);
		PolynomialFpDynamic2D.FactorResult2D contentFR = poly2d.factor(content);
		for (PolynomialFpDynamic2D.Factor f2d : contentFR.factors()) {
			ret.add(new Factor(new long[][][] { f2d.factor }, f2d.multiplicity));
		}

		long[][][] primitive = lexdivByPolyYZ(inputf, content);
		if (degX(primitive) > 0 && isIrreducibleHeuristicForPrimitive(primitive)) {
			ret.add(new Factor(monic(primitive), 1));
			return new FactorResult3D(coef, ret.toArray(new Factor[ret.size()]));
		}
		ArrayList<SquareFreeFactor> sqfFactors = squareFreePrimitiveByYun(primitive);
		for (SquareFreeFactor sqf : sqfFactors) {
			Factor[] factors = factorByWang(sqf.factor);
			for (Factor f : factors) {
				ret.add(new Factor(f.factor, f.multiplicity * sqf.multiplicity));
			}
		}
		return new FactorResult3D(coef, ret.toArray(new Factor[ret.size()]));
	}


	/**
	 * Kronecker置換を用いて多項式を既約分解する。
	 * @param inputf 因数分解対象の多項式
	 * @return 既約因子の配列
	 */
	public Factor[] factorByKronecker(long[][][] inputf) {
		long[][][] f = monic(inputf);
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0) return new Factor[0];
		ArrayList<long[][][]> raw = new ArrayList<>();
		factorDfs(f, raw);
		ArrayList<Factor> ret = new ArrayList<>();
		for (long[][][] g : raw) {
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

	/** Kronecker-Wang/EEZ の再帰分解本体。計算量: O(2^s * Mul3D + Factor1D(D)) */
	private void factorDfs(long[][][] f, ArrayList<long[][][]> out) {
		f = monic(f);
		if (degX(f) <= 0 && degY(f) <= 0 && degZ(f) <= 0) return;
		long[][][] d = findWangEezFactor(f);
		if (d == null) {
			out.add(f);
			return;
		}
		long[][][] q = lexdiv(f, d);
		if (!equals(mul(d, q), f) || equals(d, one()) || equals(q, one())) {
			out.add(f);
			return;
		}
		factorDfs(d, out);
		factorDfs(q, out);
	}

	/** 1変数に落とした因子の積から、元の多項式を割る真の因子を探す。計算量: O(2^s * (Mul1D + Div3D + Mul3D)) */
	private long[][][] findWangEezFactor(long[][][] f) {
		int strideZ = Math.max(1, degZ(f) + 1);
		int strideY = Math.max(1, degY(f) + 1);
		long[] flat = flattenKronecker(f, strideY, strideZ);
		PolynomialFpDynamic.Factor[] fs = poly1d.factor(flat).factors();
		ArrayList<long[]> factors = new ArrayList<>();
		for (PolynomialFpDynamic.Factor e : fs)
			for (int i = 0; i < e.multiplicity; i++)
				factors.add(e.factor);
		if (factors.size() <= 1) return null;
		boolean[] used = new boolean[factors.size()];
		for (int sz = 1; sz <= factors.size() / 2; sz++) {
			long[][][] d = findFactorSubsetDfs(f, factors, used, 0, sz, new long[] {1}, strideY, strideZ);
			if (d != null) return d;
		}
		return null;
	}

	/** 指定サイズの1変数因子部分集合を試す。計算量: O(C(s,k) * (Mul1D + Div3D + Mul3D)) */
	private long[][][] findFactorSubsetDfs(long[][][] f, ArrayList<long[]> factors, boolean[] used, int from, int need,
			long[] prod, int strideY, int strideZ) {
		if (need == 0) {
			long[][][] candidate = unflattenKronecker(prod, strideY, strideZ, degX(f), degY(f), degZ(f));
			if (candidate == null || degX(candidate) <= 0 && degY(candidate) <= 0 && degZ(candidate) <= 0) return null;
			candidate = monic(candidate);
			if (equals(candidate, f)) return null;
			try {
				long[][][] q = lexdiv(f, candidate);
				if (equals(mul(candidate, q), f)) return candidate;
			} catch (RuntimeException e) {
				return null;
			}
			return null;
		}
		for (int i = from; i <= factors.size() - need; i++) {
			if (used[i]) continue;
			used[i] = true;
			long[][][] res = findFactorSubsetDfs(f, factors, used, i + 1, need - 1, poly1d.mul(prod, factors.get(i)),
					strideY, strideZ);
			used[i] = false;
			if (res != null) return res;
		}
		return null;
	}

	/** monic な gcd を返す。 */
	@Override
	public long[][][] gcd(long[][][] a, long[][][] b) {
		a = resize(a);
		b = resize(b);
		if (degX(a) == -1)
			return monic(b);
		if (degX(b) == -1)
			return monic(a);

		long[][] contA = contentX(a);
		long[][] contB = contentX(b);
		long[][] gCont = poly2d.gcd(contA, contB);

		long[][][] primA = lexdivByPolyYZ(a, contA);
		long[][][] primB = lexdivByPolyYZ(b, contB);
		long[][][] resPrim = gcdZippel(primA, primB);
		if (resPrim == null) resPrim = gcdByKroneckerIfDivisible(primA, primB);
		if (resPrim == null) resPrim = superGcd(primA, primB);
		return monic(mulByPoly2D(resPrim, gCont));
	}

	/**
	 * Zippelの確率的モジュラーGCDアルゴリズムを用いてGCDを計算する。
	 */
	public long[][][] gcdZippel(long[][][] f, long[][][] g) {
		f = resize(f);
		g = resize(g);
		if (degX(f) == -1) return monic(g);
		if (degX(g) == -1) return monic(f);
		if (degX(f) < degX(g)) { long[][][] t = f; f = g; g = t; }

		long[][] lcf = f[degX(f)];
		long[][] lcg = g[degX(g)];

		Random rnd = new Random();
		for (int attempt = 0; attempt < 20; attempt++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			long rz = poly1d.fp.reduce(rnd.nextLong());
			if (poly2d.eval(lcf, ry, rz) == 0 || poly2d.eval(lcg, ry, rz) == 0) continue;

			long[] f_r = new long[f.length];
			for (int i = 0; i < f.length; i++) f_r[i] = poly2d.eval(f[i], ry, rz);
			long[] g_r = new long[g.length];
			for (int i = 0; i < g.length; i++) g_r[i] = poly2d.eval(g[i], ry, rz);

			long[] gcd_r = poly1d.gcd(f_r, g_r);
			int[] skeleton = new int[gcd_r.length];
			int skelSize = 0;
			for (int i = 0; i < gcd_r.length; i++) if (gcd_r[i] != 0) skeleton[skelSize++] = i;
			skeleton = Arrays.copyOf(skeleton, skelSize);

			long[][][] G = new long[skeleton[skelSize - 1] + 1][][];
			boolean success = true;
			for (int i : skeleton) {
				G[i] = interpolateZippel(f, g, i, ry, rz, gcd_r[i], skeleton);
				if (G[i] == null) { success = false; break; }
			}
			if (!success) continue;

			long[][][] G_full = new long[G.length][][];
			for (int i = 0; i < G.length; i++) G_full[i] = G[i] == null ? poly2d.zero() : G[i];
			long[][][] res = resize(G_full);
			try {
				if (isDivisible(f, res) && isDivisible(g, res)) return monic(res);
			} catch (Exception e) {}
		}
		return null;
	}

	private long[][] interpolateZippel(long[][][] f, long[][][] g, int xIdx, long ry, long rz, long targetVal, int[] skeleton) {
		long[][] lcf = f[degX(f)];
		long[][] lcg = g[degX(g)];
		Random rnd = new Random();

		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<Long> valuesY = new ArrayList<>();
		pointsY.add(ry);
		valuesY.add(targetVal);

		long[] currentCoeffPolyY = {targetVal};
		int retryY = 0;
		while (true) {
			if (++retryY > 100) return null;
			long ry2 = poly1d.fp.reduce(rnd.nextLong());
			if (poly2d.eval(lcf, ry2, rz) == 0 || poly2d.eval(lcg, ry2, rz) == 0) continue;

			long[] f_ry2 = new long[f.length];
			for (int j = 0; j < f.length; j++) f_ry2[j] = poly2d.eval(f[j], ry2, rz);
			long[] g_ry2 = new long[g.length];
			for (int j = 0; j < g.length; j++) g_ry2[j] = poly2d.eval(g[j], ry2, rz);

			long[] gcd_ry2 = poly1d.gcd(f_ry2, g_ry2);
			if (poly1d.deg(gcd_ry2) != skeleton[skeleton.length-1]) continue;

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
			if (poly2d.eval(lcf, ry, rz2) == 0 || poly2d.eval(lcg, ry, rz2) == 0) continue;

			long[] coeffY_rz2 = interpolateYwithFixedZ(f, g, xIdx, rz2, skeleton);
			if (coeffY_rz2 == null) continue;

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

			if (poly2d.equals(nextCoeffPolyYZ, currentCoeffPolyYZ)) break;
			currentCoeffPolyYZ = nextCoeffPolyYZ;
			if (pointsZ.size() > degZ(f) + 1) break;
		}
		return currentCoeffPolyYZ;
	}

	private long[] interpolateYwithFixedZ(long[][][] f, long[][][] g, int xIdx, long rz, int[] skeleton) {
		long[][] lcf = f[degX(f)];
		long[][] lcg = g[degX(g)];
		Random rnd = new Random();
		ArrayList<Long> pointsY = new ArrayList<>();
		ArrayList<Long> valuesY = new ArrayList<>();

		long[] currentCoeffPolyY = null;
		for (int t=0; t < degY(f) + 2; t++) {
			long ry = poly1d.fp.reduce(rnd.nextLong());
			if (poly2d.eval(lcf, ry, rz) == 0 || poly2d.eval(lcg, ry, rz) == 0) continue;

			long[] f_ry = new long[f.length];
			for (int i = 0; i < f.length; i++) f_ry[i] = poly2d.eval(f[i], ry, rz);
			long[] g_ry = new long[g.length];
			for (int i = 0; i < g.length; i++) g_ry[i] = poly2d.eval(g[i], ry, rz);

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

	private boolean isDivisible(long[][][] a, long[][][] b) {
		if (degX(b) == -1) return false;
		try {
			long[][][] q = lexdiv(a, b);
			return equals(mul(b, q), a);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 1/f の指定された項の係数を効率的に計算するための最適な分解 f = ph - q を求める。
	 * 複雑度スコア Σ(a_i-1) を最小化する分解を探索する。ここで a_i は p, h, q の各既約因子の項数である。
	 * f[0][0][0]=1を仮定
	 * @param inputf 多項式 f
	 * @return 最適な分解結果
	 */
	public BestDecomposition3D findBestDecomposition(long[][][] inputf) {
		if(inputf[0][0][0]!=1)throw new AssertionError();
		long[][][] f = resize(inputf);
		if (degX(f) == -1) return new BestDecomposition3D(0, new Factor[0], 0, new Factor[0], 0);

		// f の非ゼロ項を抽出する（定数項以外）
		ArrayList<int[]> fTerms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					f[i][j][k]=(f[i][j][k]%mod+mod)%mod;
					if (i == 0 && j == 0 && k == 0) continue;
					if (f[i][j][k] != 0) {
						fTerms.add(new int[] {i, j, k, (int) f[i][j][k]});
					}
				}
			}
		}
		ArrayList<long[][][]> candidatesP = new ArrayList<>();
		enumerateSparsePolynomialsFromF(Math.max(1, f.length), Math.max(1, f[0].length), Math.max(1, f[0][0].length), fTerms, 1, candidatesP);
		ArrayList<BestCandidate3D> scoredP = new ArrayList<>();
		for (long[][][] p : candidatesP) {
			ArrayList<int[]> pTerms = new ArrayList<>();
			for (int i = 0; i < p.length; i++) for (int j = 0; j < p[i].length; j++) for (int k = 0; k < p[i][j].length; k++)
				if (p[i][j][k] != 0) pTerms.add(new int[] {i, j, k, (int) p[i][j][k]});
			scoredP.add(new BestCandidate3D(p, pTerms, scoreFactors(factor(p).factors())));
		}
		BestDecomposition3D best = computeDecompositionResult(one(), f, f);
		scoredP.sort((x, y) -> Long.compare(x.score, y.score));
		for (var sh : scoredP) {
			for (var sp : scoredP) {
				if (best.score != -1 && sh.score + sp.score >= best.score) continue;
				BestDecomposition3D current = computeDecompositionResult(sp.poly, sh.poly, f);
				if (best.score == -1 || current.score < best.score) best = current;
			}
		}
		return best;
	}

	private record BestCandidate3D(long[][][] poly, ArrayList<int[]> terms, long score) {}

	private long scoreFactors(Factor[] factors) {
		long s = 0;
		for (Factor f : factors) s += countTerms(f.factor) - 1;
		return s;
	}

	private void enumerateSparsePolynomialsFromF(int dx, int dy, int dz, ArrayList<int[]> fTerms, long constTerm, ArrayList<long[][][]> out) {
		// 0項 (1)
		long[][][] res0 = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)];
		res0[0][0][0] = constTerm;
		out.add(resize(res0));

		// 1項 (1 + term_i)
		for (int i = 0; i < fTerms.size(); i++) {
			int[] t = fTerms.get(i);
			long[][][] res = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)];
			res[0][0][0] = constTerm;
			if(t[0] < res.length && t[1] < res[t[0]].length && t[2] < res[t[0]][t[1]].length) res[t[0]][t[1]][t[2]] = t[3];
			out.add(resize(res));
		}

		// 2項 (1 + term_i + term_j)
		for (int i = 0; i < fTerms.size(); i++) {
			for (int j = i + 1; j < fTerms.size(); j++) {
				int[] t1 = fTerms.get(i);
				int[] t2 = fTerms.get(j);
				long[][][] res = new long[Math.max(1, dx)][Math.max(1, dy)][Math.max(1, dz)];
				res[0][0][0] = constTerm;
				if(t1[0] < res.length && t1[1] < res[t1[0]].length && t1[2] < res[t1[0]][t1[1]].length) res[t1[0]][t1[1]][t1[2]] = t1[3];
				if(t2[0] < res.length && t2[1] < res[t2[0]].length && t2[2] < res[t2[0]][t2[1]].length) res[t2[0]][t2[1]][t2[2]] = t2[3];
				out.add(resize(res));
			}
		}
	}

	private BestDecomposition3D computeDecompositionResult(long[][][] p, long[][][] h, long[][][] f) {
		long[][][] ph = mul(p, h);
		long[][][] q = sub(ph, f);
		Factor[] phFactors = factor(ph).factors();
		Factor[] qFactors = factor(q).factors();

		long phConst = (lead(p) * lead(h)) % mod;
		long qConst = lead(q);

		long score = 0;
		for (Factor ef : phFactors) {
			score+=countTerms(ef.factor)-1;
		}
		for (Factor ef : qFactors) {
			score+=countTerms(ef.factor)-1;
		}
		return new BestDecomposition3D(phConst, phFactors, qConst, qFactors, score);
	}

	/** Kronecker 置換でgcd候補を作り、割り切れる場合だけ返す。計算量: O(GCD1D(degX*degY*degZ) + S) */
	private long[][][] gcdByKroneckerIfDivisible(long[][][] a, long[][][] b) {
		int maxY = Math.max(degY(a), degY(b));
		int maxZ = Math.max(degZ(a), degZ(b));
		int strideZ = maxZ + 1, strideY = maxY + 1;
		long[] f = flattenKronecker(a, strideY, strideZ);
		long[] g = flattenKronecker(b, strideY, strideZ);
		long[] h = poly1d.gcd(f, g);
		long[][][] candidate = unflattenKronecker(h, strideY, strideZ, Math.min(degX(a), degX(b)),
				Math.min(degY(a), degY(b)), Math.min(degZ(a), degZ(b)));
		if (candidate == null)
			return null;
		candidate = resize(candidate);
		if (degX(candidate) == -1)
			return one();
		long[][][] qa = lexdiv(a, candidate);
		if (!equals(mul(candidate, qa), a))
			return null;
		long[][][] qb = lexdiv(b, candidate);
		if (!equals(mul(candidate, qb), b))
			return null;
		return candidate;
	}

	/** 3変数多項式を x=t^(strideY*strideZ), y=t^strideZ, z=t として詰める。計算量: O(項数) */
	private long[] flattenKronecker(long[][][] a, int strideY, int strideZ) {
		int len = degX(a) * strideY * strideZ + degY(a) * strideZ + degZ(a) + 1;
		long[] res = new long[Math.max(0, len)];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				for (int k = 0; k < a[i][j].length; k++) {
					if (a[i][j][k] == 0)
						continue;
					res[(i * strideY + j) * strideZ + k] = a[i][j][k];
				}
		return poly1d.resize(res);
	}

	/** Kronecker 置換したgcd候補を3変数へ戻す。計算量: O(次数) */
	private long[][][] unflattenKronecker(long[] a, int strideY, int strideZ, int maxX, int maxY, int maxZ) {
		a = poly1d.resize(a);
		if (a.length == 0)
			return zero();
		long[][][] res = new long[maxX + 1][maxY + 1][maxZ + 1];
		for (int idx = 0; idx < a.length; idx++) {
			if (a[idx] == 0)
				continue;
			int x = idx / (strideY * strideZ);
			int rem = idx % (strideY * strideZ);
			int y = rem / strideZ;
			int z = rem % strideZ;
			if (x > maxX || y > maxY || z > maxZ)
				return null;
			res[x][y][z] = a[idx];
		}
		return resize(res);
	}

	/** y次数を返す。計算量: O(項数) */
	public int degY(long[][][] a) {
		int res = -1;
		for (long[][] mat : a)
			res = Math.max(res, poly2d.degX(mat));
		return res;
	}

	/** z次数を返す。計算量: O(項数) */
	public int degZ(long[][][] a) {
		int res = -1;
		for (long[][] mat : a)
			for (long[] row : mat)
				res = Math.max(res, poly1d.deg(row));
		return res;
	}

	private long[][][] superGcd(long[][][] a, long[][][] b) {
		var field = new FractionFieldStrategy<>(poly2d);
		var strategy = new PolynomialEuclideanStrategy<>(field);
		return fromFractionArray(strategy.gcd(toFractionArray(a), toFractionArray(b)));
	}

	private FractionFieldElement<long[][]>[] toFractionArray(long[][][] a) {
		@SuppressWarnings("unchecked")
		FractionFieldElement<long[][]>[] res = new FractionFieldElement[a.length];
		var field = new FractionFieldStrategy<>(poly2d);
		for (int i = 0; i < a.length; i++)
			res[i] = field.of(poly2d.resize(a[i]), poly2d.one());
		return res;
	}

	private long[][][] fromFractionArray(FractionFieldElement<long[][]>[] a) {
		long[][] commonDen = poly2d.one();
		for (var f : a) {
			long[][] g = poly2d.gcd(commonDen, f.den());
			commonDen = poly2d.mul(commonDen, poly2d.lexdiv(f.den(), g));
		}
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++)
			res[i] = poly2d.mul(a[i].num(), poly2d.lexdiv(commonDen, a[i].den()));
		long[][] c = contentX(res);
		return lexdivByPolyYZ(res, c);
	}

	public long[][][] invNaive(long[][][] a) {
		if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0] == 0)
			throw new AssertionError();
		int m0 = 0, l0 = 0;
		for (long[][] mat : a) {
			m0 = Math.max(m0, mat.length);
			for (long[] row : mat)
				l0 = Math.max(l0, row.length);
		}
		long[][][] b = new long[a.length][m0][l0];
		long inva000 = fp.inv(a[0][0][0]);
		b[0][0][0] = inva000;
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				for (int k = 0; k < b[i][j].length; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					for (int i2 = 0; i2 <= i; i2++) {
						if (i2 >= a.length)
							continue;
						for (int j2 = 0; j2 <= j; j2++) {
							if (j2 >= a[i2].length)
								continue;
							for (int k2 = 0; k2 <= k; k2++) {
								if (i2 == 0 && j2 == 0 && k2 == 0)
									continue;
								if (k2 >= a[i2][j2].length)
									continue;
								b[i][j][k] = (b[i][j][k] - a[i2][j2][k2] * b[i - i2][j - j2][k - k2] % mod + mod) % mod;
							}
						}
					}
					b[i][j][k] = b[i][j][k] * inva000 % mod;
				}
			}
		}
		return b;
	}

	/**
	 * 3変数の多項式の項を表すレコード。
	 * @param dx xの次数
	 * @param dy yの次数
	 * @param dz zの次数
	 * @param v 係数
	 */
	public record Term3D(int dx, int dy, int dz, long v) {}

	public long[][][] sparseMul(long[][][] a, long[][][] sparsePoly) {
		ArrayList<Term3D> terms = new ArrayList<>();
		int maxDx = 0, maxDy = 0, maxDz = 0;
		for (int i = 0; i < sparsePoly.length; i++) {
			for (int j = 0; j < sparsePoly[i].length; j++) {
				for (int k = 0; k < sparsePoly[i][j].length; k++) {
					if (sparsePoly[i][j][k] != 0) {
						terms.add(new Term3D(i, j, k, poly1d.fp.reduce(sparsePoly[i][j][k])));
						maxDx = Math.max(maxDx, i);
						maxDy = Math.max(maxDy, j);
						maxDz = Math.max(maxDz, k);
					}
				}
			}
		}
		int ni = a.length, nj = 0, nk = 0;
		for (long[][] row : a) {
			nj = Math.max(nj, row.length);
			for (long[] col : row)
				nk = Math.max(nk, col.length);
		}
		if (ni == 0 || terms.isEmpty())
			return zero();
		long[][][] b = new long[ni + maxDx][nj + maxDy][nk + maxDz];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				for (int k = 0; k < b[i][j].length; k++) {
					for (Term3D t : terms) {
						int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
						if (pi >= 0 && pi < a.length && pj >= 0 && pj < a[pi].length && pk >= 0 && pk < a[pi][pj].length) {
							b[i][j][k] = (b[i][j][k] + t.v * a[pi][pj][pk]) % mod;
						}
					}
				}
			}
		}
		return b;
	}

	public long[][][] sparseExp(long[][][] f, int ni, int nj, int nk) {
		if (ni <= 0 || nj <= 0 || nk <= 0)
			return zero();
		if (f.length > 0 && f[0].length > 0 && f[0][0].length > 0 && poly1d.fp.reduce(f[0][0][0]) != 0)
			throw new ArithmeticException("f[0][0][0] must be zero");
		ArrayList<Term3D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (f[i][j][k] != 0)
						terms.add(new Term3D(i, j, k, poly1d.fp.reduce(f[i][j][k])));
				}
			}
		}
		long[][][] g = new long[ni][nj][nk];
		g[0][0][0] = 1;
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					long tmp = 0;
					if (i > 0) {
						for (Term3D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
							if (pi >= 0 && pj >= 0 && pk >= 0 && pi < ni && pj < nj && pk < nk) {
								tmp = (tmp + t.dx * t.v % mod * g[pi][pj][pk]) % mod;
							}
						}
						g[i][j][k] = tmp * MathUtils.modInv(i, mod) % mod;
					} else if (j > 0) {
						for (Term3D t : terms) {
							if (t.dx != 0)
								continue;
							int pj = j - t.dy, pk = k - t.dz;
							if (pj >= 0 && pk >= 0 && pj < nj && pk < nk) {
								tmp = (tmp + t.dy * t.v % mod * g[0][pj][pk]) % mod;
							}
						}
						g[i][j][k] = tmp * MathUtils.modInv(j, mod) % mod;
					} else {
						for (Term3D t : terms) {
							if (t.dx != 0 || t.dy != 0)
								continue;
							int pk = k - t.dz;
							if (pk >= 0 && pk < nk) {
								tmp = (tmp + t.dz * t.v % mod * g[0][0][pk]) % mod;
							}
						}
						g[i][j][k] = tmp * MathUtils.modInv(k, mod) % mod;
					}
				}
			}
		}
		return g;
	}

	public long[][][] sparsePow(long[][][] f, int ni, int nj, int nk, long p) {
		if (ni <= 0 || nj <= 0 || nk <= 0)
			return zero();
		if (p == 0) {
			long[][][] res = new long[ni][nj][nk];
			res[0][0][0] = 1;
			return res;
		}
		int d0i = -1, d0j = -1, d0k = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (f[i][j][k] != 0) {
						d0i = i;
						d0j = j;
						d0k = k;
						break outer;
					}
				}
			}
		}
		if (d0i == -1)
			return new long[ni][nj][nk];
		if ((d0i > 0 && (ni - 1) / d0i < p) || (d0j > 0 && (nj - 1) / d0j < p) || (d0k > 0 && (nk - 1) / d0k < p))
			return new long[ni][nj][nk];
		int bi = (int) (d0i * p), bj = (int) (d0j * p), bk = (int) (d0k * p);
		if (bi >= ni || bj >= nj || bk >= nk)
			return new long[ni][nj][nk];
		ArrayList<Term3D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (i == d0i && j == d0j && k == d0k)
						continue;
					if (f[i][j][k] != 0)
						terms.add(new Term3D(i - d0i, j - d0j, k - d0k, poly1d.fp.reduce(f[i][j][k])));
				}
			}
		}
		long[][][] res = new long[ni][nj][nk];
		res[bi][bj][bk] = MathUtils.modPow(poly1d.fp.reduce(f[d0i][d0j][d0k]), p % (mod - 1), mod);
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0i][d0j][d0k]), mod);
		long pMod = p % mod;
		for (int i = bi; i < ni; i++) {
			for (int j = bj; j < nj; j++) {
				for (int k = bk; k < nk; k++) {
					if (i == bi && j == bj && k == bk)
						continue;
					long tmp = 0;
					if (i > bi) {
						int n = i - bi;
						for (Term3D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
							if (pi >= bi && pj >= bj && pk >= bk && pi < ni && pj < nj && pk < nk) {
								long val = ((pMod + 1) * t.dx % mod - n + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[pi][pj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
					} else if (j > bj) {
						int m = j - bj;
						for (Term3D t : terms) {
							if (t.dx != 0)
								continue;
							int pj = j - t.dy, pk = k - t.dz;
							if (pj >= bj && pk >= bk && pj < nj && pk < nk) {
								long val = ((pMod + 1) * t.dy % mod - m + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[bi][pj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
					} else {
						int l = k - bk;
						for (Term3D t : terms) {
							if (t.dx != 0 || t.dy != 0)
								continue;
							int pk = k - t.dz;
							if (pk >= bk && pk < nk) {
								long val = ((pMod + 1) * t.dz % mod - l + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[bi][bj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(l, mod) % mod;
					}
				}
			}
		}
		return res;
	}

	public long[][][] sparseSqrt(long[][][] f, int ni, int nj, int nk) {
		if (ni <= 0 || nj <= 0 || nk <= 0)
			return zero();
		int d0i = -1, d0j = -1, d0k = -1;
		outer: for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (f[i][j][k] != 0) {
						d0i = i;
						d0j = j;
						d0k = k;
						break outer;
					}
				}
			}
		}
		if (d0i == -1)
			return new long[ni][nj][nk];
		if (d0i % 2 != 0 || d0j % 2 != 0 || d0k % 2 != 0)
			return null;
		long sqrt0 = MathUtils.modKthRoot(poly1d.fp.reduce(f[d0i][d0j][d0k]), 2, mod);
		if (sqrt0 == -1)
			return null;
		if (sqrt0 * 2 > mod)
			sqrt0 = mod - sqrt0;
		int bi = d0i / 2, bj = d0j / 2, bk = d0k / 2;
		if (bi >= ni || bj >= nj || bk >= nk)
			return new long[ni][nj][nk];
		ArrayList<Term3D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (i == d0i && j == d0j && k == d0k)
						continue;
					if (f[i][j][k] != 0)
						terms.add(new Term3D(i - d0i, j - d0j, k - d0k, poly1d.fp.reduce(f[i][j][k])));
				}
			}
		}
		long[][][] res = new long[ni][nj][nk];
		res[bi][bj][bk] = sqrt0;
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[d0i][d0j][d0k]), mod);
		long kMod = MathUtils.modInv(2, mod);
		for (int i = bi; i < ni; i++) {
			for (int j = bj; j < nj; j++) {
				for (int k = bk; k < nk; k++) {
					if (i == bi && j == bj && k == bk)
						continue;
					long tmp = 0;
					if (i > bi) {
						int n = i - bi;
						for (Term3D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
							if (pi >= bi && pj >= bj && pk >= bk && pi < ni && pj < nj && pk < nk) {
								long val = ((kMod + 1) * t.dx % mod - n + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[pi][pj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(n, mod) % mod;
					} else if (j > bj) {
						int m = j - bj;
						for (Term3D t : terms) {
							if (t.dx != 0)
								continue;
							int pj = j - t.dy, pk = k - t.dz;
							if (pj >= bj && pk >= bk && pj < nj && pk < nk) {
								long val = ((kMod + 1) * t.dy % mod - m + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[bi][pj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(m, mod) % mod;
					} else {
						int l = k - bk;
						for (Term3D t : terms) {
							if (t.dx != 0 || t.dy != 0)
								continue;
							int pk = k - t.dz;
							if (pk >= bk && pk < nk) {
								long val = ((kMod + 1) * t.dz % mod - l + mod) % mod;
								tmp = (tmp + val * t.v % mod * res[bi][bj][pk]) % mod;
							}
						}
						res[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(l, mod) % mod;
					}
				}
			}
		}
		return res;
	}

	public long[][][] sparseInv(long[][][] f, int ni, int nj, int nk) {
		if (ni <= 0 || nj <= 0 || nk <= 0)
			return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0].length == 0 || f[0][0][0] == 0)
			throw new ArithmeticException("f[0][0][0] must be non-zero");
		ArrayList<Term3D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					if (f[i][j][k] != 0)
						terms.add(new Term3D(i, j, k, poly1d.fp.reduce(f[i][j][k])));
				}
			}
		}
		long[][][] res = new long[ni][nj][nk];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0][0]), mod);
		res[0][0][0] = inv0;
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					long tmp = 0;
					for (Term3D t : terms) {
						int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
						if (pi >= 0 && pj >= 0 && pk >= 0 && pi < ni && pj < nj && pk < nk) {
							tmp = (tmp + t.v * res[pi][pj][pk]) % mod;
						}
					}
					res[i][j][k] = tmp == 0 ? 0 : (mod - tmp) % mod * inv0 % mod;
				}
			}
		}
		return res;
	}

	public long[][][] sparseInv(long[][][] a) {
		int ni = a.length, nj = 0, nk = 0;
		for (long[][] row : a) {
			nj = Math.max(nj, row.length);
			for (long[] col : row)
				nk = Math.max(nk, col.length);
		}
		return sparseInv(a, ni, nj, nk);
	}

	/**
	 * Buchbergerのアルゴリズムを用いてグレブナー基底を計算する。
	 * 単項式順序は辞書式順序 (x > y > z) を使用する。
	 * @param F 多項式の集合
	 * @return グレブナー基底
	 */
	public List<long[][][]> groebnerBasis(List<long[][][]> F) {
		List<long[][][]> G = new ArrayList<>();
		for (long[][][] f : F) {
			long[][][] res = normalForm(f, G);
			if (degX(res) != -1) G.add(monic(res));
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			int n = G.size();
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					long[][][] s = sPolynomial(G.get(i), G.get(j));
					long[][][] res = normalForm(s, G);
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
	public List<long[][][]> reducedGroebnerBasis(List<long[][][]> F) {
		List<long[][][]> G = groebnerBasis(F);
		List<long[][][]> G2 = new ArrayList<>();
		for (int i = 0; i < G.size(); i++) {
			long[][][] f = G.get(i);
			boolean redundant = false;
			Term3D ltF = leadTerm(f);
			for (int j = 0; j < G.size(); j++) {
				if (i == j) continue;
				Term3D ltG = leadTerm(G.get(j));
				if (ltF.dx() >= ltG.dx() && ltF.dy() >= ltG.dy() && ltF.dz() >= ltG.dz()) {
					redundant = true;
					break;
				}
			}
			if (!redundant) G2.add(f);
		}
		List<long[][][]> G3 = new ArrayList<>();
		for (int i = 0; i < G2.size(); i++) {
			List<long[][][]> other = new ArrayList<>(G2);
			long[][][] f = other.remove(i);
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
	public long[][][] normalForm(long[][][] f, List<long[][][]> G) {
		long[][][] r = zero();
		long[][][] p = resize(f);
		while (degX(p) != -1) {
			boolean divided = false;
			Term3D ltP = leadTerm(p);
			for (long[][][] g : G) {
				Term3D ltG = leadTerm(g);
				if (ltP.dx() >= ltG.dx() && ltP.dy() >= ltG.dy() && ltP.dz() >= ltG.dz()) {
					long val = fp.mul(ltP.v(), fp.inv(ltG.v()));
					p = subMulTerm(p, g, val, ltP.dx() - ltG.dx(), ltP.dy() - ltG.dy(), ltP.dz() - ltG.dz());
					divided = true;
					break;
				}
			}
			if (!divided) {
				r = addTerm(r, ltP.v(), ltP.dx(), ltP.dy(), ltP.dz());
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
	public long[][][] sPolynomial(long[][][] f, long[][][] g) {
		Term3D ltF = leadTerm(f);
		Term3D ltG = leadTerm(g);
		int mx = Math.max(ltF.dx(), ltG.dx());
		int my = Math.max(ltF.dy(), ltG.dy());
		int mz = Math.max(ltF.dz(), ltG.dz());
		long[][][] tF = mulTerm(f, fp.inv(ltF.v()), mx - ltF.dx(), my - ltF.dy(), mz - ltF.dz());
		long[][][] tG = mulTerm(g, fp.inv(ltG.v()), mx - ltG.dx(), my - ltG.dy(), mz - ltG.dz());
		return resize(sub(tF, tG));
	}

	/**
	 * 多項式の辞書順最大の主項を返す。
	 * @param a 多項式
	 * @return 主項の情報
	 */
	public Term3D leadTerm(long[][][] a) {
		int dx = degX(a);
		if (dx == -1) return new Term3D(-1, -1, -1, 0);
		long[][] leadMat = a[dx];
		int dy = poly2d.degX(leadMat);
		int dz = poly1d.deg(leadMat[dy]);
		return new Term3D(dx, dy, dz, leadMat[dy][dz]);
	}

	/** {@code a - b * v * x^dx * y^dy * z^dz} を返す。 */
	public long[][][] subMulTerm(long[][][] a, long[][][] b, long v, int dx, int dy, int dz) {
		int nx = Math.max(a.length, b.length + dx);
		int my = 0, lz = 0;
		for (long[][] mat : a) {
			my = Math.max(my, mat.length);
			for (long[] row : mat) lz = Math.max(lz, row.length);
		}
		for (long[][] mat : b) {
			my = Math.max(my, mat.length + dy);
			for (long[] row : mat) lz = Math.max(lz, row.length + dz);
		}
		long[][][] c = new long[nx][my][lz];
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < my; j++)
				for (int k = 0; k < lz; k++) {
					long va = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					long vb = (i >= dx && j >= dy && k >= dz && i - dx < b.length && j - dy < b[i - dx].length && k - dz < b[i - dx][j - dy].length) ? b[i - dx][j - dy][k - dz] : 0;
					c[i][j][k] = fp.sub(va, fp.mul(vb, v));
				}
		return resize(c);
	}

	/** {@code a + v * x^dx * y^dy * z^dz} を返す。 */
	public long[][][] addTerm(long[][][] a, long v, int dx, int dy, int dz) {
		int nx = Math.max(a.length, dx + 1);
		int my = 0, lz = 0;
		for (long[][] mat : a) {
			my = Math.max(my, mat.length);
			for (long[] row : mat) lz = Math.max(lz, row.length);
		}
		my = Math.max(my, dy + 1);
		lz = Math.max(lz, dz + 1);
		long[][][] c = new long[nx][my][lz];
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < my; j++)
				for (int k = 0; k < lz; k++) {
					long va = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					if (i == dx && j == dy && k == dz) va = fp.add(va, v);
					c[i][j][k] = va;
				}
		return resize(c);
	}

	/** 主項を取り除いた多項式を返す。 */
	public long[][][] removeLeadTerm(long[][][] a) {
		Term3D lt = leadTerm(a);
		if (lt.dx() == -1) return a;
		long[][][] c = resize(a);
		c[lt.dx()][lt.dy()][lt.dz()] = 0;
		return resize(c);
	}

	/** {@code a * v * x^dx * y^dy * z^dz} を返す。 */
	public long[][][] mulTerm(long[][][] a, long v, int dx, int dy, int dz) {
		if (degX(a) == -1) return zero();
		int nx = a.length + dx;
		int my = 0, lz = 0;
		for (long[][] mat : a) {
			my = Math.max(my, mat.length);
			for (long[] row : mat) lz = Math.max(lz, row.length);
		}
		my += dy; lz += dz;
		long[][][] c = new long[nx][my][lz];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				for (int k = 0; k < a[i][j].length; k++)
					c[i + dx][j + dy][k + dz] = fp.mul(a[i][j][k], v);
		return resize(c);
	}

	public long[][][] sparseLog(long[][][] f, int ni, int nj, int nk) {
		if (ni <= 0 || nj <= 0 || nk <= 0)
			return zero();
		if (f.length == 0 || f[0].length == 0 || f[0][0].length == 0 || f[0][0][0] == 0)
			throw new ArithmeticException("f[0][0][0] must be non-zero");
		ArrayList<Term3D> terms = new ArrayList<>();
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[i].length; j++) {
				for (int k = 0; k < f[i][j].length; k++) {
					if (f[i][j][k] != 0)
						terms.add(new Term3D(i, j, k, poly1d.fp.reduce(f[i][j][k])));
				}
			}
		}
		long[][][] g = new long[ni][nj][nk];
		long inv0 = MathUtils.modInv(poly1d.fp.reduce(f[0][0][0]), mod);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					long tmp = 0;
					if (i > 0) {
						for (Term3D t : terms) {
							int pi = i - t.dx, pj = j - t.dy, pk = k - t.dz;
							if (pi >= 0 && pj >= 0 && pk >= 0 && pi < ni && pj < nj && pk < nk) {
								if (i == t.dx && j == t.dy && k == t.dz)
									tmp = (tmp + i * t.v) % mod;
								else
									tmp = (tmp + mod - (i - t.dx) * t.v % mod * g[pi][pj][pk] % mod) % mod;
							}
						}
						g[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(i, mod) % mod;
					} else if (j > 0) {
						for (Term3D t : terms) {
							if (t.dx != 0)
								continue;
							int pj = j - t.dy, pk = k - t.dz;
							if (pj >= 0 && pk >= 0 && pj < nj && pk < nk) {
								if (j == t.dy && k == t.dz)
									tmp = (tmp + j * t.v) % mod;
								else
									tmp = (tmp + mod - (j - t.dy) * t.v % mod * g[0][pj][pk] % mod) % mod;
							}
						}
						g[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(j, mod) % mod;
					} else {
						for (Term3D t : terms) {
							if (t.dx != 0 || t.dy != 0)
								continue;
							int pk = k - t.dz;
							if (pk >= 0 && pk < nk) {
								if (k == t.dz)
									tmp = (tmp + k * t.v) % mod;
								else
									tmp = (tmp + mod - (k - t.dz) * t.v % mod * g[0][0][pk] % mod) % mod;
							}
						}
						g[i][j][k] = tmp * inv0 % mod * MathUtils.modInv(k, mod) % mod;
					}
				}
			}
		}
		return g;
	}
	
	
	
	/**
	 * Wang の主係数トリックにより {@code F(x,y,z)} を {@code X} に関して monic にする。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code F != 0}, {@code k >= 0}, {@code L(y,z)=[x^d]F != 0}。</li>
	 * <li>事後条件: {@code F^*(X,y,z)=sum_{0<=i<d} [x^i]F L^{d-1-i}X^i + X^d}。</li>
	 * <li>事後条件: {@code [X^d]F^*=1}。</li>
	 * <li>副作用: なし。返値は入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code F=0} または {@code k<0} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 入力が {@code F_p[y,z][x]} の係数配列表現でない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(d * (Pow2D + Mul2D))。
	 */
	public WangTransform3D wangTrickForward(long[][][] F, int k) {
		if (k < 0) throw new IllegalArgumentException("factor count must be non-negative");
		F = resize(F);
		int d = degX(F);
		if (d == -1) throw new IllegalArgumentException("zero polynomial has no leading coefficient");
		long[][] L = poly2d.resize(F[d]);
		long[][][] monicF = new long[d + 1][][];
		for (int i = 0; i < d; i++) {
			int exponent = d - 1 - i;
			long[][] scale = exponent == 0 ? poly2d.one() : powFull2D(L, exponent);
			monicF[i] = poly2d.mul(F[i], scale);
		}
		monicF[d] = poly2d.one();
		return new WangTransform3D(ArrayUtils.copy(L), k, d, resize(monicF));
	}

	/**
	 * Wang の主係数トリックで monic 化された因子を逆変換し、{@code x} に関する原始部分を返す。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code L(y,z) != 0}, {@code monicFactors != null}。</li>
	 * <li>事後条件: 返値 {@code H_i=pp_x(G_i(Lx,y,z))}。</li>
	 * <li>事後条件: {@code content_x(H_i)=1}。零因子は零のまま返す。</li>
	 * <li>副作用: なし。返値は入力配列と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: {@code L=0} または {@code monicFactors=null} なら {@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 係数が content で割り切れない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(s * (d_i * Mul2D + Gcd2D))。
	 */
	public Factor[] wangTrickBackward(long[][] L, Factor[] monicFactors) {
		L = poly2d.resize(L);
		if (poly2d.degX(L) == -1) throw new IllegalArgumentException("leading coefficient must be non-zero");
		if (monicFactors == null) throw new IllegalArgumentException("monicFactors must not be null");
		Factor[] res = new Factor[monicFactors.length];
		for (int idx = 0; idx < monicFactors.length; idx++) {
			long[][][] substituted = substituteWangX(monicFactors[idx].factor, L);
			res[idx] = new Factor(monic(primitivePartX(substituted)), monicFactors[idx].multiplicity);
		}
		return res;
	}

	/** {@code a^n} を2変数多項式として返す。未テスト。計算量: O(log n * Mul2D) */
	private long[][] powFull2D(long[][] a, int n) {
		long[][] ret = poly2d.one(), b = poly2d.resize(a);
		while (n != 0) {
			if ((n & 1) != 0) ret = poly2d.mul(ret, b);
			n >>= 1;
			if (n != 0) b = poly2d.mul(b, b);
		}
		return ret;
	}

	/** {@code G(L(y,z)x,y,z)} を返す。未テスト。計算量: O(d * Mul2D) */
	private long[][][] substituteWangX(long[][][] G, long[][] L) {
		G = resize(G);
		int d = degX(G);
		if (d == -1) return zero();
		long[][][] res = new long[d + 1][][];
		long[][] pow = poly2d.one();
		for (int i = 0; i <= d; i++) {
			res[i] = poly2d.mul(G[i], pow);
			pow = poly2d.mul(pow, L);
		}
		return resize(res);
	}

	/** {@code pp_x(a)=a/content_x(a)} を返す。未テスト。計算量: O(Gcd2D * deg_x a + Div2D * deg_x a) */
	private long[][][] primitivePartX(long[][][] a) {
		a = resize(a);
		if (degX(a) == -1) return zero();
		long[][] content = contentX(a);
		if (poly2d.degX(content) == -1 || poly2d.equals(content, poly2d.one())) return resize(a);
		return resize(lexdivByPolyYZ(a, content));
	}

	/**
	 * Wang の主係数トリックとHensel持ち上げで primitive かつ square-free な多項式を既約分解する。未テスト。
	 * 計算量: O(Factor2D + Lift3D)
	 */
	public Factor[] factorByWang(long[][][] inputf) {
		long[][][] f = primitivePartX(inputf);
		int dx = degX(f);
		if (dx == -1) return new Factor[0];
		if (dx <= 0) {
			PolynomialFpDynamic2D.FactorResult2D fr = poly2d.factor(f[0]);
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) res[i] = new Factor(new long[][][] { fr.factors()[i].factor }, fr.factors()[i].multiplicity);
			return res;
		}
		if (degZ(f) <= 0) {
			PolynomialFpDynamic2D.FactorResult2D fr = poly2d.factor(evalZ(f, 0));
			Factor[] res = new Factor[fr.factors().length];
			for (int i = 0; i < fr.factors().length; i++) res[i] = new Factor(embedPoly2DAtDegZ(fr.factors()[i].factor, 0, 1), fr.factors()[i].multiplicity);
			return res;
		}
		WangTransform3D transformed = wangTrickForward(f, 0);
		long[][][] monicF = transformed.monicPolynomial();
		long a = findGoodEvaluationPointZ(monicF);
		PolynomialFpDynamic2D.FactorResult2D fr = poly2d.factor(evalZ(monicF, a));
		transformed = wangTrickForward(f, fr.factors().length);
		Factor[] lifted = liftFactors(transformed.monicPolynomial(), fr.factors(), a);
		Factor[] recombined = recombine(transformed.monicPolynomial(), lifted);
		return wangTrickBackward(transformed.leadingCoeff(), recombined);
	}

	private Factor[] recombine(long[][][] f, Factor[] lifted) {
		if (lifted.length <= 1) return lifted;
		ArrayList<long[][][]> currentFactors = new ArrayList<>();
		for (Factor fact : lifted) currentFactors.add(fact.factor);

		ArrayList<long[][][]> result = new ArrayList<>();
		long[][][] remainingF = f;

		for (int sz = 1; sz <= currentFactors.size() / 2; sz++) {
			boolean[] used = new boolean[currentFactors.size()];
			for (int[] indices : Itertools.combinations(currentFactors.size(), sz)) {
				boolean anyUsed = false;
				for (int idx : indices) if (used[idx]) anyUsed = true;
				if (anyUsed) continue;

				long[][][] prod = one();
				for (int idx : indices) prod = mul(prod, currentFactors.get(idx));
				if (degX(prod) <= degX(remainingF) && degY(prod) <= degY(remainingF) && degZ(prod) <= degZ(remainingF) && isDivisible(remainingF, prod)) {
					result.add(prod);
					remainingF = lexdiv(remainingF, prod);
					for (int idx : indices) used[idx] = true;
				}
			}
			ArrayList<long[][][]> nextFactors = new ArrayList<>();
			for (int i = 0; i < currentFactors.size(); i++) if (!used[i]) nextFactors.add(currentFactors.get(i));
			currentFactors = nextFactors;
		}
		if (degX(remainingF) > 0 || degY(remainingF) > 0 || degZ(remainingF) > 0) result.add(remainingF);

		Factor[] res = new Factor[result.size()];
		for (int i = 0; i < result.size(); i++) res[i] = new Factor(result.get(i), 1);
		return res;
	}

	/** {@code f(x,y,valZ)} を返す。未テスト。計算量: O(項数) */
	long[][] evalZ(long[][][] f, long valZ) {
		f = resize(f);
		long[][] res = new long[f.length][];
		for (int i = 0; i < f.length; i++) {
			res[i] = new long[f[i].length];
			for (int j = 0; j < f[i].length; j++) res[i][j] = poly1d.eval(f[i][j], valZ);
		}
		return poly2d.resize(res);
	}

	/** Hensel 持ち上げに適した z 評価点を返す。未テスト。計算量: O(mod候補 * Factor1D) */
	private long findGoodEvaluationPointZ(long[][][] f) {
		int dx = degX(f);
		long[][] lc = f[dx];
		long bestA = -1;
		int minFactors = Integer.MAX_VALUE;
		Random rnd=new Random();
		int found = 0;
		for (int i=0; i<Math.min(mod, 1000);++i) {
			long a = rnd.nextLong(1, mod);
			long[] lca = poly2d.evalY(lc, a);
			if (poly1d.deg(lca) == -1) continue;
			long[][] fa2d = evalZ(f, a);
			long[] fa1d = null;
			for (long b = 0; b < Math.min(mod, 100); b++) {
				if (poly1d.eval(lca, b) == 0) continue;
				long[] fb = poly2d.evalY(fa2d, b);
				if (poly1d.isSquareFree(fb)) {
					fa1d = fb;
					break;
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
		throw new ArithmeticException("good z evaluation point not found");
	}

	/**
	 * {@code f(x, y, valZ)} の互いに素な2変数因子分解から、{@code f(x, y, z)} の因子をHensel持ち上げで復元する。
	 * {@code f} は primitive かつ square-free かつ monic かつ相異なる素因子が {@code z=valZ} で同じ素因子につぶれないことを仮定する。
	 * 計算量: O((degZ(f) + 1) * (Mul3D * factors.length + PFD2D))
	 */
	public Factor[] liftFactors(long[][][] f, PolynomialFpDynamic2D.Factor[] factors, long valZ) {
		f = resize(f);
		int nz = Math.max(1, degZ(f) + 1);

		// f(x, y, z) = sum a_i(x, y) (z-valZ)^i = sum a_i(x, y) t^i と置き換える。
		long[][][] shiftedF = truncateZ(shiftZ(f, valZ), nz);
		int r = factors.length;
		if (r == 0) return new Factor[0];

		// 初期値は f(x, y, valZ) の因数分解 mod t。
		long[][][][] lifted = new long[r][][][];
		for (int i = 0; i < r; i++) {
			lifted[i] = embedPoly2DAtDegZ(factors[i].factor, 0, nz);
		}

		for (int k = 1; k < nz; k++) {
			// 既に mod t^k まで一致している shiftedF-Πlifted の t^k 係数を取り出す。
			long[][][] prod = one();
			for (int i = 0; i < r; i++) prod = truncateZ(mul(prod, lifted[i]), k + 1);
			long[][] err = poly2d.resize(poly2d.sub(coeffZ(shiftedF, k), coeffZ(prod, k)));
			long[][][] corr2D = henselCorrections2D(err, factors);
			long[][][][] corr3D = new long[r][][][];
			for (int i = 0; i < r; i++) corr3D[i] = embedPoly2DAtDegZ(corr2D[i], k, nz);
			for (int i = 0; i < r; i++) lifted[i] = truncateZ(add(lifted[i], corr3D[i]), nz);
		}

		Factor[] res = new Factor[r];
		long back = poly1d.fp.reduce(-valZ);
		for (int i = 0; i < r; i++) res[i] = new Factor(resize(shiftZ(lifted[i], back)), factors[i].multiplicity);
		return res;
	}
	
	
	/** {@code a(x,y)} を {@code z^k a(x,y)} として3変数多項式に埋め込む。未テスト。計算量: O(項数) */
	private long[][][] embedPoly2DAtDegZ(long[][] a, int k, int nz) {
		a = poly2d.resize(a);
		if (poly2d.degX(a) == -1) return zero();
		long[][][] res = new long[a.length][][];
		for (int i = 0; i < a.length; i++) {
			res[i] = new long[a[i].length][nz];
			for (int j = 0; j < a[i].length; j++) res[i][j][k] = a[i][j];
		}
		return resize(res);
	}


	/**
	 * {@code err = sum_i c_i prod_{j != i} factors[j]} を満たす2変数補正を線形方程式で返す。未テスト。
	 *
	 * <p>契約:
	 * <ul>
	 * <li>事前条件: {@code factors} は相異なる monic な2変数多項式で互いに素。</li>
	 * <li>事後条件: 返値 {@code c_i} は {@code err=sum_i c_i Q/f_i} を満たす。</li>
	 * <li>副作用: なし。返値は入力と参照共有しない。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>例外: 解が見つからない場合は {@link ArithmeticException}。</li>
	 * <li>未定義条件: 因子が互いに素でない場合。</li>
	 * </ul>
	 *
	 * <p>計算量: O(U^3 + U * Mul2D)。
	 */
	private long[][][] henselCorrections2D(long[][] err, PolynomialFpDynamic2D.Factor[] factors) {
		if (poly2d.degX(err)==-1) return new long[factors.length][0][0];

		int r = factors.length;
		// Q = prod_i factors[i]
		var Q = poly2d.one();
		for (var factor : factors) Q = poly2d.mul(Q, factor.factor);
		
		// cofactors[i] = prod_{i ≠ j} factors[j]
		var cofactors = new long[r][][];
		
		// 各iにおけるc_iのx, y, zのサイズ（最大次数 + 1）を格納する配列
		int[] xBounds = new int[r];
		int[] yBounds = new int[r];
		int[] offset = new int[r];
		int vars = 0;
		
		int maxX = Math.max(poly2d.degX(err), 0);
		int maxY = Math.max(poly2d.degY(err), 0);
		for (int i = 0; i < r; i++) {
			cofactors[i] = poly2d.lexdiv(Q, factors[i].factor);
			
			xBounds[i] = Math.max(Math.max(0, poly2d.degX(err)) - Math.max(0, poly2d.degX(Q)) + Math.max(0, poly2d.degX(factors[i].factor)), 0) + 1;
			yBounds[i] = Math.max(Math.max(0, poly2d.degY(err)) - Math.max(0, poly2d.degY(Q)) + Math.max(0, poly2d.degY(factors[i].factor)), 0) + 1;
			maxX = Math.max(maxX, xBounds[i] + Math.max(poly2d.degX(cofactors[i]), 0));
			maxY = Math.max(maxY, yBounds[i] + Math.max(poly2d.degY(cofactors[i]), 0));
			offset[i] = vars;
			vars += xBounds[i] * yBounds[i];
		}
		long[][] matA = new long[(maxX + 1) * (maxY + 1)][vars];
		long[] vecB = new long[(maxX + 1) * (maxY + 1)];
		// 行列の係数埋め込み
		for (int i = 0; i < r; i++) {
			for (int ux = 0; ux < xBounds[i]; ux++) {
				for (int uy = 0; uy < yBounds[i]; uy++) {
					int col = offset[i] + (ux * yBounds[i] + uy);
					for (int cx = 0; cx < cofactors[i].length; cx++) {
						for (int cy = 0; cy < cofactors[i][cx].length; cy++) {
							long v = cofactors[i][cx][cy];
							if (v == 0) continue;
							int row = ((ux + cx) * (maxY + 1) + uy + cy) ;
							matA[row][col] = (matA[row][col] + v) % mod;
						}
					}
				}
			}
		}
		
		// err の埋め込み
		for (int x = 0; x < err.length; x++) {
			for (int y = 0; y < err[x].length; y++) {
				vecB[(x * (maxY + 1) + y)] = err[x][y];
			}
		}
		
		long[][] res = MatrixUtilsFp.solveLinearEquation(matA, vecB, mod);
		if (res == null) throw new ArithmeticException("linear correction solve failed");
		long[] sol = res[0];
		var corr = new long[r][][];
		
		// 解の復元
		for (int i = 0; i < r; i++) {
			corr[i] = new long[xBounds[i]][yBounds[i]];
			for (int ux = 0; ux < xBounds[i]; ux++) {
				for (int uy = 0; uy < yBounds[i]; uy++) {
					corr[i][ux][uy] = sol[offset[i] + (ux * yBounds[i] + uy)];
				}
			}
			corr[i] = poly2d.resize(corr[i]);
		}
		
		return corr;
	}
	
	/**
	 * 多項式を式として表示する。
	 * @param label ラベル
	 * @param arr 多項式の係数配列
	 *
	 * <p>計算量: O(NML)
	 * <p>未テスト
	 */
	public void printPolyAsExpr(String label, long[][][] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x", "y", "z" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				for (int k = 0; k < arr[i][j].length; k++) {
					long coeff = fp.reduce(arr[i][j][k]);
					if (coeff == 0) continue;

					if (!isFirst) {
						sb.append(" + ");
					}

					StringBuilder varPart = new StringBuilder();
					int[] powers = { i, j, k };
					for (int v = 0; v < 3; v++) {
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

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}

	public void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}
