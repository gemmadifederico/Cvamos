package dtu.qpms.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HammingDistance<T> {

	private CostMapping<T> costs;
	private AttributeMapping<T> attribs;
	private Map<Character, T> map;
	
	public HammingDistance(CostMapping<T> costs, AttributeMapping<T> attribs, Map<Character, T> map) {
		this.costs = costs;
		this.attribs = attribs;
		this.map = map;
	}

	public int distance(String str1, String str2) {
		//System.err.println(str1 + " -- " + str2);
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

	public double distanceAttributes(HashMap<Character,  String> stringAttribs, HashMap<Character, String> list) {
		double totDistance = 0;
		// the motif and the substring matches, now we have to check the attributes
		// a motif can have different lists of attributes
		// given each pair motif+list of attributes, we compare it with the actual substring+list of attributes
		// in particular, we have to check if the attributes in the motif are contained in the list of attributes of the substring
		// and that their values are more or less similar.
		if(stringAttribs.isEmpty()) {
			if(list.isEmpty()) {
				totDistance = 0;
			}
		} else {
	        if (list.keySet().equals(stringAttribs.keySet())) {
	            // there is a match in keys
	        	// now I have to check the values
	        	for(Entry<Character, String> b : list.entrySet()) {
		        	double tolerance = attribs.getTolerance(map.get(b.getKey()));
		        	/*System.out.println("comparing: " + Double.parseDouble(b.getValue()) + 
					" with: " + Double.parseDouble(stringAttribs.get(b.getKey()))
					+ "tolerance: " + tolerance); */
						if((Double.parseDouble(b.getValue()) - tolerance) <= (Double.parseDouble(stringAttribs.get(b.getKey()))) &&
								(Double.parseDouble(stringAttribs.get(b.getKey()))) <= (Double.parseDouble(b.getValue()) + tolerance)) {
								totDistance += 0;
						} else {
							double x = (Double.parseDouble(b.getValue()) + tolerance) - (Double.parseDouble(stringAttribs.get(b.getKey())));
							double y = (Double.parseDouble(b.getValue()) - tolerance) - (Double.parseDouble(stringAttribs.get(b.getKey())));
							totDistance += Math.min(Math.abs(x), Math.abs(y));
						}
	        	}
		    } else {
		    	totDistance += 200;
		    }
		}
		return totDistance;
	}
	
	public boolean hasSameKey(List<HashMap<Character,String>> a, HashMap<Character, String> b) {
		for(HashMap<Character, String> map: a) {
	        if (map.keySet().equals(b.keySet())) {
	            return true;
		    }
		}
		return false;
		
	}
}
