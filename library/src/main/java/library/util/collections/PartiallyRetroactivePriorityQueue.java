package library.util.collections	;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import library.util.segtree.LazySegTreelonglong;
import library.util.segtree.SegTreeFactory;
import library.util.segtree.SegTreelong;
/**
 * 
 * https://atcoder.jp/contests/abc363/submissions/72780962
 */
public class PartiallyRetroactivePriorityQueue {
	SegTreelong alives;
	SegTreelong deads;
	LazySegTreelonglong prefixSum;
	TreeSet<Integer> eventTimes = new TreeSet<Integer>();
	long deadsSum=0;
	int n;
	int[] opType;// opType[i]=(時刻iの操作の種類)。0:empty,1:add,2:deleteMin
	int offset;
	long INF=Long.MAX_VALUE/3;
	
	public PartiallyRetroactivePriorityQueue(int n) {
		this.offset = n;
		this.n = n + offset;
		alives = SegTreeFactory.min(this.n);
		deads = SegTreeFactory.max(this.n);
		prefixSum = SegTreeFactory.add_min(this.n);
		opType = new int[this.n];
		prefixSum.fill(0);
		for (int i = 0; i < offset; i++) {
			add(i-offset, INF);
		}
	}

	public long peek() {
		return alives.prodAll();
	}

	// 時刻timeの操作をemptyからadd valに変更
	public void add(int time, long val) {
		time += offset;
		if(opType[time]!=0)throw new AssertionError();
		opType[time] = 1;
		if (deads.prodAll() == deads.identity()) {
			alives.set(time, val);
			eventTimes.add(time);
			return;
		}
		deads.set(time, val);
		if (val!=INF) {
			deadsSum += val;
		}
		int l, r;
		long min = prefixSum.fold(time, n);
		Integer ceilingTime = eventTimes.ceiling(time);
		if (ceilingTime == null)
			ceilingTime = time;
		r = prefixSum.maximalRight(ceilingTime, v -> v != min);
		l = prefixSum.minimalLeft(r - 1, v -> v != min) + 1;
		l = Math.min(l, time);
		r = n;
		long v = deads.fold(l, r);
		int vPos = deads.maximalRight(l, a -> a < v);
		deads.set(vPos, deads.identity());
		alives.set(vPos, v);
		prefixSum.act(vPos, n, -1);
		prefixSum.act(time, n, 1);
		eventTimes.add(time);
		if (v != INF) {
			deadsSum -= v;
		}
	}

	// 時刻timeの操作をemptyからdeleteMinに変更
	public void deleteMin(int time) {
		time += offset;
		if(opType[time]!=0)throw new AssertionError();
		opType[time] = 2;
		int r = prefixSum.maximalRight(time, v -> v != 0);
		long minVal = alives.fold(0, r);
		int pos = alives.maximalRight(0, v -> v > minVal);
		alives.set(pos, alives.identity());
		prefixSum.act(time, n, -1);
		prefixSum.act(pos, n, 1);
		deads.set(pos, minVal);
		eventTimes.add(time);
		if (minVal != INF) deadsSum += minVal;
	}

	public void undo(int time) {
		time += offset;
		if (opType[time] == 0)
			return;
		eventTimes.remove(time);
		if (opType[time] == 1) {// undo add
			if (alives.get(time) != alives.identity()) {
				alives.set(time, alives.identity());
				opType[time]=0;
			} else {//undoするadd vがpoll済みだった場合
				int r = prefixSum.maximalRight(time, v -> v != 0);
				int l = prefixSum.minimalLeft(r - 1, v -> v != 0) + 1;
				l = Math.min(l, time);
				long deadVal = deads.get(time);
				deads.set(time, deads.identity());
				if (deadVal != INF) deadsSum -= deadVal;
				prefixSum.act(time, n, -1);
				long val=alives.fold(0, r);
				int pos=alives.minimalLeft(r, v->v!=val);
				prefixSum.act(pos, n, 1);
				alives.set(pos, alives.identity());
				deads.set(pos, val);
				if (val != INF) deadsSum += val;
				opType[time]=0;
			}
		} else {// undo deleteMin
			int l = prefixSum.minimalLeft(time-1, v -> v != 0)+1;
			long val=deads.fold(l, n);
			int pos=deads.maximalRight(l, v->v!=val);
			if (val != INF) deadsSum -= val;
			deads.set(pos, deads.identity());
			alives.set(pos, val);
			prefixSum.act(pos, n, -1);
			prefixSum.act(time, n, 1);
			opType[time]=0;
		}
	}
	
	public long polledValueSum() {
		return deadsSum;
	}

	/**
	 * このプライオリティキューを表す文字列を返します。
	 *
	 * <p>計算量: $O(N)$</p>
	 *
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "PartiallyRetroactivePriorityQueue{opType=" + java.util.Arrays.toString(opType) +
				", deadsSum=" + deadsSum +
				"}";
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}
