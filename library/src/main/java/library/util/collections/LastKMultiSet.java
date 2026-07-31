package library.util.collections;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class LastKMultiSet<T extends Comparable<T>>{
    TreeMultiSet<T> hi;
    TreeMultiSet<T> lo;
    BinaryOperator<T> add;
    UnaryOperator<T> inv;
    Comparator<T> comp;
    T ret;
    int k;
    
    /**
     * Tをキーに持つ多重集合の上位k個について、T（群）を集約した値を返す。
     * heapだと可逆でないものも処理できるが未実装。
     * identityは空集合の値に必要。
     * @param k
     * verified:
     * https://atcoder.jp/contests/abc306/tasks/abc306_e
     * https://atcoder.jp/contests/arc210/submissions/71018238
     */
    public LastKMultiSet(int k, BinaryOperator<T> add, UnaryOperator<T> inv, T identity, Comparator<T> comp) {
        hi=new TreeMultiSet<>(comp);
        lo=new TreeMultiSet<>(comp);
        this.add=add;
        this.inv=inv;
        this.k=k;
        ret=identity;
        this.comp=comp;
    }
    
    public LastKMultiSet(int k, BinaryOperator<T> add, UnaryOperator<T> inv, T identity) {
    	this(k, add, inv, identity, Comparator.naturalOrder());
    }
    
    public T add(T element) {
    	if (k > 0 && (hi.size() < k || comp.compare(element, hi.peekFirst()) > 0)) {
	    	hi.add(element);
	    	ret = add.apply(ret, element);
    	} else {
    		lo.add(element);
    	}
    	balance();
    	return ret;
    }
    
    public T remove(T element) {
    	if (!lo.remove(element)) {
    		hi.remove(element);
    		ret = add.apply(ret, inv.apply(element));
    	}
    	balance();
    	return ret;
    }
    
    public void balance() {
    	if(hi.size() == k + 1) {
    		T hv = hi.pollFirst();
    		ret = add.apply(ret, inv.apply(hv));
    		lo.add(hv);
    	} else if(hi.size() == k - 1 && !lo.isEmpty()) {
			T lv= lo.pollLast();
			ret = add.apply(ret, lv);
			hi.add(lv);
		}
    }
    
    /**
     * @param newK
     */
    public void changeK(int newK) {
    	//https://atcoder.jp/contests/abc440/submissions/73551012
    	while(k != newK) {
    		if (k < newK) k++;
    		else k--;
    		balance();
    	}
    }
    
    /**
     * 上位k個の集約値を返す。
     * @return
     */
    public T get() {
    	return ret;
    }

    /**
     * このマルチセットと別のオブジェクトの同値性を判定します。
     * 内部の状態（k, hi, lo, ret, add, inv, comp）が一致する場合に同値とみなします。
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
        if (!(obj instanceof LastKMultiSet)) return false;
        LastKMultiSet<?> other = (LastKMultiSet<?>) obj;
        if (this.k != other.k) return false;
        if (this.hi == null ? other.hi != null : !this.hi.equals(other.hi)) return false;
        if (this.lo == null ? other.lo != null : !this.lo.equals(other.lo)) return false;
        if (this.ret == null ? other.ret != null : !this.ret.equals(other.ret)) return false;
        if (this.add == null ? other.add != null : !this.add.equals(other.add)) return false;
        if (this.inv == null ? other.inv != null : !this.inv.equals(other.inv)) return false;
        if (this.comp == null ? other.comp != null : !this.comp.equals(other.comp)) return false;
        return true;
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
        int result = 1;
        result = 31 * result + k;
        result = 31 * result + (hi != null ? hi.hashCode() : 0);
        result = 31 * result + (lo != null ? lo.hashCode() : 0);
        result = 31 * result + (ret != null ? ret.hashCode() : 0);
        result = 31 * result + (add != null ? add.hashCode() : 0);
        result = 31 * result + (inv != null ? inv.hashCode() : 0);
        result = 31 * result + (comp != null ? comp.hashCode() : 0);
        return result;
    }

    /**
     * このマルチセットを表す文字列を返します。
     *
     * <p>計算量: $O(N)$（$N$ は格納されている総要素数）</p>
     *
     * @return このマルチセットの文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        return "LastKMultiSet{" +
                "k=" + k +
                ", hi=" + hi +
                ", lo=" + lo +
                ", ret=" + ret +
                '}';
    }

	/**
	 * デバッグ用にマルチセットの状態を標準出力に出力します。
	 *
	 * 未テスト
	 * @complexity O(N) (N は格納されている総要素数)
	 */
	public void dump() {
		System.out.println(toString());
	}
}
