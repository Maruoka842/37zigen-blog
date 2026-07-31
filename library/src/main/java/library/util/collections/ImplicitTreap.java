package library.util.collections;

import java.util.Random;

public class ImplicitTreap<T> {
	Random rnd=new Random();
	int size = 0;
	public class Node {
		T key;
		Node left, right;
		int size = 1;
		int priority;

		public Node(T key) {
			this.key = key;
			this.priority = rnd.nextInt();
			recalc();
		}

		public void recalc() {
			size = 1;
			if (left != null) {
				size += left.size;
			}
			if (right != null) {
				size += right.size;
			}
		}
		
		public int size() {
			return size;
		}
		
		public T key() {
			return key;
		}

	}

	private Node root;

	public Node merge(Node l, Node r) {
		if (l == null)
			return r;
		if (r == null)
			return l;
		if (l.priority > r.priority) {
			l.right = merge(l.right, r);
			l.recalc();
			return l;
		} else {
			r.left = merge(l, r.left);
			r.recalc();
			return r;
		}
	}

	public class Pair {
		private final Node left;
		private final Node right;

		public Pair(Node left, Node right) {
			this.left = left;
			this.right = right;
		}

		public Node left() {
			return left;
		}

		public Node right() {
			return right;
		}
	}
	
	/**
	 * Pair.left= 先頭k個
	 * Pair.right = それ以外
	 * @param node 分割する Treap の根
	 * @param key
	 * @return 
	 */
	Pair split(Node node, int k) {
		if (node == null) {
			return new Pair(null, null);
		}
		if (size(node.left) < k) {
			var a = split(node.right, k - size(node.left) - 1);
			node.right = a.left();
			node.recalc();
			return new Pair(node, a.right());
		} else {
			var a = split(node.left, k);
			node.left = a.right();
			node.recalc();
			return new Pair(a.left(), node);
		}
	}
	
	/**
	 * pos番目(0indexed)にvalを挿入
	 * @param pos
	 * @param val
	 */
	public Node insert(int pos, T val) {
		//https://atcoder.jp/contests/abc392/submissions/73847892
		if (pos < 0 || pos > size) throw new AssertionError();
		Node node = new Node(val);
	    Pair p = split(root, pos);
	    root = merge(merge(p.left(), node), p.right());
	    size++;
	    return node;
	}
	
	/**
	 * pos番目(0indexed)を削除
	 * 未テスト
	 * @param pos
	 * @param coeffs
	 */
	public void erase(int pos) {
		if (pos < 0 || pos >= size) throw new AssertionError();
		Pair p1 = split(root, pos);
	    Pair p2 = split(p1.right(), 1);
	    root = merge(p1.left(), p2.right());
	    --size;
	}
	
	public int size() {
		return size;
	}

	/**
	 * トリープが空であるかどうかを判定する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 要素数が0であればtrue、そうでなければfalseを返す。</li>
	 *   <li>副作用: なし.</li>
	 *   <li>計算量: $O(1)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	// 未テスト
	public boolean isEmpty() {
		return size == 0;
	}
	
	public Node root() {
		return root;
	}

	public String toString() {
		return toString(root);
	}
	
	private int size(Node node) {
		return node==null?0:node.size;
	}
	
	
	/**
	 * kは0-origin https://judge.yosupo.jp/submission/348733
	 * 存在しなければnull
	 * @param k
	 * @return
	 */
	public T kth(int k) {
		//https://atcoder.jp/contests/abc392/submissions/73847892
		if (k < 0 || k >= size)
			return null;
		Node node = root;
		while (true) {
			if (node.left != null) {
				if (k < node.left.size) {
					node = node.left;
					continue;
				} else {
					k -= node.left.size;
				}
			}
			if (k == 0)
				return node.key;
			k--;
			node = node.right;
		}
	}

	
	
	private String toString(Node node) {
		if (node == null)
			return "";
		return toString(node.left) + node.key + " " + toString(node.right);
	}

	/**
	 * デバッグ用に集合の内容を昇順（インデックス順）に標準出力に出力します。
	 *
	 * 未テスト
	 * @complexity O(N) (N は要素数)
	 */
	public void dump() {
		if (root == null) {
			System.out.println("空集合");
		} else {
			dump(root, 0);
		}
	}

	private int dump(Node node, int index) {
		if (node == null) return index;
		int nextIndex = dump(node.left, index);
		System.out.println(nextIndex + ": " + node.key);
		return dump(node.right, nextIndex + 1);
	}
}
