package library.util.collections;

import java.util.Arrays;

public class Hash {
	public interface Strategy<K> {
		int hashCode(K o);
		boolean equals(K a, K b);
	}

	public static <K> Strategy<K> defaultStrategy() {
		return new Strategy<K>() {
			@Override
			public int hashCode(K o) {
				if (o instanceof int[]) return Arrays.hashCode((int[]) o);
				if (o instanceof long[]) return Arrays.hashCode((long[]) o);
				if (o instanceof char[]) return Arrays.hashCode((char[]) o);
				if (o instanceof boolean[]) return Arrays.hashCode((boolean[]) o);
				if (o instanceof byte[]) return Arrays.hashCode((byte[]) o);
				if (o instanceof short[]) return Arrays.hashCode((short[]) o);
				if (o instanceof float[]) return Arrays.hashCode((float[]) o);
				if (o instanceof double[]) return Arrays.hashCode((double[]) o);
				if (o instanceof Object[]) return Arrays.hashCode((Object[]) o);
				if (o instanceof Long x) {
					x ^= x >>> 33;
				    x *= 0xff51afd7ed558ccdL;
				    x ^= x >>> 33;
				    x *= 0xc4ceb9fe1a85ec53L;
				    x ^= x >>> 33;
				    return x.intValue();
				}
				return o.hashCode();
			}

			@Override
			public boolean equals(K a, K b) {
				if (a instanceof int[] && b instanceof int[])
					return Arrays.equals((int[]) a, (int[]) b);
				if (a instanceof long[] && b instanceof long[])
					return Arrays.equals((long[]) a, (long[]) b);
				if (a instanceof char[] && b instanceof char[])
					return Arrays.equals((char[]) a, (char[]) b);
				if (a instanceof boolean[] && b instanceof boolean[])
					return Arrays.equals((boolean[]) a, (boolean[]) b);
				if (a instanceof byte[] && b instanceof byte[])
					return Arrays.equals((byte[]) a, (byte[]) b);
				if (a instanceof short[] && b instanceof short[])
					return Arrays.equals((short[]) a, (short[]) b);
				if (a instanceof float[] && b instanceof float[])
					return Arrays.equals((float[]) a, (float[]) b);
				if (a instanceof double[] && b instanceof double[])
					return Arrays.equals((double[]) a, (double[]) b);
				if (a instanceof Object[] && b instanceof Object[])
					return Arrays.equals((Object[]) a, (Object[]) b);
				return a.equals(b);
			}
		};
	}
}
