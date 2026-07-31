package library.util;

import java.util.ArrayList;
import java.util.List;

/**
 * verified:
 * https://atcoder.jp/contests/abc336/submissions/70393710
 * https://atcoder.jp/contests/abc114/submissions/70488452
 */
public abstract class DigitDP {

    int radix=10;
    protected int numStates;
    protected int[] dpShape;//dp配列のサイズ。例えばdp[5][3]なら dpShape=[5,3]。
    protected int totalStateSize;
    protected long[][][] way0;//[eq][leading0][flattenIndex] 条件を満たすものの数
    protected long[][][] way1;//[eq][leading0][flattenIndex] 条件を満たすものの和

    public DigitDP() {
    	dpShape = dpShape();
    	numStates = dpShape.length;
    	totalStateSize = 1;
    	for (int s : dpShape) {
        	totalStateSize *= s;
        }
    }
    
    public DigitDP(int base) {
    	this();
    	this.radix = base;
    }


    /**
     * dp配列のサイズdpShapeを定義する。
     */
    protected abstract int[] dpShape();
    
    /**
     * 遷移をnstateに書き込む。
     * nextはdigitPositionに割り当てる数字
     * eqはNとこれまで決定した桁が同一か
     * 遷移先がない場合はfalseを返す。
     */
    protected abstract boolean transition(int[] state, int eq, int leading0, int next, int digitPosition, int[] nstate);

    protected abstract boolean isTarget(int[] state);
    
    /**
     * 0がstateのどれに対応するか
     * @return
     */
    protected abstract int[] initialState();
    
    /** mod演算にしたいときにoverride←最後の和を取る部分でmodを取れないので直す必要あり*/
    protected void addNext(long[][][] nextDP, int neq, int nlead, int newIdx, long ways) {
        nextDP[neq][nlead][newIdx] += ways;
    }

    /**
     * state[0]+state[1]dpShape[0]+state[2]dpShape[0]dpShape[1]+...を返す
     * @param state
     * @return
     */
    private int getFlatIndex(int[] state) {
        int idx = 0;
        for (int d = dpShape.length - 1; d >= 0; --d) {
            idx = idx * dpShape[d] + state[d];
        }
        return idx;
    }
    
    private void writeMultiIndex(int index, int[] buffer) {
    	for (int i = 0; i < dpShape.length; i++) {
    		buffer[i] = index % dpShape[i];
			index /= dpShape[i];
		}
    }

    /**
     * 0以上N以下の条件を満たすものの数え上げ
     * @param N
     * @return
     */
    public long count(long N) {
    	var digits=toDigits(N);
    	return count(digits);
    }
    
    
    
    /**
     * 0以上N以下の条件を満たすものの数え上げ
     * @param digits
     * @return
     */
    public long count(int[] digits, final long mod) {
    	
        way0 = new long[2][2][totalStateSize];
    	way0[1][1][getFlatIndex(initialState())]=1;
    	int[] newState=new int[dpShape.length];
    	int[] state=new int[dpShape.length];
    	for (int i = 0; i < digits.length; ++i) {
    		long[][][] nway0 = new long[2][2][totalStateSize];
        	for (int eq = 0; eq < 2; eq++) {
            	for (int leading0 = 0; leading0 < 2; ++leading0) {
            		for (int stateId = 0; stateId < totalStateSize; stateId++) {
            			writeMultiIndex(stateId, state);
						
						int stateIdx = getFlatIndex(state);
						if (way0[eq][leading0][stateIdx] == 0L) continue;
						
						for (int next = 0; next <= ((eq == 1) ? digits[i] : (radix-1)); next++) {
							int neq = (eq == 1 && next == digits[i]) ? 1 : 0;
							int nlead = leading0 & (next == 0 ? 1 : 0);
							if (transition(state, eq, leading0, next, i, newState)) {
								int newIdx = getFlatIndex(newState);
								nway0[neq][nlead][newIdx]+=way0[eq][leading0][stateIdx];
								nway0[neq][nlead][newIdx]%=mod;
							}
						}
					}
            	}
            }
        	way0 = nway0;
        }

        long result = 0L;
        for (int eq = 0; eq < 2; eq++) {
        	for (int leading0 = 0; leading0 < 2; ++leading0) {
        		for (int stateId = 0; stateId < totalStateSize; stateId++) {
					writeMultiIndex(stateId, state);
					if (isTarget(state)) {
						result += way0[eq][leading0][getFlatIndex(state)];
						if(result>=mod)result-=mod;
					}
        		}
        	}
        }
        return result;
    }

    
    /**
     * 0以上N以下の条件を満たすものの数え上げ
     * @param digits
     * @return
     */
    public long count(int[] digits) {
    	
        way0 = new long[2][2][totalStateSize];
    	way0[1][1][getFlatIndex(initialState())]=1;
    	int[] newState=new int[dpShape.length];
    	int[] state=new int[dpShape.length];
    	for (int i = 0; i < digits.length; ++i) {
    		long[][][] nway0 = new long[2][2][totalStateSize];
        	for (int eq = 0; eq < 2; eq++) {
            	for (int leading0 = 0; leading0 < 2; ++leading0) {
            		for (int stateId = 0; stateId < totalStateSize; stateId++) {
            			writeMultiIndex(stateId, state);
						
						int stateIdx = getFlatIndex(state);
						if (way0[eq][leading0][stateIdx] == 0L) continue;
						
						for (int next = 0; next <= ((eq == 1) ? digits[i] : (radix-1)); next++) {
							int neq = (eq == 1 && next == digits[i]) ? 1 : 0;
							int nlead = leading0 & (next == 0 ? 1 : 0);
							if (transition(state, eq, leading0, next, i, newState)) {
								int newIdx = getFlatIndex(newState);
								nway0[neq][nlead][newIdx]+=way0[eq][leading0][stateIdx];
							}
						}
					}
            	}
            }
        	way0 = nway0;
        }

        long result = 0L;
        for (int eq = 0; eq < 2; eq++) {
        	for (int leading0 = 0; leading0 < 2; ++leading0) {
        		for (int stateId = 0; stateId < totalStateSize; stateId++) {
					writeMultiIndex(stateId, state);
					if (isTarget(state)) {
						result += way0[eq][leading0][getFlatIndex(state)];
					}
        		}
        	}
        }
        return result;
    }

    

    
    /**
     * 0以上N以下の条件を満たすものの和
     * @param digits
     * @return
     */
    public long sum(int[] digits, final long mod) {
    	way1 = new long[2][2][totalStateSize];
    	way0 = new long[2][2][totalStateSize];
    	way0[1][1][getFlatIndex(initialState())] = 1;
    	long[] TEN=new long[digits.length];
    	TEN[0]=1;
    	for (int i = 1; i < TEN.length; i++) {
    		TEN[i] = radix * TEN[i - 1] % mod;
    	}
    	
    	long[][][] nway1 = new long[2][2][totalStateSize];
    	long[][][] nway0 = new long[2][2][totalStateSize];
    	int[] newState = new int[dpShape.length];
    	int[] state = new int[dpShape.length];
    	for (int i = 0; i < digits.length; ++i) {
            ArrayUtils.fill(nway1, 0L);
            ArrayUtils.fill(nway0, 0L);
        	for (int eq = 0; eq < 2; eq++) {
            	for (int leading0 = 0; leading0 < 2; ++leading0) {
            		for (int stateId = 0; stateId < totalStateSize; stateId++) {
            			if (way0[eq][leading0][stateId] == 0L) continue;
						writeMultiIndex(stateId, state);
						
						
						for (int next = 0; next <= ((eq == 1) ? digits[i] : (radix-1)); next++) {
							int neq = (eq == 1 && next == digits[i]) ? 1 : 0;
							int nlead = leading0 & (next == 0 ? 1 : 0);
							if (transition(state, eq, leading0, next, i, newState)) {
								int newIdx = getFlatIndex(newState);
								nway0[neq][nlead][newIdx]+=way0[eq][leading0][stateId];
								//9((1e9+7)^2)=9000000126000000441なので、way0[eq][leading0][stateId]*nextでmodをサボる
								nway1[neq][nlead][newIdx]+=way0[eq][leading0][stateId]*next*TEN[digits.length-1-i]%mod + way1[eq][leading0][stateId];
								if(nway0[neq][nlead][newIdx]>=mod)nway0[neq][nlead][newIdx]-=mod;
								if(nway1[neq][nlead][newIdx]>=mod)nway1[neq][nlead][newIdx]-=mod;
								if(nway1[neq][nlead][newIdx]>=mod)nway1[neq][nlead][newIdx]-=mod;
							}
						}
					}
            	}
            }
        	var tmp=way0;way0=nway0;nway0=tmp;
        	var tmp2=way1;way1=nway1;nway1=tmp2;
        }

        long result = 0L;
        for (int eq = 0; eq < 2; eq++) {
        	for (int leading0 = 0; leading0 < 2; ++leading0) {
        		for (int stateId = 0; stateId < totalStateSize; stateId++) {
					writeMultiIndex(stateId, state);
					if (isTarget(state)) {
						result += way1[eq][leading0][getFlatIndex(state)];
						result %= mod;
					}
        		}
        	}
        }
        return result;
    }

    
    
    
    public List<Long> enumerate(long N) {
        int[] digits = toDigits(N);
        dpShape=dpShape();
        numStates = dpShape.length;
        List<Long> results = new ArrayList<>();
        dfsEnumerate(1, 1, initialState(), 0L, results, digits, digits.length-1);
        return results;
    }

    private void dfsEnumerate(int eq, int leading0, int[] state, long prefix, List<Long> results, int[] digits, int i) {
        if (i == -1) {
            if (isTarget(state)) results.add(prefix);
            return;
        }
        int limit = eq == 1 ? digits[digits.length-1-i] : (radix-1);
        int[] newState = new int[dpShape.length];
        for (int d = 0; d <= limit; ++d) {
        	if (transition(state, eq, leading0, d, i, newState)) {
	            int neq=(eq == 1 && d == limit ? 1 : 0);
	            int nlead=leading0 & (d == 0 ? 1 : 0);
	            dfsEnumerate(neq, nlead, newState, prefix * radix + d, results, digits, i-1);
        	}
        }
    }
    
    private int[] toDigits(long N) {
        int len = 0;
        long tmp = N;
        while (tmp > 0) { tmp /= radix; len++; }
        if (len == 0) len = 1; // N = 0 の場合
        int[] digits = new int[len];
        for (int i = len - 1; i >= 0; i--) {
            digits[i] = (int)(N % radix);
            N /= radix;
        }
        return digits;
    }
}