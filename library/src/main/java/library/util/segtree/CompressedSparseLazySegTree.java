package library.util.segtree;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import library.util.ArrayUtils;
import library.util.seq.SortedArrays;

public class CompressedSparseLazySegTree <Acting, Acted> {
	private BinaryOperator<Acted> mergeX;
	private BinaryOperator<Acting> mergeA;
	private BiFunction<Acting, Acted, Acted> mergeAX;
	Acted identityX = null;
	LazySegTree<Acting, Acted> seg;
    long[] points;
    ArrayList<Long> addPoints = new ArrayList<>();

	public CompressedSparseLazySegTree(BinaryOperator<Acting> mergeA, BinaryOperator<Acted> mergeX, BiFunction<Acting, Acted, Acted> mergeAX, Acted identityX) {
		this.mergeA = mergeA;
		this.mergeAX = mergeAX;
		this.mergeX = mergeX;
		this.identityX = identityX;
	}
    
	
	/**
	 * set, addで呼ぶ点を登録
	 * @param k
	 */
	public void registerKey(long k) {
		addPoints.add(k);
	}
	
	public void build() {
		points=addPoints.stream().mapToLong(Long::longValue).toArray();
		points=ArrayUtils.sortq(points);
		seg=new LazySegTree<Acting, Acted>(Math.max(1, points.length), mergeA, mergeX, mergeAX, identityX);
	}
	
	public void set(long a, Acted add) {
		int id=SortedArrays.ceil(points, a);
		seg.set(id, add);
	}
	
	public Acted get(long a) {
		return fold(a, a+1);
	}
	
	public void act(long l, long r, Acting add) {
		int lid=SortedArrays.ceil(points, l);
		int rid=SortedArrays.ceil(points, r);
		seg.act(lid, rid, add);
	}

	public Acted fold(long l, long r) {
		int lid=SortedArrays.ceil(points, l);
		int rid=SortedArrays.ceil(points, r);
		return seg.fold(lid, rid);
	}



}
