package library.util;

import java.util.Arrays;
import java.util.function.LongBinaryOperator;

import library.util.algebra.strategy.longs.LongMonoidStrategy;

public class LongMonoidLeafBBST {
//https://atcoder.jp/contests/abc450/submissions/74323006
	int tot;//ノード数
	int[]lch;//左子
	int[]rch;//右子
	long[]val;//部分木の葉ノードに割り当てた値の積
	long[]sz;//部分木の葉の個数
	long lim=1000000000000000000L;
	LongBinaryOperator op;
	long e;
	
	public LongMonoidLeafBBST(int capacity, LongMonoidStrategy st) {
		op = st::mul;
		e  = st.identity();
		lch=new int[capacity];
		rch=new int[capacity];
		val=new long[capacity];
		sz=new long[capacity];
		val[0] = e;//0は空の木として予約
	}
	
	public void setLimit(long lim) {
		this.lim = lim;
	}
	
	void resize() {
		lch=Arrays.copyOf(lch, 2*lch.length);
		rch=Arrays.copyOf(rch, 2*rch.length);
		val=Arrays.copyOf(val, 2*val.length);
		sz=Arrays.copyOf(sz, 2*sz.length);
	}
	
	int newNode() {
		++tot;//ノード0は使わない。
		if (tot >= sz.length) resize();
		return tot;
	}
	
	public int newLeaf(int value) {
		int u=newNode();
		sz[u]=1;
		val[u]=value;
		return u;
	}
	
	/**
	 * 指定されたノードを根とする部分木の葉の総数を返します。
	 * @param v
	 * @return
	 */
	public long size(int v) {
		return sz[v];
	}
	
	/**
	 * 部分木の葉の数を子の情報を使って更新
	 * @param u
	 */
	void pushupTo(int u) {
		sz[u]=Math.min(lim, sz[lch[u]]+sz[rch[u]]);
		val[u]=op.applyAsLong(val[lch[u]],val[rch[u]]);
	}
	
	boolean isLeftHeavy(long sz0, long sz1) {
		return sz0 > 3*sz1;
	}
	
	/**
	 * u,vを子に持つ親ノードを生成して返す。平衡条件は無視。
	 * @param u
	 * @param v
	 * @return
	 */
	int join(int u, int v) {
		int x=newNode();
		lch[x]=u;
		rch[x]=v;
		pushupTo(x);
		return x;
	}
	/**
	 * u, v を左子/右子に持つ新しい親ノードを生成して返す。
	 * u=vでも動く。log(size)個のノードを生成する。計算量O(log(size))
	 * @param u
	 * @param v
	 * @return
	 */
	public int merge(int u, int v) {
		if(u==0)return v;
		if(v==0)return u;
		if(sz[u]==lim)return u;
		if(isLeftHeavy(sz[u], sz[v])) {
			/*!HEAVY!
			 *   u             v
			 *  / \            
			 * x   y           
			 */
			int x=lch[u];
			int y=rch[u];
			if (isLeftHeavy(sz[y]+sz[v], sz[x])) {
				int a=lch[y];
				int b=rch[y];
				/*
				 * 
				 * 
				 * merge前
				 *            u           v
				 *           / \
				 *          x   y
				 *             / \
				 *            a   b
				 *            
				 * merge後
				 *              *
				 *             / \
				 *            *   *
				 *           / \ / \
				 *          x  a b  v         
				 */
				return merge(merge(x, a), merge(b, v));
				// L=merge(x, a), R=merge(b, v) が !isLeftHeavy(sz[L], sz[R]) && !isLeftHeavy(sz[R], sz[L]) を満たすことの証明分からん
				// sz(x):=X, sz(a):=A, sz(b):=B, sz(v):=V と置く。
				// isLeftHeavy(sz[u], sz[v]) ⇔ X+A+B > 3V
				// isLeftHeavy(sz[y]+sz[v], sz[x]) ⇔ A+B+V > 3X
				// y自身は平衡しているので A ≤ 3B, B ≤ 3A
				// u自身は平衡しているので X ≤ 3(A+B), A+B ≤ 3X
				// !isLeftHeavy(sz[L], sz[R]) ⇔ X+A ≤ 3(B+V) を示す。
				// 証明不明。もっと強い条件がいりそう。
				// https://yoichihirai.com/bst.pdf
			} else {
				 /*       *
				 *       / \
				 *      x   *
				 *         / \
				 *        y   v
				 */
				return merge(x, merge(y, v));
			}
		} else if (isLeftHeavy(sz[v], sz[u])) {
			int x=lch[v];
			int y=rch[v];
			if (isLeftHeavy(sz[u]+sz[x], sz[y])) {
				int a=lch[x];
				int b=rch[x];
				return merge(merge(u, a), merge(b, y));
			} else {
				return merge(merge(u, x), y);
			}
		}
		return join(u, v);
	}
	
	/**
	 *  ノードuの部分木の先頭からk番目(0-indexed)の葉の値を返す。存在しないときは-1。
	 * @param u
	 * @param k
	 * @return
	 */
	public long getValue(int u, long k) {
		if(k<0||u==0||k>=sz[u]) return -1;
		if(sz[u]==1)return val[u];
		int x=lch[u];
		int y=rch[u];
		if(sz[x]<=k) return getValue(y, k-sz[x]);
		return getValue(x, k);
	}
	
	public long fold(long l, long r, int node) {
		if (node == 0 || r <= 0 || sz[node] <= l) return e;

	    if (l <= 0 && sz[node] <= r) {
	        return val[node];
	    }

	    if (sz[node] == 1) {
	        return val[node];
	    }

	    int x = lch[node];
	    int y = rch[node];

	    long left = fold(l, r, x);
	    long right = fold(l-sz[x], r-sz[x], y);
	    return op.applyAsLong(left, right);
	}
}
