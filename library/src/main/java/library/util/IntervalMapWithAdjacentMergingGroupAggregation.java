package library.util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * 同じ値の隣接する区間を自動でマージし、区間全体のアーベル群上の集計値を保持するデータ構造。
 * 各区間 [l, r) に対して値 x が割り当てられており、それらに対する集計を {@link RangeAbelianGroupAggregator} を用いて効率的に行う。
 * voidValueに対しても集計が行われることに注意。
 * @param <PositionType> 座標の型（Comparable であるか、Comparator を指定する必要がある）
 * @param <X> 各区間に割り当てる値の型
 * @param <G> 集計結果の型
 */
public class IntervalMapWithAdjacentMergingGroupAggregation<PositionType, X> {
	//https://atcoder.jp/contests/abc411/submissions/74151586
	//https://atcoder.jp/contests/past202104-open/submissions/77253504
	TreeMap<PositionType, X> map;
	X voidValue;
	PositionType L;//定義域は[L, R)
	PositionType R;
	Comparator<PositionType> comparator;
	RangeAbelianGroupAggregator<PositionType, X> aggregator;
	
	/**
	 * 指定された定義域 [l, r) を voidValue で初期化して構築する。
	 *
	 * @param l 定義域の左端
	 * @param r 定義域の右端
	 * @param voidValue 初期値
	 * @param aggregator アーベル群上の集計を管理するアグリゲータ
	 */
	public IntervalMapWithAdjacentMergingGroupAggregation(PositionType l, PositionType r, X voidValue, RangeAbelianGroupAggregator<PositionType, X> aggregator) {
		map = new TreeMap<>();//区間の左端に対して、その区間の色を返す
		this.voidValue = voidValue;
		this.aggregator = aggregator;
		L=l;
		R=r;
		map.put(L, voidValue);
		map.put(R, voidValue);
		aggregator.add(l, r, voidValue);
	}
	
	
	/**
	 * 区間 [l, r) の値を val に更新する。
	 * 隣接する区間が同じ値になった場合は自動的にマージされる。
	 *
	 * @param l 更新範囲の左端
	 * @param r 更新範囲の右端
	 * @param val 新しい値
	 */
	public void put(PositionType l, PositionType r, X val) {
		if(compare(l,L)==-1)l=L;
		if(compare(r,R)==1)r=R;
		if(compare(l,r)>=0)return;
		var mrEntry=map.floorEntry(r);
		if(mrEntry==null)throw new AssertionError();
		if (!mrEntry.getKey().equals(r)) {//  r-1, r ∈[mr, b) となる区間を [mr, r) [r, b) で分割
			{
				X oldValue = mrEntry.getValue();
				var b = map.higherKey(r);
				var mr = mrEntry.getKey();
				aggregator.remove(mr, b, oldValue);
				aggregator.add(mr, r, oldValue);
				aggregator.add(r, b, oldValue);
			}
			map.put(r, mrEntry.getValue());
		}
		
		// [ml, next) ∈　l を l で分割
		
		{
			var mlEntry = map.floorEntry(l);
			if (!mlEntry.getKey().equals(l)) {
			    X oldValue = mlEntry.getValue();
			    PositionType next = map.higherKey(mlEntry.getKey());
			    aggregator.remove(mlEntry.getKey(), next, oldValue);
			    aggregator.add(mlEntry.getKey(), l, oldValue);
			    aggregator.add(l, next, oldValue);
			    map.put(l, oldValue);
			}
		}
		
		
		
		do {
			var mlEntry = map.ceilingEntry(l);
			PositionType ml=mlEntry.getKey();
			if (compare(ml, r) == -1) {
				var next = map.higherKey(ml);
				aggregator.remove(ml, next, mlEntry.getValue());
				
				map.remove(ml);
			} else {
				break;
			}
		} while (true);
		map.put(l, val);
		
		aggregator.add(l, r, val);
		
		
		mergeAt(l);
		mergeAt(r);
	}
	
	/**
	 * 指定された範囲 [l, r) と交差する全ての区間を昇順に列挙する。
	 * voidValue の区間も含まれる。
	 *
	 * @param l 範囲の左端
	 * @param r 範囲の右端
	 * @return 交差する区間のリスト
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
	 * 値が voidValue ではない区間を昇順に列挙する。
	 *
	 * @return 非空（voidValue 以外）の区間のリスト
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
	 * 指定された座標 pos を含む区間の値が voidValue ではないか判定する。
	 *
	 * @param pos 座標
	 * @return voidValue ではない場合は true、そうでない場合は false
	 */
	public boolean contains(PositionType pos) {
		var l=map.floorEntry(pos);
		return !l.getValue().equals(voidValue);
	}
	/**
	 * 指定された座標 pos を含む区間を返す。
	 *
	 * @param pos 座標
	 * @return pos を含む区間情報
	 */
	public Interval get(PositionType pos) {
		if(compare(pos, L)==-1)throw new AssertionError();
		if(compare(pos, R) >=0)throw new AssertionError();
		var l=map.floorEntry(pos);
		return new Interval(l.getKey(), map.higherKey(pos), l.getValue());
	}
	
	/**
	 * 区間情報を表すクラス。
	 */
	public class Interval {
		/** 左端（閉） */
		public PositionType l;
		/** 右端（開） */
		public PositionType r;
		/** 区間の値 */
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
	 * 指定された境界 pos において、左右の区間の値が同じであればマージする。
	 * 通常は put メソッド内で自動的に呼ばれる。
	 *
	 * @param pos 境界の座標
	 */
	public void mergeAt(PositionType pos) {
		var leftEntry=map.lowerEntry(pos);
		if (leftEntry != null && leftEntry.getValue().equals(map.get(pos))) {
			var right=map.higherKey(pos);
			var left=leftEntry.getKey();
			aggregator.remove(left, pos, leftEntry.getValue());
			aggregator.remove(pos, right, leftEntry.getValue());
			aggregator.add(left, right, leftEntry.getValue());
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