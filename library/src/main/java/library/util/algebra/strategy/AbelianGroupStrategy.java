package library.util.algebra.strategy;

import library.util.algebra.strategy.monoid.CommutativeMonoidStrategy;

/**
 * アーベル群（可換群）の操作を定義するインターフェース。
 * @param <T>
 */
public interface AbelianGroupStrategy<T> extends GroupStrategy<T>, CommutativeMonoidStrategy<T> {
}
