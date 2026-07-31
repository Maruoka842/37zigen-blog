package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.cycle.CycleBMatching;

public class CycleBMatchingTest {

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: なし。</li>
	 * <li>事後条件: 固定シードで生成した {@code 2 <= N <= 9} かつ {@code 0 <= A_i <= 20} の疑似乱数列10,000個に対して復元した頂点被覆が制約を満たし、重みが最小値に等しい。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: なし。</li>
	 * <li>例外・未定義条件: アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: 試行回数を {@code T=10000}、最大列長を {@code N=9} として時間 {@code O(T*N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 */
	// 未テスト。計算量: T=10000、N=9 として時間 O(T*N)、追加空間 O(N)。
	@Test
	public void randomVertexCoverRestoration() {
		Random random = new Random(987654321L);
		for (int trial = 0; trial < 10000; trial++) {
			int n = 2 + random.nextInt(8);
			long[] w = new long[n];
			for (int i = 0; i < n; i++) {
				w[i] = random.nextInt(21);
			}
			boolean[] cover = CycleBMatching.minimumWeightVertexCoverSetOnCycle(w);
			assertEquals(CycleBMatching.minimumWeightVertexCoverOnCycle(w), coverWeight(w, cover), Arrays.toString(w));
			assertVertexCover(cover);
		}
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code w != null} かつ {@code cover != null} かつ {@code w.length == cover.length}。</li>
	 * <li>事後条件: 返り値は {@code sum_{cover[i]} w[i]} に等しい。</li>
	 * <li>副作用: なし。</li>
	 * <li>破壊的変更: {@code w} と {@code cover} の要素を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。和が {@code long} でオーバーフローする場合の値はJavaの整数演算に従う。</li>
	 * <li>計算量: {@code N = w.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param w 頂点重み
	 * @param cover 頂点被覆候補
	 * @return 選ばれた頂点の重み和
	 */
	// 未テスト。計算量: N = w.length として時間 O(N)、追加空間 O(1)。
	private static long coverWeight(long[] w, boolean[] cover) {
		long res = 0;
		for (int i = 0; i < w.length; i++) {
			if (cover[i]) res += w[i];
		}
		return res;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code cover != null} かつ {@code cover.length >= 2}。</li>
	 * <li>事後条件: 任意の {@code i} について {@code cover[i] || cover[(i+1) mod N]} であることを検査する。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: {@code cover} の要素を変更しない。</li>
	 * <li>参照共有・所有権: {@code cover} の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: {@code N = cover.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param cover 頂点被覆候補
	 */
	// 未テスト。計算量: N = cover.length として時間 O(N)、追加空間 O(1)。
	private static void assertVertexCover(boolean[] cover) {
		int n = cover.length;
		for (int i = 0; i < n; i++) {
			assertTrue(cover[i] || cover[(i + 1) % n], Arrays.toString(cover));
		}
	}
	
	
	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: なし。</li>
	 * <li>事後条件: 固定シードで生成した {@code 1 <= N <= 7} かつ {@code 0 <= A_i <= 4} の疑似乱数列1,000個に対して、
	 * {@link CycleBMatching#maxBMatching(long[])} が容量制約を満たし、重み総和が全探索による最大値に等しい。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: なし。</li>
	 * <li>例外・未定義条件: アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: 試行回数を {@code T=1000}、最大列長を {@code N=7}、最大容量を {@code A=4} として時間 {@code O(T*N*A^N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 */
	// 未テスト。計算量: T=1000、N=7、A=4 として時間 O(T*N*A^N)、追加空間 O(N)。
	@Test
	public void randomBMatchingRestoration() {
		Random random = new Random(123456789L);
		for (int trial = 0; trial < 1000; trial++) {
			int n = 1 + random.nextInt(7);
			long[] a = new long[n];
			for (int i = 0; i < n; i++) {
				a[i] = random.nextInt(5);
			}
			long[] matching = CycleBMatching.maxBMatching(a);
			String message = Arrays.toString(a) + " -> " + Arrays.toString(matching);
			assertBMatching(a, matching);
			assertEquals(bruteForceMaxBMatchingSize(a), matchingSize(matching), message);
			assertEquals(CycleBMatching.maxBMatchingSize(a), matchingSize(matching), message);
		}
	}
	
	
	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code matching != null} かつ {@code a.length == matching.length} かつ任意の {@code i} について {@code a[i] >= 0}。</li>
	 * <li>事後条件: {@code matching} がサイクル上の b-マッチング容量制約を満たすことを検査する。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: {@code a} と {@code matching} の要素を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @param matching b-マッチング候補
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(1)。
	private static void assertBMatching(long[] a, long[] matching) {
		assertEquals(a.length, matching.length, Arrays.toString(matching));
		int n = a.length;
		for (int i = 0; i < n; i++) {
			assertTrue(matching[i] >= 0, Arrays.toString(matching));
			long incident = n == 1 ? 2 * matching[0] : matching[(i - 1 + n) % n] + matching[i];
			assertTrue(incident <= a[i], Arrays.toString(a) + " -> " + Arrays.toString(matching));
		}
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code matching != null}。</li>
	 * <li>事後条件: 返り値は {@code sum_i matching[i]} に等しい。</li>
	 * <li>副作用: なし。</li>
	 * <li>破壊的変更: {@code matching} の要素を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。和が {@code long} でオーバーフローする場合の値はJavaの整数演算に従う。</li>
	 * <li>計算量: {@code N = matching.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param matching b-マッチング候補
	 * @return 辺重みの総和
	 */
	// 未テスト。計算量: N = matching.length として時間 O(N)、追加空間 O(1)。
	private static long matchingSize(long[] matching) {
		long res = 0;
		for (long v : matching) res += v;
		return res;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code 1 <= a.length} かつ任意の {@code i} について {@code 0 <= a[i] <= Integer.MAX_VALUE}。</li>
	 * <li>事後条件: 返り値はサイクル上の b-マッチング最大サイズ {@code max sum_i x_i} subject to 容量制約に等しい。</li>
	 * <li>副作用: なし。</li>
	 * <li>破壊的変更: {@code a} の要素を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。</li>
	 * <li>計算量: {@code N = a.length, A = max_i a[i]} として時間 {@code O(N*A^N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @return 全探索による最大 b-マッチングサイズ
	 */
	// 未テスト。計算量: N = a.length, A = max_i a[i] として時間 O(N*A^N)、追加空間 O(N)。
	private static long bruteForceMaxBMatchingSize(long[] a) {
		return bruteForceMaxBMatchingSize(a, new long[a.length], 0);
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code matching != null} かつ {@code a.length == matching.length} かつ {@code 0 <= index <= a.length}。</li>
	 * <li>事後条件: 返り値は {@code matching[0,index)} を固定した場合の最大 b-マッチングサイズに等しい。</li>
	 * <li>副作用: 再帰中に {@code matching[index, N)} を一時的に変更する。</li>
	 * <li>破壊的変更: 呼び出し終了時に {@code matching[0,index)} を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。</li>
	 * <li>計算量: {@code N = a.length, A = max_i a[i]} として時間 {@code O(N*A^(N-index))}、追加空間 {@code O(N-index)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @param matching 構築中の b-マッチング候補
	 * @param index 次に値を決める辺の添字
	 * @return 固定済み接頭辞を持つ最大 b-マッチングサイズ
	 */
	// 未テスト。計算量: N = a.length, A = max_i a[i] として時間 O(N*A^(N-index))、追加空間 O(N-index)。
	private static long bruteForceMaxBMatchingSize(long[] a, long[] matching, int index) {
		if (index == a.length) return isBMatching(a, matching) ? matchingSize(matching) : -1;
		long best = -1;
		for (long value = 0; value <= Math.min(a[index], a[(index + 1) % a.length]); value++) {
			matching[index] = value;
			best = Math.max(best, bruteForceMaxBMatchingSize(a, matching, index + 1));
		}
		matching[index] = 0;
		return best;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code matching != null} かつ {@code a.length == matching.length}。</li>
	 * <li>事後条件: 返り値は {@code matching} がサイクル上の b-マッチング容量制約を満たすことと同値である。</li>
	 * <li>副作用: なし。</li>
	 * <li>破壊的変更: {@code a} と {@code matching} の要素を変更しない。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @param matching b-マッチング候補
	 * @return 容量制約を満たすなら {@code true}
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(1)。
	private static boolean isBMatching(long[] a, long[] matching) {
		int n = a.length;
		for (int i = 0; i < n; i++) {
			if (matching[i] < 0) return false;
			long incident = n == 1 ? 2 * matching[0] : matching[(i - 1 + n) % n] + matching[i];
			if (incident > a[i]) return false;
		}
		return true;
	}
	

}
