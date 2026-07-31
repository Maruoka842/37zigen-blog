package library.util.collections;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class FirstKMultiSet<T extends Comparable<T>> extends LastKMultiSet<T>{
    public FirstKMultiSet(int k, BinaryOperator<T> add, UnaryOperator<T> inv, T identity) {
        super(k, add, inv, identity, Comparator.reverseOrder());
    }

	/**
	 * このマルチセットと別のオブジェクトの同値性を判定します。
	 * スーパークラスの `equals` に加えて、型が `FirstKMultiSet` であることを確認します。
	 *
	 * <p>計算量: $O(N)$（$N$ は格納されている総要素数）</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FirstKMultiSet)) return false;
		return super.equals(obj);
	}

	/**
	 * このマルチセットのハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$（$N$ は格納されている総要素数）</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return super.hashCode() ^ 12345;
	}

	/**
	 * マルチセットの状態を文字列として表します。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return マルチセットの状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		return "--- FirstKMultiSet hi ---\n" + hi.toString() + "\n--- FirstKMultiSet lo ---\n" + lo.toString();
	}

	/**
	 * デバッグ用にマルチセットの状態を標準出力に出力します。
	 *
	 * 未テスト
	 * @complexity O(N) (N は格納されている総要素数)
	 */
	@Override
	public void dump() {
		System.out.println(toString());
	}
}
