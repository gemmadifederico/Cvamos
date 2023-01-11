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

	public double distanceAttributes(HashMap<Character,  List<String>> stringAttribs, HashMap<Character, List<String>> motifAttribs) {
		// the motif and the substring matches, now we have to check the attributes
		// a motif can have different lists of attributes
		// given each pair motif+list of attributes, we compare it with the actual substring+list of attributes
		// in particular, we have to check if the attributes in the motif are contained in the list of attributes of the substring
		// and that their values are more or less similar.
		double totDistance = 0;
		for(Entry<Character, List<String>> motifAttr : motifAttribs.entrySet()) {
			if(stringAttribs.containsKey(motifAttr.getKey())) {
				// the substring contains the given motif attrib
				double tolerance = attribs.getTolerance(map.get(motifAttr.getKey()));
				/* System.out.println("comparing: " + Double.parseDouble(motifAttr.getValue().get(0)) + 
						" with: " + Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))
						+ "tolerance: " + tolerance); */
				if((Double.parseDouble(motifAttr.getValue().get(0)) - tolerance) <= (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))) &&
						(Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))) <= (Double.parseDouble(motifAttr.getValue().get(0)) + tolerance)) {
						totDistance += 0;
				} else {
					double a = (Double.parseDouble(motifAttr.getValue().get(0)) + tolerance) - (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0)));
					double b = (Double.parseDouble(motifAttr.getValue().get(0)) - tolerance) - (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0)));
					totDistance += Math.min(Math.abs(a), Math.abs(b));
				}
			} else {
				totDistance += 200;
			}
		}
		// System.err.println(totDistance);
		return totDistance;
	}
}
