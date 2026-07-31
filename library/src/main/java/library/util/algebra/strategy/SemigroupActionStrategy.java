package library.util.algebra.strategy;

import java.util.function.BiFunction;

import library.util.DiscreteLogarithm;
import library.util.collections.Hash;

/**
 * 半群 F の集合 S への作用を表すインターフェース。
 *
 * @param <F> 作用する半群の型
 * @param <S> 作用される集合の型
 */
public interface SemigroupActionStrategy<F, S> {
    /**
     * 半群の要素 f を s に作用させる。
     * @param f 作用させる要素
     * @param s 作用される要素
     * @return 作用後の要素
     *
     * <p>計算量: 作用の計算量に依存</p>
     */
    S act(F f, S s);

    /**
     * f を n 回繰り返し作用させた結果 f^n(s) を返す。
     * @param f 作用させる要素
     * @param n 繰り返し回数 (n >= 1)
     * @param s 作用される要素
     * @param composition 半群の演算 (f, g) -> f ∘ g
     * @return f^n(s)
     *
     * <p>計算量: O(log n) 回の composition と 1 回の act</p>
     */
    default S powAct(F f, long n, S s, BiFunction<F, F, F> composition) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive for semigroup action");
        F resF = f;
        F curF = f;
        long tmpN = n - 1;
        while (tmpN > 0) {
            if ((tmpN & 1) == 1) {
                resF = composition.apply(curF, resF);
            }
            curF = composition.apply(curF, curF);
            tmpN >>= 1;
        }
        return act(resF, s);
    }

    /**
     * 離散対数問題 min {n | f^n(s) = t, 1 <= n <= maxSearch} を解く。
     * @param f 作用させる要素
     * @param s 初期状態
     * @param t 目標状態
     * @param composition 半群の演算 (f, g) -> f ∘ g
     * @param maxSearch 最大探索範囲
     * @return 最小の n、存在すれば 1 <= n <= maxSearch、存在しなければ -1
     *
     * <p>計算量: O(sqrt(maxSearch))</p>
     */
    default long discreteLog(F f, S s, S t, BiFunction<F, F, F> composition, long maxSearch) {
        return discreteLog(f, s, t, composition, maxSearch, null);
    }

    /**
     * 離散対数問題 min {n | f^n(s) = t, 1 <= n <= maxSearch} を解く。
     * @param f 作用させる要素
     * @param s 初期状態
     * @param t 目標状態
     * @param composition 半群の演算 (f, g) -> f ∘ g
     * @param maxSearch 最大探索範囲
     * @param strategy 状態 S のハッシュ・等価性判定戦略
     * @return 最小の n、存在すれば 1 <= n <= maxSearch、存在しなければ -1
     *
     * <p>計算量: O(sqrt(maxSearch))</p>
     */
    default long discreteLog(F f, S s, S t, BiFunction<F, F, F> composition, long maxSearch, Hash.Strategy<S> strategy) {
        if (maxSearch <= 0) return -1;
        long res = DiscreteLogarithm.discreteLog(f, act(f, s), t, this::act, composition, maxSearch - 1, strategy);
        return res == -1 ? -1 : res + 1;
    }
}
