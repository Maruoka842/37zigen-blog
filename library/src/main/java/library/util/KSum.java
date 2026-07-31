package library.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

/**
 * K-SUM 問題を解くクラス。
 */
public class KSum {

	/**
	 * $a[i] + a[j] = \text{target}$ かつ $i < j$ を満たすインデックス $(i, j)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N)$
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	public static int[] twoSum(long[] a, long target) {
		int n = a.length;
		HashMap<Long, Integer> map = new HashMap<>();
		for (int j = 0; j < n; j++) {
			long needed = target - a[j];
			if (map.containsKey(needed)) {
				return new int[] {map.get(needed), j};
			}
			map.put(a[j], j);
		}
		return null;
	}

	/**
	 * $\text{lower} \le a[i] + a[j] < \text{higher}$ かつ $i < j$ を満たすインデックス $(i, j)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N \log N)$
	 *
	 * @param a 配列
	 * @param lower 下限（包含）
	 * @param higher 上限（不包含）
	 * @return インデックスの配列、または null
	 */
	public static int[] twoSumRange(long[] a, long lower, long higher) {
		int n = a.length;
		int[] idx = new int[n];
		for (int i = 0; i < n; i++) idx[i] = i;
		long[] b = a.clone();
		ArrayUtils.sortByKeyStable(b, idx);

		int p1 = 0;
		int p2 = 0;
		for (int j = n - 1; j >= 0; j--) {
			// b[p] が満たすべき範囲は lower - b[j] <= b[p] < higher - b[j]
			long L = lower - b[j];
			long R = higher - b[j];
			// j が減少する（b[j] が減少する）につれて L, R は増加するため、p1, p2 は単調に増加する
			while (p1 < j && b[p1] < L) p1++;
			while (p2 < j && b[p2] < R) p2++;
			// p1 <= p < min(p2, j) ならば b[p] + b[j] が範囲内
			if (p1 < p2 && p1 < j) {
				int[] res = {idx[p1], idx[j]};
				Arrays.sort(res);
				return res;
			}
		}
		return null;
	}

	/**
	 * $a[i] + a[j] + a[k] = \text{target}$ かつ $i < j < k$ を満たすインデックス $(i, j, k)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 * 値域が十分小さい場合、FFT（高速フーリエ変換）を用いて計算を高速化する。
	 *
	 * $O(N + R \log R)$（FFTを用いる場合、ここで $R = \max(a) - \min(a)$）
	 * $O(N^2)$（通常の場合）
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	// 未テスト
	public static int[] threeSum(long[] a, long target) {
		int n = a.length;
		if (n < 3) return null;

		long min = a[0];
		long max = a[0];
		for (int i = 1; i < n; i++) {
			if (a[i] < min) min = a[i];
			if (a[i] > max) max = a[i];
		}

		long R = max - min;
		// 閾値判定: Rが十分小さく、かつNがRに対して十分大きい（実測に基づく分岐条件）ときにFFTを使用する
		long logR = R <= 0 ? 0 : MathUtils.floorLog2(R);
		if (R <= 1000000 && (long) n * n > 21L * R * logR) {
			return threeSumFFT(a, target);
		}

		return threeSumRange(a, target, target + 1);
	}

	/**
	 * $a[i] + a[j] + a[k] = \text{target}$ かつ $i < j < k$ を満たすインデックス $(i, j, k)$ を一つ返す。
	 * 値域が十分小さい場合にFFTを用いて計算を高速化する内部メソッド。
	 *
	 * $O(N + R \log R)$
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	// 未テスト
	private static int[] threeSumFFT(long[] a, long target) {
		//https://atcoder.jp/contests/arc185/submissions/77778090
		int n = a.length;
		if (n < 3) return null;

		long min = a[0];
		long max = a[0];
		for (int i = 1; i < n; i++) {
			if (a[i] < min) min = a[i];
			if (a[i] > max) max = a[i];
		}

		long R = max - min;
		long T = target - 3 * min;
		if (T < 0 || T > 3 * R) return null;

		int X = (int) T;
		long[] f = new long[X + 1];
		for (int i = 0; i < n; i++) {
			long val = a[i] - min;
			if (val <= X) {
				f[(int) val]++;
			}
		}
		for (int i = 0; i < f.length; i++) {
			f[i] = Math.min(f[i], 10);
		}
		long[] ff = library.util.polynomial.PolynomialFp.squared(f);

		library.util.collections.IntArrayList[] list = new library.util.collections.IntArrayList[X + 1];
		for (int i = 0; i < list.length; i++) {
			list[i] = new library.util.collections.IntArrayList();
		}
		for (int i = 0; i < n; i++) {
			long val = a[i] - min;
			if (val <= X) {
				list[(int) val].add(i);
			}
		}

		for (int i = n - 1; i >= 0; --i) {
			long valA = a[i] - min;
			if (valA > X) continue;
			int val = (int) valA;
			list[val].pollLast();
			int v = X - val;
			if (v < 0 || v >= ff.length || ff[v] == 0) continue;
			long cnt = ff[v];
			if (v >= val) {
				cnt -= 2 * f[v - val];
				if (v == 2 * val) {
					cnt++;
					cnt -= (f[val] - 1);
				} else {
					if (v % 2 == 0) {
						cnt -= f[v / 2];
					}
				}
			} else {
				if (v % 2 == 0) {
					cnt -= f[v / 2];
				}
			}
			if (cnt != 0) {
				// v = A[j] + A[k]
				for (int j = 0; j < i; j++) {
					long valJ = a[j] - min;
					if (valJ > v) continue;
					int u = (int) (v - valJ);
					if (u >= 0 && u < list.length && !list[u].isEmpty()) {
						for (int kIdx = 0; kIdx < list[u].size(); kIdx++) {
							int k = list[u].get(kIdx);
							if (k != j && k != i) {
								int[] ans = {j, k, i};
								Arrays.sort(ans);
								return ans;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * $\text{lower} \le a[i] + a[j] + a[k] < \text{higher}$ かつ $i < j < k$ を満たすインデックス $(i, j, k)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^2)$
	 *
	 * @param a 配列
	 * @param lower 下限（包含）
	 * @param higher 上限（不包含）
	 * @return インデックスの配列、または null
	 */
	public static int[] threeSumRange(long[] a, long lower, long higher) {
		int n = a.length;
		int[] idx = new int[n];
		for (int i = 0; i < n; i++) idx[i] = i;
		long[] b = a.clone();
		ArrayUtils.sortByKeyStable(b, idx);

		for (int i = 0; i < n; i++) {
			int p1 = i + 1;
			int p2 = i + 1;
			for (int j = n - 1; j > i; j--) {
				// 三つ目の要素 b[p] が満たすべき範囲は lower - b[i] - b[j] <= b[p] < higher - b[i] - b[j]
				long L = lower - b[i] - b[j];
				long R = higher - b[i] - b[j];
				// j が減少するにつれて L, R は増加するため、p1, p2 は単調に増加する
				while (p1 < j && b[p1] < L) p1++;
				while (p2 < j && b[p2] < R) p2++;
				if (p1 < p2 && p1 < j) {
					int[] res = {idx[i], idx[p1], idx[j]};
					Arrays.sort(res);
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * $a[i] + a[j] + a[k] + a[l] = \text{target}$ かつ $i < j < k < l$ を満たすインデックス $(i, j, k, l)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^2)$
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	public static int[] fourSum(long[] a, long target) {
		int n = a.length;
		if (n < 4) return null;
		HashMap<Long, Long> map = new HashMap<>();
		for (int j = 0; j < n; j++) {
			for (int i = j + 1; i < n; i++) {
				long needed = target - (a[j] + a[i]);
				Long packed = map.get(needed);
				if (packed != null) {
					int k = (int) (packed >> 32);
					int l = packed.intValue();
					int[] res = {k, l, j, i};
					Arrays.sort(res);
					return res;
				}
			}
			for (int i = 0; i < j; i++) {
				map.put(a[i] + a[j], ((long) i << 32) | j);
			}
		}
		return null;
	}

	/**
	 * $\text{lower} \le a[i] + a[j] + a[k] + a[l] < \text{higher}$ かつ $i < j < k < l$ を満たすインデックス $(i, j, k, l)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^2 \log N)$
	 *
	 * @param a 配列
	 * @param lower 下限（包含）
	 * @param higher 上限（不包含）
	 * @return インデックスの配列、または null
	 */
	public static int[] fourSumRange(long[] a, long lower, long higher) {
		int n = a.length;
		if (n < 4) return null;
		TreeMap<Long, Long> map = new TreeMap<>();
		for (int j = 0; j < n; j++) {
			for (int i = j + 1; i < n; i++) {
				// 二つの和 S = a[k] + a[l] (k < l < j) が lower - (a[j] + a[i]) <= S < higher - (a[j] + a[i]) を満たせばよい
				long L = lower - (a[j] + a[i]);
				long R = higher - (a[j] + a[i]);
				// TreeMap.ceilingEntry(L) により S >= L となる最小の S を取得
				Map.Entry<Long, Long> entry = map.ceilingEntry(L);
				// 取得した S が S < R を満たせば条件合致
				if (entry != null && entry.getKey() < R) {
					long packed = entry.getValue();
					int k = (int) (packed >> 32);
					int l = (int) packed;
					int[] res = {k, l, j, i};
					Arrays.sort(res);
					return res;
				}
			}
			for (int i = 0; i < j; i++) {
				map.put(a[i] + a[j], ((long) i << 32) | j);
			}
		}
		return null;
	}

	/**
	 * $a[i] + a[j] + a[k] + a[l] + a[m] = \text{target}$ かつ $i < j < k < l < m$ を満たすインデックス $(i, j, k, l, m)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^3)$
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	public static int[] fiveSum(long[] a, long target) {
		int n = a.length;
		if (n < 5) return null;
		HashMap<Long, Long> map = new HashMap<>();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				for (int k = j + 1; k < n; k++) {
					long needed = target - (a[i] + a[j] + a[k]);
					Long packed = map.get(needed);
					if (packed != null) {
						int l = (int) (packed >> 32);
						int m = packed.intValue();
						int[] res = {i, j, k, l, m};
						Arrays.sort(res);
						return res;
					}
				}
			}
			for (int h = 0; h < i; h++) {
				map.put(a[h] + a[i], ((long) h << 32) | i);
			}
		}
		return null;
	}

	/**
	 * $\text{lower} \le a[i] + a[j] + a[k] + a[l] + a[m] < \text{higher}$ かつ $i < j < k < l < m$ を満たすインデックス $(i, j, k, l, m)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^3)$
	 *
	 * @param a 配列
	 * @param lower 下限（包含）
	 * @param higher 上限（不包含）
	 * @return インデックスの配列、または null
	 */
	public static int[] fiveSumRange(long[] a, long lower, long higher) {
		int n = a.length;
		if (n < 5) return null;

		long[] b = a.clone();
		int[] bidx = new int[n];
		for (int i = 0; i < n; i++) bidx[i] = i;
		ArrayUtils.sortByKeyStable(b, bidx);

		// 全てのペアの和を計算し、ソートしておく
		int numPairs = n * (n - 1) / 2;
		long[] pairSums = new long[numPairs];
		int[] pairIndices = new int[numPairs];
		int cur = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				pairSums[cur] = b[i] + b[j];
				pairIndices[cur] = (i << 16) | j;
				cur++;
			}
		}
		ArrayUtils.sortByKeyStable(pairSums, pairIndices);

		for (int i = 0; i < n; i++) {
			// b[i] を真ん中の要素 (j < k < i < l < m) として固定
			// sumsL = {b[j] + b[k] | k < i}, sumsR = {b[l] + b[m] | l > i}
			// これらは pairSums の部分列（サブセット）である。
			// 二ポインタを用いて pairSums[pL] + b[i] + pairSums[pR] が範囲内になるものを探す。
			int pL = 0;
			int pR = numPairs - 1;
			while (pL < numPairs && pR >= 0) {
				int leftK = pairIndices[pL] & 0xFFFF;
				if (leftK >= i) { pL++; continue; }
				int rightL = pairIndices[pR] >> 16;
				if (rightL <= i) { pR--; continue; }

				long s = pairSums[pL] + b[i] + pairSums[pR];
				if (s < lower) {
					pL++;
				} else if (s >= higher) {
					pR--;
				} else {
					int leftJ = pairIndices[pL] >> 16;
					int rightM = pairIndices[pR] & 0xFFFF;
					int[] res = {bidx[leftJ], bidx[leftK], bidx[i], bidx[rightL], bidx[rightM]};
					Arrays.sort(res);
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * $a[i] + a[j] + a[k] + a[l] + a[m] + a[n] = \text{target}$ かつ $i < j < k < l < m < n$ を満たすインデックス $(i, j, k, l, m, n)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^3)$
	 *
	 * @param a 配列
	 * @param target 目標値
	 * @return インデックスの配列、または null
	 */
	public static int[] sixSum(long[] a, long target) {
		int n = a.length;
		if (n < 6) return null;
		HashMap<Long, Long> map = new HashMap<>();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				for (int k = j + 1; k < n; k++) {
					long needed = target - (a[i] + a[j] + a[k]);
					Long packed = map.get(needed);
					if (packed != null) {
						int l = (int) (packed >> 40);
						int m = (int) ((packed >> 20) & 0xFFFFF);
						int o = (int) (packed & 0xFFFFF);
						int[] res = {i, j, k, l, m, o};
						Arrays.sort(res);
						return res;
					}
				}
			}
			for (int h = 0; h < i; h++) {
				for (int g = h + 1; g < i; g++) {
					long sum = a[h] + a[g] + a[i];
					long packedValue = ((long) h << 40) | ((long) g << 20) | (long) i;
					map.put(sum, packedValue);
				}
			}
		}
		return null;
	}

	/**
	 * $\text{lower} \le a[i] + a[j] + a[k] + a[l] + a[m] + a[n] < \text{higher}$ かつ $i < j < k < l < m < n$ を満たすインデックス $(i, j, k, l, m, n)$ を一つ返す。
	 * 存在しない場合は null を返す。
	 *
	 * $O(N^3 \log N)$
	 *
	 * @param a 配列
	 * @param lower 下限（包含）
	 * @param higher 上限（不包含）
	 * @return インデックスの配列、または null
	 */
	public static int[] sixSumRange(long[] a, long lower, long higher) {
		int n = a.length;
		if (n < 6) return null;
		TreeMap<Long, Long> map = new TreeMap<>();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				for (int k = j + 1; k < n; k++) {
					long L = lower - (a[i] + a[j] + a[k]);
					long R = higher - (a[i] + a[j] + a[k]);
					Map.Entry<Long, Long> entry = map.ceilingEntry(L);
					if (entry != null && entry.getKey() < R) {
						long packed = entry.getValue();
						int l = (int) (packed >> 40);
						int m = (int) ((packed >> 20) & 0xFFFFF);
						int o = (int) (packed & 0xFFFFF);
						int[] res = {i, j, k, l, m, o};
						Arrays.sort(res);
						return res;
					}
				}
			}
			for (int h = 0; h < i; h++) {
				for (int g = h + 1; g < i; g++) {
					long sum = a[h] + a[g] + a[i];
					long packedValue = ((long) h << 40) | ((long) g << 20) | (long) i;
					map.put(sum, packedValue);
				}
			}
		}
		return null;
	}
}
