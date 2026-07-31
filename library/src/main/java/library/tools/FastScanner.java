package library.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class FastScanner {
    private static FastScanner instance = null;

    private final InputStream in = System.in;
    private final byte[] buffer = new byte[1<<16];
    private int ptr = 0;
    private int buflen = 0;

    private FastScanner() {} 
    
    public static FastScanner getInstance() {
        if (instance == null) {
            instance = new FastScanner();
        }
        return instance;
    }

    private boolean hasNextByte() {
        if (ptr < buflen) return true;
        ptr = 0;
        try {
            buflen = in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buflen > 0;
    }

    private int readByte() {
        if (hasNextByte()) return buffer[ptr++];
        else return -1;
    }

    private boolean isPrintableChar(int c) {
        return 33 <= c && c <= 126;
    }

    public boolean hasNext() {
        while (hasNextByte() && !isPrintableChar(buffer[ptr])) ptr++;
        return hasNextByte();
    }

    public String next() {
        if (!hasNext()) throw new NoSuchElementException();
        StringBuilder sb = new StringBuilder();
        int b = readByte();
        while (isPrintableChar(b)) {
            sb.appendCodePoint(b);
            b = readByte();
        }
        return sb.toString();
    }

    public long nextLong() {
        if (!hasNext()) throw new NoSuchElementException();
        long n = 0;
        boolean minus = false;
        int b = readByte();
        if (b == '-') {
            minus = true;
            b = readByte();
        }
        while (b >= '0' && b <= '9') {
//            n = n * 10 + (b - '0');
        	n = (n << 1) + (n << 3) + (b - '0');
        	b = readByte();
        }
        return minus ? -n : n;
    }
    
    public long nextUnsignedLong() {
    	return Long.parseUnsignedLong(next());
    }

    public int nextInt() {
        return (int) nextLong();
    }
    
    public long[] nextLongs(int n) {
    	long[] a = new long[n];
    	for (int i = 0; i < n; ++i) {
    		a[i] = nextLong();
    	}
    	return a;
    }
    
    public int[] nextInts(int n) {
    	int[] a = new int[n];
    	for (int i = 0; i < n; ++i) {
    		a[i] = nextInt();
    	}
    	return a;
    }
    
    public int[][] nextInts(int H, int W) {
    	int[][] a=new int[H][W];
    	for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				a[i][j]=nextInt();
			}
		}
    	return a;
    }
    
    
    public int[][][] nextInts(int n0, int n1, int n2) {
    	int[][][] a=new int[n0][n1][n2];
    	for (int i = 0; i < n0; i++) {
			for (int j = 0; j < n1; j++) {
				for (int k = 0; k < n2; k++) {
					a[i][j][k]=nextInt();
				}
			}
		}
    	return a;
    }

    

    public double[] nextDoubles(int n) {
    	double[] a = new double[n];
    	for (int i = 0; i < n; ++i) {
    		a[i] = nextDouble();
    	}
    	return a;
    }
    
    public String[] nexts(int n) {
    	String[] a = new String[n];
    	for (int i = 0; i < n; ++i) {
    		a[i] = next();
    	}
    	return a;
    }
    
    public long[] nextUnsignedLongs(int n) {
    	long[] a = new long[n];
    	for (int i = 0; i < n; ++i) {
    		a[i] = nextUnsignedLong();
    	}
    	return a;
    }
    
    public char[][] nextChars(int H, int W) {
    	if (W == 0) return new char[H][0];
    	char[][] a=new char[H][];
    	for (int i = 0; i < H; i++) {
			a[i]=next().toCharArray();
		}
    	return a;
    }
    	
    public long[][] nextLongs(int H, int W) {
    	long[][] a=new long[H][W];
    	for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				a[i][j]=nextLong();
			}
		}
    	return a;
    }
    
    public double nextDouble() {
    	return Double.valueOf(next());
    }
}
