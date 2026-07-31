package library.util.collections;

import java.util.Random;
import java.util.function.LongBinaryOperator;

/**
 * <ul>
 * <li>キーに関して二分探索木の順序を満たす</li>
 * <li>優先度(priority)に関して最大ヒープの性質を満たす</li>
 * </ul>
 *
 */
public class TreapSet<T extends Comparable<? super T>> {
	final static Random rnd = new Random();
	private int size = 0;

	public class Node {
		T key;
		int priority;
		Node left, right;
		int size = 1;

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

	/**
	 * 右回転（Right Rotation）を行います。
	 *
	 * <p>
	 * 次のように部分木を右回転させます：
	 *
	 * <pre>
	 *     y                x
	 *    / \     =>       / \
	 *   x   C            A   y
	 *  / \                  / \
	 * A   B                B   C
	 * </pre>
	 *
	 * @param y 回転対象となる部分木の根
	 * @return 回転後の新しい部分木の根
	 */
	private Node rotateRight(Node y) {
		Node x = y.left;
		y.left = x.right;
		x.right = y;
		y.recalc();
		x.recalc();
		return x;
	}

	/**
	 * 左回転（Left Rotation）を行います。
	 *
	 * <p>
	 * 次のように部分木を左回転させます：
	 *
	 * <pre>
	 *   x                   y
	 *  / \       =>        / \
	 * A   y               x   C
	 *    / \             / \
	 *   B   C           A   B
	 * </pre>
	 *
	 * @param x 回転対象となる部分木の根
	 * @return 回転後の新しい部分木の根
	 */
	private Node rotateLeft(Node x) {
		Node y = x.right;
		x.right = y.left;
		y.left = x;
		x.recalc();
		y.recalc();
		return y;
	}

	private Node add(Node node, T key) {
		if (node == null) {
			++size;
			return new Node(key);
		}
		int comp = key.compareTo(node.key);
		if (comp < 0) {
			node.left = add(node.left, key);
			if (node.left.priority > node.priority) {
				node = rotateRight(node);
			}
		} else if (comp > 0) {
			node.right = add(node.right, key);
			if (node.right.priority > node.priority) {
				node = rotateLeft(node);
			}
		}
		node.recalc();
		return node;
	}

	public void add(T key) {
		root = add(root, key);
	}

	public boolean contains(Node node, T key) {
		if (node == null)
			return false;
		int comp = key.compareTo(node.key);
		if (comp == 0)
			return true;
		return comp < 0 ? contains(node.left, key) : contains(node.right, key);
	}

	public boolean contains(T key) {
		return contains(root, key);
	}

	/**
	 * キーを削除。
	 * <ol>
	 * <li>二分探索木として削除対象ノードを見つける</li>
	 * <li>優先度に応じて回転し、削除対象ノードを葉へ押し下げる</li>
	 * <li>葉になったら削除する</li>
	 * </ol>
	 *
	 * @param node 部分木の根
	 * @param key  削除するキー
	 * @return 更新後の部分木の根 https://judge.yosupo.jp/submission/348733
	 */
	private Node remove(Node node, T key) {
		if (node == null)
			return null;
		int comp = key.compareTo(node.key);
		if (comp < 0) {
			node.left = remove(node.left, key);
		} else if (comp > 0) {
			node.right = remove(node.right, key);
		} else {
			if (node.left == null && node.right == null) {
				--size;
				return null;
			} else if (node.left == null) {
				node = rotateLeft(node);
				node.left = remove(node.left, key);
			} else if (node.right == null) {
				node = rotateRight(node);
				node.right = remove(node.right, key);
			} else {
				if (node.left.priority > node.right.priority) {
					node = rotateRight(node);
					node.right = remove(node.right, key);
				} else {
					node = rotateLeft(node);
					node.left = remove(node.left, key);
				}
			}
		}
		node.recalc();
		return node;
	}

	/**
	 * キーを削除。存在しなければ何もしない。
	 * @param key
	 */
	public void remove(T key) {
		root = remove(root, key);
	}

	public int size() {
		return size;
	}

	/**
	 * セットが空であるかどうかを判定する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 要素数が0であればtrue、そうでなければfalseを返す。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(1)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	// 未テスト
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * kは0-origin https://judge.yosupo.jp/submission/348733
	 * 存在しなければnull
	 * @param k
	 * @return
	 */
	public T kth(int k) {
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

	/**
	 * https://judge.yosupo.jp/submission/348733
	 * @param key
	 * @return
	 */
	public T floor(T key) {
		Node node = root;
		T res = null;
		while (node != null) {
			int comp = node.key.compareTo(key);
			if (comp == 0)
				return key;
			if (comp < 0) {
				res = node.key;
				node = node.right;
			} else {
				node = node.left;
			}
		}
		return res;
	}

	/**
	 * https://judge.yosupo.jp/submission/348733
	 * @param key
	 * @return
	 */
	public T ceil(T key) {
		Node node = root;
		T res = null;
		while (node != null) {
			int comp = node.key.compareTo(key);
			if (comp == 0)
				return key;
			if (comp > 0) {
				res = node.key;
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return res;
	}

	/**
	 * /** {@code key} 以下のキーの個数を返します。
	 *
	 * https://judge.yosupo.jp/submission/348733 
	 * @param key
	 * @return
	 */
	public int countLeq(T key) {
		Node node = root;
		int res = 0;
		while (node != null) {
			int comp = node.key.compareTo(key);
			if (comp <= 0) {
				res += 1;
				if (node.left != null)
					res += node.left.size;
				node = node.right;
			} else {
				node = node.left;
			}
		}
		return res;
	}

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
	
	public Pair split(T key) {
		return split(root, key);
	}

	/**
	 * Pair.left= key 未満のノードを持つ Treap
	 * Pair.right = key 以上のノードを持つ Treap
	 * @param node 分割する Treap の根
	 * @param key
	 * @return 
	 */
	Pair split(Node node, T key) {
		if (node == null) {
			return new Pair(null, null);
		}
		int comp = node.key.compareTo(key);
		if (comp < 0) {
			var a = split(node.right, key);
			node.right = a.left();
			node.recalc();
			return new Pair(node, a.right());
		} else {
			var a = split(node.left, key);
			node.left = a.right();
			node.recalc();
			return new Pair(a.left(), node);
		}
	}
	
	public Node root() {
		return root;
	}

	/**
	 * この集合のコピー（ディープコピー）を返します。
	 *
	 * @return この集合のコピー
	 * @complexity O(N)
	 */
	// 未テスト
	public TreapSet<T> copy() {
		TreapSet<T> res = new TreapSet<>();
		res.size = this.size;
		res.root = res.copyNode(this.root);
		return res;
	}

	private Node copyNode(Node node) {
		if (node == null) {
			return null;
		}
		Node cp = new Node(node.key);
		cp.priority = node.priority;
		cp.size = node.size;
		cp.left = copyNode(node.left);
		cp.right = copyNode(node.right);
		return cp;
	}

	public String toString() {
		return toString(root);
	}

	private String toString(Node node) {
		if (node == null)
			return "";
		return toString(node.left) + node.key + " " + toString(node.right);
	}

	/**
	 * デバッグ用に集合の内容を昇順に標準出力に出力します。
	 *
	 * 未テスト
	 * @complexity O(N) (N は要素数)
	 */
	public void dump() {
		if (root == null) {
			System.out.println("空集合");
		} else {
			dump(root);
		}
	}

	private void dump(Node node) {
		if (node == null) return;
		dump(node.left);
		System.out.println(node.key);
		dump(node.right);
	}

	/**
	 * この集合と別のオブジェクトの同値性を判定します。
	 * すべての要素が一致する場合に同値とみなします（順序付き集合のため、同一の要素集合を持つか検証します）。
	 *
	 * <p>計算量: $O(N)$（$N$ は要素数）</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TreapSet)) return false;
		TreapSet<?> other = (TreapSet<?>) obj;
		if (this.size != other.size) return false;

		for (int i = 0; i < size; i++) {
			T thisK = this.kth(i);
			Object otherK = other.kth(i);
			if (thisK == null ? otherK != null : !thisK.equals(otherK)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * この集合のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$（$N$ は要素数）</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + size;
		for (int i = 0; i < size; i++) {
			T k = this.kth(i);
			result = 31 * result + (k != null ? k.hashCode() : 0);
		}
		return result;
	}
}