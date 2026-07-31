package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 4近傍のマス目塗り分け問題を解くFrontier DPの高速化と破壊的変更を検証するテストクラス。
 *
 * @author Jules
 */
public class SunukeGridColoringTest {

    /**
     * H x W グリッドの左上 (0,0) と右下 (H-1, W-1) を黒で結ぶ塗り分けの数を計算します。
     * 計算量: O(H_transposed * W_transposed * (状態数))
     * ここで W_transposed = min(H, W) <= 6 となるため、状態数は高々数百に抑えられます。
     *
     * @param inputW グリッドの列数
     * @param inputH グリッドの行数
     * @return 適合パターンの個数 (mod 1,000,000,007)
     */
    // 未テスト
    public long solveTransposed(int inputW, int inputH) {
        // 4近傍のグリッド連結性は転置に対して完全に同型であるため、
        // 状態数（輪郭線の幅）を決定する幅 W が常に最小となるように転置（Swap）します。
        int W = Math.min(inputW, inputH);
        int H = Math.max(inputW, inputH);

        Frontier4 initialFrontier = Frontier4.getInitialState(W);
        initialFrontier = initialFrontier.startVertex(1).build();
        var dp = new HashMap<Frontier4, Long>();
        dp.put(initialFrontier, 1L);
        long mod = (long) 1e9 + 7;
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                if (i == 0 && j == 0) continue;
                var ndp = new HashMap<Frontier4, Long>();
                for (var es : dp.entrySet()) {
                    {
                        // 黒マスの選択肢（新頂点を開始してUP/LEFTと接続）
                        var builder = es.getKey().startVertex();
                        builder.connect(Frontier4.Direction.UP);
                        builder.connect(Frontier4.Direction.LEFT);
                        var next = builder.build();
                        // chminDeadEndsおよびchminCyclesが破壊的（破壊的変更）になったため、
                        // 戻り値を代入せずに副作用として呼び出すだけで正しく動作します。
                        next.chminCycles(0);
                        next.chminDeadEnds(0);
                        if (next.tagOrSum() == 1)
                            ndp.merge(next, es.getValue(), (x, y) -> (x + y) % mod);
                    }
                    {
                        // 白マスの選択肢（新頂点を配置しない）
                        var next = es.getKey().nextWithoutVertex();
                        next.chminCycles(0);
                        next.chminDeadEnds(0);
                        if (next.tagOrSum() == 1) {
                            ndp.merge(next, es.getValue(), (x, y) -> (x + y) % mod);
                        }
                    }
                }
                dp = ndp;
            }
        }
        long ans = 0;
        for (var x : dp.entrySet()) {
            var f = x.getKey();
            // 境界上に成分が存在し、右下（転置後の W - 1）に頂点があり、かつそれが (0,0) の成分（tag = 1）と接続している状態を計上
            if (f.getComponentCount() > 0 && f.hasVertex(W - 1) && f.getTagFromPos(W - 1) == 1) {
                ans += x.getValue();
                ans %= mod;
            }
        }
        return ans;
    }

    @Test
    public void testSunukeGridColoring() {
        // 転置DPによって極めて高速（数ミリ秒〜数百ミリ秒）に動作することを確認します。
        long start = System.currentTimeMillis();
        long ans = solveTransposed(100, 6);
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        System.out.println("100 x 6 answer: " + ans + " calculated in " + elapsed + " ms");

        // 100 x 6 の答えが正しく計算されていること
        assertEquals(184466177, ans);
        // 実行時間が十分に高速であること（1.5秒未満, 通常は500ms程度）
        assertTrue(elapsed < 1500, "Should complete within 1.5 seconds, took " + elapsed + " ms");
    }
}
