package library.util.fold;

abstract class PrefixSum3D {
	protected long[][][] a;
	public PrefixSum3D(int len0, int len1, int len2) {
		a=new long[len0][len1][len2];
	}
	
	protected void build() {
		int N=a.length;
		int M=a[0].length;
		int L=a[0][0].length;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				for (int k = 0; k < L - 1; k++) {
					a[i][j][k+1]+=a[i][j][k];
				}
			}
		}
		for (int k = 0; k < N - 1; k++) {
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < L; j++) {
					a[k+1][i][j]+=a[k][i][j];
				}
			}
		}
		for (int i = 0; i < N; i++) {
			for (int k = 0; k < M - 1; k++) {
				for (int j = 0; j < L; j++) {
					a[i][k+1][j]+=a[i][k][j];
				}
			}
		}
	}
	
	protected void add(int i, int j, int k, long val) {
		if (i < 0 || j < 0)throw new AssertionError();
		if (i >= a.length || j >= a[i].length || k > a[i][j].length) return;
		a[i][j][k] += val;
	}
	
	protected long get(int i, int j, int k) {
		if (i < 0 || j < 0 || k < 0) return 0;
		i = Math.min(i, a.length - 1);
		j = Math.min(j, a[i].length - 1);
		k = Math.min(k, a[i][j].length - 1);
		return a[i][j][k];
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(NML)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("PrefixSum3D { a: " + java.util.Arrays.deepToString(a) + " }");
	}
}
