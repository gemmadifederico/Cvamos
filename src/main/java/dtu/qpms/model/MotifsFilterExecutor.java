package dtu.qpms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MotifsFilterExecutor <T> {

	private static final char TO_REPLACE = '!';
	private Set<String> strings;
	private Set<String> motifs;
	private CostMapping<T> costs;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;
	
	public MotifsFilterExecutor(CostMapping<T> costs) {
		this.strings = new HashSet<String>();
		this.motifs = new HashSet<String>();
		this.charsToValues = new HashMap<Character, T>();
		this.valuesToChars = new HashMap<T, Character>();
		this.costs = costs;
	}
	
	public boolean addString(Sequence<T> string) {
		return add(string, strings);
	}
	
	public boolean addMotif(Sequence<T> motif) {
		return add(motif, motifs);
	}
	
	private boolean add(Sequence<T> string, Set<String> set) {
		String s = "";
		for (int i = 0; i < string.size(); i++) {
			T t = string.get(i);
			if (!valuesToChars.containsKey(t)) {
				char v = (char) ('@' + valuesToChars.size() + 1);
				valuesToChars.put(t, v);
				charsToValues.put(v, t);
			}
			s += valuesToChars.get(t);
		}
		return set.add(s);
	}
	
	public Set<Sequence<T>> filter(double maxDistance, T replace) {
		Set<Sequence<T>> toReturn = new HashSet<Sequence<T>>();
		for (String s : filterStrings(maxDistance)) {
			Sequence<T> seq = new Sequence<T>();
			for (int i = 0; i < s.length(); i++) {
				Character c = s.charAt(i);
				if (c.equals(TO_REPLACE)) {
					seq.add(replace);
				} else {
					seq.add(charsToValues.get(c));
				}
			}
			toReturn.add(seq);
		}
		return toReturn;
	}
	
	private Set<String> filterStrings(double maxDistance) {
		if (motifs.isEmpty()) {
			return strings;
		}
		HammingDistance<T> hamming = new HammingDistance<T>(costs, charsToValues);
		Set<String> toReturn = new HashSet<String>();
		int motifLength = motifs.iterator().next().length();
		for (String s : strings) {
			List<Integer> indexesWithMotifs = new ArrayList<Integer>();
			for (String m : motifs) {
				int stringLength = s.length();
				if (stringLength >= motifLength) {
					for (int i = 0; i <= stringLength - motifLength; i++) {
						if (hamming.distance(s.substring(i, i + motifLength), m) <= maxDistance) {
							indexesWithMotifs.add(i);
							i += motifLength - 1;
						}
					}
				}
			}
			if (indexesWithMotifs.size() == 0) {
				toReturn.add(s);
			} else {
				Collections.sort(indexesWithMotifs);
				
				List<Integer> indexesWithMotifsToReplace = new ArrayList<Integer>();
				for (int i = 0; i < indexesWithMotifs.size(); i++) {
					if (!(i > 0 && indexesWithMotifs.get(i) < indexesWithMotifs.get(i - 1) + motifLength)) {
						indexesWithMotifsToReplace.add(indexesWithMotifs.get(i));
					}
				}
				
				String replacement = "";
				for (int i = 0; i < indexesWithMotifsToReplace.size(); i++) {
					if (i==0) {
						replacement += s.substring(0, indexesWithMotifsToReplace.get(i));
					} else {
						replacement += s.substring(indexesWithMotifsToReplace.get(i - 1) + motifLength, indexesWithMotifsToReplace.get(i));
					}
					replacement += TO_REPLACE;
					if (i + 1 == indexesWithMotifsToReplace.size()) {
						replacement += s.substring(indexesWithMotifsToReplace.get(i) + motifLength);
					}
				}
				toReturn.add(replacement);
			}
		}
		return toReturn;
	}
}
