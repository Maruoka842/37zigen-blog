package library.util;

import java.util.Arrays;

/**
 * エルデシュ・ギンズブルク・ジヴの定理 (Erdős–Ginzburg–Ziv theorem) に基づき、
 * 2N-1 個の整数から和が N の倍数となる N 個の整数のインデックスを抽出する。
 */
public class ErdosGinzburgZiv {

	/**
	 * 和が N の倍数となる N 個の整数のインデックスを返す。
	 * 未テスト
	 * 計算量: $O(N^2)$ (素数の場合は $O(N \log N)$)
	 *
	 * @param N 抽出する個数 ($N \ge 1$)
	 * @param A 2N-1 個以上の整数配列
	 * @return 和が N の倍数となる N 個の整数のインデックス配列。インデックスは元の配列 A のもの。
	 */
	public static int[] solve(int N, int[] A) {
		if (N <= 0) throw new IllegalArgumentException("N must be positive");
		if (A.length < 2 * N - 1) throw new IllegalArgumentException("A must have at least 2N-1 elements");
		return solveRecursive(N, A, ArrayUtils.range(0, 2 * N - 1));
	}

	private static int[] solveRecursive(int N, int[] A, int[] indices) {
		if (N == 1) {
			return new int[] { indices[0] };
		}
		int p = -1;
		for (int i = 2; 1L * i * i <= N; i++) {
			if (N % i == 0) {
				p = i;
				break;
			}
		}

		if (p != -1) {
			// 合成数 N = n * p
			int n = N / p;
			int[] rem = Arrays.copyOf(indices, 2 * N - 1);
			int remSize = 2 * N - 1;
			int[][] hold = new int[2 * n - 1][p];
			int[] B = new int[2 * n - 1];

			for (int k = 0; k < 2 * n - 1; k++) {
				int[] subValues = new int[2 * p - 1];
				for (int i = 0; i < 2 * p - 1; i++) subValues[i] = A[rem[i]];

				int[] subAnsIndicesInSub = solveRecursive(p, subValues, ArrayUtils.range(0, 2 * p - 1));

				for (int i = 0; i < p; i++) {
					int idxInRem = subAnsIndicesInSub[i];
					hold[k][i] = rem[idxInRem];
					B[k] = (int)((B[k] + (A[hold[k][i]] % (long)N + N) % N) % N);
					rem[idxInRem] = -1;
				}

				// remの最初の2p-1要素から-1を除去（O(p)）
				for (int i = 0; i < 2 * p - 1 && i < remSize; i++) {
					if (rem[i] == -1) {
						rem[i] = rem[remSize - 1];
						remSize--;
						i--;
					}
				}
			}

			for (int i = 0; i < B.length; i++) {
				// B[i] は p の倍数であることが保証される（EGZ定理より）
				// また N も p の倍数なので B[i] % N も p の倍数である
				B[i] /= p;
			}
			int[] subAnsIndicesInB = solveRecursive(n, B, ArrayUtils.range(0, 2 * n - 1));
			int[] res = new int[N];
			for (int k = 0; k < n; k++) {
				for (int i = 0; i < p; i++) {
					res[k * p + i] = hold[subAnsIndicesInB[k]][i];
				}
			}
			return res;
		} else {
			// 素数 N
			int[] currentA = new int[2 * N - 1];
			for (int i = 0; i < 2 * N - 1; i++) {
				currentA[i] = (A[indices[i]] % N + N) % N;
			}

			int[] I = ArrayUtils.range(0, 2 * N - 1);
			// currentA[I] が昇順になるように I をソート。この際 currentA 自体もソートされる。
			ArrayUtils.sortByKeyStable(currentA, I);

			for (int i = 0; i < N; i++) {
				if (currentA[i] == currentA[i + N - 1]) {
					int[] res = new int[N];
					for (int j = 0; j < N; j++) res[j] = indices[I[i + j]];
					return res;
				}
			}

			int[] inv = new int[N];
			inv[1] = 1;
			for (int a = 2; a < N; a++) {
				inv[a] = (int)((long)(N - N / a) * inv[N % a] % N);
			}

			int[] dpFrom = new int[N];
			int[] dpPrevSum = new int[N];
			Arrays.fill(dpFrom, -1);
			dpFrom[0] = 0; // dummy

			int unlit = 1;
			for (int i = 0; i < N - 1; i++) {
				while (unlit < N && dpFrom[unlit] >= 0) unlit++;
				if (unlit == N) break;

				int w = (currentA[i + N] - currentA[i] + N) % N;

				int ok = 0;
				int ng = (int)((long)unlit * inv[w] % N);
				while (ok + 1 < ng) {
					int t = (ok + ng) / 2;
					if (dpFrom[(int)((long)w * t % N)] >= 0) ok = t;
					else ng = t;
				}
				int nextSum = (int)((long)w * ng % N);
				dpFrom[nextSum] = i;
				dpPrevSum[nextSum] = (int)((long)w * ok % N);
			}

			int tg = 0;
			for (int i = 0; i < N; i++) {
				tg = (tg + (N - currentA[i])) % N;
			}

			int[] ansIdxInI = ArrayUtils.range(0, N);
			while (tg != 0) {
				int i = dpFrom[tg];
				ansIdxInI[i] += N;
				tg = dpPrevSum[tg];
			}

			int[] res = new int[N];
			for (int i = 0; i < N; i++) {
				res[i] = indices[I[ansIdxInI[i]]];
			}
			return res;
		}
	}
}
