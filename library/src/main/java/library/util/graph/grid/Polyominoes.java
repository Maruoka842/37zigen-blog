package library.util.graph.grid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import library.util.ArrayUtils;
import library.util.collections.IntQueue;

public class Polyominoes {
	/**
	 * ポリオミノを列挙する。
	 *
	 * <p>
	 * このクラスでは、Redelmeier 法を用いて、
	 * サイズ {@code n} 以下のポリオミノを列挙します。
	 * </p>
	 *
	 * <p>
	 * ただし、このメソッドが返す {@code int[][]} はポリオミノそのものだけを表す配列ではなく、
	 * 探索状態全体を表す内部表現です。
	 * 各要素の意味は次の通りです。
	 * </p>
	 *
	 * <ul>
	 * <li>{@code 0} : 未登場のマス</li>
	 * <li>{@code < 0} : frontier に入っているが、まだ採用されていないマス</li>
	 * <li>{@code > 0} : すでにポリオミノに採用済みのマス</li>
	 * </ul>
	 *
	 * <p>
	 * そのため、返り値をそのまま「形」として使うのではなく、
	 * 必要に応じて {@code > 0} のマスだけを取り出して解釈してください。
	 * </p>
	 * 
	 * 任意の polyomino に対して：
	 * 上にずらして「最上段が 0 行になる」ようにする
	 * その最上段の中で
	 * 一番左のマスが (0, n-1) に来るように横にずらす
	 * この位置に来る配置 だけ が列挙されます。
	 */
	public static List<int[][]> enumerateUpTo(int n) {
		ArrayList<int[][]> ret=new ArrayList<>();
		class State {
			int[][] id; // 0: 未登場, < 0: frontier に入った順番, > 0 : 採用済み
			IntQueue frontier; // frontier のマス一覧（座標を H * i + j で持つ）
			int size; // 現在の polyomino サイズ
			int pointer; // 次に使う id の番号

			public State(int[][] id, IntQueue frontier, int size, int pointer) {
				this.id = id;
				this.frontier = frontier;
				this.size = size;
				this.pointer = pointer;
			}
		}
		int H = 2 * n;
		int W = 2 * n;
		Queue<State> que = new ArrayDeque<>();
		{
			int pointer = 1;
			int size = 0;
			int[][] id = new int[H][W];
			id[0][n - 1] = -pointer++;
			IntQueue frontier = new IntQueue();
			frontier.add(W * 0 + (n - 1));
			State state = new State(id, frontier, size, pointer);
			que.add(state);
		}

		while (!que.isEmpty()) {
			State cur = que.poll();
			if (cur.size > 0) {
				ret.add(cur.id);
			}
			if (cur.size == n) {
				continue;
			}

			int frontierSize = cur.frontier.size();

			// frontier の各候補を 1 個ずつ選んで追加
			for (int pick = 0; pick < frontierSize; pick++) {
				int cell = cur.frontier.get(pick);
				int ci = cell / W;
				int cj = cell % W;

				int[][] nid = ArrayUtils.copy(cur.id);
				nid[ci][cj] *= -1;
				int baseId = nid[ci][cj];
				IntQueue nfrontier = new IntQueue();
				int npointer = cur.pointer;
				// 使わなかった frontier のうち、
				// Redelmeier 的に「baseId より後に追加されたもの」だけ残す
				for (int t = 0; t < frontierSize; t++) {
					int v = cur.frontier.get(t);
					int vi = v / W;
					int vj = v % W;
					if (cur.id[vi][vj] < -baseId) {
						nfrontier.add(v);
					}
				}
				// 新しく増える隣接マスを frontier に追加
				for (int d = 0; d < 4; d++) {
					int ni = ci + Grid2D.dh[d];
					int nj = cj + Grid2D.dw[d];

					if (ni < 0 || ni >= H || nj < 0 || nj >= W)
						continue;
					if (ni == 0 && nj < n - 1)
						continue;// これがないと、##が、最初の場所から右にいくか左に行くかで2通り数えられてしまう
					// すでに登場済みなら追加しない
					if (nid[ni][nj] != 0)
						continue;

					nid[ni][nj] = -npointer++;
					nfrontier.add(W * ni + nj);
				}

				que.add(new State(nid, nfrontier, cur.size + 1, npointer));
			}
		}
		return ret;
	}

}
