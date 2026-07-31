package library.util.geometry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class CoordinateOrderedSet2D {
	Map<Long, TreeSet<Long>> xToY = new HashMap<>();
	Map<Long, TreeSet<Long>> yToX = new HashMap<>();

	/**
	 * 集合に含まれる相異なる点の個数
	 */
	private int size = 0;
	
	public void add(long x, long y) {
		if (!xToY.containsKey(x)) {
			xToY.put(x, new TreeSet<>());
		}
		if (!yToX.containsKey(y)) {
			yToX.put(y, new TreeSet<>());
		}
		if (xToY.get(x).add(y)) {
			yToX.get(y).add(x);
			size++;
		}
	}
	
	public boolean remove(long x, long y) {
		if (!xToY.containsKey(x)) {
			return false;
		}
		if (!xToY.get(x).contains(y)) return false;
		xToY.get(x).remove(y);
		yToX.get(y).remove(x);
		size--;
		return true;
	}
	
	public Long ceilXFixingY(long x, long y) {
		if (!yToX.containsKey(y)) return null;
		return yToX.get(y).ceiling(x);
	}
	
	public Long floorXFixingY(long x, long y) {
		if (!yToX.containsKey(y)) return null;
		return yToX.get(y).floor(x);
	}
	
	public Long ceilYFixingX(long x, long y) {
		if (!xToY.containsKey(x)) return null;
		return xToY.get(x).ceiling(y);
	}
	
	public Long floorYFixingX(long x, long y) {
		if (!xToY.containsKey(x)) return null;
		return xToY.get(x).floor(y);
	}
	
	public boolean contains(long x, long y) {
		return xToY.containsKey(x) && xToY.get(x).contains(y);
	}

	/**
	 * 集合に含まれる相異なる点の個数を返します。
	 *
	 * 未テスト
	 * @return 集合に含まれる点の個数
	 * @complexity O(1)
	 */
	public int size() {
		return size;
	}

	/**
	 * 集合が空であるかどうかを判定します。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 点の個数が0であればtrue、そうでなければfalseを返します。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(1)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	// 未テスト
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * 集合の内容を文字列として表します。
	 *
	 * @return 点集合の文字列表現
	 * @complexity O(N \log N)
	 */
	// 未テスト
	@Override
	public String toString() {
		if (size == 0) {
			return "空集合";
		} else {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (long x : new TreeSet<>(xToY.keySet())) {
				for (long y : xToY.get(x)) {
					if (!first) sb.append("\n");
					sb.append("(").append(x).append(", ").append(y).append(")");
					first = false;
				}
			}
			return sb.toString();
		}
	}

	/**
	 * デバッグ用に集合の内容を標準出力に出力します。
	 *
	 * 未テスト
	 * @complexity O(N \log N)
	 */
	public void dump() {
		System.out.println(toString());
	}

	/**
	 * この2次元点集合と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は点数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CoordinateOrderedSet2D)) return false;
		CoordinateOrderedSet2D other = (CoordinateOrderedSet2D) obj;
		return this.xToY.equals(other.xToY);
	}

	/**
	 * この2次元点集合のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は点数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(xToY);
	}
}
