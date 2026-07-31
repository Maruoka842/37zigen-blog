package library.util.collections;


public class IntArrayWithNegativeIndex {
	private int[] data;
	private int[] stride;
	private int[] shape;
	private int[] offset;
	int n;
	
	public IntArrayWithNegativeIndex(int...shape) {
		this.n = shape.length;
		this.shape = shape;
		stride = new int[n + 1];
		stride[n] = 1;
		for (int i = n - 1; i >= 0; i--) {
			stride[i] = shape[i] * stride[i + 1];
		}
		data = new int[stride[0]];
	}
	
	public void setOffset(int...offset) {
		this.offset = offset;
	}
	
	public void set(int val, int...id) {
		data[flatten(id)] = val;
	}
	
	int add(int val, int...id) {
		return data[flatten(id)] += val;
	}
	
	public int get(int...id) {
		return data[flatten(id)];
	}
	
	public void fill(int v) {
		for (int i = 0; i < data.length; i++) {
			data[i] = v;
		}
	}
	
	int flatten(int...id) {
		int ret = 0;
		for (int i = 0; i < n; i++) {
			int a = id[i];
			if (a <= -shape[i] || a >= shape[i]) {
				a %= shape[i];
			}
			if (a < 0) a += shape[i];
			ret += a * stride[i + 1];
		}
		return ret;
	}
	
	public boolean checkBounds(int...id) {
		if (offset == null) throw new AssertionError();
		for (int i = 0; i < id.length; i++) {
			if (!(0 <= id[i]+offset[i] && id[i]+offset[i]<shape[i])) return false;
		}
		return true;
	}

	/**
	 * 内部状態を文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>計算量: $O(\text{data.length})$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "IntArrayWithNegativeIndex { shape: " + java.util.Arrays.toString(shape) + ", data: " + java.util.Arrays.toString(data) + " }";
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(\text{data.length})$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}
