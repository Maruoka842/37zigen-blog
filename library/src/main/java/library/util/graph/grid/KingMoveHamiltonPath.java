package library.util.graph.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class KingMoveHamiltonPath {
	private KingMoveHamiltonPath() {}
	/**
	 * n x m の KingMove グラフ全体をちょうど 1 回ずつ通る Hamilton path を構成する。
	 * 隣り合う 2 頂点 (r0, c0), (r1, c1) は常に max(|r0-r1|, |c0-c1|) == 1 を満たす。
	 *
	 * @param n 行数
	 * @param m 列数
	 * @return path[k] = {row, col} で表される長さ n*m の頂点列。n <= 0 または m <= 0 の場合は空配列
	 */
	public static int[][] kingMoveHamiltonPath(int n, int m) {
		if (n <= 0 || m <= 0) return new int[0][2];
		int[][] path = new int[n * m][2];
		buildKingMoveHamiltonPath(0, 0, n, m, true, path, 0);
		return path;
	}

	/**
	 * n x m の KingMove グラフ全体をちょうど 1 回ずつ通る Hamilton path を、始点 (x0, y0) から終点 (x1, y1) へ構成する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return path[k] = {row, col} で表される長さ n*m の頂点列。存在しない場合は null
	 */
	public static int[][] kingMoveHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		//https://atcoder.jp/contests/abc232/submissions/77337617
		if (!existsKingMoveHamiltonPath(x0, y0, x1, y1, n, m)) return null;
		int[][] path = new int[n * m][2];
		HashMap<Long, int[]> cache = new HashMap<>();
		int end = buildKingMoveHamiltonPath(0, 0, n, m, x0, y0, x1, y1, path, 0, cache);
		if (end != n * m) {
			throw new AssertionError("construct failed: n=" + n + ", m=" + m + ", start=(" + x0 + "," + y0 + "), goal=(" + x1 + "," + y1 + "), end=" + end);
		}
		return path;
	}

	/**
	 * n x m の KingMove グラフで、始点 (x0, y0) から終点 (x1, y1) へ全頂点をちょうど 1 回ずつ通る
	 * Hamilton path が存在するかどうかを判定する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 条件を満たす Hamilton path が存在するとき true、存在しないとき false
	 */
	public static boolean existsKingMoveHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		if (n <= 0 || m <= 0) return false;
		if (x0 < 0 || x0 >= n || y0 < 0 || y0 >= m || x1 < 0 || x1 >= n || y1 < 0 || y1 >= m) return false;
		if (x0 == x1 && y0 == y1) return n == 1 && m == 1;
		if (n == 1) return y0 == 0 && y1 == m - 1 || y0 == m - 1 && y1 == 0;
		if (m == 1) return x0 == 0 && x1 == n - 1 || x0 == n - 1 && x1 == 0;
		if (n == 2 && y0 == y1 && 0 < y0 && y0 < m - 1) return false;
		if (m == 2 && x0 == x1 && 0 < x0 && x0 < n - 1) return false;
		return true;
	}

	private static int buildKingMoveHamiltonPath(int r0, int c0, int h, int w, boolean fromTop, int[][] path, int idx) {
		if (w == 1) {
			if (fromTop) {
				for (int r = 0; r < h; r++) {
					path[idx][0] = r0 + r;
					path[idx++][1] = c0;
				}
			} else {
				for (int r = h - 1; r >= 0; r--) {
					path[idx][0] = r0 + r;
					path[idx++][1] = c0;
				}
			}
			return idx;
		}
		int lw = w / 2;
		int rw = w - lw;
		idx = buildKingMoveHamiltonPath(r0, c0, h, lw, fromTop, path, idx);
		boolean rightFromTop = (lw % 2 == 0) == fromTop;
		return buildKingMoveHamiltonPath(r0, c0 + lw, h, rw, rightFromTop, path, idx);
	}

	private static int buildKingMoveHamiltonPath(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, HashMap<Long, int[]> cache) {
		if (!existsKingMoveHamiltonPath(sR, sC, tR, tC, h, w)) return -1;
		if (h == 1) {
			int step = sC < tC ? 1 : -1;
			for (int c = sC; ; c += step) {
				path[idx][0] = r0;
				path[idx++][1] = c0 + c;
				if (c == tC) return idx;
			}
		}
		if (w == 1) {
			int step = sR < tR ? 1 : -1;
			for (int r = sR; ; r += step) {
				path[idx][0] = r0 + r;
				path[idx++][1] = c0;
				if (r == tR) return idx;
			}
		}
		int ret = tryBuildKingMoveHamiltonPathByPeelingBorder(r0, c0, h, w, sR, sC, tR, tC, path, idx, cache);
		if (ret >= 0) return ret;
		ret = tryBuildKingMoveHamiltonPathByCornerBorderPair(r0, c0, h, w, sR, sC, tR, tC, path, idx, cache);
		if (ret >= 0) return ret;
		if (h <= 3) return buildNarrowKingMoveHamiltonPath(r0, c0, h, w, sR, sC, tR, tC, path, idx, false, cache);
		if (w <= 3) return buildNarrowKingMoveHamiltonPath(c0, r0, w, h, sC, sR, tC, tR, path, idx, true, cache);
		ret = tryBuildKingMoveHamiltonPathByVerticalCut(r0, c0, h, w, sR, sC, tR, tC, path, idx, cache);
		if (ret >= 0) return ret;
		ret = tryBuildKingMoveHamiltonPathByHorizontalCut(r0, c0, h, w, sR, sC, tR, tC, path, idx, cache);
		if (ret >= 0) return ret;
		return -1;
	}

	private static int tryBuildKingMoveHamiltonPathByCornerBorderPair(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, HashMap<Long, int[]> cache) {
		if (h < 3 || w < 3 || (h == 3 && w < 4) || (w == 3 && h < 4)) return -1;
		int[][] a = {{0, 1}, {0, w - 2}, {h - 1, 1}, {h - 1, w - 2}};
		int[][] b = {{1, 0}, {1, w - 1}, {h - 2, 0}, {h - 2, w - 1}};
		for (int k = 0; k < 4; k++) {
			boolean rev = false;
			if (sR == a[k][0] && sC == a[k][1] && tR == b[k][0] && tC == b[k][1]) {
				rev = false;
			} else if (sR == b[k][0] && sC == b[k][1] && tR == a[k][0] && tC == a[k][1]) {
				rev = true;
			} else {
				continue;
			}
			int[][] tmp = new int[h * w][2];
			int end = buildTopLeftCornerBorderPair(h, w, tmp, 0, cache);
			if (end != h * w) return -1;
			if (rev) {
				for (int l = 0, r = tmp.length - 1; l < r; l++, r--) {
					int tr0 = tmp[l][0], tc0 = tmp[l][1];
					tmp[l][0] = tmp[r][0]; tmp[l][1] = tmp[r][1];
					tmp[r][0] = tr0; tmp[r][1] = tc0;
				}
			}
			for (int i = 0; i < tmp.length; i++) {
				int rr = tmp[i][0], cc = tmp[i][1];
				if (k == 1 || k == 3) cc = w - 1 - cc;
				if (k == 2 || k == 3) rr = h - 1 - rr;
				path[idx][0] = r0 + rr;
				path[idx++][1] = c0 + cc;
			}
			return idx;
		}
		return -1;
	}

	private static int buildTopLeftCornerBorderPair(int h, int w, int[][] path, int idx, HashMap<Long, int[]> cache) {
		path[idx][0] = 0; path[idx++][1] = 1;
		path[idx][0] = 0; path[idx++][1] = 0;
		path[idx][0] = 1; path[idx++][1] = 1;
		if (h == 3) {
			path[idx][0] = 1;
			path[idx++][1] = 2;
			path[idx][0] = 0;
			path[idx++][1] = 2;
			for (int c = 3; c < w; c++) {
				path[idx][0] = 0;
				path[idx++][1] = c;
				path[idx][0] = 1;
				path[idx++][1] = c;
			}
			for (int c = w - 1; c >= 0; c--) {
				path[idx][0] = 2;
				path[idx++][1] = c;
			}
			path[idx][0] = 1; path[idx++][1] = 0;
			return idx;
		}
		for (int c = 2; c < w; c++) {
			path[idx][0] = 0;
			path[idx++][1] = c;
		}
		for (int c = w - 1; c >= 2; c--) {
			path[idx][0] = 1;
			path[idx++][1] = c;
		}
		int ret = buildKingMoveHamiltonPath(2, 0, h - 2, w, 0, 1, 0, 0, path, idx, cache);
		if (ret < 0) return -1;
		path[ret][0] = 1;
		path[ret++][1] = 0;
		return ret;
	}

	private static int tryBuildKingMoveHamiltonPathByPeelingBorder(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, HashMap<Long, int[]> cache) {
		if (h < 3 || w < 3) return -1;
		boolean sB = isKingMoveGridBorder(sR, sC, h, w);
		boolean tB = isKingMoveGridBorder(tR, tC, h, w);
		if (sB == tB) return -1;
		int len = 2 * h + 2 * w - 4;
		int[] br = new int[len], bc = new int[len];
		int p = 0;
		for (int c = 0; c < w; c++) { br[p] = 0; bc[p++] = c; }
		for (int r = 1; r < h; r++) { br[p] = r; bc[p++] = w - 1; }
		for (int c = w - 2; c >= 0; c--) { br[p] = h - 1; bc[p++] = c; }
		for (int r = h - 2; r >= 1; r--) { br[p] = r; bc[p++] = 0; }
		if (sB) {
			int sId = kingMoveBorderIndex(br, bc, len, sR, sC);
			for (int dir = -1; dir <= 1; dir += 2) {
				int endId = (sId - dir + len) % len;
				for (int nr = br[endId] - 1; nr <= br[endId] + 1; nr++) for (int nc = bc[endId] - 1; nc <= bc[endId] + 1; nc++) {
					if (nr <= 0 || nr >= h - 1 || nc <= 0 || nc >= w - 1) continue;
					if (!existsKingMoveHamiltonPath(nr - 1, nc - 1, tR - 1, tC - 1, h - 2, w - 2)) continue;
					for (int i = 0, id = sId; i < len; i++, id = (id + dir + len) % len) {
						path[idx + i][0] = r0 + br[id];
						path[idx + i][1] = c0 + bc[id];
					}
					int ret = buildKingMoveHamiltonPath(r0 + 1, c0 + 1, h - 2, w - 2, nr - 1, nc - 1, tR - 1, tC - 1, path, idx + len, cache);
					if (ret >= 0) return ret;
				}
			}
		} else {
			int tId = kingMoveBorderIndex(br, bc, len, tR, tC);
			for (int dir = -1; dir <= 1; dir += 2) {
				int entryId = (tId - dir + len) % len;
				for (int nr = br[entryId] - 1; nr <= br[entryId] + 1; nr++) for (int nc = bc[entryId] - 1; nc <= bc[entryId] + 1; nc++) {
					if (nr <= 0 || nr >= h - 1 || nc <= 0 || nc >= w - 1) continue;
					if (!existsKingMoveHamiltonPath(sR - 1, sC - 1, nr - 1, nc - 1, h - 2, w - 2)) continue;
					int midIdx = buildKingMoveHamiltonPath(r0 + 1, c0 + 1, h - 2, w - 2, sR - 1, sC - 1, nr - 1, nc - 1, path, idx, cache);
					if (midIdx < 0) continue;
					for (int i = 0, id = entryId; i < len; i++, id = (id - dir + len) % len) {
						path[midIdx + i][0] = r0 + br[id];
						path[midIdx + i][1] = c0 + bc[id];
					}
					return midIdx + len;
				}
			}
		}
		return -1;
	}

	private static boolean isKingMoveGridBorder(int r, int c, int h, int w) {
		return r == 0 || r == h - 1 || c == 0 || c == w - 1;
	}

	private static int kingMoveBorderIndex(int[] br, int[] bc, int len, int r, int c) {
		for (int i = 0; i < len; i++) if (br[i] == r && bc[i] == c) return i;
		return -1;
	}

	private static int tryBuildKingMoveHamiltonPathByVerticalCut(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, HashMap<Long, int[]> cache) {
		if (sC == tC) return -1;
		int lo = Math.min(sC, tC) + 1, hi = Math.max(sC, tC);
		int[] cuts = adjacentMiddleCuts(lo, hi);
		int[] rows = new int[] {0, h-1};
		for (int cut : cuts) {
			if (cut < lo || cut > hi) continue;
			boolean sLeft = sC < cut;
			for (int r : rows) {
				if (sLeft) {
					if (!existsKingMoveHamiltonPath(sR, sC, r, cut - 1, h, cut)) continue;
					if (!existsKingMoveHamiltonPath(r, 0, tR, tC - cut, h, w - cut)) continue;
					int midIdx = buildKingMoveHamiltonPath(r0, c0, h, cut, sR, sC, r, cut - 1, path, idx, cache);
					if (midIdx < 0) continue;
					int ret = buildKingMoveHamiltonPath(r0, c0 + cut, h, w - cut, r, 0, tR, tC - cut, path, midIdx, cache);
					if (ret >= 0) return ret;
				} else {
					if (!existsKingMoveHamiltonPath(sR, sC - cut, r, 0, h, w - cut)) continue;
					if (!existsKingMoveHamiltonPath(r, cut - 1, tR, tC, h, cut)) continue;
					int midIdx = buildKingMoveHamiltonPath(r0, c0 + cut, h, w - cut, sR, sC - cut, r, 0, path, idx, cache);
					if (midIdx < 0) continue;
					int ret = buildKingMoveHamiltonPath(r0, c0, h, cut, r, cut - 1, tR, tC, path, midIdx, cache);
					if (ret >= 0) return ret;
				}
			}
		}
		return -1;
	}

	private static int tryBuildKingMoveHamiltonPathByHorizontalCut(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, HashMap<Long, int[]> cache) {
		if (sR == tR) return -1;
		int lo = Math.min(sR, tR) + 1, hi = Math.max(sR, tR);
		int[] cuts = adjacentMiddleCuts(lo, hi);
		int[] cols = new int[] {0, w-1};
		for (int cut : cuts) {
			if (cut < lo || cut > hi) continue;
			boolean sTop = sR < cut;
			for (int c : cols) {
				if (sTop) {
					if (!existsKingMoveHamiltonPath(sR, sC, cut - 1, c, cut, w)) continue;
					if (!existsKingMoveHamiltonPath(0, c, tR - cut, tC, h - cut, w)) continue;
					int midIdx = buildKingMoveHamiltonPath(r0, c0, cut, w, sR, sC, cut - 1, c, path, idx, cache);
					if (midIdx < 0) continue;
					int ret = buildKingMoveHamiltonPath(r0 + cut, c0, h - cut, w, 0, c, tR - cut, tC, path, midIdx, cache);
					if (ret >= 0) return ret;
				} else {
					if (!existsKingMoveHamiltonPath(sR - cut, sC, 0, c, h - cut, w)) continue;
					if (!existsKingMoveHamiltonPath(cut - 1, c, tR, tC, cut, w)) continue;
					int midIdx = buildKingMoveHamiltonPath(r0 + cut, c0, h - cut, w, sR - cut, sC, 0, c, path, idx, cache);
					if (midIdx < 0) continue;
					int ret = buildKingMoveHamiltonPath(r0, c0, cut, w, cut - 1, c, tR, tC, path, midIdx, cache);
					if (ret >= 0) return ret;
				}
			}
		}
		return -1;
	}

	private static int[] adjacentMiddleCuts(int lo, int hi) {
		int a = (lo + hi) / 2;
		int b = a + 1 <= hi ? a + 1 : a - 1;
		if (b < lo || b > hi || a == b) return new int[] {a};
		return new int[] {a, b};
	}

	private static int[] adjacentMiddleIndices(int len) {
		int a = (len - 1) / 2;
		int b = a + 1 < len ? a + 1 : a - 1;
		return a == b ? new int[] {a} : new int[] {a, b};
	}

	private static int buildNarrowKingMoveHamiltonPath(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, int[][] path, int idx, boolean swp, HashMap<Long, int[]> cache) {
		int cross = 0;
		int[] ca = new int[3 * h], cb = new int[3 * h];
		for (int a = 0; a < h; a++) for (int b = Math.max(0, a - 1); b <= Math.min(h - 1, a + 1); b++) {
			ca[cross] = a;
			cb[cross++] = b;
		}
		@SuppressWarnings("unchecked")
		HashMap<Integer, Integer>[] prevState = new HashMap[w + 1];
		@SuppressWarnings("unchecked")
		HashMap<Integer, Integer>[] prevMask = new HashMap[w + 1];
		HashMap<Integer, Integer> cur = new HashMap<>();
		cur.put(0, 0);
		for (int c = 0; c < w; c++) {
			HashMap<Integer, Integer> nxtMap = new HashMap<>();
			prevState[c + 1] = new HashMap<>();
			prevMask[c + 1] = new HashMap<>();

			int reqMask = 0;
			for (int r = 0; r < h; r++) {
				if ((r == sR && c == sC) || (r == tR && c == tC)) {
					reqMask |= 1 << r;
				}
			}
			boolean isLast = (c + 1 == w);

			for (int st : cur.keySet()) {
				int[] trans = getNarrowKingMoveTransitions(h, st, reqMask, isLast, ca, cb, cross, cache);
				for (int i = 0; i < trans.length; i += 2) {
					int mask = trans[i];
					int ns = trans[i + 1];
					if (!nxtMap.containsKey(ns)) {
						nxtMap.put(ns, 0);
						prevState[c + 1].put(ns, st);
						prevMask[c + 1].put(ns, mask);
					}
				}
			}
			cur = nxtMap;
		}
		if (!cur.containsKey(0)) return -1;
		List<Integer>[] g = new List[h * w];
		for (int i = 0; i < h * w; i++) g[i] = new ArrayList<>();
		int st = 0;
		for (int c = w - 1; c >= 0; c--) {
			int mask = prevMask[c + 1].get(st);
			for (int r = 0; r + 1 < h; r++) if (((mask >> r) & 1) != 0) addEdge(g, r * w + c, (r + 1) * w + c);
			if (c + 1 < w) {
				for (int e = 0; e < cross; e++) if (((mask >> (h - 1 + e)) & 1) != 0) addEdge(g, ca[e] * w + c, cb[e] * w + c + 1);
			}
			st = prevState[c + 1].get(st);
		}
		int curV = sR * w + sC, par = -1;
		for (int i = 0; i < h * w; i++) {
			int rr = curV / w, cc = curV % w;
			if (!swp) {
				path[idx][0] = r0 + rr;
				path[idx++][1] = c0 + cc;
			} else {
				path[idx][0] = c0 + cc;
				path[idx++][1] = r0 + rr;
			}
			if (curV == tR * w + tC) return i + 1 == h * w ? idx : -1;
			int nv = -1;
			for (int v : g[curV]) if (v != par) {
				nv = v;
				break;
			}
			if (nv < 0) return -1;
			par = curV;
			curV = nv;
		}
		return -1;
	}

	/**
	 * 与えられた状態、要求、および終了判定に基づき、次の列への遷移を取得する。
	 * キャッシュに結果が存在する場合はキャッシュされた配列を返し、存在しない場合は計算してキャッシュする。
	 *
	 * @param h 高さ
	 * @param state 現在の状態
	 * @param reqMask 開始点・終了点がある行のマスク
	 * @param isLast 最後の列であるか
	 * @param ca 隣接行の組のソースインデックス
	 * @param cb 隣接行の組のターゲットインデックス
	 * @param cross 隣接行の遷移数
	 * @param cache 遷移を格納するキャッシュ
	 * @return 遷移先情報の配列。{mask_1, state_1, mask_2, state_2, ...} の形式。
	 *
	 * 計算量: {@code O(2^(h-1+cross))}（キャッシュミス時）または {@code O(1)}（キャッシュヒット時）
	 */
	// 未テスト
	private static int[] getNarrowKingMoveTransitions(int h, int state, int reqMask, boolean isLast, int[] ca, int[] cb, int cross, HashMap<Long, int[]> cache) {
		long key = (((long) h) << 56) ^ (((long) state) << 24) ^ (((long) reqMask) << 1) ^ (isLast ? 1L : 0L);
		int[] cached = cache.get(key);
		if (cached != null) return cached;
		int[] buf = new int[32];
		int size = 0;
		for (int mask = 0; mask < (1 << (h - 1 + cross)); mask++) {
			int ns = nextNarrowKingMoveState(h, state, mask, reqMask, isLast, ca, cb, cross);
			if (ns >= 0) {
				if (size + 2 > buf.length) buf = Arrays.copyOf(buf, buf.length * 2);
				buf[size++] = mask;
				buf[size++] = ns;
			}
		}
		cached = Arrays.copyOf(buf, size);
		cache.put(key, cached);
		return cached;
	}

	private static int nextNarrowKingMoveState(int h, int state, int mask, int reqMask, boolean isLast, int[] ca, int[] cb, int cross) {
		if (isLast && (mask >> (h - 1)) != 0) return -1;
		int[] degIn = new int[h], lab = new int[h];
		decodeNarrowKingMoveState(h, state, degIn, lab);
		int[] p = new int[3 * h];
		for (int i = 0; i < p.length; i++) p[i] = i;
		boolean[] used = new boolean[3 * h];
		for (int r = 0; r < h; r++) {
			used[h + r] = true;
			if (lab[r] >= 0) {
				used[lab[r]] = true;
				if (!twoRowUnion(p, lab[r], h + r)) return -1;
			}
		}
		int[] deg = degIn.clone(), degNext = new int[h];
		for (int r = 0; r + 1 < h; r++) if (((mask >> r) & 1) != 0) {
			deg[r]++;
			deg[r + 1]++;
			if (!twoRowUnion(p, h + r, h + r + 1)) return -1;
		}
		for (int e = 0; e < cross; e++) if (((mask >> (h - 1 + e)) & 1) != 0) {
			int a = ca[e], b = cb[e];
			deg[a]++;
			degNext[b]++;
			used[2 * h + b] = true;
			if (!twoRowUnion(p, h + a, 2 * h + b)) return -1;
		}
		for (int r = 0; r < h; r++) {
			int req = ((reqMask >> r) & 1) != 0 ? 1 : 2;
			if (deg[r] != req || degNext[r] > 2) return -1;
		}
		boolean[] hasNext = new boolean[3 * h], comp = new boolean[3 * h];
		for (int r = 0; r < h; r++) if (degNext[r] > 0) hasNext[twoRowFind(p, 2 * h + r)] = true;
		int compCount = 0;
		for (int i = 0; i < 3 * h; i++) if (used[i] && twoRowFind(p, i) == i) {
			comp[i] = true;
			compCount++;
		}
		if (isLast) return compCount == 1 ? 0 : -1;
		for (int i = 0; i < 3 * h; i++) if (comp[i] && !hasNext[i]) return -1;
		int[] nextLab = new int[h];
		Arrays.fill(nextLab, -1);
		int[] rootToLab = new int[3 * h];
		Arrays.fill(rootToLab, -1);
		int id = 0;
		for (int r = 0; r < h; r++) if (degNext[r] > 0) {
			int root = twoRowFind(p, 2 * h + r);
			if (rootToLab[root] < 0) rootToLab[root] = id++;
			nextLab[r] = rootToLab[root];
		}
		return encodeNarrowKingMoveState(h, degNext, nextLab);
	}

	private static void decodeNarrowKingMoveState(int h, int state, int[] deg, int[] lab) {
		for (int i = 0; i < h; i++) {
			deg[i] = state % 3;
			state /= 3;
		}
		for (int i = 0; i < h; i++) {
			int v = state % (h + 1);
			state /= h + 1;
			lab[i] = v - 1;
		}
	}

	private static int encodeNarrowKingMoveState(int h, int[] deg, int[] lab) {
		int[] map = new int[h];
		Arrays.fill(map, -1);
		int id = 0, code = 0, mul = 1;
		for (int i = 0; i < h; i++) {
			code += deg[i] * mul;
			mul *= 3;
		}
		for (int i = 0; i < h; i++) {
			int v = 0;
			if (deg[i] > 0) {
				if (map[lab[i]] < 0) map[lab[i]] = id++;
				v = map[lab[i]] + 1;
			}
			code += v * mul;
			mul *= h + 1;
		}
		return code;
	}

	private static int twoRowFind(int[] p, int x) {
		if (p[x] == x) return x;
		return p[x] = twoRowFind(p, p[x]);
	}

	private static boolean twoRowUnion(int[] p, int a, int b) {
		a = twoRowFind(p, a);
		b = twoRowFind(p, b);
		if (a == b) return false;
		p[a] = b;
		return true;
	}


	private static void addEdge(List<Integer>[] g, int a, int b) {
		g[a].add(b);
		g[b].add(a);
	}
}
