package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.collections.IntArrayList;
import library.util.graph.tree.DoubleValueForest;
import library.util.graph.tree.Forest;
import library.util.graph.tree.LongValueForest;
import library.util.linalg.MatrixUtilsFp;
import library.util.polynomial.PolynomialFp;
import library.util.poset.BooleanLattice;
import library.util.unionfind.UnionFind;

public class Graphs {
	/**
	 * 有向グラフの隣接行列Aを受け取り、オイラー回路の数を返す。
	 * @param A
	 * @return
	 * https://judge.yosupo.jp/submission/339347
	 */
	public static long countEulerTourForLabelledDirectedEdges(long[][] A, long mod) {
		int N=A.length;
		int[]indeg=new int[N];
		int[]outdeg=new int[N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				outdeg[i]+=A[i][j];
				indeg[j]+=A[i][j];
			}
		}
		for (int i = 0; i < N; i++) {
			if(indeg[i]!=outdeg[i])return 0;
		}
		ArrayList<Integer> list=new ArrayList<>();
		for (int i = 0; i < N; i++) {
			if(outdeg[i]!=0)list.add(i);
		}
		long[][]C=ArrayUtils.take(A, list, list);
		outdeg=ArrayUtils.take(outdeg, list);
		Fp fp=new Fp(mod);
		int root=0;
		long x=Graphs.countSpanningTreeDirectedToRoot(root, C, mod);
		for (int i = 0; i < list.size(); i++) {
			x=x*fp.fac(outdeg[i]-1)%mod;
		}
		return x;
	}

	
	
	/**
	 * 隣接行列Aを受け取り、rootの方向に辺が向きづけられた全域木の個数を返す
	 * https://judge.yosupo.jp/submission/339306
	 * @param root
	 * @param A
	 * @param mod
	 * @return
	 */
	public static long countSpanningTreeDirectedToRoot(int root, long[][] A, long mod) {
		/*
		 * f, g: E×V → {1,-1} を
		 * 
		 * 辺 e = (u, v) ∈ E ならば f(u, e) = 1. さもなくば f(u, e) = 0 
		 * 辺 e = (u, v) ∈ E ならば g(u, e) = 1, g(v, e) = -1. さもなくば g(u, e) = 0
		 * 
		 * として定める。
		 * 
		 * C[u, v] = Σ_e f(u, e) g(v, e) は C[u, u] = deg(u), C[u, v] = - (辺uvの重複度)
		 * となり、root 行目と root列目を除いた行列の行列式が求める値。
		 * 
		 */
		
		
		int N=A.length;
		if(N==1)return 1;
		long[][]B=new long[N-1][N-1];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				int offsetI=i>=root?-1:0;
				int offsetJ=j>=root?-1:0;
				if(i==j)continue;//自己辺は全域木に含まれないのでスルー
				if(i!=root&&j!=root) B[i+offsetI][j+offsetJ]=(mod-A[i][j])%mod;
				if(i!=root) B[offsetI+i][offsetI+i]+=A[i][j];
			}
		}
		return MatrixUtilsFp.modDeterminant(B, mod);
	}

	
    
	/**
	 * 引数のdをd[i][j]=(有向辺ijの重み)としたとき、頂点srcから頂点集合sの頂点を経由（sは先頭のsrc含む)してdstにたどり着くまでの最短距離dist[dst][s]とした配列distを返す。
	 * 負辺が合っても動く。
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(n<sup>2</sup> · 2<sup>n</sup>)</li>
	 *   <li>空間計算量: O(n · 2<sup>n</sup>)</li>
	 * </ul>
	 * @param d
	 * @param src
	 * @return
	 * https://atcoder.jp/contests/abc343/submissions/72152092
	 */
	public static long[][] hamiltonPathForEachSubgraphs(long[][] d, int src) {
		int n=d.length;
		long[][]dist=new long[n][1<<n];
		long INF=Long.MAX_VALUE/3;
		ArrayUtils.fill(dist, INF);
		dist[src][1 << src]=0;
		for (int s = 0; s < 1<<n; s++) {
			for (int v = 0; v < n; v++) {
				if (dist[v][s] == INF) continue;
				for (int next = 0; next < n; next++) {
					if((s & (1 << next))==0) {
						int ns=s|(1<<next);
						long nd=dist[v][s]+d[v][next];
						if(nd<dist[next][ns])
							dist[next][ns]=nd;
					}
				}
			}
		}
		return dist;
	}

	
	public static long hamiltonPath(long[][] d) {
		int n=d.length;
		long[][]dist=new long[n][1<<n];
		long INF=Long.MAX_VALUE/3;
		ArrayUtils.fill(dist, INF);
		for (int i = 0; i < n; i++) {
			dist[i][1 << i]=0;
		}
		for (int s = 0; s < 1<<n; s++) {
			for (int v = 0; v < n; v++) {
				if (dist[v][s] == INF) continue;
				for (int next = 0; next < n; next++) {
					if((s & (1 << next))==0) {
						int ns=s|(1<<next);
						long nd=dist[v][s]+d[v][next];
						if(nd<dist[next][ns])
							dist[next][ns]=nd;
					}
				}
			}
		}
		long ans=INF;
		for (int i = 0; i < n; i++) {
			ans=Math.min(ans, dist[i][(1<<n)-1]);
		}
		return ans;
	}

	/**
	 * 無向グラフの彩色数を返す。
	 * 各独立集合を「使える色クラス」とみなし、全頂点集合を被覆する最小集合被覆サイズを求める。
	 * 計算量: O(2^N)
	 */
	public static int chromaticNumber(Graph g) {
		//https://judge.yosupo.jp/submission/373270
		int n = g.N;
		if (n >= 31) throw new IllegalArgumentException("N must be <= 30");
		int mask = 1 << n;
		int[] neighborhood = new int[n];
		for (int v = 0; v < n; v++) {
			int bits = 0;
			for (int i = 0; i < g.adj[v].size(); i++) {
				int u = g.adj[v].get(i);
				bits |= 1 << u;
			}
			neighborhood[v] = bits;
		}
		boolean[] independent = new boolean[mask];
		independent[0] = true;
		for (int s = 1; s < mask; s++) {
			int lsb = s & -s;
			int v = Integer.numberOfTrailingZeros(lsb);
			int t = s ^ lsb;
			independent[s] = independent[t] && (t & neighborhood[v]) == 0;
		}
		return BooleanLattice.minmumSetCoverSize(independent);
	}

	
	
	public static LongValueForest minimumSpanningForest(LongValueGraph g) {
		//https://atcoder.jp/contests/abc451/submissions/74513609
		List<Edge> list = g.edges();
		Collections.sort(list);
		UnionFind uf = new UnionFind(g.N);
		LongValueForest mst = new LongValueForest(g.N);
		for (Edge e : list) {
			if (!uf.equiv(e.src, e.dst)) {
				uf.union(e.src, e.dst);
				mst.addEdge(e.src, e.dst, e.cost);
			}
		}
		return mst;
	}

	public static DoubleValueForest minimumSpanningForest(DoubleValueGraph g) {
		//https://atcoder.jp/contests/past21-open/submissions/75136521
		List<DoubleValueEdge> list = g.edges();
		Collections.sort(list);
		UnionFind uf = new UnionFind(g.N);
		DoubleValueForest mst = new DoubleValueForest(g.N);
		for (DoubleValueEdge e : list) {
			if (!uf.equiv(e.src, e.dst)) {
				uf.union(e.src, e.dst);
				mst.addEdge(e.src, e.dst, e.cost);
			}
		}
		return mst;
	}

	/**
	 * 二次元平面上の頂点たちのマンハッタン距離によるMSTを返す。
	 * 未テスト
	 * 計算量: O(N log N)
	 * @param xs x座標
	 * @param ys y座標
	 * @return マンハッタン距離によるMST
	 */
	public static LongValueForest manhattanMST(long[] xs, long[] ys) {
		//https://judge.yosupo.jp/submission/371892
		int n = xs.length;
		if (ys.length != n) throw new IllegalArgumentException();
		xs = Arrays.copyOf(xs, n);
		ys = Arrays.copyOf(ys, n);
		Integer[] idx = new Integer[n];
		Arrays.setAll(idx, i -> i);
		List<Edge> edges = new ArrayList<>();
		for (int s = 0; s < 2; s++) {
			for (int t = 0; t < 2; t++) {
				final long[] curXs = xs;
				final long[] curYs = ys;
				Arrays.sort(idx, (i, j) -> Long.compare(curXs[i] + curYs[i], curXs[j] + curYs[j]));
				TreeMap<Long, Integer> sweep = new TreeMap<>();
				for (int i : idx) {
					while (true) {
						var entry = sweep.floorEntry(ys[i]);
						if (entry == null) break;
						int j = entry.getValue();
						if (xs[i] - xs[j] < ys[i] - ys[j]) break;
						edges.add(new Edge(i, j, Math.abs(xs[i] - xs[j]) + Math.abs(ys[i] - ys[j])));
						sweep.remove(entry.getKey());
					}
					sweep.put(ys[i], i);
				}
				long[] tmp = xs;
				xs = ys;
				ys = tmp;
			}
			for (int i = 0; i < n; i++) xs[i] = -xs[i];
		}
		Collections.sort(edges);
		UnionFind uf = new UnionFind(n);
		LongValueForest mst = new LongValueForest(n);
		for (Edge e : edges) {
			if (!uf.equiv(e.src, e.dst)) {
				uf.union(e.src, e.dst);
				mst.addEdge(e.src, e.dst, e.cost);
			}
		}
		return mst;
	}
	
	
	/**
	 * 全てのN頂点D-regularグラフに対して、workを適用する
	 * @param N
	 * @param D
	 * @param work
	 */
    public static void forEachDRegularGraphs(int N, int D, Consumer<Graph> work) {
        boolean[][] a = new boolean[N][N];
        int[] deg = new int[N];
        dfsInternal(0, 1, a, deg, D, work);
    }
	
	static void dfsInternal(int i, int j, boolean[][] a, int[] deg, int D, Consumer<Graph> f) {
		int N=a.length;
		int ni=i;
		int nj=j+1;
		if (deg[i]+(N-j)<D)return;
		if (i == N - 1 && j == N) {
	        if (deg[i] != D) return;
	        f.accept(new Graph(a));
	        return;
	    }
		if (i+1==j && i > 0) {
			if(deg[i-1]!=D)return;
		}		

		if (nj>=N) {
			ni=i+1;
			nj=ni+1;
		}
		
		//ijを不採用
		dfsInternal(ni, nj, a, deg, D, f);
		//ijを採用
		if (i != j && !a[i][j] && deg[i]<D&&deg[j]<D) {
			a[i][j]=a[j][i]=true;
			deg[i]++;
			deg[j]++;
			dfsInternal(ni,nj,a,deg,D,f);
			a[i][j]=a[j][i]=false;
			deg[i]--;
			deg[j]--;
		}
	}
	
	
	public static long[][] warshalFloyd(long[][] A) {
		long[][]d=ArrayUtils.copy(A);
		int N=d.length;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					d[j][k]=Math.min(d[j][i]+d[i][k],d[j][k]);
				}
			}
		}
		return d;
	}
	
	
	public static long[][] warshalFloyd(long[][] A, long INF) {
		long[][]d=ArrayUtils.copy(A);
		int N=d.length;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					if(d[j][i]==INF||d[i][k]==INF)continue;
					d[j][k]=Math.min(d[j][i]+d[i][k],d[j][k]);
				}
			}
		}
		return d;
	}
	
	
	/**
	 * dp[i][j]=fromを始点、iを終点とする長さjのウォークの個数
	 * 未テスト
	 * @param from
	 * @param len
	 * @param g
	 * @param mod
	 * @return
	 */
	public static long[][] modCountWalkFrom(int from, int len, Graph g, long mod) {
		long[][] dp=new long[g.N][len+1];
		dp[from][0]=1;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < g.N; j++) {
				for (int dst:g.adj[j]) {
					dp[dst][i+1]+=dp[j][i];
					if(dp[dst][i+1] >= mod) dp[dst][i+1]-=mod;
				}
			}
		}
		return dp;
	}
	
	/**
	 * dp[i][j]=頂点iを始点とする長さjのウォークの個数
	 * 未テスト
	 * @param len
	 * @param g
	 * @param mod
	 * @return
	 */
	public static long[][] modCountWalkForEachStart(int len, Graph g, long mod) {
		long[][] dp=new long[g.N][len+1];
		for (int i = 0; i < g.N; i++) {
			dp[i][0]=1;
		}
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < g.N; j++) {
				for (int dst:g.adj[j]) {
					dp[dst][i+1]+=dp[j][i];
					if(dp[dst][i+1] >= mod) dp[dst][i+1]-=mod;
				}
			}
		}
		return dp;
	}
	
	/**
	 * Kをターミナル数とする
	 * O(N3^K + 2^N(N+M)logN)
	 * @param g
	 * @param terminals
	 * @return
	 * https://atcoder.jp/contests/abc395/submissions/72815831
	 */
	public static long[][] steinerTree(LongValueGraph g, int[] terminals) {
		int N=g.N;
		int K=terminals.length;
        long[][] dp = new long[1 << K][N];
        long INF = Long.MAX_VALUE / 3;
        ArrayUtils.fill(dp, INF);
        for (int i = 0; i < K; i++) {
            dp[1 << i][terminals[i]] = 0;
        }
        for (int s = 0; s < 1 << K; s++) {
            for (int t = s; t >= 0; t = (t - 1) & s) {
                for (int i = 0; i < N; i++) {
                    dp[s][i] = Math.min(dp[s][i], dp[s ^ t][i] + dp[t][i]);
                }
                if (t == 0) {
                    break;
                }
            }
            PriorityQueue<long[]> pq = new PriorityQueue<long[]>((x, y) -> Long.compare(x[0], y[0]));
            for (int i = 0; i < N; i++) {
                pq.add(new long[]{ dp[s][i], i });
            }
            while (!pq.isEmpty()) {
                long[] state = pq.poll();
                int v = (int) (state[1]);
                long d = state[0];
                if (dp[s][v] != d) {
                    continue;
                }
                for (var e : g.adj[v]) {
                    if (dp[s][e.dst] > (dp[s][v] + e.cost)) {
                        dp[s][e.dst] = dp[s][v] + e.cost;
                        pq.add(new long[]{ dp[s][e.dst], e.dst });
                    }
                }
            } 
        }
        return dp;
	}
	
	
	/**
	 * https://atcoder.jp/contests/abc412/submissions/72805732
	 * @param g
	 * @return
	 */
    Integer minPerfectMatchingValue(LongValueGraph g) {
        Random rnd = new Random();
        long mod = 998244353L;
        if (g.M==0) {
        	if(g.N==0)return 0;
        	else return null;
        }
        int maxCost = Integer.MIN_VALUE;
        for (var e : g.edges()) {
            if (e.cost > Integer.MAX_VALUE) {
            	throw new AssertionError();
            }
            maxCost = Math.max(maxCost, ((int) (e.cost)));
        }
        
        long[][] A = new long[g.N][g.N];
        long[][] x = new long[g.N][g.N];
        for (int i = 0; i < g.N; i++) {
            for (int j = i + 1; j < g.N; j++) {
                x[i][j] = rnd.nextLong(mod);
                x[j][i] = (mod - x[i][j]) % mod;
            }
        }
        Fp fp = Fp.MOD998244353;
        long[] evals = new long[(maxCost * g.N) + 1];
        for (int y = 0; y <= (maxCost * g.N); y++) {
            for (var e : g.edges()) {
                long v = (x[e.src][e.dst] * fp.pow(y, e.cost)) % mod;
                A[e.src][e.dst] = v;
                A[e.dst][e.src] = (mod - v) % mod;
            }
            evals[y] = MatrixUtilsFp.modDeterminant(A, mod);
        }
        long[] xs = new long[(maxCost * g.N) + 1];
        Arrays.setAll(xs, i -> i);
        var f=PolynomialFp.interpolate(xs, evals);
        int ans = 0;
        while ((ans < f.length) && (f[ans] == 0)) {
            ++ans;
        } 
        if (ans == f.length) {
            return null;
        }
        if ((ans % 2) != 0) {
            throw new AssertionError();
        }
        return ans / 2;
    }

    /**
     * 無向グラフの最大独立集合（Maximum Independent Set）を返す。
     * 独立集合とは、集合内のどの 2 頂点も辺で結ばれていないような頂点集合のこと。
     * 最大独立集合は、その中で最大の大きさを持つもの。
     * <h3>計算量</h3>
     * <ul>
     *   <li>時間計算量: O(3^(n/3)) ≈ O(1.44^n)</li>
     *   <li>空間計算量: O(n^2)</li>
     * </ul>
     * @param g 無向グラフ
     * @return 最大独立集合を表す頂点番号の配列
     */
    public static int[] maximumIndependentSet(Graph g) {
        //https://judge.yosupo.jp/submission/372303
    	int n = g.N;
        // 補グラフでの最大クリーク問題として解く
        // 補グラフ：元のグラフで辺がない頂点対に辺があるグラフ
        boolean[][] complementAdj = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(complementAdj[i], true);
            complementAdj[i][i] = false; // 自己ループはなし
        }
        for (int i = 0; i < n; i++) {
            for (int j : g.adj[i]) {
                complementAdj[i][j] = false;
                complementAdj[j][i] = false;
            }
        }
        
        // 補グラフで最大クリークを求める（Bron-Kerbosch アルゴリズム：ピボット選択付き）
        // currentSet: 現在構築中の独立集合（結果候補）
        // candidates: currentSet に追加して独立集合を拡張できる可能性のある頂点の集合
        // excluded: すでに探索済みで、currentSet に追加しない頂点の集合（除外済み）
        List<Integer> bestClique = new ArrayList<>();
        List<Integer> allVertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
        	allVertices.add(i);
        }
        bronKerbosch(new ArrayList<>(), allVertices, new ArrayList<>(), complementAdj, bestClique);
        
        int[] result = new int[bestClique.size()];
        for (int i = 0; i < bestClique.size(); i++) {
            result[i] = bestClique.get(i);
        }
        return result;
    }
    
    /**
     * Bron-Kerbosch アルゴリズム（ピボット選択付き）を用いて極大クリークを探索する。
     * @param currentSet 現在構築中のクリーク
     * @param candidates currentSet に追加可能な頂点の集合
     * @param excluded 探索済みの頂点の集合
     * @param adj 隣接行列
     * @param bestClique 現在までの最大クリーク
     */
    private static void bronKerbosch(List<Integer> currentSet, List<Integer> candidates, List<Integer> excluded, 
                                      boolean[][] adj, List<Integer> bestClique) {
        if (candidates.isEmpty() && excluded.isEmpty()) {
            // currentSet が極大クリーク
            if (currentSet.size() > bestClique.size()) {
                bestClique.clear();
                bestClique.addAll(currentSet);
            }
            return;
        }
        
        if (candidates.size() + currentSet.size() <= bestClique.size()) {
            // 枝刈り：これ以上大きくならない
            return;
        }
        
        // ピボット選択（ヒューリスティック）
        int pivot = -1;
        int maxDegree = -1;
        for (int v : candidates) {
            int degree = 0;
            for (int u : candidates) {
                if (adj[v][u]) degree++;
            }
            if (degree > maxDegree) {
                maxDegree = degree;
                pivot = v;
            }
        }
        
        // candidates \ N(pivot) の頂点に対して再帰
        List<Integer> toExplore = new ArrayList<>();
        for (int v : candidates) {
            if (!adj[pivot][v]) {
                toExplore.add(v);
            }
        }
        
        for (int v : toExplore) {
            List<Integer> newCurrentSet = new ArrayList<>(currentSet);
            newCurrentSet.add(v);
            
            List<Integer> newCandidates = new ArrayList<>();
            List<Integer> newExcluded = new ArrayList<>();
            for (int u : candidates) {
                if (adj[v][u]) newCandidates.add(u);
            }
            for (int u : excluded) {
                if (adj[v][u]) newExcluded.add(u);
            }
            
            bronKerbosch(newCurrentSet, newCandidates, newExcluded, adj, bestClique);
            
            candidates.remove(Integer.valueOf(v));
            excluded.add(v);
        }
    }
    
    
    
    
	/**
	 * グラフを長さ2のパス（3頂点、2辺）に分解する。
	 * 各連結成分において辺の数が奇数の場合、1つの辺が余る。
	 * 未テスト
	 * 計算量: O(N + M)
	 * @param graph
	 * @return 長さ2のパスのリスト。各要素は [u, v, w] (辺 uv と vw)
	 */
	public static List<int[]> decomposeToPathsOfLength2(Graph graph) {
		//https://hitonanode.github.io/cplib-cpp/graph/paths_of_length_two_decomposition.hpp
		List<int[]> result = new ArrayList<>();
		if (graph.N == 0) return result;

		// 1. エッジに一意のID（0 ～ M-1）を付与するための準備
		int sumDeg = 0;
		for (int i = 0; i < graph.N; i++) sumDeg += graph.adj[i].size();

		int[] vidToEid = new int[graph.N];
		Arrays.fill(vidToEid, -1);
		int[] eidToSrc = new int[sumDeg];
		int[] eidToNextEid = new int[sumDeg];//同じ頂点から出る次の辺のインデックス(インデックスは減る）
		int[] eidToBidirectionalEid = new int[sumDeg];//有向辺idから無向辺id
		int edgePtr = 0;
		int idPtr = 0;

		// グラフの隣接リストを辿り、連結リスト構造（head, toArr, nextArr）を構築しつつ、各辺にIDを振る
		for (int u = 0; u < graph.N; u++) {
			for (int i = 0; i < graph.adj[u].size(); i++) {
				int v = graph.adj[u].get(i);
				if (u <= v) { // 無向辺なので u <= v のときだけ処理（多重辺・自己ループも正しくID付与される）
					eidToSrc[edgePtr] = v;
					eidToBidirectionalEid[edgePtr] = idPtr;
					eidToNextEid[edgePtr] = vidToEid[u];
					vidToEid[u] = edgePtr++;

					if (u != v) { // 自己ループでなければ逆方向の辺も追加
						eidToSrc[edgePtr] = u;
						eidToBidirectionalEid[edgePtr] = idPtr;
						eidToNextEid[edgePtr] = vidToEid[v];
						vidToEid[v] = edgePtr++;
					}
					idPtr++; // 新しいエッジID
				}
			}
		}

		boolean[] usedEdge = new boolean[idPtr];
		boolean[] visited = new boolean[graph.N];
		int[] parent = new int[graph.N];
		int[] parentEdge = new int[graph.N];
		int[] bfsOrder = new int[graph.N];
		int bfsCount = 0;

		// 2. グラフの各連結成分についてBFSを行い、BFS木を構築する
		for (int i = 0; i < graph.N; i++) {
			if (!visited[i]) {
				int startBfs = bfsCount;
				java.util.Queue<Integer> queue = new java.util.ArrayDeque<>();
				queue.add(i);
				visited[i] = true;
				parent[i] = -1;
				parentEdge[i] = -1;

				// BFSで頂点を探索し、探索順（bfsOrder）と親ノード・親へ繋がる辺のIDを記録する
				while (!queue.isEmpty()) {
					int u = queue.poll();
					bfsOrder[bfsCount++] = u;

					for (int e = vidToEid[u]; e != -1; e = eidToNextEid[e]) {
						int v = eidToSrc[e];
						int id = eidToBidirectionalEid[e];
						if (!visited[v]) {
							visited[v] = true;
							parent[v] = u;
							parentEdge[v] = id;
							queue.add(v);
						}
					}
				}

				// 3. BFSの逆順（木の下から上へ・葉から根へ）で辺のペアリング処理を行う
				// 逆順で処理することで、親へ繋がる辺を「親へ託す（未処理のまま残す）」か「自分が使う」かを子ノード側で決定できる
				for (int j = bfsCount - 1; j >= startBfs; j--) {
					int u = bfsOrder[j];
					IntArrayList availableNeighbors = new IntArrayList();

					// 自分(u)に接続されている全ての辺のうち、未使用のものを集める
					for (int e = vidToEid[u]; e != -1; e = eidToNextEid[e]) {
						int v = eidToSrc[e];
						int id = eidToBidirectionalEid[e];
						if (id == parentEdge[u]) continue; // 親へ繋がる辺は一旦保留する（必要になった時だけ使う）
						if (!usedEdge[id]) {
							availableNeighbors.add(v);
							usedEdge[id] = true; // この時点で使うことを確定させる
						}
					}

					// 集めた辺を2つずつペアにして長さ2のパスを作る
					while (availableNeighbors.size() >= 2) {
						int v1 = availableNeighbors.pollLast();
						int v2 = availableNeighbors.pollLast();
						result.add(new int[] { v1, u, v2 });
					}

					// もし辺が1つ余っていて、かつ自分に親がいるなら、親へ繋がる辺とペアにする
					// 余りが0個なら、親へ繋がる辺は未使用のまま親の処理(availableNeighbors)へ回される
					if (availableNeighbors.size() == 1 && parent[u] != -1) {
						int v = availableNeighbors.pollLast();
						result.add(new int[] { v, u, parent[u] });
						usedEdge[parentEdge[u]] = true; // 親へ繋がる辺を使ったのでフラグを立てる
					}
				}
			}
		}

		return result;
	}
	
	
	
	
	/**
	 * 無向グラフにおける、辺数が最大で、その中で総重みが最小となる、辺素な2つの全域森を求める。
	 * グラフィックマトロイドの分配（Matroid Partitioning）アルゴリズムに基づき、重みの昇順に辺を走査する貪欲法で構築する。
	 *
	 * マトロイドの性質により、重みの小さい順に「独立性を保ったまま（森である状態を保ったまま）追加または入れ替え」を行うことで、
	 * 最大辺数かつ最小重みの解が得られる。
	 *
	 * @param g 無向グラフ
	 * @return 2つの全域森（LongValueForest）の配列
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(NM)</li>
	 * </ul>
	 */
	public static LongValueForest[] edgeDisjointMinSpanningForests(LongValueGraph g) {
		List<Edge> edges = g.edges();
		// 重みの昇順にソートすることで、マトロイド分配の貪欲法により最小重みが保証される
		Collections.sort(edges);
		return edgeDisjointSpanningForestsInternal(g.N, edges, 2);
	}

	/**
	 * 無向グラフにおける、最大辺数の辺素な2つの全域森を求める。
	 * @param g 無向グラフ
	 * @return 2つの全域森（Forest）の配列
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(NM)</li>
	 * </ul>
	 */
	public static Forest[] edgeDisjointSpanningForests(Graph g) {
		List<int[]> edges = g.edges();
		List<Edge> eList = new ArrayList<>();
		// すべての辺の重みを1として扱うことで最大辺数を求める
		for (int[] e : edges) eList.add(new Edge(e[0], e[1], 1));
		LongValueForest[] res = edgeDisjointSpanningForestsInternal(g.N, eList, 2);
		Forest[] ret = new Forest[2];
		for (int i = 0; i < 2; i++) {
			ret[i] = new Forest(g.N);
			for (Edge e : res[i].edges()) {
				ret[i].addEdge(e.src, e.dst);
			}
		}
		return ret;
	}

	/**
	 * マトロイド分配（Matroid Partitioning）アルゴリズムを用いて、k個の辺素な森を構築する内部メソッド。
	 * グラフィックマトロイドの性質を利用し、全体の辺数を最大化しつつ重みの総和を最小化する。
	 *
	 * アルゴリズムの背景:
	 * - この問題は k 個のグラフィックマトロイドの和マトロイドにおける独立集合を求める問題。
	 * - 重みの小さい辺から順に「現在の独立集合に加えても独立性が保たれるか」を確認し、
	 *   保たれない場合は「交換グラフ」上で増大路を探すことで、貪欲法により最適解が得られる。
	 *
	 * 最適化（計算量 O(NM) の実現）:
	 * - 各辺 e_new = (u, v) について、追加先の森 j の構成要素を u を根とするように向きづける。
	 * - BFS で交換グラフを探索する際、各森において一度走査したノードを `visitedNode` で記録し、
	 *   根（u）に向かって遡ることで、閉路上の未走査の辺のみを効率的にキューに追加する。
	 * - 既に走査済みの頂点（visitedNode）をスキップすることで、各 BFS ステップが $O(N+M)$、全体で $O(NM)$ となる。
	 */
	private static LongValueForest[] edgeDisjointSpanningForestsInternal(int N, List<Edge> edges, int k) {
		int M = edges.size();
		int[] edgeInForest = new int[M];
		Arrays.fill(edgeInForest, -1);
		@SuppressWarnings("unchecked")
		Set<Integer>[] forestEdgeIndices = new HashSet[k];
		for (int i = 0; i < k; i++) forestEdgeIndices[i] = new HashSet<>();

		int[] prevIdx = new int[M];
		int[] fromForest = new int[M];
		int[] visitedEdge = new int[M];
		int[][] visitedNode = new int[k][N + 1];

		for (int i = 0; i < M; i++) {
			Edge e_new = edges.get(i);
			if (e_new.src == e_new.dst) continue;

			LongValueForest[] currentForests = new LongValueForest[k];
			int[][] pEdgeIdx = new int[k][N];
			int[][] rootOf = new int[k][N];
			for (int j = 0; j < k; j++) {
				currentForests[j] = new LongValueForest(N);
				for (int idx : forestEdgeIndices[j]) {
					Edge e = edges.get(idx);
					currentForests[j].addEdge(e.src, e.dst, e.cost);
				}
				// 増大路の探索を効率化するため、一方の端点 e_new.src を根として向きづける
				int[] bfsOrder = currentForests[j].rooted(e_new.src);
				Arrays.fill(pEdgeIdx[j], -1);
				for (int v : bfsOrder) {
					if (currentForests[j].isRoot(v)) rootOf[j][v] = v;
					else rootOf[j][v] = rootOf[j][currentForests[j].parent[v]];
				}
				for (int idx : forestEdgeIndices[j]) {
					Edge e = edges.get(idx);
					if (currentForests[j].parent[e.src] == e.dst) pEdgeIdx[j][e.src] = idx;
					else if (currentForests[j].parent[e.dst] == e.src) pEdgeIdx[j][e.dst] = idx;
				}
				Arrays.fill(visitedNode[j], 0);
				// 根は既に「そこから先」がないため訪問済み扱い
				visitedNode[j][e_new.src] = i + 1;
				// 仮想頂点 N もストッパーとして機能させる
				visitedNode[j][N] = i + 1;
			}

			int timer = i + 1;
			IntArrayList stk = new IntArrayList();
			stk.add(i);
			visitedEdge[i] = timer;
			prevIdx[i] = -1;

			int foundTargetForest = -1;
			int lastIdx = -1;

			bfs: while (!stk.isEmpty()) {
				int currIdx = stk.pollLast();
				Edge e = edges.get(currIdx);

				for (int j = 0; j < k; j++) {
					if (rootOf[j][e.src] != rootOf[j][e.dst]) {
						foundTargetForest = j;
						lastIdx = currIdx;
						break bfs;
					} else {
						// 閉路上の辺を根に向かって走査。既に走査済みの頂点に達したら終了。
						for (int v : new int[]{e.src, e.dst}) {
							int currV = v;
							while (visitedNode[j][currV] != timer) {
								visitedNode[j][currV] = timer;
								int idx = pEdgeIdx[j][currV];
								if (idx != -1 && visitedEdge[idx] != timer) {
									visitedEdge[idx] = timer;
									prevIdx[idx] = currIdx;
									fromForest[idx] = j;
									stk.add(idx);
								}
								currV = currentForests[j].parent[currV];
							}
						}
					}
				}
			}

			if (foundTargetForest != -1) {
				int c = lastIdx;
				int f = foundTargetForest;
				while (c != -1) {
					int next_c = prevIdx[c];
					int next_f = fromForest[c];
					int old_f = edgeInForest[c];
					if (old_f != -1) forestEdgeIndices[old_f].remove(c);
					forestEdgeIndices[f].add(c);
					edgeInForest[c] = f;
					c = next_c;
					f = next_f;
				}
			}
		}

		LongValueForest[] result = new LongValueForest[k];
		for (int j = 0; j < k; j++) {
			result[j] = new LongValueForest(N);
			for (int idx : forestEdgeIndices[j]) {
				Edge e = edges.get(idx);
				result[j].addEdge(e.src, e.dst, e.cost);
			}
		}
		return result;
	}

	/**
	 * 三角形の3頂点を受け取る関数型インターフェース。
	 */
	@FunctionalInterface
	public interface TriConsumer {
		/**
		 * 見つかった三角形の3頂点を受け取り、何らかの処理を行う。
		 * @param u 頂点1
		 * @param v 頂点2
		 * @param w 頂点3
		 */
		void accept(int u, int v, int w);
	}

	/**
	 * グラフに含まれる三角形を列挙し、各三角形に対して work を適用する。
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(N + M√M)</li>
	 * </ul>
	 * @param g 無向グラフ
	 * @param work 三角形の3頂点を受け取る関数
	 */
	public static void enumerateTriangles(Graph g, TriConsumer work) {
		int N = g.N;
		int[] deg = new int[N];
		for (int i = 0; i < N; i++) {
			deg[i] = g.adj[i].size();
		}

		IntArrayList[] oriented = new IntArrayList[N];
		for (int i = 0; i < N; i++) {
			oriented[i] = new IntArrayList();
		}

		for (int i = 0; i < N; i++) {
			for (int jIdx = 0; jIdx < g.adj[i].size(); jIdx++) {
				int j = g.adj[i].get(jIdx);
				if (i < j) {
					// 度数が小さい方から大きい方へ、度数が同じならインデックスが小さい方から大きい方へ向きづける
					if (deg[i] < deg[j] || (deg[i] == deg[j] && i < j)) {
						oriented[i].add(j);
					} else {
						oriented[j].add(i);
					}
				}
			}
		}

		boolean[] flag = new boolean[N];
		for (int i = 0; i < N; i++) {
			for (int jIdx = 0; jIdx < oriented[i].size(); jIdx++) {
				flag[oriented[i].get(jIdx)] = true;
			}
			for (int jIdx = 0; jIdx < oriented[i].size(); jIdx++) {
				int j = oriented[i].get(jIdx);
				for (int kIdx = 0; kIdx < oriented[j].size(); kIdx++) {
					int k = oriented[j].get(kIdx);
					if (flag[k]) {
						work.accept(i, j, k);
					}
				}
			}
			for (int jIdx = 0; jIdx < oriented[i].size(); jIdx++) {
				flag[oriented[i].get(jIdx)] = false;
			}
		}
	}

	/**
	 * 根から各頂点へ到達可能な最小有向全域木（最小重み全域有向樹、arborescence）を求める。
	 * マトロイド交差（グラフィックマトロイドと分割マトロイド）を利用して解く。O(E^2 V)。
	 * @param V 頂点数
	 * @param edges 辺集合
	 * @param root 根となる頂点
	 * @return 最小有向全域木に含まれる辺のインデックスを表すビット配列。全域木が存在しない場合、true の要素数は V-1 未満となる。
	 */
	public static boolean[] minimumDirectedSpanningTreeFromRoot(int V, List<Edge> edges, int root) {
		//https://judge.u-aizu.ac.jp/onlinejudge/review.jsp?rid=11503081#1
		int M = edges.size();
		int[] u = new int[M];
		int[] v = new int[M];
		long[] weights = new long[M];
		IntArrayList[] groups = new IntArrayList[V];
		for (int i = 0; i < V; i++) groups[i] = new IntArrayList();

		for (int i = 0; i < M; i++) {
			Edge e = edges.get(i);
			u[i] = e.src;
			v[i] = e.dst;
			weights[i] = e.cost;
			// 根から外向き: 各頂点(root以外)の入次数が1
			groups[v[i]].add(i);
		}

		int[][] parts = new int[V][];
		int[] capacities = new int[V];
		for (int i = 0; i < V; i++) {
			parts[i] = groups[i].toArray();
			capacities[i] = (i == root) ? 0 : 1;
		}

		GraphicMatroid m1 = new GraphicMatroid(V, u, v);
		PartitionMatroid m2 = new PartitionMatroid(M, parts, capacities);

		return MatroidIntersection.solve(m1, m2, weights);
	}

	/**
	 * 各頂点から根へ到達可能な最小有向全域木（最小重み全域逆有向樹、anti-arborescence）を求める。
	 * 辺の向きを反転させて minimumDirectedSpanningTreeFromRoot に移譲する。
	 * @param V 頂点数
	 * @param edges 辺集合
	 * @param root 根となる頂点
	 * @return 最小有向全域木に含まれる辺のインデックスを表すビット配列。
	 */
	public static boolean[] minimumDirectedSpanningTreeToRoot(int V, List<Edge> edges, int root) {
		List<Edge> reversedEdges = new ArrayList<>(edges.size());
		for (Edge e : edges) {
			reversedEdges.add(new Edge(e.dst, e.src, e.cost));
		}
		return minimumDirectedSpanningTreeFromRoot(V, reversedEdges, root);
	}

	/**
	 * 与えられた無向グラフにおいて、各頂点部分集合 S ⊆ V に対して、
	 * S を頂点集合とする木の個数 f(S) を O(2^N N^2) 時間で列挙する。
	 * Javadoc contract:
	 * <ul>
	 *   <li>返り値 res は長さ 2^N の配列であり、res[S] は頂点集合 S を構成する木の個数 mod mod。res[0]=1に注意。</li>
	 *   <li>計算量: O(2^N N^2)</li>
	 * </ul>
	 * 未テスト
	 * @param g 無向グラフ
	 * @param mod 法
	 * @return 各頂点部分集合 S に対する木の個数の配列
	 */
	public static long[] countSpanningTreeForSubsets(Graph g, long mod) {
		//https://atcoder.jp/contests/abc253/submissions/77450867
		int n = g.N;
		int numSubsets = 1 << n;
		if (n == 0) {
			return new long[]{1};
		}
		if (n == 1) {
			return new long[]{1, 1};
		}

		long[] e = new long[1 << n];
		{
			for (int u = 0; u < n; u++) {
				for (int jIdx = 0; jIdx < g.adj[u].size(); jIdx++) {
					int v = g.adj[u].get(jIdx);
					if (u < v) {
						e[(1 << u) | (1 << v)]++;
					}
				}
			}
			e = BooleanLattice.zeta(e);// e[S] = G[S] の辺数
		}
		
		Fp fp = new Fp(mod);

		IntArrayList[] subsetsByCount = new IntArrayList[n + 1];
		for (int i = 0; i <= n; i++) {
			subsetsByCount[i] = new IntArrayList();
		}
		for (int S = 0; S < numSubsets; S++) {
			subsetsByCount[Integer.bitCount(S)].add(S);
		}

		// f[S] は、S ⊆ V 上の全域木の個数
		long[] f = new long[numSubsets];
		f[0] = 1;
		for (int i = 0; i < n; i++) {
			f[1 << i] = 1;
		}

		// F_hat[k][S] は、サイズ k の木カウント関数 F_k (F_k(T) = f(T) (if |T|　=　k), 0 (otherwise)) の Zeta 変換。
		// 数式定義: F_hat[k][S] = \sum_{T ⊆ S, |T|=k} f(T)
		long[][] F_hat = new long[n + 1][numSubsets];
		// H_hat[k][S] は、重み付き木カウント関数 H_k (H_k(T) = f(T) * e(T) (if |T|　=　k), 0 (otherwise)) の Zeta 変換。
		// 数式定義: H_hat[k][S] = \sum_{T ⊆ S, |T|=k} f(T) * e(T)
		long[][] H_hat = new long[n + 1][numSubsets];

		Arrays.fill(F_hat[0], 1);
		
		for (int i = 0; i < n; i++) {
			F_hat[1][1 << i] = 1;
		}
		F_hat[1] = BooleanLattice.zeta(F_hat[1], mod);
		// H_hat[1] is all 0

		// DP over subset sizes s >= 2
		// P_hat[S] は、F_hat と F_hat の Zeta 空間における点毎の畳み込み（積の総和）。
		// 数式定義: P_hat[S] = \sum_{k=1}^{s-1} F_hat[k][S] * F_hat[s-k][S]
		long[] P_hat = new long[numSubsets];
		// Q_hat[S] は、H_hat と F_hat の Zeta 空間における点毎の畳み込み。
		// 数式定義: Q_hat[S] = \sum_{k=1}^{s-1} H_hat[k][S] * F_hat[s-k][S]
		long[] Q_hat = new long[numSubsets];
		
		for (int s = 2; s <= n; s++) {
			long inv2sMinus1 = fp.inv(2 * (s - 1));

			for (int S = 0; S < numSubsets; S++) {
				long sumP = 0;
				long sumQ = 0;
				for (int k = 1; k < s; k++) {
					sumP = (sumP + F_hat[k][S] * F_hat[s - k][S]) % mod;
					sumQ = (sumQ + H_hat[k][S] * F_hat[s - k][S]) % mod;
				}
				P_hat[S] = sumP;
				Q_hat[S] = sumQ;
			}

			long[] P = BooleanLattice.moebius(P_hat, mod);// P[S] = ∑_T f(T) f(S-T)
			long[] Q = BooleanLattice.moebius(Q_hat, mod);// Q[S] = ∑_T e(T)f(T) f(S-T)

			for (int S : subsetsByCount[s]) {
				long valP = P[S];
				long valQ = Q[S];
				// f(S) = (1/(2(|S| - 1)) * sum f(T)f(S-T)(e(S)-e(T)-e(S-T)) 
				// = e(S)sum f(T)-2 sum e(T)f(T)
				long num = (e[S] * valP - 2 * valQ) % mod;
				if (num < 0) num += mod;
				f[S] = num * inv2sMinus1 % mod;
			}

			if (s < n) {
				long[] F_s = new long[numSubsets];
				long[] H_s = new long[numSubsets];
				for (int j = 0; j < subsetsByCount[s].size(); j++) {
					int S = subsetsByCount[s].get(j);
					F_s[S] = f[S];
					H_s[S] = f[S] * e[S] % mod;
				}
				F_hat[s] = BooleanLattice.zeta(F_s, mod);
				H_hat[s] = BooleanLattice.zeta(H_s, mod);
			}
		}
		return f;
	}

	/**
	 * 与えられた重み付き無向グラフ $G = (V, E)$ において、頂点部分集合 $S \subseteq V$ に対する
	 * 距離空間（metric closure）上の最小全域木（または最小全域森）を求める。辺は非負コストを仮定。
	 *
	 * <p>アルゴリズムとして Multi-source Dijkstra を用いて各頂点に最も近いターミナルを割り当て、
	 * 境界をまたぐ辺を候補辺として収集し、Kruskal 法により最小全域木を構築する。</p>
	 *
	 * <h3>時間計算量</h3>
	 * <ul>
	 *   <li>$O(|E| \log |E|)$</li>
	 * </ul>
	 *
	 * <h3>空間計算量</h3>
	 * <ul>
	 *   <li>$O(|V| + |E|)$</li>
	 * </ul>
	 *
	 * @param g 辺重み付き無向グラフ $G$
	 * @param S ターミナル頂点集合 $S$
	 * @return 構築された最小全域木（または森）を表す {@link LongValueForest} オブジェクト
	 * @throws IllegalArgumentException {@code g} or {@code S} が null の場合、または {@code S} 内の頂点インデックスが範囲外の場合
	 */
	// 未テスト
	public static LongValueForest minimumSpanningTreeOfMetricClosure(LongValueGraph g, int[] S) {
		//https://atcoder.jp/contests/abc250/submissions/77548139
		if (g == null || S == null) {
			throw new IllegalArgumentException("Graph or terminals array cannot be null");
		}
		int N = g.N;
		LongValueForest mst = new LongValueForest(N);

		// ユニークなターミナルを配列に格納

		// Multi-source Dijkstra
		long[] dist = new long[N];
		int[] rootTerminal = new int[N];
		Arrays.fill(dist, Long.MAX_VALUE);
		Arrays.fill(rootTerminal, -1);

		PriorityQueue<long[]> pq = new PriorityQueue<>((x, y) -> Long.compare(x[0], y[0]));
		for (int s : S) {
			dist[s] = 0;
			rootTerminal[s] = s;
			pq.add(new long[]{0, s});
		}

		while (!pq.isEmpty()) {
			long[] state = pq.poll();
			long d = state[0];
			int u = (int) state[1];
			if (d > dist[u]) continue;
			for (Edge e : g.adj[u]) {
				int v = e.dst;
				long nd = d + e.cost;
				if (dist[v] > nd) {
					dist[v] = nd;
					rootTerminal[v] = rootTerminal[u];
					pq.add(new long[]{nd, v});
				}
			}
		}

		// Collect candidate edges
		List<Edge> candidateEdges = new ArrayList<>();
		for (int u = 0; u < N; u++) {
			int rU = rootTerminal[u];
			if (rU == -1) continue;
			for (Edge e : g.adj[u]) {
				int v = e.dst;
				int rV = rootTerminal[v];
				if (rV != -1 && rU != rV) {
					if (rU < rV) {
						long cost = dist[u] + e.cost + dist[v];
						candidateEdges.add(new Edge(rU, rV, cost));
					}
				}
			}
		}

		// Run Kruskal's algorithm
		Collections.sort(candidateEdges);
		UnionFind uf = new UnionFind(N);
		for (Edge e : candidateEdges) {
			if (!uf.equiv(e.src, e.dst)) {
				uf.union(e.src, e.dst);
				mst.addEdge(e.src, e.dst, e.cost);
			}
		}

		return mst;
	}
}
