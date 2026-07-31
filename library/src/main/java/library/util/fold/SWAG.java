package library.util.fold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

import library.util.algebra.strategy.monoid.MonoidStrategy;

public class SWAG<T> {
	private final MonoidStrategy<T> strategy;
	private ArrayList<T> frontValues = new ArrayList<>();
	private ArrayList<T> frontProducts = new ArrayList<>();
	private ArrayList<T> backValues = new ArrayList<>();
	private ArrayList<T> backProducts = new ArrayList<>();
	
	public SWAG(MonoidStrategy<T> strategy) {
		this.strategy = strategy;
	}
	
	public void addLast(T value) {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		backValues.add(value);
		T product = backProducts.isEmpty() ? value : strategy.mul(backProducts.get(backProducts.size() - 1), value);
		backProducts.add(product);
	}
	
	public T pollFirst() {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		if (isEmpty()) throw new NoSuchElementException();
		if (frontValues.isEmpty()) moveBackToFront();
		frontProducts.remove(frontProducts.size() - 1);
		return frontValues.remove(frontValues.size() - 1);
	}
	
	public T fold() {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		if (frontProducts.isEmpty() && backProducts.isEmpty()) return strategy.identity();
		if (frontProducts.isEmpty()) return backProducts.get(backProducts.size() - 1);
		if (backProducts.isEmpty()) return frontProducts.get(frontProducts.size() - 1);
		return strategy.mul(frontProducts.get(frontProducts.size() - 1), backProducts.get(backProducts.size() - 1));
	}
	
	public int size() {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		return frontValues.size() + backValues.size();
	}
	
	public boolean isEmpty() {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		return size() == 0;
	}
	
	// 未テスト
	public boolean isNonEmpty() {
		return size() != 0;
	}
	
	// 未テスト
	public void clear() {
		frontValues.clear();
		frontProducts.clear();
		backValues.clear();
		backProducts.clear();
	}
	
	private void moveBackToFront() {
		//https://atcoder.jp/contests/abc456/submissions/75498467
		// a = front, b = back と置く。
		// a[n] .. a[1]a[0] b[0]b[1] .. b[m] のように並んでいる。
		// いま a が empty なので、swap(front, back); reverse(front); として pollFirst できる形に戻す。

		{//swap
			ArrayList<T> tmp = frontValues;
			frontValues = backValues;
			backValues = tmp;
		}
		Collections.reverse(frontValues);
		backValues.clear();
		
		frontProducts.clear();
		for (T value : frontValues) {
			T product = frontProducts.isEmpty() ? value : strategy.mul(value, frontProducts.get(frontProducts.size() - 1));
			frontProducts.add(product);
		}
		backProducts.clear();
	}
}
