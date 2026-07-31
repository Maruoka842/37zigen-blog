package library.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.BiPredicate;

import library.util.collections.LongArrayList;
import library.util.graph.Graph;
import library.util.graph.LongValueGraph;

public class Itertools {
	
	
    /**
     * 配列 a を空でない連続部分配列たちに順序付き分割する。
     * <p>
     * 例えば a = [10, 20, 30] のとき、
     * [[10, 20, 30]],
     * [[10], [20, 30]],
     * [[10, 20], [30]],
     * [[10], [20], [30]]
     * を返す。
     * <p>
     * a.length == 0 のときは空分割を 1 回だけ返す。
     * 未テスト
     * @param a 分割する配列
     * @return a の連続順序付き分割
     */
    public static Iterable<int[][]> orderedPartition(int[] a) {
        return orderedPartitions(a);
    }

    /**
     * 配列 a を空でない連続部分配列たちに順序付き分割する。
     * {@link #orderedPartition(int[])} の複数形エイリアス。
     */
    public static Iterable<int[][]> orderedPartitions(int[] a) {
        return () -> new IntArrayOrderedPartitionIterator(a);
    }

    static class IntArrayOrderedPartitionIterator implements Iterator<int[][]> {
        final int[] a;
        final int n;
        final long end;
        long mask;

        IntArrayOrderedPartitionIterator(int[] a) {
            this.a = a;
            this.n = a.length;

            if (n >= 63) {
                throw new IllegalArgumentException("a.length must be at most 62");
            }

            this.mask = 0;
            this.end = n == 0 ? 1 : 1L << (n - 1);
        }

        @Override
        public boolean hasNext() {
            return mask < end;
        }

        @Override
        public int[][] next() {
            if (!hasNext()) throw new NoSuchElementException();

            long bits = mask++;
            if (n == 0) return new int[0][];

            int len = 1 + Long.bitCount(bits);
            int[][] res = new int[len][];

            int p = 0;
            int prev = 0;

            for (int i = 1; i < n; i++) {
                if (((bits >>> (i - 1)) & 1L) != 0) {
                    res[p++] = Arrays.copyOfRange(a, prev, i);
                    prev = i;
                }
            }

            res[p] = Arrays.copyOfRange(a, prev, n);
            return res;
        }
    }

    /**
     * n の順序付き分割を返す。
     * {@link #orderedPartition(int)} の複数形エイリアス。
     * 未テスト
     */
    public static Iterable<int[]> orderedPartitions(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        return () -> new OrderedPartitionIterator(n);
    }

    static class OrderedPartitionIterator implements Iterator<int[]> {
        final int n;
        final long end;
        long mask;

        OrderedPartitionIterator(int n) {
            this.n = n;

            if (n >= 63) {
                throw new IllegalArgumentException("n must be at most 62");
            }

            this.mask = 0;
            this.end = n == 0 ? 1 : 1L << (n - 1);
        }

        @Override
        public boolean hasNext() {
            return mask < end;
        }

        @Override
        public int[] next() {
            if (!hasNext()) throw new NoSuchElementException();

            long bits = mask++;
            if (n == 0) return new int[0];

            int len = 1 + Long.bitCount(bits);
            int[] res = new int[len];

            int p = 0;
            int prev = 0;

            // i は切れ目の位置。
            // i=1 は 1 個目と 2 個目の間、i=n-1 は n-1 個目と n 個目の間。
            for (int i = 1; i < n; i++) {
                if (((bits >>> (i - 1)) & 1L) != 0) {
                    res[p++] = i - prev;
                    prev = i;
                }
            }

            res[p] = n - prev;
            return res;
        }
    }
	
	
	/**
	 * 自己辺・多重辺なし。
	 * @param o
	 * @param N
	 * @return
	 */
	public static Iterable<LongValueGraph> completeValueGraph(int[] o, int N) {
    	return () ->  new ValueCompleteGraphIterator(o, N);
    }

    static class ValueCompleteGraphIterator implements Iterator<LongValueGraph> {
    	private final ProductIterator baseIterator;
        private final int N;
        /***
         * repeat=0のときは空文字列を返す。
         */
        public ValueCompleteGraphIterator(int[] o, int N) {
        	this.baseIterator = new ProductIterator(o, N * (N - 1) / 2);
        	this.N = N;
        }

        public boolean hasNext() {
        	return baseIterator.hasNext();
        }

        public LongValueGraph next() {
        	int[] a = baseIterator.next();
        	LongValueGraph g= new LongValueGraph(N);
        	int pointer = 0;
        	for (int i = 0; i < N; ++i) {
        		for (int j = i + 1; j < N; ++j) {
        			g.addEdge(i, j, a[pointer]);
        			pointer++;
        		}
        	}
        	return g;
        }
    }


    
	/**
	 * 自己辺・多重辺なし。
	 * n=8,9 のとき 2^{n(n-1)/2}=3e8, 7e10
	 * @param o
	 * @param N
	 * @return
	 */
	public static Iterable<Graph> Graph(int N) {
    	return () ->  new GraphIterator(N);
    }

    static class GraphIterator implements Iterator<Graph> {
    	private final ProductIterator baseIterator;
        private final int N;
        /***
         * repeat=0のときは空文字列を返す。
         */
        public GraphIterator(int N) {
        	this.baseIterator = new ProductIterator(ArrayUtils.range(0, 2), N * (N - 1) / 2);
        	this.N = N;
        }

        public boolean hasNext() {
        	return baseIterator.hasNext();
        }

        public Graph next() {
        	int[] a = baseIterator.next();
        	Graph g= new Graph(N);
        	int pointer = 0;
        	for (int i = 0; i < N; ++i) {
        		for (int j = i + 1; j < N; ++j) {
        			if (a[pointer] == 1)
        				g.addEdge(i, j);
        			pointer++;
        		}
        	}
        	return g;
        }
    }

    
    /**
     * 2 進表現において隣接する 1 を含まない非負整数を昇順に列挙するイテレータ。
     * 列挙は {@code from}（含む）から開始し、
	 * {@code lastExclusive}（含まない）未満の値のみを返す。
	 * 列挙順は数値としての昇順である。
     * @param from
     * @param lastExclusive
     * @return
     */
	public static Iterable<Integer> nonadjacentBits(int from, int lastExclusive) {
    	return () ->  new nonadjacentBitsIterator(from, lastExclusive);
    }

    static class nonadjacentBitsIterator implements PrimitiveIterator.OfInt {
    	int val;
    	int lastExclusive;
        boolean computed=true;
        
        public nonadjacentBitsIterator(int from, int lastExclusive) {
        	this.val = from;
        	this.lastExclusive = lastExclusive;
        }

        public boolean hasNext() {
        	return nextVal() < lastExclusive;
        }

        int nextVal() {
        	if (computed) {
        		return val;
        	}
        	int i=0;
        	if (Ints.bitAt(val, 0) == 0) {
        		val^=1;
        	} else {
        		val^=1;
        		val^=1<<1;
        		i++;
        	}
        	while (Ints.bitAt(val, i) == 1 && Ints.bitAt(val, i + 1) == 1) {
        		val ^= 1 << i;
        		val ^= 1 << (i + 1);
        		val ^= 1 << (i + 2);
        		i += 2;
        	}
        	computed=true;
        	return val;
        }

		@Override
		public int nextInt() {
        	nextVal();
        	computed = false;
        	return val;
		}
    }

	/**
	 * repeat=0のときは空配列を返す。返り値はIteratorによって破壊的に更新される。
	 * @param o
	 * @param repeat
	 * @return
	 */
	public static Iterable<int[]> product(int[] o, int repeat) {
    	return () ->  new ProductIterator(o, repeat);
    }

    static class ProductIterator implements Iterator<int[]> {
        private final int[] elements;
        private final int[] current;
        private final int[] currentByInts;
        boolean first = true;
        
        /***
         * repeat=0のときは空文字列を返す。
         */
        public ProductIterator(int[] o, int repeat) {
        	elements= o;
        	current = new int[repeat];
        	currentByInts = new int[repeat];
        	Arrays.fill(current, o[0]);
        	Arrays.fill(currentByInts, 0);
        }

        public boolean hasNext() {
        	if (first) return true;
        	int t=currentByInts.length-1;
        	while(t >= 0 && currentByInts[t]==elements.length-1)--t;
        	if(t == -1)return false;
        	return true;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        		return current;
        	}
        	int t=currentByInts.length-1;
        	while(t >= 0 && currentByInts[t]==elements.length-1)--t;
        	currentByInts[t]++;
        	current[t] = elements[currentByInts[t]];
        	for(int i=t+1;i<current.length;++i) {
        		current[i]=elements[0];
        		currentByInts[i]=0;
        	}
        	return current;
        }
    }

    public record Range(long lower, long upper) {
    }

    /**
     * ２冪の幅の区間に分割する
     * @param right
     * @return
     */
    public static Iterable<Range> dyadicRangeDecomposition(long L, long R) {
    	if (L < 0) throw new AssertionError();
    	return () ->  new DyadicRangeDecompositionIterator(L, R);
    }
    
    static class DyadicRangeDecompositionIterator implements Iterator<Range> {
        long right;
        long left;

        public DyadicRangeDecompositionIterator(long right) {
        	this.left = 0;
        	this.right = right;
        }
        
        public DyadicRangeDecompositionIterator(long left, long right) {
        	this.left = left;
        	this.right = right;
        }

        
        public boolean hasNext() {
        	return left < right;
        }

        public Range next() {
        	Range ret;
        	long ml0=left+Long.lowestOneBit(left);
    		long ml1=left|Long.highestOneBit(left^right);
    		if(left < ml0 && ml0<=right) {
    			ret = new Range(left, ml0);
    			left = ml0;
    		} else {
    			ret = new Range(left, ml1);
    			left = ml1;
    		}
        	return ret;
        }
    }
    
    
    /**
     * [0, N)を２冪の幅の区間に分割する。区間は昇順に並ぶ。
     * @param n
     * @return
     */
    public static Iterable<Range> dyadicRangeDecomposition(long n) {
    	return () ->  new DyadicRangeDecompositionIterator(n);
    }
    
    /***
     * k = floor(N/i) が等しい 1 <= smallestQuotientInclusive <= i <=largestQuotientInclusive <= N がなす閉区間[l, r]について組 [l, r, k] を k の昇順 （i の降順）に返す。
     * smallestQuotientInclusive, largestQuotientInclusive は floor(n / i) の形で表せなくてもよい。
     */    
    public static Iterable<FloorRange> floorRange(long n, long smallestQuotientInclusive, long largestQuotientInclusive) {
    	return () ->  new FloorRangeIterator(n, smallestQuotientInclusive, largestQuotientInclusive);
    }

    /***
     * k = floor(N/i) が等しい 1 <= i <= N がなす閉区間[l, r]について組 [l, r, k] を k の昇順 （i の降順）に返す。
     */    
    public static Iterable<FloorRange> floorRange(long n) {
    	return () ->  new FloorRangeIterator(n, 1, n);
    }
    
    public static class FloorRange{
    	private long l;
		private long r;
		private long quotient;
		public long l() {
			return l;
		}
		public long r() {
			return r;
		}
		public long quotient() {
			return quotient;
		}
    }
    
    static class FloorRangeIterator implements Iterator<FloorRange> {
        boolean first = true;
        long n;
        long quotient;
        long lower, upper;
        long startQuotient = 1;
        long endQuotient;
        final FloorRange fr = new FloorRange(); 

        public FloorRangeIterator(long n, long startQuotient, long endQuotient) {
        	if (startQuotient > endQuotient) throw new AssertionError();
        	this.startQuotient = startQuotient;
        	this.endQuotient = Math.min(endQuotient, n);
        	this.n = n;
        }
        
        public boolean hasNext() {
        	if (first) {
        		if (startQuotient <= 0) throw new AssertionError();
        		if (startQuotient > n) return false;
        		startQuotient = n / (n / startQuotient);
        		quotient = startQuotient;
            	upper=n/quotient;
        		lower=n/(quotient+1)+1;
        		return quotient <= endQuotient;
        	}
        	return quotient != endQuotient && n/(lower-1) <= endQuotient;
        }

        public FloorRange next() {
        	if (first) {
        		first = false;
        	} else {
        		quotient=n/(lower-1);
        		upper=lower-1;//upper=n/quotientから書き換えたが未テスト
        		lower=n/(quotient+1)+1;
        	}
        	fr.l = lower;
        	fr.r = upper;
        	fr.quotient = quotient;
        	return fr;
        }
    }
    
    
    
    /**
     * aの順列を、aから順に辞書順で返す。要素に重複があってよい。aは変更しない。
     * @param a
     * @return
     */
    public static Iterable<int[]> permutations(int[] a) {
    	return () ->  new PermutationIterator(a);
    }
    
    /**
     * aの順列を、aから順に辞書順で返す。要素に重複があってよい。
     * @param a
     * @return
     */
	public static Iterable<char[]> permutations(char[] a) {
    	return () ->  new CharPermutationIterator(a);
	}
	
	
    static class CharPermutationIterator implements Iterator<char[]> {
        boolean first = true;
        char[] a;
        
        public CharPermutationIterator(char[] a) {
        	this.a = Arrays.copyOf(a, a.length);
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	for (int i=0;i+1<a.length;++i) {
        		if(a[i]<a[i+1])return true;
        	}
        	return false;
        }

        public char[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=a.length-1;
        		while(i>0&&a[i-1]>=a[i])--i;
        		if(i==0)throw new AssertionError();
        		int j=i;
        		while(j+1<a.length&&a[i-1]<a[j+1])++j;
        		ArrayUtils.swap(i-1, j, a);
        		int s=i;
        		int t=a.length-1;
        		while(s<t) {
        			ArrayUtils.swap(s,t,a);
        			++s;--t;
        		}
        	}
        	return a;
        }
    }

    /**
     * 未テスト
     * @param sum
     * @param n
     * @return
     */
    public static Iterable<int[]> nonnegativeIntArrayOfFixedSumFixedLength(int sum, int n) {
    	int[]a=new int[n];
    	a[n-1]=sum;
    	return () ->  new NonnegativeIntArrayOfFixedSumFixedLengthIterator(a);
    }
    
    static class NonnegativeIntArrayOfFixedSumFixedLengthIterator implements Iterator<int[]> {
        boolean first = true;
        int[] a;
        long sum;
        
        public NonnegativeIntArrayOfFixedSumFixedLengthIterator(int[] a) {
        	this.a = Arrays.copyOf(a, a.length);
        	sum=ArrayUtils.sum(a);
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	return a[0] != sum;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=a.length-1;
        		while(a[i]==0)i--;
        		a[i-1]++;
        		a[i]--;
        		for (int j = i; j < a.length - 1; j++) {
					a[a.length-1]+=a[j];
					a[j]=0;
        		}
        	}
        	return a;
        }
    }

    
    
    public static Iterable<int[]> permutations(int n) {
    	return () ->  new PermutationIterator(ArrayUtils.range(0, n));
    }
    
    static class PermutationIterator implements Iterator<int[]> {
        boolean first = true;
        int[] a;
        
        public PermutationIterator(int[] a) {
        	this.a = Arrays.copyOf(a, a.length);
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	for (int i=0;i+1<a.length;++i) {
        		if(a[i]<a[i+1])return true;
        	}
        	return false;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=a.length-1;
        		while(i>0&&a[i-1]>=a[i])--i;
        		if(i==0)throw new AssertionError();
        		int j=i;
        		while(j+1<a.length&&a[i-1]<a[j+1])++j;
        		ArrayUtils.swap(i-1, j, a);
        		int s=i;
        		int t=a.length-1;
        		while(s<t) {
        			ArrayUtils.swap(s,t,a);
        			++s;--t;
        		}
        	}
        	return a;
        }
    }

    /**
     * {0,1,..,n-1}からk元集合を選ぶ。1回あたりの計算量は償却　f(n,k) = (n+1)/(n-k+1) ≤ k + 1。また、min(f(n,k), f(n,n-k)) ≤ 2
     * k = 0 のときは空配列を返す。
     * @param n
     * @param k
     * @return
     */
    public static Iterable<int[]> combinations(int n, int k) {
    	return () ->  new CombinationIterator(n, k);
    }
    
    static class CombinationIterator implements Iterator<int[]> {
        boolean first = true;
        int[] a;
        int n;
        int k;
        
        public CombinationIterator(int n, int k) {
        	this.n=n;
        	this.k=k;
        	a=new int[k];
        	for (int i = 0; i < k; i++) {
				a[i]=i;
			}
        }
        
        public boolean hasNext() {
        	if (!(n >= k && k >= 0)) return false;
        	if (first) {
        		return true;
        	}
        	//a=[..,n-3,n-2,n-1]だと終了
        	for (int i = a.length - 1; i >= 0; i--) {
				if(a[i]!=n-1-(a.length-1-i)) return true;
			}
        	return false;
        }

        // tが少なくともi回デクリメントされる選び方がcomb(n-i, k-i)個ある。
        //　∑[i>=0] comb(n-i, k-i) = comb(n+1,n-k+1)
        // comb(n+1, n-k+1) / comb(n, n-k)
        //=(n+1) / (n-k+1)
        // 1回あたりの計算量は償却　f(n,k) = (n+1)/(n-k+1) ≤ k + 1。
        // また、min(f(n,k), f(n,n-k)) ≤ 2
        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int t = a.length - 1;
        		while (t >= 0 && a[t]==n-1-(a.length-1-t)) {
        			--t;
        		}
        		a[t]++;
        		for (int j = t+1; j < a.length; j++) {
					a[j]=a[j-1]+1;
				}
        	}
        	return a;
        }
    }

    
    
    static class CombinationIterator2 implements Iterator<int[]> {
        boolean first = true;
        int[] a;
        int[] b;
        int n;
        int k;
        
        public CombinationIterator2(int[] b, int k) {
        	this.n=b.length;
        	this.k=k;
        	a=new int[k];
        	for (int i = 0; i < k; i++) {
				a[i]=i;
			}
        	this.b=b;
        }
        
        public boolean hasNext() {
        	if (!(n >= k && k >= 0)) return false;
        	if (first) {
        		return true;
        	}
        	//a=[..,n-3,n-2,n-1]だと終了
        	for (int i = a.length - 1; i >= 0; i--) {
				if(a[i]!=n-1-(a.length-1-i)) return true;
			}
        	return false;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=ArrayUtils.maxDecrement(0, a.length-1, id->a[id]==n-1-(a.length-1-id));
        		a[i]++;
        		for (int j = i+1; j < a.length; j++) {
					a[j]=a[j-1]+1;
				}
        	}
        	return ArrayUtils.take(b, a);
        }
    }

    
    
    
    /**
     * 正整数nの分割を返す。例えばn=5で11111,1112,122,23,5を返す。
     * n=80で15796476通り。
     * @param n
     * @param k
     * @return
     */
    public static Iterable<int[]> partitions(int n) {
    	return () ->  new PartitionIterator(n);
    }
    
    static class PartitionIterator implements Iterator<int[]> {
        boolean first = true;
        int[] a;//a[0..size)が返り値（値は降順）。a[size..)=[0,0,..0]
        int n;
        int size;
        
        public PartitionIterator(int n) {
        	this.n=n;
        	a=new int[n];
        	Arrays.fill(a, 1);
        	size = n;
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	return size!=1;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		//a, b
        		//x, a+b-x
        		//a+b-x>=x
        		//a+b>=2x
        		//111111111
        		//11111112
        		//1111113
        		//1111122
        		//111114
        		
        		
        		// a+1,a+1,..,a+1,
        		int res=a[size-1]-1;
        		a[size-1]=0;
        		a[size-2]++;
        		size--;
        		while(res!=0) {
        			if(res>=a[size-1]) {
        				a[size]=a[size-1];
        				res-=a[size-1];
        				size++;
        			}else {
        				a[size-1]+=res;
        				break;
        			}
        		}
        	}
        	return Arrays.copyOf(a, size);
        }
    }
    
    
    /**
     * サイズ0以上n以下の0,..,maxExclusive-1からなる多重集合を返す
     * @param n
     * @param maxExclusive
     * @return
     * https://atcoder.jp/contests/abc421/submissions/73263274
     */
    public static Iterable<int[]> multiset(int n, int maxExclusive) {
    	return () ->  new  MultisetIterator(n, maxExclusive);
    }
    
    
    
    public static class MultisetIterator implements Iterator<int[]> {
    	int n;
    	int maxExclusive;
        private int currentLength = 0;
        private Iterator<int[]> currentIterator;

        public MultisetIterator(int n, int maxExclusive) {
            this.n = n;
            this.maxExclusive = maxExclusive;
            this.currentIterator = combinationsWithReplacement(maxExclusive, 0).iterator();
        }

        @Override
        public boolean hasNext() {
            while (!currentIterator.hasNext()) {
                currentLength++;
                if (currentLength > n) {
                    return false;
                }
                currentIterator = combinationsWithReplacement(maxExclusive, currentLength).iterator();
            }
            return true;
        }

        @Override
        public int[] next() {
            if (!hasNext()) throw new NoSuchElementException();
            return currentIterator.next();
        }
    }
    
    
    /**
     * {0, 1, ..., n - 1} から重複を許して k 個選ぶ組合せ（広義単調増加列）を列挙する。
     * <p>
     * k = 0 のときは空配列（長さ 0 の配列）を 1 回だけ返す。
     *
     * <h3>列挙順序</h3>
     * 生成される組合せは、配列要素を左から比較したときの**辞書順（lexicographical order）の昇順**で列挙される。
     * <p>
     * 例：{@code n = 3, k = 2} の場合、以下の順序（計 6 通り）で列挙される。
     * <ol>
     *   <li>{@code [0, 0]}</li>
     *   <li>{@code [0, 1]}</li>
     *   <li>{@code [0, 2]}</li>
     *   <li>{@code [1, 1]}</li>
     *   <li>{@code [1, 2]}</li>
     *   <li>{@code [2, 2]}</li>
     * </ol>
     *
     * <h3>事前条件</h3>
     * <ul>
     *   <li>n &gt;= 1</li>
     *   <li>k &gt;= 0</li>
     * </ul>
     *
     * <h3>破壊的変更 / 参照共有</h3>
     * 返されるイテレータの {@code next()} メソッドは、呼び出しごとに同一の {@code int[]} 配列インスタンスを
     * 破壊的に更新（再利用）して返す。そのため、各ステップにおける組合せの状態を保存して再利用したい場合は、
     * 返された配列のコピー（例：{@code clone()}）を保存する必要がある。
     *
     * @param n 選択元の要素数（0 以上 n - 1 以下の整数から選ぶ）
     * @param k 選択する要素数
     * @return 組合せを辞書順で列挙する {@link Iterable}
     * @complexity
     * <ul>
     *   <li>時間計算量: 全列挙の総ステップ数は H(n, k) = C(n + k - 1, k) 通り。イテレータの 1 ステップ（{@code next()}）あたり最悪 O(k) 時間、かつ償却 O(1) 時間。</li>
     *   <li>空間計算量: O(k)（内部保持および返却用配列のサイズ）</li>
     * </ul>
     */
    public static Iterable<int[]> combinationsWithReplacement(int n, int k) {
    	return () ->  new CombinationWithReplacementIterator(n, k);
    }
    
    static class CombinationWithReplacementIterator implements Iterator<int[]> {
        boolean first = true;
        int[] a;
        int n;
        int k;
        
        public CombinationWithReplacementIterator(int n, int k) {
        	this.n=n;
        	this.k=k;
        	a=new int[k];
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	for (int i=a.length-1;i>=0;--i) {
        		if(a[i]!=n-1)return true;
        	}
        	return false;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=ArrayUtils.maxDecrement(0, a.length-1, id->a[id]==n-1);
        		a[i]++;
        		for (int j = i+1; j < a.length; j++) {
					a[j]=a[i];
				}
        	}
        	return a;
        }
    }


    /**
     * lower以上upper未満の長さnの狭義単調増加列を返す。
     * @param lowerInclusive
     * @param upperExclusive
     * @param n
     * @return
     */
    public static Iterable<int[]> strictlyIncreasingIntArrays(int lowerInclusive, int upperExclusive, int n) {
    	return () -> new CombinationIterator2(ArrayUtils.range(lowerInclusive, upperExclusive), n);
    }
    
    /**
     * lower以上upper未満の長さnの狭義単調減少列を返す。
     * @param lowerInclusive
     * @param upperExclusive
     * @param n
     * @return
     */
    public static Iterable<int[]> strictlyDecreasingIntArrays(int lowerInclusive, int upperExclusive, int n) {
    	int[] a=ArrayUtils.range(lowerInclusive, upperExclusive);
    	ArrayUtils.reverse(a);
    	return () -> new CombinationIterator2(a, n);
    }

    
    /**
     * lower以上upper未満の長さnの広義単調増加列を辞書順に返す。
     * @param lowerInclusive
     * @param upperExclusive
     * @param n
     * @return
     * https://atcoder.jp/contests/abc378/submissions/72838615
     */
    public static Iterable<int[]> nondecreasingIntArrays(int lowerInclusive, int upperExclusive, int n) {
    	return () ->  new CombinationsWithReplacementIterator2(ArrayUtils.range(lowerInclusive, upperExclusive), n);
    }
    
    /**
     * CombinationsWithReplacementIteratorは{0,1,..,n-1}から多重k元集合を選んでいたが、こちらはint[] valuesからk元集合を選ぶ。
     */
    static class CombinationsWithReplacementIterator2 implements Iterator<int[]> {
        boolean first = true;
        int[] indices;
        int[] values;
        int[] ret;
        int n;
        int k;
        
        public CombinationsWithReplacementIterator2(int[] values, int k) {
        	this.k=k;
        	this.n=values.length;
        	indices=new int[k];
        	ret=new int[k];
        	this.values=values;
        	for (int i = 0; i < k; i++) {
        		ret[i]=values[0];
        	}
        }
        
        public boolean hasNext() {
        	if (first) {
        		return true;
        	}
        	for (int i = indices.length - 1; i >= 0; i--) {
        		if(indices[i]!=n-1)return true;
        	}
        	return false;
        }

        public int[] next() {
        	if (first) {
        		first = false;
        	} else {
        		int i=ArrayUtils.maxDecrement(0, indices.length-1, id->indices[id]==n-1);
        		indices[i]++;
        		ret[i]=values[indices[i]];
        		for (int j = i+1; j < indices.length; j++) {
					indices[j]=indices[i];
					ret[j]=values[indices[i]];
        		}
        	}
        	return ret;
        }
    }

    @FunctionalInterface
    public interface IntBinaryPredicate {
        public boolean test(int left, int right);
    }

    @FunctionalInterface
    public interface LongBinaryPredicate {
        public boolean test(long left, long right);
    }

    /***
     * [0,1,2,..,a.length-1] を f(a[i], a[i+1]) = True となる極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(int[] a, IntBinaryPredicate f) {
    	int sz = 1;
    	if (a.length == 0) return new int[0][];
    	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
    	int[][] ret = new int[sz][];
    	int p = 0;
    	for (int i  = 0; i < a.length; ++i) {
    		int j = i;
    		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
    		ret[p] = new int[j - i + 1];
    		for (int k = i; k <= j; ++k) {
    			ret[p][k - i] = k;
    		}
    		p++;
    		i = j;
    	}
    	return ret;
    }

    /***
     * [0,1,2,..,a.length-1] を f(a[i], a[i+1]) = True となる極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(long[] a, LongBinaryPredicate f) {
	int sz = 1;
	if (a.length == 0) return new int[0][];
	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
	int[][] ret = new int[sz][];
	int p = 0;
	for (int i  = 0; i < a.length; ++i) {
		int j = i;
		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
		ret[p] = new int[j - i + 1];
		for (int k = i; k <= j; ++k) {
			ret[p][k - i] = k;
		}
		p++;
		i = j;
	}
	return ret;
    }


    /***
     * [0,1,2,..,a.length-1] を f(a[i], a[i+1]) = True となる極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(char[] a, BiPredicate<Character, Character> f) {
    	int sz = 1;
    	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
    	int[][] ret = new int[sz][];
    	int p = 0;
    	for (int i  = 0; i < a.length; ++i) {
    		int j = i;
    		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
    		ret[p] = new int[j - i + 1];
    		for (int k = i; k <= j; ++k) {
    			ret[p][k - i] = k;
    		}
    		p++;
    		i = j;
    	}
    	return ret;
    }
    
    
    /***
     * [0,1,2,..,a.length-1] を f(a[i], a[i+1]) = True となる極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(boolean[] a, BiPredicate<Boolean, Boolean> f) {
    	int sz = 1;
    	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
    	int[][] ret = new int[sz][];
    	int p = 0;
    	for (int i  = 0; i < a.length; ++i) {
    		int j = i;
    		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
    		ret[p] = new int[j - i + 1];
    		for (int k = i; k <= j; ++k) {
    			ret[p][k - i] = k;
    		}
    		p++;
    		i = j;
    	}
    	return ret;
    }

    
    /***
     * a を値が等しい極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(int[] a) {
    	return groupBy(a, (u, v) -> u == v);
    }

    /***
     * a を値が等しい極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @return
     */
    public static int[][] groupBy(long[] a) {
	return groupBy(a, (u, v) -> u == v);
    }
    
    
    /***
     * a を値が等しい極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(char[] a) {
    	return groupBy(a, (u, v) -> u == v);
    }

    
    /***
     * a を値が等しい極大な区間に分割し、その区間(インデックスの列)を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(boolean[] a) {
    	return groupBy(a, (u, v) -> u == v);
    }    
    /***
     * [0,1,2,..,a.length-1] を f(a[i], a[i+1]) = True となる極大な区間に分割し、その区間を並べた配列を返す。
     * @param a
     * @param f
     * @return
     */
    public static int[][] groupBy(int[][] a, BiPredicate<int[], int[]> f) {
    	int sz = 1;
    	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
    	int[][] ret = new int[sz][];
    	int p = 0;
    	for (int i  = 0; i < a.length; ++i) {
    		int j = i;
    		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
    		ret[p] = new int[j - i + 1];
    		for (int k = i; k <= j; ++k) {
    			ret[p][k - i] = k;
    		}
    		p++;
    		i = j;
    	}
    	return ret;
    }
    
    public static <T> int[][] groupBy(T[] a, BiPredicate<T, T> f) {
    	if(a.length==0)return new int[0][0];
    	int sz = 1;
    	for (int i  = 0; i + 1 < a.length; ++i) if (!f.test(a[i], a[i + 1])) ++sz;
    	int[][] ret = new int[sz][];
    	int p = 0;
    	for (int i  = 0; i < a.length; ++i) {
    		int j = i;
    		while (j + 1 < a.length && f.test(a[j], a[j+1])) ++j;
    		ret[p] = new int[j - i + 1];
    		for (int k = i; k <= j; ++k) {
    			ret[p][k - i] = k;
    		}
    		p++;
    		i = j;
    	}
    	return ret;
    }
    
    
    /**
     * [0, v)を[a2^i,(a+1)2^i)の形の区間の足し引きで表す。
     * 返り値の要素[a, b]について、a>bならば[b, a)を引くことを表す。
     * https://atcoder.jp/contests/abc355/submissions/71266421
     */
    List<long[]> dyadicDecompositionWithSubtraction(long L, long R) {
    	
    	ArrayList<long[]> list1=new ArrayList<>();
    	{// L2 <=L <= R <= R2
    		long L2=L;
    		long R2=R;
    		while(true) {
    			long w=Long.lowestOneBit(L2^R2);
    			if(R2-L2==w && Long.lowestOneBit(L2|R2) == w) {
    				break;
    			}
    			if(L2 != 0 && Long.lowestOneBit(L2)<Long.lowestOneBit(R2)) {
    				L2-=Long.lowestOneBit(L2);
    			}else {
    				R2+=Long.lowestOneBit(R2);
    			}
    		}
    		list1.add(new long[] {L2, R2});
		var Ldec=dyadicDecompositionWithSubtraction(L-L2);
		var Rdec=dyadicDecompositionWithSubtraction(R2-R);
    		long curL=L;
    		for (long v : Ldec) {
    			list1.add(new long[] {curL, curL-v});
				curL -= v;
    		}
    		long curR=R;
    		for (long v : Rdec) {
    			list1.add(new long[] {curR+v, curR});
    			curR += v;
    		}
    	}
    	ArrayList<long[]> list2=new ArrayList<>();
    	
    	{
    		long mid=Longs.binaryLcp(L, R)^Long.highestOneBit(Longs.binaryLcp(L, R)^R);
		var Ldec=dyadicDecompositionWithSubtraction(mid-L);
		var Rdec=dyadicDecompositionWithSubtraction(R-mid);
    		long curL=L;
    		for (long v : Ldec) {
				list2.add(new long[] {curL, curL+v});
				curL += v;
    		}
    		long curR=R;
    		for (long v : Rdec) {
    			list2.add(new long[] {curR-v, curR});
    			curR -= v;
    		}

    	
    	}
    	var ans=list1.size()<list2.size()?list1:list2;
    	return ans;
    }
    
    /**
     * [0, v)を[a2^i,(a+1)2^i)の形の区間の足し引きで表す。
     * 返り値について、v<0ならば[0, v)を引くことを表す。
     * https://atcoder.jp/contests/abc355/submissions/71266421
     * @param v
     * @return
     */
    List<Long> dyadicDecompositionWithSubtraction(long v) {
    	ArrayList<Long> ret=new ArrayList<>();
    	for (int i = 0; i < 60; i++) {
    		if(Longs.bitAt(v, i)==1) {
    		int cnt0=0;
			int cnt1=0;
				int j=i;
				while(true) {
					++cnt1;
					if(Longs.bitAt(v, j+1)==0&&Longs.bitAt(v, j+2)==0) {
						break;
					} else if (Longs.bitAt(v, j+1)==1) {
						++j;
					} else if (Longs.bitAt(v, j+1)==0&&Longs.bitAt(v, j+2)==1) {
						j+=2;
						++cnt0;
					}
				}
				if(cnt1<=cnt0+2) {
					for (int k = i; k <= j; k++) {
						if(Longs.bitAt(v, k) == 1) {
							ret.add(1L<<k);
						}
					}
				} else {
					ret.add(-(1L<<i));
					for (int k = i; k <= j; k++) {
						if(Longs.bitAt(v, k) == 0) {
							ret.add(-(1L<<k));
						}
					}
					ret.add(1L<<(j+1));
				}
				i=j;
			}
		}
    	if(ArrayUtils.sum(ret)!=v) throw new AssertionError();
    	return ret;
    }
    
    
    
    
    /**
     * 部分列（空列除く）を（長さ, インデックス)の辞書順に返す
     * @param a
     * @return
     */
    public static Iterable<int[]> subsequences(int[] a) {
        return () -> new IntSubsequenceIterator(a);
    }

    static class IntSubsequenceIterator implements Iterator<int[]> {
        final int[] a;
        int n;

        // 現在の部分列の長さ
        int k = 0;

        // 現在選んでいるインデックス
        int[] idx;

        boolean first = true;

        IntSubsequenceIterator(int[] a) {
            this.a = a;
            this.n = a.length;
            this.idx = new int[0];
        }

        @Override
        public boolean hasNext() {
            if (first) return true;
            if (k < n) return true;
            // k == n のときは [0,1,2,...,n-1] しかない
            return false;
        }

        @Override
        public int[] next() {
            if (first) {
                first = false;
            } else {
            	if (nextCombination()) {
            		return materialize();
            	}
            }


            // 次の長さへ
            k++;

            idx = new int[k];
            for (int i = 0; i < k; i++) idx[i] = i;
            return materialize();
        }

        /** idx を次の組合せに進める。なければ false */
        boolean nextCombination() {
            for (int i = k - 1; i >= 0; i--) {
            	// 選んだ箇所(take)が
            	// ????36789となっているとき
            	// ????45678とする
                if (idx[i] < n - (k - i)) {
                    idx[i]++;
                    for (int j = i + 1; j < k; j++) {
                        idx[j] = idx[j - 1] + 1;
                    }
                    return true;
                }
            }
            return false;
        }

        int[] materialize() {
        	int[] res = new int[k];
            for (int i = 0; i < k; i++) {
                res[i] = a[idx[i]];
            }
            return res;
        }
    }

    


    /**
     * n の重複を許す multiplicative partition を返す。
     * <p>
     * 高度合成数に対する実行時間の目安：
     * <ul>
     *   <li>n=735,134,400 (約数13,440個) : 約204万通り、約0.6秒</li>
     *   <li>n=2,940,537,600 (約数20,160個) : 約721万通り、約1.7秒</li>
     *   <li>n=13,967,553,600 (約数30,240個) : 約1,375万通り、約3.7秒</li>
     * </ul>
     * @param n
     * @return
     */
    public static Iterable<long[]> multiplicativePartitions(long n) {
        if (n < 1) throw new IllegalArgumentException("n must be at least 1");
        return () -> new MultiplicativePartitionIterator(n, false);
    }

    /**
     * n の重複を許さない multiplicative partition を返す。
     * <p>
     * 高度合成数に対する実行時間の目安：
     * <ul>
     *   <li>n=735,134,400 (約数13,440個) : 約125万通り、約0.3秒</li>
     *   <li>n=2,940,537,600 (約数20,160個) : 約390万通り、約1.0秒</li>
     *   <li>n=13,967,553,600 (約数30,240個) : 約880万通り、約2.3秒</li>
     * </ul>
     * @param n
     * @return
     */
    public static Iterable<long[]> distinctMultiplicativePartitions(long n) {
        if (n < 1) throw new IllegalArgumentException("n must be at least 1");
        return () -> new MultiplicativePartitionIterator(n, true);
    }

    /**
     * multiplicative partition を生成するイテレータ。
     * 深い再帰を避けるため、明示的なスタックを用いた DFS で実装されている。
     */
    static class MultiplicativePartitionIterator implements Iterator<long[]> {
        private final long n;
        private final boolean distinct;
        private final ArrayList<Long> allDivisors;

        /**
         * 探索の状態を保持するスタックフレーム。
         */
        private static class Frame {
            // 現在のフレームにおける分解対象の残り数値。
            long remN;
            // このフレームで選択可能な最小の因数値（分割の順序を固定するため）。
            long minF;
            // allDivisors の中での探索開始インデックス。
            int divIdx;
            // 自身 (remN) を一つの因数として出力済みかどうか。
            boolean selfYielded;

            Frame(long remN, long minF, int divIdx) {
                this.remN = remN;
                this.minF = minF;
                this.divIdx = divIdx;
                this.selfYielded = false;
            }
        }

        private final Deque<Frame> stack = new ArrayDeque<>();
        // 現在の探索パスで確定している因数のリスト。
        private final LongArrayList currentFactors = new LongArrayList();
        private long[] nextVal;

        public MultiplicativePartitionIterator(long n, boolean distinct) {
            this.n = n;
            this.distinct = distinct;
            if (n < 1) throw new IllegalArgumentException("n must be at least 1");
            if (n == 1) {
                nextVal = new long[]{1};
                allDivisors = null;
                return;
            }
            this.allDivisors = MathUtils.divisors(n);
            // allDivisors[0] is 1, we skip it.
            stack.push(new Frame(n, 2, 1));
            advance();
        }

        /**
         * 次の分割を探索して nextVal にセットする。
         */
        private void advance() {
            nextVal = null;
            while (!stack.isEmpty()) {
                Frame f = stack.peek();

                // 1. まず、現在の残りの数 remN 自身を最後の因数とする分割を返す（自身の探索）。
                //    これにより、例えば 12 に対する [12], [2, 6], [2, 2, 3] などの順で生成される。
                if (!f.selfYielded) {
                    f.selfYielded = true;
                    // 分割を非減少順（または厳密増加順）に保つため、remN >= minF が必要。
                    if (f.remN >= f.minF) {
                        long[] res = new long[currentFactors.size() + 1];
                        for (int i = 0; i < currentFactors.size(); i++) res[i] = currentFactors.get(i);
                        res[res.length - 1] = f.remN;
                        nextVal = res;
                        return;
                    }
                }

                // 2. 次に、remN をさらに分解できるか試みる。
                boolean found = false;
                for (int i = f.divIdx; i < allDivisors.size(); i++) {
                    long d = allDivisors.get(i);
                    // d * d' = remN かつ d <= d' となる d を探せば十分。
                    if (d > f.remN / d) {
                        f.divIdx = allDivisors.size();
                        break;
                    }
                    // 因数 d を選び、残りの remN/d をさらに分解する子フレームをスタックに積む。
                    if (f.remN % d == 0 && d >= f.minF) {
                        currentFactors.add(d);
                        f.divIdx = i + 1;
                        // 重複を許さない場合は次の因数を d+1 以上に制限する。
                        stack.push(new Frame(f.remN / d, distinct ? d + 1 : d, distinct ? i + 1 : i));
                        found = true;
                        break;
                    }
                }

                // 3. このフレームでの探索が終了したら、スタックから除いて親に戻る。
                if (!found) {
                    Frame popped = stack.pop();
                    // 親の remN をさらに分解するために積まれたフレーム（popped.remN != n）のみ、
                    // 対応する因数が currentFactors に入っているので pop する。
                    if (popped.remN != n) {
                        currentFactors.pollLast();
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return nextVal != null;
        }

        @Override
        public long[] next() {
            if (nextVal == null) throw new NoSuchElementException();
            long[] ret = nextVal;
            advance();
            return ret;
        }
    }

    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}