package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class CompleteGraphBMatchingTest {
	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: なし。</li>
	 * <li>事後条件: 代表例について返り値が多重 b-matching の制約を満たし、辺数が理論上限に等しい。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: なし。</li>
	 * <li>例外・未定義条件: アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: 時間 {@code O(1)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 */
	// 未テスト。計算量: 時間 O(1)、追加空間 O(1)。
	@Test
	public void handWrittenCases() {
		assertMaximum(new int[] {0, 0, 0});
		assertMaximum(new int[] {1, 1, 2});
		assertMaximum(new int[] {0, 3, 2, 2});
		assertMaximum(new int[] {100, 100, 100});
		assertMaximum(new int[] {100, 1, 1, 1});
		assertMaximum(new int[] {4, 4, 4, 1, 1, 1});
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: なし。</li>
	 * <li>事後条件: 固定シードで生成した {@code 1 <= N <= 100} かつ {@code 0 <= b_i < 10^6} の疑似乱数列10,000個について圧縮返り値が制約を満たし、辺数が理論上限に等しい。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: なし。</li>
	 * <li>例外・未定義条件: アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: 試行回数を {@code T=10000}、最大頂点数を {@code N=100} として時間 {@code O(T*N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 */
	// 未テスト。計算量: T=10000、N=100 として時間 O(T*N)、追加空間 O(N)。
	@Test
	public void randomCompressedCases() {
		Random random = new Random(20260616L);
		for (int trial = 0; trial < 10000; trial++) {
			int n = 1 + random.nextInt(100);
			int[] b = new int[n];
			for (int i = 0; i < n; i++) b[i] = random.nextInt(1_000_000);
			assertMaximum(b);
		}
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code b != null} かつ任意の {@code i} について {@code b[i] >= 0}。</li>
	 * <li>事後条件: {@link CompleteGraphBMatching#maximumCardinalityCompressed(int[])} の返り値が圧縮多重 b-matching であり、次数制約を満たし、辺数が {@code min(floor(sum b / 2), sum b - max b)} に等しい。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: {@code b} の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: {@code N=b.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param b 各頂点の次数上限
	 */
	// 未テスト。計算量: N=b.length として時間 O(N)、追加空間 O(N)。
	private static void assertMaximum(int[] b) {
		CompleteGraphBMatching.Edge[] edges = CompleteGraphBMatching.maximumCardinalityCompressed(b);
		long[] deg = new long[b.length];
		long size = 0;
		HashSet<Long> set = new HashSet<>();
		assertTrue(edges.length <= Math.max(0, 2 * b.length - 1), Arrays.toString(edges));
		for (CompleteGraphBMatching.Edge edge : edges) {
			assertTrue(0 <= edge.u() && edge.u() < edge.v() && edge.v() < b.length, edge.toString());
			assertTrue(edge.count() > 0, edge.toString());
			assertTrue(set.add((((long) edge.u()) << 32) ^ edge.v()), Arrays.toString(edges));
			deg[edge.u()] += edge.count();
			deg[edge.v()] += edge.count();
			size += edge.count();
		}
		assertDegreeAndSize(b, deg, size);
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code b != null} かつ {@code deg != null} かつ {@code b.length == deg.length}。</li>
	 * <li>事後条件: {@code deg_i <= b_i} かつ {@code size == min(floor(sum b / 2), sum b - max b)} を検査する。</li>
	 * <li>副作用: JUnit のアサーション失敗を報告しうる。</li>
	 * <li>破壊的変更: なし。</li>
	 * <li>参照共有・所有権: 引数の参照を保持しない。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。アサーション失敗時に JUnit が例外を送出する。</li>
	 * <li>計算量: {@code N=b.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param b 各頂点の次数上限
	 * @param deg 検査対象の次数
	 * @param size 検査対象の辺数
	 */
	// 未テスト。計算量: N=b.length として時間 O(N)、追加空間 O(1)。
	private static void assertDegreeAndSize(int[] b, long[] deg, long size) {
		long sum = 0;
		int max = 0;
		for (int i = 0; i < b.length; i++) {
			assertTrue(deg[i] <= b[i], Arrays.toString(b));
			sum += b[i];
			max = Math.max(max, b[i]);
		}
		long expected = Math.min(sum / 2, sum - max);
		assertEquals(expected, size, Arrays.toString(b));
	}
}
