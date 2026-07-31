package library.util;

import java.util.Arrays;

public class MonotoneMinima {
	
	
	public interface CostFunction {
		long calc(int i, int j);
	}
	
	public static int[] rowMinima(int H, int W, CostFunction f) {
		int[]ans=new int[H];
		Arrays.fill(ans, -1);
		solve(0, H, 0, W, ans, f);
		return ans;
	}
	
	private static void solve(int x0, int x1, int y0, int y1, int[]ans, CostFunction f) {
		if (x0 >= x1 || y0 >= y1) return;
		int mid=(x0+x1) >>> 1;
		long minValue = Long.MAX_VALUE;
		int minArg = -1;
		for (int i = y0; i < y1; i++) {
			long value = f.calc(mid, i);
			if (minArg==-1 || minValue > value) {
				minValue = value;
				minArg = i;
			}
		}
		ans[mid]=minArg;
		solve(x0, mid, y0, minArg+1, ans, f);
		solve(mid+1, x1, minArg, y1, ans, f);
	}
	
	private static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
	
}

