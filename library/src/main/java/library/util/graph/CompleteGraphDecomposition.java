package library.util.graph;

/**
 * 無向完全グラフをハミルトンパス、ハミルトンサイクル、マッチングなどに分解するユーティリティクラス。
 */
public class CompleteGraphDecomposition {

    /**
     * 偶数頂点の無向完全グラフ K_N を N/2 個のハミルトンパスに分解する。
     * @param N 頂点数 (偶数であること)
     * @return N/2 個のハミルトンパス (各パスは N 頂点の配列)
     */
    public static int[][] hamiltonianPathDecomposition(int N) {
        if (!(N >= 0 && N % 2 == 0)) {
            throw new IllegalArgumentException("N must be even");
        }
        int K = N / 2;
        int[][] paths = new int[K][N];
        for (int k = 0; k < K; k++) {
            for (int n = 0; n < N; n++) {
                int x = k + (n % 2 != 0 ? (n + 1) / 2 : -(n / 2));
                x %= N;
                if (x < 0) x += N;
                paths[k][n] = x;
            }
        }
        return paths;
    }

    /**
     * 奇数頂点の無向完全グラフ K_N を (N-1)/2 個のハミルトンサイクルに分解する。
     * @param N 頂点数 (奇数であること)
     * @return (N-1)/2 個のハミルトンサイクル (各サイクルは N 頂点の配列)
     */
    public static int[][] hamiltonianCycleDecomposition(int N) {
        if (!(N >= 0 && N % 2 == 1)) {
            throw new IllegalArgumentException("N must be odd");
        }
        if (N == 1) return new int[0][0];
        int K = (N - 1) / 2;
        int[][] paths = hamiltonianPathDecomposition(N - 1);
        int[][] cycles = new int[K][N];
        for (int k = 0; k < K; k++) {
            System.arraycopy(paths[k], 0, cycles[k], 0, N - 1);
            cycles[k][N - 1] = N - 1;
        }
        return cycles;
    }

    /**
     * 偶数頂点の無向完全グラフ K_N を N-1 個の完全マッチングに分解する (辺彩色)。
     * @param N 頂点数 (偶数であること)
     * @return N-1 個の完全マッチング。各マッチングは int[N/2][2] 形式。
     */
    public static int[][][] perfectMatchingDecomposition(int N) {
        if (!(N > 0 && N % 2 == 0)) {
            throw new IllegalArgumentException("N must be positive and even");
        }
        int mod = N - 1;
        int[][][] res = new int[mod][N / 2][2];
        for (int a = 0; a < mod; a++) {
            res[a][0][0] = N - 1;
            res[a][0][1] = a;
            int x = a, y = a;
            for (int i = 1; i < N / 2; i++) {
                x--;
                y++;
                if (x < 0) x += mod;
                if (y >= mod) y -= mod;
                res[a][i][0] = x;
                res[a][i][1] = y;
            }
        }
        return res;
    }

    /**
     * 偶数頂点の無向完全グラフ K_N を N/2-1 個のハミルトンサイクルと 1 つの完全マッチングに分解する。
     * @param N 頂点数 (偶数であること)
     * @return ハミルトンサイクルのリストと完全マッチングのペア。
     */
    public static CyclesAndMatching cyclesAndMatchingDecomposition(int N) {
        if (!(N > 0 && N % 2 == 0)) {
            throw new IllegalArgumentException("N must be positive and even");
        }
        if (N == 2) {
            return new CyclesAndMatching(new int[0][0], new int[][]{{0, 1}});
        }
        int[][] cycles = new int[N / 2 - 1][N];
        int mod = N - 1;
        for (int a = 0; a < mod - 1; a++) {
            if (a % 2 == 0) {
                int[] C = cycles[a / 2];
                C[0] = a;
                for (int i = 0; i < N - 2; i++) {
                    int nxt = (i % 2 == 0 ? 2 * a + 2 - C[i] : 2 * a - C[i]);
                    nxt %= mod;
                    if (nxt < 0) nxt += mod;
                    C[i + 1] = nxt;
                }
                C[N - 1] = mod;
            }
        }
        int[] matchArray = new int[N];
        for (int a = 0; a < mod / 2; a++) {
            int b = mod - 2 - a;
            matchArray[2 * a] = a;
            matchArray[2 * a + 1] = b;
        }
        matchArray[N - 2] = N - 2;
        matchArray[N - 1] = N - 1;

        int[][] matching = new int[N / 2][2];
        for (int i = 0; i < N / 2; i++) {
            matching[i][0] = matchArray[2 * i];
            matching[i][1] = matchArray[2 * i + 1];
        }
        return new CyclesAndMatching(cycles, matching);
    }

    public static record CyclesAndMatching(int[][] cycles, int[][] matching) {}
}
