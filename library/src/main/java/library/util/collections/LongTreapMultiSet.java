package library.util.collections;

import java.util.List;

/**
 * {@code long} に特殊化した Treap ベースのマルチセット。
 * 内部で {@link TreapMap} を使用しています。
 *
 * 超未テスト未完成
 */
public class LongTreapMultiSet {
	/** 内部の要素数を管理する TreapMap */
    private final TreapMap map;
    /** 重複を含めた総要素数 */
    private long size = 0;
    
    /**
     * 空のマルチセットを構築します。
     *
     * @complexity O(1)
     */
    public LongTreapMultiSet() {
    	map = new TreapMap();
	}

    private LongTreapMultiSet(TreapMap map, long size) {
        this.map = map;
        this.size = size;
    }

    /**
     * このマルチセットのコピー（ディープコピー）を返します。
     *
     * @return このマルチセットのコピー
     * @complexity O(N) (N は異なる要素の数)
     */
    // 未テスト
    public LongTreapMultiSet copy() {
        return new LongTreapMultiSet(this.map.copy(), this.size);
    }
    
    /***
     * repeatは負（削除）でもよいが、操作後の個数が負になるならerror。
     * 要素を {@code repeat} 回追加します。
     *
     * @param element 追加する要素
     * @param repeat 追加する回数
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public void add(long element, long repeat) {
    	if (repeat == 0) return;
    	size += repeat;
    	long num = map.getOrDefault(element, 0L) + repeat;
    	if (num < 0) throw new AssertionError();
    	if (num == 0) map.remove(element);
    	else map.put(element, num);
    }
    
	/**
	 * 要素を1つ追加します。
	 *
	 * @param element 追加する要素
	 * @complexity O(\log N) (N は異なる要素の数)
	 */
	public void add(long element) {
		//https://atcoder.jp/contests/abc241/submissions/73413087
		add(element, 1);
	}

    /**
     * elementを一つ削除する。
     *
     * @param element 削除する要素
     * @return 削除に成功した（もともと要素が存在した）場合は {@code true}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public boolean remove(long element) {
    	//https://atcoder.jp/contests/abc241/submissions/73413087
    	return remove(element, 1);
    }
    
    /**
     * もともと入っていた要素数がrepeat未満の時はfalse。
     * 要素を最大 {@code repeat} 回削除します。
     *
     * @param element 削除する要素
     * @param repeat 削除する回数
     * @return もともと入っていた要素数が {@code repeat} 以上の場合は {@code true}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public boolean remove(long element, long repeat) {
    	long num = map.getOrDefault(element, 0L);
    	boolean ret = num >= repeat;
    	repeat = Math.min(repeat, num);
    	size -= repeat;
    	if (num == repeat) map.remove(element);
    	else map.put(element, num - repeat);
    	return ret;
    }
    
    /**
     * keyが要素, valueが要素の個数。
     *
     * @return エントリーのリスト
     * @complexity O(N) (N は異なる要素の数)
     */
    public List<library.util.collections.TreapMap.Entry> entryList() {
    	return map.entryList();
    }
    
    /**
     * 重複を込めたサイズを返す。
     *
     * @return 総要素数
     * @complexity O(1)
     */
    public long size() {
    	return size;
    }
    
    /**
     * 異なる要素の数を返します。
     *
     * @return 異なる要素の数
     * @complexity O(1)
     */
    public int numberDistinctElements() {
    	return map.size();
    }
    
    /**
     * 集合が空であるかどうかを返します。
     *
     * @return 空であれば {@code true}
     * @complexity O(1)
     */
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    /**
     * 最小の要素を1つ取り出して削除します。
     *
     * @return 最小の要素
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long pollFirst() {
    	var e = map.firstKey();
    	remove(e);
    	return e;
    }

    /**
     * 最大の要素を1つ取り出して削除します。
     *
     * @return 最大の要素
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long pollLast() {
    	var e = map.lastKey();
    	remove(e);
    	return e;
    }
    
    /**
     * 最小の要素を返します（削除はしません）。
     *
     * @return 最小の要素
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long peekFirst() {
    	var e = map.firstKey();
    	return e;
    }
    
    /**
     * 最大の要素を返します（削除はしません）。
     *
     * @return 最大の要素
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long peekLast() {
    	var e = map.lastKey();
    	return e;
    }
    
    /**
     * 指定された値より厳密に小さい最大の要素を返します。
     *
     * @param v 値
     * @return 存在するならその要素、存在しなければ {@code null}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public Long lower(long v) {
	return map.lowerKey(v);
    }
    
    /**
     * 指定された値より厳密に大きい最小の要素を返します。
     *
     * @param v 値
     * @return 存在するならその要素、存在しなければ {@code null}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public Long higher(long v) {
	return map.higherKey(v);
    }
    
    /**
     * 指定された値以下の最大の要素を返します。
     *
     * @param v 値
     * @return 存在するならその要素、存在しなければ {@code null}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public Long floor(long v) {
    	//https://atcoder.jp/contests/abc241/submissions/73413087
    	return map.floorKey(v);
    }
    
    /**
     * 指定された値以上の最小の要素を返します。
     *
     * @param v 値
     * @return 存在するならその要素、存在しなければ {@code null}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public Long ceil(long v) {
    	return map.ceilKey(v);
    }
    
    /**
     * 最大の要素のエントリーを返します。
     *
     * @return 最大の要素のエントリー
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public library.util.collections.TreapMap.Entry peekLastEntry() {
    	return map.lastEntry();
    }

    /**
     * 最小の要素のエントリーを返します。
     *
     * @return 最小の要素のエントリー
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public library.util.collections.TreapMap.Entry peekFirstEntry() {
    	return map.firstEntry();
    }

    /**
     * 指定された要素の個数を返します。
     *
     * @param element 要素
     * @return 個数
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long count(long element) {
    	return map.getOrDefault(element, 0L);
    }

    /**
     * 指定された要素が含まれているかどうかを返します。
     *
     * @param element 要素
     * @return 含まれていれば {@code true}
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public boolean contains(long element) {
    	return map.containsKey(element);
    }
    
    /**
     * 指定された値以下の要素の総数を返します。
     *
     * @param element 値
     * @return 総数
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long countLeq(long element) {
    	//https://atcoder.jp/contests/abc231/submissions/73433911
    	return map.rangeSum(Long.MIN_VALUE, element+1);
    }
    
    /**
     * 指定された値以上の要素の総数を返します。
     *
     * @param element 値
     * @return 総数
     * @complexity O(\log N) (N は異なる要素の数)
     */
    public long countGeq(long element) {
    	return map.rangeSum(element, Long.MAX_VALUE);
    }

	/**
	 * 集合に含まれない最小の非負整数を返します。
	 *
	 * <p>数学的定義: mex(S) = min { x ∈ ℕ₀ | x ∉ S }</p>
	 *
	 * @return 集合に含まれない最小の非負整数
	 * @complexity O(\log N) (N は異なる要素の数)
	 */
	public long mex() {
		int c = (int) map.countLeq(-1);
		long res = (long) map.size() - c;
		TreapMap.Node node = map.getRoot();
		int offset = 0;
		while (node != null) {
			int rank = offset + (node.left == null ? 0 : node.left.size);
			if (node.key < 0) {
				offset = rank + 1;
				node = node.right;
			} else {
				int nonNegRank = rank - c;
				if (node.key > (long) nonNegRank) {
					res = (long) nonNegRank;
					node = node.left;
				} else {
					offset = rank + 1;
					node = node.right;
				}
			}
		}
		return res;
	}

    /**
     * 0-origin。
     * 重複を込めて昇順に並べたときの {@code k} 番目の要素を返します。
     *
     * @param k インデックス (0-origin)
     * @return {@code k} 番目の要素。存在しなければ {@code null}
     * @complexity O(\log N) (N は異なる要素の数)
     */
	public Long kthKey(long k) {
		//https://atcoder.jp/contests/abc241/submissions/73413588
		if (k < 0 || k >= size) return null;
		TreapMap.Node node = map.getRoot();
		while(true) {
			if(node.left!=null) {
				if(k<node.left.sum) {
					node=node.left;
					continue;
				} else {
					k-=node.left.sum;
				}
			}
			if(k < node.val)return node.key;
			k -= node.val;
			node = node.right;
		}
	}
    
    /**
     * 集合の状態を文字列として表します。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし。</li>
     *   <li>計算量: $O(N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * @return 集合の状態を表す文字列
     */
    // 未テスト
    @Override
    public String toString() {
    	if (isEmpty()) {
		return "空集合";
    	} else {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
    		for (var entry : entryList()) {
			if (!first) sb.append("\n");
			sb.append(entry.key).append(" が ").append(entry.value).append("個");
			first = false;
    		}
		return sb.toString();
    	}
    }

    /**
     * 集合の状態を標準出力にダンプします。
     *
     * @complexity O(N) (N は異なる要素 of の数)
     */
    public void dump() {
	System.out.println(toString());
    }
    
    /**
     * 内部で使用している {@link TreapMap} を返します。
     *
     * @return 内部の TreapMap オブジェクト
     * @complexity O(1)
     */
    public TreapMap getTreapMap() {
    	return map;
    }

	/**
	 * このマルチセットと別のオブジェクトの同値性を判定します。
	 * 内部の各要素とその個数が一致する場合に同値とみなします。
	 *
	 * <p>計算量: $O(N)$（$N$ は異なる要素の数）</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongTreapMultiSet)) return false;
		LongTreapMultiSet other = (LongTreapMultiSet) obj;
		if (this.size != other.size) return false;
		if (this.numberDistinctElements() != other.numberDistinctElements()) return false;

		List<TreapMap.Entry> thisEntries = this.entryList();
		List<TreapMap.Entry> otherEntries = other.entryList();
		if (thisEntries.size() != otherEntries.size()) return false;
		for (int i = 0; i < thisEntries.size(); i++) {
			TreapMap.Entry e1 = thisEntries.get(i);
			TreapMap.Entry e2 = otherEntries.get(i);
			if (e1.key != e2.key || e1.value != e2.value) return false;
		}
		return true;
	}

	/**
	 * このマルチセットのハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$（$N$ は異なる要素の数）</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + Long.hashCode(size);
		for (TreapMap.Entry e : entryList()) {
			result = 31 * result + Long.hashCode(e.key);
			result = 31 * result + Long.hashCode(e.value);
		}
		return result;
	}
}
