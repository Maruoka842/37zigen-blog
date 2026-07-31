package library.util.seq;

import library.util.fold.WaveletMatrix;
import library.util.segtree.IntSumBinaryIndexedTree;

/**
 * AllSubstringsLCS: All-substrings Longest Common Subsequence
 *
 * 論文: Alves, Cáceres, Song (2008)
 *       "An all-substrings common subsequence algorithm"
 *       Discrete Applied Mathematics 156, pp.1025-1035
 *
 * =====================================================================
 * 【問題の概要】
 * 文字列 S (長さna) と T (長さnb) が与えられたとき、
 * T のすべての部分文字列 T[i:j) に対して
 * S との LCS (最長共通部分列) の長さを求める問題。
 *
 * 普通の LCS は O(na*nb) で1ペア分しか解けない。
 * このアルゴリズムは O(na*nb) 時間・O(na+nb) 空間で
 * 全部分文字列分の答えをまとめて準備できる。
 * =====================================================================
 */
public class AllSubstringsLCS {
    public final int na, nb;
    public final int[] IG; // IG[j] = ih(na, j),  j=1..nb
    public final WaveletMatrix wm;
    public final WaveletMatrix[] wms;

    /**
     * コンストラクタで O(na * nb log nb) の前処理を行う。
     * @param S 文字列 S
     * @param T 文字列 T
     */
    public AllSubstringsLCS(String S, String T) {
        this(S.toCharArray(), T.toCharArray());
    }

    public AllSubstringsLCS(char[] S, char[] T) {
        this.na = S.length;
        this.nb = T.length;

        int[] ih = new int[nb + 1];
        for (int j = 0; j <= nb; j++) {
            ih[j] = j;
        }

        this.wms = new WaveletMatrix[na + 1];
        this.wms[0] = new WaveletMatrix(ih);

        int[] ihNew = new int[nb + 1];

        for (int l = 1; l <= na; l++) {
            int iv = 0;
            char ci = S[l - 1];
            for (int j = 1; j <= nb; j++) {
                int iv_prev = iv;
                int ih_prev = ih[j];

                if (ci != T[j - 1]) {
                    ihNew[j] = Math.max(iv_prev, ih_prev);
                    iv       = Math.min(iv_prev, ih_prev);
                } else {
                    ihNew[j] = iv_prev;
                    iv       = ih_prev;
                }
            }
            int[] tmp = ih;
            ih = ihNew;
            ihNew = tmp;
            this.wms[l] = new WaveletMatrix(ih);
        }

        this.IG = ih;
        this.wm = this.wms[na];
    }

    /**
     * LCS(S[0:i), T[j:k)) の長さを返す。
     * @param i Sの接頭辞の長さ (0-indexed, inclusive)
     * @param j Tの開始インデックス (0-indexed, inclusive)
     * @param k Tの終了インデックス (0-indexed, exclusive)
     * @return LCS の長さ
     * 計算量: O(log nb) / query
     */
    public int query(int i, int j, int k) {
    	//https://judge.yosupo.jp/submission/372381
        if (j >= k) return 0;
        return wms[i].countLeq(j + 1, k + 1, j);
    }
}

