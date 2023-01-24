package dtu.qpms.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import dtu.qpms.model.AttributeMapping.AttribOperation;
import dtu.qpms.utils.Operations;

public class MotifsFilterExecutor <T, K> {

	private static final char TO_REPLACE = '!';
	private Set<Pair<String, Sequence<K>>> strings;
	//private Set<Pair<String, Sequence<K>>> motifs;
	//private Set<Pair< Pair<String, Sequence<K>>, List<HashMap<Character, Object>>>> motifs
	//private Map<String, List<HashMap<Character, Object>>> motifs;
	private List<Map<String, HashMap<Character, String>>> motifs;
	private CostMapping<T> costs;
	private AttributeMapping<T> attributes;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;
	private Collection<XTrace> traces;
	
	public MotifsFilterExecutor(CostMapping<T> costs, AttributeMapping<T> attributes) {
		this.strings = new HashSet<Pair<String, Sequence<K>>>();	
		this.motifs = new ArrayList<Map<String, HashMap<Character, String>>>();
		//this.motifs = new HashSet<Pair<String, Sequence<K>>>();
		this.charsToValues = new HashMap<Character, T>();
		this.valuesToChars = new HashMap<T, Character>();
		this.costs = costs;
		this.attributes = attributes;
		this.traces = new ArrayList<>(); 
	}
	
	public void addTrace(XTrace t) {
		this.traces.add(t);
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
	
	public void addMotif(Sequence<T> motif,  HashMap<String, Object> attr) {
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
		
		HashMap<Character, Object> convertedMotifAttributes = new HashMap();
		
			for(Entry<String, Object> entry : attr.entrySet()) {
				// if the converting set doesn't contain the attrib names, add them
				if (!valuesToChars.containsKey(entry.getKey())) {
					char v = (char) ('@' + valuesToChars.size() + 1);
					valuesToChars.put((T) entry.getKey(), v);
					charsToValues.put(v, (T) entry.getKey());
				}
				// and then add the attributes converted to the new hashmap
				convertedMotifAttributes.put(valuesToChars.get(entry.getKey()), entry.getValue().toString());
			}
		HashMap temp = new HashMap();
		temp.put(s, convertedMotifAttributes);
		motifs.add(temp);
	}
	
	public Set<Triple<XAttribute, Sequence<T>, Sequence<K>>> filter(double maxDistance, T replace) {
		Set<Pair<Sequence<T>, Sequence<K>>> toReturn = new HashSet<Pair<Sequence<T>, Sequence<K>>>();

		return filterStrings(maxDistance, replace);
	}
	
	private Set<Triple<XAttribute, Sequence<T>, Sequence<K>>> filterStrings(double maxDistance, T replace) {
		/*if (motifs.isEmpty()) {
			return strings;
		}*/
		HammingDistance<T> hamming = new HammingDistance<T>(costs, attributes, charsToValues);
		Set<Triple<XAttribute, Sequence<T>, Sequence<K>>> toReturn = new HashSet<Triple<XAttribute, Sequence<T>, Sequence<K>>>();
		int motifLength = motifs.get(0).entrySet().iterator().next().getKey().length();
		//int motifLength = motifs.entrySet().iterator().next().getKey().length();
		for(XTrace trace : traces) {
			System.out.println("\n trace: " + trace.getAttributes().get("concept:name"));
			List<Pair<Integer, Integer>> indexesWithMotifs = new ArrayList<>();
			int sIndex = 0;
			int eIndex = sIndex;
			for(Map<String, HashMap<Character, String>> el : motifs) {
			for(Entry<String, HashMap<Character, String>> motifPair : el.entrySet()) {
				// m is the motif
				String m = motifPair.getKey();
				int counter = 0;
				HashMap<Character, List<String>> attrib = new HashMap<>();
				String str = "";
				for(int j=sIndex; j<trace.size();) {
					String ename = XConceptExtension.instance().extractName(trace.get(j));
					char echar = valuesToChars.get(ename);
					eIndex = trace.indexOf(trace.get(j));
					if(attributes.contains(ename)){
						if(attrib.get(echar) != null) {
							attrib.get(echar).add(trace.get(j).getAttributes().get("value").toString());
						} else {
							attrib.put(echar, new ArrayList<>(Arrays.asList(trace.get(j).getAttributes().get("value").toString())));
						}
					} else {
						str += echar;
						counter += 1;
					}
					
					if(counter == motifLength) {
						if (hamming.distance(str, m) <= maxDistance) {
							HashMap<Character,  String> aggAttrib = aggregateAttributes(attrib);
							if (aggAttrib.isEmpty() | hamming.distanceAttributes(aggAttrib, motifPair.getValue()) <= maxDistance) {
								//System.out.println("motif found " + sIndex + " " + eIndex);
								indexesWithMotifs.add(Pair.of(sIndex, eIndex));
								sIndex = eIndex;
								j = eIndex+1;
							} else {
								//System.err.println("there is a match but not in attributes");
								sIndex += 1;
								j = sIndex;
								
							}
						} else {
							//System.err.println("hamming not satisfied");
							sIndex += 1;
							j = sIndex;
							eIndex = sIndex;
						}
						counter = 0;
						str = "";
						attrib.clear();
					} else {
						//System.out.println("sindex " + sIndex+ " eindex" + eIndex);
						j++;
					}
				}
				
				Sequence<T> recomposedTrace = new Sequence<T>();
				Sequence<K> attributes = new Sequence<K>();
				int end = 0;
				if(indexesWithMotifs.isEmpty()) {
					for(int p = 0; p<trace.size(); p++) {
						recomposedTrace.add((T) XConceptExtension.instance().extractName(trace.get(p)));
						attributes.add((K) trace.get(p).getAttributes());
					}
				}
				for(Pair<Integer, Integer> pair : indexesWithMotifs) {
					//System.out.println("The pair is" + pair);
					int start = pair.getLeft();
										
					for(int i=end; i<=start; i++) {
						// so until I reach the start I add them
						if(i<start) {
							recomposedTrace.add((T) XConceptExtension.instance().extractName(trace.get(i)));
							attributes.add((K) trace.get(i).getAttributes());
						}
						// when I reach the start, I put the motif instead
						if(i == start) {
							recomposedTrace.add(replace);
							attributes.add(null);
							end = pair.getRight()+1;
							i = pair.getRight()+1;
						}
					}
					// if the pair is the last index, then add all the remaining of the trace
					if(indexesWithMotifs.indexOf(pair) == (indexesWithMotifs.size()-1)) {
						for (int k =end; k<trace.size(); k++) {
							recomposedTrace.add((T) XConceptExtension.instance().extractName(trace.get(k)));
							attributes.add((K) trace.get(k).getAttributes());
						}
					}
					
					
					//System.out.println("start " + start + "end" + end + trace.size());
					
					//now I create a new string, adding a piece at at time, based on the original trace indexes
					// e.g. in the indexesWithMotifs I have the pairs (1,3) and (5,7)
					// starting from the first pair I take the elements from the beginning of the trace until the first start
					// and then I add the label
					// After that I set the starting point of the next pair as the end of this pair 
					// X A B C Y A B C X X X 
					// so I take the events until I reach the start value of the first pair, and put them in the new trace
					// then I put the motif
					// then I start again looping through the trace but starting from the endvalue + 1
				}
				//System.err.println("The recomposed trace is the following" + trace.getAttributes().get("concept:name")+ recomposedTrace);
				Triple<XAttribute, Sequence<T>, Sequence<K>> triplet = Triple.of(trace.getAttributes().get("concept:name"), recomposedTrace, attributes);
				toReturn.add(triplet);
				System.out.println("indexes with motifs" + indexesWithMotifs);
			}}
		}
		
		/*for (Pair<String, Sequence<K>> pair : strings) {
			// this is the new trace that I'm going to populate
			String newtrace = "";
			// s is the trace
			String s = pair.getLeft();
			List<Integer> indexesWithMotifs = new ArrayList<Integer>();
			for(Entry<String, List<HashMap<Character, Object>>> motifPair : motifs.entrySet()) {
				// m is the motif
				String m = motifPair.getKey();
				int stringLength = s.length();
				if (stringLength >= motifLength) {
					int counter = 0;
					for(int i = 0; i < stringLength; i++) {
						if(attributes.contains(charsToValues.get(s.charAt(i)))){
							System.err.println("this is an attribute value");
						} else {
							newtrace += charsToValues.get(s.charAt(i));
							counter ++;
						}
						if(counter == motifLength) {
							System.out.println(newtrace);
						}
					}
					for (int i = 0; i <= stringLength - motifLength; i++) {
						System.out.println(charsToValues.get(s.charAt(i)));
						if(attributes.contains(charsToValues.get(s.charAt(i)))){
							System.err.println("this is an attribute value");
						}
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
		}*/
		return toReturn;
	}

	private HashMap<Character, String> aggregateAttributes(HashMap<Character, List<String>> attrib) {
		HashMap aggAttribs = new HashMap<>();
		for(Entry<Character, List<String>> attr : attrib.entrySet()) {
			Character attrChar = attr.getKey();
			AttribOperation operation = attributes.getOperation(attr.getKey().toString());
			if(operation.equals(AttribOperation.MEAN)) {
				DoubleStream intStream = Operations.convertListToStream(attr.getValue());
				aggAttribs.put(attrChar, String.valueOf(Operations.calculateMedianString(attr.getValue())));
			}
			if(operation.equals(AttribOperation.MAX)) {
				//IntStream intStream = convertListToStream(attr.getValue());
				//aggAttribs.put(attrChar, Arrays.asList(intStream.max()));
				aggAttribs.put(attrChar, String.valueOf(Operations.calculateMaxString(attr.getValue())));
			}
			if(operation.equals(AttribOperation.MIN)) {
				// IntStream intStream = convertListToStream(attr.getValue());
				// aggAttribs.put(attrChar, Arrays.asList(intStream.min()));
				aggAttribs.put(attrChar,String.valueOf(Operations.calculateMinString(attr.getValue())));
			}
			if(operation.equals(AttribOperation.EQUALS)) {
				aggAttribs.put(attrChar, attr.getValue());
				// in this case I return a list of possible values, i.e. I do nothing
			}
		}
		return aggAttribs;
	}
	
	public String getMotifs() {
		return motifs.toString();
	}
	
	public String getStrings() {
		return strings.toString();
	}
}
