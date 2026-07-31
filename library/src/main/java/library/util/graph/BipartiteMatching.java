package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;

/**
 * 二部グラフの最大マッチングを求めるクラス。
 * Hopcroft-Karp 法を利用する。
 *
 * 計算量: O(E√V)
 * V = L + R, E = 辺数
 */
public class BipartiteMatching {
	int L, R;

	/** adj[l] = 左側頂点 l から辺がある右側頂点のリスト */
	IntArrayList[] adj;

	int[] fromLtoR;
	int[] fromRtoL;
	
	/** Hopcroft-Karp の BFS 距離。左側頂点だけに持つ。 */
	int[] dist;
	int[] it;
	
	/** 最短増加路の長さを、左側レイヤー数で持つ。実際の辺数は 2 * shortest - 1。 */
	int shortest;

	static final int INF = 1 << 28;

	/** DM 分解用に、追加された辺を保存しておく。 */
	IntArrayList edgesL = new IntArrayList();
	IntArrayList edgesR = new IntArrayList();

	boolean calculated = false;

	/**
	 * 左側頂点数 L、右側頂点数 R の二部グラフを作成する。
	 *
	 * @param L 左側の頂点数
	 * @param R 右側の頂点数
	 */
	@SuppressWarnings("unchecked")
	public BipartiteMatching(int L, int R) {
		this.L = L;
		this.R = R;

		adj = new IntArrayList[L];
		for (int i = 0; i < L; i++) {
			adj[i] = new IntArrayList();
		}

		fromLtoR = new int[L];
		fromRtoL = new int[R];
		dist = new int[L];
		it = new int[L];
		
		Arrays.fill(fromLtoR, -1);
		Arrays.fill(fromRtoL, -1);
	}

	/**
	 * 左側の頂点 from と右側の頂点 to の間に辺を追加する。
	 *
	 * @param from 左側の頂点番号 (0 ～ L-1)
	 * @param to 右側の頂点番号 (0 ～ R-1)
	 */
	public void addEdge(int from, int to) {
		if (from < 0 || from >= L) {
			throw new IndexOutOfBoundsException("left vertex out of range: " + from);
		}
		if (to < 0 || to >= R) {
			throw new IndexOutOfBoundsException("right vertex out of range: " + to);
		}

		adj[from].add(to);

		edgesL.add(from);
		edgesR.add(to);

		calculated = false;
	}

	/**
	 * 最大マッチングを計算し、マッチングのサイズを返す。
	 *
	 * 計算量: O(E√V)
	 *
	 * @return 最大マッチングのサイズ
	 */
	public int calc() {
		//https://judge.yosupo.jp/submission/381131
		Arrays.fill(fromLtoR, -1);
		Arrays.fill(fromRtoL, -1);

		int matching = 0;

		while (bfs()) {
			Arrays.fill(it, 0);
			for (int l = 0; l < L; l++) {
				if (fromLtoR[l] == -1 && dfs(l)) {
					matching++;
				}
			}
		}

		calculated = true;
		return matching;
	}

	/**
	 * 未マッチ左頂点を始点として、最短増加路用の距離を作る。
	 *
	 * dist[l] は左側頂点だけで見た距離。
	 * dist[l] = d の左頂点から未マッチ右頂点へ行けるとき、
	 * 増加路の辺数は 2d + 1。
	 *
	 * @return 増加路が存在するなら true
	 */
	private boolean bfs() {
		Arrays.fill(dist, -1);

		IntDeque que = new IntDeque();

		for (int l = 0; l < L; l++) {
			if (fromLtoR[l] == -1) {
				dist[l] = 0;
				que.addLast(l);
			}
		}

		shortest = INF;

		while (!que.isEmpty()) {
			int l = que.pollFirst();

			// これ以上深く進むと、最短増加路より長くなる。
			if (dist[l] + 1 > shortest) {
				continue;
			}

			for (int r : adj[l]) {
				int nl = fromRtoL[r];

				if (nl == -1) {
					// l -> r で未マッチ右頂点に到達。
					shortest = dist[l] + 1;
				} else if (dist[nl] == -1) {
					// l -> r は非マッチ辺、r -> nl はマッチ辺。
					dist[nl] = dist[l] + 1;
					que.addLast(nl);
				}
			}
		}

		return shortest != INF;
	}

	/**
	 * BFS で作った最短増加路レイヤーに沿って DFS し、
	 * 増加路を 1 本見つけたら反転する。
	 */
	private boolean dfs(int l) {
		for (; it[l] < adj[l].size(); it[l]++) {
			int r = adj[l].get(it[l]);
			int nl = fromRtoL[r];
			
			if (nl == -1) {
				if (dist[l] + 1 == shortest) {
					fromLtoR[l] = r;
					fromRtoL[r] = l;
					return true;
				}
			} else if (dist[nl] == dist[l] + 1 && dfs(nl)) {
				fromLtoR[l] = r;
				fromRtoL[r] = l;
				return true;
			}
		}
		return false;
	}

	/**
	 * 左側の頂点 v がどの右側の頂点とマッチングしているかを返す。
	 *
	 * @param v 左側の頂点番号
	 * @return マッチング相手の右側頂点番号。マッチングしていない場合は -1。
	 */
	public int fromLtoR(int v) {
		if (!calculated) throw new IllegalStateException("calc() has not been called.");
		return fromLtoR[v];
	}

	/**
	 * 右側の頂点 v がどの左側の頂点とマッチングしているかを返す。
	 *
	 * @param v 右側の頂点番号
	 * @return マッチング相手の左側頂点番号。マッチングしていない場合は -1。
	 */
	public int fromRtoL(int v) {
		if (!calculated) throw new IllegalStateException("calc() has not been called.");
		return fromRtoL[v];
	}

	/**
	 * DM 分解の各グループ（部分グラフ）を表すレコード。
	 *
	 * @param left 左側の頂点集合
	 * @param right 右側の頂点集合
	 */
	public record DMGroup(IntArrayList left, IntArrayList right) {
		public DMGroup() {
			this(new IntArrayList(), new IntArrayList());
		}
	}

	/**
	 * 二部グラフを強連結成分分解の一種である Dulmage-Mendelsohn (DM) 分解により、
	 * 以下の 3 種類の部分集合に分割する。
	 *
	 * 1. W0: 右側頂点の未マッチング頂点から到達可能な頂点集合。
	 * 2. W1, ..., Wk: 完全マッチングに含まれる頂点集合の強連結成分。
	 * 3. W(k+1): 左側頂点の未マッチング頂点から到達可能な頂点集合。
	 *
	 * 戻り値は [W0, W1, ..., Wk, W(k+1)] の順で、
	 * 各要素は (leftVertices, rightVertices) のペア。
	 *
	 * 計算量: O(V + E) (calc() 呼び出し済みの場合)
	 *
	 * @see <a href="https://yukicoder.me/problems/no/1615">yukicoder No.1615</a>
	 */
	public List<DMGroup> dmDecomposition() {
		if (!calculated) throw new IllegalStateException("calc() has not been called.");

		Digraph g = new Digraph(L + R);

		for (int i = 0; i < edgesL.size(); i++) {
			g.addEdge(edgesL.get(i), L + edgesR.get(i));
		}

		for (int l = 0; l < L; l++) {
			int r = fromLtoR(l);
			if (r != -1) {
				g.addEdge(L + r, l);
			}
		}

		IntArrayList[] sccComps = g.scc();
		int nscc = sccComps.length;

		int[] nodeToCmp = new int[L + R];
		for (int i = 0; i < nscc; i++) {
			for (int v : sccComps[i]) {
				nodeToCmp[v] = i;
			}
		}

		int[] cmpMap = new int[nscc];
		Arrays.fill(cmpMap, -2);

		for (int c = 0; c < 2; c++) {
			Digraph to = (c == 0) ? g : g.reverse();

			boolean[] vis = new boolean[L + R];
			IntDeque que = new IntDeque();

			for (int i = 0; i < L + R; i++) {
				boolean isUnmatched = (i < L) ? fromLtoR(i) == -1 : fromRtoL(i - L) == -1;
				boolean isTargetColor = (c == 0) ? (i < L) : (i >= L);

				if (isUnmatched && isTargetColor && !vis[i]) {
					vis[i] = true;
					que.addLast(i);

					while (!que.isEmpty()) {
						int now = que.pollFirst();

						cmpMap[nodeToCmp[now]] = c - 1;

						for (int nxt : to.adj[now]) {
							if (!vis[nxt]) {
								vis[nxt] = true;
								que.addLast(nxt);
							}
						}
					}
				}
			}
		}

		int nset = 1;

		for (int i = 0; i < nscc; i++) {
			if (cmpMap[i] == -2) {
				cmpMap[i] = nset++;
			}
		}

		for (int i = 0; i < nscc; i++) {
			if (cmpMap[i] == -1) {
				cmpMap[i] = nset;
			}
		}
		nset++;

		List<DMGroup> groups = new ArrayList<>(nset);
		for (int i = 0; i < nset; i++) {
			groups.add(new DMGroup());
		}

		for (int l = 0; l < L; l++) {
			int c = cmpMap[nodeToCmp[l]];
			groups.get(c).left().add(l);
		}

		for (int r = 0; r < R; r++) {
			int c = cmpMap[nodeToCmp[L + r]];
			groups.get(c).right().add(r);
		}

		return groups;
	}
}