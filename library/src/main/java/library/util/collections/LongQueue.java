package library.util.collections;

/**
 * long型のキュー。
 */
public class LongQueue {
	private LongDeque d = new LongDeque();

	/**
	 * 要素vをキューの末尾に追加する。
	 * @param v 追加する要素
	 * 計算量: O(1) (amortized)
	 */
	public void add(long v) {
		d.addLast(v);
	}

	/**
	 * キューの先頭の要素を取り出して削除する。
	 * @return 取り出した要素
	 * 計算量: O(1)
	 */
	public long poll() {
		return d.pollFirst();
	}

	/**
	 * キューの先頭の要素を、削除せずに取得する。
	 * @return 先頭の要素
	 * 計算量: O(1)
	 */
	public long peek() {
		return d.peekFirst();
	}

	/**
	 * キューが空であるかを判定する。
	 * @return 空であればtrue、そうでなければfalse
	 * 計算量: O(1)
	 */
	public boolean isEmpty() {
		return d.isEmpty();
	}

	/**
	 * キューの要素数を取得する。
	 * @return 要素数
	 * 計算量: O(1)
	 */
	public int size() {
		return d.size();
	}

	/**
	 * キューのi番目の要素を取得する。
	 * @param i インデックス
	 * @return i番目の要素
	 * 計算量: O(1)
	 */
	public long get(int i) {
		return d.get(i);
	}

	/**
	 * キューのコピーを作成する。
	 * @return コピーされたキュー
	 * 計算量: O(N) (N = size())
	 */
	public LongQueue copy() {
		LongQueue ret = new LongQueue();
		ret.d = d.copy();
		return ret;
	}

	/**
	 * 内部状態を文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし.</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "LongQueue { elements: " + java.util.Arrays.toString(d.toArray()) + " }";
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}

	/**
	 * このキューと別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N)$（$N$ はキューの要素数）</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongQueue)) return false;
		LongQueue other = (LongQueue) obj;
		return java.util.Objects.equals(this.d, other.d);
	}

	/**
	 * このキューのハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$（$N$ はキューの要素数）</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return d.hashCode();
	}
}
