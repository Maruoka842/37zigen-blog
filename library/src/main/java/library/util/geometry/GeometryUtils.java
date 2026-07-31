package library.util.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import library.util.ArrayUtils;
import library.util.FractionUtils;
import library.util.MathUtils;
import library.util.collections.ObjectDeque;
import library.util.segtree.Add_CountMin;

public class GeometryUtils {

	/**
	 * 与えられた円 {@link DoubleCircle} と線分 {@link DoubleSegment} の交点を計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param circle 円
	 * @param seg 線分
	 * @return 交点のリスト
	 */
	// 未テスト
	public static List<DoublePoint> intersect(DoubleCircle circle, DoubleSegment seg) {
		return circle.intersect(seg);
	}
    
    /**
     * sqrt((x1-x2)^2+(y1-y2)^2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
	public static double dist(double x1, double y1, double x2, double y2) {
		return Math.hypot(x1-x2, y1-y2);
	}
	
	public static double dist(int x1, int y1, int x2, int y2) {
		return Math.hypot(x1-x2, y1-y2);
	}
	
	public static long distSquared(long x, long y, long x2, long y2) {
		long dx=x-x2;
		long dy=y-y2;
		return dx*dx+dy*dy;
	}
	
	public static boolean isOrthogonal(LongVector u, LongVector v) {
		return u.x() * v.x() + u.y() * v.y() == 0;
	}
	
	/**
	 * atan2(x,y)で比較する。つまり、角-π < Θ <= π に移す。atan2(0,0)=0とする。
     * 偏角が同じときは区別しないのでsortqに使える。
     * @return
     * verified:https://judge.yosupo.jp/submission/323743
     */
	public static Comparator<LongPoint> PolarAngleComparatorForPoint() {
        return (a, b) -> {
            boolean upA = (a.y > 0) || ((a.y == 0) && (a.x < 0));
            boolean upB = (b.y > 0) || ((b.y == 0) && (b.x < 0));
            if (upA != upB) {
                if (upA) {
                    return 1;
                } else {
                    return -1;
                }
            }
            long cross = (a.x * b.y) - (a.y * b.x);
            if (cross != 0) return -Long.signum(cross);
            //片方が (0, 0), もう片方が ≠(0, 0) で向きが (1, 0)でないとき、大きさで比較するとバグるので注意
			if (a.x == b.x && a.y == b.y) return 0;// a=b=(0, 0)用
            if (a.x == 0 && a.y == 0 && b.y !=0) {
            	return 1;
            }
            if (b.x == 0 && b.y == 0 && a.y !=0) {
            	return -1;
            }
//            return Long.compare(a.x*a.x+a.y*a.y, b.x*b.x+b.y*b.y);
            return 0;
        };
    }
	

	
	public static Comparator<LongPoint> PolarAngleComparator(LongPoint origin) {
        return (a, b) -> {
        	long ax=a.x-origin.x;
        	long ay=a.y-origin.y;
        	long bx=b.x-origin.x;
        	long by=b.y-origin.y;
        	boolean upA = (ay > 0) || ((ay == 0) && (ax < 0));
            boolean upB = (by > 0) || ((by == 0) && (bx < 0));
            if (upA != upB) {
                if (upA) {
                    return 1;
                } else {
                    return -1;
                }
            }
            long cross = (ax * by) - (ay * bx);
            if (cross != 0) return -Long.signum(cross);
            //片方が (0, 0), もう片方が ≠(0, 0) で向きが (1, 0)でないとき、大きさで比較するとバグるので注意
			if (a.x == b.x && a.y == b.y) return 0;// a=b=(0, 0)用
            if (a.x == 0 && a.y == 0 && b.y !=0) {
            	return 1;
            }
            if (b.x == 0 && b.y == 0 && a.y !=0) {
            	return -1;
            }
//            return Long.compare(a.x*a.x+a.y*a.y, b.x*b.x+b.y*b.y);
            return 0;
        };
	}	
	

	
	
	
	public static Comparator<DoublePoint> PolarAngleComparator(DoublePoint origin) {
        return (a, b) -> {
        	double ax=a.x-origin.x;
        	double ay=a.y-origin.y;
        	double bx=b.x-origin.x;
        	double by=b.y-origin.y;
        	boolean upA = (ay > 0) || ((ay == 0) && (ax < 0));
            boolean upB = (by > 0) || ((by == 0) && (bx < 0));
            if (upA != upB) {
                if (upA) {
                    return 1;
                } else {
                    return -1;
                }
            }
            double cross = (ax * by) - (ay * bx);
            if (cross != 0) return -Double.compare(cross, 0);
            //片方が (0, 0), もう片方が ≠(0, 0) で向きが (1, 0)でないとき、大きさで比較するとバグるので注意
			if (a.x == b.x && a.y == b.y) return 0;// a=b=(0, 0)用
            if (a.x == 0 && a.y == 0 && b.y !=0) {
            	return 1;
            }
            if (b.x == 0 && b.y == 0 && a.y !=0) {
            	return -1;
            }
//            return Long.compare(a.x*a.x+a.y*a.y, b.x*b.x+b.y*b.y);
            return 0;
        };
	}	

	
	
	public static Comparator<LongVector> PolarAngleComparatorForVector() {
        return (a, b) -> {
        	long ax=a.x;
        	long ay=a.y;
        	long bx=b.x;
        	long by=b.y;
        	boolean upA = (ay > 0) || ((ay == 0) && (ax < 0));
            boolean upB = (by > 0) || ((by == 0) && (bx < 0));
            if (upA != upB) {
                if (upA) {
                    return 1;
                } else {
                    return -1;
                }
            }
            long cross = (ax * by) - (ay * bx);
            if (cross != 0) return -Long.signum(cross);
            //片方が (0, 0), もう片方が ≠(0, 0) で向きが (1, 0)でないとき、大きさで比較するとバグるので注意
			if (a.x == b.x && a.y == b.y) return 0;// a=b=(0, 0)用
            if (a.x == 0 && a.y == 0 && b.y !=0) {
            	return 1;
            }
            if (b.x == 0 && b.y == 0 && a.y !=0) {
            	return -1;
            }
//            return Long.compare(a.x*a.x+a.y*a.y, b.x*b.x+b.y*b.y); 
            return 0;
        };
	}	

	
	
	
	/**
	 * 入力は偏角ソートされているとする。
	 * 尺取り法により、
	 * f[0][i]=[i,..j)が90度未満
	 * f[1][i]=[i,..j)が90度以下
	 * f[2][i]=[i,..j)が180度未満
	 * という配列を返す。
	 * @param list
	 * @return
	 * verified:https://atcoder.jp/contests/abc033/submissions/70504283
	 */
	public int[][] angleRangeDivide(ArrayList<LongVector> list) {
		int k0=0;//
		int k1=0;
		int k2=0;
		int[][]f=new int[3][list.size()];
		for (int j = 0; j < list.size(); j++) {
			k0=Math.max(k0, j);
			k1=Math.max(k1, j);
			k2=Math.max(k2, j);
			while(k0<j+list.size() &&list.get(j).isPolarAngleLes90(list.get(k0%list.size())))++k0;
			while(k1<j+list.size() &&list.get(j).isPolarAngleLeq90(list.get(k1%list.size())))++k1;
			while(k2<j+list.size() && list.get(j).isPolarAngleLes180(list.get(k2%list.size())))++k2;
			f[0][j]=k0;
			f[1][j]=k1;
			f[2][j]=k2;
		}
		return f;
	}
	
	/**
	 * 多角形内部の点かを返す。周上はfalse。
	 * @param p
	 * @param polygon
	 * @return
	 */
	public static boolean isInside(LongPoint p,  LongPoint[] polygon) {
		long[] cross=new long[polygon.length];
		for (int i = 0; i < polygon.length; i++) {
			cross[i]=polygon[i].sub(p).cross(polygon[(i+1)%polygon.length].sub(p));
			if (cross[i]==0) return false;
			if (Long.signum(cross[0])!=Long.signum(cross[i])) return false;
		}
		return true;
	}
	
	/**
	 * 凸多角形の頂点で最も下にあるものの中で最も左にある頂点から順に、反時計周りに、
	 * convexHullをなす頂点の番号を並べて配列で返す.
	 * convexHull上の全ての頂点を含む。つまり一辺上に複数の頂点が乗っている可能性がある。
	 * @param points
	 * @return
	 */
	public static LongPoint[] convexHull(LongPoint[] inputPoints) {
		//https://atcoder.jp/contests/abc286/submissions/74528640
		LongPoint[] points = inputPoints.clone();
	    for (int i = 0; i < points.length; i++) {
			if (points[0].y > points[i].y || (points[0].y == points[i].y && points[0].x > points[i].x)) {
				ArrayUtils.swap(0, i, points);
			}
		}
		Arrays.sort(points, 1, points.length, PolarAngleComparator(points[0]).thenComparing(p->p.sub(points[0]).squaredLength()));
		{
			int last=points.length-1;
			while(last>=0 && points[last].sub(points[0]).cross(points[points.length-1].sub(points[0]))==0)--last;
			Arrays.sort(points, last+1, points.length, (p,q)->Long.compare(p.sub(points[0]).squaredLength(), q.sub(points[0]).squaredLength()));
		}
		ObjectDeque<LongPoint> dq=new ObjectDeque<>();
		for (int i = 0; i < points.length; i++) {
			if(!dq.isEmpty()&&dq.peekLast().equals(points[i]))continue;
			while(dq.size() >= 2 && dq.get(dq.size()-1).sub(dq.get(dq.size()-2))
					.cross(points[i].sub(dq.peekLast())) < 0) {
				dq.pollLast();
			}
			dq.addLast(points[i]);
		}
		LongPoint[]ret=new LongPoint[dq.size()];
		for (int i = 0; i < dq.size(); i++) {
			ret[i]=dq.get(i);
		}
		return ret;
	}
	
	
	
	
	
	/**
	 * 凸多角形の頂点で最も下にあるものの中で最も左にある頂点から順に、反時計周りに、
	 * convexHullをなす頂点の番号を並べて配列で返す.
	 * convexHull上の全ての頂点を含む。つまり一辺上に複数の頂点が乗っている可能性がある。
	 * @param points
	 * @return
	 */
	public static DoublePoint[] convexHullbyGraham(DoublePoint[] points) {
		if(points.length<=2)return points;
		for (int i = 0; i < points.length; i++) {
			if (points[0].y > points[i].y || (points[0].y == points[i].y && points[0].x > points[i].x)) {
				ArrayUtils.swap(0, i, points);
			}
		}
		Arrays.stream(points).filter(p->DoubleVector.EX.isPolarAngleLes180(p.sub(points[0]))).toArray(m->new DoublePoint[m]);
		Arrays.sort(points, 1, points.length, PolarAngleComparator(points[0]).thenComparing(p->p.sub(points[0]).squaredLength()));
		{
			int last=points.length-1;
			while(last>=0 && points[last].sub(points[0]).cross(points[points.length-1].sub(points[0]))==0)--last;
			Arrays.sort(points, last+1, points.length, (p,q)->Double.compare(p.sub(points[0]).squaredLength(), q.sub(points[0]).squaredLength()));
		}
		ObjectDeque<DoublePoint> dq=new ObjectDeque<>();
		dq.addLast(points[0]);
		dq.addLast(points[1]);
		for (int i = 2; i < points.length; i++) {
			while(dq.size() >= 2 && dq.get(dq.size()-1).sub(dq.get(dq.size()-2))
					.cross(points[i].sub(dq.peekLast())) < 0) {
				dq.pollLast();
			}
			dq.addLast(points[i]);
		}
		DoublePoint[]ret=new DoublePoint[dq.size()];
		for (int i = 0; i < dq.size(); i++) {
			ret[i]=dq.get(i);
		}
		return ret;
	}

	
	
	
	/**
	 * 凸多角形の頂点で最も下にあるものの中で最も左にある頂点から順に、反時計周りに、
	 * convexHullをなす頂点の番号を並べて配列で返す.
	 * 一辺上には他の頂点は載っていない。
	 * https://atcoder.jp/contests/abc275/submissions/72110893
	 * @param points
	 * @return
	 */
	public static DoublePoint[] convexHull(DoublePoint[] points) {
		int n=points.length;
		int[] order=ArrayUtils.argSort(points, (p, q)->{
			if(p.x!=q.x) {
				return Double.compare(p.x, q.x);
			} else {
				return Double.compare(p.y, q.y);
			}
		});
		
		
		DoublePoint[] hull = new DoublePoint[n + 1];
        int k = 0;

        // 下側凸包の構築
        for (int i:order) {
            while (k >= 2 && hull[k-1].sub(hull[k-2]).cross(points[i].sub(hull[k-2]))<= 0) {
                k--;
            }
            hull[k++] = points[i];
        }

        // 上側凸包の構築
        ArrayUtils.reverse(order);
        int t=k+1;
        for (int i:Arrays.copyOfRange(order, 1, order.length)){
            while (k >= t && hull[k-1].sub(hull[k-2]).cross(points[i].sub(hull[k-2]))<= 0) {
                k--;
            }
            hull[k++] = points[i];
        }

        // 始点が重複して入るので削除
        if (k > 1) {
            k--;
        }

        return Arrays.copyOf(hull, k);
	}

	
	
	
	public static long chebyshevDistance(long x0, long y0, long x1, long y1) {
		long dx=Math.abs(x0-x1);
		long dy=Math.abs(y0-y1);
		return Math.max(dx, dy);
	}
	
	
	/**
	 * A[i]x+B[i]y ≤ C[i]の共通部分のmax yをなす直線のインデックスを左から順に返す。
	 * A[i] > 0 >  B[i] を仮定している。
	 * https://atcoder.jp/contests/abc289/submissions/72144433
	 */
	public static int[] upperEnvelope(long[] A, long[] B, long[] C) {
		// y ≥ -A[i]x/B[i] + C[i]/B[i]
		// -A[i]/B[i]の昇順に並び変える
		int N=A.length;
		Integer[] order=new Integer[N];
		Arrays.setAll(order, i->i);
		Arrays.sort(order, (x, y)->{
			if (A[x]*B[y]==A[y]*B[x]) {
				//傾きが同じときy切片c/bの昇順
				return FractionUtils.compareFractionByEuclid(C[x], B[x], C[y], B[y]);
			} else {
				return -Long.compare(A[x]*B[y], A[y]*B[x]);
			}
		});
		var a=ArrayUtils.take(A, order);
		var b=ArrayUtils.take(B, order);
		var c=ArrayUtils.take(C, order);
		ArrayList<Integer>list=new ArrayList<>();
		list.add(0);
		for (int i = 1; i < N; i++) {
			while(list.size()>=1) {
				int t=list.getLast();
				if(a[i]*b[t]==a[t]*b[i]) {
					list.removeLast();
				} else {
					break;
				}
			}
			while(list.size()>=2) {
				int t=list.getLast();
				int s=list.get(list.size()-2);
				if (FractionUtils.compareFractionByEuclid(b[t]*c[s]-b[s]*c[t], a[s]*b[t]-a[t]*b[s], b[i]*c[s]-b[s]*c[i], a[s]*b[i]-a[i]*b[s]) >= 0) {
					list.removeLast();
				} else {
					break;
				}
			}
			list.add(i);
		}
		int[] ret=list.stream().mapToInt(Integer::intValue).toArray();
		for (int i = 0; i < ret.length; i++) {
			ret[i]=order[ret[i]];
		}
		return ret;
	}

	
	
	
	/**
	 * A[i]x+B[i]y ≤ C[i]の共通部分のmin yをなす直線のインデックスを左から順に返す。
	 * A[i], B[i] > 0 を仮定している。
	 * 
	 * https://atcoder.jp/contests/abc372/submissions/71816159
	 */
	public static int[] lowerEnvelope(long[] A, long[] B, long[] C) {
		//A[i]/B[i]の昇順に並び変える
		int N=A.length;
		Integer[] order=new Integer[N];
		Arrays.setAll(order, i->i);
		Arrays.sort(order, (x, y)->{
			if (A[x]*B[y]==A[y]*B[x]) {
				//傾きが同じときy切片c/bの降順
				return -FractionUtils.compareFractionByEuclid(C[x], B[x], C[y], B[y]);
			} else {
				return Long.compare(A[x]*B[y], A[y]*B[x]);
			}
		});
		var a=ArrayUtils.take(A, order);
		var b=ArrayUtils.take(B, order);
		var c=ArrayUtils.take(C, order);
		ArrayList<Integer>list=new ArrayList<>();
		list.add(0);
		for (int i = 1; i < N; i++) {
			while(list.size()>=1) {
				int t=list.getLast();
				if(a[i]*b[t]==a[t]*b[i]) {
					list.removeLast();
				} else {
					break;
				}
			}
			while(list.size()>=2) {
				int t=list.getLast();
				int s=list.get(list.size()-2);
				if (FractionUtils.compareFractionByEuclid(b[t]*c[s]-b[s]*c[t], a[s]*b[t]-a[t]*b[s], b[i]*c[s]-b[s]*c[i], a[s]*b[i]-a[i]*b[s]) >= 0) {
					list.removeLast();
				} else {
					break;
				}
			}
			list.add(i);
		}
		int[] ret=list.stream().mapToInt(Integer::intValue).toArray();
		for (int i = 0; i < ret.length; i++) {
			ret[i]=order[ret[i]];
		}
		return ret;
	}
	
	/**
	 * pを中心に回転したときrはqから見てどっち回転の方向にあるかを返す。
	 * 1ならば反時計回り。-1ならば時計回り。0ならば直線p-q上
	 * https://atcoder.jp/contests/abc251/submissions/72065074
	 * @param alives
	 * @param p
	 * @return
	 */
	public static int orientation(LongPoint p, LongPoint q, LongPoint r) {
		long x0=q.x-p.x;
		long y0=q.y-p.y;
		long x1=r.x-p.x;
		long y1=r.y-p.y;
		long cross=x0*y1-y0*x1;
		return Long.compare(cross, 0);
	}
	
	/**
	 * n頂点の凸包を返す。
	 * @param n
	 * @return
	 */
	public static LongPoint[] randomConvexHull(int n) {
		Random rnd=new Random();
		LongPoint[]p=new LongPoint[n];
		out:while(true) {
			for (int i = 0; i < n; i++) {
				p[i]=new LongPoint(rnd.nextLong(-10, 10), rnd.nextLong(-10, 10));
			}
			for (int i = 0; i < n; i++) {
				for (int j = i+1; j < n; j++) {
					for (int k = j+1; k < n; k++) {
						if(new LongLine(p[i], p[j]).onLine(p[k])) continue out;
					}
				}
			}
			break;
		}
		return GeometryUtils.convexHull(p);
	}
	
	
	
	/**
	 * [L[i], R[i]) * [D[i], U[i]) からなる長方形のUnionの面積
	 * @param L
	 * @param R
	 * @param D
	 * @param U
	 * @return
	 */
	public static long areaOfUnionOfRectangle(long[]L, long[] R, long[] D, long[] U) {
		//https://atcoder.jp/contests/abc346/submissions/72433386
		//https://judge.yosupo.jp/submission/359731
		//https://atcoder.jp/contests/abc449/submissions/74133161
		int N=L.length;
		long[][]registerdX=new long[2*N][];
		long[][]query=new long[2*N][];
		int querySize = 0;
		for (int i = 0; i < N; i++) {
			if (L[i] >= R[i] )
				continue;
			if (D[i] >= U[i])
				continue;
			querySize++;
			registerdX[2*i]=new long[] {L[i],   2*i};
			registerdX[2*i+1]=new long[] {R[i], 2*i+1};
			query[2*i]=new long[] {L[i], R[i], D[i],   +1, i};
			query[2*i+1]=new long[] {L[i], R[i], U[i], -1, i};
		}
		if (querySize == 0) return 0;
		registerdX = Arrays.copyOf(registerdX, 2 * querySize);
		query = Arrays.copyOf(query, 2 * querySize);
		Arrays.sort(registerdX, (a, b) -> Long.compare(a[0], b[0]));
		int[] queryIdToLeftXIndex=new int[N];
		int[] queryIdToRightXIndex=new int[N];
		for (int i = 0; i < registerdX.length; i++) {
			if(registerdX[i][1]%2==0) {
				queryIdToLeftXIndex[(int)registerdX[i][1]/2]=i;
			} else {
				queryIdToRightXIndex[(int)registerdX[i][1]/2]=i;
			}
		}
		Arrays.sort(query, (a, b) -> Long.compare(a[2], b[2]));
		long ans=0;
//		var seg=SegTreeFactory.add_countMin(registerdX.length-1);
		var seg=new Add_CountMin(registerdX.length-1);
		long[] initialState=new long[registerdX.length-1];
		for (int i = 0; i < registerdX.length-1; i++) {
			initialState[i]=registerdX[i+1][0]-registerdX[i][0];
		}
		seg.fill0WithGivenWidth(initialState);
		long totalXLen = registerdX[registerdX.length-1][0]-registerdX[0][0];
		for (int i = 0; i < query.length; i++) {
			int j=i;
			while(j+1<query.length && query[j+1][2]==query[i][2])++j;
			if(j==query.length-1)break;
			for (int k = i; k <= j; k++) {
				int queryId=(int)query[k][4];
				int l=queryIdToLeftXIndex[queryId];
				int r=queryIdToRightXIndex[queryId];
				if (query[k][3]==1) {
					seg.act(l, r, 1L);
				} else {
					seg.act(l, r, -1L);
				}
			}
//			var ret=seg.foldAll();
//			if(ret[0]!=0) {//全面を覆っている
			if (seg.min() != 0) {
				long dx=totalXLen;
				long dy=query[j+1][2]-query[i][2];
				ans+=dx*dy;
			} else {
//				long dx=totalXLen-ret[1];
				long dx=totalXLen-seg.minCount();
				long dy=query[j+1][2]-query[i][2];
				ans+=dx*dy;
			}
			i=j;
		}
		return ans;
	}

	
	
    /**
     * アポロニウスの問題：3円に接する円の中心と半径を求めます。
     *
     * @param c1 円1の中心
     * @param r1 円1の半径
     * @param c2 円2の中心
     * @param r2 円2の半径
     * @param c3 円3の中心
     * @param r3 円3の半径
     * @param sgn1 円1に対する接し方（-1: 外接, 1: 内接）
     * @param sgn2 円2に対する接し方（-1: 外接, 1: 内接）
     * @param sgn3 円3に対する接し方（-1: 外接, 1: 内接）
     * @return 条件を満たす円のリスト
     */
    public static List<DoubleCircle> problemOfApollonius(DoublePoint c1, double r1, DoublePoint c2, double r2,
                                                         DoublePoint c3, double r3, int sgn1, int sgn2, int sgn3) {
        DoubleVector v2 = c2.sub(c1);
        DoubleVector v3 = c3.sub(c1);
        double a2 = -v2.x * 2, b2 = -v2.y * 2, c_2 = (-r1 * sgn1 + r2 * sgn2) * 2,
               d2 = -r1 * r1 - v2.squaredLength() + r2 * r2;
        double a3 = -v3.x * 2, b3 = -v3.y * 2, c_3 = (-r1 * sgn1 + r3 * sgn3) * 2,
               d3 = -r1 * r1 - v3.squaredLength() + r3 * r3;
        double denom = a2 * b3 - b2 * a3;
        List<DoubleCircle> retCircles = new ArrayList<>();
        if (denom == 0) return retCircles;
        DoubleVector v0 = new DoubleVector((b3 * d2 - b2 * d3) / denom, (-a3 * d2 + a2 * d3) / denom);
        DoubleVector v1 = new DoubleVector((-b3 * c_2 + b2 * c_3) / denom, (a3 * c_2 - a2 * c_3) / denom);
        double A = v1.squaredLength() - 1, B = 2 * (v1.dot(v0) + r1 * sgn1),
               C = v0.squaredLength() - r1 * r1;
        double[] quadRet = MathUtils.quadraticSolve(A, B, C);
        for (double r : quadRet) {
            if (r >= 0.0) {
                DoublePoint center = c1.add(v0).add(v1.mul(r));
                retCircles.add(new DoubleCircle(center, r));
            }
        }
        return retCircles;
    }

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * 3点 a, b, c が作る円（a, b, c は同一直線上にない）に対し、点 d がその境界の内側、境界上、または外側のいずれに位置するかを判定する。
	 * 内部の判定は、点 d を原点に平行移動した座標系における $3 \times 3$ 行列式の符号と、
	 * 3点 a, b, c の向き（反時計回りまたは時計回り）を組み合わせて行う。
	 * すべての計算はオーバーフローを防ぐために 64ビット符号付き整数型 (long) のみを用いて高速に行われる。
	 *
	 * <p><b>演算オーバーフローに関する安全性の保証範囲:</b><br>
	 * 内部では 4次形式 $M^4$ の項が含まれるため、判定対象の点 d と各基準点 $p \in \{a, b, c\}$ との最大座標差
	 * $M = \max(\{ |x_p - x_d|, |y_p - y_d| \}_{p \in \{a, b, c\}})$
	 * について、計算中の絶対最大値は $12 M^4$ に抑えられます。<br>
	 * したがって、最大座標差が $M \le 29000$ （例：各点の座標値がすべて $[-14500, 14500]$ の範囲内）であれば、
	 * 中間計算値 $12 M^4 \approx 8.48 \times 10^{18} < 2^{63} - 1$ となり、
	 * 64ビット符号付き整数型 (long) の演算オーバーフローが発生しないことが数学的に完全に保証されています。</p>
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param a 円を構成する第1の点
	 * @param b 円を構成する第2の点
	 * @param c 円を構成する第3の点
	 * @param d 判定対象 of 点
	 * @return a, b, c の外接円に対する d の位置
	 * @throws IllegalArgumentException a, b, c が同一直線上（共線）にある場合
	 */
	// 未テスト
	public static BoundedSide sideOfBoundedCircle(IntPoint a, IntPoint b, IntPoint c, IntPoint d) {
		long acx = (long) a.x - c.x;
		long acy = (long) a.y - c.y;
		long bcx = (long) b.x - c.x;
		long bcy = (long) b.y - c.y;
		long cross = acx * bcy - acy * bcx;
		int orientSign = Long.compare(cross, 0);
		if (orientSign == 0) {
			throw new IllegalArgumentException("Points a, b, and c must not be collinear.");
		}

		long adx = (long) a.x - d.x;
		long ady = (long) a.y - d.y;
		long bdx = (long) b.x - d.x;
		long bdy = (long) b.y - d.y;
		long cdx = (long) c.x - d.x;
		long cdy = (long) c.y - d.y;

		long sa = adx * adx + ady * ady;
		long sb = bdx * bdx + bdy * bdy;
		long sc = cdx * cdx + cdy * cdy;

		long t1 = sa * (bdx * cdy - bdy * cdx);
		long t2 = sb * (adx * cdy - ady * cdx);
		long t3 = sc * (adx * bdy - ady * bdx);

		long det = t1 - t2 + t3;
		int detSign = Long.compare(det, 0);

		if (detSign == 0) {
			return BoundedSide.ON_BOUNDARY;
		}

		if (orientSign > 0) {
			return detSign > 0 ? BoundedSide.ON_BOUNDED_SIDE : BoundedSide.ON_UNBOUNDED_SIDE;
		} else {
			return detSign < 0 ? BoundedSide.ON_BOUNDED_SIDE : BoundedSide.ON_UNBOUNDED_SIDE;
		}
	}

	/**
	 * 2点 p, q を直径の両端とする円に対し、点 t がその境界の内側、境界上、または外側のいずれに位置するかを判定する。
	 * すべての計算はオーバーフローを防ぐために 64ビット符号付き整数型 (long) のみを用いて高速に行われる。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param p 直径の端点1
	 * @param q 直径の端点2
	 * @param t 判定対象の点
	 * @return p, q を直径とする円に対する t の位置
	 */
	// 未テスト
	public static BoundedSide sideOfBoundedCircle(IntPoint p, IntPoint q, IntPoint t) {
		long tpx = (long) p.x - t.x;
		long tpy = (long) p.y - t.y;
		long tqx = (long) q.x - t.x;
		long tqy = (long) q.y - t.y;
		long dot = tpx * tqx + tpy * tqy;
		if (dot < 0) {
			return BoundedSide.ON_BOUNDED_SIDE;
		} else if (dot > 0) {
			return BoundedSide.ON_UNBOUNDED_SIDE;
		} else {
			return BoundedSide.ON_BOUNDARY;
		}
	}

	/**
	 * 3直線 l1, l2, l3 によって形成される三角形の内接円（incircle）を返します。
	 * 直線が平行、一点で交わる（共点）、または退化しているなどの理由で三角形を形成しない場合は null を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param l1 直線 l1
	 * @param l2 直線 l2
	 * @param l3 直線 l3
	 * @return 三角形の内接円を表す {@link DoubleCircle}、または三角形が形成されない場合は null
	 */
	// 未テスト
	public static DoubleCircle incircle(DoubleLine l1, DoubleLine l2, DoubleLine l3) {
		double norm1 = Math.hypot(l1.a, l1.b);
		double norm2 = Math.hypot(l2.a, l2.b);
		double norm3 = Math.hypot(l3.a, l3.b);
		if (norm1 < 1e-18 || norm2 < 1e-18 || norm3 < 1e-18) {
			return null;
		}
		double a1 = l1.a / norm1, b1 = l1.b / norm1, c1 = l1.c / norm1;
		double a2 = l2.a / norm2, b2 = l2.b / norm2, c2 = l2.c / norm2;
		double a3 = l3.a / norm3, b3 = l3.b / norm3, c3 = l3.c / norm3;

		double det12 = a1 * b2 - a2 * b1;
		double det23 = a2 * b3 - a3 * b2;
		double det31 = a3 * b1 - a1 * b3;

		if (Math.abs(det12) < 1e-9 || Math.abs(det23) < 1e-9 || Math.abs(det31) < 1e-9) {
			return null; // 平行な直線があるため、三角形ができない
		}

		// 3交点（頂点）
		double v3x = (b1 * c2 - b2 * c1) / det12;
		double v3y = (a2 * c1 - a1 * c2) / det12;
		DoublePoint v3 = new DoublePoint(v3x, v3y); // l1 と l2 の交点

		double v1x = (b2 * c3 - b3 * c2) / det23;
		double v1y = (a3 * c2 - a2 * c3) / det23;
		DoublePoint v1 = new DoublePoint(v1x, v1y); // l2 と l3 の交点

		double v2x = (b3 * c1 - b1 * c3) / det31;
		double v2y = (a1 * c3 - a3 * c1) / det31;
		DoublePoint v2 = new DoublePoint(v2x, v2y); // l3 と l1 の交点

		double side1 = Math.hypot(v2.x - v3.x, v2.y - v3.y); // l1上の辺の長さ(v2-v3)
		double side2 = Math.hypot(v3.x - v1.x, v3.y - v1.y); // l2上の辺の長さ(v3-v1)
		double side3 = Math.hypot(v1.x - v2.x, v1.y - v2.y); // l3上の辺の長さ(v1-v2)

		if (side1 < 1e-9 || side2 < 1e-9 || side3 < 1e-9) {
			return null; // 頂点が非常に近い、または一点に収束している
		}

		double perimeter = side1 + side2 + side3;
		// 内心
		double cx = (side1 * v1.x + side2 * v2.x + side3 * v3.x) / perimeter;
		double cy = (side1 * v1.y + side2 * v2.y + side3 * v3.y) / perimeter;
		DoublePoint center = new DoublePoint(cx, cy);

		// 内接円の半径
		double r = Math.abs(a1 * cx + b1 * cy + c1);

		return new DoubleCircle(center, r);
	}

	/**
	 * 与えられた点 p を通り、2直線 l1, l2 に接する円のリストを返します。
	 * 直線が退化している場合は null を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param p 円が通る点 p
	 * @param l1 接する直線 l1
	 * @param l2 接する直線 l2
	 * @return 条件を満たす円 {@link DoubleCircle} のリスト、または直線が退化している場合は null
	 */
	// 未テスト
	public static List<DoubleCircle> circleThroughPointTangentTo2Lines(DoublePoint p, DoubleLine l1, DoubleLine l2) {
		double norm1 = Math.hypot(l1.a, l1.b);
		double norm2 = Math.hypot(l2.a, l2.b);
		if (norm1 < 1e-18 || norm2 < 1e-18) {
			return null;
		}
		double a1 = l1.a / norm1, b1 = l1.b / norm1, c1 = l1.c / norm1;
		double a2 = l2.a / norm2, b2 = l2.b / norm2, c2 = l2.c / norm2;

		double det = a1 * b2 - a2 * b1;
		List<DoubleCircle> result = new ArrayList<>();

		if (Math.abs(det) < 1e-9) {
			// 平行な2直線
			// 距離
			double distBetweenLines = Math.abs(c1 - (a1 * a2 + b1 * b2) * c2);
			double r = distBetweenLines / 2.0;
			if (r < 1e-9) {
				// 同一の直線の場合、pを通る接円はpを接点とする、半径任意の無限個存在するため一意には決まらないが、
				// 通常、幅が0の平行線なら半径0の円が点pを中心に存在する。
				addUniqueCircle(result, new DoubleCircle(p, 0.0));
				return result;
			}
			// 中心cは、l1とl2のちょうど中間の直線上にあり、かつpからの距離がr
			// 中間の直線: a1 * x + b1 * y + (c1 + sgn * c2)/2 = 0
			// sgn = +/-1 depending on whether they point in same or opposite direction
			double sgn = (a1 * a2 + b1 * b2) > 0 ? 1.0 : -1.0;
			double midC = (c1 + sgn * c2) / 2.0;
			// pから中線へ垂線を下ろした足が中心になる
			double dMid = a1 * p.x + b1 * p.y + midC;
			// 向きを調整：中線への向き
			// 中心 cx = p.x - dMid * a1, cy = p.y - dMid * b1
			// その方向への単位ベクトルに沿って、距離は1つか2つ（平行線の間にあるので、pから中線方向へr進んだ点か、
			// または、中線上のpに最も近い点）。実際は、pは平行線l1, l2の間にあり、その中間線からの距離は0。
			// したがって、方向は中線に沿った方向。
			// 中線の方向ベクトル：(-b1, a1)
			double dx = -b1;
			double dy = a1;
			// pを中心として、中線に沿って左右に r_eff = sqrt(r^2 - dMid^2) 進む
			double radVal = r * r - dMid * dMid;
			if (radVal >= -1e-9) {
				if (radVal < 0.0) radVal = 0.0;
				double step = Math.sqrt(radVal);
				if (step < 1e-9) {
					double cx = p.x - dMid * a1;
					double cy = p.y - dMid * b1;
					addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx, cy), r));
				} else {
					double cx1 = p.x - dMid * a1 + step * dx;
					double cy1 = p.y - dMid * b1 + step * dy;
					addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx1, cy1), r));

					double cx2 = p.x - dMid * a1 - step * dx;
					double cy2 = p.y - dMid * b1 - step * dy;
					addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx2, cy2), r));
				}
			}
		} else {
			// 交差する2直線
			// 交点
			double ipx = (b1 * c2 - b2 * c1) / det;
			double ipy = (a2 * c1 - a1 * c2) / det;
			DoublePoint ip = new DoublePoint(ipx, ipy);

			if (Math.hypot(p.x - ip.x, p.y - ip.y) < 1e-9) {
				// pが交点そのものである場合、半径0の円が1つ存在する
				addUniqueCircle(result, new DoubleCircle(p, 0.0));
				return result;
			}

			// 2つの角二等分線
			double[][] bisectors = {
				{a1 + a2, b1 + b2, c1 + c2},
				{a1 - a2, b1 - b2, c1 - c2}
			};

			for (double[] bis : bisectors) {
				double ba = bis[0];
				double bb = bis[1];
				double bc = bis[2];
				double bNorm = Math.hypot(ba, bb);
				if (bNorm < 1e-9) continue;
				ba /= bNorm;
				bb /= bNorm;
				bc /= bNorm;

				// 角二等分線上の任意の点 Q。交点 ip から単位距離だけ角二等分線に沿って進んだ点。
				// 方向ベクトルは (-bb, ba)
				double qx = ip.x - bb;
				double qy = ip.y + ba;
				// Qからl1への距離 = 半径 R_q
				double rq = Math.abs(a1 * qx + b1 * qy + c1);
				if (rq < 1e-9) continue;

				// 相似比を求める。
				// 求める円の中心 C = ip + k * (Q - ip).
				// Cからpへの距離 |C - p| = k * rq.
				// |ip + k * v - p|^2 = k^2 * rq^2, where v = Q - ip.
				double vx = qx - ip.x;
				double vy = qy - ip.y;
				double v2 = vx * vx + vy * vy;
				double px = p.x - ip.x;
				double py = p.y - ip.y;
				double vp = vx * px + vy * py;
				double p2 = px * px + py * py;

				double A = v2 - rq * rq;
				double B = -2.0 * vp;
				double C_coeff = p2;

				if (Math.abs(A) < 1e-9) {
					if (Math.abs(B) >= 1e-9) {
						double k = -C_coeff / B;
						double cx = ip.x + k * vx;
						double cy = ip.y + k * vy;
						double r = Math.abs(k) * rq;
						addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx, cy), r));
					}
				} else {
					double qDet = B * B - 4.0 * A * C_coeff;
					if (qDet >= -1e-9) {
						if (qDet < 0.0) qDet = 0.0;
						double sqrtQDet = Math.sqrt(qDet);
						double k1 = (-B - sqrtQDet) / (2.0 * A);
						double k2 = (-B + sqrtQDet) / (2.0 * A);
						for (double k : new double[]{k1, k2}) {
							double cx = ip.x + k * vx;
							double cy = ip.y + k * vy;
							double r = Math.abs(k) * rq;
							addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx, cy), r));
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 与えられた2点 a, b を通り、直線 l に接する円のリストを返します。
	 * 2点が極めて近い場合、または直線が退化している場合は null を返します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param a 円が通る点 a
	 * @param b 円が通る点 b
	 * @param l 接する直線 l
	 * @return 条件を満たす円 {@link DoubleCircle} のリスト、または無効な入力の場合は null
	 */
	// 未テスト
	public static List<DoubleCircle> circleThrough2PointsTangentToLine(DoublePoint a, DoublePoint b, DoubleLine l) {
		double distAB = Math.hypot(a.x - b.x, a.y - b.y);
		if (distAB < 1e-9) {
			return null;
		}
		double normL = Math.hypot(l.a, l.b);
		if (normL < 1e-18) {
			return null;
		}
		double la = l.a / normL;
		double lb = l.b / normL;
		double lc = l.c / normL;

		double dx = b.x - a.x;
		double dy = b.y - a.y;
		double L2 = dx * dx + dy * dy;
		double mx = (a.x + b.x) / 2.0;
		double my = (a.y + b.y) / 2.0;
		double K = la * mx + lb * my + lc;
		double D = lb * dx - la * dy;
		double Ka = la * a.x + lb * a.y + lc;
		double Kb = la * b.x + lb * b.y + lc;
		double d_diff = Ka - Kb;
		double A = d_diff * d_diff;
		double B = -2.0 * K * D;
		double C = 0.25 * L2 - K * K;

		List<DoubleCircle> result = new ArrayList<>();
		if (Math.abs(A) < 1e-9) {
			if (Math.abs(B) >= 1e-9) {
				double t = -C / B;
				double cx = mx - t * dy;
				double cy = my + t * dx;
				double r = Math.sqrt(t * t * L2 + 0.25 * L2);
				addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx, cy), r));
			}
		} else {
			double det = B * B - 4.0 * A * C;
			if (det >= -1e-9) {
				if (det < 0.0) {
					det = 0.0;
				}
				double sqrtDet = Math.sqrt(det);
				double t1 = (-B - sqrtDet) / (2.0 * A);
				double t2 = (-B + sqrtDet) / (2.0 * A);
				for (double t : new double[]{t1, t2}) {
					double cx = mx - t * dy;
					double cy = my + t * dx;
					double r = Math.sqrt(t * t * L2 + 0.25 * L2);
					addUniqueCircle(result, new DoubleCircle(new DoublePoint(cx, cy), r));
				}
			}
		}
		return result;
	}

	private static void addUniqueCircle(List<DoubleCircle> list, DoubleCircle c) {
		for (DoubleCircle existing : list) {
			double dist = Math.hypot(existing.center().x() - c.center().x(), existing.center().y() - c.center().y());
			if (dist < 1e-9 && Math.abs(existing.radius() - c.radius()) < 1e-9) {
				return;
			}
		}
		list.add(c);
	}


	/**
	 * 与えられた複数の凸多角形への最短距離の最大値を最小化する2次元平面上の点を求めます。
	 *
	 * <p>各凸多角形への最短距離は、点が多角形の内部（または周上）にある場合は 0、外部にある場合は境界（各辺 of 線分）への最小距離と定義されます。
	 * 点 $P$ から凸多角形 $Q_i$ への距離関数 $d(P, Q_i)$ は凸関数であり、それらの最大値 $F(P) = \max_i d(P, Q_i)$ もまた凸関数です。
	 * 本メソッドは、x 軸方向および y 軸方向の2段階の入れ子型三分探索（Ternary Search）を用いて、この凸関数 $F(P)$ の大域的最小値を与える点 $P$ を高精度に探索します。</p>
	 *
	 * <p>計算量: $O(I_x \cdot I_y \cdot V)$、ここで $I_x$ と $I_y$ はそれぞれ x 軸、y 軸方向の三分探索の反復回数（ともに90回）、$V$ は全多角形の頂点の総数です。</p>
	 *
	 * @param polygons 凸多角形 {@link DoublePolytope2D} のリスト。
	 * @return 距離の最大値を最小化する点 {@link DoublePoint}
	 * @throws IllegalArgumentException 多角形のリストが空であるか、頂点が1つも存在しない場合
	 */
	// 未テスト
	public static DoublePoint minimizeMaxDistanceToPolygons(List<DoublePolytope2D> polygons) {
		//https://atcoder.jp/contests/abc314/submissions/77534070
		if (polygons == null || polygons.isEmpty()) {
			throw new IllegalArgumentException("Polygons list must not be null or empty.");
		}
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		int totalVertices = 0;
		for (DoublePolytope2D poly : polygons) {
			if (poly == null || poly.vertices == null || poly.vertices.length == 0) {
				continue;
			}
			for (DoublePoint p : poly.vertices) {
				minX = Math.min(minX, p.x());
				maxX = Math.max(maxX, p.x());
				minY = Math.min(minY, p.y());
				maxY = Math.max(maxY, p.y());
				totalVertices++;
			}
		}
		if (totalVertices == 0) {
			throw new IllegalArgumentException("There must be at least one vertex across all polygons.");
		}

		double lx = minX;
		double rx = maxX;
		int iters = 90;

		for (int iter = 0; iter < iters; iter++) {
			double mx1 = lx + (rx - lx) / 3.0;
			double mx2 = rx - (rx - lx) / 3.0;
			double f1 = evaluateMaxDistanceAtX(mx1, minY, maxY, polygons);
			double f2 = evaluateMaxDistanceAtX(mx2, minY, maxY, polygons);
			if (f1 < f2) {
				rx = mx2;
			} else {
				lx = mx1;
			}
		}

		double xOpt = (lx + rx) / 2.0;
		double yOpt = getOptimalYAtX(xOpt, minY, maxY, polygons);
		return new DoublePoint(xOpt, yOpt);
	}

	private static double getOptimalYAtX(double x, double minY, double maxY, List<DoublePolytope2D> polygons) {
		double ly = minY;
		double ry = maxY;
		int iters = 90;
		for (int iter = 0; iter < iters; iter++) {
			double my1 = ly + (ry - ly) / 3.0;
			double my2 = ry - (ry - ly) / 3.0;
			double f1 = evaluateMaxDistanceAtXY(x, my1, polygons);
			double f2 = evaluateMaxDistanceAtXY(x, my2, polygons);
			if (f1 < f2) {
				ry = my2;
			} else {
				ly = my1;
			}
		}
		return (ly + ry) / 2.0;
	}

	private static double evaluateMaxDistanceAtX(double x, double minY, double maxY, List<DoublePolytope2D> polygons) {
		double yOpt = getOptimalYAtX(x, minY, maxY, polygons);
		return evaluateMaxDistanceAtXY(x, yOpt, polygons);
	}

	private static double evaluateMaxDistanceAtXY(double x, double y, List<DoublePolytope2D> polygons) {
		DoublePoint p = new DoublePoint(x, y);
		double maxDist = 0.0;
		for (DoublePolytope2D poly : polygons) {
			if (poly == null || poly.vertices == null || poly.vertices.length == 0) {
				continue;
			}
			double d = poly.distance(p);
			if (d > maxDist) {
				maxDist = d;
			}
		}
		return maxDist;
	}

}
