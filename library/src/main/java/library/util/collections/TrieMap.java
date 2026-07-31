package library.util.collections;

import java.util.ArrayDeque;
import java.util.Queue;

public class TrieMap {
	class Node {
		int c;
		int hit = 0;
		Node[] child = new Node[26];
		long value;
		
		public Node(char c) {
			this.c = (int) (c - 'a');
		}
		
		public Node(int c) {
			this.c = c;
		}
	}
	
	
	Node root = new Node('#');
	
	public void add(char[] s, long value) {
		int[] a=new int[s.length];
		for(int i=0;i<s.length;++i)a[i]=s[i]-'a';
		put(a, value);
	}
	
	public void put(int[] s, long value) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]] == null) {
				cur.child[s[i]] = new Node(s[i]);
			}
			cur = cur.child[s[i]];
		}
		cur.hit++;
		cur.value = value;
	}
	
	public boolean contains(int[] s) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]] == null) return false;
			cur = cur.child[s[i]];
		}
		return cur.hit > 0;
	}
	
	
	public long get(char[] s) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]-'a'] == null) throw new AssertionError();
			cur = cur.child[s[i]-'a'];
		}
		return cur.value;
	}
	
	public long get(int[] s) {
		Node cur = root;
		for (int i = 0; i < s.length; ++i) {
			if (cur.child[s[i]] == null) throw new AssertionError();
			cur = cur.child[s[i]];
		}
		return cur.value;
	}
	
	
	public boolean contains(char[] s) {
		int[] a=new int[s.length];
		for(int i=0;i<s.length;++i)a[i]=s[i]-'a';
		return contains(a);
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
	 * このマップと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(\text{numberOfNodes})$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TrieMap)) return false;
		TrieMap other = (TrieMap) o;
		return equals(this.root, other.root);
	}

	private boolean equals(Node n1, Node n2) {
		if (n1 == n2) return true;
		if (n1 == null || n2 == null) return false;
		if (n1.c != n2.c) return false;
		if (n1.hit != n2.hit) return false;
		if (n1.hit > 0 && n1.value != n2.value) return false;
		for (int i = 0; i < 26; i++) {
			if (!equals(n1.child[i], n2.child[i])) return false;
		}
		return true;
	}

	/**
	 * このマップのハッシュコード値を返します。
	 * @return ハッシュコード値
	 * $O(\text{numberOfNodes})$
	 * // 未テスト
	 */
	@Override
	public int hashCode() {
		return hashCode(root);
	}

	private int hashCode(Node node) {
		if (node == null) return 0;
		int h = Integer.hashCode(node.c);
		h = 31 * h + Integer.hashCode(node.hit);
		if (node.hit > 0) {
			h = 31 * h + Long.hashCode(node.value);
		}
		for (int i = 0; i < 26; i++) {
			if (node.child[i] != null) {
				h = 31 * h + hashCode(node.child[i]);
			}
		}
		return h;
	}
}