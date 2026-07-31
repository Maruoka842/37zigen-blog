package library.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import library.util.collections.IntArrayList;
import library.util.graph.Digraph;
import library.util.unionfind.UnionFind;
/**
 * 始点は0
 */
public class DFA {
	int n;
	int m=0;
	int alphabetSize;
	int[][] adj;
	boolean[] isAcceptable;
	
	
	public DFA(int n, int alphabetSize) {
		this.n = n;
		this.alphabetSize = alphabetSize;
		adj = new int[n][alphabetSize];
		ArrayUtils.fill(adj, -1);
		isAcceptable = new boolean[n];
	}
	
	public int size() {
		return n;
	}
	
	public void setAcceptable(int v) {
		isAcceptable[v] = true;
	}
	
	public void addEdge(int from, int to, int symbol) {
		++m;
		adj[from][symbol] = to;
	}
	
	public int transite(int from, int symbol) {
		return adj[from][symbol];
	}
	
	public int run(int[] input) {
		int cur = 0;
		for (int symbol : input) {
			cur = adj[cur][symbol];
			if (cur == -1) return -1; // dead
		}
		return cur;
	}
	
	public boolean accepts(int[] input) {
		int last = run(input);
		return last != -1 && isAcceptable[last];
	}
	
	public boolean isAcceptable(int v) {
		return isAcceptable[v];
	}
	
	public int next(int v, int symbol) {
		return adj[v][symbol];
	}
	
	/* 例：https://en.wikipedia.org/wiki/DFA_minimization#/media/File:DFA_to_be_minimized.svg
	 *  DFA dfa=new DFA(6, 2);
		dfa.addEdge(0, 1, 0);
		dfa.addEdge(0, 2, 1);
		dfa.addEdge(1, 0, 0);
		dfa.addEdge(1, 3, 1);
		dfa.addEdge(2, 4, 0);
		dfa.addEdge(2, 5, 1);
		dfa.addEdge(3, 4, 0);
		dfa.addEdge(3, 5, 1);
		dfa.addEdge(4, 4, 0);
		dfa.addEdge(4, 5, 1);
		dfa.addEdge(5, 5, 0);
		dfa.addEdge(5, 5, 1);
		dfa.setAcceptable(2);
		dfa.setAcceptable(3);
		dfa.setAcceptable(4);
		dfa.draw();
		var x=dfa.minimize();
	 * 
	 */
	
	/**
	 * Valmari, Antti. "Fast brief practical DFA minimization." Information Processing Letters 112.6 (2012): 213-217.
	 * @return
	 * https://atcoder.jp/contests/abc418/submissions/72991363
	 */
	public DFA minimize() {
		RefineSet vertexEquivalence=new RefineSet(n);
		RefineSet edgeEquivalence=new RefineSet(m);
		int[] src=new int[m];
		int[] dst=new int[m];
		int[] symbol=new int[m];
		IntArrayList[] dstToEdgeId=new IntArrayList[n];
		for (int i = 0; i < n; i++) {
			dstToEdgeId[i]=new IntArrayList();
		}
		{
			int size = 0;
			for (int i = 0; i < alphabetSize; i++) {
				for (int j = 0; j < n; j++) {
					if(adj[j][i]!=-1) {
						src[size]=j;
						symbol[size]=i;
						dst[size]=adj[j][i];
						dstToEdgeId[dst[size]].add(size);
						size++;
					}
				}
			}
		}
		for (int i = 0; i < n; i++) {
			if(isAcceptable(i)) {
				vertexEquivalence.mark(i);
			}
		}
		vertexEquivalence.refine();
		
		for (int i = 0; i < m; i++) {
			int j=i;
			while(j+1<m && symbol[i]==symbol[j+1])++j;
			for (int k = i; k <= j; k++) {
				edgeEquivalence.mark(k);
			}
			edgeEquivalence.refine();
			i=j;
		}
		
		for (int c = 0, b = 1; c < edgeEquivalence.numberOfBlocks(); c++) {
			for (int e : edgeEquivalence.elementsInBlock(c)) {
				vertexEquivalence.mark(src[e]);
			}
			vertexEquivalence.refine();
			for (; b < vertexEquivalence.numberOfBlocks(); b++) {
				for (int v : vertexEquivalence.elementsInBlock(b)) {
					for (int e : dstToEdgeId[v]) {
						edgeEquivalence.mark(e);
					}
				}
				edgeEquivalence.refine();
			}
		}
		int newN = vertexEquivalence.numberOfBlocks;
		DFA ret = new DFA(newN, alphabetSize);
		int[]f=new int[newN];//blockId(0)=0となるように調節
		Arrays.setAll(f, i->i);
		if(vertexEquivalence.blockid(0)!=0) {
			int a=vertexEquivalence.blockid(0);
			f[a]=0;
			f[0]=a;
		}
		for (int i = 0; i < m; i++) {//edgeEquivalence自体は最小DFAの辺の同値類にはなっていない
			if(vertexEquivalence.elementToRepresentative(src[i]) == src[i]) {
				ret.addEdge(f[vertexEquivalence.blockid(src[i])], f[vertexEquivalence.blockid(dst[i])], symbol[i]);
			}
		}
		for (int i = 0; i < n; i++) {
			if(isAcceptable(i)) {
				ret.setAcceptable(f[vertexEquivalence.blockid(i)]);
			}
		}
		return ret;
	}
	
	/**
	 * このDFAは、最小DFAが文字列の長さLで抑えられるDFA　Xの、長さ2L-1までの文字列全てを使い、それ以降は途中で打ち切ったDFAとする。
	 * DFA　Xを復元する。
	 * @param L
	 * @return
	 * https://atcoder.jp/contests/abc418/submissions/72991363
	 */
	public DFA compress(int L) {
		UnionFind uf=new UnionFind(n);
		Digraph g=new Digraph(n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < alphabetSize; j++) {
				if(adj[i][j]!=-1) {
					g.addEdge(i, adj[i][j]);
				}
			}
		}
		int[]dist=g.bfsDistances(0);
		IntArrayList list=new IntArrayList();
		for (int i = 0; i < n; i++) {
			if(dist[i]<=L) {
				list.add(i);
			}
		}
		boolean[][][] dp=new boolean[n][n][L];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				dp[i][j][0]=(isAcceptable(i)==isAcceptable(j));
			}
		}
		for (int len = 1; len < L; len++) {
			for (int u = 0; u < n; u++) {
				for (int v = 0; v < n; v++) {
					dp[u][v][len]=dp[u][v][len-1];
					for (int i = 0; i < alphabetSize && dp[u][v][len]; i++) {
						dp[u][v][len] &=(adj[u][i]==-1)==(adj[v][i]==-1);
						if(adj[u][i]!=-1 && dp[u][v][len]) {
							dp[u][v][len] &= dp[adj[u][i]][adj[v][i]][len-1];
						}
					}
					
				}
			}
		}
		for (int u : list) {
			for (int v : list) {
				if (uf.equiv(u, v)) continue;
				if (dp[u][v][L-1]) {
					uf.union(u, v);
				}
			}
		}
		int[] rootToId=new int[n];
		Arrays.fill(rootToId, -1);
		{
			int pointer = 0;
			for (int i = 0; i < n; i++) {
				if(uf.isRoot(i) && dist[i] <= L) rootToId[i] = pointer++;
			}
			int x=rootToId[uf.root(0)];
			if(x != 0) {
				{
					int k=0;
					while(rootToId[k]!=0)++k;
					rootToId[uf.root(0)]=0;
					rootToId[k]=x;
				}
			}
		}
		int newN = 0;
		for (int i = 0; i < n; i++) {
			if(uf.isRoot(i) && dist[i] <= L) ++newN;
		}
		DFA dfa=new DFA(newN, alphabetSize);
		for (int i = 0; i < n; i++) {
			if(uf.isRoot(i) && dist[i] <= L) {
				if(isAcceptable[i]) dfa.setAcceptable(rootToId[i]);
				for (int j = 0; j < alphabetSize; j++) {
					if (adj[i][j] != -1) {
						dfa.addEdge(rootToId[i], rootToId[uf.root(adj[i][j])], j);
					}
				}
			}
		}
		return dfa;
	}
	
	
	
	public DFA removeDeadends() {
		Digraph g=new Digraph(n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < alphabetSize; j++) {
				if(adj[i][j]!=-1) {
					g.addEdge(adj[i][j], i);
				}
			}
		}
		IntArrayList list=new IntArrayList();
		for (int i = 0; i < n; i++) {
			if(isAcceptable(i)) {
				list.add(i);
			}
		}
		int[] dst=g.bfsDistances(list);
		int newN=0;
		int[]id=new int[n];
		for (int i = 0; i < n; i++) {
			if(dst[i]<n) {
				id[i]=newN;
				newN++;
			}
		}
		DFA dfa=new DFA(newN, 2);
		for (int i = 0; i < n; i++) {
			if(dst[i]>=n)continue;
			for (int j = 0; j < alphabetSize; j++) {
				if(adj[i][j]==-1)continue;
				if(dst[adj[i][j]]>=n)continue;
				dfa.addEdge(id[i], id[adj[i][j]], j);
			}
		}
		for (int i = 0; i < n; i++) {
			if(isAcceptable(i))dfa.setAcceptable(id[i]);
		}
		return dfa;
	}
	
	public void draw() {

	    System.setProperty("org.graphstream.ui", "swing");

	    org.graphstream.graph.Graph g = new org.graphstream.graph.implementations.SingleGraph("DFA");

	    // DFA専用のスタイルシート
	    // 受理状態(accept)は外枠を太く
	    g.setAttribute("ui.stylesheet", """
	        node {
	            fill-color: white;
	            size: 35px;
	            text-alignment: center;
	            text-size: 18;
	            stroke-mode: plain;
	            stroke-color: black;
	            stroke-width: 1px;
		    z-index: 1;
	        }
	        node.accept {
	            stroke-width: 4px;
	            fill-color: #E0FFE0;
	        }
	        edge {
	            fill-color: black;
	            text-size: 15px;
	            text-background-mode: plain;
	            text-background-color: white;
	            arrow-size: 10px, 6px;
	            shape: cubic-curve;
                text-alignment: center;
			text-offset: 8px;
	        }
	        edge.loop {
			text-offset: 20px;
		}
	    """);

	    // ノードの追加
	    for (int i = 0; i < n; i++) {
	        org.graphstream.graph.Node node = g.addNode(String.valueOf(i));
	        node.setAttribute("ui.label", String.valueOf(i));

	        // スタイルの適用
	        String uiClass = "";
	        if (isAcceptable[i]) uiClass += "accept";
	        node.setAttribute("ui.class", uiClass.trim());
	    }

	    // エッジの追加（同じ遷移先へのラベルをまとめる）
	    for (int u = 0; u < n; u++) {
	        // 遷移先ごとに記号をグループ化する (dest -> list of symbols)
	        Map<Integer, List<Integer>> group = new HashMap<>();
	        for (int s = 0; s < alphabetSize; s++) {
	            int v = adj[u][s];
	            if (v != -1) {
	                group.computeIfAbsent(v, k -> new ArrayList<>()).add(s);
	            }
	        }

	        for (Map.Entry<Integer, List<Integer>> entry : group.entrySet()) {
	            int v = entry.getKey();
	            List<Integer> symbols = entry.getValue();

	            String id = u + "->" + v;
	            org.graphstream.graph.Edge edge = g.addEdge(id, String.valueOf(u), String.valueOf(v), true);

	            // ラベルを "0, 1, 2" の形式に整形
	            Collections.sort(symbols);
	            StringBuilder label = new StringBuilder();
	            for (int i = 0; i < symbols.size(); i++) {
	                label.append(symbols.get(i));
	                if (i < symbols.size() - 1) label.append(",");
	            }
	            if (u == v) edge.setAttribute("ui.class", "loop");
	            edge.setAttribute("ui.label", label.toString());
	        }
	    }
	    g.display();
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}