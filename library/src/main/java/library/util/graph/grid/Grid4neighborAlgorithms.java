package library.util.graph.grid;

import java.util.ArrayDeque;
import java.util.Queue;
import library.util.ArrayUtils;

/**
 * 4近傍の格子グラフ（グリッド）上での探索アルゴリズムを提供するクラス。
 * すべてのメソッドは静的（static）メソッドであり、インスタンス化は不要である。
 * マス間の境界に壁（V, H）が存在する状況下での通常の幅優先探索（BFS）および滑る床（icefloor）BFSをサポートする。
 *
 * V[i][j] はマス (i, j) とマス (i, j+1) の間に壁があるか (true / false) を表す。
 * H[i][j] はマス (i, j) とマス (i+1, j) の間に壁があるか (true / false) を表す。
 */
public final class Grid4neighborAlgorithms {

    // インスタンス化を防ぐためのプライベートコンストラクタ
    private Grid4neighborAlgorithms() {}

    /** 方向ベクトル：下、左、上、右（反時計回り） */
    public static final int[] dh = {1, 0, -1, 0};
    public static final int[] dw = {0, -1, 0, 1};

    /**
     * マス (r, c) から方向 dir への移動が可能であるかを判定する。
     *
     * @param r 移動元の行インデックス
     * @param c 移動元の列インデックス
     * @param dir 移動方向（0: 下, 1: 左, 2: 上, 3: 右）
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報
     * @return 移動可能であれば true、そうでなければ false
     */
    // 未テスト
    private static boolean canMove(int r, int c, int dir, int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle) {
        int nr = r + dh[dir];
        int nc = c + dw[dir];
        if (nr < 0 || nr >= R || nc < 0 || nc >= C) {
            return false;
        }
        if (isObstacle != null && isObstacle[nr][nc]) {
            return false;
        }
        if (dir == 0) { // 下
            if (H != null && r < H.length && c < H[r].length && H[r][c]) return false;
        } else if (dir == 1) { // 左
            if (V != null && r < V.length && c - 1 >= 0 && c - 1 < V[r].length && V[r][c - 1]) return false;
        } else if (dir == 2) { // 上
            if (H != null && r - 1 >= 0 && r - 1 < H.length && c < H[r - 1].length && H[r - 1][c]) return false;
        } else if (dir == 3) { // 右
            if (V != null && r < V.length && c < V[r].length && V[r][c]) return false;
        }
        return true;
    }

    /**
     * 始点 (si, sj) から各マスへの最短距離を通常のBFSにより計算する。
     * 壁（V, H）およびセル障害物（isObstacle）による通行不能を考慮する。
     * 到達不能なマスの最短距離は Integer.MAX_VALUE となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return 各マスへの最短距離を表す R x C の2次元配列
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] bfs(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj) {
        int[][] dist = new int[R][C];
        int INF = Integer.MAX_VALUE;
        ArrayUtils.fill(dist, INF);
        if (si < 0 || si >= R || sj < 0 || sj >= C) {
            return dist;
        }
        if (isObstacle != null && isObstacle[si][sj]) {
            return dist;
        }

        Queue<int[]> que = new ArrayDeque<>();
        dist[si][sj] = 0;
        que.add(new int[]{si, sj});

        while (!que.isEmpty()) {
            int[] curr = que.poll();
            int r = curr[0];
            int c = curr[1];

            for (int dir = 0; dir < 4; dir++) {
                if (canMove(r, c, dir, R, C, V, H, isObstacle)) {
                    int nr = r + dh[dir];
                    int nc = c + dw[dir];
                    if (dist[nr][nc] == INF) {
                        dist[nr][nc] = dist[r][c] + 1;
                        que.add(new int[]{nr, nc});
                    }
                }
            }
        }
        return dist;
    }

    /**
     * 始点 (si, sj) から各マスへの最短距離を通常のBFSにより計算する。
     * 壁（V, H）による通行不能を考慮する。
     * 到達不能なマスの最短距離は Integer.MAX_VALUE となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return 各マスへの最短距離を表す R x C の2次元配列
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] bfs(int R, int C, boolean[][] V, boolean[][] H, int si, int sj) {
        return bfs(R, C, V, H, null, si, sj);
    }

    /**
     * 始点 (si, sj) からの滑る床（icefloor）上での最短距離を01-BFSにより計算する。
     * 各マスにおいて、移動を開始した方向（0: 下, 1: 左, 2: 上, 3: 右）ごとに到着距離を求める。
     *
     * 一度進むと壁（V, H）や障害物、またはグリッドの境界に衝突するまで止まれない。
     * 移動中に止まらずに通過した場合、その方向から到着したとして
     * 状態（方向、行、列）の距離を求める。
     *
     * 初期状態は始点 (si, sj) からある方向に移動を開始しているとして、
     * すべての移動開始方向に対して距離を 1 とする。
     * また、止まった位置で方向転換をするごとに距離が 1 増加する。
     * 到達不能なマスの状態に対する距離は Integer.MAX_VALUE / 3 となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return dist[dir][r][c]（dir方向への移動中にマス (r, c) を通過するための最小コスト）
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][][] bfsIcefloor(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj) {
        ArrayDeque<int[]> dq = new ArrayDeque<>();
        int[][][] dist = new int[4][R][C];
        int INF = Integer.MAX_VALUE / 3;
        ArrayUtils.fill(dist, INF);
        if (si < 0 || si >= R || sj < 0 || sj >= C) {
            return dist;
        }
        if (isObstacle != null && isObstacle[si][sj]) {
            return dist;
        }

        for (int dir = 0; dir < 4; dir++) {
            dist[dir][si][sj] = 1;
            dq.add(new int[]{dir, si, sj});
        }

        while (!dq.isEmpty()) {
            int[] state = dq.pollFirst();
            int dir = state[0];
            int x = state[1];
            int y = state[2];

            if (canMove(x, y, dir, R, C, V, H, isObstacle)) {
                int nx = x + dh[dir];
                int ny = y + dw[dir];
                if (dist[dir][nx][ny] == INF) {
                    dist[dir][nx][ny] = dist[dir][x][y];
                    dq.addFirst(new int[]{dir, nx, ny});
                }
            } else {
                for (int ndir = 0; ndir < 4; ndir++) {
                    if (ndir == dir) continue;
                    if (dist[ndir][x][y] == INF) {
                        dist[ndir][x][y] = 1 + dist[dir][x][y];
                        dq.addLast(new int[]{ndir, x, y});
                    }
                }
            }
        }
        return dist;
    }

    /**
     * 始点 (si, sj) からの滑る床（icefloor）上での最短距離を01-BFSにより計算する。
     * 到達不能なマスの状態に対する距離は Integer.MAX_VALUE / 3 となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return dist[dir][r][c]（dir方向への移動中にマス (r, c) を通過するための最小コスト）
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][][] bfsIcefloor(int R, int C, boolean[][] V, boolean[][] H, int si, int sj) {
        return bfsIcefloor(R, C, V, H, null, si, sj);
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への通常のBFSによる最短パス（マス目の座標列）を取得する。
     * 壁（V, H）およびセル障害物（isObstacle）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPath(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj, int ti, int tj) {
        int[][] dist = bfs(R, C, V, H, isObstacle, si, sj);
        if (ti < 0 || ti >= R || tj < 0 || tj >= C || dist[ti][tj] == Integer.MAX_VALUE) {
            return null;
        }
        int len = dist[ti][tj] + 1;
        int[][] path = new int[len][2];
        int currR = ti;
        int currC = tj;
        for (int step = len - 1; step >= 0; step--) {
            path[step][0] = currR;
            path[step][1] = currC;
            if (step == 0) break;
            for (int dir = 0; dir < 4; dir++) {
                int prevR = currR - dh[dir];
                int prevC = currC - dw[dir];
                if (prevR >= 0 && prevR < R && prevC >= 0 && prevC < C) {
                    if (canMove(prevR, prevC, dir, R, C, V, H, isObstacle) && dist[prevR][prevC] == step - 1) {
                        currR = prevR;
                        currC = prevC;
                        break;
                    }
                }
            }
        }
        return path;
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への通常のBFSによる最短パス（マス目の座標列）を取得する。
     * 壁（V, H）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPath(int R, int C, boolean[][] V, boolean[][] H, int si, int sj, int ti, int tj) {
        return findPath(R, C, V, H, null, si, sj, ti, tj);
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への滑る床上での最短パス（通過するマス目の座標列）を取得する。
     * 移動中に通過するすべてのマスの座標（スライド中の途中経過マスを含む）が含まれる。
     * 壁（V, H）およびセル障害物（isObstacle）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPathIcefloor(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj, int ti, int tj) {
        int[][][] dist = bfsIcefloor(R, C, V, H, isObstacle, si, sj);
        if (ti < 0 || ti >= R || tj < 0 || tj >= C) {
            return null;
        }
        int minDist = Integer.MAX_VALUE / 3;
        int targetDir = -1;
        for (int dir = 0; dir < 4; dir++) {
            if (dist[dir][ti][tj] < minDist) {
                minDist = dist[dir][ti][tj];
                targetDir = dir;
            }
        }
        if (minDist == Integer.MAX_VALUE / 3) {
            return null;
        }

        java.util.ArrayList<int[]> pathList = new java.util.ArrayList<>();
        int currR = ti;
        int currC = tj;
        int currDir = targetDir;

        pathList.add(new int[]{currR, currC});

        while (!(currR == si && currC == sj && dist[currDir][currR][currC] == 1)) {
            // 0-weight スライド遷移を試みる
            int prevR = currR - dh[currDir];
            int prevC = currC - dw[currDir];
            if (prevR >= 0 && prevR < R && prevC >= 0 && prevC < C
                && canMove(prevR, prevC, currDir, R, C, V, H, isObstacle)
                && dist[currDir][prevR][prevC] == dist[currDir][currR][currC]) {
                currR = prevR;
                currC = prevC;
                pathList.add(new int[]{currR, currC});
                continue;
            }

            // 1-weight 方向転換遷移を試みる
            boolean turned = false;
            for (int prevDir = 0; prevDir < 4; prevDir++) {
                if (prevDir == currDir) continue;
                if (!canMove(currR, currC, prevDir, R, C, V, H, isObstacle)
                    && dist[prevDir][currR][currC] == dist[currDir][currR][currC] - 1) {
                    currDir = prevDir;
                    turned = true;
                    break;
                }
            }
            if (!turned) {
                // 無限ループ防止用のフォールバック
                break;
            }
        }

        // 逆順で収集したため、反転する
        int len = pathList.size();
        int[][] res = new int[len][2];
        for (int i = 0; i < len; i++) {
            res[i] = pathList.get(len - 1 - i);
        }
        return res;
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への滑る床上での最短パス（通過するマス目の座標列）を取得する。
     * 移動中に通過するすべてのマスの座標（スライド中の途中経過マスを含む）が含まれる。
     * 壁（V, H）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPathIcefloor(int R, int C, boolean[][] V, boolean[][] H, int si, int sj, int ti, int tj) {
        return findPathIcefloor(R, C, V, H, null, si, sj, ti, tj);
    }

    /**
     * 始点 (si, sj) からの、スライドの途中（任意のマスの境界）で停止して方向転換することが可能な滑る床上での最短距離を01-BFSにより計算する。
     * 各マスにおいて、移動を開始した方向（0: 下, 1: 左, 2: 上, 3: 右）ごとに到着距離を求める。
     *
     * プレイヤーは進行方向の正面に壁や障害物がない場合でも、任意のマスでスライドを停止し、
     * 別の方向に方向転換して再び移動を開始することができる。
     *
     * 初期状態は始点 (si, sj) からある方向に移動を開始しているとして、
     * すべての移動開始方向に対して距離を 1 とする。
     * また、任意のマスで停止して方向転換をするごとに距離が 1 増加する。
     * 到達不能なマスの状態に対する距離は Integer.MAX_VALUE / 3 となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return dist[dir][r][c]（dir方向への移動中にマス (r, c) を通過するための最小コスト）
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][][] bfsIcefloorWithStopping(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj) {
        ArrayDeque<int[]> dq = new ArrayDeque<>();
        int[][][] dist = new int[4][R][C];
        int INF = Integer.MAX_VALUE / 3;
        ArrayUtils.fill(dist, INF);
        if (si < 0 || si >= R || sj < 0 || sj >= C) {
            return dist;
        }
        if (isObstacle != null && isObstacle[si][sj]) {
            return dist;
        }

        for (int dir = 0; dir < 4; dir++) {
            dist[dir][si][sj] = 1;
            dq.add(new int[]{dir, si, sj});
        }

        while (!dq.isEmpty()) {
            int[] state = dq.pollFirst();
            int dir = state[0];
            int x = state[1];
            int y = state[2];

            // 1. スライド移動（コスト0、常に可能な場合に実行）
            if (canMove(x, y, dir, R, C, V, H, isObstacle)) {
                int nx = x + dh[dir];
                int ny = y + dw[dir];
                if (dist[dir][nx][ny] == INF) {
                    dist[dir][nx][ny] = dist[dir][x][y];
                    dq.addFirst(new int[]{dir, nx, ny});
                }
            }

            // 2. 任意の場所での方向転換（コスト1、常に可能）
            for (int ndir = 0; ndir < 4; ndir++) {
                if (ndir == dir) continue;
                if (dist[ndir][x][y] == INF) {
                    dist[ndir][x][y] = 1 + dist[dir][x][y];
                    dq.addLast(new int[]{ndir, x, y});
                }
            }
        }
        return dist;
    }

    /**
     * 始点 (si, sj) からの、スライドの途中（任意のマスの境界）で停止して方向転換することが可能な滑る床上での最短距離を01-BFSにより計算する。
     * 到達不能なマスの状態に対する距離は Integer.MAX_VALUE / 3 となる。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @return dist[dir][r][c]（dir方向への移動中にマス (r, c) を通過するための最小コスト）
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][][] bfsIcefloorWithStopping(int R, int C, boolean[][] V, boolean[][] H, int si, int sj) {
        return bfsIcefloorWithStopping(R, C, V, H, null, si, sj);
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への、スライドの途中での停止・方向転換が可能な滑る床上での最短パス（通過するマス目の座標列）を取得する。
     * 壁（V, H）およびセル障害物（isObstacle）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param isObstacle セル障害物情報（null の場合は障害物なし）
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPathIcefloorWithStopping(int R, int C, boolean[][] V, boolean[][] H, boolean[][] isObstacle, int si, int sj, int ti, int tj) {
        int[][][] dist = bfsIcefloorWithStopping(R, C, V, H, isObstacle, si, sj);
        if (ti < 0 || ti >= R || tj < 0 || tj >= C) {
            return null;
        }
        int minDist = Integer.MAX_VALUE / 3;
        int targetDir = -1;
        for (int dir = 0; dir < 4; dir++) {
            if (dist[dir][ti][tj] < minDist) {
                minDist = dist[dir][ti][tj];
                targetDir = dir;
            }
        }
        if (minDist == Integer.MAX_VALUE / 3) {
            return null;
        }

        java.util.ArrayList<int[]> pathList = new java.util.ArrayList<>();
        int currR = ti;
        int currC = tj;
        int currDir = targetDir;

        pathList.add(new int[]{currR, currC});

        while (!(currR == si && currC == sj && dist[currDir][currR][currC] == 1)) {
            // 0-weight スライド遷移を試みる
            int prevR = currR - dh[currDir];
            int prevC = currC - dw[currDir];
            if (prevR >= 0 && prevR < R && prevC >= 0 && prevC < C
                && canMove(prevR, prevC, currDir, R, C, V, H, isObstacle)
                && dist[currDir][prevR][prevC] == dist[currDir][currR][currC]) {
                currR = prevR;
                currC = prevC;
                pathList.add(new int[]{currR, currC});
                continue;
            }

            // 1-weight 方向転換遷移を試みる（停止可能なので、canMoveの状況に関わらず常に可能）
            boolean turned = false;
            for (int prevDir = 0; prevDir < 4; prevDir++) {
                if (prevDir == currDir) continue;
                if (dist[prevDir][currR][currC] == dist[currDir][currR][currC] - 1) {
                    currDir = prevDir;
                    turned = true;
                    break;
                }
            }
            if (!turned) {
                break;
            }
        }

        // 逆順収集を反転
        int len = pathList.size();
        int[][] res = new int[len][2];
        for (int i = 0; i < len; i++) {
            res[i] = pathList.get(len - 1 - i);
        }
        return res;
    }

    /**
     * 始点 (si, sj) から終点 (ti, tj) への、スライドの途中での停止・方向転換が可能な滑る床上での最短パス（通過するマス目の座標列）を取得する。
     * 壁（V, H）による通行不能を考慮する。
     * 到達不可能な場合は null を返す。
     *
     * @param R 行数
     * @param C 列数
     * @param V 垂直壁情報
     * @param H 水平壁情報
     * @param si 始点の行インデックス
     * @param sj 始点の列インデックス
     * @param ti 終点の行インデックス
     * @param tj 終点の列インデックス
     * @return パスを構成する各マスの座標 [r, c] の配列。到達不能時は null
     * @complexity O(R \cdot C)
     */
    // 未テスト
    public static int[][] findPathIcefloorWithStopping(int R, int C, boolean[][] V, boolean[][] H, int si, int sj, int ti, int tj) {
        return findPathIcefloorWithStopping(R, C, V, H, null, si, sj, ti, tj);
    }
}
