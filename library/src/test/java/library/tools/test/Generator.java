package library.tools.test;

import java.util.Arrays;
import java.util.Random;

import library.tools.FastScanner;
import library.tools.MyPrintWriter;
import library.util.ArrayUtils;

public class Generator {
	static MyPrintWriter pw=MyPrintWriter.getInstance();
	static FastScanner sc=FastScanner.getInstance();
	
	public static void main(String[] args) {
        Random rnd = new Random();
        int N=rnd.nextInt(1,5);
        int M=rnd.nextInt(1,5);
        int Q=rnd.nextInt(1,5);
        pw.println(N+" "+M+" "+Q);
        for (int i = 0; i < Q; i++) {
			int L=rnd.nextInt(1,N+1);
			int R=rnd.nextInt(1,N+1);
			int X=rnd.nextInt(1,M+1);
			L=Math.min(L, R);
			pw.println(L+" "+R+" "+X);
        }
        pw.flush();
    }
}
