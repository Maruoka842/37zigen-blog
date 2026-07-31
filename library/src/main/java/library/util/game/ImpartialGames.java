package library.util.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.BitSet;
import java.util.Comparator;
import java.math.BigInteger;
import library.util.graph.Graph;
import library.util.unionfind.UnionFind;
import library.util.Ints;
import library.util.Nimber;
import library.util.collections.IntArrayList;

/**
 * 代表的な不偏ゲームの実装集。
 *
 * <p>不偏ゲーム (Impartial Games) は、先手と後手の可能な遷移が常に同じであるゲームである。
 * 多くの不偏ゲームはニム (Nim) に帰着させることができ、状態のグランディー数（ニム値）
 * を計算することで勝敗を判定できる。</p>
 */
public class ImpartialGames {

	/**
	 * ニム (Nim)。
	 *
	 * <p>ルール: 1つの山から任意の数（1つ以上）の石を取り除くことができる。
	 * 石を取ることができなくなったプレイヤーが負けとなる。
	 * 状態 n に対するグランディー数は n 自体である。</p>
	 */
	public static class Nim extends ImpartialGame<Long> {
		@Override
		public Iterable<Long> nextStates(Long n) {
			List<Long> nexts = new ArrayList<>();
			for (long i = 0; i < n; i++) nexts.add(i);
			return nexts;
		}
		@Override
		public long grundy(Long n) { return n; }

		/**
		 * ニム（1つの山）におけるリモートネスを計算する。
		 *
		 * @param n 山の石の数
		 * @return リモートネス (n > 0 なら 1, n = 0 なら 0)
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code n >= 0}</li>
		 *   <li>事後条件: リモートネスを返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: O(1)</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public int remoteness(Long n) {
			return n == 0 ? 0 : 1;
		}

		@Override
		public int remoteness(Long n, Map<Long, Integer> memo) {
			return remoteness(n);
		}
	}

	/**
	 * 階段ニム (Staircase Nim)。
	 *
	 * <p>ルール: 階段状に並んだ山 (p0, p1, ..., pn) があり、pi (i > 0) から任意の数の石を取り、
	 * pi-1 に移動させることができる。p0 に移動された石はそれ以上動かせない。
	 * 奇数番目の山 (p1, p3, ...) を対象としたニムと等価である。</p>
	 */
	public static class StaircaseNim extends ImpartialGame<List<Long>> {
		@Override
		public Iterable<List<Long>> nextStates(List<Long> state) {
			List<List<Long>> nexts = new ArrayList<>();
			for (int i = 1; i < state.size(); i++) {
				for (long take = 1; take <= state.get(i); take++) {
					List<Long> next = new ArrayList<>(state);
					next.set(i, state.get(i) - take);
					next.set(i - 1, state.get(i - 1) + take);
					nexts.add(next);
				}
			}
			return nexts;
		}
		@Override
		public long grundy(List<Long> piles) {
			long res = 0;
			for (int i = 1; i < piles.size(); i += 2) res ^= piles.get(i);
			return res;
		}
	}

	/**
	 * 複数の不偏ゲームの和 (Nim-sum)。
	 *
	 * <p>ルール: 複数の独立した不偏ゲームが並んでおり、手番のプレイヤーはいずれか1つのゲームを選んで
	 * そのゲーム内で1手進める。全体のグランディー数は各ゲームのグランディー数の排他的論理和 (XOR) となる。</p>
	 */
	public static class Sum<S> extends ImpartialGame<List<S>> {
		private final List<ImpartialGame<S>> games;
		public Sum(List<ImpartialGame<S>> games) {
			this.games = games;
		}
		@Override
		public Iterable<List<S>> nextStates(List<S> states) {
			List<List<S>> nexts = new ArrayList<>();
			for (int i = 0; i < games.size(); i++) {
				for (S nextState : games.get(i).nextStates(states.get(i))) {
					List<S> nextList = new ArrayList<>(states);
					nextList.set(i, nextState);
					nexts.add(nextList);
				}
			}
			return nexts;
		}
		/**
		 * 和ゲームのグランディー数を計算する。
		 *
		 * @param states 各ゲームの状態のリスト
		 * @return グランディー数（各ゲームのグランディー数のニム和）
		 *
		 * <p>計算量: O(N) (N: ゲームの数)</p>
		 */
		@Override
		public long grundy(List<S> states) {
			long res = 0;
			for (int i = 0; i < games.size(); i++) res ^= games.get(i).grundy(states.get(i));
			return res;
		}

		/**
		 * 和ゲームにおいて先手勝ちの場合、1つの最善手を返す。
		 *
		 * @param states 現在の状態
		 * @return [gameIndex, nextState] (i番目のゲームの状態を nextState に変更する手)。
		 *         後手勝ちの場合は null を返す。
		 * <p>計算量: \sum M_i (M_i: i番目のゲームの次の状態数)</p>
		 * <p>副作用: なし</p>
		 */
		public Object[] winningMove(List<S> states) {
			long s = grundy(states);
			if (s == 0) return null;
			for (int i = 0; i < games.size(); i++) {
				long target = games.get(i).grundy(states.get(i)) ^ s;
				for (S next : games.get(i).nextStates(states.get(i))) {
					if (games.get(i).grundy(next) == target) return new Object[]{i, next};
				}
			}
			return null;
		}
	}

	/**
	 * ウィソフのゲーム (Wythoff's Game)。
	 *
	 * <p>ルール: 2つの山があり、1つの山から任意の数の石を取るか、
	 * または両方の山から同数の石を同時に取ることができる。
	 * P-ポジション（後手勝ち）の状態 (a, b) は、b - a = k としたとき a = floor(k * phi) で表される。</p>
	 */
	public static class WythoffGame extends ImpartialGame<WythoffGame.State> {
		public record State(long n, long m) {
			public State normalized() { return n <= m ? this : new State(m, n); }
		}
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			for (long i = 0; i < state.n; i++) nexts.add(new State(i, state.m).normalized());
			for (long i = 0; i < state.m; i++) nexts.add(new State(state.n, i).normalized());
			for (long i = 1; i <= Math.min(state.n, state.m); i++) {
				nexts.add(new State(state.n - i, state.m - i).normalized());
			}
			return nexts;
		}
		@Override
		public boolean isWin(State state) {
			long a = Math.min(state.n, state.m), b = Math.max(state.n, state.m);
			long k = b - a;
			if (k == 0) return a != 0;
			// a = floor(k * (1 + sqrt(5)) / 2) かどうかを整数演算で判定。
			// 2a <= k + k*sqrt(5) < 2a + 2  <=>  2a - k <= k*sqrt(5) < 2a - k + 2
			BigInteger ak2 = BigInteger.valueOf(2 * a - k);
			if (ak2.signum() < 0) return true;
			BigInteger k5 = BigInteger.valueOf(k).pow(2).multiply(BigInteger.valueOf(5));
			boolean isP = ak2.pow(2).compareTo(k5) <= 0 && k5.compareTo(ak2.add(BigInteger.valueOf(2)).pow(2)) < 0;
			return !isP;
		}
	}

	/**
	 * 木の上のグリーン・ハッケンブッシュ (Green Hackenbush on Trees)。
	 *
	 * <p>ルール: 地面に接地した根を持つ木がある。1つの辺を切り、接地していない方の部分木を
	 * すべて取り除く。最後に辺を切ったプレイヤーが勝ちとなる。
	 * グランディー数は、各子の (子のグランディー数 + 1) のニム和となる。</p>
	 */
	public static class GreenHackenbushTree extends ImpartialGame<GreenHackenbushTree.State> {
		/**
		 * グリーン・ハッケンブッシュの木の状態を表すレコード。
		 * @param tree 木構造
		 * @param root 接地している頂点（根）
		 */
		public record State(Graph tree, int root) {}

		/**
		 * 現在の状態から遷移可能な次の状態の集合を返す。
		 *
		 * @param state 現在の状態
		 * @return 遷移可能な状態（木と根）のイテラブル
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}</li>
		 *   <li>事後条件: 1 手で遷移可能なすべての状態を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $V$ を頂点数、$E$ を辺数として $O(E(V+E))$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: 新しいオブジェクトを返す。</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			List<int[]> edges = state.tree().edges();
			for (int i = 0; i < edges.size(); i++) {
				nexts.add(afterCut(state, i));
			}
			return nexts;
		}

		/**
		 * 指定された状態のグランディー数を返す。
		 *
		 * @param state 現在の状態
		 * @return グランディー数
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}</li>
		 *   <li>事後条件: Sprague-Grundy 値を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $O(V+E)$</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public long grundy(State state) {
			return grundy(state.tree(), state.root());
		}

		/**
		 * グラフと根からグランディー数を返す。
		 *
		 * @param tree 木構造
		 * @param root 根
		 * @return グランディー数
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code tree != null}</li>
		 *   <li>事後条件: Sprague-Grundy 値を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $O(V+E)$</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		public static long grundy(Graph tree, int root) {
			return grundy(root, -1, tree.adj);
		}

		/**
		 * 隣接リスト形式の木からグランディー数を返す。
		 *
		 * @param v 現在の頂点
		 * @param p 親の頂点
		 * @param adj 隣接リスト
		 * @return グランディー数
		 */
		public static long grundy(int v, int p, IntArrayList[] adj) {
			long g = 0;
			for (int next : adj[v]) {
				if (next == p) continue;
				g ^= (grundy(next, v, adj) + 1);
			}
			return g;
		}

		/**
		 * 隣接リスト形式の木からグランディー数を返す。
		 *
		 * @param v 現在の頂点
		 * @param p 親の頂点
		 * @param adj 隣接リスト
		 * @return グランディー数
		 */
		public static long grundy(int v, int p, List<Integer>[] adj) {
			long g = 0;
			for (int next : adj[v]) {
				if (next == p) continue;
				g ^= (grundy(next, v, adj) + 1);
			}
			return g;
		}

		private static State afterCut(State state, int removedEdgeIndex) {
			Graph tree = state.tree();
			List<int[]> edges = tree.edges();

			Graph nextTree = new Graph(tree.N);
			for (int i = 0; i < edges.size(); i++) {
				if (i == removedEdgeIndex) continue;
				int[] e = edges.get(i);
				nextTree.addEdge(e[0], e[1]);
			}

			int[] dist = nextTree.bfsDistances(state.root());
			Graph groundedTree = new Graph(tree.N);
			for (int[] e : nextTree.edges()) {
				if (dist[e[0]] <= nextTree.N && dist[e[1]] <= nextTree.N) {
					groundedTree.addEdge(e[0], e[1]);
				}
			}
			return new State(groundedTree, state.root());
		}
	}

	/**
	 * ムーアのニム (Moore's Nim)。
	 *
	 * <p>ルール: 通常のニムと同様だが、1手で最大 k 個までの山を選んで、
	 * それぞれから任意の数の石を取ることができる。
	 * P-ポジションは、各ビットごとの石の数の和がすべて k+1 で割り切れる状態である。</p>
	 */
	public static class MooreNim extends ImpartialGame<List<Long>> {
		private final int k;

		/**
		 * ムーアのニムを初期化する。
		 *
		 * @param k 1手に同時に石を取ることができる山の最大数
		 */
		public MooreNim(int k) { this.k = k; }

		@Override
		public Iterable<List<Long>> nextStates(List<Long> state) {
			// 未テスト: 次の状態をすべて生成する。
			// 計算量: N = state.size(), S = max(state) として O(\sum_{m=1}^k \binom{N}{m} S^m)
			Set<List<Long>> nexts = new HashSet<>();
			int n = state.size();
			for (int m = 1; m <= k && m <= n; m++) {
				generateCombinations(nexts, state, new int[m], 0, 0, n);
			}
			return nexts;
		}

		private void generateCombinations(Set<List<Long>> nexts, List<Long> state, int[] indices, int mIdx, int startIdx, int n) {
			if (mIdx == indices.length) {
				generateReductions(nexts, state, indices, 0, new ArrayList<>(state));
				return;
			}
			for (int i = startIdx; i <= n - (indices.length - mIdx); i++) {
				indices[mIdx] = i;
				generateCombinations(nexts, state, indices, mIdx + 1, i + 1, n);
			}
		}

		private void generateReductions(Set<List<Long>> nexts, List<Long> state, int[] indices, int iIdx, List<Long> current) {
			if (iIdx == indices.length) {
				List<Long> next = new ArrayList<>(current);
				Collections.sort(next);
				nexts.add(Collections.unmodifiableList(next));
				return;
			}
			int pileIdx = indices[iIdx];
			long original = state.get(pileIdx);
			for (long v = 0; v < original; v++) {
				current.set(pileIdx, v);
				generateReductions(nexts, state, indices, iIdx + 1, current);
			}
		}

		/**
		 * ムーアのニムにおける勝敗判定を行う。
		 *
		 * @param piles 各山の石の数のリスト
		 * @return 先手勝ち（N-ポジション）なら true、後手勝ち（P-ポジション）なら false
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code piles != null}</li>
		 *   <li>事後条件: 各ビット $i$ について、そのビットが立っている山の数 $c_i$ が $c_i \equiv 0 \pmod{k+1}$ を満たすときかつそのときに限り false を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $N = \text{piles.size()}$ として $O(N \log(\max(\text{piles})))$</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public boolean isWin(List<Long> piles) {
			// 計算量: N = piles.size() として O(N * log(max(piles)))
			for (int bit = 0; bit < 62; bit++) {
				long sum = 0;
				for (long p : piles) if (((p >>> bit) & 1) != 0) sum++;
				if (sum % (k + 1) != 0) return true;
			}
			return false;
		}

		/**
		 * ムーアのニムにおいて先手勝ちの場合、1つの最善手を返す。
		 *
		 * @param state 各山の石の数のリスト
		 * @return P-ポジションに移行するための新しい山の状態のリスト。後手勝ちの場合は null。
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}</li>
		 *   <li>事後条件: 先手勝ちなら P-ポジションとなる状態を返し、後手勝ちなら null を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: N = {@code state.size()} として O(N * log(max(state)))</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: 新しいリストを返す。</li>
		 *   <li>例外・未定義条件: なし</li>
		 * </ul>
		 * </p>
		 */
		public List<Long> winningMove(List<Long> state) {
			// 未テスト: 解析的に最善手を1つ見つける。
			if (!isWin(state)) return null;
			int n = state.size();
			long[] current = new long[n];
			for (int i = 0; i < n; i++) current[i] = state.get(i);

			Set<Integer> z = new HashSet<>();
			for (int i = 61; i >= 0; i--) {
				int count = 0;
				for (int j = 0; j < n; j++) if (((current[j] >>> i) & 1) != 0) count++;
				int r = count % (k + 1);
				if (r == 0) continue;

				List<Integer> zOn = new ArrayList<>();
				List<Integer> zOff = new ArrayList<>();
				for (int idx : z) {
					if (((current[idx] >>> i) & 1) != 0) zOn.add(idx);
					else zOff.add(idx);
				}

				if (zOn.size() >= r) {
					for (int j = 0; j < r; j++) current[zOn.get(j)] ^= (1L << i);
				} else if (zOff.size() >= (k + 1 - r)) {
					for (int j = 0; j < k + 1 - r; j++) current[zOff.get(j)] ^= (1L << i);
				} else {
					int need = r - zOn.size();
					for (int j = 0; j < n; j++) {
						if (!z.contains(j) && ((current[j] >>> i) & 1) != 0) {
							z.add(j);
							zOn.add(j);
							if (--need == 0) break;
						}
					}
					for (int j = 0; j < r; j++) current[zOn.get(j)] ^= (1L << i);
				}
			}
			List<Long> res = new ArrayList<>();
			for (long v : current) res.add(v);
			Collections.sort(res);
			return res;
		}
	}

	/**
	 * チョンプ (Chomp)。
	 *
	 * <p>ルール: R x C のグリッド（チョコ）があり、左下 (0,0) が毒入りである。
	 * 好きなマスを選び、そのマスおよびその右上の領域をすべて取り除く。
	 * 毒を食べてしまったプレイヤーが負けとなる。不偏ゲームとして扱う場合、
	 * 1x1 より大きい盤面は常に先手勝ちであることが証明されている。</p>
	 *
	 * <p>状態は二次元 boolean 配列をラップした {@link State} で表される。</p>
	 */
	public static class Chomp extends ImpartialGame<Chomp.State> {
		/**
		 * チョンプの状態を表すレコード。
		 * 二次元配列の等価性を正しく判定するために equals と hashCode をオーバーライドしている。
		 */
		public record State(boolean[][] grid) {
			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				State state = (State) o;
				return Arrays.deepEquals(grid, state.grid);
			}
			@Override
			public int hashCode() {
				return Arrays.deepHashCode(grid);
			}
		}

		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			int rows = state.grid.length;
			int cols = state.grid[0].length;
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (r == 0 && c == 0) continue;
					if (state.grid[r][c]) {
						boolean[][] nextGrid = new boolean[rows][cols];
						for (int i = 0; i < rows; i++) {
							for (int j = 0; j < cols; j++) {
								nextGrid[i][j] = state.grid[i][j] && (i < r || j < c);
							}
						}
						nexts.add(new State(nextGrid));
					}
				}
			}
			return nexts;
		}

		/**
		 * 指定されたサイズ盤面の初期状態を返す。
		 *
		 * @param rows 行数
		 * @param cols 列数
		 * @return 初期状態
		 */
		public static State initialState(int rows, int cols) {
			boolean[][] grid = new boolean[rows][cols];
			for (int i = 0; i < rows; i++) Arrays.fill(grid[i], true);
			return new State(grid);
		}

		/**
		 * 指定されたサイズの盤面が先手勝ちかどうかを判定する。
		 *
		 * @param rows 行数
		 * @param cols 列数
		 * @return 先手勝ちなら true
		 */
		public static boolean isWin(int rows, int cols) { return rows > 1 || cols > 1; }
	}

	/**
	 * 一般のグラフ上のグリーン・ハッケンブッシュ。
	 *
	 * <p>ルール: グラフの辺を切り、地面（接地ノード集合）に繋がらなくなった部分をすべて取り除く。
	 * Fusion Principle により、サイクルは1つの頂点に縮約可能であり、木に変換して計算できる。
	 * 接地ノードは特殊なノード（根）に接続されているとみなす。</p>
	 */
	public static class GreenHackenbush extends ImpartialGame<GreenHackenbush.State> {
		/**
		 * グリーン・ハッケンブッシュの状態を表すレコード。
		 *
		 * @param graph 無向グラフ
		 * @param ground 接地している頂点の配列
		 */
		public record State(Graph graph, int[] ground) {}

		/**
		 * 現在の状態から1手で到達可能な状態を返す。
		 *
		 * @param state 現在の状態
		 * @return {@code state.graph()} の各辺を1本切り、接地成分から到達不能になった頂点と辺を削除した状態の列
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}, {@code state.graph() != null}, {@code state.ground() != null}。</li>
		 *   <li>事後条件: 返り値は {@code state.graph()} の各辺を1本切った後の接地成分誘導部分グラフ全体である。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $V = state.graph().N, E = state.graph().M, G = state.ground().length$ として $O(E(V+E+G))$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: 返される {@link State}、{@link Graph}、接地配列は新規に作成される。</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public Iterable<State> nextStates(State state) {
			// 未テスト: 各合法手後に接地成分だけを残す。
			// 計算量: V = state.graph().N, E = state.graph().M, G = state.ground().length として O(E(V+E+G))
			List<State> nexts = new ArrayList<>();
			for (int removed = 0; removed < state.graph().M; removed++) {
				nexts.add(afterCut(state, removed));
			}
			return nexts;
		}

		/**
		 * 指定された状態のグランディー数を返す。
		 *
		 * @param state 現在の状態
		 * @return Fusion Principle で縮約した木のグランディー数
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}, {@code state.graph() != null}, {@code state.ground() != null}。</li>
		 *   <li>事後条件: グリーン・ハッケンブッシュの Sprague-Grundy 値を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $V = state.graph().N, E = state.graph().M, G = state.ground().length$ として $O(V+E+G)$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public long grundy(State state) {
			// 未テスト: Fusion Principle により一般グラフを木へ縮約して値を返す。
			// 計算量: V = state.graph().N, E = state.graph().M, G = state.ground().length として O(V+E+G)
			return grundy(state.graph(), state.ground());
		}

		/**
		 * グラフ・接地頂点集合からグランディー数を返す。
		 *
		 * @param graph 無向グラフ
		 * @param ground 接地している頂点の配列
		 * @return グリーン・ハッケンブッシュの Sprague-Grundy 値
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code graph != null}, {@code ground != null}, 各 {@code u in ground} は {@code 0 <= u < graph.N}。</li>
		 *   <li>事後条件: Fusion Principle で非橋辺連結成分を縮約し、各縮約成分の非橋辺数の偶奇を反映した接地根付き木のグランディー数を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $O(V+E+G)$, $V = graph.N, E = graph.M, G = ground.length$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: なし</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		public static long grundy(Graph graph, int[] ground) {
			// 未テスト: Fusion Principle により一般グラフを木へ縮約して値を返す。
			// 計算量: V = graph.N, E = graph.M, G = ground.length として O(V+E+G)
			ArrayList<int[]> bridges = graph.bridges();
			UnionFind uf = new UnionFind(graph.N + 1);
			int root = graph.N;
			for (int u : ground) uf.union(u, root);
			Set<Long> bridgeSet = new HashSet<>();
			for (int[] b : bridges) bridgeSet.add(Ints.packUnorderedPair(b[0], b[1]));
			for (int[] e : graph.edges()) {
				if (!bridgeSet.contains(Ints.packUnorderedPair(e[0], e[1]))) uf.union(e[0], e[1]);
			}
			boolean[] oddCycleEdges = new boolean[graph.N + 1];
			for (int[] e : graph.edges()) {
				if (!bridgeSet.contains(Ints.packUnorderedPair(e[0], e[1]))) {
					int component = uf.root(e[0]);
					oddCycleEdges[component] = !oddCycleEdges[component];
				}
			}
			List<Integer>[] treeAdj = new List[graph.N + 1];
			for (int i = 0; i <= graph.N; i++) treeAdj[i] = new ArrayList<>();
			for (int[] b : bridges) {
				int bu = uf.root(b[0]), bv = uf.root(b[1]);
				if (bu != bv) { treeAdj[bu].add(bv); treeAdj[bv].add(bu); }
			}
			return grundyWithCycleParity(uf.root(root), -1, treeAdj, oddCycleEdges);
		}

		/**
		 * 非橋辺連結成分の辺数の偶奇を持つ接地根付き木のグランディー数を返す。
		 *
		 * @param v 現在の頂点
		 * @param p 親の頂点。根では {@code -1}
		 * @param adj 橋だけで作った縮約木の隣接リスト
		 * @param oddCycleEdges {@code oddCycleEdges[x] == true} iff 頂点 {@code x} に縮約された非橋辺数が奇数
		 * @return {@code (oddCycleEdges[v] ? 1 : 0) xor xor_{u in child(v)} (grundyWithCycleParity(u, v, adj, oddCycleEdges) + 1)}
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code 0 <= v < adj.length}, {@code -1 <= p < adj.length}, {@code adj != null}, {@code oddCycleEdges != null}, {@code oddCycleEdges.length == adj.length}。</li>
		 *   <li>事後条件: 非橋辺を各縮約頂点上のループとして残し、そのループ数の偶奇をニム和した接地根付き木の Sprague-Grundy 値を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: 呼び出し根から到達可能な縮約頂点数を $V'$, 橋数を $E'$ として $O(V'+E')$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: {@code adj} と {@code oddCycleEdges} は読み取りのみ。</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合、または {@code adj} が {@code p} 以外の閉路を含む場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		private static long grundyWithCycleParity(int v, int p, List<Integer>[] adj, boolean[] oddCycleEdges) {
			// 未テスト: 縮約頂点に残った非橋辺の偶奇を SG 値へ反映する。
			// 計算量: 呼び出し根から到達可能な縮約頂点数を V', 橋数を E' として O(V'+E')
			long g = oddCycleEdges[v] ? 1 : 0;
			for (int next : adj[v]) {
				if (next == p) continue;
				g ^= (grundyWithCycleParity(next, v, adj, oddCycleEdges) + 1);
			}
			return g;
		}

		/**
		 * 頂点数・辺集合・接地頂点集合からグランディー数を返す。
		 *
		 * @param V 頂点数
		 * @param edges 無向辺のリスト
		 * @param ground 接地している頂点の配列
		 * @return グリーン・ハッケンブッシュの Sprague-Grundy 値
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code V >= 0}, {@code edges != null}, {@code ground != null}, 各辺 {@code e} は {@code e.length >= 2} かつ {@code 0 <= e[0], e[1] < V}。</li>
		 *   <li>事後条件: Fusion Principle で非橋辺連結成分を縮約し、各縮約成分の非橋辺数の偶奇を反映した接地根付き木のグランディー数を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $O(V+E+G)$, $E = edges.size(), G = ground.length$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: 入力の辺配列は読み取りのみ。内部で新規 {@link Graph} を作成する。</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		public static long grundy(int V, List<int[]> edges, int[] ground) {
			// 未テスト: 辺リストを Graph に変換してグランディー数を返す。
			// 計算量: V を頂点数、E = edges.size(), G = ground.length として O(V+E+G)
			Graph graph = new Graph(V);
			for (int[] e : edges) graph.addEdge(e[0], e[1]);
			return grundy(graph, ground);
		}

		/**
		 * 指定された辺を切った後の接地成分誘導部分グラフを返す。
		 *
		 * @param state 現在の状態
		 * @param removedEdgeIndex 切る辺の添字
		 * @return {@code removedEdgeIndex} 番目の辺を除去した後に接地頂点から到達可能な辺だけを持つ状態
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state != null}, {@code 0 <= removedEdgeIndex < state.graph().M}。</li>
		 *   <li>事後条件: 返り値のグラフは、指定辺除去後のグラフで接地頂点から到達可能な頂点のみが誘導する辺集合である。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $V = state.graph().N, E = state.graph().M, G = state.ground().length$ として $O(V+E+G)$。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: 返される {@link State}、{@link Graph}、接地配列は新規に作成される。</li>
		 *   <li>例外・未定義条件: 事前条件に反する場合の動作は未定義。</li>
		 * </ul>
		 * </p>
		 */
		private static State afterCut(State state, int removedEdgeIndex) {
			// 未テスト: 指定した辺を除去し、接地成分だけを抽出する。
			// 計算量: V = state.graph().N, E = state.graph().M, G = state.ground().length として O(V+E+G)
			Graph graph = state.graph();
			List<int[]> edges = graph.edges();
			Graph nextGraph = new Graph(graph.N);
			for (int i = 0; i < edges.size(); i++) {
				if (i == removedEdgeIndex) continue;
				int[] e = edges.get(i);
				nextGraph.addEdge(e[0], e[1]);
			}
			List<Integer> groundList = new ArrayList<>();
			for (int g : state.ground()) groundList.add(g);
			int[] dist = nextGraph.bfsDistances(groundList);
			Graph groundedGraph = new Graph(graph.N);
			for (int[] e : nextGraph.edges()) {
				if (dist[e[0]] <= nextGraph.N && dist[e[1]] <= nextGraph.N) groundedGraph.addEdge(e[0], e[1]);
			}
			int keptGroundCount = 0;
			for (int g : state.ground()) if (dist[g] <= nextGraph.N) keptGroundCount++;
			int[] nextGround = new int[keptGroundCount];
			int idx = 0;
			for (int g : state.ground()) if (dist[g] <= nextGraph.N) nextGround[idx++] = g;
			return new State(groundedGraph, nextGround);
		}
	}

	/**
	 * ターニング・コーナーズ (Turning Corners)。
	 *
	 * <p>ルール: 格子上のいくつかの点にコインが置かれている。
	 * (r, c) にある表のコインを選び、(r, c) およびその左上にある3点 (nr, c), (r, nc), (nr, nc)
	 * の計4点のコインをすべて裏返す (0 <= nr < r, 0 <= nc < c)。
	 * グランディー数は各コインの位置 (r, c) に対する (r+1) と (c+1) のニム積のニム和となる。</p>
	 */
	public static class TurningCorners extends ImpartialGame<List<TurningCorners.Coin>> {
		public record Coin(int r, int c) implements Comparable<Coin> {
			@Override
			public int compareTo(Coin o) {
				if (r != o.r) return Integer.compare(r, o.r);
				return Integer.compare(c, o.c);
			}
		}
		@Override
		public Iterable<List<Coin>> nextStates(List<Coin> board) {
			List<List<Coin>> nexts = new ArrayList<>();
			for (int i = 0; i < board.size(); i++) {
				int r = board.get(i).r, c = board.get(i).c;
				for (int nr = 0; nr <= r; nr++) {
					for (int nc = 0; nc <= c; nc++) {
						if (nr == r && nc == c) continue;
						List<Coin> next = new ArrayList<>(board);
						next.remove(i);
						flip(next, nr, c); flip(next, r, nc); flip(next, nr, nc);
						Collections.sort(next);
						nexts.add(next);
					}
				}
			}
			return nexts;
		}
		private void flip(List<Coin> board, int r, int c) {
			Coin coin = new Coin(r, c);
			if (board.contains(coin)) board.remove(coin);
			else board.add(coin);
		}
		
		@Override
		public long grundy(List<TurningCorners.Coin> board) {
			long res = 0;
			for (Coin coin : board) res ^= Nimber.mul(coin.r + 1, coin.c + 1);
			return res;
		}
	}

	/**
	 * ノード・ジオグラフィー (Node Geography)。
	 *
	 * <p>ルール: グラフ上を移動する。すでに訪問した頂点には移動できない。
	 * 移動先がなくなったプレイヤーが負けとなる。</p>
	 */
	public static class NodeGeography extends ImpartialGame<NodeGeography.State> {
		public record State(int currentVertex, long visitedMask) {}
		private final List<Integer>[] adj;
		public NodeGeography(List<Integer>[] adj) { this.adj = adj; }
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			for (int nextV : adj[state.currentVertex]) {
				if (((state.visitedMask >> nextV) & 1) == 0) nexts.add(new State(nextV, state.visitedMask | (1L << nextV)));
			}
			return nexts;
		}
	}

	/**
	 * エッジ・ジオグラフィー (Edge Geography)。
	 *
	 * <p>ルール: グラフ上を移動する。すでに通過した辺は再度利用できない。
	 * 移動できなくなったプレイヤーが負けとなる。</p>
	 */
	public static class EdgeGeography extends ImpartialGame<EdgeGeography.State> {
		public record State(int currentVertex, Graph g) {}
		public EdgeGeography() {}
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			Graph g = state.g;
			for (int nextV : g.adj[state.currentVertex]) {
				Graph nextG = g.copy();
				nextG.removeEdge(state.currentVertex, nextV);
				nexts.add(new State(nextV, nextG));
			}
			return nexts;
		}
	}

	/**
	 * ニム・スクエア (Nim Square)。
	 *
	 * <p>ルール: 1つの山から、1, 4, 9, ... といった平方数の個数だけ石を取り除くことができる。
	 * 取ることができなくなったプレイヤーが負けとなる。</p>
	 */
	public static class NimSquare extends ImpartialGame<Long> {
		@Override
		public Iterable<Long> nextStates(Long n) {
			List<Long> nexts = new ArrayList<>();
			for (long i = 1; i * i <= n; i++) nexts.add(n - i * i);
			return nexts;
		}
	}

	/**
	 * ホワイト・ナイト (White Knight)。
	 *
	 * <p>ルール: チェスのナイトの駒を (x, y) から、(x-2, y-1), (x-1, y-2), (x+1, y-2), (x-2, y+1)
	 * のいずれかに移動させる。駒を動かせなくなったプレイヤーが負けとなる。</p>
	 */
	public static class WhiteKnight extends ImpartialGame<WhiteKnight.Pos> {
		public record Pos(int x, int y) {}
		@Override
		public Iterable<Pos> nextStates(Pos p) {
			List<Pos> nexts = new ArrayList<>();
			int[][] moves = {{-2, -1}, {-1, -2}, {1, -2}, {-2, 1}};
			for (int[] m : moves) {
				int nx = p.x + m[0], ny = p.y + m[1];
				if (nx >= 0 && ny >= 0) nexts.add(new Pos(nx, ny));
			}
			return nexts;
		}
	}

	/**
	 * 素数引きゲーム (Prime Subtraction Game)。
	 *
	 * <p>ルール: 1つの山から素数の個数だけ石を取り除くことができる。
	 * 石を取ることができなくなったプレイヤーが負けとなる。</p>
	 */
	public static class PrimeSubtractionGame extends ImpartialGame<Long> {
		private final int[] primes;
		public PrimeSubtractionGame(int maxN) {
			boolean[] isPrime = new boolean[maxN + 1];
			java.util.Arrays.fill(isPrime, true);
			isPrime[0] = isPrime[1] = false;
			List<Integer> ps = new ArrayList<>();
			for (int i = 2; i <= maxN; i++) {
				if (isPrime[i]) {
					ps.add(i);
					for (int j = i * 2; j <= maxN; j += i) isPrime[j] = false;
				}
			}
			primes = ps.stream().mapToInt(Integer::intValue).toArray();
		}
		@Override
		public Iterable<Long> nextStates(Long n) {
			List<Long> nexts = new ArrayList<>();
			for (int p : primes) {
				if (p <= n) nexts.add(n - (long) p);
				else break;
			}
			return nexts;
		}
	}

	/**
	 * フィボナッチ・ニム (Fibonacci Nim)。
	 *
	 * <p>ルール: 山から n 個の石を取る。最初のプレイヤーは 1 個以上 n-1 個以下の石を取る。
	 * 次のプレイヤーは、直前のプレイヤーが取った数の 2 倍までの石を取ることができる。
	 * 最後に石を取ったプレイヤーが勝ちとなる。</p>
	 */
	public static class FibonacciNim extends ImpartialGame<FibonacciNim.State> {
		public record State(long n, long maxTake) {}
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			for (long take = 1; take <= Math.min(state.n, state.maxTake); take++) nexts.add(new State(state.n - take, take * 2));
			return nexts;
		}
		@Override
		public boolean isWin(State state) { return getMinFibComponent(state.n) <= state.maxTake; }
		private long getMinFibComponent(long n) {
			if (n == 0) return Long.MAX_VALUE;
			List<Long> fibs = new ArrayList<>();
			fibs.add(1L); fibs.add(1L);
			while (fibs.get(fibs.size() - 1) < n) fibs.add(fibs.get(fibs.size() - 1) + fibs.get(fibs.size() - 2));
			long res = 0;
			for (int i = fibs.size() - 1; i >= 1; i--) {
				if (n >= fibs.get(i)) { n -= fibs.get(i); res = fibs.get(i); }
			}
			return res;
		}
	}

	/**
	 * 1回パス可能ニム (Nim with a Pass)。
	 *
	 * <p>ルール: 通常のニムに加え、各プレイヤーはゲーム中に合計1回だけ「パス」ができる。
	 * パスは状態を変化させずに手番を相手に渡す。
	 * パス権があるかないかでグランディー数が変化する。</p>
	 */
	public static class NimWithPass extends ImpartialGame<NimWithPass.State> {
		/**
		 * 1回パス可能ニムの手番基準状態。
		 *
		 * @param piles 山列 p。
		 * @param currentPassAvailable 手番プレイヤーがまだパス可能なら true。
		 * @param opponentPassAvailable 非手番プレイヤーがまだパス可能なら true。
		 */
		public record State(List<Long> piles, boolean currentPassAvailable, boolean opponentPassAvailable) {}
		@Override
		public Iterable<State> nextStates(State state) {
			List<State> nexts = new ArrayList<>();
			for (int i = 0; i < state.piles.size(); i++) {
				for (long take = 1; take <= state.piles.get(i); take++) {
					List<Long> nextPiles = new ArrayList<>(state.piles);
					nextPiles.set(i, state.piles.get(i) - take);
					nexts.add(new State(nextPiles, state.opponentPassAvailable, state.currentPassAvailable));
				}
			}
			if (state.currentPassAvailable) nexts.add(new State(state.piles, state.opponentPassAvailable, false));
			return nexts;
		}

	    /**
	     * 状態のグランディー数を返す。
	     *
	     * <p>x を全ての山の石数の XOR とすると、グランディー数は次のように与えられる。</p>
	     *
	     * <ul>
	     *   <li>{@code currentPassAvailable == opponentPassAvailable} のとき: {@code x}</li>
	     *   <li>{@code currentPassAvailable && !opponentPassAvailable} のとき:
	     *       {@code (x == 1 ? 2 : 1)}</li>
	     *   <li>{@code !currentPassAvailable && opponentPassAvailable} のとき:
	     *       {@code 0}</li>
	     * </ul>
	     *
	     * <p>この公式を用いるため、再帰的な mex 計算は行わない。</p>
	     *
	     * @param state 状態。
	     * @return {@code state} のグランディー数。
	     *
	     * @implSpec
	     * 引数を変更せず、内部にも保持しない。
	     *
	     * @implNote
	     * 計算量は山の数を {@code N} として {@code O(N)}。
	     */
		@Override
		public long grundy(State state) {
			// 未テスト: 解析式により、再帰的な mex 計算を行わずに値を返す。計算量: O(N)
			long x = 0;
			for (long p : state.piles) x ^= p;
			if (state.currentPassAvailable == state.opponentPassAvailable) return x;
			if (state.currentPassAvailable) return x == 1 ? 2 : 1;
			return 0;
		}
	}

	/**
	 * 連言和 (Conjunctive Sum)。
	 *
	 * <p>ルール: 毎手、すべてのゲームをそれぞれ1手ずつ進める。いずれかのゲームで動けなくなった
	 * プレイヤーが負けとなる。
	 * 2ゲームの連言和 {@code G&H} のグランディー数は {@code G} と {@code H} のグランディー数の
	 * ニム積 {@code G(G) ⊗ G(H)} に等しい。</p>
	 */
	public static class ConjunctiveSum<S> extends ImpartialGame<List<S>> {
		/** 各コンポーネントゲームのリスト。 */
		private final List<ImpartialGame<S>> games;

		/**
		 * 連言和を初期化する。
		 *
		 * @param games コンポーネントゲームのリスト。空でないこと。
		 */
		public ConjunctiveSum(List<ImpartialGame<S>> games) {
			this.games = games;
		}

		/**
		 * 現在の状態から1手で遷移可能な状態の集合を返す。
		 *
		 * <p>各手で、すべてのゲームを同時に1手進める。
		 * ゲーム {@code i} の次の状態を {@code s'_i \in next(s_i)} として、
		 * すべての {@code i} について選択した組み合わせのデカルト積が合法手となる。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 遷移可能な状態のリスト（各ゲームを1手ずつ同時に進めた全組み合わせ）
		 * </p>
		 */
		// 未テスト
		@Override
		public Iterable<List<S>> nextStates(List<S> states) {
			List<List<S>> result = new ArrayList<>();
			List<List<S>> nextsPerGame = new ArrayList<>();
			for (int i = 0; i < games.size(); i++) {
				List<S> n = new ArrayList<>();
				for (S s : games.get(i).nextStates(states.get(i))) n.add(s);
				if (n.isEmpty()) return result; // このゲームで動けない → 合法手なし
				nextsPerGame.add(n);
			}
			// デカルト積を生成
			generateCartesian(nextsPerGame, 0, new ArrayList<>(), result);
			return result;
		}

		private void generateCartesian(List<List<S>> nextsPerGame, int idx, List<S> current, List<List<S>> result) {
			if (idx == nextsPerGame.size()) {
				result.add(new ArrayList<>(current));
				return;
			}
			for (S s : nextsPerGame.get(idx)) {
				current.add(s);
				generateCartesian(nextsPerGame, idx + 1, current, result);
				current.remove(current.size() - 1);
			}
		}

		/**
		 * 与えられた状態が先手勝ち（N-ポジション）かどうかを判定する。
		 *
		 * <p>連言和の remoteness は、各コンポーネントゲームの remoteness の最小値となる。
		 * そのため、最小の remoteness が奇数であるとき、かつそのときに限り先手勝ちとなる。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 先手勝ちなら true、後手勝ちなら false
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code states != null} かつサイズがコンポーネントゲーム数と等しい。</li>
		 *   <li>事後条件: 勝敗判定を返す。</li>
		 *   <li>副作用: 各コンポーネントゲームのメモを更新する可能性がある。</li>
		 *   <li>計算量: $N$ をゲーム数、$V_i, E_i$ を $i$ 番目のゲームの到達可能状態数、遷移数として $O(\sum (V_i + E_i))$</li>
		 * </ul>
		 * </p>
		 */
		// 未テスト
		@Override
		public boolean isWin(List<S> states) {
			int minRem = Integer.MAX_VALUE;
			for (int i = 0; i < games.size(); i++) {
				minRem = Math.min(minRem, games.get(i).remoteness(states.get(i)));
			}
			return minRem % 2 != 0;
		}

	}

	/**
	 * 選択和 (Selective Sum)。
	 *
	 * <p>ルール: 毎手、任意の非空集合のゲームを選び、選んだゲームをそれぞれ1手ずつ進める。
	 * すべてのゲームが終了してどのゲームでも動けなくなったプレイヤーが負けとなる。
	 * P-ポジションはすべてのゲームがP-ポジション（グランディー数 = 0）のときかつそのときに限る。
	 * </p>
	 */
	public static class SelectiveSum<S> extends ImpartialGame<List<S>> {
		/** 各コンポーネントゲームのリスト。 */
		private final List<ImpartialGame<S>> games;

		/**
		 * 選択和を初期化する。
		 *
		 * @param games コンポーネントゲームのリスト。空でないこと。
		 */
		public SelectiveSum(List<ImpartialGame<S>> games) {
			this.games = games;
		}

		/**
		 * 現在の状態から1手で遷移可能な状態の集合を返す。
		 *
		 * <p>任意の非空部分集合 $T \subseteq [N]$ を選び、$T$ の各ゲームをそれぞれ1手進める。
		 * 選ばれたゲームについては合法手が必要。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 遷移可能な状態のリスト
		 * </p>
		 */
		// 未テスト
		@Override
		public Iterable<List<S>> nextStates(List<S> states) {
			// 計算量: N をゲーム数として O(2^N * prod(M_i) * N) (重複あり)
			int n = games.size();
			// 各ゲームの次の状態をキャッシュ
			List<List<S>> nextsPerGame = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				List<S> ns = new ArrayList<>();
				for (S s : games.get(i).nextStates(states.get(i))) ns.add(s);
				nextsPerGame.add(ns);
			}
			Set<List<S>> resultSet = new HashSet<>();
			for (int mask = 1; mask < (1 << n); mask++) {
				// mask のビットが立っているゲームを選んで進める
				// まず、選んだゲームに合法手があるか確認
				boolean feasible = true;
				for (int i = 0; i < n; i++) {
					if (((mask >> i) & 1) == 1 && nextsPerGame.get(i).isEmpty()) {
						feasible = false;
						break;
					}
				}
				if (!feasible) continue;
				// 選んだゲームのデカルト積を生成
				generateForMask(states, nextsPerGame, mask, n, 0, new ArrayList<>(states), resultSet);
			}
			return new ArrayList<>(resultSet);
		}

		private void generateForMask(List<S> states, List<List<S>> nextsPerGame, int mask, int n, int idx, List<S> current, Set<List<S>> result) {
			if (idx == n) {
				result.add(new ArrayList<>(current));
				return;
			}
			if (((mask >> idx) & 1) == 1) {
				// このゲームを進める
				for (S s : nextsPerGame.get(idx)) {
					current.set(idx, s);
					generateForMask(states, nextsPerGame, mask, n, idx + 1, current, result);
				}
				current.set(idx, states.get(idx)); // 復元
			} else {
				// このゲームはそのまま
				generateForMask(states, nextsPerGame, mask, n, idx + 1, current, result);
			}
		}
		
		/**
		 * 与えられた状態が先手勝ち（N-ポジション）かどうかを判定する。
		 *
		 * <p>選択和 (Selective Sum) は、少なくとも1つのコンポーネントゲームが先手勝ち（isWin == true）
		 * であるとき、かつそのときに限り先手勝ちとなる。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 先手勝ちなら true、後手勝ち（すべてのコンポーネントが P-ポジション）なら false
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code states != null} かつサイズがコンポーネントゲーム数と等しい。</li>
		 *   <li>事後条件: 勝敗判定を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: $N$ をゲーム数として $O(\sum \text{isWin}_i)$</li>
		 * </ul>
		 * </p>
		 */
		// 未テスト
		@Override
		public boolean isWin(List<S> states) {
			for (int i = 0; i < games.size(); i++) {
				if (games.get(i).isWin(states.get(i))) {
					return true;
				}
			}
			return false;
		}
		
	}

	/**
	 * 継続連言和 (Continued Conjunctive Sum)。
	 *
	 * <p>ルール: 毎手、まだ終了していないすべてのゲームをそれぞれ1手ずつ進める。
	 * 終了したゲーム（合法手がないゲーム）は以後無視される。
	 * 動けるゲームがまったくなくなったプレイヤーが負けとなる。</p>
	 */
	public static class ContinuedConjunctiveSum<S> extends ImpartialGame<List<S>> {
		/** 各コンポーネントゲームのリスト。 */
		private final List<ImpartialGame<S>> games;

		/**
		 * 継続連言和を初期化する。
		 *
		 * @param games コンポーネントゲームのリスト。空でないこと。
		 */
		public ContinuedConjunctiveSum(List<ImpartialGame<S>> games) {
			this.games = games;
		}

		/**
		 * 現在の状態から1手で遷移可能な状態の集合を返す。
		 *
		 * <p>終了していないゲーム（合法手があるゲーム）のすべてを同時に1手進める。
		 * 全ゲームが終了している場合は合法手がなく、空集合を返す。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 遷移可能な状態のリスト（動いているゲームを全て1手進めた全組み合わせ）
		 * </p>
		 */
		// 未テスト
		@Override
		public Iterable<List<S>> nextStates(List<S> states) {
			int n = games.size();
			List<List<S>> nextsPerGame = new ArrayList<>();
			List<Integer> activeIndices = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				List<S> ns = new ArrayList<>();
				for (S s : games.get(i).nextStates(states.get(i))) ns.add(s);
				nextsPerGame.add(ns);
				if (!ns.isEmpty()) activeIndices.add(i);
			}
			List<List<S>> result = new ArrayList<>();
			if (activeIndices.isEmpty()) return result; // 全ゲーム終了
			// activeIndices のゲームのみデカルト積を生成、それ以外は現在の状態のまま
			generateActiveCartesian(states, nextsPerGame, activeIndices, 0, new ArrayList<>(states), result);
			return result;
		}

		private void generateActiveCartesian(List<S> states, List<List<S>> nextsPerGame, List<Integer> activeIndices, int aidx, List<S> current, List<List<S>> result) {
			if (aidx == activeIndices.size()) {
				result.add(new ArrayList<>(current));
				return;
			}
			int i = activeIndices.get(aidx);
			for (S s : nextsPerGame.get(i)) {
				current.set(i, s);
				generateActiveCartesian(states, nextsPerGame, activeIndices, aidx + 1, current, result);
			}
		}

		/**
		 * 与えられた状態が先手勝ち（N-ポジション）かどうかを判定する。
		 *
		 * <p>継続連言和 (Continued Conjunctive Sum / Long-rule Conjunctive Sum) のサスペンスは、
		 * 終了していない (active な) 各コンポーネントゲームのサスペンスの最大値となる。
		 * そのため、最大のサスペンスが奇数であるとき、かつそのときに限り先手勝ちとなる。</p>
		 *
		 * @param states 各ゲームの現在の状態のリスト
		 * @return 先手勝ちなら true、後手勝ちなら false
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code states != null} かつサイズがコンポーネントゲーム数と等しい。</li>
		 *   <li>事後条件: 勝敗判定を返す。</li>
		 *   <li>副作用: 各コンポーネントゲームのメモを更新する可能性がある。</li>
		 *   <li>計算量: $N$ をゲーム数、$V_i, E_i$ を $i$ 番目のゲームの到達可能状態数、遷移数として $O(\sum (V_i + E_i))$</li>
		 * </ul>
		 * </p>
		 */
		// 未テスト
		@Override
		public boolean isWin(List<S> states) {
			int maxSuspense = -1;
			boolean anyActive = false;
			for (int i = 0; i < games.size(); i++) {
				boolean hasMoves = false;
				for (S next : games.get(i).nextStates(states.get(i))) {
					hasMoves = true;
					break;
				}
				if (hasMoves) {
					anyActive = true;
					maxSuspense = Math.max(maxSuspense, games.get(i).suspense(states.get(i)));
				}
			}
			if (!anyActive) {
				return false;
			}
			return maxSuspense % 2 != 0;
		}
	}

	/**
	 * ニム和 (Nim-sum) のクラス。
	 *
	 * <p>複数の山の石の数の排他的論理和 (XOR) を計算し、勝敗判定や最善手の探索を行う。
	 * {@link Sum} のインスタンスとして実装されており、各コンポーネントは {@link Nim} である。</p>
	 */
	public static class NimSum extends Sum<Long> {
		/**
		 * 指定された山の数を持つニムゲームを初期化する。
		 *
		 * @param n 山の数
		 */
		public NimSum(int n) {
			super(Collections.nCopies(n, new Nim()));
		}

		/**
		 * ニム和におけるリモートネス（勝敗が決まるまでの手数）を計算する。
		 *
		 * @param state 各山の石の数のリスト
		 * @return リモートネス。先手勝ちの場合は最短手数（奇数）、後手勝ちの場合は最長手数（偶数）。
		 *
		 * <p>【契約】
		 * <ul>
		 *   <li>事前条件: {@code state} の各要素は 0 以上である。</li>
		 *   <li>事後条件: 理論的なリモートネスの値を返す。</li>
		 *   <li>副作用: なし</li>
		 *   <li>計算量: 山の数を N とすると、負けの状態では XOR 和の計算を除いて O(1)、勝ちの状態では最善手の探索を含めて O(N) である。全体としては O(N) である。</li>
		 *   <li>破壊的変更: なし</li>
		 *   <li>参照共有・所有権: {@code state} への参照は保持されない。</li>
		 *   <li>例外・未定義条件: {@code state} が null の場合は NullPointerException を投げる可能性がある。</li>
		 * </ul>
		 * </p>
		 */
		@Override
		public int remoteness(List<Long> state) {
			long xorSum = 0;
			long sum = 0;
			for (long x : state) {
				xorSum ^= x;
				sum += x;
			}
			if (xorSum == 0) return (int) sum;
			long minNextSum = Long.MAX_VALUE;
			for (long x : state) {
				long nextX = x ^ xorSum;
				if (nextX < x) {
					minNextSum = Math.min(minNextSum, sum - x + nextX);
				}
			}
			return (int) (minNextSum + 1);
		}

		@Override
		public int remoteness(List<Long> state, Map<List<Long>, Integer> memo) {
			return remoteness(state);
		}
	}
}
