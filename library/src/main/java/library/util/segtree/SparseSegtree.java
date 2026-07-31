package library.util.segtree;

import java.util.ArrayList;

import library.util.Intervals;
import library.util.Longs;
import library.util.MathUtils;
/**
 * 
 * @param <T>
 * verified:https://atcoder.jp/contests/abc403/submissions/70710529
 */
public class SparseSegtree<T> {
	public T[] v;
	int inputN;
	Node<T> root;
	int bitLength = 63;
	T identity = null;
	java.util.function.BinaryOperator<T> op;
	
	public SparseSegtree(long n, java.util.function.BinaryOperator<T> op) {
		bitLength = MathUtils.floorLog2(2*Long.highestOneBit(Math.max(1, n-1)));
		root = new Node<>(0L, 1L << bitLength);
		this.op = op;
	}
	
	public SparseSegtree(long n, java.util.function.BinaryOperator<T> op, T identity) {
		this.identity = identity;
		bitLength = MathUtils.floorLog2(2*Long.highestOneBit(Math.max(1, n-1)));
		root = new Node<>(0L, 1L << bitLength);
		root.value = identity;
		this.op = op;
		
	}

	
	//[left, right)を表す
    public static class Node<T> {
		Node<T> leftNode;
		Node<T> rightNode;
		Node<T> parent;
		long left;
		long right;
		int numberOfNodes = 1;	
		
		public Node(long left, long right) {
			this.left = left;
			this.right = right;
		}
		
		int id = -1;
		T value;
		int bit;
		
		public boolean isLeftEmpty() {
			return leftNode == null;
		}
		
		public boolean isRightEmpty() {
			return rightNode == null;
		}
		
		public Node<T> left() {
			if (isLeftEmpty()) throw new AssertionError();
			return leftNode;
		}
		
		public Node<T> right() {
			if (isRightEmpty()) throw new AssertionError();
			return rightNode;
		}
		
		public Node<T> parent() {
			return parent;
		}
		
		/**
		 * bit(), bit()+1, .., bitLength-1番目までのbitが決定済み
		 * @return
		 */
		public int bit() {
			return bit;
		}
		
		public T value() {
			return value;
		}
		
		public ArrayList<Node<T>> childs() {
			ArrayList<Node<T>> ret=new ArrayList<>();
			if (!isLeftEmpty()) ret.add(leftNode);
			if (!isRightEmpty()) ret.add(rightNode);
			return ret;
		}
		
		
		public int id() {
			if (id == -1) throw new AssertionError();
			return id;
		}
	}
    
    public Node<T> root() {
    	return root;
    }
    
    /**
     * [i, i+1)にvalを割り当ててtrueを返す。
     * 既に同じ値が割り当てられている場合はfalseを返す。
     * @param i
     * @param val
     * @return
     */
    public boolean set(long i, T val) {
    	var node=getNode(i);
    	if (node.value != null && node.value.equals(val)) return false;
    	node.value = val;
    	while(node != root) {
    		node.parent.value = node.value;
    		if (!node.parent.isLeftEmpty() && !node.parent.isRightEmpty()) {
    			node.parent.value = op.apply(node.parent.leftNode.value, node.parent.rightNode.value);
    		}
    		node = node.parent;
    	}
    	return true;
    }
    
    public void mul(long i, T val) {
    	var node=getNode(i);
    	node.value = node.value == identity ? val : op.apply(val, node.value);
    	while(node != root) {
    		node.parent.value = node.value;
    		if (!node.parent.isLeftEmpty() && !node.parent.isRightEmpty()) {
    			node.parent.value = op.apply(node.parent.leftNode.value, node.parent.rightNode.value);
    		}
    		node = node.parent;
    	}
    }
    
    public T prodAll() {
    	return root.value;
    }
    
    public T fold(long l, long r) {
    	if(r-l<=0)return identity;
    	return fold(0, 1L<<bitLength, l, r, root);
    }

    private T fold(long a, long b, long l, long r, Node<T> node) {
    	if(!Intervals.hasOverlap(a, b, l, r))return identity;
    	if(l<=a&&b<=r)return node.value;
    	long middle=(a+b)/2;
    	if(node.isLeftEmpty() && node.isRightEmpty()) return identity;
    	if(node.isLeftEmpty()) return fold(middle, b, l, r, node.rightNode);
    	if(node.isRightEmpty()) return fold(a, middle, l, r, node.leftNode);
    	T lv=fold(a, middle, l, r, node.leftNode);
    	T rv=fold(middle, b, l, r, node.rightNode);
    	if (lv == identity) return rv;
    	if (rv == identity) return lv;
    	return op.apply(lv, rv);
    }

    public T get(long v) {
    	return getNode(v).value();
    }
    
    /**
     * [v, v+1) を表すノード
     * @param v
     * @return
     */
	private Node<T> getNode(long v) {
		if (v < 0) throw new AssertionError();
		if (v >= 1L << bitLength) throw new AssertionError();
		Node<T> cur = root;
		for (int i = bitLength - 1; i >= 0; i--) {
			if (Longs.bitAt(v, i) == 0) {
				if (cur.isLeftEmpty()) {
					cur.leftNode= new Node<>(cur.left, (cur.left+cur.right)/2);
					cur.leftNode.parent = cur;
					cur.leftNode.value = identity;
				}
				cur = cur.leftNode;
			} else {
				if (cur.isRightEmpty()) {
					cur.rightNode= new Node<>((cur.left+cur.right)/2, cur.right);
					cur.rightNode.parent = cur;
					cur.rightNode.value = identity;
				}
				cur = cur.rightNode;
			}
		}
		return cur;
	}
	
}