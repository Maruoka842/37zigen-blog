package library.util.graph.grid;

import java.util.Arrays;

/**
 * 8近傍輪郭線DP (8-Neighbor Frontier DP / Profile DP) における連結成分の状態を管理するクラス。
 * 境界上の各位置の連結成分ID、境界の分割位置（c）、消滅した連結成分 of 有無、デッドエンド数（deadEnds）、およびサイクル数（cycles）を保持します。
 *
 * <p>このクラスは不変（Immutable）です。
 *
 * @author Jules
 */
public final class Frontier8 {

    /**
     * 新しい頂点を接続する方向を表す列挙型。
     */
    public enum Direction {
        /** 左隣 of セル */
        LEFT,
        /** 上 of セル */
        UP,
        /** 左上 of セル */
        TOP_LEFT,
        /** 右上 of セル */
        TOP_RIGHT
    }

    /** 境界上の各位置の連結成分ID。0は空、1以上は連結成分ID。 */
    public final byte[] parent;

    /** 各連結成分のタグ。インデックスは連結成分ID。 */
    public final int[] tags;

    /** [0, c)[c, W) に分割していて、次の更新位置が c*/
    public final int c;

    /** すでに消滅した（境界から退場した）連結成分が存在するかどうか。 */
    public final boolean hasDead;

    /** 退場した連結成分の総数（デッドエンド数）。 */
    public int deadEnds;

    /** 形成されたサイクルの総数。 */
    public int cycles;

    /**
     * [0, c)[c, parent.length) に分割していて、次の更新位置が c
     * @param parent 各位置の連結成分ID配列
     * @param c 分割位置
     * @param hasDead 消滅した成分の有無
     */
    public Frontier8(byte[] parent, int c, boolean hasDead) {
        this(parent, new int[parent.length + 3], c, hasDead, 0, 0);
    }

    /**
     * [0, c)[c, parent.length) に分割していて、次の更新位置が c
     * @param parent 各位置 of 連結成分ID配列
     * @param c 分割位置
     * @param hasDead 消滅した成分の有無
     * @param deadEnds デッドエンド数
     * @param cycles サイクル数
     */
    // 未テスト
    public Frontier8(byte[] parent, int c, boolean hasDead, int deadEnds, int cycles) {
        this(parent, new int[parent.length + 3], c, hasDead, deadEnds, cycles);
    }

    /**
     * [0, c)[c, parent.length) に分割していて、次の更新位置が c、タグ情報を伴う
     * @param parent 各位置 of 連結成分ID配列
     * @param tags 各連結成分 of タグ配列
     * @param c 分割位置
     * @param hasDead 消滅した成分 of 有無
     * @param deadEnds デッドエンド数
     * @param cycles サイクル数
     */
    // 未テスト
    public Frontier8(byte[] parent, int[] tags, int c, boolean hasDead, int deadEnds, int cycles) {
        this.parent = parent;
        this.tags = tags;
        this.c = c;
        this.hasDead = hasDead;
        this.deadEnds = deadEnds;
        this.cycles = cycles;
    }

    /**
     * 初期状態のFrontier8を生成する。
     * 計算量: O(M)
     *
     * @param M グリッドの列数
     * @return 初期状態のFrontier8
     */
    // 未テスト
    public static Frontier8 getInitialState(int M) {
        return new Frontier8(new byte[M + 1], 0, false, 0, 0);
    }

    /**
     * デッドエンド数が少ない方を優先して状態をchminする。
     * @param val 比較するデッドエンド数
     * @return 変更後のFrontier8
     */
    // 未テスト
    public Frontier8 chminDeadEnds(int val) {
        if (val < this.deadEnds) {
            this.deadEnds = val;
        }
        return this;
    }

    /**
     * サイクル数が少ない方を優先して状態をchminする。
     * @param val 比較するサイクル数
     * @return 変更後のFrontier8
     */
    // 未テスト
    public Frontier8 chminCycles(int val) {
        if (val < this.cycles) {
            this.cycles = val;
        }
        return this;
    }

    /**
     * 指定された位置に頂点（黒マス）が存在するかを判定する。
     * @param index インデックス
     * @return 頂点が存在する場合はtrue、存在しない場合はfalse
     */
    // 未テスト
    public boolean hasVertex(int index) {
        return parent[index] > 0;
    }

    /**
     * 与えられた2つのインデックスのセルが連結しているかを判定する。
     * @param i インデックス1
     * @param j インデックス2
     * @return 連結している場合はtrue、そうでない場合はfalse
     */
    // 未テスト
    public boolean isConnected(int i, int j) {
        return parent[i] > 0 && parent[i] == parent[j];
    }

    /**
     * 与えられた接続方向の集合から、可能なすべての部分集合（接続パターン）を試行し、
     * 生成された遷移後の有効な Frontier8 の集合を返します。
     *
     * @param dirs 試行する接続方向の集合
     * @return 遷移後の有効な Frontier8 の反復可能（Iterable）オブジェクト
     */
    // 未テスト
    public Iterable<Frontier8> getPossibleConnections(Direction... dirs) {
        java.util.List<Frontier8> list = new java.util.ArrayList<>();
        int n = dirs.length;
        for (int mask = 0; mask < (1 << n); mask++) {
            Builder builder = startVertex();
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                if (((mask >> i) & 1) != 0) {
                    if (!builder.connect(dirs[i])) {
                        ok = false;
                        break;
                    }
                }
            }
            if (ok) {
                Frontier8 next = builder.build();
                if (next != null) {
                    list.add(next);
                }
            }
        }
        return list;
    }

    /**
     * 現在の更新位置 c において、指定された初期タグを持つ新しい頂点を生成して接続を構築するための Builder を生成して返します。
     * 計算量: O(M)
     *
     * @param initialTag 新しい頂点に付与する初期タグ
     * @return 新しい状態を構築するための完全に独立した Builder インスタンス
     */
    // 未テスト
    public Builder startVertex(int initialTag) {
        return new Builder(this, initialTag);
    }

    /**
     * 現在の更新位置 c において、新しく頂点（黒マスなど）を生成（開始）して接続を構築するための Builder を生成して返します。
     * <p>このメソッドは非破壊的であり、現在の状態は一切変更されません。
     * 戻り値となる Builder は独立した新しい内部配列（parent のクローン）を保持するため、
     * 複数の Builder インスタンス（例：state0 = state.startVertex(); state1 = state.startVertex();）は
     * 互いに完全に独立しており、一方の変更が他方に影響を与える（エンタングルする）ことはありません。
     *
     * 計算量: O(M)
     *
     * @return 新しい状態を構築するための完全に独立した Builder インスタンス
     */
    // 未テスト
    public Builder startVertex() {
        return new Builder(this, 0);
    }

    /**
     * 現在の更新位置 c において、新しく頂点（黒マス）を生成せずに、
     * 次の更新位置へ状態を遷移させた新しい Frontier8 を生成して返します。
     * これは新頂点を使用しない（白マスなどとしてスキップする）ことに相当します。
     * 計算量: O(M)
     *
     * @return 遷移後の新しい Frontier8 オブジェクト。無効な遷移の場合は null。
     */
    // 未テスト
    public Frontier8 nextWithoutVertex() {
        Builder builder = new Builder(this);
        builder.used = false;
        builder.parent[this.c] = 0;
        return builder.build();
    }

    /**
     * 境界を次の行へシフト（ラップ処理）する際、右端から退場する連結成分のタグを取得します。
     * 消滅する成分が存在しない、またはタグが割り当てられていない場合は 0 を返します。
     * 計算量: O(1)
     *
     * @return 右端から退場する連結成分のタグ
     */
    // 未テスト
    public int getShiftRetiringTag() {
        int M = parent.length - 1;
        byte pM = parent[M];
        if (pM > 0) {
            boolean existsOther = false;
            for (int j = 0; j < M; j++) {
                if (parent[j] == pM) {
                    existsOther = true;
                    break;
                }
            }
            if (!existsOther) {
                int idx = pM & 0xFF;
                if (idx < tags.length) {
                    return tags[idx];
                }
            }
        }
        return 0;
    }

    /**
     * 境界を次の行へシフト（ラップ処理）する。
     * 最右端の要素が消滅するか判定し、新たな行の先頭（c=0）の状態を生成する。
     * 計算量: O(M)
     *
     * @return シフト後の新しいFrontier8。
     */
    // 未テスト
    public Frontier8 shift() {
        int M = parent.length - 1;
        byte pM = parent[M];
        boolean newHasDead = hasDead;
        int nextDeadEnds = deadEnds;
        if (pM > 0) {
            boolean existsOther = false;
            for (int j = 0; j < M; j++) {
                if (parent[j] == pM) {
                    existsOther = true;
                    break;
                }
            }
            if (!existsOther) {
                newHasDead = true;
                nextDeadEnds++;
            }
        }
        byte[] shifted = new byte[parent.length];
        System.arraycopy(parent, 0, shifted, 1, M);
        shifted[0] = 0;

        int[] shiftedTags = new int[tags.length];
        System.arraycopy(tags, 0, shiftedTags, 0, tags.length);

        return new Frontier8(shifted, shiftedTags, 0, newHasDead, nextDeadEnds, cycles);
    }

    /**
     * 連結成分を表すID配列を左から順に正規化（1から順に再割当て）した新しい配列を返す。
     * 計算量: O(M)
     *
     * @param parent 正規化対象 of ID配列
     * @return 正規化された新しいID配列
     */
    // 未テスト
    public static byte[] canonicalize(byte[] parent) {
        byte[] map = new byte[256];
        byte nextId = 1;
        byte[] canonical = new byte[parent.length];
        for (int i = 0; i < parent.length; i++) {
            if (parent[i] > 0) {
                int idx = parent[i] & 0xFF;
                if (map[idx] == 0) {
                    map[idx] = nextId++;
                }
                canonical[i] = map[idx];
            }
        }
        return canonical;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frontier8)) return false;
        Frontier8 that = (Frontier8) o;
        return c == that.c && hasDead == that.hasDead && deadEnds == that.deadEnds && cycles == that.cycles && Arrays.equals(parent, that.parent) && Arrays.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(parent);
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + c;
        result = 31 * result + (hasDead ? 1 : 0);
        result = 31 * result + deadEnds;
        result = 31 * result + cycles;
        return result;
    }

    /**
     * 現在の境界上に存在する、アクティブな（空でない）連結成分の個数を返す。
     * 計算量: O(M)
     *
     * @return アクティブな連結成分の個数
     */
    // 未テスト
    public int getActiveComponentCount() {
        int activeCount = 0;
        boolean[] seen = new boolean[256];
        for (byte b : parent) {
            if (b > 0) {
                int idx = b & 0xFF;
                if (!seen[idx]) {
                    seen[idx] = true;
                    activeCount++;
                }
            }
        }
        return activeCount;
    }

    /**
     * Frontier8 の新頂点における遷移状態を構築するためのビルダー。
     *
     * <p>注意: このクラスは状態変更メソッド（connect など）を呼び出すと、内部状態を破壊的に変更します。
     */
    public static final class Builder {
        /** 構築中の連結成分配列。 */
        public final byte[] parent;
        /** 構築中のタグ配列。 */
        public final int[] tags;
        private final byte[] initialParent;
        private final int[] initialTags;
        private final boolean[] connectedInitialIds = new boolean[256];
        private int addedCycles = 0;
        private final int c;
        private final boolean hasDead;
        private final int deadEnds;
        private final int cycles;
        private final byte pRetire;
        private final boolean isDying;
        private boolean used = true;
        private boolean connectsToRetire = false;
        private boolean connectedToAny = false;
        private final byte tempId;

        /**
         * 与えられたFrontier8を基にBuilderを初期化する。
         * 計算量: O(M)
         *
         * @param frontier 遷移元のFrontier8
         */
        public Builder(Frontier8 frontier) {
            this(frontier, 0);
        }

        /**
         * 与えられたFrontier8と初期タグを基にBuilderを初期化する。
         * 計算量: O(M)
         *
         * @param frontier 遷移元のFrontier8
         * @param initialTag 新しい頂点に付与する初期タグ
         */
        // 未テスト
        public Builder(Frontier8 frontier, int initialTag) {
            this.parent = frontier.parent.clone();
            this.initialParent = frontier.parent;
            this.tags = new int[parent.length + 3];
            System.arraycopy(frontier.tags, 0, this.tags, 0, Math.min(frontier.tags.length, this.tags.length));
            this.initialTags = frontier.tags;
            this.c = frontier.c;
            this.hasDead = frontier.hasDead;
            this.deadEnds = frontier.deadEnds;
            this.cycles = frontier.cycles;
            this.pRetire = parent[c];
            this.tempId = (byte) (parent.length + 2);

            boolean dying = false;
            if (c > 0 && pRetire > 0) {
                boolean existsOther = false;
                for (int j = 0; j < parent.length; j++) {
                    if (j != c && parent[j] == pRetire) {
                        existsOther = true;
                        break;
                    }
                }
                if (!existsOther) {
                    dying = true;
                }
            }
            this.isDying = dying;
            this.parent[c] = tempId;
            this.tags[tempId] = initialTag;
        }

        /**
         * 新しい頂点と指定された方向の隣接頂点を連結します。
         * 隣接頂点が存在し、接続に成功した場合は true、接続されなかった場合は false を返します。
         * 計算量: O(M)
         *
         * @param dir 連結方向
         * @return 接続に成功した場合は true、接続されなかった場合は false
         */
        // 未テスト
        public boolean connect(Direction dir) {
            return connect(dir, false);
        }

        /**
         * 新しい頂点と指定された方向の隣接頂点を連結します。
         * 隣接頂点が存在し、接続に成功した場合は true、接続されなかった場合は false を返します。
         * 計算量: O(M)
         *
         * @param dir 連結方向
         * @param activate 連結方向の隣接頂点が未接続（IDが0）の場合、新頂点の連結成分ID（一時IDなど）でその位置を活性化（上書き）するかどうかのフラグ
         * @return 接続に成功した場合は true、接続されなかった場合は false
         */
        // 未テスト
        public boolean connect(Direction dir, boolean activate) {
            if (!used) {
                throw new IllegalStateException("Cannot connect after state has been configured as unused");
            }
            int nbIdx = getNeighborIndex(dir);
            if (nbIdx < 0) {
                return false;
            }
            byte neighborVal = (dir == Direction.TOP_LEFT) ? pRetire : initialParent[nbIdx];
            byte to = parent[c];
            if (neighborVal > 0) {
                byte from = neighborVal;
                if (from != to) {
                    int mergedTag = tags[from] | tags[to];
                    for (int j = 0; j < parent.length; j++) {
                        if (parent[j] == from) {
                            parent[j] = to;
                        }
                    }
                    tags[to] = mergedTag;
                    tags[from] = 0;
                }
                connectedToAny = true;
                if (dir == Direction.TOP_LEFT) {
                    connectsToRetire = true;
                }

                int idx = from & 0xFF;
                if (connectedInitialIds[idx]) {
                    addedCycles++;
                }
                connectedInitialIds[idx] = true;
                return true;
            } else if (activate) {
                parent[nbIdx] = to;
                connectedToAny = true;
                if (dir == Direction.TOP_LEFT) {
                    connectsToRetire = true;
                }
                return true;
            }
            return false;
        }

        /**
         * このステップで消滅（退場）する連結成分のタグを取得します。
         * 消滅する成分が存在しない、またはタグが割り当てられていない場合は 0 を返します。
         * 計算量: O(1)
         *
         * @return 消滅する連結成分のタグ
         */
        // 未テスト
        public int getRetiringTag() {
            if (isDying && (!used || !connectsToRetire)) {
                int idx = pRetire & 0xFF;
                if (idx < initialTags.length) {
                    return initialTags[idx];
                }
            }
            return 0;
        }

        private int getNeighborIndex(Direction dir) {
            switch (dir) {
                case LEFT:
                    return c > 0 ? c - 1 : -1;
                case UP:
                    return c + 1 < parent.length ? c + 1 : -1;
                case TOP_LEFT:
                    return c > 0 ? c : -1;
                case TOP_RIGHT:
                    return c + 2 < parent.length ? c + 2 : -1;
                default:
                    return -1;
            }
        }

        /**
         * 連結成分の更新、正規化、消滅判定を行い、次のFrontier8状態を生成する。
         * 計算量: O(M)
         *
         * @return 次のFrontier8状態。
         */
        // 未テスト
        public Frontier8 build() {
            int nextDeadEnds = deadEnds;
            boolean nextHasDead = hasDead;
            if (isDying && (!used || !connectsToRetire)) {
                nextDeadEnds++;
                nextHasDead = true;
            }

            // parentの正規化
            byte[] map = new byte[256];
            byte nextId = 1;
            byte[] canonicalParent = new byte[parent.length];
            for (int i = 0; i < parent.length; i++) {
                if (parent[i] > 0) {
                    int idx = parent[i] & 0xFF;
                    if (map[idx] == 0) {
                        map[idx] = nextId++;
                    }
                    canonicalParent[i] = map[idx];
                }
            }

            // tagsの正規化
            int[] canonicalTags = new int[parent.length + 3];
            for (int oldId = 1; oldId < 256; oldId++) {
                int newId = map[oldId] & 0xFF;
                if (newId > 0 && oldId < tags.length) {
                    canonicalTags[newId] = tags[oldId];
                }
            }

            return new Frontier8(canonicalParent, canonicalTags, c + 1, nextHasDead, nextDeadEnds, cycles + addedCycles);
        }
    }
}
