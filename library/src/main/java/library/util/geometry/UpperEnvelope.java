package library.util.geometry;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.FractionUtils;
import library.util.LongFraction;
import library.util.collections.ObjectDeque;


public class UpperEnvelope {
	ObjectDeque<LongLine> lines;
	
	boolean built=true;
	
	public UpperEnvelope() {
		lines=new ObjectDeque<LongLine>();
	}
	
	public void registerLine(LongLine line) {
		if(line.b==0) {
			throw new AssertionError();
		}
		built=false;
		lines.addLast(line);
	}
	
	public void addLast(LongLine line) {
		//linesは直線が傾きの昇順に並んでいる
		
		while(!lines.isEmpty()) {//同じ傾きの直線を処理
			if (line.a * lines.peekLast().b == line.b * lines.peekLast().a) {
				//ax+by+c=0についてx=0とするとy=-c/b
				if (line.yInterceptAsFraction().compareTo(lines.peekLast().yInterceptAsFraction()) <= 0) return;
				lines.pollLast();
			} else {
				break;
			}
		}
		
		
		while(lines.size() >= 2) {//同じ傾きの直線を処理
			var s=lines.get(lines.size()-2);
			var t=lines.get(lines.size()-1);
			if(s.intersection(line).x().compareTo(t.intersection(line).x()) >= 0) {
				lines.pollLast();
			} else {
				break;
			}
		}
		lines.addLast(line);
		
	}

	/**
	 * 上包絡線 y = max_i(a_i x + b_i) と ax+by+c=0 の交点を返す
	 * 限られた条件についてのみしか動かない。
	 * @param y
	 * @return
	 * @see https://atcoder.jp/contests/abc341/submissions/72405436
	 */
	public FractionPoint intersectionWithLine(LongLine l) {
	    if (lines.isEmpty()) throw new AssertionError();
	    if (!built) build();
	    if (l.a * l.b < 0) throw new AssertionError("未実装");//傾き非負の場合以外は未実装
	    if(lines.peekFirst().a*lines.peekFirst().b>0) {//傾き負から始まり、傾き負と交差する場合以外は未実装。
	    	int ok=0;
	    	int ng=lines.size();
	    	while(ng-ok!=1) {
	    		int mid=(ok+ng)/2;
	    		if(lines.get(mid).a*lines.get(mid).b <= 0) {//傾き非負は無視
	    			ng=mid;
	    			continue;
	    		}
	    		var L=lines.get(mid);
	    		if(L.intersection(lines.get(mid-1)).x().compareTo(L.intersection(l).x()) <= 0) {// ax+by+c=0⇔x=(-by-c)/a
	    			ok=mid;
	    		} else {
	    			ng=mid;
	    		}
	    	}
	    	return lines.get(ok).intersection(l);
	    } else {
	    	throw new AssertionError("未実装");
	    }
	}
	
	//https://atcoder.jp/contests/abc289/submissions/72484278
	public LongFraction maxYAt(long x) {
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
	
	
	public LongFraction maxYAt(LongFraction x) {
		if (lines.isEmpty()) throw new AssertionError();
		if(!built)build();
		int ng=lines.size();
		int ok=0;
		while(Math.abs(ok-ng)!=1) {
			int m=(ok+ng)/2;
			if(lines.get(m-1).intersection(lines.get(m)).x.compareTo(x) <= 0) ok=m; 
			else ng=m;
		}
		// ax+by+c=0
		// y=-c/b-(ax)/b
		var line=lines.get(ok);
		return new LongFraction(-line.c, line.b).add(new LongFraction(-line.a, line.b).mul(x));
	}
	
	
	void build() {
		built=true;
		if(lines.isEmpty()) return;
		// ax+by+c=0
		// y = -ax/b-c/b
		// -A[i]/B[i]の昇順に並び変える
		LongLine[] array=lines.toArray(LongLine[]::new);
		lines.clear();
		Arrays.sort(array, (u, v)-> {
			if (u.a*v.b==v.a*u.b) {
				//傾きが同じときy切片-c/bの昇順
				return FractionUtils.compareFraction(-u.c, u.b, -v.c, v.b);
			} else {
				return FractionUtils.compareFraction(-u.a, u.b, -v.a, v.b);
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
	
	
	/**
	 * 上包絡線の各直線情報を文字列として表す。
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

	/**
	 * 上包絡線の直線情報を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(N)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * この上側包絡線と別のオブジェクトの同値性を判定します。
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
		if (!(obj instanceof UpperEnvelope)) return false;
		UpperEnvelope other = (UpperEnvelope) obj;
		if (!this.built) this.build();
		if (!other.built) other.build();
		return this.lines.equals(other.lines);
	}

	/**
	 * この上側包絡線のハッシュコードを計算します。
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
