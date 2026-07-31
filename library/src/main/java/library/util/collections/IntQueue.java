package library.util.collections;

public class IntQueue {
    private IntDeque d = new IntDeque();
    
    public void add(int v) {
    	//https://atcoder.jp/contests/abc391/submissions/74625990
    	d.addLast(v);
    }

    public int poll() {
    	//https://atcoder.jp/contests/abc391/submissions/74625990
    	return d.pollFirst();
    }
    
    public int peek() {
    	return d.peekFirst();
    }

    public boolean isEmpty() {
        return d.isEmpty();
    }

    public int size() {
        return d.size();
    }
    
    public int get(int i) {
    	return d.get(i);
    }
    
    public IntQueue copy() {
        IntQueue ret = new IntQueue();
        ret.d=d.copy();
        return ret;
    }

	/**
	 * キューの内容を表す文字列を返す。
	 * @return キュー内容 of the string
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
		if (!(obj instanceof IntQueue)) return false;
		IntQueue other = (IntQueue) obj;
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
