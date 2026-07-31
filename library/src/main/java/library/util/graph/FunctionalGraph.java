package library.util.graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntPredicate;

/**
 * コンストラクタ呼び出しの時点で、サイクルの頂点の列挙とorderの計算を行う。
 */
public class FunctionalGraph {
	public int N;
	public int[] parent;
	public ArrayList<ArrayList<Integer>> cycles;
	public boolean[] isCycleVertice;
	int[] order;//葉を貪欲に取り除いて順に並べ、その後、サイクルの頂点を並べたもの。
	public int LOG=64;//2^LOG未満の値を探索可能
	/**
	 * parentOf2powers[i][j] は j に parent を i 回適用した頂点 ( 0 <= i < 64 )
	 */
	public int[][] parentOf2powers;
	
	public FunctionalGraph(int[] parent) {
		this.N = parent.length;
		this.parent = parent;
		build();
	}
	
	public void buildDoubling(int LOG) {
		this.LOG = LOG;
		parentOf2powers = new int[LOG][N];
		parentOf2powers[0] = parent;
		for (int i = 0; i + 1 < LOG; ++i) {
			for (int j = 0; j < N; ++j) {
				parentOf2powers[i + 1][j] = parentOf2powers[i][parentOf2powers[i][j]];
			}
		}
	}
	
	public void buildDoubling() {
		buildDoubling(64);
	}
	
	public int getPower2LevelAncestor(int v, int loglevel) {
		if(parentOf2powers == null) buildDoubling();
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
		if(parentOf2powers == null) buildDoubling();
		for (int i = 0; i < LOG; ++i) {
			if (repeat % 2 == 1) {
				vertex = parentOf2powers[i][vertex];
			}
			repeat /= 2;
		}
		return vertex;
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
	
}