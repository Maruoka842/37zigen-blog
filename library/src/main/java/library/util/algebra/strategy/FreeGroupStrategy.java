package library.util.algebra.strategy;

import java.util.Arrays;

import library.util.ArrayUtils;
/**
 * 未テスト
 */
public class FreeGroupStrategy implements GroupStrategy <int[]>{

	@Override
	public int[] identity() {
		return new int[0];
	}

	@Override
	public int[] mul(int[] a, int[] b) {
		for (int v : a) if (v == 0) throw new AssertionError();
		for (int v : b) if (v == 0) throw new AssertionError();
		int lastA = a.length - 1;
		int firstB = 0;
		while (lastA >= 0 && firstB < b.length && a[lastA] == -b[firstB]) {
			--lastA;
			++firstB;
		}
		return ArrayUtils.concat(Arrays.copyOf(a, lastA + 1), Arrays.copyOfRange(b, firstB, b.length));
	}

	@Override
	public int[] inverse(int[] a) {
		for (int v : a) if (v == 0) throw new AssertionError();
		int[]ia=new int[a.length];
		for (int i = 0; i < a.length; i++) {
			ia[a.length-1-i]=-a[i];
		}
		return ia;
	}
	

	@Override
	public boolean equals(int[] a, int[] b) {
		for (int v : a) if (v == 0) throw new AssertionError();
		for (int v : b) if (v == 0) throw new AssertionError();
		return Arrays.equals(a, b);
	}
}
