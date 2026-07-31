package library.util.seq;

import java.util.Arrays;

import library.util.collections.IntArrayList;
import library.util.collections.IntQueue;

/**
 * Aho-Corasick法による複数パターン検索アルゴリズム。
 *
 * <p>複数のパターン文字列を同時に検索するためのオートマトンを構築する。
 * Trie木に失敗時遷移（failure link）を付加することで、テキストを1回走査するだけで
 * 全てのパターンの出現箇所（または個数）を特定できる。</p>
 *
 * <p>計算量:
 * <ul>
 *   <li>構築: O(Σ|pattern| * alphabetSize)</li>
 *   <li>検索 (個数合計): O(|text|)</li>
 *   <li>検索 (個別個数): O(|text| + nodeSize)</li>
 * </ul>
 * </p>
 */
public class AhoCorasick {
	//https://judge.yosupo.jp/submission/372603
    /** 各状態からの文字による遷移先 [nodeId][alphabetIndex] */
    private int[][] next;
    /** 各状態の親ノード */
    private int [] parent;
    /** そのノードおよび、その接尾辞に対応するノードで終わるパターンの合計数 */
    private int[] count;
    /** 現在のノード数（状態数） */
    private int size;
    /** アルファベットの種類数 */
    private int alphabetSize = 26;
    /** 失敗リンク（最長の真の接尾辞に対応する状態） */
    private int[] link;
    /** 追加された各パターンに対応する終端ノードのリスト */
    private IntArrayList patternNodes = new IntArrayList();
    /** BFS（幅優先探索）によるノードの訪問順序 */
    private int[] bfsOrder;
    /** そのノードが追加されたパターンの終端であるか */
    private boolean[] isTerminal;

    /**
     * 指定された最大ノード数で初期化する。デフォルトのアルファベットサイズは 26。
     * @param maxNodes 全パターンの合計長 + 1 (根が空文字列)
     */
    public AhoCorasick(int maxNodes) {
        this(maxNodes, 26);
    }
    
    public int root() {
    	return 0;
    }

    /**
     * 指定された最大ノード数とアルファベットサイズで初期化する。
     * @param maxNodes 全パターンの合計長 + 1 (根が空文字列)
     * @param alphabetSize 使用する文字の種類数
     */
    public AhoCorasick(int maxNodes, int alphabetSize) {
        this.alphabetSize = alphabetSize;
        next = new int[maxNodes][alphabetSize];
        count = new int[maxNodes];
        parent = new int[maxNodes];
        link = new int[next.length];
        for (int i = 0; i < next.length; i++) {
            Arrays.fill(next[i], -1);
        }
        size = 1; // 根はノード0
        parent[0] = -1;
        bfsOrder = new int[maxNodes];
        isTerminal = new boolean[maxNodes];
    }

    /**
     * 英小文字からなるパターンを追加する。追加したパターンに対応するノードを返す。
     * @param s パターン文字列
     */
    public int add(String s) {
        int[] a = new int[s.length()];
        for (int i = 0; i < s.length(); i++) a[i] = s.charAt(i) - 'a';
        return add(a);
    }

    
    /**
     * 英小文字からなるパターンを追加する。追加したパターンに対応するノードを返す。
     * @param s パターン文字列
     */
    public int add(char[] s) {
        int[] a = new int[s.length];
        for (int i = 0; i < s.length; i++) a[i] = s[i] - 'a';
        return add(a);
    }
    
    /**
     * 整数配列として表現されたパターンを追加する。
     * @param s パターン配列（各要素は 0 以上 alphabetSize 未満）
     */
    public int add(int[] s) {
        int cur = 0;
        for (int c : s) {
            if (next[cur][c] == -1) {
                next[cur][c] = size++;
                parent[next[cur][c]] = cur;
            }
            cur = next[cur][c];
        }
        count[cur]++;
        isTerminal[cur] = true;
        patternNodes.add(cur);
        return cur;
    }

    /**
     * Failure linkを構築し、オートマトンを完成させる。
     * addの後に必ず一度呼び出す必要がある。
     */
    public void build() {
    	//nextはトライをなし、根に近いほど番号が小さい。
        IntQueue que = new IntQueue();
        int bfsPtr = 0;
        bfsOrder[bfsPtr++] = 0;
        for (int i = 0; i < alphabetSize; i++) {
            if (next[0][i] != -1) {
                que.add(next[0][i]);//1文字の場合のみ積む
            } else {
                next[0][i] = 0;
            }
        }

        while (!que.isEmpty()) {
            int u = que.poll();
            bfsOrder[bfsPtr++] = u;
            for (int i = 0; i < alphabetSize; i++) {
                if (next[u][i] != -1) {
                    link[next[u][i]] = next[link[u]][i];
                    // そのノードで終わるパターンの数に、失敗リンク先の情報を加算
                    count[next[u][i]] += count[link[next[u][i]]];
                    que.add(next[u][i]);
                } else {
                    next[u][i] = next[link[u]][i];
                }
            }
        }
    }

    /**
     * テキスト中に含まれるパターンの総出現回数を数える。
     * @param text 検索対象のテキスト
     * @return 全パターンの出現回数の合計
     */
    public long match(int[] text) {
        long res = 0;
        int cur = 0;
        for (int c : text) {
            cur = next[cur][c];
            res += count[cur];
        }
        return res;
    }

    /**
     * 各パターンの個別の出現回数を数える。
     * @param text 検索対象のテキスト
     * @return add した順番での各パターンの出現回数
     */
    public long[] matchEach(int[] text) {
        int[] nodeVisitCount = new int[size];
        int cur = 0;
        for (int c : text) {
            cur = next[cur][c];
            nodeVisitCount[cur]++;
        }

        long[] nodeTotalCount = new long[size];
        for (int i = 0; i < size; i++) {
            nodeTotalCount[i] = nodeVisitCount[i];
        }

        // BFSの逆順で回して、failure linkを辿って出現回数を伝播させる。
        // 頂点番号の降順では正しい伝搬が保証されない（後から追加された短いパターンの方が番号が大きいため）。
        for (int i = size - 1; i >= 1; i--) {
            int u = bfsOrder[i];
            nodeTotalCount[link[u]] += nodeTotalCount[u];
        }

        long[] ret = new long[patternNodes.size()];
        for (int i = 0; i < patternNodes.size(); i++) {
            ret[i] = nodeTotalCount[patternNodes.get(i)];
        }
        return ret;
    }

    /**
     * 現在の状態から文字 c による遷移先のノード番号を返す。
     * 文字 c に対応する直接の子ノードが存在しない場合、最長接尾辞(failure link)へ移動する。
     * @param node 現在のノード番号
     * @param c 文字（0 以上 alphabetSize 未満）
     * @return 遷移先のノード番号
     */
    public int transition(int node, int c) {
        return next[node][c];
    }

    /**
     * 指定されたノード {@code node}で終了するパターンの総数（接尾辞も含む）を返す。
     * @param node ノード番号
     * @return パターン数
     */
    public int getCount(int node) {
        return count[node];
    }

    /**
     * 指定されたノード{@code node}がパターンの終端であるか（直接追加されたか）を返す。
     * @param node ノード番号
     * @return パターンの終端であれば true
     */
    public boolean isTerminal(int node) {
        return isTerminal[node];
    }

    /**
     * テキストの接頭辞とマッチするパターンの最大長を返す。
     * @param s テキスト文字列
     * @return 最大マッチ長。マッチしない場合は -1。
     */
    public int maxPrefixMatchLength(String s) {
        int[] a = new int[s.length()];
        for (int i = 0; i < s.length(); i++) a[i] = s.charAt(i) - 'a';
        return maxPrefixMatchLength(a);
    }

    /**
     * テキストの接頭辞とマッチするパターンの最大長を返す。
     * @param s テキスト文字列
     * @return 最大マッチ長。マッチしない場合は -1。
     */
    public int maxPrefixMatchLength(char[] s) {
        int[] a = new int[s.length];
        for (int i = 0; i < s.length; i++) a[i] = s[i] - 'a';
        return maxPrefixMatchLength(a);
    }

    /**
     * テキストの接頭辞とマッチするパターンの最大長を返す。
     * @param s テキスト配列
     * @return 最大マッチ長。マッチしない場合は -1。
     */
    public int maxPrefixMatchLength(int[] s) {
        int res = -1;
        int cur = 0;
        if (isTerminal[cur]) res = 0;
        for (int i = 0; i < s.length; i++) {
            int c = s[i];
            if (c < 0 || c >= alphabetSize) break;
            int nxt = next[cur][c];
            if (nxt == -1 || parent[nxt] != cur) break;
            cur = nxt;
            if (isTerminal[cur]) res = i + 1;
        }
        return res;
    }

    /**
     * 使用されているノード数を返す(空文字列含む)。
     * @return ノード数
     */
    public int size() {
        return size;
    }
    
    public int parent(int v) {
    	return parent[v];
    }
    
    public int suffixLink(int v) {
    	return link[v];
    }

    public String getString(int v) {
        if (v == 0) return "";
        StringBuilder sb = new StringBuilder();
        int cur = v;
        while (cur != 0) {
            int p = parent[cur];
            for (int c = 0; c < alphabetSize; c++) {
                if (next[p][c] == cur) {
                    sb.append(getChar(c));
                    break;
                }
            }
            cur = p;
        }
        return sb.reverse().toString();
    }

    private String getChar(int c) {
        if (alphabetSize == 26) {
            return String.valueOf((char) ('a' + c));
        }
        return "[" + c + "]";
    }

	/**
	 * オートマトンの状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(\text{size} \times \Sigma)$</li>
	 * </ul>
	 * @return オートマトンの状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("AhoCorasick size=").append(size).append("\n");
		for (int i = 0; i < size; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Node %d: str=\"%s\" parent=%d link=%d count=%d terminal=%b [",
				i, getString(i), parent[i], link[i], count[i], isTerminal[i]));
			boolean first = true;
			for (int c = 0; c < alphabetSize; c++) {
				int nxt = next[i][c];
				if (nxt != -1) {
					if (!first) sb.append(", ");
					sb.append(getChar(c)).append("->").append(nxt);
					first = false;
				}
			}
			sb.append("]");
			res.append(sb.toString());
			if (i < size - 1) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	/**
	 * オートマトンの状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(\text{size} \times \Sigma)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}

    public void draw() {
        System.setProperty("org.graphstream.ui", "swing");

        org.graphstream.graph.Graph g =
            new org.graphstream.graph.implementations.SingleGraph("AhoCorasick");

        g.setAttribute("ui.stylesheet", """
            node {
                fill-color: lightblue;
                size: 30px;
                text-alignment: center;
                text-size: 16;
                text-color: black;
                stroke-mode: plain;
                stroke-color: black;
            }
            node.accept {
                fill-color: orange;
            }
            edge {
                fill-color: gray;
                text-size: 14;
                text-background-mode: plain;
                text-background-color: white;
                text-padding: 2px;
                arrow-size: 10px, 6px;
                shape: line;
                text-offset: -10px, 0;
                text-alignment: along;
            }
            edge.link {
                fill-color: red;
                stroke-mode: dashes;
                text-color: red;
                shape: cubic-curve;
            }
        """);

        // ノード追加
        for (int i = 0; i < size; ++i) {
            org.graphstream.graph.Node node =
                g.addNode(String.valueOf(i));

            // ノードラベル：id + 対応文字列
            node.setAttribute(
                "ui.label",
                i + ": " + getString(i)
            );
            if (count[i] > 0) {
                node.setAttribute("ui.class", "accept");
            }
        }

        // 辺追加
        for (int v = 1; v < size; v++) {
            // Trie edge
            int p = parent[v];
            for (int c = 0; c < alphabetSize; c++) {
                if (next[p][c] == v) {
                    String trieId = p + "->" + v;
                    org.graphstream.graph.Edge trieEdge =
                        g.addEdge(trieId, String.valueOf(p), String.valueOf(v), true);
                    trieEdge.setAttribute("ui.label", getChar(c));
                    break;
                }
            }

            // Failure link
            int l = link[v];
            String linkId = v + "--link-->" + l;
            org.graphstream.graph.Edge linkEdge =
                g.addEdge(linkId, String.valueOf(v), String.valueOf(l), true);
            linkEdge.setAttribute("ui.class", "link");
        }

        g.display();
    }
}
