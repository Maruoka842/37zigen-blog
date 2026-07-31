package library.util.graph;

import library.util.graph.tree.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.DoubleToLongFunction;
import java.util.function.LongBinaryOperator;

/**
 * コンストラクタ呼び出しの時点で、サイクルの頂点の列挙とorderの計算を行う。
 * 
 * 
 * まだ作りかけ。
 */
public class LongValueFunctionalGraph {
	public int N;
	public int[] parent;
	public long[] data;
	public ArrayList<ArrayList<Integer>> cycles;
	public boolean[] isCycleVertice;
	int[] order;//葉を貪欲に取り除いて順に並べ、その後、サイクルの頂点を並べたもの。
	int LOG=64;//2^LOG未満の値を探索可能
	/**
	 * parentOf2powers[i][j] は j に parent を i 回適用した頂点 ( 0 <= i < 64 )
	 */
	int[][] parentOf2powers;
	long[][] dataOf2powers;
	LongBinaryOperator op;
	
	public LongValueFunctionalGraph(int[] parent) {
		this.N = parent.length;
		this.parent = parent;
		build();
	}
	
	public LongValueFunctionalGraph(int[] parent, int LOG) {
		this.N = parent.length;
		this.parent = parent;
		this.LOG = LOG;
		build();
	}
	
	void buildDoublingOfParents() {
		parentOf2powers = new int[LOG][N];
		parentOf2powers[0] = parent;
		for (int i = 0; i + 1 < LOG; ++i) {
			for (int j = 0; j < N; ++j) {
				parentOf2powers[i + 1][j] = parentOf2powers[i][parentOf2powers[i][j]];
			}
		}
	}
	
	/**
	 * op(a, b)はaがancestor側。data[i]は頂点iに載せる。
	 * @param data
	 * @param op
	 */
	public void buildDoublingOfData(long[]data, LongBinaryOperator op) {
		if (parentOf2powers==null) buildDoublingOfParents();
		this.op = op;
		this.data = data;
		dataOf2powers = new long[LOG][N];
		dataOf2powers[0] = new long[N];
		for (int i = 0; i < N; i++) {
			dataOf2powers[0][i] = data[parent[i]];
		}
		for (int i = 0; i + 1 < LOG; ++i) {
			for (int j = 0; j < N; ++j) {
				dataOf2powers[i + 1][j] = op.applyAsLong(dataOf2powers[i][parentOf2powers[i][j]], dataOf2powers[i][j]);
			}
		}
	}
	
	public int getPower2LevelAncestor(int v, int loglevel) {
		if(parentOf2powers == null) buildDoublingOfParents();
		return parentOf2powers[loglevel][v];
	}
	
	/**
	 *  vertexのrepeat個上の祖先を返す。
	 * @param vertex
	 * @param repeat
	 * @return
	 */
	public int getLevelAncestor(int vertex, long repeat) {
		if(repeat<0)throw new AssertionError();
		if(parentOf2powers == null) buildDoublingOfParents();
		for (int i = 0; i < LOG; ++i) {
			if (repeat % 2 == 1) {
				vertex = parentOf2powers[i][vertex];
			}
			repeat /= 2;
		}
		return vertex;
	}
	
	/**
	 * repeat=0のときdata[vertex]を返す。repeat=1のときop(data[parent[v]], v)を返す。
	 * @param vertex
	 * @param repeat
	 * @return
	 */
	public long foldToAncestor(int vertex, long repeat) {
		//https://atcoder.jp/contests/abc179/submissions/72276069
		if(repeat < 0)throw new AssertionError();
		long ret = data[vertex];
		for (int i = 0; i < LOG; ++i) {
			if (repeat % 2 == 1) {
				ret = op.applyAsLong(dataOf2powers[i][vertex], ret);
				vertex = parentOf2powers[i][vertex];
			}
			repeat /= 2;
		}
		return ret;
	}


	/**
	 * parent が定める写像 {@code p : {0,…,N−1} → {0,…,N−1}} に対し、
	 * A を
	 * <pre>{@code
	 *   (A * x)[u] =  Σ_{v : p(v)=u}  op( data[u], x[v] )
	 * }</pre>
	 * で定める。
	 * このとき本メソッドが返す {@code ret} は、写像 A の反復適用による
	 * <b>ステップ制限付き bottom-up 集約</b>
	 * <pre>{@code
	 *   ret = data  +  (A * data)  +  (A^2 * data)  +  …  +  (A^repeat * data)
	 * }</pre>
	 * に対応。左分配則が必要。
	 * @param repeat
	 * @return
	 */
	public long[] sumSubtree(long repeat) {
		//https://atcoder.jp/contests/abc310/submissions/72280118
		if (parentOf2powers == null) buildDoublingOfParents();
		if(repeat < 0)throw new AssertionError();
		long[] x = data.clone();
		long[] ret = new long[N];
		repeat++;
		for (int t = 0; t < LOG; ++t) {
			if ((repeat >> t) % 2 == 1) {
				for (int i = 0; i < N; i++) {
					ret[i] = ret[i] + x[i];
				}
				long[] nx = new long[N];
				for (int i = 0; i < N; i++) {
					nx[parentOf2powers[t][i]] += op.applyAsLong(dataOf2powers[t][i], x[i]);
				}
				x = nx;//b ← A^{2^t}b
			}
			long[] nx = x.clone();
			for (int i = 0; i < N; i++) {
				nx[parentOf2powers[t][i]] += op.applyAsLong(dataOf2powers[t][i], x[i]);
			}
			x = nx;
			// x = (1+A)(1+A^2)..(1+A^{2^t})b
		}
		return ret;
	}
	
	
	
	public long[] modSumSubtree(long repeat, long mod) {
		if (parentOf2powers == null) buildDoublingOfParents();
		if(repeat < 0)throw new AssertionError();
		long[] x = data.clone();
		long[] ret = new long[N];
		repeat++;
		for (int t = 0; t < LOG; ++t) {
			if ((repeat >> t) % 2 == 1) {
				for (int i = 0; i < N; i++) {
					ret[i] = (ret[i] + x[i]) % mod;
				}
				long[] nx = new long[N];
				for (int i = 0; i < N; i++) {
					nx[parentOf2powers[t][i]] += op.applyAsLong(dataOf2powers[t][i], x[i]);
					nx[parentOf2powers[t][i]] %= mod;
				}
				x = nx;//b ← A^{2^t}b
			}
			long[] nx = x.clone();
			for (int i = 0; i < N; i++) {
				nx[parentOf2powers[t][i]] += op.applyAsLong(dataOf2powers[t][i], x[i]);
				nx[parentOf2powers[t][i]] %= mod;
			}
			x = nx;
			// x = (1+A)(1+A^2)..(1+A^{2^t})b
		}
		return ret;
	}

	
	
	private void build() {
		int[] inDegree = new int[N];
		order = new int[N];
		int cnt = 0;
		for (int i = 0; i < N; ++i) inDegree[parent[i]]++;
		Queue<Integer> que = new ArrayDeque<>();
		for (int i = 0; i < N; ++i) {
			if (inDegree[i] == 0) {
				que.add(i);
			}
		}
		while (!que.isEmpty()) {
			int v = que.poll();
			int u = parent[v];
			order[cnt++] = v;
			inDegree[u]--;
			if (inDegree[u] == 0)  {
				que.add(u);
			}
		}
		cycles = new ArrayList<>();
		isCycleVertice = new boolean[N];
		for (int i = 0; i< N; ++i) {
			if (inDegree[i] == 1) {
				ArrayList<Integer> cycle = new ArrayList<>();
				int v = i;
				do {
					cycle.add(v);
					order[cnt++] = v;
					isCycleVertice[v] = true;
					inDegree[parent[v]]--;
					v = parent[v];
				} while (v != i);
				cycles.add(cycle);
			}
		}
		
	}
	
	
	/**
	 * 葉を貪欲に取り除いて順に並べ、その後、サイクルの頂点を並べたもの。
	 * @return
	 */
	public int[] order() {
		return order;
	}
	
	/**
	 * 部分木のサイズを頂点ごとに並べた配列を返す
	 * @return
	 */
	public int[] subtreeSizes() {
		int[]size=new int[N];
		Arrays.fill(size, 1);
		for (int i:order()) {
			if (!isCycleVertice[i]) {
				size[parent[i]]+=size[i];
			}
		}
		return size;
	}
	
	public void draw() {
		Digraph g = new Digraph(N);
		for (int i = 0; i < N; i++) {
			g.addEdge(i, parent[i]);
		}
		g.draw();
	}
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}