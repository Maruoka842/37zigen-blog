package library.util.graph;

public class DoubleValueEdge implements Comparable<DoubleValueEdge> {
	public int src;
	public int dst;
	public double cost;

	public DoubleValueEdge(int src, int dst, double cost) {
		this.src = src;
		this.dst = dst;
		this.cost = cost;
	}
	
	public int compareTo(DoubleValueEdge o) {
		if (cost != o.cost) return Double.compare(cost, o.cost);
		else return Integer.compare(dst, o.dst);
	}

}
