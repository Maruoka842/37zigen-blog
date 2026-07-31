package library.util.seq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import library.util.ArrayUtils;

/**
 * Runs 構造を抽出するクラス。
 * <p>
 * 文字列中の run (極大な周期的な部分文字列) を抽出する。
 * run とは、周期 p, 開始位置 l, 終了位置 r の 3 つ組 (p, l, r) であり、以下の条件を満たすものを指す：
 * <ul>
 *     <li>s[l:r] の最小周期が p である</li>
 *     <li>r - l >= 2p である</li>
 *     <li>s[l-1:r] および s[l:r+1] が周期 p を持たない (極大性)</li>
 * </ul>
 */
public class Runs {
	/**
	 * run (極大な周期的な部分文字列) を表す。
	 * @param period 最小周期 p
	 * @param l 開始位置 (inclusive)
	 * @param r 終了位置 (exclusive)
	 */
	public record Run(int period, int l, int r) {}

	/**
	 * 文字列 s に含まれるすべての Runs を列挙する。
	 * @param s
	 * @return
	 */
	public static List<Run> enumerateRuns(char[] s) {
		int[] a = new int[s.length];
		for (int i = 0; i < s.length; i++) a[i] = s[i];
		return enumerateRuns(a);
	}

	/**
	 * 文字列 s に含まれるすべての Runs を列挙する。
	 * @param s
	 * @return
	 */
	public static List<Run> enumerateRuns(int[] s) {
		int n = s.length;
		if (n == 0) return Collections.emptyList();

		int[] rev = s.clone();
		ArrayUtils.reverse(rev);

		int[] t = s.clone();
		int min = s[0], max = s[0];
		for (int v : s) {
			if (v < min) min = v;
			if (v > max) max = v;
		}
		for (int i = 0; i < n; i++) {
			t[i] = max - (s[i] - min);
		}

		SuffixArrayLCP lcp = new SuffixArrayLCP(s);
		SuffixArrayLCP revLcp = new SuffixArrayLCP(rev);

		int[] l1 = Lyndon.longestLyndonPrefixes(s, lcp);
		int[] l2 = Lyndon.longestLyndonPrefixes(t, lcp);

		List<Run> ret = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			{
				int j = i + l1[i];
				int L = i - revLcp.lcp(n - i, n - j);
				int R = j + lcp.lcp(i, j);
				if (R - L >= (j - i) * 2) {
					ret.add(new Run(j - i, L, R));
				}
			}
			if (l1[i] != l2[i]) {
				int j = i + l2[i];
				int L = i - revLcp.lcp(n - i, n - j);
				int R = j + lcp.lcp(i, j);
				if (R - L >= (j - i) * 2) {
					ret.add(new Run(j - i, L, R));
				}
			}
		}

		ret.sort(Comparator.comparingInt(Run::period)
				.thenComparingInt(Run::l)
				.thenComparingInt(Run::r));

		List<Run> unique = new ArrayList<>();
		if (!ret.isEmpty()) {
			unique.add(ret.get(0));
			for (int i = 1; i < ret.size(); i++) {
				if (!ret.get(i).equals(ret.get(i - 1))) {
					unique.add(ret.get(i));
				}
			}
		}
		return unique;
	}
}
