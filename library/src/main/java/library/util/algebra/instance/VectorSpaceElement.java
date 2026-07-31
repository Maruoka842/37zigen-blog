package library.util.algebra.instance;

import java.util.Map;

import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.VectorSpaceStrategy;

/**
 * 体 K 上の基底 B を持つベクトル空間 K^(B) の可変な元。
 * K^(B) は係数環を体 K に制限した自由加群であり、元 x は有限台写像 x: B -> K とする。
 * x(b) = 0_K である b は Map から除外する。
 *
 * @param <K> 係数体の型
 * @param <B> 基底の型
 */
public class VectorSpaceElement<K, B> extends FreeModuleElement<K, B, VectorSpaceElement<K, B>> {
	/** ベクトル空間 K^(B) の演算ストラテジ。 */
	protected final VectorSpaceStrategy<K, B> strategy;

	/**
	 * 値とストラテジを直接指定して構築する。
	 * 未テスト。
	 * 事前条件: val != null, strategy != null, val は正規形。
	 * 事後条件: this.val() == val かつ this.strategy() == strategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: val と strategy の参照を保持し、所有権は移動しない。
	 * 例外・未定義条件: val == null または strategy == null または val が正規形でないとき以後のメソッド呼び出しは未定義。
	 * @param val 保持する有限台写像
	 * @param strategy ストラテジ
	 */
	public VectorSpaceElement(Map<B, K> val, VectorSpaceStrategy<K, B> strategy) {
		super(val, strategy);
		this.strategy = strategy;
	}

	/**
	 * 値と係数体ストラテジを指定して構築する。
	 * 未テスト。
	 * 事前条件: val != null, field != null, val は正規形。
	 * 事後条件: this.val() == val かつ this.strategy() は field 上の新しい VectorSpaceStrategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: val と field の参照を保持し、所有権は移動しない。
	 * 例外・未定義条件: val == null または field == null または val が正規形でないとき以後のメソッド呼び出しは未定義。
	 * @param val 保持する有限台写像
	 * @param field 係数体 K の演算ストラテジ
	 */
	public VectorSpaceElement(Map<B, K> val, FieldStrategy<K> field) {
		this(val, new VectorSpaceStrategy<>(field));
	}


	/**
	 * ベクトル空間 K^(B) の演算ストラテジを返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 == this.strategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: this.strategy の参照を返し、所有権は移動しない。
	 * 例外・未定義条件: なし。
	 * @return 演算ストラテジ
	 */
	public VectorSpaceStrategy<K, B> strategy() {
		return strategy;
	}

	/**
	 * c x を返す。
	 * 未テスト。
	 * 事前条件: 引数がある場合は null でなく、入力は正規形。
	 * 事後条件: FreeModuleElement の同名メソッドの契約に従う。
	 * 副作用: Inplace/Into メソッドのみ書き込み先を更新し、それ以外はなし。
	 * 計算量: strategy の対応する演算に同じ。
	 * 破壊的変更: Inplace/Into メソッドのみ書き込み先を破壊的に変更する。
	 * 参照共有・所有権: strategy 参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: FreeModuleElement の同名メソッドの契約に従う。
	 */
	public VectorSpaceElement<K, B> scalarMul(K coefficient) {
		return strategy.scalarMul(coefficient, this);
	}

	/**
	 * this <- c this を実行する。
	 * 未テスト。
	 * 事前条件: 引数がある場合は null でなく、入力は正規形。
	 * 事後条件: FreeModuleElement の同名メソッドの契約に従う。
	 * 副作用: Inplace/Into メソッドのみ書き込み先を更新し、それ以外はなし。
	 * 計算量: strategy の対応する演算に同じ。
	 * 破壊的変更: Inplace/Into メソッドのみ書き込み先を破壊的に変更する。
	 * 参照共有・所有権: strategy 参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: FreeModuleElement の同名メソッドの契約に従う。
	 */
	public void scalarMulInplace(K coefficient) {
		val = strategy.scalarMul(coefficient, val);
	}

	/**
	 * c^{-1} x を返す。
	 * 未テスト。
	 * 事前条件: divisor != null, divisor != 0_K, this.val() は正規形。
	 * 事後条件: 戻り値.val() = divisor^{-1} * this.val()。
	 * 副作用: なし。
	 * 計算量: strategy.scalarDiv(divisor, this.val()) に同じ。
	 * 破壊的変更: this を変更しない。
	 * 参照共有・所有権: 戻り値は this.strategy() を共有する。
	 * 例外・未定義条件: divisor == null、divisor = 0_K、または this.val() が正規形でないとき未定義。
	 * @param divisor 除数 c
	 * @return c^{-1} x
	 */
	public VectorSpaceElement<K, B> scalarDiv(K divisor) {
		return new VectorSpaceElement<>(strategy.scalarDiv(divisor, val), strategy);
	}

	/**
	 * this <- c^{-1} this を実行する。
	 * 未テスト。
	 * 事前条件: divisor != null, divisor != 0_K, this.val() は正規形。
	 * 事後条件: this.val() = divisor^{-1} * old(this.val())。
	 * 副作用: this.val() を更新する。
	 * 計算量: strategy.scalarDiv(divisor, old(this.val())) に同じ。
	 * 破壊的変更: this.val() を破壊的に置換する。
	 * 参照共有・所有権: 新しい Map を this.val() として保持する。
	 * 例外・未定義条件: divisor == null、divisor = 0_K、または this.val() が正規形でないとき未定義。
	 * @param divisor 除数 c
	 */
	public void scalarDivInplace(K divisor) {
		val = strategy.scalarDiv(divisor, val);
	}

	/**
	 * pivot 基底の係数を 1_K に正規化したベクトルを返す。
	 * 未テスト。
	 * 事前条件: pivot != null, this.val() は正規形, this.val()(pivot) != null, this.val()(pivot) != 0_K。
	 * 事後条件: 戻り値.val()(pivot) = 1_K かつ 各 q ∈ B について 戻り値.val()(q) = this.val()(pivot)^{-1} * this.val()(q)。
	 * 副作用: なし。
	 * 計算量: strategy.normalizeBy(pivot, this.val()) に同じ。
	 * 破壊的変更: this を変更しない。
	 * 参照共有・所有権: 戻り値は this.strategy() を共有する。
	 * 例外・未定義条件: pivot == null、this.val() が正規形でない、this.val()(pivot) が存在しない、または this.val()(pivot) = 0_K のとき未定義。
	 * @param pivot 正規化する基底
	 * @return this.val()(pivot)^{-1} this
	 */
	public VectorSpaceElement<K, B> normalizeBy(B pivot) {
		return new VectorSpaceElement<>(strategy.normalizeBy(pivot, val), strategy);
	}

	/**
	 * 等価性を判定する。
	 * 未テスト。
	 * 事前条件: this.val は正規形。
	 * 事後条件: Object の同名メソッドの契約に従う。
	 * 副作用: なし。
	 * 計算量: val の同名メソッドまたは strategy.equals に同じ。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: 比較対象の値が正規形でないとき未定義。
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof VectorSpaceElement<?, ?>)) return false;
		@SuppressWarnings("unchecked")
		VectorSpaceElement<K, B> that = (VectorSpaceElement<K, B>) o;
		return strategy.equals(this, that);
	}

	/**
	 * ハッシュ値を返す。
	 * 未テスト。
	 * 事前条件: this.val は正規形。
	 * 事後条件: Object の同名メソッドの契約に従う。
	 * 副作用: なし。
	 * 計算量: val の同名メソッドまたは strategy.equals に同じ。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: 比較対象の値が正規形でないとき未定義。
	 */
	@Override
	public int hashCode() {
		return val.hashCode();
	}

	/**
	 * 文字列表現を返す。
	 * 未テスト。
	 * 事前条件: this.val は正規形。
	 * 事後条件: Object の同名メソッドの契約に従う。
	 * 副作用: なし。
	 * 計算量: val の同名メソッドまたは strategy.equals に同じ。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: 比較対象の値が正規形でないとき未定義。
	 */
	@Override
	public String toString() {
		return val.toString();
	}
}