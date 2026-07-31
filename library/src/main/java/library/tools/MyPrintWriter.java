package library.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import library.util.collections.IntArrayList;
import library.util.collections.LongArrayList;

public class MyPrintWriter extends PrintWriter {
    private static MyPrintWriter instance = null;
    private MyPrintWriter() {
    	super(System.out);
    }
    public static MyPrintWriter getInstance() {
        if (instance == null) {
            instance = new MyPrintWriter();
        }
        return instance;
    }

    
    public MyPrintWriter(PrintStream out) {
        super(out);
    }
    
    public void println(char[][] a) {
    	for (int i = 0; i < a.length; i++) {
			println(a[i]);
		}
    }
    
    public void println(int[][] a) {
    	for (int i = 0; i < a.length; i++) {
			println(a[i]);
		}
    }
    
    public void println(long[][] a) {
    	for (int i = 0; i < a.length; i++) {
			println(a[i]);
		}
    }

    public void println(boolean[][] a) {
    	for (int i = 0; i < a.length; i++) {
			println(a[i], " ");
		}
    }
    
    public void printlnAs01(boolean[][] a) {
    	for (int i = 0; i < a.length; i++) {
    		for (int j = 0; j < a[i].length; j++) {
				print(a[i][j]?1:0);
			}
    		println();
		}
    }
    
    public void println(long[] a) {
    	println(a, " ");
    }
    
    public void println(int[] a) {
    	println(a, " ");
    }
    
    public void println(IntArrayList a) {
    	println(a.toArray());
    }
    
    public void println(LongArrayList a) {
    	println(a.toArray());
    }
    
    public <T> void println(List<T> a) {
        println(a, " ");
    }

    public <T> void println(List<T> a, String separator) {
    	for (int i = 0; i < a.size(); ++i) {
    		super.print(a.get(i)+(i==a.size()-1?"":separator));
    	}
    	super.println();//a.length=0でも改行させたいのでループの外に。
    }
    
    public void println(int[] a, String separator) {
    	for (int i = 0; i < a.length; ++i) {
    		super.print(a[i]+(i==a.length-1?"":separator));
    	}
    	super.println();//a.length=0でも改行させたいのでループの外に。
    }
    
    public void println(long[] a, String separator) {
    	for (int i = 0; i < a.length; ++i) {
    		super.print(a[i]+(i==a.length-1?"":separator));
    	}
    	super.println();//a.length=0でも改行させたいのでループの外に。
    }
    
    public void println(boolean[] a, String separator) {
    	for (int i = 0; i < a.length; ++i) {
    		super.print(a[i]+(i==a.length-1?"":separator));
    	}
    	super.println();//a.length=0でも改行させたいのでループの外に。
    }

    public void printlnYesNo(boolean flag) {
    	println(flag?"Yes":"No");
    }
    
    public void print(Object...objects) {
    	for (int i = 0; i < objects.length; i++) {
			print(objects[i]+(i==objects.length-1?"\n":" "));
		}
    }    
    
    public void tr(Object...objects) {
    	println(Arrays.deepToString(objects));
    }
}
