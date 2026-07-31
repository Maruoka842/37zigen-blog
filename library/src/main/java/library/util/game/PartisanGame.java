package library.util.game;

import java.util.Iterator;

import library.util.DyadicRational;
import library.util.collections.OpenHashMap;
import library.util.collections.OpenHashSet;

/**
 * 非不偏ゲーム (Partisan Game) を表す抽象クラス。
 *
 * <p>非不偏ゲームは、各状態において可能な遷移がプレイヤー（先手・後手、または Left・Right）に依存するゲームである。
 * 状態の勝敗クラス（Outcome Class）を計算することで、ゲームの勝敗を判定できる。</p>
 *
 * @param <S> 状態の型
 */
public abstract class PartisanGame<S> {

	
	protected enum Winner {
		LEFT,
		RIGHT
	}

	/**
	 * null なら通常の再帰ゲーム。
	 * LEFT / RIGHT なら、その時点で勝者が確定している終端状態。
	 */
	protected Winner terminalWinner(S state) {
		return null;
	}
	
	/**
	 * ゲームの勝敗クラスを表す列挙型。
	 */
	public enum Outcome {
		/** Left が先手でも後手でも勝つ (G > 0) */
		LEFT,
		/** Right が先手でも後手でも勝つ (G < 0) */
		RIGHT,
		/** 先手が勝つ (G || 0) */
		NEXT,
		/** 後手が勝つ (G = 0) */
		PREV,
		/** 引き分け（無限ループなど） */
		DRAW
	}

	/**
	 * 勝敗の計算結果を保持するメモ。
	 * キーは (状態, 手番) のペア。
	 */
	protected final OpenHashMap<StateTurn<S>, Boolean> memo = new OpenHashMap<>();

	/**
	 * リモートネス (Remoteness) の計算結果を保持するメモ。
	 */
	protected final OpenHashMap<StateTurn<S>, Integer> remotenessMemo = new OpenHashMap<>();

	/**
	 * 各状態に対するゲーム値（Game Value）の計算結果を保持するメモ。
	 */
	protected final OpenHashMap<S, DyadicRational> valueMemo = new OpenHashMap<>();

	/**
	 * ゲーム値の計算における探索中の状態集合（サイクル検出用のグローバル変数、一時利用）。
	 */
	private final OpenHashSet<S> valueVisiting = new OpenHashSet<>();

	/**
	 * 状態と手番を保持するレコード。
	 */
	protected record StateTurn<S>(S state, boolean leftTurn) {}

	/**
	 * Left が移動可能な次の状態の集合を返す。
	 *
	 * @param state 現在の状態
	 * @return 次の状態の集合
	 */
	public abstract Iterable<S> nextStatesLeft(S state);

	/**
	 * Right が移動可能な次の状態の集合を返す。
	 *
	 * @param state 現在の状態
	 * @return 次の状態の集合
	 */
	public abstract Iterable<S> nextStatesRight(S state);

	/**
	 * 手番のプレイヤーが勝てるかどうかを判定する。terminalの勝敗はterminalWinnerで設定できる。
	 *
	 * @param state 現在の状態
	 * @param leftTurn Left の手番なら true, Right の手番なら false
	 * @return 手番のプレイヤーが勝てるなら true, 勝てないなら false
	 *
	 * </p>
	 */
	public boolean canWin(S state, boolean leftTurn) {
		StateTurn<S> st = new StateTurn<>(state, leftTurn);
		if (memo.containsKey(st)) return memo.get(st);

		Iterator<S> it = (leftTurn ? nextStatesLeft(state) : nextStatesRight(state)).iterator();

		boolean win;

		if (!it.hasNext()) {
			Winner winner = terminalWinner(state);
			if (winner == null) {
				win = false;
			} else {
				win = leftTurn ? winner == Winner.LEFT : winner == Winner.RIGHT;
			}
			memo.put(st, win);
			return win;
		}
		win = false;
		do {
			S next = it.next();
			if (!canWin(next, !leftTurn)) {
				win = true;
				break;
			}
		} while (it.hasNext());
		memo.put(st, win);
		return win;
	}

	/**
	 * 与えられた状態の勝敗クラスを返す。
	 *
	 * @param state 状態
	 * @return 勝敗クラス
	 *
	 * <p>【契約】
	 * <ul>
	 *   <li>事前条件: state が null でないこと。</li>
	 *   <li>事後条件: LEFT, RIGHT, NEXT, PREV のいずれかを返す。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: canWin(state, true) と canWin(state, false) の計算量の和。</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: なし。</li>
	 *   <li>例外・未定義条件: なし。</li>
	 * </ul>
	 * </p>
	 */
	public Outcome getOutcome(S state) {
		boolean leftStartsAndWins = canWin(state, true);
		boolean rightStartsAndWins = canWin(state, false);

		if (leftStartsAndWins && !rightStartsAndWins) return Outcome.LEFT;
		if (!leftStartsAndWins && rightStartsAndWins) return Outcome.RIGHT;
		if (leftStartsAndWins && rightStartsAndWins) return Outcome.NEXT;
		return Outcome.PREV;
	}

	/**
	 * リモートネス (Remoteness) を計算する。
	 * リモートネスは、双方が最善を尽くした際の終局までの手数。
	 * <p>不偏ゲームと同様に、勝ちの状態では最短で決着をつけ、負けの状態では最長で遅延させる。
	 * terminalWinnerがデフォルトなら勝ちの状態のリモートネスは奇数、負けの状態のリモートネスは偶数となる。</p>
	 *
	 * @param state 現在の状態
	 * @param leftTurn Left の手番なら true, Right の手番なら false
	 * @return リモートネス
	 */
	public int remoteness(S state, boolean leftTurn) {
		StateTurn<S> st = new StateTurn<>(state, leftTurn);
		if (remotenessMemo.containsKey(st)) return remotenessMemo.get(st);

		Iterator<S> it = (leftTurn ? nextStatesLeft(state) : nextStatesRight(state)).iterator();

		if (terminalWinner(state) != null || !it.hasNext()) {
			remotenessMemo.put(st, 0);
			return 0;
		}

		int res;
		if (canWin(state, leftTurn)) {
			int minRem = Integer.MAX_VALUE;

			do {
				S next = it.next();
				if (!canWin(next, !leftTurn)) {
					minRem = Math.min(minRem, remoteness(next, !leftTurn));
				}
			} while (it.hasNext());

			res = minRem + 1;
		} else {
			int maxRem = Integer.MIN_VALUE;

			do {
				S next = it.next();
				maxRem = Math.max(maxRem, remoteness(next, !leftTurn));
			} while (it.hasNext());

			res = maxRem + 1;
		}

		remotenessMemo.put(st, res);
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
	 * @param leftTurn Left の手番から始まるなら true, Right なら false
	 * @return 状態の遷移リスト（初期状態を含む）
	 */
	// 未テスト
	public java.util.List<S> getRemotenessPath(S state, boolean leftTurn) {
		java.util.List<S> path = new java.util.ArrayList<>();
		S curr = state;
		boolean currLeft = leftTurn;
		path.add(curr);

		int r = remoteness(curr, currLeft);
		for (int i = 0; i < r; i++) {
			S bestNext = null;
			for (S next : (currLeft ? nextStatesLeft(curr) : nextStatesRight(curr))) {
				if (remoteness(next, !currLeft) + 1 == r - i) {
					bestNext = next;
					break;
				}
			}
			curr = bestNext;
			currLeft = !currLeft;
			path.add(curr);
		}
		return path;
	}

	/**
	 * 与えられた状態のゲーム値（Game Value）を dyadic rational（2進有理数）として計算する。
	 *
	 * <p>ゲーム値 V(G) の数学的定義：
	 * G = { G_L | G_R } に対し、すべての左オプション G_L と右オプション G_R が数であり、
	 * かつ max(V(G_L)) < min(V(G_R)) が成り立つとき、G は数であり、
	 * その値は開区間 (max(V(G_L)), min(V(G_R))) に含まれる「最も単純な有理数（Simplest Number）」となる。
	 * ここで、左オプションが存在しない場合は -infinity、右オプションが存在しない場合は +infinity とする。
	 * 上記の条件を満たさない場合（不偏ゲームのスター * などの非数、またはサイクルが存在する場合）は、ゲーム値は定義されず null を返す。</p>
	 *
	 * @param state 状態
	 * @return ゲーム値、定義されない場合は null
	 *
	 * <p>【契約】
	 * <ul>
	 *   <li>事前条件: state が null でないこと。</li>
	 *   <li>事後条件: dyadic rational 値または null を返す。</li>
	 *   <li>計算量: 到達可能な状態数を V、状態からの平均遷移数を d とするとき、O(V * d)（ただしメモ化を使用）。</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * </p>
	 */
	// 未テスト
	public DyadicRational gameValue(S state) {
		//https://atcoder.jp/contests/abc229/submissions/77910788
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null");
		}
		if (valueMemo.containsKey(state)) {
			return valueMemo.get(state);
		}
		if (valueVisiting.contains(state)) {
			return null;
		}

		boolean hasLeft = nextStatesLeft(state).iterator().hasNext();
		boolean hasRight = nextStatesRight(state).iterator().hasNext();
		if (!hasLeft && !hasRight) {
			Winner winner = terminalWinner(state);
			if (winner == Winner.LEFT) {
				DyadicRational one = new DyadicRational(1);
				valueMemo.put(state, one);
				return one;
			} else if (winner == Winner.RIGHT) {
				DyadicRational negOne = new DyadicRational(-1);
				valueMemo.put(state, negOne);
				return negOne;
			}
		}

		valueVisiting.add(state);
		try {
			DyadicRational maxLeft = null;
			for (S next : nextStatesLeft(state)) {
				DyadicRational val = gameValue(next);
				if (val == null) {
					valueMemo.put(state, null);
					return null;
				}
				if (maxLeft == null || val.compareTo(maxLeft) > 0) {
					maxLeft = val;
				}
			}

			DyadicRational minRight = null;
			for (S next : nextStatesRight(state)) {
				DyadicRational val = gameValue(next);
				if (val == null) {
					valueMemo.put(state, null);
					return null;
				}
				if (minRight == null || val.compareTo(minRight) < 0) {
					minRight = val;
				}
			}

			if (maxLeft != null && minRight != null && maxLeft.compareTo(minRight) >= 0) {
				valueMemo.put(state, null);
				return null;
			}

			DyadicRational x = (maxLeft == null ? DyadicRational.negInfinity() : maxLeft);
			DyadicRational y = (minRight == null ? DyadicRational.infinity() : minRight);
			DyadicRational result = DyadicRational.simplest(x, y, false, false);
			valueMemo.put(state, result);
			return result;
		} finally {
			valueVisiting.remove(state);
		}
	}
}
