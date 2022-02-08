package dtu.qpms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class MotifsFilterExecutor <T, K> {

	private static final char TO_REPLACE = '!';
	private Set<Pair<String, Sequence<K>>> strings;
	private Set<Pair<String, Sequence<K>>> motifs;
	private CostMapping<T> costs;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;
	
	public MotifsFilterExecutor(CostMapping<T> costs) {
		this.strings = new HashSet<Pair<String, Sequence<K>>>();
		this.motifs = new HashSet<Pair<String, Sequence<K>>>();
		this.charsToValues = new HashMap<Character, T>();
		this.valuesToChars = new HashMap<T, Character>();
		this.costs = costs;
	}
	
	public boolean addString(Sequence<T> string, Sequence<K> attributes) {
		return add(string, attributes, strings);
	}
	
	public boolean addMotif(Sequence<T> motif) {
		return add(motif, null, motifs);
	}
	
	private boolean add(Sequence<T> string, Sequence<K> attributes, Set<Pair<String, Sequence<K>>> set) {
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
		return set.add(Pair.of(s, attributes));
	}
	
	public Set<Pair<Sequence<T>, Sequence<K>>> filter(double maxDistance, T replace) {
		Set<Pair<Sequence<T>, Sequence<K>>> toReturn = new HashSet<Pair<Sequence<T>, Sequence<K>>>();
		for (Pair<String, Sequence<K>> pair : filterStrings(maxDistance)) {
			String s = pair.getLeft();
			Sequence<T> seq = new Sequence<T>();
			Sequence<K> attSeq = new Sequence<K>();
			for (int i = 0; i < s.length(); i++) {
				Character c = s.charAt(i);
				if (c.equals(TO_REPLACE)) {
					seq.add(replace);
					attSeq.add(pair.getRight().get(i));
				} else {
					seq.add(charsToValues.get(c));
					attSeq.add(pair.getRight().get(i));
				}
			}
			toReturn.add(Pair.of(seq, attSeq));
		}
		return toReturn;
	}
	
	private Set<Pair<String, Sequence<K>>> filterStrings(double maxDistance) {
		if (motifs.isEmpty()) {
			return strings;
		}
		HammingDistance<T> hamming = new HammingDistance<T>(costs, charsToValues);
		Set<Pair<String, Sequence<K>>> toReturn = new HashSet<Pair<String, Sequence<K>>>();
		int motifLength = motifs.iterator().next().getLeft().length();
		for (Pair<String, Sequence<K>> pair : strings) {
			String s = pair.getLeft();
			List<Integer> indexesWithMotifs = new ArrayList<Integer>();
			for (Pair<String, Sequence<K>> motifPair : motifs) {
				String m = motifPair.getLeft();
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
				toReturn.add(Pair.of(s, pair.getRight()));
			} else {
				Collections.sort(indexesWithMotifs);
				
				List<Integer> indexesWithMotifsToReplace = new ArrayList<Integer>();
				for (int i = 0; i < indexesWithMotifs.size(); i++) {
					if (!(i > 0 && indexesWithMotifs.get(i) < indexesWithMotifs.get(i - 1) + motifLength)) {
						indexesWithMotifsToReplace.add(indexesWithMotifs.get(i));
					}
				}
				
				String replacement = "";
				Sequence<K> attributes = new Sequence<K>();
				for (int i = 0; i < indexesWithMotifsToReplace.size(); i++) {
					if (i==0) {
						replacement += s.substring(0, indexesWithMotifsToReplace.get(i));
						for (int j = 0; j < indexesWithMotifsToReplace.get(i); j++) {
							attributes.add(pair.getRight().get(j));
						}
					} else {
						replacement += s.substring(indexesWithMotifsToReplace.get(i - 1) + motifLength, indexesWithMotifsToReplace.get(i));
						for (int j = indexesWithMotifsToReplace.get(i - 1) + motifLength; j < indexesWithMotifsToReplace.get(i); j++) {
							attributes.add(pair.getRight().get(j));
						}
					}
					
					replacement += TO_REPLACE;
					attributes.add(pair.getRight().get(indexesWithMotifsToReplace.get(i)));
					
					if (i + 1 == indexesWithMotifsToReplace.size()) {
						replacement += s.substring(indexesWithMotifsToReplace.get(i) + motifLength);
						for (int j = indexesWithMotifsToReplace.get(i) + motifLength; j < s.length(); j++) {
							attributes.add(pair.getRight().get(j));
						}
					}
				}
				toReturn.add(Pair.of(replacement, attributes));
			}
		}
		return toReturn;
	}
}
