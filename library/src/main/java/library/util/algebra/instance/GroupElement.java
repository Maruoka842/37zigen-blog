package library.util.algebra.instance;

import library.util.algebra.strategy.GroupStrategy;

public interface GroupElement<X extends GroupElement<X>> extends MonoidElement<X> {
	/**
	 * この元の親群 G の演算ストラテジを返す。
	 * 未テスト。
	 * 事前条件: this ∈ G。
	 * 事後条件: 戻り値 st は st.identity() = e_G, st.mul(a,b) = a * b, st.inverse(a) = a^{-1} を満たす。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 戻り値のストラテジ参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: this が親群を持たないとき未定義。
	 * @return 親群 G のストラテジ
	 */
	@Override
	GroupStrategy<X> parent();

	/**
	 * this^{-1} を返す。
	 * 未テスト。
	 * 事前条件: this ∈ G。
	 * 事後条件: 戻り値 = parent().inverse(this) = this^{-1}。
	 * 副作用: parent().inverse(self()) に同じ。
	 * 計算量: parent().inverse(self()) に同じ。
	 * 破壊的変更: parent().inverse(self()) に同じ。
	 * 参照共有・所有権: parent().inverse(self()) に同じで、所有権は移動しない。
	 * 例外・未定義条件: this が親群 G の元でないとき未定義。
	 * @return this^{-1}
	 */
	default X inverse() {
		return parent().inverse(self());
	}
}
