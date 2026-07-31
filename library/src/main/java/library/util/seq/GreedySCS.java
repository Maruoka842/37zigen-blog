package library.util.seq;

import java.util.*;
import library.util.seq.StringUtils;

/**
 * Greedy Shortest Common Superstring (SCS) アルゴリズムの実装。
 * Suffix Array を用いることで、全ペアの overlap 計算を避け、 $O(N \log N)$ の計算量を実現する。
 *
 * <p> $O(m^2)$ を回避する仕組み:
 * <ul>
 *   <li>全 $m^2$ ペアの overlap グラフを明示的に構築しない。</li>
 *   <li>各ソース文字列 $i$ とその各接尾辞 $\alpha$ (長さ $d$) に対して、 $\alpha$ で始まるターゲット文字列 $j$ を Suffix Array の範囲検索で特定する。</li>
 *   <li>$(i, \alpha)$ のペア（イベント）の総数は $O(N)$ である。</li>
 *   <li>各イベントに対して、SA 区間内の利用可能なターゲットのうち、高々 2 個のみを確認する。</li>
 *   <li>ターゲットの利用可能性は、SA インデックス上の削除専用 successor DSU で管理する。</li>
 * </ul>
 * </p>
 *
 * <p>計算量:
 * <ul>
 *   <li>前処理: $O(N \log N)$ ($N$ は入力文字列の合計長)</li>
 *   <li>Greedy マージ: $O(N \log N)$</li>
 *   <li>空間計算量: $O(N \log N)$ (SuffixArrayLCP 内の Sparse Table)</li>
 * </ul>
 * </p>
 */
public class GreedySCS {

    /**
     * 与えられた文字列集合に対して、貪欲法による最短共通超文字列 (SCS) を計算する。
     * @param input 入力文字列のリスト。
     * @return すべての入力文字列を部分文字列として含む超文字列。
     *
     * <p>計算量: $O(N \log N)$ ($N = \sum |input_i|$)</p>
     */
    public static String greedySuperstring(List<String> input) {
        if (input == null || input.isEmpty()) return "";
        Index index = new Index(input);
        if (index.reduced.isEmpty()) return "";
        if (index.reduced.size() == 1) return index.reduced.get(0).s;
        return new Solver(index).solve().superstring;
    }

    /**
     * 超文字列を構成する元の文字列の ID の順序（貪欲パス）を計算する。
     * @param input 入力文字列のリスト。
     * @return 超文字列に出現する順序での元のインデックスの配列。
     *
     * <p>計算量: $O(N \log N)$ ($N = \sum |input_i|$)</p>
     */
    public static int[] greedyPath(List<String> input) {
        if (input == null || input.isEmpty()) return new int[0];
        Index index = new Index(input);
        if (index.reduced.isEmpty()) return new int[0];
        if (index.reduced.size() == 1) return new int[]{index.reduced.get(0).originalId};
        return new Solver(index).solve().path;
    }

    private static class ReducedString {
        String s;
        int originalId;
        int idInIndex;
        ReducedString(String s, int originalId, int idInIndex) {
            this.s = s;
            this.originalId = originalId;
            this.idInIndex = idInIndex;
        }
    }

    private static class Index {
        final List<ReducedString> reduced;
        final int[] T;
        final int[] sa;
        final int[] rank;
        final int[] start;
        final SuffixArrayLCP saLcp;

        Index(List<String> input) {
            Map<String, Integer> originalIds = new LinkedHashMap<>();
            for (int i = 0; i < input.size(); i++) {
                String str = input.get(i);
                if (str != null) originalIds.putIfAbsent(str, i);
            }

            String[] filtered = StringUtils.removeSubstrings(input.toArray(new String[0]));
            List<String> uList = Arrays.asList(filtered);
            int m = uList.size();
            int totalLen = 0;
            for (String str : uList) totalLen += str.length() + 1;
            this.T = new int[totalLen + 1];
            this.start = new int[m];
            int ptr = 0;
            for (int i = 0; i < m; i++) {
                T[ptr++] = 0; // SEP
                start[i] = ptr;
                String str = uList.get(i);
                for (int j = 0; j < str.length(); j++) {
                    T[ptr++] = (int)str.charAt(j) + 1;
                }
            }
            T[ptr] = 0;
            this.sa = StringUtils.suffixArray(T);
            this.saLcp = new SuffixArrayLCP(T, sa);
            this.rank = new int[sa.length];
            for (int i = 0; i < sa.length; i++) rank[sa[i]] = i;

            this.reduced = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                String str = uList.get(i);
                reduced.add(new ReducedString(str, originalIds.get(str), i));
            }
        }
    }

    private static class Solver {
        private final Index idx;
        private final int m;

        Solver(Index idx) {
            this.idx = idx;
            this.m = idx.reduced.size();
        }

        static class Result {
            String superstring;
            int[] path;
        }

        /**
         * 貪欲なマージを実行する。
         *
         * <p>計算量: $O(N \log N)$</p>
         */
        Result solve() {
            int maxLen = 0;
            for (ReducedString s : idx.reduced) maxLen = Math.max(maxLen, s.s.length());

            List<Event>[] byDepth = new List[maxLen + 1];
            for (int d = 0; d <= maxLen; d++) byDepth[d] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int len = idx.reduced.get(i).s.length();
                for (int d = 0; d <= len; d++) {
                    byDepth[d].add(new Event(i, len - d));
                }
            }

            boolean[] canIn = new boolean[m];
            boolean[] canOut = new boolean[m];
            int[] leftEnd = new int[m];
            int[] rightEnd = new int[m];
            int[] successor = new int[m];
            int[] overlapLen = new int[m];
            Arrays.fill(canIn, true);
            Arrays.fill(canOut, true);
            Arrays.fill(successor, -1);
            for (int i = 0; i < m; i++) {
                leftEnd[i] = i;
                rightEnd[i] = i;
            }

            int[] parentNext = new int[idx.T.length + 1];
            for (int i = 0; i <= idx.T.length; i++) parentNext[i] = i;
            int[] saIndexToTargetId = new int[idx.T.length];
            Arrays.fill(saIndexToTargetId, -1);
            int[] targetSAIndex = new int[m];
            for (int j = 0; j < m; j++) {
                targetSAIndex[j] = idx.rank[idx.start[idx.reduced.get(j).idInIndex] - 1];
                saIndexToTargetId[targetSAIndex[j]] = j;
            }

            boolean[] isTargetSAIndex = new boolean[idx.T.length];
            for (int j = 0; j < m; j++) isTargetSAIndex[targetSAIndex[j]] = true;
            for (int i = idx.T.length - 1; i >= 0; i--) {
                if (!isTargetSAIndex[i]) parentNext[i] = findNext(i + 1, parentNext);
            }

            int merges = 0;
            outer:
            for (int d = maxLen; d >= 0; d--) {
                for (Event ev : byDepth[d]) {
                    int i = ev.source;
                    if (!canOut[i]) continue;

                    int[] range = findSARange(i, ev.offset, d);
                    int l = range[0], r = range[1];
                    if (l > r) continue;

                    int p1 = findNext(l, parentNext);
                    if (p1 <= r) {
                        int j1 = saIndexToTargetId[p1];
                        if (j1 != -1 && canAdd(i, j1, leftEnd)) {
                            addEdge(i, j1, d, canOut, canIn, leftEnd, rightEnd, successor, overlapLen, parentNext, targetSAIndex);
                            merges++;
                            if (merges == m - 1) break outer;
                        } else {
                            int p2 = findNext(p1 + 1, parentNext);
                            if (p2 <= r) {
                                int j2 = saIndexToTargetId[p2];
                                if (j2 != -1 && canAdd(i, j2, leftEnd)) {
                                    addEdge(i, j2, d, canOut, canIn, leftEnd, rightEnd, successor, overlapLen, parentNext, targetSAIndex);
                                    merges++;
                                    if (merges == m - 1) break outer;
                                }
                            }
                        }
                    }
                }
            }

            int startNode = -1;
            for (int i = 0; i < m; i++) {
                if (canIn[i]) {
                    startNode = i;
                    break;
                }
            }

            Result res = new Result();
            StringBuilder sb = new StringBuilder();
            sb.append(idx.reduced.get(startNode).s);
            int[] path = new int[m];
            path[0] = idx.reduced.get(startNode).originalId;
            int cur = startNode;
            int pathIdx = 1;
            while (successor[cur] != -1) {
                int nextNode = successor[cur];
                sb.append(idx.reduced.get(nextNode).s.substring(overlapLen[cur]));
                path[pathIdx++] = idx.reduced.get(nextNode).originalId;
                cur = nextNode;
            }
            res.superstring = sb.toString();
            res.path = path;
            return res;
        }

        private int[] findSARange(int sourceId, int offset, int d) {
            int L, R;
            int n = idx.T.length;
            {
                int low = 0, high = n;
                while (low < high) {
                    int mid = (low + high) / 2;
                    if (compare(sourceId, offset, d, idx.sa[mid]) <= 0) high = mid;
                    else low = mid + 1;
                }
                L = low;
            }
            {
                int low = L, high = n - 1;
                while (low <= high) {
                    int mid = (low + high) / 2;
                    if (compare(sourceId, offset, d, idx.sa[mid]) == 0) low = mid + 1;
                    else high = mid - 1;
                }
                R = high;
            }
            return new int[]{L, R};
        }

        private int compare(int sourceId, int offset, int d, int saPos) {
            if (0 != idx.T[saPos]) return Integer.compare(0, idx.T[saPos]);
            int lenMatch = idx.saLcp.lcp(idx.start[idx.reduced.get(sourceId).idInIndex] + offset, saPos + 1);
            if (lenMatch >= d) return 0;
            int p1 = idx.start[idx.reduced.get(sourceId).idInIndex] + offset + lenMatch;
            int p2 = saPos + 1 + lenMatch;
            int c1 = idx.T[p1];
            int c2 = (p2 >= idx.T.length) ? -1 : idx.T[p2];
            return Integer.compare(c1, c2);
        }

        private int findNext(int x, int[] parentNext) {
            int cur = x;
            while (cur < parentNext.length && parentNext[cur] != cur) {
                cur = parentNext[cur];
            }
            if (cur >= parentNext.length) return parentNext.length - 1;
            int root = cur;
            cur = x;
            while (cur < parentNext.length && parentNext[cur] != root) {
                int next = parentNext[cur];
                parentNext[cur] = root;
                cur = next;
            }
            return root;
        }

        private boolean canAdd(int i, int j, int[] leftEnd) {
            return leftEnd[i] != j;
        }

        private void addEdge(int i, int j, int d, boolean[] canOut, boolean[] canIn, int[] leftEnd, int[] rightEnd, int[] successor, int[] overlapLen, int[] parentNext, int[] targetSAIndex) {
            successor[i] = j;
            overlapLen[i] = d;
            canOut[i] = false;
            canIn[j] = false;
            int p = targetSAIndex[j];
            parentNext[p] = findNext(p + 1, parentNext);
            int A = leftEnd[i], B = rightEnd[j];
            rightEnd[A] = B;
            leftEnd[B] = A;
        }

        private static class Event {
            int source, offset;
            Event(int s, int o) { this.source = s; this.offset = o; }
        }
    }
}
