package library.util.graph;

import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * 分割マトロイド（partition matroid）を表すクラス。
 * 台集合をいくつかの互いに素な集合に分割し、各部分集合から選べる要素数の上限を指定したマトロイド。
 * 未テスト
 */
public class PartitionMatroid implements Matroid {
	private final int M;
	private final int[] belong;
	/** R[i] は i 番目の part から選べる要素数の上限 */
	private final int[] R;
	/** cnt[i] は現在の独立集合において、i 番目の part からあといくつ要素を選べるか */
	private int[] cnt;
	private IntArrayList[] circuits;

	/**
	 * 分割マトロイドを構築する。
	 * @param M 台集合のサイズ
	 * @param parts 台集合の分割。parts[i] は i 番目の部分集合に含まれる要素のリスト。
	 *              各要素はいずれか一つの part に属する必要がある。
	 *              属していない要素がある場合、それらは単独の part となり、容量 1 が割り当てられる。
	 * @param capacities 各 part から選べる要素数の上限。capacities[i] は parts[i] から選べる最大要素数。
	 */
	public PartitionMatroid(int M, int[][] parts, int[] capacities) {
		this.M = M;
		this.belong = new int[M];
		Arrays.fill(belong, -1);
		int numParts = parts.length;
		for (int i = 0; i < numParts; i++) {
			for (int e : parts[i]) {
				belong[e] = i;
			}
		}
		int unassigned = 0;
		for (int e = 0; e < M; e++) {
			if (belong[e] == -1) unassigned++;
		}
		this.R = new int[numParts + unassigned];
		System.arraycopy(capacities, 0, this.R, 0, numParts);
		int nextPart = numParts;
		for (int e = 0; e < M; e++) {
			if (belong[e] == -1) {
				belong[e] = nextPart;
				this.R[nextPart] = 1;
				nextPart++;
			}
		}
	}

	@Override
	public int size() {
		return M;
	}

	@Override
	public void set(boolean[] I) {
		cnt = Arrays.copyOf(R, R.length);
		for (int e = 0; e < M; e++) {
			if (I[e]) {
				cnt[belong[e]]--;
			}
		}
		if (circuits == null) {
			circuits = new IntArrayList[R.length];
			for (int i = 0; i < R.length; i++) {
				circuits[i] = new IntArrayList();
			}
		} else {
			for (int i = 0; i < R.length; i++) {
				circuits[i].clear();
			}
		}
		for (int e = 0; e < M; e++) {
			if (I[e] && cnt[belong[e]] == 0) {
				circuits[belong[e]].add(e);
			}
		}
	}

	@Override
	public IntArrayList circuit(int e) {
		int p = belong[e];
		if (cnt[p] == 0) {
			IntArrayList ret = new IntArrayList();
			ret.addAll(circuits[p]);
			ret.add(e);
			return ret;
		}
		return new IntArrayList(0);
	}
}
