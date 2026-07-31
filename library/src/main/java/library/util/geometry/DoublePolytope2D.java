package library.util.geometry;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * 2次元実数多面体（実数凸多角形）を double 精度で表すクラス。
 */
public class DoublePolytope2D {
	public final DoublePoint[] vertices;

	/**
	 * 頂点集合から実数凸多角形を構築する。頂点は反時計回りに並んでいる必要がある。
	 * @param vertices 頂点集合
	 */
	public DoublePolytope2D(DoublePoint[] vertices) {
		this.vertices = vertices;
	}

	/**
	 * 多角形の面積の2倍を返す。
	 * <p>計算量: $O(N)$ (Nは頂点数)</p>
	 * @return 2 * Area
	 */
	public double twiceArea() {
		double area = 0;
		int n = vertices.length;
		for (int i = 0; i < n; i++) {
			double x1 = vertices[i].x();
			double y1 = vertices[i].y();
			double x2 = vertices[(i + 1) % n].x();
			double y2 = vertices[(i + 1) % n].y();
			area += (x1 * y2 - x2 * y1);
		}
		return Math.abs(area);
	}

	/**
	 * 多角形の面積を返す。
	 * <p>計算量: $O(N)$ (Nは頂点数)</p>
	 * @return Area
	 */
	public double area() {
		return twiceArea() / 2.0;
	}

	/**
	 * 2つの凸多角形のMinkowski和を返す。
	 * <p>計算量: $O(N + M)$ (N, Mはそれぞれの頂点数)</p>
	 * @param other 他の多角形
	 * @return Minkowski和としての凸多角形
	 */
	public DoublePolytope2D minkowskiSum(DoublePolytope2D other) {
		int n1 = this.vertices.length;
		int n2 = other.vertices.length;
		if (n1 == 0) return other;
		if (n2 == 0) return this;

		int start1 = 0;
		for (int i = 1; i < n1; i++) {
			if (this.vertices[i].y() < this.vertices[start1].y() || (this.vertices[i].y() == this.vertices[start1].y() && this.vertices[i].x() < this.vertices[start1].x())) {
				start1 = i;
			}
		}
		int start2 = 0;
		for (int i = 1; i < n2; i++) {
			if (other.vertices[i].y() < other.vertices[start2].y() || (other.vertices[i].y() == other.vertices[start2].y() && other.vertices[i].x() < other.vertices[start2].x())) {
				start2 = i;
			}
		}

		DoubleVector[] e1 = new DoubleVector[n1];
		for (int i = 0; i < n1; i++) e1[i] = this.vertices[(start1 + i + 1) % n1].sub(this.vertices[(start1 + i) % n1]);
		DoubleVector[] e2 = new DoubleVector[n2];
		for (int i = 0; i < n2; i++) e2[i] = other.vertices[(start2 + i + 1) % n2].sub(other.vertices[(start2 + i) % n2]);
		Comparator<DoubleVector> cmp = (u, v) -> {
			boolean upU = (u.y() > 0) || (u.y() == 0 && u.x() >= 0);
			boolean upV = (v.y() > 0) || (v.y() == 0 && v.x() >= 0);
			if (upU != upV) {
				return upU ? -1 : 1;
			}
			double cross = u.cross(v);
			return -Double.compare(cross, 0);
		};
		ArrayList<DoubleVector> merged = new ArrayList<>();
		int c1 = 0, c2 = 0;
		while (c1 < n1 || c2 < n2) {
			if (c1 < n1 && c2 < n2) {
				int res = cmp.compare(e1[c1], e2[c2]);
				if (res < 0) {
					merged.add(e1[c1++]);
				} else if (res > 0) {
					merged.add(e2[c2++]);
				} else {
					merged.add(e1[c1++].add(e2[c2++]));
				}
			} else if (c1 < n1) {
				merged.add(e1[c1++]);
			} else {
				merged.add(e2[c2++]);
			}
		}

		DoublePoint cur = new DoublePoint(this.vertices[start1].x() + other.vertices[start2].x(), this.vertices[start1].y() + other.vertices[start2].y());
		DoublePoint[] res = new DoublePoint[merged.size()];
		for (int i = 0; i < merged.size(); i++) {
			res[i] = cur;
			cur = cur.add(merged.get(i));
		}

		return new DoublePolytope2D(res);
	}

	/**
	 * 任意の点集合から凸包を構築し、DoublePolytope2Dを返す。
	 * <p>計算量: $O(N \log N)$ (Nは入力点数)</p>
	 * @param points 点集合
	 * @return 凸包としての凸多角形
	 */
	public static DoublePolytope2D fromPoints(DoublePoint[] points) {
		DoublePoint[] hull = GeometryUtils.convexHull(points);
		ArrayList<DoublePoint> strictly = new ArrayList<>();
		int n = hull.length;
		if (n <= 2) return new DoublePolytope2D(hull);
		for (int i = 0; i < n; i++) {
			DoubleVector v1 = hull[i].sub(hull[(i + n - 1) % n]);
			DoubleVector v2 = hull[(i + 1) % n].sub(hull[i]);
			if (Math.abs(v1.cross(v2)) > 1e-9) {
				strictly.add(hull[i]);
			}
		}
		if (strictly.size() >= 3) {
			return new DoublePolytope2D(strictly.toArray(new DoublePoint[0]));
		} else {
			DoublePoint p0 = points[0];
			DoublePoint p1 = points[1];
			for (int i = 0; i < points.length; i++) {
				if (points[i].x() < p0.x() || (points[i].x() == p0.x() && points[i].y() < p0.y())) {
					p0 = points[i];
				}
				if (points[i].x() > p1.x() || (points[i].x() == p1.x() && points[i].y() > p1.y())) {
					p1 = points[i];
				}
			}
			return new DoublePolytope2D(new DoublePoint[] {p0, p1});
		}
	}

	/**
	 * この多角形を原点を中心として {@code t} 倍に拡大した多角形を返します。
	 *
	 * <p>各頂点 {@code (x, y)} は {@code (tx, ty)} に写されます。
	 * {@code t < 0} の場合は原点に関する反転を伴います。
	 *
	 * <p>計算量: $O(N)$</p>
	 *
	 * @param t 拡大率
	 * @return 原点を中心として {@code t} 倍に拡大した多角形
	 */
	public DoublePolytope2D scale(double t) {
		DoublePoint[] newVertices = new DoublePoint[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = new DoublePoint(vertices[i].x() * t, vertices[i].y() * t);
		}
		return new DoublePolytope2D(newVertices);
	}

	/**
	 * 多角形を(dx, dy)だけ平行移動したものを返す。
	 * <p>計算量: $O(N)$</p>
	 * @param dx x方向移動量
	 * @param dy y方向移動量
	 * @return 平行移動された多角形
	 */
	public DoublePolytope2D translate(double dx, double dy) {
		DoublePoint[] newVertices = new DoublePoint[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = new DoublePoint(vertices[i].x() + dx, vertices[i].y() + dy);
		}
		return new DoublePolytope2D(newVertices);
	}

	/**
	 * 点 P からこの凸多角形への最短距離を計算します。
	 * 点が凸多角形の内部または周上にある場合は 0.0 を返し、外部にある場合は境界への最短距離を返します。
	 * 内部の判定および外部からの距離計算はすべて二分探索・三分探索を用いて高速に行われます。
	 *
	 * <p>計算量: $O(\log N)$、ここで $N$ は凸多角形の頂点数です。</p>
	 *
	 * @param p 判定対象の点
	 * @return 点から凸多角形への最短距離
	 */
	// 未テスト
	public double distance(DoublePoint p) {
		int n = vertices.length;
		if (n == 0) return 0.0;
		if (n == 1) {
			return Math.hypot(p.x() - vertices[0].x(), p.y() - vertices[0].y());
		}
		if (n == 2) {
			return distToSegment(p, vertices[0], vertices[1]);
		}

		if (isInside(p)) {
			return 0.0;
		}

		int closestEdgeIdx = findClosestEdgeIndex(p);
		return distToSegment(p, vertices[closestEdgeIdx], vertices[(closestEdgeIdx + 1) % n]);
	}

	/**
	 * $\operatorname{dist}(P, Q)=\min\{\lVert p-q\rVert_2\mid p\in P,\ q\in Q\}$ を返す。
	 *
	 * <p>計算量: $O(\log N+\log M)$ (N, M はそれぞれの多角形の頂点数)</p>
	 *
	 * @param poly $Q$
	 * @return $\operatorname{dist}(P, Q)$
	 */
	// 未テスト
	public double dist(DoublePolytope2D poly) {
		if (vertices.length == 0 || poly.vertices.length == 0) return 0.0;
		DoublePoint[] simplex = new DoublePoint[3];
		int size = 1;
		simplex[0] = minkowskiSupport(poly, 1.0, 0.0);
		DoublePoint closest = simplex[0];
		for (int iteration = 0; iteration < 64; iteration++) {
			double dx = -closest.x();
			double dy = -closest.y();
			if (dx * dx + dy * dy <= 1e-24) return 0.0;

			DoublePoint candidate = minkowskiSupport(poly, dx, dy);
			for (int i = 0; i < size; i++) {
				if (Math.hypot(candidate.x() - simplex[i].x(), candidate.y() - simplex[i].y()) <= 1e-12) {
					return Math.hypot(closest.x(), closest.y());
				}
			}
			simplex[size++] = candidate;
			if (size == 2) {
				closest = closestToSegment(simplex[0], simplex[1]);
			} else {
				if (containsOrigin(simplex[0], simplex[1], simplex[2])) return 0.0;
				int[] closestEdge = closestEdge(simplex[0], simplex[1], simplex[2]);
				DoublePoint first = simplex[closestEdge[0]];
				DoublePoint second = simplex[closestEdge[1]];
				simplex[0] = first;
				simplex[1] = second;
				size = 2;
				closest = closestToSegment(first, second);
			}
		}
		return Math.hypot(closest.x(), closest.y());
	}

	/**
	 * $\operatorname*{argmax}_{a\in P,b\in Q}\langle a-b,(dx,dy)\rangle$ を返す。
	 *
	 * <p>計算量: $O(\log N+\log M)$</p>
	 */
	// 未テスト
	private DoublePoint minkowskiSupport(DoublePolytope2D poly, double dx, double dy) {
		DoublePoint a = support(vertices, dx, dy);
		DoublePoint b = support(poly.vertices, -dx, -dy);
		return new DoublePoint(a.x() - b.x(), a.y() - b.y());
	}

	/**
	 * $\operatorname*{argmax}_{v\in V}\langle v,(dx,dy)\rangle$ を返す。
	 *
	 * <p>計算量: $O(\log N)$</p>
	 */
	// 未テスト
	private static DoublePoint support(DoublePoint[] points, double dx, double dy) {
		int left = 0;
		int right = points.length - 1;
		while (right - left > 3) {
			int first = left + (right - left) / 3;
			int second = right - (right - left) / 3;
			if (dot(points[first], dx, dy) < dot(points[second], dx, dy)) left = first;
			else right = second;
		}
		DoublePoint best = points[0];
		for (int i = left; i <= right; i++) {
			if (dot(points[i], dx, dy) > dot(best, dx, dy)) best = points[i];
		}
		for (int i = 1; i < points.length; i += points.length - 1) {
			if (dot(points[i], dx, dy) > dot(best, dx, dy)) best = points[i];
		}
		return best;
	}

	/**
	 * $\langle p,(dx,dy)\rangle$ を返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static double dot(DoublePoint p, double dx, double dy) {
		return p.x() * dx + p.y() * dy;
	}

	/**
	 * 線分 $[a,b]$ 上で $\lVert x\rVert_2$ を最小化する $x$ を返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static DoublePoint closestToSegment(DoublePoint a, DoublePoint b) {
		double dx = b.x() - a.x();
		double dy = b.y() - a.y();
		double lengthSquared = dx * dx + dy * dy;
		if (lengthSquared == 0.0) return a;
		double t = -(a.x() * dx + a.y() * dy) / lengthSquared;
		t = Math.max(0.0, Math.min(1.0, t));
		return new DoublePoint(a.x() + t * dx, a.y() + t * dy);
	}

	/**
	 * $0\in\operatorname{conv}\{a,b,c\}$ であるかを返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static boolean containsOrigin(DoublePoint a, DoublePoint b, DoublePoint c) {
		double ab = cross(a, b);
		double bc = cross(b, c);
		double ca = cross(c, a);
		return (ab >= -1e-12 && bc >= -1e-12 && ca >= -1e-12)
			|| (ab <= 1e-12 && bc <= 1e-12 && ca <= 1e-12);
	}

	/**
	 * 三角形の辺で $\min_{x\in e}\lVert x\rVert_2$ を与える辺の端点添字を返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static int[] closestEdge(DoublePoint a, DoublePoint b, DoublePoint c) {
		DoublePoint[] points = {a, b, c};
		int[] best = {0, 1};
		double bestDistance = squaredLength(closestToSegment(a, b));
		int[][] edges = {{1, 2}, {2, 0}};
		for (int[] edge : edges) {
			double distance = squaredLength(closestToSegment(points[edge[0]], points[edge[1]]));
			if (distance < bestDistance) {
				bestDistance = distance;
				best = edge;
			}
		}
		return best;
	}

	/**
	 * $\lVert p\rVert_2^2$ を返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static double squaredLength(DoublePoint p) {
		return p.x() * p.x() + p.y() * p.y();
	}

	/**
	 * $a_xb_y-a_yb_x$ を返す。
	 *
	 * <p>計算量: $O(1)$</p>
	 */
	// 未テスト
	private static double cross(DoublePoint a, DoublePoint b) {
		return a.x() * b.y() - a.y() * b.x();
	}

	/**
	 * 指定された点 P がこの凸多角形の内部または境界上にある（含まれる）かどうかを判定します。
	 * 基準点 $V_0$ からの各頂点の偏角順序に基づき、二分探索を用いて点 P が位置する扇形セクターを特定し、
	 * セクター外境界との位置関係を $O(1)$ 時間で検証することで判定します。
	 *
	 * <p>計算量: $O(\log N)$、ここで $N$ は凸多角形の頂点数です。</p>
	 *
	 * @param p 判定対象の点
	 * @return 点 P が凸多角形の内部または境界上にある場合は true、それ以外の場合は false
	 */
	// 未テスト
	private boolean isInside(DoublePoint p) {
		int n = vertices.length;
		DoubleVector v0 = vertices[1].sub(vertices[0]);
		DoubleVector vp = p.sub(vertices[0]);
		DoubleVector vn = vertices[n - 1].sub(vertices[0]);

		double cross0 = v0.cross(vp);
		double crossn = vn.cross(vp);

		if (cross0 < -1e-9 || crossn > 1e-9) return false;
		if (Math.abs(cross0) < 1e-9) {
			double dot = v0.dot(vp);
			return dot >= -1e-9 && dot <= v0.squaredLength() + 1e-9;
		}
		if (Math.abs(crossn) < 1e-9) {
			double dot = vn.dot(vp);
			return dot >= -1e-9 && dot <= vn.squaredLength() + 1e-9;
		}

		int low = 1, high = n - 1;
		while (high - low > 1) {
			int mid = (low + high) / 2;
			if (vertices[mid].sub(vertices[0]).cross(vp) >= -1e-9) {
				low = mid;
			} else {
				high = mid;
			}
		}

		DoubleVector v_low_high = vertices[high].sub(vertices[low]);
		DoubleVector v_low_p = p.sub(vertices[low]);
		double cross = v_low_high.cross(v_low_p);
		return cross >= -1e-9;
	}

	/**
	 * 与えられた外部点 P に対し、P と凸多角形との接線を形成する左右の2つの頂点のインデックス [L, R] を $O(\log N)$ 時間で求めます。
	 *
	 * <p>計算量: $O(\log N)$、ここで $N$ は凸多角形の頂点数です。</p>
	 *
	 * @param p 外部点 P
	 * @return 左右 of 接線となる頂点インデックスの配列 [L_idx, R_idx]
	 */
	// 未テスト
	public int[] tangents(DoublePoint p) {
		int n = vertices.length;
		if (n == 0) return null;
		if (n == 1) return new int[]{0, 0};
		if (n == 2) return new int[]{0, 1};

		// We want to find a point with a positive cross product (hidden) and a point with a negative cross product (visible)
		int posIdx = -1;
		int negIdx = -1;

		// Sample 8 points to find the two signs
		for (int i = 0; i < 8; i++) {
			int idx = (i * n) / 8;
			double c = getCross(p, idx);
			if (c >= 0) {
				posIdx = idx;
			} else {
				negIdx = idx;
			}
		}

		// If all sampled points have the same sign, we find by linear scan to be 100% safe
		if (posIdx == -1 || negIdx == -1) {
			posIdx = -1;
			negIdx = -1;
			for (int i = 0; i < n; i++) {
				double c = getCross(p, i);
				if (c >= 0) {
					posIdx = i;
				} else {
					negIdx = i;
				}
			}
			if (posIdx == -1 || negIdx == -1) {
				// Inside or degenerate
				return new int[]{0, 0};
			}
		}

		// Transition 1: from positive to negative in CCW direction (Left Tangent L)
		int l1 = 0, r1 = (negIdx - posIdx + n) % n;
		while (l1 < r1) {
			int mid = (l1 + r1) / 2;
			int idx = (posIdx + mid) % n;
			if (getCross(p, idx) >= 0) {
				l1 = mid + 1;
			} else {
				r1 = mid;
			}
		}
		int lIdx = (posIdx + l1) % n;

		// Transition 2: from negative to positive in CCW direction (Right Tangent R)
		int l2 = 0, r2 = (posIdx - negIdx + n) % n;
		while (l2 < r2) {
			int mid = (l2 + r2) / 2;
			int idx = (negIdx + mid) % n;
			if (getCross(p, idx) < 0) {
				l2 = mid + 1;
			} else {
				r2 = mid;
			}
		}
		int rIdx = (negIdx + l2) % n;

		return new int[]{lIdx, rIdx};
	}

	private double getCross(DoublePoint p, int i) {
		int n = vertices.length;
		DoublePoint curr = vertices[i];
		DoublePoint next = vertices[(i + 1) % n];
		return (curr.x() - p.x()) * (next.y() - curr.y()) - (curr.y() - p.y()) * (next.x() - curr.x());
	}

	private int findClosestEdgeIndex(DoublePoint p) {
		int n = vertices.length;
		int[] tangentIdxs = tangents(p);
		int lIdx = tangentIdxs[0];
		int rIdx = tangentIdxs[1];

		int start, end;
		if (getCross(p, rIdx) < 0) {
			start = rIdx;
			end = lIdx;
		} else {
			start = lIdx;
			end = rIdx;
		}

		int len = (end - start + n) % n;
		if (len == 0) {
			return start;
		}

		// Binary/Ternary search on the visible CCW chain from start to end
		int l = 0, r = len - 1;
		while (r - l > 2) {
			int m1 = l + (r - l) / 3;
			int m2 = r - (r - l) / 3;
			int idx1 = (start + m1) % n;
			int idx2 = (start + m2) % n;
			double v1 = distToSegment(p, vertices[idx1], vertices[(idx1 + 1) % n]);
			double v2 = distToSegment(p, vertices[idx2], vertices[(idx2 + 1) % n]);
			if (v1 < v2) {
				r = m2;
			} else {
				l = m1;
			}
		}

		int bestOffset = l;
		int idxL = (start + l) % n;
		double minDist = distToSegment(p, vertices[idxL], vertices[(idxL + 1) % n]);
		for (int i = l + 1; i <= r; i++) {
			int idx = (start + i) % n;
			double d = distToSegment(p, vertices[idx], vertices[(idx + 1) % n]);
			if (d < minDist) {
				minDist = d;
				bestOffset = i;
			}
		}

		return (start + bestOffset) % n;
	}

	private double distToSegment(DoublePoint p, DoublePoint a, DoublePoint b) {
		double dx = b.x() - a.x();
		double dy = b.y() - a.y();
		double lenSq = dx * dx + dy * dy;
		if (lenSq < 1e-18) {
			return Math.hypot(p.x() - a.x(), p.y() - a.y());
		}
		double t = ((p.x() - a.x()) * dx + (p.y() - a.y()) * dy) / lenSq;
		if (t < 0.0) t = 0.0;
		if (t > 1.0) t = 1.0;
		double cx = a.x() + t * dx;
		double cy = a.y() + t * dy;
		return Math.hypot(p.x() - cx, p.y() - cy);
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	/**
	 * 実数凸多角形の状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return 実数凸多角形の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "DoublePolytope2D { vertices: " + java.util.Arrays.toString(vertices) + " }";
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}

	/**
	 * この凸多角形と別のオブジェクトの同値性を判定します（巡回シフトを考慮します）。
	 *
	 * <p>計算量: $O(N^2)$、ここで $N$ は頂点数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DoublePolytope2D)) return false;
		DoublePolytope2D other = (DoublePolytope2D) obj;
		if (vertices == null || other.vertices == null) {
			return vertices == other.vertices;
		}
		int n = vertices.length;
		if (n != other.vertices.length) return false;
		if (n == 0) return true;

		// 巡回シフトによる一致を判定
		for (int shift = 0; shift < n; shift++) {
			boolean match = true;
			for (int i = 0; i < n; i++) {
				if (!vertices[i].equals(other.vertices[(i + shift) % n])) {
					match = false;
					break;
				}
			}
			if (match) return true;
		}
		return false;
	}

	/**
	 * この凸多角形のハッシュコードを計算します（巡回シフト不変）。
	 *
	 * <p>計算量: $O(N^2)$、ここで $N$ は頂点数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		if (vertices == null || vertices.length == 0) return 0;
		int n = vertices.length;
		// 巡回シフトの中で辞書順最小の表現を見つける
		int bestShift = 0;
		for (int shift = 1; shift < n; shift++) {
			for (int i = 0; i < n; i++) {
				DoublePoint p1 = vertices[(bestShift + i) % n];
				DoublePoint p2 = vertices[(shift + i) % n];
				int cmpX = Double.compare(p1.x(), p2.x());
				if (cmpX != 0) {
					if (cmpX > 0) {
						bestShift = shift;
					}
					break;
				}
				int cmpY = Double.compare(p1.y(), p2.y());
				if (cmpY != 0) {
					if (cmpY > 0) {
						bestShift = shift;
					}
					break;
				}
			}
		}
		// 辞書順最小表現のハッシュコードを計算
		int h = 1;
		for (int i = 0; i < n; i++) {
			DoublePoint p = vertices[(bestShift + i) % n];
			h = 31 * h + (p == null ? 0 : p.hashCode());
		}
		return h;
	}
}
