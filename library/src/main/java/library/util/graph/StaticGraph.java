package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PrimitiveIterator;

public class StaticGraph {
	public int N;
	public int M;
	private int[] to;
	private int[] deg;
	private int[] start;
	ArrayList<int[]> edges;
	
	public StaticGraph(int N) {
		this.N = N;
		edges=new ArrayList<>();
	}
	
	public void setN(int N) {
		this.N = N;
	}
	
	public void addEdge(int a, int b) {
		edges.add(new int[] {a, b});
	}
	
	/**
	 * 自己辺は未対応
	 */
	public void build() {
		this.M = edges.size();
		to = new int[2 * edges.size()];
		deg=new int[N];
		for (int[] e : edges) {
			deg[e[0]]++;
			deg[e[1]]++;
		}
		start = new int[N];
		for (int i = 1; i < deg.length; i++) {
			start[i] += start[i - 1] + deg[i - 1];
		}
		int[] id = Arrays.copyOf(start, start.length);
		for (int[] e : edges) {
			to[id[e[0]]] = e[1];
			id[e[0]]++;
			to[id[e[1]]] = e[0];
			id[e[1]]++;
		}
	}
	
	
	public int deg(int i) {
		return deg[i];
	}
	
	public Iterable<Integer> adj(int v) {
		int begin = start[v];
		int end = (v + 1 < N ? start[v + 1] : M);
		return () -> new PrimitiveIterator.OfInt() {
			int cur = begin;
			
			@Override
			public boolean hasNext() {
				return cur < end;
			}
			
			@Override
			public int nextInt() {
				return to[cur++];
			}
		};
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
