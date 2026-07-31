package library.util.fold;

import library.util.ArrayUtils;

/**
 * verified:
 * https://atcoder.jp/contests/abc354/tasks/abc354_d
 * https://atcoder.jp/contests/abc331/tasks/abc331_d
 */
public class CircularLongSum2D {
	
	final long[][] a;
	long[][] prefixSum;
	int N;
	int M;
	
	public CircularLongSum2D(long[][] a) {
		this.a  = ArrayUtils.copy(a);
		this.N = a.length;
		this.M = a[0].length;
	}
	
	public CircularLongSum2D(int[][] a) {
		this.a  = new long[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				this.a[i][j]=a[i][j];
			}
		}
		this.N = a.length;
		this.M = a[0].length;
	}
	
	
	
	/**
	 * a（入力配列)を無限個並べた配列上での累積和
	 * 矩形[x0, x1)×[y0, y1)の和
	 */
	public long sum(long x0, long y0, long x1, long y1) {
		if (x1 - x0 <= 0 || y1 - y0 <= 0) return 0;
		if (prefixSum == null) 	{
			this.prefixSum = new long[2 * N][2 * M];
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < M; j++) {
					this.prefixSum[i][j] = a[i][j];
					this.prefixSum[i][j + M] = a[i][j];
					this.prefixSum[i + N][j] = a[i][j];
					this.prefixSum[i + N][j + M] = a[i][j];	
				}
			}
			this.prefixSum = ArrayUtils.prefixSum(prefixSum);
		}
		long lenX = x1 - x0;
		long lenY = y1 - y0;
		long qX = lenX / N;
		long qY = lenY / M;
		x0 = (x0 % N + N) % N;
		y0 = (y0 % M + M) % M;
		x1 = x0 + lenX % N;
		y1 = y0 + lenY % M;
		long ret=prefixSum[N - 1][M - 1] * qX * qY;
		ret+=smallsum(x0%N, y0%M, x0%N+lenX%N, y0%M+lenY%M);
		ret+=smallsum(0, y0%M, N, y0%M+lenY%M) * qX;
		ret+=smallsum(x0%N, 0, x0%N+lenX%N, M) * qY;
		return ret;
	}
	
	/**
	 * 高々2×2のaの繰り返しに収まる矩形[x0, x1)×[y0, y1)の和
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	private long smallsum(long x0, long y0, long x1, long y1) {
		if(x1==0||y1==0)return 0;
		long ret=prefixSum[(int)x1-1][(int)y1-1];
		if(x0!=0)ret-=prefixSum[(int)x0-1][(int)y1-1];
		if(y0!=0)ret-=prefixSum[(int)x1-1][(int)y0-1];
		if(x0!=0&&y0!=0)ret+=prefixSum[(int)x0-1][(int)y0-1];
		return ret;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(NM)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("CircularLongSum2D { a: " + java.util.Arrays.deepToString(a) + ", prefixSum: " + (prefixSum == null ? "null" : java.util.Arrays.deepToString(prefixSum)) + " }");
	}
}
