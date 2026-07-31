package library.util.unionfind;

import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * undoができるUnionFind。
 * 経路圧縮を行うとundoが困難になるため、経路圧縮は行わず、サイズによるマージのみを行う。
 *
 * root(x) の計算量は O(log N) となる。
 */
public class UndoUnionFind {
    private final int[] parent;
    private int numberConnectedComponents;
    private final IntArrayList history;

    public UndoUnionFind(int n) {
        parent = new int[n];
        Arrays.fill(parent, -1);
        numberConnectedComponents = n;
        history = new IntArrayList();
    }

    private UndoUnionFind(int[] parent, int numberConnectedComponents, IntArrayList history) {
        this.parent = parent;
        this.numberConnectedComponents = numberConnectedComponents;
        this.history = history;
    }

    /**
     * UndoUnionFindの現在の状態をコピーした新しいインスタンスを返す。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: コピー元の状態とは独立した新しいインスタンスを返す。コピー元、コピー先に対する変更は互いに影響しない。</li>
     *   <li>副作用: なし。</li>
     *   <li>計算量: $O(N + H)$ (ここで $N$ は要素数、$H$ は履歴のサイズ)</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * 未テスト
     */
    // 未テスト
    public UndoUnionFind copy() {
        int[] nextParent = Arrays.copyOf(this.parent, this.parent.length);
        IntArrayList nextHistory = this.history.copy();
        return new UndoUnionFind(nextParent, this.numberConnectedComponents, nextHistory);
    }

    public int size() {
        return parent.length;
    }

    public int root(int x) {
        while (parent[x] >= 0) {
            x = parent[x];
        }
        return x;
    }

    public boolean isRoot(int x) {
        return parent[x] < 0;
    }

    /**
     * xとyを併合する。
     * @param x
     * @param y
     * @return 実際に併合された場合true、既に同じ集合だった場合false
     */
    public boolean union(int x, int y) {
        x = root(x);
        y = root(y);
        if (x == y) {
            history.add(-1);
            history.add(-1);
            history.add(-1);
            history.add(-1);
            return false;
        }
        // サイズによるマージ
        if (size(x) < size(y)) {
            int tmp = x;
            x = y;
            y = tmp;
        }
        // yをxにマージする
        history.add(x);
        history.add(y);
        history.add(parent[x]);
        history.add(parent[y]);

        parent[x] += parent[y];
        parent[y] = x;
        numberConnectedComponents--;
        return true;
    }

    /**
     * 直前のunion操作を取り消す。
     */
    public void undo() {
        if (history.isEmpty()) return;
        int py = history.pollLast();
        int px = history.pollLast();
        int y = history.pollLast();
        int x = history.pollLast();
        if (x != -1) {
            parent[x] = px;
            parent[y] = py;
            numberConnectedComponents++;
        }
    }

    /**
     * 全てのunion操作を取り消す。
     */
    public void reset() {
        while (!history.isEmpty()) {
            undo();
        }
    }

    public boolean equiv(int x, int y) {
        return root(x) == root(y);
    }

    public int size(int x) {
        return -parent[root(x)];
    }

    public int numberConnectedComponents() {
        return numberConnectedComponents;
    }

    /**
     * 現在の状態を保存するためのスナップショット（現在の履歴サイズ）を返す。
     * @return スナップショット
     */
    public int snapshot() {
        return history.size();
    }

    /**
     * 指定したスナップショットの状態までロールバックする。
     * @param snapshot snapshot() で取得した値
     */
    public void rollback(int snapshot) {
        while (history.size() > snapshot) {
            undo();
        }
    }

	/**
	 * UndoUnionFindの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(N \log N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		int n = parent.length;
		int[] root = new int[n];
		for (int i = 0; i < n; i++) root[i] = root(i);
		int[] count = new int[n];
		for (int i = 0; i < n; i++) count[root[i]]++;
		int[][] groups = new int[n][];
		for (int i = 0; i < n; i++) if (count[i] > 0) groups[i] = new int[count[i]];
		int[] ptr = new int[n];
		for (int i = 0; i < n; i++) groups[root[i]][ptr[root[i]]++] = i;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			if (groups[i] != null) {
				sb.append("{");
				for (int j = 0; j < groups[i].length; j++) {
					sb.append(groups[i][j]);
					if (j < groups[i].length - 1) sb.append(", ");
				}
				sb.append("}");
			}
		}
		return sb.toString();
	}

	/**
	 * UndoUnionFindの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括って出力する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \log N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
}
