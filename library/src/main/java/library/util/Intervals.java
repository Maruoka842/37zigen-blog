package library.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import library.util.segtree.SegTreeFactory;
import library.util.seq.SortedArrays;

public class Intervals{
	/**
	 * 半開区間[L, R)についてL[i] <= L[j] < R[i] <= R[j] となるi,jのunordered pairの個数
	 * 0 <= L[i], R[i] <= 2Nとする。
	 * @param L
	 * @param R
	 */
	public static long countCross(int[] L, int[] R) {
		//https://atcoder.jp/contests/abc338/submissions/73848223
		int N=L.length;
		long cross = 0;
		var seg=SegTreeFactory.sum(2*N+10);
    	ArrayUtils.sort(L, R);
    	for (int i = 0; i < N; i++) {
			long add=seg.fold(L[i]+1, R[i]+1);
			cross+=add;
			seg.mul(R[i], 1L);
		}
    	return cross;
	}
	
	public static boolean isMonge(long[][] a) {
		for (int i = 1; i < a.length; i++) {
			for (int j = i+1; j < a[i].length; j++) {
				long x=a[i][j]+a[i-1][j-1];
				long y=a[i-1][j]+a[i][j-1];
				if(x>y)return false;
			}
		}
		return true;
	}
	
	/**
	 * d[i] = min_{0 <= j < i} (d[j] + f.cost(j, i)) としたときの d[N] を返す。
	 * O(N^2)
	 * @param f
	 * @param n
	 * @return
	 */
	public static long minPartitionNaive(Cost f, int n) {
		long[]d=new long[n+1];
		long INF=Long.MAX_VALUE/3;
		Arrays.fill(d, INF);
		d[0]=0;
		for (int i = 1; i < d.length; i++) {
			for (int j = 0; j < i; j++) {
				d[i]=Math.min(d[i], d[j]+f.cost(j, i));
			}
		}
		return d[n];
	}
	
	static Cost costWithPenalty(Cost f, long penalty) {
		return new Intervals.Cost() {
			
			@Override
			public long cost(int i, int j) {
				return f.cost(i, j)+penalty;
			}
		};

	}
	
	/**
	 * 0 ≤ i < j ≤ nにおいて定義されたMongeな関数c(i,j):=f.cost(i,j)を受け取り、
	 * d[0][0]=0
	 * d[i][j]=min_{i' < i} d[k][j-1]+c(i', i) 
	 * を計算してd[k][n]を返す。
	 * @param f
	 * @param n
	 * @return
	 * @see https://atcoder.jp/contests/abc355/submissions/72475463
	 */
	public static long minKPartitionOfMongeCost(Cost f, int n, int k, long absBoundOfCost) {
        long l=-3*absBoundOfCost;
        long r=3*absBoundOfCost;
        long x0=l;
        long x1=l+1;
        long x2=l+2;
        long x3=l+3;
        //F[i]=F[i-1]+F[i-2]
        //F[0]=F[1]=1
        //としたとき、(x1-x0,x2-x1,x3-x2)=(F[k+1],F[k],F[k+1])となるように取る。
        while(x3<r) {
        	long F1=x1-x0;
        	long F0=x2-x1;
        	long F2=F0+F1;
        	x1=l+F2;
        	x2=x1+F1;
        	x3=x2+F2;
        }
        long v0=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x0), n)-x0*k;
        long v1=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x1), n)-x1*k;
        long v2=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x2), n)-x2*k;
        long v3=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x3), n)-x3*k;
        while(x3-x0>3) {
        	//最大値をフィボナッチ探索
        	//https://qiita.com/tanaka-a/items/f380257328da421c6584
        	//2分探索に比べて計算量 log φ / (2log(3/2)) = 0.42 倍
        	if(v1<=v2) {
        		x0=x1;
        		v0=v1;
        		x1=x2;
        		v1=v2;
        		x2=x3-(x1-x0);
        		v2=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x2), n)-x2*k;
        	} else {
        		x3=x2;
        		v3=v2;
        		x2=x1;
        		v2=v1;
        		x1=x0+(x3-x2);
        		v1=Intervals.minPartitionOfMongeCost(costWithPenalty(f, x1), n)-x1*k;
        	}
        }
        return MathUtils.max(v0,v1,v2,v3);
	}

	
	
	
	
	/**
	 * 0 ≤ i < j ≤ nにおいて定義されたMongeな関数c(i,j):=f.cost(i,j)を受け取り、
	 * d[0]=0
	 * d[i]=min_{k < i} d[k]+c(k, i) 
	 * を計算してd[n]を返す。
	 * @param f
	 * @param n
	 * @return
	 * @see https://yukicoder.me/submissions/1144894
	 * @see Galil, Zvi, and Raffaele Giancarlo. "Speeding up dynamic programming with applications to molecular biology." Theoretical Computer Science 64.1 (1989): 107-118.
	 */
	public static long minPartitionOfMongeCost(Cost f, int n) {
		long[]d=new long[n+1];
		
		// g(k, i) := d[k]+c(k , i) とすると
		// k < l の下で g(k, x) - g(l, x) は x の単調増加関数。
		// なぜなら
		// c(k, x) - c(l, x) ≤ c(k, x+1) - c(l, x+1)
		// ⇔ c(k, x) + c(l, x+1)  ≤ c(l, x) + c(k, x+1) - 
		// が Monge の定義より成り立つから。
		// 従って、lower envelope を管理する CHT と同様の構造を持つ。
		
		int[][]deque=new int[2][n];//交点のx座標のceil, 交点の左側を担当する g(k, i) の k
		int s=0;
		int t=1;
		int INFX=n+1;
		deque[0][0] = INFX;//右側に直線がない場合、右側の交点のx座標は∞と置く。
		for (int i = 1; i <= n; i++) {
			while(deque[0][s] <= i) {
				s++;
			}
			d[i]=d[deque[1][s]]+f.cost(deque[1][s], i);
			if(i==n)break;
			do {
				int ok=n+1;
				int ng=i;
				while(Math.abs(ok-ng)!=1) {
					int mid=(ok+ng)/2;
					if(d[deque[1][t-1]]+f.cost(deque[1][t-1], mid) >= d[i]+f.cost(i, mid)) {
						ok=mid;
					} else {
						ng=mid;
					}
				}
				if(t-s >= 2 && ok <= deque[0][t-2]) {
					t--;
				} else {
					deque[0][t - 1]	= ok;
					deque[0][t] = INFX;
					deque[1][t] = i;
					t++;
					break;
				}
			} while(true);
		}
		return d[n];
	}
	
	public interface Cost {
		public long cost(int i, int j);
	}

	public static Cost randomMongeFunction(int N) {
		long[][]a=ArrayUtils.randomLongTable(-10, 1, N, N);
		long[]b=ArrayUtils.randomLongArray(-10, 10, N);
		long[]c=ArrayUtils.randomLongArray(-10, 10, N);
		long[][]d=ArrayUtils.prefixSum(a);
		return new Cost() {
			
			@Override
			public long cost(int i, int j) {
				if(i>=j)return Long.MAX_VALUE/3;
				return d[i][j]+b[i]+c[j];
			}
		};
	}
	
	/**
	 * [l0, r0), [l1, r1) が共通部分を持つか
	 * @param l0
	 * @param r0
	 * @param l1
	 * @param r1
	 * @return
	 */
	public static boolean hasOverlap(long l0, long r0, long l1, long r1) {
		return !(r0 <= l1 || r1 <= l0);
	}
	
	/**
	 * 重み付き区間 {@code [L[i], R[i])} から互いに重ならないものを選ぶときの重み和の最大値を返す。
	 *
	 * <p>区間は半開区間として扱い、{@code R[i] <= L[j]} なら重ならない。
	 * 空集合も選べるため、返り値は 0 以上になる。
	 *
	 * <p>計算量: {@code O(n log n)}
	 * 未テスト
	 * @param L 各区間の左端
	 * @param R 各区間の右端
	 * @param W 各区間の重み
	 * @return 重ならない区間集合の重み和の最大値
	 */
	public static long weightedIntervalScheduling(long[] L, long[] R, long[] W) {
		if(L.length != R.length || L.length != W.length)throw new AssertionError();
		int n=L.length;
		long[][] a=new long[n][3];
		for (int i = 0; i < n; i++) {
			a[i][0]=L[i];
			a[i][1]=R[i];
			a[i][2]=W[i];
		}
		Arrays.sort(a, (x, y)->{
			int c=Long.compare(x[1], y[1]);
			if(c!=0)return c;
			return Long.compare(x[0], y[0]);
		});
		long[] right=new long[n];
		for (int i = 0; i < n; i++) {
			right[i]=a[i][1];
		}
		long[] dp=new long[n+1];
		for (int i = 0; i < n; i++) {
			int p=Math.min(i, SortedArrays.higher(right, a[i][0]));
			dp[i+1]=Math.max(dp[i], dp[p]+a[i][2]);
		}
		return dp[n];
	}
	
	/**
	 * 2つの区間列 {@code X}, {@code Y} の共通部分を列挙する。
	 *
	 * <p>各区間は {@code long[]} によって表され、{@code [a[0], a[1])} という
	 * 半開区間として扱う。すなわち、左端を含み、右端は含まない。</p>
	 *
	 * <p>返り値には、{@code X} のいずれかの区間と {@code Y} のいずれかの区間が
	 * 正の長さで重なる部分区間 {@code [lo, hi)} が、走査順に格納される。
	 * 端点だけが一致する場合、例えば {@code [1, 3)} と {@code [3, 5)} は
	 * 重なっていないものとして扱われる。</p>
	 *
	 * <h3>前提条件</h3>
	 * <ul>
	 *   <li>{@code X}, {@code Y} は {@code null} でない。</li>
	 *   <li>各要素 {@code a} は長さ2以上の {@code long[]} である。</li>
	 *   <li>各区間は空でなく、{@code a[0] < a[1]} を満たす。</li>
	 *   <li>各リスト内の区間は左端の昇順に並んでいる。</li>
	 *   <li>通常は、各リスト内の区間同士は互いに重ならないことを想定している。</li>
	 * </ul>
	 *
	 * <p>これらの前提を満たさない場合、返り値は未定義であり、
	 * 特に空区間 {@code [a, a)} が含まれると正しく終了しない可能性がある。</p>
	 *
	 * @param X 1つ目の区間列
	 * @param Y 2つ目の区間列
	 * @return {@code X} と {@code Y} の共通部分を表す半開区間のリスト
	 *
	 * @implNote 入力リストおよび入力区間配列は変更しない。
	 *           返り値の各区間は新しい {@code long[]} として生成される。
	 *           計算量は {@code O(X.size() + Y.size())}。
	 */
	public static ArrayList<long[]> overlaps(ArrayList<long[]> X, ArrayList<long[]> Y) {
		ArrayList<long[]> ret=new ArrayList<long[]>();
		int i=0;
		int j=0;
		while(i<X.size() && j<Y.size()) {
			while(j < Y.size() && Y.get(j)[1] <= X.get(i)[0]) ++j;
			if(j==Y.size())break;
			if(i==X.size())break;
			if(X.get(i)[1] <= Y.get(j)[0]) {
				++i;
				continue;
			}
			long lo=Math.max(X.get(i)[0], Y.get(j)[0]);
			long hi=Math.min(X.get(i)[1], Y.get(j)[1]);
			if(lo==hi)continue;
			ret.add(new long[] {lo, hi});
			if(Y.get(j)[0] <= X.get(i)[0] && X.get(i)[1] <= Y.get(j)[1]) {
				++i;
				continue;
			}
			if(X.get(i)[0] <= Y.get(j)[0] && Y.get(j)[1] <= X.get(i)[1]) {
				++j;
				continue;
			}
			if(X.get(i)[0] <= Y.get(j)[0])++i;
			else ++j;
		}
		return ret;
	}
	
	/**
	 * 半開区間 {@code [a, b)} を表す区間列をマージする。
	 *
	 * <p>各区間は {@code new long[] {a, b}} の形で与えられ、
	 * {@code [0]} が左端 {@code a}、{@code [1]} が右端 {@code b} を表す。
	 *
	 * <p>このメソッドは入力リストを辞書順にソートしたうえで、
	 * 重なっている区間、または隙間なく接している区間を 1 つの区間にまとめる。
	 *
	 * <p>例えば {@code [1, 2)} と {@code [2, 3)} は隙間なく接しているため、
	 * {@code [1, 3)} にマージされる。
	 *
	 * <p>注意: このメソッドは入力リスト {@code list} をその場でソートするため、
	 * 引数の順序を保持しない。
	 *
	 * @param list 半開区間 {@code [a, b)} のリスト
	 * @return ソート後、重複または隙間なく接する区間をマージしたリスト
	 */
	public static ArrayList<long[]> merge(ArrayList<long[]> list) {
		ArrayList<long[]> ret=new ArrayList<long[]>();
		Collections.sort(list, (x, y) -> Arrays.compare(x, y));
		for (int i = 0; i < list.size(); i++) {
			long t=list.get(i)[1];
			int j=i;
			while(j+1<list.size() && hasOverlap(list.get(i)[0], t+1, list.get(j+1)[0], list.get(j+1)[1]+1)) {
				++j;
				t=Math.max(t, list.get(j)[1]);
			}
			ret.add(new long[] {list.get(i)[0], t});
			i=j;
		}
		
		return ret;
	}

	/**
	 * 与えられた半開区間の集合の中から、他の区間を部分集合として含まない極小な半開区間 (inclusion minimal intervals) のリストを返す。
	 *
	 * <p>半開区間 [A, B) が [C, D) に含まれる ([A, B) ⊆ [C, D)) とは、
	 * C ≤ A かつ B ≤ D であることを定義とする。
	 * [C, D) が別の区間 [A, B) を含むとき、[C, D) は極小ではないため破棄される。
	 * 入力に重複する区間が存在する場合、そのうち1つのみが極小な区間として残る。
	 * 空区間（L ≥ R）は極小な区間の計算から除外される。</p>
	 *
	 * @param list 半開区間 [L, R) を表す long[] のリスト。各要素は list.get(i)[0] = L, list.get(i)[1] = R。
	 * @return 極小な区間のリスト（L の昇順、等しい場合は R の昇順にソートされている）
	 * @complexity O(N \log N)
	 */
	// 未テスト
	public static ArrayList<long[]> inclusionMinimalIntervals(ArrayList<long[]> list) {
		if (list == null) return new ArrayList<>();
		ArrayList<long[]> valid = new ArrayList<>();
		for (long[] interval : list) {
			if (interval != null && interval.length >= 2 && interval[0] < interval[1]) {
				valid.add(interval);
			}
		}
		valid.sort((x, y) -> {
			int cmp = Long.compare(x[1], y[1]); // Rの昇順
			if (cmp != 0) return cmp;
			return Long.compare(y[0], x[0]); // Lの降順
		});
		ArrayList<long[]> temp = new ArrayList<>();
		boolean hasMaxL = false;
		long maxL = 0;
		for (int i = 0; i < valid.size(); i++) {
			long[] interval = valid.get(i);
			if (!hasMaxL || interval[0] > maxL) {
				temp.add(interval);
				maxL = interval[0];
				hasMaxL = true;
			}
		}
		return temp;
	}

	/**
	 * 与えられた半開区間の集合 [L[i], R[i]) のうち、包含関係に関して極小な半開区間 (inclusion minimal intervals) のリストを返す。
	 *
	 * <p>半開区間 [A, B) が [C, D) に含まれる ([A, B) ⊆ [C, D)) とは、
	 * C ≤ A かつ B ≤ D であることを定義とする。
	 * [C, D) が別の区間 [A, B) を含むとき、[C, D) は極小ではないため破棄される。
	 * 入力に重複する区間が存在する場合、そのうち1つのみが極小な区間として残る。
	 * 空区間（L ≥ R）は極小な区間の計算から除外される。</p>
	 *
	 * @param L 各区間の左端
	 * @param R 各区間の右端
	 * @return 極小な区間のリスト
	 * @complexity O(N \log N)
	 */
	// 未テスト
	public static ArrayList<long[]> inclusionMinimalIntervals(long[] L, long[] R) {
		if (L == null || R == null) return new ArrayList<>();
		if (L.length != R.length) {
			throw new IllegalArgumentException("L and R must have the same length.");
		}
		int n = L.length;
		ArrayList<long[]> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			list.add(new long[]{L[i], R[i]});
		}
		return inclusionMinimalIntervals(list);
	}

	/**
	 * 与えられた半開区間の集合 [L[i], R[i]) のうち、包含関係に関して極小な半開区間 (inclusion minimal intervals) のリストを返す。
	 *
	 * <p>半開区間 [A, B) が [C, D) に含まれる ([A, B) ⊆ [C, D)) とは、
	 * C ≤ A かつ B ≤ D であることを定義とする。
	 * [C, D) が別の区間 [A, B) を含むとき、[C, D) は極小ではないため破棄される。
	 * 入力に重複する区間が存在する場合、そのうち1つのみが極小な区間として残る。
	 * 空区間（L ≥ R）は極小な区間の計算から除外される。</p>
	 *
	 * @param L 各区間の左端
	 * @param R 各区間の右端
	 * @return 極小な区間のリスト
	 * @complexity O(N \log N)
	 */
	// 未テスト
	public static ArrayList<long[]> inclusionMinimalIntervals(int[] L, int[] R) {
		if (L == null || R == null) return new ArrayList<>();
		if (L.length != R.length) {
			throw new IllegalArgumentException("L and R must have the same length.");
		}
		int n = L.length;
		ArrayList<long[]> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			list.add(new long[]{L[i], R[i]});
		}
		return inclusionMinimalIntervals(list);
	}
	
	
	/**
	 * 半開区間 {@code [L[i], R[i])} のうち、共通部分を持つ非順序対 {i, j} (i ≠ j) の個数を返す。
	 *
	 * <p>共通部分を持つとは、{@code [L[i], R[i]) ∩ [L[j], R[j]) ≠ ∅} であることを指す。
	 * 空区間 ({@code L[i] >= R[i]}) は無視される。
	 *
	 * <p>計算量: O(n log n)
	 * @param L 各区間の左端
	 * @param R 各区間の右端
	 * @return 共通部分を持つ対の個数
	 */
	public static long countOverlappedPairs(long[] L, long[] R) {
		int n = L.length;
		int count = 0;
		for (int i = 0; i < n; i++) if (L[i] < R[i]) count++;
		if (count <= 1) return 0;
		long[] l = new long[count];
		long[] r = new long[count];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			if (L[i] < R[i]) {
				l[idx] = L[i];
				r[idx] = R[i];
				idx++;
			}
		}
		Arrays.sort(l);
		Arrays.sort(r);
		long nonOverlapped = 0;
		int rIdx = 0;
		for (int i = 0; i < count; i++) {
			while (rIdx < count && r[rIdx] <= l[i]) rIdx++;
			nonOverlapped += rIdx;
		}
		return (long) count * (count - 1) / 2 - nonOverlapped;
	}

	/**
	 * 半開区間 {@code [L[i], R[i])} の全非順序対 {i, j} (i ≠ j) について、共通部分の長さの総和を返す。
	 *
	 * <p>共通部分の長さとは、{@code length([L[i], R[i]) ∩ [L[j], R[j]))} である。
	 * 空区間 ({@code L[i] >= R[i]}) は無視される。
	 *
	 * <p>計算量: O(n log n)
	 * @param L 各区間の左端
	 * @param R 各区間の右端
	 * @return 共通部分の長さの総和
	 */
	public static long sumOverlappedLength(long[] L, long[] R) {
		int n = L.length;
		int count = 0;
		for (int i = 0; i < n; i++) if (L[i] < R[i]) count++;
		if (count <= 1) return 0;

		long[] eventCoords = new long[2 * count];
		int[] eventTypes = new int[2 * count];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			if (L[i] < R[i]) {
				eventCoords[idx] = L[i];
				eventTypes[idx] = 1; // Start
				idx++;
				eventCoords[idx] = R[i];
				eventTypes[idx] = -1; // End
				idx++;
			}
		}
		ArrayUtils.sortByKeyStable(eventCoords, eventTypes);

		long totalSum = 0;
		long activeIntervals = 0;
		for (int i = 0; i < 2 * count - 1; i++) {
			activeIntervals += eventTypes[i];
			long segmentLength = eventCoords[i+1] - eventCoords[i];
			if (segmentLength > 0) {
				totalSum += activeIntervals * (activeIntervals - 1) / 2 * segmentLength;
			}
		}
		return totalSum;
	}
	
	
	
	
    /**
     * C[i][j] = d(i, j) + min_{i < k < j} (C[i][k] + C[k][j])
     *
     * base:
     *   C[i][i+1] = 0
     *
     * indices:
     *   0 <= i < j <= N
     *
     * Knuth optimization が成立する前提。
     */
    public static long[][] intervalDP(int N, Cost d) {
    	//https://atcoder.jp/contests/awc0100/submissions/76969842
        final long INF = Long.MAX_VALUE / 4;

        long[][] C = new long[N + 1][N + 1];
        for (int i = 0; i <= N; i++) {
            Arrays.fill(C[i], INF);
        }

        // K[i] は直前の対角線、つまり現在 len を計算するときは
        // K[i] = opt[i][i + len - 1]
        int[] K = new int[N];

        // 長さ 1 の区間を base とする
        for (int i = 0; i < N; i++) {
            C[i][i + 1] = 0;
            K[i] = i + 1;
        }

        for (int len = 2; len <= N; len++) {
            int[] newK = new int[N + 1 - len];

            for (int i = 0; i + len <= N; i++) {
                int j = i + len;

                int p = K[i];       // opt[i][j - 1]
                int q = K[i + 1];   // opt[i + 1][j]

                // strict: i < k < j
                if (p < i + 1) p = i + 1;
                if (q > j - 1) q = j - 1;

                long best = INF;
                int bestK = p;

                for (int k = p; k <= q; k++) {
                    long v = C[i][k] + C[k][j];
                    if (v < best) {
                        best = v;
                        bestK = k;
                    }
                }

                C[i][j] = best + d.cost(i, j);
                newK[i] = bestK;
            }

            K = newK;
        }

        return C;
    }

	/**
	 * [0, N) から 互いに隣接しない (non-adjacent) 半開区間 [a_{2i}, a_{2i+1}) を選ぶ。
	 * ここで、選び方 S の重みは、各区間 w の重み f(w) の積 prod_{w in S} f(w) と定義される。
	 *
	 * <p>隣接しないとは、すべての i に対して a_{2i+1} < a_{2i+2} を満たすことを意味する。</p>
	 *
	 * <p>計算量: O(N^2) 回の半環（SemiRing）演算。</p>
	 * // 未テスト
	 *
	 * @param <T> 区間重みの代数的構造の型
	 * @param N 区間の右端の最大値
	 * @param f 各区間 [L, R) に対する重みを返す関数 (L < R)
	 * @param sr 半環のストラテジー（和・積・単位元を定義する）
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T sumOfNonAdjacentIntervalProducts(
			int N,
			java.util.function.BiFunction<Integer, Integer, T> f,
			library.util.algebra.strategy.SemiRingStrategy<T> sr
	) {
		T[] dp = (T[]) new Object[N + 2];
		for (int i = 0; i <= N + 1; i++) {
			dp[i] = sr.one();
		}
		//dp[i] = [0, i-1) まで選んだ
		for (int i = 1; i <= N; i++) {
			T sum = dp[i];
			for (int L = 0; L < i; L++) {
				// [L, i) を選ぶ
				T val = f.apply(L, i);
				T term = sr.mul(dp[L], val);
				sum = sr.add(sum, term);
			}
			dp[i + 1] = sum;
		}
		return dp[N + 1];
	}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}


}
