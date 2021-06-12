package mapper.util;

import java.util.ArrayList;

// Not a complete implementation 

public class AvgMap<K extends Number, V> {
	private ArrayList<Entry<K, V>> entries;
	private double sum, average;
	
	private static final int INITIAL_SIZE = 16;
	
	public AvgMap() {
		entries = new ArrayList<>(INITIAL_SIZE);
	}
	
	public void put(K key, V value) {
		final double keyValue = key.doubleValue();
		
		if (average == 0.0) {
			add(key, value);
			
			if (entries.size() >= 2) {
				for(Entry<K, V> entry : entries) {
					sum += entry.key.doubleValue();
				}
				
				average = sum / entries.size();
			}
		} else {
			int avgPos = (int)(keyValue / average);
			if (avgPos >= entries.size()) {
				add(key, value);
			} else {
				Entry<K, V> entryAtPos = entries.get(avgPos);
				
				K entryKey = entryAtPos.key;
				int newPos;
				
				if (entryKey.doubleValue() < keyValue) {				
					for(newPos = avgPos - 1; newPos > 0; newPos--) {
						 entryAtPos = entries.get(newPos);
						 entryKey = entryAtPos.key;
						 
						 if (entryKey.doubleValue() >= keyValue) {
							 break;
						 }
					}
					
					add(newPos, key, value);	
				} else {
					for(newPos = avgPos + 1; newPos < entries.size(); newPos++) {
						 entryAtPos = entries.get(newPos);
						 entryKey = entryAtPos.key;
						 
						 if (entryKey.doubleValue() <= keyValue) {
							 break;
						 }
					}
					
					add(newPos, key, value);
				}
			}
			
			sum += key.doubleValue();
			average = sum / entries.size();
		}
	}
	
	private int getIndex(K key) {
		final double keyValue = key.doubleValue();
		
		if (average == 0.0) {
			
			int i = 0;
			for(Entry<K, V> entry : entries) {
				if (entry.key == key) {
					return i;
				}
				i++;
			}
			
			return -1;
		} else {
			int avgPos = (int)(keyValue / average);
			if (avgPos >= entries.size()) {
				avgPos = entries.size() - 1;
			}
			
			Entry<K, V> entryAtPos = entries.get(avgPos);
			
			K entryKey = entryAtPos.key;
			int newPos;
			
			if (entryKey.doubleValue() < keyValue) {				
				for(newPos = avgPos - 1; newPos > 0; newPos--) {
					 entryAtPos = entries.get(newPos);
					 entryKey = entryAtPos.key;
					 
					 if (entryKey == key) {
						 return newPos;
					 }
				}
				
				return -1;
			} else {
				for(newPos = avgPos + 1; newPos < entries.size(); newPos++) {
					 entryAtPos = entries.get(newPos);
					 entryKey = entryAtPos.key;
					 
					 if (entryKey == key) {
						 return newPos;
					 }
				}
				
				return -1;
			}
		}
	}
	
	public void remove(K key) {
		int index = getIndex(key);
		
		if (index == -1) {
			return;
		}
		
		entries.remove(index);
	}
	
	public V get(K key) {
		int index = getIndex(key);
		
		if (index == -1) {
			return null;
		}
		
		return entries.get(index).value;
	}
	
	private void add(K key, V value) {
		entries.add(new Entry<K, V>(key, value));
	}
	
	private void add(int index, K key, V value) {
		entries.add(index, new Entry<K, V>(key, value));
	}

	static class Entry<K, V> {
		protected K key;
		protected V value;
		
		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
}
