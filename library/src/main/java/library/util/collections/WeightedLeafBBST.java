package library.util.collections;

import java.util.Arrays;
import library.util.algebra.strategy.longs.LongMonoidStrategy;

/**
 * @see https://oi-wiki.org/ds/wblt/#__tabbed_1_1
 * @see https://atcoder.jp/contests/abc417/submissions/72634717
 */
public class WeightedLeafBBST {
	int tot;//ノード数
	int[]lch;//左子
	int[]rch;//右子
	long[]val;//各ノードに割り当てる値、または部分木の葉ノードに割り当てた値の積
	long[]sz;//部分木の葉の個数
	long lim=1000000000000000000L;//部分木の葉の個数の上限値
	LongMonoidStrategy st;//モノイドの演算・単位元を定義するストラテジー
	
	public WeightedLeafBBST(int capacity) {
		this(capacity, null);
	}

	/**
	 * モノイドのストラテジーを指定して初期化します。
	 *
	 * 未テスト
	 * @param capacity 初期容量
	 * @param st モノイドストラテジー
	 * @complexity O(\text{capacity})
	 */
	public WeightedLeafBBST(int capacity, LongMonoidStrategy st) {
		lch=new int[capacity];
		rch=new int[capacity];
		val=new long[capacity];
		sz=new long[capacity];
		this.st=st;
		if (st != null) {
			val[0] = st.identity();
		}
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
	
	/**
	 * 指定された値を持つ新しい葉ノードを生成して返します。
	 *
	 * 未テスト
	 * @param value 葉ノードに割り当てる値
	 * @return 生成された葉ノード of インデックス
	 * @complexity O(1)
	 */
	public int newLeaf(long value) {
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
		if (st != null) {
			val[u]=st.mul(val[lch[u]], val[rch[u]]);
		}
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
	 * 指定されたノード node が表す列を k 回繰り返して連結した新しいノードを生成して返します。
	 *
	 * 未テスト
	 *
	 * @param node 繰り返す列の根ノードのインデックス
	 * @param k 繰り返し回数
	 * @return node の列を k 回繰り返した列の根ノードのインデックス
	 * @throws IllegalArgumentException k < 0 の場合
	 * @throws ArithmeticException u != 0 且つ sz[node] != 0 且つ k > Long.MAX_VALUE / sz[node] の場合 (列の長さが 64 ビット符号付き整数を超過する場合)
	 * @complexity O(log(sz[node]) + log k)
	 */
	public int repeat(int node, long k) {
		if (k < 0) {
			throw new IllegalArgumentException("repeat count must be nonnegative");
		}
		if (node != 0 && sz[node] != 0 && k > Long.MAX_VALUE / sz[node]) {
			throw new ArithmeticException("rope length exceeds int64");
		}
		int result = 0;
		int base = node;
		long n = k;
		while (n != 0) {
			if ((n & 1) != 0) {
				result = merge(result, base);
			}
			n >>>= 1;
			if (n != 0) {
				base = merge(base, base);
			}
		}
		return result;
	}
	
	/**
	 * 指定された部分木 node (要素数 sz[node] = N) を、先頭の k 個の要素からなる部分木 L と残りの N - k 個の要素からなる部分木 R に分割します。
	 *
	 * 未テスト
	 *
	 * @param node 分割する部分木の根ノードのインデックス
	 * @param k 部分木 L に含める要素数 (0 &le; k &le; sz[node])
	 * @return 分割後の [L, R] の根ノードのインデックスを含むサイズ 2 の配列
	 * @throws IllegalArgumentException k < 0 または k > sz[node] の場合
	 * @complexity O(\log N)
	 */
	public int[] split(int node, long k) {
		if (k < 0 || k > sz[node]) {
			throw new IllegalArgumentException("k is out of bounds");
		}
		if (k == 0) {
			return new int[]{0, node};
		}
		if (k == sz[node]) {
			return new int[]{node, 0};
		}
		int left = lch[node];
		int right = rch[node];
		if (k == sz[left]) {
			return new int[]{left, right};
		} else if (k < sz[left]) {
			int[] res = split(left, k);
			return new int[]{res[0], merge(res[1], right)};
		} else {
			int[] res = split(right, k - sz[left]);
			return new int[]{merge(left, res[0]), res[1]};
		}
	}

	/**
	 * 指定された部分木 node (要素数 sz[node] = N) の半開区間 [l, r) に対応する部分列を切り出して、新しい部分木を生成して返します。
	 *
	 * 未テスト
	 *
	 * @param node 切り出し元の部分木の根ノードのインデックス
	 * @param l 切り出し開始位置 (0 &le; l &le; r &le; sz[node])
	 * @param r 切り出し終了位置 (0 &le; l &le; r &le; sz[node])
	 * @return 切り出した部分木 [l, r) の根ノード of インデックス
	 * @throws IllegalArgumentException l < 0, l > r, または r > sz[node] の場合
	 * @complexity O(\log N)
	 */
	public int slice(int node, long l, long r) {
		if (l < 0 || l > r || r > sz[node]) {
			throw new IllegalArgumentException("l or r is out of bounds");
		}
		int[] resR = split(node, r);
		int[] resL = split(resR[0], l);
		return resL[1];
	}

	/**
	 * 指定された部分木 node (要素数 sz[node] = N) の半開区間 [l, r) に対応する部分列を削除し、残りの部分列を連結して新しい部分木を生成して返します。
	 *
	 * 未テスト
	 *
	 * @param node 削除元の部分木の根ノードのインデックス
	 * @param l 削除開始位置 (0 &le; l &le; r &le; sz[node])
	 * @param r 削除終了位置 (0 &le; l &le; r &le; sz[node])
	 * @return 削除後の部分木の根ノードのインデックス
	 * @throws IllegalArgumentException l < 0, l > r, または r > sz[node] の場合
	 * @complexity O(\log N)
	 */
	public int erase(int node, long l, long r) {
		if (l < 0 || l > r || r > sz[node]) {
			throw new IllegalArgumentException("l or r is out of bounds");
		}
		int[] resR = split(node, r);
		int[] resL = split(resR[0], l);
		return merge(resL[0], resR[1]);
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

	/**
	 * 指定されたノードを根とする部分木の半開区間 [l, r) に対応する葉の値の総積を返します。
	 *
	 * 未テスト
	 * @param l 範囲の左端 (0-indexed)
	 * @param r 範囲の右端 (0-indexed)
	 * @param node 根ノードのインデックス
	 * @return [l, r) の総積
	 * @throws UnsupportedOperationException モノイドストラテジーが指定されていない場合
	 * @complexity O(\log N)
	 */
	public long fold(long l, long r, int node) {
		if (st == null) {
			throw new UnsupportedOperationException("Monoid strategy is not set");
		}
		if (node == 0 || r <= 0 || sz[node] <= l) return st.identity();

		if (l <= 0 && sz[node] <= r) {
			return val[node];
		}

		if (sz[node] == 1) {
			return val[node];
		}

		int x = lch[node];
		int y = rch[node];

		long left = fold(l, r, x);
		long right = fold(l - sz[x], r - sz[x], y);
		return st.mul(left, right);
	}

	/**
	 * 指定されたノードを根とする部分木のすべての葉の値を順に文字列として取得します。
	 *
	 * <p>計算量: $O(N)$（$N$ は部分木の葉の個数）</p>
	 *
	 * @param root 部分木の根ノードのインデックス
	 * @return 葉の値を並べた文字列表現
	 */
	// 未テスト
	public String toString(int root) {
		if (root == 0 || sz[root] == 0) return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		toStringInternal(root, sb, new boolean[]{true});
		sb.append("]");
		return sb.toString();
	}

	private void toStringInternal(int u, StringBuilder sb, boolean[] isFirst) {
		if (u == 0) return;
		if (lch[u] == 0 && rch[u] == 0) {
			if (!isFirst[0]) sb.append(", ");
			isFirst[0] = false;
			sb.append(val[u]);
			return;
		}
		toStringInternal(lch[u], sb, isFirst);
		toStringInternal(rch[u], sb, isFirst);
	}

	/**
	 * このデータ構造の全体情報を表す文字列を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return メタデータの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "WeightedLeafBBST{tot=" + tot + ", capacity=" + sz.length + "}";
	}

	/**
	 * 指定されたノードを根とする部分木のすべての葉の値を順に出力します。
	 *
	 * 未テスト
	 * @param root 部分木の根ノードのインデックス
	 * @complexity O(N) (N は部分木の葉の個数)
	 */
	public void dump(int root) {
		System.out.println(toString(root));
	}
}
