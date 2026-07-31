package library.util.graph.tree;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.MathUtils;
import library.util.collections.HashStrategies;
import library.util.collections.IntArrayList;
import library.util.collections.OpenHashMap;
import library.util.graph.FunctionalGraph;
import library.util.seq.Permutation;
import library.util.unionfind.IntVertexValueUnionFind;
import library.util.unionfind.VertexValueUnionFindFactory;

public class Tree extends Forest {
	int[] preOrder;
	int[] postOrder;
	int[] inv_preOrder;//inv_preOrder[preOrder[i]]=i
	int[] inv_postOrder;//inv_postOrder[postOrder[i]]=i
	int pointer = 0;
	int root = -1; //未定
	
	public boolean isRooted() {
		return root != -1;
	}
	
	public int root() {
		return root;
	}
	
	public Tree(int N) {
		super(N);
	}
	
	/**
	 * 長さN-2のプルファーコードから頂点数Nの木を返す
	 * @param prufer
	 * @return
	 */
	public static Tree pruferToTree(int[] prufer) {
		int N=prufer.length+2;
		int[]numChilds=new int[N];
		for (int i = 0; i < prufer.length; i++) {
			numChilds[prufer[i]]++;
		}
		numChilds[0]++;
		TreeSet<Integer>leafs=new TreeSet<>();
		for (int i = 0; i < N; i++) {
			if (numChilds[i] == 0) {
				leafs.add(i);
			}
		}
		Tree tree=new Tree(N);
		for (int i = 0; i < N-1; i++) {
			int leaf=leafs.pollFirst();
			int parent=(i==N-2?0:prufer[i]);
			numChilds[parent]--;
			tree.addEdge(leaf, parent);
			if (numChilds[parent]==0) leafs.add(parent);
		}
		return tree;
	}
	
	public static Tree randomTree(int N) {
		int[] prufer=ArrayUtils.randomIntArray(0, N, N-2);
		return pruferToTree(prufer);
	}

	/**
	 * 頂点数 $n$ のパスグラフを返す。
	 * 辺集合は $E = \{(i, i+1) \mid 0 \le i < n-1\}$。
	 * O(n)
	 * @param n 頂点数
	 * @return 頂点数 $n$ のパスグラフ
	 */
	public static Tree path(int n) {
		Tree tree = new Tree(n);
		for (int i = 0; i < n - 1; i++) {
			tree.addEdge(i, i + 1);
		}
		return tree;
	}

	/**
	 * 頂点数 $n$ のスターグラフを返す。
	 * 辺集合は $E = \{(0, i) \mid 1 \le i < n\}$。
	 * O(n)
	 * @param n 頂点数
	 * @return 頂点数 $n$ のスターグラフ
	 */
	public static Tree star(int n) {
		Tree tree = new Tree(n);
		for (int i = 1; i < n; i++) {
			tree.addEdge(0, i);
		}
		return tree;
	}

	public static Tree read(int N) {
	    FastScanner fs=FastScanner.getInstance();
		Tree t = new Tree(N);
	    for (int i = 0; i < N - 1; i++) {
	        int a = fs.nextInt() - 1;
	        int b = fs.nextInt() - 1;
	        t.addEdge(a, b);
	    }
	    return t;
	}
	/**
	    void dfs(int v) {
		   for (u : childs[v]) {
				dfs(u);
			}
			postOrder[pointer++]=v;
		}

	 * @return
	 */
	public int[] postOrder() {
		if (root == -1) throw new AssertionError("rootedを呼んでいない");
		if (postOrder == null) {
			postOrder = new int[N];
			pointer = 0;
			_dfs_postOrder(root);
		}
		return postOrder;
	}
	
	private void _dfs_postOrder(int v) {
		for (int u : childs[v]) {
			_dfs_postOrder(u);
		}
		postOrder[pointer++]=v;
	}
	
	/**
	 void dfs(int v) {
		preOrder[pointer++]=v;
		for (u : childs[v]) {
		  dfs(u);
		}
      }
	 * @return
	 */
	public int[] preOrder() {
		if (root == -1) throw new AssertionError("rootedを呼んでいない");
		if (preOrder == null) {
			preOrder = new int[N];
			pointer = 0;
			_dfs_preOrder(root);
		}
		return preOrder;
	}
	
	/**
	 * 頂点vのpreOrder
	 * @param v
	 * @return
	 * https://atcoder.jp/contests/abc329/submissions/72850244
	 */
	public int preOrderOf(int v) {
		if (inv_preOrder == null) {
			if (preOrder == null) preOrder();
			inv_preOrder=Permutation.inverse(preOrder);
		}
		return inv_preOrder[v];
	}

	private void _dfs_preOrder(int v) {
		preOrder[pointer++]=v;
		for (int u : childs[v]) {
			_dfs_preOrder(u);
		}
	}
	
	/**
	 * 頂点vのpreOrder
	 * @param v
	 * @return
	 */
	public int postOrderOf(int v) {
		if (inv_postOrder == null) {
			if (postOrder == null) postOrder();
			inv_postOrder=Permutation.inverse(postOrder);
		}
		return inv_postOrder[v];
	}
	
	


	
	/**
	 * BFSorderを返す。
	 * @param root
	 * @return
	 */
	public int[] rooted(int root) {
		this.root = root;
		preOrder = null;
		postOrder = null;
		inv_preOrder = null;
		inv_postOrder = null;
		fg = null;

		parent = new int[N];
		depth = new int[N];
		size = new int[N];
		Arrays.fill(parent, N);
		childs = new IntArrayList[N];
		for (int i = 0; i < childs.length; i++) {
			childs[i] = new IntArrayList();
		}
		depth[root] = 0;
		pointer = 0;
		Queue<Integer> que = new ArrayDeque<>();
		que.add(root);
		bfsOrder = new int[N];

		while (!que.isEmpty()) {
			int v = que.poll();
			bfsOrder[pointer++] = v;
			for (int next : adj[v]) {
				if (next == parent[v])
					continue;
				childs[v].add(next);
				que.add(next);
				parent[next] = v;
				depth[next] = depth[v] + 1;
			}
		}
		Arrays.fill(size, 1);
		for (int i = N - 1; i >= 1; --i) {
			int v = bfsOrder[i];
			size[parent[v]] += size[v];
		}
		return bfsOrder;
	}
	


	
	
	/**
	 * merge(木, 部分木）
	 * @param <T>
	 * @param single
	 * @param merge
	 * @return
	 */
	public <T> T[] treeDP(
			IntFunction<T> single,
			BinaryOperator<T> merge
		) {
		@SuppressWarnings("unchecked")
		T[] dp=(T[]) Array.newInstance(single.apply(root).getClass(), N);
		if(root==-1)throw new AssertionError();
		dfs(root, N, dp, single, merge);
		return dp;
	}

	public <T> T[] treeDP(DpStrategy<T> strategy) {
		return treeDP(strategy::single, strategy::merge);
	}
	
	public interface DpStrategy<T> {
		T single(int v);
		T merge(T parentTree, T childTree);
	}
	
	
	private <T> T dfs(
			int v, int p, T[] dp,
			IntFunction<T> single,
			BinaryOperator<T> merge
	) {
		T acc = single.apply(v);
		for (int u : adj[v]) {
			if (u == p) continue;
			T sub = dfs(u, v, dp, single, merge);
			acc = merge.apply(acc, sub);
		}
		return dp[v] = acc;
	}
	
	public interface RerootStrategy<T> {
		T merge(T leftTree, T rightTree);
		T up(T childsProduct, int v);
		T leaf(int v);
	}
	
	public static final class RerootResult<T> {
	    public final T[] lower;
	    public final T[] upper;
	    public final T[] branch;
	    public final T[] reroot;

	    public RerootResult(T[] lower, T[] upper, T[] branch, T[] reroot) {
	        this.lower = lower;
	        this.upper = upper;
	        this.branch = branch;
	        this.reroot = reroot;
	    }
	}

	/**
	 * @param <T>
	 * @param st
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> RerootResult<T> rerootDP(RerootStrategy<T> st) {
		//https://atcoder.jp/contests/abc223/submissions/73256170
		//https://atcoder.jp/contests/abc348/submissions/74165000
		//https://atcoder.jp/contests/s8pc-4/submissions/76928200
		if(root==-1)throw new AssertionError("rootが未定義");
		T[] lower=(T[]) Array.newInstance(st.leaf(0).getClass(), N);
		T[] upper=(T[]) Array.newInstance(st.leaf(0).getClass(), N);
		T[] branch=(T[]) Array.newInstance(st.leaf(0).getClass(), N);
		T[] reroot=(T[]) Array.newInstance(st.leaf(0).getClass(), N);
		int[] order=bfsOrder();
		
		BinaryOperator<T> merge=new BinaryOperator<T>() {
			@Override
			public T apply(T t, T u) {
				if (t==null) return u;
				if (u==null) return t;
				return st.merge(t, u);
			}
		};
		
		BiFunction<T, Integer, T> up=new BiFunction<>() {
			@Override
			public T apply(T t, Integer v) {
				if (t == null) return st.leaf(v);
				else return st.up(t, v);
			}
		};
		
		ArrayUtils.reverse(order);
		for (int i : order) {
			lower[i] = null;
			for (int c : childs[i]) {
				lower[i] = merge.apply(lower[i], branch[c]);
			}
			branch[i] = up.apply(lower[i], i);
		}
		ArrayUtils.reverse(order);
		for (int i : order) {
			for (int j = childs[i].size() - 2; j >= 0; j--) {
				upper[childs[i].get(j)] = merge.apply(branch[childs[i].get(j + 1)], upper[childs[i].get(j + 1)]);
			}
			T prefix = null;
			for (int j = 0; j < childs[i].size(); j++) {
				upper[childs[i].get(j)] = up.apply(merge.apply(upper[i], merge.apply(prefix, upper[childs[i].get(j)])), i);
				prefix = merge.apply(prefix, branch[childs[i].get(j)]);
			}
		}
		for (int i = 0; i < N; i++) {
			reroot[i]=up.apply(merge.apply(lower[i], upper[i]), i);
		}
		return new RerootResult<T>(lower, upper, branch, reroot);
	}
	
	/**
	 * 根付き木の同型類分類（部分木のみ）。
	 * 各頂点 v について、v を根とする部分木の同型類 ID を返す。
	 * O(N log N)
	 * //未テスト
	 */
	public int[] isomorphismClassificationOnlyBranches() {
		return isomorphismClassificationOnlyBranches(new OpenHashMap<int[], Integer>(HashStrategies.INT_ARRAY));
	}

	/**
	 * 根付き木の同型類分類（部分木のみ）。
	 * 各頂点 v について、v を根とする部分木の同型類 ID を返す。
	 * 外部の map を使用することで、複数の木にまたがって一貫した ID を割り振ることができる。
	 * O(N log N)
	 */
	public int[] isomorphismClassificationOnlyBranches(OpenHashMap<int[], Integer> map) {
		if (root == -1)
			throw new AssertionError("rootが未定義");
		int[] ids = new int[N];
		int[] order = postOrder();
		for (int v : order) {
			int[] children = new int[childs[v].size()];
			for (int i = 0; i < children.length; i++) {
				children[i] = ids[childs[v].get(i)];
			}
			Arrays.sort(children);
			Integer id = map.getOrDefaultValue(children, -1);
			if (id == -1) {
				id = map.size();
				map.put(children, id);
			}
			ids[v] = id;
		}
		return ids;
	}

	/**
	 * 2つの（根付きでない）木が同型であるか判定する。
	 * 重心（1〜2個）を根とした根付き木として判定を行う。
	 * O(N log N)
	 */
	public static boolean areIsomorphic(Tree t1, Tree t2) {
		if (t1.N != t2.N)
			return false;
		if (t1.N == 0)
			return true;

		if (t1.root == -1)
			t1.rooted(0);
		if (t2.root == -1)
			t2.rooted(0);

		List<Integer> c1 = t1.centroids();
		List<Integer> c2 = t2.centroids();
		var map = new OpenHashMap<int[], Integer>(HashStrategies.INT_ARRAY);

		int[] ids1 = new int[c1.size()];
		for (int i = 0; i < c1.size(); i++) {
			t1.rooted(c1.get(i));
			ids1[i] = t1.isomorphismClassificationOnlyBranches(map)[c1.get(i)];
		}

		for (int r2 : c2) {
			t2.rooted(r2);
			int id2 = t2.isomorphismClassificationOnlyBranches(map)[r2];
			for (int id1 : ids1) {
				if (id1 == id2)
					return true;
			}
		}
		return false;
	}

	//未テスト
	public RerootResult<Tree.RootedIsoState> isomorphismClassification() {

		if (root == -1) rooted(0);
		var strategy = new RootedIsoStrategy();
		return rerootDP(strategy);
	}

	private static class RootedIsoStrategy implements RerootStrategy<RootedIsoState> {
		int nextClass = 0;
		OpenHashMap<int[], Integer> map = new OpenHashMap<>(HashStrategies.INT_ARRAY);

		@Override
		public RootedIsoState merge(RootedIsoState leftTree, RootedIsoState rightTree) {
			return new RootedIsoState(leftTree, rightTree);
		}

		@Override
		public RootedIsoState up(RootedIsoState childsProduct, int v) {
			int[] children = childsProduct.toArray();
			Arrays.sort(children);
			return new RootedIsoState(classId(children));
		}

		@Override
		public RootedIsoState leaf(int v) {
			return new RootedIsoState(classId(new int[0]));
		}

		int classId(int[] children) {
			Integer id = map.getOrDefaultValue(children, -1);
			if (id == -1) {
				id = nextClass++;
				map.put(children, id);
			}
			return id;
		}
	}

	private static class RootedIsoState {
		final int id;
		final int size;
		final RootedIsoState left;
		final RootedIsoState right;

		RootedIsoState(int id) {
			this.id = id;
			this.size = 1;
			this.left = null;
			this.right = null;
		}

		RootedIsoState(RootedIsoState left, RootedIsoState right) {
			this.id = -1;
			this.size = left.size + right.size;
			this.left = left;
			this.right = right;
		}

		int[] toArray() {
			int[] ret = new int[size];
			ArrayList<RootedIsoState> stack = new ArrayList<>(2 * size);
			int rp = 0;
			stack.add(this);
			while (!stack.isEmpty()) {
				RootedIsoState now = stack.remove(stack.size() - 1);
				if (now.id >= 0) {
					ret[rp++] = now.id;
				} else {
					stack.add(now.left);
					stack.add(now.right);
				}
			}
			return ret;
		}
	}


	
	
	/**
	 * O(terminals.length * log(N)).
	 * toOriginalVertex[i] = 縮約後の木の頂点 i に対応する、元の木の頂点
	 * @param terminals
	 * @return
	 */
	public homeomorphicReductionOf2coreResult homeomorphicReductionOf2core(int[] terminals) {
		//https://atcoder.jp/contests/abc340/submissions/72428491
		//https://atcoder.jp/contests/abc163/submissions/70513197
		if(terminals.length==0)throw new AssertionError();
		if (root==-1)rooted(0);
		int[]order=new int[2*terminals.length];
		for (int i=0;i<terminals.length;++i) {
			order[i]=preOrderOf(terminals[i]);
		}
		Arrays.sort(order, 0, terminals.length);
		for (int i = 0; i < terminals.length; i++) {
			order[i+terminals.length]=preOrderOf(lca(preOrder[order[i]], preOrder[order[(i+1)%terminals.length]]));
		}
		int[] vertices= ArrayUtils.sortq(order);
		for (int i = 0; i < vertices.length; i++) {
			vertices[i]=preOrder[vertices[i]];
		}
		int[] toOriginalVertex=new int[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			toOriginalVertex[i]=vertices[i];
		}
		int[] stk=new int[vertices.length];
		stk[0]=0;
		int last=0;
		LongValueTree compressedTree=new LongValueTree(vertices.length);
		for (int i = 1; i < vertices.length; ++i) {
			while (postOrderOf(toOriginalVertex[stk[last]]) < postOrderOf(toOriginalVertex[i])) {
				last--;
			}
			int par = stk[last];
			compressedTree.addEdge(i, par, depth[toOriginalVertex[i]] - depth[toOriginalVertex[par]]);
			stk[++last]=i;
		}
		return new homeomorphicReductionOf2coreResult(compressedTree, toOriginalVertex);
	}
	
	public record homeomorphicReductionOf2coreResult(LongValueTree g, int[] id) {
	};
	
	public int parent(int v) {
		if(v>=N) throw new AssertionError();
		return parent[v];
	}
	
	void buildFunctionalGraph() {
		if (root == -1) throw new AssertionError("root is undefined");
		var functionalParent=new int[N+1];
		for (int i = 0; i < N; i++) {
			functionalParent[i]=parent[i];
		}
		functionalParent[root]=root;
		fg = new FunctionalGraph(functionalParent);
		fg.buildDoubling(1+MathUtils.floorLog2(N));
	}
	

	public int lca(int u, int v) {
		if (fg == null) {
			buildFunctionalGraph();
		}
		
		if (depth[u] < depth[v]) return lca(v, u);
		int diff = depth[u] - depth[v];
		int logn=MathUtils.floorLog2(N);
		for (int i = 0; i <= logn; ++i) {
			if (diff % 2 == 1) {
				u = fg.parentOf2powers[i][u];
			}
			diff /= 2;
		}
		if (u == v) return u;
		for (int i = logn; i >= 0; --i) {
			int nu = fg.parentOf2powers[i][u];
			int nv = fg.parentOf2powers[i][v];
			if (nu != nv) {
				u = nu;
				v = nv;
			}
		}
		return parent[u];
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
	 * b[i]=root-iパス上のaの値の和
	 * @param a
	 * @return
	 */
	public long[] prefixSum(long[] a) {
		long[] b = Arrays.copyOf(a, a.length);
		if (bfsOrder == null) throw new AssertionError("rootが未設定");
		for (int v : bfsOrder) {
			for (int ch:childs[v]) {
				b[ch]+=b[v];
			}
		}
		return b;
	}
	
	
	
	/**
	 * b[i]=iを根とする部分木上のaの値の和
	 * @param a
	 * @return
	 */
	public long[] subtreeSum(long[] a) {
		long[] b = Arrays.copyOf(a, a.length);
		if (bfsOrder == null) throw new AssertionError("rootが未設定");
		for (int i = bfsOrder.length - 1; i >= 0; i--) {
			int v = bfsOrder[i];
			for (int ch:childs[v]) {
				b[v]+=b[ch];
			}
		}
		return b;
	}
	
	public long[] subtreeSumExceptSelf(long[] a) {
		long[]b=subtreeSum(a);
		for (int i = 0; i < a.length; i++) {
			b[i]-=a[i];
		}
		return b;
	}
	
	
	/**
	 * b[i]=天地逆転したときの、iを根とする部分木上のaの値の和
	 * @param a
	 * @return
	 */
	public long[] cosubtreeSum(long[] a) {
		long[] b=subtreeSum(a);
		long all=b[root];
		for (int i = 0; i < a.length; i++) {
			b[i]=all-b[i]+a[i];
		}
		return b;
	}
	
	public long[] cosubtreeSumExceptSelf(long[] a) {
		long[]b=cosubtreeSum(a);
		for (int i = 0; i < a.length; i++) {
			b[i]-=a[i];
		}
		return b;
	}
	
	public List<Integer> centroids() {
		if (root == -1) throw new AssertionError();
		List<Integer>ret=new ArrayList<>();
		out : for (int i = 0; i < N; i++) {
			int subtreeSize = 0;
			for (int j : adj[i]) {
				if (j == parent[i]) continue;
				if (size[j] > N/2) continue out;
				subtreeSize += size[j];
			}
			if (N - subtreeSize - 1 > N / 2) continue;
			ret.add(i);
		}
		return ret;
	}

	/**
	 * 各頂点について、それを根としたとき、この頂点を通る最長パスの長さを求める。
	 * O(N)
	 * @return 各頂点を根としたときの最長パスの長さ
	 */
	public int[] longestPathLengths() {
		if (root == -1) rooted(0);
		RerootResult<HeightPair> res = rerootDP(new LongestPathStrategy());
		int[] ret = new int[N];
		for (int i = 0; i < N; i++) {
			HeightPair hp = HeightPair.merge(res.lower[i], res.upper[i]);
			// hp.h1 と hp.h2 はその頂点から出る上位2つの枝の長さ（辺数）。
			// それらの和がその頂点を通る最長パスの長さとなる。
			ret[i] = (hp == null) ? 0 : hp.h1 + hp.h2;
		}
		return ret;
	}

	private record HeightPair(int h1, int h2) {
		static HeightPair merge(HeightPair a, HeightPair b) {
			if (a == null) return b;
			if (b == null) return a;
			if (a.h1 > b.h1) return new HeightPair(a.h1, Math.max(a.h2, b.h1));
			return new HeightPair(b.h1, Math.max(b.h2, a.h1));
		}
	}

	private static class LongestPathStrategy implements RerootStrategy<HeightPair> {
		@Override
		public HeightPair merge(HeightPair a, HeightPair b) {
			return HeightPair.merge(a, b);
		}

		@Override
		public HeightPair up(HeightPair t, int v) {
			// 子の高さに1を足して親に返す
			return new HeightPair(t.h1 + 1, 0);
		}

		@Override
		public HeightPair leaf(int v) {
			// 葉の先の仮想的な枝の長さは1（自身と親を結ぶ辺の分）
			return new HeightPair(1, 0);
		}
	}
	
	public Tree centroidDecomposition() {
		//https://atcoder.jp/contests/abc291/submissions/74523589
		tmp = 0;
		int[] col=new int[N];
		Arrays.fill(col, -1);
		Tree ret = new Tree(N);
		dfsCentroidDecomposition(0, -1, N, col, ret, -1);
		ret.rooted(ret.root);
		return ret;
	}
	
	int tmp;
	
	int dfsCentroidDecomposition(int v,int p,int sz, int[] col, Tree ret, int root){
		//https://qiita.com/hotman78/items/6d54c2713bc151a0a1ce
		int inf=Integer.MAX_VALUE/3;
	    if(col[v] != -1)return 0;
	    boolean isCentroid=true;
	    int subtreeSize=1;
	    for(int e:adj[v]){
	        if(p==e)continue;
	        int t=dfsCentroidDecomposition(e,v,sz,col,ret,root);
	        subtreeSize+=t;
	        isCentroid &= t <= sz/2;
	    }
	    isCentroid &= sz - subtreeSize <= sz / 2;
	    if(!isCentroid) return subtreeSize;//重心で無いなら部分木のサイズを返す
	    if (root==-1) {
	    	ret.root = v;
	    } else {
	    	ret.addEdge(root, v);
	    }
	    //重心を登録
	    col[v]=tmp++;
	    
	    //重心分解後の木で自身の子となる重心を探す
	    //dfs(e,n,inf,n)では部分木のサイズを求めてる
	    //(szをinfにすると当然重心が見つからないため部分木のサイズが返る)
	    for(int e:adj[v])dfsCentroidDecomposition(e,v,dfsCentroidDecomposition(e,v,inf,col,ret,v),col,ret,v);
	    
	    //重心はすでに見つかったためinfを返す
	    return inf;
	}
	
	public record PathDecomp(int[][] pair, int root){
	}
	
	/**
	 * 木の辺を被覆するパス分解を構成して返す。
	 * <p>
	 * 返される各パスは {@code root} を通る。各端点対 {@code ret[i] = {u, v}} について、
	 * 通常は {@code u, v} はともに葉である。葉の個数が奇数の場合に限り、最後の点対で
	 * {@code v = root} となる。これらのパスの辺の和集合は木の全ての辺を覆い、異なるパスどうしは端点を共有しない。
	 * O(N log N)</p>
	 * @return 構成したパス分解
	 */
	public PathDecomp pathDecomposition() {
		//https://atcoder.jp/contests/abc453/submissions/74902766
		IntArrayList leafs=new IntArrayList();
		for (int i = 0; i < N; i++) {
			if(deg(i)==1) {
				leafs.add(i);
			}
		}
		long[] weight=new long[N];
		for (int i = 0; i < N; i++) {
			if(deg(i)==1) {
				weight[i]=1;
			}
		}
		int root=centroids(weight).get(0);
		rooted(root);
		int[] f=new int[N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < childs[i].size(); j++) {
				f[childs[i].get(j)]=j;
			}
		}
		IntArrayList[]list=new IntArrayList[childs[root].size()];
		for (int i = 0; i < list.length; i++) {
			list[i]=new IntArrayList();
		}
		for (int i = 0; i < N; i++) {
			if(deg(i)==1 && i!=root) {
				list[f[getLevelAncestor(i, depth[i]-1)]].add(i);
			}
		}
		PriorityQueue<IntArrayList>pq=new PriorityQueue<>((u, v) -> -Integer.compare(u.size(), v.size()));
		for (int i = 0; i < list.length; i++) {
			pq.add(list[i]);
		}
		if(deg(root)==1) {
			var L=new IntArrayList();
			L.add(root);
			pq.add(L);
		}
		int[][]ret=new int[(leafs.size()+1)/2][2];
		int pointer=0;
		while(!pq.isEmpty()) {
			if(pq.size()==1) {
				var list0=pq.poll();
				ret[pointer][0]=list0.pollLast();
				ret[pointer][1]=root;
				break;
			}
			var list0=pq.poll();
			var list1=pq.poll();
			int a=list0.pollLast();
			int b=list1.pollLast();
			ret[pointer][0]=a;
			ret[pointer][1]=b;
			pointer++;
			if(list0.isNonEmpty())
				pq.add(list0);
			if(list1.isNonEmpty())
				pq.add(list1);
		}
		return new PathDecomp(ret, root);
	}
	
	/**
	 * 頂点iの重みをC[i]としたときの重心を返す。
	 * vを根としたとき、どの部分木のC[i]の和も(1/2) sum C[i] 以下であるような頂点を返す。
	 * https://atcoder.jp/contests/abc348/submissions/71279224
	 * @param weights
	 * @return
	 */
	public List<Integer> centroids(long[] C) {
		if (root == -1) rooted(0);
		List<Integer>ret=new ArrayList<>();
		
		long[]sum=subtreeSum(C);
    	long all=ArrayUtils.sum(C);
    	boolean[]removed=new boolean[N];
    	for (int i = 1; i < N; i++) {
			if(sum[i]<all-sum[i]) {
				removed[i]=true;
			}
			if(sum[i]>all-sum[i]) {
				removed[parent(i)]=true;
			}
		}
    	for (int i = 0; i < N; i++) {
			if(!removed[i])ret.add(i);
		}
		return ret;
	}

	
	public int[] bfsOrder() {
		if (!isRooted()) throw new AssertionError();
		return bfsOrder;
	}
	
	public int[] heights() {
		int[] order=bfsOrder.clone();
		int[]ret=new int[N];
		ArrayUtils.reverse(order);
		for (int i : order) {
			for (int ch:childs[i]) {
				ret[i]=Math.max(ret[i], 1+ret[ch]);
			}
		}
		return ret;
	}
	
	
	public boolean isLeaf(int v) {
		if (root == -1) {
			return adj[v].size() <= 1;
		} else {
			if (N==1) return true;
			else return v != root && adj[v].size() <= 1;
		}
	}

	/**
	 * N-size(v)
	 * @param v
	 * @return
	 */
	public int cosize(int v) {
		if (root==-1)throw new AssertionError();
		return N-size(v);
	}

	/**
	 * 根を一時的に r に変更したときの、頂点 v の部分木のサイズを返す。
	 *
	 * グラフにおける頂点 a の、根 r に対する部分木 T(r, a) は、r を根としたときの a の下位の頂点集合を表す。
	 * T(r, a) の要素数を O(log N) で計算する。
	 *
	 * 計算量: O(log N)
	 *
	 * @param v 部分木の根とする頂点
	 * @param r 一時的な根とする頂点
	 * @return 根が r のときの頂点 v の部分木のサイズ
	 */
	// 未テスト
	public int size(int v, int r) {
		if (root == -1) throw new AssertionError("rootedを呼んでいない");
		if (v == r) {
			return N;
		}
		int c = jump(v, r, 1);
		if (c == parent[v]) {
			return size(v);
		} else {
			return N - size(c);
		}
	}
	
	/**
	 * O(log n)
	 * https://atcoder.jp/contests/abc438/submissions/72049900
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean isBranched(int a, int b) {
		if (a == b) return false;
		if (depth[a] < depth[b]) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return lca(a, b) != b;
	}
	
	/**
	 * uからv方向にk頂点移動した頂点を返す。vを通り過ぎる場合は-1。
	 * 根付きでない場合エラー。
	 * @param u
	 * @param v
	 * @param k
	 * @return
	 */
	public int jump(int u, int v, int k) {
		//https://judge.yosupo.jp/submission/357777
		if (k < 0) throw new AssertionError();
		int w=lca(u, v);
		int du=depth[u]-depth[w];
		int dv=depth[v]-depth[w];
		if(k>du+dv)return -1;
		if(k<=du) {
			return getLevelAncestor(u, k);
		} else {
			return getLevelAncestor(v, du+dv-k);
		}
	}
	/**
	 * https://atcoder.jp/contests/abc376/submissions/72928232
	 * @param cnt0
	 * @param cnt1
	 * @return
	 */
	public int[] topologicalSortMinimizingInversions(long[]cnt0, long[]cnt1) {
		if(!isRooted()) throw new AssertionError();
		var cnt0Uf=VertexValueUnionFindFactory.sum(N);
		var cnt1Uf=VertexValueUnionFindFactory.sum(N);
		var firstUf=new IntVertexValueUnionFind(N, (x, y)->x);
		var lastUf=new IntVertexValueUnionFind(N, (x, y)->y);
		for (int i = 0; i < N; i++) {
			cnt1Uf.set(i, cnt1[i]);
			cnt0Uf.set(i, cnt0[i]);
			firstUf.set(i, i);
			lastUf.set(i, i);
		}
		class State implements Comparable<State> {
			int id;
			long c0;
			long c1;
			
			public State(int id, long c0, long c1) {
				this.id = id;
				this.c0 = c0;
				this.c1 = c1;
			}
			
			public int compareTo(State o) {
				//転倒数は cnt1[i]cnt0[j] 
				// i < j (pqの順序)
				// ⇔ cnt1[i]cnt0[j] <= cnt1[j]cnt0[i]
				// cnt1[i]/cnt1[j] <= cnt1[j]/cnt1[j]
				return Long.compare(c1*o.c0, o.c1*c0);
			};
			
		}
		PriorityQueue<State>pq=new PriorityQueue<>();
		for (int i = 0; i < N; i++) {
			if(i==root)continue;
			pq.add(new State(i, cnt0[i], cnt1[i]));
		}
		int[]next=new int[N];
		while(!pq.isEmpty()) {
			State state=pq.poll();
			int v=firstUf.getVertexValue(state.id);
			if(state.c0 != cnt0Uf.getVertexValue(v)) continue;
			if(state.c1 != cnt1Uf.getVertexValue(v)) continue;
			int p=parent(v);
			next[lastUf.getVertexValue(p)]=firstUf.getVertexValue(v);
			firstUf.union(p, v);
			lastUf.union(p, v);
			cnt0Uf.union(p, v);
			cnt1Uf.union(p, v);
			if(firstUf.getVertexValue(v)!=root) {
				pq.add(new State(p, cnt0Uf.getVertexValue(p), cnt1Uf.getVertexValue(p)));
			}
		}
		int[]ret=new int[N];
		{
			int v=root;
			for (int i = 0; i < N; i++) {
				ret[i]=v;
				v=next[v];
			}
		}
		return ret;
	}
	
	public void dump() {
		System.out.println("N="+N+" M="+M);
		for (var e : edges()) {
			System.out.println(e[0] + " " + e[1]);
		}
	}
	

	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
