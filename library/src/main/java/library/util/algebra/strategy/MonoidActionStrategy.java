package library.util.algebra.strategy;

import library.util.DiscreteLogarithm;
import library.util.algebra.strategy.monoid.MonoidStrategy;
import library.util.collections.Hash;

/**
 * モノイド F の集合 S への作用を表すインターフェース。
 *
 * @param <F> 作用するモノイドの型
 * @param <S> 作用される集合の型
 */
public interface MonoidActionStrategy<F, S> extends SemigroupActionStrategy<F, S> {
    /**
     * 作用するモノイドの戦略を返す。
     * @return モノイドの戦略
     */
    public MonoidStrategy<F> actingMonoidStrategy();

    /**
     * f を n 回繰り返し作用させた結果 f^n(s) を返す。
     * @param f 作用させる要素
     * @param n 繰り返し回数 (n >= 0)
     * @param s 作用される要素
     * @return f^n(s)
     *
     * <p>計算量: O(log n)</p>
     */
    public default S powAct(F f, long n, S s) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0) return s;
        return powAct(f, n, s, actingMonoidStrategy()::mul);
    }

    /**
     * 離散対数問題 min {n | f^n(s) = t, 0 <= n <= maxSearch} を解く。
     * @param f 作用させる要素
     * @param s 初期状態
     * @param t 目標状態
     * @param maxSearch 最大探索範囲
     * @return 最小の n、存在すれば 0 <= n <= maxSearch、存在しなければ -1
     *
     * <p>計算量: O(sqrt(maxSearch))</p>
     */
    public default long discreteLog(F f, S s, S t, long maxSearch) {
        return discreteLog(f, s, t, maxSearch, null);
    }

    /**
     * 離散対数問題 min {n | f^n(s) = t, 0 <= n <= maxSearch} を解く。
     * @param f 作用させる要素
     * @param s 初期状態
     * @param t 目標状態
     * @param maxSearch 最大探索範囲
     * @param strategy 状態 S のハッシュ・等価性判定戦略
     * @return 最小の n、存在すれば 0 <= n <= maxSearch、存在しなければ -1
     *
     * <p>計算量: O(sqrt(maxSearch))</p>
     */
    public default long discreteLog(F f, S s, S t, long maxSearch, Hash.Strategy<S> strategy) {
        if (maxSearch < 0) return -1;
        return DiscreteLogarithm.discreteLog(f, s, t, this::act, actingMonoidStrategy()::mul, maxSearch, strategy);
    }
}
