package library.util.algebra.instance;

/**
 * 完全除算を持つ整域。
 * @param <X> X は同一の {@code ExactDivRingElement<X>} かつ {@code IntegralDomainElement<X>} の元型。
 */
public interface ExactDivRingElement<X extends ExactDivRingElement<X>> extends IntegralDomainElement<X> {
	/**
	 * x / a が同じ整域 D に存在する場合にその商を返す。
	 * 未テスト。
	 * 事前条件: this, a ∈ D, a != null, a != 0_D, ∃q ∈ D: this = a * q。
	 * 事後条件: 戻り値 q は this = a * q を満たす。
	 * 副作用: なし。
	 * 計算量: 実装の完全除算に依存する。
	 * 破壊的変更: this と a を変更しない。
	 * 参照共有・所有権: 実装に依存し、所有権は移動しない。
	 * 例外・未定義条件: a == null、a = 0_D、または a が this を割り切らないとき未定義。
	 * @param a 除数
	 * @return q such that this = a * q
	 */
	X exactDiv(X a);
}
