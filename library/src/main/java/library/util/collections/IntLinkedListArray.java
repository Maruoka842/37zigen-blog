package library.util.collections;

import java.util.PrimitiveIterator;

/**
 * ノード番号 {@code 0..n-1} を対象にした固定サイズの連結リスト。
 * <p>
 * 各番号をノードとして扱い、前後のリンクを配列で管理する。 {@code prev} が {@code -1} のノードを先頭、{@code next} が
 * {@code -1} のノードを末尾とみなす。
 */
public class IntLinkedListArray {

	private int[] prev;
	private int[] next;

	/**
	 * {@code n} 個の孤立したノードを作る。
	 *
	 * @param n ノード数
	 */
	public IntLinkedListArray(int n) {
		prev = new int[n];
		next = new int[n];
		for (int i = 0; i < n; i++) {
			prev[i] = -1;
			next[i] = -1;
		}
	}

	/**
	 * {@code target} の直後に {@code newNode} を挿入する。
	 *
	 * @param target  すでにリストに含まれているノード
	 * @param insertNode 挿入するノード。前後が未接続である必要がある
	 * @throws AssertionError {@code newNode} にすでに前後のノードがある場合
	 */
	public void insertSingleAfter(int target, int insertNode) {
		// https://atcoder.jp/contests/abc237/submissions/75279173
		if (target == insertNode) throw new AssertionError();
		if (prev[insertNode] != -1 || next[insertNode] != -1)
			throw new AssertionError();
		int b = next[target];
		next[target] = insertNode;
		prev[insertNode] = target;
		next[insertNode] = b;
		if (b != -1)
			prev[b] = insertNode;
	}

	/**
	 * {@code target} の直前に {@code newNode} を挿入する。
	 *
	 * @param target  すでにリストに含まれているノード
	 * @param insertNode 挿入するノード。前後が未接続である必要がある
	 * @throws AssertionError {@code newNode} にすでに前後のノードがある場合
	 */
	public void insertSingleBefore(int target, int insertNode) {
		// https://atcoder.jp/contests/abc237/submissions/75279173
		if (next[insertNode] != -1 || prev[insertNode] != -1)
			throw new AssertionError();
		int a = prev[target];
		if (a != -1)
			next[a] = insertNode;
		prev[insertNode] = a;
		next[insertNode] = target;
		prev[target] = insertNode;
	}

	public void addEdge(int src, int dst) {
		// https://atcoder.jp/contests/abc455/submissions/75279143
		if (next[src] != -1 || prev[dst] != -1)
			throw new AssertionError();
		if (src == dst) throw new AssertionError();
		next[src] = dst;
		prev[dst] = src;
	}

	/**
	 * {@code x} を含むリストの先頭ノードを返す。
	 *
	 * @param x ノード番号
	 * @return 前方向のリンクをたどって到達する先頭ノード
	 */
	public int getHead(int x) {
		// https://atcoder.jp/contests/abc237/submissions/74232561
		while (prev[x] != -1)
			x = prev[x];
		return x;
	}

	/**
	 * {@code x} が所属するリストの先頭かを返す。
	 *
	 * @param x ノード番号
	 * @return {@code x} に前のノードがなければ {@code true}
	 */
	public boolean isHead(int x) {
		return prev[x] == -1;
	}

	/**
	 * {@code x} が所属するリストの末尾かを返す。
	 *
	 * @param x ノード番号
	 * @return {@code x} に次のノードがなければ {@code true}
	 */
	public boolean isTail(int x) {
		return next[x] == -1;
	}

	/**
	 * {@code start} から末尾まで、次方向のリンクをたどって列挙する。
	 *
	 * @param start 開始ノード番号。{@code -1} の場合は空の列挙になる
	 * @return 次方向のリンクをたどるノード番号の {@link Iterable}
	 */
	public Iterable<Integer> iterateFrom(int start) {
		// https://atcoder.jp/contests/abc237/submissions/74232561
		return () -> new PrimitiveIterator.OfInt() {
			int cur = start;

			@Override
			public boolean hasNext() {
				return cur != -1;
			}

			@Override
			public int nextInt() {
				if (cur == -1)
					throw new java.util.NoSuchElementException();
				int ret = cur;
				cur = next[cur];
				return ret;
			}
		};
	}

	/**
	 * {@code x} の直前でリストを分割する。
	 * <p>
	 * 分割後、{@code x} は後半リストの先頭になる。 {@code x} がすでに先頭なら何もしない。
	 *
	 * @param x 後半リストの先頭にしたいノード
	 * @return 分割前に {@code x} の直前にあったノード。 {@code x} が先頭だった場合は {@code -1}
	 */
	public int splitBefore(int x) {
		// https://atcoder.jp/contests/abc455/submissions/75279143
		int a = prev[x];
		if (a == -1)
			return -1;
		next[a] = -1;
		prev[x] = -1;
		return a;
	}

	/**
	 * {@code x} の直後でリストを分割する。
	 * <p>
	 * 分割後、{@code next[x]} だったノードは後半リストの先頭になる。 {@code x} がすでに末尾なら何もしない。
	 *
	 * @param x 前半リストの末尾にしたいノード
	 * @return 分割前に {@code x} の直後にあったノード。 {@code x} が末尾だった場合は {@code -1}
	 */
	public int splitAfter(int x) {
		int b = next[x];
		if (b == -1)
			return -1;

		next[x] = -1;
		prev[b] = -1;
		return b;
	}

	/**
	 * ノード {@code x} をリストから取り除き、その前後のノードを互いに接続する。
	 *
	 * @param x 取り除くノード番号
	 */
	public void spliceOut(int x) {
		//https://atcoder.jp/contests/abc421/submissions/76771605
		int a = prev[x];
		int b = next[x];
		if (a != -1)
			next[a] = b;
		if (b != -1)
			prev[b] = a;
		prev[x] = -1;
		next[x] = -1;
	}

	/**
	 * {@code x} から {@code y} への符号付き距離を返す。
	 * <p>
	 * {@code x} から次方向にたどって {@code y} に到達できる場合はその距離（正）を、
	 * {@code y} から次方向にたどって {@code x} に到達できる場合はその距離のマイナスを返す。
	 * 同一のリスト上にない場合は {@link Integer#MAX_VALUE} を返す。
	 *
	 * @param x 開始ノード
	 * @param y 目標ノード
	 * @return {@code x} から {@code y} への符号付き距離。到達不能なら {@link Integer#MAX_VALUE}
	 * @complexity O(dist(x, y))
	 */
	public int signedDist(int x, int y) {
		//https://atcoder.jp/contests/abc421/submissions/76772498
		if (x == y)
			return 0;
		int curX = x;
		int curY = y;
		int d = 0;
		while (curX != -1 || curY != -1) {
			d++;
			if (curX != -1)
				curX = next[curX];
			if (curY != -1)
				curY = next[curY];
			if (curX == y)
				return d;
			if (curY == x)
				return -d;
		}
		return Integer.MAX_VALUE;
	}

	public int prev(int x) {
		return prev[x];
	}

	public int next(int x) {
		return next[x];
	}

	public void checkValidity() {
		int n = next.length;
		if (prev.length != n)
			throw new AssertionError();

		for (int i = 0; i < n; i++) {
			if (!(-1 <= next[i] && next[i] < n))
				throw new AssertionError();
			if (!(-1 <= prev[i] && prev[i] < n))
				throw new AssertionError();
			if (next[i] != -1) {
				if (prev[next[i]] != i)
					throw new AssertionError();
			}
			if (prev[i] != -1) {
				if (next[prev[i]] != i)
					throw new AssertionError();
			}
		}
		boolean[] vis = new boolean[n];
		for (int i = 0; i < n; i++) {
			if (vis[i])
				continue;
			if (prev[i] == -1) {
				int cur = i;
				vis[cur] = true;
				while (next[cur] != -1) {
					cur = next[cur];
					vis[cur] = true;
				}
			}
		}
		for (int i = 0; i < n; i++) {
			if (!vis[i])
				throw new AssertionError();// 円環がある
		}
	}

	/**
	 * 各先頭ノードから始まる全リストを文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 各先頭ノードから始まる全リストの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		checkValidity();
		StringBuilder sb = new StringBuilder();
		sb.append("====list dump====");
		for (int i = 0; i < next.length; i++) {
			if (prev[i] == -1) {
				sb.append("\n");
				int x = i;
				while (true) {
					sb.append(x).append(" ");
					x = next[x];
					if (x == -1)
						break;
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 各先頭ノードから始まる全リストを出力。
	 */
	public void dump() {
		System.out.println(toString());
	}
}
