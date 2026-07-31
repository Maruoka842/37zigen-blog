package library.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
public class OfflineDisjointSparseTable<T, S> {
	//https://judge.yosupo.jp/submission/346434
	//https://atcoder.jp/contests/abc426/submissions/74058862
	int n;
	T[] dp;
	S[] ans;
	OfflineDSTStrategy<T, S> strategy;
	T identity;
	
	@SuppressWarnings("unchecked")
	public OfflineDisjointSparseTable(int n, OfflineDSTStrategy<T, S> strategy) {
		this.n = n;
		this.dp = (T[]) new Object[n + 1];
		this.identity = strategy.identity();
		this.strategy = strategy;
	}
	
	/**
	 * queriesはnew int[]{id, l, r}を並べる
	 * @param queries
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public S[] solve(ArrayList<int[]> queries) {
	    S dummy = strategy.answer(identity, identity, 0);
	    ans = (S[]) Array.newInstance(dummy.getClass(), queries.size());
		dfs(0, n, queries);
		return ans;
	}

	void dfs(int L, int R, ArrayList<int[]> queries) {
		if (queries.isEmpty()) return;

		if (R - L == 1) {
			for (int[] q : queries) {
				T v = strategy.mergeLeftSingle(L, identity);
				ans[q[0]] = strategy.answer(v, identity, q[0]);
			}
			return;
		}

		int M = (L + R) >>> 1;

		dp[M] = identity;
		for (int i = M - 1; i >= L; i--) {
			dp[i] = strategy.mergeLeftSingle(i, dp[i + 1]);
		}
		for (int i = M + 1; i <= R; i++) {
			dp[i] = strategy.mergeRightSingle(i - 1, dp[i - 1]);
		}

		ArrayList<int[]> left = new ArrayList<>();
		ArrayList<int[]> right = new ArrayList<>();

		for (var q : queries) {
			int id=q[0];
			int l=q[1];
			int r=q[2];
			if (r <= M) {
				left.add(q);
			} else if (M <= l) {
				right.add(q);
			} else {
				ans[id] = strategy.answer(dp[l], dp[r], id);
			}
		}

		dfs(L, M, left);
		dfs(M, R, right);
	}

	void tr(Object... o) {
		System.out.println(Arrays.deepToString(o));
	}
	
	
	public interface OfflineDSTStrategy<T,S> {

	    T identity();

	    T mergeLeftSingle(int id, T right);
	    T mergeRightSingle(int id, T left);
	    
	    S answer(T left, T right, int queryId);
	}
}

