package library.util.collections;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * {@code int} に特殊化した Treap ベースの順序付き集合。
 *
 * <ul>
 * <li>キーに関して二分探索木の順序を満たす</li>
 * <li>優先度(priority)に関して最大ヒープの性質を満たす</li>
 * </ul>
 * 未テスト
 */
public class IntTreapSet implements Iterable<Integer> {
	final static Random rnd = new Random();
	private int size = 0;

	public class Node {
		int key;
		int priority;
		Node left, right;
		int size = 1;

		public Node(int key) {
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

		public int key() {
			return key;
		}
	}

	private Node root;

	private Node rotateRight(Node y) {
		Node x = y.left;
		y.left = x.right;
		x.right = y;
		y.recalc();
		x.recalc();
		return x;
	}

	private Node rotateLeft(Node x) {
		Node y = x.right;
		x.right = y.left;
		y.left = x;
		x.recalc();
		y.recalc();
		return y;
	}

	private Node add(Node node, int key) {
		if (node == null) {
			++size;
			return new Node(key);
		}
		if (key < node.key) {
			node.left = add(node.left, key);
			if (node.left.priority > node.priority) {
				node = rotateRight(node);
			}
		} else if (key > node.key) {
			node.right = add(node.right, key);
			if (node.right.priority > node.priority) {
				node = rotateLeft(node);
			}
		}
		node.recalc();
		return node;
	}

	public void add(int key) {
		root = add(root, key);
	}

	public boolean contains(Node node, int key) {
		if (node == null) {
			return false;
		}
		if (key == node.key) {
			return true;
		}
		return key < node.key ? contains(node.left, key) : contains(node.right, key);
	}

	public boolean contains(int key) {
		return contains(root, key);
	}

	private Node remove(Node node, int key) {
		if (node == null) {
			return null;
		}
		if (key < node.key) {
			node.left = remove(node.left, key);
		} else if (key > node.key) {
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

	public void remove(int key) {
		root = remove(root, key);
	}

	public int size() {
		return size;
	}

	/**
	 * k は 0-origin。存在しなければ {@code null}。
	 */
	public Integer kth(int k) {
		if (k < 0 || k >= size) {
			return null;
		}
		Node node = root;
		while (true) {
			if (node.left != null) {
				if (k < node.left.size) {
					node = node.left;
					continue;
				}
				k -= node.left.size;
			}
			if (k == 0) {
				return node.key;
			}
			k--;
			node = node.right;
		}
	}

	/**
	 * 指定キー以下の最大の値。存在しなければ {@code null}。
	 */
	public Integer floor(int key) {
		Node node = root;
		Integer res = null;
		while (node != null) {
			if (node.key == key) {
				return key;
			}
			if (node.key < key) {
				res = node.key;
				node = node.right;
			} else {
				node = node.left;
			}
		}
		return res;
	}

	/**
	 * 指定キー未満の最大の値。存在しなければ {@code null}。
	 * @complexity O(log N)
	 */
	public Integer lower(int key) {
		Node node = root;
		Integer res = null;
		while (node != null) {
			if (node.key < key) {
				res = node.key;
				node = node.right;
			} else {
				node = node.left;
			}
		}
		return res;
	}

	/**
	 * 指定キーより大きい最小の値。存在しなければ {@code null}。
	 * @complexity O(log N)
	 */
	public Integer higher(int key) {
		Node node = root;
		Integer res = null;
		while (node != null) {
			if (node.key > key) {
				res = node.key;
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return res;
	}

	/**
	 * 指定キー以上の最小の値。存在しなければ {@code null}。
	 */
	public Integer ceil(int key) {
		Node node = root;
		Integer res = null;
		while (node != null) {
			if (node.key == key) {
				return key;
			}
			if (node.key > key) {
				res = node.key;
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return res;
	}

	/**
	 * {@code key} 以下のキーの個数を返します。
	 */
	public int countLeq(int key) {
		Node node = root;
		int res = 0;
		while (node != null) {
			if (node.key <= key) {
				res += 1;
				if (node.left != null) {
					res += node.left.size;
				}
				node = node.right;
			} else {
				node = node.left;
			}
		}
		return res;
	}

	/**
	 * 集合に含まれない最小の非負整数を返します。
	 *
	 * <p>数学的定義: mex(S) = min { x ∈ ℕ₀ | x ∉ S }</p>
	 *
	 * @return 集合に含まれない最小の非負整数
	 * @complexity O(log N)
	 */
	public int mex() {
		int c = countLeq(-1);
		int res = size - c;
		Node node = root;
		int offset = 0;
		while (node != null) {
			int rank = offset + (node.left == null ? 0 : node.left.size);
			if (node.key < 0) {
				offset = rank + 1;
				node = node.right;
			} else {
				int nonNegRank = rank - c;
				if (node.key > nonNegRank) {
					res = nonNegRank;
					node = node.left;
				} else {
					offset = rank + 1;
					node = node.right;
				}
			}
		}
		return res;
	}

	public Node merge(Node l, Node r) {
		if (l == null) {
			return r;
		}
		if (r == null) {
			return l;
		}
		if (l.priority > r.priority) {
			l.right = merge(l.right, r);
			l.recalc();
			return l;
		}
		r.left = merge(l, r.left);
		r.recalc();
		return r;
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

	public Pair split(int key) {
		return split(root, key);
	}

	/**
	 * Pair.left = key 未満のノードを持つ Treap。
	 * Pair.right = key 以上のノードを持つ Treap。
	 */
	Pair split(Node node, int key) {
		if (node == null) {
			return new Pair(null, null);
		}
		if (node.key < key) {
			var a = split(node.right, key);
			node.right = a.left();
			node.recalc();
			return new Pair(node, a.right());
		}
		var a = split(node.left, key);
		node.left = a.right();
		node.recalc();
		return new Pair(a.left(), node);
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
	public IntTreapSet copy() {
		IntTreapSet res = new IntTreapSet();
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
	
	public void addAll(IntTreapSet a) {
		addAll(a.root);
	}
	
	private void addAll(Node node) {
		if(node==null)return;
		add(node.key);
		addAll(node.left);
		addAll(node.right);
	}
	
	//未テスト
	@Override
	public PrimitiveIterator.OfInt iterator() {
		return new PrimitiveIterator.OfInt() {
			private final ArrayDeque<Node> stack = new ArrayDeque<>();

			{
				pushLeftPath(root);
			}

			private void pushLeftPath(Node node) {
				while (node != null) {
					stack.push(node);
					node = node.left;
				}
			}

			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}

			@Override
			public int nextInt() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				Node node = stack.pop();
				pushLeftPath(node.right);
				return node.key;
			}
		};
	}

	@Override
	public String toString() {
		return toString(root);
	}

	private String toString(Node node) {
		if (node == null) {
			return "";
		}
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
		if (!(obj instanceof IntTreapSet)) return false;
		IntTreapSet other = (IntTreapSet) obj;
		if (this.size != other.size) return false;

		PrimitiveIterator.OfInt it1 = this.iterator();
		PrimitiveIterator.OfInt it2 = other.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			if (it1.nextInt() != it2.nextInt()) return false;
		}
		return !it1.hasNext() && !it2.hasNext();
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
		PrimitiveIterator.OfInt it = this.iterator();
		while (it.hasNext()) {
			result = 31 * result + Integer.hashCode(it.nextInt());
		}
		return result;
	}
}
