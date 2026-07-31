package library.util.graph.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class GridHamiltonPath {
	private GridHamiltonPath() {}
	/**
	 * n x m の格子グラフ全体をちょうど 1 回ずつ通る Hamilton path を、始点 (x0, y0) から終点 (x1, y1) へ構成する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 構成に成功した場合は 'L', 'R', 'U', 'D' からなる移動列。存在しない場合は null
	 * @throws AssertionError 存在するはずなのに構成や復元に失敗した場合
	 * Itai, Alon, Christos H. Papadimitriou, and Jayme Luiz Szwarcfiter. "Hamilton paths in grid graphs." SIAM Journal on Computing 11.4 (1982): 676-686.
	 */
	public static char[] hamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		if (!existsHamiltonPath(x0, y0, x1, y1, n, m)) return null;
		GridHamiltonPathSolver solver = new GridHamiltonPathSolver(n, m);
		int[] next = solver.construct(x0, y0, x1, y1);
		if (next == null) {
			throw new AssertionError("construct returned null: n=" + n + ", m=" + m + ", start=(" + x0 + "," + y0 + "), goal=(" + x1 + "," + y1 + ")");
		}
		char[] res = new char[n * m - 1];
		int cur = x0 * m + y0;
		for (int i = 0; i < n * m - 1; i++) {
			int nxt = next[cur];
			if (nxt < 0) {
				int r = cur / m, c = cur % m;
				throw new AssertionError("next is unset at step " + i + ": n=" + n + ", m=" + m + ", start=(" + x0 + "," + y0 + "), goal=(" + x1 + "," + y1 + "), cur=(" + r + "," + c + ")");
			}
			int r1 = cur / m, c1 = cur % m;
			int dr = nxt / m - r1, dc = nxt % m - c1;
			if (dr == -1 && dc == 0) res[i] = 'U';
			else if (dr == 1 && dc == 0) res[i] = 'D';
			else if (dr == 0 && dc == -1) res[i] = 'L';
			else if (dr == 0 && dc == 1) res[i] = 'R';
			else {
				throw new AssertionError("next is not adjacent at step " + i + ": n=" + n + ", m=" + m + ", start=(" + x0 + "," + y0 + "), goal=(" + x1 + "," + y1 + "), cur=(" + r1 + "," + c1 + "), nxt=(" + (nxt / m) + "," + (nxt % m) + ")");
			}
			cur = nxt;
		}
		return res;
	}

	/**
	 * n x m の格子グラフにおいて、始点 (x0, y0) から終点 (x1, y1) へ全頂点をちょうど 1 回ずつ通る
	 * Hamilton path が存在するかどうかを判定する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 条件を満たす Hamilton path が存在するとき true、存在しないとき false
	 * Itai, Alon, Christos H. Papadimitriou, and Jayme Luiz Szwarcfiter. "Hamilton paths in grid graphs." SIAM Journal on Computing 11.4 (1982): 676-686.
	 */
	public static boolean existsHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		if (x0 < 0 || x0 >= n || y0 < 0 || y0 >= m || x1 < 0 || x1 >= n || y1 < 0 || y1 >= m) return false;
		if (x0 == x1 && y0 == y1) return n == 1 && m == 1;
		long area = (long) n * m;
		int p0 = (x0 + y0) & 1, p1 = (x1 + y1) & 1;
		if (area % 2 == 0) {
			if (p0 == p1) return false;
		} else {
			if (p0 != 0 || p1 != 0) return false;
		}
		if (n == 1) return (y0 == 0 && y1 == m - 1) || (y0 == m - 1 && y1 == 0);
		if (m == 1) return (x0 == 0 && x1 == n - 1) || (x0 == n - 1 && x1 == 0);
		if (n == 2) {
			if (y0 == y1 && 1 <= y0 && y0 <= m - 2) return false;
		} else if (m == 2) {
			if (x0 == x1 && 1 <= x0 && x0 <= n - 2) return false;
		}
		if (n == 3 && m % 2 == 0 && isForbiddenF3(n, m, x0, y0, x1, y1)) return false;
		if (m == 3 && n % 2 == 0 && isForbiddenF3(m, n, y0, x0, y1, x1)) return false;
		return true;
	}

	private static boolean isForbiddenF3(int n, int m, int r0, int c0, int r1, int c1) {
		for (int fr = 0; fr < 2; fr++) for (int fc = 0; fc < 2; fc++) {
			int sr = fr == 0 ? r0 : n - 1 - r0, sc = fc == 0 ? c0 : m - 1 - c0;
			int tr = fr == 0 ? r1 : n - 1 - r1, tc = fc == 0 ? c1 : m - 1 - c1;
			if (((sr + sc) & 1) == 1 && (sc + 1 < tc || (sr == 1 && sc < tc))) return true;
		}
		return false;
	}

	private static void buildThinPath(int x0, int y0, int x1, int y1, int n, int m, boolean flipx, boolean flipy, boolean swapxy, char[] ans, int l, int r) {
		if (l > r) return;
		if (x0 > x1 || (x0 == x1 && x0 >= (n + 1) / 2)) { flipx = !flipx; x0 = n - 1 - x0; x1 = n - 1 - x1; }
		if (y0 > y1 || (y0 == y1 && y0 >= (m + 1) / 2)) { flipy = !flipy; y0 = m - 1 - y0; y1 = m - 1 - y1; }
		if (n > m) {
			swapxy = !swapxy;
			int tmp = x0; x0 = y0; y0 = tmp;
			tmp = x1; x1 = y1; y1 = tmp;
			tmp = n; n = m; m = tmp;
		}
		if (n == 1) {
			for (int i = 0; i < m - 1; i++) ans[l++] = moveDir('R', flipx, flipy, swapxy);
		} else if (n == 2) {
			if (y0 != 0) {
				for (int i = 0; i < y0; i++) ans[l++] = moveDir('L', flipx, flipy, swapxy);
				ans[l++] = moveDir(x0 == 0 ? 'D' : 'U', flipx, flipy, swapxy);
				x0 ^= 1;
				for (int i = 0; i < y0; i++) ans[l++] = moveDir('R', flipx, flipy, swapxy);
				m -= y0; y1 -= y0; y0 = 0;
				if (x0 == x1 && y0 == y1) return;
				ans[l++] = moveDir('R', flipx, flipy, swapxy);
				m--; y1--;
			}
			if (y1 != m - 1) {
				for (int i = 0; i < m - 1 - y1; i++) ans[r--] = moveDir('L', flipx, flipy, swapxy);
				ans[r--] = moveDir(x1 == 0 ? 'U' : 'D', flipx, flipy, swapxy);
				x1 ^= 1;
				for (int i = 0; i < m - 1 - y1; i++) ans[r--] = moveDir('R', flipx, flipy, swapxy);
				m = y1 + 1;
				if (x0 == x1 && y0 == y1) return;
				ans[r--] = moveDir('R', flipx, flipy, swapxy);
				y1--; m--;
			}
			while (!(x0 == x1 && y0 == y1)) {
				ans[l++] = moveDir(x0 == 0 ? 'D' : 'U', flipx, flipy, swapxy);
				x0 ^= 1;
				if (!(x0 == x1 && y0 == y1)) {
					ans[l++] = moveDir('R', flipx, flipy, swapxy);
					y0++;
				}
			}
		}
	}

	private static char moveDir(char dir, boolean flipx, boolean flipy, boolean swapxy) {
		if (swapxy) {
			if (dir == 'U') dir = 'L';
			else if (dir == 'D') dir = 'R';
			else if (dir == 'L') dir = 'U';
			else dir = 'D';
		}
		if (flipx) {
			if (dir == 'U') dir = 'D';
			else if (dir == 'D') dir = 'U';
		}
		if (flipy) {
			if (dir == 'L') dir = 'R';
			else if (dir == 'R') dir = 'L';
		}
		return dir;
	}

	private static char[] solveNarrowByM(int h, int x0, int y0, int x1, int y1, int m, HashMap<Long, int[]> cache) {
		if (h > 7) return null;
		int sc = 1;
		for (int i = 0; i < h; i++) sc *= (h + 1);
		boolean[][] dp = new boolean[m + 1][sc];
		int[][] prevS = new int[m + 1][sc], choice = new int[m + 1][sc];
		for (int i = 0; i <= m; i++) Arrays.fill(prevS[i], -1);
		dp[0][0] = true;
		for (int c = 0; c < m; c++) {
			int[] req = new int[h];
			for (int r = 0; r < h; r++) req[r] = ((r == x0 && c == y0) || (r == x1 && c == y1)) ? 1 : 2;
			int reqCode = encodeReq(req);
			for (int state = 0; state < sc; state++) {
				if (!dp[c][state]) continue;
				int[] trans = getNarrowTransitions(h, state, req, reqCode, c == m - 1, cache);
				for (int i = 0; i < trans.length; i += 2) {
					int mask = trans[i], nxt = trans[i + 1];
					if (!dp[c + 1][nxt]) {
						dp[c + 1][nxt] = true;
						prevS[c + 1][nxt] = state;
						choice[c + 1][nxt] = mask;
					}
				}
			}
		}
		if (!dp[m][0]) return null;
		List<Integer>[] g = new List[h * m];
		for (int i = 0; i < h * m; i++) g[i] = new ArrayList<>();
		int st = 0;
		for (int c = m; c >= 1; c--) {
			int mask = choice[c][st];
			for (int r = 0; r + 1 < h; r++) if (((mask >> r) & 1) != 0) addEdge(g, r * m + (c - 1), (r + 1) * m + (c - 1));
			if (c - 1 < m - 1) for (int r = 0; r < h; r++) if (((mask >> (h - 1 + r)) & 1) != 0) addEdge(g, r * m + (c - 1), r * m + c);
			st = prevS[c][st];
		}
		int sV = x0 * m + y0, curV = sV, pvV = -1;
		int[] order = new int[h * m];
		order[0] = sV;
		for (int i = 1; i < h * m; i++) {
			int nv = -1;
			for (int v : g[curV]) if (v != pvV) { nv = v; break; }
			if (nv < 0) return null;
			order[i] = nv;
			pvV = curV;
			curV = nv;
		}
		char[] res = new char[h * m - 1];
		for (int i = 0; i < h * m - 1; i++) {
			int a = order[i], b = order[i + 1], ar = a / m, ac = a % m, br = b / m, bc = b % m;
			if (br == ar - 1) res[i] = 'U';
			else if (br == ar + 1) res[i] = 'D';
			else if (bc == ac - 1) res[i] = 'L';
			else res[i] = 'R';
		}
		return res;
	}

	private static int encodeReq(int[] req) {
		int code = 0;
		for (int i = 0; i < req.length; i++) if (req[i] == 1) code |= 1 << i;
		return code;
	}

	private static void addEdge(List<Integer>[] g, int a, int b) {
		g[a].add(b);
		g[b].add(a);
	}

	private static int ufFind(int[] parent, int x) {
		if (parent[x] == x) return x;
		return parent[x] = ufFind(parent, parent[x]);
	}

	private static void ufUnion(int[] p, int[] ec, int a, int b) {
		a = ufFind(p, a);
		b = ufFind(p, b);
		if (a != b) {
			p[a] = b;
			ec[b] += ec[a];
		}
	}

	private static int[] decodeNarrowState(int code, int h) {
		int[] raw = new int[h];
		int base = h + 1;
		for (int i = 0; i < h; i++) {
			raw[i] = (code % base) - 1;
			code /= base;
		}
		return raw;
	}

	private static int encodeNarrowState(int[] s, int h) {
		int[] canon = new int[h], map = new int[h];
		Arrays.fill(canon, -1);
		Arrays.fill(map, -1);
		int nxtL = 0;
		for (int i = 0; i < h; i++) {
			if (s[i] < 0) continue;
			if (map[s[i]] < 0) map[s[i]] = nxtL++;
			canon[i] = map[s[i]];
		}
		int code = 0, mul = 1, base = h + 1;
		for (int i = 0; i < h; i++) {
			code += (canon[i] + 1) * mul;
			mul *= base;
		}
		return code;
	}

	private static int[] getNarrowTransitions(int h, int state, int[] req, int reqCode, boolean isLast, HashMap<Long, int[]> cache) {
		long key = (((long) h) << 56) ^ (((long) state) << 24) ^ (((long) reqCode) << 1) ^ (isLast ? 1L : 0L);
		int[] cached = cache.get(key);
		if (cached != null) return cached;
		int[] buf = new int[32];
		int size = 0;
		for (int mask = 0; mask < (1 << (2 * h - 1)); mask++) {
			int nxtS = nextNarrowState(h, state, mask, req, isLast);
			if (nxtS >= 0) {
				if (size + 2 > buf.length) buf = Arrays.copyOf(buf, buf.length * 2);
				buf[size++] = mask;
				buf[size++] = nxtS;
			}
		}
		cached = Arrays.copyOf(buf, size);
		cache.put(key, cached);
		return cached;
	}

	private static int nextNarrowState(int h, int sC, int mask, int[] req, boolean isL) {
		int[] left = decodeNarrowState(sC, h), p = new int[3 * h], ec = new int[3 * h];
		boolean[] used = new boolean[3 * h];
		for (int i = 0; i < 3 * h; i++) p[i] = i;
		int[] lS = new int[h];
		for (int r = 0; r < h; r++) if (left[r] >= 0) lS[left[r]]++;
		int[] rN = new int[h];
		Arrays.fill(rN, -1);
		for (int r = 0; r < h; r++) {
			used[h + r] = true;
			if (req[r] == 1) ec[h + r] = 1;
			if (left[r] >= 0) {
				if (rN[left[r]] < 0) {
					rN[left[r]] = left[r];
					used[left[r]] = true;
					if (lS[left[r]] == 1) ec[left[r]] = 1;
				}
				ufUnion(p, ec, h + r, rN[left[r]]);
			}
		}
		for (int r = 0; r < h; r++) {
			if ((left[r] >= 0 ? 1 : 0) + (r > 0 ? (mask >> (r - 1)) & 1 : 0) + (r + 1 < h ? (mask >> r) & 1 : 0) + ((mask >> (h - 1 + r)) & 1) != req[r]) return -1;
		}
		for (int r = 0; r + 1 < h; r++) if (((mask >> r) & 1) != 0) ufUnion(p, ec, h + r, h + r + 1);
		for (int r = 0; r < h; r++) if (((mask >> (h - 1 + r)) & 1) != 0) {
			used[2 * h + r] = true;
			ufUnion(p, ec, h + r, 2 * h + r);
		}
		int rCount = 0;
		int[] nxtS = new int[h];
		Arrays.fill(nxtS, -1);
		int[] rM = new int[3 * h];
		for (int r = 0; r < h; r++) if (((mask >> (h - 1 + r)) & 1) != 0) rM[ufFind(p, 2 * h + r)] |= 1 << r;
		int nxtL = 0;
		for (int i = 0; i < 3 * h; i++) {
			if (!used[i] || ufFind(p, i) != i) continue;
			rCount++;
			if (ec[i] + Integer.bitCount(rM[i]) != 2) return -1;
			if (rM[i] == 0) {
				if (!isL) return -1;
				continue;
			}
			int id = nxtL++;
			for (int r = 0; r < h; r++) if (((rM[i] >> r) & 1) != 0) nxtS[r] = id;
		}
		if (isL) return rCount == 1 ? 0 : -1;
		return encodeNarrowState(nxtS, h);
	}

	private static final class GridHamiltonPathSolver {
		private final int rows;
		private final int cols;
		private final int[] next;
		private final HashMap<Long, int[]> narrowTransitionCache = new HashMap<>();

		private GridHamiltonPathSolver(int rows, int cols) {
			this.rows = rows;
			this.cols = cols;
			this.next = new int[rows * cols];
			Arrays.fill(next, -1);
		}

		private int[] construct(int x0, int y0, int x1, int y1) {
			if (constructRect(0, 0, rows, cols, x0, y0, x1, y1, false)) return next;
			return null;
		}

		private boolean constructRect(int r0, int c0, int h, int w, int sR, int sC, int tR, int tC, boolean swp) {
			if ((h <= 3 && w <= 3) || (h == 4 && w == 5) || (h == 5 && w == 4)) {
				char[] mv = solveNarrowByM(h, sR - r0, sC - c0, tR - r0, tC - c0, w, narrowTransitionCache);
				if (mv == null) throw new AssertionError("solveNarrow failed: r0=" + r0 + ", c0=" + c0 + ", h=" + h + ", w=" + w + ", sR=" + sR + ", sC=" + sC + ", tR=" + tR + ", tC=" + tC + ", swp=" + swp);
				applyMoves(sR, sC, mv, swp);
				return true;
			}
			if (h > w) return constructRect(c0, r0, w, h, sC, sR, tC, tR, !swp);
			if (h <= 2) {
				char[] mv = new char[h * w - 1];
				buildThinPath(sR - r0, sC - c0, tR - r0, tC - c0, h, w, false, false, false, mv, 0, mv.length - 1);
				applyMoves(sR, sC, mv, swp);
				return true;
			}
			if (w >= 4) {
				int left0 = c0, left1 = c0 + 1;
				if (sC != left0 && sC != left1 && tC != left0 && tC != left1) {
					int nr0 = r0, nc0 = c0 + 2, nh = h, nw = w - 2;
					if (existsHamiltonPath(sR - nr0, sC - nc0, tR - nr0, tC - nc0, nh, nw)) {
						if (constructRect(nr0, nc0, nh, nw, sR, sC, tR, tC, swp) && peelTwo(r0, h, swp, c0 + 2, left0, left1, 1)) return true;
					}
				}
				int right0 = c0 + w - 2, right1 = c0 + w - 1;
				if (sC != right0 && sC != right1 && tC != right0 && tC != right1) {
					int nr0 = r0, nc0 = c0, nh = h, nw = w - 2;
					if (existsHamiltonPath(sR - nr0, sC - nc0, tR - nr0, tC - nc0, nh, nw)) {
						if (constructRect(nr0, nc0, nh, nw, sR, sC, tR, tC, swp) && peelTwo(r0, h, swp, c0 + w - 3, right0, right1, 0)) return true;
					}
				}
			}
			if (h >= 4) {
				int top0 = r0, top1 = r0 + 1;
				if (sR != top0 && sR != top1 && tR != top0 && tR != top1) {
					int nr0 = r0 + 2, nc0 = c0, nh = h - 2, nw = w;
					if (existsHamiltonPath(sR - nr0, sC - nc0, tR - nr0, tC - nc0, nh, nw)) {
						if (constructRect(nr0, nc0, nh, nw, sR, sC, tR, tC, swp) && peelTwo(c0, w, !swp, r0 + 2, top0, top1, 1)) return true;
					}
				}
				int bottom0 = r0 + h - 2, bottom1 = r0 + h - 1;
				if (sR != bottom0 && sR != bottom1 && tR != bottom0 && tR != bottom1) {
					int nr0 = r0, nc0 = c0, nh = h - 2, nw = w;
					if (existsHamiltonPath(sR - nr0, sC - nc0, tR - nr0, tC - nc0, nh, nw)) {
						if (constructRect(nr0, nc0, nh, nw, sR, sC, tR, tC, swp) && peelTwo(c0, w, !swp, r0 + h - 3, bottom0, bottom1, 0)) return true;
					}
				}
			}
			int lowC = Math.min(sC, tC) + 1, highC = Math.max(sC, tC);
			if (lowC <= highC) {
				int baseCut = Math.max(lowC, Math.min(highC, c0 + w / 2));
				int maxCut = Math.min(highC, baseCut + 1);
				for (int r = r0; r <= Math.min(r0 + 1, r0 + h - 1); r++) {
					for (int cut = baseCut; cut <= maxCut; cut++) {
						if (sC < cut && tC >= cut) {
							if (existsHamiltonPath(sR - r0, sC - c0, r - r0, cut - 1 - c0, h, cut - c0) && existsHamiltonPath(r - r0, 0, tR - r0, tC - cut, h, w - (cut - c0))) {
								if (constructRect(r0, c0, h, cut - c0, sR, sC, r, cut - 1, swp) && constructRect(r0, cut, h, w - (cut - c0), r, cut, tR, tC, swp)) {
									setNext(r, cut - 1, r, cut, swp);
									return true;
								}
							}
						}
						if (tC < cut && sC >= cut) {
							if (existsHamiltonPath(r - r0, cut - 1 - c0, tR - r0, tC - c0, h, cut - c0) && existsHamiltonPath(sR - r0, sC - cut, r - r0, 0, h, w - (cut - c0))) {
								if (constructRect(r0, c0, h, cut - c0, r, cut - 1, tR, tC, swp) && constructRect(r0, cut, h, w - (cut - c0), sR, sC, r, cut, swp)) {
									setNext(r, cut, r, cut - 1, swp);
									return true;
								}
							}
						}
					}
				}
			}
			if (h <= 7) {
				char[] mv = solveNarrowByM(h, sR - r0, sC - c0, tR - r0, tC - c0, w, narrowTransitionCache);
				if (mv != null) {
					applyMoves(sR, sC, mv, swp);
					return true;
				}
			}
			throw new AssertionError("constructRect failed: r0=" + r0 + ", c0=" + c0 + ", h=" + h + ", w=" + w + ", sR=" + sR + ", sC=" + sC + ", tR=" + tR + ", tC=" + tC + ", swp=" + swp);
		}

		private void applyMoves(int startR, int startC, char[] mv, boolean swp) {
			int cr = startR, cc = startC;
			for (char m : mv) {
				int nr = cr, nc = cc;
				if (m == 'U') nr--;
				else if (m == 'D') nr++;
				else if (m == 'L') nc--;
				else nc++;
				setNext(cr, cc, nr, nc, swp);
				cr = nr;
				cc = nc;
			}
		}

		private boolean peelTwo(int r0, int h, boolean swp, int coreC, int stripC0, int stripC1, int innerLocalC) {
			for (int r = r0; r + 1 < r0 + h; r++) {
				int v1 = idx(r, coreC, swp), v2 = idx(r + 1, coreC, swp);
				int sr, er;
				if (next[v1] == v2) {
					sr = r;
					er = r + 1;
				} else if (next[v2] == v1) {
					sr = r + 1;
					er = r;
				} else {
					continue;
				}
				char[] mv = hamiltonPath(sr - r0, innerLocalC, er - r0, innerLocalC, h, 2);
				if (mv == null) continue;
				int startStripC = innerLocalC == 0 ? stripC0 : stripC1;
				setNext(sr, coreC, sr, startStripC, swp);
				int cr = sr, cc = startStripC;
				for (char m : mv) {
					int nr = cr, nc = cc;
					if (m == 'U') nr--;
					else if (m == 'D') nr++;
					else if (m == 'L') nc = (cc == stripC0 ? stripC0 - 1 : stripC0);
					else nc = (cc == stripC0 ? stripC1 : stripC1 + 1);
					if (nc != stripC0 && nc != stripC1) return false;
					setNext(cr, cc, nr, nc, swp);
					cr = nr;
					cc = nc;
				}
				setNext(er, innerLocalC == 0 ? stripC0 : stripC1, er, coreC, swp);
				return true;
			}
			return false;
		}

		private int idx(int r, int c, boolean swp) {
			return !swp ? r * cols + c : c * cols + r;
		}

		private void setNext(int r1, int c1, int r2, int c2, boolean swp) {
			int v1 = idx(r1, c1, swp), v2 = idx(r2, c2, swp);
			if (v1 >= 0 && v1 < next.length) next[v1] = v2;
		}
	}
	
}
