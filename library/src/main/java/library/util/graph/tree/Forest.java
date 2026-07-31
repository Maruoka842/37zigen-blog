package library.util.graph.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.IntPredicate;

import library.tools.FastScanner;
import library.util.collections.IntArrayList;
import library.util.graph.FunctionalGraph;
import library.util.graph.Graph;

public class Forest extends Graph {
	protected int[] parent;//根の親をN, Nの親をNとしてfunctional graphにする。従って、parent, depth の長さはN+1
	public IntArrayList[] childs;
	public int[] depth;
	int[] size;
	int[] bfsOrder;
	
	FunctionalGraph fg;
	
	@Override
	public void addEdge(int u, int v) {
		super.addEdge(u, v);
		if(u == v) throw new AssertionError("森で自己ループは禁止");
	}
	
	public Forest(int N) {
		super(N);
	}
	
	/**
	 * parent[i]=-1はiが根として構築する
	 * @param parent
	 */
	public Forest(int[] parent) {
		super(parent.length);
		this.parent = new int[N + 1];
		depth = new int[N + 1];
		size = new int[N + 1];
		childs = new IntArrayList[N + 1];
		for (int i = 0; i < childs.length; i++) {
			childs[i]=new IntArrayList();
		}
		
		depth[N] = -1;
		this.parent[N] = N;
		Queue<Integer> que = new ArrayDeque<>();
		for (int i = 0; i < parent.length; i++) {
			if (parent[i] == -1) {
				this.parent[i] = N;
				depth[i] = 0;
				que.add(i);
			} else {
				this.parent[i] = parent[i];
				addEdge(this.parent[i], i);
			}
		}
		bfsOrder = new int[N];
		int pointer = 0;
		while (!que.isEmpty()) {
			int v = que.poll();
			bfsOrder[pointer++] = v;
			for (int neighbor : adj[v]) {
				if (neighbor != this.parent[v]) childs[v].add(neighbor);
			}
			for (int child : childs[v]) {
				depth[child] = depth[v] + 1;
				que.add(child);
			}
		}
		
		for (int i = N - 1; i >= 0; i--) {
			int v = bfsOrder[i];
			size[v] = 1;
			for (int child : childs[v]) {
				size[v] += size[child];
			}
		}		
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
	 * v <- parent[v] を level 回した v を返す。
	 * @param v
	 * @param level
	 * @return
	 */
	public int getLevelAncestor(int v, int level) {
		if (fg == null) {
			if (parent == null) throw new AssertionError("root is undefined");
			fg = new FunctionalGraph(parent);
			fg.buildDoubling();
		}
		return fg.getLevelAncestor(v, level);
	}
	
	public boolean isRooted() {
		return parent != null;
	}
	
	void buildDoubling() {
		fg = new FunctionalGraph(parent);
		fg.buildDoubling();
	}
	
	public int lca(int u, int v) {
		if (!isRooted()) throw new AssertionError("root is undefined");
		if (fg == null) {
			buildDoubling();
		}
		if (depth[u] < depth[v]) return lca(v, u);
		int diff = depth[u] - depth[v];
		for (int i = 0; i < 30; ++i) {
			if (diff % 2 == 1) {
				u = fg.parentOf2powers[i][u];
			}
			diff /= 2;
		}
		if (u == v) return u;
		for (int i = 29; i >= 0; --i) {
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
		parent = new int[N + 1];
		depth = new int[N + 1];
		size = new int[N + 1];
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
				for (int next : adj[v]) {
					if (next == parent[v]) continue;
					childs[v].add(next);
					vis[next] = true;
					que.add(next);
					parent[next] = v;
					depth[next] = depth[v] + 1;
				}
			}
		}
		for (int i = 0; i < N; ++i) {
			int v = ret[N - 1 - i];
			size[v] = 1;
			for (int u : adj[v]) if (u != parent[v]) {
				size[v] += size[u];
			}
		}
		bfsOrder = ret;
		fg = null;
		return ret;
	}
	
	/**
	 * a-b-cがパスをなすか判定
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public boolean isPath(int a, int b, int c) {
		if (depth == null) throw new AssertionError("rootedを呼んでいない");
		return dist(a, b) + dist(b, c) == dist(a, c);
	}
	
	public int dist(int a, int b) {
		if (!isRooted()) throw new AssertionError();
		return depth[a] + depth[b] - 2 * depth[lca(a, b)];
	}
	
	public int[] path(int a, int b) {
		//https://atcoder.jp/contests/abc270/submissions/74379959
		if (depth == null) throw new AssertionError();
		IntArrayList left=new IntArrayList();
		IntArrayList right=new IntArrayList();
		left.add(a);
		right.add(b);
		while(a != b) {
			if(depth[a] > depth[b]) {
				a=parent[a];
				left.add(a);
			} else {
				b=parent[b];
				right.add(b);
			}
		}
		for (int i = 0; i < right.size() - 1; i++) {
			left.add(right.get(right.size()-2-i));
		}
		return left.toArray();
	}
	
	public static Forest read(int N, int M) {
		Forest forest = new Forest(N);
		FastScanner sc=FastScanner.getInstance();
		for (int i = 0; i < M; i++) {
			int a=sc.nextInt()-1;
			int b=sc.nextInt()-1;
			forest.addEdge(a, b);
		}
		return forest;
	}
	
	public int parent(int x) {
		if (isRoot(x)) throw new AssertionError();
		return parent[x];
	}
	
	public long[] subtreeSum(long[] a) {
		//https://atcoder.jp/contests/abc345/submissions/74247165
		if (childs[N]==null) throw new AssertionError();
		long[] ret=new long[N];
		for (int k = N - 1; k >= 0; k--) {
			int i = bfsOrder[k];
			ret[i] = a[i];
			for (int c : childs[i]) {
				ret[i] += ret[c];
			}
		}
		return ret;
	}
	
	/**
	 * vertexの先祖(vertex含む)のうち f.test(vertex) となる最も根に近いものを返す。存在しない場合-1
	 * @param vertex
	 * @param f
	 * @return
	 */
	public int binarySearch(int vertex, IntPredicate f) {
		//https://atcoder.jp/contests/abc212/submissions/74399562
		if (depth == null) throw new AssertionError();
		if (fg == null) {
			buildDoubling();
		}

		if (!f.test(vertex)) return -1;
		for (int i = 20; i >= 0; i--) {
			int nv=fg.getPower2LevelAncestor(vertex, i);
			if (nv != N && f.test(nv)) {
				vertex=nv;
			}
		}
		return vertex;
	}

	
}
