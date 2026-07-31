package library.util.seq;

import java.util.Arrays;

public class Eertree {
	//https://judge.yosupo.jp/submission/372368
	//https://atcoder.jp/contests/abc237/submissions/74334147
	private int[][] next; 
	private int[] link;   // suffix link (最長の真の接尾辞回文)
	private int[] len;    // ノードが表す回文の長さ
    private int[] parent; // 親ノード
    
    char[] s;
    int size; // ノード数
    int last; // 現在の文字列の最長接尾辞回文のノードID
    int n;    // 入力文字列の現在の長さ
    
    public Eertree(int maxLen) {
        next = new int[maxLen + 3][26];
        link = new int[maxLen + 3];
        len  = new int[maxLen + 3];
        s = new char[maxLen + 3];
        parent = new int[maxLen + 3];

        for (int i = 0; i < next.length; i++) {
            Arrays.fill(next[i], -1);
        }

        // root(-1)
        len[0] = -1;
        link[0] = 0;

        // root(0)
        len[1] = 0;
        link[1] = 0;

        size = 2;
        last = 1;
        n = 0;

        s[0] = '#';
    }
    
    public Eertree(String s) {
    	this(s.length());
        for (int i = 0; i < s.length(); i++) {
        	add(s.charAt(i));
        }
    }
    
    /**
     * ノード {@code v} の suffix link を返す。
     *
     * <p>
     * Eertree における suffix link とは、
     * {@code v} が表す回文の「最長の真の接尾辞回文」
     * を表すノードへのリンクである。
     * </p>
     *
     * <p>
     * 根ノードについては：
     * </p>
     * <ul>
     *   <li>長さ {@code -1} の根 → 自分自身</li>
     *   <li>長さ {@code 0} の根 → 長さ {@code -1} の根</li>
     * </ul>
     *
     * @param v ノード番号
     * @return {@code v} の suffix link 先ノード
     */
    public int suffixLink(int v) {
        return link[v];
    }
    
    /**
     * ノード {@code v} の親ノードを返す。
     *
     * <p>
     * Eertree における親とは，
     * {@code v} が表す回文の先頭文字と末尾文字を
     * 取り除いて得られる回文に対応するノードである。
     * </p>
     *
     * <p>
     * 長さ 1 の回文の親は ODD 根(頂点番号0)，
     * 長さ 2 の回文の親は EVEN 根(頂点番号1)である。
     * </p>
     *
     * @param v ノード番号
     * @return {@code v} の親ノード番号
     */
    public int parent(int v) {
        return parent[v];
    }
    
    
    /**
     * 現在追加しようとしている文字 s[n] を左右に付け足して、新しい回文に拡張できるような suffix palindrome を探す
     * @param v
     * @return
     */
    private int getLink(int v) {
        while (true) {
            int l = len[v];
            if (n - 1 - l >= 0 && s[n - 1 - l] == s[n]) return v;
            v = link[v];
        }
    }

    /**
     * 英子文字 {@code c} を現在の文字列の末尾に追加する。
     *
     * <p>
     * 追加後，新たに出現した回文が存在する場合は
     * 対応するノードを生成する。
     * </p>
     *
     * <p>
     * このメソッドの返り値は，
     * 追加後の文字列全体の最大回文接尾辞
     * （最長の suffix palindrome）
     * に対応するノード番号である。
     * </p>
     *
     * <p>
     * 返されるノードは内部番号であり，
     * 根ノードの番号は以下の通り：
     * </p>
     * <ul>
     *   <li>{@code 0}: ODD 根（長さ {@code -1} の仮想回文）</li>
     *   <li>{@code 1}: EVEN 根（空文字列）</li>
     * </ul>
     *
     * @param c 追加する文字
     * @return 追加後の文字列の最大回文接尾辞に対応するノード番号
     */
    public int add(char c) {
        s[++n] = c;
        int cur = getLink(last);
        int idx = c - 'a';

        if (next[cur][idx] != -1) {
            last = next[cur][idx];
            return last;
        }

        int newNode = size++;
        len[newNode] = len[cur] + 2;
        next[cur][idx] = newNode;
        parent[newNode]=cur;
        
        if (len[newNode] == 1) {
            link[newNode] = 1;
        } else {
            int to = getLink(link[cur]);
            link[newNode] = next[to][idx];
        }

        return last = newNode;
    }


    // 異なる回文の数(空文字列除く）
    public int numberOfDistinctPalindromes() {
    	//https://atcoder.jp/contests/abc237/submissions/74334147
        return size - 2;
    }
    
    /**
     * ノード {@code v} が表す回文の長さを返す。
     *
     * <p>
     * 根ノードの長さは以下の通り：
     * </p>
     * <ul>
     *   <li>{@code 0}: ODD 根（長さ {@code -1}）</li>
     *   <li>{@code 1}: EVEN 根（長さ {@code 0}）</li>
     * </ul>
     *
     * @param v ノード番号
     * @return {@code v} が表す回文の長さ
     */
    public int length(int v) {
        return len[v];
    }

	private String getPalindromeString(int v) {
		if (v == 0) return "(ODD ROOT)";
		if (v == 1) return "";
		char[] path = new char[len[v]];
		int l = 0, r = len[v] - 1;
		int cur = v;
		while (cur > 1) {
			int p = parent[cur];
			char ch = ' ';
			for (int c = 0; c < 26; c++) {
				if (next[p][c] == cur) {
					ch = (char) ('a' + c);
					break;
				}
			}
			path[l++] = ch;
			path[r--] = ch;
			cur = p;
		}
		return new String(path);
	}

	/**
	 * 回文木の状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(\text{size} \times 26)$</li>
	 * </ul>
	 * @return 回文木の状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("Eertree size=").append(size).append(" last=").append(last).append(" n=").append(n).append("\n");
		for (int i = 0; i < size; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Node %d: len=%d parent=%d link=%d str=\"%s\" [",
				i, len[i], parent[i], link[i], getPalindromeString(i)));
			boolean first = true;
			for (int c = 0; c < 26; c++) {
				int nxt = next[i][c];
				if (nxt != -1) {
					if (!first) sb.append(", ");
					sb.append((char) ('a' + c)).append("->").append(nxt);
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
	 * 回文木の状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(\text{size} \times 26)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}

