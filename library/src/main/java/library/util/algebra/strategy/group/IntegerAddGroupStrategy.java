package library.util.algebra.strategy.group;

import library.util.algebra.strategy.AbelianGroupStrategy;
import library.util.algebra.strategy.GroupStrategy;

public class IntegerAddGroupStrategy implements AbelianGroupStrategy<Integer> {

    @Override
    public Integer mul(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public Integer inverse(Integer a) {
        return -a;
    }

    @Override
    public Integer identity() {
        return 0;
    }

    @Override
    public boolean equals(Integer a, Integer b) {
        return a.equals(b);
    }
}
