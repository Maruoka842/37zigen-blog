package library.util;

/**
 * 区間 [l, r) に値 x が割り当てられたときの情報を集計するインターフェース。
 * 集計対象はアーベル群であることを想定しており、要素の追加 (add) と削除 (remove) が可能である必要がある。
 *
 * @param <PositionType> 座標の型
 * @param <X> 各区間に割り当てる値の型
 * @param <G> 集計結果の型
 */
public interface RangeAbelianGroupAggregator<PositionType, X> {
	/**
	 * 区間 [l, r) に値 x を追加する。
	 *
	 * @param l 左端
	 * @param r 右端
	 * @param x 値
	 */
	void add(PositionType l, PositionType r, X x);

	/**
	 * 区間 [l, r) に割り当てられていた値 x を削除する。
	 *
	 * @param l 左端
	 * @param r 右端
	 * @param x 値
	 */
	void remove(PositionType l, PositionType r, X x);

}
