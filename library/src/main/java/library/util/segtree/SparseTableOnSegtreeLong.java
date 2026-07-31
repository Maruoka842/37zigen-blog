package library.util.segtree;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.LongBinaryOperator;
import java.util.function.Predicate;

import library.util.ArrayUtils;
import library.util.fold.SparseTableLong;

/***
 * 
 * https://atcoder.jp/contests/abc228/submissions/71560169
 */
public class SparseTableOnSegtreeLong {
	int n;//2冪
	LongBinaryOperator op;
	SparseTableLong[] v;
	long identity;
	
	public SparseTableOnSegtreeLong(long[][] a, LongBinaryOperator op, long identity) {
		n = Integer.highestOneBit(a.length) << 1;
		this.identity = identity;
		this.op = op;
		v = new SparseTableLong[2*n];
		build(a);
	}
	
	public void build(long[][] a) {
		for (int i = 0; i < n; i++) {
			if (i < a.length) {
				v[id(i, i + 1)] = new SparseTableLong(a[i], op);
			} else {
				v[id(i, i + 1)] = new SparseTableLong(ArrayUtils.full(identity, a[0].length), op);
			}
		}
		for (int i = n - 1; i >= 1; i--) {
			long[]b=new long[a[0].length];
			for (int j = 0; j < a[0].length; j++) {
				b[j]=op.applyAsLong(v[2*i].fold(j, j+1), v[2*i+1].fold(j, j+1));
			}
			v[i] = new SparseTableLong(b, op);
		}
	}
	
	// [a2^k, (a+1)2^k)
	int id(int a, int b) {
		int w = b - a;
		return n / w + a / w;
	}
	
	public long fold(int i0, int j0, int i1, int j1) {
		i1=Math.min(i1, n);
		i0=Math.max(i0, 0);
		if (i1 - i0 <= 0) return identity;
		int ml = i0 + Integer.lowestOneBit(i0);
		int mr = i1 - Integer.lowestOneBit(i1);
		if (i0 < ml && ml <= i1) {
			return op.applyAsLong(v[id(i0, ml)].fold(j0, j1), fold(ml, j0, i1, j1));
		} else {
			return op.applyAsLong(fold(i0, j0, mr, j1), v[id(mr, i1)].fold(j0, j1));
		}
	}
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}