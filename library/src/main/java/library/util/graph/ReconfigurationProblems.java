package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import library.util.ArrayUtils;
import library.util.collections.Hash;
import library.util.collections.HashStrategies;
import library.util.collections.IntArrayList;
import library.util.graph.tree.Tree;

/**
 * 代表的な再構成問題（Reconfiguration Problems）に対する {@link ImplicitDigraph} の実装を提供する。
 */
public class ReconfigurationProblems {

    /**
     * ビット列に対する $0^k \leftrightarrow 1^k$ 書き換え規則による再構成問題。
     *
     * <p>状態：長さ $n$ の $\{0, 1\}$ 配列。
     * 隣接条件：状態 $u, v$ が隣接するのは、ある $i \in [0, n-k]$ が存在し、
     * $u[i..i+k-1]$ が全て同一かつ $v$ がそれらを反転させたものであるとき。</p>
     *
     * <p>計算量：
     * <ul>
     *   <li>{@code nextStates}: $O(n \cdot k)$</li>
     *   <li>{@code onPath}: $O(n)$</li>
     *   <li>{@code dist}: $O(n)$</li>
     * </ul></p>
     *
     * @param n ビット列の長さ
     * @param k 書き換えブロックの長さ
     * @return 状態空間を表す {@link ImplicitDigraph}
     */
    // 未テスト
    public static ImplicitDigraph<int[]> zerosOnesRewriting(int n, int k) {
        return new ImplicitDigraph<int[]>(HashStrategies.INT_ARRAY) {
            @Override
            public Iterable<int[]> nextStates(int[] v) {
                List<int[]> nexts = new ArrayList<>();
                for (int i = 0; i <= n - k; i++) {
                    boolean allSame = true;
                    for (int j = 1; j < k; j++) {
                        if (v[i + j] != v[i]) {
                            allSame = false;
                            break;
                        }
                    }
                    if (allSame) {
                        int[] next = v.clone();
                        for (int j = 0; j < k; j++) {
                            next[i + j] = 1 - next[i + j];
                        }
                        nexts.add(next);
                    }
                }
                return nexts;
            }

            @Override
            public long dist(int[] src, int[] dst) {
                if (src == null || dst == null || src.length != n || dst.length != n) return -1;
                if (!onPath(src, dst)) return -1;
                if (n == 0) return 0;

                IntArrayList stackA = new IntArrayList(n + 1);
                IntArrayList stackB = new IntArrayList(n + 1);
                int lcp = 0;

                long sum = 0;

                for (int p = 0; p < n; p++) {
                    int bitA = (p == 0) ? src[0] : (src[p - 1] ^ src[p]);
                    if (bitA == 1) {
                        int r = p % k;
                        if (!stackA.isEmpty() && stackA.peekLast() == r) {
                            stackA.pollLast();
                            if (lcp > stackA.size()) lcp = stackA.size();
                        } else {
                            if (lcp == stackA.size() && stackA.size() < stackB.size() && stackB.get(stackA.size()) == r) {
                                lcp++;
                            }
                            stackA.add(r);
                        }
                    }

                    int bitB = (p == 0) ? dst[0] : (dst[p - 1] ^ dst[p]);
                    if (bitB == 1) {
                        int r = p % k;
                        if (!stackB.isEmpty() && stackB.peekLast() == r) {
                            stackB.pollLast();
                            if (lcp > stackB.size()) lcp = stackB.size();
                        } else {
                            if (lcp == stackB.size() && stackB.size() < stackA.size() && stackA.get(stackB.size()) == r) {
                                lcp++;
                            }
                            stackB.add(r);
                        }
                    }
                    sum += stackA.size() + stackB.size() - 2L * lcp;
                }

                if (sum % k != 0) throw new AssertionError();
                return sum / k;
            }

            @Override
            public boolean onPath(int[] src, int[] dst) {
                if (src == null || dst == null || src.length != n || dst.length != n) return false;
                return reduce(src).equals(reduce(dst));
            }

            private String reduce(int[] x) {
                List<Integer> stack = new ArrayList<>();
                List<Integer> count = new ArrayList<>();
                for (int v : x) {
                    if (stack.isEmpty() || stack.get(stack.size() - 1) != v) {
                        stack.add(v);
                        count.add(1);
                    } else {
                        count.set(count.size() - 1, count.get(count.size() - 1) + 1);
                    }
                    if (count.get(count.size() - 1) == k) {
                        stack.remove(stack.size() - 1);
                        count.remove(count.size() - 1);
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < stack.size(); i++) {
                    for (int j = 0; j < count.get(i); j++) sb.append(stack.get(i));
                }
                return sb.toString();
            }
        };
    }

    /**
     * 木上のスライディングパズル（15パズルの木版）。
     *
     * <p>状態：頂点にラベルが割り振られた木。ラベル 0 は空きスペース（穴）とみなす。
     * 隣接条件：ラベル 0 がある頂点 $u$ と、ラベル 0 ではない値を持つ隣接頂点 $v$ について、
     * ラベル $P[u]$ と $P[v]$ を入れ替えることができる。ラベル 0 同士の入れ替えはできない。</p>
     *
     * <p>計算量：
     * <ul>
     *   <li>{@code nextStates}: $O(n)$</li>
     * </ul></p>
     *
     * @param tree 木
     * @return 状態空間を表す {@link ImplicitDigraph}
     */
    public static ImplicitDigraph<int[]> treeSlidingPuzzle(Tree tree) {
        final int n = tree.N;
        return new ImplicitDigraph<int[]>(HashStrategies.INT_ARRAY) {
            @Override
            public Iterable<int[]> nextStates(int[] v) {
                List<int[]> nexts = new ArrayList<>();
                for (int u = 0; u < n; u++) {
                    if (v[u] == 0) {
                        for (int neighbor : tree.adj[u]) {
                            if (v[neighbor] != 0) {
                                int[] next = v.clone();
                                next[u] = v[neighbor];
                                next[neighbor] = 0;
                                nexts.add(next);
                            }
                        }
                    }
                }
                return nexts;
            }
        };
    }

    /**
     * 長さ $n$ の $\{0, 1, 2, \dots\}$ 配列において、
     * 部分列 $0112$ の出現を禁止した上で、隣接要素の入れ替えを行う再構成問題。
     *
     * <p>出典: <a href="https://yukicoder.me/problems/no/3587">No.3587 0112-Free Reconfiguration - yukicoder</a></p>
     *
     * <p>隣接条件：状態 $u, v$ が隣接するのは、ある $i \in [0, n-2]$ について
     * $u[i] \neq u[i+1]$ かつ、それらを入れ替えた配列が $0112$ を部分列（連続する要素）として
     * 含まないとき。</p>
     *
     * <p>計算量：
     * <ul>
     *   <li>{@code nextStates}: $O(n^2)$</li>
     *   <li>{@code onPath}: $O(n)$</li>
     * </ul></p>
     *
     * @return 状態空間を表す {@link ImplicitDigraph}
     */
    // 未テスト
    public static ImplicitDigraph<int[]> avoiding0112AdjacentSwapping() {
        return new ImplicitDigraph<int[]>(HashStrategies.INT_ARRAY) {
            private void validate(int[] v) {
                for (int x : v) {
                    if (x < 0 || x > 2) {
                        throw new IllegalArgumentException("Array contains invalid element: " + x + ". Only 0, 1, 2 are allowed.");
                    }
                }
            }

            @Override
            public Iterable<int[]> nextStates(int[] v) {
                validate(v);
                if (!check(v)) throw new AssertionError();
                ArrayList<int[]> list = new ArrayList<>();
                for (int i = 0; i < v.length - 1; i++) {
                    if (v[i] == v[i + 1]) continue;
                    int[] u = v.clone();
                    {
                        var tmp = u[i];
                        u[i] = u[i + 1];
                        u[i + 1] = tmp;
                    }
                    if (check(u)) list.add(u);
                }
                return list;
            }

            private boolean check(int[] v) {
                for (int i = 0; i + 3 < v.length; i++) {
                    if (v[i] == 0 && v[i + 1] == 1 && v[i + 2] == 1 && v[i + 3] == 2) return false;
                }
                return true;
            }

            @Override
            public boolean onPath(int[] src, int[] dst) {
                validate(src);
                validate(dst);
                int[] cnt0 = new int[3];
                int[] cnt1 = new int[3];
                for (int v : src) {
                    cnt0[v]++;
                }
                for (int v : dst) {
                    cnt1[v]++;
                }
                if (!Arrays.equals(cnt0, cnt1)) return false;
                if (cnt0[0] == 0 || cnt0[1] <= 1 || cnt0[2] == 0) return true;
                return type(src) == type(dst);
            }

            private int type(int[] S) {
                int a = ArrayUtils.indexOf(S, 2);
                int b = -1;
                for (int i = S.length - 1; i >= 0; i--) {
                    if (S[i] == 0) {
                        b = i;
                        break;
                    }
                }
                if (a < b) return 1;
                for (int i = 0; i + 1 < S.length; i++) {
                    if (S[i] == 0) {
                        int j = i;
                        while (j + 1 < S.length && S[j + 1] == 1) j++;
                        if (j + 1 < S.length && j - i >= 2 && S[j + 1] == 2) return 0;
                        i = j;
                    }
                }
                return 1;
            }
        };
    }
}
