package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.algebra.strategy.RingStrategy;
/**
 * 未テスト
 * @param <T>
 */
public class Polynomial<T> {
	RingStrategy<T> st;
	
	public Polynomial(RingStrategy<T> st) {
		this.st = st;
	}
	
	public T[] add(T[] a,T[] b) {
		T[] ret=(T[])(new Object[Math.max(a.length, b.length)]);
		for (int i = 0; i < ret.length; i++) {
			ret[i] = st.add( (i < a.length ? a[i] : st.zero()), (i < b.length ? b[i] : st.zero()));
		}
		return ret;
	}
	
    public T[] subtract(T[] a, T[] b) {
        int n = Math.max(a.length, b.length);
        T[] ret = (T[]) new Object[n];
        for (int i = 0; i < n; i++) {
            T x = i < a.length ? a[i] : st.zero();
            T y = i < b.length ? b[i] : st.zero();
            ret[i] = st.sub(x, y); 
        }
        return ret;
    }

    public T[] mulNaive(T[] a, T[] b) {
        int n = a.length + b.length - 1;
        T[] ret = (T[]) new Object[n];
        Arrays.fill(ret, st.zero());

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                ret[i + j] = st.add(ret[i + j], st.mul(a[i], b[j]));
            }
        }
        return ret;
    }

    public T[] mul(T[] a, T[] b) {
    	return mulNaive(a, b);
    }
    
    public T[] sparseInv(T[] a) {
    	if (!st.equals(a[0], st.one())) throw new AssertionError();
    	ArrayList<Integer> degs=new ArrayList<>();
    	ArrayList<T> coefs=new ArrayList<>();
        for (int i = 1; i < a.length; i++) {
            if (!st.equals(a[i], st.zero())) {
                degs.add(i);
                coefs.add(a[i]);
            }
        }

        T[] b = (T[]) java.lang.reflect.Array
                .newInstance(a.getClass().getComponentType(), a.length);
        b[0] = st.one();
        for (int i = 1; i < a.length; i++) {
    		for (int j=0;j<degs.size();++j) {
    			int deg=degs.get(j);
    			if(i-deg<0)break;
    			b[i] = st.sub(b[i], st.mul(coefs.get(j), b[i-deg]));
    		}
    	}
    	return b;
    }

}