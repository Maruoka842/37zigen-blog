package library.tools.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import library.util.Itertools;

public class Naive {
    static MyPrintWriter pw = MyPrintWriter.getInstance();

    static FastScanner sc = FastScanner.getInstance();

    public static void main(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> System.exit(1));
        new Naive().run();
        pw.flush();
    }

    void run() {
        var fp = Fp.MOD998244353;
        long mod = fp.modulus();
        int N = sc.nextInt();
        int M = sc.nextInt();
        int Q = sc.nextInt();
        int[]L=new int[Q];
        int[]R=new int[Q];
        int[]X=new int[Q];
        for (int i = 0; i < Q; i++) {
        	L[i]=sc.nextInt()-1;
        	R[i]=sc.nextInt();
        	X[i]=sc.nextInt();
        }
        int ans=0;
        for (var a:Itertools.product(IntStream.range(0, M+1).toArray(), N)) {
        	boolean f=true;
        	for (int i = 0; i < Q; i++) {
        		int max=Integer.MIN_VALUE;
				for (int j = L[i]; j < R[i]; j++) {
					max=Math.max(max, a[j]);
				}
				f&=max==X[i];
			}
        	if(f)++ans;
        }
        System.out.println(ans);
    }
}

class ArrayUtils {
    public static void swap(long[] A, long[] B) {
        if (A.length != B.length) {
            throw new AssertionError();
        }
        for (int i = 0; i < A.length; i++) {
            long tmp = A[i];
            A[i] = B[i];
            B[i] = tmp;
        }
    }
}

class FastScanner {
    private static FastScanner instance = null;

    private final InputStream in = System.in;

    private final byte[] buffer = new byte[1 << 16];

    private int ptr = 0;

    private int buflen = 0;

    private FastScanner() {
    }

    public static FastScanner getInstance() {
        if (instance == null) {
            instance = new FastScanner();
        }
        return instance;
    }

    private boolean hasNextByte() {
        if (ptr < buflen) {
            return true;
        }
        ptr = 0;
        try {
            buflen = in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buflen > 0;
    }

    private int readByte() {
        if (hasNextByte()) {
            return buffer[ptr++];
        } else {
            return -1;
        }
    }

    private boolean isPrintableChar(int c) {
        return (33 <= c) && (c <= 126);
    }

    public boolean hasNext() {
        while (hasNextByte() && (!isPrintableChar(buffer[ptr]))) {
            ptr++;
        } 
        return hasNextByte();
    }

    public long nextLong() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        long n = 0;
        boolean minus = false;
        int b = readByte();
        if (b == '-') {
            minus = true;
            b = readByte();
        }
        while ((b >= '0') && (b <= '9')) {
            // n = n * 10 + (b - '0');
            n = ((n << 1) + (n << 3)) + (b - '0');
            b = readByte();
        } 
        return minus ? -n : n;
    }

    public int nextInt() {
        return ((int) (nextLong()));
    }
}

class Fp extends Zn implements LongFieldStrategy {
    /**
     * 998244353 を法とする有限体 F_998244353。
     */
    public static final Fp MOD998244353 = new Fp(998244353);

    public Fp(long mod) {
        super(mod);
    }

    /**
     * inv(n) 用に遅延初期化される逆元テーブル。
     */
    int[] inv = new int[0];

    /**
     * n < 0 でもok
     *
     * @param n
     * @return  */
    public long inv(long n) {
        if (n < 0) {
            n = reduce(n);
        }
        return n < inv.length ? inv[((int) (n))] & 0xffffffffL : MathUtils.modInv(n, mod);
    }

    /**
     * 事前条件: b != 0 in F_mod。返り値 r は r = a / b in F_mod。
     * 未テスト
     * 計算量: O(log mod)
     *
     * @param a
     * 		被除数
     * @param b
     * 		除数
     * @return a / b mod mod
     */
    @Override
    public long div(long a, long b) {
        return mul(a, inv(b));
    }

    /**
     * 事前条件: b != 0 in F_mod。返り値 r は r = 0。
     * 未テスト
     * 計算量: O(1)
     *
     * @param a
     * 		被除数
     * @param b
     * 		除数
     * @return 0
     */
    @Override
    public long mod(long a, long b) {
        if (equals(b, zero())) {
            throw new ArithmeticException("Division by zero");
        }
        return zero();
    }

    /**
     * 返り値 r は a = 0 なら r = 0、そうでなければ r = 1。
     * 未テスト
     * 計算量: O(1)
     *
     * @param a
     * 		対象
     * @return a のノルム
     */
    @Override
    public long norm(long a) {
        return equals(a, zero()) ? 0 : 1;
    }

    /**
     * 返り値 u は a = 0 なら u = 1、そうでなければ u = a。
     * 未テスト
     * 計算量: O(1)
     *
     * @param a
     * 		対象
     * @return a = u * canonical(a) を満たす単元 u
     */
    @Override
    public long canonicalUnit(long a) {
        if (equals(a, zero())) {
            return one();
        }
        return a;
    }

    /**
     * a は 32bit 整数を仮定
     */
    public long pow(long a, long n) {
        if (n < 0) {
            a = inv(a);
            n = -n;
        }
        return MathUtils.modPow(a, n, mod);
    }
}

class Intervals {
    /**
     * 与えられた半開区間の集合の中から、他の区間を部分集合として含まない極小な半開区間 (inclusion minimal intervals) のリストを返す。
     *
     * <p>半開区間 [A, B) が [C, D) に含まれる ([A, B) ⊆ [C, D)) とは、
     * C ≤ A かつ B ≤ D であることを定義とする。
     * [C, D) が別の区間 [A, B) を含むとき、[C, D) は極小ではないため破棄される。
     * 入力に重複する区間が存在する場合、そのうち1つのみが極小な区間として残る。
     * 空区間（L ≥ R）は極小な区間の計算から除外される。</p>
     *
     * @param list
     * 		半開区間 [L, R) を表す long[] のリスト。各要素は list.get(i)[0] = L, list.get(i)[1] = R。
     * @return 極小な区間のリスト（L の昇順、等しい場合は R の昇順にソートされている）
     * @complexity O(N \log N)
     */
    // 未テスト
    public static ArrayList<long[]> inclusionMinimalIntervals(ArrayList<long[]> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        ArrayList<long[]> valid = new ArrayList<>();
        for (long[] interval : list) {
            if (((interval != null) && (interval.length >= 2)) && (interval[0] < interval[1])) {
                valid.add(interval);
            }
        }
        valid.sort((x, y) -> {
            int cmp = Long.compare(x[1], y[1]);// Rの昇順

            if (cmp != 0) {
                return cmp;
            }
            return Long.compare(y[0], x[0]);// Lの降順

        });
        ArrayList<long[]> temp = new ArrayList<>();
        boolean hasMaxL = false;
        long maxL = 0;
        for (int i = 0; i < valid.size(); i++) {
            long[] interval = valid.get(i);
            if ((!hasMaxL) || (interval[0] > maxL)) {
                temp.add(interval);
                maxL = interval[0];
                hasMaxL = true;
            }
        }
        return temp;
    }
}

class LazySegTreelonglong {
    int n = 1;

    int inputN;

    long[] v;

    long[] lazy;

    private LongBinaryOperator mergeX;

    private LongBinaryOperator mergeA;

    private LongBinaryOperator mergeAX;

    long identityA;

    long identityX;

    int root = 1;

    @SuppressWarnings("unchecked")
    public LazySegTreelonglong(int n, LongBinaryOperator mergeA, LongBinaryOperator mergeX, LongBinaryOperator mergeAX, long identityA, long identityX) {
        this.inputN = n;
        this.n = 2 * Integer.highestOneBit(n);
        v = new long[2 * this.n];
        lazy = new long[2 * this.n];
        this.identityX = identityX;
        this.identityA = identityA;
        Arrays.fill(v, identityX);
        Arrays.fill(lazy, identityA);
        this.mergeA = (x, y) -> {
            if (x == identityA) {
                return y;
            }
            if (y == identityA) {
                return x;
            }
            return mergeA.applyAsLong(x, y);
        };
        this.mergeX = (x, y) -> {
            if (x == identityX) {
                return y;
            }
            if (y == identityX) {
                return x;
            }
            return mergeX.applyAsLong(x, y);
        };
        this.mergeAX = (x, y) -> {
            if (x == identityA) {
                return y;
            }
            return mergeAX.applyAsLong(x, y);
        };
    }

    void push(int k) {
        if (lazy[k] == identityA) {
            return;
        }
        v[k] = mergeAX.applyAsLong(lazy[k], v[k]);
        if (((2 * k) + 1) < v.length) {
            lazy[2 * k] = mergeA.applyAsLong(lazy[k], lazy[2 * k]);
            lazy[(2 * k) + 1] = mergeA.applyAsLong(lazy[k], lazy[(2 * k) + 1]);
        }
        lazy[k] = identityA;
    }

    public long get(int a) {
        return fold(a, a + 1);
    }

    public long fold(int a, int b) {
        return fold(0, n, a, b, root);
    }

    public long act(int a, int b, long add) {
        return act(0, n, a, b, root, add);
    }

    long act(int l, int r, int a, int b, int k, long add) {
        if ((a <= l) && (r <= b)) {
            lazy[k] = mergeA.applyAsLong(add, lazy[k]);
        }
        push(k);
        if ((a <= l) && (r <= b)) {
            return v[k];
        } else if ((r <= a) || (b <= l)) {
            return identityX;
        } else {
            int m = (l + r) / 2;
            long vl = act(l, m, a, b, 2 * k, add);
            long vr = act(m, r, a, b, (2 * k) + 1, add);
            v[k] = mergeX.applyAsLong(v[2 * k], v[(2 * k) + 1]);
            return mergeX.applyAsLong(vl, vr);
        }
    }

    long fold(int l, int r, int a, int b, int k) {
        push(k);
        if ((a <= l) && (r <= b)) {
            return v[k];
        } else if ((r <= a) || (b <= l)) {
            return identityX;
        } else {
            int m = (l + r) / 2;
            long vl = fold(l, m, a, b, 2 * k);
            long vr = fold(m, r, a, b, (2 * k) + 1);
            v[k] = mergeX.applyAsLong(v[2 * k], v[(2 * k) + 1]);
            return mergeX.applyAsLong(vl, vr);
        }
    }

    int id(int a, int b) {
        int w = Integer.lowestOneBit(a ^ b);
        return (n / w) + (a / w);
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "acted\n";
        for (int w = n; w >= 1; w /= 2) {
            for (int i = 0; i < n; i += w) {
                ret += String.valueOf(v[id(i, i + w)]) + " ";
            }
            ret += "\n";
        }
        ret += "acting\n";
        for (int w = n; w >= 1; w /= 2) {
            for (int i = 0; i < n; i += w) {
                ret += String.valueOf(lazy[id(i, i + w)]) + " ";
            }
            if (w != 1) {
                ret += "\n";
            }
        }
        return ret;
    }
}

/**
 * primitive long に特化した可換環の代数的構造。
 */
interface LongCommutativeRingStrategy extends LongRingStrategy {}

/**
 * primitive long に特化したユークリッド整域の代数的構造。
 */
interface LongEuclideanDomainStrategy extends LongGCDDomainStrategy {
    /**
     *
     * @param a
     * @param b
     * @return a / b
     */
    long div(long a, long b);

    /**
     *
     * @param a
     * @param b
     * @return a % b
     */
    long mod(long a, long b);

    /**
     *
     * @param a
     * @return a のノルム
     */
    long norm(long a);

    /**
     * a = u * canonical(a) となる単元 u を返す。
     *
     * @param a
     * @return 単元 u
     */
    default long canonicalUnit(long a) {
        return one();
    }

    @Override
    default long gcd(long a, long b) {
        while (!equals(b, zero())) {
            a = mod(a, b);
            long t = a;
            a = b;
            b = t;
        } 
        if (equals(a, zero())) {
            return a;
        }
        return div(a, canonicalUnit(a));
    }

    record ExtGCDResult(long x, long y, long gcd) {}

    /**
     * ax + by = gcd(a, b) を解く。
     *
     * @param a
     * @param b
     * @return 解 (x, y, gcd)
     */
    default ExtGCDResult extgcd(long a, long b) {
        long x0 = one();
        long y0 = zero();
        long g0 = a;
        long x1 = zero();
        long y1 = one();
        long g1 = b;
        while (!equals(g1, zero())) {
            long q = div(g0, g1);
            long nextG = sub(g0, mul(q, g1));
            long nextX = sub(x0, mul(q, x1));
            long nextY = sub(y0, mul(q, y1));
            x0 = x1;
            y0 = y1;
            g0 = g1;
            x1 = nextX;
            y1 = nextY;
            g1 = nextG;
        } 
        if (equals(g0, zero())) {
            return new ExtGCDResult(x0, y0, g0);
        }
        long u = canonicalUnit(g0);
        return new ExtGCDResult(div(x0, u), div(y0, u), div(g0, u));
    }
}

/**
 * primitive long に特化した除法が可能な環の代数的構造。
 */
interface LongExactDivRingStrategy extends LongIntegralDomainStrategy {
    /**
     * 割り切れることが保証されている場合に a / b を計算する。
     *
     * @param a
     * @param b
     * @return a / b
     */
    long divExact(long a, long b);
}

/**
 * primitive long に特化した体の代数的構造。
 */
interface LongFieldStrategy extends LongEuclideanDomainStrategy , LongExactDivRingStrategy {
    @Override
    default long divExact(long a, long b) {
        return div(a, b);
    }

    /**
     *
     * @param a
     * @return a^-1
     */
    long inv(long a);

    /**
     *
     * @param a
     * @param b
     * @return a / b
     */
    default long div(long a, long b) {
        return mul(a, inv(b));
    }

    /**
     * 1 + a + a^2 + ... = 1 / (1 - a) を計算する。
     *
     * @param a
     * 		公比
     * @return 等比級数の和
     */
    default long geometricSum(long a) {
        return inv(sub(one(), a));
    }

    @Override
    default LongEuclideanDomainStrategy.ExtGCDResult extgcd(long a, long b) {
        if (!equals(a, zero())) {
            return new LongEuclideanDomainStrategy.ExtGCDResult(inv(a), zero(), one());
        } else if (!equals(b, zero())) {
            return new LongEuclideanDomainStrategy.ExtGCDResult(zero(), inv(b), one());
        } else {
            return new LongEuclideanDomainStrategy.ExtGCDResult(zero(), zero(), zero());
        }
    }
}

/**
 * primitive long に特化したGCD整域の代数的構造。
 */
interface LongGCDDomainStrategy extends LongIntegralDomainStrategy {
    /**
     *
     * @param a
     * @param b
     * @return gcd(a, b)
     */
    long gcd(long a, long b);
}

/**
 * primitive long に特化した整域の代数的構造。
 */
interface LongIntegralDomainStrategy extends LongCommutativeRingStrategy {}

/**
 * primitive long に特化した環の代数的構造。
 */
interface LongRingStrategy extends LongSemiRingStrategy {
    /**
     *
     * @param a
     * @return -a
     */
    long neg(long a);

    /**
     *
     * @param a
     * @param b
     * @return a - b
     */
    default long sub(long a, long b) {
        return add(a, neg(b));
    }
}

/**
 * primitive long に特化した半環の代数的構造。
 */
interface LongSemiRingStrategy {
    /**
     *
     * @return 加法の単位元
     */
    long zero();

    /**
     *
     * @return 乗法の単位元
     */
    long one();

    /**
     *
     * @param a
     * @param b
     * @return a + b
     */
    long add(long a, long b);

    /**
     *
     * @param a
     * @param b
     * @return a * b
     */
    long mul(long a, long b);

    /**
     *
     * @param a
     * @param b
     * @return a == b
     */
    boolean equals(long a, long b);
}

class MathUtils {
    public static long modPow(long a, long n, long mod) {
        if (n < 0) {
            long inv = MathUtils.modInv(a, mod);
            return MathUtils.modPow(inv, -n, mod);
        }
        if (n == 0) {
            return 1;
        }
        return (MathUtils.modPow((a * a) % mod, n / 2, mod) * ((n % 2) == 1 ? a : 1)) % mod;
    }

    /**
     * 拡張ユークリッドの互除法で逆元を求める。
     *
     * @param a
     * @param mod
     * @return  */
    public static long modInv(long a, long mod) {
        a = ((a % mod) + mod) % mod;
        long[] f0 = new long[]{ 1, 0, mod };
        long[] f1 = new long[]{ 0, 1, a };
        while (f1[2] != 0) {
            long q = f0[2] / f1[2];
            for (int i = 0; i < 3; i++) {
                f0[i] -= q * f1[i];
            }
            ArrayUtils.swap(f0, f1);
        } 
        return f0[1] < 0 ? mod + f0[1] : f0[1];
    }
}

class MyPrintWriter extends PrintWriter {
    private static MyPrintWriter instance = null;

    private MyPrintWriter() {
        super(System.out);
    }

    public static MyPrintWriter getInstance() {
        if (instance == null) {
            instance = new MyPrintWriter();
        }
        return instance;
    }
}

/**
 * null は identity の代わり
 */
class SegTreeFactory {
    /**
     * identityX=Long.MAX_VALUE
     *
     * @param n
     * @return  */
    public static LazySegTreelonglong min_min(int n) {
        // var tree=new LazySegTree<Long, Long>(n, Long::min, Long::min, Long::min, Long.MAX_VALUE);
        var tree = new LazySegTreelonglong(n, Long::min, Long::min, Long::min, Long.MAX_VALUE, Long.MAX_VALUE);
        return tree;
    }
}

class Zn implements LongCommutativeRingStrategy {
    /**
     * 剰余環 Z/modZ の法。
     */
    final long mod;

    public Zn(long mod) {
        this.mod = mod;
    }

    public long modulus() {
        return this.mod;
    }

    /**
     * 返り値 r は r = 0。
     * 未テスト
     * 計算量: O(1)
     *
     * @return 0
     */
    @Override
    public long zero() {
        return 0;
    }

    /**
     * 返り値 r は r ≡ 1 (mod mod) かつ 0 ≤ r < mod。
     * 未テスト
     * 計算量: O(1)
     *
     * @return 1 mod mod
     */
    @Override
    public long one() {
        return 1 % mod;
    }

    public long pow(long a, long n) {
        if (n < 0) {
            throw new AssertionError();
        }
        return MathUtils.modPow(a, n, mod);
    }

    @Override
    public long add(long a, long b) {
        long ret = (a + b) % mod;
        if (ret < 0) {
            ret += mod;
        }
        return ret;
    }

    public long sub(long a, long b) {
        long ret = (a - b) % mod;
        if (ret < 0) {
            ret += mod;
        }
        return ret;
    }

    @Override
    public long mul(long a, long b) {
        return (a * b) % mod;
    }

    /**
     * 返り値 r は r ≡ -a (mod mod) かつ 0 ≤ r < mod。
     * 未テスト
     * 計算量: O(1)
     *
     * @param a
     * 		被減元
     * @return -a mod mod
     */
    @Override
    public long neg(long a) {
        return a == 0 ? 0 : mod - a;
    }

    /**
     * 返り値 r は r ⇔ a = b。
     * 未テスト
     * 計算量: O(1)
     *
     * @param a
     * 		比較対象
     * @param b
     * 		比較対象
     * @return a == b
     */
    @Override
    public boolean equals(long a, long b) {
        return a == b;
    }

    /**
     * *
     * 剰余を取り、0以上mod未満の値を返す。
     *
     * @param a
     * @return  */
    public long reduce(long a) {
        a %= mod;
        if (a < 0) {
            a += mod;
        }
        return a;
    }
}


// --- Original Code ---
// import java.io.IOException;
// import java.util.ArrayDeque;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Queue;
// 
// import library.tools.FastScanner;
// import library.tools.MergeFiles;
// import library.tools.MyPrintWriter;
// import library.util.Fp;
// import library.util.Intervals;
// import library.util.collections.IntArrayList;
// import library.util.segtree.SegTreeFactory;
// 
// public class Main {
// 	static MyPrintWriter pw = MyPrintWriter.getInstance();
// 	static FastScanner sc = FastScanner.getInstance();
// 
// 	public static void main(String[] args) throws IOException {
// 		new Main().run();
// 		pw.flush();
// 		MergeFiles.export();
// 	}
// 
// 	void run() {
// 		var fp=Fp.MOD998244353;
// 		long mod=fp.modulus();
// 		int N=sc.nextInt();
// 		int M=sc.nextInt();
// 		int Q=sc.nextInt();
// 		var seg=SegTreeFactory.min_min(N);
// 		long ans=1;
// 		HashMap<Integer, ArrayList<long[]>> queries=new HashMap<>();
// 		HashMap<Integer, ArrayList<Integer>> positions=new HashMap<>();
// 		for (int i = 0; i < Q; i++) {
// 			int L=sc.nextInt()-1;
// 			int R=sc.nextInt();
// 			int X=sc.nextInt();
// 			seg.act(L, R, X);
// 			queries.putIfAbsent(X, new ArrayList<>());
// 			queries.get(X).add(new long[] {L, R});
// 		}
// 		for (var es : queries.entrySet()) {
// 			var list=es.getValue();
// 			list=Intervals.inclusionMinimalIntervals(list);
// 			es.setValue(list);
// 		}
// 		for (int i = 0; i < N; i++) {
// 			long v=seg.get(i);
// 			if(v > M) ans=ans*(M+1)%mod;
// 			else {
// 				positions.putIfAbsent((int) v, new ArrayList<>());
// 				positions.get((int)v).add(i);
// 				
// //				list[(int)v].add(i);
// 			}
// 		}
// 		for (var es : queries.entrySet()) {
// 			int v=es.getKey();
// 			var query = es.getValue();
// 			class State {
// 				int pos;
// 				long val;
// 				
// 				public State(int pos, long val) {
// 					this.pos=pos;
// 					this.val=val;
// 				}
// 			}
// 			Queue<State>que=new ArrayDeque<State>();
// 			que.add(new State(-1, 1));
// 			long sum=1;
// 			int pointer=0;
// 			var list=positions.get(v);
// 			for (int i = 0; i < list.size(); i++) {
// 				int pos=(int)list.get(i);
// 				que.add(new State(pos, sum));
// 				sum=sum*(v+1)%mod;
// 				while(pointer<query.size() && (query.get(pointer)[1] <= pos+1 || i==list.size()-1)) {
// 					while(!que.isEmpty() && que.peek().pos < query.get(pointer)[0]) {
// 						var state=que.poll();
// 						sum-=state.val*fp.pow(v, que.size());
// 						sum%=mod;
// 					}
// 					pointer++;
// 				}
// 			}
// 			ans*=sum;
// 			ans%=mod;
// 		}
// 		pw.println(ans);
// 	}
// 	
// 	
// 
// 	void tr(Object... objects) {
// 		System.out.println(Arrays.deepToString(objects));
// 	}
// }
// 
