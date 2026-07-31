package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;

public class MinCut {
	MaxFlow mf;
	int N;
	int inputN;
	int source, sink;
	long[] trueValue;
	long[] falseValue;
	long base=0;
	ArrayList<long[]> edges;
	final long INF=Long.MAX_VALUE/3;
	
	public MinCut(int N) {
		this.N=N;
		this.inputN=N;
		trueValue=new long[N];
		falseValue=new long[N];
		edges=new ArrayList<>();
	}
	
	// source側, sink側をそれぞれ False, True とする
	
	public void forceTrue(int a) {
		if (trueValue[a] == INF) throw new AssertionError();
		falseValue[a] = INF;
	}
	
	public void forceFalse(int a) {
		if (falseValue[a] == INF) throw new AssertionError();
		trueValue[a] = INF;
	}
	
	/**
	 * (a ⇒ b)という条件を課す。
	 * @param a
	 * @param b
	 */
	public void ifThen(int a, int b) {
		//!(!b ⋀ a) ⇔ b ∨ !a ⇔ (a ⇒ b)
		addEdge(b, a, INF);
	}
	
	/**
	 * !a ⋀ b ならばcostを加える。
	 * @param a
	 * @param b
	 */
	public void addPositiveCostIfFalseTrue(int a, int b, long cost) {
		if (cost < 0) throw new AssertionError();
		//!a ⋀ b ⇔ (a ⇒ b)
		addEdge(a, b, cost);
	}
	
	void addEdge(int a, int b, long cost) {
		edges.add(new long[] {a, b, cost});
	}
	
	
	/**
	 * cost > 0 ならばエラー
	 * @param a
	 * @param b
	 */
	public void addNegativeCostIfTrueTrue(int a, int b, long cost) {
		//https://atcoder.jp/contests/abc225/submissions/71972434
		//https://atcoder.jp/contests/abc347/submissions/72267257
		/*
		* a ⋀ b ならばcost(<0)を加える。
		 * = 事前にcostを足しておき、!a ∨ !b ならば-costを加える
		 * = 事前にcostを足しておき、(!a ⇒ p) ⋀ (!b ⇒ p) の下、p ならば -cost を加える
		 * = 事前にcostを足しておき、(!p ⇒ a) ⋀ (!p ⇒ b) の下、p ならば -cost を加える
		 * = 事前にcostを足しておき、(q ⇒ a) ⋀ (q ⇒ b) の下、!q ならば -cost を加える
		 */
		if (cost > 0) throw new AssertionError();
		if (cost == 0) return;
		base += cost;
		addNode();
		int q = N - 1;
		ifThen(q, a);
		ifThen(q, b);
		addCostIfFalse(q, -cost);
	}
	
	/**
	 * cost > 0 ならばエラー
	 * @param variables
	 * @param cost
	 */
	public void addNegativeCostIfAllTrue(int[] variables, long cost) {
		//https://atcoder.jp/contests/abc326/submissions/72312971
		/*
		 * variables = {v1, v2, ..., vk}
		 * v1 ∧ v2 ∧ ... ∧ vk ならばcost(<0)を加える。
		 * = 事前にcostを足しておき、!v1 ∧ !v2 ∧ ... ∧ !vk ならば-costを加える
		 * = 事前にcostを足しておき、(q ⇒ v1) ⋀ (q ⇒ v1) ⋀ ... ⋀ (q ⇒ vk)  の下、!q ならば -cost を加える
		 */
		if (cost > 0) throw new AssertionError();
		if (cost == 0) return;
		base += cost;
		addNode();
		int q = N - 1;
		for (int v : variables) {
			ifThen(q, v);
		}
		addCostIfFalse(q, -cost);
	}

	
	
	void addNode() {
		if (N+1>=falseValue.length) {
			falseValue=Arrays.copyOf(falseValue, 2*N);
			trueValue=Arrays.copyOf(trueValue, 2*N);
		}
		N++;
	}
	

	
	/**
	 * aが真ならばcostを加える
	 * https://atcoder.jp/contests/abc225/submissions/71972434
	 * @param a
	 */
	public void addCostIfTrue(int a, long cost) {
		trueValue[a]+=cost;
	}
	
	/**
	 * aが偽ならばcostを加える
	 * https://atcoder.jp/contests/abc274/submissions/72104036
	 * @param a
	 */
	public void addCostIfFalse(int a, long cost) {
		falseValue[a]+=cost;
	}
	
	/**
	 *  O(N^2 M). INFを除いた平均辺容量kのときO(kM^{1.5})
	 * @return
	 */
	public long minCutValue() {
		mf=new MaxFlow(N+2);
		source=N;
		sink=N+1;
		for (int i = 0; i < N; i++) {
			long min=Math.min(trueValue[i], falseValue[i]);
			base+=min;
			trueValue[i]-=min;
			falseValue[i]-=min;
			if(trueValue[i]!=0) {
				mf.addEdge(source, i, trueValue[i]);
			}
			if (falseValue[i]!=0) {
				mf.addEdge(i, sink, falseValue[i]);
			}
		}
		for (var e : edges) {
			mf.addEdge((int)e[0], (int)e[1], e[2]);
		}
		return mf.maxFlowValue(source, sink, INF)+base;
	}
	
	public boolean[] restoreMinCut() {
		//https://atcoder.jp/contests/abc347/submissions/74038394
		boolean[] ret=mf.reachableFromSourceOnResidualNetwork();//source, sinkが付け足されているので、長さN+2
		for (int i = 0; i < N; i++) {
			ret[i]=!ret[i];
		}
		ret=Arrays.copyOf(ret, inputN);
		return ret;
	}
}
