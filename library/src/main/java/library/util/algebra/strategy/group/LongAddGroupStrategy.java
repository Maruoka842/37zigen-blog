package library.util.algebra.strategy.group;

import library.util.algebra.strategy.AbelianGroupStrategy;
import library.util.algebra.strategy.GroupStrategy;

public class LongAddGroupStrategy implements AbelianGroupStrategy<Long> {

    @Override
    public Long mul(Long a, Long b) {
        return a + b;
    }

    @Override
    public Long inverse(Long a) {
        return -a;
    }

    @Override
    public Long identity() {
        return 0L;
    }

    @Override
    public boolean equals(Long a, Long b) {
        return a.equals(b);
    }
}
