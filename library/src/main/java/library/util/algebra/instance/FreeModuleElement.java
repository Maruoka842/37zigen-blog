package library.util.algebra.instance;

import java.util.Map;

import library.util.algebra.strategy.FreeModuleStrategy;

/**
 * 環 R 上の基底 B を持つ自由加群 R^(B) の元の契約。 元 x は有限台写像 x: B -> R とし、x(b) = 0_R である b は
 * Map から除外する。
 *
 * @param <R> 係数環の型
 * @param <B> 基底の型
 */
public abstract class FreeModuleElement<R, B, E extends FreeModuleElement<R, B, E>> {

	protected Map<B, R> val;
	protected final FreeModuleStrategy<R, B, E> parent;

	protected FreeModuleElement(Map<B, R> val, FreeModuleStrategy<R, B, E> parent) {
		if (val == null)
			throw new NullPointerException("val");
		if (parent == null)
			throw new NullPointerException("parent");

		this.val = val;
		this.parent = parent;
	}

	public Map<B, R> val() {
		return val;
	}

	public FreeModuleStrategy<R, B, E> freeModuleParent() {
		return parent;
	}

	/**
	 * 指定された基底の係数を返す。
	 * 未テスト。
	 * 数学的表記: [basis]this。
	 * 事前条件: basis != null。
	 * 事後条件: basis が台にないなら 0_R、あるなら対応する係数を返す。
	 * 副作用: なし。
	 * 計算量: O(log |val|) (Map が TreeMap の場合) または O(1) (Map が HashMap の場合)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 係数が存在する場合は内部係数参照を共有する。
	 * 例外・未定義条件: basis == null のとき NullPointerException。
	 * @param basis 基底。
	 * @return basis の係数。
	 */
	public R coefficientOf(B basis) {
		return parent.coefficientOf(val, basis);
	}
}