package library.util.graph;

import library.util.graph.tree.*;

import java.awt.Component;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import library.tools.FastScanner;
import library.util.ArrayUtils;

/**
 * 無向グラフ
 */
public class DoubleValueDAG extends DoubleValueDigraph {
	int[] order;
	boolean isNaturalOrder;
	
	public DoubleValueDAG(int N) {
		super(N);
	}
	
	public static DoubleValueDAG ofNaturalOrder(int N) {
		DoubleValueDAG g = new DoubleValueDAG(N);
		g.isNaturalOrder = true;
		g.order = new int[N];
		for (int i = 0; i < N; i++) g.order[i] = i;
		return g;
	}
	
	@Override
	public void addEdge(int from, int to, double cost) {
		if (isNaturalOrder && from >= to) throw new AssertionError("natural order DAG requires from < to: " + from + " -> " + to);
		super.addEdge(from, to, cost);
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
			for (var e : adj[i]) {
				dp[i]=Math.max(dp[i], dp[e.dst]+1);
			}
		}
		return dp;
	}
	
	/**
	 * dp[s]=sを始点としたときの最長パスコストを返す。
	 * 辺のcostの総和を最大化する。
	 * 長さ0のパスも候補に含む。
	 * 未テスト。
	 * @return
	 */
	public double[] longestPathCostForEachStart() {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		for (int k = N - 1; k >= 0; k--) {
			int i = order[k];
			for (var e : adj[i]) {
				dp[i]=Math.max(dp[i], dp[e.dst]+e.cost);
			}
		}
		return dp;
	}
	
	/**
	 * dp[s]=sを始点、endを終点としたときの最長パスコストを返す。
	 * endに到達できない頂点の値はDouble.NEGATIVE_INFINITY。
	 * dp[end]=0。
	 * @param end
	 * @return
	 */
	public double[] longestPathCostForEachStartFixingEnd(int end) {
		//https://atcoder.jp/contests/abc324/submissions/75180824
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		Arrays.fill(dp, Double.NEGATIVE_INFINITY);
		dp[end]=0;
		for (int k = N - 1; k >= 0; k--) {
			int i = order[k];
			for (var e : adj[i]) {
				if (dp[e.dst] == Double.NEGATIVE_INFINITY) continue;
				dp[i]=Math.max(dp[i], dp[e.dst]+e.cost);
			}
		}
		return dp;
	}
	
	/**
	 * dp[i]=srcを始点としてランダムウォークしたとき頂点iを通る確率
	 * 辺ijのコストcを、iからjに移動する確率と見なして計算する
	 * @param src
	 * @return
	 */
	public double[] visitProbabilityFrom(int src) {
		if (order == null) order = topologicalOrder();
		double[]dp=new double[N];
		dp[src]=1;
		for (int i:order) {
			for (var e: adj[i]) {
				dp[e.dst]+=dp[i]*e.cost;
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
			for (var e : adj[i]) {
				dp[i]+=dp[e.dst]*e.cost;
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
			for (var e: adj[i]) {
				dp[e.dst]+=(dp[i]+f[i])*e.cost;
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
		DoubleValueDAG ig=reverse();
		double[] f=visitProbabilityTo(dst);
		for (int i:ig.order) {
			for (var e: adj[i]) {
				dp[i]+=(dp[e.dst]+f[e.dst])*e.cost;
			}
		}
		return dp;
	}
	
	public DoubleValueDAG reverse() {
		DoubleValueDAG g = new DoubleValueDAG(N);
		for (int i = 0; i < N; ++i) {
			for (var e : adj[i]) {
				g.addEdge(e.dst, i, e.cost);
			}
		}
		if (order == null) order = topologicalOrder();
		g.order = Arrays.copyOf(order, N);
		ArrayUtils.reverse(g.order);
		return g;
	}


	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
