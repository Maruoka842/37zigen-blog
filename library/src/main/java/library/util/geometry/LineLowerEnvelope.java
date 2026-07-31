package library.util.geometry;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.FractionUtils;
import library.util.LongFraction;
import library.util.collections.ObjectDeque;


public class LineLowerEnvelope {
	ObjectDeque<LongLine> lines;
	boolean built=false;
	
	public LineLowerEnvelope() {
		lines=new ObjectDeque<LongLine>();
	}
	
	/**
	 * 直線の数
	 * @return
	 */
	public int size() {
		return lines.size();
	}
	
	public LongLine pollFirst() {
		return lines.pollFirst();
	}
	
	public LongLine pollLast() {
		return lines.pollLast();
	}
	
	public LongLine getLine(int i) {
		return lines.get(i);
	}
	
	/**
	 * max ax+by
	 * @param a
	 * @param b
	 * @return
	 * @see https://atcoder.jp/contests/abc356/submissions/72484853
	 */
	public double maximizeAxPlusByAsDouble(long a, long b) {
		if(!built)throw new AssertionError();
		//ax+by=k
		//y=-ax/b+k/b
		int ok = -1;
        int ng = lines.size();
        LongFraction p = new LongFraction(-a, b);
        while (Math.abs(ok - ng) != 1) {
            int mid = (ok + ng) / 2;
            // ax+by+c=0
            // y=-(a/b)x-(c/b)
            if (new LongFraction(-lines.get(mid).a, lines.get(mid).b).compareTo(p) >= 0) {
                ok = mid;
            } else {
                ng = mid;
            }
        } 
        if (ok == -1) {
        	return Double.POSITIVE_INFINITY;
        } else if (ok == lines.size()- 1) {
            if (new LongFraction(-lines.get(ok).a, lines.get(ok).b).fastCompareTo(p) == 0) {
                double y = lines.get(ok).yIntercept();
                return y*b;
            } else {
            	return Double.POSITIVE_INFINITY;
            }
        } else {
            var xy = lines.get(ok).intersection(lines.get(ok+1));
            return a*xy.x.toDouble()+b*xy.y().toDouble();
        }

	}
	
	//https://atcoder.jp/contests/abc356/submissions/72484087
	public void registerLine(LongLine line) {
		if(line.b==0) {
			throw new AssertionError();
		}
		built=false;
		lines.addLast(line);
	}
	
	
	
	public LongFraction minYAt(long x) {
		if (lines.isEmpty()) throw new AssertionError();
		if(!built)build();
		int ng=lines.size();
		int ok=0;
		while(Math.abs(ok-ng)!=1) {
			int m=(ok+ng)/2;
			if(lines.get(m-1).intersection(lines.get(m)).x.compareTo(new LongFraction(x, 1)) <= 0) ok=m; 
			else ng=m;
		}
		// ax+by+c=0
		// y=-c/b-(ax)/b
		var line=lines.get(ok);
		return new LongFraction(-line.c, line.b).add(new LongFraction(-line.a*x, line.b));
	}
	
	//https://atcoder.jp/contests/abc356/submissions/72484087
	public void build() {
		built=true;
		if(lines.isEmpty()) return;
		// ax+by+c=0
		// y = -ax/b-c/b
		// -A[i]/B[i]の降順に並び変える
		LongLine[] array=lines.toArray(LongLine[]::new);
		lines.clear();
		Arrays.sort(array, (u, v)-> {
			if (u.a*v.b==v.a*u.b) {
                // 傾きが同じときy切片-c/bの降順
				return -FractionUtils.compareFraction(-u.c, u.b, -v.c, v.b);//new Fractionすると3倍掛かる
            } else {
				return -FractionUtils.compareFraction(-u.a, u.b, -v.a, v.b);//new Fractionすると3倍掛かる
            }
		});
		ArrayList<LongLine>list=new ArrayList<>();
		list.add(array[0]);
		for (int i = 1; i < array.length; i++) {
			while(list.size()>=1) {
				LongLine t=list.getLast();
				if(array[i].a*t.b==t.a*array[i].b) {
					list.removeLast();
				} else {
					break;
				}
			}
			while(list.size()>=2) {
				LongLine t=list.getLast();
				LongLine s=list.get(list.size()-2);
				if (t.intersection(s).x.compareTo(s.intersection(array[i]).x) >= 0) {
					list.removeLast();
				} else {
					break;
				}
			}
			list.add(array[i]);
		}
		for (int i = 0; i < list.size(); i++) {
			lines.addLast(list.get(i));
		}
	}
	
	//https://atcoder.jp/contests/abc356/submissions/72484087
	public LongLine[] toArray() {
		if(!built) build();
		return lines.toArray(LongLine[]::new);
	}
	
	/**
	 * 下側包絡線の各直線情報を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 * @return 直線情報の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (var line : lines) {
			if (!first) sb.append("\n");
			sb.append(Arrays.deepToString(new Object[]{line}));
			first = false;
		}
		return sb.toString();
	}

	//https://atcoder.jp/contests/abc356/submissions/72484087
	public void dump() {
		System.out.println(toString());
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * この下側包絡線と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N \log N)$、ここで $N$ は登録された直線の数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LineLowerEnvelope)) return false;
		LineLowerEnvelope other = (LineLowerEnvelope) obj;
		if (!this.built) this.build();
		if (!other.built) other.build();
		return this.lines.equals(other.lines);
	}

	/**
	 * この下側包絡線のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N \log N)$、ここで $N$ は登録された直線の数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		if (!this.built) this.build();
		return java.util.Objects.hash(lines);
	}
}
