package library.util.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * 2次元平面上の円を double 精度で表すクラスです。
 */
public final class DoubleCircle implements GeometricObject {

	private final DoublePoint center;
	private final double radius;

	/**
	 * 中心と半径を指定して円を構築します。半径が負の場合は {@link IllegalArgumentException} をスローします。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param center 中心点
	 * @param radius 半径（非負）
	 * @throws IllegalArgumentException 半径が負の場合
	 */
	// 未テスト
	public DoubleCircle(DoublePoint center, double radius) {
		if (radius < 0.0) {
			throw new IllegalArgumentException("Radius must be non-negative.");
		}
		this.center = center;
		this.radius = radius;
	}

	/**
	 * 中心点を取得します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return 中心点
	 */
	// 未テスト
	public DoublePoint center() {
		return center;
	}

	/**
	 * 半径を取得します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return 半径
	 */
	// 未テスト
	public double radius() {
		return radius;
	}

	/**
	 * この円と与えられた線分 {@link DoubleSegment} の交点を計算します。
	 * 交点は線分の始点から終点への方向（パラメータ $t \in [0, 1]$）の順に整列され、
	 * 重複する交点（接点など）は重複排除されて返されます。
	 *
	 * <p>幾何的な比較のしきい値（許容誤差 $\epsilon$）として以下の値を使用します：</p>
	 * <ul>
	 *   <li>パラメータ $t$ が線分上にあるかどうかの判定（$t \in [-\epsilon, 1 + \epsilon]$）: $\epsilon = 10^{-9}$</li>
	 *   <li>線分の長さが $0$ であるか（退化した線分か）の判定: $10^{-18}$</li>
	 *   <li>退化した線分が円周上にあるかの距離比較判定: $\epsilon = 10^{-9}$</li>
	 *   <li>2つの交点が同一であるかどうかの距離比較判定（重複排除）: $\epsilon = 10^{-9}$</li>
	 * </ul>
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param seg 交点を求める対象 of 線分
	 * @return 円と線分の交点のリスト
	 */
	// 未テスト
	public List<DoublePoint> intersect(DoubleSegment seg) {
		double dx = seg.x2 - seg.x1;
		double dy = seg.y2 - seg.y1;
		double ox = seg.x1 - center.x();
		double oy = seg.y1 - center.y();

		double A = dx * dx + dy * dy;
		double B = 2.0 * (ox * dx + oy * dy);
		double C = ox * ox + oy * oy - radius * radius;

		if (A < 1e-18) {
			// 退化した線分（点）
			if (Math.abs(ox * ox + oy * oy - radius * radius) < 1e-9) {
				return List.of(new DoublePoint(seg.x1, seg.y1));
			} else {
				return List.of();
			}
		}

		double D = B * B - 4.0 * A * C;
		if (D < 0.0) {
			double cross = ox * dy - oy * dx;
			double dSq = (cross * cross) / A;
			double maxR = radius + 1e-9;
			if (dSq <= maxR * maxR) {
				D = 0.0;
			} else {
				return List.of();
			}
		}

		double sqrtD = Math.sqrt(D);
		double t1 = (-B - sqrtD) / (2.0 * A);
		double t2 = (-B + sqrtD) / (2.0 * A);

		List<DoublePoint> result = new ArrayList<>();
		double eps = 1e-9;
		boolean t1Valid = (t1 >= -eps && t1 <= 1.0 + eps);
		boolean t2Valid = (t2 >= -eps && t2 <= 1.0 + eps);

		if (t1Valid) {
			double clampedT1 = Math.max(0.0, Math.min(1.0, t1));
			result.add(new DoublePoint(seg.x1 + clampedT1 * dx, seg.y1 + clampedT1 * dy));
		}
		if (t2Valid) {
			double clampedT2 = Math.max(0.0, Math.min(1.0, t2));
			DoublePoint p2 = new DoublePoint(seg.x1 + clampedT2 * dx, seg.y1 + clampedT2 * dy);
			if (!result.isEmpty()) {
				DoublePoint p1 = result.get(0);
				double dist = Math.hypot(p1.x() - p2.x(), p1.y() - p2.y());
				if (dist > 1e-9) {
					result.add(p2);
				}
			} else {
				result.add(p2);
			}
		}
		return result;
	}

	/**
	 * 与えられた2点 a, b を直径の両端とする円を構築します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param a 直径の端点1
	 * @param b 直径の端点2
	 * @return 2点 a, b を直径とする円
	 */
	// 未テスト
	public static DoubleCircle fromDiameter(DoublePoint a, DoublePoint b) {
		double cx = (a.x() + b.x()) / 2.0;
		double cy = (a.y() + b.y()) / 2.0;
		DoublePoint center = new DoublePoint(cx, cy);
		double radius = Math.hypot(a.x() - b.x(), a.y() - b.y()) / 2.0;
		return new DoubleCircle(center, radius);
	}

	/**
	 * 与えられた3点 a, b, c の外接円を計算します。
	 * 3点が同一直線上（共線）にある場合、一意な外接円が存在しないため null を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param a 基準となる第1の点
	 * @param b 基準となる第2の点
	 * @param c 基準となる第3の点
	 * @return 3点 a, b, c の外接円
	 */
	// 未テスト
	public static DoubleCircle circumcircle(DoublePoint a, DoublePoint b, DoublePoint c) {
		double bx = b.x() - a.x();
		double by = b.y() - a.y();
		double cx = c.x() - a.x();
		double cy = c.y() - a.y();
		double cross = bx * cy - by * cx;
		if (cross == 0.0) {
			return null;
		}
		double b_sq = bx * bx + by * by;
		double c_sq = cx * cx + cy * cy;
		double ux = (b_sq * cy - c_sq * by) / (2.0 * cross);
		double uy = (bx * c_sq - cx * b_sq) / (2.0 * cross);
		DoublePoint center = new DoublePoint(a.x() + ux, a.y() + uy);
		double radius = Math.hypot(ux, uy);
		return new DoubleCircle(center, radius);
	}

	/**
	 * 指定された点 p がこの円の内部または境界上にある（含まれる）かどうかを判定します。
	 * 判定には中心点と点 p の間の距離 $d$ と半径 $r$ を比較し、
	 * 浮動小数点誤差を考慮するために $d^2 \le r + 10^{-9}$ を満たす場合に true を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param p 判定対象の点
	 * @return
	 */
	// 未テスト
	public boolean contains(DoublePoint p) {
		double dx = p.x() - center.x();
		double dy = p.y() - center.y();
		return dx*dx+dy*dy <= radius*radius + 1e-9;
	}

	/**
	 * 円の面積を計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return 円の面積 \pi * r^2
	 */
	// 未テスト
	public double area() {
		return Math.PI * radius * radius;
	}

	/**
	 * 円の周長を計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return 円の周長 2 * \pi * r
	 */
	// 未テスト
	public double circumference() {
		return 2.0 * Math.PI * radius;
	}

	/**
	 * この円と別の円 {@link DoubleCircle} の交差部分を計算します。
	 *
	 * <p>2つの円が完全に一致する場合（中心の距離が 10^-9 未満かつ半径の差が 10^-9 未満）、
	 * 共通部分としてこの円自体（{@code this}）を唯一の要素とするリストを返します。
	 * 浮動小数点誤差を考慮するため、しきい値として 10^-9 を使用します。</p>
	 *
	 * <p>計算量: O(1)</p>
	 *
	 * @param other 交差部分を求める対象の円
	 * @return 共通部分を表す {@link GeometricObject} のリスト（同一円なら円自体、そうでない場合は境界線の交点（0〜2点）のリスト）
	 */
	// 未テスト
	public List<GeometricObject> intersect(DoubleCircle other) {
		double dx = other.center.x() - center.x();
		double dy = other.center.y() - center.y();
		double d = Math.hypot(dx, dy);

		if (d < 1e-9 && Math.abs(radius - other.radius) < 1e-9) {
				return List.of(this);
		}

		if (d > radius + other.radius + 1e-9 || d < Math.abs(radius - other.radius) - 1e-9) {
			// 離れすぎているか、一方が他方に完全に含まれている（交差しない）
			return List.of();
		}
		//thisの中心から2交点の中点までの距離
		double a = (radius * radius - other.radius * other.radius + d * d) / (2.0 * d);
		double val = radius * radius - a * a;
		if (val < 0.0) {
			if (val >= -1e-9) {
				val = 0.0;
			} else {
				return List.of();
			}
		}

		double h = Math.sqrt(val);
		if (h < 1e-9) {
			// 接点（1点）
			double cx = center.x() + a * (dx / d);
			double cy = center.y() + a * (dy / d);
			return List.of(new DoublePoint(cx, cy));
		} else {
			// 交点（2点）
			double ex = dx / d;
			double ey = dy / d;

			DoublePoint p1 = new DoublePoint(center.x() + a * ex - h * ey, center.y() + a * ey + h * ex);
			DoublePoint p2 = new DoublePoint(center.x() + a * ex + h * ey, center.y() + a * ey - h * ex);
			return List.of(p1, p2);
		}
	}

	/**
	 * この円と別の円 {@link DoubleCircle} の共通部分（論理積領域）の面積を計算します。
	 *
	 * <p>計算量: O(1)</p>
	 *
	 * @param other 共通部分を求める対象の円
	 * @return 2つの円の共通部分の面積
	 */
	public double intersectionArea(DoubleCircle other) {
		//https://onlinejudge.u-aizu.ac.jp/solutions/problem/CGL_7_I/review/11623247/fortoobye321/JAVA
		double dx = other.center.x() - center.x();
		double dy = other.center.y() - center.y();
		double d = Math.hypot(dx, dy);

		double r = radius;
		double R = other.radius;

		if (d >= r + R) {
			return 0.0;
		}

		if (d <= Math.abs(r - R)) {
			double minR = Math.min(r, R);
			return Math.PI * minR * minR;
		}

		//ヘロンの公式
		double h = Math.sqrt(
				(-d + r + R) *
				(d + r - R) *
				(d - r + R) *
				(d + r + R)
		);

		double a1 = Math.atan2(h, (r * r + d * d - R * R));
		double a2 = Math.atan2(h, (R * R + d * d - r * r));

		return r * r * a1 + R * R * a2 - 0.5 * h;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DoubleCircle)) return false;
		DoubleCircle that = (DoubleCircle) o;
		return Double.compare(that.radius, radius) == 0 &&
			java.util.Objects.equals(center, that.center);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(center, radius);
	}

	@Override
	public String toString() {
		return "DoubleCircle[center=" + center + ", radius=" + radius + "]";
	}
}
