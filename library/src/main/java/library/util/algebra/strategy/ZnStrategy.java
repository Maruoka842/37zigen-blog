package library.util.algebra.strategy;

/**
 * 剰余環 Z/nZ 上の可換環としての代数的構造。
 */
public class ZnStrategy implements CommutativeRingStrategy<Long> {
	protected final long mod;

	public ZnStrategy(long mod) {
		this.mod = mod;
	}

	/**
	 * 法 n を返す。
	 * 未テスト。
	 * 数学的表記: n where this = Z/nZ。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 == コンストラクタに渡した mod。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: なし。
	 * @return 法 n。
	 */
	public long getMod() {
		return mod;
	}

	@Override
	public Long zero() {
		return 0L;
	}

	@Override
	public Long one() {
		return 1L % mod;
	}

	@Override
	public Long add(Long a, Long b) {
		long res = (a + b) % mod;
		if (res < 0) res += mod;
		return res;
	}

	@Override
	public Long mul(Long a, Long b) {
		long res = (a % mod) * (b % mod) % mod;
		if (res < 0) res += mod;
		return res;
	}

	@Override
	public Long neg(Long a) {
		long res = (-a) % mod;
		if (res < 0) res += mod;
		return res;
	}

	@Override
	public boolean equals(Long a, Long b) {
		long ra = a % mod;
		if (ra < 0) ra += mod;
		long rb = b % mod;
		if (rb < 0) rb += mod;
		return ra == rb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ZnStrategy that = (ZnStrategy) o;
		return mod == that.mod;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(mod) ^ getClass().hashCode();
	}

	@Override
	public int hashCode(Long a) {
		long r = a % mod;
		if (r < 0) r += mod;
		return Long.hashCode(r);
	}
}
