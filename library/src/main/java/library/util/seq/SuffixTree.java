package library.util.seq;

import java.util.Arrays;

import library.util.collections.IntQueue;
/**
 * 根は空文字列に対応。
 * 各頂点はSaffixArrayの各区間のLCPに対応。
 * 根から各頂点までの経路長がLCPの長さに対応。
 */
public class SuffixTree {
	char[] S;
	int[] sa;
	int[] lcp;
	public int[] rangeLCP;//根からそのノードまでの辺重み（文字列長）の和
	public int[] startPos;//その頂点が表す部分文字列を prefix に持つ suffix のうち、suffix array 上で最初に現れるものの開始位置。S[startPos[v]:startPos[v]+rangeLCP[v])が頂点vの表すLCP
	public int[] suffixCount;
	public int[] firstChild;
	public int[] nextSibling;
	public int[] parent;
	public int N;
	
	int size;
	
	public SuffixTree(char[] S, int[] suffixArray, int[] lcpAray) {
		this.S = S;
		this.sa = suffixArray;
		this.lcp  = lcpAray;
		build();
		N = size;
        buildChildren();
		buildSuffixCount();
	}
	
	//https://stackoverflow.com/questions/57502012/how-to-construct-suffix-tree-from-lcp-array-and-suffix-array
	void build() {
		//https://atcoder.jp/contests/abc433/submissions/74947178
    	//lcp (range-min) が変化しない極大な区間それぞれがsuffix treeのノードと一対一対応（根を除く）。
    	//saはIn-order順に並んでいるが、suffix treeのノード全てではない。葉といくつかの内点のみ。
    	
    	//葉が高々n個で根以外の頂点の次数が2以上なので頂点数は2n以下。
		parent=new int[2*S.length];
    	rangeLCP=new int[parent.length];//文字列長を辺重みとしたときの深さ
    	startPos=new int[2*S.length];
    	Arrays.fill(parent, -1);
    	int root = 0;
    	int p = 1;//（空文字列を除く）辞書順最小のsuffix
    	size = 2;// [0:root, 1:辞書順最小のsuffix] なのでサイズは2から
    	rangeLCP[p] = S.length-sa[0];
    	parent[p] = root;
    	startPos[p] = sa[0];
    	// 辞書順2番目のSuffixから順に追加
    	for (int i = 1; i < S.length; i++) {
    		int L = lcp[i - 1];//直前のSuffix(sa[i-1])と、今のSuffix(sa[i])の共通接頭辞の長さ
    		int preP = -1;
    		// 現在のノード p の深さが、共通部分 L より深い間、親へ遡る。
    		// 理由: 今見ている Suffix との分岐点は、深さ L の地点にあるはずだから。
    		while (rangeLCP[p] > L) {
    			preP = p;
    			p = parent[p];
    		}
    		// ここで p は「深さが L 以下のノード」の中で最も深いものになっています。
    		// つまり、分岐点は p の直下、あるいは p 自身。
    		if (rangeLCP[p] == L) {
    			parent[size]=p;
    			p = size;
    			rangeLCP[size] = S.length - sa[i];
    			startPos[size] = sa[i];
    			size++;
    		} else {
    			// 親子関係preP→pを
    			// preP→q→p
    			//      ↑
    			//      np 
    			// に更新。
    			// qは深さLに取る。
    			int q = size++;
    			int np = size++;
    			parent[preP]=q;
    			parent[np] = q;
    			parent[q]=p;
    			rangeLCP[q] = L;
    			startPos[q] = startPos[preP];
    			rangeLCP[np] = S.length - sa[i];
    			startPos[np] = sa[i];
    			p = np;
    		}
		}
    	parent = Arrays.copyOf(parent, size);
    	rangeLCP = Arrays.copyOf(rangeLCP, size);
    	startPos = Arrays.copyOf(startPos, size);
	}
	
	public int[] bfsOrder() {
		//https://atcoder.jp/contests/abc433/submissions/74947178
	    int[] order = new int[N];
	    int ptr = 0;
	    IntQueue que=new IntQueue();
	    que.add(0);
	    while (!que.isEmpty()) {
	    	int v = que.poll();
	        order[ptr++] = v;

	        for (int u = firstChild[v]; u != -1; u = nextSibling[u]) {
	        	que.add(u);
	        }
	    }
	    return order;
	}
	
	public int parentEdgeLength(int v) {
		//https://atcoder.jp/contests/abc433/submissions/74947178
	    return rangeLCP[v] - rangeLCP[parent[v]];
	}
	
    void buildChildren() {
        firstChild = new int[N];
        nextSibling = new int[N];
        Arrays.fill(firstChild, -1);
        Arrays.fill(nextSibling, -1);
        for (int v = N - 1; v >= 1; --v) {//子を辞書順に並べたいので逆順に回している。
            int p = parent[v];
            nextSibling[v] = firstChild[p];
            firstChild[p] = v;
        }
    }
	
    void buildSuffixCount() {
        suffixCount = new int[N];
        int[] order = new int[N];
        int ptr = 0;
        int[] stack = new int[N];
        int sp = 0;
        stack[sp++] = 0;
        while (sp > 0) {
            int v = stack[--sp];
            order[ptr++] = v;
            for (int u = firstChild[v]; u != -1; u = nextSibling[u]) {
                stack[sp++] = u;
            }
        }
        for (int i = ptr - 1; i >= 0; --i) {
            int v = order[i];
            int ret = (startPos[v] + rangeLCP[v] == S.length) ? 1 : 0;
            for (int u = firstChild[v]; u != -1; u = nextSibling[u]) {
                ret += suffixCount[u];
            }
            suffixCount[v] = ret;
        }
    }
	
	public boolean isSuffix(int v) {
		return startPos[v] + rangeLCP[v] == S.length;
	}
	
	public String getString(int v) {
		if (v == 0) return "(空文字列)";
		return new String(S, startPos[v], rangeLCP[v]);
	}
	
	
	public void draw() {
		System.setProperty("org.graphstream.ui", "swing");

		org.graphstream.graph.Graph g =
			new org.graphstream.graph.implementations.SingleGraph("SuffixTree");

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
		""");

		// ノード追加
		for (int i = 0; i < N; ++i) {
			org.graphstream.graph.Node node =
				g.addNode(String.valueOf(i));

			// ノードラベル：id + 対応文字列長
			node.setAttribute(
				"ui.label",
				i+" "+getString(i)
			);
		}

		// 辺追加（parent 配列から）
		for (int v = 1; v < N; v++) {
			int p = parent[v];
			String id = p + "->" + v;

			org.graphstream.graph.Edge edge =
				g.addEdge(id, String.valueOf(p), String.valueOf(v), true);

			// 辺ラベルを文字列にする
			edge.setAttribute("ui.label", getEdgeString(v));
		}

		g.display();
	}
	
	/**
	 * 親 parent[v] から v への辺に対応する文字列を返す。
	 */
	public String getEdgeString(int v) {
		int p = parent[v];
		int l = startPos[v] + rangeLCP[p];
		int r = startPos[v] + rangeLCP[v];
		return new String(S, l, r - l);
	}
	
	/**
	 * 頂点vの表す文字列をS(v)と置く。
	 * TとS(v)について一方がもう一方のprefixとなるような頂点vを返す。
	 * 複数ある場合、LCP(T, S(v))が最大となるもっとも浅い頂点を返す。
	 * @param T
	 * @return
	 * https://atcoder.jp/contests/abc362/submissions/71909554
	 */
	public int longestPrefixMatchNode(char[] T) {
		int v=0;
		boolean moved=false;
		int L=0;
		while(L < T.length) {
			moved=false;
			for (int u=firstChild[v];u!=-1;u=nextSibling[u]) {
				if(startPos[u]+L<S.length && S[startPos[u]+L]==T[L]) {
					int nL=L;
					while(nL < T.length && nL<rangeLCP[u] && T[nL]==S[startPos[u]+nL]) {
						++nL;
					}
					if(nL==T.length || nL==rangeLCP[u]) {
						L=nL;
						moved=true;
						v=u;
						break;
					}
				}
			}
			if(!moved)break;
		}
		return v;
	}
	
	/**
	 * Sに含まれる部分文字列の種類数を返す。
	 * Suffix Treeの各辺の長さの総和に等しい。
	 */
	public long countDistinctSubstrings() {
		//https://judge.yosupo.jp/submission/372107
		long ans = 0;
		for (int v = 1; v < N; v++) ans += parentEdgeLength(v);
		return ans;
	}

	/**
	 * n(n+1)/2個の部分文字列のうち、0-indexedでk番目に辞書順に小さい部分文字列を返す。
	 * @return
	 */
	public String kthSubstring(long k) {
		//https://atcoder.jp/contests/past23-open/submissions/74946979
		String[] ans=new String[1];
		dfs(0, k, ans);
		return ans[0];
	}
	
	
	long dfs(int v, long k, String[] ans) {
		for (int dst=firstChild[v];dst!=-1;dst=nextSibling[dst]) {
			int cost=rangeLCP[dst] - rangeLCP[parent[dst]];
			if(1L * cost * suffixCount[dst] > k) {
				int q = 1 + (int)(k / suffixCount[dst]);
				ans[0] = String.valueOf(Arrays.copyOfRange(S, startPos[dst], startPos[dst] + rangeLCP[v] + q));
				return -1;
			} else {
				k -= 1L * cost * suffixCount[dst];
				long ret=dfs(dst, k, ans);
				if(ret==-1)return -1;
				else k=ret;
			}
		}
		return k;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * サフィックスツリーの状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return サフィックスツリーの状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("SuffixTree size=").append(N).append("\n");
		for (int i = 0; i < N; i++) {
			int p = parent[i];
			String edgeStr = (i == 0) ? "" : getEdgeString(i);
			StringBuilder chBuilder = new StringBuilder("[");
			boolean first = true;
			for (int u = firstChild[i]; u != -1; u = nextSibling[u]) {
				if (!first) chBuilder.append(", ");
				chBuilder.append(u);
				first = false;
			}
			chBuilder.append("]");
			res.append(String.format("Node %d: str=\"%s\" parent=%d edgeStr=\"%s\" rangeLCP=%d startPos=%d suffixCount=%d children=%s",
				i, getString(i), p, edgeStr, rangeLCP[i], startPos[i], suffixCount[i], chBuilder.toString()));
			if (i < N - 1) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	/**
	 * サフィックスツリーの状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}