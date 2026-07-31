package library.util.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongBinaryOperator;

/**
 * <ul>
 *   <li>キーに関して二分探索木の順序を満たす</li>
 *   <li>優先度(priority)に関して最大ヒープの性質を満たす</li>
 * </ul>
 *
 */
public class TreapMap {
	final static Random rnd = new Random();
	private int size = 0;
	
	static class Node {
		long key;
		int priority;
		Node left, right;
		int size;
		long sum;
		long val;
		
		public Node(long key, long val) {
			this.key = key;
			this.val = val;
			this.priority = rnd.nextInt();
			recalc(this);
		}
	}
	
	private Node root;
	
	public static void recalc(Node t) {
		if (t==null)return;
		t.sum = t.val;
		t.size = 1;
		if (t.left != null) {
			t.sum += t.left.sum;
			t.size += t.left.size; 
		}
		if(t.right != null) {
			t.sum += t.right.sum;
			t.size += t.right.size;
		}
	}
	
    /**
     * 右回転（Right Rotation）を行います。
     *
     * <p>次のように部分木を右回転させます：
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
		Node x=y.left;
		y.left=x.right;
		x.right=y;
		recalc(y);
		recalc(x);
		return x;
	}
	
    /**
     * 左回転（Left Rotation）を行います。
     *
     * <p>次のように部分木を左回転させます：
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
		Node y=x.right;
		x.right=y.left;
		y.left=x;
		recalc(x);
		recalc(y);
		return y;
	}
	
	private Node put(Node node, long key, long val) {
		if (node == null) {
			++size;
			return new Node(key, val);
		} 
		if (key < node.key) {
			node.left = put(node.left, key, val);
			if (node.left.priority > node.priority) {
				node = rotateRight(node);
			}
		} else if (key > node.key) {
			node.right = put(node.right, key, val);
			if (node.right.priority > node.priority) {
				node = rotateLeft(node);
			}
		} else {
			node.val = val;
		}
		recalc(node);
		return node;
	}
	
	public void put(long key, long val) {
		root=put(root, key, val);
	}
	
	public long get(long key) {
		return get(getRoot(), key);
	}
	
	public long getOrDefault(long key, long defaultValue) {
		return getOrDefaultValue(getRoot(), key, defaultValue);
	}
	
	private long getOrDefaultValue(Node node, long key, long defaultValue) {
		if(node==null)return defaultValue;
		if(key==node.key)return node.val;
		return key<node.key ? getOrDefaultValue(node.left, key, defaultValue) : getOrDefaultValue(node.right, key, defaultValue);
	}

	public void merge(long key, long value, LongBinaryOperator op) {
		if (containsKey(key)) {
			put(key, op.applyAsLong(get(key), value));
		} else {
			put(key, value);
		}
	}


	
	private long get(Node node, long key) {
		if(node==null)throw new AssertionError();
		if(key==node.key)return node.val;
		return key<node.key ? get(node.left, key) : get(node.right, key);
	}
	
	public boolean contains(Node node, long key) {
		if(node==null)return false;
		if(key==node.key)return true;
		return key<node.key ? contains(node.left, key) : contains(node.right, key);
	}
	
	public boolean containsKey(long key) {
		return contains(getRoot(), key);
	}
	
    /**
     * Treap から指定したキーを削除。
     *
     * <p>
     * 削除は次の手順で行われます：
     * <ol>
     *   <li>二分探索木として削除対象ノードを見つける</li>
     *   <li>優先度に応じて回転し、削除対象ノードを葉へ押し下げる</li>
     *   <li>葉になったら削除する</li>
     * </ol>
     *
     * @param node 部分木の根
     * @param key 削除するキー
     * @return 更新後の部分木の根
     * https://judge.yosupo.jp/submission/336359
     */
	private Node remove(Node node, long key) {
		if (node == null)return null;
		if(key<node.key) {
			node.left=remove(node.left, key);
		} else if (key>node.key) {
			node.right=remove(node.right, key);
		} else {
			if(node.left==null&&node.right==null) {
				--size;
				return null;
			} else if(node.left==null) {
				node = rotateLeft(node);
				node.left = remove(node.left, key);
			} else if(node.right==null) {
				node = rotateRight(node);
				node.right=remove(node.right, key);
			}else {
				if(node.left.priority>node.right.priority) {
					node=rotateRight(node);
					node.right=remove(node.right, key);
				} else {
					node=rotateLeft(node);
					node.left=remove(node.left,key);
				}
			}
		}
		recalc(node);
		return node;
	}
	
	public void remove(long key) {
		root=remove(getRoot(),key);
	}
	
	/**
	 *  keyを0-indexedで並べたときのa番目からb-1番目までのvalの和をO(logN)で返す。
	 *  https://atcoder.jp/contests/past18-open/editorial/10099
	 * @param a
	 * @param b
	 * @return
	 */
	public long rangeSumByIndex(int a, int b) {
		a=Math.max(0, a);
		b=Math.min(b, size);
		if(b<=a)return 0;
		return rangeSumByIndex(a, b, getRoot());
	}

	private long rangeSumByIndex(int a, int b, Node node) {
		if(node==null||b<=0)return 0;
		if(node.size<=a)return 0;
		if(a <= 0 && node.size <= b) {
			return node.sum;
		}
		int leftSize=node.left==null?0:node.left.size;
		long res=0;
		if (node.left != null) {
			res += rangeSumByIndex(a, b, node.left);
		}
		if(a <= leftSize && leftSize < b) {
			res += node.val;
		}
		return res+rangeSumByIndex(a-leftSize-1, b-leftSize-1, node.right);
	}

	
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	/**
	 * kは0-origin
	 * https://judge.yosupo.jp/submission/336359
	 * @param k
	 * @return
	 */
	public Long kthKey(int k) {
		if (k < 0 || k >= size) return null;
		Node node = getRoot();
		while(true) {
			if(node.left!=null) {
				if(k<node.left.size) {
					node=node.left;
					continue;
				} else {
					k-=node.left.size;
				}
			}
			if(k==0)return node.key;
			k--;
			node=node.right;
		}
	}
	
	public Long firstKey() {
		if(getRoot()==null) return null;
		Node node = getRoot();
		while (node.left != null) {
			node = node.left;
		}
		return node.key;
	}

	/**
	 * https://atcoder.jp/contests/abc406/submissions/73198102
	 * @return
	 */
	public Entry firstEntry() {
		if(getRoot()==null) return null;
		Node node = getRoot();
		while (node.left != null) {
			node = node.left;
		}
		return new Entry(node.key, node.val);
	}
	
	/**
	 * https://atcoder.jp/contests/abc406/submissions/73198102
	 * @return
	 */
	public Entry lastEntry() {
		if(getRoot()==null) return null;
		Node node = getRoot();
		while (node.right != null) {
			node = node.right;
		}
		return new Entry(node.key, node.val);
	}
	
	
	public Long lastKey() {
		if(getRoot()==null) return null;
		Node node = getRoot();
		while (node.right != null) {
			node = node.right;
		}
		return node.key;
	}
	
	
	public static class Entry {
		public final long key;
		public final long value;
		
		public Entry(long key, long value) {
			this.key = key;
			this.value = value;
		}
	}
	
	
	/**
	 * https://judge.yosupo.jp/submission/336359
	 * @param key
	 * @return
	 */
	public Long floorKey(long key) {
		Node node = getRoot();
		Long res=null;
		while(node != null) {
			if(node.key==key)return key;
			if (node.key < key) {
				res=node.key;
				node=node.right;
			}else {
				node=node.left;
			}
		}
		return res;
	}

	/**
	 * key より小さい最大のキーを返します。
	 * @param key
	 * @return
	 */
	public Long lowerKey(long key) {
		Node node = getRoot();
		Long res = null;
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
	 * key より大きい最小のキーを返します。
	 * @param key
	 * @return
	 */
	public Long higherKey(long key) {
		Node node = getRoot();
		Long res = null;
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
	 * https://judge.yosupo.jp/submission/336359
	 * @param key
	 * @return
	 */
	public Long ceilKey(long key) {
		Node node = getRoot();
		Long res=null;
		while(node != null) {
			if(node.key==key)return key;
			if (node.key > key) {
				res=node.key;
				node=node.left;
			}else {
				node=node.right;
			}
		}
		return res;
	}
	
	/**
	 * 	/**
	 * {@code key} 以下のキーの個数を返します。
	 *
	 * https://judge.yosupo.jp/submission/336359
	 * @param key
	 * @return
	 */
	public long countLeq(long key) {
		Node node = getRoot();
		int res=0;
		while(node != null) {
			if (node.key <= key) {
				res += 1;
				if (node.left != null) res += node.left.size;
				node=node.right;
			}else {
				node=node.left;
			}
		}
		return res;
	}
	
	/**
	 * キーが {@code [l, r)} に含まれるノードの val の総和を返します。
	 * @param l 下限キー（含む）
	 * @param r 上限キー（含まない）
	 * @return キーが {@code [l, r)} に含まれる val の総和
	 * https://atcoder.jp/contests/abc255/submissions/72080766
	 */
	public long rangeSum(long l, long r) {
		if (l >= r) return 0;
		return rangeSumByKeyValue(getRoot(), l, r, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	
	private long rangeSumByKeyValue(Node node, long l, long r, long a, long b) {
		if (node == null) return 0;
		if (b <= l || r <= a) return 0;
		if (l <= a && b <= r) {
			return node.sum;
		}
		long res = 0;
		if (l <= node.key && node.key < r) {
			res += node.val;
		}
		res += rangeSumByKeyValue(node.left, l, r, a, node.key);
		res += rangeSumByKeyValue(node.right, l, r, node.key + 1, b);
		return res;
	}
	
	public Node merge(Node l, Node r) {
		if (l == null) return r;
		if (r == null) return l;
		if (l.priority > r.priority) {
			l.right = merge(l.right , r);
			recalc(l);
			return l;
		} else {
			r.left = merge(l, r.left);
			recalc(r);
			return r;
		}
	}
	
	/**
     * @param node 分割する Treap の根
     * @param key
     * @return Node[] {key 未満のノードを持つ Treap, key 以上のノードを持つ Treap}
	 */
	public Node[] split(Node node, long key) {
		if (node==null) {
			return new Node[] {null, null};
		}
		if (node.key < key) {
			var a=split(node.right, key);
			node.right = a[0];
			recalc(node);
			return new Node[] {node, a[1]};
		} else {
			var a=split(node.left, key);
			node.left = a[1];
			recalc(node);
			return new Node[] {a[0], node};
		}
	}
	
	/**
	 * https://atcoder.jp/contests/abc406/submissions/73198102
	 * @return
	 */
	public List<Entry> entryList() {
		List<Entry> res=new ArrayList<>();
		inOrderEntries(getRoot(), res);
		return res;
	}
	
	private void inOrderEntries(Node node, List<Entry> res) {
	    if (node == null) return;
	    inOrderEntries(node.left, res);
	    res.add(new Entry(node.key, node.val));
	    inOrderEntries(node.right, res);
	}
	
	public String toString() {
		return toString(getRoot());
	}
	
	private String toString(Node node) {
		if (node==null)return "";
		return toString(node.left)+"("+node.key+","+node.val+")"+toString(node.right);
	}

	public Node getRoot() {
		return root;
	}

	/**
	 * このマップのコピー（ディープコピー）を返します。
	 *
	 * @return このマップのコピー
	 * @complexity O(N)
	 */
	// 未テスト
	public TreapMap copy() {
		TreapMap res = new TreapMap();
		res.size = this.size;
		res.root = res.copyNode(this.root);
		return res;
	}

	private Node copyNode(Node node) {
		if (node == null) {
			return null;
		}
		Node cp = new Node(node.key, node.val);
		cp.priority = node.priority;
		cp.size = node.size;
		cp.sum = node.sum;
		cp.left = copyNode(node.left);
		cp.right = copyNode(node.right);
		return cp;
	}
	
	
	
	public void verify() {
	    verify(root, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	private void verify(Node node, long minKey, long maxKey) {
	    if (node == null) return;

	    // 1. 二分探索木の性質チェック
	    if (node.key < minKey || node.key > maxKey) {
	        throw new AssertionError("BST property violated at key: " + node.key);
	    }

	    // 2. ヒープの性質（優先度）チェック
	    if (node.left != null && node.left.priority > node.priority) {
	        throw new AssertionError("Priority property violated at left of: " + node.key);
	    }
	    if (node.right != null && node.right.priority > node.priority) {
	        throw new AssertionError("Priority property violated at right of: " + node.key);
	    }

	    // 3. recalc 内容の整合性チェック
	    long expectedSum = node.val;
	    int expectedSize = 1;

	    if (node.left != null) {
	        verify(node.left, minKey, node.key - 1);
	        expectedSum += node.left.sum;
	        expectedSize += node.left.size;
	    }
	    if (node.right != null) {
	        verify(node.right, node.key + 1, maxKey);
	        expectedSum += node.right.sum;
	        expectedSize += node.right.size;
	    }

	    if (node.sum != expectedSum) {
	        throw new AssertionError("Sum mismatch at key " + node.key + ": expected " + expectedSum + " but got " + node.sum);
	    }
	    if (node.size != expectedSize) {
	        throw new AssertionError("Size mismatch at key " + node.key + ": expected " + expectedSize + " but got " + node.size);
	    }
	}

	public void dumpTree() {
	    System.out.println("=== Treap Dump (size=" + size + ") ===");
	    dumpRoot(root);
	    System.out.println("===================================");
	}

	private void dumpRoot(Node root) {
	    if (root == null) {
	        System.out.println("(empty)");
	        return;
	    }
	    System.out.println(
	        "[Root] " +
	        "key=" + root.key +
	        ", pri=" + root.priority +
	        ", val=" + root.val +
	        ", sum=" + root.sum +
	        ", size=" + root.size
	    );

	    // 左を上、右を下
	    if (root.left != null)
	        dump(root.left, "", root.right == null, "L");
	    if (root.right != null)
	        dump(root.right, "", true, "R");
	}

	private void dump(Node node, String prefix, boolean isTail, String pos) {
	    if (node == null) return;

	    System.out.print(prefix);
	    System.out.print(isTail ? "└── " : "├── ");
	    System.out.println(
	        "[" + pos + "] " +
	        "key=" + node.key +
	        ", pri=" + node.priority +
	        ", val=" + node.val +
	        ", sum=" + node.sum +
	        ", size=" + node.size
	    );

	    String childPrefix = prefix + (isTail ? "    " : "│   ");

	    if (node.left != null)
	        dump(node.left, childPrefix, node.right == null, "L");
	    if (node.right != null)
	        dump(node.right, childPrefix, true, "R");
	}	
	
	/**
	 * このマップと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TreapMap)) return false;
		TreapMap other = (TreapMap) o;
		if (this.size() != other.size()) return false;
		List<Entry> thisEntries = this.entryList();
		List<Entry> otherEntries = other.entryList();
		for (int i = 0; i < thisEntries.size(); i++) {
			Entry e1 = thisEntries.get(i);
			Entry e2 = otherEntries.get(i);
			if (e1.key != e2.key || e1.value != e2.value) {
				return false;
			}
		}
		return true;
	}

	/**
	 * このマップのハッシュコード値を返します。
	 * @return ハッシュコード値
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public int hashCode() {
		int h = 0;
		List<Entry> entries = this.entryList();
		for (Entry entry : entries) {
			h += Long.hashCode(entry.key) ^ Long.hashCode(entry.value);
		}
		return h;
	}
}