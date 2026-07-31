package library.util.graph;

import java.util.Arrays;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.collections.IntArrayList;

/**
 * 無向グラフ
 */
public class DAG extends Digraph {
	int[] order;
	boolean isNaturalOrder;
	
	public DAG(int N) {
		//DAGのreverseでN=0で生成してから手動で作ってるので変える時は注意
		super(N);
	}
	
	public static DAG ofNaturalOrder(int N) {
		DAG g = new DAG(N);
		g.isNaturalOrder = true;
		g.order = new int[N];
		for (int i = 0; i < N; i++) g.order[i] = i;
		return g;
	}
	
	public static DAG read(int N, int M) {
		DAG g = new  DAG(N);
		FastScanner sc=FastScanner.getInstance();
		for (int i = 0; i < M; i++) {
			int u=sc.nextInt()-1;
			int v=sc.nextInt()-1;
			g.addEdge(u, v);
		}
		return g;
	}
	
	@Override
	public void addEdge(int from, int to) {
		if (isNaturalOrder && from >= to) throw new AssertionError("natural order DAG requires from < to: " + from + " -> " + to);
		super.addEdge(from, to);
	}
	
	public int minPathCoverSize() {
		//https://atcoder.jp/contests/abc237/submissions/74334147
		BipartiteMatching matching=new BipartiteMatching(N, N);
		for (int i = 0; i < N; i++) {
			for (int j : adj[i]) {
				matching.addEdge(i, j);
			}
		}
		return N-matching.calc();
	}
	
	public int[] topologicalOrder() {
		if (order == null) order = super.topologicalOrder();
		return order;
	}
	
	/**
	 * dp[s]=sを始点としたときの最長パス長を返す。
	 * パス長は辺数で数える。
	 * 未テスト。
	 * @return
	 */
	public int[] longestPathLengthForEachStart() {
		if (order == null) order = topologicalOrder();
		int[]dp=new int[N];
		for (int k = N - 1; k >= 0; k--) {
			int i = order[k];
			for (int j : adj[i]) {
				dp[i]=Math.max(dp[i], dp[j]+1);
			}
		}
		return dp;
	}
	
	public long[] countPath_long(int src) {
		if (order == null) order = topologicalOrder();
		long[]dp=new long[N];
		dp[src]=1;
		for (int i:order) {
			for (int j : adj[i]) {
				dp[j]+=dp[i];
			}
		}
		return dp;
	}
	
	public long[] countPath(int src, long mod) {
		//https://atcoder.jp/contests/ndpc/submissions/75520712
		if (order == null) order = topologicalOrder();
		long[]dp=new long[N];
		dp[src]=1;
		for (int i:order) {
			for (int j : adj[i]) {
				dp[j] += dp[i];
				dp[j] %= mod;
			}
		}
		return dp;
	}
	
	public double[] countPath_double(int src) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		dp[src]=1;
		for (int i:order) {
			for (int j : adj[i]) {
				dp[j]+=dp[i];
			}
		}
		return dp;
	}
	
	/**
	 * dp[i]=全src-iパスの長さの総和
	 * @param src
	 * @return
	 */
	public double[] countPathLength_double(int src) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		double[]path=countPath_double(src);
		for (int i:order) {
			for (int j : adj[i]) {
				dp[j] += dp[i] + path[i];
			}
		}
		return dp;
	}
	
	/**
	 * dp[i]=srcを始点としてランダムウォークしたとき頂点iを通る確率
	 * @param src
	 * @return
	 */
	public double[] visitProbabilityFrom(int src) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		dp[src]=1;
		for (int i:order) {
			double d=outDegree(i);
			for (int j : adj[i]) {
				dp[j]+=dp[i]/d;
			}
		}
		return dp;
	}
	
	/**
	 * dp[i]=iを始点としてランダムウォークしたとき頂点dstを通る確率
	 * @param src
	 * @return
	 */
	public double[] visitProbabilityTo(int dst) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		var ig=reverse();
		dp[dst]=1;
		for (int i:ig.order) {
			double d=outDegree(i);
			for (int j : adj[i]) {
				dp[i]+=dp[j]/d;
			}
		}
		return dp;
	}
	
	
	/**
	 * 各src-iパスPに対して
	 * dp[i]=(始点srcからランダムウォークでPを取る確率)×(Pの長さ)
	 * @param src
	 * @return
	 */
	public double[] expectedPathLengthFrom(int src) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		double[] f=visitProbabilityFrom(src);
		for (int i:order) {
			double d=outDegree(i);
			for (int j : adj[i]) {
				dp[j]+=dp[i]/d+f[i]/d;
			}
		}
		return dp;
	}

	/**
	 * 各i-dstパスPに対して
	 * dp[i]=(始点iからランダムウォークでPを取る確率)×(Pの長さ)
	 * @param dst
	 * @return
	 */
	public double[] expectedPathLengthTo(int dst) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		DAG ig=reverse();
		double[] f=visitProbabilityTo(dst);
		for (int i:ig.order) {
			double d=outDegree(i);
			for (int j : adj[i]) {
				dp[i]+=dp[j]/d+f[j]/d;
			}
		}
		return dp;
	}
	
	public long[] vertexValueMaxPathCostFrom(long[] cost) {
		if (order == null) order = topologicalOrder();
		long[]dp=new long[N];
		for (int i = 0; i < N; i++) {
			dp[i] = cost[i];
		}
		for (int i = order.length - 1; i >= 0; i--) {
			int from = order[i];
			for (int to : adj[from])
			dp[from] = Math.max(dp[from], cost[from] + dp[to]);
		}
		return dp;
	}
	
	
	
	
	
	public DAG reverse() {
		DAG g = new DAG(0);
		g.adj = new IntArrayList[N];
		g.N = N;
		int[]indeg=inDegrees();
		for (int i = 0; i < N; i++) {
			g.adj[i]=new IntArrayList(indeg[i]);
		}
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				g.addEdge(j, i);
			}
		}
		if (order != null) {
			g.order = Arrays.copyOf(order, N);
			ArrayUtils.reverse(g.order);
		}
		return g;
	}

	/**
	 * 各頂点 $v$ に対し、トポロジカル順序における 0-based の位置が閉区間 $[L[v], R[v]]$ に収まるようなトポロジカルソート（スケジュール）が存在するか判定し、
	 * 存在するならトポロジカルソート順に並べた頂点配列を返す。存在しない場合は null を返す。
	 *
	 * <p>AtCoder Beginner Contest 304 H (abc304_h) に対応する。</p>
	 * <p>計算量: $O(N \log N + M)$
	 * <p>事前条件:
	 * <ul>
	 *   <li>$L$ および $R$ はともに長さが $N$ で、各要素は $[0, N-1]$ 内の値であること。</li>
	 * </ul></p>
	 *
	 * @param L 各頂点の下限位置配列（0-based）
	 * @param R 各頂点の上限位置配列（0-based）
	 * @return トポロジカルソート順に並んだ頂点配列。条件を満たす解が存在しない場合は null
	 */
	// 未テスト
	public int[] topologicalSortWithShedule(int[] L, int[] R) {
		if (L == null || R == null || L.length != N || R.length != N) {
			throw new IllegalArgumentException("Bounds arrays must have length equal to the number of vertices.");
		}

		int[] order = topologicalOrder();
		if (order == null) {
			return null;
		}

		int[] targetR = R.clone();
		int[] targetL = L.clone();

		for (int i = N - 1; i >= 0; i--) {
			int v = order[i];
			for (int to : adj[v]) {
				targetR[v] = Math.min(targetR[v], targetR[to] - 1);
			}
		}

		for (int i = 0; i < N; i++) {
			if (targetR[i] < targetL[i]) {
				return null;
			}
			if (targetL[i] < 0 || targetL[i] >= N || targetR[i] < 0 || targetR[i] >= N) {
				return null;
			}
		}

		java.util.PriorityQueue<Integer> que = new java.util.PriorityQueue<>((x, y) -> Integer.compare(targetR[x], targetR[y]));
		IntArrayList[] pending = new IntArrayList[N + 5];
		for (int i = 0; i < pending.length; i++) {
			pending[i] = new IntArrayList();
		}

		int[] indeg = inDegrees();
		for (int i = 0; i < N; i++) {
			if (indeg[i] == 0) {
				pending[targetL[i]].add(i);
			}
		}

		IntArrayList ans = new IntArrayList();
		for (int i = 0; i < N; i++) {
			if (i < pending.length) {
				while (!pending[i].isEmpty()) {
					que.add(pending[i].pollLast());
				}
			}
			if (!que.isEmpty()) {
				int v = que.poll();
				ans.add(v);
				for (int u : adj[v]) {
					indeg[u]--;
					if (indeg[u] == 0) {
						int nextPendingIdx = Math.max(i + 1, targetL[u]);
						if (nextPendingIdx < pending.length) {
							pending[nextPendingIdx].add(u);
						} else {
							return null;
						}
					}
				}
			} else {
				break;
			}
		}

		if (ans.size() != N) {
			return null;
		}

		boolean ok = true;
		for (int i = 0; i < N; i++) {
			int v = ans.get(i);
			if (targetL[v] > i || i > targetR[v]) {
				ok = false;
				break;
			}
		}

		if (!ok) {
			return null;
		}

		return ans.toArray();
	}


	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
