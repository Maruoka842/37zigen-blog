package library.util.graph.grid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import library.tools.FastScanner;
import library.util.ArrayUtils;
/***
 * obstacleの初期文字は '#'
 * emptyの初期文字は '.'
 */
public class Grid2D {
	public int H=-1;
	public int W=-1;
	public char[][]map;
	public char obstacle = '#';
	public char empty = '.';
	public class Edge {
		public int i, j;
		public char c;
		public int di, dj;
		
		public Edge(int i, int j, char c, int dh, int dw) {
			this.i = i;
			this.j = j;
			this.c = c;
			this.di = dh;
			this.dj = dw;
		}
		
		@Override
		public String toString() {
			return "("+i+","+j+")";
			
		}
		
		public int id() {
			return i*W+j;
		}
	}
	
	Grid2DVertex vertexArray[]; 
	public static int[]dh = new int[] {1,0,-1,0};//反時計回り
	public static int[]dw = new int[] {0,-1,0,1};
	/**
	 * H, Wがでかいときにcontainsが呼べるようにadjなどを宣言しない
	 * @param H
	 * @param W
	 */
	public Grid2D(int H, int W) {
		this.H = H;
		this.W = W;
	}
	
	public Grid2DVertex[] vertices() {
		return vertexArray;
	}
	
	@SuppressWarnings("unchecked")
	public static Grid2D read(int H, int W) {
		FastScanner sc = FastScanner.getInstance();
		Grid2D g = new Grid2D(H, W);
		g.map = new char[H][W];
		for (int i = 0; i < H; ++i) {
			g.map[i] = sc.next().toCharArray();
		}
		g.vertexArray = new Grid2DVertex[H * W];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				g.vertexArray[i + j * H] = new Grid2DVertex(i, j, g.map[i][j], g);
			}
		}
		
		return g;
	}
	
	
	
	public Grid2D(char[][] a) {
		this.H=a.length;
		this.W=a[0].length;
		map = ArrayUtils.copy(a);
		vertexArray = new Grid2DVertex[H * W];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				vertexArray[i + j * H] = new Grid2DVertex(i, j, map[i][j], this);
			}
		}
	}
	
    public Iterable<Edge> adj(Grid2DVertex v) {
    	return adj(v.i, v.j);
    }
	
	/***
	 * (h, w) に隣接する高々4マス (i, j) への Edge の Iterator を返す。
	 * @param h
	 * @param w
	 * @return
	 */
    public Iterable<Edge> adj(int i, int j) {
        return () -> new Iterator<Edge>() {
            int dir = 0;
            @Override public boolean hasNext() {
                while (dir < 4) {
                    int ni = i + dh[dir], nj = j + dw[dir];
                    if (0 <= ni && ni < H && 0 <= nj && nj < W) return true;
                    dir++;
                }
                return false;
            }
            @Override public Edge next() {
                int ni = i + dh[dir], nj = j + dw[dir];
                Edge e = new Edge(ni, nj, map[ni][nj], dh[dir], dw[dir]);
                dir++;
                return e;
            }
        };
    }
	
	
	public static nonCharEdge[] adjacentOnInfiniteGrid(int h, int w) {
		nonCharEdge[] ret=new nonCharEdge[4];
		for (int k = 0; k < 4; k++) {
			int nh=h+dh[k];
			int nw=w+dw[k];
			ret[k]=new nonCharEdge(nh, nw, dh[k], dw[k]);
		}
		return ret;
	}
	
	public static record nonCharEdge(int i, int j, int di, int dj) {
	}
	
	/***
	 * (h, w) に隣接する高々4マス (i, j) で empty であるものへの Edge の Iterable を返す。
	 * @param h
	 * @param w
	 * @return
	 */
	/***
	 * (h, w) に隣接する高々4マス (i, j) への Edge の Iterator を返す。
	 * @param i
	 * @param j
	 * @return
	 */
    public Iterable<Edge> adjacentEmpties(int i, int j) {
        return () -> new Iterator<Edge>() {
            int dir = 0;
            @Override public boolean hasNext() {
                while (dir < 4) {
                    int ni = i + dh[dir], nj = j + dw[dir];
                    if (0 <= ni && ni < H && 0 <= nj && nj < W && map[ni][nj] == empty) return true;
                    dir++;
                }
                return false;
            }
            @Override public Edge next() {
                int ni = i + dh[dir], nj = j + dw[dir];
                Edge e = new Edge(ni, nj, map[ni][nj], dh[dir], dw[dir]);
                dir++;
                return e;
            }
        };
    }
	
	public int[] indexOf(char c) {
		for (int i = 0; i < H; ++i) {
			for (int j = 0; j < W; ++j) {
				if (map[i][j] == c) return new int[] {i, j};
			}
		}
		return null;
	}
	
	public Grid2DVertex indexOf(char c, int i, int j, int di, int dj) {
		while (contains(i, j)) {
			if (map[i][j] == c) {
				return new Grid2DVertex(i, j, c, null);
			}
			i += di;
			j += dj;
		}
		return new Grid2DVertex(i, j, '無', null);
	}
	
	public boolean contains(int i, int j) {
		return 0 <= i && i < H && 0 <= j && j < W;
	}
	
	public boolean containsAndNotobstacle(int i, int j) {
		return 0 <= i && i < H && 0 <= j && j < W && map[i][j] != obstacle;
	}
	
	public boolean isObstacle(int i, int j) {
		return map[i][j] == obstacle;
	}
	
	/***
	 * (i, j) からの最短距離を返す。
	 * 到達不能な場合、距離Integer.MAX_VALUEとする。
	 * @param i
	 * @param j
	 * @return
	 */
	public int[][] bfs(int i, int j) {
		Queue<int[]>que=new ArrayDeque<>();
		int[][]dist=new int[H][W];
		int INF=Integer.MAX_VALUE;
		ArrayUtils.fill(dist, INF);
		dist[i][j] = 0;
		que.add(new int[] {i, j});
		while(!que.isEmpty()) {
			int[] xy=que.poll();
			for(Edge e:adj(xy[0], xy[1])) {
				if(dist[e.i][e.j]==INF && map[e.i][e.j]!=obstacle) {
					dist[e.i][e.j]=dist[xy[0]][xy[1]]+1;
					que.add(new int[] {e.i, e.j});
				}
			}
		}
		return dist;
	}

	
	
	/***
	 * (si, sj) からの最短距離を返す。
	 * ただし滑る床で一度進むと障害物にあたるまで止まれない。
	 * 移動中に止まらずに通過した場合、その時点でその方向から到着したとして
	 * 方向ごとに到着距離を求める。
	 * 初期状態は(si, sj)上をある方向に移動を開始しているとして距離１とする。
	 * また、距離は方向転換を一回するごとに１つ増えるとする。
	 * 到達不能な場合、距離Integer.MAX_VALUE/3とする。
	 * dist[dir][i][j]=dir方向に滑っている状態で(i,j)上を通過するのに必要なコスト　を返す。
	 * マップの範囲外は全て壁とする。
	 * @param i
	 * @param j
	 * @return
	 */
	public int[][][] bfs_icefloor(int si, int sj) {
		ArrayDeque<int[]>dq=new ArrayDeque<>();
		int[][][]dist=new int[4][H][W];
		int INF=Integer.MAX_VALUE/3;
		ArrayUtils.fill(dist, INF);
		for (int dir = 0; dir < 4; dir++) {
			dist[dir][si][sj]=1;
			dq.add(new int[] {dir, si, sj});
		}
		while(!dq.isEmpty()) {
			int[] state=dq.pollFirst();
			int dir=state[0];
			int x=state[1];
			int y=state[2];
			if(contains(x+dh[dir], y+dw[dir]) && map[x+dh[dir]][y+dw[dir]]!=obstacle) {
				if (dist[dir][x+dh[dir]][y+dw[dir]]==INF) {
					dist[dir][x+dh[dir]][y+dw[dir]] = dist[dir][x][y];
					dq.addFirst(new int[] {dir, x+dh[dir],y+dw[dir]});
				}
			} else {
				for (int ndir = 0; ndir < 4; ndir++) {
					if (ndir == dir) continue;
					if(dist[ndir][x][y]==INF) {
						dist[ndir][x][y] = 1+dist[dir][x][y];
						dq.addLast(new int[] {ndir, x, y});
					}					
				}
			}
		}
		return dist;
	}

	
	
	public record Rectangle(int leftInclusive, int rightExclusive, long height) {}
	
	/***
	 * 与えられたヒストグラムaに対して極大な長方形を重複なく列挙する。
	 * https://atcoder.jp/contests/abc311/submissions/72286022
	 * @param a
	 * @return
	 */
	public static ArrayList<Rectangle> enumerateMaximalRectangles(long[] a) {
		Deque<Integer> stk = new ArrayDeque<>();
		int[] left = new int[a.length];//高さa[i]を保ったままiからleft[i]まで動かせる。
		int[] right = new int[a.length];//高さa[i]を保ったままiからright[i]まで動かせる。
		for (int i = 0; i < a.length; ++i) {
			while (!stk.isEmpty() && a[stk.peekFirst()] >= a[i]) stk.removeFirst();
			left[i] = stk.isEmpty() ? 0 : (stk.peekFirst() + 1);
			stk.addFirst(i);
		}
		stk.clear();
		ArrayList<Rectangle> ret=new ArrayList<>();
		for (int i = a.length - 1; i >= 0; --i) {
			//高さa[i]で(i, a[i])を含む極大な長方形をretに追加。ただし、同じ高さのバーがあった場合(eq真)、既に登録されているのでスキップ。
			boolean eq = false;
			while (!stk.isEmpty() && a[stk.peekFirst()] >= a[i]) {
				eq |= a[i] == a[stk.peekFirst()];
				stk.removeFirst();
			}
			right[i] = stk.isEmpty() ? a.length - 1 : (stk.peekFirst() - 1);
			stk.addFirst(i);
			if (!eq) {
				ret.add(new Rectangle(left[i], right[i]+1, a[i]));
			}
		}
		return ret;
	}
	
    public Iterator<Grid2DVertex> vertexIterator() {
        return new Iterator<Grid2DVertex>() {
            int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < H * W;
            }

            @Override
            public Grid2DVertex next() {
                if (!hasNext()) throw new NoSuchElementException();
                Grid2DVertex ret = vertexArray[idx];
                idx++;
                return ret;
            }
        };
    }
	
	
	@Override
	public String toString() {
		String ret="";
		for(int i=0;i<map.length;++i) {
			ret=ret+String.valueOf(map[i])+"\n";
		}
		return ret;
	}
	
	/**
	 * dp[i][j][k]=(dh[i],dw[i])方向に(j,k)から何回移動できるかを返す。計算量O(HW)。
	 * 下、上、右、左の順。
	 * (dh[i],dw[i])=(1,0),(-1,0),(0,1),(0,-1)
	 * @return
	 * https://atcoder.jp/contests/abc443/submissions/72918654
	 */
	public int[][][]maximalMove() {
		int[][][]dp=new int[4][H][W];
		for (int i = H - 2; i >= 0; --i) {
			for (int j = 0; j < W; ++j) {
				if (map[i][j] == empty) {
					if (map[i+1][j] == empty) {
						dp[0][i][j]=dp[0][i+1][j]+1;
					}
				}
			}
		}
		for (int i = 1; i < H; ++i) {
			for (int j = 0; j < W; j++) {
				if (map[i][j] == empty) {
					if (map[i-1][j] == empty) {
						dp[1][i][j]=dp[1][i-1][j]+1;
					}
				}
			}
		}
		
		for (int i = 0; i < H; ++i) {
			for (int j = W - 2; j >= 0; --j) {
				if (map[i][j] == empty) {
					if (map[i][j+1] == empty) {
						dp[2][i][j]=dp[2][i][j+1]+1;
					}
				}
			}
		}
		
		for (int i = 0; i < H; ++i) {
			for (int j = 1; j < W; ++j) {
				if (map[i][j] == empty) {
					if (map[i][j-1] == empty) {
						dp[3][i][j]=dp[3][i][j-1]+1;
					}
				}
			}
		}
		return dp;
		
	}

	/**
	 * n x m の格子グラフ全体をちょうど 1 回ずつ通る Hamilton path を、始点 (x0, y0) から終点 (x1, y1) へ構成する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 構成に成功した場合は 'L', 'R', 'U', 'D' からなる移動列。存在しない場合は null
	 * @throws AssertionError 存在するはずなのに構成や復元に失敗した場合
	 * Itai, Alon, Christos H. Papadimitriou, and Jayme Luiz Szwarcfiter. "Hamilton paths in grid graphs." SIAM Journal on Computing 11.4 (1982): 676-686.
	 */
	public static char[] hamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		return GridHamiltonPath.hamiltonPath(x0, y0, x1, y1, n, m);
	}
	/**
	 * n x m の KingMove グラフ全体をちょうど 1 回ずつ通る Hamilton path を構成する。
	 * 隣り合う 2 頂点 (r0, c0), (r1, c1) は常に max(|r0-r1|, |c0-c1|) == 1 を満たす。
	 *
	 * @param n 行数
	 * @param m 列数
	 * @return path[k] = {row, col} で表される長さ n*m の頂点列。n <= 0 または m <= 0 の場合は空配列
	 */
	public static int[][] kingMoveHamiltonPath(int n, int m) {
		return KingMoveHamiltonPath.kingMoveHamiltonPath(n, m);
	}

	/**
	 * n x m の KingMove グラフ全体をちょうど 1 回ずつ通る Hamilton path を、始点 (x0, y0) から終点 (x1, y1) へ構成する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return path[k] = {row, col} で表される長さ n*m の頂点列。存在しない場合は null
	 */
	public static int[][] kingMoveHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		return KingMoveHamiltonPath.kingMoveHamiltonPath(x0, y0, x1, y1, n, m);
	}

	/**
	 * n x m の KingMove グラフで、始点 (x0, y0) から終点 (x1, y1) へ全頂点をちょうど 1 回ずつ通る
	 * Hamilton path が存在するかどうかを判定する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 条件を満たす Hamilton path が存在するとき true、存在しないとき false
	 */
	public static boolean existsKingMoveHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		return KingMoveHamiltonPath.existsKingMoveHamiltonPath(x0, y0, x1, y1, n, m);
	}
	/**
	 * n x m の格子グラフにおいて、始点 (x0, y0) から終点 (x1, y1) へ全頂点をちょうど 1 回ずつ通る
	 * Hamilton path が存在するかどうかを判定する。
	 *
	 * @param x0 始点の行
	 * @param y0 始点の列
	 * @param x1 終点の行
	 * @param y1 終点の列
	 * @param n 行数
	 * @param m 列数
	 * @return 条件を満たす Hamilton path が存在するとき true、存在しないとき false
	 * Itai, Alon, Christos H. Papadimitriou, and Jayme Luiz Szwarcfiter. "Hamilton paths in grid graphs." SIAM Journal on Computing 11.4 (1982): 676-686.
	 */
	public static boolean existsHamiltonPath(int x0, int y0, int x1, int y1, int n, int m) {
		return GridHamiltonPath.existsHamiltonPath(x0, y0, x1, y1, n, m);
	}
}
