package library.util.graph.tree;

import library.util.graph.*;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.graph.tree.Tree.DpStrategy;

public class TreedpFactory {
	/**
	 * 頂点vの重みをA[v]とする木上の最大重み独立集合（Maximum Weighted Independent Set; MWIS）について、
	 * 「独立集合のサイズごとの最大重み」を計算
	 *
	 * <p>
	 * 各頂点 v に対して、次の DP テーブルを構築。
	 * 
	 * <pre>
	 *   dp[v][0][k] … v を選ばない場合における、
	 *                  「v を根とする部分木内でサイズ k の独立集合の最大重み」
	 *   dp[v][1][k] … v を選ぶ場合における、
	 *                  「v を根とする部分木内でサイズ k の独立集合の最大重み」
	 * </pre>
	 * <p>
	 *
	 * @param tree
	 * @param A
	 * @return
	 */
	public static long[][][] maximumWeightedIndependentSet(Tree tree, long[] A) {
		long INF = Long.MAX_VALUE / 3;
		var dp = tree.treeDP(new DpStrategy<long[][]>() {
			@Override
			public long[][] merge(long[][] a, long[][] b) {
				long[][] ret = new long[2][a[0].length + b[0].length - 1];
				ArrayUtils.fill(ret, -INF);
				for (int used = 0; used <= 1; used++) {
					for (int i = 0; i < a[used].length; i++) {
						if (a[used][i] == -INF)
							continue;
						for (int used2 = 0; used2 <= 1; used2++) {
							if (used == 1 && used2 == 1)
								continue;
							for (int j = 0; j < b[used2].length; j++) {
								if (b[used2][j] == -INF)
									continue;
								ret[used][i + j] = Math.max(ret[used][i + j], a[used][i] + b[used2][j]);
							}
						}
					}
				}
				return ret;
			}

			@Override
			public long[][] single(int v) {
				long[][] a = new long[2][2];
				ArrayUtils.fill(a, -INF);
				a[0][0] = 0;
				a[1][1] = A[v];
				return a;
			}
		});
		return dp;
	}

	/**
	 * 木の頂点をいくつか選んだとき、その隣接頂点（選んだ頂点自身も含む）の集合のサイズがkになるものの数え上げ。
	 * [部分木の根][根が覆われているか][根が直接覆われているか][覆われた頂点数]という状態で返す。
	 * https://atcoder.jp/contests/abc207/tasks/abc207_f
	 */
	public static long[][][][] vertexNeighborCover(Tree tree) {
		long[][][] single = new long[2][2][2];// [根が覆われているか][根が直接覆われているか][覆われた頂点数]
		single[0][0][0] = 1;
		single[1][1][1] = 1;

		return tree.treeDP(v -> single, (x, y) -> {
			int xsz = x[0][0].length;
			int ysz = y[0][0].length;
			long[][][] ret = new long[2][2][xsz + ysz - 1];
			for (int cover = 0; cover <= 1; cover++) {
				for (int dircover = 0; dircover <= 1; dircover++) {
					for (int cover2 = 0; cover2 <= 1; cover2++) {
						for (int dircover2 = 0; dircover2 <= 1; dircover2++) {
							for (int size = 0; size < xsz; size++) {
								for (int size2 = 0; size2 < ysz; size2++) {
									long par = x[cover][dircover][size];
									long ch = y[cover2][dircover2][size2];
									if (par == 0 || ch == 0)
										continue;
									int nsize = size + size2;
									if (dircover == 1 && cover2 == 0)
										nsize++;
									if (dircover2 == 1 && cover == 0)
										nsize++;
									ret[cover | dircover2][dircover][nsize] += par * ch;
								}
							}
						}
					}
				}
			}
			return ret;
		});
	}

	/**
	 * 木の各辺を向きづけるコストの最小値を計算する。
	 *
	 * <p>
	 * 各頂点 v について、その入次数が k であるときのコスト f(v, k) = costs[v][k] が与えられる。
	 * 全ての辺をどちらかの向きに向きづけたときの、全頂点のコストの総和の最小値を求める。
	 * </p>
	 *
	 * @param tree
	 * @param costs costs[v][k] は頂点 v の入次数が k のときのコスト
	 * @return 最小コスト
	 */
	public static long minimumCostOrientation(Tree tree, long[][] costs) {
		int N = tree.N;
		if (N == 0)
			return 0;
		if (!tree.isRooted())
			tree.rooted(0);
		int[] order = tree.postOrder();
		long[][] dp = new long[N][2];
		long INF = Long.MAX_VALUE / 3;

		for (int v : order) {
			int m = tree.childs[v].size();
			long[] diffs = new long[m];
			long baseCost = 0;
			for (int i = 0; i < m; i++) {
				int u = tree.childs[v].get(i);
				baseCost += dp[u][1];
				diffs[i] = dp[u][0] - dp[u][1];
			}
			Arrays.sort(diffs);
			long[] prefixSum = ArrayUtils.prefixSumFromZERO(diffs);

			// dp[v][0]: 頂点 v とその親 p の間の辺が v -> p と向きづけられている（v の入次数に寄与しない）
			// dp[v][1]: 頂点 v とその親 p の間の辺が p -> v と向きづけられている（v の入次数が 1 増える）
			for (int b = 0; b <= 1; b++) {
				if (v == tree.root() && b == 1) {
					dp[v][b] = INF;
					continue;
				}
				long minV = INF;
				for (int j = 0; j <= m; j++) {
					// v の入次数は、子からの入辺数 j と親からの入辺数 b の和
					if (j + b < costs[v].length) {
						minV = Math.min(minV, costs[v][j + b] + prefixSum[j]);
					}
				}
				dp[v][b] = baseCost + minV;
			}
		}
		return dp[tree.root()][0];
	}

}
