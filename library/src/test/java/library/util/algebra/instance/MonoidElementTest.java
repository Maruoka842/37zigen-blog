package library.util.algebra.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.monoid.MonoidStrategy;
import library.util.polynomial.Monomial;

class MonoidElementTest {
    static class CountingElement implements MonoidElement<CountingElement> {
        /** 値を表す整数。 */
        final int value;
        /** 親モノイドストラテジ。 */
        final CountingStrategy strategy;

        CountingElement(int value, CountingStrategy strategy) {
            this.value = value;
            this.strategy = strategy;
        }

        /**
         * この元の親モノイドを返す。
         * 未テスト。
         * 事前条件: this.strategy != null。
         * 事後条件: 戻り値 == this.strategy。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: strategy 参照を共有し、所有権は移動しない。
         * 例外・未定義条件: this.strategy == null のとき未定義。
         * @return 親モノイド
         */
        @Override
        public MonoidStrategy<CountingElement> parent() {
            return strategy;
        }

        /**
         * この元自身を返す。
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
        public CountingElement self() {
            return this;
        }
    }

    static class CountingStrategy implements MonoidStrategy<CountingElement> {
        /** identity() の呼び出し回数。 */
        int identityCalls;
        /** merge(a,b) の呼び出し回数。 */
        int mergeCalls;

        /**
         * 加法モノイドの単位元 0 を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 戻り値.value = 0 かつ identityCalls は 1 増加する。
         * 副作用: identityCalls を 1 増やす。
         * 計算量: O(1)。
         * 破壊的変更: identityCalls を変更する。
         * 参照共有・所有権: 戻り値は新規所有され、strategy 参照を共有する。
         * 例外・未定義条件: なし。
         * @return 0
         */
        @Override
        public CountingElement identity() {
            identityCalls++;
            return new CountingElement(0, this);
        }

        /**
         * a.value + b.value を値にもつ元を返す。
         * 未テスト。
         * 事前条件: a != null, b != null, a.strategy == this, b.strategy == this。
         * 事後条件: 戻り値.value = a.value + b.value かつ mergeCalls は 1 増加する。
         * 副作用: mergeCalls を 1 増やす。
         * 計算量: O(1)。
         * 破壊的変更: mergeCalls を変更し、a と b は変更しない。
         * 参照共有・所有権: 戻り値は新規所有され、strategy 参照を共有する。
         * 例外・未定義条件: 事前条件を満たさない場合は未定義。
         * @param a 左項
         * @param b 右項
         * @return a.value + b.value
         */
        @Override
        public CountingElement mul(CountingElement a, CountingElement b) {
            mergeCalls++;
            return new CountingElement(a.value + b.value, this);
        }
    }

    /**
     * MonoidElement の既定 mul/one が parent() へ移譲することを検査する。
     * 未テスト。
     * 事前条件: CountingStrategy は呼び出し回数を記録する。
     * 事後条件: mul は merge を 1 回、one は identity を 1 回呼ぶ。
     * 副作用: ローカルな CountingStrategy のカウンタだけを変更する。
     * 計算量: O(1)。
     * 破壊的変更: strategy のカウンタを変更する。
     * 参照共有・所有権: テスト内のローカル参照だけを共有し、所有権は移動しない。
     * 例外・未定義条件: アサーション不成立時に JUnit の例外を送出する。
     */
    @Test
    void monoidElementMulAndOneDelegateToParentStrategy() {
        CountingStrategy strategy = new CountingStrategy();
        CountingElement a = new CountingElement(2, strategy);
        CountingElement b = new CountingElement(5, strategy);

        CountingElement product = a.mul(b);
        CountingElement identity = a.one();

        assertEquals(7, product.value);
        assertEquals(1, strategy.mergeCalls);
        assertEquals(0, identity.value);
        assertEquals(1, strategy.identityCalls);
        assertSame(strategy, product.strategy);
        assertSame(strategy, identity.strategy);
    }

    /**
     * Monomial が MonoidElement の親ストラテジ構成で積と単位元を返すことを検査する。
     * 未テスト。
     * 事前条件: 指数配列の全要素 >= 0。
     * 事後条件: x^A.mul(x^B) = x^{A+B} かつ x^A.one() = x^0。
     * 副作用: なし。
     * 計算量: O(max(|A|, |B|))。
     * 破壊的変更: なし。
     * 参照共有・所有権: static 親ストラテジ参照を共有し、所有権は移動しない。
     * 例外・未定義条件: アサーション不成立時に JUnit の例外を送出する。
     */
    @Test
    void monomialUsesMonoidElementParentForMulAndOne() {
        Monomial x2y = new Monomial(new int[] {2, 1});
        Monomial xy3z = new Monomial(new int[] {1, 3, 1});

        assertEquals(new Monomial(new int[] {3, 4, 1}), x2y.mul(xy3z));
        assertEquals(new Monomial(new int[0]), x2y.one());
        assertSame(x2y.parent(), x2y.parent());
    }
}
