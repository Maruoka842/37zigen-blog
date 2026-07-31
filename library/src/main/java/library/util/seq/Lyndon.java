package library.util.seq;

import java.util.ArrayList;
import java.util.List;

/**
 * Lyndon 語に関するユーティリティクラス。
 */
public class Lyndon {
	/**
	 * 各接尾辞 s[i:n] について、最長の Lyndon 接頭辞の長さを求める。
	 * (本実装の計算量は O(n * (lcp query の計算量)))
	 * @param s
	 * @param lcp
	 * @return
	 */
	public static int[] longestLyndonPrefixes(char[] s, SuffixArrayLCP lcp) {
		int[] a = new int[s.length];
		for (int i = 0; i < s.length; i++) a[i] = s[i];
		return longestLyndonPrefixes(a, lcp);
	}

	/**
	 * 各接尾辞 s[i:n] について、最長の Lyndon 接頭辞の長さを求める。
	 * (本実装の計算量は O(n * (lcp query の計算量)))
	 * @param s
	 * @param lcp
	 * @return
	 */
	public static int[] longestLyndonPrefixes(int[] s, SuffixArrayLCP lcp) {
		int n = s.length;
		int[] stIv = new int[n + 1];
		int[] stJv = new int[n + 1];
		int ptr = 0;
		stIv[ptr] = n;
		stJv[ptr] = n;
		ptr++;
		int[] ret = new int[n];
		for (int i = n - 1; i >= 0; i--) {
			int j = i;
			while (ptr > 1) {
				int iv = stIv[ptr - 1];
				int jv = stJv[ptr - 1];
				int l = lcp.lcp(i, iv);
				if (!(iv + l < n && s[i + l] < s[iv + l])) break;
				j = jv;
				ptr--;
			}
			stIv[ptr] = i;
			stJv[ptr] = j;
			ptr++;
			ret[i] = j - i + 1;
		}
		return ret;
	}

	/**
	 * アルファベットサイズ k, 長さ n 以下の Lyndon 語を辞書順で列挙する。
	 * @param k
	 * @param n
	 * @return
	 */
	public static List<int[]> enumerateLyndonWords(int k, int n) {
		List<int[]> ret = new ArrayList<>();
		int[] aux = new int[n + 1];
		enumerateLyndonWordsRecursive(0, 1, k, n, aux, ret);
		return ret;
	}

	/**
	 * Lyndon 語を再帰的に生成する。
	 * @param t 現在の長さ
	 * @param p 現在の最小周期の長さ
	 * @param k アルファベットサイズ
	 * @param n 最大長
	 * @param aux 作業用配列
	 * @param ret 結果を格納するリスト
	 */
	private static void enumerateLyndonWordsRecursive(int t, int p, int k, int n, int[] aux, List<int[]> ret) {
		if (t == n) {
			int[] word = new int[p];
			System.arraycopy(aux, 1, word, 0, p);
			ret.add(word);
		} else {
			t++;
			aux[t] = aux[t - p];
			enumerateLyndonWordsRecursive(t, p, k, n, aux, ret);
			while (++aux[t] < k) {
				enumerateLyndonWordsRecursive(t, t, k, n, aux, ret);
			}
		}
	}
}
