package library.util.geometry;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.LongFraction;
import library.util.MathUtils;

public class LongLine3D {
	long[] dir;
	long[] from;
	/**
	 * (x0,y0,z0),(x1,y1,z1)を結ぶ直線
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param x1
	 * @param y1
	 * @param z1
	 */
	public LongLine3D(long x0, long y0, long z0, long x1, long y1, long z1) {
		dir=new long[3];
		from=new long[3];
		dir[0]=x0-x1;
		dir[1]=y0-y1;
		dir[2]=z0-z1;
		if(dir[0]==0&&dir[1]==0&&dir[2]==0)throw new AssertionError();
		from[0]=x0;
		from[1]=y0;
		from[2]=z0;
		long g=MathUtils.gcd(dir[0],dir[1],dir[2]);
		dir[0]/=g;
		dir[1]/=g;
		dir[2]/=g;
		if(dir[0]<0||(dir[0]==0&&dir[1]<0)||(dir[0]==0&&dir[1]==0&&dir[2]<0)) {
			for (int i = 0; i < 3; i++) {
				dir[i]*=-1;
			}
		}
	}
	/**
     * この直線を一意に表すための識別用ベクトルを返す。
     *
     * <p>
     * 返り値は長さ 6 の {@code long[]} で、
     * 前半 3 要素が一意な方向ベクトル {@code dir}、
     * 後半 3 要素が一意な位置直線上の点 {@code q} からなる。
     * </p>
     *
     * <p>
     * {@code q} は次で定義される：
     * <pre>
     * q = |dir|^2 * from - (dir ・ from) * dir
     * </pre>
     * これは原点から直線への最近点ベクトルを {@code |dir|^2} 倍したものであり、
     * 同一直線上の異なる点 {@code from} を用いても同じ値になる。
     * </p>
     * <p><b>注意:</b>
     * 内部で {@code |dir|^2 * from} のような積を行うため、
     * 座標値が大きい場合（おおよそ |coordinate| ≳ 10^6）には
     * {@code long} のオーバーフローが発生する可能性がある。
     * </p>
     *
     * @return この直線を一意に表す長さ 6 の配列
     * https://atcoder.jp/contests/abc301/submissions/72141873
     */
	public long[] uniqueId() {
		long dot=0;
		for (int k = 0; k < 3; k++) {
			dot+=dir[k]*from[k];
		}
		long[]q=new long[3];
		for (int k = 0; k < 3; k++) {
			q[k]=(dir[0]*dir[0]+dir[1]*dir[1]+dir[2]*dir[2])*from[k]-dot*dir[k];
		}
		return ArrayUtils.concat(dir, q);
	}
	
	/**
	 * 点(x, y, z)が直線上にあるかを返す。
	 * 128ビット精度の整数演算を用いてオーバーフローを回避している。
	 *
	 * @param x 点の x 座標
	 * @param y 点の y 座標
	 * @param z 点の z 座標
	 * @return 直線上にあるなら true
	 * @complexity O(1)
	 * @see <a href="https://atcoder.jp/contests/abc301/submissions/72141873">Reference AtCoder Submission</a>
	 */
	public boolean onLine(long x, long y, long z) {
		long dx = x - from[0];
		long dy = y - from[1];
		long dz = z - from[2];

		return isCrossZero(dx, dir[1], dy, dir[0])
				&& isCrossZero(dy, dir[2], dz, dir[1])
				&& isCrossZero(dz, dir[0], dx, dir[2]);
	}

	private boolean isCrossZero(long a, long b, long c, long d) {
		long abLo = a * b;
		long cdLo = c * d;
		if (abLo != cdLo) return false;
		long abHi = Math.multiplyHigh(a, b);
		long cdHi = Math.multiplyHigh(c, d);
		return abHi == cdHi;
	}
	
	sealed interface Intersection3D permits Point, None, Line {}
	public record Point(LongFraction x, LongFraction y, LongFraction z) implements Intersection3D {}
	public record None() implements Intersection3D {}
	public record Line(LongLine3D line) implements Intersection3D {} // 同一直線
	
	/**
	 * public LongLine3D(long x0, long y0, long z0, long x1, long y1, long z1)のx0,y0,z0,x1,y1,z1の絶対値が5e4ぐらいまでしか動かない。
	 * https://atcoder.jp/contests/abc301/submissions/72141873
	 * @param line
	 * @return
	 */
	public Intersection3D intersection(LongLine3D line) {
		// A, B, C, D をベクトル, s, t を実数として
		// A + sB = C + tD
		// を満たす s を求めたい。
		// D と外積を取ると
		// (A × D) + s(B × D) = (C × D)
		// s(B × D) = (C ×　D) - (A × D)
		// (B × D) と内積を取ると
		// s = ((C ×　D) - (A × D)) ・ (B × D) / |B × D|²
		// 座標の絶対値の最大値をMとすると(C ×　D) - (A × D) の各成分の絶対値の最大値は4M²。
		// (B × D) の各成分の絶対値の最大値は8M²。
		// 従って、((C ×　D) - (A × D)) ・ (B × D)は32M⁴まで行くはず。
		// 
		
		long[] bd=cross(dir, line.dir);
		long[] p=new long[] {from[0]-line.from[0], from[1]-line.from[1], from[2]-line.from[2]};
		if(bd[0]==0&&bd[1]==0&&bd[2]==0) {//平行
			long[] q=cross(p, dir);
			if(q[0]==0&&q[1]==0&&q[2]==0) {
				return new Line(this);
			} else {
				return new None();
			}
		}
		if(dot(p, bd)!=0) return new None();//ねじれの関係
		long[] cd=cross(line.from, line.dir);
		long[] ad=cross(from, line.dir);
		
		
		long a=(cd[0]-ad[0])*bd[0]+(cd[1]-ad[1])*bd[1]+(cd[2]-ad[2])*bd[2];
		long b=dot(bd, bd);
		
		
		// s=a/b
		LongFraction[]ret=new LongFraction[3];
		for (int i = 0; i < 3; i++) {
			ret[i]=new LongFraction(from[i]*b+dir[i]*a,b);
		}
		return new Point(ret[0], ret[1], ret[2]);
	}

	
	/**
	 * https://atcoder.jp/contests/abc301/submissions/72141873
	 * @return
	 */
	public Intersection3D yzPlaneIntersection() {
		if(dir[0]==0) {
			if(from[0]==0) {
				return new Line(this);
			} else {
				return new None();
			}
		}
		LongFraction t=new LongFraction(-from[0], dir[0]);
	    LongFraction y = new LongFraction(from[1], 1).add(new LongFraction(dir[1], 1).mul(t));
	    LongFraction z = new LongFraction(from[2], 1).add(new LongFraction(dir[2], 1).mul(t));
	    return new Point(new LongFraction(0, 1), y, z);
	}
	
	long[] cross(long[] a, long[] b) {
		long[]c=new long[3];
		c[0]=a[1]*b[2]-a[2]*b[1];
		c[1]=a[2]*b[0]-a[0]*b[2];
		c[2]=a[0]*b[1]-a[1]*b[0];
		return c;
	}
	
	long dot(long[] a, long[] b) {
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	
	@Override
	public String toString() {
	    // 例: Line(from=[1, 2, 3], dir=[4, 5, 6])
	    return String.format("Line(from=[%d, %d, %d], dir=[%d, %d, %d])",
	            from[0], from[1], from[2],
	            dir[0], dir[1], dir[2]);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(uniqueId());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if (obj instanceof LongLine3D line) {
			return Arrays.equals(uniqueId(), line.uniqueId());
		} else {
			return false;
		}
	}
}
