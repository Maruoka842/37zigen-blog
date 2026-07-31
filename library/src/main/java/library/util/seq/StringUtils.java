package library.util.seq;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.IntStream;
import library.util.ArrayUtils;
import library.util.fold.SparseTableInt;
import library.util.segtree.SegTreeFactory;

public class StringUtils {
	
	/**
	 * 最長共通部分文字列（LCS）の情報を保持する。
	 * @param p1 sにおける開始位置
	 * @param p2 tにおける開始位置
	 * @param len 長さ
	 */
	public record LCSResult(int p1, int p2, int len) {}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
	
	/**
	 * O(n)
	 * @param s
	 * @return
	 */
	public static long countSubstrings(char[] s) {
		//https://judge.yosupo.jp/submission/372108
		int[] sa = suffixArray(s);
		int[] lcp = lcpArray(s, sa);
		int n = s.length;
		long ans = (long)n*(n+1)/2;
		for(int v : lcp){
			ans -= v;
		}
		return ans;
	}
	
	public static int longestCommonSuffixPrefixLength(char[] suffix, char[] prefix) {
		//https://atcoder.jp/contests/abc343/submissions/74574335
        int[] a = StringUtils.lcpForEachSuffix(prefix, suffix);
        int ret=0;
        for (int k = 0; k < a.length; k++) {
            if (a[k] >= (suffix.length - k)) {
                ret = Math.max(ret, (a.length - k));
            }
        }
        return ret;
	}
	
	public static int period(char[] a) {
		//https://atcoder.jp/contests/abc312/submissions/74542906
		int[] Z=zAlgo(a);
		for (int j = 1; j <= Z.length; j++) {
			if(j==Z.length || (Z[j]==a.length-j && a.length%j==0)) {
				return j;
			}
		}
		throw new AssertionError();
	}
	
	/**
	 * ret[i] = s[i] と s[i+1] の間を中心とする偶数長回文の長さを 2k としたときの k
	 * @param s
	 * @return
	 */
	public static int[] manacherOfEvenlength(char[] s) {
		//https://judge.yosupo.jp/submission/372443
		int n = s.length;
		if (n <= 1) return new int[0];
		int i = 0, j = 0;
		int[] ret = new int[n - 1];

		while (i < ret.length) {
			while (i - j >= 0 && i + j + 1 < s.length && s[i - j] == s[i + j + 1]) {
				++j;
			}

			ret[i] = j;

			int k = 1;
			while (i - k >= 0 && k + ret[i - k] < j) {
				ret[i + k] = ret[i - k];
				++k;
			}

			i += k;
			j -= k;
			if (j < 0) j = 0;
		}
		return ret;
	}
	
	/**
	 * ret[i] = s[i]を中心とする回文の長さを2k-1としたときのk
	 * @param s
	 * @return
	 */
	public static int[] manacherOfOddlength(char[] s) {
		//https://judge.yosupo.jp/submission/372443
		int i = 0, j = 0;
		int[]ret=new int[s.length];
		while (i < s.length) {
		  while (i-j >= 0 && i+j < s.length && s[i-j] == s[i+j]) ++j;
		  ret[i] = j;
		  int k = 1;
		  while (i-k >= 0 && k+ret[i-k] < j) {
			  ret[i+k] = ret[i-k];
			  ++k;
		  }
		  i += k; j -= k;
		}
		return ret;
	}
	
    /**
     * 辞書順最小の巡回シフトをBoothのアルゴリズムで求めて返す
     * @param a
     * @return
     * https://atcoder.jp/contests/abc223/submissions/73247800
     */
	public static char[] minRotate(char[] a) {
    	int p=0;
    	int q=1;
    	int k=0;
    	char[]b=ArrayUtils.concat(a, a);
    	while(p < a.length && q < a.length && k < a.length) {
    		//サイクルシフト b[i:i+N) : i ∈ {0,1,..,min(p, q), p} の中で b[p:p+N) か b[q,q+N) が辞書順最小
    		if(b[p+k]==b[q+k])++k;
    		else {
    			if(b[p+k]<b[q+k]) {
    				q=q+k+1;
    			} else {
    				p=p+k+1;
    			}
    			if(p==q)q++;
    			k=0;
    		}
    	}
    	return Arrays.copyOfRange(b, Math.min(p, q), Math.min(p, q) + a.length);
    }
    
    
    
    /**
     * 辞書順最大の巡回シフトをBoothのアルゴリズムで求めて返す
     * @param a
     * @return
     * https://atcoder.jp/contests/abc223/submissions/73247800
     */
    public static char[] maxRotate(char[] a) {
    	int p=0;
    	int q=1;
    	int k=0;
    	char[]b=ArrayUtils.concat(a, a);
    	while(p < a.length && q < a.length && k < a.length) {
    		//サイクルシフト b[i:i+N) : i ∈ {0,1,..,min(p, q), p} の中で b[p:p+N) か b[q,q+N) が辞書順最大
    		if(b[p+k]==b[q+k])++k;
    		else {
    			if(b[p+k]>b[q+k]) {
    				q=q+k+1;
    			} else {
    				p=p+k+1;
    			}
    			if(p==q)q++;
    			k=0;
    		}
    	}
    	return Arrays.copyOfRange(b, Math.min(p, q), Math.min(p, q) + a.length);
    }

	
	/**
	 * s[i-1] > s[i] < s[i+1] < s[i+2] < .. となっているかを判定
	 * @param i
	 * @param isL
	 * @return
	 */
	static boolean isLMS(int i, boolean[]isL) {
		return i>0&&isL[i-1]&&!isL[i];
	}
	
	/**
	 * lmssはLMSのindexの列
	 * 1回目はindexの昇順（1回目は何でもよい）、2回目はsuffixの辞書順に並んでいる。
	 * @param S
	 * @param isL
	 * @param lmsIndices
	 * @return
	 */
	public static int[] induceSort(int[] S, boolean[]isL, int[] lmsIndices) {
		int N=S.length;
		int[] sa=new int[N];
		Arrays.fill(sa, -1);
		
		int[]count=new int[ArrayUtils.max(S)+2];
		for (int i = 0; i < N; i++) {
			count[S[i]+1]++;
		}
		for (int i = 0; i + 1 < count.length; i++) {
			count[i+1]+=count[i];
		}
		
		int[]count2=count.clone();
		{//leftmost smallをSAの下側から埋めていく
			for (int i = lmsIndices.length-1;i>=0;--i) {
				int id = lmsIndices[i];
				sa[count2[S[id]+1] - 1] = id;
				count2[S[id]+1]--;
			}
		}
		{
			//LMS型から左に比較結果を伸ばしていき、L型を全て埋める
			for (int i = 0; i < count.length; i++) {
				count2[i]=count[i];
			}
			for (int i = -1; i < N; i++) {
				int j = i==-1 ? N-1:(sa[i]-1);//Sの末尾には辞書順最小の文字εがあり、それが、SAの先頭(index=-1)にある
				if(j >= 0 && isL[j]) {
					int v=S[j];
					sa[count2[v]]=j;
					count2[v]++;
				}
			}
		}
		{//S型を埋める
			for (int i = N-1; i >=0; i--) {
				if(sa[i] > 0 && !isL[sa[i]-1]) {
					int v=S[sa[i]-1];
					sa[count[v+1]-1]=sa[i]-1;
					count[v+1]--;
				}
			}
		}
		return sa;
	}
	
	/**
	 * S[0:],S[1:],..を辞書順に並べた配列を返す。ただし、S[0:i]を並べる代わりにiを並べている。
	 * O(N log N)
	 * @param S
	 * @return
	 */
	public static int[] suffixArray(char[] S) {
		int[]a=new int[S.length];
		for (int i = 0; i < a.length; i++) {
			a[i]=(int)S[i];
		}
		int min=ArrayUtils.min(a);
		for (int i = 0; i < a.length; i++) {
			a[i]-=min;
		}
		return suffixArray(a);
	}
	
	/**
	 * https://mametter.hatenablog.com/entry/20180130/p1#f-1aedbc3e
	 * verified:https://judge.yosupo.jp/submission/327932
	 */
	public static int[] suffixArray(int[] S) {
		int N=S.length;
		boolean[] isL=new boolean[N+1];
		isL[N]=false;//Sの末尾には、辞書順最小の文字である空文字εがあり、εはS扱い
		for (int i = N - 1; i >= 0; i--) {
			if(i==N-1) isL[i]=true;//S[N]は辞書順最小の文字扱い
			else if(S[i]==S[i+1])isL[i]=isL[i+1];
			else if(S[i]>S[i+1])isL[i]=true;
		}
		int[]lmsIndices=new int[N];
		{
			int pointer=0;
			for (int i = 0; i < N; i++) {
				if(isLMS(i, isL)) lmsIndices[pointer++]=i;
			}
			lmsIndices=Arrays.copyOf(lmsIndices, pointer);
		}
		int[]sa=induceSort(S, isL, lmsIndices);
		if (lmsIndices.length == 0) return sa;
		int[] nextWord=new int[N];
		Arrays.fill(nextWord, -1);
		{
			int i=0;
			while(!isLMS(sa[i], isL)) {
				i++;
			}
			nextWord[sa[i]]=0;
			while (i < N) {
				int j=i+1;
				while(j < N && !isLMS(sa[j], isL)) {
					j++;
				}
				if (j == N) break;
				nextWord[sa[j]]=nextWord[sa[i]];
				for (int a = sa[i], b=sa[j]; a <= N && b <= N; a++, b++) {
					if(isLMS(a, isL) != isLMS(b, isL)) {
						nextWord[sa[j]]++;
						break;
					}
					if((a == N ? -1 : S[a]) != (b == N ? -1 : S[b])) {
						nextWord[sa[j]]++;
						break;
					}
					if (a != sa[i] && (isLMS(a, isL) || isLMS(b, isL))) break;
				}
				i=j;
			}
		}
		
//		nextWord = Arrays.stream(nextWord).filter(v->v!=-1).toArray();
		{
			int m = 0;
			for (int i = 0; i < N; i++) {
			    if (nextWord[i] != -1) {
			        nextWord[m++] = nextWord[i];
			    }
			}
			nextWord = Arrays.copyOf(nextWord, m);
		}
		int max=Integer.MIN_VALUE;
		for (int i=0;i<nextWord.length;++i)max=Math.max(max, nextWord[i]);
		if(max == nextWord.length - 1) {
			sa = Permutation.inverse(nextWord);
		} else {
			sa = suffixArray(nextWord);
		}
		lmsIndices = ArrayUtils.take(lmsIndices, sa);
		return induceSort(S, isL, lmsIndices);
	}

	
	
	/**
	 * S[0:],S[1:],..を辞書順に並べた配列を返す。ただし、S[0:i]を並べる代わりにiを並べている。
	 * O(N log N)
	 * verified:https://judge.yosupo.jp/problem/suffixarray
	 */
    public static int[] suffixArray_doubling(char[] S) {
        S = ArrayUtils.concat(S, new char[]{ '$' });
        int N = S.length;
        int[] sa = ArrayUtils.argSort(S);
        int[] rank = new int[N];
        for (int i = 1; i < N; i++) {
            rank[sa[i]] = rank[sa[i - 1]];
            if (S[sa[i]] != S[sa[i - 1]]) {
                rank[sa[i]]++;
            }
        }
        for (int len = 1; (len / 2) < N; len *= 2) {
        	// ペア(rank[i], rank[i+k])をradixソートする。
        	final int k = len;
            {
                int[] count = new int[N + 1];
                int[] nsa = new int[N];
                for (int i = 0; i < N; i++) {
                    count[rank[(sa[i] + k) % N] + 1]++;
                }
                for (int i = 0; (i + 1) < count.length; i++) {
                    count[i + 1] += count[i];
                }
                for (int i = 0; i < N; i++) {
                    nsa[count[rank[(sa[i] + k) % N]]] = sa[i];
                    count[rank[(sa[i] + k) % N]]++;
                }
                sa = nsa;
            }
            {
                int[] count = new int[N + 1];
                int[] nsa = new int[N];
                for (int i = 0; i < N; i++) {
                    count[rank[sa[i]] + 1]++;
                }
                for (int i = 0; (i + 1) < count.length; i++) {
                    count[i + 1] += count[i];
                }
                for (int i = 0; i < N; i++) {
                    nsa[count[rank[sa[i]]]] = sa[i];
                    count[rank[sa[i]]]++;
                }
                sa = nsa;
            }
            int[] nrank = new int[N];
            for (int i = 1; i < N; i++) {
                nrank[sa[i]] = nrank[sa[i - 1]];
                if ((rank[sa[i]] != rank[sa[i - 1]]) || (rank[(sa[i] + k) % N] != rank[(sa[i - 1] + k) % N])) {
                    nrank[sa[i]]++;
                }
            }
            rank = nrank;
        }
        return Arrays.copyOfRange(sa, 1, N);
    }

	
	
	/**
	 * S[0:],S[1:],..を辞書順に並べた配列を返す。ただし、S[0:i]を並べる代わりにiを並べている。
	 * O(Nlog(N)^2)
	 * verified:https://judge.yosupo.jp/problem/suffixarray
	 */
	private static int[] _slow_suffixArray(char[] S) {
		int N = S.length;
		int[] rank = IntStream.range(0, N).map(i -> (int) S[i]).toArray();
		Integer[] sa = IntStream.range(0, N).boxed().toArray(Integer[]::new);
		for (int len=1;len/2<N;len*=2) {
			final int[] rank_ = Arrays.copyOf(rank, N);
			final int k = len;
			Comparator<Integer> cmp = (x, y) -> {
				if (rank_[x] != rank_[y]) {
					return Integer.compare(rank_[x], rank_[y]);
				} else {
					return Integer.compare((x+k<N)?rank_[x+k]:-1, (y+k<N)?rank_[y+k]:-1);
				}
			};
			Arrays.sort(sa, cmp);
			rank[sa[0]] = 0;
			for (int i = 1; i < N; ++i) {
				rank[sa[i]] = rank[sa[i-1]] + (int) Math.signum(cmp.compare(sa[i], sa[i-1]));
			}
		}
		return Arrays.stream(sa).mapToInt(Integer::intValue).toArray();
	}
	/**
	 * 各位置 i について、{@code text[i..)} と {@code prefix} の
	 * 最長共通接頭辞（LCP: Longest Common Prefix）の長さを求める。
	 * <p>
	 * 返される配列 {@code ret} は長さ {@code text.length} を持ち、
	 * {@code ret[i]} は {@code prefix} と {@code text} の部分文字列
	 * {@code text[i..)} の LCP の長さを表す。
	 * @param prefix
	 * @param text
	 * @return
	 * https://atcoder.jp/contests/abc257/submissions/72083693
	 */
	public static int[] lcpForEachSuffix(char[] prefix, char[] text) {
		var Z=StringUtils.zAlgo(ArrayUtils.concat(prefix, text));
		int[]ret=Arrays.copyOfRange(Z, prefix.length, prefix.length+text.length);
		for (int i = 0; i < ret.length; i++) {
			ret[i]=Math.min(ret[i], prefix.length);
		}
		return ret;
	}
	
	
	/**
	 * verified:https://judge.yosupo.jp/submissions/?problem=zalgorithm&user=37zigen&status=AC
	 */
	public static int[] zAlgo(char[] a) {
    	int[]ret=new int[a.length];
    	ret[0]=a.length;
    	int last=1;
    	for (int i = 1; i < a.length; i++) {
			last=Math.max(last, i);
			while (last<a.length && a[last]==a[last-i])++last;
			ret[i]=last-i;
			//S[i, last) = S[0, last-i)
			int j=i;
			while (j+1<last && ret[(j+1)-i] < last-(j+1)) {
				//j ∈ [i, last) の範囲で極大まで動かす
				//LCP(S[j+1:], S) < last-(j+1)ならば LCP(S[j+1:], S) = LCP(S[(j+1)-i])が確定
				j++;
				ret[j] = ret[j - i];
			}
			i = j;
    	}
    	return ret;
    }
	
	public static int[] zAlgo(int[] a) {
    	int[]ret=new int[a.length];
    	ret[0]=a.length;
    	int last=1;
    	for (int i = 1; i < a.length; i++) {
			last=Math.max(last, i);
			while (last<a.length && a[last]==a[last-i])++last;
			ret[i]=last-i;
			//S[i, last) = S[0, last-i)
			int j=i;
			while (j+1<last && ret[(j+1)-i] < last-(j+1)) {
				//j ∈ [i, last) の範囲で極大まで動かす
				//LCP(S[j+1:], S) < last-(j+1)ならば LCP(S[j+1:], S) = LCP(S[(j+1)-i])が確定
				j++;
				ret[j] = ret[j - i];
			}
			i = j;
    	}
    	return ret;
    }
	
	/**
	 * Lyndon分解を返す。Duvalのアルゴリズム。O(N)。
	 * s = w_1 w_2 ... w_k s.t. w_i is Lyndon and w_1 >= w_2 >= ... >= w_k
	 * 返り値は各w_iの開始位置の配列。最後にs.lengthを追加する。
	 * @param s
	 * @return
	 */
	public static int[] lyndonDecomposition(char[] s) {
		//https://judge.yosupo.jp/submission/171546
		int n = s.length;
		int i = 0;
		int[] res = new int[n + 1];
		int ptr = 0;
		// [0:i) 確定
		// [i:j) = [i-j+k:k)
		// [i:k) Lyndon語w^mのprefix
		while (i < n) {
			int k = i + 1, j = i;
			
			while (k < n && s[j] <= s[k]) {
				if (s[j] < s[k]) j = i;
				else j++;
				k++;
			}
			while (i <= j) {
				res[ptr++] = i;
				i += k - j;
			}
		}
		res[ptr++] = n;
		return Arrays.copyOf(res, ptr);
	}

	/**
	 * Lyndon分解を返す。Duvalのアルゴリズム。O(N)。
	 * s = w_1 w_2 ... w_k s.t. w_i is Lyndon and w_1 >= w_2 >= ... >= w_k
	 * 返り値は各w_iの開始位置の配列。最後にs.lengthを追加する。
	 * 未テスト
	 * @param s
	 * @return
	 */
	public static int[] lyndonDecomposition(int[] s) {
		int n = s.length;
		int i = 0;
		int[] res = new int[n + 1];
		int ptr = 0;
		while (i < n) {
			int j = i + 1, k = i;
			while (j < n && s[k] <= s[j]) {
				if (s[k] < s[j]) k = i;
				else k++;
				j++;
			}
			while (i <= k) {
				res[ptr++] = i;
				i += j - k;
			}
		}
		res[ptr++] = n;
		return Arrays.copyOf(res, ptr);
	}
	
	
	
    /**
     * 英小文字からなる文字列aの長さlengthの部分列のうち、辞書順最小を返す。O(N log N)
     * @param a
     * @param length
     * @return
     * verified:https://www.codechef.com/viewsolution/1204479055
     */
	public static char[] lexSmallestSubsequenceOfLength(char[] a, int length) {
    	int[] P=ArrayUtils.rank(a);
		int[]ans=lexSmallestSubsequenceOfLength(P, length);
		int[]iP=Permutation.inverse(P);
    	char[]ret=new char[length];
		for (int i = 0; i < length; i++) {
			ret[i]=a[iP[ans[i]]];
		}
		return ret;
    }
    
    /**
     * O(N log N)
     * @param a
     * @param length
     * @return
     * verified:https://www.codechef.com/viewsolution/1204479055
     */
    public static int[] lexSmallestSubsequenceOfLength(int[] a, int length) {
    	if (length > a.length) throw new AssertionError();
    	int delete=a.length-length;
    	int[]P=ArrayUtils.rank(a);
    	int N=P.length;
    	var seg=SegTreeFactory.add_Argmin(N);
		for (int i = 0; i < N; i++) {
			seg.set(i, 1L * P[i]);
		}
		int[]ans=new int[length];
		int pointer=0;
		for (int i = 0; i < length; ++i) {
			var v=seg.fold(0, i+delete+1);
			ans[pointer++]=(int) v.val;
			seg.delete(v.key);
			while (true) {
				var u=seg.fold(0, v.key);
				if (u.val == Long.MAX_VALUE)break;
				seg.delete(u.key);
			}
		}
		int[]iP=Permutation.inverse(P);
    	int[]ret=new int[length];
		for (int i = 0; i < length; i++) {
			ret[i]=a[iP[ans[i]]];
		}

		return ans;
    }
    
    /**
     * 長さn-1の配列を返す。i番目の要素はa[sa[i]:]とa[sa[i+1]:]のlcpの長さ
     * @param a
     * @return
     * 125 Problems in Text Algorithms
     */
    public static int[] lcpArray(char[] a) {
    	int[]sa=_slow_suffixArray(a);
    	int[]rank=Permutation.inverse(sa);
    	int L=0;
    	int[]LCP=new int[a.length-1];
    	for (int i = 0; i < a.length; i++) {
    		if (rank[i]!=a.length-1) {
    			while(i+L<a.length && sa[rank[i]+1]+L < a.length && a[i+L]==a[sa[rank[i]+1]+L])++L;
				LCP[rank[i]]=L;
				L=Math.max(L-1, 0);
    		}
		}
    	return LCP;
    }
    

    /**
     * 長さn-1の配列を返す。i番目の要素はa[sa[i]:]とa[sa[i+1]:]のlcpの長さ
     * suffixArrayは前計算して引数に渡す
     * @param a
     * @param suffixArray
     * @return
     * 125 Problems in Text Algorithms
     */
    public static int[] lcpArray(char[] a, int[] suffixArray) {
    	int[]rank=Permutation.inverse(suffixArray);
    	int L=0;
    	int[]LCP=new int[a.length-1];
    	for (int i = 0; i < a.length; i++) {
    		if (rank[i]!=a.length-1) {
    			while(i+L<a.length && suffixArray[rank[i]+1]+L < a.length && a[i+L]==a[suffixArray[rank[i]+1]+L])++L;
				LCP[rank[i]]=L;
				L=Math.max(L-1, 0);
    		}
		}
    	return LCP;
    }

    /**
     * 長さn-1の配列を返す。i番目の要素はa[sa[i]:]とa[sa[i+1]:]のlcpの長さ
     * @param a
     * @return
     * 125 Problems in Text Algorithms
     * 未テスト
     * 計算量: O(N)
     */
    public static int[] lcpArray(int[] a) {
    	int[]sa=suffixArray(a);
    	return lcpArray(a, sa);
    }

    /**
     * 長さn-1の配列を返す。i番目の要素はa[sa[i]:]とa[sa[i+1]:]のlcpの長さ
     * suffixArrayは前計算して引数に渡す
     * @param a
     * @param suffixArray
     * @return
     * 125 Problems in Text Algorithms
     * 未テスト
     * 計算量: O(N)
     */
    public static int[] lcpArray(int[] a, int[] suffixArray) {
    	int[]rank=Permutation.inverse(suffixArray);
    	int L=0;
    	int[]LCP=new int[a.length-1];
    	for (int i = 0; i < a.length; i++) {
    		if (rank[i]!=a.length-1) {
    			while(i+L<a.length && suffixArray[rank[i]+1]+L < a.length && a[i+L]==a[suffixArray[rank[i]+1]+L])++L;
				LCP[rank[i]]=L;
				L=Math.max(L-1, 0);
    		}
		}
    	return LCP;
    }
    
    public static int longestCommonPrefixLength(char[] a, char[] b) {
    	//https://atcoder.jp/contests/abc324/submissions/75179344
    	for (int i = 0; i < Math.min(a.length, b.length); i++) {
    		if(a[i]!=b[i])return i;
    	}
    	return Math.min(a.length, b.length);
    }
    
    public static int longestCommonSuffixLength(char[] a, char[] b) {
    	//https://atcoder.jp/contests/abc324/submissions/75179344
    	for (int i = 0; i < Math.min(a.length, b.length); i++) {
    		if(a[a.length-1-i]!=b[b.length-1-i])return i;
    	}
    	return Math.min(a.length, b.length);
    }
    
    public static char[] min(char[] a, char[] b) {
    	if(Arrays.compare(a, b)<=0) {
    		return a;
    	}else {
    		return b;
    	}
    }
    
    /**
     *Arrays.sort(S, (x, y)->Arrays.compare(ArrayUtils.concat(x, y), ArrayUtils.concat(y, x)));
     * @param S
     */
    public static void sortByConcatOrder(char[][] S) {
		Arrays.sort(S, (x, y)->Arrays.compare(ArrayUtils.concat(x, y), ArrayUtils.concat(y, x)));
    }
    
    /**
     * Si+SiのZ ArrayをZi, Sj+SjのZ ArrayをZj, Si, Sjのlcpの長さをlcpとする。
     * Si+SjとSj+Siを辞書順で比較する。
     * https://atcoder.jp/contests/abc434/tasks/abc434_f
     * @param Si
     * @param Sj
     * @param lcp
     * @param Zi
     * @param Zj
     * @return
     */
    public static int concatCompare(char[] Si, char[] Sj, int lcp, int[] Zi, int[] Zj) {
    	if(lcp==Si.length&&lcp==Sj.length)return 0;
		if(lcp<Si.length&&lcp<Sj.length) {
			return Character.compare(Si[lcp], Sj[lcp]);
		} else if (lcp==Si.length) {
			//compare(S[j], S[j][LCP:))
			int len=Zj[lcp];
			if (lcp+len < Zj.length)
				return Character.compare(Sj[len], Sj[(lcp+len)%Sj.length]);
			return 0;
		} else {
			int len=Zi[lcp];
			if (lcp+len < Zi.length)
				return Character.compare(Si[(lcp+len)%Si.length], Si[len]);
			return 0;
		}
    }

    /**
     * 数字, +, * のみからなる数式を評価し、modを取った値を返す。
     * 先頭が数字以外だと壊れる。-,/,()には対応していない。
     * @param S
     * @param mod
     * @return
     * https://atcoder.jp/contests/past16-open/tasks/past202309_f
     */
    public static long evalMod(char[] S, long mod) {
    	long preProduct=1;//掛け算でこれまでに掛けられた数の積
    	long sum=0;
    	for (int i = 0; i < S.length; i++) {
    		if(S[i]=='+') {
    			sum=(sum+preProduct)%mod;
    			preProduct=1;
    		} else if (S[i] == '*') {
    			continue;
    		} else {
    			long a=0;
    			int j=i;
    			while(j < S.length && Character.isDigit(S[j])) {
    				a=10*a+(int)(S[j]-'0');
    				a%=mod;
    				j++;
    			}
    			preProduct=preProduct*a%mod;
    			i=j-1;
    			if(j==S.length)sum=(sum+preProduct)%mod;
    		}
    	}
    	return sum;
    }
    
    
    
    public static boolean isPalindrome(char[] a) {
		int s=0;
		int t=a.length-1;
		while(s<t) {
			if(a[s]!=a[t])return false;
			++s;--t;
		}
		return true;
	}
    
    public static boolean isPalindrome(int[] a) {
		int s=0;
		int t=a.length-1;
		while(s<t) {
			if(a[s]!=a[t])return false;
			++s;--t;
		}
		return true;
	}
    
	/**
	 * 部分文字列か判定
	 * @param text
	 * @param subword
	 * @return
	 */
	public static boolean isSubString(char[] subword, char[] text) {
		//https://atcoder.jp/contests/abc354/submissions/72136622
		if(text.length < subword.length)return false;
		int[] z=zAlgo(ArrayUtils.concat(subword, text));
		for (int i = subword.length; i < z.length; i++) {
			if(z[i] >= subword.length) return true;
		}
		return false;
	}
	
	public static boolean isSubString(int[] subword, int[] text) {
		if(text.length < subword.length)return false;
		int[] z=zAlgo(ArrayUtils.concat(subword, text));
		for (int i = subword.length; i < z.length; i++) {
			if(z[i] >= subword.length) return true;
		}
		return false;
	}
	

	public static boolean isSubSequence(char[] subseq, char[] text) {
		//https://www.acmicpc.net/submit/6550/104719048
		if(text.length < subseq.length)return false;
		int x = 0;
		for (int i = 0; i < text.length && x < subseq.length; i++) {
			if(subseq[x] == text[i]) ++x;
		}
		return x == subseq.length;
	}
	
	
	public static boolean isSubSequence(int[] subseq, int[] text) {
		//https://atcoder.jp/contests/past22-open/submissions/74948588
		if(text.length < subseq.length)return false;
		int x = 0;
		for (int i = 0; i < text.length && x < subseq.length; i++) {
			if(subseq[x] == text[i]) ++x;
		}
		return x == subseq.length;
	}

	/**
	 * sとtの最長共通部分文字列（LCS）の情報を返す。O(|s|+|t|)。
	 * @param s
	 * @param t
	 * @return LCSの情報。見つからない場合は len=0 の結果を返す。
	 * 計算量: O(|s|+|t|)
	 */
	public static LCSResult longestCommonSubstring(char[] s, char[] t) {
		//https://judge.yosupo.jp/submission/372051
		int n = s.length;
		int m = t.length;
		if (n == 0 || m == 0) return new LCSResult(0, 0, 0);
		int[] combined = new int[n + m + 1];
		for (int i = 0; i < n; i++) combined[i] = s[i] + 1;
		combined[n] = 0;
		for (int i = 0; i < m; i++) combined[n + 1 + i] = t[i] + 1;

		int[] sa = suffixArray(combined);
		int[] lcp = lcpArray(combined, sa);

		int maxLen = 0;
		int posS = 0;
		int posT = 0;
		for (int i = 0; i < sa.length - 1; i++) {
			boolean firstInS = sa[i] < n;
			boolean secondInS = sa[i + 1] < n;
			boolean firstInT = sa[i] > n;
			boolean secondInT = sa[i + 1] > n;

			if ((firstInS && secondInT) || (firstInT && secondInS)) {
				if (lcp[i] > maxLen) {
					maxLen = lcp[i];
					if (firstInS) {
						posS = sa[i];
						posT = sa[i + 1] - (n + 1);
					} else {
						posT = sa[i] - (n + 1);
						posS = sa[i + 1];
					}
				}
			}
		}
		
		return new LCSResult(posS, posT, maxLen);
	}

	/**
	 * sとtの最長共通部分文字列（LCS）の情報を返す。O(|s|+|t|)。
	 * @param s
	 * @param t
	 * @return LCSの情報。見つからない場合は len=0 の結果を返す。
	 * 未テスト
	 * 計算量: O(|s|+|t|)
	 */
	public static LCSResult longestCommonSubstring(int[] s, int[] t) {
		int n = s.length;
		int m = t.length;
		if (n == 0 || m == 0) return new LCSResult(0, 0, 0);

		int[] all = ArrayUtils.concat(s, t);
		int[] compressed = ArrayUtils.compress(all);
		int[] combined = new int[n + m + 1];
		for (int i = 0; i < n; i++) combined[i] = compressed[i] + 1;
		combined[n] = 0;
		for (int i = 0; i < m; i++) combined[n + 1 + i] = compressed[n + i] + 1;

		int[] sa = suffixArray(combined);
		int[] lcp = lcpArray(combined, sa);

		int maxLen = 0;
		int posS = 0;
		int posT = 0;
		for (int i = 0; i < sa.length - 1; i++) {
			boolean firstInS = sa[i] < n;
			boolean secondInS = sa[i + 1] < n;
			boolean firstInT = sa[i] > n;
			boolean secondInT = sa[i + 1] > n;

			if ((firstInS && secondInT) || (firstInT && secondInS)) {
				if (lcp[i] > maxLen) {
					maxLen = lcp[i];
					if (firstInS) {
						posS = sa[i];
						posT = sa[i + 1] - (n + 1);
					} else {
						posT = sa[i] - (n + 1);
						posS = sa[i + 1];
					}
				}
			}
		}

		return new LCSResult(posS, posT, maxLen);
	}

    /**
     * dp[i][j] = LCS(S[0:i), T[j:nb)) の長さを計算する。
     * 計算量: O(na * nb)
     *
     * 【仕組みの解説】
     * 論文 Alves, Cáceres, Song (2008) の Section 4 および Definition 3.2 によれば、
     * S[0:i) と T[j:k) の LCS は以下の式で求まる:
     *   LCS(S[0:i), T[j:k)) = Σ_{m=j+1}^{k} [ih(i, m) < j]
     * ここで [condition] は条件が真なら 1、偽なら 0 を返す。
     *
     * 本メソッドでは S[0:i) と T[j:nb) の LCS を求めたいので、k = nb とすると:
     *   dp[i][j] = Σ_{m=j+1}^{nb} [ih(i, m) < j]
     *
     * このままだと各 j に対して O(nb) かかり全体で O(na * nb^2) になるが、
     * Σ_{m=1}^{nb} [ih(i, m) < j] を C(i, j) とおくと、
     *   dp[i][j] = C(i, j) - (j 以下の ih(i, m) の個数)
     * ここで、全 m において ih(i, m) は 0 以上 nb 以下の整数であり、
     * さらに {ih(i, 1), ..., ih(i, nb)} は 0..nb-1 の置換に近い性質（各値が高々1回出現）
     * を持つため、各行で ih の頻度分布をとれば C(i, j) は累積和で O(nb) で求まる。
     *
     * 実際には、ih(i, m) < j という条件は、
     * 「列 m において、左端が j までなら LCS が 1 増える」ことを意味している。
     *
     * @param S 文字列 S
     * @param T 文字列 T
     * @return dp[na+1][nb+1]
     */
    public static int[][] computePrefixSuffixLCS(String S, String T) {
        int na = S.length();
        int nb = T.length();
        int[][] dp = new int[na + 1][nb + 1];

        int[] ih = new int[nb + 1];
        for (int j = 0; j <= nb; j++) {
            ih[j] = j;
        }

        int[] ihNew = new int[nb + 1];
        int[] freq = new int[nb + 1];

        for (int i = 1; i <= na; i++) {
            int iv = 0;
            char ci = S.charAt(i - 1);
            for (int j = 1; j <= nb; j++) {
                int iv_prev = iv;
                int ih_prev = ih[j];

                if (ci != T.charAt(j - 1)) {
                    ihNew[j] = Math.max(iv_prev, ih_prev);
                    iv       = Math.min(iv_prev, ih_prev);
                } else {
                    ihNew[j] = iv_prev;
                    iv       = ih_prev;
                }
            }
            ihNew[0] = 0;
            int[] tmp = ih;
            ih = ihNew;
            ihNew = tmp;

            for (int j = 0; j <= nb; j++) freq[j] = 0;
            for (int j = 1; j <= nb; j++) {
                freq[ih[j]]++;
            }

            int count = 0;
            for (int j = 0; j <= nb; j++) {
                count += freq[j];
                dp[i][j] = count - j;
            }
        }
        return dp;
    }

    /**
     * 与えられた文字列集合 $S = \{s_1, s_2, \dots, s_m\}$ から重複を削除し、
     * 他の文字列の部分文字列となっているものを排除した集合を返す。
     * $S' = \{s \in \text{distinct}(S) \mid \nexists t \in \text{distinct}(S) \setminus \{s\}, s \text{ is a substring of } t\}$。
     * 返り値の順序は、入力における出現順（重複は最初のもの）を維持する。
     *
     * <p>計算量: $O(N \log N)$ ($N = \sum |s_i|$)</p>
     *
     * @param input 入力文字列の配列
     * @return フィルタリングされた文字列の配列
     */
    public static String[] removeSubstrings(String[] input) {
        if (input == null || input.length == 0) return new String[0];

        LinkedHashSet<String> uniqueStrings = new LinkedHashSet<>();
        for (String s : input) {
            if (s != null) uniqueStrings.add(s);
        }

        if (uniqueStrings.isEmpty()) return new String[0];
        if (uniqueStrings.size() == 1) return uniqueStrings.toArray(new String[0]);

        List<String> list = new ArrayList<>(uniqueStrings);
        int m = list.size();
        int totalLen = 0;
        for (String s : list) totalLen += s.length() + 1;

        int[] T = new int[totalLen];
        int[] start = new int[m];
        int[] len = new int[m];
        int ptr = 0;
        for (int i = 0; i < m; i++) {
            start[i] = ptr;
            String s = list.get(i);
            len[i] = s.length();
            for (int j = 0; j < s.length(); j++) {
                T[ptr++] = s.charAt(j) + 1;
            }
            T[ptr++] = 0; // Separator
        }

        SuffixArrayLCP saLcp = new SuffixArrayLCP(T);

        List<String> result = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            int[] range = saLcp.occurrenceRange(start[i], start[i] + len[i]);
            // 重複排除済みなので、出現回数が1回（自分自身のみ）であれば、他の文字列の部分文字列ではない。
            if (range[1] - range[0] == 1) {
                result.add(list.get(i));
            }
        }

        return result.toArray(new String[0]);
    }
}