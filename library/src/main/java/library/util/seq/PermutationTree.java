package library.util.seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 順列木 (Permutation Tree) を構築するクラス。
 *
 * <p>順列 P において、値が連続する区間（共通区間、Common Interval）をノードとして木構造を構築する。
 * 各ノードは以下のいずれかのタイプを持つ：
 * <ul>
 *   <li>Leaf: 1要素の区間</li>
 *   <li>JoinAsc: 子ノードの区間を結合すると値が増加していく連続した区間</li>
 *   <li>JoinDesc: 子ノードの区間を結合すると値が減少していく連続した区間</li>
 *   <li>Cut: 2つ以上の共通区間に分割できない（素な）共通区間</li>
 * </ul>
 * </p>
 *
 * <p>計算量: O(N log N)</p>
 */
public class PermutationTree {
    public enum NodeType {
        JoinAsc,    // Join，特に P[i] の値が増加していく
        JoinDesc,   // Join，特に P[i] の値が減少していく
        Cut,        // Cut
        Leaf,       // 葉である
        None,
    }

    public static class Node {
        public NodeType tp;
        public int L, R;                // [L, R) : 頂点が表す区間
        public int mini, maxi;          // 区間に含まれる P[i] (L <= i < R) の最小・最大値
        public List<Integer> child = new ArrayList<>();  // 子の頂点番号（昇順）

        public Node(NodeType tp, int L, int R, int mini, int maxi) {
            this.tp = tp;
            this.L = L;
            this.R = R;
            this.mini = mini;
            this.maxi = maxi;
        }

        @Override
        public String toString() {
            return String.format("Node{tp=%s, L=%d, R=%d, mini=%d, maxi=%d, child=%s}", tp, L, R, mini, maxi, child);
        }
    }

    public List<Node> nodes = new ArrayList<>();
    public int root;

	/**
	 * 順列木の状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * // 未テスト
	 */
	public void dump() {
		System.out.println("PermutationTree root=" + root + " totalNodes=" + nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			System.out.println(String.format("Node %d: tp=%s [%d, %d) mini=%d maxi=%d child=%s",
				i, node.tp, node.L, node.R, node.mini, node.maxi, node.child));
		}
	}

    public PermutationTree(int[] P) {
        int n = P.length;
        if (n == 0) {
            root = -1;
            return;
        }

        int[] maxStack = new int[n];
        int[] minStack = new int[n];
        int maxTop = 0, minTop = 0;

        SegTree seg = new SegTree(n);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            // Update f(j, i) = max(j, i) - min(j, i) - (i - j)
            // Initially f(i, i) = 0
            seg.activate(i);
            if (i > 0) seg.add(0, i, -1);

            while (maxTop > 0 && P[maxStack[maxTop - 1]] < P[i]) {
                int idx = maxStack[maxTop - 1];
                int prev = (maxTop > 1) ? maxStack[maxTop - 2] + 1 : 0;
                seg.add(prev, idx + 1, P[i] - P[idx]);
                maxTop--;
            }
            maxStack[maxTop++] = i;

            while (minTop > 0 && P[minStack[minTop - 1]] > P[i]) {
                int idx = minStack[minTop - 1];
                int prev = (minTop > 1) ? minStack[minTop - 2] + 1 : 0;
                seg.add(prev, idx + 1, P[minStack[minTop - 1]] - P[i]);
                minTop--;
            }
            minStack[minTop++] = i;

            int uIdx = nodes.size();
            nodes.add(new Node(NodeType.Leaf, i, i + 1, P[i], P[i]));

            while (!stack.isEmpty()) {
                Node u = nodes.get(uIdx);
                Node v = nodes.get(stack.peek());
                if (isContiguous(v, u)) {
                    int vIdx = stack.pop();
                    NodeType targetTp = v.mini < u.mini ? NodeType.JoinAsc : NodeType.JoinDesc;
                    if (v.tp == targetTp) {
                        v.child.add(uIdx);
                        v.R = u.R;
                        v.mini = Math.min(v.mini, u.mini);
                        v.maxi = Math.max(v.maxi, u.maxi);
                        uIdx = vIdx;
                    } else {
                        Node newNode = new Node(targetTp, v.L, u.R, Math.min(v.mini, u.mini), Math.max(v.maxi, u.maxi));
                        newNode.child.add(vIdx);
                        newNode.child.add(uIdx);
                        uIdx = nodes.size();
                        nodes.add(newNode);
                    }
                } else {
                    int rightL = seg.findRightmostZero(u.L);
                    if (rightL != -1) {
                        Node newNode = new Node(NodeType.Cut, rightL, i + 1, -1, -1);
                        int minVal = u.mini;
                        int maxVal = u.maxi;
                        newNode.child.add(0, uIdx);
                        while (true) {
                            int topIdx = stack.pop();
                            Node top = nodes.get(topIdx);
                            newNode.child.add(0, topIdx);
                            minVal = Math.min(minVal, top.mini);
                            maxVal = Math.max(maxVal, top.maxi);
                            if (top.L == rightL) break;
                        }
                        newNode.mini = minVal;
                        newNode.maxi = maxVal;
                        uIdx = nodes.size();
                        nodes.add(newNode);
                    } else {
                        break;
                    }
                }
            }
            stack.push(uIdx);
        }
        root = stack.pop();
    }

    private boolean isContiguous(Node v, Node u) {
        return v.maxi + 1 == u.mini || u.maxi + 1 == v.mini;
    }

    private static class SegTree {
        int n;
        long[] min;
        long[] lazy;

        SegTree(int n) {
            this.n = 1;
            while (this.n < n) this.n *= 2;
            min = new long[2 * this.n];
            lazy = new long[2 * this.n];
            for (int i = 0; i < 2 * this.n; i++) min[i] = 1_000_000_000_000_000L;
        }

        void activate(int i) {
            add(i, i + 1, -1_000_000_000_000_000L);
        }

        void add(int l, int r, long val) {
            add(l, r, val, 1, 0, n);
        }

        void add(int l, int r, long val, int k, int sl, int sr) {
            if (r <= sl || sr <= l) return;
            if (l <= sl && sr <= r) {
                lazy[k] += val;
                min[k] += val;
                return;
            }
            push(k);
            int mid = (sl + sr) / 2;
            add(l, r, val, 2 * k, sl, mid);
            add(l, r, val, 2 * k + 1, mid, sr);
            min[k] = Math.min(min[2 * k], min[2 * k + 1]);
        }

        void push(int k) {
            if (lazy[k] != 0) {
                lazy[2 * k] += lazy[k];
                min[2 * k] += lazy[k];
                lazy[2 * k + 1] += lazy[k];
                min[2 * k + 1] += lazy[k];
                lazy[k] = 0;
            }
        }

        int findRightmostZero(int r) {
            return findRightmostZero(1, 0, n, r);
        }

        int findRightmostZero(int k, int sl, int sr, int r) {
            if (min[k] > 0 || sl >= r) return -1;
            if (sr - sl == 1) return sl;
            push(k);
            int mid = (sl + sr) / 2;
            int res = findRightmostZero(2 * k + 1, mid, sr, r);
            if (res == -1) {
                res = findRightmostZero(2 * k, sl, mid, r);
            }
            return res;
        }
    }
}
