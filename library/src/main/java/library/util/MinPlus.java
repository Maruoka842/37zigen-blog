package library.util;

import java.util.Arrays;
import java.util.Random;

import library.util.MonotoneMinima.CostFunction;
import library.util.collections.IntDeque;

public class MinPlus {
	
	/**
	 * 行列aのn乗
	 * @param a
	 * @param n
	 * @return
	 */
	public static long[][] pow(long[][] a, long n) {
		//https://atcoder.jp/contests/abc445/submissions/73319196
		if(n==0) {
			long[][]ret=new long[a.length][a.length];
			for (int i = 0; i < ret.length; i++) {
				for (int j = 0; j < ret[i].length; j++) {
					if(i!=j)ret[i][j]=Long.MAX_VALUE/3;
				}
			}
			return ret;
		} else if(n==1)return a; {
			long[][]a2=matrixMultiplication(a, a);
			if(n%2==1) {
				return matrixMultiplication(a, pow(a2, n/2));
			} else {
				return pow(a2, n/2);
			}
		}
	}
	
    /**
     * INF=Long.MAX_VALUE/3
     * @param a
     * @param b
     * @return
     */
	public static long[][] matrixMultiplication(long[][] a, long[][] b) {
		long[][]c=new long[a.length][b[0].length];
		long INF=Long.MAX_VALUE/3;
		ArrayUtils.fill(c, INF);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				for (int k = 0; k < a[0].length; k++) {
					c[i][j]=Math.min(c[i][j], a[i][k]+b[k][j]);
				}
			}
		}
		return c;
	}
	
	/**
	 * Aが下に凸。
	 * C[i,j]=A[i-j]+B[i]とすると
	 * C[i,j]+C[i+1,j+1] ≤ C[i+1,j]+C[i,j+1]
	 * となりCはMonge。Monge⇒totally monotone min⇒monotone min
	 * https://judge.yosupo.jp/submission/336730
	 * @param A
	 * @param B
	 * @return
	 */
	public static long[] convolveConvexAndArbitrary(long[] A, long[] B) {
		CostFunction f=(i, j)->{
			if(i-j<0||i-j>=A.length)return Long.MAX_VALUE;
			return A[i-j]+B[j];
		};
		// A[i],B[j]が0≤i≤5, 0≤j≤3で定義されているときのC[i][j]=A[i-j]+B[j]の様子
		// A[0]+B[0]
		// A[1]+B[0] A[0]+B[1]
		// A[2]+B[0] A[1]+B[1] A[0]+B[2]
		// A[3]+B[0] A[2]+B[1] A[1]+B[2] A[0]+B[3]
		// A[4]+B[0] A[3]+B[1] A[2]+B[2] A[1]+B[3] 
		// A[5]+B[0] A[4]+B[1] A[3]+B[2] A[2]+B[3]
		//           A[5]+B[1] A[4]+B[2] A[3]+B[3]
		//                     A[5]+B[2] A[4]+B[3]
		//                               A[5]+B[3]
		var args=MonotoneMinima.rowMinima(A.length+B.length-1, B.length, f);
		long[]ret=new long[A.length+B.length-1];
		for (int i = 0; i < args.length; i++) {
			ret[i]=f.calc(i, args[i]);
		}
		return ret;
	}

	
    
    /**
     * a, bがともに下に凸の場合。
     * Long.MAX_VALUEを∞として扱う。
     * <p>計算量: O(N + M)</p>
     * // 未テスト
     * @param a
     * @param b
     * @return
     */
    public static long[] convolveConvexConvex(long[] a, long[] b) {
    	//https://judge.yosupo.jp/submission/387768
		int n = a.length, m = b.length;
		long[] c = new long[n + m - 1];
    	Arrays.fill(c, Long.MAX_VALUE);
	
		int l1 = 0; while (l1 < n && a[l1] == Long.MAX_VALUE) l1++;
		int r1 = n - 1; while (r1 >= l1 && a[r1] == Long.MAX_VALUE) r1--;
		int l2 = 0; while (l2 < m && b[l2] == Long.MAX_VALUE) l2++;
		int r2 = m - 1; while (r2 >= l2 && b[r2] == Long.MAX_VALUE) r2--;
		if (l1 > r1 || l2 > r2) return c; // 全部∞
	
		int n1 = r1 - l1 + 1;
		int n2 = r2 - l2 + 1;
		for (int i = l1; i <= r1; i++) {
			if (a[i] == Long.MAX_VALUE) throw new AssertionError();
		}
	    for (int i = l2; i <= r2; i++) {
			if (b[i] == Long.MAX_VALUE) throw new AssertionError();
		}

	    int[] lower = new int[n1 + 1];
	    lower[0] = 0;
	    lower[n1] = n2 - 1;
	    {
		    int j = 0;
		    for (int i = 1; i < n1; i++) {
		        while (j + 1 < n2 && b[l2 + j + 1] != Long.MAX_VALUE && b[l2 + j] != Long.MAX_VALUE
		                && a[l1 + i] - a[l1 + i - 1] > b[l2 + j + 1] - b[l2 + j]) {
		            ++j;
		        }
		        lower[i] = j;
		    }
	    }

	    for (int i = 0; i < n1; i++) {
	        for (int j = lower[i]; j <= lower[i + 1]; j++) {
	            int k = (l1 + i) + (l2 + j);
	            long v = a[l1 + i] + b[l2 + j];
	            c[k] = Math.min(c[k], v);
	        }
	    }
	    return c;
    }
    
    /**
     * O(N^2). Long.MAX_VALUEをINFとして扱う。
     * @param a
     * @param b
     * @return
     */
    public static long[] convolve(long[] a, long[] b) {
    	long[]c=new long[a.length+b.length-1];
    	Arrays.fill(c, Long.MAX_VALUE);
    	for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				if (a[i] == Long.MAX_VALUE || b[j] == Long.MAX_VALUE) continue;
				c[i+j]=Math.min(c[i+j], a[i]+b[j]);
			}
		}
    	return c;
    }

    
    
    /**
     * f+f^2+f^3+...
     * O(N^2)
     * https://atcoder.jp/contests/abc285/submissions/71290628
     * @param f
     * @return
     */
	public static long[] geometricSum(long[] f) {
		long[]g=f.clone();
		for (int i = 0; i < g.length; i++) {
			for (int j = 0; i+j < f.length; j++) {
				g[i+j]=Math.min(g[i+j], g[i]+f[j]);
			}
		}
		return g;
    }
	
	
	/**
	 * <pre>
	 * b[i]=min_j(|i-j|+a[j])
	 * </pre>
	 * @param a
	 * @return
	 * https://atcoder.jp/contests/abc443/submissions/72920701
	 */
	public static long[] l1distanceTransform(long[] a) {
		long[]b=a.clone();
		for (int i = 0; i < a.length-1; i++) {
			b[i+1]=Math.min(b[i+1], b[i]+1);
		}
		for (int i = a.length - 1; i >= 1; i--) {
			b[i-1]=Math.min(b[i-1], b[i]+1);
		}
		return b;
	}
	
	/**
	 * g[k] = min{ f[i] + aj : k = i + bj && 0 <= j < length}
	 * a,bに符号の制約はない。
	 * @param f
	 * @param a
	 * @param b
	 * @param length
	 * @param stride
	 * @return
	 */
	public static long[] convolveLinearSegment(long[]f, long a, int b, int length) {
		//https://atcoder.jp/contests/abc269/submissions/74015131
		if (b < 0) {
			ArrayUtils.reverse(f);
			long[] ret=convolveLinearSegment(f, a, -b, length);
			ArrayUtils.reverse(f);
			ArrayUtils.reverse(ret);
			return ret;
		} else if (b == 0) {
			long[] ret=f.clone();
			if(a<0) {
				for (int i = 0; i < f.length; i++) {
					ret[i]+=a*(length-1);
				}
			}
			return ret;
		} else {
			long[]g=new long[f.length];
			for (int i = 0; i < b; i++) {//mod b で独立な問題に分割
				IntDeque dq=new IntDeque();
				for (int j = 0; i + b * j < f.length; j++) {
					int pos=i+b*j;
					while(!dq.isEmpty()) {
						var y = dq.peekLast();
						if (f[i + b * y] + a * (j - y) >= f[pos]) {
							dq.pollLast();
						} else {
							break;
						}
					}
					dq.addLast(j);
					while (!dq.isEmpty() && dq.peekFirst() <= j-length) dq.pollFirst();
					int k = dq.peekFirst();
					g[i + b * j] = f[i + b * k] + a * (j - k);
				}
			}
			return g;
		}
	}
	
	
	public static long[] naive(long[] f, long a, int b, int length) {
		int n = f.length;
		long[] g = new long[n];
		long INF=Long.MAX_VALUE/3;
		Arrays.fill(g, INF);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < length; j++) {
				int k = i + b * j;
				if (k >= g.length || k < 0) continue;
				g[k] = Math.min(g[k], f[i] + a * j);
			}
		}
		return g;
	}

	public static void randomTest() {
		Random rnd = new Random();

		for (int t = 0; t < 100000; t++) {

			int n = rnd.nextInt(10) + 1;
			long[] f = new long[n];

			for (int i = 0; i < n; i++) {
				f[i] = rnd.nextInt(20) - 10;
			}

			long a = rnd.nextInt(-10, 10) - 5;
			int b = rnd.nextInt(-10, 10);
			int length = rnd.nextInt(6) + 1;

			long[] g1 = naive(f, a, b, length);
			long[] g2 = convolveLinearSegment(f, a, b, length);

			if (!Arrays.equals(g1, g2)) {
				System.out.println("WA");
				System.out.println(Arrays.toString(f));
				System.out.println(a + " " + b + " " + length);
				System.out.println(Arrays.toString(g1));
				System.out.println(Arrays.toString(g2));
				return;
			}
		}

		System.out.println("OK");
	}
	
	/**
	 * 上三角 Monge 行列 A, B の (min, +) 積 C を $O(N^2)$ で計算する。
	 * $C_{i,j} = \min_{i \le k \le j} (A_{i,k} + B_{k,j})$ である。
	 * A, B が上三角 Monge ならば、C も上三角 Monge となる。
	 *
	 * <p>計算量: $O(N^2)$。</p>
	 *
	 * @param N 行列のサイズ (0 から N までのインデックスを持つ)
	 * @param A $A_{i,j}$ を計算する関数
	 * @param B $B_{i,j}$ を計算する関数
	 * @return $(N+1) \times (N+1)$ の行列 C
	 */
	public static long[][] mongeMatrixProduct(int N, CostFunction A, CostFunction B) {
		// https://topcoder-g-hatena-ne-jp.jag-icpc.org/spaghetti_source/20120915/1347668163.html
		// https://judge.yosupo.jp/submission/336730
		long[][] C = new long[N + 1][N + 1];
		for (long[] row : C) Arrays.fill(row, Long.MAX_VALUE / 3);
		int[] K = new int[N + 1];
		for (int i = 0; i <= N; i++) {
			C[i][i] = A.calc(i, i) + B.calc(i, i);
			K[i] = i;
		}
		for (int s = 1; s <= N; s++) {
			int[] nextK = new int[N + 1 - s];
			for (int i = 0; i <= N - s; i++) {
				int j = i + s;
				int p = K[i];
				int q = K[i + 1];
				for (int k = p; k <= q; k++) {
					long val = A.calc(i, k) + B.calc(k, j);
					if (C[i][j] > val) {
						C[i][j] = val;
						nextK[i] = k;
					}
				}
			}
			K = nextK;
		}
		return C;
	}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
