package library.util;


import java.util.PrimitiveIterator;

import library.util.collections.IntArrayList;

public class RefineSet {
	int[] elements;
	int numberOfBlocks=0;
	int[] first;
	int[] last;
	int[] elementToLocation;
	int[] elementToBlockId;
	IntArrayList markedBlocks = new IntArrayList();
	boolean[] isMarkedBlock;
	int[] markedCountOfBlock;

	public RefineSet(int n) {
		elements = new int[n];
		numberOfBlocks = n>0?1:0;
		first = new int[n];
		last = new int[n];
		elementToLocation = new int[n];
		elementToBlockId = new int[n];
		isMarkedBlock = new boolean[n];
		markedCountOfBlock = new int[n];
		for (int i = 0; i < n; i++) {
			elements[i] = i;
			elementToLocation[i] = i;
		}
		first[0] = 0;
		last[0] = n;
	}	
	
	public boolean mark(int e) {
		int id = elementToBlockId[e];
		if (!isMarkedBlock[id]) {
			isMarkedBlock[id] = true;
			markedBlocks.add(id);
		}
		int i = elementToLocation[e];
		int j = first[id] + markedCountOfBlock[id];
		if (i < j) {
			return false;
		}
		int f = elements[j];
		{
			{
				var tmp = elementToLocation[e];
				elementToLocation[e] = elementToLocation[f];
				elementToLocation[f] = tmp;
			}
		}
		{
			var tmp = elements[i];
			elements[i] = elements[j];
			elements[j] = tmp;
		}
		markedCountOfBlock[id]++;
		return true;
	}
	
	public void refine() {
		for (int id : markedBlocks) {
			if (markedCountOfBlock[id] != last[id] - first[id]) {
				if(2 * markedCountOfBlock[id] > last[id]-first[id]) {
					first[numberOfBlocks]=first[id]+markedCountOfBlock[id];
					last[numberOfBlocks]=last[id];
					last[id]=first[numberOfBlocks];
				} else {
					first[numberOfBlocks]=first[id];
					last[numberOfBlocks]=first[id]+markedCountOfBlock[id];
					first[id]=last[numberOfBlocks];
				}
				for (int i = first[numberOfBlocks]; i < last[numberOfBlocks]; i++) {
					elementToBlockId[elements[i]] = numberOfBlocks;
				}
				numberOfBlocks++;
			}
			markedCountOfBlock[id] = 0;
			isMarkedBlock[id] = false;
		}
		markedBlocks.clear();
	}
	
	public int blockid(int e) {
		return elementToBlockId[e];
	}
	
	public int getBlockSize(int blockid) {
		return last[blockid] - first[blockid];
	}
	
	public int blockCount() {
		return numberOfBlocks;
	}
	
	public boolean inSameBlock(int e, int f) {
		return elementToBlockId[e] == elementToBlockId[f];
	}
	
	public int blockIdToRepresentative(int id) {
		return elements[first[id]];
	}

	public int elementToRepresentative(int v) {
		return elements[first[blockid(v)]];
	}
	
	public Iterable<Integer> elementsInBlock(int id) {
		return () -> new PrimitiveIterator.OfInt() {
			int i = first[id];
			
			@Override
			public boolean hasNext() {
				return i < last[id];
			}
			
			@Override
			public int nextInt() {
				return elements[i++];
			}
		};
		
	}
	
	public int numberOfBlocks() {
		return numberOfBlocks;
	}
	
	/**
	 * この集合分割を表す文字列を返します。
	 *
	 * <p>計算量: $O(N)$（$N$ は要素数）</p>
	 *
	 * @return 集合分割の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfBlocks; i++) {
			sb.append("{");
			for (int j = first[i]; j < last[i]; j++) {
				sb.append(elements[j]);
				if (j < last[i] - 1) sb.append(",");
			}
			sb.append("}");
		}
		return sb.toString();
	}

	/**
	 * 内部状態を標準出力に出力する。
	 *
	 * 未テスト
	 * @complexity O(N)
	 */
	public void dump() {
		System.out.println(toString());
	}
}
