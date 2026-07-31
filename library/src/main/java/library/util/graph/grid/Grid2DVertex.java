package library.util.graph.grid;

public class Grid2DVertex {
	public int i, j;
	public char c;
	private Grid2D g;
	
	public Grid2DVertex(int i, int j, char c, Grid2D g) {
		this.i = i;
		this.j = j;
		this.c = c;
		this.g=g;
	}
	
	public int dist(Grid2DVertex v) {
		return Math.abs(i-v.i)+Math.abs(j-v.j);
	}
	
	public int id() {
		return i*g.W+j;
	}
	
	@Override
	public String toString() {
		return "("+i+","+j+")";
	}
}
