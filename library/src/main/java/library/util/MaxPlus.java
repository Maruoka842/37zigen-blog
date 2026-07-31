package library.util;

import java.util.Arrays;

public class MaxPlus {
	
	/**
	 * Aが上に凸。
	 * C[i,j]=A[i-j]+B[i]とすると
	 * C[i,j]+C[i+1,j+1] ≥ C[i+1,j]+C[i,j+1]
	 * となりCはanti-Monge。anti-Monge⇒totally monotone max⇒monotone max
	 * 凸性が壊れるので両端のINFはtrimしてから渡すこと。
	 * @param A
	 * @param B
	 * @return
	 * https://atcoder.jp/contests/abc348/submissions/72232522
	 */
	public static long[] convolveConcaveAndArbitrary(long[] A, long[] B) {
		MonotoneMaxima.CostFunction f=(i, j)->{
			if(i-j<0||i-j>=A.length)return Long.MIN_VALUE;
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
		var args=MonotoneMaxima.rowMaxima(A.length+B.length-1, B.length, f);
		long[]ret=new long[A.length+B.length-1];
		for (int i = 0; i < args.length; i++) {
			ret[i]=f.calc(i, args[i]);
		}
		return ret;
	}
	
    public static long[] convolveConcaveConcaveNaive(long[] a, long[] b) {
    	long[] c = new long[a.length + b.length - 1];
    	Arrays.fill(c, Long.MIN_VALUE);
    	for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				c[i+j]=Math.max(c[i+j], a[i]+b[j]);
			}
		}
    	return c;
    }
	
    /**
     * a, bがともに上に凸の場合。O(n)
     * Long.MIN_VALUEを-∞として扱う。
     * @param a
     * @param b
     * @return
     */
    public static long[] convolveConcaveConcave(long[] a, long[] b) {
    	//@see https://atcoder.jp/contests/abc383/submissions/72617643
    	//https://atcoder.jp/contests/abc218/submissions/77783080
	    int n = a.length, m = b.length;
	    long[] c = new long[n + m - 1];
	    Arrays.fill(c, Long.MIN_VALUE);

	    int l1 = 0; while (l1 < n && a[l1] == Long.MIN_VALUE) l1++;
	    int r1 = n - 1; while (r1 >= l1 && a[r1] == Long.MIN_VALUE) r1--;
	    int l2 = 0; while (l2 < m && b[l2] == Long.MIN_VALUE) l2++;
	    int r2 = m - 1; while (r2 >= l2 && b[r2] == Long.MIN_VALUE) r2--;
	    if (l1 > r1 || l2 > r2) return c; // 全部-∞
	    // a[l1, r1] が有限
	    // b[l2, r2] が有限
	    // 有限部分の長さ
	    int n1 = r1 - l1 + 1;
	    int n2 = r2 - l2 + 1;
	    for (int i = l1; i <= r1; i++) {
			if(a[i] == Long.MIN_VALUE) throw new AssertionError();
		}
	    for (int i = l2; i <= r2; i++) {
			if(b[i] == Long.MIN_VALUE) throw new AssertionError();
		}
	    int[] upper = new int[n1 + 1];
	    upper[0] = 0;
	    upper[n1] = n2 - 1;
	    {
		    int j = 0;
		    for (int i = 1; i < n1; i++) {
		        while (j + 1 < n2 && b[l2 + j + 1]!= Long.MIN_VALUE && b[l2 + j]!= Long.MIN_VALUE
		                && a[l1 + i] - a[l1 + i - 1] < b[l2 + j + 1] - b[l2 + j]) {
		            ++j;
		        }
		        upper[i] = j;
		    }
	    }

	    for (int i = 0; i < n1; i++) {
	        for (int j = upper[i]; j <= upper[i + 1]; j++) {
	            int k = (l1 + i) + (l2 + j);
	            long v = a[l1 + i] + b[l2 + j];
	            if (v < c[k]) throw new AssertionError();
	            c[k] = v;
	        }
	    }
	    return c;
	}
    
    

	/**
	 * O(N^2). Long.MIN_VALUEを-INFとして扱う。
	 * 未テスト
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] convolve(long[] a, long[] b) {
		long[] c = new long[a.length + b.length - 1];
		Arrays.fill(c, Long.MIN_VALUE);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				if (a[i] == Long.MIN_VALUE || b[j] == Long.MIN_VALUE) continue;
				c[i + j] = Math.max(c[i + j], a[i] + b[j]);
			}
		}
		return c;
	}
	
    
    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
