package library.util.graph.tree;

import library.util.graph.*;

import java.util.function.IntConsumer;

/**
 * Guni (DSU on tree) のための一般的な問題に対する Strategy を提供するファクトリクラス
 */
public class GuniStrategyFactory {

    /**
     * 部分木に含まれる異なる値の個数を計算する Strategy
     * @param results 結果を格納する配列
     * @param values 各頂点の値 (0 以上 maxValue 未満)
     * @param maxValue 値の最大値 + 1
     * @return GuniStrategy
     */
    public static Guni.GuniStrategy distinctCount(int[] results, int[] values, int maxValue) {
        return new Guni.GuniStrategy() {
        	final int[] count = new int[maxValue];
            int distinct = 0;
            @Override
            public void add(int v) {
                if (count[values[v]] == 0) distinct++;
                count[values[v]]++;
            }
            @Override
            public void resetSubtree(int[] preOrder, int start, int size) {
                for (int i = start; i < start + size; i++) {
                    int v = preOrder[i];
                    count[values[v]]--;
                    if (count[values[v]] == 0) distinct--;
                }
            }
            @Override
            public void solve(int v) { results[v] = distinct; }
        };
    }
    
    
    
    /**
     * 部分木に含まれる値の最頻値の情報を計算する Strategy
     * 「出現回数が最大の値の種類数」と「その最大出現回数」を計算する。
     * 
     * @param results 結果を格納する配列。各要素は long[2] 配列で、
     *                results[v][0] = 出現回数が最大の色の種類数
     *                results[v][1] = その最大出現回数
     * @param color 各頂点の値 (0 以上 maxValue 未満)
     * @param numOfColor 値の最大値 + 1
     * @return GuniStrategy
     */
    public static Guni.GuniStrategy modeSumWithMaxCount(long[][] results, int[] color, int numOfColor) {
        return new Guni.GuniStrategy() {
            // cnt[x]: 現在テーブルに入っている頂点のうち、色 x の頂点数
            final int[] colToCnt = new int[numOfColor];
            // num[t]: 現在テーブルに入っている色のうち、ちょうど t 回現れている色の値の種類数
            final long[] num = new long[numOfColor + 2];
            // mx: 現在の最大出現回数
            int mx = 0;
            
            @Override
            public void add(int v) {
                int col = color[v];
                if (col < 0 || col >= colToCnt.length) {
                    throw new RuntimeException("Invalid value: " + col + ", colToCnt[col].length=" + colToCnt.length);
                }
                if (colToCnt[col] > 0) {
                    num[colToCnt[col]]--;
                }
                colToCnt[col]++;
                num[colToCnt[col]] ++;
                
                if (colToCnt[col] > mx) {
                    mx = colToCnt[col];
                }
            }
            
            @Override
            public void resetSubtree(int[] preOrder, int start, int size) {
                for (int i = start; i < start + size; i++) {
                    int col = color[preOrder[i]];
                    num[colToCnt[col]]--;
                    colToCnt[col]--;
                    num[colToCnt[col]]++;
                }
                while (mx > 0 && num[mx] == 0) {
                    mx--;
                }
            }
            
            @Override
            public void solve(int v) {
                results[v][0] = num[mx];
                results[v][1] = mx;
            }
        };
    }
}
