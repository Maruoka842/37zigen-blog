package library.util.polynomial;

import java.util.Arrays;

import library.util.Ints;
import library.util.MathUtils;
import library.util.Fp;

/**
 * Fp[X_1, X_2, ...] / (1 - X_1^2, 1 - X_2^2, ...) 上の多項式（対合多項式）の演算を行うクラス。
 * ビットごとのXOR演算に対応する畳み込み（Walsh-Hadamard変換を用いた高速化など）を提供します。
 */
public class InvolutivePolynomialFp {
	/** 法（modulo）とする値 */
	public final long mod;
	/** 有限体 F_p 上の基本演算をサポートするオブジェクト */
	private final Fp mo;

	/** ナイーブな畳み込みを行う配列長の閾値 */
	public static final int MUL_NAIVE_THRESHOLD = 16;

	/** 998244353 を法とする標準的な対合多項式演算インスタンス */
	public static final InvolutivePolynomialFp MOD998244353 = new InvolutivePolynomialFp(998244353L);

	/**
	 * 指定した法 mod を持つ対合多項式演算器を構築します。
	 *
	 * @param mod 法（素数）
	 */
	public InvolutivePolynomialFp(long mod) {
		this.mod = mod;
		this.mo = new Fp(mod);
	}

	/**
	 * 高速ウォルシュ・アダマール逆変換（Inverse Fast Walsh-Hadamard Transform, IFT）を行います。
	 * 2^nでの除算は行わず、各要素の加減算のみを行います。
	 *
	 * @param a 対象とする配列。長さは2の冪乗でなければなりません。
	 */
	// 未テスト
	public void ifft(long[] a) {
		fft(a);
	}
	
	/**
	 * 高速ウォルシュ・アダマール変換（Fast Walsh-Hadamard Transform, FFT）を行います。
	 * 2^nでの除算は行いません。
	 *
	 * 計算量: O(N log N) (N = a.length)
	 *
	 * @param a 対象とする配列。長さは2の冪乗でなければなりません。
	 */
	// 未テスト
	public void fft(long[] a) {
		if(Integer.bitCount(a.length)!=1)throw new AssertionError();
		if(a.length<=1)throw new AssertionError();
		int n=MathUtils.floorLog2(a.length);

		for (int i = 0; i < n; i++) {
			for (int s = 0; s < 1<<n; s++) {
				if(Ints.bitAt(s, i)==0) {
					long u=a[s];
					long v=a[s|(1<<i)];
					a[s]=(u+v)%mod;
					a[s|(1<<i)]=(u+mod-v)%mod;
				}
			}
		}
	}
	
	/**
	 * 2つの配列のビット XOR 畳み込みをナイーブ（直接）計算します。
	 * 長さが異なる、または2の冪乗でない場合にも対応し、配列外の値は0と仮定して計算します。
	 *
	 * @param a 配列a
	 * @param b 配列b
	 * @return 畳み込み結果の配列
	 */
	// 未テスト
	public long[] mulNaive(long[] a, long[] b) {
		int len = Math.max(a.length, b.length);
		if (len == 0) return new long[0];
		int p = 1;
		while (p < len) {
			p <<= 1;
		}
		long[] c = new long[p];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				c[i ^ j] = (c[i ^ j] + a[i] * b[j]) % mod;
				if (c[i ^ j] < 0) c[i ^ j] += mod;
			}
		}
		return c;
	}
	
	/**
	 * 2つの配列のビット XOR 畳み込みを、配列の長さに応じてナイーブ法またはウォルシュ・アダマール変換を用いて計算します。
	 * 長さが異なる、または2の冪乗でない場合にも対応し、配列外の値は0と仮定して計算します。
	 *
	 * @param a 配列a
	 * @param b 配列b
	 * @return 畳み込み結果の配列
	 */
	// 未テスト
	public long[] mul(long[] a, long[] b) {
		int len = Math.max(a.length, b.length);
		if (len == 0) return new long[0];
		int p = 1;
		while (p < len) {
			p <<= 1;
		}
		if (p <= MUL_NAIVE_THRESHOLD || mod % 2 == 0) {
			return mulNaive(a, b);
		} else {
			return mulFFT(a, b);
		}
	}
	
	/**
	 * 内部のウォルシュ・アダマール変換を用いた畳み込みの実体です。
	 * 長さが異なる、または2の冪乗でない場合にも対応し、配列外の値は0と仮定して計算します。
	 *
	 * @param a 配列a
	 * @param b 配列b
	 * @return 畳み込み結果
	 */
	// 未テスト
	long[] mulFFT(long[] a, long[] b) {
		int len = Math.max(a.length, b.length);
		if (len == 0) return new long[0];
		int p = 1;
		while (p < len) {
			p <<= 1;
		}
		if (p <= 1) {
			long[] c = new long[1];
			if (a.length > 0 && b.length > 0) {
				c[0] = (a[0] * b[0]) % mod;
				if (c[0] < 0) c[0] += mod;
			}
			return c;
		}
		long[] A = Arrays.copyOf(a, p);
		long[] B = Arrays.copyOf(b, p);
		fft(A);
		fft(B);
		for (int i = 0; i < A.length; i++) {
			A[i] = A[i] * B[i] % mod;
		}
		ifft(A);
		long inv = mo.inv(A.length);
		for (int i = 0; i < A.length; i++) {
			A[i] = A[i] * inv % mod;
			if (A[i] < 0) A[i] += mod;
		}
		return A;
	}
	
	/**
	 * XOR 畳み込みの逆元を計算します。
	 * y * a = e (e = [1, 0, 0, ...]) となる y を返します。
	 *
	 * 計算量: O(N log N) (N = a.length)
	 *
	 * @param a 対象とする配列
	 * @return 逆元配列
	 */
	// 未テスト
	public long[] inverse(long[] a) {
		long[]A=a.clone();
		fft(A);
		for (int i = 0; i < A.length; i++) {
			if(A[i]==0)throw new AssertionError("inverseが存在しない");
			A[i]=mo.inv(A[i]);
		}
		ifft(A);
		long inv=mo.inv(A.length);
		for (int i = 0; i < A.length; i++) {
			A[i]=A[i]*inv%mod;
		}
		return A;
	}

	/**
	 * 等比級数の和 A^0 + A^1 + ... + A^(n-1) を計算します。
	 *
	 * 計算量: O(N log N) (N = a.length)
	 *
	 * @param a 公比
	 * @param n 項数
	 * @return 等比級数の和
	 */
	// 未テスト
	public long[] geometricSeries(long[]a, int n) {
		long[]A=a.clone();
		fft(A);
		for (int i = 0; i < A.length; i++) {
			A[i]=mo.geometricSum(A[i], n);
		}
		ifft(A);
		long inv=mo.inv(A.length);
		for (int i = 0; i < A.length; i++) {
			A[i]=A[i]*inv%mod;
		}
		return A;
	}
	
	/**
	 * 2つの多項式の和を計算します。
	 *
	 * 計算量: O(N) (N = a.length)
	 *
	 * @param a 配列a
	 * @param b 配列b
	 * @return 和配列
	 */
	// 未テスト
	public long[] add(long[] a, long[] b) {
		if(a.length!=b.length)throw new AssertionError();
		long[] c=new long[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i]=(a[i]+b[i])%mod;
		}
		return c;
	}
	
	/**
	 * 各要素 v について (1 + x^v) の総乗 Π_{i} (1 + x^a[i]) を計算します。
	 *
	 * 計算量: O(N log N) (N = 1 << 20)
	 *
	 * @param a 入力値の集合
	 * @return 各XOR和に対応する部分集合の個数をカウントした配列
	 * @see https://atcoder.jp/contests/abc367/tasks/abc367_g
	 */
	// 未テスト
	public long[] subsetXorSum(int[] a) {
		long[]f=new long[1<<20];
		for (int v:a)f[v]++;
		fft(f);
		long val = mo.pow(2, a.length);
		for (int i = 0; i < f.length; i++) {
			long diff = (f[i] - a.length) % mod;
			if (diff < 0) diff += mod;
			if (diff == 0) {
				f[i] = val;
			} else {
				f[i] = 0;
			}
		}
		ifft(f);
		long inv = mo.inv(f.length);
		for (int i = 0; i < f.length; i++) {
			f[i] = f[i] * inv % mod;
		}
		return f;
	}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
