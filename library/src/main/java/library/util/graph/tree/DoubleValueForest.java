package library.util.graph.tree;

import library.util.graph.*;

import library.tools.FastScanner;

public class DoubleValueForest extends DoubleValueGraph {

	public DoubleValueForest(int N) {
		super(N);
	}

	@Override
	public void addEdge(int u, int v, double cost) {
		super.addEdge(u, v, cost);
		if (u == v) throw new AssertionError("森で自己ループは禁止");
	}

	public static DoubleValueForest read(int N, int M) {
		DoubleValueForest forest = new DoubleValueForest(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; i++) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			double c = sc.nextDouble();
			forest.addEdge(a, b, c);
		}
		return forest;
	}
}
