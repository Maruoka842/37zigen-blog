package library.util;

import java.util.Arrays;

public class BigInt {
	
    /**
     * (sum a[i]10^i)%mod
     * @param a
     * @param mod
     * @return
     */
    public static long mod(int[] a, long mod) {
    	long ret=0;
    	for (int i = a.length - 1; i >= 0; i--) {
			ret=(10*ret+a[i])%mod;
		}
    	return ret;
    }
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
