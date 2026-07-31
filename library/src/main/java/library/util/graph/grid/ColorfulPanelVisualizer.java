package library.util.graph.grid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Chokudai Contest 005 - Colorful Panel Visualizer
 */
public class ColorfulPanelVisualizer {
    /** グリッドのサイズ */
    private final int N;
    /** 色の数 */
    private final int K;
    /** グリッドの状態の履歴 */
    private final List<int[][]> history;
    /** 現在表示しているステップ */
    private int currentStep;
    /** 各ステップのタッチ操作 (y, x, c) */
    private final List<int[]> touches;
    /** 各ステップの操作説明 */
    private final List<String> operationDescriptions;

    /**
     * コンストラクタ
     * @param N グリッドサイズ
     * @param K 色数
     * @param initialGrid 初期状態のグリッド (0-indexed または 1-indexed の色はそのまま保持される)
     */
    public ColorfulPanelVisualizer(int N, int K, int[][] initialGrid) {
        this.N = N;
        this.K = K;
        this.history = new ArrayList<>();
        this.touches = new ArrayList<>();
        this.operationDescriptions = new ArrayList<>();
        this.history.add(copyGrid(initialGrid));
        this.touches.add(null); // Step 0 has no touch
        this.operationDescriptions.add(null);
        this.currentStep = 0;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] res = new int[N][N];
        for (int i = 0; i < N; i++) res[i] = grid[i].clone();
        return res;
    }

    /**
     * 指定した座標 (y, x) を色 c でタッチする操作を追加する。
     * <ul>
     *   <li>事前条件: {@code 0 <= y < N}, {@code 0 <= x < N}</li>
     *   <li>事後条件: 履歴の末尾のグリッドに対して (y, x) から始まる flood fill を行い、新しい状態を履歴に追加する。</li>
     *   <li>副作用: {@code history}, {@code touches} が更新される。</li>
     *   <li>計算量: $O(N^2)$</li>
     * </ul>
     * 未テスト
     * @param y 上からの座標 (0-indexed)
     * @param x 左からの座標 (0-indexed)
     * @param c 変更後の色
     */
    public void paintConnectedComponent(int y, int x, int c) {
        int[][] lastGrid = history.get(history.size() - 1);
        int[][] nextGrid = copyGrid(lastGrid);
        int oldColor = nextGrid[y][x];
        if (oldColor != c) {
            floodFill(nextGrid, y, x, oldColor, c);
        }
        history.add(nextGrid);
        touches.add(new int[]{y, x, c});
        operationDescriptions.add("Touch: (" + y + ", " + x + ") -> Color " + c);
    }

    /**
     * <ul>
     *   <li>事前条件: {@code 0 <= y0 <= y1 <= N}, {@code 0 <= x0 <= x1 <= N}</li>
     *   <li>事後条件: 追加されるグリッド G' は、直前のグリッド G に対して
     *       {@code y0 <= y < y1} かつ {@code x0 <= x < x1} なら {@code G'[y][x] = c}、
     *       それ以外なら {@code G'[y][x] = G[y][x]} を満たす。</li>
     *   <li>副作用: {@code history}, {@code touches} が更新される。</li>
     *   <li>計算量: $O(N^2 + (y1 - y0)(x1 - x0))$</li>
     * </ul>
     * 未テスト
     * @param y0 上端の座標 (0-indexed, inclusive)
     * @param x0 左端の座標 (0-indexed, inclusive)
     * @param y1 下端の座標 (0-indexed, exclusive)
     * @param x1 右端の座標 (0-indexed, exclusive)
     * @param c 変更後の色
     */
    public void paintRectangle(int y0, int x0, int y1, int x1, int c) {
        int[][] lastGrid = history.get(history.size() - 1);
        int[][] nextGrid = copyGrid(lastGrid);
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                nextGrid[y][x] = c;
            }
        }
        history.add(nextGrid);
        touches.add(null);
        operationDescriptions.add("Rectangle: [" + y0 + ", " + y1 + ") x [" + x0 + ", " + x1 + ") -> Color " + c);
    }

    private void floodFill(int[][] grid, int y, int x, int oldColor, int newColor) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{y, x});
        grid[y][x] = newColor;

        int[] dy = {1, -1, 0, 0};
        int[] dx = {0, 0, 1, -1};

        while (!stack.isEmpty()) {
            int[] curr = stack.pop();
            for (int i = 0; i < 4; i++) {
                int ny = curr[0] + dy[i];
                int nx = curr[1] + dx[i];
                if (ny >= 0 && ny < N && nx >= 0 && nx < N && grid[ny][nx] == oldColor) {
                    grid[ny][nx] = newColor;
                    stack.push(new int[]{ny, nx});
                }
            }
        }
    }

    /**
     * ビジュアライザを表示する。
     * 未テスト
     */
    public void draw() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chokudai Contest 005 Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            VisualPanel panel = new VisualPanel();
            frame.add(panel);

            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if (currentStep < history.size() - 1) {
                            currentStep++;
                            panel.repaint();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        if (currentStep > 0) {
                            currentStep--;
                            panel.repaint();
                        }
                    }
                }
            });

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private class VisualPanel extends JPanel {
        private static final int CELL_SIZE = 8;

        public VisualPanel() {
            setPreferredSize(new Dimension(N * CELL_SIZE + 200, N * CELL_SIZE + 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int[][] grid = history.get(currentStep);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    g.setColor(getColor(grid[i][j]));
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.LIGHT_GRAY);
                    // g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }

            g.setColor(Color.BLACK);
            g.drawString("Step: " + currentStep + " / " + (history.size() - 1), N * CELL_SIZE + 20, 30);
            String operationDescription = operationDescriptions.get(currentStep);
            if (operationDescription != null) {
                g.drawString(operationDescription, N * CELL_SIZE + 20, 50);
            } else {
                int[] touch = touches.get(currentStep);
                if (touch != null) {
                    g.drawString("Touch: (" + (touch[0]) + ", " + (touch[1]) + ") -> Color " + touch[2], N * CELL_SIZE + 20, 50);
                }
            }
            g.drawString("Left/Right arrows to navigate", N * CELL_SIZE + 20, 110);
        }

        private Color getColor(int c) {
        	if (c >= 9) throw new AssertionError();
            return switch (c) {
            	case 0 -> Color.WHITE;
            	case 1 -> Color.RED;
                case 2 -> Color.BLUE;
                case 3 -> Color.GREEN;
                case 4 -> Color.YELLOW;
                case 5 -> Color.MAGENTA;
                case 6 -> Color.CYAN;
                case 7 -> Color.ORANGE;
                case 8 -> Color.BLACK;
                default -> Color.WHITE;
            };
        }
    }
}
