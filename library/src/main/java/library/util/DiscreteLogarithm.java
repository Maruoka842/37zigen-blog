package library.util;

import java.util.Objects;
import java.util.function.BiFunction;

import library.util.collections.Hash;
import library.util.collections.LongOpenHashSet;
import library.util.collections.OpenHashSet;

/**
 * モノイド作用に関する離散対数問題を解く。
 * https://maspypy.com/%e3%83%a2%e3%83%8e%e3%82%a4%e3%83%89%e4%bd%9c%e7%94%a8%e3%81%ab%e9%96%a2%e3%81%99%e3%82%8b%e9%9b%a2%e6%95%a3%e5%af%be%e6%95%b0%e5%95%8f%e9%a1%8c
 */
public class DiscreteLogarithm {

    /**
     * Solve min_n f^n s = t (0 <= n <= maxSearch).
     * baby = f, giant = f^giantStride.
     * If solution is not found, return -1.
     */
    public static <S, F> long discreteLog(F baby, F giant, S s, S t,
                                          BiFunction<F, S, S> mapping, long maxSearch, int giantStride) {
        return discreteLog(baby, giant, s, t, mapping, maxSearch, giantStride, null);
    }

    /**
     * Solve min_n f^n s = t (0 <= n <= maxSearch) with custom Hash Strategy.
     */
    public static <S, F> long discreteLog(F baby, F giant, S s, S t,
                                          BiFunction<F, S, S> mapping, long maxSearch, int giantStride,
                                          Hash.Strategy<S> strategy) {
        if (strategy == null) {
            if (Objects.equals(s, t)) return 0;
        } else {
            if (strategy.equals(s, t)) return 0;
        }
        if (maxSearch <= 0) return -1;

        OpenHashSet<S> ys = strategy == null ? new OpenHashSet<>() : new OpenHashSet<>(strategy);
        {
            S yt = t;
            for (int i = 0; i < giantStride; ++i) {
                ys.add(yt);
                yt = mapping.apply(baby, yt);
            }
        }

        int numFails = 0;
        S cur = s;

        for (long k = 1; ; ++k) {
            S nxt = mapping.apply(giant, cur);
            if (ys.contains(nxt)) {
                for (int i = 1; i <= giantStride; ++i) {
                    cur = mapping.apply(baby, cur);
                    boolean match = strategy == null ? Objects.equals(cur, t) : strategy.equals(cur, t);
                    if (match) {
                        long ret = (k - 1) * giantStride + i;
                        return (ret <= maxSearch) ? ret : -1;
                    }
                }
                ++numFails;
            } else {
                cur = nxt;
            }

            if (numFails >= 2 || k * giantStride > maxSearch) return -1;
        }
    }

    /**
     * Solve min_n f^n s = t (0 <= n <= maxSearch).
     */
    public static <S, F> long discreteLog(F f, S s, S t,
                                          BiFunction<F, S, S> mapping, BiFunction<F, F, F> composition, long maxSearch) {
        return discreteLog(f, s, t, mapping, composition, maxSearch, null);
    }

    /**
     * Solve min_n f^n s = t (0 <= n <= maxSearch) with custom Hash Strategy.
     */
    public static <S, F> long discreteLog(F f, S s, S t,
                                          BiFunction<F, S, S> mapping, BiFunction<F, F, F> composition, long maxSearch,
                                          Hash.Strategy<S> strategy) {
        boolean match = strategy == null ? Objects.equals(s, t) : strategy.equals(s, t);
        if (match) return 0;
        if (maxSearch <= 0) return -1;
        final int giantStride = (int) Math.ceil(Math.sqrt(maxSearch));
        if (giantStride <= 0) return match ? 0 : -1;
        F giant = f;
        F tmp = f;
        for (int n = giantStride - 1; n > 0; n >>= 1) {
            if ((n & 1) == 1) giant = composition.apply(giant, tmp);
            tmp = composition.apply(tmp, tmp);
        }
        return discreteLog(f, giant, s, t, mapping, maxSearch, giantStride, strategy);
    }

    /**
     * Solve min_n x^n = y (1 <= n <= maxSearch).
     */
    public static <S> long discreteLogNonzero(S x, S y, BiFunction<S, S, S> op, long maxSearch) {
        return discreteLogNonzero(x, y, op, maxSearch, null);
    }

    /**
     * Solve min_n x^n = y (1 <= n <= maxSearch) with custom Hash Strategy.
     */
    public static <S> long discreteLogNonzero(S x, S y, BiFunction<S, S, S> op, long maxSearch, Hash.Strategy<S> strategy) {
        long res = discreteLog(x, x, y, op, op, maxSearch, strategy);
        if (res < 0 || res >= maxSearch) return -1;
        return res + 1;
    }

    /**
     * Solve min_n x^n = y mod md (n >= 0).
     */
    public static long discreteLogMod(long x, long y, long md) {
        return discreteLogMod(x, y, md, md);
    }

    public static long discreteLogMod(long x, long y, long md, long maxSearch) {
        x = (x % md + md) % md;
        y = (y % md + md) % md;
        return discreteLogModInternal(x, 1 % md, y, md, maxSearch);
    }

    /**
     * Solve min_n x^n = y mod md (n >= 1).
     */
    public static long discreteLogModNonzero(long x, long y, long md) {
        return discreteLogModNonzero(x, y, md, md);
    }

    public static long discreteLogModNonzero(long x, long y, long md, long maxSearch) {
        x = (x % md + md) % md;
        y = (y % md + md) % md;
        long res = discreteLogModInternal(x, x % md, y, md, maxSearch);
        if (res < 0 || res >= maxSearch) return -1;
        return res + 1;
    }

    private static long discreteLogModInternal(long f, long s, long t, long md, long maxSearch) {
        if (s == t) return 0;
        if (maxSearch <= 0) return -1;

        final int giantStride = (int) Math.ceil(Math.sqrt(maxSearch));
        if (giantStride <= 0) return -1;

        long giant = 1 % md;
        long tmp = f;
        for (int n = giantStride; n > 0; n >>= 1) {
            if ((n & 1) == 1) giant = mulMod(giant, tmp, md);
            tmp = mulMod(tmp, tmp, md);
        }

        return discreteLogModLong(f, giant, s, t, md, maxSearch, giantStride);
    }

    private static long discreteLogModLong(long baby, long giant, long s, long t, long md, long maxSearch, int giantStride) {
        if (s == t) return 0;
        if (maxSearch <= 0) return -1;

        LongOpenHashSet ys = new LongOpenHashSet();
        {
            long yt = t;
            for (int i = 0; i < giantStride; ++i) {
                ys.add(yt);
                yt = mulMod(yt, baby, md);
            }
        }

        int numFails = 0;
        long cur = s;

        for (long k = 1; ; ++k) {
            long nxt = mulMod(cur, giant, md);
            if (ys.contains(nxt)) {
                for (int i = 1; i <= giantStride; ++i) {
                    cur = mulMod(cur, baby, md);
                    if (cur == t) {
                        long ret = (k - 1) * giantStride + i;
                        return (ret <= maxSearch) ? ret : -1;
                    }
                }
                ++numFails;
            } else {
                cur = nxt;
            }

            if (numFails >= 2 || k * giantStride > maxSearch) return -1;
        }
    }

    private static long mulMod(long a, long b, long md) {
        if (md <= 3037000499L) return a * b % md;
        return java.math.BigInteger.valueOf(a).multiply(java.math.BigInteger.valueOf(b)).remainder(java.math.BigInteger.valueOf(md)).longValue();
    }
}
