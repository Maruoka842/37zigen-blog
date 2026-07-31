package library.util.polynomial;

import java.util.Arrays;

import library.util.algebra.instance.MonoidElement;
import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * 多変数単項式。
 * 変数とその指数の積を表す。
 * 次数付き逆辞書式順序 (grevlex) による比較を実装する。
 */
public class Monomial implements Comparable<Monomial>, MonoidElement<Monomial>{
    /** 各変数の指数。 */
    private final int[] exponents;
    /** 全指数（全次数）の和。 */
    private final int totalDegree;
    /** ハッシュコード。 */
    private final int hash;
    /** 単項式モノイド (N^n, +, 0) の親ストラテジ。 */
    public static final MonoidStrategy<Monomial> STRATEGY = new MonoidStrategy<>() {
        /**
         * 単項式モノイドの単位元 x^0 を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値 = x^0, すなわちすべての i について exponent_i = 0。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: 戻り値は新規所有され、所有権は移動しない。
         * 例外・未定義条件: なし。
         * @return x^0
         */
        @Override
        public Monomial identity() {
            return new Monomial(new int[0]);
        }

        /**
         * x^A * x^B = x^{A+B} を返す。
         * 未テスト。
         * 事前条件: a != null, b != null, a と b の全指数 >= 0。
         * 事後条件: すべての i について 戻り値.exponent_i = a.exponent_i + b.exponent_i。
         * 副作用: なし。
         * 計算量: O(max(a.size(), b.size()))。
         * 破壊的変更: a と b を変更しない。
         * 参照共有・所有権: 戻り値は新規所有され、所有権は移動しない。
         * 例外・未定義条件: a == null、b == null、またはいずれかに負指数が含まれる場合は未定義。
         * @param a 左因子 x^A
         * @param b 右因子 x^B
         * @return x^{A+B}
         */
        @Override
        public Monomial mul(Monomial a, Monomial b) {
            int n = Math.max(a.exponents.length, b.exponents.length);
            int[] res = new int[n];
            for (int i = 0; i < n; i++) {
                res[i] = a.getExponent(i) + b.getExponent(i);
            }
            return new Monomial(res);
        }
    };
    
    public int[] exponents() {
    	return exponents;
    }
    
    /**
     * Monomial のコンストラクタ。
     * 数学的表記: x^E (E は exponents 配列)。
     * 事前条件: exponents != null, すべての i について exponents[i] >= 0。
     * 事後条件: 不変な Monomial インスタンスを作成する。
     * 計算量: O(L) (L は変数の数)。
     * 参照共有: exponents は内部でクローンされる。
     */
    public Monomial(int[] exponents) {
        this.exponents = exponents.clone();
        int deg = 0;
        int h = 0;
        for (int i = 0; i < this.exponents.length; i++) {
            deg += this.exponents[i];
            if (this.exponents[i] != 0) {
                h = 31 * h + i;
                h = 31 * h + this.exponents[i];
            }
        }
        this.totalDegree = deg;
        this.hash = h;
    }

    /**
     * 変数のインデックスを指定された量だけシフトした新しい単項式を返す。
     * @param delta シフト量。
     * @return シフトされた単項式。
     */
    public Monomial shiftVariables(int delta) {
        if (delta == 0) return this;
        int n = exponents.length + delta;
        if (n < 0) n = 0;
        int[] res = new int[n];
        for (int i = 0; i < exponents.length; i++) {
            if (i + delta >= 0 && i + delta < n) {
                res[i + delta] = exponents[i];
            }
        }
        return new Monomial(res);
    }

    /**
     * 変数のインデックスを置換した新しい単項式を返す。
     * 未テスト。
     * @param p 置換マップ (i -> p[i])。
     * @return 置換された単項式。
     */
    public Monomial permuteVariables(int[] p) {
        int max = -1;
        for (int x : p) max = Math.max(max, x);
        for (int i = 0; i < exponents.length; i++) {
            if (exponents[i] != 0) max = Math.max(max, i);
        }
        int[] newExps = new int[Math.max(0, max + 1)];
        for (int i = 0; i < exponents.length; i++) {
            if (exponents[i] == 0) continue;
            if (i < p.length) {
                if (p[i] >= 0) newExps[p[i]] = exponents[i];
            } else if (i < newExps.length) {
                newExps[i] = exponents[i];
            }
        }
        return new Monomial(newExps);
    }

    /**
     * 単項式の全次数を返す。
     * 数学的表記: |E| = sum e_i。
     * 計算量: O(1)。
     */
    public int getDegree() {
        return totalDegree;
    }

    /**
     * i 番目の変数の指数を返す。
     * 計算量: O(1)。
     */
    public int getExponent(int i) {
        return i < exponents.length ? exponents[i] : 0;
    }

    /**
     * 指数が指定された変数の数を返す。
     * 計算量: O(1)。
     */
    public int size() {
        return exponents.length;
    }

    /**
     * この単項式が別の単項式で割り切れるかどうかを判定する。
     * 数学的表記: すべての i について E_i >= F_i のとき、x^E は x^F で割り切れる。
     * 計算量: O(L)。
     */
    public boolean isDivisibleBy(Monomial other) {
        if (this.totalDegree < other.totalDegree) return false;
        int n = other.exponents.length;
        if (this.exponents.length < n) {
            for (int i = 0; i < this.exponents.length; i++) {
                if (this.exponents[i] < other.exponents[i]) return false;
            }
            for (int i = this.exponents.length; i < n; i++) {
                if (other.exponents[i] > 0) return false;
            }
        } else {
            for (int i = 0; i < n; i++) {
                if (this.exponents[i] < other.exponents[i]) return false;
            }
        }
        return true;
    }

    /**
     * この単項式を別の単項式で除算する。
     * 数学的表記: x^E / x^F = x^{E-F}。
     * 事前条件: isDivisibleBy(other) が true であること。
     * 計算量: O(L)。
     * 例外: 割り切れない場合、ArithmeticException を投げる。
     */
    public Monomial divide(Monomial other) {
        int n = Math.max(this.exponents.length, other.exponents.length);
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = this.getExponent(i) - other.getExponent(i);
            if (res[i] < 0) throw new ArithmeticException("Monomial division not possible");
        }
        return new Monomial(res);
    }

    /**
     * 2 つの単項式の最小公倍数を計算する。
     * 数学的表記: lcm(x^E, x^F) = x^L (L_i = max(e_i, f_i))。
     * 計算量: O(L)。
     */
    public static Monomial lcm(Monomial a, Monomial b) {
        int n = Math.max(a.exponents.length, b.exponents.length);
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = Math.max(a.getExponent(i), b.getExponent(i));
        }
        return new Monomial(res);
    }

    /**
     * 2 つの単項式が互いに素であるかどうかを判定する。
     * 数学的表記: gcd(x^E, x^F) = 1 iff すべての i について min(e_i, f_i) = 0。
     * 計算量: O(L)。
     */
    public static boolean areRelativelyPrime(Monomial a, Monomial b) {
        int n = Math.max(a.exponents.length, b.exponents.length);
        for (int i = 0; i < n; i++) {
            if (a.getExponent(i) > 0 && b.getExponent(i) > 0) return false;
        }
        return true;
    }

    /**
     * この単項式を grevlex 順序で別の単項式と比較する。
     * 事前条件: 変数はインデックスによって順序付けられている。
     * 計算量: O(L)。
     */
    @Override
    public int compareTo(Monomial other) {
        if (this.totalDegree != other.totalDegree) {
            return Integer.compare(this.totalDegree, other.totalDegree);
        }
        // grevlex: graded reverse lexicographic
        int n1 = this.exponents.length;
        int n2 = other.exponents.length;
        int n = Math.max(n1, n2);
        for (int i = n - 1; i >= 0; i--) {
            int e1 = i < n1 ? this.exponents[i] : 0;
            int e2 = i < n2 ? other.exponents[i] : 0;
            if (e1 != e2) {
                return e1 > e2 ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monomial monomial = (Monomial) o;
        return compareTo(monomial) == 0;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(exponents);
    }

	/**
	 * この単項式の親モノイド (N^n, +, 0) を返す。
	 * 未テスト。
	 * 事前条件: this.exponents の全要素 >= 0。
	 * 事後条件: 戻り値 st は st.identity() = x^0, st.mul(x^A,x^B) = x^{A+B} を満たす。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: static ストラテジ参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: this.exponents に負値が含まれる場合は未定義。
	 * @return 単項式モノイドのストラテジ
	 */
	@Override
	public MonoidStrategy<Monomial> parent() {
		return STRATEGY;
	}

	/**
	 * この単項式自身を返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 == this。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: this 参照を共有し、所有権は移動しない。
	 * 例外・未定義条件: なし。
	 * @return this
	 */
	@Override
	public Monomial self() {
		return this;
	}
}
