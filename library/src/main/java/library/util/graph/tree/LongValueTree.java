package library.util.graph.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.util.MathUtils;
import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;
import library.util.graph.Edge;
import library.util.graph.FunctionalGraph;

public class LongValueTree extends LongValueForest {
	int pointer = 0;
	int root = -1; //未定
	

	public LongValueTree(int N) {
		super(N);
	}

	public int root() {
		return root;
	}
	
	
	/**
	 * BFSorderを返す。
	 * @param root
	 * @return
	 */
	public int[] rooted(int root) {
		if (root>=N)throw new AssertionError("存在しない頂点をルートに設定している");
		this.root =root;
		isRooted=true;
		parent = new int[N + 1];
		parentEdgeCost = new long[N + 1];
		depth = new int[N + 1];
		weightedDepth = new long[N + 1];
		size = new int[N + 1];
		Arrays.fill(parent, N);
		Arrays.fill(size, 1);
		childs = new IntArrayList[N + 1];
		for (int i = 0; i < N + 1; i++) {
			childs[i]=new IntArrayList();
		}
		parent[root] = N;
		depth[root] = 0;
		depth[N] = -1;
		pointer = 0;
		IntDeque que=new IntDeque();
		que.addLast(root);
		bfsOrder = new int[N];
		while(!que.isEmpty()) {
			int v = que.pollFirst();
			bfsOrder[pointer++]=v;
			for (Edge e: adj[v]) {
				if (e.dst == parent[v]) continue;
				childs[v].add(e.dst);
				que.addLast(e.dst);
				parent[e.dst] = v;
				parentEdgeCost[e.dst] = e.cost;
				depth[e.dst] = depth[v] + 1;
				weightedDepth[e.dst] = weightedDepth[v] + e.cost;
			}
		}
	    for (int i = N - 1; i >= 1; i--) {
	        int v = bfsOrder[i];
	        size[parent[v]] += size[v];
	    }
		preOrder = null;
		inv_preOrder = null;
		fg = null;
		return bfsOrder;
	}
	
	public int[] bfsOrder() {
		if(!isRooted)throw new AssertionError();
		return bfsOrder;
	}
	
	/**
	 * rootが設定されているときは、srcがparent側になるようにする。
	 */
	public List<Edge> edges() {
		List<Edge>ret=new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (var e : adj[i]) {
				if (root != -1) {
					if (e.src == parent[e.dst]) {
						ret.add(e);
					}
				} else {
					if (i < e.dst) {
						ret.add(e);
					}
				}
			}
		}
		return ret;
	}
	
	
	
	/**
	 * v <- parent[v] を level 回した v を返す。
	 * ただし、parent[root]=root
	 * @param v
	 * @param level
	 * @return
	 */
	public int getLevelAncestor(int v, int level) {
		if (fg == null) {
			buildFunctionalGraph();
		}
		return fg.getLevelAncestor(v, level);
	}
	
	/**
	 * v <- parent[v] を 2^level 回した v を返す。
	 * @param v
	 * @param level
	 * @return
	 */
	public int getPower2LevelAncestor(int v, int loglevel) {
		if (fg == null) {
			buildFunctionalGraph();
		}
		return fg.getLevelAncestor(v, 1<<loglevel);
	}
	
	
	void buildFunctionalGraph() {
		if (!isRooted) throw new AssertionError("root is undefined");
		var functionalParent=new int[N+1];
		for (int i = 0; i < N; i++) {
			functionalParent[i]=parent[i];
		}
		functionalParent[root]=root;
		fg = new FunctionalGraph(functionalParent);
		fg.buildDoubling(MathUtils.floorLog2(N)+1);
	}

	@Override
	void buildDoubling() {
		buildFunctionalGraph();
	}
	
}
