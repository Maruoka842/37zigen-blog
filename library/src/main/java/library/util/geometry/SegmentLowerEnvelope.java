package library.util.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;


public class SegmentLowerEnvelope {
//https://atcoder.jp/contests/abc303/submissions/73784511
	ArrayList<Segment> segs;
	ArrayList<Segment> env;
	boolean built=false;
	
	public SegmentLowerEnvelope() {
		segs=new ArrayList<Segment>();
	}
	
	public void add(long l, long r, long a, long b) {
		segs.add(new Segment(l, r, a, b));
	}
	
	
    public ArrayList<Segment> build() {
	    ArrayList<Segment> env= new ArrayList<>(segs.size());
	    for (int i = 0; i < segs.size(); i++) {
	    	var seg=segs.get(i);
	    	env.add(new Segment(seg.l, seg.r, seg.a, seg.b));
		}
	    env.sort(Comparator.comparingLong(s -> s.l));
	    return env = divideConquerLowerEnvelope(env);
    }


    ArrayList<Segment> divideConquerLowerEnvelope(ArrayList<Segment> segs) {
    	if (segs.isEmpty()) return new ArrayList<>();
    	if (segs.size() == 1) {
        	return segs;
        }
        
        int mid = segs.size() / 2;
        ArrayList<Segment> left = new ArrayList<>(segs.subList(0, mid));
        ArrayList<Segment> right = new ArrayList<>(segs.subList(mid, segs.size()));

        ArrayList<Segment> envL = divideConquerLowerEnvelope(left);
        ArrayList<Segment> envR = divideConquerLowerEnvelope(right);

        return mergeLowEnvelope(envL, envR);
    }
    
    
    
    ArrayList<Segment> mergeLowEnvelope(ArrayList<Segment> list0, ArrayList<Segment> list1) {
    	ArrayList<Segment> ret=new ArrayList<>();
    	int p = 0;
    	int q = 0;
    	while (p != list0.size() || q != list1.size()) {
    		if (p < list0.size() && list0.get(p).l >= list0.get(p).r) {
    			++p;
    			continue;
    		}
    		if (q < list1.size() && list1.get(q).l >= list1.get(q).r) {
    			++q;
    			continue;
    		}
    		if (p == list0.size()) {
    			var seg=list1.get(q);
				ret.add(seg);
    			++q;
    		} else if (q == list1.size()){
    			var seg=list0.get(p);
				ret.add(seg);
    			++p;
    		} else {
    			var seg0 = list0.get(p);
    			var seg1 = list1.get(q);
    			if (seg0.l < seg1.l) {
    				long r = Math.min(seg0.r, seg1.l);
					ret.add(new Segment(seg0.l, r, seg0.a, seg0.b));
    				seg0.l = r;
    			} else if (seg1.l < seg0.l) {
    				long r = Math.min(seg0.l, seg1.r);
					ret.add(new Segment(seg1.l, r, seg1.a, seg1.b));
    				seg1.l = r;
    			} else {
    				long l = seg0.l;
    				long r = Math.min(seg0.r, seg1.r);
//					long lv0 = seg0.a * l + seg0.b;
//					long lv1 = seg1.a * l + seg1.b;
//					long rv0 = seg0.a * r + seg0.b;
//					long rv1 = seg1.a * r + seg1.b;
    				// lv0 <= lv1 
    				//⇔ seg0.a * l + seg0.b <= seg1.a * l + seg1.b
    				//⇔ (seg0.b - seg1.b) / l <= (seg1.a - seg0.a)
					boolean lv0LeqLv1 = lessOrEqual(seg0.a, seg0.b, seg1.a, seg1.b, l);
					boolean rv0LeqRv1 = lessOrEqual(seg0.a, seg0.b, seg1.a, seg1.b, r);
					boolean lv1LeqLv0 = lessOrEqual(seg1.a, seg1.b, seg0.a, seg0.b, l);
					boolean rv1LeqRv0 = lessOrEqual(seg1.a, seg1.b, seg0.a, seg0.b, r);
					if (lv0LeqLv1 && rv0LeqRv1) {
    					ret.add(new Segment(l, r, seg0.a, seg0.b));
					} else if (lv1LeqLv0 && rv1LeqRv0) {
						ret.add(new Segment(l, r, seg1.a, seg1.b));
					} else {
						// a0 x + b0 = a1 x + b1
						// x = (b1 - b0) / (a0 - a1)
						long mid = Math.ceilDiv(seg1.b - seg0.b, seg0.a - seg1.a);
						if (lv0LeqLv1) {
							if (l != mid)
								ret.add(new Segment(l, mid, seg0.a, seg0.b));
							if (mid != r)
								ret.add(new Segment(mid, r, seg1.a, seg1.b));
						} else {
							if (l != mid)
								ret.add(new Segment(l, mid, seg1.a, seg1.b));
							if (mid != r)
								ret.add(new Segment(mid, r, seg0.a, seg0.b));							
						}
					}
    				seg0.l = r;
    				seg1.l = r;
    				if (seg0.l == seg0.r) {
        				++p;
        				continue;
        			}
        			if (seg1.l == seg1.r) {
        				++q;
        				continue;
        			}
    			}
    		}
    	}
    	return ret;
    }
    
    
    public class Segment {
    	//区間[l, r)のy=ax+b
    	public long l;
    	public long r;
    	public long a;
    	public long b;
    	
    	public Segment(long l, long r, long a, long b) {
    		this.l = l;
    		this.r = r;
    		this.a = a;
    		this.b = b;
    	}

	@Override
	public String toString() {
		return "[" + l + ", " + r + "): y=" + a + "x+" + b;
	}

        /**
         * この線分と別のオブジェクトの同値性を判定します。
         *
         * <p>計算量: $O(1)$</p>
         *
         * @param obj 比較対象のオブジェクト
         * @return 同値であれば true, そうでなければ false
         */
        // 未テスト
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Segment)) return false;
            Segment other = (Segment) obj;
            return l == other.l && r == other.r && a == other.a && b == other.b;
        }

        /**
         * この線分のハッシュコードを計算します。
         *
         * <p>計算量: $O(1)$</p>
         *
         * @return ハッシュコード
         */
        // 未テスト
        @Override
        public int hashCode() {
            return java.util.Objects.hash(l, r, a, b);
        }
    }
    
    
    // returns a0*x + b0 <= a1*x + b1
    boolean lessOrEqual(long a0, long b0, long a1, long b1, long x) {
        long a = a0 - a1;
        long b = b1 - b0;
        // a * x <= b
        if (a == 0) return 0 <= b;
        if (a > 0) {
        	return x <= Math.floorDiv(b, a);
        } else {
        	a *= -1;
        	b *= -1;
        	// a * x >= b
        	return x >= Math.ceilDiv(b, a);
        }
    }
    
    
    
    
    void testRandom() {
        Random rnd = new Random(1);
        for (int it = 0; it < 1000; it++) {
            ArrayList<Segment> segs = new ArrayList<>();
            ArrayList<Segment> segs2 = new ArrayList<>();

            int n = 100;
            for (int i = 0; i < n; i++) {
                long l = rnd.nextInt(20);
                long r = l + 1 + rnd.nextInt(10);
                long a = rnd.nextInt(11) - 5;
                long b = rnd.nextInt(21) - 10;
                segs.add(new Segment(l, r, a, b));
                segs2.add(new Segment(l, r, a, b));
//                tr("l,r,a,b",l,r,a,b);
            }

            segs.sort(Comparator.comparingLong(s -> s.l));

            var env = divideConquerLowerEnvelope(segs);
            long x=rnd.nextLong(-10, 10);
            long v0=brute(segs2, x);
            long v1=evalEnvelope(env, x);
            if(v0!=v1) throw new AssertionError();
            System.out.println("OK");
        }
    }
    
    long evalEnvelope(ArrayList<Segment> env, long x) {
        for (Segment s : env) {
            if (s.l <= x && x < s.r) {
            	return eval(s, x);
            }
        }
        return Long.MAX_VALUE;
    }
    
    long eval(Segment s, long x) {
        return s.a * x + s.b;
    }
    
    
    long brute(ArrayList<Segment> segs, long x) {
        long best = Long.MAX_VALUE;
        for (Segment s : segs) {
            if (s.l <= x && x < s.r) {
                best = Math.min(best, eval(s, x));
            }
        }
        return best;
    }

	
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

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
		System.out.println("SegmentLowerEnvelope { segs: " + segs + ", env: " + env + ", built: " + built + " }");
	}

	/**
	 * この線分下側包絡線と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は登録された線分の数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SegmentLowerEnvelope)) return false;
		SegmentLowerEnvelope other = (SegmentLowerEnvelope) obj;
		return this.segs.equals(other.segs);
	}

	/**
	 * この線分下側包絡線のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は登録された線分の数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(segs);
	}
}
