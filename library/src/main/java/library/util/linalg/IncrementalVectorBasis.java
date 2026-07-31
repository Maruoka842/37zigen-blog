package library.util.linalg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import library.util.algebra.instance.VectorSpaceElement;
import library.util.algebra.strategy.VectorSpaceStrategy;

/**
 * 体 K 上のベクトル空間 K^(B) の部分空間を、追加されたベクトル列の RREF 基底として管理する。
 * RREF の pivot 順序は構築時に与えた comparator、または B の自然順序で定まる。
 *
 * @param <K> 係数体の型
 * @param <B> 基底の型
 */
public class IncrementalVectorBasis<K, B> {
	/** ベクトル空間 K^(B) の演算ストラテジ。 */
	private final VectorSpaceStrategy<K, B> strategy;
	/** pivot の全順序を定める比較器。 */
	private final Comparator<? super B> comparator;
	/** pivot p から RREF 行 r_p への写像。各 r_p は r_p(p)=1_K を満たす。 */
	private final TreeMap<B, VectorSpaceElement<K, B>> rows;

	/**
	 * strategy と comparator を指定して空の RREF 基底を構築する。
	 * 未テスト。
	 * 事前条件: strategy != null, comparator != null。
	 * 事後条件: span(this)= {0}, dimension()=0, this.strategy()==strategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: strategy と comparator の参照を保持し、所有権は移動しない。
	 * 例外・未定義条件: strategy == null または comparator == null のとき以後のメソッド呼び出しは未定義。
	 * @param strategy ベクトル空間 K^(B) の演算ストラテジ
	 * @param comparator pivot 順序を定める比較器
	 */
	public IncrementalVectorBasis(VectorSpaceStrategy<K, B> strategy, Comparator<? super B> comparator) {
		this.strategy = strategy;
		this.comparator = comparator;
		this.rows = new TreeMap<>(comparator);
	}

	/**
	 * strategy と B の自然順序を指定して空の RREF 基底を構築する。
	 * 未テスト。
	 * 事前条件: strategy != null かつ 全ての非零入力 x の supp(x) の元は Comparable<? super B> を実装する。
	 * 事後条件: span(this)= {0}, dimension()=0, this.strategy()==strategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: strategy の参照を保持し、所有権は移動しない。
	 * 例外・未定義条件: strategy == null、または比較不能な基底元を含む入力を渡したとき未定義。
	 * @param strategy ベクトル空間 K^(B) の演算ストラテジ
	 */
	public IncrementalVectorBasis(VectorSpaceStrategy<K, B> strategy) {
		this(strategy, naturalOrder());
	}

	/**
	 * 保持するベクトル空間 K^(B) の演算ストラテジを返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 == 構築時に渡した strategy。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: strategy の参照を返し、所有権は移動しない。
	 * 例外・未定義条件: なし。
	 * @return ベクトル空間 K^(B) の演算ストラテジ
	 */
	public VectorSpaceStrategy<K, B> strategy() {
		return strategy;
	}

	/**
	 * RREF 基底の次元を返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 = rank(this.rows) = |{pivot(r) | r ∈ RREF 基底}|。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: なし。
	 * @return 管理する部分空間の次元
	 */
	public int dimension() {
		return rows.size();
	}

	/**
	 * RREF 基底が空か判定する。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 = true ⇔ dimension()=0。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: なし。
	 * @return dimension()=0 なら true
	 */
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	/**
	 * RREF 基底の pivot 集合を pivot 順に返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 = [p_0,...,p_{d-1}], p_i は RREF 基底の pivot, i<j ⇒ comparator(p_i,p_j)<0。
	 * 副作用: なし。
	 * 計算量: O(d)。ただし d=dimension()。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 戻り値 List は新規。基底元 B の参照は共有する。
	 * 例外・未定義条件: なし。
	 * @return pivot 集合を昇順に並べたリスト
	 */
	public List<B> pivots() {
		return new ArrayList<>(rows.keySet());
	}

	/**
	 * RREF 基底ベクトルを pivot 順に返す。
	 * 未テスト。
	 * 事前条件: なし。
	 * 事後条件: 戻り値 = [r_{p_0},...,r_{p_{d-1}}] かつ各 r_{p_i} は RREF 行のコピー。
	 * 副作用: なし。
	 * 計算量: O(d + Σ_{r∈rows}|supp(r)|)。ただし d=dimension()。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: 戻り値 List と各 VectorSpaceElement および各 Map は新規。係数 K と基底 B の参照は共有する。
	 * 例外・未定義条件: なし。
	 * @return RREF 基底ベクトルのコピーのリスト
	 */
	public List<VectorSpaceElement<K, B>> basis() {
		List<VectorSpaceElement<K, B>> res = new ArrayList<>(rows.size());
		for (VectorSpaceElement<K, B> row : rows.values()) res.add(copy(row));
		return res;
	}

	/**
	 * v を現在の RREF 基底で簡約した剰余を返す。
	 * 未テスト。
	 * 事前条件: v != null, v.strategy() は this.strategy と同じ体上の演算を定める, v.val() は正規形。
	 * 事後条件: 戻り値 = v - Σ_{p∈P} c_p r_p かつ ∀p∈P, 戻り値(p)=0_K。ここで P は現在の pivot 集合、r_p は RREF 行。
	 * 副作用: なし。
	 * 計算量: O(d * (cost(B.hashCode/equals)+cost(K.mul)+cost(K.add)+cost(K.equals)) * S) expected。
	 * ここで d=dimension(), S は reduce 中の剰余と行の最大 support サイズ。
	 * 破壊的変更: v と RREF 基底を変更しない。
	 * 参照共有・所有権: 戻り値 VectorSpaceElement と Map は新規。係数 K と基底 B の参照は共有する。
	 * 例外・未定義条件: v == null または v が正規形でない、または互換でない strategy のとき未定義。
	 * @param v 簡約するベクトル
	 * @return 現在の RREF 基底による剰余
	 */
	public VectorSpaceElement<K, B> reduce(VectorSpaceElement<K, B> v) {
		Map<B, K> res = new HashMap<>(v.val());
		for (Map.Entry<B, VectorSpaceElement<K, B>> entry : rows.entrySet()) {
			K coefficient = res.get(entry.getKey());
			if (coefficient != null && !strategy.field().equals(coefficient, strategy.field().zero())) {
				res = strategy.sub(res, strategy.scalarMul(coefficient, entry.getValue().val()));
			}
		}
		return new VectorSpaceElement<>(res, strategy);
	}

	/**
	 * v が現在の RREF 基底の線形包に含まれるか判定する。
	 * 未テスト。
	 * 事前条件: v != null, v.strategy() は this.strategy と同じ体上の演算を定める, v.val() は正規形。
	 * 事後条件: 戻り値 = true ⇔ v ∈ span(rows)。
	 * 副作用: なし。
	 * 計算量: reduce(v) + O(1)。
	 * 破壊的変更: v と RREF 基底を変更しない。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: reduce(v) に同じ。
	 * @param v 判定するベクトル
	 * @return v ∈ span(rows) なら true
	 */
	public boolean contains(VectorSpaceElement<K, B> v) {
		return reduce(v).val().isEmpty();
	}

	/**
	 * v を追加し、線形独立なら RREF 基底を更新する。
	 * 未テスト。
	 * 事前条件: v != null, v.strategy() は this.strategy と同じ体上の演算を定める, v.val() は正規形。
	 * 事後条件: 戻り値 = true ⇔ v ∉ span(old rows)。戻り値が true なら span(new rows)=span(old rows ∪ {v}) かつ dimension()=old dimension()+1。
	 * 戻り値が false なら rows は変更されない。
	 * 副作用: 戻り値が true のとき this.rows を RREF に更新する。
	 * 計算量: O((d+1) * (cost(B.hashCode/equals)+cost(K.inv)+cost(K.mul)+cost(K.add)+cost(K.equals)) * S) expected。
	 * ここで d は追加前の dimension(), S は処理中の行の最大 support サイズ。
	 * 破壊的変更: this.rows のみ破壊的に変更し、v は変更しない。
	 * 参照共有・所有権: 追加される VectorSpaceElement と Map は新規。係数 K と基底 B の参照は共有する。
	 * 例外・未定義条件: reduce(v) に同じ。
	 * @param v 追加するベクトル
	 * @return v が追加前の線形包に含まれず、基底に追加されたなら true
	 */
	public boolean add(VectorSpaceElement<K, B> v) {
		Map<B, K> remainder = reduce(v).val();
		if (remainder.isEmpty()) return false;
		B pivot = minKey(remainder.keySet());
		VectorSpaceElement<K, B> newRow = new VectorSpaceElement<>(strategy.normalizeBy(pivot, remainder), strategy);
		for (Map.Entry<B, VectorSpaceElement<K, B>> entry : new ArrayList<>(rows.entrySet())) {
			K coefficient = entry.getValue().val().get(pivot);
			if (coefficient != null && !strategy.field().equals(coefficient, strategy.field().zero())) {
				Map<B, K> eliminated = strategy.sub(entry.getValue().val(), strategy.scalarMul(coefficient, newRow.val()));
				entry.setValue(new VectorSpaceElement<>(eliminated, strategy));
			}
		}
		rows.put(pivot, newRow);
		return true;
	}

	/**
	 * vectors を順に追加し、次元が増えた回数を返す。
	 * 未テスト。
	 * 事前条件: vectors != null かつ各 v∈vectors は add(v) の事前条件を満たす。
	 * 事後条件: 戻り値 = |{v_i | v_i ∉ span(old rows ∪ {v_j | j<i かつ add(v_j)=true})}|。
	 * 副作用: this.rows を、old rows と vectors の線形包を表す RREF に更新する。
	 * 計算量: Σ_{v∈vectors} add(v) の計算量。
	 * 破壊的変更: this.rows のみ破壊的に変更し、vectors とその要素は変更しない。
	 * 参照共有・所有権: add(v) に同じ。
	 * 例外・未定義条件: vectors == null またはいずれかの要素が add(v) の事前条件を満たさないとき未定義。
	 * @param vectors 追加するベクトル列
	 * @return 次元が増えた回数
	 */
	public int addAll(Collection<? extends VectorSpaceElement<K, B>> vectors) {
		int added = 0;
		for (VectorSpaceElement<K, B> v : vectors) if (add(v)) added++;
		return added;
	}

	/**
	 * v のコピーを返す。
	 * 未テスト。
	 * 事前条件: v != null, v.val() は正規形。
	 * 事後条件: 戻り値.val() != v.val() かつ ∀b∈B, 戻り値(b)=v(b)。
	 * 副作用: なし。
	 * 計算量: O(|supp(v)|)。
	 * 破壊的変更: v を変更しない。
	 * 参照共有・所有権: 戻り値 VectorSpaceElement と Map は新規。係数 K と基底 B の参照は共有する。
	 * 例外・未定義条件: v == null または v が正規形でないとき未定義。
	 * @param v コピー元
	 * @return v のコピー
	 */
	private VectorSpaceElement<K, B> copy(VectorSpaceElement<K, B> v) {
		return new VectorSpaceElement<>(new HashMap<>(v.val()), strategy);
	}

	/**
	 * keys の最小元を返す。
	 * 未テスト。
	 * 事前条件: keys != null かつ keys は空でない。
	 * 事後条件: 戻り値 ∈ keys かつ ∀b∈keys, comparator.compare(戻り値,b) <= 0。
	 * 副作用: なし。
	 * 計算量: O(|keys| * cost(comparator.compare))。
	 * 破壊的変更: keys を変更しない。
	 * 参照共有・所有権: 戻り値は keys 内の基底元参照。
	 * 例外・未定義条件: keys == null または keys が空、または比較不能な基底元を含むとき未定義。
	 * @param keys 候補集合
	 * @return comparator 最小の基底元
	 */
	private B minKey(Collection<B> keys) {
		return Collections.min(keys, comparator);
	}

	/**
	 * B の自然順序を使う比較器を返す。
	 * 未テスト。
	 * 事前条件: 比較される全ての x は Comparable<? super B> を実装し、x.compareTo(y) が全順序を定める。
	 * 事後条件: 戻り値.compare(x,y)=((Comparable<? super B>)x).compareTo(y)。
	 * 副作用: なし。
	 * 計算量: O(1)。
	 * 破壊的変更: なし。
	 * 参照共有・所有権: なし。
	 * 例外・未定義条件: 比較不能な基底元を比較したとき ClassCastException。
	 * @param <B> 基底の型
	 * @return B の自然順序を使う比較器
	 */
	@SuppressWarnings("unchecked")
	private static <B> Comparator<? super B> naturalOrder() {
		return (x, y) -> ((Comparable<? super B>) x).compareTo(y);
	}
}
