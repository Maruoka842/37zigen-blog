package library.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import library.util.collections.IntArrayList;
import library.util.collections.LongArrayList;
import library.util.geometry.GeometryUtils;
import library.util.geometry.LongPoint;
import library.util.poset.BooleanLattice;

public class Knapsack{
	/**
	 * 各品物の重さが小さい場合の個数制限付きナップサック。
	 * 品物 {@code i} は高々 {@code nums[i]} 個まで使うことができ、総重量が {@code totalWeightBound} 以下となるときの最大価値を返す。
	 * M = max(weights) として、時間計算量は O(N log N + M^3)。
	 *
	 * @param weights 各品物の重量
	 * @param values 各品物の価値
	 * @param nums 各品物を使える個数の上限
	 * @param totalWeightBound 総重量の上限
	 * @return 総重量が {@code totalWeightBound} 以下となるように品物を選んだときの最大価値
	 * https://x.com/noshi91/status/2015082109309006147
	 * https://atcoder.jp/contests/abc442/submissions/72742510
	 */
	public static long knapsackSmallWeights(int[] weights, long[] values, long[] nums, long totalWeightBound) {
		int N=weights.length;
		int maxW = ArrayUtils.max(weights);
		long[][] items=new long[N][];
		for (int i = 0; i < N; i++) {
			items[i]=new long[] {weights[i], values[i], nums[i]};
		}
		Arrays.sort(items, (x, y)-> -FractionUtils.compareFraction(x[1], x[0], y[1], y[0]));//価値/重みの降順
		ArrayList<long[]> greedyList=new ArrayList<>();
		long greedyWeight = 0;
		long greedyAns = 0;
		//貪欲法
		for (int i = 0; i < N; i++) {
			long m = Math.min((totalWeightBound - greedyWeight) / items[i][0], items[i][2]);
			if (m == 0) {
				break;
			} else {
				var a=ArrayUtils.copy(items[i]);
				items[i][2] -= m;
				a[2] = m;
				greedyList.add(a);
				greedyAns += m * items[i][1];
				greedyWeight += m * items[i][0];
			}
		}
		Collections.sort(greedyList, (x, y)->Long.compare(x[1], y[1]));//価値の昇順にソート
		int[]cnt=new int[maxW+1];
		int[] weights2 = new int[maxW * (2 * maxW + 1)];//01Knapsakでまじめに計算するアイテム
		long[] values2 = new long[maxW * (2 * maxW + 1)];//01Knapsakでまじめに計算するアイテム
		int size = 0;
		//貪欲解から除外するアイテムの候補を価値が低い順に選ぶ
		for (int i = 0; i < greedyList.size(); i++) {
			int w=(int)greedyList.get(i)[0];
			long value=greedyList.get(i)[1];
			long num=greedyList.get(i)[2];
			int m=(int)Math.min(num, 2*maxW+1-cnt[w]);//貪欲解から高々2*maxW+1個しか除外されない
			for (int j = 0; j < m; j++) {
				weights2[size] = w;
				values2[size] = value;
				size++;
			}
			cnt[w]+=m;
		}
		weights2 = Arrays.copyOf(weights2, size);
		values2 = Arrays.copyOf(values2, size);
		int[] weights3 = new int[maxW * (2 * maxW + 1)];//01Knapsakでまじめに計算するアイテム
		long[] values3 = new long[maxW * (2 * maxW + 1)];//01Knapsakでまじめに計算するアイテム
		size = 0;
		Arrays.fill(cnt, 0);
		Arrays.sort(items, (x, y)-> -Long.compare(x[1], y[1]));//価値の降順
		//貪欲解に選ばれなかったアイテムで最適解に選ばれる候補を選定
		for (int i = 0; i < items.length; i++) {
			int w=(int)items[i][0];
			long value=items[i][1];
			long num=items[i][2];
			if (w > totalWeightBound) continue;
			int m=(int)Math.min(num, 2*maxW+1-cnt[w]);//貪欲解不採用のアイテムから高々2*maxW+1個しか採用しない
			for (int j = 0; j < m; j++) {
				weights3[size] = w;
				values3[size] = value;
				size++;
			}
			cnt[w]+=m;
		}
		weights3 = Arrays.copyOf(weights3, size);
		values3 = Arrays.copyOf(values3, size);
		for (int i = 0; i < values2.length; i++) {
			values2[i]*=-1;
		}
		long[]dp0=knapsack01(weights2, values2, (int) ArrayUtils.sum(weights2));
		long[]dp1=knapsack01(weights3, values3, (int) ArrayUtils.sum(weights3));
		for (int i = dp0.length-1; i >= 1; i--) {
			dp0[i-1]=Math.max(dp0[i-1], dp0[i]);
		}
		for (int i = 0; i < dp1.length-1; i++) {
			dp1[i+1]=Math.max(dp1[i+1], dp1[i]);
		}
		long ans=greedyAns;
		for (int i = 0; i < dp0.length; i++) {
			//-i + j + greedyWeight = totalWeightBound
			long j = i + totalWeightBound - greedyWeight;
			j = Math.max(j, 0);
			if (j < dp1.length) {
				ans=Math.max(ans, greedyAns+dp0[i]+dp1[(int)j]);
			}
		}
		return ans;
	}

	
	
	
	/**
	 * 0/1 ナップサック。　w ≤ {@code maxWeight} について、重量wとなるように品物を選んだときの最大価値を返す。
	 * O({@code O(N * maxWeight)})
	 *
	 * @param weights 各品物の重量
	 * @param values  各品物の価値
	 * @param maxWeight 総重量の上限
	 * @return 長さ {@code maxWeight + 1} の配列 {@code dp}。{@code dp[w]} は総重量がちょうど {@code w} のときの最大価値
	 */
	public static long[] knapsack01Naive(int[] weights, long[] values, int maxWeight) {
		long[]dp=new long[maxWeight+1];
		long INF=Long.MAX_VALUE/3;
		Arrays.fill(dp, -INF);
		dp[0]=0;
		int N=weights.length;
		for (int i = 0; i < N; i++) {
			for (int w = maxWeight - weights[i]; w >= 0; w--) {
				if(dp[w]==-INF)continue;
				int nw=w+weights[i];
				dp[nw]=Math.max(dp[nw], dp[w]+values[i]);
			}
		}
		return dp;
	}
	
	/**
	 * 0/1 ナップサック。
	 * 添字 {@code i} 以降の品物だけを使い、空集合から始めたときのDPテーブルを返す。
	 * 時間計算量・空間計算量は O(N * maxWeight)。
	 *
	 * @param weights 各品物の重量
	 * @param values 各品物の価値
	 * @param maxWeight 総重量の上限
	 * @return {@code dp[i][w]} は {@code i} 番目以降の品物から総重量がちょうど {@code w} となるように選んだときの最大価値
	 */
	public static long[][] suffixKnapsack01FromEmpty(int[] weights, long[] values, int maxWeight) {
		//https://atcoder.jp/contests/abc441/submissions/72568320
		int N=weights.length;
		long[][] dp = new long[N + 1][maxWeight + 1];
        dp[N][0] = 0;
        for (int i = N - 1; i >= 0; i--) {
        	dp[i] = dp[i + 1].clone();
            for (int j = 0; (j + weights[i]) <= maxWeight; j++) {
                long nv = dp[i + 1][j] + values[i];
                int np = j + weights[i];
                if (nv > dp[i][np]) {
                	dp[i][np] = nv;
                }
            }
        }
		return dp;
	}

	
	/**
	 * 0/1 ナップサック。
	 * 先頭 {@code i} 個の品物だけを使い、空集合から始めたときのDPテーブルを返す。
	 * 時間計算量・空間計算量は O(N * maxWeight)。
	 *
	 * @param weights 各品物の重量
	 * @param values 各品物の価値
	 * @param maxWeight 総重量の上限
	 * @return {@code dp[i][w]} は先頭 {@code i} 個の品物から総重量がちょうど {@code w} となるように選んだときの最大価値
	 */
	public static long[][] prefixKnapsack01FromEmpty(int[] weights, long[] values, int maxWeight) {
		int N=weights.length;
		long[][] dp = new long[N + 1][maxWeight + 1];

        dp[0][0] = 0;
        for (int i = 0; i < N; i++) {
            dp[i + 1] = dp[i].clone();
            for (int j = 0; j + weights[i] <= maxWeight; j++) {
                long nv = dp[i][j] + values[i];
                int np = j + weights[i];
                if (nv > dp[i + 1][np]) {
                    dp[i + 1][np] = nv;
                }
            }
        }
		return dp;
	}
	
	/**
	 * O(N log N + maxWeight * (重さの種類数))
	 * 重さ0がある場合が未実装
	 * @param weights
	 * @param values
	 * @param maxWeight
	 * @return
	 */
	public static long[] knapsack01(int[] weights, long[] values, int maxWeight) {
		//https://atcoder.jp/contests/abc373/submissions/71711193
		int N=weights.length;
		if(weights.length!=values.length)throw new AssertionError();
		long[]dp=new long[maxWeight+1];
		long INF=Long.MAX_VALUE/3;
		Arrays.fill(dp, -INF);
		dp[0]=0;
		Integer[] order=new Integer[N];
		Arrays.setAll(order, i->i);
		Arrays.sort(order, (i, j)->{
			int comp=Integer.compare(weights[i], weights[j]);
			if(comp!=0)return comp;
			return -Long.compare(values[i], values[j]);
		});
		for (int i = 0; i < N; i++) {
			int j=i;
			while(j+1<N && weights[order[i]]==weights[order[j+1]])++j;
			int m=j-i+1;
			int w=weights[order[i]];
			// k*w<=maxW
			// k <= maxW/w
			long[]f=new long[1 + Math.min(m, maxWeight / w)];
			for (int k = 0; k < f.length - 1; k++) {
				f[k + 1] = f[k] + values[order[i + k]];
			}
			
			for (int res = 0; res < w; res++) {
				//res+iw <= f.length-1
				long[] dp2=new long[(dp.length+w-1)/w];
				for (int k = 0; res+w*k < dp.length; k++) {
					dp2[k]=dp[res+w*k];
				}
				dp2=MaxPlus.convolveConcaveAndArbitrary(f, dp2);
				for (int k = 0; res+w*k < dp.length; k++) {
					dp[res+w*k]=dp2[k];
				}
			}
			
			i=j;
		}
		return dp;
	}

	/**
	 * 各要素を何回でも使える部分和問題。
	 * 和が {@code x} となる使い方が存在する場合は、使う要素数が最小となるように各要素を使う回数を並べた配列を返す。
	 * 存在しない場合はnullを返す。
	 * u = max(a) として、Chan--He の all-targets change-making のアイディアにより、
	 * {@code x >= u^2} なら最大要素を使って {@code x < u^2} まで落としてから、
	 * 各和 {@code j} について値が大きい方から高々 {@code ceil(2u^2 / j)} 種類だけを見るDPを行う。
	 * L = min(x, u^2) として、時間計算量 O(u^2 log u)、空間計算量 O(L)。
	 * 未テスト。
	 * https://arxiv.org/pdf/2110.02503
	 * @param a 各要素の値。すべて正である必要がある
	 * @param x 作りたい和
	 * @return {@code ret[i]} は {@code a[i]} を使う回数
	 */
	public static long[] subsetSumUnbounded(int[] a, long x) {
		if(x<0)throw new AssertionError();
		if(ArrayUtils.min(a)<=0)throw new AssertionError();
		int N=a.length;
		if(N==0)return x==0?new long[0]:null;
		long[]ret=new long[N];
		int maxIndex=ArrayUtils.argMax(a);
		int u=a[maxIndex];
		// a[maxIndex] 以外の要素で、合計u^2以上ならば、鳩ノ巣原理より uの倍数の部分集合が取れる。
		// 個数最小ではそのようなことが起きない。
		long threshold=MathUtils.saturatingMul(u, u);
		long target=x;
		if(target>=threshold) {
			long maxCount=Math.ceilDiv(target-threshold+1, u);
			ret[maxIndex]+=maxCount;
			target-=maxCount*u;
		}
		if(target>Integer.MAX_VALUE)throw new AssertionError();
		int X=(int)target;
		int[]ord=ArrayUtils.argRSort(a);
		IntArrayList index=new IntArrayList(N);
		for (int oi:ord) {
			if(index.isNonEmpty()&&a[index.peekLast()]==a[oi])continue;
			index.add(oi);
		}
		int INF=Integer.MAX_VALUE/3;
		int[]dp=new int[X+1];
		int[]item=new int[X+1];
		Arrays.fill(dp, INF);
		Arrays.fill(item, -1);
		dp[0]=0;
		long twoUSquared=MathUtils.saturatingMul(2, threshold);
		for (int j = 1; j <= X; j++) {
			// Chan--He の3行DP。Σ_j ceil(2u^2/j) が調和級数で O(u^2 log u) になる。
			int limit=(int)Math.min(index.size(), (twoUSquared+j-1)/j);
			for (int i = 0; i < limit; i++) {
				int k=index.get(i);
				if(a[k]>j)continue;
				int pre=j-a[k];
				if(dp[pre]+1<dp[j]) {
					dp[j]=dp[pre]+1;
					item[j]=k;
				}
			}
		}
		if(dp[X]==INF)return null;
		int cur=X;
		while(cur!=0) {
			ret[item[cur]]++;
			cur-=a[item[cur]];
		}
		return ret;
	}

	/**
	 * 各二次元ベクトルを何回でも使える部分和問題。
	 * 和が {@code (targetX, targetY)} となる使い方が存在する場合は、使う要素数が最小となるように各要素を使う回数を並べた配列を返す。
	 * 存在しない場合はnullを返す。
	 * 時間計算量 O(a.length * targetX * targetY)、空間計算量 O(targetX * targetY)。
	 * 未テスト。
	 *
	 * @param a 各要素のベクトル。{@code a[i][0]}, {@code a[i][1]} は非負で、少なくとも一方は正である必要がある
	 * @param targetX 作りたい和の第1成分
	 * @param targetY 作りたい和の第2成分
	 * @return {@code ret[i]} は {@code a[i]} を使う回数
	 */
	public static long[] subsetSumUnbounded2D(int[][] a, long targetX, long targetY) {
		int N=a.length;
		int[]x=new int[N];
		int[]y=new int[N];
		for (int i = 0; i < N; i++) {
			if(a[i].length!=2)throw new AssertionError();
			x[i]=a[i][0];
			y[i]=a[i][1];
		}
		return subsetSumUnbounded2D(x, y, targetX, targetY);
	}

	/**
	 * 各二次元ベクトルを何回でも使える部分和問題。
	 * 和が {@code (targetX, targetY)} となる使い方が存在する場合は、使う要素数が最小となるように各要素を使う回数を並べた配列を返す。
	 * 存在しない場合はnullを返す。
	 * 時間計算量 O(x.length * targetX * targetY)、空間計算量 O(targetX * targetY)。
	 * 未テスト。
	 *
	 * @param x 各要素の第1成分
	 * @param y 各要素の第2成分
	 * @param targetX 作りたい和の第1成分
	 * @param targetY 作りたい和の第2成分
	 * @return {@code ret[i]} は {@code (x[i], y[i])} を使う回数
	 */
	public static long[] subsetSumUnbounded2D(int[] x, int[] y, long targetX, long targetY) {
		if(targetX<0||targetY<0)throw new AssertionError();
		if(targetX>Integer.MAX_VALUE||targetY>Integer.MAX_VALUE)throw new AssertionError();
		int N=x.length;
		if(y.length!=N)throw new AssertionError();
		if(N==0)return targetX==0&&targetY==0?new long[0]:null;
		for (int i = 0; i < N; i++) {
			if(x[i]<0||y[i]<0||x[i]==0&&y[i]==0)throw new AssertionError();
		}
		int X=(int)targetX;
		int Y=(int)targetY;
		int INF=Integer.MAX_VALUE/3;
		int[][]dp=new int[X+1][Y+1];
		int[][]prevX=new int[X+1][Y+1];
		int[][]prevY=new int[X+1][Y+1];
		int[][]item=new int[X+1][Y+1];
		ArrayUtils.fill(dp, INF);
		ArrayUtils.fill(prevX, -1);
		ArrayUtils.fill(prevY, -1);
		ArrayUtils.fill(item, -1);
		dp[0][0]=0;
		for (int i = 0; i < N; i++) {
			for (int sx = 0; sx+x[i] <= X; sx++) {
				for (int sy = 0; sy+y[i] <= Y; sy++) {
					int nx=sx+x[i];
					int ny=sy+y[i];
					if(dp[sx][sy]+1<dp[nx][ny]) {
						dp[nx][ny]=dp[sx][sy]+1;
						prevX[nx][ny]=sx;
						prevY[nx][ny]=sy;
						item[nx][ny]=i;
					}
				}
			}
		}
		if(dp[X][Y]==INF)return null;
		long[]ret=new long[N];
		int curX=X;
		int curY=Y;
		while(curX!=0||curY!=0) {
			int i=item[curX][curY];
			ret[i]++;
			int nx=prevX[curX][curY];
			int ny=prevY[curX][curY];
			curX=nx;
			curY=ny;
		}
		return ret;
	}

	/**
	 * 各二次元ベクトルを何回でも使える部分和問題。
	 * 凸包上でtarget方向に最も進む辺について、その2点で埋める前の残差を有限領域DPで求める。
	 * 和が {@code (targetX, targetY)} となる使い方が存在する場合は、使う要素数が最小となるように各要素を使う回数を並べた配列を返す。
	 * 存在しない場合はnullを返す。
	 * D = max(max(x), max(y)), B = min(max(targetX, targetY), D^3) として、時間計算量 O(x.length * B^2)、空間計算量 O(B^2)。
	 * 未テスト。未証明。2D(4D+1)^2なら証明がある（https://arxiv.org/pdf/1707.00481）
	 * @param x 各要素の第1成分
	 * @param y 各要素の第2成分
	 * @param targetX 作りたい和の第1成分
	 * @param targetY 作りたい和の第2成分
	 * @return {@code ret[i]} は {@code (x[i], y[i])} を使う回数
	 */
	public static long[] subsetSumUnbounded2DConvex(int[] x, int[] y, long targetX, long targetY) {
		if(targetX<0||targetY<0)throw new AssertionError();
		int N=x.length;
		if(y.length!=N)throw new AssertionError();
		if(N==0)return targetX==0&&targetY==0?new long[0]:null;
		if(targetX==0&&targetY==0)return new long[N];
		for (int i = 0; i < N; i++) {
			if(x[i]<0||y[i]<0||x[i]==0&&y[i]==0)throw new AssertionError();
		}
		if(targetX<=Integer.MAX_VALUE&&targetY<=Integer.MAX_VALUE&&targetX<=10000000L/Math.max(1, targetY))return subsetSumUnbounded2D(x, y, targetX, targetY);
		LongPoint[]points=new LongPoint[N];
		for (int i = 0; i < N; i++) {
			points[i]=new LongPoint(x[i], y[i]);
		}
		LongPoint[]hull=GeometryUtils.convexHull(points);
		if(hull.length<2)return subsetSumUnbounded2DCollinear(x, y, targetX, targetY);
		long bestNum=-1;
		long bestDen=1;
		long px=0;
		long py=0;
		long qx=0;
		long qy=0;
		int pIndex=-1;
		int qIndex=-1;
		for (int i = 0; i < hull.length; i++) {
			LongPoint p=hull[i];
			LongPoint q=hull[(i+1)%hull.length];
			long det=Math.abs(cross(p.x(), p.y(), q.x(), q.y()));
			if(det==0)continue;
			long cp=cross(p.x(), p.y(), targetX, targetY);
			long cq=cross(q.x(), q.y(), targetX, targetY);
			int pi=indexOfPoint(x, y, p.x(), p.y());
			int qi=indexOfPoint(x, y, q.x(), q.y());
			if(pi==-1||qi==-1)continue;
			long num;
			long den;
			if(cp==0&&dot(p.x(), p.y(), targetX, targetY)>0) {
				num=dot(p.x(), p.y(), targetX, targetY);
				den=1;
			} else if(cq==0&&dot(q.x(), q.y(), targetX, targetY)>0) {
				num=dot(q.x(), q.y(), targetX, targetY);
				den=1;
			} else if(Long.signum(cp)!=Long.signum(cq)) {
				long coefP=Math.abs(cq);
				long coefQ=Math.abs(cp);
				long g=MathUtils.gcd(coefP, coefQ);
				coefP/=g;
				coefQ/=g;
				num=dot(coefP*p.x()+coefQ*q.x(), coefP*p.y()+coefQ*q.y(), targetX, targetY);
				den=coefP+coefQ;
			} else {
				continue;
			}
			if(pIndex==-1||FractionUtils.compareFraction(num, den, bestNum, bestDen)>0) {
				bestNum=num;
				bestDen=den;
				px=p.x();
				py=p.y();
				qx=q.x();
				qy=q.y();
				pIndex=pi;
				qIndex=qi;
			}
		}
		if(pIndex==-1)return subsetSumUnbounded2DCollinear(x, y, targetX, targetY);
		long det=cross(px, py, qx, qy);
		if(det<0) {
			long tx=px; px=qx; qx=tx;
			long ty=py; py=qy; qy=ty;
			int ti=pIndex; pIndex=qIndex; qIndex=ti;
			det=-det;
		}
		return subsetSumUnbounded2DConvexDP(x, y, targetX, targetY, px, py, qx, qy, pIndex, qIndex, det);
	}

	/**
	 * 各二次元ベクトルを何回でも使える部分和問題。
	 * 凸包上でtarget方向に最も進む辺について、その2点で埋める前の残差を有限領域DPで求める。
	 * D = max(max(a[i][0]), max(a[i][1])), B = min(max(targetX, targetY), D^3) として、時間計算量 O(a.length * B^2)、空間計算量 O(B^2)。
	 * 未テスト。未証明。
	 *
	 * @param a 各要素のベクトル。{@code a[i][0]}, {@code a[i][1]} は非負で、少なくとも一方は正である必要がある
	 * @param targetX 作りたい和の第1成分
	 * @param targetY 作りたい和の第2成分
	 * @return {@code ret[i]} は {@code a[i]} を使う回数
	 */
	public static long[] subsetSumUnbounded2DConvex(int[][] a, long targetX, long targetY) {
		int N=a.length;
		int[]x=new int[N];
		int[]y=new int[N];
		for (int i = 0; i < N; i++) {
			if(a[i].length!=2)throw new AssertionError();
			x[i]=a[i][0];
			y[i]=a[i][1];
		}
		return subsetSumUnbounded2DConvex(x, y, targetX, targetY);
	}

	private static long[] subsetSumUnbounded2DCollinear(int[] x, int[] y, long targetX, long targetY) {
		long g=MathUtils.gcd(targetX, targetY);
		long ux=targetX/g;
		long uy=targetY/g;
		int[]index=new int[x.length];
		int[]a=new int[x.length];
		int size=0;
		for (int i = 0; i < x.length; i++) {
			if(cross(x[i], y[i], targetX, targetY)!=0||dot(x[i], y[i], targetX, targetY)<=0)continue;
			long k;
			if(ux==0) {
				if(x[i]!=0||y[i]%uy!=0)continue;
				k=y[i]/uy;
			} else if(uy==0) {
				if(y[i]!=0||x[i]%ux!=0)continue;
				k=x[i]/ux;
			} else {
				if(x[i]%ux!=0||y[i]%uy!=0)continue;
				k=x[i]/ux;
				if(y[i]/uy!=k)continue;
			}
			if(k<=0||k>Integer.MAX_VALUE)continue;
			index[size]=i;
			a[size++]=(int)k;
		}
		if(size==0)return null;
		a=Arrays.copyOf(a, size);
		long[]sub=subsetSumUnbounded(a, g);
		if(sub==null)return null;
		long[]ret=new long[x.length];
		for (int i = 0; i < size; i++) {
			ret[index[i]]=sub[i];
		}
		return ret;
	}

	// p = (px, py), q = (qx, qy) を基底として選ぶ。
	// x[i], y[i]: 何回でも使えるベクトルの成分。
	// pIndex, qIndex: p, q に対応する元の配列上の添字。最後に残りを p, q で埋めるために使う。
	// det: cross(p, q) の正の値。格子 <p, q> の基本平行四辺形の面積。
	// D = max(max(x), max(y)) として、残差候補を 0 <= rx, ry <= D^3 の範囲でDPし、
	// target - residual が p, q の非負整数結合になる候補のうち、総使用個数が最小のものを復元する。
	private static long[] subsetSumUnbounded2DConvexDP(
			int[] x, int[] y, long targetX, long targetY,
			long px, long py, long qx, long qy, int pIndex, int qIndex, long det) {
		int maxX=0;
		int maxY=0;
		for (int i = 0; i < x.length; i++) {
			maxX=Math.max(maxX, x[i]);
			maxY=Math.max(maxY, y[i]);
		}
		long d=Math.max(maxX, maxY);
		long bound=MathUtils.saturatingMul(MathUtils.saturatingMul(d, d), d);
		long limitX=Math.min(targetX, bound);
		long limitY=Math.min(targetY, bound);
		if(limitX>Integer.MAX_VALUE||limitY>Integer.MAX_VALUE)throw new AssertionError();
		int X=(int)limitX;
		int Y=(int)limitY;
		int INF=Integer.MAX_VALUE/3;
		int[][]dp=new int[X+1][Y+1];
		int[][]prevX=new int[X+1][Y+1];
		int[][]prevY=new int[X+1][Y+1];
		int[][]item=new int[X+1][Y+1];
		ArrayUtils.fill(dp, INF);
		ArrayUtils.fill(prevX, -1);
		ArrayUtils.fill(prevY, -1);
		ArrayUtils.fill(item, -1);
		dp[0][0]=0;
		for (int i = 0; i < x.length; i++) {
			for (int sx = 0; sx+x[i] <= X; sx++) {
				for (int sy = 0; sy+y[i] <= Y; sy++) {
					int nx=sx+x[i];
					int ny=sy+y[i];
					if(dp[sx][sy]+1<dp[nx][ny]) {
						dp[nx][ny]=dp[sx][sy]+1;
						prevX[nx][ny]=sx;
						prevY[nx][ny]=sy;
						item[nx][ny]=i;
					}
				}
			}
		}
		long bestCount=Long.MAX_VALUE;
		int bestX=-1;
		int bestY=-1;
		long bestPCount=0;
		long bestQCount=0;
		for (int rx = 0; rx <= X; rx++) {
			for (int ry = 0; ry <= Y; ry++) {
				if(dp[rx][ry]==INF)continue;
				long dx=targetX-rx;
				long dy=targetY-ry;
				long pCountNum=cross(dx, dy, qx, qy);
				long qCountNum=cross(px, py, dx, dy);
				if(pCountNum<0||qCountNum<0||pCountNum%det!=0||qCountNum%det!=0)continue;
				long pCount=pCountNum/det;
				long qCount=qCountNum/det;
				long count=dp[rx][ry]+pCount+qCount;
				if(count<bestCount) {
					bestCount=count;
					bestX=rx;
					bestY=ry;
					bestPCount=pCount;
					bestQCount=qCount;
				}
			}
		}
		if(bestX==-1)return null;
		long[]ret=new long[x.length];
		int curX=bestX;
		int curY=bestY;
		while(curX!=0||curY!=0) {
			int i=item[curX][curY];
			ret[i]++;
			int nx=prevX[curX][curY];
			int ny=prevY[curX][curY];
			curX=nx;
			curY=ny;
		}
		ret[pIndex]+=bestPCount;
		ret[qIndex]+=bestQCount;
		return ret;
	}

	private static long cross(long ax, long ay, long bx, long by) {
		return ax*by-ay*bx;
	}

	private static long dot(long ax, long ay, long bx, long by) {
		return ax*bx+ay*by;
	}

	private static int indexOfPoint(int[] x, int[] y, long px, long py) {
		for (int i = 0; i < x.length; i++) {
			if(x[i]==px&&y[i]==py)return i;
		}
		return -1;
	}

	/**
	 * 和がxとなる選び方を返す。存在しない場合はnull。
	 * O(a.length * max a)
	 * @param a
	 * @param x
	 * @return
	 */
	public static boolean[] subsetSumPisinger(int[] a, long x) {
		//https://atcoder.jp/contests/abc221/submissions/71965633
		//https://atcoder.jp/contests/abc221/editorial/2741
		int maxA=ArrayUtils.max(a);
		int q=0;
		long sum=0;
		while(q<a.length && sum+a[q]<=x) {
			sum+=a[q];
			++q;
		}
		if(q==a.length&&sum<x)return null;
		int[]L=new int[2*maxA+1];
		int[]L2=new int[2*maxA+1];
		int offset=maxA;
		int INF=q+1;
		Arrays.fill(L, INF);
		Arrays.fill(L2, INF);
		L[(int)(sum-x+offset)]=0;//重さsum+(i-offset)の状態からa[L[i]]を削除するかどうかで、次に分岐を決める。
		int[][]op=new int[a.length][2*maxA+1];
		ArrayUtils.fill(op, -1);
		for (int i = q; i < a.length; i++) {
			for (int j = maxA; j >= -maxA; j--) {
				if(j + a[i] + offset  < L.length && L[j+a[i]+offset] > L[j+offset]) {
					L[j+a[i]+offset]=L[j+offset];
					op[i][j+a[i]+offset]=2*i;
				}
			}
			for (int j = maxA; j >= -maxA; j--) {
				for (int k=L[j+offset]; k < q && k < L2[j + offset]; ++k) {
					int v=a[k];
					if (j-v >= -maxA) {
						if(L[j-v+offset] > k + 1) {
							L[j-v+offset]=k+1;
							op[i][j-v+offset]=2*k+1;
						}
					} else break;
				}
				L2[j + offset] = L[j+offset];
			}
		}
		
		if(L[offset] == INF)return null;
		boolean[]used=new boolean[a.length];
		for (int i = 0; i < q; i++) {
			used[i]=true;
		}
		int z=0;
		int i=a.length-1;
		while(i >= q) {
			while(op[i][z+offset]!=-1 && op[i][z+offset]%2==1) {
				int k=op[i][z+offset]/2;
				if(!used[k])throw new AssertionError();
				used[k]=false;
				z+=a[k];
			}
			if (op[i][z+offset]!=-1 && op[i][z+offset]%2==0) {
				int k=op[i][z+offset]/2;
				if(used[k])throw new AssertionError();
				used[k]=true;
				z-=a[k];
			}
			i--;
		}
		return used;
	}
	

    /**
     * aの部分集合であって、和がxとなるものが存在するか判定する。
     * 存在する場合は、各要素を使うかどうかを並べたboolean配列を返す。
     * 存在しない場合はnullを返す。
     * n = a.lengthとして、時間計算量 O(n 2^(n/2))、空間計算量 O(2^(n/2))。
     * verified:https://atcoder.jp/contests/abc326/tasks/abc326_f
     */
	public static boolean[] subsetSumMeetInMiddle(long[] a, long x) {
    	int N=a.length/2;
    	int M=a.length-N;
    	long[]head=new long[1<<N];
    	long[]tail=new long[1<<M];
    	for (int i = 0; i < N; i++) {
			head[1<<i]=a[i];
		}
    	for (int i = 0; i < M; i++) {
			tail[1<<i]=a[i+N];
		}
    	head=BooleanLattice.zeta(head);
    	tail=BooleanLattice.zeta(tail);
    	var sortedHead=head.clone();
    	var sortedTail=tail.clone();
    	Arrays.sort(sortedHead);
    	Arrays.sort(sortedTail);
    	int pointer=(1<<M)-1;
    	for (int i = 0; i < 1<<N; i++) {
			while(pointer>=0 && sortedHead[i]+sortedTail[pointer]>x)--pointer;
			if(pointer==-1)return null;
			if(sortedHead[i]+sortedTail[pointer]==x) {
				int s=ArrayUtils.indexOf(sortedHead[i], head);
				int t=ArrayUtils.indexOf(sortedTail[pointer], tail);
				boolean[]ret=new boolean[N+M];
				for (int j = 0; j < N; j++) {
					if(Ints.bitAt(s, j)==1)ret[j]=true;
				}
				for (int j = 0; j < M; j++) {
					if(Ints.bitAt(t, j)==1)ret[N+j]=true;
				}
				return ret;
			}
    	}
    	return null;
    }
	
	
	
    /**
     * 配列 {@code a} の部分集合和について、和が {@code X} 未満である部分集合の個数を
     * 部分集合のサイズごとに数える。
     *
     * <p>返り値 {@code ret} は長さ {@code a.length + 1} の配列であり、
     * {@code ret[k]} は
     *
     * <pre>
     * |{ S ⊆ {0, 1, ..., a.length - 1} : |S| = k かつ Σ_{i∈S} a[i] < X }|
     * </pre>
     *
     * を表す。未テスト
     * <h3>計算量</h3>
     * <ul>
     *   <li>時間計算量: {@code O(n 2^(n/2))}</li>
     *   <li>空間計算量: {@code O(2^(n/2))}</li>
     * </ul>
     * @param a 各要素の重み
     * @param X 閾値
     * @return {@code ret[k]} がサイズ {@code k} の部分集合のうち、和が {@code X} 未満であるものの個数である配列
     */
    public long[] countSubsetSumsLessThanBySize(long[] a, long X) {
    	int N = a.length / 2;
        int M = a.length - N;
        long[] head = new long[1 << N];
        long[] tail = new long[1 << M];
        for (int i = 0; i < N; i++) {
            head[1 << i] = a[i];
        }
        for (int i = 0; i < M; i++) {
            tail[1 << i] = a[i + N];
        }
        head = BooleanLattice.zeta(head);
        tail = BooleanLattice.zeta(tail);
        LongArrayList[] U = new LongArrayList[N + 1];
        LongArrayList[] V = new LongArrayList[M + 1];
        for (int i = 0; i < U.length; i++) {
            U[i] = new LongArrayList((int)MathUtils.comb(N, i));
        }
        for (int i = 0; i < V.length; i++) {
        	V[i] = new LongArrayList((int)MathUtils.comb(M, i));
        }
        for (int i = 0; i < head.length; i++) {
            U[Integer.bitCount(i)].add(head[i]);
        }
        for (int i = 0; i < tail.length; i++) {
            V[Integer.bitCount(i)].add(tail[i]);
        }
        for (int i = 0; i < U.length; i++) {
            U[i].sort();
        }
        for (int i = 0; i < V.length; i++) {
            V[i].sort();
        }
        long[]ret=new long[N+M+1];
        for (int sz0 = 0; sz0 < U.length; sz0++) {
            for (int sz1 = 0; sz1 < V.length; sz1++) {
                int sz = sz0 + sz1;
                int p = V[sz1].size() - 1;
                for (int i = 0; i < U[sz0].size(); i++) {
                    while ((p >= 0) && ((V[sz1].get(p) + U[sz0].get(i)) >= X)) {
                        --p;
                    } 
                    ret[sz] += p+1;
                }
            }
        }
        return ret;
    }


	
	private static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
