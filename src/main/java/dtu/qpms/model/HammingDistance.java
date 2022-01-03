package dtu.qpms.model;

import java.util.Map;

public class HammingDistance<T> {

	private CostMapping<T> costs;
	private Map<Character, T> map;
	
	public HammingDistance(CostMapping<T> costs, Map<Character, T> map) {
		this.costs = costs;
		this.map = map;
	}

	public int distance(String str1, String str2) {
		int count = 0;
		for (int i = 0; i < str1.length(); i++) {
			char c1 = str1.charAt(i);
			char c2 = str2.charAt(i);
			if (c1 != c2) {
				count += costs.getCost(map.get(c1), map.get(c2));
			}
		}
		return count;
	}
}
