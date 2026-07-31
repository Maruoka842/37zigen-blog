package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import library.util.game.ImpartialGames;
import library.util.game.ImpartialGame;
import library.util.graph.Graph;

public class ImpartialGamesTest {

    private long bruteForceGrundy(long n, Map<Long, Long> memo) {
        if (n == 0) return 0;
        if (memo.containsKey(n)) return memo.get(n);
        Set<Long> nexts = new HashSet<>();
        for (long i = 1; i <= n / 2; i++) {
            nexts.add(bruteForceGrundy(n - i, memo));
        }
        long res = 0;
        while (nexts.contains(res)) res++;
        memo.put(n, res);
        return res;
    }

    private int bruteForceRemoteness(long n, Map<Long, Integer> rMemo, Map<Long, Long> gMemo) {
        if (n <= 1) return 0;
        if (rMemo.containsKey(n)) return rMemo.get(n);

        long g = bruteForceGrundy(n, gMemo);
        int res;
        if (g != 0) {
            int minRem = Integer.MAX_VALUE;
            for (long i = 1; i <= n / 2; i++) {
                if (bruteForceGrundy(n - i, gMemo) == 0) {
                    minRem = Math.min(minRem, bruteForceRemoteness(n - i, rMemo, gMemo));
                }
            }
            res = minRem + 1;
        } else {
            int maxRem = -1;
            for (long i = 1; i <= n / 2; i++) {
                maxRem = Math.max(maxRem, bruteForceRemoteness(n - i, rMemo, gMemo));
            }
            res = maxRem + 1;
        }
        rMemo.put(n, res);
        return res;
    }

    @Test
    public void testStaircaseNim() {
        ImpartialGames.StaircaseNim game = new ImpartialGames.StaircaseNim();

        for (int p1 = 0; p1 <= 3; p1++) {
            for (int p2 = 0; p2 <= 3; p2++) {
                for (int p3 = 0; p3 <= 3; p3++) {
                    List<Long> piles = Arrays.asList(0L, (long)p1, (long)p2, (long)p3);
                    int expectedR = bruteForceStaircaseRemoteness(piles, new HashMap<>());
                    assertEquals(expectedR, game.remoteness(piles), "Remoteness for piles=" + piles);
                }
            }
        }
    }

    private long staircaseGrundy(List<Long> piles) {
        long res = 0;
        for (int i = 1; i < piles.size(); i += 2) res ^= piles.get(i);
        return res;
    }

    private int bruteForceStaircaseRemoteness(List<Long> piles, Map<List<Long>, Integer> memo) {
        if (memo.containsKey(piles)) return memo.get(piles);
        long g = staircaseGrundy(piles);
        int res;
        if (g != 0) {
            int minRem = Integer.MAX_VALUE;
            for (int i = 1; i < piles.size(); i++) {
                for (long take = 1; take <= piles.get(i); take++) {
                    List<Long> next = new ArrayList<>(piles);
                    next.set(i, piles.get(i) - take);
                    next.set(i - 1, piles.get(i - 1) + take);
                    if (staircaseGrundy(next) == 0) {
                        minRem = Math.min(minRem, bruteForceStaircaseRemoteness(next, memo));
                    }
                }
            }
            res = minRem + 1;
        } else {
            int maxRem = -1;
            boolean canMove = false;
            for (int i = 1; i < piles.size(); i++) {
                if (piles.get(i) > 0) {
                    canMove = true;
                    for (long take = 1; take <= piles.get(i); take++) {
                        List<Long> next = new ArrayList<>(piles);
                        next.set(i, piles.get(i) - take);
                        next.set(i - 1, piles.get(i - 1) + take);
                        maxRem = Math.max(maxRem, bruteForceStaircaseRemoteness(next, memo));
                    }
                }
            }
            if (!canMove) res = 0;
            else res = maxRem + 1;
        }
        memo.put(piles, res);
        return res;
    }

    // ----------------------------- ConjunctiveSum ----------------------------- //

    /**
     * 連言和 (Conjunctive Sum) のグランディー数テスト。
     *
     * <p>G(G1 & G2) = G(G1) ⊗ G(G2) (ニム積)。
     * Nim(a) & Nim(b) のグランディー数を bruteForcegrundy で検証する。</p>
     */
    @Test
    public void testConjunctiveSumGrundy() {
        // ConjunctiveSum(Nim(a), Nim(b)) のグランディー数を mex で検証
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        List<ImpartialGame<Long>> games = Arrays.asList(nim, nim);
        ImpartialGames.ConjunctiveSum<Long> cs = new ImpartialGames.ConjunctiveSum<>(games);

        // a, b <= 4 の範囲でブルートフォースと比較
        Map<List<Long>, Long> memo = new HashMap<>();
        for (long a = 0; a <= 4; a++) {
            for (long b = 0; b <= 4; b++) {
                List<Long> state = Arrays.asList(a, b);
                long expected = bruteForceMex(cs, state, memo);
                long actual = cs.grundy(state);
                assertEquals(expected, actual,
                    "ConjunctiveSum grundy mismatch for Nim(" + a + ") & Nim(" + b + ")");

                // isWin(state) must match actual != 0 (as calculated by Sprague-Grundy values or equivalent winner logic)
                boolean expectedIsWin = actual != 0;
                assertEquals(expectedIsWin, cs.isWin(state),
                    "ConjunctiveSum isWin mismatch for Nim(" + a + ") & Nim(" + b + ")");
            }
        }
    }

    /** ブルートフォース mex による grundy 計算（テスト用ヘルパー）。 */
    private <S> long bruteForceMex(ImpartialGame<S> game, S state, Map<S, Long> memo) {
        if (memo.containsKey(state)) return memo.get(state);
        Set<Long> nextGs = new HashSet<>();
        for (S next : game.nextStates(state)) {
            nextGs.add(bruteForceMex(game, next, memo));
        }
        long res = 0;
        while (nextGs.contains(res)) res++;
        memo.put(state, res);
        return res;
    }

    /**
     * 連言和 (Conjunctive Sum) の nextStates テスト。
     *
     * <p>Nim(1) & Nim(2) の合法手は Nim(0)&Nim(1) の1通りのみ
     * （各ゲームから1手ずつ進める）。</p>
     */
    @Test
    public void testConjunctiveSumNextStates() {
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        ImpartialGames.ConjunctiveSum<Long> cs = new ImpartialGames.ConjunctiveSum<>(Arrays.asList(nim, nim));

        // Nim(1)&Nim(1): 唯一の次の状態は [0,0]
        List<List<Long>> nexts11 = new ArrayList<>();
        for (List<Long> s : cs.nextStates(Arrays.asList(1L, 1L))) nexts11.add(s);
        assertEquals(1, nexts11.size());
        assertEquals(Arrays.asList(0L, 0L), nexts11.get(0));

        // Nim(0)&Nim(2): Nim(0)は合法手なし → 合法手なし
        List<List<Long>> nexts02 = new ArrayList<>();
        for (List<Long> s : cs.nextStates(Arrays.asList(0L, 2L))) nexts02.add(s);
        assertEquals(0, nexts02.size());
    }

    // ----------------------------- SelectiveSum ----------------------------- //

    /**
     * 選択和 (Selective Sum) のグランディー数テスト。
     *
     * <p>G(G1 ∨ G2) = G(G1) | G(G2) (ビット OR)。
     * Nim(a) ∨ Nim(b) のグランディー数をブルートフォース mex で検証する。</p>
     */
    @Test
    public void testSelectiveSumGrundy() {
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        ImpartialGames.SelectiveSum<Long> ss = new ImpartialGames.SelectiveSum<>(Arrays.asList(nim, nim));

        // a, b <= 4 の範囲でブルートフォースと比較
        Map<List<Long>, Long> memo = new HashMap<>();
        for (long a = 0; a <= 4; a++) {
            for (long b = 0; b <= 4; b++) {
                List<Long> state = Arrays.asList(a, b);
                long expected = bruteForceMex(ss, state, memo);
                long actual = ss.grundy(state);
                assertEquals(expected, actual,
                    "SelectiveSum grundy mismatch for Nim(" + a + ") v Nim(" + b + ")");

                // isWin(state) must match actual != 0
                boolean expectedIsWin = actual != 0;
                assertEquals(expectedIsWin, ss.isWin(state),
                    "SelectiveSum isWin mismatch for Nim(" + a + ") v Nim(" + b + ")");
            }
        }
    }

    /**
     * 選択和 (Selective Sum) の nextStates テスト。
     *
     * <p>Nim(1) ∨ Nim(1) からは:
     * - ゲーム1のみ進める: [0, 1]
     * - ゲーム2のみ進める: [1, 0]
     * - 両方進める:        [0, 0]
     * の3通り。</p>
     */
    @Test
    public void testSelectiveSumNextStates() {
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        ImpartialGames.SelectiveSum<Long> ss = new ImpartialGames.SelectiveSum<>(Arrays.asList(nim, nim));

        Set<List<Long>> nexts = new HashSet<>();
        for (List<Long> s : ss.nextStates(Arrays.asList(1L, 1L))) nexts.add(s);

        assertEquals(3, nexts.size());
        assertTrue(nexts.contains(Arrays.asList(0L, 1L)));
        assertTrue(nexts.contains(Arrays.asList(1L, 0L)));
        assertTrue(nexts.contains(Arrays.asList(0L, 0L)));
    }

    // ---------------------- ContinuedConjunctiveSum ----------------------- //

    /**
     * 継続連言和 (Continued Conjunctive Sum) のグランディー数テスト。
     *
     * <p>G(CCS(G1, ..., Gn)) = XOR of G(Gi) for running Gi.
     * Nim(a) & Nim(b) (continued) のグランディー数をブルートフォース mex で検証する。</p>
     */
    @Test
    public void testContinuedConjunctiveSumGrundy() {
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        ImpartialGames.ContinuedConjunctiveSum<Long> ccs =
            new ImpartialGames.ContinuedConjunctiveSum<>(Arrays.asList(nim, nim));

        // a, b <= 4 の範囲でブルートフォースと比較
        Map<List<Long>, Long> memo = new HashMap<>();
        for (long a = 0; a <= 4; a++) {
            for (long b = 0; b <= 4; b++) {
                List<Long> state = Arrays.asList(a, b);
                long expected = bruteForceMex(ccs, state, memo);
                long actual = ccs.grundy(state);
                assertEquals(expected, actual,
                    "ContinuedConjunctiveSum grundy mismatch for Nim(" + a + ") && Nim(" + b + ")");

                // isWin(state) must match actual != 0
                boolean expectedIsWin = actual != 0;
                assertEquals(expectedIsWin, ccs.isWin(state),
                    "ContinuedConjunctiveSum isWin mismatch for Nim(" + a + ") && Nim(" + b + ")");
            }
        }
    }

    /**
     * 継続連言和 (Continued Conjunctive Sum) の nextStates テスト。
     *
     * <p>CCS(Nim(2), Nim(0)):
     * - Nim(0)は終了済み → Nim(2)のみ1手進める
     * - 次の状態: [0, 0] または [1, 0]
     * </p>
     */
    @Test
    public void testContinuedConjunctiveSumNextStates() {
        ImpartialGames.Nim nim = new ImpartialGames.Nim();
        ImpartialGames.ContinuedConjunctiveSum<Long> ccs =
            new ImpartialGames.ContinuedConjunctiveSum<>(Arrays.asList(nim, nim));

        // CCS(Nim(2), Nim(0)): Nim(0) は終了、Nim(2) のみ動く
        Set<List<Long>> nexts20 = new HashSet<>();
        for (List<Long> s : ccs.nextStates(Arrays.asList(2L, 0L))) nexts20.add(s);
        assertEquals(2, nexts20.size());
        assertTrue(nexts20.contains(Arrays.asList(0L, 0L)));
        assertTrue(nexts20.contains(Arrays.asList(1L, 0L)));

        // CCS(Nim(1), Nim(1)): 両方動く → デカルト積 [0,0] のみ
        Set<List<Long>> nexts11 = new HashSet<>();
        for (List<Long> s : ccs.nextStates(Arrays.asList(1L, 1L))) nexts11.add(s);
        assertEquals(1, nexts11.size());
        assertTrue(nexts11.contains(Arrays.asList(0L, 0L)));

        // CCS(Nim(0), Nim(0)): 全ゲーム終了 → 合法手なし
        List<List<Long>> nexts00 = new ArrayList<>();
        for (List<Long> s : ccs.nextStates(Arrays.asList(0L, 0L))) nexts00.add(s);
        assertEquals(0, nexts00.size());
    }

    @Test
    public void testEdgeGeography() {
        ImpartialGames.EdgeGeography game = new ImpartialGames.EdgeGeography();

        // Triangle graph
        Graph triangle = new Graph(3);
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);

        ImpartialGames.EdgeGeography.State state = new ImpartialGames.EdgeGeography.State(0, triangle);

        // From 0, can go to 1 or 2.
        // If 0 -> 1, remaining edges are (1,2) and (2,0), current vertex 1.
        // From 1, must go to 2. Remaining edge (2,0), current vertex 2.
        // From 2, must go to 0. No remaining edges, current vertex 0.
        // Total 3 moves ->先手勝ち

        assertTrue(game.isWin(state));
        assertEquals(3, game.remoteness(state));

        // Path graph 0-1-2
        Graph path = new Graph(3);
        path.addEdge(0, 1);
        path.addEdge(1, 2);

        ImpartialGames.EdgeGeography.State pathState = new ImpartialGames.EdgeGeography.State(0, path);
        // 0 -> 1 -> 2. 2 moves -> 後手勝ち
        assertFalse(game.isWin(pathState));
        assertEquals(2, game.remoteness(pathState));

        ImpartialGames.EdgeGeography.State pathState1 = new ImpartialGames.EdgeGeography.State(1, path);
        // 1 -> 0 or 1 -> 2. 1 move -> 先手勝ち
        assertTrue(game.isWin(pathState1));
        assertEquals(1, game.remoteness(pathState1));
    }
}
