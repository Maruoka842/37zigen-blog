package library.util.seq;

import java.util.Arrays;

import library.util.polynomial.PolynomialFpDynamic;

/**
 * ワイルドカードを含む文字列のマッチングを行うクラス。
 */
public class WildcardMatcher {

	/**
	 * テキスト text に対するパターン pattern のマッチング位置を計算する。
	 * text, pattern に含まれる wildcard 文字は任意の文字にマッチする。
	 * 二つの法（998244353 と 469762049）で計算し、両方で 0 になった場合のみマッチとする。
	 * 未テスト。計算量 O((N+M) log (N+M))。
	 * 
	 * @param text テキスト
	 * @param pattern パターン
	 * @param wildcard ワイルドカード文字
	 * @return matches[i] が true なら text.substring(i, i + pattern.length) が pattern とマッチする
	 */
	public static boolean[] match(char[] text, char[] pattern, char wildcard) {
		//https://judge.yosupo.jp/submission/372038
		int n = text.length;
		int m = pattern.length;
		if (n < m) return new boolean[0];

		long[] s = new long[n];
		long[] t = new long[m];
		// char は unsigned 16bit で 65536 = 2^16
		for (int i = 0; i < n; i++) {
			s[i] = (text[i] == wildcard) ? 0 : (text[i] == 0 ? 65536 : text[i]);
		}
		for (int i = 0; i < m; i++) {
			t[i] = (pattern[i] == wildcard) ? 0 : (pattern[i] == 0 ? 65536 : pattern[i]);
		}

		boolean[] matches = new boolean[n - m + 1];
		Arrays.fill(matches, true);
		for (int k = 0; k < 2; k++) {
			PolynomialFpDynamic poly = (k == 0) ? PolynomialFpDynamic.MOD998244353 : PolynomialFpDynamic.MOD469762049;
			long mod = poly.mod;

			long[] s2 = new long[n], s3 = new long[n];
			long[] t2 = new long[m], t3 = new long[m];
			for (int i = 0; i < n; i++) {
				s2[i] = s[i] % mod * (s[i] % mod) % mod;
				s3[i] = s2[i] * (s[i] % mod) % mod;
			}
			for (int i = 0; i < m; i++) {
				t2[i] = t[i] % mod * (t[i] % mod) % mod;
				t3[i] = t2[i] * (t[i] % mod) % mod;
			}

			// sum_j S_{i+j} T_j (S_{i+j} - T_j)^2 = sum_j S_{i+j}^3 T_j - 2 sum_j S_{i+j}^2 T_j^2 + sum_j S_{i+j} T_j^3
			long[] term1 = poly.validShiftedDotProducts(t, s3);
			long[] term2 = poly.validShiftedDotProducts(t2, s2);
			long[] term3 = poly.validShiftedDotProducts(t3, s);

			for (int i = 0; i < n - m + 1; i++) {
				long val = (term1[i] - 2 * term2[i] + term3[i]) % mod;
				matches[i] &= val == 0;
			}
		}
		return matches;
	}

	/**
	 * 文字列形式でのマッチング。
	 * 未テスト。計算量 O((N+M) log (N+M))。
	 */
	public static boolean[] match(String text, String pattern, char wildcard) {
		return match(text.toCharArray(), pattern.toCharArray(), wildcard);
	}
}