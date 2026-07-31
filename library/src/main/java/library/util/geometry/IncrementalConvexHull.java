package library.util.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 2次元平面上における整数の点集合に対するインクリメンタルな凸包（Incremental Convex Hull）を管理するクラス。
 * 点を追加しながら、現在の凸包の端点、面積、包含判定、特定の方向への最大内積（極値点）などを効率的に求めることができる。
 * 内部的には、上側凸包（Upper Hull）と下側凸包（Lower Hull）をそれぞれ {@link TreeMap} で管理する。
 * 全ての計算は 64ビット符号付き整数型 (long) のみを用いて高速に行われる。
 *
 * 座標の上限に関する安全性:
 * 座標の絶対値が 10^9 以下であれば、外積および極値点判定の計算中に 64ビット符号付き整数 (long) がオーバーフローすることはありません。
 *
 */
public class IncrementalConvexHull {

    // 上側凸包。キーはx座標、値は対応する最大y座標。
    private final TreeMap<Long, Long> upper;
    // 下側凸包。キーはx座標、値は対応する最小y座標。
    private final TreeMap<Long, Long> lower;

    /**
     * 空のインクリメンタル凸包を初期化する。
     * 計算量: O(1)
     * // 未テスト
     */
    public IncrementalConvexHull() {
        this.upper = new TreeMap<>();
        this.lower = new TreeMap<>();
    }

    private IncrementalConvexHull(TreeMap<Long, Long> upper, TreeMap<Long, Long> lower) {
        this.upper = upper;
        this.lower = lower;
    }

    /**
     * IncrementalConvexHullの現在の状態をコピーした新しいインスタンスを返す。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: コピー元の状態とは独立した新しいインスタンスを返す。コピー元、コピー先に対する変更は互いに影響しない。</li>
     *   <li>副作用: なし。</li>
     *   <li>計算量: $O(N)$ (ここで $N$ は凸包に含まれる頂点の数)</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * 未テスト
     */
    // 未テスト
    public IncrementalConvexHull copy() {
        return new IncrementalConvexHull(new TreeMap<>(this.upper), new TreeMap<>(this.lower));
    }

    /**
     * 3点 p, q, r の位置関係を判定する。
     * q-p から r-p への外積を求めることで、
     * 反時計回りのとき正、時計回りのとき負、同一直線上のとき0を返す。
     *
     * @param p 基準点1
     * @param q 基準点2
     * @param r 基準点3
     * @return 位置関係を示す整数 (正: 反時計回り, 負: 時計回り, 0: 同一直線上)
     */
    private static int orientation(LongPoint p, LongPoint q, LongPoint r) {
        long dx0 = q.x() - p.x();
        long dy0 = q.y() - p.y();
        long dx1 = r.x() - p.x();
        long dy1 = r.y() - p.y();
        long cross = dx0 * dy1 - dy0 * dx1;
        return Long.compare(cross, 0);
    }

    /**
     * 点 (x, y) を凸包に追加する。
     * 計算量: ならし O(log N)
     * // 未テスト
     *
     * @param x 点のx座標
     * @param y 点のy座標
     * @return 凸包に変化があった場合（点が新たに追加された場合）は true、既存の凸包に含まれ変化がなかった場合は false
     */
    public boolean add(long x, long y) {
    	//https://judge.yosupo.jp/submission/385509
        boolean changedUpper = addUpper(x, y);
        boolean changedLower = addLower(x, y);
        return changedUpper || changedLower;
    }

    /**
     * 点 p を凸包に追加する。
     * 計算量: ならし O(log N)
     * // 未テスト
     *
     * @param p 追加する点
     * @return 凸包に変化があった場合（点が新たに追加された場合）は true、既存の凸包に含まれ変化がなかった場合は false
     */
    public boolean add(LongPoint p) {
        return add(p.x(), p.y());
    }

    /**
     * 点 (x, y) を上側凸包に追加・更新する。
     * 既存の凸包頂点との位置関係を判定し、追加された点によって不要（冗長）になった隣接頂点を左右に探索・剪定する。
     *
     * @param x 点のx座標
     * @param y 点のy座標
     * @return 上側凸包に変更があった場合は true、既存の上側凸包に含まれており変化がなかった場合は false
     */
    private boolean addUpper(long x, long y) {
        Long prevY = upper.get(x);
        if (prevY != null && prevY >= y) {
            return false;
        }
        if (prevY != null) {
            upper.remove(x);
        }

        Map.Entry<Long, Long> L_entry = upper.lowerEntry(x);
        Map.Entry<Long, Long> R_entry = upper.higherEntry(x);
        if (L_entry != null && R_entry != null) {
            LongPoint L = new LongPoint(L_entry.getKey(), L_entry.getValue());
            LongPoint P = new LongPoint(x, y);
            LongPoint R = new LongPoint(R_entry.getKey(), R_entry.getValue());
            if (orientation(L, P, R) >= 0) {
                return false;
            }
        }

        upper.put(x, y);
        LongPoint P = new LongPoint(x, y);

        // 右側の不要な頂点を剪定
        while (true) {
            Map.Entry<Long, Long> R1_entry = upper.higherEntry(P.x());
            if (R1_entry == null) break;
            Map.Entry<Long, Long> R2_entry = upper.higherEntry(R1_entry.getKey());
            if (R2_entry == null) break;

            LongPoint R1 = new LongPoint(R1_entry.getKey(), R1_entry.getValue());
            LongPoint R2 = new LongPoint(R2_entry.getKey(), R2_entry.getValue());
            if (orientation(P, R1, R2) >= 0) {
                upper.remove(R1.x());
            } else {
                break;
            }
        }

        // 左側の不要な頂点を剪定
        while (true) {
            Map.Entry<Long, Long> L1_entry = upper.lowerEntry(P.x());
            if (L1_entry == null) break;
            Map.Entry<Long, Long> L2_entry = upper.lowerEntry(L1_entry.getKey());
            if (L2_entry == null) break;

            LongPoint L1 = new LongPoint(L1_entry.getKey(), L1_entry.getValue());
            LongPoint L2 = new LongPoint(L2_entry.getKey(), L2_entry.getValue());
            if (orientation(L2, L1, P) >= 0) {
                upper.remove(L1.x());
            } else {
                break;
            }
        }

        return true;
    }

    /**
     * 点 (x, y) を下側凸包に追加・更新する。
     * 既存の凸包頂点との位置関係を判定し、追加された点によって不要（冗長）になった隣接頂点を左右に探索・剪定する。
     *
     * @param x 点のx座標
     * @param y 点のy座標
     * @return 下側凸包に変更があった場合は true、既存の下側凸包に含まれており変化がなかった場合は false
     */
    private boolean addLower(long x, long y) {
        Long prevY = lower.get(x);
        if (prevY != null && prevY <= y) {
            return false;
        }
        if (prevY != null) {
            lower.remove(x);
        }

        Map.Entry<Long, Long> L_entry = lower.lowerEntry(x);
        Map.Entry<Long, Long> R_entry = lower.higherEntry(x);
        if (L_entry != null && R_entry != null) {
            LongPoint L = new LongPoint(L_entry.getKey(), L_entry.getValue());
            LongPoint P = new LongPoint(x, y);
            LongPoint R = new LongPoint(R_entry.getKey(), R_entry.getValue());
            if (orientation(L, P, R) <= 0) {
                return false;
            }
        }

        lower.put(x, y);
        LongPoint P = new LongPoint(x, y);

        // 右側の不要な頂点を剪定
        while (true) {
            Map.Entry<Long, Long> R1_entry = lower.higherEntry(P.x());
            if (R1_entry == null) break;
            Map.Entry<Long, Long> R2_entry = lower.higherEntry(R1_entry.getKey());
            if (R2_entry == null) break;

            LongPoint R1 = new LongPoint(R1_entry.getKey(), R1_entry.getValue());
            LongPoint R2 = new LongPoint(R2_entry.getKey(), R2_entry.getValue());
            if (orientation(P, R1, R2) <= 0) {
                lower.remove(R1.x());
            } else {
                break;
            }
        }

        // 左側の不要な頂点を剪定
        while (true) {
            Map.Entry<Long, Long> L1_entry = lower.lowerEntry(P.x());
            if (L1_entry == null) break;
            Map.Entry<Long, Long> L2_entry = lower.lowerEntry(L1_entry.getKey());
            if (L2_entry == null) break;

            LongPoint L1 = new LongPoint(L1_entry.getKey(), L1_entry.getValue());
            LongPoint L2 = new LongPoint(L2_entry.getKey(), L2_entry.getValue());
            if (orientation(L2, L1, P) <= 0) {
                lower.remove(L1.x());
            } else {
                break;
            }
        }

        return true;
    }

    /**
     * 指定された座標 (x, y) が凸包の内部（境界線を含む）にあるかを判定する。
     * 計算量: O(log N)
     * // 未テスト
     *
     * @param x 判定する点のx座標
     * @param y 判定する点のy座標
     * @return 凸包の内部または境界線上にあれば true、そうでなければ false
     */
    public boolean contains(long x, long y) {
        if (upper.isEmpty()) {
            return false;
        }
        long minX = upper.firstKey();
        long maxX = upper.lastKey();
        if (x < minX || x > maxX) {
            return false;
        }

        LongPoint P = new LongPoint(x, y);

        // 上側凸包によるチェック
        if (upper.containsKey(x)) {
            if (y > upper.get(x)) {
                return false;
            }
        } else {
            Map.Entry<Long, Long> L_entry = upper.lowerEntry(x);
            Map.Entry<Long, Long> R_entry = upper.higherEntry(x);
            if (L_entry == null || R_entry == null) {
                return false;
            }
            LongPoint L = new LongPoint(L_entry.getKey(), L_entry.getValue());
            LongPoint R = new LongPoint(R_entry.getKey(), R_entry.getValue());
            if (orientation(L, P, R) < 0) {
                return false;
            }
        }

        // 下側凸包によるチェック
        if (lower.containsKey(x)) {
            if (y < lower.get(x)) {
                return false;
            }
        } else {
            Map.Entry<Long, Long> L_entry = lower.lowerEntry(x);
            Map.Entry<Long, Long> R_entry = lower.higherEntry(x);
            if (L_entry == null || R_entry == null) {
                return false;
            }
            LongPoint L = new LongPoint(L_entry.getKey(), L_entry.getValue());
            LongPoint R = new LongPoint(R_entry.getKey(), R_entry.getValue());
            if (orientation(L, P, R) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 点 p が凸包の内部（境界線を含む）にあるかを判定する。
     * 計算量: O(log N)
     * // 未テスト
     *
     * @param p 判定する点
     * @return 凸包の内部または境界線上にあれば true、そうでなければ false
     */
    public boolean contains(LongPoint p) {
        return contains(p.x(), p.y());
    }

    /**
     * 現在の凸包を構成する極値頂点を反時計回りの順序で取得する。
     * 計算量: O(N)
     * // 未テスト
     *
     * @return 反時計回りの頂点リスト
     */
    public List<LongPoint> getVertices() {
    	//https://judge.yosupo.jp/submission/385509
        List<LongPoint> res = new ArrayList<>();
        if (upper.isEmpty()) {
            return res;
        }

        if (upper.size() == 1) {
            res.add(new LongPoint(upper.firstKey(), lower.firstEntry().getValue()));
            if (!upper.get(upper.firstKey()).equals(lower.get(lower.firstKey()))) {
                res.add(new LongPoint(upper.firstKey(), upper.firstEntry().getValue()));
            }
            return res;
        }

        // 下側凸包を左から右へ
        for (Map.Entry<Long, Long> entry : lower.entrySet()) {
            res.add(new LongPoint(entry.getKey(), entry.getValue()));
        }

        // 上側凸包を右から左へ。端点は、上下のy座標が一致する場合のみスキップする（垂直境界を正しく管理するため）
        long firstKey = upper.firstKey();
        long lastKey = upper.lastKey();
        for (Map.Entry<Long, Long> entry : upper.descendingMap().entrySet()) {
            long x = entry.getKey();
            if (x == firstKey && entry.getValue().equals(lower.get(firstKey))) {
                continue;
            }
            if (x == lastKey && entry.getValue().equals(lower.get(lastKey))) {
                continue;
            }
            res.add(new LongPoint(x, entry.getValue()));
        }

        return res;
    }

    /**
     * 凸包の面積の2倍を返す。頂点座標がすべて整数のため、この値は必ず整数になる。
     * 計算量: O(N)
     * // 未テスト
     *
     * @return 凸包の面積の2倍を表す long値
     */
    public long getDoubleArea() {
        List<LongPoint> vertices = getVertices();
        if (vertices.size() < 3) {
            return 0L;
        }
        long area2 = 0L;
        for (int i = 0; i < vertices.size(); i++) {
            LongPoint curr = vertices.get(i);
            LongPoint next = vertices.get((i + 1) % vertices.size());
            long term1 = curr.x() * next.y();
            long term2 = curr.y() * next.x();
            area2 += (term1 - term2);
        }
        return Math.abs(area2);
    }

    /**
     * ベクトル (dx, dy) とのドット積 x * dx + y * dy を最大化する凸包の頂点（極値点）を返す。
     * なお、本メソッドでは、凸包の辺上の頂点（角ではない中途の点、すなわち同一直線上の冗長な点）は考慮対象外であり、
     * 凸包の角をなす極値頂点（極値点）のみを対象として探索を行います。
     * 計算量: O(log(coordinate_range) * log N)
     * // 未テスト
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分
     * @return ドット積を最大化する LongPoint、凸包が空なら null
     */
    public LongPoint getExtremePoint(long dx, long dy) {
        if (upper.isEmpty()) {
            return null;
        }
        if (dy == 0) {
            if (dx >= 0) {
                long x = upper.lastKey();
                return new LongPoint(x, upper.get(x));
            } else {
                long x = upper.firstKey();
                return new LongPoint(x, upper.get(x));
            }
        }

        if (dy > 0) {
            return getExtremeUpper(dx, dy);
        } else {
            return getExtremeLower(dx, dy);
        }
    }

    /**
     * 上側凸包において、指定されたベクトル (dx, dy) (dy > 0) とのドット積を最大化する極値点を探索する。
     * 凸包の上側は上に凸な形状であるため、ドット積の評価値はx座標に関して単峰性（unimodal）を持つ。
     * この性質を利用して、x座標範囲に対する二分探索により O(log(coordinate_range) * log N) で最適解を求める。
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分（正数）
     * @return ドット積を最大化する上側凸包の頂点
     */
    private LongPoint getExtremeUpper(long dx, long dy) {
        if (upper.isEmpty()) {
            return null;
        }
        long L = upper.firstKey();
        long R = upper.lastKey();
        LongPoint best = null;
        Long bestVal = null;

        while (L <= R) {
            long mid = L + (R - L) / 2;
            Long K = upper.ceilingKey(mid);
            if (K == null || K > R) {
                K = upper.floorKey(mid);
            }
            if (K == null || K < L) {
                break;
            }

            LongPoint pK = new LongPoint(K, upper.get(K));
            long valK = pK.x() * dx + pK.y() * dy;

            if (bestVal == null || valK > bestVal) {
                bestVal = valK;
                best = pK;
            }

            Long prevK = upper.lowerKey(K);
            Long nextK = upper.higherKey(K);

            Long valPrev = null;
            if (prevK != null && prevK >= L) {
                LongPoint pPrev = new LongPoint(prevK, upper.get(prevK));
                valPrev = pPrev.x() * dx + pPrev.y() * dy;
            }

            Long valNext = null;
            if (nextK != null && nextK <= R) {
                LongPoint pNext = new LongPoint(nextK, upper.get(nextK));
                valNext = pNext.x() * dx + pNext.y() * dy;
            }

            if (valPrev != null && valPrev > valK) {
                R = prevK;
            } else if (valNext != null && valNext > valK) {
                L = nextK;
            } else {
                return pK;
            }
        }
        return best;
    }

    /**
     * 下側凸包において、指定されたベクトル (dx, dy) (dy < 0) とのドット積を最大化する極値点を探索する。
     * 凸包の下側は下に凸な形状であり、dy < 0 であるため、ドット積評価値はx座標に関して単峰性（unimodal）を持つ。
     * この性質を利用して、x座標範囲に対する二分探索により O(log(coordinate_range) * log N) で最適解を求める。
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分（負数）
     * @return ドット積を最大化する下側凸包の頂点
     */
    private LongPoint getExtremeLower(long dx, long dy) {
        if (lower.isEmpty()) {
            return null;
        }
        long L = lower.firstKey();
        long R = lower.lastKey();
        LongPoint best = null;
        Long bestVal = null;

        while (L <= R) {
            long mid = L + (R - L) / 2;
            Long K = lower.ceilingKey(mid);
            if (K == null || K > R) {
                K = lower.floorKey(mid);
            }
            if (K == null || K < L) {
                break;
            }

            LongPoint pK = new LongPoint(K, lower.get(K));
            long valK = pK.x() * dx + pK.y() * dy;

            if (bestVal == null || valK > bestVal) {
                bestVal = valK;
                best = pK;
            }

            Long prevK = lower.lowerKey(K);
            Long nextK = lower.higherKey(K);

            Long valPrev = null;
            if (prevK != null && prevK >= L) {
                LongPoint pPrev = new LongPoint(prevK, lower.get(prevK));
                valPrev = pPrev.x() * dx + pPrev.y() * dy;
            }

            Long valNext = null;
            if (nextK != null && nextK <= R) {
                LongPoint pNext = new LongPoint(nextK, lower.get(nextK));
                valNext = pNext.x() * dx + pNext.y() * dy;
            }

            if (valPrev != null && valPrev > valK) {
                R = prevK;
            } else if (valNext != null && valNext > valK) {
                L = nextK;
            } else {
                return pK;
            }
        }
        return best;
    }

    /**
     * ベクトル (dx, dy) とのドット積 x * dx + y * dy の最大値を返す。
     * なお、本メソッドでは、凸包の辺上の頂点（角ではない中途の点、すなわち同一直線上の冗長な点）は考慮対象外であり、
     * 凸包の角をなす極値頂点（極値点）のみを対象としてドット積の最大値を求めます。
     * 計算量: O(log(coordinate_range) * log N)
     * // 未テスト
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分
     * @return ドット積の最大値を表す Long型、凸包が空なら null
     */
    public Long getMaxDotProduct(long dx, long dy) {
    	//https://atcoder.jp/contests/abc244/submissions/77490508
        LongPoint p = getExtremePoint(dx, dy);
        if (p == null) {
            return null;
        }
        return p.x() * dx + p.y() * dy;
    }

    /**
     * ベクトル (dx, dy) とのドット積 x * dx + y * dy を最小化する凸包の頂点（極値点）を返す。
     * なお、本メソッドでは、凸包の辺上の頂点（角ではない中途の点、すなわち同一直線上の冗長な点）は考慮対象外であり、
     * 凸包の角をなす極値頂点（極値点）のみを対象として探索を行います。
     * 計算量: O(log(coordinate_range) * log N)
     * // 未テスト
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分
     * @return ドット積を最小化する LongPoint、凸包が空なら null
     */
    public LongPoint getExtremePointMin(long dx, long dy) {
        return getExtremePoint(-dx, -dy);
    }

    /**
     * ベクトル (dx, dy) とのドット積 x * dx + y * dy の最小値を返す。
     * なお、本メソッドでは、凸包の辺上の頂点（角ではない中途の点、すなわち同一直線上の冗長な点）は考慮対象外であり、
     * 凸包の角をなす極値頂点（極値点）のみを対象としてドット積の最小値を求めます。
     * 計算量: O(log(coordinate_range) * log N)
     * // 未テスト
     *
     * @param dx ベクトルのx成分
     * @param dy ベクトルのy成分
     * @return ドット積の最小値を表す Long型、凸包が空なら null
     */
    public Long getMinDotProduct(long dx, long dy) {
        LongPoint p = getExtremePointMin(dx, dy);
        if (p == null) {
            return null;
        }
        return p.x() * dx + p.y() * dy;
    }

    /**
     * 凸包の頂点数を返す。
     * 計算量: O(N)
     * // 未テスト
     *
     * @return 頂点数
     */
    public int size() {
        return getVertices().size();
    }

	/**
	 * このインクリメンタル凸包と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は凸包の頂点数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IncrementalConvexHull)) return false;
		IncrementalConvexHull other = (IncrementalConvexHull) obj;
		return upper.equals(other.upper) && lower.equals(other.lower);
	}

	/**
	 * このインクリメンタル凸包のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は凸包の頂点数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(upper, lower);
	}
}
