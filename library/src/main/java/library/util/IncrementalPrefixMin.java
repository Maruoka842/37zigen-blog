package library.util;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * prefix minがなす狭義減少列を管理する。
 * @param <K>
 * @param <V>
 */
public class IncrementalPrefixMin<K, V extends Comparable<? super V>> {
	TreeMap<K, V> map=new TreeMap<>();
	
	public void put(K key, V value) {
		var floor=map.floorEntry(key);
		if(floor!=null&&floor.getValue().compareTo(value)<=0)return;
		while(true) {
			var ceil=map.ceilingEntry(key);
			if(ceil==null)break;
			if(ceil.getValue().compareTo(value)>=0) {
				map.remove(ceil.getKey());
			}else {
				break;
			}
		}
		map.put(key, value);
	}
	
	public Entry<K, V> lowerEntry(K key) {
		return map.lowerEntry(key);
	}
	
	public Entry<K, V> floorEntry(K key) {
		return map.floorEntry(key);
	}
	
	public Entry<K, V> higherEntry(K key) {
		return map.higherEntry(key);
	}
	
	public Entry<K, V> ceilingEntry(K key) {
		return map.ceilingEntry(key);
	}	

}
