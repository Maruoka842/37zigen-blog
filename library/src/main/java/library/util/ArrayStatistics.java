package library.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.util.collections.IntDeque;
import library.util.collections.ObjectDeque;
import library.util.segtree.MinSeg;
import library.util.segtree.RangeAddRangeSum;
import library.util.segtree.SegTreeFactory;
import library.util.seq.SortedArrays;
import library.util.unionfind.VertexValueUnionFindFactory;

public class ArrayStatistics {
    /**
     * b[i] = (A[j] ≤ .. ≤ A[i-1] ≤ A[i] となる最小のj)
     * @param A
     * @return
     */
    public static int[] increasingRangeFixingRight(int[] A) {
    	var uf=VertexValueUnionFindFactory.minRepresentativeUnionFind(A.length);
    	int[]ord=ArrayUtils.argSort(A);
    	int[]ret=new int[A.length];
    	for (int i:ord) {
    		if(i>0&&A[i-1]<=A[i]) {
    			uf.union(i-1, i);
    		}
    		ret[i]=uf.getVertexValue(i);
    	}
    	return ret;
    }
    

    /**
     * b[i] = (A[i] ≥ .. ≥ A[i-1] ≥ A[i] となる最大のj)
     * @param A
     * @return
     */
    public static int[] decreasingRangeFixingLeft(int[] A) {
    	int[]B=A.clone();
    	ArrayUtils.reverse(B);
    	int[]ret=increasingRangeFixingRight(A);
    	ArrayUtils.reverse(ret);
    	for (int i = 0; i < A.length; i++) {
			ret[i]=A.length-1-ret[i];
		}
    	return ret;
    }    
    
    
    /**
     * sum[i≤j] abs(A[i]-A[j])
     * verified:https://atcoder.jp/contests/abc351/submissions/71278124
     * @param A
     * @return
     */
    public static long absdiffSum(long[] A) {
    	long ret=0;
    	int[] rank=ArrayUtils.rank(A);
    	for (int i = 0; i < A.length; i++) {
    		ret+=(rank[i]-(A.length-1-rank[i]))*A[i];
    	}
    	return ret;
    }
    
    /**
     * 昇順配列 {@code sortedA = (a_0, a_1, ..., a_{n-1})} に対して，
     * 各要素を少なくとも {@code target} にするための最小総加算量を返す。
     * O(log n)
	 * https://atcoder.jp/contests/abc373/submissions/71280391
	 * @param sortedA
	 * @param prefixSum
	 * @param target
	 * @return
	 */
    public static long costToMakeAtLeast(long[] sortedA, long[] prefixSum, long target) {
		int id=SortedArrays.lower(sortedA, target);
		long need=id==-1?0: (-prefixSum[id]+target*(id+1));
		return need;
    }
    
    
	/***
	 * 最長狭義単調増加部分列の長さを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int LIS(int[] a) {
		int[] b = ArrayUtils.compress(a);
		int n = a.length;
		int[] dp = new int[n + 1];
		int INF = n;
		Arrays.fill(dp, INF);
		for (int i = 0; i < n; i++) {
			int next = SortedArrays.ceil(dp, b[i]);
			dp[next] = b[i];
		}
		return 1 + ArrayUtils.maxDecrement(0, n - 1, i -> dp[i] == INF);
	}

	/***
	 * 最長狭義単調増加部分列の添字列を返す。
	 * 未テスト
	 * 計算量: O(N log N)
	 * 
	 * @param a
	 * @return
	 */
	public static int[] strictLISIndices(int[] a) {
		long[] b = new long[a.length];
		for (int i = 0; i < a.length; i++) b[i] = a[i];
		return strictLISIndices(b);
	}

	/***
	 * 最長狭義単調増加部分列の添字列を返す。
	 * 計算量: O(N log N)
	 * 
	 * @param a
	 * @return
	 */
	public static int[] strictLISIndices(long[] a) {
		//https://judge.yosupo.jp/submission/371619
		int n = a.length;
		long[] tailValue = new long[n];
		int[] tailIndex = new int[n];
		int[] prev = new int[n];
		Arrays.fill(tailValue, Long.MAX_VALUE);
		Arrays.fill(prev, -1);
		int len = 0;
		for (int i = 0; i < n; i++) {
			int k = SortedArrays.ceil(tailValue, a[i]);
			if (k > 0) prev[i] = tailIndex[k - 1];
			tailValue[k] = a[i];
			tailIndex[k] = i;
			if (k == len) len++;
		}
		int[] ret = new int[len];
		int cur = len == 0 ? -1 : tailIndex[len - 1];
		for (int i = len - 1; i >= 0; i--) {
			ret[i] = cur;
			cur = prev[cur];
		}
		return ret;
	}

	/***
	 * 最長狭義単調減少部分列の添字列を返す。
	 * 未テスト
	 * 計算量: O(N log N)
	 * 
	 * @param a
	 * @return
	 */
	public static int[] strictLDSIndices(int[] a) {
		long[] b = new long[a.length];
		for (int i = 0; i < a.length; i++) b[i] = -(long)a[i];
		return strictLISIndices(b);
	}

	/***
	 * 最長狭義単調減少部分列の添字列を返す。
	 * 未テスト
	 * 計算量: O(N log N)
	 * 
	 * @param a
	 * @return
	 */
	public static int[] strictLDSIndices(long[] a) {
		long[] b = new long[a.length];
		boolean hasLongMinValue = false;
		for (int i = 0; i < a.length; i++) {
			if(a[i]==Long.MIN_VALUE)hasLongMinValue = true;
		}
		if(hasLongMinValue) {
			int[] rank = ArrayUtils.compress(a);
			for (int i = 0; i < a.length; i++) b[i] = -rank[i];
		} else {
			for (int i = 0; i < a.length; i++) b[i] = -a[i];
		}
		return strictLISIndices(b);
	}


	
	/***
	 * dp[i]=(i番目の要素で終わる狭義単調増加列の最大の長さ)を返す
	 * 
	 * @param a
	 * @return
	 */
	public static int[] LISfixingEnd(int[] a) {
		int[] b = ArrayUtils.compress(a);
		int n = a.length;
		int[] dp = new int[n + 1];
		int[] ret=new int[n];
		int INF = n;
		Arrays.fill(dp, INF);
		for (int i = 0; i < n; i++) {
			int next = SortedArrays.ceil(dp, b[i]);
			dp[next] = b[i];
			ret[i]=next+1;
		}
		return ret;
	}
	
	
	
	/***
	 * dp[i]=(i番目の要素を先頭とする狭義単調増加列の最大の長さ)を返す
	 * 
	 * @param a
	 * @return
	 */
	public static int[] LISfixingStart(int[] a) {
		int[] b = ArrayUtils.compress(a);
		for (int i = 0; i < b.length; i++) {
			b[i]=b.length-1-b[i];
		}
		int n = a.length;
		int[] dp = new int[n + 1];
		int[] ret=new int[n];
		Arrays.fill(dp, n);
		for (int i = n-1; i >= 0; i--) {
			int next = SortedArrays.ceil(dp, b[i]);
			dp[next] = b[i];
			ret[i]=next+1;
		}
		return ret;
	}

	/**
	 * 各区間 {@code [i, j)} について、
	 * {@code a[i]} を先頭、{@code a[j-1]} を末尾に必ず含む
	 * 広義単調増加部分列の最大長を動的計画法で計算する。
	 *
	 * 未テスト
	 * 計算量: O(N^3)
	 * @param a
	 * @return
	 */
	public static int[][] LISForEachIntervals(int[] a) {
		int N=a.length;
		int[][]dp=new int[N][N+1];
		int INF=Integer.MAX_VALUE/3;
		ArrayUtils.fill(dp, -INF);
		// dp[i][j]=a[i:j)でa[i],a[j-1]を先頭,末尾とする広義単調増加部分列の最大長
		for (int i = 0; i < N; i++) {
			dp[i][i+1]=1;
		}
		for (int w = 2; w <= N; w++) {
			for (int i = 0; i+w <= N; i++) {
				if(a[i]<=a[i+w-1]) {
					dp[i][i+w]=2;
					for (int j = i+1; j < i+w-1; j++) {
						if(a[i]<=a[j]) {
							dp[i][i+w]=Math.max(dp[i][i+w], dp[i][j+1]+dp[j][i+w]-1);
						}
					}
				}
			}
		}
		return dp;
	}
	
	
	
	
    /**
     * aからいくつか(0個でもよい)の数を選び、その和を最小化したい。
     * ただし、選ばない数はw個以下しか連続しない。
     * @param a
     * @param w
     * @return
     * verified:https://atcoder.jp/contests/abc334/submissions/48748089
     */
	public static double minSumWithSkipLimit(double[] a, int w) {
    	double[]A=Arrays.copyOf(a, a.length+1);
    	double[]dp=new double[A.length+1];//dp[i]=a[0:i)まで選び、かつ、a[i-1]を選んでいる時の最小値
    	var dq=new ArrayDeque<Integer>();
    	dq.addLast(0);
    	for (int i = 0; i < A.length; i++) {
			if(!dq.isEmpty() && i-dq.peekFirst()>=w+1)dq.pollFirst();
			dp[i+1]=dp[dq.peekFirst()]+A[i];
			while(!dq.isEmpty() && dp[i+1] < dp[dq.peekLast()]) {
				dq.pollLast();
			}
			dq.addLast(i+1);
    	}
    	return dp[A.length];
    }

	
	
    /**
     * a[i] = Σ[j≤i] max(f[j],f[j+1],..,f[i])
     * @param f
     * @return
     * verified:https://atcoder.jp/contests/abc359/submissions/71072257
     */
    public static long[] rangemaxsumFixingEnd(long[] f) {
    	long[]g=f.clone();
    	for (int i = 0; i < f.length; i++) {
			g[i]*=-1;
		}
    	long[]ret=rangeminsumFixingEnd(g);
    	for (int i = 0; i < ret.length; i++) {
			ret[i]*=-1;
		}
    	return ret;
    }

	
	
    
    /**
     * a[i] = Σ[j≤i] min(f[j],f[j+1],..,f[i])
     * @param f
     * @return
     * verified:https://atcoder.jp/contests/abc353/tasks/abc353_e
     */
    public static long[] rangeminsumFixingEnd(long[] f) {
        ArrayDeque<long[]> dq = new ArrayDeque<>();// value, width
        long[]a=new long[f.length];
        long sum = 0;
        for (int i = 0; i < f.length; i++) {
            long width = 1;
            while (!dq.isEmpty() && (dq.peekLast()[0] >= f[i])) {
                sum -= dq.peekLast()[0] * dq.peekLast()[1];
                width += dq.peekLast()[1];
                dq.pollLast();
            } 
            dq.addLast(new long[]{ f[i], width });
            sum += f[i] * width;
            a[i] += sum;
        }
        return a;
    }
    
    /**
     * a[i] = Σ[j≥i] min(f[i],f[i+1],..,f[j])
     * @param f
     * @return
     */
    public static long[] rangeminsumFixingStart(long[] f) {
    	long[]g=f.clone();
    	ArrayUtils.reverse(g);
    	long[]a=rangeminsumFixingEnd(g);
    	ArrayUtils.reverse(a);
    	return a;
    }
    
    
    /**
     * a[i] = (任意の j ≤ k ≤ i について f[k] ≤ f[i]となる最小のj)となるaを返す。
     * @param f
     * @return
     */
    public static int[] maxDominatingRangeFixingEnd(int[] f) {
    	IntDeque stk=new IntDeque();
    	int[] a=new int[f.length];
    	for (int i = 0; i < f.length; i++) {
    		while (!stk.isEmpty() && f[stk.peekLast()] <= f[i]) stk.pollLast();
    		if(!stk.isEmpty()) {
    			a[i]=stk.peekLast()+1;
    		} else {
    			a[i]=0;
    		}
    		stk.addLast(i);
    	}
    	return a;
    }
	

    /**
     * a[i] = (任意の i ≤ k ≤ j について f[k] ≤ f[i]となる最大のj)となるaを返す。
     * @param f
     * @return
     */
    public static int[] maxDominatingRangeFixingStart(int[] f) {
    	IntDeque stk=new IntDeque();
    	int[] a=new int[f.length];
    	for (int i = f.length-1; i >= 0; i--) {
    		while (!stk.isEmpty() && f[stk.peekLast()] <= f[i]) stk.pollLast();
    		if(!stk.isEmpty()) {
    			a[i]=stk.peekLast()-1;
    		} else {
    			a[i]=a.length-1;
    		}
    		stk.addLast(i);
    	}
    	return a;
    }

    
    
    /**
     * a[i] = (任意の j ≤ k ≤ i について f[k] ≤ f[i]となる最小のj)となるaを返す。
     * @param f
     * @return
     */
    public static int[] maxDominatingRangeFixingEnd(long[] f) {
    	IntDeque stk=new IntDeque();
    	int[] a=new int[f.length];
    	for (int i = 0; i < f.length; i++) {
    		while (!stk.isEmpty() && f[stk.peekLast()] <= f[i]) stk.pollLast();
    		if(!stk.isEmpty()) {
    			a[i]=stk.peekLast()+1;
    		} else {
    			a[i]=0;
    		}
    		stk.addLast(i);
    	}
    	return a;
    }
	

    /**
     * a[i] = (任意の i ≤ k ≤ j について f[k] ≤ f[i]となる最大のj)となるaを返す。
     * @param f
     * @return
     */
    public static int[] maxDominatingRangeFixingStart(long[] f) {
    	IntDeque stk=new IntDeque();
    	int[] a=new int[f.length];
    	for (int i = f.length-1; i >= 0; i--) {
    		while (!stk.isEmpty() && f[stk.peekLast()] <= f[i]) stk.pollLast();
    		if(!stk.isEmpty()) {
    			a[i]=stk.peekLast()-1;
    		} else {
    			a[i]=a.length-1;
    		}
    		stk.addLast(i);
    	}
    	return a;
    }
    
    
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}


	/**
	 * 未テスト
	 * 各クエリ $(L, R, C, D)$ に対し、$D \le i + \min(C, a_L, a_{L+1}, \dots, a_i)$ を満たす $i \in [L, R-1]$ の個数をカウントし、各クエリに対する答えを配列で返す。
	 *
	 * <h3>仕様・契約</h3>
	 * <ul>
	 *   <li><b>事前条件:</b>
	 *     <ul>
	 *       <li>$0 \le L \le R \le N$</li>
	 *       <li>$0 \le a[i] < N$ ($0 \le i < N$)</li>
	 *       <li>クエリの形式は各行 $\{L, R, C, D\}$</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>計算量:</b>
	 *     <ul>
	 *       <li>時間計算量: $O((N + Q) \log N)$</li>
	 *       <li>空間計算量: $O(N + Q)$</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param a 長さ $N$ の整数列
	 * @param queries クエリの二次元配列
	 * @return 各クエリの条件を満たす要素数
	 */
	@SuppressWarnings("unchecked")
	public static long[] countPrefixMinWithThresholdAndIndexShift(long[] a, long[][] queries) {
		int N = a.length;
		int Q = queries.length;
	
		MinSeg minSeg = new MinSeg(N);
		for (int i = 0; i < N; i++) {
			minSeg.set(i, a[i]);
		}
	
		int maxVal = 2 * N + 5;
		RangeAddRangeSum seg = SegTreeFactory.add_sum(maxVal);
	
		class Query {
			int id;
			long D;
			boolean isSub;
			Query(int id, long D, boolean isSub) {
				this.id = id;
				this.D = Math.max(0L, Math.min(maxVal - 2, D)); // クランプ
				this.isSub = isSub;
			}
		}
	
		List<Query>[] qActive = new List[N];
		for (int i = 0; i < N; i++) qActive[i] = new ArrayList<>();
	
		long[] ans = new long[Q];

		for (int q = 0; q < Q; q++) {
			int L = (int)queries[q][0];
			int R = (int)queries[q][1];
			long C = queries[q][2];
			long D = queries[q][3];
			if (L >= R) {
				continue;
			}

			int J = Math.min(R, minSeg.firstLeqPos(L, C));
			ans[q] += Math.max(0L, (long)J - Math.max((long)L, D - C));

			if (J < R) {
				qActive[J].add(new Query(q, D, false));
				if (R < N) {
					long C_fold = minSeg.fold(L, R);
					long C_new = Math.min(C, C_fold);
					int J_sub = Math.min(N, minSeg.firstLeqPos(R, C_new));
					ans[q] -= Math.max(0L, (long)J_sub - Math.max((long)R, D - C_new));
					if (J_sub < N) {
						qActive[J_sub].add(new Query(q, D, true));
					}
				}
			}
		}
		
		class Element {
			int l, r;
			long val;
			Element(int l, int r, long val) {
				this.l = l;
				this.r = r;
				this.val = val;
			}
		}
		ObjectDeque<Element> stack = new ObjectDeque<>();
	
		for (int L = N - 1; L >= 0; L--) {
			// i = L の追加
			seg.add((int)(a[L] + L), (int)(a[L] + L + 1), 1L);
	
			int lastR = L;
			while (!stack.isEmpty() && stack.peekFirst().val > a[L]) {
				Element top = stack.pollFirst();
				// [top.val + top.l, top.val + top.r] から 1 を引く
				seg.add((int)(top.val + top.l), (int)(top.val + top.r + 1), -1L);
				// [a[L] + top.l, a[L] + top.r] に 1 を足す
				seg.add((int)(a[L] + top.l), (int)(a[L] + top.r + 1), 1L);
				lastR = top.r;
			}
			stack.addFirst(new Element(L, lastR, a[L]));

			for (Query q : qActive[L]) {
				long D = q.D;
				long count = seg.sum((int)D, maxVal);
				if (q.isSub) {
					ans[q.id] -= count;
				} else {
					ans[q.id] += count;
				}
			}
		}
	
		return ans;
	}

	/**
	 * 未テスト
	 * 各クエリ $(L, R, C, D)$ に対し、$D \le i + \min(C, a_L, a_{L+1}, \dots, a_i)$ を満たす $i \in [L, R-1]$ の個数をカウントし、各クエリに対する答えを配列で返す。
	 *
	 * <h3>仕様・契約</h3>
	 * <ul>
	 *   <li><b>事前条件:</b>
	 *     <ul>
	 *       <li>$0 \le L \le R \le N$</li>
	 *       <li>$0 \le a[i] < N$ ($0 \le i < N$)</li>
	 *       <li>クエリの形式は各要素が $\{L, R, C, D\}$ であるリスト</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>計算量:</b>
	 *     <ul>
	 *       <li>時間計算量: $O((N + Q) \log N)$</li>
	 *       <li>空間計算量: $O(N + Q)$</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param a 長さ $N$ の整数列
	 * @param queries クエリのリスト
	 * @return 各クエリの条件を満たす要素数
	 */
	public static long[] countPrefixMinWithThresholdAndIndexShift(long[] a, List<long[]> queries) {
		//https://atcoder.jp/contests/abc248/submissions/77553482
		int Q = queries.size();
		long[][] qArr = new long[Q][];
		for (int i = 0; i < Q; i++) {
			qArr[i] = queries.get(i);
		}
		return countPrefixMinWithThresholdAndIndexShift(a, qArr);
	}

}
