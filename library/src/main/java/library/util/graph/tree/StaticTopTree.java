package library.util.graph.tree;

import java.util.Arrays;

import library.util.collections.IntArrayList;
/**
 * https://atcoder.jp/contests/abc351/submissions/72445737
 * https://www.mathenachia.blog/mergetech-and-logn/
 */
public class StaticTopTree {
	int N;
	int root;
	IntArrayList[] childs;
	HLDecomposition hld;
	int sttRoot=-1;
	int nodeNum;
    int[] parent, leftChild, rightChild;
    int[] size;//StaticTopTreeの部分木の頂点のうち、元の木treeの頂点の個数。
    Type[] type;
    
	enum Type { AddLeaf, JoinHeavyEdge, MergeVirtualRoot, AppendVirtualRoot, ReplaceVirtualRoot }
	
	/**
	 * treeは根付き木
	 * @param tree
	 */
	public StaticTopTree(Tree tree) {
		this(tree.N, tree.root(), tree.childs);
	}

	public StaticTopTree(int N, int root, IntArrayList[] childs) {
		this.N = N;
		this.root = root;
		this.childs = childs;
		hld = new HLDecomposition(N, root, childs);
		nodeNum = N;
		parent = new int[4 * N];
		leftChild = new int[4 * N];
		rightChild = new int[4 * N];
		size = new int[4 * N];
		type = new Type[4 * N];
		Arrays.fill(parent, -1);
		Arrays.fill(leftChild, -1);
		Arrays.fill(rightChild, -1);
		Arrays.fill(type, Type.AddLeaf);
		sttRoot = genSubtreeOf(root);
	}
	
	int genLightSubtreeOf(int i) {
		int r=genLightSubtreeWithVirtualRoot(i);
		Type type = r == -1 ? Type.AddLeaf : Type.ReplaceVirtualRoot;
		int v=add(i, r, -1, type);
		size[v]++;
		return v;
	}
	
	int genLightSubtreeWithVirtualRoot(int i) {
		IntArrayList chs=new IntArrayList();
		for (int ch : childs[i]) {
			if (hld.heavy[i] == ch) continue;
			chs.add(genSubtreeWithAppendedVirtualRoot(ch));
		}
		if(chs.isEmpty()) {
			return -1;
		} else {
			return merge(chs, Type.MergeVirtualRoot);
		}
	}
	
	int genSubtreeWithAppendedVirtualRoot(int i) {
		int r=genSubtreeOf(i);
		return add(-1, r, -1, Type.AppendVirtualRoot);
	}
	
	int merge(IntArrayList chs, Type t) {
		if (chs.size() == 1) return chs.get(0);
		IntArrayList b=new IntArrayList();
		IntArrayList c=new IntArrayList();
		int sz0 = 0;
		int sz1 = 0;
		for (int p : chs) sz0 += size[p];
		for (int i=0;i<chs.size();++i) {
			int p=chs.get(i);
			sz1+=size[p];
			sz0-=size[p];
			if(i==0||sz1<=sz0) {
				b.add(p);
			} else {
				c.add(p);
			}
		}
		int x=merge(b, t);
		int y=merge(c, t);
		return add(-1, x, y, t);
	}
	
	
	int add(int k, int l, int r, Type t) {
		if (k == -1) k=nodeNum++;
        parent[k] = -1;
        leftChild[k] = l;
        rightChild[k] = r;
        type[k] = t;
        if (l != -1) {
        	parent[l] = k;
        	size[k]+=size[l];
        }
        if (r != -1) {
        	parent[r] = k;
        	size[k]+=size[r];
        }
        return k;
	}
	
	int genSubtreeOf(int i) {
		IntArrayList chs=new IntArrayList();
		for (int j=i;j!=-1;j=hld.heavy[j]) {
			chs.add(genLightSubtreeOf(j));
		}
		return merge(chs, Type.JoinHeavyEdge);
	}
	
	public int numberOfNodes() {
		return nodeNum;
	}
	
	public int root() {
		return sttRoot;
	}
}