package library.util.algebra.strategy;

import java.util.Map;

import library.util.algebra.instance.VectorSpaceElement;

/**
 * 体 K 上の基底 B を持つベクトル空間 K^(B) の演算を定義するストラテジ。
 * K^(B) は係数環を体 K に制限した自由加群であり、元 x は有限台写像 x: B -> K とする。
 * x(b) = 0_K である b は Map から除外する。
 *
 * @param <K> 係数体の型
 * @param <B> 基底の型
 */
public class VectorSpaceStrategy<K, B> extends FreeModuleStrategy<K, B, VectorSpaceElement<K, B>>
		implements AbelianGroupStrategy<VectorSpaceElement<K, B>> {
	/** 係数体 K の演算ストラテジ。 */
	protected final FieldStrategy<K> field;

	@Override
	protected VectorSpaceElement<K, B> create(Map<B, K> normalizedVal) {
		return new VectorSpaceElement<>(normalizedVal, this);
	}

	/**
	 * ベクトル空間 K^(B) のストラテジを構築する。
	 * 未テスト。
	 * 事前条件: field != null。
	 * 事後条件: this.field == field かつ this.ring == field。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: field の参照を保持し、所有権は移動しない。
	 * 例外・未定義条件: field == null のとき以後のメソッド呼び出しは未定義。
	 * @param field 係数体 K の演算ストラテジ
	 */
	public VectorSpaceStrategy(FieldStrategy<K> field) {
		super(field);
		this.field = field;
	}

	/**
	 * 係数体 K の演算ストラテジを返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 == this.field。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: field の参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: なし。
	 * @return 係数体 K の演算ストラテジ
	 */
	public FieldStrategy<K> field() {
		return field;
	}

	/**
	 * c^{-1} x を返す。
	 * 未テスト。
	 * 事前条件: divisor != null, a != null, divisor != 0_K, a は正規形。
	 * 事後条件: 各 p ∈ B について 戻り値(p) = divisor^{-1} * a(p)、かつ 0_K 係数は supp(戻り値) に含まれない。
	 * 副作用: なし。
	 * 計算量: O(cost(K.inv) + |supp(a)| * (cost(K.mul) + cost(K.equals)))。
	 * 破壊的変更: a を変更しない。
	 * 参照共有・所有権: 基底の参照を再利用し得る。戻り値 Map は新規。
	 * 例外・未定義条件: divisor == null、a == null、divisor = 0_K、または a が正規形でないとき未定義。
	 * @param divisor 除数 c
	 * @param a 対象 x
	 * @return c^{-1} x
	 */
	public VectorSpaceElement<K, B> scalarDiv(K divisor, VectorSpaceElement<K, B> a) {
		return create(scalarDiv(divisor, a.val()));
	}

	public Map<B, K> scalarDiv(K divisor, Map<B, K> a) {
		return scalarMul(field.inv(divisor), a);
	}

	/**
	 * x の pivot 基底 p に関する係数を 1_K に正規化した p^{-1}x を返す。
	 * ここで p^{-1} は a(p)^{-1} を意味し、基底の逆元ではない。
	 * 未テスト。
	 * 事前条件: pivot != null, a != null, a は正規形, a(pivot) != null, a(pivot) != 0_K。
	 * 事後条件: 戻り値(pivot) = 1_K かつ 各 q ∈ B について 戻り値(q) = a(pivot)^{-1} * a(q)。
	 * 副作用: なし。
	 * 計算量: O(cost(B.hashCode/equals) + cost(K.inv) + |supp(a)| * (cost(K.mul) + cost(K.equals))) expected。
	 * 破壊的変更: a を変更しない。
	 * 参照共有・所有権: 基底の参照を再利用し得る。戻り値 Map は新規。
	 * 例外・未定義条件: pivot == null、a == null、a が正規形でない、a(pivot) が存在しない、または a(pivot) = 0_K のとき未定義。
	 * @param pivot 正規化する基底 p
	 * @param a 対象 x
	 * @return a(pivot)^{-1} x
	 */
	public VectorSpaceElement<K, B> normalizeBy(B pivot, VectorSpaceElement<K, B> a) {
		return create(normalizeBy(pivot, a.val()));
	}

	public Map<B, K> normalizeBy(B pivot, Map<B, K> a) {
		return scalarDiv(a.get(pivot), a);
	}

	@Override
	public VectorSpaceElement<K, B> identity() {
		return zero();
	}

	@Override
	public VectorSpaceElement<K, B> mul(VectorSpaceElement<K, B> a, VectorSpaceElement<K, B> b) {
		return add(a, b);
	}

	@Override
	public VectorSpaceElement<K, B> inverse(VectorSpaceElement<K, B> a) {
		return neg(a);
	}

}
