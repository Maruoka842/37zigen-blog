package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import library.util.algebra.strategy.GroupStrategy;
import library.util.unionfind.UnionFind;

/**
 * 非ゼロの元（群ラベル）を持つ最短パスを求める。
 *
 * [1] Y. Iwata and Y. Yamaguchi, "Finding a Shortest Non-zero Path in Group-Labeled Graphs,"
 *     https://arxiv.org/abs/1906.04062
 *
 * 計算量: O(M log M)
 *
 * SSSP は Single-Source Shortest Path (単一始点最短経路) の略。
 *
 * @param <G> 群の元の型
 */
public class ShortestNonidentityPath<G> {
	public record LabeledEdge<G>(int to, long len, G g) {}

	private final int V;
	private final ArrayList<LabeledEdge<G>>[] adj;
	private final GroupStrategy<G> group;

	public static final long INF = Long.MAX_VALUE / 3;

	// SSSPの結果
	public long[] dist_sp;
	public int[] parent_sp;
	public int[] depth_sp;
	public Object[] psi;

	// 結果
	public int s; // ソース頂点
	/**
	 * dist[i] は s から i までの最短の「非ゼロパス」の距離。
	 * 存在しない場合は INF。
	 */
	public long[] dist;

	@SuppressWarnings("unchecked")
	public ShortestNonidentityPath(int n, GroupStrategy<G> group) {
		this.V = n;
		this.group = group;
		this.adj = new ArrayList[V];
		for (int i = 0; i < V; i++) {
			adj[i] = new ArrayList<>();
		}
	}

	public void addBiEdge(int u, int v, long len, G g) {
		adj[u].add(new LabeledEdge<>(v, len, g));
		adj[v].add(new LabeledEdge<>(u, len, group.inverse(g)));
	}

	@SuppressWarnings("unchecked")
	public void solve(int s) {
		this.s = s;
		dist_sp = new long[V];
		Arrays.fill(dist_sp, INF);
		parent_sp = new int[V];
		Arrays.fill(parent_sp, -1);
		depth_sp = new int[V];
		Arrays.fill(depth_sp, -1);
		psi = new Object[V];
		G identity = group.identity();
		for (int i = 0; i < V; i++) psi[i] = identity;

		PriorityQueue<long[]> que = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
		dist_sp[s] = 0;
		depth_sp[s] = 0;
		psi[s] = identity;
		que.add(new long[]{0, s});

		while (!que.isEmpty()) {
			long[] state = que.poll();
			long d = state[0];
			int u = (int) state[1];
			if (dist_sp[u] != d) continue;

			for (LabeledEdge<G> e : adj[u]) {
				int v = e.to;
				long d2 = d + e.len;
				if (dist_sp[v] > d2) {
					dist_sp[v] = d2;
					depth_sp[v] = depth_sp[u] + 1;
					parent_sp[v] = u;
					psi[v] = group.mul((G) psi[u], e.g);
					que.add(new long[]{d2, v});
				}
			}
		}

		UnionFind uf = new UnionFind(V);
		dist = new long[V];
		Arrays.fill(dist, INF);

		// state: {h, u0, index_in_adj_u0}
		PriorityQueue<long[]> queNonzero = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
		for (int u = 0; u < V; u++) {
			if (dist_sp[u] == INF) continue;
			for (int i = 0; i < adj[u].size(); i++) {
				LabeledEdge<G> e = adj[u].get(i);
				int v = e.to;
				if (dist_sp[v] == INF) continue;
				if (u <= v && !group.equals(group.mul((G) psi[u], e.g), (G) psi[v])) {
					queNonzero.add(new long[]{dist_sp[u] + dist_sp[v] + e.len, u, i});
				}
			}
		}

		while (!queNonzero.isEmpty()) {
			long[] state = queNonzero.poll();
			long h = state[0];
			int u0 = (int) state[1];
			int idx = (int) state[2];
			int v0 = adj[u0].get(idx).to;

			int u = uf.root(u0), v = uf.root(v0);
			if (u == v) {
				updateNode(u, h, queNonzero);
				continue;
			}

			ArrayList<Integer> bs = new ArrayList<>();
			int currU = u, currV = v;
			while (currU != currV) {
				if (depth_sp[currU] > depth_sp[currV]) {
					bs.add(currU);
					currU = uf.root(parent_sp[currU]);
				} else {
					bs.add(currV);
					currV = uf.root(parent_sp[currV]);
				}
			}
			int lca = currU;
			for (int x : bs) {
				uf.union(x, lca);
				updateNode(x, h, queNonzero);
			}
			updateNode(lca, h, queNonzero);
		}

		for (int i = 0; i < V; i++) {
			if (dist_sp[i] != INF && !group.equals((G) psi[i], identity) && dist_sp[i] < dist[i]) {
				dist[i] = dist_sp[i];
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateNode(int x, long h, PriorityQueue<long[]> que) {
		if (h - dist_sp[x] < dist[x]) {
			dist[x] = h - dist_sp[x];
			for (int j = 0; j < adj[x].size(); j++) {
				LabeledEdge<G> e = adj[x].get(j);
				if (dist_sp[e.to] != INF && group.equals(group.mul((G) psi[x], e.g), (G) psi[e.to])) {
					que.add(new long[]{dist[x] + dist_sp[e.to] + e.len, x, j});
				}
			}
		}
	}
}