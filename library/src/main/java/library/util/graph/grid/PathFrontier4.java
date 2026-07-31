package library.util.graph.grid;

import java.util.Arrays;

/**
 * 4近傍パス輪郭線DP (4-Neighbor Path Frontier DP) におけるパス・サイクルの状態を管理するクラス。
 * 境界上の各位置の連結成分ID、境界の分割位置（c）、消滅した連結成分の有無、デッドエンド数（deadEnds）、およびサイクル数（cycles）を保持します。
 *
 * <p>このクラスは不変（Immutable）です。
 *
 * @author Jules
 */
public final class PathFrontier4 {

    public enum Direction {
        LEFT,
        UP
    }

    public final byte[] parent;
    public final int c;
    public final boolean hasDead;
    public final int deadEnds;
    public final int cycles;

    public PathFrontier4(byte[] parent, int c, boolean hasDead) {
        this(parent, c, hasDead, 0, 0);
    }

    // 未テスト
    public PathFrontier4(byte[] parent, int c, boolean hasDead, int deadEnds, int cycles) {
        this.parent = parent;
        this.c = c;
        this.hasDead = hasDead;
        this.deadEnds = deadEnds;
        this.cycles = cycles;
    }

    // 未テスト
    public static PathFrontier4 getInitialState(int M) {
        return new PathFrontier4(new byte[M], 0, false, 0, 0);
    }

    // 未テスト
    public PathFrontier4 chminDeadEnds(int val) {
        if (this.deadEnds <= val) return this;
        return new PathFrontier4(parent, c, hasDead, val, cycles);
    }

    // 未テスト
    public PathFrontier4 chminCycles(int val) {
        if (this.cycles <= val) return this;
        return new PathFrontier4(parent, c, hasDead, deadEnds, val);
    }

    // 未テスト
    public boolean hasVertex(int index) {
        return parent[index] > 0;
    }

    // 未テスト
    public boolean isConnected(int i, int j) {
        return parent[i] > 0 && parent[i] == parent[j];
    }

    /**
     * 与えられた接続方向の集合から、可能なすべての部分集合（接続パターン）を試行し、
     * 生成された遷移後の有効な PathFrontier4 の集合を返します。
     *
     * @param dirs 試行する接続方向の集合
     * @return 遷移後の有効な PathFrontier4 の反復可能（Iterable）オブジェクト
     */
    // 未テスト
    public Iterable<PathFrontier4> getPossibleConnections(Direction... dirs) {
        return getPossibleConnections((byte) 0, dirs);
    }

    /**
     * 与えられた接続方向の集合から、可能なすべての部分集合（接続パターン）を試行し、
     * 生成された遷移後の有効な PathFrontier4 の集合を返します。
     *
     * @param startId パスの開始点ID
     * @param dirs 試行する接続方向の集合
     * @return 遷移後の有効な PathFrontier4 の反復可能（Iterable）オブジェクト
     */
    // 未テスト
    public Iterable<PathFrontier4> getPossibleConnections(byte startId, Direction... dirs) {
        java.util.List<PathFrontier4> list = new java.util.ArrayList<>();
        int n = dirs.length;
        for (int mask = 0; mask < (1 << n); mask++) {
            Builder builder = startVertex();
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                if (((mask >> i) & 1) != 0) {
                    if (!builder.connect(dirs[i], startId)) {
                        ok = false;
                        break;
                    }
                }
            }
            if (ok) {
                PathFrontier4 next = builder.build(startId);
                if (next != null) {
                    list.add(next);
                }
            }
        }
        return list;
    }

    // 未テスト
    public Builder startVertex() {
        return new Builder(this);
    }

    // 未テスト
    public PathFrontier4 nextWithoutVertex() {
        return nextWithoutVertex((byte) 0);
    }

    // 未テスト
    public PathFrontier4 nextWithoutVertex(byte startId) {
        Builder builder = new Builder(this);
        builder.used = false;
        builder.parent[this.c] = 0;
        return builder.build(startId);
    }

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
        if (!(o instanceof PathFrontier4)) return false;
        PathFrontier4 that = (PathFrontier4) o;
        return c == that.c && hasDead == that.hasDead && deadEnds == that.deadEnds && cycles == that.cycles && Arrays.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(parent);
        result = 31 * result + c;
        result = 31 * result + (hasDead ? 1 : 0);
        result = 31 * result + deadEnds;
        result = 31 * result + cycles;
        return result;
    }

    // 未テスト
    public int getComponentCount() {
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

    public static final class Builder {
        public final byte[] parent;
        private final byte[] initialParent;
        private int addedCycles = 0;
        private final int c;
        private final boolean hasDead;
        private final int deadEnds;
        private final int cycles;
        private final byte pRetire;
        private boolean used = true;
        private boolean connectsToRetire = false;
        private final byte tempId;
        private int connectionsCount = 0;

        public Builder(PathFrontier4 frontier) {
            this.parent = frontier.parent.clone();
            this.initialParent = frontier.parent;
            this.c = frontier.c;
            this.hasDead = frontier.hasDead;
            this.deadEnds = frontier.deadEnds;
            this.cycles = frontier.cycles;
            this.pRetire = parent[c];
            this.tempId = (byte) (parent.length + 2);
            this.parent[c] = tempId;
        }

        // 未テスト
        public boolean connect(Direction dir) {
            return connect(dir, (byte) 0);
        }

        // 未テスト
        public boolean connect(Direction dir, byte startId) {
            return connect(dir, false, startId);
        }

        // 未テスト
        public boolean connect(Direction dir, boolean activate, byte startId) {
            if (!used) {
                throw new IllegalStateException("Cannot connect after state has been configured as unused");
            }
            int nbIdx = getNeighborIndex(dir);
            if (nbIdx < 0) {
                return false;
            }
            byte neighborVal = (dir == Direction.UP) ? pRetire : initialParent[nbIdx];
            if (neighborVal == 0) {
                return false;
            }
            if (connectionsCount >= 2) {
                return false;
            }
            if (dir == Direction.UP) {
                connectsToRetire = true;
            }

            int occurrences = 0;
            for (byte b : initialParent) {
                if (b == neighborVal) {
                    occurrences++;
                }
            }
            if (startId > 0 && neighborVal == startId) {
                occurrences = 2;
            }

            if (connectionsCount == 0) {
                connectionsCount = 1;
                if (occurrences == 1) {
                    parent[c] = neighborVal;
                } else {
                    parent[nbIdx] = 0;
                    parent[c] = neighborVal;
                }
            } else if (connectionsCount == 1) {
                connectionsCount = 2;
                byte from = parent[c];
                byte to = neighborVal;
                if (occurrences == 1) {
                    if (from != to) {
                        byte mergeTo = to;
                        byte mergeFrom = from;
                        if (startId > 0 && from == startId) {
                            mergeTo = from;
                            mergeFrom = to;
                        }
                        for (int j = 0; j < parent.length; j++) {
                            if (parent[j] == mergeFrom) parent[j] = mergeTo;
                        }
                    } else {
                        addedCycles++;
                    }
                    parent[c] = 0;
                } else {
                    if (from != to) {
                        byte mergeTo = to;
                        byte mergeFrom = from;
                        if (startId > 0 && from == startId) {
                            mergeTo = from;
                            mergeFrom = to;
                        }
                        for (int j = 0; j < parent.length; j++) {
                            if (parent[j] == mergeFrom) parent[j] = mergeTo;
                        }
                    } else {
                        addedCycles++;
                    }
                    parent[nbIdx] = 0;
                    parent[c] = 0;
                }
            }
            return true;
        }

        private int getNeighborIndex(Direction dir) {
            switch (dir) {
                case LEFT:
                    return c > 0 ? c - 1 : -1;
                case UP:
                    return c;
                default:
                    return -1;
            }
        }

        // 未テスト
        public PathFrontier4 build() {
            return build((byte) 0);
        }

        // 未テスト
        public PathFrontier4 build(byte startId) {
            int nextDeadEnds = deadEnds;
            if (pRetire > 0 && pRetire != startId && !connectsToRetire) {
                nextDeadEnds++;
            }

            byte[] canonicalParent = canonicalize(parent);
            int nextC = (c + 1) % parent.length;
            return new PathFrontier4(canonicalParent, nextC, hasDead, nextDeadEnds, cycles + addedCycles);
        }
    }
}
