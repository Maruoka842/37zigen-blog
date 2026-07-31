package library.util.algebra.instance;

import library.util.algebra.strategy.monoid.MonoidStrategy;

public interface MonoidElement<X> {
	/**
	 * この元の親モノイド M の演算ストラテジを返す。
	 * 未テスト。
	 * 事前条件: this ∈ M。
	 * 事後条件: 戻り値 st は st.identity() = 1_M, st.mul(a,b) = a * b を満たす。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 戻り値のストラテジ参照を呼び出し側と共有し、所有権は移動しない。
	 * 例外・未定義条件: this が親モノイドを持たないとき未定義。
	 * @return 親モノイド M のストラテジ
	 */
	MonoidStrategy<X> parent();

	/**
	 * この元自身を X として返す。
	 * 未テスト。
	 * 事前条件: this ∈ X。
	 * 事後条件: 戻り値 == this。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: this 参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: this が X の元でない実装は未定義。
	 * @return this
	 */
	X self();

	/**
	 * this * a を返す。
	 * 未テスト。
	 * 事前条件: this, a ∈ M, a != null。
	 * 事後条件: 戻り値 = parent().mul(this, a) = this * a。
	 * 副作用: parent().mul(self(), a) に同じ。
	 * 計算量: parent().mul(self(), a) に同じ。
	 * 破壊的変更: parent().mul(self(), a) に同じ。
	 * 参照共有・所有権: parent().mul(self(), a) に同じで、所有権は移動しない。
	 * 例外・未定義条件: a == null、または this と a が同一モノイド M の元でないとき未定義。
	 * @param a 右因子
	 * @return this * a
	 */
	default X mul(X a) {
		return parent().mul(self(), a);
	}

	/**
	 * 親モノイド M の単位元 1_M を返す。
	 * 未テスト。
	 * 事前条件: this ∈ M。
	 * 事後条件: 戻り値 = parent().identity() = 1_M。
	 * 副作用: parent().identity() に同じ。
	 * 計算量: parent().identity() に同じ。
	 * 破壊的変更: parent().identity() に同じ。
	 * 参照共有・所有権: parent().identity() に同じで、所有権は移動しない。
	 * 例外・未定義条件: this が親モノイド M の元でないとき未定義。
	 * @return 1_M
	 */
	default X one() {
		return parent().identity();
	}
}
