package library.util.graph.tree;

import library.util.graph.*;


/**
 * @param <Point>
 * @param <Path>
 */
public class STTSolver<Point, Path> {
	//https://atcoder.jp/contests/abc351/submissions/72445737
	//https://atcoder.jp/contests/abc269/submissions/74172466
	StaticTopTree stt;
	STTStrategy<Point, Path> strategy;
	Point[] point;
	Path[] path;

	/**
	 * treeは根付き木
	 * @param tree
	 * @param strategy
	 */
	@SuppressWarnings("unchecked")
	public STTSolver(Tree tree, STTStrategy<Point, Path> strategy) {
		this(new StaticTopTree(tree), strategy);
	}

	public STTSolver(StaticTopTree stt, STTStrategy<Point, Path> strategy) {
		this.stt = stt;
		this.strategy = strategy;
		this.point = (Point[]) new Object[stt.numberOfNodes()];
		this.path = (Path[]) new Object[stt.numberOfNodes()];
		build(stt.sttRoot);
	}
	
	void build(int k) {
		if(stt.leftChild[k] != -1) build(stt.leftChild[k]);
		if(stt.rightChild[k] != -1) build(stt.rightChild[k]);
		update(k);
	}
	
	void update(int k) {
		if (stt.type[k]==StaticTopTree.Type.AddLeaf) {
			path[k]=strategy.createVertex(k); 
		} else if (stt.type[k]==StaticTopTree.Type.AppendVirtualRoot) {
			point[k]=strategy.appendVirtualRoot(path[stt.leftChild[k]]);
		} else if (stt.type[k]==StaticTopTree.Type.ReplaceVirtualRoot) {
			path[k]=strategy.replaceVirtualRoot(point[stt.leftChild[k]], k);
		} else if (stt.type[k]==StaticTopTree.Type.JoinHeavyEdge) {
			if (stt.rightChild[k] == -1) throw new AssertionError("JoinHeavyEdge must have two children.");
			path[k]=strategy.joinHeavyEdge(path[stt.leftChild[k]], path[stt.rightChild[k]]);
		} else if (stt.type[k]==StaticTopTree.Type.MergeVirtualRoot) {
			if (stt.rightChild[k] == -1) throw new AssertionError("MergeVirtualRoot must have two children.");
			point[k]=strategy.mergeVirtualRoot(point[stt.leftChild[k]], point[stt.rightChild[k]]);
		}
	}
	
	public void updateFrom(int v) {
		int cur=v;
		while(cur!=-1) {
			update(cur);
			cur=stt.parent[cur];
		}
	}
	
	public Path getResult() {
		return path[stt.root()];
	}
}