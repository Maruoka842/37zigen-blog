package library.util.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SparseGraph{
	private Set<Integer> vertices;
	private Map<Integer, Set<Integer>> adj;
	
	public SparseGraph() {
		vertices = new HashSet<>();
		adj = new HashMap<>();
	}
	
	public int N() {
		return vertices.size();
	}
	
	public Set<Integer> adjacents(int v) {
		return adj.get(v);
	}
	
	public Set<Integer> vertices() {
		return vertices;
	}
	
	public void addVertex(int v) {
		if (vertices.contains(v)) throw new AssertionError();
		vertices.add(v);
		adj.put(v, new HashSet<>());
	}
	
	public void addEdge(int u, int v) {
		var setU=adj.get(u);
		setU.add(v);
		if (u == v) return;
		var setV=adj.get(v);
		setV.add(u);
	}
	
	public void removeVertex(int v) {
		for (int u:adj.get(v)) {
			if (u != v) adj.get(u).remove(v);
		}
		vertices.remove(v);
		adj.remove(v);
	}
	
	public void removeEdge(int u, int v) {
		adj.get(u).remove(v);
		adj.get(v).remove(u);
	}
	
	/**
	 * 頂点u,vを縮約する。
	 * 多重辺は多重でない辺に置き換えられる。
	 * @param u
	 * @param v
	 */
	public void contractThenRemoveMultipleEdge(int u, int v) {
		for (int x:adj.get(Math.max(u, v))) {
			addEdge(x, Math.min(u, v));
		}
		removeVertex(Math.max(u, v));
	}
	
	public SparseGraph clone() {
		SparseGraph g=new SparseGraph();
		for(int v:vertices) {
			g.vertices.add(v);
			g.adj.put(v, new HashSet<>());
			
		}
		for (var es:adj.entrySet()) {
			int u=es.getKey();
			for (int v:es.getValue()) {
				if(u<=v) {
					g.addEdge(u, v);
				}
			}
		}
		return g;
	}
	
	public int deg(int v) {
		return adj.get(v).size();
	}
}
