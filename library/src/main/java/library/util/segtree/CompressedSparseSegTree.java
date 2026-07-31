package library.util.segtree;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.seq.SortedArrays;

public class CompressedSparseSegTree<T> {
	T identity;
    java.util.function.BinaryOperator<T> op;
    long[] points;
    ArrayList<Long> addPoints = new ArrayList<>();
    SegTree<T> seg;
    boolean built=false;
    		
	public CompressedSparseSegTree(java.util.function.BinaryOperator<T> op, T identity) {
		this.op = op;
		this.identity = identity;
	}
	
	/**
	 * {@link #set(long, Object)}, {@link #mul(long, Object)}　を呼び出す点を事前に登録する。
	 * @param k
	 */
	public void registerKey(long k) {
		addPoints.add(k);
	}
	
	public void build() {
		built=true;
		points=addPoints.stream().mapToLong(Long::longValue).toArray();
		points=ArrayUtils.sortq(points);
		seg=new SegTree<T>(Math.max(1, points.length), op, identity);
	}
	
	public void set(long k, T t) {
		int id=SortedArrays.indexOf(points, k);
		if(id==-1)throw new AssertionError();
		seg.set(id, t);
	}
	
	public void mul(long k, T t) {
		int id=SortedArrays.indexOf(points, k);
		if(id==-1)throw new AssertionError();
		seg.mul(id, t);
	}

	public T fold(long l, long r) {
		if(!built) throw new AssertionError();
		int lid=SortedArrays.ceil(points, l);
		int rid=SortedArrays.ceil(points, r);
		return seg.fold(lid, rid);
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
