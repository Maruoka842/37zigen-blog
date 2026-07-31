package library.util.segtree;

import java.util.Arrays;
import library.util.ArrayUtils;
import library.util.seq.SortedArrays;

/**
 * Li-Chao Tree
 * 直線の追加および、指定した点における最小値クエリを $O(\log N)$ で処理する。
 */
public class LiChaoTree {
	/** x 座標の数 */
	private final int n;
	/** セグメント木の葉の数 */
	private final int head;
	/** クエリを投げる可能性のある x 座標（ソート済み・重複除去済み） */
	private final long[] xs;

	/** ノードに保持されている直線の傾き a */
	private final long[] lineA;
	/** ノードに保持されている直線の切片 b */
	private final long[] lineB;
	/** ノードに保持されている直線のインデックス */
	private final int[] lineIdx;
	/** ノードに直線が保持されているか */
	private final boolean[] isValid;

	/** ノードの範囲内の最左の x 座標 */
	private final long[] l;
	/** ノードの右側の子の最左の x 座標（比較の中心点） */
	private final long[] c;
	/** ノードの範囲内の最右の x 座標 */
	private final long[] r;

	/**
	 * get(x) でクエリを投げる可能性のある x 座標の集合を指定して初期化する。
	 * @param xs_ x 座標の集合
	 * 計算量: $O(N \log N)$
	 */
	public LiChaoTree(long[] xs_) {
		this.xs = ArrayUtils.sortq(xs_);
		this.n = xs.length;
		int h = 1;
		while (h < n) h <<= 1;
		this.head = h;

		lineA = new long[2 * head];
		lineB = new long[2 * head];
		lineIdx = new int[2 * head];
		Arrays.fill(lineIdx, -1);
		isValid = new boolean[2 * head];

		l = new long[2 * head];
		c = new long[2 * head];
		r = new long[2 * head];

		if (n > 0) {
			for (int i = 0; i < n; i++) {
				l[head + i] = c[head + i] = r[head + i] = xs[i];
			}
			for (int i = n; i < head; i++) {
				l[head + i] = c[head + i] = r[head + i] = xs[n - 1];
			}

			for (int i = head - 1; i > 0; i--) {
				l[i] = l[2 * i];
				c[i] = l[2 * i + 1];
				r[i] = r[2 * i + 1];
			}
		}
	}

	private long eval(long a, long b, long x) {
		return a * x + b;
	}

	/**
	 * 直線 $y = ax + b$ を追加する。
	 * 未テスト
	 * @param a 傾き
	 * @param b 切片
	 * 計算量: $O(\log N)$
	 */
	public void addLine(long a, long b) {
		addLine(a, b, -1);
	}

	/**
	 * 直線 $y = ax + b$ をインデックス付きで追加する。
	 * 未テスト
	 * @param a 傾き
	 * @param b 切片
	 * @param idx 直線のインデックス
	 * 計算量: $O(\log N)$
	 */
	public void addLine(long a, long b, int idx) {
		if (n == 0) return;
		update(1, 0, head, a, b, idx, 0, n);
	}

	/**
	 * 線分 $y = ax + b (xl \le x < xr)$ を追加する。
	 * 未テスト
	 * @param xl 左端
	 * @param xr 右端
	 * @param a 傾き
	 * @param b 切片
	 * 計算量: $O(\log^2 N)$
	 */
	public void addSegment(long xl, long xr, long a, long b) {
		addSegment(xl, xr, a, b, -1);
	}

	/**
	 * 線分 $y = ax + b (xl \le x < xr)$ をインデックス付きで追加する。
	 * 未テスト
	 * @param xl 左端
	 * @param xr 右端
	 * @param a 傾き
	 * @param b 切片
	 * @param idx 直線のインデックス
	 * 計算量: $O(\log^2 N)$
	 */
	public void addSegment(long xl, long xr, long a, long b, int idx) {
		if (n == 0) return;
		int il = SortedArrays.ceil(xs, xl);
		int ir = SortedArrays.ceil(xs, xr);
		if (il >= ir) return;
		update(1, 0, head, a, b, idx, il, ir);
	}

	private void update(int now, int nowl, int nowr, long a, long b, int idx, int il, int ir) {
		if (nowl >= ir || nowr <= il) return;

		int nowc = (nowl + nowr) / 2;
		if (il <= nowl && nowr <= ir) {
			if (!isValid[now]) {
				lineA[now] = a;
				lineB[now] = b;
				lineIdx[now] = idx;
				isValid[now] = true;
				return;
			}

			boolean updL = eval(lineA[now], lineB[now], l[now]) > eval(a, b, l[now]);
			boolean updC = eval(lineA[now], lineB[now], c[now]) > eval(a, b, c[now]);
			boolean updR = eval(lineA[now], lineB[now], r[now]) > eval(a, b, r[now]);

			if (updL && updC && updR) {
				lineA[now] = a;
				lineB[now] = b;
				lineIdx[now] = idx;
				return;
			}
			if (!updL && !updC && !updR) return;

			if (updC) {
				long ta = lineA[now]; lineA[now] = a; a = ta;
				long tb = lineB[now]; lineB[now] = b; b = tb;
				int ti = lineIdx[now]; lineIdx[now] = idx; idx = ti;
				if (updR) { // !updL && updC && updR
					update(2 * now, nowl, nowc, a, b, idx, il, ir);
				} else { // updL && updC && !updR
					update(2 * now + 1, nowc, nowr, a, b, idx, il, ir);
				}
			} else {
				if (updL) { // updL && !updC && !updR
					update(2 * now, nowl, nowc, a, b, idx, il, ir);
				} else { // !updL && !updC && updR
					update(2 * now + 1, nowc, nowr, a, b, idx, il, ir);
				}
			}
		} else {
			if (il < nowc) update(2 * now, nowl, nowc, a, b, idx, il, ir);
			if (ir > nowc) update(2 * now + 1, nowc, nowr, a, b, idx, il, ir);
		}
	}

	/**
	 * クエリ結果を保持するレコード
	 * @param a 傾き
	 * @param b 切片
	 * @param idx 直線のインデックス
	 * @param isValid 有効な直線が存在するか
	 * @param minVal 最小値
	 */
	public record Result(long a, long b, int idx, boolean isValid, long minVal) {}

	/**
	 * 指定した $x$ における最小値を求める。
	 * 未テスト
	 * @param x クエリを投げる x 座標
	 * @return 最小値およびそれを与える直線の情報
	 * 計算量: $O(\log N)$
	 */
	public Result get(long x) {
		int i = Arrays.binarySearch(xs, x);
		if (i < 0) throw new IllegalArgumentException("x must be one of the points specified in the constructor");

		int now = i + head;
		long resA = 0, resB = 0;
		int resIdx = -1;
		boolean resValid = false;
		long minVal = Long.MAX_VALUE;

		while (now > 0) {
			if (isValid[now]) {
				long val = eval(lineA[now], lineB[now], x);
				if (!resValid || val < minVal) {
					resValid = true;
					minVal = val;
					resA = lineA[now];
					resB = lineB[now];
					resIdx = lineIdx[now];
				}
			}
			now /= 2;
		}
		return new Result(resA, resB, resIdx, resValid, minVal);
	}
}
