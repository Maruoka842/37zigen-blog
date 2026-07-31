package library.util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * 同じ値の区間は自動でマージされる。
 * @param <PositionType>
 * @param <X>
 */
public class IntervalMapWithAdjacentMerging<PositionType, X> {
	//https://yukicoder.me/submissions/1130804
	TreeMap<PositionType, X> map;
	X voidValue;
	PositionType L;//定義域は[L, R)
	PositionType R;
	Comparator<PositionType> comparator;
	
	/**
	 * 定義域を[l, r)として、[l, r)の値をvoidValueに初期化
	 * @param l
	 * @param r
	 * @param voidValue
	 */
	public IntervalMapWithAdjacentMerging(PositionType l, PositionType r, X voidValue) {
		map = new TreeMap<>();//区間の左端に対して、その区間の色を返す
		this.voidValue = voidValue;
		L=l;
		R=r;
		map.put(L, voidValue);
		map.put(R, voidValue);
	}
	
	
	/**
	 * [l, r)
	 * @param l
	 * @param r
	 * @param val
	 */
	public void put(PositionType l, PositionType r, X val) {
		if(compare(l,L)==-1)l=L;
		if(compare(r,R)==1)r=R;
		if(compare(l,r)>=0)return;
		var mr=map.floorEntry(r);
		if(mr==null)throw new AssertionError();
		if (!mr.getKey().equals(r)) {//  r-1, r ∈[a, b) となる区間を [a, r) [r, b) で分割
			map.put(r, mr.getValue());
		}
		do {
			PositionType ml=map.ceilingKey(l);
			if (compare(ml, r) == -1) {
				map.remove(ml);
			} else {
				break;
			}
		} while (true);
		map.put(l, val);
		mergeAt(l);
		mergeAt(r);
	}
	
	/**
	 * [l, r)と交わる区間を列挙する。voidValueの区間も含める。
	 * @param l
	 * @param r
	 * verified:https://atcoder.jp/contests/abc430/submissions/70639556
	 */
	public ArrayList<Interval> intersectingIntervals(PositionType l, PositionType r) {
		if(compare(l,L)==-1)l=L;
		if(compare(r,R)==1)r=R;
		if(compare(l,r)>=0)return new ArrayList<>();
		
		var left = map.floorEntry(l);
		ArrayList<Interval> list=new ArrayList<>();
		while (compare(left.getKey(), r) == -1) {
			var ml = map.higherEntry(left.getKey());
			list.add(new Interval(left.getKey(), ml.getKey(), left.getValue()));
			left=ml;
		}
		return list;
	}
	
	
	/**
	 * voidValue以外が設定された区間を昇順に列挙する。
	 */
	public ArrayList<Interval> nonemptyIntervals() {
		//https://atcoder.jp/contests/abc256/submissions/73810110
		var l=L;
		var r=R;
		var left = map.firstEntry();
		ArrayList<Interval> list=new ArrayList<>();
		while (compare(left.getKey(), r) == -1) {
			var ml = map.higherEntry(left.getKey());
			var interval=new Interval(left.getKey(), ml.getKey(), left.getValue());
			if(!interval.x.equals(voidValue)) {
				list.add(interval);
			}
			left=ml;
		}
		return list;
	}

	
	
	/**
	 * posを含む区間が存在するか判定
	 * @param pos
	 * @return
	 */
	public boolean contains(PositionType pos) {
		var l=map.floorEntry(pos);
		return !l.getValue().equals(voidValue);
	}
	/**
	 * posを含む区間を返す
	 * @param pos
	 * @return
	 */
	public Interval get(PositionType pos) {
		if(compare(pos, L)==-1)throw new AssertionError();
		if(compare(pos, R) >=0)throw new AssertionError();
		var l=map.floorEntry(pos);
		return new Interval(l.getKey(), map.higherKey(pos), l.getValue());
	}
	
	public class Interval {
		public PositionType l;
		public PositionType r;
		public X x;
		
		public Interval(PositionType l, PositionType r, X x) {
			this.l = l;
			this.r = r;
			this.x = x;
		}
		
		@Override
		public String toString() {
			return "(l,r,x)="+l+","+r+","+x;
		}
		
	}	

	
	/**
	 *[hoge,  pos)[pos, fuga) の二つの区間が同じ値ならマージする
	 * @param pos
	 */
	public void mergeAt(PositionType pos) {
		var left=map.lowerEntry(pos);
		if (left != null && left.getValue().equals(map.get(pos))) {
			map.remove(pos);
		}
	}
	
	@SuppressWarnings("unchecked")
	int compare(PositionType k1, PositionType k2) {
	    return comparator == null
	    		? ((Comparable<? super PositionType>) k1).compareTo(k2)
	        : comparator.compare(k1, k2);
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for (var es:map.entrySet()) {
			sb=sb.append("[l="+es.getKey()+",x="+es.getValue()+")");
		}
		return sb.toString();
	}
}