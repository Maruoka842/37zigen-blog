package library.util.seq;

import library.util.fold.SparseTableInt;

/**
 * Suffix Array と Sparse Table を用いて、最長共通接頭辞 (LCP: Longest Common Prefix) クエリを処理するクラス。
 * <p>
 * 構築に $O(N)$ または $O(N \log N)$、クエリに $O(1)$ かかる。
 * </p>
 */
public class SuffixArrayLCP {
	private final int n;
	private final int[] rank;
	private final SparseTableInt st;
	private final int[] sa;
	private final int[] lcpArray;

	/**
	 * @param s
	 */
	public SuffixArrayLCP(char[] s) {
		this(s, s.length == 0 ? new int[0] : StringUtils.suffixArray(s));
	}

	public SuffixArrayLCP(char[] s, int[] sa) {
		this.n = s.length;
		this.rank = Permutation.inverse(sa);
		this.sa = sa;
		this.lcpArray = n == 0 ? new int[0] : StringUtils.lcpArray(s, sa);
		this.st = n <= 1 ? null : new SparseTableInt(lcpArray, Math::min);
	}

	public SuffixArrayLCP(int[] s) {
		this(s, s.length == 0 ? new int[0] : StringUtils.suffixArray(s));
	}

	public SuffixArrayLCP(int[] s, int[] sa) {
		this.n = s.length;
		this.rank = Permutation.inverse(sa);
		this.sa = sa;
		this.lcpArray = n == 0 ? new int[0] : StringUtils.lcpArray(s, sa);
		this.st = n <= 1 ? null : new SparseTableInt(lcpArray, Math::min);
	}

	/**
	 * s[i:] と s[j:] の最長共通接頭辞の長さを返す。
	 * 計算量: $O(1)$
	 * @param i
	 * @param j
	 * @return
	 */
	public int lcp(int i, int j) {
		if (i < 0 || i > n || j < 0 || j > n) return 0;
		if (i == n || j == n) return 0;
		if (i == j) return n - i;
		int l = rank[i];
		int r = rank[j];
		if (l > r) {
			int tmp = l;
			l = r;
			r = tmp;
		}
		return st.fold(l, r);
	}

	/**
	 * 異なる部分文字列の個数を返す。
	 * 計算量: $O(N)$
	 * @return
	 */
	public long countDistinctSubstrings() {
		long count = (long) n * (n + 1) / 2;
		for (int v : lcpArray) count -= v;
		return count;
	}

	/**
	 * $s[l_1:r_1]$ と $s[l_2:r_2]$ が等しいか判定する。
	 * 計算量: $O(1)$
	 * @param l1
	 * @param r1
	 * @param l2
	 * @param r2
	 * @return
	 */
	public boolean equals(int l1, int r1, int l2, int r2) {
		if (r1 - l1 != r2 - l2) return false;
		return lcp(l1, l2) >= (r1 - l1);
	}

	/**
	 * $s[l_1:r_1]$ と $s[l_2:r_2]$ を辞書順比較する。
	 * 計算量: $O(1)$
	 * @param l1
	 * @param r1
	 * @param l2
	 * @param r2
	 * @return
	 */
	public int compare(int l1, int r1, int l2, int r2) {
		int len1 = r1 - l1;
		int len2 = r2 - l2;
		int common = lcp(l1, l2);
		if (common >= Math.min(len1, len2)) {
			return Integer.compare(len1, len2);
		}
		// If lcp < length, l1 and l2 are not n, so rank access is safe.
		return Integer.compare(rank[l1], rank[l2]);
	}

	/**
	 * 接尾辞 $s[i:]$ と $s[j:]$ を辞書順比較する。
	 * 計算量: $O(1)$
	 * @param i
	 * @param j
	 * @return
	 */
	public int compareSuffix(int i, int j) {
		if (i == j) return 0;
		if (i == n) return -1;
		if (j == n) return 1;
		return Integer.compare(rank[i], rank[j]);
	}

	/**
	 * SA 上の rank a, b にある接尾辞同士の LCP を返す。
	 * 計算量: $O(1)$
	 * @param a
	 * @param b
	 * @return
	 */
	public int lcpByRank(int a, int b) {
		if (a < 0 || a >= n || b < 0 || b >= n) return 0;
		if (a == b) return n - sa[a];
		int l = a, r = b;
		if (l > r) {
			int tmp = l;
			l = r;
			r = tmp;
		}
		return st.fold(l, r);
	}

	/**
	 * 部分文字列 s[l:r] が prefix として現れる suffix の SA rank 区間 [L, R) を返す。
	 * 計算量: $O(\log N)$
	 * @param l
	 * @param r
	 * @return
	 */
	public int[] occurrenceRange(int l, int r) {
		int len = r - l;
		if (len <= 0) return new int[]{0, n};
		if (l < 0 || r > n) return new int[]{0, 0};
		int pos = rank[l];
		int lower;
		{
			int ok = pos, ng = -1;
			while (ok - ng > 1) {
				int mid = (ok + ng) / 2;
				if (lcpByRank(mid, pos) >= len) ok = mid;
				else ng = mid;
			}
			lower = ok;
		}
		int upper;
		{
			int ok = pos, ng = n;
			while (ng - ok > 1) {
				int mid = (ok + ng) / 2;
				if (lcpByRank(pos, mid) >= len) ok = mid;
				else ng = mid;
			}
			upper = ng;
		}
		return new int[]{lower, upper};
	}

	/**
	 * SA rank k にある接尾辞の開始位置を返す。
	 * 計算量: $O(1)$
	 * @param k
	 * @return
	 */
	public int suffixAtRank(int k) {
		return sa[k];
	}

	/**
	 * 接尾辞 s[i:] の SA rank を返す。
	 * 計算量: $O(1)$
	 * @param i
	 * @return
	 */
	public int rankOfSuffix(int i) {
		return rank[i];
	}

	/**
	 * Suffix Array を返す。
	 * 計算量: $O(1)$
	 * @return
	 */
	public int[] suffixArray() {
		return sa;
	}

	/**
	 * Suffix Array と LCP の情報を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return Suffix Array と LCP の情報を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("SuffixArrayLCP n=").append(n).append("\n");
		for (int i = 0; i < n; i++) {
			int suffixStart = sa[i];
			int r = rank[i];
			String lcpVal = (i < lcpArray.length) ? String.valueOf(lcpArray[i]) : "-";
			res.append(String.format("Rank %d: sa=%d rank_of_suffix_%d=%d lcp_with_next=%s",
				i, suffixStart, i, r, lcpVal));
			if (i < n - 1) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	/**
	 * Suffix Array と LCP の情報を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}
