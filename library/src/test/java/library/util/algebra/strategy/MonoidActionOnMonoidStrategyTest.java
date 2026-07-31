package library.util.algebra.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.monoid.MonoidStrategy;

public class MonoidActionOnMonoidStrategyTest {
    static class LongAddMonoid implements MonoidStrategy<Long> {
        /**
         * Long 加法モノイドの単位元 0 を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値 = 0。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: なし。
         * 例外・未定義条件: なし。
         * @return 0
         */
        @Override public Long identity() { return 0L; }

        /**
         * a + b を返す。
         * 未テスト。
         * 事前条件: a, b != null かつ a + b が long で overflow しない。
         * 事後条件: 戻り値 = a + b。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: なし。
         * 例外・未定義条件: a == null, b == null, または long overflow のとき未定義。
         * @param a 左辺
         * @param b 右辺
         * @return a + b
         */
        @Override public Long mul(Long a, Long b) { return a + b; }
    }

    static class LongMulMonoid implements MonoidStrategy<Long> {
        /**
         * Long 乗法モノイドの単位元 1 を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値 = 1。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: なし。
         * 例外・未定義条件: なし。
         * @return 1
         */
        @Override public Long identity() { return 1L; }

        /**
         * a * b を返す。
         * 未テスト。
         * 事前条件: a, b != null かつ a * b が long で overflow しない。
         * 事後条件: 戻り値 = a * b。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: なし。
         * 例外・未定義条件: a == null, b == null, または long overflow のとき未定義。
         * @param a 左辺
         * @param b 右辺
         * @return a * b
         */
        @Override public Long mul(Long a, Long b) { return a * b; }
    }

    static class ScaleSumAction implements MonoidActionOnMonoidStrategy<Long, Long> {
        /** 作用する乗法モノイド (Long, *, 1)。 */
        final LongMulMonoid acting = new LongMulMonoid();
        /** 作用される加法モノイド (Long, +, 0)。 */
        final LongAddMonoid acted = new LongAddMonoid();

        /**
         * 作用するモノイド (Long, *, 1) の戦略を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値 == this.acting。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: acting の参照を共有し、所有権は移動しない。
         * 例外・未定義条件: なし。
         * @return 作用するモノイドの戦略
         */
        @Override public MonoidStrategy<Long> actingMonoidStrategy() { return acting; }

        /**
         * 作用されるモノイド (Long, +, 0) の戦略を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値 == this.acted。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: acted の参照を共有し、所有権は移動しない。
         * 例外・未定義条件: なし。
         * @return 作用されるモノイドの戦略
         */
        @Override public MonoidStrategy<Long> actedMonoidStrategy() { return acted; }

        /**
         * f ⋅ x を返す。
         * 未テスト。
         * 事前条件: f, x != null かつ f * x が long で overflow しない。
         * 事後条件: 戻り値 = f * x。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: なし。
         * 例外・未定義条件: f == null, x == null, または long overflow のとき未定義。
         * @param f 作用元
         * @param x 作用対象
         * @return f * x
         */
        @Override public Long act(Long f, Long x) { return f * x; }
    }

    @Test
    public void testActedMonoidHelpers() {
        ScaleSumAction action = new ScaleSumAction();

        assertEquals(0L, action.actedMonoidStrategy().identity());
        assertEquals(7L, action.actedMonoidStrategy().mul(3L, 4L));
        assertEquals(action.actedMonoidStrategy().mul(action.act(5L, 3L), action.act(5L, 4L)), action.act(5L, action.actedMonoidStrategy().mul(3L, 4L)));
    }

    @Test
    public void testInheritedMonoidActionMethods() {
        ScaleSumAction action = new ScaleSumAction();

        assertEquals(72L, action.powAct(2L, 3, 9L));
        assertEquals(4L, action.discreteLog(3L, 2L, 162L, 10));
    }
}
