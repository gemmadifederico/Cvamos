package dtu.qpms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class MotifsFilterExecutor <T, K> {

	private static final char TO_REPLACE = '!';
	private Set<Pair<String, Sequence<K>>> strings;
	//private Set<Pair<String, Sequence<K>>> motifs;
	//private Set<Pair< Pair<String, Sequence<K>>, List<HashMap<Character, Object>>>> motifs;
	private Map<String, List<HashMap<Character, Object>>> motifs;
	private CostMapping<T> costs;
	private AttributeMapping<T> attributes;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;
	
	public MotifsFilterExecutor(CostMapping<T> costs, AttributeMapping<T> attributes) {
		this.strings = new HashSet<Pair<String, Sequence<K>>>();
		this.motifs = new HashMap<String, List<HashMap<Character, Object>>>();
		//this.motifs = new HashSet<Pair<String, Sequence<K>>>();
		this.charsToValues = new HashMap<Character, T>();
		this.valuesToChars = new HashMap<T, Character>();
		this.costs = costs;
		this.attributes = attributes;
	}
	
	public boolean addString(Sequence<T> string,  Sequence<K> attributes) {
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
		return strings.add(Pair.of(s, attributes));
	}
	
	public void addMotif(Sequence<T> motif,  List<HashMap<String, Object>> motifAttributes) {
		String s = "";
		for (int i = 0; i < motif.size(); i++) {
			T t = motif.get(i);
			if (!valuesToChars.containsKey(t)) {
				char v = (char) ('@' + valuesToChars.size() + 1);
				valuesToChars.put(t, v);
				charsToValues.put(v, t);
			}
			s += valuesToChars.get(t);
		} 
		
			List<HashMap<Character, Object>> convertedMotifAttributes = new ArrayList();
			
			for(HashMap<String, Object> m : motifAttributes) {
				HashMap<Character, Object> temp = new HashMap();
				for(Entry<String, Object> entry : m.entrySet()) {
					// if the converting set doesn't contain the attrib names, add them
					if (!valuesToChars.containsKey(entry.getKey())) {
						char v = (char) ('@' + valuesToChars.size() + 1);
						valuesToChars.put((T) entry.getKey(), v);
						charsToValues.put(v, (T) entry.getKey());
					}
					// and then add the attributes converted to the new hashmap
					temp.put(valuesToChars.get(entry.getKey()), entry.getValue());
				}
				System.err.println(motifs.toString());
				convertedMotifAttributes.add(temp);
			}
			
			motifs.put(s, convertedMotifAttributes);
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
		HammingDistance<T> hamming = new HammingDistance<T>(costs, attributes, charsToValues);
		Set<Pair<String, Sequence<K>>> toReturn = new HashSet<Pair<String, Sequence<K>>>();
		int motifLength = motifs.entrySet().iterator().next().getKey().length();
		for (Pair<String, Sequence<K>> pair : strings) {
			// s is the trace
			String s = pair.getLeft();
			List<Integer> indexesWithMotifs = new ArrayList<Integer>();
			for(Entry<String, List<HashMap<Character, Object>>> motifPair : motifs.entrySet()) {
				// m is the motif
				String m = motifPair.getKey();
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

	public String getMotifs() {
		return motifs.toString();
	}
	
	public String getStrings() {
		return strings.toString();
	}
}
