package library.util.collections;

/**
 * char型のキュー。
 */
public class CharQueue {
	private CharDeque d = new CharDeque();

	/**
	 * 要素vをキューの末尾に追加する。
	 * @param v 追加する要素
	 * 計算量: O(1) (amortized)
	 */
	public void add(char v) {
		d.addLast(v);
	}

	/**
	 * キューの先頭の要素を取り出して削除する。
	 * @return 取り出した要素
	 * 計算量: O(1)
	 */
	public char poll() {
		return d.pollFirst();
	}

	/**
	 * キューの先頭の要素を、削除せずに取得する。
	 * @return 先頭の要素
	 * 計算量: O(1)
	 */
	public char peek() {
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
	public char get(int i) {
		return d.get(i);
	}

	/**
	 * キューのコピーを作成する。
	 * @return コピーされたキュー
	 * 計算量: O(N) (N = size())
	 */
	public CharQueue copy() {
		CharQueue ret = new CharQueue();
		ret.d = d.copy();
		return ret;
	}

	/**
	 * キューの内容を表す文字列を返す。
	 * @return キュー内容の文字列
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public String toString() {
		return d.toString();
	}

	/**
	 * キューの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
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
		if (!(obj instanceof CharQueue)) return false;
		CharQueue other = (CharQueue) obj;
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
