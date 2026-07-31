package library.util.linalg;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.collections.IntArrayList;

public class GF2QuadraticForm {
	/**
	 * Σx[i]x[i+1]+y+(ab+a+b)の形に直す。xはpairに、yはsingleに、a,bはellに行ベクトルで格納して返す。
	 * @param adj
	 * @return
	 */
	public static Base canonicalForm(boolean[][] adj) {
		//https://atcoder.jp/contests/abc220/submissions/74193023
		// 3頂点サイクルのとき ab + bc + ca = (a + c)(b + c) + c
		// 4頂点サイクルの時 ab + bc + cd + da = (a + c)(b + d)
		// 4頂点完全グラフのとき a(b+c+d)+b(c+d)+cd = (a + c + d)(b + c + d) + c + d + cd
		// ad+ac+ab+bd+be
	    // = (a + d + e)(b + c + d) + ce + cd + d + de
	    // = (a + d + e)(b + c + d) + (c + d)(d + e)
    	// ce+ac+cd+ad+bd
    	//=(a+d+e)(c+d)+d+ed+bd
    	//=(a+d+e)(c+d)+(b+d+e)d
		//bd+be+ce+de=(b+e)(d+e)+e+ce=(b+e)(d+e)+e(e+c)

		int n=adj.length;
		boolean[][] base = new boolean[n][n];//i行目が基底のi番目のベクトル
		boolean[][] a = ArrayUtils.copy(adj);
		for (int i = 0; i < n; i++) {
			base[i][i]=true;
		}
		
		// base[i] が表す多項式を f[i] とすると
		// (元の式) = Σ a[i][j] f[i] f[j] for i <= j)
		
		boolean[] used = new boolean[n];
		IntArrayList pair = new IntArrayList();
		IntArrayList ellpair = new IntArrayList();
		for (int i = 0; i < n; i++) {
			if (used[i]) continue;
			int pivot = -1;
			for (int j = i + 1; j < n; j++) {
				if (used[j]) continue;
				if (a[i][j]) {
					pivot = j;
					break;
				}
			}
			if (pivot == -1) continue;
			
			used[i] = true;
			used[pivot] = true;
			
			IntArrayList L = new IntArrayList();
			IntArrayList R = new IntArrayList();
			for (int j = 0; j < n; j++) {
				if (used[j]) continue;
				if (a[i][j]) L.add(j);
				if (a[pivot][j]) R.add(j);
			}
			
			boolean flag0=a[i][i];
			boolean flag1=a[pivot][pivot];
			
			if (flag0 && flag1) {
				// y[i]y[pivot] + y[i] L + y[pivot] R + y[i] + y[pivot] + other
				//=(y[i] + R)(y[pivot] + L) + (y[i] + R) + (y[pivot] + L) + L + R + LR + other
				ellpair.add(i);
				ellpair.add(pivot);
			} else if (!flag0 && !flag1) {
				// y[i]y[pivot] + y[i] L + y[pivot] R + other
				//=(y[i] + R)(y[pivot] + L) + LR + other
				pair.add(i);
				pair.add(pivot);
			} else if (flag0){
				// y[i]y[pivot] + y[i] L + y[pivot] R + y[i] + other
				//=(y[i] + R)(y[i] + y[pivot] + L + R) + R + RL + other
				pair.add(i);
				pair.add(pivot);
			} else {
				pair.add(i);
				pair.add(pivot);
			}
			for (int x : L) {
				for (int y : R) {
					a[x][y] = !a[x][y];
					a[y][x] = a[x][y];
				}
			}
			for (int x : R) {
				for (int j = 0; j < n; j++) {
					base[i][j] ^= base[x][j];
				}
			}
			for (int x : L) {
				for (int j = 0; j < n; j++) {
					base[pivot][j] ^= base[x][j];
				}
			}
			for (int j = 0; j < n; j++) {
				if (j != pivot) {
					a[i][j] = a[j][i] = false;
				}
			}
			for (int j = 0; j < n; j++) {
				if (j != i) {
					a[pivot][j] = a[j][pivot] = false;
				}
			}			
		
			if (flag0) for (int x : R) a[x][x] = !a[x][x];
			if (flag1) for (int x : L) a[x][x] = !a[x][x];
			if (flag0 && !flag1) {
				for (int j = 0; j < n; j++) {
					base[pivot][j] ^= base[i][j];
				}
			}
			if (!flag0 && flag1) {
				for (int j = 0; j < n; j++) {
					base[i][j] ^= base[pivot][j];
				}
			}
			
		}
		// xy+x+y+zw+z+w
		//=(x+z)(x+y+z)+(y+w)(y+z+w)
		//なので二つのellはまとめられる。
		for (int i = ellpair.size()-4; i >= 0; i-=4) {
			boolean[]x=ArrayUtils.copy(base[ellpair.get(i)]);
			boolean[]y=ArrayUtils.copy(base[ellpair.get(i+1)]);
			boolean[]z=ArrayUtils.copy(base[ellpair.get(i+2)]);
			boolean[]w=ArrayUtils.copy(base[ellpair.get(i+3)]);
			for (int j = 0; j < n; j++) {
				base[ellpair.get(i+0)][j]=x[j]^z[j];
				base[ellpair.get(i+1)][j]=x[j]^y[j]^z[j];
				base[ellpair.get(i+2)][j]=y[j]^w[j];
				base[ellpair.get(i+3)][j]=y[j]^z[j]^w[j];
			}
			pair.add(ellpair.get(i+0));
			pair.add(ellpair.get(i+1));
			pair.add(ellpair.get(i+2));
			pair.add(ellpair.get(i+3));
		}
		while(ellpair.size()>=4) {
			for (int i = 0; i < 4; i++) {
				ellpair.pollLast();
			}
		}
		
		Base ret = new Base();
		ret.pair = new boolean[pair.size()][];
		for (int i = 0; i < pair.size(); i++) {
			ret.pair[i] = base[pair.get(i)];
		}
		if(!ellpair.isEmpty()) {
			ret.ell = new boolean[2][];
			for (int i = 0; i < ellpair.size(); i++) {
				ret.ell[i] = base[ellpair.get(i)];
			}
		}
		IntArrayList singleList = new IntArrayList();
		for (int i = 0; i < n; i++) {
			if (a[i][i]) {
				singleList.add(i);
			}
		}
		for (int i = 1; i < singleList.size(); i++) {
			for (int j = 0; j < n; j++) {
				base[singleList.get(0)][j] ^= base[singleList.get(i)][j];
			}
		}
		if (!singleList.isEmpty()) {
			ret.single = base[singleList.get(0)];
			for (int i = 1; i < singleList.size(); i++) {
				Arrays.fill(base[singleList.get(i)], false);
			}
		}
		int pointer = 0;
		ret.free = new boolean[n - ret.pair.length - ret.ell.length - (ret.single==null?0:1)][];
		for (int i = 0; i < n; i++) {
			boolean isFree = false;
			for (int j = 0; j < n; j++) {
				isFree &= !a[i][j];
			}
			if (isFree) ret.free[pointer++] = base[i];
		}
		return ret;
	}
	
	public static class Base {
		public boolean[][] pair;
		public boolean[] single;//存在しないときはnull
		public boolean[][] free;
		public boolean[][] ell=new boolean[0][];
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
