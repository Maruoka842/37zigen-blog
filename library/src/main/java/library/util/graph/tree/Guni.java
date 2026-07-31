package library.util.graph.tree;

import library.util.graph.*;

import java.util.function.IntConsumer;

/**
 * Guni / Sack / DSU on tree
 * 根付き木の全ての部分木を経由するような頂点追加・削除操作列の生成
 * <p>
 * できること:
 * 頂点数 n の根付き木について、頂点集合の部分集合の列 S = (S0, ..., Sm) で以下を満たすものを構築する。
 * - S0 = Sm = ∅
 * - |Si ⊕ Si+1| = 1
 * - 各部分木について、それに含まれる頂点集合と Sk が一致するような k が存在する
 * - m = O(n log n)
 * </p>
 *
 * 計算量: O(n log n)
 */
public class Guni {
	//https://hitonanode.github.io/cplib-cpp/tree/guni.hpp
    private final Tree tree;

    public Guni(Tree tree) {
        if (!tree.isRooted()) {
            throw new IllegalArgumentException("Tree must be rooted.");
        }
        this.tree = tree;
    }

    /**
     * DSU on tree の操作を定義するインターフェース
     */
    public interface GuniStrategy {
        /** 頂点を集合に追加する */
        void add(int v);
        /** 部分木に含まれる全頂点を集合から削除する 
         *  preOrder[start]が根
         *  preOrder[start:start+size)に部分木のすべての頂点
         * */
        void resetSubtree(int[] preOrder, int start, int size);
        /** 現在の頂点 v の部分木が集合と一致した時に呼ばれる */
        void solve(int v);
    }

    /**
     * DSU on tree を実行する。
     *
     * @param strategy 実行する操作の定義
     */
    public void run(GuniStrategy strategy) {
        int[] preOrder = tree.preOrder();
        dfs(tree.root(), strategy, preOrder);
        strategy.resetSubtree(preOrder, tree.preOrderOf(tree.root()), tree.size(tree.root()));
    }

    private void dfs(int v, GuniStrategy strategy, int[] preOrder) {
        int bigChild = -1;
        int maxSz = -1;
        for (int i = 0; i < tree.childs[v].size(); i++) {
            int u = tree.childs[v].get(i);
            if (tree.size(u) > maxSz) {
                maxSz = tree.size(u);
                bigChild = u;
            }
        }

        for (int i = 0; i < tree.childs[v].size(); i++) {
            int u = tree.childs[v].get(i);
            if (u != bigChild) {
                dfs(u, strategy, preOrder);
                strategy.resetSubtree(preOrder, tree.preOrderOf(u), tree.size(u));
            }
        }

        if (bigChild != -1) {
            dfs(bigChild, strategy, preOrder);
        }
        for (int j = 0; j < tree.childs[v].size(); j++) {
        	int u = tree.childs[v].get(j);
            if (u != bigChild) {
                int start = tree.preOrderOf(u);
                int end = start + tree.size(u);
                for (int i = start; i < end; i++) {
                    strategy.add(preOrder[i]);
                }
            }
        }
        strategy.add(v);
        strategy.solve(v);
    }

}
