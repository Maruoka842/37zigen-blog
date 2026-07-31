package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import library.util.graph.Edge;

import library.util.algebra.instance.FractionFieldElement;
import library.util.graph.LongValueDigraph;
import library.util.graph.TransferMatrixMethod;
import library.util.polynomial.PolynomialFpDynamic;
import org.junit.jupiter.api.Test;

public class TransferMatrixMethodTest {

	@Test
	public void testRandomGraphs() {
		Random rnd = new Random(42);
		long mod = 998244353;
		PolynomialFpDynamic poly = new PolynomialFpDynamic(mod, 3);

		for (int t = 0; t < 20; t++) {
			int N = rnd.nextInt(5) + 2;
			int M = rnd.nextInt(N * N);
			LongValueDigraph g = new LongValueDigraph(N);
			for (int i = 0; i < M; i++) {
				g.addEdge(rnd.nextInt(N), rnd.nextInt(N), rnd.nextInt(100));
			}

			// Test fixed walk
			int src = rnd.nextInt(N);
			int dst = rnd.nextInt(N);
			var resFixed = TransferMatrixMethod.fixedWalkGeneratingFunction(g, src, dst, mod);
			long[] naiveFixed = calculateNaiveWalks(g, src, dst, 2 * N, mod);
			long[] extractedFixed = extractCoefficients(resFixed, 2 * N, poly);
			assertArrayEquals(naiveFixed, extractedFixed, "Fixed walk failed at trial " + t);

			// Test fixed start walk
			var resFixedStart = TransferMatrixMethod.fixedStartWalkGeneratingFunction(g, src, mod);
			long[] naiveFixedStart = calculateNaiveFixedStartWalks(g, src, 2 * N, mod);
			long[] extractedFixedStart = extractCoefficients(resFixedStart, 2 * N, poly);
			assertArrayEquals(naiveFixedStart, extractedFixedStart, "Fixed start walk failed at trial " + t);

			// Test free walk
			var resFree = TransferMatrixMethod.freeWalkGeneratingFunction(g, mod);
			long[] naiveFree = calculateNaiveFreeWalks(g, 2 * N, mod);
			long[] extractedFree = extractCoefficients(resFree, 2 * N, poly);
			assertArrayEquals(naiveFree, extractedFree, "Free walk failed at trial " + t);

			// Test closed walk
			var resClosed = TransferMatrixMethod.closedWalkGeneratingFunction(g, mod);
			long[] naiveClosed = calculateNaiveClosedWalks(g, 2 * N, mod);
			long[] extractedClosed = extractCoefficients(resClosed, 2 * N, poly);
			assertArrayEquals(naiveClosed, extractedClosed, "Closed walk failed at trial " + t);
		}
	}

	private long[] calculateNaiveFixedStartWalks(LongValueDigraph g, int src, int len, long mod) {
		long[] res = new long[len];
		long[] dp = new long[g.N];
		dp[src] = 1;
		for (int n = 0; n < len; n++) {
			long sum = 0;
			for (int i = 0; i < g.N; i++) sum = (sum + dp[i]) % mod;
			res[n] = sum;
			long[] nextDp = new long[g.N];
			for (int u = 0; u < g.N; u++) {
				if (dp[u] == 0) continue;
				for (var e : g.adj[u]) {
					nextDp[e.dst] = (nextDp[e.dst] + dp[u] * (e.cost % mod)) % mod;
				}
			}
			dp = nextDp;
		}
		return res;
	}

	private long[] calculateNaiveWalks(LongValueDigraph g, int src, int dst, int len, long mod) {
		long[] res = new long[len];
		long[] dp = new long[g.N];
		dp[src] = 1;
		for (int n = 0; n < len; n++) {
			res[n] = dp[dst];
			long[] nextDp = new long[g.N];
			for (int u = 0; u < g.N; u++) {
				if (dp[u] == 0) continue;
				for (var e : g.adj[u]) {
					nextDp[e.dst] = (nextDp[e.dst] + dp[u] * (e.cost % mod)) % mod;
				}
			}
			dp = nextDp;
		}
		return res;
	}

	private long[] calculateNaiveFreeWalks(LongValueDigraph g, int len, long mod) {
		long[] res = new long[len];
		long[] dp = new long[g.N];
		for (int i = 0; i < g.N; i++) dp[i] = 1;
		for (int n = 0; n < len; n++) {
			long sum = 0;
			for (int i = 0; i < g.N; i++) sum = (sum + dp[i]) % mod;
			res[n] = sum;
			long[] nextDp = new long[g.N];
			for (int u = 0; u < g.N; u++) {
				if (dp[u] == 0) continue;
				for (var e : g.adj[u]) {
					nextDp[e.dst] = (nextDp[e.dst] + dp[u] * (e.cost % mod)) % mod;
				}
			}
			dp = nextDp;
		}
		return res;
	}

	private long[] calculateNaiveClosedWalks(LongValueDigraph g, int len, long mod) {
		long[] res = new long[len];
		int N = g.N;
		long[][] A = new long[N][N];
		for (int u = 0; u < N; u++) {
			for (var e : g.adj[u]) {
				A[u][e.dst] = (A[u][e.dst] + (e.cost % mod + mod) % mod) % mod;
			}
		}

		long[][] curr = new long[N][N];
		for (int i = 0; i < N; i++) curr[i][i] = 1;

		for (int n = 0; n < len; n++) {
			long tr = 0;
			if (n > 0) {
				for (int i = 0; i < N; i++) tr = (tr + curr[i][i]) % mod;
			}
			res[n] = tr;

			long[][] next = new long[N][N];
			for (int i = 0; i < N; i++) {
				for (int k = 0; k < N; k++) {
					if (curr[i][k] == 0) continue;
					for (int j = 0; j < N; j++) {
						next[i][j] = (next[i][j] + curr[i][k] * A[k][j]) % mod;
					}
				}
			}
			curr = next;
		}
		return res;
	}

	private long[] extractCoefficients(FractionFieldElement<long[]> gf, int len, PolynomialFpDynamic poly) {
		long[] num = gf.num();
		long[] den = gf.den();
		long[] invDen = poly.inv(java.util.Arrays.copyOf(den, len));
		long[] res = poly.mul(num, invDen);
		return java.util.Arrays.copyOf(res, len);
	}

}
