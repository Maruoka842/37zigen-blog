package library.util.graph.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import library.tools.FastScanner;
import library.util.MathUtils;
import library.util.collections.IntArrayList;
import library.util.graph.Edge;
import library.util.graph.FunctionalGraph;
import library.util.graph.LongValueGraph;

public class LongValueForest extends  LongValueGraph {
	public int N;
	public int[] parent;//根の親をN, Nの親をNとしてfunctional graphにする。従って、parent, depth の長さはN+1
	public long[] parentEdgeCost;
	public int[] depth;
	public long[]weightedDepth;
	int[] size;
	boolean isRooted=false;
	int[] bfsOrder;
	public int[] preOrder;
	public int[] inv_preOrder;
	public IntArrayList[] childs;

	FunctionalGraph fg;
	
	public int virtualRoot() {
		return N;
	}
	
	@Override
	public void addEdge(int u, int v, long cost) {
		super.addEdge(u, v, cost);
		if(u == v) throw new AssertionError("森で自己ループは禁止");
	}
	
	public LongValueForest(int N) {
		super(N);
		this.N = N;
	}
	
	/**
	 * rootedを先に呼ぶ必要あり。
	 * @param v
	 * @return
	 */
	public int size(int v) {
		return size[v];
	}
	
	/**
	 * N-size(v)
	 * @param v
	 * @return
	 */
	public int cosize(int v) {
		return N-size[v];
	}
	
	/**
	 * v <- parent[v] を level 回した v を返す。
	 * @param v
	 * @param level
	 * @return
	 */
	public int getLevelAncestor(int v, int level) {
		if (fg == null) {
			buildDoubling();
		}
		return fg.getLevelAncestor(v, level);
	}
	
	/**
	 * v <- parent[v] を 2^loglevel 回した v を返す。
	 * @param v
	 * @param level
	 * @return
	 */
	public int getPower2LevelAncestor(int v, int loglevel) {
		if (fg == null) {
			buildDoubling();
		}
		return fg.getPower2LevelAncestor(v, loglevel);
	}

	void buildDoubling() {
		if (parent == null) throw new AssertionError("root is undefined");
		fg = new FunctionalGraph(parent);
		fg.buildDoubling(MathUtils.floorLog2(N)+1);
	}
	
	public int lca(int u, int v) {
		//https://atcoder.jp/contests/abc451/submissions/74513448
		if (fg == null) {
			buildDoubling();
		}

		if (depth[u] < depth[v]) return lca(v, u);
		int diff = depth[u] - depth[v];
		int logn=MathUtils.floorLog2(N);
		for (int i = 0; i <= logn; ++i) {
			if (diff % 2 == 1) {
				u = fg.parentOf2powers[i][u];
			}
			diff /= 2;
		}
		if (u == v) return u;
		for (int i = logn; i >= 0; --i) {
			int nu = fg.parentOf2powers[i][u];
			int nv = fg.parentOf2powers[i][v];
			if (nu != nv) {
				u = nu;
				v = nv;
			}
		}
		return parent[u];
	}
	
	public boolean isRoot(int x) {
		return parent[x] == N;
	}
	
	/**
	 * 森に含まれる各連結成分を任意の頂点を根として向きづけ、BFS順の配列を返す。
	 * @return BFS順に並んだ頂点列
	 */
	public int[] rooted() {
		return rooted(0);
	}

	/**
	 * 指定された頂点 firstRoot を含む連結成分において、firstRoot が根となるように辺の向き（親子関係）を決定し、
	 * 森全体の連結成分を走査した結果をBFS順の配列として返す。
	 * firstRoot を含む連結成分以外の成分については、成分内の任意の頂点が根として選ばれる。
	 * @param firstRoot 優先的に根とする頂点
	 * @return BFS順に並んだ頂点列
	 */
	public int[] rooted(int firstRoot) {
		//https://atcoder.jp/contests/abc451/submissions/74513609
		parent = new int[N + 1];
		parentEdgeCost = new long[N + 1];
		depth = new int[N + 1];
		size = new int[N + 1];
		weightedDepth = new long[N + 1];
		Arrays.fill(parent, N);
		int[]ret=new int[N];
		childs = new IntArrayList[N + 1];
		for (int i = 0; i < childs.length; i++) {
			childs[i]=new IntArrayList();
		}
		depth[N] = -1;
		boolean[] vis = new boolean[N + 1];
		int pointer=0;
		for (int i = 0; i < N; ++i) {
			int root = (i == 0) ? firstRoot : (i <= firstRoot ? i - 1 : i);
			if (vis[root]) continue;
			parent[root] = N;
			depth[root] = 0;
			Queue<Integer> que=new ArrayDeque<>();
			que.add(root);
			vis[root] = true;
			while(!que.isEmpty()) {
				int v = que.poll();
				ret[pointer++]=v;
				for (Edge e: adj[v]) {
					int next=e.dst;
					if (next == parent[v]) continue;
					childs[v].add(next);
					vis[next] = true;
					que.add(next);
					parent[next] = v;
					parentEdgeCost[next] = e.cost;
					depth[next] = depth[v] + 1;
					weightedDepth[next] = weightedDepth[v] + e.cost;
				}
			}
		}
		for (int i = 0; i < N; ++i) {
			int v = ret[N - 1 - i];
			size[v] = 1;
			for (Edge e : adj[v]) if (e.dst != parent[v]) {
				size[v] += size[e.dst];
			}
		}
		isRooted=true;
		bfsOrder = ret;
		preOrder = null;
		inv_preOrder = null;
		fg = null;
		return ret;
	}

	/**
	 * 未テスト
	 */
	public int[] preOrder() {
		if (!isRooted) throw new AssertionError("rootedを呼んでいない");
		if (preOrder == null) {
			preOrder = new int[N];
			int[] pointer = {0};
			boolean[] vis = new boolean[N];
			for (int i = 0; i < N; i++) {
				int v = bfsOrder[i];
				if (!vis[v] && isRoot(v)) {
					_dfs_preOrder(v, vis, pointer);
				}
			}
		}
		return preOrder;
	}

	private void _dfs_preOrder(int v, boolean[] vis, int[] pointer) {
		vis[v] = true;
		preOrder[pointer[0]++] = v;
		for (int u : childs[v]) {
			_dfs_preOrder(u, vis, pointer);
		}
	}

	/**
	 * 未テスト
	 */
	public int preOrderOf(int v) {
		if (inv_preOrder == null) {
			if (preOrder == null) preOrder();
			inv_preOrder = new int[N];
			for (int i = 0; i < N; i++) inv_preOrder[preOrder[i]] = i;
		}
		return inv_preOrder[v];
	}

	/**
	 * 未テスト
	 */
	public boolean inSubtree(int p, int v) {
		int preP = preOrderOf(p);
		int preV = preOrderOf(v);
		return preP <= preV && preV < preP + size[p];
	}

	/**
	 * 未テスト
	 * uからv方向にk頂点移動した頂点を返す。vを通り過ぎる場合は-1。
	 * @param u
	 * @param v
	 * @param k
	 * @return
	 */
	public int jump(int u, int v, int k) {
		if (k < 0) throw new AssertionError();
		int w = lca(u, v);
		int du = depth[u] - depth[w];
		int dv = depth[v] - depth[w];
		if (k > du + dv) return -1;
		if (k <= du) {
			return getLevelAncestor(u, k);
		} else {
			return getLevelAncestor(v, du + dv - k);
		}
	}
	
	/**
	 * a-b-cがパスをなすか判定
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public boolean isOnPath(int a, int b, int c) {
		if (depth == null) throw new AssertionError("rootedを呼んでいない");
		return rawDist(a, b) + rawDist(b, c) == rawDist(a, c);
	}
	
	public long dist(int a, int b) {
		return weightedDepth[a] + weightedDepth[b] - 2 * weightedDepth[lca(a, b)];
	}

	public long rawDist(int a, int b) {
		return depth[a] + depth[b] - 2 * depth[lca(a, b)];
	}
	
	public long parentEdgeCost(int v) {
		if(!isRooted)throw new AssertionError();
		if(isRoot(v))throw new AssertionError();
		return weightedDepth[v]-weightedDepth[parent(v)];
	}
	
	public long[] prefixXor() {
		//https://atcoder.jp/contests/abc451/submissions/74513837
		if (!isRooted) throw new AssertionError();
		long[]ret=new long[N];
		for (int i : bfsOrder) {
			for (int ch : childs[i]) {
				ret[ch] = ret[i] ^ parentEdgeCost(ch);
			}
		}
		return ret;
	}
	
	/**
	 * b[i]=iを根とする部分木のaの値の和
	 * @param a
	 * @return
	 */
	public long[] subtreeSum(long[] a) {
		long[] b = Arrays.copyOf(a, a.length);
		if (!isRooted) throw new AssertionError("rootが未設定");
		for (int i = N - 1; i >= 0; i--) {
			int v=bfsOrder[i];
			for (int ch:childs[v]) {
				b[v]+=b[ch];
			}
		}
		return b;
	}

	
	public int parent(int x) {
		if (isRoot(x)) throw new AssertionError();
		return parent[x];
	}
	
	
	public boolean isRooted() {
		return isRooted;
	}
	
	public static LongValueForest read(int N, int M) {
		LongValueForest forest = new LongValueForest(N);
		FastScanner sc=FastScanner.getInstance();
		for (int i = 0; i < M; i++) {
			int a=sc.nextInt()-1;
			int b=sc.nextInt()-1;
			long c=sc.nextLong();
			forest.addEdge(a, b, c);
		}
		return forest;
	}
	
	public static LongValueTree readAsTree(int N) {
		LongValueTree tree = new LongValueTree(N);
		FastScanner sc=FastScanner.getInstance();
		for (int i = 0; i < N - 1; i++) {
			int a=sc.nextInt()-1;
			int b=sc.nextInt()-1;
			long c=sc.nextLong();
			tree.addEdge(a, b, c);
		}
		return tree;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
