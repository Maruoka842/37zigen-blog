package library.util.poset;

import java.util.Arrays;

import library.util.ArrayStatistics;
import library.util.ArrayUtils;
import library.util.segtree.SegTreeFactory;

public class IntpairLattice {

    /**
     * {@code (x[i], y[i]) <= (x[j], y[j])} を {@code x[i] <= x[j] && y[i] <= y[j]}
     * で定める半順序において、最大サイズの antichain をひとつ返す。
     * 返り値の各要素は {@code int[] {x, y}} であり、入力に同じ点が複数ある場合でも
     * antichain には同じ点を高々ひとつしか含めない。
     *
     * @param x 各点の第1座標
     * @param y 各点の第2座標
     * @return 最大 antichain
     */
    public static int[][] maximumAntichain(int[] x, int[] y) {
    	//未テスト
    	//O(N log N)
    	if(x.length!=y.length) throw new AssertionError();
    	int N=x.length;
    	if(N==0)return new int[0][2];
    	int[] X=x.clone();
    	int[] Y=y.clone();
    	ArrayUtils.sort(X, Y);
    	int[] indices=ArrayStatistics.strictLDSIndices(Y);
    	int[][] ret=new int[indices.length][2];
    	for (int i = 0; i < indices.length; i++) {
    		int t=indices[i];
			ret[i]=new int[] {X[t], Y[t]};
		}
    	return ret;
    }

    /**
     * {@code (x[i], y[i]) <= (x[j], y[j])} を {@code x[i] <= x[j] && y[i] <= y[j]}
     * で定める半順序において、最大長の chain をひとつ返す。
     * 返り値の各要素は {@code int[] {x, y}} で、同じ点が複数ある場合は高々ひとつだけ使う。
     *
     * 計算量: O((N + max(y)) log max(y))
     * @param x 各点の第1座標。0以上10^6以下である必要がある
     * @param y 各点の第2座標。0以上10^6以下である必要がある
     * @return 最大 chain
     */
    public static int[][] longestChain(int[] x, int[] y) {
    	//verified:https://atcoder.jp/contests/abc369/tasks/abc369_f
    	int N=x.length;
    	int[] X=x.clone();
    	int[] Y=y.clone();
    	int maxY=ArrayUtils.max(Y);
    	if(ArrayUtils.max(x)>1e6) throw new AssertionError();
    	if(ArrayUtils.max(y)>1e6) throw new AssertionError();
    	if(ArrayUtils.min(x)<0) throw new AssertionError();
    	if(ArrayUtils.min(y)<0) throw new AssertionError();
    	
    	ArrayUtils.sort(X, Y);
    	var seg=SegTreeFactory.max(maxY+1);
    	int[]pre=new int[N];
    	int[]lastId=new int[maxY+1];
    	Arrays.fill(pre, -1);
    	Arrays.fill(lastId, -1);
    	for (int i = 0; i < N; i++) {
    		if(i>0&&X[i]==X[i-1]&&Y[i]==Y[i-1])continue;
    		final long max=seg.fold(0, Y[i]+1);
    		int arg=-1;
    		if(max>0) {
    			arg=seg.minimalLeft(Y[i], v->v<max);
    		}
    		seg.set(Y[i], Math.max(max, 0)+1);
    		if(arg!=-1) {
    			pre[i]=lastId[arg];
    		}
    		lastId[Y[i]]=i;
		}
    	int max=(int) seg.fold(0, maxY+1);
    	int arg=seg.minimalLeft(maxY+1, v->v<max);
    	int[][] ret=new int[max][max];
    	int t = lastId[arg];
    	for (int i = 0; i < max; i++) {
			ret[max-1-i]=new int[] {X[t], Y[t]};
			t=pre[t];
    	}
    	return ret;
    }
	
	
    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
