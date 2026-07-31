package library.util.unionfind;

import java.util.TreeSet;

import library.util.collections.IntArrayList;
import library.util.collections.IntTreapSet;
import library.util.collections.TreeMultiSet;

public class VertexValueUnionFindFactory {
	/**
	 * 初期状態では空のMultiSetが入っている
	 * @param <T>
	 * @param N
	 * @return
	 */
	public static <T extends Comparable<? super T>> VertexValueUnionFind<TreeMultiSet<T>> multiset(int N) {
		VertexValueUnionFind<TreeMultiSet<T>> uf=new VertexValueUnionFind<>(N, (t, u) -> {
        	if (u.size() > t.size()) {
        		u.addAll(t);
        		return u;
        	} else {
        		t.addAll(u);
        		return t;
        	}
        });
		for (int i = 0; i < N; i++) {
			uf.set(i, new TreeMultiSet<>());
		}

		return uf;
    }
	
	
	
	
	public static VertexValueUnionFind<IntArrayList> intArrayList(int N) {
		//https://atcoder.jp/contests/abc239/submissions/74359008
		VertexValueUnionFind<IntArrayList> uf=new VertexValueUnionFind<>(N, (t, u) -> {
        	if (u.size() > t.size()) {
        		for (int v : t) {
        			u.add(v);
        		}
        		return u;
        	} else {
        		for (int v : u) {
        			t.add(v);
        		}
        		return t;
        	}
        });
		for (int i = 0; i < N; i++) {
			uf.set(i, new IntArrayList());
		}
		return uf;
    }
	
	/**
	 * 初期値0
	 * @param n
	 * @return
	 */
	public static IntVertexValueUnionFind intsum(int n) {
		var uf=new IntVertexValueUnionFind(n, Integer::sum);
		return uf;
	}
	
	
	/**
	 * 初期値0
	 * @param n
	 * @return
	 */
	public static LongVertexValueUnionFind sum(int n) {
		var uf=new LongVertexValueUnionFind(n, Long::sum);
		return uf;
	}
	
	
	/**
	 * 初期状態では空のTreeSetが入っている
	 * @param <T>
	 * @param N
	 * @return
	 */
	public static <T extends Comparable<? super T>> VertexValueUnionFind<TreeSet<T>> set(int N) {
		VertexValueUnionFind<TreeSet<T>> uf=new VertexValueUnionFind<>(N, (t, u) -> {
        	if (u.size() > t.size()) {
        		u.addAll(t);
        		return u;
        	} else {
        		t.addAll(u);
        		return t;
        	}
        });
		for (int i = 0; i < N; i++) {
			uf.set(i, new TreeSet<>());
		}
    	return uf;
    }
	
	
	public static VertexValueUnionFind<IntTreapSet> intTreapSet(int N) {
		VertexValueUnionFind<IntTreapSet> uf=new VertexValueUnionFind<>(N, (t, u) -> {
        	if (u.size() > t.size()) {
        		u.addAll(t);
        		return u;
        	} else {
        		t.addAll(u);
        		return t;
        	}
        });
		for (int i = 0; i < N; i++) {
			uf.set(i, new IntTreapSet());
		}
    	return uf;
    }

	
	/**
	 * 最小の代表元を取るUnionFind
	 * @param N
	 * @return
	 */
	public static VertexValueUnionFind<Integer> minRepresentativeUnionFind(int N) {
    	VertexValueUnionFind<Integer>uf=new VertexValueUnionFind<Integer>(N, Integer::min);
    	for (int i = 0; i < N; i++) {
			uf.set(i, i);
		}
    	return uf;
	}

	/**
	 * 最小の代表元を取るUnionFind
	 * @param N
	 * @return
	 */
	public static VertexValueUnionFind<Integer> maxRepresentativeUnionFind(int N) {
    	VertexValueUnionFind<Integer>uf=new VertexValueUnionFind<Integer>(N, Integer::max);
    	for (int i = 0; i < N; i++) {
			uf.set(i, i);
		}
    	return uf;
	}
	
	/**
	 * セットされた値のminとmaxを持つ
	 * @param N
	 * @return
	 */
	public static VertexValueUnionFind<int[]> minMaxInt(int N) {
		return new VertexValueUnionFind<int[]>(N, (x,y)->new int[] {Math.min(x[0], y[0]),Math.max(x[1], y[1])});
	}	
	

}
