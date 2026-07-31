package library.util.graph;

import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * グラフィックマトロイド（無向グラフの森）。未テスト。
 */
public class GraphicMatroid implements Matroid {
	private final int V;
	private final int M;
	private final int[] u, v;
	private final int[] head, next, to, edgeId;

	private final int[] backtrack; // この頂点に到達するために使用された辺のID
	private final int[] vprev;     // 親頂点
	private final int[] depth;
	private final int[] root;
	private final int[] que;

	/**
	 * グラフを指定してグラフィックマトロイドを構築する。
	 * @param V 頂点数
	 * @param u 辺の端点1の配列
	 * @param v 辺の端点2の配列
	 */
	public GraphicMatroid(int V, int[] u, int[] v) {
		this.V = V;
		this.M = u.length;
		this.u = u.clone();
		this.v = v.clone();
		this.head = new int[V];
		Arrays.fill(head, -1);
		this.next = new int[2 * M];
		this.to = new int[2 * M];
		this.edgeId = new int[2 * M];

		int ptr = 0;
		for (int i = 0; i < M; i++) {
			if (u[i] == v[i]) continue;
			int ui = u[i], vi = v[i];
			to[ptr] = vi;
			edgeId[ptr] = i;
			next[ptr] = head[ui];
			head[ui] = ptr++;

			to[ptr] = ui;
			edgeId[ptr] = i;
			next[ptr] = head[vi];
			head[vi] = ptr++;
		}

		this.backtrack = new int[V];
		this.vprev = new int[V];
		this.depth = new int[V];
		this.root = new int[V];
		this.que = new int[V];
	}

	@Override
	public int size() {
		return M;
	}

	@Override
	public void set(boolean[] I) {
		Arrays.fill(backtrack, -1);
		Arrays.fill(vprev, -1);
		Arrays.fill(depth, -1);
		Arrays.fill(root, -1);

		int qb = 0, qe = 0;
		for (int i = 0; i < V; i++) {
			if (depth[i] != -1) continue;
			int r = i;
			que[qe++] = i;
			depth[i] = 0;
			root[i] = r;
			while (qb < qe) {
				int now = que[qb++];
				for (int e = head[now]; e != -1; e = next[e]) {
					int nxt = to[e];
					int id = edgeId[e];
					if (I[id] && depth[nxt] == -1) {
						depth[nxt] = depth[now] + 1;
						backtrack[nxt] = id;
						vprev[nxt] = now;
						root[nxt] = r;
						que[qe++] = nxt;
					}
				}
			}
		}
	}

	@Override
	public IntArrayList circuit(int e) {
		int s = u[e];
		int t = v[e];
		if (s == t) {
			IntArrayList res = new IntArrayList();
			res.add(e);
			return res;
		}
		if (root[s] == -1 || root[t] == -1 || root[s] != root[t]) {
			return new IntArrayList();
		}
		IntArrayList res = new IntArrayList();
		res.add(e);

		int ds = s, dt = t;
		int ddiff = depth[ds] - depth[dt];
		while (ddiff > 0) {
			res.add(backtrack[ds]);
			ds = vprev[ds];
			ddiff--;
		}
		while (ddiff < 0) {
			res.add(backtrack[dt]);
			dt = vprev[dt];
			ddiff++;
		}
		while (ds != dt) {
			res.add(backtrack[ds]);
			ds = vprev[ds];
			res.add(backtrack[dt]);
			dt = vprev[dt];
		}
		return res;
	}
}
