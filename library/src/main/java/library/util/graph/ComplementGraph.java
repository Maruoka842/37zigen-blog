package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;

public class ComplementGraph {
	int N;
	public HashSet<Integer>[] nonAdj;
	
	
	public ComplementGraph(int N) {
		this.N = N;
		nonAdj=new HashSet[N];
		for (int i = 0; i < N; i++) {
			nonAdj[i]=new HashSet<>();
		}
	}
	
	/**
	 * K^nから辺を削除していく
	 * @param a
	 * @param b
	 */
	public void deleteEdge(int a, int b) {
		nonAdj[a].add(b);
		nonAdj[b].add(a);
	}
	
	/**
	 * 
	 * @param src
	 * @return
	 * verified:https://atcoder.jp/contests/abc319/submissions/70920123
	 */
	public int[] distances(int src) {
		Queue<Integer>que=new ArrayDeque<>();
		que.add(src);
		boolean[] blocked=new boolean[N];
		int[]dist=new int[N];
		final int INF=Integer.MAX_VALUE/3;
		Arrays.fill(dist, INF);
		dist[src]=0;
		ArrayList<Integer>list=new ArrayList<>();
		for (int i = 0; i < N; i++) {
			if(i!=src)list.add(i);
		}
		while(!que.isEmpty()) {
			int v=que.poll();
			ArrayList<Integer> nlist=new ArrayList<>();
			for (int u : nonAdj[v]) {
				blocked[u]=true;
			}
			for (int u:list) {
				if (dist[u]!=INF)continue;
				if (blocked[u]) {
					nlist.add(u);
				} else {
					dist[u]=dist[v]+1;
					que.add(u);
				}
			}
			list=nlist;
			for (int u : nonAdj[v]) {
				blocked[u] = false;
			}
		}
		
		return dist;
	}
}
