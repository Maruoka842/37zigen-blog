package library.util.game;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 不偏ゲーム (Impartial Game) を表す抽象クラス。
 *
 * <p>不偏ゲームは、各状態において可能な遷移がプレイヤーに依存しないゲームである。
 * 状態のグランディー数（ニム値）を計算することで、ゲームの勝敗を判定できる。</p>
 *
 * @param <S> 状態の型
 */
public abstract class ImpartialGame<S> {

	/**
	 * グランディー数の計算結果を保持するメモ。
	 */
	protected final Map<S, Long> memo = new HashMap<>();

	/**
	 * リモートネス (Remoteness) の計算結果を保持するメモ。
	 */
	protected final Map<S, Integer> remotenessMemo = new HashMap<>();

	/**
	 * サスペンス (Suspense) の計算結果を保持するメモ。
	 */
	protected final Map<S, Integer> suspenseMemo = new HashMap<>();

	/**
	 * 与えられた状態から遷移可能な次の状態を返す。
	 *
	 * @param state 現在の状態
	 * @return 次の状態の集合
	 */
	public abstract Iterable<S> nextStates(S state);

	/**
	 * メモ化再帰によりグランディー数を計算する。
	 *
	 * @param state 現在の状態
	 * @return グランディー数
	 *
	 * <p>計算量: 遷移可能な状態数を M とすると、一度の状態計算につき平均 O(M) (MEXの計算に HashSet を使用)
	 * 全体の計算量は到達可能な状態数に比例する。</p>
	 */
	public long grundy(S state) {
		return grundy(state, memo);
	}

	/**
	 * メモ化再帰によりグランディー数を計算する。
	 *
	 * @param state 現在の状態
	 * @param memo メモ
	 * @return グランディー数
	 */
	private long grundy(S state, Map<S, Long> memo) {
		if (memo != null && memo.containsKey(state)) return memo.get(state);

		Set<Long> nextGrundies = new HashSet<>();
		for (S next : nextStates(state)) {
			nextGrundies.add(grundy(next, memo));
		}

		long res = 0;
		while (nextGrundies.contains(res)) {
			res++;
		}
		if (memo != null) memo.put(state, res);
		return res;
	}

	/**
	 * 与えられた状態が先手勝ち（N-ポジション）かどうかを判定する。
	 *
	 * <p>デフォルトではグランディー数が 0 でないことを確認するが、
	 * グランディー数の計算が困難で勝敗のみが効率的に判定できる場合にオーバーライドする。</p>
	 *
	 * @param state 現在の状態
	 * @return 先手勝ちなら true、後手勝ち（P-ポジション）なら false
	 */
	public boolean isWin(S state) {
		return grundy(state) != 0;
	}

	/**
	 * リモートネス (Remoteness) を計算する。
	 *
	 * <p>リモートネスは、勝敗が決まるまでの手数（距離）を表す。
	 * 勝ちの状態では負けの状態へ最短で向かい、負けの状態では勝ちの状態へ最長で遅延させるようにプレイしたときの手数である。
	 * 勝ちの状態のリモートネスは奇数、負けの状態のリモートネスは偶数となる。</p>
	 *
	 * @param state 現在の状態
	 * @return リモートネス
	 */
	public int remoteness(S state) {
		return remoteness(state, remotenessMemo);
	}

	/**
	 * メモ化再帰によりリモートネスを計算する。
	 *
	 * @param state 現在の状態
	 * @param memo メモ
	 * @return リモートネス
	 */
	public int remoteness(S state, Map<S, Integer> memo) {
		if (memo != null && memo.containsKey(state)) return memo.get(state);

		int res;
		if (isWin(state)) {
			int minRem = Integer.MAX_VALUE;
			for (S next : nextStates(state)) {
				if (!isWin(next)) {
					minRem = Math.min(minRem, remoteness(next, memo));
				}
			}
			res = minRem + 1;
		} else {
			int maxRem = -1;
			for (S next : nextStates(state)) {
				maxRem = Math.max(maxRem, remoteness(next, memo));
			}
			res = maxRem + 1;
		}

		if (memo != null) memo.put(state, res);
		return res;
	}

	/**
	 * リモートネスを達成する1本のパス（状態のリスト）を取得する。双方がリモートネス最善を尽くす。
	 *
	 * <p>【契約】
	 * <ul>
	 *   <li>事前条件: state が null でないこと。</li>
	 *   <li>事後条件: リモートネス最善のプレイによる状態のリストを返す。</li>
	 *   <li>計算量: O(R * d) ここで R は remoteness の値、d は平均次数。各ステップで次の状態の遷移先を探索。</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * </p>
	 *
	 * @param state 初期状態
	 * @return 状態の遷移リスト（初期状態を含む）
	 */
	// 未テスト
	public List<S> getRemotenessPath(S state) {
		List<S> path = new java.util.ArrayList<>();
		S curr = state;
		path.add(curr);

		int r = remoteness(curr);
		for (int i = 0; i < r; i++) {
			S bestNext = null;
			for (S next : nextStates(curr)) {
				if (remoteness(next) + 1 == r - i) {
					bestNext = next;
					break;
				}
			}
			curr = bestNext;
			path.add(curr);
		}
		return path;
	}

	/**
	 * サスペンス (Suspense) を計算する。
	 *
	 * <p>サスペンスは、勝敗が決まるまでの手数（距離）を表す。
	 * 勝ちの状態では負けの状態へ最長で遅延させるようにプレイし、
	 * 負けの状態では勝ちの状態へ最短で向かうようにプレイしたときの手数である。
	 * 勝ちの状態のサスペンスは奇数、負けの状態のサスペンスは偶数となる。</p>
	 *
	 * @param state 現在の状態
	 * @return サスペンス
	 *
	 * <p>【契約】
	 * <ul>
	 *   <li>事前条件: {@code state != null}</li>
	 *   <li>事後条件: サスペンスを返す。</li>
	 *   <li>副作用: なし</li>
	 *   <li>計算量: 到達可能な状態数を $V$、遷移の総数を $E$ とすると $O(V + E)$</li>
	 * </ul>
	 * </p>
	 */
	public int suspense(S state) {
		// 未テスト
		return suspense(state, suspenseMemo);
	}

	/**
	 * メモ化再帰によりサスペンスを計算する。
	 *
	 * @param state 現在の状態
	 * @param memo メモ
	 * @return サスペンス
	 *
	 * <p>【契約】
	 * <ul>
	 *   <li>事前条件: {@code state != null}</li>
	 *   <li>事後条件: サスペンスを返す。</li>
	 *   <li>副作用: なし</li>
	 *   <li>計算量: 到達可能な状態数を $V$、遷移の総数を $E$ とすると $O(V + E)$</li>
	 * </ul>
	 * </p>
	 */
	public int suspense(S state, Map<S, Integer> memo) {
		// 未テスト
		if (memo != null && memo.containsKey(state)) return memo.get(state);

		int res;
		boolean hasMoves = false;
		for (S next : nextStates(state)) {
			hasMoves = true;
			break;
		}

		if (!hasMoves) {
			res = 0;
		} else if (isWin(state)) {
			int maxSuspense = -1;
			for (S next : nextStates(state)) {
				if (!isWin(next)) {
					maxSuspense = Math.max(maxSuspense, suspense(next, memo));
				}
			}
			res = maxSuspense + 1;
		} else {
			int minSuspense = Integer.MAX_VALUE;
			for (S next : nextStates(state)) {
				minSuspense = Math.min(minSuspense, suspense(next, memo));
			}
			res = minSuspense + 1;
		}

		if (memo != null) memo.put(state, res);
		return res;
	}

	/**
	 * 有向グラフとして表現されたゲーム of 各頂点を始点としたときの勝敗を返す。
	 *
	 * <p>0: 負け, 1: 勝ち, -1: ドロー（無限ループなどで決着がつかない場合）を返す。</p>
	 *
	 * @param N 頂点数
	 * @param edges 有向グラフの辺リスト [from, to]
	 * @return 各頂点の勝敗を格納した配列
	 *
	 * <p>計算量: O(N + M) (N: 頂点数, M: 辺数)</p>
	 * verified: https://atcoder.jp/contests/abc380/submissions/70093200
	 */
	public static int[] outcomes(int N, List<int[]> edges) {
		int[] dp = new int[N];
		Arrays.fill(dp, -1);
		int[] outDeg = new int[N];
		int[] start = new int[N + 1];
		for (int i = 0; i < edges.size(); ++i) {
			var e = edges.get(i);
			outDeg[e[0]]++;
			start[e[1] + 1]++;
		}
		for (int i = 1; i <= N; i++) {
			start[i] += start[i - 1];
		}
		int[] revTo = new int[edges.size()];
		int[] id = Arrays.copyOf(start, N);
		for (int i = 0; i < edges.size(); ++i) {
			var e = edges.get(i);
			revTo[id[e[1]]++] = e[0];
		}

		int[] que = new int[N];
		int head = 0;
		int tail = 0;
		for (int i = 0; i < N; i++) {
			if (outDeg[i] == 0) {
				que[tail++] = i;
				dp[i] = 0;
			}
		}

		while (head != tail) {
			int v = que[head++];
			int begin = start[v];
			int end = start[v + 1];
			for (int i = begin; i < end; ++i) {
				int u = revTo[i];
				--outDeg[u];
				if (dp[u] == -1) {
					if (dp[v] == 0) {
						dp[u] = 1;
						que[tail++] = u;
					} else if (outDeg[u] == 0) {
						dp[u] = 0;
						que[tail++] = u;
					}
				}
			}
		}
		return dp;
	}

	/**
	 * 有向グラフとして表現されたゲームの各頂点を始点としたときのリモートネスを返す。
	 *
	 * <p>勝てる場合は最短の手数、負ける場合は最長の手数を返す。引き分けの場合は -1 を返す。</p>
	 *
	 * @param N 頂点数
	 * @param edges 有向グラフの辺リスト [from, to]
	 * @return 各頂点のリモートネスを格納した配列
	 */
	public static int[] remotenesses(int N, List<int[]> edges) {
		int[] res = new int[N];
		Arrays.fill(res, -1);
		int[] outcomes = outcomes(N, edges);
		int[] outDeg = new int[N];
		int[] start = new int[N + 1];
		for (int[] e : edges) {
			outDeg[e[0]]++;
			start[e[1] + 1]++;
		}
		for (int i = 1; i <= N; i++) {
			start[i] += start[i - 1];
		}
		int[] revTo = new int[edges.size()];
		int[] id = Arrays.copyOf(start, N);
		for (int[] e : edges) {
			revTo[id[e[1]]++] = e[0];
		}

		int[] que = new int[N];
		int head = 0;
		int tail = 0;
		for (int i = 0; i < N; i++) {
			if (outcomes[i] == 0 && outDeg[i] == 0) {
				res[i] = 0;
				que[tail++] = i;
			}
		}

		while (head != tail) {
			int v = que[head++];
			int begin = start[v];
			int end = start[v + 1];
			for (int i = begin; i < end; i++) {
				int u = revTo[i];
				if (res[u] != -1) continue;

				if (outcomes[u] == 1) {
					if (outcomes[v] == 0) {
						res[u] = res[v] + 1;
						que[tail++] = u;
					}
				} else if (outcomes[u] == 0) {
					// All successors must be winning positions for u to be a settled losing position.
					// BFS ensures that we settle winning positions in increasing order of remoteness.
					// A losing position's remoteness is max(rem(successors)) + 1.
					// Since we decrement outDeg[u] for each winning successor v,
					// the last one to reach 0 will be the one with the maximum remoteness.
					if (outcomes[v] == 1) {
						outDeg[u]--;
						if (outDeg[u] == 0) {
							res[u] = res[v] + 1;
							que[tail++] = u;
						}
					}
				}
			}
		}
		return res;
	}
}
