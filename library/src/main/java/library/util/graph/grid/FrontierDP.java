package library.util.graph.grid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;

/**
 * 輪郭線DP (Frontier DP / Profile DP) を用いて、グリッド上の様々な連結・非交差パス・サイクル数え上げ問題を解くための統一ライブラリ。
 * 状態遷移、境界情報の管理に Frontier4、Frontier8、PathFrontier4、PathFrontier8 を使用します。
 *
 * @author Jules
 */
public final class FrontierDP {

    private FrontierDP() {}

    /**
     * グリッド上の黒マスの連結成分の数え上げを指定された法を用いて行う。
     * 計算量: O(N * M * (状態数))
     *
     * @param N             グリッドの行数
     * @param M             グリッドの列数
     * @param connectivity8 8連結とする場合はtrue、4連結の場合はfalse
     * @param mod           割る法（modulo）
     * @return 適合パターンの個数 (mod `mod`)
     */
    // 未テスト
    public static long countBlackConnectivity(int N, int M, boolean connectivity8, long mod) {
        if (N <= 0 || M <= 0) return 0;
        if (N < M) {
            int t = N; N = M; M = t;
        }
        final int finalM = M;
        if (connectivity8) {
            Map<Frontier8, Long> dp = new HashMap<>();
            dp.put(Frontier8.getInitialState(finalM), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<Frontier8, Long> nextDp = new HashMap<>();
                    for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                        Frontier8 state = entry.getKey();
                        long count = entry.getValue();

                        // 白マス（新頂点を使用しない）
                        Frontier8 f1 = state.nextWithoutVertex();
                        if (f1.deadEnds <= 1) {
                            nextDp.merge(f1, count, (v1, v2) -> (v1 + v2) % mod);
                        }

                        // 黒マス（新頂点を使用する）
                        Frontier8.Builder builder = state.startVertex();
                        boolean connected = false;
                        connected |= builder.connect(Frontier8.Direction.LEFT);
                        connected |= builder.connect(Frontier8.Direction.UP);
                        connected |= builder.connect(Frontier8.Direction.TOP_LEFT);
                        connected |= builder.connect(Frontier8.Direction.TOP_RIGHT);

                        Frontier8 f2 = builder.build();
                        if (f2 != null) {
                            if (f2.deadEnds > 1 || (f2.deadEnds > 0 && !connected)) {
                                continue;
                            }
                            nextDp.merge(f2, count, (v1, v2) -> (v1 + v2) % mod);
                        }
                    }
                    dp = nextDp;
                }

                if (r < N - 1) {
                    Map<Frontier8, Long> shiftedDp = new HashMap<>();
                    for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                        Frontier8 state = entry.getKey();
                        long count = entry.getValue();
                        Frontier8 shiftedState = state.shift();
                        if (shiftedState != null && shiftedState.deadEnds <= 1) {
                            shiftedDp.merge(shiftedState, count, (v1, v2) -> (v1 + v2) % mod);
                        }
                    }
                    dp = shiftedDp;
                }
            }

            long ans = 0;
            for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                Frontier8 state = entry.getKey();
                long count = entry.getValue();
                int activeCount = state.getActiveComponentCount();
                long finalVal = 0;
                if (state.deadEnds > 0) {
                    finalVal = (activeCount == 0) ? count : 0;
                } else {
                    finalVal = (activeCount <= 1) ? count : 0;
                }
                ans = (ans + finalVal) % mod;
            }
            return ans;
        } else {
            Map<Frontier4, Long> dp = new HashMap<>();
            dp.put(Frontier4.getInitialState(finalM), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<Frontier4, Long> nextDp = new HashMap<>();
                    for (Map.Entry<Frontier4, Long> entry : dp.entrySet()) {
                        Frontier4 state = entry.getKey();
                        long count = entry.getValue();

                        // 白マス（新頂点を使用しない）
                        Frontier4 f1 = state.nextWithoutVertex();
                        if (f1 != null && f1.deadEnds <= 1) {
                            nextDp.merge(f1, count, (v1, v2) -> (v1 + v2) % mod);
                        }

                        // 黒マス（新頂点を使用する）
                        Frontier4.Builder builder = state.startVertex();
                        boolean connected = false;
                        connected |= builder.connect(Frontier4.Direction.LEFT);
                        connected |= builder.connect(Frontier4.Direction.UP);

                        Frontier4 f2 = builder.build();
                        if (f2 != null) {
                            if (f2.deadEnds > 1 || (f2.deadEnds > 0 && !connected)) {
                                continue;
                            }
                            nextDp.merge(f2, count, (v1, v2) -> (v1 + v2) % mod);
                        }
                    }
                    dp = nextDp;
                }
            }

            long ans = 0;
            for (Map.Entry<Frontier4, Long> entry : dp.entrySet()) {
                Frontier4 state = entry.getKey();
                long count = entry.getValue();
                int activeCount = state.getComponentCount();
                long finalVal = 0;
                if (state.deadEnds > 0) {
                    finalVal = (activeCount == 0) ? count : 0;
                } else {
                    finalVal = (activeCount <= 1) ? count : 0;
                }
                ans = (ans + finalVal) % mod;
            }
            return ans;
        }
    }

    // ==========================================
    // 2. 左上から右下への黒マスのパス数え上げストラテジー
    // ==========================================

    /**
     * 左上から右下へのパス数え上げにおける状態を表すレコード。
     *
     * @param frontier 輪郭線の状態
     * @param startId  開始位置ID
     * @param reached  到達フラグ
     */
    public record PathState(PathFrontier8 frontier, byte startId, boolean reached) {}

    /**
     * 左上から右下への黒マスのパスの個数を計算する。
     * 計算量: O(H * W * (状態数))
     *
     * @param H   グリッドの行数
     * @param W   グリッドの列数
     * @param mod 法
     * @return 適合パターンの個数 (mod `mod`)
     */
    // 未テスト
    public static long solvePathTopLeftToBottomRight(int H, int W, long mod) {
        if (H <= 0 || W <= 0) return 0;
        final int finalW = W;

        Map<PathState, Long> dp = new HashMap<>();
        dp.put(new PathState(PathFrontier8.getInitialState(finalW), (byte) 0, false), 1L);

        for (int r = 0; r < H; r++) {
            for (int c = 0; c < W; c++) {
                Map<PathState, Long> nextDp = new HashMap<>();
                boolean isLastCell = (r == H - 1 && c == finalW - 1);

                for (Map.Entry<PathState, Long> entry : dp.entrySet()) {
                    PathState state = entry.getKey();
                    long count = entry.getValue();

                    // 白 (不使用)
                    if (r != 0 || c != 0) {
                        if (!isLastCell) {
                            PathFrontier8 f1 = state.frontier().nextWithoutVertex(state.startId());
                            if (f1 != null && f1.deadEnds == 0) {
                                byte[] parentBefore = state.frontier().parent.clone();
                                parentBefore[c] = 0;
                                byte nextStartId = getCanonicalStartId(parentBefore, state.startId());
                                if (nextStartId > 0 || state.reached()) {
                                    nextDp.merge(new PathState(f1, nextStartId, state.reached()), count, (v1, v2) -> (v1 + v2) % mod);
                                }
                            }
                        }
                    }

                    // 黒 (使用)
                    {
                        PathFrontier8.Builder builder = state.frontier().startVertex();
                        builder.connect(PathFrontier8.Direction.LEFT, state.startId());
                        builder.connect(PathFrontier8.Direction.UP, state.startId());

                        byte[] parentBefore = builder.parent.clone();
                        byte preStartId = 0;
                        if (r == 0 && c == 0) {
                            preStartId = parentBefore[c];
                        } else {
                            preStartId = state.startId();
                        }

                        PathFrontier8 f2 = builder.build(preStartId);
                        if (f2 != null && f2.cycles == 0 && f2.deadEnds == 0) {
                            byte nextStartId = getCanonicalStartId(parentBefore, preStartId);
                            boolean nextReached = state.reached();
                            if (isLastCell) {
                                if (preStartId == parentBefore[c] && preStartId > 0) {
                                    nextReached = true;
                                }
                            }
                            if (nextStartId > 0 || nextReached) {
                                nextDp.merge(new PathState(f2, nextStartId, nextReached), count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }
                    }
                }
                dp = nextDp;
            }

            if (r < H - 1) {
                Map<PathState, Long> shiftedDp = new HashMap<>();
                for (Map.Entry<PathState, Long> entry : dp.entrySet()) {
                    PathState state = entry.getKey();
                    long count = entry.getValue();
                    byte pW = state.frontier().parent[finalW];
                    if (pW == state.startId() && pW > 0) {
                        boolean existsOther = false;
                        for (int j = 0; j < finalW; j++) {
                            if (state.frontier().parent[j] == state.startId()) {
                                existsOther = true;
                                break;
                            }
                        }
                        if (!existsOther && !state.reached()) {
                            continue;
                        }
                    }
                    PathFrontier8 shifted = state.frontier().shift(state.startId());
                    if (shifted != null && shifted.deadEnds == 0) {
                        shiftedDp.merge(new PathState(shifted, state.startId(), state.reached()), count, (v1, v2) -> (v1 + v2) % mod);
                    }
                }
                dp = shiftedDp;
            }
        }

        long ans = 0;
        for (Map.Entry<PathState, Long> entry : dp.entrySet()) {
            PathState state = entry.getKey();
            long count = entry.getValue();
            if (state.reached()) {
                ans = (ans + count) % mod;
            }
        }
        return ans;
    }

    private static byte getCanonicalStartId(byte[] parentBefore, byte startId) {
        if (startId == 0) return 0;
        byte[] map = new byte[256];
        byte nextId = 1;
        for (int i = 0; i < parentBefore.length; i++) {
            if (parentBefore[i] > 0) {
                int idx = parentBefore[i] & 0xFF;
                if (map[idx] == 0) {
                    map[idx] = nextId++;
                }
            }
        }
        return map[startId & 0xFF];
    }

    // ==========================================
    // サイクル／ハミルトンサイクル数え上げストラテジー
    // ==========================================

    /**
     * 4近傍サイクルDPにおける状態を表すレコード。
     *
     * @param frontier 輪郭線の状態
     * @param hasCycle サイクル形成フラグ
     */
    public record CycleState4(PathFrontier4 frontier, boolean hasCycle) {}

    /**
     * 8近傍サイクルDPにおける状態を表すレコード。
     *
     * @param frontier 輪郭線の状態
     * @param hasCycle サイクル形成フラグ
     */
    public record CycleState8(PathFrontier8 frontier, boolean hasCycle) {}

    private static long solveCycleGeneric(int N, int M, boolean connectivity8, boolean isHamiltonian, long mod) {
        if (N <= 0 || M <= 0) return 0;
        if (N < M) {
            int t = N; N = M; M = t;
        }
        final int finalM = M;
        if (connectivity8) {
            Map<CycleState8, Long> dp = new HashMap<>();
            dp.put(new CycleState8(PathFrontier8.getInitialState(finalM), false), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<CycleState8, Long> nextDp = new HashMap<>();
                    for (Map.Entry<CycleState8, Long> entry : dp.entrySet()) {
                        CycleState8 state = entry.getKey();
                        long count = entry.getValue();
                        byte pRetire = state.frontier.parent[c];

                        // 1. Not in cycle (degree 0) - only if !isHamiltonian and pRetire == 0
                        if (!isHamiltonian && pRetire == 0) {
                            PathFrontier8 f1 = state.frontier.nextWithoutVertex();
                            if (f1 != null && f1.deadEnds == 0 && f1.cycles == state.frontier().cycles) {
                                nextDp.merge(new CycleState8(f1, state.hasCycle()), count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }

                        // 2. In cycle (degree 2)
                        java.util.List<PathFrontier8.Direction> directions = new java.util.ArrayList<>();
                        directions.add(PathFrontier8.Direction.LEFT);
                        directions.add(PathFrontier8.Direction.UP);
                        directions.add(PathFrontier8.Direction.TOP_LEFT);
                        directions.add(PathFrontier8.Direction.TOP_RIGHT);

                        Iterable<PathFrontier8> possible = state.frontier.getPossibleConnections(
                            directions.toArray(new PathFrontier8.Direction[0])
                        );
                        for (PathFrontier8 nextF : possible) {
                            if (nextF.deadEnds == 0 && nextF.cycles <= 1) {
                                boolean nextHasCycle = (nextF.cycles > 0);
                                nextDp.merge(new CycleState8(nextF, nextHasCycle), count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }
                    }
                    dp = nextDp;
                }

                if (r < N - 1) {
                    Map<CycleState8, Long> shiftedDp = new HashMap<>();
                    for (Map.Entry<CycleState8, Long> entry : dp.entrySet()) {
                        CycleState8 state = entry.getKey();
                        long count = entry.getValue();
                        if (state.frontier().parent[finalM] > 0) {
                            continue;
                        }
                        PathFrontier8 shiftedF = state.frontier().shift();
                        if (shiftedF != null && shiftedF.deadEnds == 0 && shiftedF.cycles <= 1) {
                            shiftedDp.merge(new CycleState8(shiftedF, state.hasCycle()), count, (v1, v2) -> (v1 + v2) % mod);
                        }
                    }
                    dp = shiftedDp;
                }
            }

            long ans = 0;
            for (Map.Entry<CycleState8, Long> entry : dp.entrySet()) {
                CycleState8 state = entry.getKey();
                long count = entry.getValue();
                if (state.hasCycle()) {
                    boolean empty = true;
                    for (byte b : state.frontier.parent) {
                        if (b != 0) {
                            empty = false;
                            break;
                        }
                    }
                    if (empty) {
                        ans = (ans + count) % mod;
                    }
                }
            }
            return ans;
        } else {
            Map<CycleState4, Long> dp = new HashMap<>();
            dp.put(new CycleState4(PathFrontier4.getInitialState(finalM), false), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<CycleState4, Long> nextDp = new HashMap<>();
                    for (Map.Entry<CycleState4, Long> entry : dp.entrySet()) {
                        CycleState4 state = entry.getKey();
                        long count = entry.getValue();
                        byte pRetire = state.frontier.parent[c];

                        // 1. Not in cycle (degree 0) - only if !isHamiltonian and pRetire == 0
                        if (!isHamiltonian && pRetire == 0) {
                            PathFrontier4 f1 = state.frontier.nextWithoutVertex();
                            if (f1 != null && f1.deadEnds == 0 && f1.cycles == state.frontier().cycles) {
                                nextDp.merge(new CycleState4(f1, state.hasCycle()), count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }

                        // 2. In cycle (degree 2)
                        java.util.List<PathFrontier4.Direction> directions = new java.util.ArrayList<>();
                        directions.add(PathFrontier4.Direction.LEFT);
                        directions.add(PathFrontier4.Direction.UP);

                        Iterable<PathFrontier4> possible = state.frontier.getPossibleConnections(
                            directions.toArray(new PathFrontier4.Direction[0])
                        );
                        for (PathFrontier4 nextF : possible) {
                            if (nextF.deadEnds == 0 && nextF.cycles <= 1) {
                                boolean nextHasCycle = (nextF.cycles > 0);
                                nextDp.merge(new CycleState4(nextF, nextHasCycle), count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }
                    }
                    dp = nextDp;
                }
            }

            long ans = 0;
            for (Map.Entry<CycleState4, Long> entry : dp.entrySet()) {
                CycleState4 state = entry.getKey();
                long count = entry.getValue();
                if (state.hasCycle()) {
                    boolean empty = true;
                    for (byte b : state.frontier.parent) {
                        if (b != 0) {
                            empty = false;
                            break;
                        }
                    }
                    if (empty) {
                        ans = (ans + count) % mod;
                    }
                }
            }
            return ans;
        }
    }

    /**
     * サイクル数を計算する。
     * 計算量: O(N * M * (状態数))
     *
     * @param N             行数
     * @param M             列数
     * @param connectivity8 8連結ならtrue
     * @param mod           法
     * @return 適合パターンの個数 (mod `mod`)
     */
    // 未テスト
    public static long solveCycleCount(int N, int M, boolean connectivity8, long mod) {
        return solveCycleGeneric(N, M, connectivity8, false, mod);
    }

    /**
     * ハミルトンサイクル数を計算する。
     * 計算量: O(N * M * (状態数))
     *
     * @param N             行数
     * @param M             列数
     * @param connectivity8 8連結ならtrue
     * @param mod           法
     * @return 適合パターンの個数 (mod `mod`)
     */
    // 未テスト
    public static long solveHamiltonianCycleCount(int N, int M, boolean connectivity8, long mod) {
        return solveCycleGeneric(N, M, connectivity8, true, mod);
    }

    /**
     * サイクルを含まない連結部分集合（木）の数え上げを指定された法を用いて行う。
     * 計算量: O(N * M * (状態数))
     *
     * @param N             グリッドの行数
     * @param M             グリッドの列数
     * @param connectivity8 8連結とする場合はtrue、4連結の場合はfalse
     * @param mod           割る法（modulo）
     * @return 適合パターンの個数 (mod `mod`)
     */
    // 未テスト
    public static long solveTreeCount(int N, int M, boolean connectivity8, long mod) {
        if (N <= 0 || M <= 0) return 0;
        if (N < M) {
            int tmp = N; N = M; M = tmp;
        }
        final int finalM = M;
        if (connectivity8) {
            Map<Frontier8, Long> dp = new HashMap<>();
            dp.put(Frontier8.getInitialState(finalM), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<Frontier8, Long> nextDp = new HashMap<>();
                    for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                        Frontier8 state = entry.getKey();
                        long count = entry.getValue();

                        // 不使用
                        Frontier8 f1 = state.nextWithoutVertex();
                        nextDp.merge(f1, count, (v1, v2) -> (v1 + v2) % mod);

                        // 使用（connect）
                        java.util.List<Frontier8.Direction> directions = new java.util.ArrayList<>();
                        directions.add(Frontier8.Direction.LEFT);
                        directions.add(Frontier8.Direction.UP);
                        directions.add(Frontier8.Direction.TOP_LEFT);
                        directions.add(Frontier8.Direction.TOP_RIGHT);

                        Iterable<Frontier8> possible = state.getPossibleConnections(
                            directions.toArray(new Frontier8.Direction[0])
                        );
                        for (Frontier8 nextF : possible) {
                            if (nextF.cycles == 0) {
                                int currentCellIdx = (nextF.c == 0) ? nextF.parent.length - 1 : nextF.c - 1;
                                byte id = nextF.parent[currentCellIdx];
                                int size = 0;
                                for (byte val : nextF.parent) {
                                    if (val == id) size++;
                                }
                                boolean connected = (size > 1);

                                if (nextF.deadEnds > 1 || (nextF.deadEnds > 0 && !connected)) {
                                    continue;
                                }
                                nextDp.merge(nextF, count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }
                    }
                    dp = nextDp;
                }

                if (r < N - 1) {
                    Map<Frontier8, Long> shiftedDp = new HashMap<>();
                    for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                        Frontier8 state = entry.getKey();
                        long count = entry.getValue();
                        Frontier8 shiftedState = state.shift();
                        if (shiftedState != null && shiftedState.cycles == 0 && shiftedState.deadEnds <= 1) {
                            shiftedDp.merge(shiftedState, count, (v1, v2) -> (v1 + v2) % mod);
                        }
                    }
                    dp = shiftedDp;
                }
            }

            long ans = 0;
            for (Map.Entry<Frontier8, Long> entry : dp.entrySet()) {
                Frontier8 state = entry.getKey();
                long count = entry.getValue();
                int activeCount = state.getActiveComponentCount();
                long finalVal = 0;
                if (state.deadEnds > 0) {
                    finalVal = (activeCount == 0) ? count : 0;
                } else {
                    finalVal = (activeCount <= 1) ? count : 0;
                }
                ans = (ans + finalVal) % mod;
            }
            return ans;
        } else {
            Map<Frontier4, Long> dp = new HashMap<>();
            dp.put(Frontier4.getInitialState(finalM), 1L);

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    Map<Frontier4, Long> nextDp = new HashMap<>();
                    for (Map.Entry<Frontier4, Long> entry : dp.entrySet()) {
                        Frontier4 state = entry.getKey();
                        long count = entry.getValue();

                        // 不使用
                        Frontier4 f1 = state.nextWithoutVertex();
                        if (f1 != null && f1.cycles == 0 && f1.deadEnds <= 1) {
                            nextDp.merge(f1, count, (v1, v2) -> (v1 + v2) % mod);
                        }

                        // 使用（connect）
                        java.util.List<Frontier4.Direction> directions = new java.util.ArrayList<>();
                        directions.add(Frontier4.Direction.LEFT);
                        directions.add(Frontier4.Direction.UP);

                        Iterable<Frontier4> possible = state.getPossibleConnections(
                            directions.toArray(new Frontier4.Direction[0])
                        );
                        for (Frontier4 nextF : possible) {
                            if (nextF.cycles == 0) {
                                int currentCellIdx = (nextF.c == 0) ? nextF.parent.length - 1 : nextF.c - 1;
                                byte id = nextF.parent[currentCellIdx];
                                int size = 0;
                                for (byte val : nextF.parent) {
                                    if (val == id) size++;
                                }
                                boolean connected = (size > 1);

                                if (nextF.deadEnds > 1 || (nextF.deadEnds > 0 && !connected)) {
                                    continue;
                                }
                                nextDp.merge(nextF, count, (v1, v2) -> (v1 + v2) % mod);
                            }
                        }
                    }
                    dp = nextDp;
                }
            }

            long ans = 0;
            for (Map.Entry<Frontier4, Long> entry : dp.entrySet()) {
                Frontier4 state = entry.getKey();
                long count = entry.getValue();
                int activeCount = state.getComponentCount();
                long finalVal = 0;
                if (state.deadEnds > 0) {
                    finalVal = (activeCount == 0) ? count : 0;
                } else {
                    finalVal = (activeCount <= 1) ? count : 0;
                }
                ans = (ans + finalVal) % mod;
            }
            return ans;
        }
    }
}
