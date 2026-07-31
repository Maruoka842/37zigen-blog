package library.util.fold;

import library.util.ArrayUtils;

public class StaticSlopeAddPointGet1D {
	int N;
	long[]b0;
	long[]b1;
	long[]sum;
	boolean isBuilt = false;
	
	// Σsum[i]x^i = A(x)/(1-x) + B(x)/(1-x)²
	// としたとき、
	//A(x) = Σb0[i]x^i
	//B(x) = Σb1[i]x^i
	//sum[i] = Σ[j ≤ i] b0[j] + (i - j + 1)b1[j]
	
	public StaticSlopeAddPointGet1D(int N) {
		this.N = N;
		b0 = new long[N];
		b1 = new long[N];
		sum = new long[N];//最終結果
	}
	
	
	/**
	 * i番目(0-indexed)の要素にval|i-center|を足す。
	 * https://atcoder.jp/contests/abc366/submissions/71357778
	 * @param center
	 * @param val
	 */
	public void addDistanceFrom(long val, int center) {
		b1[0]-=val;
		b1[center+1]+=2*val;
		b0[0]+=(center+1)*val;
	}
	
	/**
	 * 円環上でcenterからの距離にaを掛けた値a min(|i-center|,N-|i-center|)足す。
	 * https://atcoder.jp/contests/abc268/submissions/73357510
	 * @param center
	 * @param coeffs
	 */
	public void addCircularDistanceFrom(long a, int center) {
		
		//N=11(奇数)の場合、距離は
		//12012345543
		//N=12(偶数)の場合、距離は
		//120123456543
		//が足される
		
		center = (center % N + N) % N;
		for (int c : new int[] {center - N, center}) {
			addSlope(c, c+(N+1)/2, a, 0);
			addSlope(c+(N+1)/2, c+N, -a, (N/2)*a);
		}
	}

	/**
	 * 円環上を正の向き（インデックスが増加する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(i) = a * ((i - center) mod N) (0 <= i < N) です。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addCircularDistanceFromPositive(long a, int center) {
		center = (center % N + N) % N;
		for (int c : new int[] { center - N, center }) {
			addSlope(c, c + N, a, 0);
		}
	}

	/**
	 * 円環上を負の向き（インデックスが減少する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(i) = a * ((center - i) mod N) (0 <= i < N) です。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addCircularDistanceFromNegative(long a, int center) {
		center = (center % N + N) % N;
		for (int c : new int[] { center - N, center }) {
			addSlope(c + 1, c + 1 + N, -a, (N - 1) * a);
		}
	}
	
	
	/**
	 * i番目(0-indexed)の要素に a max(0, i-b) を足す
	 * 未テスト
	 * @param a
	 * @param b
	 */
	public void addRightRamp(long a, int b) {
		if(b>=N-1)return;
		b1[b+1] += a;
	}
	
	/**
	 * i番目(0-indexed)の要素に a max(0, -i+b) を足す
	 * 未テスト
	 * @param a
	 * @param b
	 */
	public void addLeftRamp(long a, int b) {
		b0[0] += a*(b+1);
		b1[0] -= a;
		b1[b+1] += a;
	}

	/**
	 * [l, r) に定数 c を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param l 区間の左端（包含）
	 * @param r 区間の右端（非包含）
	 * @param c 加算する定数
	 */
	// 未テスト
	public void addConstant(int l, int r, long c) {
		addSlope(l, r, 0, c);
	}

	/**
	 * min(x - l, r - x, a) * scale を [l, r] に加算する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param l 範囲の左端
	 * @param r 範囲の右端（包含）
	 * @param a 山の最大高さ
	 * @param scale 拡大係数
	 */
	// 未テスト
	public void addMountain(int l, int r, int a, long scale) {
		if (isBuilt) throw new AssertionError("Already built");
		if (l > r || a <= 0) return;

		int mL = Math.min(a, (r - l + 1) / 2);
		if (mL > 0) {
			// Left ascending slope on [l, l + mL)
			addSlope(l, l + mL, scale, 0);
		}

		// Flat peak on [l + mL, r - mL + 1)
		int peakL = l + mL;
		int peakR = r - mL + 1;
		if (peakL < peakR) {
			long peakVal = mL * scale;
			addConstant(peakL, peakR, peakVal);
		}

		if (mL > 0) {
			// Right descending slope on [r - mL + 1, r + 1)
			long startVal = (mL - 1) * scale;
			addSlope(r - mL + 1, r + 1, -scale, startVal);
		}
	}
	
	/**
	 * [l, r) に a(x-l)+bを足す。
	 * https://atcoder.jp/contests/abc268/submissions/73357293
	 * @param l
	 * @param r
	 * @param a
	 * @param b
	 */
	public void addSlope(int l, int r, long a, long b) {
		if(l<0) {
			b-=a*l;
			l=0;
		}
		r=Math.min(r, N);
		if(l>=r)return;
		if(l>=N)return;
		b0[l]+=b;
		if(l+1<b1.length) b1[l+1]+=a;
		if(r<N) {
			b1[r]-=a;
			b0[r]-=a*(r-l-1)+b;
		}
		
	}
	
	public void build() {
		if (isBuilt) throw new AssertionError();
		sum = ArrayUtils.prefixSum(b1);
		for (int i = 0; i < sum.length; i++) {
			sum[i]+=b0[i];
		}
		sum=ArrayUtils.prefixSum(sum);
		isBuilt = true;
	}
	
	public long get(int i) {
		if (!isBuilt) {
			build();
		}
		return sum[i];
	}
	
	public long[] values() {
		if (!isBuilt)build();
		return sum;
	}
	
	public void dump() {
		System.out.println("b0");
		for (int i = 0; i < b0.length; i++) {
			System.out.print(b0[i]+(i==b0.length-1?"\n":" "));
		}
		System.out.println("b1");
		for (int i = 0; i < b1.length; i++) {
			System.out.print(b1[i]+(i==b1.length-1?"\n":" "));
		}
		long[] a = ArrayUtils.prefixSum(b1);
		for (int i = 0; i < a.length; i++) {
			a[i]+=b0[i];
		}
		a=ArrayUtils.prefixSum(a);

		System.out.println("累積和を２回取った後の値");
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i]+(i==a.length-1?"\n":" "));
		}
	}
}
