package library.util.segtree;

import library.util.ArrayUtils;
import library.util.monoid.BinaryInversionState;

/**
 *  null は identity の代わり
 */
public class SegTreeFactory {
	static class State2ndMax {
        long max;

        long max2;

        int cnt;

        int cnt2;

        public State2ndMax(long v) {
            max = v;
            max2 = Long.MIN_VALUE;
            cnt = 1;
            cnt2 = 0;
        }

        public State2ndMax(long max, long max2, int cnt, int cnt2) {
            this.max = max;
            this.max2 = max2;
            this.cnt = cnt;
            this.cnt2 = cnt2;
        }
    }
	
	public static SegTree<State2ndMax> count2ndMax(int n) {
        var seg = new SegTree<State2ndMax>(n, (x, y) -> {
            long max = Math.max(x.max, y.max);
            int cnt = 0;
            if (x.max == max) {
                cnt += x.cnt;
            }
            if (y.max == max) {
                cnt += y.cnt;
            }
            long max2 = Long.MIN_VALUE;
            for (long v : new long[]{ x.max, x.max2, y.max, y.max2 }) {
                if (v != max) {
                    max2 = Math.max(max2, v);
                }
            }
            int cnt2 = 0;
            if (x.max == max2) {
                cnt2 += x.cnt;
            }
            if (x.max2 == max2) {
                cnt2 += x.cnt2;
            }
            if (y.max == max2) {
                cnt2 += y.cnt;
            }
            if (y.max2 == max2) {
                cnt2 += y.cnt2;
            }
            return new State2ndMax(max, max2, cnt, cnt2);
        });
        return seg;
	}
	
	/**
	 * 各要素 {@code x} に対して、作用 {@code f(x) = a * x + b} を適用する。
	 * 作用は {@code long[] {b, a}} で表す。
	 * @param N 要素数
	 * @param mod 法
	 * @return 区間 affine 変換・区間和取得用の遅延セグメント木
	 */
	public static LazySegTree<long[], Long> affine_modSum(int N, long mod) {
		//https://atcoder.jp/contests/abc332/submissions/71399167
    	var seg=new LazySegTree<long[], Long>(N, new LazySegTreeStrategy<long[], Long>() {
			final long[] identityA = new long[] {0, 1};

			@Override
			public Long identityX() {
				return 0L;
			}

			@Override
			public Long mergeX(Long a, Long b) {
				return (a + b) % mod;
			}

			@Override
			public long[] identityA() {
				return identityA;
			}

			@Override
			public long[] mergeA(long[] newer, long[] older) {
				return new long[] {(newer[1] * older[0] + newer[0]) % mod, newer[1] * older[1] % mod};
			}

			@Override
			public Long mergeAX(long[] a, Long x) {
				return (a[0] + a[1] * x) % mod;
			}
		});
    	return seg;
	}
	
	
	
	/**
	 * 複数のminargがある場合、最小を返す。
	 * @param n
	 * @return
	 */
    public static AddArgmin add_Argmin(int n) {
    	return new AddArgmin(n);
    }
    
    public static LazySegTree<Long, long[]> add_countMin(int N) {
    	return new LazySegTree<Long, long[]>(N, new LazySegTreeStrategy<Long, long[]>() {

			@Override
			public long[] identityX() {
				return new long[] {Long.MAX_VALUE/3, 0};
			}

			@Override
			public long[] mergeX(long[] a, long[] b) {//long[]{min, minの個数}
				if(a[0]==b[0]) {
					return new long[] {a[0], a[1]+b[1]};
				} else if (a[0] < b[0]) {
					return new long[] {a[0], a[1]};
				} else {
					return new long[] {b[0], b[1]};
				}
			}

			@Override
			public Long identityA() {
				return 0L;
			}

			@Override
			public Long mergeA(Long newer, Long older) {
				return newer + older;
			}

			@Override
			public long[] mergeAX(Long a, long[] x) {
				return new long[] {x[0] + a, x[1]};
			}
			
		});
    }
    
	/**
	 * minの個数を返す。
	 * @param n
	 * @return
	 */
    public static Add_CountMax add_countMax(int n) {
    	return new Add_CountMax(n);
    }
    
    /**
     * 複数のmaxargがある場合、最小を返す。初期値は-∞,-∞,-∞,...
     * オーバーフロー対策している
     * @param n
     * @return
     */
    public static Safeadd_Argmax safeadd_Argmax(int n) {
    	return new Safeadd_Argmax(n);
    }
    
    /**
     * 複数のmaxargがある場合、最小を返す。初期値は-∞,-∞,-∞,...
     * オーバーフロー対策していない
     * @param n
     * @return
     */
    public static Add_Argmax add_Argmax(int n) {
    	return new Add_Argmax(n);
    }
    
    public static RangeAddPointGetBinaryIndexedTree add_pointget(int n) {
    	return new RangeAddPointGetBinaryIndexedTree(n);
    }

    /**
     * -∞で初期化
     * @param n
     * @return
     */
    public static LazySegTreelonglong add_min(int n) {
    	var seg=new LazySegTreelonglong(n, Long::sum, Long::min, Long::sum, 0L, Long.MAX_VALUE);
    	return seg;
    }
    
    
    public static LazySegTreelonglong add_max(int n) {
    	var seg=new LazySegTreelonglong(n, Long::sum, Long::max, Long::sum, 0L, Long.MIN_VALUE);
    	return seg;
    }
    
    
    public static LazySegTreelonglong add_max(int n, long initialValue) {
    	var seg=new LazySegTreelonglong(n, Long::sum, Long::max, Long::sum, 0L, Long.MIN_VALUE);
    	seg.fill(initialValue);
    	return seg;
    }
    
    public static SegTree<long[][]> matrixConvolutionZZn(int N, long mod) {
    	var tree=new SegTree<long[][]>(N, (x, y)->{
	    	if (x[0].length!=y.length)throw new AssertionError();
	    	long[][]z=new long[x.length][y[0].length];
	    	for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < y[0].length; j++) {
					for (int k = 0; k < x[i].length; k++) {
						z[i][j]+=x[i][k]*y[k][j];
						z[i][j]%=mod;
					}
				}
			}
	    	return z;
	    });
        return tree;
    }
    

    /**
     * INF=Long.MAX_VALUE/2
     * @param n
     * @return
     */
    public static SegTree<long[][]> minplusMatrixConvolution(int n) {
        var tree=new SegTree<long[][]>(n, (x, y)->{
        	if (x[0].length!=y.length)throw new AssertionError();
        	long[][]z=new long[x.length][y[0].length];
        	ArrayUtils.fill(z, Long.MAX_VALUE/2);
        	for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < y[0].length; j++) {
					for (int k = 0; k < x[i].length; k++) {
						z[i][j]=Math.min(z[i][j], x[i][k]+y[k][j]);
					}
				}
			}
        	return z;
        });
        return tree;
    }
    

    public static IntSumBinaryIndexedTree intsum(int n) {
    	//https://atcoder.jp/contests/abc426/submissions/74055406
    	//セグ木だとTLEしたのでBITに置き換え
    	var tree=new IntSumBinaryIndexedTree(n);
    	return tree;
    }

    /**
     * 0で初期化
     * @param n
     * @return
     */
    public static SegTreelong sum(int n) {
        var tree=new SegTreelong(n, Long::sum, 0L);
        return tree;
    }
    
   /**
    * 0で初期化
    * @param n
    * @return
    */
   public static RangeAddRangeSum add_sum(int n) {
	   return new RangeAddRangeSum(n);
   }

   /**
    * 重み付き区間加算・重み付き区間和取得を行う構造を生成する。
    * @param weights 各要素の重み c_i
    * @return 重み付き区間加算・区間和取得
    */
   public static RangeAddWeightedSum add_weightedSum(long[] weights) {
	   return new RangeAddWeightedSum(weights);
   }

   /**
    * 区間加算・区間 2 乗和取得を法 {@code mod} で行う遅延セグメント木を生成する。
    * 各要素の 0 乗和（要素数）、1 乗和（合計）、2 乗和（ 2 乗の合計）を保持する。
    *
    * @param n 要素数
    * @param mod 法
    * @return 区間加算・区間 2 乗和取得用の遅延セグメント木
    */
   public static LazySegTree<Long, long[]> modAdd_squaredSum(int n, long mod) {
       var seg = new LazySegTree<>(n, new LazySegTreeStrategy<Long, long[]>() {
           @Override
           public long[] identityX() {
               return new long[3];
           }

           @Override
           public long[] mergeX(long[] a, long[] b) {
               long[] ret = new long[3];
               ret[0] = a[0] + b[0];
               ret[1] = (a[1] + b[1]) % mod;
               ret[2] = (a[2] + b[2]) % mod;
               return ret;
           }

           @Override
           public Long identityA() {
               return 0L;
           }

           @Override
           public Long mergeA(Long newer, Long older) {
               return (newer + older) % mod;
           }

           @Override
           public long[] mergeAX(Long a, long[] x) {
               long[] ret = new long[3];
               // ret[0]=0乗和
               // ret[1]=1乗和
               // ret[2]=2乗和
               ret[0] = x[0];
               ret[1] = (x[1] + (ret[0] * a)) % mod;
               ret[2] = ((x[2] + (((a * a) % mod) * x[0])) + (((2 * a) % mod) * x[1])) % mod;
               return ret;
           }
       });
       return seg;
   }
   
   /**
    * 0で初期化
    * @param n
    * @return
    */
   public static RangeAddRangeModSum modAdd_modSum(int n, long mod) {
	   return new RangeAddRangeModSum(n, mod);
   }
    
    public static SegTreelong modSum(int n, final long mod) {
//        var tree=new SegTree<Long>(n, (a, b)->(a+b)%mod, 0L);
        return new SegTreelong(n, (a, b)->(a+b)%mod, 0L);
    }
    
    public static MaxSeg max(int n) {
    	return new MaxSeg(n);
    }
    
    public static MinSeg min(int n) {
    	return new MinSeg(n);
    }

    /**
     * {@code set(i, v)} で {@code a[i] = v} を更新し、
     * {@code get(i)} で {@code min_j (|i-j| + a[j])} を返すデータ構造を生成する。
     * 初期値はすべて {@link L1DistanceMinQuery#INF}。
     *
     * @param n 配列長
     * @return L1 距離つき最小値クエリ構造
     */
    public static L1DistanceMinQuery l1DistanceMinQuery(int n) {
    	return new L1DistanceMinQuery(n);
    }
    
    /**
     * identityX=Long.MAX_VALUE
     * @param n
     * @return
     */
    public static LazySegTreelonglong min_min(int n) {
//    	var tree=new LazySegTree<Long, Long>(n, Long::min, Long::min, Long::min, Long.MAX_VALUE);
    	var tree=new LazySegTreelonglong(n, Long::min, Long::min, Long::min, Long.MAX_VALUE, Long.MAX_VALUE);
    	return tree;
    }
    
    /**
     * rangeAssignに単位元がないので、Long.MAX_Valueを便宜上の単位元にしている。
     * @param n
     * @return
     */
    public static LazySegTreelonglong assign_min(int n) {
    	return new LazySegTreelonglong(n, (x, y) -> x, Long::min, (x, y) -> x, Long.MAX_VALUE, Long.MAX_VALUE);
    }
    
    /**
     * rangeAssignに単位元がないので、Long.MIN_VALUEを便宜上の単位元にしている。
     * @param n
     * @return
     */
    public static IndexedLazySegTreelonglong assign_sum(int n) {
    	return new IndexedLazySegTreelonglong(n, new IndexedLazySegTreeStrategy_longlong() {
			
			@Override
			public long mergeX(long a, long b, int l, int m, int r) {
				return a+b;
			}
			
			@Override
			public long mergeAX(long a, long x, int l, int r) {
				return a*(r-l);
			}
			
			@Override
			public long mergeA(long newer, long older) {
				return newer;
			}
			
			@Override
			public long identityX() {
				return 0;
			}
			
			@Override
			public long identityA() {
				return Long.MIN_VALUE;
			}
		});
    }

    /**
     * 重み付き区間更新・重み付き区間和取得を行う構造を生成する。
     * rangeAssignに単位元がないので、Long.MIN_VALUEを便宜上の単位元にしている。
     * @param weights 各要素の重み c_i
     * @return 重み付き区間更新・区間和取得
     */
    public static RangeAssignWeightedSum assign_weightedSum(long[] weights) {
        return new RangeAssignWeightedSum(weights);
    }
    
    /**
     * 重み付き区間更新・重み付き区間和取得を行う構造を生成する。
     * rangeAssignに単位元がないので、Long.MIN_VALUEを便宜上の単位元にしている。
     * @param weights 各要素の重み c_i
     * @return 重み付き区間更新・区間和取得
     */
    public static RangeAssignWeightedModSum assign_weightedModSum(long[] weights, long mod) {
        return new RangeAssignWeightedModSum(weights, mod);
    }
    
    
    /**
     * 初期値True
     * @param n
     * @return
     */
    public static SegTree<Boolean> logicalAnd(int n) {
        return new SegTree<Boolean>(n, Boolean::logicalAnd, true);
    }
    
    public static RollingHash rollinghash(int n, long radix, long mod) {
    	return new RollingHash(n, radix, mod);
    }
    
    public static LazySegTreelonglong modMul_sum(int n, long mod) {
    	//https://atcoder.jp/contests/abc450/submissions/74321230
		var seg=new LazySegTreelonglong(n, new LazySegTreeStrategy_longlong() {
			
			@Override
			public long mergeX(long a, long b) {
				long ret = a + b;
				if (ret >= mod) ret -= mod;
				return ret;
			}
			
			@Override
			public long mergeAX(long a, long x) {
				return a * x % mod;
			}
			
			@Override
			public long mergeA(long newer, long older) {
				return newer * older % mod;
			}
			
			@Override
			public long identityX() {
				return 0;
			}
			
			@Override
			public long identityA() {
				return 1;
			}
		});
		return seg;
    }

	/**
	 * 区間 01 反転・区間反転数取得用の遅延セグメント木を生成する。
	 * 未テスト。
	 * 計算量: O(N)
	 * @param N 要素数
	 * @return 遅延セグメント木
	 */
	public static LazySegTree<Boolean, BinaryInversionState> flip_inversionCount(int N) {
		return new LazySegTree<>(N, new LazySegTreeStrategy<Boolean, BinaryInversionState>() {
			@Override
			public BinaryInversionState identityX() {
				return BinaryInversionState.STRATEGY.identity();
			}

			@Override
			public BinaryInversionState mergeX(BinaryInversionState a, BinaryInversionState b) {
				return a.mul(b);
			}

			@Override
			public Boolean identityA() {
				return false;
			}

			@Override
			public Boolean mergeA(Boolean newer, Boolean older) {
				return newer ? !older : older;
			}

			@Override
			public BinaryInversionState mergeAX(Boolean a, BinaryInversionState x) {
				return a ? x.flip() : x;
			}
		});
	}

}
