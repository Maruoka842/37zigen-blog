package library.util;

import java.util.Arrays;

public class BitArray {
	
	final int n;
	final int lg = 5;
	final int wordSize = 1 << lg;
	final int mask = (1 << lg) - 1;
	final int[] prefixSum;
	final int[] data;
	final int len;
	
	public BitArray(int n) {
		this.n = n;
		len = (n + wordSize - 1) / wordSize;
		data = new int[len + 1];//prefixSum(i)でi=a.lengthが飛んでくる場合があるので、1つ大きめに取っている。
		prefixSum = new int[data.length + 1];
		
	}
	
	public void set(int k) {
		data[k >> lg] |= 1 << (k & mask);
	}
	
	public void build() {
		for (int i = 1; i < data.length; i++) {
			prefixSum[i] = Integer.bitCount(data[i - 1]) + prefixSum[i - 1];
		}
	}
	
	/**
	 * [0, i)の和を返す
	 * @param i
	 * @return
	 */
	public int prefixSum(int i) {
		return prefixSum[i >> lg] + Integer.bitCount(data[i >> lg] & ((1 << (i & mask)) - 1));
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * 内部状態を文字列として表現します。
	 *
	 * <p>計算量: $O(\text{len})$</p>
	 *
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "BitArray{n=" + n + ", data=" + java.util.Arrays.toString(data) + ", prefixSum=" + java.util.Arrays.toString(prefixSum) + "}";
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(\text{len})$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}
