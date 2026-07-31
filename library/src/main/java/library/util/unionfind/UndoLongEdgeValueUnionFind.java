package library.util.unionfind;

import java.util.Arrays;
import library.util.algebra.strategy.longs.LongGroupStrategy;
import library.util.collections.LongArrayList;

/**
 * undoができる重み付きUnionFind。
 * 経路圧縮を行うとundoが困難になるため、経路圧縮は行わず、サイズによるマージのみを行う。
 *
 * root(x) および getValue(x) の計算量は O(log N) となる。
 */
public class UndoLongEdgeValueUnionFind {
	//https://atcoder.jp/contests/awc0100/submissions/76984399
    private final int[] parent;
    private final long[] v;
    private final LongGroupStrategy st;
    private int numberConnectedComponents;
    private final LongArrayList history;

    public UndoLongEdgeValueUnionFind(int n, LongGroupStrategy st) {
        this.parent = new int[n];
        Arrays.fill(parent, -1);
        this.v = new long[n];
        Arrays.fill(v, st.identity());
        this.st = st;
        this.numberConnectedComponents = n;
        this.history = new LongArrayList();
    }

    private UndoLongEdgeValueUnionFind(int[] parent, long[] v, LongGroupStrategy st, int numberConnectedComponents, LongArrayList history) {
        this.parent = parent;
        this.v = v;
        this.st = st;
        this.numberConnectedComponents = numberConnectedComponents;
        this.history = history;
    }

    /**
     * UndoLongEdgeValueUnionFindの現在の状態をコピーした新しいインスタンスを返す。
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
    public UndoLongEdgeValueUnionFind copy() {
        int[] nextParent = Arrays.copyOf(this.parent, this.parent.length);
        long[] nextV = Arrays.copyOf(this.v, this.v.length);
        LongArrayList nextHistory = this.history.copy();
        return new UndoLongEdgeValueUnionFind(nextParent, nextV, this.st, this.numberConnectedComponents, nextHistory);
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
     * rootからxまでの重みの累積を返す。
     * @param x
     * @return
     */
    public long getValue(int x) {
        long res = st.identity();
        while (parent[x] >= 0) {
            res = st.mul(v[x], res);
            x = parent[x];
        }
        return res;
    }

    /**
     * getValue(a) = st.mul(getValue(b), w) となるように a と b を併合する。
     * 既に同じ集合に属している場合、矛盾がなければ true を返し、矛盾があれば false を返す。
     * @param a
     * @param b
     * @param w
     * @return 併合に成功（または既に矛盾なく併合済み）なら true
     */
    public boolean union(int a, int b, long w) {
        int ra = root(a);
        int rb = root(b);
        if (ra == rb) {
            history.add(-1);
            history.add(-1);
            history.add(-1);
            history.add(-1);
            history.add(-1);
            return getValue(a) == st.mul(getValue(b), w);
        }

        long va = getValue(a);
        long vb = getValue(b);

        if (-parent[ra] < -parent[rb]) {
            // raをrbの子にする
            // new_getValue(a) = st.mul(v[ra], va) = st.mul(vb, w)
            // v[ra] = st.mul(st.mul(vb, w), st.inverse(va))
            long nvw = st.mul(st.mul(vb, w), st.inverse(va));
            history.add(ra);
            history.add(rb);
            history.add(parent[ra]);
            history.add(parent[rb]);
            history.add(v[ra]);

            parent[rb] += parent[ra];
            parent[ra] = rb;
            v[ra] = nvw;
        } else {
            // rbをraの子にする
            // new_getValue(b) = st.mul(v[rb], vb)
            // va = st.mul(new_getValue(b), w) = st.mul(st.mul(v[rb], vb), w)
            // st.mul(v[rb], vb) = st.mul(va, st.inverse(w))
            // v[rb] = st.mul(st.mul(va, st.inverse(w)), st.inverse(vb))
            long nvw = st.mul(st.mul(va, st.inverse(w)), st.inverse(vb));
            history.add(rb);
            history.add(ra);
            history.add(parent[rb]);
            history.add(parent[ra]);
            history.add(v[rb]);

            parent[ra] += parent[rb];
            parent[rb] = ra;
            v[rb] = nvw;
        }
        numberConnectedComponents--;
        return true;
    }

    /**
     * 直前の union 操作を取り消す。
     */
    public void undo() {
        if (history.isEmpty()) return;
        long oldVChild = history.pollLast();
        int oldParParent = (int) history.pollLast();
        int oldParChild = (int) history.pollLast();
        int parentRoot = (int) history.pollLast();
        int childRoot = (int) history.pollLast();

        if (childRoot != -1) {
            parent[childRoot] = oldParChild;
            parent[parentRoot] = oldParParent;
            v[childRoot] = oldVChild;
            numberConnectedComponents++;
        }
    }

    /**
     * 全ての union 操作を取り消す。
     */
    public void reset() {
        while (!history.isEmpty()) {
            undo();
        }
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
	 * UndoLongEdgeValueUnionFindの現在の状態と各要素の重みを、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素と重み（rootからの累積重み）を括弧で括った文字列を返す。</li>
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
					int u = groups[i][j];
					sb.append(u).append("(").append(getValue(u)).append(")");
					if (j < groups[i].length - 1) sb.append(", ");
				}
				sb.append("}");
			}
		}
		return sb.toString();
	}

	/**
	 * UndoLongEdgeValueUnionFindの現在の状態と各要素の重みを、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素と重み（rootからの累積重み）を括弧で括って出力する。</li>
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
