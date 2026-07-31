package library.util.graph.tree;

import java.util.ArrayDeque;
import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * rootを根として重軽分解
 * verified:https://atcoder.jp/contests/abc301/submissions/70367326
 */
public class HLDecomposition {
	int N;
	public int[] depth;
	public int[] head;
	public int[] heavy;//heavy[v]=vの子のうち最も重い頂点。vが葉のときは-1
	public int[] parent;
	int[] sz;
	public int[] id;//頂点集合[N]から[N]への全単射。同じパス内の頂点は親から子に向かって昇順かつ連番のidになる。
	public Tree f;
	private final int root;
	private final IntArrayList[] childs;

	public HLDecomposition(Tree f) {
		this(f.N, f.root(), f.childs);
		this.f = f;
	}

	public HLDecomposition(int N, int root, IntArrayList[] childs) {
		this.N = N;
		this.root = root;
		this.childs = childs;
		depth = new int[N];
		head = new int[N];
		heavy = new int[N];
		parent = new int[N];
		id = new int[N];
		sz = new int[N];
		Arrays.fill(head, -1);
		Arrays.fill(id, -1);
		Arrays.fill(parent, -1);
		Arrays.fill(heavy, -1);
		build();
	}

	void build() {
		dfs(root, -1);
		bfs();
	}

	void bfs() {
		ArrayDeque<Integer> pend = new ArrayDeque<>();
		int gen = 0;
		pend.add(root);
		while (!pend.isEmpty()) {
			int v = pend.pollFirst();
			int top = v;
			for (; v != -1; v = heavy[v]) {
				id[v] = gen++;
				head[v] = top;
				for (int d : childs[v]) {
					if (d == heavy[v]) {
						continue;
					}
					pend.add(d);
				}
			}
		}
	}

	public int lca(int u, int v) {
		if (head[u] != head[v]) {
			if (depth[head[u]] < depth[head[v]]) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			return lca(parent[head[u]], v);
		} else {
			if (depth[u] > depth[v]) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			return u;
		}
	}

	int dfs(int v, int p) {
		parent[v] = p;
		int s = 1;//vの部分木のサイズ=sz[v]
		int to = -1;//vの子のうち最も重い頂点=heavy[v]
		for (int d : childs[v]) {
			depth[d] = depth[v] + 1;
			s += dfs(d, v);
			if (to == -1 || sz[d] > sz[to]) {
				to = d;
			}
		}
		sz[v] = s;
		heavy[v] = to;
		return s;
	}
}