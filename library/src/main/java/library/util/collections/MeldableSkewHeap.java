package library.util.collections;

/** * 重みの遅延加算をサポートする Skew Heap。 */
public class MeldableSkewHeap {
	/** * Skew Heap のノード。 */
	public static class Node {
		public long add, val;
		public int id;
		public Node l, r;

		public Node(long val, int id) {
			this.val = val;
			this.id = id;
		}

		/** * このノードの現在の重みを返す。 * @return 重み */
		public long weight() {
			return val + add;
		}

		/** * 加算値を子ノードに伝播させる。 */
		public void push() {
			if (l != null)
				l.add += add;
			if (r != null)
				r.add += add;
			val += add;
			add = 0;
		}
	}

	/** * 2つのヒープをマージする。 * @param a ヒープ1 * @param b ヒープ2 * @return マージ後のヒープの根 */
	public static Node meld(Node a, Node b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		if (a.weight() > b.weight()) {
			Node tmp = a;
			a = b;
			b = tmp;
		}
		a.push();
		a.r = meld(a.r, b);
		Node tmp = a.l;
		a.l = a.r;
		a.r = tmp;
		return a;
	}

	/** * 最小値を取り除いた後のヒープを返す。 * @param a ヒープの根 * @return 最小値除外後のヒープの根 */
	public static Node pop(Node a) {
		if (a == null)
			return null;
		a.push();
		return meld(a.l, a.r);
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public static void dump(Node node) {
		dumpInternal(node);
		System.out.println();
	}

	private static void dumpInternal(Node node) {
		if (node == null) {
			System.out.print("null");
			return;
		}
		node.push();
		System.out.print("Node(id=" + node.id + ", val=" + node.val + ", add=" + node.add + ") -> [left: ");
		dumpInternal(node.l);
		System.out.print(", right: ");
		dumpInternal(node.r);
		System.out.print("]");
	}
}