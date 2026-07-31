package library.util.unionfind;

import java.util.ArrayList;
import java.util.List;

/**
 * UnionFindのunionの列と、頂点のクエリに対して、
 * 各クエリの頂点対が連結になる最古の時刻（ユニオン操作の回数）をオフラインで一括取得するユーティリティクラス。
 * 並列二分探索（Parallel Binary Search）を用いて、すべてのクエリに対して効率的に探索を行う。
 *
 * // 未テスト
 */
public class UnionFindUtils {

	/**
	 * 与えられた頂点数 N とユニオン操作の列 unions、およびクエリ頂点対の始点配列 s と終点配列 t について、
	 * 各クエリが連結となる最小のユニオン操作回数を並列二分探索により求める。
	 *
	 * 具体的には、以下の引数を受け取って一括処理を行う：
	 * - N: 対象となるグラフの頂点数。頂点のインデックスは 0 から N-1 まで。
	 * - unions: 実行されるユニオン操作の時系列データ。各要素は [u, v] の形式で、頂点 u と頂点 v をマージする操作を表す。
	 * - s: 各クエリの始点頂点のリスト。
	 * - t: 各クエリの終点頂点のリスト。s の j 番目の要素 s[j] と、t の j 番目の要素 t[j] が 1 つのクエリペアを構成する。
	 *
	 * // 未テスト
	 *
	 * <ul>
	 *   <li>事前条件:
	 *     <ul>
	 *       <li>N >= 0</li>
	 *       <li>unions != null、かつ各要素の長さが 2 以上。</li>
	 *       <li>s != null、t != null、かつ s.length == t.length。</li>
	 *       <li>すべての 0 &lt;= i &lt; U に対し、0 &lt;= unions[i][0] &lt; N かつ 0 &lt;= unions[i][1] &lt; N。</li>
	 *       <li>すべての 0 &lt;= j &lt; Q に対し、0 &lt;= s[j] &lt; N かつ 0 &lt;= t[j] &lt; N。</li>
	 *     </ul>
	 *   </li>
	 *   <li>事後条件:
	 *     <ul>
	 *       <li>長さ Q の配列を返す。</li>
	 *       <li>各クエリ j について、戻り値の j 番目の要素 ans[j] は以下を満たす：
	 *         <ul>
	 *           <li>s[j] == t[j] の場合は 0。</li>
	 *           <li>最初の k 回（1 &lt;= k &lt;= U）のユニオン操作の後に初めて s[j] と t[j] が連結になる場合は k。</li>
	 *           <li>最後まで連結にならない場合は -1。</li>
	 *         </ul>
	 *       </li>
	 *     </ul>
	 *   </li>
	 *   <li>計算量:
	 *     <ul>
	 *       <li>時間計算量: O((U \alpha(N) + Q) \log U)</li>
	 *       <li>空間計算量: O(N + U + Q)</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param N 頂点数
	 * @param unions ユニオン操作の列。各行は連結する2頂点 [u, v] のペア。長さ U。
	 * @param s クエリの始点配列。長さ Q。
	 * @param t クエリの終点配列。長さ Q。
	 * @return 各クエリが連結になる最古 of 時刻（ユニオン操作の回数。0は初期状態、-1は最後まで非連結）
	 */
	public static int[] solve(int N, int[][] unions, int[] s, int[] t) {
		int U = unions.length;
		int Q = s.length;
		int[] ans = new int[Q];
		int[] L = new int[Q];
		int[] R = new int[Q];

		for (int j = 0; j < Q; j++) {
			if (s[j] == t[j]) {
				ans[j] = 0;
				L[j] = 1;
				R[j] = 0; // すでに終了
			} else {
				ans[j] = -1;
				L[j] = 1;
				R[j] = U;
			}
		}

		@SuppressWarnings("unchecked")
		List<Integer>[] queriesAtMid = new ArrayList[U + 1];
		for (int i = 0; i <= U; i++) {
			queriesAtMid[i] = new ArrayList<>();
		}

		while (true) {
			boolean hasActive = false;
			for (int i = 0; i <= U; i++) {
				queriesAtMid[i].clear();
			}

			for (int j = 0; j < Q; j++) {
				if (L[j] <= R[j]) {
					int mid = (L[j] + R[j]) >>> 1;
					queriesAtMid[mid].add(j);
					hasActive = true;
				}
			}

			if (!hasActive) {
				break;
			}

			UnionFind uf = new UnionFind(N);
			for (int i = 0; i < U; i++) {
				uf.union(unions[i][0], unions[i][1]);
				int step = i + 1;
				for (int q : queriesAtMid[step]) {
					if (uf.equiv(s[q], t[q])) {
						ans[q] = step;
						R[q] = step - 1;
					} else {
						L[q] = step + 1;
					}
				}
			}
		}

		return ans;
	}
}
