package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator;

public class StaticDigraph {
	public int N;
	public int M;
	public int[] to;
	private int[] outDeg;
	public int[] start;
	
	public StaticDigraph(int N, List<int[]> edges) {
		this.N = N;
		this.M = edges.size();
		to = new int[edges.size()];
		outDeg=new int[N];
		for (int[] e : edges) {
			outDeg[e[0]]++;
		}
		start = new int[N];
		for (int i = 1; i < outDeg.length; i++) {
			start[i] += start[i - 1] + outDeg[i - 1];
		}
		int[] id = Arrays.copyOf(start, start.length);
		for (int[] e : edges) {
			to[id[e[0]]] = e[1];
			id[e[0]]++;
		}
	}
	
	
	public StaticDigraph(int N, List<int[]> edges, boolean reversed) {
		this.N = N;
		this.M = edges.size();
		to = new int[edges.size()];
		outDeg=new int[N];
		for (int[] e : edges) {
			if (!reversed) {
				outDeg[e[0]]++;
			}  else {
				outDeg[e[1]]++;
			}
		}
		start = new int[N];
		for (int i = 1; i < outDeg.length; i++) {
			start[i] += start[i - 1] + outDeg[i - 1];
		}
		int[] id = Arrays.copyOf(start, start.length);
		for (int[] e : edges) {
			if (!reversed) {
				to[id[e[0]]] = e[1];
				id[e[0]]++;
			} else {
				to[id[e[1]]] = e[0];
				id[e[1]]++;
			}
		}
	}
	
	public int outDegree(int i) {
		return outDeg[i];
	}
	
	public int[] outDegrees() {
		return Arrays.copyOf(outDeg, N);
	}
	
	public StaticDigraph reverse() {
		ArrayList<int[]> edges = new ArrayList<>(M);
		for (int i = 0; i < N; i++) {
			for (int v : adj(i)) {
				edges.add(new int[] {v, i});
			}
		}
		return new StaticDigraph(N, edges);
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
	
	
	@FunctionalInterface
	public interface IntIterable {
		PrimitiveIterator.OfInt iterator();
	}
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
