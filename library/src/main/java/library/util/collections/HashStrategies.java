package library.util.collections;
import java.util.Arrays;

import library.util.LongFraction;

public class HashStrategies {
	public static final Hash.Strategy<long[]> LONG_ARRAY = new Hash.Strategy<long[]>() {
		@Override
		public boolean equals(long[] a, long[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(long[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<boolean[]> BOOLEAN_ARRAY = new Hash.Strategy<boolean[]>() {
		@Override
		public boolean equals(boolean[] a, boolean[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(boolean[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<byte[]> BYTE_ARRAY = new Hash.Strategy<byte[]>() {
		@Override
		public boolean equals(byte[] a, byte[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(byte[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<short[]> SHORT_ARRAY = new Hash.Strategy<short[]>() {
		@Override
		public boolean equals(short[] a, short[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(short[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<float[]> FLOAT_ARRAY = new Hash.Strategy<float[]>() {
		@Override
		public boolean equals(float[] a, float[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(float[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<double[]> DOUBLE_ARRAY = new Hash.Strategy<double[]>() {
		@Override
		public boolean equals(double[] a, double[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(double[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<Object[]> OBJECT_ARRAY = new Hash.Strategy<Object[]>() {
		@Override
		public boolean equals(Object[] a, Object[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(Object[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<char[]> CHAR_ARRAY = new Hash.Strategy<char[]>() {
		@Override
		public boolean equals(char[] a, char[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(char[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<int[]> INT_ARRAY = new Hash.Strategy<int[]>() {
		@Override
		public boolean equals(int[] a, int[] b) {
			return Arrays.equals(a, b);
		}

		@Override
		public int hashCode(int[] o) {
			return Arrays.hashCode(o);
		}
	};

	public static final Hash.Strategy<LongFraction[]> FRACTION_ARRAY = new Hash.Strategy<LongFraction[]>() {

		@Override
		public int hashCode(LongFraction[] o) {
			return Arrays.hashCode(o);
		}

		@Override
		public boolean equals(LongFraction[] a, LongFraction[] b) {
			if (a.length != b.length)
				return false;
			for (int i = 0; i < a.length; i++) {
				if (!a[i].equals(b[i]))
					return false;
			}
			return true;
		}
	};

}

