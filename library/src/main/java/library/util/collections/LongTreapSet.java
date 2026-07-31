package library.util.collections;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * {@code long} に特殊化した Treap ベースの順序付き集合。
 *
 * <ul>
 * <li>キーに関して二分探索木の順序を満たす</li>
 * <li>優先度(priority)に関して最大ヒープの性質を満たす</li>
 * </ul>
 */
public class LongTreapSet implements Iterable<Long> {
	final static Random rnd = new Random();
	private int size = 0;

	public class Node {
		long key;
		int priority;
		Node left, right;
		int size = 1;

		public Node(long key) {
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

		public long key() {
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

	private Node add(Node node, long key) {
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

	public void add(long key) {
		root = add(root, key);
	}

	public boolean contains(Node node, long key) {
		if (node == null) {
			return false;
		}
		if (key == node.key) {
			return true;
		}
		return key < node.key ? contains(node.left, key) : contains(node.right, key);
	}

	public boolean contains(long key) {
		return contains(root, key);
	}

	private Node remove(Node node, long key) {
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

	public void remove(long key) {
		root = remove(root, key);
	}

	public int size() {
		return size;
	}

	/**
	 * k は 0-origin。存在しなければ {@code null}。
	 */
	public Long kth(int k) {
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
	public Long floor(long key) {
		Node node = root;
		Long res = null;
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
	 * 指定キー以上の最小の値。存在しなければ {@code null}。
	 */
	public Long ceil(long key) {
		Node node = root;
		Long res = null;
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
	public int countLeq(long key) {
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
	 * {@code key} 未満のキーの個数を返します。
	 */
	public int countLess(long key) {
		return countLess(root, key);
	}

	/**
	 * {@code key} 以上のキーの個数を返します。
	 */
	public int countGeq(long key) {
		return size - countLess(key);
	}

	/**
	 * {@code key} より大きいキーの個数を返します。
	 */
	public int countGreater(long key) {
		return size - countLeq(key);
	}

	/**
	 * 集合に含まれない最小の非負整数を返します。
	 *
	 * <p>数学的定義: mex(S) = min { x ∈ ℕ₀ | x ∉ S }</p>
	 *
	 * @return 集合に含まれない最小の非負整数
	 * @complexity O(log N)
	 */
	public long mex() {
		int c = countLeq(-1L);
		long res = (long) size - c;
		Node node = root;
		int offset = 0;
		while (node != null) {
			int rank = offset + (node.left == null ? 0 : node.left.size);
			if (node.key < 0) {
				offset = rank + 1;
				node = node.right;
			} else {
				int nonNegRank = rank - c;
				if (node.key > (long) nonNegRank) {
					res = (long) nonNegRank;
					node = node.left;
				} else {
					offset = rank + 1;
					node = node.right;
				}
			}
		}
		return res;
	}

	private int countLess(Node node, long v) {
		if (node == null) {
			return 0;
		}
		if (v <= node.key) {
			return countLess(node.left, v);
		}
		int leftSize = node.left == null ? 0 : node.left.size;
		return leftSize + 1 + countLess(node.right, v);
	}

	/**
	 * {@code x} 以上の整数のうち、集合に含まれない最小値を返します。Long.MAX_VALUE+1のときはnull
	 */
	public Long firstMissingAtLeast(long x) {
		int rank = countLess(root, x);
		Long found = firstMissingAtLeast(root, 0, rank, x);
		if (found != null) {
			return found;
		}
		if (x + size - rank - 1 == Long.MAX_VALUE) return null;
		return x + size -  rank;
	}
	
	
	private Long firstMissingAtLeast(Node node, int indexOffset, int rank, long x) {
		if (node == null) {
			return null;
		}
		int leftSize = node.left == null ? 0 : node.left.size;
		int nodeIndex = indexOffset + leftSize;//0-indexedにおけるnodeの順位
		if (nodeIndex < rank) {
			return firstMissingAtLeast(node.right, nodeIndex + 1, rank, x);
		}
		long expected = x + nodeIndex - rank;
		if (node.key == expected) {
			return firstMissingAtLeast(node.right, nodeIndex + 1, rank, x);
		}
		Long leftRes = firstMissingAtLeast(node.left, indexOffset, rank, x);
		if (leftRes != null) {
			return leftRes;
		}
		return expected;
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

	public Pair split(long key) {
		return split(root, key);
	}

	/**
	 * Pair.left = key 未満のノードを持つ Treap。
	 * Pair.right = key 以上のノードを持つ Treap。
	 */
	Pair split(Node node, long key) {
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
	public LongTreapSet copy() {
		LongTreapSet res = new LongTreapSet();
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

	//未テスト
	@Override
	public PrimitiveIterator.OfLong iterator() {
		return new PrimitiveIterator.OfLong() {
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
			public long nextLong() {
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
		if (!(obj instanceof LongTreapSet)) return false;
		LongTreapSet other = (LongTreapSet) obj;
		if (this.size != other.size) return false;

		PrimitiveIterator.OfLong it1 = this.iterator();
		PrimitiveIterator.OfLong it2 = other.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			if (it1.nextLong() != it2.nextLong()) return false;
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
		PrimitiveIterator.OfLong it = this.iterator();
		while (it.hasNext()) {
			result = 31 * result + Long.hashCode(it.nextLong());
		}
		return result;
	}
}
