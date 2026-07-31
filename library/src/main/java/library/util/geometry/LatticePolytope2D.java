package library.util.geometry;

import java.util.ArrayList;
import java.util.Comparator;
import library.util.MathUtils;

/**
 * 2次元格子多面体（格子凸多角形）を表すクラス。
 */
public class LatticePolytope2D {
	public final LongPoint[] vertices;

	/**
	 * 点の位置（内部、境界上、外部）を表す列挙型。
	 */
	public enum Location {
		INSIDE, ON_BOUNDARY, OUTSIDE
	}

	/**
	 * 頂点集合から格子多角形を構築する。頂点は反時計回りに並んでいる必要がある。
	 * @param vertices 頂点集合
	 */
	public LatticePolytope2D(LongPoint[] vertices) {
		this.vertices = vertices;
	}

	/**
	 * 多角形の面積の2倍を返す。
	 * 計算量: O(N) (Nは頂点数)
	 * @return 2 * Area
	 */
	public long twiceArea() {
		long area = 0;
		int n = vertices.length;
		for (int i = 0; i < n; i++) {
			long x1 = vertices[i].x;
			long y1 = vertices[i].y;
			long x2 = vertices[(i + 1) % n].x;
			long y2 = vertices[(i + 1) % n].y;
			area += (x1 * y2 - x2 * y1);
		}
		return Math.abs(area);
	}

	/**
	 * 境界上の格子点の個数を返す。
	 * 計算量: O(N log(max(coords)))
	 * @return 境界上の格子点数
	 */
	public long boundaryLatticePoints() {
		long count = 0;
		int n = vertices.length;
		for (int i = 0; i < n; i++) {
			long dx = Math.abs(vertices[i].x - vertices[(i + 1) % n].x);
			long dy = Math.abs(vertices[i].y - vertices[(i + 1) % n].y);
			count += MathUtils.gcd(dx, dy);
		}
		return count;
	}

	/**
	 * 内部の格子点の個数を返す。Pickの定理（A = I + B/2 - 1）を使用する。
	 * 計算量: O(N log(max(coords)))
	 * @return 内部の格子点数
	 */
	public long interiorLatticePoints() {
		// 2A = 2I + B - 2 => 2I = 2A - B + 2
		long area2 = twiceArea();
		long b = boundaryLatticePoints();
		return (area2 - b + 2) / 2;
	}

	/**
	 * 多角形に含まれる格子点の総数を返す（内部 + 境界）。
	 * 計算量: O(N log(max(coords)))
	 * @return 格子点総数
	 */
	public long countLatticePoints() {
		//https://atcoder.jp/contests/typical90/submissions/76788884
		return interiorLatticePoints() + boundaryLatticePoints();
	}

	/**
	 * t倍に拡大したときの格子点数を返す（Ehrhart多項式 L(P, t)）。
	 * L(P, t) = Area * t^2 + (Boundary / 2) * t + 1
	 * 計算量: O(N log(max(coords)))
	 * @param t 拡大率
	 * @return t倍時の格子点数
	 */
	public long countLatticePoints(long t) {
		if (t == 0) return 1;
		long area2 = twiceArea();
		long b = boundaryLatticePoints();
		// ピックの定理より
		// A = I + B / 2 - 1
		// I + B = A + B / 2 - 1
		
		// L(P, t) = (2 * Area * t^2 + Boundary * t + 2) / 2
		return (area2 * t * t + b * t + 2) / 2;
	}

	/**
	 * Ehrhart多項式 L(P, t) = c2 * t^2 + c1 * t + c0 の係数を返す。
	 * 係数は [2*c2, 2*c1, 2*c0] の形式で返す（整数にするため2倍している）。
	 * 計算量: O(N log(max(coords)))
	 * @return [2*c2, 2*c1, 2*c0]
	 */
	public long[] ehrhartCoefficientsTwice() {
		return new long[] {
			twiceArea(),
			boundaryLatticePoints(),
			2
		};
	}

	/**
	 * Ehrhart多項式の係数を返す。精度が必要な場合は ehrhartCoefficientsTwice() を使用すること。
	 * 計算量: O(N log(max(coords)))
	 * @return [c2, c1, c0] (c1 = boundary / 2)
	 */
	public double[] ehrhart() {
		return new double[] {
			twiceArea() / 2.0,
			boundaryLatticePoints() / 2.0,
			1.0
		};
	}

	/**
	 * 点pが多角形（凸多角形を想定）の内部、境界上、外部のどこにあるかを判定する。
	 * 計算量: O(log N)
	 * @param p 判定する点
	 * @return 位置
	 */
	public Location locate(LongPoint p) {
		int n = vertices.length;
		if (n == 0) return Location.OUTSIDE;
		if (n == 1) return vertices[0].equals(p) ? Location.ON_BOUNDARY : Location.OUTSIDE;
		if (n == 2) {
			LongVector v1 = vertices[1].sub(vertices[0]);
			LongVector vp = p.sub(vertices[0]);
			if (v1.cross(vp) != 0) return Location.OUTSIDE;
			long dot = v1.dot(vp);
			if (dot < 0 || dot > v1.squaredLength()) return Location.OUTSIDE;
			return Location.ON_BOUNDARY;
		}

		LongVector v0 = vertices[1].sub(vertices[0]);
		LongVector vp = p.sub(vertices[0]);
		LongVector vn = vertices[n - 1].sub(vertices[0]);

		long cross0 = v0.cross(vp);
		long crossn = vn.cross(vp);

		if (cross0 < 0 || crossn > 0) return Location.OUTSIDE;
		if (cross0 == 0) {
			long dot = v0.dot(vp);
			if (dot < 0 || dot > v0.squaredLength()) return Location.OUTSIDE;
			return Location.ON_BOUNDARY;
		}
		if (crossn == 0) {
			long dot = vn.dot(vp);
			if (dot < 0 || dot > vn.squaredLength()) return Location.OUTSIDE;
			return Location.ON_BOUNDARY;
		}

		int low = 1, high = n - 1;
		while (high - low > 1) {
			int mid = (low + high) / 2;
			if (vertices[mid].sub(vertices[0]).cross(vp) >= 0) {
				low = mid;
			} else {
				high = mid;
			}
		}

		LongVector v_low_high = vertices[high].sub(vertices[low]);
		LongVector v_low_p = p.sub(vertices[low]);
		long cross = v_low_high.cross(v_low_p);
		if (cross < 0) return Location.OUTSIDE;
		if (cross == 0) return Location.ON_BOUNDARY;
		return Location.INSIDE;
	}

	/**
	 * 2つの凸多角形のMinkowski和を返す。
	 * 計算量: O(N + M) (N, Mはそれぞれの頂点数)
	 * @param other 他の多角形
	 * @return Minkowski和としての凸多角形
	 */
	public LatticePolytope2D minkowskiSum(LatticePolytope2D other) {
		int n1 = this.vertices.length;
		int n2 = other.vertices.length;
		if (n1 == 0) return other;
		if (n2 == 0) return this;

		int start1 = 0;
		for (int i = 1; i < n1; i++) {
			if (this.vertices[i].y < this.vertices[start1].y || (this.vertices[i].y == this.vertices[start1].y && this.vertices[i].x < this.vertices[start1].x)) {
				start1 = i;
			}
		}
		int start2 = 0;
		for (int i = 1; i < n2; i++) {
			if (other.vertices[i].y < other.vertices[start2].y || (other.vertices[i].y == other.vertices[start2].y && other.vertices[i].x < other.vertices[start2].x)) {
				start2 = i;
			}
		}

		LongVector[] e1 = new LongVector[n1];
		for (int i = 0; i < n1; i++) e1[i] = this.vertices[(start1 + i + 1) % n1].sub(this.vertices[(start1 + i) % n1]);
		LongVector[] e2 = new LongVector[n2];
		for (int i = 0; i < n2; i++) e2[i] = other.vertices[(start2 + i + 1) % n2].sub(other.vertices[(start2 + i) % n2]);

		Comparator<LongVector> cmp = GeometryUtils.PolarAngleComparatorForVector();
		ArrayList<LongVector> merged = new ArrayList<>();
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

		LongPoint cur = new LongPoint(this.vertices[start1].x + other.vertices[start2].x, this.vertices[start1].y + other.vertices[start2].y);
		LongPoint[] res = new LongPoint[merged.size()];
		for (int i = 0; i < merged.size(); i++) {
			res[i] = cur;
			cur = new LongPoint(cur.x + merged.get(i).x, cur.y + merged.get(i).y);
		}

		return new LatticePolytope2D(res);
	}

	/**
	 * 任意の点集合から凸包を構築し、LatticePolytope2Dを返す。
	 * 計算量: O(N log N) (Nは入力点数)
	 * @param points 点集合
	 * @return 凸包としての格子多角形
	 */
	public static LatticePolytope2D fromPoints(LongPoint[] points) {
		//https://atcoder.jp/contests/typical90/submissions/76788884
		LongPoint[] hull = GeometryUtils.convexHull(points);
		ArrayList<LongPoint> strictly = new ArrayList<>();
		int n = hull.length;
		if (n <= 2) return new LatticePolytope2D(hull);
		for (int i = 0; i < n; i++) {
			LongVector v1 = hull[i].sub(hull[(i + n - 1) % n]);
			LongVector v2 = hull[(i + 1) % n].sub(hull[i]);
			if (v1.cross(v2) != 0) {
				strictly.add(hull[i]);
			}
		}
		if (strictly.size() >= 3) {
			return new LatticePolytope2D(strictly.toArray(new LongPoint[0]));
		} else {
			//全点が一直線に並ぶ。
			LongPoint p0=points[0];
			LongPoint p1=points[1];
			for (int i = 0; i < points.length; i++) {
				if(points[i].x < p0.x || (points[i].x == p0.x && points[i].y < p0.y)) {
					p0 = points[i];
				}
				if(points[i].x > p1.x || (points[i].x == p1.x && points[i].y > p1.y)) {
					p1 = points[i];
				}
			}
			return new LatticePolytope2D(new LongPoint[] {p0, p1});
		}
	}

	/**
	 * 多角形をt倍に拡大したものを返す。
	 * 計算量: O(N)
	 * @param t 拡大率
	 * @return 拡大された多角形
	 */
	public LatticePolytope2D scale(long t) {
		LongPoint[] newVertices = new LongPoint[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = new LongPoint(vertices[i].x * t, vertices[i].y * t);
		}
		return new LatticePolytope2D(newVertices);
	}

	/**
	 * 多角形を(dx, dy)だけ平行移動したものを返す。
	 * 計算量: O(N)
	 * @param dx x方向移動量
	 * @param dy y方向移動量
	 * @return 平行移動された多角形
	 */
	public LatticePolytope2D translate(long dx, long dy) {
		LongPoint[] newVertices = new LongPoint[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = new LongPoint(vertices[i].x + dx, vertices[i].y + dy);
		}
		return new LatticePolytope2D(newVertices);
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
	 * 格子多角形の状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return 格子多角形の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return "LatticePolytope2D { vertices: " + java.util.Arrays.toString(vertices) + " }";
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
	 * この格子多角形と別のオブジェクトの同値性を判定します（巡回シフトを考慮します）。
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
		if (!(obj instanceof LatticePolytope2D)) return false;
		LatticePolytope2D other = (LatticePolytope2D) obj;
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
	 * この格子多角形のハッシュコードを計算します（巡回シフト不変）。
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
				LongPoint p1 = vertices[(bestShift + i) % n];
				LongPoint p2 = vertices[(shift + i) % n];
				int cmpX = Long.compare(p1.x(), p2.x());
				if (cmpX != 0) {
					if (cmpX > 0) {
						bestShift = shift;
					}
					break;
				}
				int cmpY = Long.compare(p1.y(), p2.y());
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
			LongPoint p = vertices[(bestShift + i) % n];
			h = 31 * h + (p == null ? 0 : p.hashCode());
		}
		return h;
	}
}
