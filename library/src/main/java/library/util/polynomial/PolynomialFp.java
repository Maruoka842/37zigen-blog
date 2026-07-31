package library.util.polynomial;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.MathUtils;
/**
 * 998244353=1119*2^{23}+1は2^23=8388608まで計算可能。
 */
public class PolynomialFp {
	public static final long mod = 998244353L;//119×2^{23}+1
	static final Fp fp = Fp.MOD998244353;

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] zero() {
		return new long[0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] one() {
		return new long[] { 1 };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] x() {
		return new long[] { 0, 1 };
	}
	public long[] f;
	static long[][] bitreversedRoots = new long[30][];
	static long[][] bitreversedInvRoots = new long[30][];
	public static int HALF_GCD_NAIVE_THRESHOLD = 128;
	public PolynomialFp(long[] v) {
		if (v != null) f = Arrays.copyOf(v, v.length);
	}
	
	public static PolynomialFp of(long[] v) {
		return new PolynomialFp(v);
	}
	
	static long ADD(long a,long b) {
		long sum=(a+b);
		return sum>=mod?sum-mod:sum;
	}
	
	static long SUB(long a,long b) {
		return ADD(a,mod-b);
	}
	
	public static long[] prefixSum(long[] a) {
		return ArrayUtils.prefixModSum(a, mod);
	}

	/**
	 * Berlekamp-Massey 法で、列 s の最小線形漸化多項式を返す。
	 * 返り値 coeff は coeff[0] s[i] + coeff[1] s[i+1] + ... + coeff[d] s[i+d] を満たす最短のもので、
	 * coeff[d] = 1 に正規化する。ゼロ列は便宜上 {1} を返す。
	 * half-gcdによるselected half remainderでBM境界を探す。
	 * NTT乗算なら O(m log^2 m)、一般には O(M(m) log m), m = s.length。
	 * s = (1, 1, 0) の場合 1-x+x^2,x^2 の両方が最小線形漸化多項式になるように、返り値は一意ではない。
	 * 未テスト。
	 */
	public static long[] berlekampMassey(long[] s) {
		/*
		 * A = x^m
		 * S = s[0] + s[1] x + ... + s[m - 1] x^{m - 1}
		 * に対して拡張 Euclid を考える。
		 * Euclid 剰余列を
		 *
		 *   R_0 = x^m
		 *   R_1 = S
		 *   R_{i+1} = R_{i-1} mod R_i
		 * とし、各行を
		 *   R_i = U_i x^m + V_i S
		 * と書く。
		 *
		 * このとき mod x^m では
		 *
		 *   R_i ≡ V_i S(x) (mod x^m)
		 *
		 * である。
		 * したがって、Euclid 行 (R_i, V_i) は
		 *
		 *   C(x) = V_i(x)
		 *
		 * という接続多項式候補を与える。ただし内部表現では C(0)=1 に
		 * 正規化したいので、V_i(0) != 0 の行だけを候補にする。
		 * この行を使うときのBM上の次数は、V_iの実次数とは限らず、
		 *
		 *   L_i = max(deg(V_i), deg(R_i) + 1)
		 *
		 * として見る。必要条件は deg(R_i) < L_i である。
		 *
		 * さらに、A=x^m の特殊性から 
		 *
		 *   deg(V_i) = m - deg(R_{i-1})
		 *
		 * が成り立つ。よって BM 境界条件
		 *
		 *   deg(R_i) < deg(V_i)
		 *
		 * は
		 *
		 *   deg(R_{i-1}) + deg(R_i) < m
		 *
		 * と同値である。
		 * 
		 * ここで h = ceil(m / 2) とする。
		 * euclidCrossHalfFast(A, B, h) は、Euclid 剰余列の中で初めて
		 *
		 *   deg(R_j) < h
		 *
		 * となる行 R_j と、その直前 R_{j-1} を返す。
		 *
		 * つまり
		 *
		 *   deg(R_{j-1}) >= h > deg(R_j)
		 *
		 * を満たす連続する 2 行を返す。
		 *
		 * この周辺で L_i が最小になるので、この実装では
		 * R_{j-1}, R_j, R_{j+1} のうち V_i(0) != 0 を満たす行を候補にし、
		 * L_i が最小のものを選ぶ。
		 */
		int m = s.length;
		long[] a = new long[m + 1];
		a[m] = 1;
		long[] b = Arrays.copyOf(s, m);
		for (int i = 0; i < m; i++) {
			b[i] %= mod;
			if (b[i] < 0) b[i] += mod;
		}
		b = resize(b);
		if (deg(b) == -1) return new long[] {1};
		EuclidCrossHalfResult cross = euclidCrossHalfFast(a, b, (m + 1) / 2);
		EuclidRow prev = cross.prev;
		EuclidRow cur = cross.cur;
		EuclidRow next = deg(cur.r) == -1 ? null : euclidNext(prev, cur);
		BerlekampMasseyCandidate candidate = bestBerlekampMasseyCandidate(prev, cur, next);
		long[] C = resize(candidate.c, candidate.nominalDegree + 1);
		long inv = MathUtils.modInv(C[0], mod);
		C = mul(C, inv);
		int L = candidate.nominalDegree;
		long[] coeff = new long[L + 1];
		/*
		 * 選んだ V_i を C とする。
		 *
		 *   C[0] s[n] + C[1] s[n-1] + ... + C[L] s[n-L] = 0
		 *
		 * という内部表現で、ここでの L は C の実次数ではなく nominal degree。
		 * 要求仕様は
		 *
		 *   coeff[0] s[i] + ... + coeff[L] s[i+L] = 0
		 *
		 * なので、n = i + L と見て係数の順序を反転する。
		 */
		for (int i = 0; i <= L; i++) {
			coeff[i] = C[L - i] % mod;
			if (coeff[i] < 0) coeff[i] += mod;
		}
		return coeff;
	}

	static class BerlekampMasseyCandidate {
		long[] c;
		int nominalDegree;
		BerlekampMasseyCandidate(long[] c, int nominalDegree) {
			this.c = c;
			this.nominalDegree = nominalDegree;
		}
	}

	static BerlekampMasseyCandidate bestBerlekampMasseyCandidate(EuclidRow... rows) {
		BerlekampMasseyCandidate best = null;
		for (EuclidRow row : rows) {
			if (row == null || row.y.length == 0 || row.y[0] == 0) continue;
			int nominalDegree = Math.max(deg(row.y), deg(row.r) + 1);
			if (best == null || nominalDegree < best.nominalDegree) {
				best = new BerlekampMasseyCandidate(row.y, nominalDegree);
			}
		}
		if (best == null) throw new AssertionError("no admissible Berlekamp-Massey candidate");
		return best;
	}

	/**
	 * Euclid列の1行を表す。
	 * {@code r = u * A + y * B} を満たす剰余 {@code r} と
	 * Bézout係数 {@code u, y} を持つ。
	 */
	public static class EuclidRow {
		public long[] r;
		public long[] u;
		public long[] y;
		EuclidRow(long[] r, long[] u, long[] y) {
			this.r = resize(r);
			this.u = resize(u);
			this.y = resize(y);
		}
	}

	/**
	 * half-degree境界をまたぐ連続するEuclid行。
	 * {@code cur} は最初に {@code deg(cur.r) < h} を満たす行で、
	 * {@code prev} はその直前の行。
	 */
	public static class EuclidCrossHalfResult {
		public EuclidRow prev;
		public EuclidRow cur;
		EuclidCrossHalfResult(EuclidRow prev, EuclidRow cur) {
			this.prev = prev;
			this.cur = cur;
		}
	}

	static EuclidRow euclidNext(EuclidRow prev, EuclidRow cur) {
		DivModResult dm = divmod(prev.r, cur.r);
		return new EuclidRow(dm.r, subtract(prev.u, mul(dm.q, cur.u)), subtract(prev.y, mul(dm.q, cur.y)));
	}

	/**
	 * Euclid列で最初に deg(R) < h となる行 cur と、その直前行 prev を愚直に返す。
	 * 未テスト。計算量 O(n M(n))。
	 */
	public static EuclidCrossHalfResult euclidCrossHalfNaive(long[] a, long[] b, int h) {
		EuclidRow prev = new EuclidRow(a, new long[] {1}, new long[] {0});
		EuclidRow cur = new EuclidRow(b, new long[] {0}, new long[] {1});
		while (deg(cur.r) >= h) {
			EuclidRow next = euclidNext(prev, cur);
			prev = cur;
			cur = next;
		}
		return new EuclidCrossHalfResult(prev, cur);
	}

	/**
	 * Euclid列が指定した次数境界 {@code h} を初めて下回る場所をhalf-gcdで探す。
	 *
	 * <p>
	 * 入力多項式 {@code a, b} から
	 * {@code R0 = a, R1 = b, R_{i+1} = R_{i-1} mod R_i}
	 * というEuclid剰余列を作る。返り値 {@code res} は、最初に
	 * {@code deg(res.cur.r) < h} となる行 {@code res.cur} と、その直前行
	 * {@code res.prev} を表す。
	 * 各行は {@code row.r = u * a + row.y * b} の {@code r, y} だけを持つ。
	 *
	 * <p>
	 * Berlekamp-Masseyでは {@code a = x^m}, {@code b = S(x)},
	 * {@code h = ceil(m / 2)} として呼び、半分境界をまたぐ2行から
	 * BM境界 {@code deg(R) < deg(V)} の候補を選ぶ。
	 *
	 * <p>
	 * 既存の {@link #halfGcd(long[], long[])} が返すselected half remainderを使う。
	 * NTT乗算なら {@code O(n log^2 n)}、一般には {@code O(M(n) log n)}。
	 * この計算量は {@code halfGcd} がhalf-degree境界を越える行列を返すことに依存する。
	 * 未テスト。
	 */
	public static EuclidCrossHalfResult euclidCrossHalfFast(long[] a, long[] b, int h) {
		return selectedRemainder(a, b, h);
	}

	/**
	 * Euclid列で最初に {@code deg(R) < h} となる行と、その直前行を返す。
	 * 入力は {@code deg(a) > deg(b)} と {@code 0 <= h <= deg(a)} を仮定する。
	 * half-gcdで境界を再帰的に探索する selected remainder。
	 * 未テスト。計算量 O(M(n) log n)。
	 */
	public static EuclidCrossHalfResult selectedRemainder(long[] a, long[] b, int h) {
		a = resize(a);
		b = resize(b);
		int n = deg(a);
		if (deg(b) < h) return new EuclidCrossHalfResult(new EuclidRow(a, new long[] {1}, new long[] {0}), new EuclidRow(b, new long[] {0}, new long[] {1}));
		if (n <= HALF_GCD_NAIVE_THRESHOLD) return euclidCrossHalfNaive(a, b, h);
		int half = (n + 1) / 2;
		if (h <= half) {
			HalfGcdResult mat = halfGcd(a, b);
			long[][] cd = mat.apply(a, b);
			EuclidRow row0 = new EuclidRow(cd[0], mat.p00, mat.p01);
			EuclidRow row1 = new EuclidRow(cd[1], mat.p10, mat.p11);
			if (deg(row1.r) < h) return new EuclidCrossHalfResult(row0, row1);
			EuclidRow row2 = euclidNext(row0, row1);
			if (deg(row2.r) < h) return new EuclidCrossHalfResult(row1, row2);
			EuclidCrossHalfResult sub = selectedRemainder(row1.r, row2.r, h);
			return composeEuclidRows(sub, row1, row2);
		}
		int shift = 2 * h - n;
		long[] ah = divideByX(a, shift);
		long[] bh = divideByX(b, shift);
		EuclidCrossHalfResult high = selectedRemainder(ah, bh, h - shift);
		// high の係数は高々 (n - h) で、a - ah, b - bh の次数が高々 h - 1　で
		// (n - h) + (shift - 1) = h - 1 < h
		// なので影響がない。
		
		/*
		 * 再帰先の境界は
		 *   h' = h - shift = deg(a) - h
		 * であり、再帰先の次数は
		 *   deg(ah) = deg(a) - shift = 2(deg(a) - h)
		 * なので、
		 *   h' = deg(ah) / 2
		 * となる。したがって、再帰先では halfgcd が呼ばれる。
		 */
		EuclidRow prev = applyEuclidRow(high.prev, a, b);
		EuclidRow cur = applyEuclidRow(high.cur, a, b);
		if (deg(cur.r) >= h) throw new AssertionError("selected remainder did not cross boundary");
		if (deg(prev.r) < h) throw new AssertionError("selected remainder overshot boundary");
		return new EuclidCrossHalfResult(prev, cur);
	}

	/**
	 * 縮小問題 {@code (row0.r, row1.r)} 上で得た境界結果を、
	 * 元の入力 {@code (A, B)} 上のEuclid行に戻す。
	 *
	 * <p>
	 * {@code sub} の各行が
	 * {@code r = row.u * row0.r + row.y * row1.r} を表すとき、
	 * {@code row0.r = row0.u * A + row0.y * B},
	 * {@code row1.r = row1.u * A + row1.y * B} を代入して、
	 * 元の {@code A, B} に対するBézout係数へ合成する。
	 */
	static EuclidCrossHalfResult composeEuclidRows(EuclidCrossHalfResult sub, EuclidRow row0, EuclidRow row1) {
		return new EuclidCrossHalfResult(composeEuclidRow(sub.prev, row0, row1), composeEuclidRow(sub.cur, row0, row1));
	}

	/**
	 * 縮小問題の1行を、元の入力 {@code (A, B)} に対する1行へ合成する。
	 *
	 * <p>
	 * 縮小問題では {@code row.r = row.u * row0.r + row.y * row1.r}。
	 * ここに {@code row0}, {@code row1} の元の入力に対するBézout係数を代入し、
	 * {@code row.r = u * A + y * B} となる {@code u, y} を作る。
	 */
	static EuclidRow composeEuclidRow(EuclidRow row, EuclidRow row0, EuclidRow row1) {
		long[] u = resize(add(mul(row.u, row0.u), mul(row.y, row1.u)));
		long[] y = resize(add(mul(row.u, row0.y), mul(row.y, row1.y)));
		return new EuclidRow(row.r, u, y);
	}

	static EuclidRow applyEuclidRow(EuclidRow row, long[] a, long[] b) {
		long[] r = resize(add(mul(row.u, a), mul(row.y, b)));
		return new EuclidRow(r, row.u, row.y);
	}

	static void prepareRoots(int n) {
		int sz = Integer.numberOfTrailingZeros(n);
		if (bitreversedRoots[sz] != null) return;
		long g = 3;
		long root = MathUtils.modPow(g, (mod - 1) / n, mod); 
		long iroot = MathUtils.modInv(root, mod);
		bitreversedRoots[sz] = new long[n];
		bitreversedInvRoots[sz] = new long[n];
		for (int n_ = n / 2; n_ >= 1; n_ /= 2, root = root * root % mod, iroot = iroot * iroot % mod) {
			long w = 1;
			long iw = 1;
			for(int j=0;j<n_;++j) {
				bitreversedRoots[sz][n_+j] = w;
				bitreversedInvRoots[sz][n_+j] = iw;
				w = w * root % mod;
				iw = iw * iroot % mod;
			}
			int cur=0;
			for(int j=0;j<n_;++j) {
				if(cur<j) {
					ArrayUtils.swap(n_+cur,n_+j,bitreversedRoots[sz]);
					ArrayUtils.swap(n_+cur,n_+j,bitreversedInvRoots[sz]);
				}
				for (int k=n_/2;k>(cur^=k);k/=2) ;
			}
		}

	}
	
	public static void fft(long[] a, long g) {
		int n=a.length;
		{
			int cur=0;
			for (int i=0;i<n;++i) {
				if (cur<i) {
					a[i]^=a[cur];a[cur]^=a[i];a[i]^=a[cur];
				}
				for (int k=n/2;k>(cur^=k);k/=2) ;
			}
		}
		for (int s=1;s<=n/2;s*=2) {
			long mul=MathUtils.modPow(g,n/(2*s),mod);
			for (int i=0;i<n;i+=2*s) {
				long x=1;
					for (int j=0;j<s;++j) {
					long A=a[i+j];
					long B=a[i+j+s]*x%mod;
					a[i+j]=ADD(A,B);
					a[i+j+s]=SUB(A,B);
					x=x*mul%mod;
				}
			}
		}
	}
	
	static void ifft(long[] a, long g) {
		int n=a.length;
		{
			int cur=0;
			for (int i=0;i<n;++i) {
				if (cur<i) {
					a[i]^=a[cur];a[cur]^=a[i];a[i]^=a[cur];
				}
				for (int k=n/2;k>(cur^=k);k/=2) ;
			}
		}
		long invN = MathUtils.modInv(a.length, mod);
		for (int s=1;s<=n/2;s*=2) {
			long mul=MathUtils.modPow(g,n/(2*s),mod);
			for (int i=0;i<n;i+=2*s) {
				long x=(s==n/2?invN:1);
				for (int j=0;j<s;++j) {
					long A=a[i+j];
					long B=a[i+j+s]*x%mod;
					if(s==n/2)A=A*invN%mod;
					a[i+j]=ADD(A,B);
					a[i+j+s]=SUB(A,B);
					x=x*mul%mod;
				}
			}
		}
	}	
	
	/**
	 * fftをbitreversedした順で返す。
	 * Scott, Michael. "A note on the implementation of the number theoretic transform." IMA International Conference on Cryptography and Coding. Cham: Springer International Publishing, 2017.
	 * @param a
	 */
	public static void fftTobitReversed(long[] a) {
		int n=a.length;
		int sz=Integer.numberOfTrailingZeros(a.length);
		if (bitreversedRoots[sz] == null) prepareRoots(a.length);
		for(int m = 1, t = n/2; m <= n/2; m *= 2, t /= 2) {
			for(int i = 0, k = 0; i<m; ++i, k += 2*t) {
				long S=bitreversedRoots[sz][m+i];
				for(int j=k;j<k+t;++j) { 
					long u=a[j];
					long v=a[j+t]*S%mod;
					a[j]=ADD(u,v);
					a[j+t]=SUB(u,v);
				}
			}
		}
	}
	
	
	
	
	/**
	 * Scott, Michael. "A note on the implementation of the number theoretic transform." IMA International Conference on Cryptography and Coding. Cham: Springer International Publishing, 2017.
	 * @param a
	 */
	public static void ifftFromBitreversed(long[] a) {
		long invN = MathUtils.modInv(a.length, mod);
		int n=a.length;
		int sz = Integer.numberOfTrailingZeros(n);
		if (bitreversedInvRoots[sz] == null) prepareRoots(a.length);
		for(int m = n/2, t = 1; m >= 1; m /= 2, t *= 2) {
			for(int i = 0, k = 0; i<m; ++i, k += 2*t) {
				long S=bitreversedInvRoots[sz][m+i];
				if (m == 1) S=S*invN%mod;
				for(int j=k;j<k+t;++j) { 
					long u=a[j];
					long v=a[j+t];
					if(m == 1) a[j] = (u + v) * invN % mod;
					else a[j]=ADD(u,v);
					a[j+t]=(u+mod-v)*S%mod;
				}
			}
		}

	}

	
	public static long[] add(long[] a,long[] b) {
		long[] ret=new long[Math.max(a.length, b.length)];
		for (int i=0;i<ret.length;++i) ret[i]=ADD(i<a.length?a[i]:0,i<b.length?b[i]:0);
		return ret;
	}
	
	public static long[] subtract(long[] a,long[] b) {
		long[] ret=new long[Math.max(a.length, b.length)];
		for (int i=0;i<ret.length;++i) ret[i]=ADD(i<a.length?a[i]:0,i<b.length?(mod-b[i]):0);
		return ret;
	}
	
	static long[] mulFFT(long[] a,long[] b) {
		int n=1;
		int len = a.length + b.length - 1;
		while (n<a.length+b.length-1) n*=2;
		a=Arrays.copyOf(a, n);
		b=Arrays.copyOf(b, n);
		prepareRoots(n);
		fftTobitReversed(a);
		fftTobitReversed(b);
		for (int i = 0; i < a.length; ++i) a[i] = a[i] * b[i] % mod;
		ifftFromBitreversed(a);
		return resize(a, len);
	}

	/**
	 * a(x)^m mod (1-x^{2^n}) 
	 * @param a
	 * @param b
	 * @param n
	 * @return
	 * verified:https://atcoder.jp/contests/fps-24/submissions/70689871
	 */
	static public long[] cyclicPowFFT(long[] a, long m, int n) {
		if (Integer.bitCount(n)!=1) throw new AssertionError();
		long[] A = new long[n];
		for (int i = 0; i < a.length; i++) {
			A[i%n]+=a[i];
		}
		prepareRoots(n);
		fftTobitReversed(A);
		for (int i = 0; i < A.length; ++i) {
			A[i] = fp.pow(A[i], m) % mod;
		}
		ifftFromBitreversed(A);
		return resize(A, n);
	}

	
	
	/**
	 * a(x)b(x) mod (1-x^n) 
	 * 未テスト
	 * @param a
	 * @param b
	 * @param n
	 * @return
	 */
	public static long[] cyclicmulFFT(long[] a,long[] b, int n) {
		if (Integer.bitCount(n)!=1) throw new AssertionError();
		long[] A = new long[n];
		long[] B = new long[n];
		for (int i = 0; i < a.length; i++) {
			A[i%n]+=a[i];
		}
		for (int i = 0; i < b.length; i++) {
			B[i%n]+=b[i];
		}
		prepareRoots(n);
		fftTobitReversed(A);
		fftTobitReversed(B);
		for (int i = 0; i < A.length; ++i) A[i] = A[i] * B[i] % mod;
		ifftFromBitreversed(A);
		return resize(A, n);
	}
	
	
	public static long[] mulNaive(long[] a, long[] b) {
		long[] ret=new long[a.length+b.length-1];
		for(int i=0;i<a.length;++i) {
			for(int j=0;j<b.length;++j) {
				ret[i+j]+=a[i]*b[j];
				ret[i+j]%=mod;
			}
		}
		return ret;
	}

	/**
	 * Lagrange-Bürmannの公式を用いて、多項式の合成 A(B(x)) の第 n 係数を計算する。
	 * B(x) の合成逆関数 G(y) = B⁻¹(y) が既知であるときに使用する。
	 *
	 * <p>事前条件:
	 * <ul>
	 *   <li>G(0) = 0</li>
	 *   <li>G'(0) ≠ 0</li>
	 * </ul>
	 * </p>
	 *
	 * @param A 多項式 A(x) の係数配列
	 * @param G B(x) の合成逆関数 G(y) の係数配列
	 * @param n 求めたい係数の次数
	 * @return [xⁿ] A(B(x))
	 *
	 * <p>計算量: O(n log n)</p>
	 */
	public static long lagrangeBurmann(long[] A, long[] G, int n) {
		if (n == 0) return A.length > 0 ? (A[0] % mod + mod) % mod : 0;
		if (n < 0) return 0;
		long[] Adiff = differentiate(A);
		if (G.length < 2 || (G[0] % mod + mod) % mod != 0 || (G[1] % mod + mod) % mod == 0) {
			throw new IllegalArgumentException("G must satisfy G(0) = 0 and G'(0) != 0");
		}
		long[] H = divideByX(G, 1);
		long[] HinvN = pow(inv(resize(H, n)), n);
		long[] Res = mul(resize(Adiff, n), HinvN);
		long coeff = (n - 1 < Res.length) ? Res[n - 1] : 0;
		return (coeff % mod + mod) % mod * MathUtils.modInv(n, mod) % mod;
	}
	
	/**
	 * [-mod+1, mod-1]の範囲外の要素があると、ADD/SUBでバグる。
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] mul(long[] a, long[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] < 0) a[i] += mod;
		}
		for (int i = 0; i < b.length; i++) {
			if (b[i] < 0) b[i] += mod;
		}
		if (a.length + b.length - 1 <= 512 || Math.min(a.length, b.length) <=  10) {
			return mulNaive(a, b);
		} else {
			return mulFFT(a, b);
		}
	}
	
	public static long[] squared(long[] a) {
		if (a.length <= 128) {
			long[] ret=new long[2 * a.length-1];
			for(int i=0;i<a.length;++i) {
				for(int j=i + 1;j<a.length;++j) {
					ret[i+j]+=2*a[i]*a[j];
					ret[i+j]%=mod;
				}
			}
			for (int i = 0; i < a.length; ++i) {
				ret[2 * i] += a[i] * a[i];
				ret[2 * i] %= mod;
			}
			return ret;
		} else {
			int n=1;
			int len = a.length + a.length - 1;
			while (n<a.length+a.length-1) n*=2;
			a=Arrays.copyOf(a, n);
			prepareRoots(n);
			fftTobitReversed(a);
			for (int i = 0; i < a.length; ++i) a[i] = a[i] * a[i] % mod;
			ifftFromBitreversed(a);
			return resize(a, len);
		}
	}
	
	public static long[] sqrt(long[] a) {
		long[] ret=new long[a.length];
		long b=MathUtils.modKthRoot(a[0],2,mod);
		ret=mul(a,MathUtils.modInv(a[0],mod));
		ret=log(ret);
		ret=mul(ret,MathUtils.modInv(2,mod));
		ret=exp(ret);
		ret=mul(ret,b);
		return ret;
	}
	
	
	public static long[] pow(long[] a,long m) {
		int len = a.length;
		if(m==0) {
			long[] ret=new long[a.length];
			ret[0]=1;
			return ret;
		} else if (m==1) {
			return a.clone();
		} else if (m == 2) {
			return squared(a);
		}
		int s=0;
		while (s<a.length && a[s]==0) ++s;
		if (s==a.length) return a;
		if (s != 0) a=Arrays.copyOfRange(a, s, a.length);
		long b=MathUtils.modInv(a[0], mod);
		for (int i=0;i<a.length;++i) a[i]=b*a[i]%mod;
		a=log(a);
		for (int i=0;i<a.length;++i) a[i]=m%mod*a[i]%mod;
		a=exp(a);
		b=MathUtils.modPow(MathUtils.modInv(b, mod),m%(mod-1), mod);
		for (int i=0;i<a.length;++i) a[i]=b*a[i]%mod;
		long[]ret=new long[len];
		if(s <= (len - 1) / m) {
			for(long i = s * m; i<len && i - s * m < a.length;++i) {
				ret[(int) i] = a[(int) (i - s * m)];
			}
		}
		return ret;
	}
	
	/**
	 * log(a)
	 * @param a
	 * @return
	 */
	public static long[] log(long[] a) {
		return integrate(resize(mul(differentiate(a), inv(a)),a.length));
	}
	

	/**
	 * $\prod_{k=0}^{M-1} f(r^k x) \pmod{x^n}$ を計算する。
	 * $f(0) = 1$ であることを仮定する。
	 *
	 * @param f 多項式
	 * @param r 公比
	 * @param M 積をとる項数
	 * @return $\prod_{k=0}^{M-1} f(r^k x) \pmod{x^n}$
	 *
	 * <p>計算量: O(n \log n)
	 */
	public static long[] productOfFRKx(long[] f, long r, long M) {
		if (f.length == 0) return new long[0];
		if ((f[0] % mod + mod) % mod != 1) throw new AssertionError("f[0] must be 1");
		if (M == 0) {
			long[] ret = new long[f.length];
			ret[0] = 1;
			return ret;
		}
		int n = f.length;
		long[] g = log(f);
		long redR = (r % mod + mod) % mod;
		long rm = MathUtils.modPow(redR, M, mod);
		long ri = 1;
		long rmi = 1;
		for (int i = 1; i < n; i++) {
			ri = ri * redR % mod;
			rmi = rmi * rm % mod;
			if (ri == 1) {
				g[i] = g[i] * (M % mod) % mod;
			} else {
				g[i] = g[i] * SUB(rmi, 1) % mod * MathUtils.modInv(SUB(ri, 1), mod) % mod;
			}
		}
		return exp(g);
	}

	/**
	 * AtCoderでa.length=1e6で1.2sec
	 * A simple and fast algorithm for computing exponentials of power series. Alin Bostan
	 * a[i]<0があるとバグる！！
	 * @param a
	 * @return
	 */
	public static long[] exp(long[] a) {
		long[] exp=new long[1];
		long[] iexp=new long[1];
		exp[0]=1;
		iexp[0]=1;
		long[] differeniatedA = differentiate(a);
		long[] differentiatedExp = new long[2 * a.length];
		for (int len=1;len<a.length;len*=2) {
			long[] expFFT = resize(exp, 2 * len);
			long[] iexpFFT = resize(iexp, 2 * len);
			fftTobitReversed(expFFT);
			fftTobitReversed(iexpFFT);
			long[] x = new long[2 * len];
			for (int i = 0; i < 2 * len; ++i) {
				x[i] = iexpFFT[i] * iexpFFT[i] % mod * expFFT[i] % mod;
			}
			ifftFromBitreversed(x);
			if (len !=1 ) iexp = resize(add(iexp, subtract(iexp, x)), len);
			long[] q = resize(differeniatedA, len);
			long[] qFFT = resize(q, len);
			long[] expFFT2 = resize(expFFT, len);
			fftTobitReversed(qFFT);
			for (int i = 0; i < q.length; ++i) {
				qFFT[i] = qFFT[i] * expFFT2[i] % mod;
			}
			ifftFromBitreversed(qFFT);
			
			long[] s = new long[len];
			for (int i = 0; i < len; ++i) {
				s[i] = (exp[i] * i + mod - (i == 0 ? qFFT[len - 1] : qFFT[i - 1])) % mod;
			}
			
			long[] t = resize(mul(s, iexp), len);
			t = multiplyByX(t, len - 1);
			q = add(q, t);
			q = resize(q, 2*len);
			long[] u=divideByX(subtract(resize(a, 2*len), integrate(q)), len);
			
			u = resize(u, 2*len);
			fftTobitReversed(u);
			for (int i = 0; i < u.length; ++i) u[i] = u[i] * expFFT[i] % mod;
			ifftFromBitreversed(u);

			exp = add(exp, multiplyByX(resize(u,len), len));
			
			if (2 * len < a.length) {
				for (int i = len; i < 2 * len; ++i) {
					differentiatedExp[i - 1] = i * exp[i] % mod;
				}
			}
		}
		exp = resize(exp, a.length);
		return exp;
	}

	
	static long[] exp2(long[] a) {
		long[] exp=new long[a.length];
		exp[0]=1;
		for (int len=1;len<a.length;len*=2) {
			long[] tmp=subtract(resize(a,2*len),log(resize(exp,2*len)));
			++tmp[0];
			exp=resize(mul(exp,tmp),2*len);
		}
		return exp;
	}
	
	
	public static long[] differentiate(long[] a) {
		long[] ret=new long[a.length];
		for (int i=1;i<a.length;++i) ret[i-1]=i*a[i]%mod;
		return ret;
	}
	
	public static long[] integrate(long[] a) {
		long[] ret=new long[a.length];
		for (int i=0;i+1<a.length;++i) ret[i+1]=MathUtils.modInv(i+1,mod)*a[i]%mod;
		return ret;
	}
	
	/**
	 * https://judge.yosupo.jp/submission/344599
	 * @param a
	 * @return
	 */
	public static long[] inv(long[] a) {
		long[] g=new long[1];
		g[0]=MathUtils.modInv(a[0],mod);
		for (int len=1;len<a.length;len*=2) {
			long[] fftG=Arrays.copyOf(g, len * 4);
			long[] fftA=new long[4 * len];
			System.arraycopy(a, 0, fftA, 0, Math.min(2 * len, a.length));
			prepareRoots(4*len);
			fftTobitReversed(fftG);
			fftTobitReversed(fftA);
			for (int i = 0; i < fftG.length; ++i) {
				fftG[i] = fftG[i] * fftG[i] % mod * fftA[i] % mod;
			}
			ifftFromBitreversed(fftG);
			
			//  g ← 2g-g²a mod x^{2len}
			
			// mod x^{len}では変化なし
			for (int i = 0; i < len; ++i) {
				fftG[i] = g[i];
			}
			//　更新前のgは [x^len..x^{2len-1}]g = 0
			// 従って -g²a を代入する
			for (int i = len; i < 2 * len; ++i) {
				if (fftG[i]!=0) fftG[i] = mod - fftG[i];
			}
			g=resize(fftG,Math.min(a.length, 2*len));
		}
		return g;
	}
	
	
	public static long[] mul(long[] a,long b) {
		long[] ret=new long[a.length];
		for (int i=0;i<a.length;++i) ret[i]=a[i]*b%mod;
		return ret;
	}
	
	static long[] resize(long[] a,int len) {
		return Arrays.copyOf(a, len);
	}
	
	public PolynomialFp mul(long a) {
		return new PolynomialFp(mul(f, a));
	}
	
	public PolynomialFp mul(PolynomialFp poly) {
		return new PolynomialFp(mul(f, poly.f));
	}
	
	public PolynomialFp sqrt() {
		PolynomialFp poly = new PolynomialFp(null);
		poly.f = exp(f);
		return poly;
	}
	
	public PolynomialFp inv() {
		PolynomialFp poly = new PolynomialFp(null);
		poly.f = inv(f);
		return poly;
	}
	
	public PolynomialFp subtract(PolynomialFp p) {
		PolynomialFp poly = new PolynomialFp(null);
		poly.f = subtract(f, p.f);
		return poly;
	}
	
	public PolynomialFp add(PolynomialFp p) {
		PolynomialFp poly = new PolynomialFp(null);
		poly.f = add(f, p.f);
		return poly;
	}
	/**
	 * f^src/(1-f) を返す
	 */
	public PolynomialFp geometricSum(int src) {
		return new PolynomialFp(mul(pow(f, src), inv(subtract(new long[] {1}, f))));
	}
	
	/**
	 * 1/(1-f)
	 * @param f
	 * @return
	 */
	public static long[] geometricSum(long[] f) {
		return inv(subtract(new long[] {1}, f));
	}
	
	public PolynomialFp resize(int n) {
		return new PolynomialFp(Arrays.copyOf(f, n));
	}
	
	/**
	 * f / x^n を返す。ただし、f は x^n を因数に持つとする
	 */
	
	public static long[] divideByX(long[] f, int repeat) {
		return Arrays.copyOfRange(f, repeat, f.length);
	}
	
	public static long[] multiplyByX(long[] f, int repeat) {
		long[] ret = new long[f.length + repeat];
		for (int i = 0; i < f.length; ++i) ret[repeat + i] = f[i];
		return ret;
	}
	
	public PolynomialFp multiplyByX(int repeat) {
		PolynomialFp poly = new PolynomialFp(null);
		poly.f = multiplyByX(f, repeat);
		return poly;
	}
	
	public PolynomialFp pow(int n) {
		return new PolynomialFp(pow(f, n));
	}

	public static PolynomialFp motzkin(int n) {
		PolynomialFp poly = new PolynomialFp(new long[n]);
		poly.f[0] = 1;
		poly.f[1] = 1;
		for (int i = 2; i < n; ++i) {
			poly.f[i] = ((2 * i + 1)*poly.f[i - 1]+(3*i-3)*poly.f[i-2]) % mod*fp.inv(i + 2);
			poly.f[i] = (poly.f[i] % mod + mod)%mod;
		}
		return poly;
		
	}
	
	/**
	 * [x^0] (1+x^{-1}+x)^n = [x^n] 1/√(1-2x-3xx)
	 * @param n
	 * @return
	 */
	public static PolynomialFp centralTrinomials(int n) {
		//f=1/sqrt(1-2x-3xx)
		//ff(1-2x-3xx)=1
		//ff(-2-6x)+2ff'(1-2x-3xx)=0
		//(1+3x)f=2(1-2x-3xx)f'=0
		PolynomialFp poly = new PolynomialFp(new long[n]);
		poly.f[0] = 1;
		poly.f[1] = 1;
		for (int i = 2; i < n; ++i) {
			poly.f[i] = 2*poly.f[i - 1]+3*poly.f[i-2]-(poly.f[i-1]+3*poly.f[i-2])*fp.inv(i);
			poly.f[i] = (poly.f[i] % mod + mod)%mod;
		}
		return poly;
	}
	
	
	/**
	 *  [x^n] poly(x)mahler(x)mahler(x^m)mahler(x^{2m})...
	 * @param poly
	 * @param mahler
	 * @return
	 */
	public static long nthMahler(String n, int m, long[] poly, long[] mahler, long mod) {
		if (mahler[0] != 1) throw new AssertionError();
		if (m == 10) return nthMahlerBase10(n, poly, mahler);
		BigInteger nBigInt=new BigInteger(n);
		BigInteger mBigInt=BigInteger.valueOf(m);
		long[] g = Arrays.copyOf(poly, poly.length);
		while (!nBigInt.equals(BigInteger.ZERO)) {
			int r = nBigInt.mod(mBigInt).intValue();
			long[] f = PolynomialFp.mul(g, mahler);
			long[] ng = new long[1 + (f.length - 1 - r) / m];
			for (int i = r; i < f.length; i += m) {
				ng[i / m] = f[i];
			}
			g = ng;
			nBigInt = nBigInt.divide(mBigInt);
		}
		return g[0];
	}
	/**
	 *  [x^n] poly(x)mahler(x)mahler(x^m)mahler(x^{2m})...を求める。
	 *  poly, mahlerの次数が(m-1)d, d-1ならば計算量はM(d-1,(m-1)d)log_m(N)
	 *  ただし、M(a,b)は次数a,bの多項式の掛け算の計算量 
	 * @param poly
	 * @param mahler
	 * @return
	 */
	public static long nthMahlerBase10(String n, long[] poly, long[] mahler) {
		if (mahler[0] != 1) throw new AssertionError();
		int m = 10;
		long[] g = Arrays.copyOf(poly, poly.length);
		for (int i = n.length()-1; i >= 0; --i) {
			int r = (int) (n.charAt(i) - '0');
			long[] f = PolynomialFp.mul(g, mahler);
			long[] ng = new long[1 + (f.length - 1 - r) / m];
			for (int j = r; j < f.length; j += m) {
				ng[j / m] = f[j];
			}
			g = ng;
		}
		return g[0];
	}
	
	
    /**
     *  多項式f[0],f[1],..に対して
     * 	[x^n] f[0](x)f[1](x^m)f[2](x^{2m})...を求める。
     *  verified:https://atcoder.jp/contests/abc356/submissions/71206386
     * @param n
     * @param poly
     * @param mahler
     * @return
     */
	public static long nthMahler(long n, long[][] h, int base) {
		long[] g = new long[] {1};
		
		for (int i = 0; i < h.length; i++) {
			if (n == 0) {
				g[0] = g[0] * h[i][0] % mod;
				continue;
			}
			int r = (int) (n % base);
			n /= base;
			long[] f = PolynomialFp.mul(g, h[i]);
			long[] ng = new long[1 + (f.length - 1 - r) / base];
			for (int j = r; j < f.length; j += base) {
				ng[j / base] = f[j];
			}
			g = ng;
		}
		return g[0];
	}
	
	/**
	 * f(x)f(-x)
	 * @param f
	 * @return
	 */
	public static long[] evenMul(long[] f) {
		//https://atcoder.jp/contests/abc241/submissions/73410798
		if(f.length==1) {
			return new long[] {f[0]*f[0]%mod};
		}
		if(f.length + f.length - 1 <= 512) {
			long[]ret=new long[2*f.length-1];
			for (int i = 0; i < f.length; i+=2) {
				for (int j = i+2; j < f.length; j+=2) {
					ret[i+j]=(ret[i+j]+f[i]*f[j]*2)%mod;
				}
				ret[2*i]=(ret[2*i]+f[i]*f[i])%mod;
			}
			for (int i = 1; i < f.length; i+=2) {
				for (int j = i+2; j < f.length; j+=2) {
					ret[i+j]=ret[i+j]+mod-f[i]*f[j]*2%mod;
					if(ret[i+j]>=mod)ret[i+j]-=mod;
				}
				ret[2*i]=(ret[2*i]+mod-f[i]*f[i]%mod);
				if(ret[2*i]>=mod)ret[2*i]-=mod;
			}
		}
		int n=1;
		while (n<f.length+f.length-1) n*=2;
		prepareRoots(n);
		prepareRoots(n/2);
		long[]fft=Arrays.copyOf(f, n);
		fftTobitReversed(fft);
		long[]fft2=new long[n];
		for (int i = 0; i < fft.length; i++) {
			fft2[i]=fft[(i^1)%n];
		}
		for (int i = 0; 2*i < fft.length; i++) {
			fft[i]=fft[2*i]*fft2[2*i]%mod;
		}
		fft=Arrays.copyOf(fft, n/2);
		ifftFromBitreversed(fft);
		long[]ret=new long[f.length+f.length-1];
		for (int i = 0; 2*i < ret.length; i++) {
			ret[2*i]=fft[i];
		}
		return ret;
	}
	
	/***
	 * [x^n] numerator / denominator を Bosta-Mori 法で求める。
	 * m = max(deg numerator, deg denominator) として O(m log m log n) 
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static long nth(long n, long[] numerator, long[] denominator) {
		if (denominator[0] != 1) throw new AssertionError();
		while (n != 0) {
			long[] a = Arrays.copyOf(denominator, denominator.length);
			for (int i = 1; i < a.length; i += 2) a[i] *= -1;
			numerator = mul(numerator, a);
			denominator = evenMul(denominator);
			long[] num2 = new long[(numerator.length + 1) / 2];
			long[] den2 = new long[(denominator.length + 1) / 2];
			for (int i = (int) (n%2); i < numerator.length; i += 2) {
				num2[i / 2] = numerator[i];
			}
			for (int i = 0; i < denominator.length; i += 2) {
				den2[i / 2] = denominator[i];
			}
			numerator = num2;
			denominator = den2;
			n /= 2;
		}
		return (numerator[0] + mod) % mod;
	}

	/**
	 * 線形漸化列 a の k 番目の項を返す。
	 * {@code initial[i] = a_i} とし、
	 * {@code a_n = recurrence[0] a_{n-1} + ... + recurrence[d-1] a_{n-d}} を仮定する。
	 * 未テスト。計算量 O(d log d log k), d = recurrence.length。
	 *
	 * @param initial a_0, ..., a_{d-1}
	 * @param recurrence 漸化係数
	 * @param k 求める項番号
	 * @return a_k
	 */
	public static long kthTermOfLinearlyRecurrentSequence(long[] initial, long[] recurrence, long k) {
		//https://judge.yosupo.jp/submission/371640
		int d = recurrence.length;
		if (initial.length != d) throw new AssertionError();
		long[] denominator = new long[d + 1];
		denominator[0] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			denominator[i + 1] = (mod - c) % mod;
		}
		long[] numerator = Arrays.copyOf(mul(initial, denominator), d);
		return nth(k, numerator, denominator);
	}

	/**
	 * 線形漸化列の k 番目の項を初期 d 項の線形結合で表す係数を返す。
	 * {@code d = recurrence.length} とし、
	 * {@code a_n = recurrence[0] a_{n-1} + ... + recurrence[d-1] a_{n-d}} を仮定する。
	 * 特性多項式を
	 * {@code C(x) = x^d - recurrence[0]x^{d-1} - ... - recurrence[d-1]} とすると、
	 * 返り値は {@code x^k mod C(x)} の係数であり、
	 * {@code a_k = ret[0] a_0 + ... + ret[d-1] a_{d-1}} を満たす。
	 * 未テスト。計算量 O(M(d) log k), d = recurrence.length。
	 *
	 * @param recurrence 漸化係数 {@code recurrence[i] = c_{i+1}}
	 * @param k 求める項番号
	 * @return {@code x^k mod C(x)} の係数
	 */
	public static long[] extendedLinearlyRecurrentSequenceCoefficients(long[] recurrence, long k) {
		if (k < 0) throw new AssertionError();
		int d = recurrence.length;
		if (d == 0) throw new AssertionError();
		long[] characteristic = new long[d + 1];
		characteristic[d] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			characteristic[d - 1 - i] = (mod - c) % mod;
		}
		return Arrays.copyOf(powMod(new long[] {0, 1}, k, characteristic), d);
	}

	/**
	 * 線形漸化列 a の連続する m 項 {@code a_k, ..., a_{k+m-1}} を返す。
	 * {@code d = recurrence.length}, {@code initial[i] = a_i} とし、
	 * {@code a_n = recurrence[0] a_{n-1} + ... + recurrence[d-1] a_{n-d}} を仮定する。
	 * 母関数 {@code A(x) = P(x) / Q(x)} の先頭 {@code m + d - 1} 項を FPS inverse で作り、
	 * {@code x^k mod C(x)} とのスライド内積を畳み込みでまとめて計算する。
	 * 未テスト。計算量 O(M(m + d) + M(d) log k), d = recurrence.length。
	 *
	 * @param initial 初期値 {@code a_0, ..., a_{d-1}}
	 * @param recurrence 漸化係数 {@code recurrence[i] = c_{i+1}}
	 * @param k 先頭の項番号
	 * @param m 返す項数
	 * @return {@code a_k, ..., a_{k+m-1}}
	 */
	public static long[] consecutiveTermsOfLinearlyRecurrentSequence(long[] initial, long[] recurrence, long k, int m) {
		//https://judge.yosupo.jp/submission/371741
		if (k < 0 || m < 0) throw new AssertionError();
		int d = recurrence.length;
		if (initial.length != d) throw new AssertionError();
		if (d == 0) throw new AssertionError();
		if (m == 0) return new long[0];
		long[] denominator = new long[d + 1];
		denominator[0] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			denominator[i + 1] = (mod - c) % mod;
		}
		long[] numerator = Arrays.copyOf(mul(initial, denominator), d);
		int len = m + d - 1;
		long[] prefix = resize(mul(numerator, inv(resize(denominator, len))), len);
		long[] coefficients = extendedLinearlyRecurrentSequenceCoefficients(recurrence, k);
		return validShiftedDotProducts(coefficients, prefix);
	}

	/**
	 * ans[k] = [x^N] x^(s k) / (1 - a x^p - b x^(q k + r)) を k=0..N について返す。未テスト
	 * q>0, r>=0, p>0, s>0 を仮定する。満たさない場合はエラー。//未テスト
	 * 計算量は O(N log N)。
	 * @param N
	 * @param s
	 * @param p
	 * @param q
	 * @param r
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] geometricSumOfxPlusXpowkFixingNForeachK(int N, int s, int p, int q, int r, long a, long b) {
		if (N < 0 || s <= 0 || p <= 0 || q <= 0 || r < 0) throw new AssertionError();
		a %= mod;
		if (a < 0) a += mod;
		b %= mod;
		if (b < 0) b += mod;
		long[] powA = new long[N + 1];
		long[] powB = new long[N + 1];
		powA[0] = 1;
		powB[0] = 1;
		for (int i = 0; i < N; i++) {
			powA[i + 1] = powA[i] * a % mod;
			powB[i + 1] = powB[i] * b % mod;
		}
		long[] ans = new long[N + 1];
		for (int k = 0; k <= N; k++) {
			/*
			 * x^(s k) / (1 - a x^p - b x^(q k + r))
			 * = x^(s k) Σ_{t>=0} (a x^p + b x^(q k + r))^t
			 * = x^(s k) Σ_{i,j>=0} binom(i+j, j) (a x^p)^i (b x^(q k + r))^j
			 * = Σ_{i,j>=0} binom(i+j, j) a^i b^j x^(s k + p i + j(q k + r)).
			 * したがって [x^N] に寄与するのは
			 *   s k + p i + j(q k + r) = N
			 * つまり
			 *   i = (N - s k - j(q k + r)) / p
			 * が非負整数になる j だけ。
			 *
			 * 固定した k では j を
			 *   s k + j(q k + r) <= N
			 * の範囲だけ調べるので O((N - s k) / (q k + r) + 1)。
			 * step=0 となる k=0,r=0 だけは
			 *   1 / (1 - b - a x^p)
			 * = 1/(1-b) * 1/(1 - a/(1-b) x^p)
			 * として別処理する。b=1 なら定数項が 0 なのでエラー。
			 *
			 * それ以外の総計算量は
			 *   O(N + Σ_{0<=k<=N, qk+r>0} (N-sk)/(qk+r))。
			 * q>0, r>=0, s>0 なので Σ N/(qk+r) = O(N log N)。
			 */
			long base = 1L * s * k;
			long step = 1L * q * k + r;
			if (step == 0) {
				//  1 / (1 - b - a x^p)
				// = 1/(1-b) * 1/(1 - a/(1-b) x^p)
				if (b == 1) throw new AssertionError();
				if (N % p == 0) {
					int i = N / p;
					long inv = fp.inv(1 - b);
					ans[k] = powA[i] * fp.pow(inv, i + 1) % mod;
				}
				continue;
			}
			if (base > N) continue;
			long sum = 0;
			for (long jLong = 0, deg = base; deg <= N; jLong++, deg += step) {
				// 上の式 s k + p i + j(q k + r) = N で、
				//   deg = s k + j(q k + r), rem = N - deg = p i。
				long rem = N - deg;
				if (rem % p != 0) continue;
				int i = (int) (rem / p);
				int j = (int) jLong;
				// term = binom(i+j, j) a^i b^j。
				long term = fp.comb(i + j, j) * powA[i] % mod * powB[j] % mod;
				sum += term;
				if (sum >= mod) sum -= mod;
			}
			ans[k] = sum;
		}
		return ans;
	}
	
	/**
	 * f(x+c)
	 * @param f
	 * @param c
	 * @return
	 */
	public static long[] taylorShift(long[]f, long c) {
		// f(x+c) = sum f(c)^{i}/i!x^i
		// f(c)^{i} = sum k!f[k]  c^{k-i}/(k-i)!
		long[] a = new long[f.length];
		long[] b = new long[f.length];
		for (int i = 0; i < f.length; i++) {
			a[i] = fp.fac(i) * f[i] % mod;
		}
		b[0] = 1;
		for (int i = 1; i < f.length; i++) {
			b[i] = fp.mul(b[i - 1], c, fp.inv(i));
		}
		//n-1-i+j=k
		//i-j=k
		ArrayUtils.reverse(a);
		long[] differentiated = mul(a, b);
		differentiated = resize(differentiated, f.length);
		ArrayUtils.reverse(differentiated);
		long[] ret = new long[f.length];
		for (int i = 0; i < f.length; ++i) {
			ret[i] = differentiated[i] * fp.ifac(i) % mod;
		}
		return ret;
	}
	
	/**
	 * f(-x)
	 * @param f
	 * @return
	 */
    public static long[] negatedX(long[] f) {
    	long[]g=Arrays.copyOf(f, f.length);
    	for (int i = 0; i < f.length; i++) {
			if(i%2==1&&g[i]!=0)g[i]=mod-g[i];
		}
    	return g;
    }
    
    /**
     * a=qb+rとしてqを返す
     * @param a
     * @param b
     * @return
     * verified:https://judge.yosupo.jp/submission/319867
     */
    public static long[] divNaive(long[] a, long[] b) {
    	long[] r = a.clone();
    	long[] q = new long[a.length];
    	b=resize(b);
    	long invB=fp.inv(b[b.length-1]);
    	for (int i = r.length - 1; i >= b.length-1; i--) {
    		if (r[i] != 0) {
    			long c=r[i]*invB%mod;
    			q[i-(b.length-1)]=c;
    			for (int j = 0; j < b.length; j++) {
					r[j+i-(b.length-1)]+=mod-c*b[j]%mod;
					r[j+i-(b.length-1)]%=mod;
				}
    		}
		}
    	return resize(q);
    }
    
    
    /**
     * a=qb+rとしてqを返す
     * @param a
     * @param b
     * @return
     * verified:https://judge.yosupo.jp/submission/319867
     */
    public static long[] div(long[] a, long[] b) {
    	/**
    	 * a=qb+r
    	 * deg(r) < deg(b)
    	 * a=qb mod x^n
    	 * d=deg(a)-deg(r)> deg(a)-deg(b)
    	 * A=QB+Rx^d
    	 */	
    	int degA=deg(a);
    	int degB=deg(b);
    	if (degA < degB) return new long[] {0};
    	long[] ra=resize(a);
    	long[] rb=resize(b);
    	ArrayUtils.reverse(ra);
    	ArrayUtils.reverse(rb);
    	rb = resize(rb, degA + 1);
    	long[]q=mul(ra, inv(rb));
    	q=resize(q, degA - degB + 1);
    	ArrayUtils.reverse(q);
    	return q;
    }
    
    /**
     * resize(a, deg(a)+1)
     * @param a
     * @return
     */
    public static long[] resize(long[] a) {
    	//a=0のときdeg(a)=-1なのでmax(0, deg(a))
    	return resize(a, Math.max(0, deg(a))+1);
    }
    
    /**
     * a(x) mod b(x)
     * @param a
     * @param b
     * @return
     */
    public static long[] mod(long[] a, long[] b) {
    	if (a.length + b.length <= 512) {
    		return modNaive(a, b);
    	} else {
    		long[]q=PolynomialFp.div(a, b);
    		long[]r=PolynomialFp.subtract(a, PolynomialFp.mul(b, q));
    		return resize(r);
    	}
    }
    
    public static class DivModResult{
    	public long[] q;
        public long[] r;
        public DivModResult(long[] q, long[] r) {
        	this.q=q;
        	this.r=r;
        }
    }
    
    public static DivModResult divmod(long[]a, long[] b) {
		long[]q=PolynomialFp.div(a, b);
		long[]r=PolynomialFp.subtract(a, PolynomialFp.mul(b, q));
		r=resize(r);    	
    	return new DivModResult(q, r);
    }

    public static class ExtGcdResult {
    	public long[] a;
    	public long[] b;
    	public long[] gcd;
    	// 未テスト
    	public ExtGcdResult(long[] a, long[] b, long[] gcd) {
    		this.a=a;
    		this.b=b;
    		this.gcd=gcd;
    	}
    }
    
    public static class SquareFreeFactor {
    	public long[] factor;
    	public int multiplicity;
    	public SquareFreeFactor(long[] factor, int multiplicity) {
    		this.factor=factor;
    		this.multiplicity=multiplicity;
    	}
    }
    /**
     * a(x) mod b(x)
     * @param a
     * @param b
     * @return
     */
    public static long[] modNaive(long[] a, long[] b) {
        long[]r=Arrays.copyOf(a, a.length);
        int deg=deg(b);
        long[] monicB = Arrays.copyOf(b, deg+1);
        long inv=MathUtils.modInv(b[deg], mod);
        for (int i = 0; i <= deg; i++) {
			monicB[i]=inv*monicB[i]%mod;
		}
        for (int i = r.length - 1; i >= deg; i--) {
			if (r[i] == 0) continue;
			for (int j = 0; j <= deg; ++j) {
				r[j+i-deg]-=monicB[j]*r[i]%mod;//(monicB)x^{i-deg}
				if(r[j+i-deg]<0)r[j+i-deg]+=mod;
			}
		}
        return resize(r);
    }

    /**
     * gcd(a, b)をユークリッドの互除法で求める。
     * @param a
     * @param b
     * @return monicな最大公約多項式
     */
    public static long[] gcdNaive(long[] a, long[] b) {
    	a=resize(a);
    	b=resize(b);
    	while (deg(b) != -1) {
    		long[] r=modNaive(a, b);
    		a=b;
    		b=r;
    	}
    	int deg=deg(a);
    	if (deg == -1) return new long[] {0};
    	long inv=MathUtils.modInv(a[deg], mod);
    	for (int i = 0; i <= deg; i++) {
			a[i]=a[i]*inv%mod;
    	}
    	return resize(a);
    }

    /**
     * half-gcdで得られる2×2の多項式変換行列。
     * 行列 [[p00, p01], [p10, p11]] は、入力ペア(a, b)に作用して
     * ユークリッド互除法を途中まで進めたペア(c, d)を作る。
     */
    public static class HalfGcdResult {
	public long[] p00;
	public long[] p01;
	public long[] p10;
	public long[] p11;
    	public HalfGcdResult(long[] p00, long[] p01, long[] p10, long[] p11) {
		this.p00=p00;
		this.p01=p01;
		this.p10=p10;
		this.p11=p11;
    	}
    	
    	/**
    	 * この2×2変換行列を多項式ペア(a, b)に作用させる。
    	 * 返り値は {p00*a + p01*b, p10*a + p11*b}。
    	 * @param a 多項式a
    	 * @param b 多項式b
    	 * @return 変換後の多項式ペア
    	 */
    	public long[][] apply(long[] a, long[] b) {
    		return new long[][] {
    			resize(add(mul(p00, a), mul(p01, b))),
    			resize(add(mul(p10, a), mul(p11, b)))
    		};
    	}
    	
    	HalfGcdResult swapColumns() {
		return new HalfGcdResult(p01, p00, p11, p10);
	}
    }

	/**
	 * Half-GCDの結果を保持するクラス。
	 * 行列 $M$ と、それに対応するユークリッド商列、および変換後の多項式 $(c, d)^T = M(a, b)^T$ を格納する。
	 */
	public static class HalfGcdResultWithQuotients {
		/** 2x2 変換行列の各成分。 */
		public long[] p00, p01, p10, p11;
		/** 変換行列に対応するユークリッド商のリスト（時系列順）。 */
		public List<long[]> quotients;
		/** 変換後の多項式ペア。$(c, d)^T = M(a, b)^T$ を満たす。 */
		public long[] c, d;
		public HalfGcdResultWithQuotients(long[] p00, long[] p01, long[] p10, long[] p11, List<long[]> quotients, long[] c, long[] d) {
			this.p00 = p00; this.p01 = p01; this.p10 = p10; this.p11 = p11;
			this.quotients = quotients;
			this.c = c; this.d = d;
		}
		/**
		 * 変換行列を多項式ペア $(a, b)$ に作用させる。
		 * @param a 多項式 $a$
		 * @param b 多項式 $b$
		 * @return $(c, d)^T = M(a, b)^T$
		 */
		public long[][] apply(long[] a, long[] b) {
			return new long[][] {
				resize(add(mul(p00, a), mul(p01, b))),
				resize(add(mul(p10, a), mul(p11, b)))
			};
		}
		/**
		 * 列を入れ替えた新しい結果を返す。$\deg a < \deg b$ の場合の初期化に使用する。
		 * @return 列を入れ替えた結果
		 */
		HalfGcdResultWithQuotients swapColumns() {
			List<long[]> newQs = new ArrayList<>();
			newQs.add(new long[0]);
			newQs.addAll(quotients);
			return new HalfGcdResultWithQuotients(p01, p00, p11, p10, newQs, c, d);
		}
	}

    static HalfGcdResult identityMatrix() {
    	return new HalfGcdResult(new long[] {1}, new long[] {0}, new long[] {0}, new long[] {1});
    }

    static HalfGcdResult leftMulEuclideanStep(HalfGcdResult mat, long[] q) {
    	return new HalfGcdResult(
    		mat.p10,
    		mat.p11,
    		subtract(mat.p00, mul(q, mat.p10)),
		subtract(mat.p01, mul(q, mat.p11))
    	);
    }

    static HalfGcdResult multiplyMatrix(HalfGcdResult a, HalfGcdResult b) {
    	return new HalfGcdResult(
    		resize(add(mul(a.p00, b.p00), mul(a.p01, b.p10))),
    		resize(add(mul(a.p00, b.p01), mul(a.p01, b.p11))),
    		resize(add(mul(a.p10, b.p00), mul(a.p11, b.p10))),
		resize(add(mul(a.p10, b.p01), mul(a.p11, b.p11)))
    	);
    }

	static HalfGcdResultWithQuotients identityMatrixWithQuotients() {
		return new HalfGcdResultWithQuotients(new long[] {1}, new long[] {0}, new long[] {0}, new long[] {1}, new ArrayList<>(), null, null);
	}

	static HalfGcdResultWithQuotients leftMulEuclideanStepWithQuotients(HalfGcdResultWithQuotients mat, long[] q) {
		List<long[]> newQs = new ArrayList<>(mat.quotients);
		newQs.add(q);
		return new HalfGcdResultWithQuotients(
			mat.p10,
			mat.p11,
			subtract(mat.p00, mul(q, mat.p10)),
			subtract(mat.p01, mul(q, mat.p11)),
			newQs, null, null
		);
	}

	static HalfGcdResultWithQuotients multiplyMatrixWithQuotients(HalfGcdResultWithQuotients a, HalfGcdResultWithQuotients b) {
		List<long[]> newQs = new ArrayList<>(b.quotients);
		newQs.addAll(a.quotients);
		return new HalfGcdResultWithQuotients(
			resize(add(mul(a.p00, b.p00), mul(a.p01, b.p10))),
			resize(add(mul(a.p00, b.p01), mul(a.p01, b.p11))),
			resize(add(mul(a.p10, b.p00), mul(a.p11, b.p10))),
			resize(add(mul(a.p10, b.p01), mul(a.p11, b.p11))),
			newQs, null, null
		);
	}

    static HalfGcdResult halfGcdNaiveOrdered(long[] a, long[] b) {
    	int threshold=deg(a) / 2;
    	long[] c=a;
    	long[] d=b;
    	HalfGcdResult mat=identityMatrix();
    	while (deg(d) > threshold) {
    		DivModResult divmod=divmod(c, d);
    		mat=leftMulEuclideanStep(mat, divmod.q);
    		c=d;
    		d=divmod.r;
    	}
    	return mat;
    }

	/**
	 * 愚直な方法で Half-GCD のステップを進め、商列を記録する。
	 * @param a 多項式 $a$
	 * @param b 多項式 $b$
	 * @return Half-GCD 結果
	 */
	static HalfGcdResultWithQuotients halfGcdNaiveOrderedWithQuotients(long[] a, long[] b) {
		int threshold=deg(a) / 2;
		long[] c=a;
		long[] d=b;
		HalfGcdResultWithQuotients mat=identityMatrixWithQuotients();
		while (deg(d) > threshold) {
			DivModResult divmod=divmod(c, d);
			mat=leftMulEuclideanStepWithQuotients(mat, divmod.q);
			c=d;
			d=divmod.r;
		}
		mat.c = c; mat.d = d;
		return mat;
	}

	/**
	 * Half-GCD アルゴリズムを用いて、商列を記録しながら変換行列を計算する。
	 * $\deg a = n, \deg b < n$ に対して、$\deg d \le n/2$ となるまでユークリッド互除法を進める。
	 *
	 * <p>計算量: $O(M(n) \log n)$
	 *
	 * @param a 多項式 $a$
	 * @param b 多項式 $b$
	 * @return Half-GCD 結果（商列を含む）
	 */
	public static HalfGcdResultWithQuotients halfGcdWithQuotients(long[] a, long[] b) {
		a=resize(a);
		b=resize(b);
		int degA=deg(a);
		int degB=deg(b);
		if (degB == -1) {
			HalfGcdResultWithQuotients res = identityMatrixWithQuotients();
			res.c = a; res.d = b;
			return res;
		}
		if (degA < degB) {
			HalfGcdResultWithQuotients res = halfGcdWithQuotients(b, a).swapColumns();
			long[][] cd = res.apply(a, b);
			res.c = cd[0]; res.d = cd[1];
			return res;
		}
		if (degB <= degA / 2) {
			HalfGcdResultWithQuotients res = identityMatrixWithQuotients();
			res.c = a; res.d = b;
			return res;
		}
		if (degA <= HALF_GCD_NAIVE_THRESHOLD) return halfGcdNaiveOrderedWithQuotients(a, b);
		int threshold=degA / 2;
		int shift=(degA + 1) / 2;
		HalfGcdResultWithQuotients mat=halfGcdWithQuotients(divideByX(a, shift), divideByX(b, shift));
		long[][] cd=mat.apply(a, b);
		long[] c=cd[0];
		long[] d=cd[1];
		if (deg(d) <= threshold) {
			mat.c = c; mat.d = d;
			return mat;
		}
		DivModResult dm=divmod(c, d);
		mat=leftMulEuclideanStepWithQuotients(mat, dm.q);
		c=d;
		d=dm.r;
		if (deg(d) <= threshold) {
			mat.c = c; mat.d = d;
			return mat;
		}
		int secondShift = 2 * threshold - deg(c);
		if (secondShift < 0) {
			throw new AssertionError("halfGcd invariant broken");
		}
		HalfGcdResultWithQuotients mat2=halfGcdWithQuotients(divideByX(c, secondShift), divideByX(d, secondShift));
		HalfGcdResultWithQuotients res = multiplyMatrixWithQuotients(mat2, mat);
		long[][] cd2 = mat2.apply(c, d);
		res.c = cd2[0]; res.d = cd2[1];
		return res;
	}

	/**
	 * Returns the quotient sequence produced by the Euclidean algorithm on (a, b) naively.
	 *
	 * <p>計算量: O(N^2)
	 */
	public static List<long[]> quotientSequenceNaive(long[] a, long[] b) {
		List<long[]> qs = new ArrayList<>();
		a = resize(a);
		b = resize(b);
		while (deg(b) != -1) {
			DivModResult dm = divmod(a, b);
			qs.add(dm.q);
			a = b;
			b = dm.r;
		}
		return qs;
	}

	/**
	 * Returns the quotient sequence produced by the Euclidean algorithm on (a, b).
	 *
	 * If a = q0 b + r1,
	 *    b = q1 r1 + r2,
	 *    r1 = q2 r2 + r3, ...
	 * then this method returns [q0, q1, q2, ...].
	 *
	 * <p>計算量: O(M(N) \log N)
	 */
	public static List<long[]> quotientSequenceFast(long[] a, long[] b) {
		a = resize(a); b = resize(b);
		if (deg(b) == -1) return new ArrayList<>();
		if (deg(a) < deg(b)) {
			DivModResult dr = divmod(a, b);
			List<long[]> res = new ArrayList<>();
			res.add(dr.q);
			res.addAll(quotientSequenceFast(b, dr.r));
			return res;
		}
		HalfGcdResultWithQuotients h = halfGcdWithQuotients(a, b);
		List<long[]> ans = new ArrayList<>(h.quotients);
		long[] c = h.c;
		long[] d = h.d;
		if (deg(d) == -1) return ans;
		DivModResult dr = divmod(c, d);
		ans.add(dr.q);
		ans.addAll(quotientSequenceFast(d, dr.r));
		return ans;
	}

    /**
     * half-gcdの変換行列を返す。
     * aを高次側、bを低次側として扱う。deg(a) < deg(b)の場合は内部で入れ替える。
     * 返り値Mについて、M(a,b)^T = (c,d)^T は gcd(a,b) を保ち、
     * deg(d) <= deg(a) / 2 までユークリッド互除法を進めた状態になる。
     * @param a 多項式a
     * @param b 多項式b
     * @return half-gcdの変換行列
     */
    public static HalfGcdResult halfGcd(long[] a, long[] b) {
	a=resize(a);
	b=resize(b);
	int degA=deg(a);
	int degB=deg(b);
	if (degB == -1) return identityMatrix();
	if (degA < degB) return halfGcd(b, a).swapColumns();
	if (degB <= degA / 2) return identityMatrix();
	if (degA <= HALF_GCD_NAIVE_THRESHOLD) return halfGcdNaiveOrdered(a, b);
	int threshold=degA / 2;
	int shift=(degA + 1) / 2;
	// まず高次側だけを見て、上位係数から分かるユークリッドステップをまとめて進める。
	// 再帰に渡す次数が floor(degA/2) で、第二成分を floor(degA/4) まで落とすので、
	// mat の成分の次数が ceil(degA/4) で抑えられる。
	// したがって、切り捨てた部分にmatを掛けても次数は ceil(degA/4) + ceil(deg(A)/2) - 1 で抑えられる。
	HalfGcdResult mat=halfGcd(divideByX(a, shift), divideByX(b, shift));
	long[][] cd=mat.apply(a, b);
	long[] c=cd[0];
	long[] d=cd[1];
	if (deg(d) <= threshold) return mat;
	// c, d ともに次数 3deg(A) / 4 + 1 以下にする。
	DivModResult dm=divmod(c, d);
	mat=leftMulEuclideanStep(mat, dm.q);
	c=d;
	d=dm.r;
	if (deg(d) <= threshold) return mat;
	// 残った部分に対してもう一度half-gcdをかける。
	// 元の次数に戻したときdeg(第2成分) <= thresholdになるよう、
	// 現在の第1成分cの次数から必要な高次シフト量を決める。
	int secondShift = 2 * threshold - deg(c);
	// σ = secondShift と置く。
	// halfGcd後は第二成分の次数が (deg(c)-σ) / 2 以下になる
	// 元の次数に戻すと
	// σ + (deg(c)-σ) / 2 ≤ threshold
	// σ ≤　2 threshold - deg(c)
	// また、切り捨てた項については、matの次数が (deg(c)-(2(threshold)-deg(c))) / 2 = deg(c) - threshold 以下で
	// これに 2 (threshold) - deg(c) - 1 を足しても threshold 以下なのでよい。
	if (secondShift < 0) {
		throw new AssertionError("halfGcd invariant broken");
	}
	HalfGcdResult mat2=halfGcd(divideByX(c, secondShift), divideByX(d, secondShift));
	return multiplyMatrix(mat2, mat);
    }

    /**
     * half-gcdを用いてgcd(a, b)を求める。
     * @param a 多項式a
     * @param b 多項式b
     * @return monicな最大公約多項式
     */
    public static long[] gcd(long[] a, long[] b) {
    	a=resize(a);
    	b=resize(b);
    	if (Math.max(deg(a), deg(b)) <= 3072) return gcdNaive(a, b);
    	if (deg(a) < deg(b)) {
    		long[] tmp=a;
    		a=b;
    		b=tmp;
    	}
    	while (deg(b) != -1) {
    		HalfGcdResult mat=halfGcd(a, b);
    		long[][] cd=mat.apply(a, b);
    		a=cd[0];
    		b=cd[1];
    		if (deg(b) == -1) break;
    		DivModResult divmod=divmod(a, b);
    		a=b;
    		b=divmod.r;
    		if (deg(a) < deg(b)) {
    			long[] tmp=a;
    			a=b;
    			b=tmp;
    		}
    	}
    	return monic(a);
    }

    /**
     * aCoef*f + bCoef*g = gcd(f, g)となるaCoef, bCoef, gcdを返す。未テスト
     * @param f 多項式f
     * @param g 多項式g
     * @return Bezout係数とmonicな最大公約多項式
     */
    public static ExtGcdResult extgcd(long[] f, long[] g) {
    	f=resize(f);
    	g=resize(g);
    	long[] a=f;
    	long[] b=g;
    	long[] x0=new long[] {1};
    	long[] y0=new long[] {0};
    	long[] x1=new long[] {0};
    	long[] y1=new long[] {1};
    	if (Math.max(deg(a), deg(b)) <= 3072) {
    		while (deg(b) != -1) {
    			DivModResult divmod=divmod(a, b);
    			long[] nx=subtract(x0, mul(divmod.q, x1));
    			long[] ny=subtract(y0, mul(divmod.q, y1));
    			a=b;
    			b=divmod.r;
    			x0=x1;
    			y0=y1;
    			x1=resize(nx);
    			y1=resize(ny);
    		}
    		return normalizeExtGcdResult(x0, y0, a);
    	}
    	if (deg(a) < deg(b)) {
    		long[] tmp=a;
    		a=b;
    		b=tmp;
    		tmp=x0;
    		x0=x1;
    		x1=tmp;
    		tmp=y0;
    		y0=y1;
    		y1=tmp;
    	}
    	while (deg(b) != -1) {
    		//half-gcdをしてから1stepユークリッドの互除法を進めると次数が半分になる
    		HalfGcdResult mat=halfGcd(a, b);
    		long[][] cd=mat.apply(a, b);
    		long[] nx0=resize(add(mul(mat.p00, x0), mul(mat.p01, x1)));
    		long[] ny0=resize(add(mul(mat.p00, y0), mul(mat.p01, y1)));
    		long[] nx1=resize(add(mul(mat.p10, x0), mul(mat.p11, x1)));
    		long[] ny1=resize(add(mul(mat.p10, y0), mul(mat.p11, y1)));
    		a=cd[0];
    		b=cd[1];
    		x0=nx0;
    		y0=ny0;
    		x1=nx1;
    		y1=ny1;
    		if (deg(b) == -1) break;
    		DivModResult divmod=divmod(a, b);
    		nx1=resize(subtract(x0, mul(divmod.q, x1)));
    		ny1=resize(subtract(y0, mul(divmod.q, y1)));
    		a=b;
    		b=divmod.r;
    		x0=x1;
    		y0=y1;
    		x1=nx1;
    		y1=ny1;
    		if (deg(a) < deg(b)) {
    			long[] tmp=a;
    			a=b;
    			b=tmp;
    			tmp=x0;
    			x0=x1;
    			x1=tmp;
    			tmp=y0;
    			y0=y1;
    			y1=tmp;
    		}
    	}
    	return normalizeExtGcdResult(x0, y0, a);
    }

    // 未テスト
    static ExtGcdResult normalizeExtGcdResult(long[] aCoef, long[] bCoef, long[] gcd) {
    	gcd=resize(gcd);
    	int deg=deg(gcd);
    	if (deg == -1) return new ExtGcdResult(new long[] {0}, new long[] {0}, new long[] {0});
    	long inv=MathUtils.modInv(gcd[deg], mod);
    	return new ExtGcdResult(resize(mul(aCoef, inv)), resize(mul(bCoef, inv)), resize(mul(gcd, inv)));
    }

    static long[] monic(long[] a) {
    	a=resize(a);
    	int deg=deg(a);
    	if (deg == -1) return new long[] {0};
    	long inv=MathUtils.modInv(a[deg], mod);
    	for (int i = 0; i <= deg; i++) {
			a[i]=a[i]*inv%mod;
		}
    	return a;
    }

    /**
     * fをYunのアルゴリズムでsquare-free分解する。
     * 各factorは、fの既約因子のうちmultiplicity回現れるものの積。
     * 入力fは変更しない。未テスト
     * @param f
     * @return factorとmultiplicityの配列
     */
    public static SquareFreeFactor[] factorSquareFree(long[] inputf) {
    	var f=resize(inputf);
    	if (deg(f) <= 0) return new SquareFreeFactor[0];
    	long[] df=differentiate(f);
    	long[] g=gcd(f, df);
    	// f = Π[i≥1] z[i]^i (z[i]は重複度iの既約多項式の積）とすると
    	// g = gcd(f, f') = Π[i≥2] z[i]^{i-1}
    	// w = Π[i≥1] z[i] と置くと
    	// f'=g'w+gw'
    	// f'-gw'=g'w
    	// f'/g-w'=g'w/g (g | g'w なので単に割り算）
    	// g'/g=∑(i-1)z[i]'/z[i]
    	// wg'/g=∑(i-1)z[i]'Π[j≠i] z[j]
    	// gcd(g'w/g, w) = z[1]
    	if (deg(g) <= 0) return new SquareFreeFactor[] {new SquareFreeFactor(monic(f), 1)};
    	long[] w=div(f, g);//Π[i≥1] z[i]
    	long[] c=div(df, g);//
    	long[] d=subtract(c, differentiate(w));
    	ArrayList<SquareFreeFactor> ret=new ArrayList<>();
    	for (int multiplicity = 1; deg(w) > 0; multiplicity++) {
    		// m = multiplicity とする。
    		// ループ開始時：
    		// w = Π[i≥m] z[i]
    		// c =  w ∑[i≥m] (i-m+1) z'[i]/z[i]
    		// d = w ∑[i≥m] (i-m) z'[i]/z[i]
    		long[] a=gcd(w, d);//z[m]
    		if (deg(a) > 0) {
    			ret.add(new SquareFreeFactor(a, multiplicity));
    		}
    		w=div(w, a);
    		c=div(d, a);
    		d=subtract(c, differentiate(w));
    	}
    	return ret.toArray(new SquareFreeFactor[ret.size()]);
    }

    public static class DistinctDegreeFactor {
    	public long[] factor;
    	public int degree;
    	public DistinctDegreeFactor(long[] factor, int degree) {
    		this.factor=factor;
    		this.degree=degree;
    	}
    }

    public static class Factor {
    	public long[] factor;
    	public int multiplicity;
    	public Factor(long[] factor, int multiplicity) {
    		this.factor=factor;
    		this.multiplicity=multiplicity;
    	}
    }

    /**
     * P(x) / Π factors[i].factor^{factors[i].multiplicity} を部分分数分解する。未テスト
     * @param p 分子
     * @param factors monicな既約因子と重複度
     * @return 部分分数分解の結果
     */
    public static PartialFractionDecomposition.Result partialFractionDecomposition(long[] p, Factor[] factors) {
    	return PartialFractionDecomposition.decompose(p, factors);
    }

    /**
     * P(x) / Q(x) を部分分数分解する。未テスト
     * @param p 分子
     * @param q 分母
     * @return 部分分数分解の結果
     */
    public static PartialFractionDecomposition.Result partialFractionDecomposition(long[] p, long[] q) {
    	q=resize(q);
    	int degQ=deg(q);
    	if (degQ == -1) throw new ArithmeticException("division by zero polynomial");
    	long[] normalizedP=p;
    	if (q[degQ] != 1) normalizedP=mul(p, MathUtils.modInv(q[degQ], mod));//factorがmonicに変換されるため
    	return PartialFractionDecomposition.decompose(normalizedP, factor(q));
    }

    static long[] mulMod(long[] a, long[] b, long[] m) {
    	return mod(mul(a, b), m);
    }

    static long[] powMod(long[] a, BigInteger e, long[] m) {
    	long[] ret=mod(new long[] {1}, m);
    	long[] base=mod(a, m);
    	for (int i = e.bitLength() - 1; i >= 0; i--) {
    		ret=mulMod(ret, ret, m);
    		if (e.testBit(i)) ret=mulMod(ret, base, m);
    	}
    	return ret;
    }

    /**
     * a(x)^e mod m(x) を二分累乗で計算する。
     * @param a 底となる多項式
     * @param e 指数
     * @param m 法多項式
     * @return a(x)^e mod m(x)
     */
    public static long[] powMod(long[] a, long e, long[] m) {
    	long[] ret=mod(new long[] {1}, m);
    	long[] base=mod(a, m);
    	for (int i = 63 - Long.numberOfLeadingZeros(e); i >= 0; i--) {
    		ret=mulMod(ret, ret, m);
    		if (((e >>> i) & 1) != 0) ret=mulMod(ret, base, m);
    	}
    	return ret;
    }

    static long[] equalDegreePower(long[] a, int degree, long[] m) {
    	long[] x=powMod(a, (mod - 1) / 2, m);
    	long[] ret=mod(new long[] {1}, m);
    	for (int i = 0; i < degree; i++) {
    		ret=mulMod(ret, x, m);
    		if (i + 1 < degree) x=powMod(x, mod, m);
    	}
    	return ret;
    }

    static long[] randomPolynomial(int len, Random rnd) {
    	long[] ret=new long[len];
    	for (int i = 0; i < len; i++) {
			ret[i]=rnd.nextLong(mod);
		}
    	return resize(ret);
    }

    /**
     * Equal-Degree Factorization.
     * fはsquare-freeで、すべての既約因子の次数がdegreeであることを仮定する。
     * 入力fは変更しない。
     * @param f
     * @param degree
     * @return monicな既約因子の配列
     */
    public static long[][] factorEqualDegree(long[] inputf, int degree) {
    	if (degree <= 0) throw new AssertionError();
    	var f=monic(inputf);
    	int deg=deg(f);
    	if (deg <= 0) return new long[0][];
    	if (deg % degree != 0) throw new AssertionError();
    	Random rnd=new Random();
    	ArrayList<long[]> ret=new ArrayList<>();
    	Queue<long[]> que=new ArrayDeque<>();
    	que.add(f);
    	while (!que.isEmpty()) {
    		long[] cur=monic(que.poll());
    		int curDeg=deg(cur);
    		if (curDeg == degree) {
    			ret.add(cur);
    			continue;
    		}
    		if (curDeg % degree != 0) throw new AssertionError();
    		long[] g;
    		do {
    			long[] a=randomPolynomial(curDeg, rnd);
    			long[] h=subtract(equalDegreePower(a, degree, cur), new long[] {1});
    			g=gcd(cur, h);
    		} while (deg(g) <= 0 || deg(g) == curDeg);
    		que.add(g);
    		que.add(div(cur, g));
    	}
    	return ret.toArray(new long[ret.size()][]);
    }

    public static long[][] factorEqualDegreeNaive(long[] f, int degree) {
    	return factorEqualDegree(f, degree);
    }

    /**
     * 多項式を既約因子に分解する。
     * DDFで次数ごとの積を取り出し、EDFで既約因子に分解し、multiplicityは元の多項式への試し割で求める。
     * @param inputf
     * @return monicな既約因子とmultiplicityの配列
     */
    public static Factor[] factor(long[] inputf) {
    	long[] f=monic(inputf);
    	if (deg(f) <= 0) return new Factor[0];
    	ArrayList<Factor> ret=new ArrayList<>();
    	long[] x=new long[] {0, 1};
    	long[] h=x;
    	for (int degree = 1; deg(f) > 0 && 2 * degree <= deg(f); degree++) {
    		h=powMod(h, mod, f);
    		long[] g=gcd(f, subtract(h, x));
    		if (deg(g) > 0) {
    			for (long[] factor: factorEqualDegree(g, degree)) {
    				int multiplicity=0;
    				while (deg(f) >= deg(factor)) {
    					DivModResult divmod=divmod(f, factor);
    					if (deg(divmod.r) != -1) break;
    					f=divmod.q;
    					multiplicity++;
    				}
    				if (multiplicity == 0) throw new AssertionError();
    				ret.add(new Factor(factor, multiplicity));
    			}
    			if (deg(f) <= 0) break;
    			h=mod(h, f);
    		}
    	}
    	if (deg(f) > 0) {
    		ret.add(new Factor(monic(f), 1));
    	}
    	return ret.toArray(new Factor[ret.size()]);
    }
    
    public static int deg(long[] a) {
    	return ArrayUtils.maxDecrement(0, a.length-1, id->a[id]==0);
    }
    
    
    
	/**
	 * A(x)=sum a[i]x^iに対して、x=points[i]を代入した値を求める。
	 * @param a
	 * @param points
	 */
	public static long[] multipointEval(long[]a, long[] points) {
		int M = points.length;
		int len=1;
		while (len < M) len*=2;
		long[][]mods=new long[2*len][];
		long[][]modded=new long[2*len][];
		for (int i = 0; i < len; i++) {
			if (i < M)
				mods[i+len]=new long[] {(mod-points[i])%mod,1};
			else
				mods[i+len]=new long[] {1};
		}
		for (int i = len - 1, e=0; i >= 1; i--) {
			if(mods[2*i].length==mods[2*i+1].length&&mods[2*i].length==1+(1<<e)&&e>=10) {
				//長さ1+2^e（次数2^e)の多項式同士を掛け算
				//本来 mod x^{2^{e+2}-1}-1 で計算するが
				// mod x^{2^{e+1}}-1 で計算すると、最高次数の項x^2^{e+1}だけが初項に回りこむがこれを後で調整する。
				mods[i] = PolynomialFp.cyclicmulFFT(mods[2*i], mods[2*i+1], 1<<(e+1));
				mods[i] = Arrays.copyOf(mods[i], 1+(1<<(e+1)));
				mods[i][0] -= 1;
				if (mods[i][0]<0)mods[i][0]+=mod;
				mods[i][1<<(e+1)]=1;
			} else {
				mods[i] = PolynomialFp.mul(mods[2*i], mods[2*i+1]);
			}
		}
		modded[1]=mod(a, mods[1]);
		for (int i = 2; i < 2*len; i++) {
			modded[i]=mod(modded[i/2], mods[i]);//ここの計算がボトルネック。modsの計算の十倍重い。
		}
		long[]ret=new long[M];
		for (int i = 0; i < M; i++) {
			if (modded[i+len].length > 0)
				ret[i]=modded[i+len][0];
		}
		return ret;
	}
	
	/**
	 * exp(-x)
	 * 未テスト
	 * @param n
	 * @return
	 */
	public static long[] invExp(int n) {
        long[] iexp = new long[n];
        for (int i = 0; i < iexp.length; i++) {
        	if (i%2==0) {
        		iexp[i] = fp.ifac(i);
        	} else {
        		iexp[i] = mod - fp.ifac(i);
        	}
        }
        return iexp;
	}
	
	
	/**
	 * exp(x)
	 * 未テスト
	 * @param n
	 * @return
	 */
	public static long[] exp(int n) {
        long[] exp = new long[n];
        for (int i = 0; i < exp.length; i++) {
        	exp[i] = fp.ifac(i);
        }
        return exp;
	}
	
	/**
	 * c[s] = sum_i a[i]b[i+s] for each 0 ≤ s ≤ len(b)-len(a)を返す。
	 * aをシフトしたときにbに完全に覆われるものだけ計算する。	
	 * @param a
	 * @param b
	 * @return
	 * verified:https://atcoder.jp/contests/abc291/submissions/70727681
	 */
	public static long[] validShiftedDotProducts(long[] a, long[] b) {
		/*
		* 以下、実装方針。
		 * n=a.length, n+m-1=b.lengthとするとc.length=m
		 * x^{n-1}a(x^{-1})b(x)を計算
		 * すればよい。
		 * x^{n-1}からx^{n+m-2}までだけが必要なので
		 * x^{n+m-1},..,x^{2n+m-2} は mod x^{n+m-1} で回り込ませても問題ない。
		 */
		long[]A=a.clone();
		ArrayUtils.reverse(A);
		int len=1;
		while(len<b.length)len*=2;
		A=cyclicmulFFT(A, b, len);
		return Arrays.copyOfRange(A, a.length-1, b.length);
	}

	/**
	 * 次数 N 未満の多項式 f について、f(0), f(1), ..., f(N-1) から
	 * f(c), f(c+1), ..., f(c+M-1) を返す。
	 * 未テスト
	 * @param y y[i] = f(i)
	 * @param c 開始点
	 * @param M 返す値の個数
	 * @return shifted samples
	 */
	public static long[] samplePointShift(long[] y, long c, int M) {
		//https://judge.yosupo.jp/submission/370795
		if (M < 0) throw new AssertionError();
		int N = y.length;
		long[] ret = new long[M];
		if (N == 0 || M == 0) return ret;
		if (N == 1) {
			Arrays.fill(ret, y[0]);
			return ret;
		}
		if (N > mod) throw new AssertionError();
		c %= mod;
		if (c < 0) c += mod;
		// ラグランジュ補間より
		// f(x) = sum_i y[i] prod_{i ≠ j} (x - j) / (i - j)
		// f(x) = sum_i y[i] prod_{i ≠ j} (x - j) / {(-1)^(N - 1 - i) (N - 1 - i)! i!}
		// f(x) = sum_i weights[i] prod_{i ≠ j} (x - j)
		long[] weights = new long[N];
		for (int i = 0; i < N; i++) {
			long w = y[i] * fp.ifac(i) % mod * fp.ifac(N - 1 - i) % mod;
			if ((N - 1 - i) % 2 == 1 && w != 0) w = mod - w;
			weights[i] = w;
		}
		// f(c + t) = (sum_i  weights[i] / (c + t - i)) prod_j (c + t - j)
		// f(c + t) = (sum_i  weights[i] invs[N - 1 - i + t]) prod_j (c + t - j)
		long[] invs = new long[N + M - 1];
		long start = c - (N - 1);
		start %= mod;
		if (start < 0) start += mod;
		for (int i = 0; i < invs.length; i++) {
			long v = c - N + 1 + i;
			v = fp.reduce(v);
			invs[i] = fp.inv(v);//weights[i] / (c + t - i) の分母が 0 になる場合（既知の評価）は壊れるが、後で場合分けして処理。
		}
		long[] sums = mul(weights, invs);
		long prod = 1;
		int zeroCount = 0;
		// prod = prod_j (c - j)
		for (int r = -(N - 1); r <= 0; r++) {
			long v = c + r;
			v %= mod;
			if (v < 0) v += mod;
			if (v == 0) zeroCount++;
			else prod = prod * v % mod;
		}
		// sums[N - 1 + t] = sum_i  weights[i] invs[N - 1 - i + t]
		// prod =  prod_j (c + t - j) を用いて
		// f(c + t) = sums[N - 1 + t] * prod
		for (int t = 0; t < M; t++) {
			long x = c + t;
			x %= mod;
			if (x >= 0 && x < N) {
				ret[t] = y[(int) x];
			} else if (zeroCount == 0) {
				ret[t] = prod * sums[N - 1 + t] % mod;
			}
			if (t + 1 < M) {// prod を更新
				long out = c + t - (N - 1);
				out %= mod;
				if (out < 0) out += mod;
				if (out == 0) zeroCount--;
				else prod = prod * fp.inv(out) % mod;
				long in = c + t + 1;
				in %= mod;
				if (in < 0) in += mod;
				if (in == 0) zeroCount++;
				else prod = prod * in % mod;
			}
		}
		return ret;
	}
	
	
	/**
	 * 配列 a と b の全てのずらし内積を返す。
	 * {@code N = a.length}, {@code M = b.length} とすると、返り値 c は長さ {@code N + M - 1} で、
	 * {@code -(N - 1) <= s <= M - 1} について
	 * {@code c[N - 1 + s] = Σ_i a[i] b[i + s]} を満たす。
	 * 範囲外の {@code b[i + s]} は 0 とみなす。
	 * 未テスト。計算量 O(T(N + M)), T(L) は長さ L 程度の畳み込みの計算量。
	 *
	 * @param a 左側の配列
	 * @param b 右側の配列
	 * @return 全てのずらし内積
	 * https://atcoder.jp/contests/abc409/submissions/73358071
	 */
	public static long[] fullShiftedDotProducts(long[] a, long[] b) {
		long[]A=a.clone();
		ArrayUtils.reverse(A);
		return mul(A, b);
	}

	public static class Term {
		public int d;
		public long v;
		public Term(int d, long v) { this.d = d; this.v = v; }
	}

	/**
	 * 稀な多項式の累乗を O(nk) で求める。
	 * ここで n は求める次数、k は入力多項式 f の非ゼロ項数である。
	 * @param f 多項式
	 * @param n 求める次数
	 * @param k 指数
	 * @return f(x)^k mod x^n
	 */
	public static long[] sparsePow(long[] f, int n, long k) {
		if (n <= 0) return new long[0];
		if (k == 0) {
			long[] res = new long[n];
			res[0] = 1;
			return res;
		}
		int d0 = 0;
		while (d0 < f.length && d0 < n && fp.reduce(f[d0]) == 0) d0++;
		if (d0 == f.length || d0 >= n) return new long[n];
		if (d0 > 0 && (n - 1) / d0 < k) return new long[n];
		int bias = (int) (d0 * k);
		if (bias >= n) return new long[n];

		ArrayList<Term> terms = new ArrayList<>();
		for (int i = d0 + 1; i < f.length && i < n; i++) {
			long v = fp.reduce(f[i]);
			if (v != 0) terms.add(new Term(i - d0, v));
		}
		long[] res = new long[n];
		long f0 = fp.reduce(f[d0]);
		res[bias] = MathUtils.modPow(f0, k % (mod - 1), mod);
		long inv0 = fp.inv(f0);
		long kMod = k % mod;
		for (int d = 0; bias + d + 1 < n; d++) {
			long tmp = 0;
			for (Term t : terms) {
				int j = d - t.d;
				if (j >= 0) {
					tmp = (tmp + mod - t.v * res[bias + j + 1] % mod * (j + 1) % mod) % mod;
				}
				j = d - (t.d - 1);
				if (j >= 0) {
					tmp = (tmp + t.v * t.d % mod * res[bias + j] % mod * kMod % mod) % mod;
				}
			}
			res[bias + d + 1] = tmp * inv0 % mod * fp.inv(d + 1) % mod;
		}
		return res;
	}

	/**
	 * 多項式の累乗 a(x)^e を計算します。
	 * PolynomialFp.pow と異なり、次数の切り捨てを行わず、正確な多項式を返します。
	 * @param a 底となる多項式
	 * @param e 指数
	 * @return a(x)^e
	 */
	public static long[] powFull(long[] a, int e) {
		if (e == 0) return new long[] { 1 };
		if (e == 1) return a.clone();
		int d = deg(a);
		if (d == -1) return new long[0];
		if (d == 0) return new long[] { MathUtils.modPow(a[0], e, mod) };
		if (d <= 40) {
			return sparsePow(a, d * e + 1, e);
		}
		long[] res = {1};
		long[] base = a;
		while (e > 0) {
			if (e % 2 == 1) res = PolynomialFp.mul(res, base);
			base = PolynomialFp.mul(base, base);
			e /= 2;
		}
		return res;
	}

    public String toRationalSeries(long[] a) {
    	StringBuilder sb=new StringBuilder();
    	for (int i = 0; i < a.length; i++) {
    		long num=a[i];
    		long den=1;
    		for (int j = -100; j < 100; j++) {
				for (int k = 1; k < 100; k++) {
					if((j+mod)%mod*MathUtils.modInv(k, mod)%mod==a[i]) {
						if(Math.abs(j)+k<num+den) {
							num=j;
							den=k;
						}
					}
				}
			}
    		sb.append((num>=0?"+":"-")+num+"/"+den);
		}
    	return sb.toString();
    }
    
    /**
     * f[0], f[1], .. の総積を O(Nlog(N)^2) で求める。ただし、Nは長さの和。
     * f.length=0のときは1を返す
     * @param f
     * @return
     */
    public static long[] mulAll(long[][] f) {
        // https://atcoder.jp/contests/abc331/submissions/72411296
        if (f.length == 0) return new long[] { 1 };
        long[][] copy = f.clone();
        Arrays.sort(copy, (a, b) -> Arrays.compare(a, b));
        Queue<long[]> pq = new ArrayDeque<>();
        for (int i = 0; i < copy.length; ) {
            int j = i;
            while (j < copy.length && Arrays.equals(copy[i], copy[j])) j++;
            int count = j - i;
            if (count == 1) {
                pq.add(copy[i]);
            } else {
                pq.add(powFull(copy[i], count));
            }
            i = j;
        }
        while (pq.size() >= 2) {
            var a = pq.poll();
            var b = pq.poll();
            pq.add(PolynomialFp.mul(a, b));
        }
        return pq.peek();
    }

    /**
     * f[0], f[1], .. の総積を O(Nlog(N)^2) で求める。ただし、Nは長さの和。
     * https://atcoder.jp/contests/abc331/submissions/72411296
     * @param f
     * @return
     */
    public static long[] mulAll(long[][] f, int cutoff) {
        if (f.length == 0) return new long[] { 1 };
        long[][] copy = f.clone();
        Arrays.sort(copy, (a, b) -> Arrays.compare(a, b));
        Queue<long[]> pq = new ArrayDeque<>();
        for (int i = 0; i < copy.length; ) {
            int j = i;
            while (j < copy.length && Arrays.equals(copy[i], copy[j])) j++;
            int count = j - i;
            if (count == 1) {
                pq.add(copy[i]);
            } else {
                long[] res = powFull(copy[i], count);
                if (res.length > cutoff) res = Arrays.copyOf(res, cutoff);
                pq.add(res);
            }
            i = j;
        }
        while (pq.size() >= 2) {
            var a = pq.poll();
            var b = pq.poll();
            var c = PolynomialFp.mul(a, b);
            if (c.length > cutoff) c = Arrays.copyOf(c, cutoff);
            pq.add(c);
        }
        return pq.peek();
    }
    
    
    public static long[] sparseInv(long[] a) {
    	if(a[0]==0)throw new AssertionError();
    	ArrayList<Integer> degs=new ArrayList<>();
    	ArrayList<Long> coefs=new ArrayList<>();
    	for (int i = 1; i < a.length; i++) {
			if(a[i]!=0) {
				degs.add(i);
				coefs.add(a[i]);
			}
		}
    	long[]b=new long[a.length];
    	long constInv=fp.inv(mod-a[0]);
    	b[0]=fp.inv(a[0]);
    	for (int i = 1; i < a.length; i++) {
    		for (int j=0;j<degs.size();++j) {
    			int deg=degs.get(j);
    			if(i-deg<0)break;
    			b[i]+=coefs.get(j)*b[i-deg];
    			b[i]%=mod;
    		}
    		b[i]=constInv*b[i]%mod;
    	}
    	return b;
    }
    
    
	/**
	 * 複数の有理式の和を計算し、その結果を 1 つの有理式 {@code f(x) / g(x)} として返す。
	 *
	 * <p>
	 * **有理式の表現形式:**
	 * 各有理式は {@code long[][]}（2要素の配列）として表されます。
	 * <ul>
	 * <li>{@code rational[0]} : 分子多項式（numerator）</li>
	 * <li>{@code rational[1]} : 分母多項式（denominator）</li>
	 * </ul>
	 *
	 * <p>
	 * **加算アルゴリズム:**
	 * {@code Queue} を用いて有理式を2つずつ取り出し、以下の有理数の加算規則を多項式に繰り返し適用します。
	 * </p>
	 *
	 * <pre>
	 * A/B + C/D = (AD + BC) / (BD)
	 * </pre>
	 *
	 * <p>
	 * 多項式の加算および乗算には {@link PolynomialFp} の演算（{@code add} および {@code mul}）を用います。
	 * **注意:** 計算結果に対する既約化（分子・分母の共通因子による約分）は**行いません**。
	 * </p>
	 *
	 * @param rationals 加算される有理式の配列。
	 * 型は {@code long[][][]} で、各要素は有理式 {@code {numerator, denominator}} の形式を持つ {@code long[][]} です。
	 * @return          すべての有理式の和を表す単一の有理式 {@code long[][] }。
	 * {@code result[0]} が分子多項式、
	 * {@code result[1]} が分母多項式を表します。
	 *
	 * @see PolynomialFp#add(long[], long[])
	 * @see PolynomialFp#mul(long[], long[])
	 * https://atcoder.jp/contests/abc439/submissions/72223955
	 */
    public static long[][] sumRationals(long[][][] rationals) {
		Queue<long[][]> que=new ArrayDeque<>();
		for (int i = 0; i < rationals.length; i++) {
			que.add(rationals[i]);
		}
		while(que.size() >= 2) {
			var a=que.poll();
			var b=que.poll();
			
			long[] numerator=PolynomialFp.add(PolynomialFp.mul(a[0], b[1]), PolynomialFp.mul(a[1], b[0]));
			long[] denominator=PolynomialFp.mul(a[1], b[1]);
			que.add(new long[][] {numerator, denominator});
		}
		return que.poll();
	}
    /**
     * https://judge.yosupo.jp/submission/348454
     * @param x
     * @param y
     * @return
     */
	public static long[] interpolate(long[]x, long[] y) {
		/*
		 * f =(x-x_1)..(x-x_n)
		 * f'(x_i)=Π_{i≠j}(x_i-x_j)
		 * h=f Σ_i y_i/{f'(x_i)(x-x_i)}は
		 * h(x_i)=y_iを満たすn-1次式
		 */
		if(x.length!=y.length)throw new AssertionError();
		int n=x.length;
		long[][]a=new long[n][2];
		for (int i = 0; i < n; i++) {
			a[i]=new long[] {(mod-x[i])%mod, 1};
		}
		long[]f=PolynomialFp.mulAll(a);
		long[]df=PolynomialFp.differentiate(f);
		long[]evals=PolynomialFp.multipointEval(df, x);
		long[][][]b=new long[n][2][2];
		for (int i = 0; i < b.length; i++) {
			b[i][0]=new long[] {y[i] * fp.inv(evals[i])%mod};
			b[i][1]=new long[] {(mod-x[i])%mod, 1};
		}
		long[][]q=PolynomialFp.sumRationals(b);
		long[]h=PolynomialFp.div(PolynomialFp.mul(f, q[0]), q[1]);
		h=Arrays.copyOf(h, n);
		return h;
	}

	/**
	 * f(g) mod x^n = Σ f[i] (g[j]x^j)^i  mod x^n
	 * https://arxiv.org/pdf/2404.05177
	 * @param f
	 * @param g
	 * @param n
	 * @return
	 */
	public static long[] comp(long[] f, long[] g, int n) {
		if(g[0]!=0)throw new AssertionError();
		// Σ f[i] (g[j]x^j)^i
		// m=f.lengthとしてc[i]=f[m-1-i]と置く。
        // Σ c[m-1-i] (g[j]x^j)^i を求めたい。
        // C(x) = Σ c[i]x^i
        // B(x) = Σ g[i]x^i
        // [y^{m-1}] C(y) / (1 - y B(x))
		long[]num=resize(f);
		int m=num.length;
		ArrayUtils.reverse(num);
		int degG=deg(g);
		if(degG==-1) {//g(x)=0
			long[]ret=new long[n];
			ret[0]=f[0];
			return ret;
		}
        long[][]den=new long[degG+1][2];
        den[0][0]=1;
        for (int i = 0; i <= degG; i++) {
			den[i][1]=(mod-g[i])%mod;
        }
        long[][]x=comp(m-1, m, n, num, den);
        long[]ret=new long[x.length];
        for (int i = 0; i < x.length; i++) {
			ret[i]=x[i][0];
		}
        return ret;
	}
	
	/**
	 * Σ p[i] y^i / Σ q[i][j]x^i y^j mod x^n
	 * = Σ a[i](x) y^i mod x^n
	 * としたとき、
 	 *  Σ[i=l..r-1] a[i](x) y^{i-l} mod x^n
 	 *  を返す。
	 * @param l
	 * @param r
	 * @param n
	 * @param p
	 * @param q
	 * @return
	 */
	static long[][] comp(int l, int r, int n, long[] p, long[][] q) {
		if(n==0) {
			return new long[1][1];
		} else if(n==1) {
			long[]a=mul(p, inv(q[0]));
			long[][]ret=new long[1][r-l];
			for (int i = l; i < r; i++) {
				ret[0][i-l]=a[i];
			}
			return ret;
		} else {
			long[][] negatedQ = ArrayUtils.copy(q);
			for (int i = 0; i < negatedQ.length; i++) {
				for (int j = 0; j < negatedQ[i].length; j++) {
					if(i%2==1)negatedQ[i][j]=negatedQ[i][j]*(mod-1)%mod;
				}
			}
			int e=Math.max(0, l - (q[0].length-1));
			q = PolynomialFp2D.mul(q, negatedQ);//xの次数半分、yの次数2倍、
			long[][]v=new long[(n+1)/2][q[0].length];
			for (int i = 0; i < n; i+=2) {
				for (int j = 0; j < q[i].length; j++) {
					v[i/2][j]=q[i][j];
				}
			}
			long[][] x=comp(e, r, (n+1)/2, p, v);
			long[][]a=new long[x.length*2][x[0].length];
			for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < x[i].length; j++) {
					a[2*i][j]=x[i][j];
				}
			}
			a=PolynomialFp2D.mul(a, negatedQ);
			long[][]ret=new long[n][r-l];
			for (int i = 0; i < n && i < a.length; i++) {
				for (int j = l-e; j < r-e && j < a[i].length; j++) {
					ret[i][j-(l-e)]=a[i][j];
				}
			}
			return ret;
			/*
			 * f(x)=1+2x^2
			 * g(x)=3x+x^2
			 * f(g(x)) mod x^8 を求めたい。
			 *  [y^2] (2+y^2) / (1-y(3x+x^2))
			 * =[y^2] (2+y^2)(1-y(3x+x^2)) / {(1-y(3x+x^2))(1-y(-3x+x^2))}   mod x^8
			 * =[y^2] (2+y^2)(1-y(3x+x^2)) / (1-9x^2y^2-2yx^2+y^2x^4)   mod x^8
			 * =[y^2] {(2+y^2) / (1-9uy^2-2yu+y^2u^2) mod u^4 } (1-y(3x+x^2)) mod x^8    (u:=x^2)
			 * =[y^2] {[y^1+y^2]{(2+y^2) / (1-9uy^2-2yu+y^2u^2) mod u^4 }} (1-y(3x+x^2)) mod x^8    (u:=x^2)
			 */
		}
	}

	/**
	 * https://judge.yosupo.jp/submission/353930
	 * @param f
	 * @return
	 */
	public static long[] compInverse(long[] f) {
		if(f[0] != 0) throw new AssertionError();
		long[] b = f.clone();
		long inv = fp.inv(f[1]);
		long c = inv;
		for (int i = 1; i < f.length; i++) {
			b[i] = f[i] * c % mod;
			c = c * inv % mod;
		}
		// a=[x^1]gとする。
		// g(f(x)) = x
		// a g(f(x/a)) = x
		// より f(x/a) の逆関数 g を 1/a　倍すればよい。
		// [x^1] g = 1 とする。
		// [x^n]f(x)^i = (i/n) [x^{n-i}] (x/g(x))^n for i = 1 .. n
		// なので左辺から (x/g(x))^n mod x^n が求まり、-1/n乗すれば g(x) が求まる。
		
		// 例えば f(x)=2x のとき
		// [x^2] f^1 = 0 = (1 / 2) [x^1] (x/g(x))^2
		// [x^2] f^2 = 4 = (2 / 2) [x^0] (x/g(x))^2
		// (x/g(x))^2 = 4
		//  g(x) = x
		
		long[][] h=new long[f.length + 1][2];
		h[0][0]=1;
		for (int i = 0; i < f.length; i++) {
			if(b[i]!=0)
				h[i][1]=mod-b[i];
		}
		int n=f.length + 1;
		long[] fPow = PolynomialFp2D.fixingXofRational(new long[][] {{1}}, h, n);
		for (int i = 1; i < fPow.length; i++) {
			fPow[i] = fPow[i] * n % mod * fp.inv(i) % mod;
		}
		fPow = Arrays.copyOf(fPow, n + 1);
		ArrayUtils.reverse(fPow);
		fPow = pow(fPow, mod-fp.inv(n));
		long[] ret = new long[f.length];
		for (int i = 1; i < f.length; i++) {
			ret[i] = fPow[i - 1] * inv % mod;
		}
		return ret;
	}

    public static void ogfToEgfInplace(long[]f) {
    	//https://yukicoder.me/submissions/1153298
    	for (int i = 0; i < f.length; i++) {
			f[i]=f[i]*fp.ifac(i)%mod;
		}
    }
	
    public static void egfToOgfInplace(long[]f) {
    	//https://yukicoder.me/submissions/1153298
    	for (int i = 0; i < f.length; i++) {
			f[i]=f[i]*fp.fac(i)%mod;
		}
    }
    
    /**
	 * 単項式基底（monomial basis）で与えられた多項式を
	 * Newton基底（Newton basis）に変換する。
	 *
	 * <p>入力として
	 *
	 * <pre>{@code
	 * f(x) = a[0] + a[1] x + ... + a[n-1] x^(n-1)
	 * }</pre>
	 *
	 * と、点列 {@code p[0], p[1], ..., p[n-1]} が与えられたとき、
	 * 次を満たす係数 {@code d[0], d[1], ..., d[n-1]} を返す：
	 *
	 * <pre>{@code
	 * f(x) = d[0]
	 *      + d[1] (x - p[0])
	 *      + d[2] (x - p[0])(x - p[1])
	 *      + ...
	 *      + d[n-1] ∏_{k=0}^{n-2} (x - p[k])
	 * }</pre>
	 */
	public static long[] monomialToNewtonBasis(long[]a, long[] p) {
		//https://judge.yosupo.jp/submission/366774
		if(a.length != p.length) throw new AssertionError();
		int len=1;
		while (len < a.length) len*=2;
		long[][]mods=new long[2*len][];
		long[][]modded=new long[2*len][];
		for (int i = 0; i < len; i++) {
			if (i < a.length)
				mods[i+len]=new long[] {(mod-p[i])%mod,1};
			else
				mods[i+len]=new long[] {1};
		}
		for (int i = len - 1; i >= 1; i--) {
			mods[i] = PolynomialFp.mul(mods[2*i], mods[2*i+1]);
		}
		
		modded[1]=PolynomialFp.mod(a, mods[1]);
		for (int i = 1; 2*i < modded.length; i++) {
			var res=PolynomialFp.divmod(modded[i], mods[2*i]);
			modded[2*i]=res.r;
			modded[2*i+1]=res.q;
		}
		long[]ret=new long[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[i]=modded[len+i][0];
		}
		return ret;
	}
	
	
	
	
	
	/**
	 * Newton基底（Newton basis）で表された係数列を、
	 * monomial basis に戻す。
	 *
	 * <p>入力 {@code a[0], a[1], ..., a[n-1]} は
	 *
	 * <pre>{@code
	 * f(x) = a[0]
	 *      + a[1] (x - p[0])
	 *      + a[2] (x - p[0])(x - p[1])
	 *      + ...
	 *      + a[n-1] ∏_{k=0}^{n-2} (x - p[k])
	 * }</pre>
	 *
	 * を表すものとする。これを monomial basis に変換する。
	 */
	public static long[] newtonToMonomialBasis(long[]a, long[] p) {
		if(a.length != p.length) throw new AssertionError();
		int len = 1;
		while (len < a.length) len *= 2;
		long[][] mods = new long[2 * len][];
		long[][] built = new long[2 * len][];
		for (int i = 0; i < len; i++) {
			if (i < a.length) {
				// 葉 i は (x - p[i]) と Newton 係数 a[i] を持つ。
				mods[i + len] = new long[] {(mod - p[i]) % mod, 1};
				built[i + len] = new long[] {a[i]};
			} else {
				mods[i + len] = new long[] {1};
				built[i + len] = new long[] {0};
			}
		}
		for (int i = len - 1; i >= 1; i--) {
			// mods[i] = 左区間に対応する積 ∏(x - p[k])。
			if(i != 1) mods[i] = PolynomialFp.mul(mods[2 * i], mods[2 * i + 1]);
			// 右部分木は左区間の積を掛けてから足し込む。
			built[i] = PolynomialFp.add(built[2 * i], PolynomialFp.mul(mods[2 * i], built[2 * i + 1]));
		}
		return resize(built[1], a.length);
	}

	
    
    
    
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
