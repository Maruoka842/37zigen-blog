package library.util.graph;

import library.util.collections.IntArrayList;

/**
 * マトロイドを表すインターフェース。
 */
public interface Matroid {
	/**
	 * 台集合（ground set）のサイズを返す。
	 * @return 台集合のサイズ
	 */
	int size();

	/**
	 * 現在の独立集合を設定する。
	 * @param I 独立集合を表すビット配列
	 */
	void set(boolean[] I);

	/**
	 * I ∪ {e} に含まれる唯一のサーキット（基本閉路）を返す。
	 * I ∪ {e} が独立な場合は空のリストを返す。
	 * @param e 追加する要素
	 * @return サーキットに含まれる要素のリスト
	 */
	IntArrayList circuit(int e);
}
