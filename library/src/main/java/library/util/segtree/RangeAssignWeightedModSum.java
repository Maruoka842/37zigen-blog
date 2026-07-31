package library.util.segtree;

/**
 * 重み付き区間更新・重み付き区間和取得を行うデータ構造。
 * 整数列 A に対し、Σ A_i c_i を計算量 O(log N) で処理する。
 * (c_i は事前に与えられた重み)
 */
public class RangeAssignWeightedModSum {
	private final IndexedLazySegTreelonglong seg;
	private final long[] weights;
	private final int N;
	private long mod;

	/**
	 * @param weights 各要素の重み c_i
	 * 計算量: O(N)
	 */
	public RangeAssignWeightedModSum(long[] weights, long mod) {
		this.N = weights.length;
		this.weights = weights;
		for (int i = 0; i < weights.length; i++) {
			weights[i]%=mod;
		}
		this.mod = mod;
		long[] C = library.util.ArrayUtils.prefixSumFromZERO(weights);
		for (int i = 0; i < C.length; i++) {
			C[i]%=mod;
		}
		this.seg = new IndexedLazySegTreelonglong(N, new IndexedLazySegTreeStrategy_longlong() {
			@Override
			public long identityX() {
				return 0;
			}

			@Override
			public long mergeX(long a, long b, int l, int m, int r) {
				return (a + b) % mod;
			}

			@Override
			public long identityA() {
				// rangeAssignに単位元がないので、Long.MIN_VALUEを便宜上の単位元にしている。
				return Long.MIN_VALUE;
			}

			@Override
			public long mergeA(long newer, long older) {
				return newer;
			}

			@Override
			public long mergeAX(long a, long x, int l, int r) {
				long ret = a * (C[Math.min(r, N)] - C[Math.min(l, N)]) % mod;
				if (ret < 0) ret += mod;
				return ret;
			}
		});
	}

	/**
	 * 区間 [l, r) を val で更新する。
	 * A_i ← val (l ≦ i < r)
	 * 計算量: O(log N)
	 */
	public void assign(int l, int r, long val) {
		seg.act(l, r, val);
	}

	/**
	 * A_i を val に更新する。
	 * 計算量: O(log N)
	 */
	public void set(int i, long val) {
		seg.set(i, val * weights[i]);
	}

	/**
	 * A_i * c_i を取得する。
	 * 計算量: O(log N)
	 */
	public long get(int i) {
		return seg.get(i);
	}

	/**
	 * 区間 [l, r) の重み付き和 Σ_{i=l}^{r-1} A_i c_i を取得する。
	 * 計算量: O(log N)
	 */
	public long sum(int l, int r) {
		return seg.fold(l, r);
	}

	/**
	 * A の初期値を構築する。
	 * @param A 初期配列
	 * 計算量: O(N log N)
	 */
	public void build(long[] A) {
		long[] weighted = new long[N];
		for (int i = 0; i < N; i++) weighted[i] = (A[i] % mod) * weights[i] % mod;
		seg.build(weighted);
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
		System.out.println("RangeAssignWeightedModSum { N: " + N + ", weights: " + java.util.Arrays.toString(weights) + ", mod: " + mod + " }");
		System.out.print("  seg: ");
		seg.dump();
	}
}
