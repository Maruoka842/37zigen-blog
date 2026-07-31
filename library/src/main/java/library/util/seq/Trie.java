package library.util.seq;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
/**
 * 英小文字にしか対応していない(c-'a'としてint[]にしたものは対応している）
 */
public class Trie {
	int counter = 0;
	int alphabetSize = 26;
	
	public class Node {
		int c;
		int hit = 0;
		Node[] child;
		public int id=counter++;
		
		public Node(char c) {
			child = new Node[alphabetSize];
			this.c = (int) (c - 'a');
		}
		
		public Node(int c) {
			child = new Node[alphabetSize];
			this.c = c;
		}
		
		public Node getChild(char c) {
			return child[c-'a'];
		}
		
		public int hit() {
			return hit;
		}
	}
	
	public ArrayList<Node> nodes = new ArrayList<Trie.Node>();
	
	Node root;
	
	public Trie() {
		root = new Node('#');
	}

	public Trie(int alphabetSize) {
		this.alphabetSize = alphabetSize;
		root = new Node('#');
	}

	
	public Node root() {
		return root;
	}
	
	public void add(char[] s) {
		int[] a=new int[s.length];
		for(int i=0;i<s.length;++i)a[i]=s[i]-'a';
		add(a);
	}
	
	public void add(int[] s) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]] == null) {
				cur.child[s[i]] = new Node(s[i]);
				nodes.add(cur.child[s[i]]);
			}
			cur = cur.child[s[i]];
		}
		cur.hit++;
	}
	
	public boolean contains(int[] s) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]] == null) return false;
			cur = cur.child[s[i]];
		}
		return cur.hit > 0;
	}
	
	public boolean contains(char[] s) {
		int[] a=new int[s.length];
		for(int i=0;i<s.length;++i)a[i]=s[i]-'a';
		return contains(a);
	}

	/**
	 * テキストの接頭辞とマッチするパターンの最大長を返す。
	 * <p>計算量: O(|s|)</p>
	 * @param s テキスト文字列
	 * @return max { |p| | p is a prefix of s and p is in this Trie }. マッチしない場合は 0。
	 */
	public int maxPrefixMatchLength(String s) {
		int[] a = new int[s.length()];
		for (int i = 0; i < s.length(); i++) a[i] = s.charAt(i) - 'a';
		return maxPrefixMatchLength(a);
	}

	/**
	 * テキストの接頭辞とマッチするパターンの最大長を返す。
	 * <p>計算量: O(|s|)</p>
	 * @param s テキスト文字列
	 * @return max { |p| | p is a prefix of s and p is in this Trie }. マッチしない場合は 0。
	 */
	public int maxPrefixMatchLength(char[] s) {
		int[] a = new int[s.length];
		for (int i = 0; i < s.length; i++) a[i] = s[i] - 'a';
		return maxPrefixMatchLength(a);
	}

	/**
	 * テキストの接頭辞とマッチするパターンの最大長を返す。
	 * <p>計算量: O(|s|)</p>
	 * @param s テキスト配列
	 * @return max { |p| | p is a prefix of s and p is in this Trie }. マッチしない場合は 0。
	 */
	public int maxPrefixMatchLength(int[] s) {
		int res = 0;
		Node cur = root;
		for (int i = 0; i < s.length; i++) {
			int c = s[i];
			if (c < 0 || c >= alphabetSize || cur.child[c] == null) break;
			cur = cur.child[c];
			if (cur.hit > 0) res = i + 1;
		}
		return res;
	}

	/**
	 * 追加された相異なる文字列の個数を返す。
	 * @return
	 */
	public int size() {
		int ret=0;
		Queue<Node>que=new ArrayDeque<>();
		que.add(root);
		while(!que.isEmpty()) {
			Node node=que.poll();
			if(node.hit>0)++ret;
			for(Node ch:node.child) {
				if(ch==null)continue;
				que.add(ch);
			}
		}
		return ret;
	}

	/**
	 * トライ木の状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(\text{nodes.size()} \times \text{alphabetSize})$</li>
	 * </ul>
	 * @return トライ木の状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("Trie size=").append(size()).append(" totalNodes=").append(nodes.size() + 1).append("\n");
		java.util.Queue<Node> que = new java.util.ArrayDeque<>();
		que.add(root);
		while (!que.isEmpty()) {
			Node cur = que.poll();
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Node %d: char=%s hit=%d [",
				cur.id, cur == root ? "#" : String.valueOf((char) ('a' + cur.c)), cur.hit));
			boolean first = true;
			for (int i = 0; i < alphabetSize; i++) {
				if (cur.child[i] != null) {
					if (!first) sb.append(", ");
					sb.append((char) ('a' + i)).append("->").append(cur.child[i].id);
					first = false;
					que.add(cur.child[i]);
				}
			}
			sb.append("]");
			res.append(sb.toString());
			if (!que.isEmpty()) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	/**
	 * トライ木の状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(\text{nodes.size()} \times \text{alphabetSize})$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}