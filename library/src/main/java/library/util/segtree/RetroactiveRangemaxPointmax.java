package library.util.segtree;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
/**
 * https://atcoder.jp/contests/abc342/submissions/72649989
 */
public class RetroactiveRangemaxPointmax {
	int n;//2冪
	int inputN;
	public PriorityQueue<Long>[] v;
	private int identity=Integer.MIN_VALUE;
	boolean[] deleted=new boolean[200001];
	
	public RetroactiveRangemaxPointmax(int[] A) {
		this.n = 1;
		while(n < 2 * A.length) n *= 2;
		this.inputN = A.length; 
		v = new PriorityQueue[2 * this.n];
		for (int i = 0; i < v.length; i++) {
			v[i]=new PriorityQueue<Long>(Collections.reverseOrder());
			v[i].add(1L*identity);
		}
		for (int i = 0; i < inputN; i++) {
			v[id(i, i + 1)].add(1L*A[i]<<32);
		}
	}
	
	// [a2^k, (a+1)2^k)
	int id(int a, int b) {
		int w = b - a;
		return n / w + a / w;
	}
	
	
	/***
	 * id の逆関数。
	 * x = id(a, b) のとき rangeFromID(x) = new int[] {a, b}
	 * @param id
	 * @return
	 */
	int[] rangeFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return new int[] {a, a + w};
	}
	
	public void act(int l, int r, int val, int id) {
		r=Math.min(r, n);
		l=Math.max(l, 0);
		long state=(((1L*val)<<32)) | id;
		if (r - l <= 0) return;
		int ml = l + Integer.lowestOneBit(l);
		int mr = r - Integer.lowestOneBit(r);
		if (l < ml && ml <= r) {
			v[id(l, ml)].add(state);
			act(ml, r, val, id);
		} else {
			v[id(mr, r)].add(state);
			act(l, mr, val, id);
		}
	}
	
	public void delete(int id) {
		deleted[id]=true;
	}
	

	
	public int get(int i) {
		int k=id(i, i+1);
		int ans=0;
		while(k!=0) {
			while(true) {
				long val=v[k].peek();
				int id=(int)val;
				if(deleted[id]) {
					v[k].poll();
				} else {
					break;
				}
			}
			ans=Math.max(ans, (int)(v[k].peek()>>>32));
			k/=2;
		}
		return ans;
	}
	
	@Override
	public String toString() {
		String ret = "";
		for (int w = 1; w <= n; w *= 2) {
			for (int i = 0; i < w; ++i) {
				ret += v[i + w] + " ".repeat((n == w ? 1 : 2 * n / w - 1));
			}
			ret += "\n";
		}
		return ret;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \log N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("RetroactiveRangemaxPointmax { n: " + n + ", inputN: " + inputN + ", v: " + java.util.Arrays.toString(v) + " }");
	}
}