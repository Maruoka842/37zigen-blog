package library.util.graph.tree;

import java.util.Iterator;

import library.util.collections.IntArrayList;

/**
 * 辺の追加が可能な森（Incremental Forest）を管理するクラス。
 * Binary Lifting の一種である Jump Pointer 構造を用いることで、動的な辺追加と、
 * LCA (Lowest Common Ancestor), LA (Level Ancestor), 距離クエリを効率的にサポートする。
 *
 * Jump Pointer 構造:
 * 各頂点 v に対して jump[v] を保持する。
 * d = depth[v] としたとき、
 * - todbl[d-1] が true ならば jump[v] = jump[jump[parent[v]]]
 * - todbl[d-1] が false ならば jump[v] = parent[v]
 * todbl はフラクタル的な構造を持つブール列であり、これにより任意の深さへの遡上が
 * O(log N) ステップで可能となる。
 */
public class IncrementalForest {
    // 頂点の属性
    private final IntArrayList jump = new IntArrayList();      // Jump Pointer (遡上を高速化)
    private final IntArrayList parent = new IntArrayList();    // 親頂点 (-1 は根)
    private final IntArrayList depth = new IntArrayList();     // 根からの深さ
    private final IntArrayList child = new IntArrayList();     // 最初の子頂点 (-1 は子なし)
    private final IntArrayList brother = new IntArrayList();   // 次の兄弟頂点 (-1 は末尾)

    // DSU (Union-Find) による連結成分管理
    private final IntArrayList dsu = new IntArrayList();       // 負値は根かつ成分サイズの絶対値
    private boolean[] todbl = new boolean[]{false};            

    /**
     * 空の IncrementalForest を構築する。
     * 計算量: O(1)
     */
    public IncrementalForest() {
        this(0);
    }

    /**
     * n 個の独立した頂点を持つ IncrementalForest を構築する。
     * 計算量: O(n)
     * @param n 初期頂点数
     */
    public IncrementalForest(int n) {
        makeToDbl(n);
        for (int i = 0; i < n; i++) {
            addNode();
        }
    }

    /**
     * 新しい頂点を独立した成分として追加する。
     * 計算量: O(1) (ならし計算量、内部配列の再確保を除く)
     * @return 追加された頂点のインデックス
     */
    public int addNode() {
        int v = numVertices();
        jump.add(-1);
        parent.add(-1);
        depth.add(0);
        child.add(-1);
        brother.add(-1);
        dsu.add(-1);
        makeToDbl(v + 1);
        return v;
    }

    /**
     * todbl 配列を x まで拡張
     * 計算量: O(x)
     */
    private void makeToDbl(int x) {
        while (todbl.length < x) {
            int z = todbl.length;
            boolean[] next = new boolean[z * 2 + 1];
            System.arraycopy(todbl, 0, next, 0, z);
            System.arraycopy(todbl, 0, next, z, z);
            next[z * 2] = true;
            todbl = next;
        }
    }

    /**
     * 現在の頂点数を返す。
     * 計算量: O(1)
     * @return 頂点数
     */
    public int numVertices() {
        return dsu.size();
    }

    /**
     * 頂点 u が属する連結成分の DSU 上の代表元（根）を返す。
     * パス圧縮を行う。
     * 計算量: ならし O(α(N))
     * @param u 頂点
     * @return 代表元
     */
    public int rootOf(int u) {
        if (dsu.get(u) < 0) return u;
        int r = rootOf(dsu.get(u));
        dsu.set(u, r);
        return r;
    }

    /**
     * 頂点 u と v が同じ連結成分に属するか判定する。
     * 計算量: ならし O(α(N))
     */
    public boolean areConnected(int u, int v) {
        return rootOf(u) == rootOf(v);
    }

    /**
     * 頂点 u が属する連結成分の頂点数を返す。
     * 計算量: ならし O(α(N))
     */
    public int componentSize(int u) {
        return -dsu.get(rootOf(u));
    }

    /**
     * 頂点 u と v の間に辺を張る。既に連結な場合は false を返す。
     * 常にサイズの小さい方の木を再構築し、大きい方の木の根に接続する（マージテク）。
     * 再構築の際、小さい方の木の全ての頂点について parent, jump, depth 等を更新する。
     * 計算量: ならし O(log N)
     * @param u 頂点1
     * @param v 頂点2
     * @return 追加に成功したならtrue/すでに連結ならfalse
     */
    public boolean addEdge(int u, int v) {
        int ru = rootOf(u);
        int rv = rootOf(v);
        if (ru == rv) return false;

        // Union by size: u の属する成分が常に大きくなるようにする
        if (-dsu.get(ru) < -dsu.get(rv)) {
            int tmp = u; u = v; v = tmp;
            tmp = ru; ru = rv; rv = tmp;
        }

        int sizeV = -dsu.get(rv);
        int[] bfs = new int[sizeV];
        dsu.set(ru, dsu.get(ru) + dsu.get(rv));
        dsu.set(rv, ru);

        int bfsp = 0;

        // 頂点 v から元の木の根に向かってパスを遡り、辺の向きを反転させながら再構築する。
        // これにより頂点 v が新しい（接続前の）木の根になる。
        int p = u;
        int p2 = v;
        // v の子を BFS キューに追加
        for (int x = child.get(v); x >= 0; x = brother.get(x)) {
            bfs[bfsp++] = x;
        }

        // 矢印は親を指す
		// 　　　
		//  　　[ rv ] ((接げる小さい木の根)
		//    ▲
		//    │
		//    ▲
		//    │
		//    [ p3 ] 
		//   　▲
		//    │
		//    [ p2 ] (最初は、追加辺の端点のうち小さい方の木の方 v)
		//    ▲
		//    │
		//    [ p  ] (最初は、追加辺の端点のうち大きい方の木の方 u)
		//    ▲
		//    │
		//    ▲
		//    │
		//    [ v ] (接げるときの根)
        
        while (parent.get(p2) >= 0) {
            int p3 = parent.get(p2);
            int prevChild = -1;
            int childOfP3 = child.get(p3);
            while (childOfP3 >= 0) {
                int nextChild = brother.get(childOfP3);
                if (childOfP3 == p2) {//p3の子からp2を消している
                    if (prevChild < 0) {
                        child.set(p3, nextChild);
                    } else {
                        brother.set(prevChild, nextChild);
                    }

                    // p2 を p の子にする（逆転）
                    brother.set(p2, child.get(p));
                    child.set(p, p2);
                    setParent(p2, p);
                } else {
                    // p2 以外の兄弟は、後で depth や jump を更新するために BFS に入れる
                    bfs[bfsp++] = childOfP3;
                    prevChild = childOfP3;
                }
                childOfP3 = nextChild;
            }
            p = p2;
            p2 = p3;
        }
        
        // p2=rv
        brother.set(p2, child.get(p));
        child.set(p, p2);
        setParent(p2, p);

        // 再構成された成分内の全頂点について、上から順に parent 等の情報を再計算する
        for (int i = 0; i < bfsp; i++) {
            int w = bfs[i];
            setParent(w, parent.get(w));
            for (int x = child.get(w); x >= 0; x = brother.get(x)) {
                if (bfsp < sizeV) {
                    bfs[bfsp++] = x;
                }
            }
        }
        return true;
    }

    /**
     * 頂点 v の親を p に設定し、付随する属性を更新する。
     * 計算量: O(1)
     */
    private void setParent(int v, int p) {
        parent.set(v, p);
        int d = depth.get(p) + 1;
        depth.set(v, d);
        // Jump Pointer の計算: Binary Lifting のエッセンス
        jump.set(v, todbl[d - 1] ? jump.get(jump.get(p)) : p);
    }

    /**
     * 頂点 u の親を返す。
     * 計算量: O(1)
     */
    public int parentOf(int u) {
        return parent.get(u);
    }

    /**
     * 頂点 u の根からの深さを返す。
     * 計算量: O(1)
     */
    public int depth(int u) {
        return depth.get(u);
    }

    /**
     * 頂点 u と v の最近共通祖先（LCA）を返す。
     * 計算量: O(log N)
     */
    public int lca(int u, int v) {
        if (!areConnected(u, v)) return -1;
        if (depth.get(u) < depth.get(v)) {
            int tmp = u; u = v; v = tmp;
        }
        int dv = depth.get(v);
        // 深さを揃える
        while (depth.get(u) != dv) {
            int j = jump.get(u);
            u = (depth.get(j) >= dv ? j : parent.get(u));
        }
        // 二分探索的に遡上
        while (u != v) {
            int ju = jump.get(u);
            int jv = jump.get(v);
            if (ju != jv) {
                u = ju;
                v = jv;
            } else {
                u = parent.get(u);
                v = parent.get(v);
            }
        }
        return u;
    }

    /**
     * 3頂点 u, v, w によって形成される木のジャンクション（中心頂点）を返す。
     * 数学的には LCA(u,v), LCA(v,w), LCA(w,u) のうち、他と異なる1つ（または全て同じならその点）である。
     * XOR 合成によって計算可能。
     * 計算量: O(log N)
     */
    public int middle(int u, int v, int w) {
        if (!areConnected(u, v) || !areConnected(v, w)) return -1;
        return lca(u, v) ^ lca(v, w) ^ lca(w, u);
    }

    /**
     * 頂点 u と v の間の距離（パスに含まれる辺の数）を返す。u,vが非連結なら-1。
     * 計算量: O(log N)
     */
    public int dist(int u, int v) {
        if (!areConnected(u, v)) return -1;
        return depth.get(u) - depth.get(lca(u, v)) * 2 + depth.get(v);
    }

    /**
     * 頂点 u の祖先で、深さが d である頂点を返す。
     * 計算量: O(log N)
     */
    public int la(int u, int d) {
        if (d < 0 || depth.get(u) < d) return -1;
        while (depth.get(u) > d) {
            int j = jump.get(u);
            u = (depth.get(j) >= d ? j : parent.get(u));
        }
        return u;
    }

    /**
     * 頂点 from から to へ向かうパス上で、from からの距離が d である頂点を返す。
     * 計算量: O(log N)
     */
    public int la(int from, int to, int d) {
        int q = dist(from, to);
        if (q == -1 || d < 0 || q < d) return -1;
        if (depth.get(from) - d > depth.get(to) - (q - d)) return la(from, depth.get(from) - d);
        return la(to, depth.get(to) - (q - d));
    }

    /**
     * 頂点 v の直接の子頂点を列挙する Iterable を返す。
     * 計算量: 子の数に対する線形時間
     */
    public Iterable<Integer> children(int v) {
        return () -> new Iterator<Integer>() {
            int cur = child.get(v);
            @Override
            public boolean hasNext() {
                return cur >= 0;
            }
            @Override
            public Integer next() {
                int res = cur;
                cur = brother.get(cur);
                return res;
            }
        };
    }
}
