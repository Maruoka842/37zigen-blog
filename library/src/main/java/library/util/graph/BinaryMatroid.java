package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * 二値マトロイド（F2 上のベクトルマトロイド）を表すクラス。
 * 線形独立なベクトル集合を独立集合とする。
 * 未テスト
 */
public class BinaryMatroid implements Matroid {
    /**
     * 台集合の各要素を表すベクトル。
     * mat[e] = e 番目の要素に対応する F2 ベクトル
     */
	private final long[][] mat;
    private final int words;//mat[0].length
    /**
     * 現在の独立集合 I の要素番号のリスト。
     */
    private IntArrayList Iset;
    /**
     * 各 i に対して、
     * 「I \ {Iset[i]} の張るベクトル空間の基底」を保持する。
     * ただし、i==bs.sizeはIの張るベクトル空間の基底
     */
    private ArrayList<ArrayList<long[]>> bs;

    /**
     * 各要素がビットベクトル（long配列）で表される二値マトロイドを構築する。
     * @param mat 台集合の要素（各要素は同じ長さの long 配列）
     */
    public BinaryMatroid(long[][] mat) {
        this.mat = mat;
        this.words = (mat != null && mat.length > 0 && mat[0] != null) ? mat[0].length : 0;
    }

    /**
     * 各要素が 64 ビット整数で表される二値マトロイドを構築する。
     * @param mat 台集合の要素
     */
    public BinaryMatroid(long[] mat) {
        this.mat = new long[mat.length][1];
        for (int i = 0; i < mat.length; i++) {
            this.mat[i][0] = mat[i];
        }
        this.words = 1;
    }

    @Override
    public int size() {
        return mat.length;
    }

    @Override
    public void set(boolean[] I) {
        Iset = new IntArrayList();
        for (int e = 0; e < mat.length; e++) {
            if (I[e]) Iset.add(e);
        }
        int isize = Iset.size();
        bs = new ArrayList<>(isize + 1);
        for (int i = 0; i <= isize; i++) {
            bs.add(new ArrayList<>());
        }
        for (int i = 0; i <= isize; i++) {
            ArrayList<long[]> currentBs = bs.get(i);
            for (int j = 0; j < isize; j++) {
                if (i == j) continue;
                long[] v = Arrays.copyOf(mat[Iset.get(j)], words);
                for (long[] b : currentBs) {
                    chxormin(v, b);
                }
                if (isNonZero(v)) {
                    currentBs.add(v);
                }
            }
        }
    }

    @Override
    public IntArrayList circuit(int e) {
        long[] v = Arrays.copyOf(mat[e], words);
        ArrayList<long[]> lastBs = bs.get(bs.size() - 1);
        for (long[] b : lastBs) {
            chxormin(v, b);
        }
        if (isNonZero(v)) return new IntArrayList(0); // I + {e} is independent

        IntArrayList ret = new IntArrayList();
        ret.add(e);
        for (int i = 0; i < Iset.size(); i++) {
            long[] w = Arrays.copyOf(mat[e], words);
            ArrayList<long[]> currentBs = bs.get(i);
            for (long[] b : currentBs) {
                chxormin(w, b);
            }
            if (isNonZero(w)) {
                ret.add(Iset.get(i));
            }
        }
        return ret;
    }

    /**
     * l <- min(l, l xor r)
     * @param l
     * @param r
     */
    private void chxormin(long[] l, long[] r) {
        int i = findFirst(r);
        if (i != -1 && isBit1(l, i)) {
            for (int j = 0; j < words; j++) {
                l[j] ^= r[j];
            }
        }
    }
    
    /**
     * 最下位bit の位置
     * @param v
     * @return
     */
    private int findFirst(long[] v) {
        for (int i = 0; i < words; i++) {
            if (v[i] != 0) {
                return i * 64 + Long.numberOfTrailingZeros(v[i]);
            }
        }
        return -1;
    }

    private boolean isBit1(long[] v, int bit) {
        return (v[bit >> 6] & (1L << (bit & 63))) != 0;
    }

    private boolean isNonZero(long[] v) {
        for (int i = 0; i < words; i++) {
            if (v[i] != 0) return true;
        }
        return false;
    }
}
