
package library.util.segtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import library.util.ArrayUtils;
import library.util.seq.SortedArrays;
/**
 *  * 二次元平面における静的な矩形加算を x 方向にスイープしながら処理し、
 * 任意の y 区間における最大値とその y 座標を求めるデータ構造。
 *
 * <p>特徴：
 * <ul>
 *   <li>矩形 [x0, x1) × [y0, y1) への加算を登録可能</li>
 *   <li>x 昇順にクエリ argmax(x, y0, y1) を呼び出すことで、
 *       x 以下の加算をすべて反映した状態の y 区間最大値を返す</li>
 *   <li>内部では差分イベント（x0 で +add、x1 で -add）を用いる</li>
 *   <li>y はすべて圧縮され LazySegTreelonglong により管理される</li>
 * </ul>
 *
 * 使用手順：
 * <ol>
 *   <li>rectAdd(...) で全ての更新を追加する</li>
 *   <li>build()</li>
 *   <li>x を単調非減少で argmax(x, y0, y1) を呼ぶ</li>
 * </ol>
 *
 * 制約・注意：
 * <ul>
 *   <li>x クエリは必ず昇順で行うこと（逆順は例外）</li>
 *   <li>オーバーフロー対策はしていない</li>
 *   <li>区間の閉半開は [l, r) を採用</li>
 * </ul>
 * verified:
 * https://atcoder.jp/contests/abc360/submissions/71235684
 * https://atcoder.jp/contests/abc211/submissions/71783851
 */
public class OfflineStatic_RectAdd_Rowmax {
	ArrayList<long[]> updates = new ArrayList<>();
	ArrayList<Long> ys = new ArrayList<>();
	long[] sortqY;
	long base = 0;
	/**
	 * [x0, x1) × [y0 × y1) に add を加算
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param add
	 */
	public void rectAdd(long x0, long y0, long x1, long y1, long add) {
		if (x0 <= x1 && y0 <= y1) {
			updates.add(new long[] {x0, y0, y1, add});
			updates.add(new long[] {x1, y0, y1, -add});
			registerY(y0);
			registerY(y1);
		}
	}
	
	/**
	 * 未テスト
	 * @param add
	 */
	public void addAll(long add) {
		base += add;
	}
	
	public OfflineStatic_RectAdd_Rowmax(int rectAddQuerSize) {
		updates=new ArrayList<long[]>(2 * rectAddQuerSize);
		ys=new ArrayList<Long>(2 + 2 * rectAddQuerSize);//2は±∞の分。
	}
	
	public OfflineStatic_RectAdd_Rowmax() {
	}
	
	public void registerY(long y) {
		ys.add(y);
	}
	
	LazySegTreelonglong seg;
	boolean built = false;
	
	public void build() {
		registerY(Long.MIN_VALUE);
		registerY(Long.MAX_VALUE);
		
		Collections.sort(updates, (x, y)->Arrays.compare(x, y));
		sortqY=new long[ys.size()];
		for (int i = 0; i < ys.size(); i++) {
			sortqY[i]=ys.get(i);
		}
		sortqY=ArrayUtils.sortq(sortqY);
		seg=SegTreeFactory.add_max(Math.max(1, sortqY.length));
		seg.fill(0);
		built = true;	
		
	}
	
	
	
	long lastX = Long.MIN_VALUE;
	int pointer = 0;
	
	/**
	 * [key, minValue]を返す
	 * @param x
	 * @param y0
	 * @param y1
	 * @return
	 */
	public long[] argmax(long x, long y0, long y1) {
		if (y0 >= y1) throw new AssertionError();
		if (!built) throw new AssertionError();
		if (lastX > x) throw new AssertionError();
		lastX = x;
		while (pointer < updates.size() && updates.get(pointer)[0] <= x) {
			int l = SortedArrays.ceil(sortqY, updates.get(pointer)[1]);
			int r = SortedArrays.ceil(sortqY, updates.get(pointer)[2]);
			seg.act(l, r, updates.get(pointer)[3]);
			pointer++;
		}
		//registerされたy座標をy0,y1,..とすると、セグ木の葉は[y0,y1),[y1,y2),..を表す
		int l = SortedArrays.floor(sortqY, y0);
		int r = SortedArrays.ceil(sortqY, y1);
		long max = seg.fold(l, r);
		int key = seg.maximalRight(l, v->v<max);
		return new long[] {Math.max(y0, sortqY[key]), max + base};
	}
	
	public long max(long x, long y0, long y1) {
		if (!built) throw new AssertionError("buildされていない");
		if (lastX > x) throw new AssertionError();
		lastX = x;
		while (pointer < updates.size() && updates.get(pointer)[0] <= x) {
			int l = SortedArrays.ceil(sortqY, updates.get(pointer)[1]);
			int r = SortedArrays.ceil(sortqY, updates.get(pointer)[2]);
			seg.act(l, r, updates.get(pointer)[3]);
			pointer++;
		}
		int l = SortedArrays.floor(sortqY, y0);
		int r = SortedArrays.ceil(sortqY, y1);
		return seg.fold(l, r) + base;
	}
	
	@Override
	public String toString() {
		return seg.toString();
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
	
}