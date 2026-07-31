package library.util.graph;

public class Edge implements Comparable<Edge> {
	public int src;
	public int dst;
	public long cost;

	public Edge(int src, int dst, long cost) {
		this.src = src;
		this.dst = dst;
		this.cost = cost;
	}
	
	@Override
	public int compareTo(Edge o) {
		if (cost != o.cost) return Long.compare(cost, o.cost);
		else return Integer.compare(dst, o.dst);
	}
}
