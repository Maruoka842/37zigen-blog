package library.util.segtree;

public class IntSumBinaryIndexedTree {
	int identity=0;
	int[] v;
	/*
	 * 8xxxxxxx
	 * 4xxx  
	 * 2x  6x
	 * 1 3 5 7
	 */
	public IntSumBinaryIndexedTree(int n) {
		v = new int[n + 1];
	}
	
	// k は 0-indexed
	public int prefixSum(int k) {
		if (k < 0) return 0;
		k++;
		int ret=0;
		while(k!=0) {
			ret+=v[k];
			k-=Integer.lowestOneBit(k);
		}
		return ret;
	}
	
	
	// k は 0-indexed
	public void add(int k, int val) {
		k++;
		while(k<v.length) {
			v[k]+=val;
			k+=Integer.lowestOneBit(k);
		}
	}
	
	// l, r は 0-indexed
	// [l, r) 
	public int fold(int l, int r) {
		if (r <= l) return 0;
		return prefixSum(r - 1) - prefixSum(l - 1);
	}
	
	public void clear() {
		java.util.Arrays.fill(v, 0);
	}

	/**
	 * prefixSum(x) <= val となる最大の x を返す。存在しないときは -1 。
	 * @param val
	 * @return
	 */
	public int floorOnPrefixSum(int val) {
		//https://codeforces.com/contest/1354/problem/D
		int a = 0;
		for (int i = Integer.highestOneBit(v.length - 1); i >= 1; i /= 2) {
			if (a + i < v.length && v[a + i] <= val) {
				a += i;
				val -= v[a];
			}
		}
		return a - 1;
	}

	/**
	 * f(fold(l, r)) が真となる最大の r を返す。
	 *
	 * @param l 開始インデックス (0-indexed)
	 * @param f 判定式
	 * @return 最大の r
	 * 計算量: O(log N)
	 * 未テスト
	 */
	public int maximalRight(int l, java.util.function.IntPredicate f) {
		int prefixL = prefixSum(l - 1);
		int a = 0;
		int res = 0;
		int n = v.length - 1;
		for (int i = Integer.highestOneBit(n); i >= 1; i /= 2) {
			if (a + i <= n) {
				int nextRes = res + v[a + i];
				if (a + i <= l || f.test(nextRes - prefixL)) {
					a += i;
					res = nextRes;
				}
			}
		}
		return a;
	}

	/**
	 * f(fold(l, r)) が真となる最小の l を返す。
	 * ここで fold(l, r) は A[l...r-1] の和。
	 * SegTree の仕様に合わせ、(l, r] に対する演算として、f(fold(l+1, r+1)) が真となる最小の l を返す。
	 *
	 * @param r 終了インデックス (0-indexed, inclusive)
	 * @param f 判定式
	 * @return 最小の l
	 * 計算量: O(log N)
	 * 未テスト
	 */
	public int minimalLeft(int r, java.util.function.IntPredicate f) {
		int prefixR = prefixSum(r);
		int a = 0;
		int res = 0;
		int n = v.length - 1;
		for (int i = Integer.highestOneBit(n); i >= 1; i /= 2) {
			if (a + i <= r) {
				int nextRes = res + v[a + i];
				if (!f.test(prefixR - nextRes)) {
					a += i;
					res = nextRes;
				}
			}
		}
		return f.test(prefixR) ? -1 : a;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("IntSumBinaryIndexedTree { v: " + java.util.Arrays.toString(v) + ", identity: " + identity + " }");
	}
}
