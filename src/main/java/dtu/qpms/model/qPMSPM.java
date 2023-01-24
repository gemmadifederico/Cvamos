package dtu.qpms.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import dtu.qpms.model.AttributeMapping.AttribOperation;
import dtu.qpms.utils.Operations;

public class qPMSPM<T> {
	
	private int motifLengthMin;
	private int motifLengthMax;
	private double motifMaxDistance;
	private Set<Integer> ngramLengths;
	private double quorum;
	private int threads;
	private Map<String, List<HashMap<Character, List<Object>>>> potentialMotifs;
	private Map<String, List<HashMap<Character, String>>> newPotentialMotifs;
	private List<Map<String,HashMap<Character, String>>>verifiedMotifs;
	private List<Map.Entry<XAttribute, XAttribute>> newstrings;
	private Set<String> strings;
	private CostMapping<T> costs;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;
	private AttributeMapping<T> cvalues;
	private Set<Character> cvaluesasChars;
	private Collection<XTrace> traces;

	public qPMSPM(int mlen, double d, double q, int threads, CostMapping<T> costs, AttributeMapping<T> a) {
		this.motifLengthMax = mlen;
		this.motifMaxDistance = d;
		this.quorum = q;
		this.threads = threads;
		this.potentialMotifs = new HashMap<>();
		this.verifiedMotifs = new ArrayList<>();
		this.strings = new HashSet<String>();
		this.costs = costs;
		this.cvalues = a;
		newPotentialMotifs = new HashMap<>();
		charsToValues = new HashMap<Character, T>();
		valuesToChars = new HashMap<T, Character>();
		newstrings = new ArrayList<>();
		traces = new ArrayList<>();
		cvaluesasChars = new HashSet<>();
	}
	/*
	 * Return the list of UNIQUE motifs, not considering the attributes
	 */
	public Set<Sequence<T>> getCandidateMotifs() {
		Set<Sequence<T>> motifs = new HashSet<Sequence<T>>();
		for (Entry<String, List<HashMap<Character,String>>> s : newPotentialMotifs.entrySet()) {
			Sequence<T> seq = new Sequence<T>();
			for (int i = 0; i < s.getKey().length(); i++) {
				seq.add(charsToValues.get(s.getKey().charAt(i)));
			}
			motifs.add(seq);
		}
		return motifs;
	}
	
	
	public List<Map<Sequence<T>, Map<T, String>>> getMotifs() {
		List<Map<Sequence<T>, Map<T, String>>> motifs = new ArrayList();
		for (Map<String, HashMap<Character, String>> vm : verifiedMotifs) {
		for (Entry<String, HashMap<Character, String>> s : vm.entrySet()) {
			// for each motif --> attribs, I convert the motif name again to values
			//for(Entry<Character, List<String>> attribs : s.getValue().entrySet()) {
				Sequence<T> seq = new Sequence<T>();
				//seq.add(charsToValues.get(s.getKey()));
				for (int i = 0; i < s.getKey().length(); i++) {
					seq.add(charsToValues.get(s.getKey().charAt(i)));
				}
				Map<T, String> temp = new HashMap(); 
				
				HashMap<Character, String> k = s.getValue();
				for(Entry<Character, String> el : k.entrySet()) {
					T seq2 = charsToValues.get(el.getKey());
					temp.put(seq2, el.getValue());
				}
				Map<Sequence<T>, Map<T, String>> motiftemp = new HashMap();
				motiftemp.put(seq, temp);
				motifs.add(motiftemp);
			//}
		}
		}
		return motifs;
	}
	
	public Collection<XTrace> getTraces() {
		return traces;
	}
	public void addTrace(XTrace trace) {
		this.traces.add(trace);
	}

	public boolean addString(XTrace trace) {
		this.traces.add(trace);
		// generate all the substrings of the strings
		int stringLength = trace.size();
		String traceToString = "";
		if (stringLength >= motifLengthMax) {
			for (int i = 0; i < stringLength; i++) {
				HashMap<Character, List<Object>> attrib = new HashMap<>();
				String str = "";
				int counter = 0;
				// loop through the string until I reach the desired motif length
				// So I have to go through all the elements from i on, until to reach the motif length
				// Hence, I use the loop to go through the trace, starting from position i, i.e. position i+0, i+1, ..., i+n
				for (int j=0; j<=stringLength; j++) {
					if(i+j>=stringLength) {
						break;
					}
					if(counter>motifLengthMax) {
						break;
					}
					XEvent e = trace.get(j+i);
					String ename = XConceptExtension.instance().extractName(e);
					char v = 0;
					if (!valuesToChars.containsKey(ename)) {
						v = (char) ('@' + valuesToChars.size() + 1);
						valuesToChars.put((T) ename, v);
						charsToValues.put(v, (T) ename);
					} else {
						v= valuesToChars.get(ename);
					}
					if(cvalues.contains(ename)) {
						cvaluesasChars.add(v);
						if(attrib.containsKey(v)) {
							attrib.get(v).add(e.getAttributes().get("value"));
						} else {
							attrib.put(v, new ArrayList<>(Arrays.asList(e.getAttributes().get("value"))));
						}
					} else {
						// to have strings without env, uncomment here 
						/*if(j==0) {
							traceToString += valuesToChars.get((T) ename);
						}*/
						counter += 1;
						str += valuesToChars.get((T) ename);
					}
					// to have strings with env, uncomment here
					if(j==0) {
						traceToString += valuesToChars.get((T) ename);
					}
					if(counter == motifLengthMax) {
						// if the motif already exists, check if the set of attributes already exists
						//System.out.println(potentialMotifs.toString());
						if(potentialMotifs.containsKey(str)) {
							List<HashMap<Character, List<Object>>> attributes = potentialMotifs.get(str);
							// if not, add them to the list of the attributes
							// if one of the hashmap inside attributes has the same set of keys attrib, then it's ok
							// otherwise I have to put attrib in attributes
							if(!hasSameKey(attributes, attrib)) {
								attributes.add(attrib);
								potentialMotifs.put(str, attributes);
							} 
						} else {
							// the element is completely new
							List<HashMap<Character, List<Object>>> attributes = new ArrayList<>();
							attributes.add(attrib);
							potentialMotifs.put(str, attributes);
						}
						break;
					}
				}
			}
		}
		//System.out.println(valuesToChars);
		return strings.add(traceToString);
	}
	
	public void aggregateAttributes() {
		for(Entry<String, List<HashMap<Character, List<Object>>>> pm : potentialMotifs.entrySet()) {
			ArrayList newlist = new ArrayList();
			for(HashMap<Character, List<Object>> ms : pm.getValue()) {
				HashMap values = new HashMap<>();
				for(Entry<Character, List<Object>> attr : ms.entrySet()) {
					AttribOperation operation = cvalues.getOperation(charsToValues.get(attr.getKey()).toString());
					if(operation.equals(AttribOperation.MEAN)) {
						//IntStream intStream = convertListToStream(attr.getValue());
						//values.put(attr.getKey(), Arrays.asList(intStream.average()));
						values.put(attr.getKey(), String.valueOf(Operations.calculateMedian(attr.getValue())));
					}
					if(operation.equals(AttribOperation.MAX)) {
						//IntStream intStream = convertListToStream(attr.getValue());
						//values.put(attr.getKey(), Arrays.asList(intStream.max()));
						values.put(attr.getKey(), Arrays.asList(String.valueOf(Operations.calculateMax(attr.getValue()))));
					}
					if(operation.equals(AttribOperation.MIN)) {
						//IntStream intStream = convertListToStream(attr.getValue());
						//values.put(attr.getKey(), Arrays.asList(intStream.min()));
						values.put(attr.getKey(), Arrays.asList(String.valueOf(Operations.calculateMin(attr.getValue()))));
					}
					if(operation.equals(AttribOperation.EQUALS)) {
						values.put(attr.getKey(), attr.getValue());
						// in this case I return a list of possible values, i.e. I do nothing
					}
				}
			newlist.add(values);
			}
			if(newPotentialMotifs.get(pm.getKey()) != null){
				newPotentialMotifs.get(pm.getKey()).addAll(newlist);
				// System.out.println("first");
			} else {
				newPotentialMotifs.put(pm.getKey(), newlist);
			}
			
		}
	}
	
	public boolean hasSameKey(List<HashMap<Character, List<Object>>> a, HashMap<Character, List<Object>> b) {
		for(HashMap<Character, List<Object>> map: a) {
			for (Character key: map.keySet()) {
		        if (map.keySet().equals(b.keySet())) {
		            return true;
		        } 
		    }
		}
		return false;
		
	}
	
	public String printString() {
		return "qPMSPM [strings=" + strings + "]";
	}

	public int getMinMotifLength() {
		return motifLengthMin;
	}
	
	public int getMaxMotifLength() {
		return motifLengthMax;
	}
	
	public double getMaxDistance() {
		return motifMaxDistance;
	}
	
	public Set<Integer> getNgramLengths() {
		return ngramLengths;
	}
	
	public double getQuorum() {
		return quorum;
	}

//	public void generateAlphabet() {
//		Set<Character> alpha = new HashSet<Character>();
//		for (String s : strings) {
//			for (Character c : s.toCharArray()) {
//				alpha.add(c);
//			}
//		}
//		this.alphabet = new Character[alpha.size()];
//		alpha.toArray(this.alphabet);
//	}
	
	public void verifyMotifs() {
		Set<MotifsVerifierExecutor<T>> threads = new HashSet<MotifsVerifierExecutor<T>>();
		
		List<Map<String, HashMap<Character, String>>> pm = new ArrayList();
		for(Entry<String, List<HashMap<Character, String>>> el : newPotentialMotifs.entrySet()) {
			for (HashMap<Character, String> j: el.getValue()) {
				HashMap t = new HashMap();
				t.put(el.getKey(), j);
				pm.add(t);
			}
		}
		System.out.println("- tot candidate " + pm.size() + ", partition size: " + (pm.size() / this.threads));
		
		List<List<Map<String, HashMap<Character, String>>>> smallerLists = Lists.partition(pm, pm.size() / this.threads);
		
		for (int i = 0; i < smallerLists.size(); i++) {
			System.out.println("Creating thread for " + smallerLists.get(i).size() + " points...");
			MotifsVerifierExecutor<T> e = new MotifsVerifierExecutor<T>(smallerLists.get(i), traces, motifMaxDistance, quorum, costs, charsToValues, valuesToChars, cvalues);
			threads.add(e);
			e.start();
			System.out.println("Starting thread");
			
		}
		 
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (MotifsVerifierExecutor<T> t : threads) {
			verifiedMotifs.addAll(t.getVerifiedMotifs());
		}
		
		
		// convert the cvalues table according to chars
		//MotifsVerifierExecutor<T> e = new MotifsVerifierExecutor<T>(newPotentialMotifs, traces, motifMaxDistance, quorum, costs, charsToValues, valuesToChars, cvalues);
		
		//e.run();
		//this.verifiedMotifs = new HashMap<>();
		//verifiedMotifs.putAll(e.getVerifiedMotifs());
		
		//-------------------------------------------
		
//		this.verifiedMotifs = new HashSet<String>();
//		for (String m : potentialMotifs) {
//			double stringsWithMotif = 0;
//			for (String s : strings) {
//				if (verifyMotifInString(s, m, motifMaxDistance)) {
//					stringsWithMotif++;
//				} else {
//					if (quorum == 1) {
//						break;
//					}
//				}
//			}
//			if (stringsWithMotif / strings.size() >= quorum) {
//				verifiedMotifs.add(m);
//			}
//		}
	}
	
	/**
	 * Generate only motifs from the given string
	 */
	/*public void generateCandidateMotifs() {
		// generate all the substrings of the strings
		for (String s : strings) {
			int stringLength = s.length();
			if (stringLength >= motifLengthMax) {
				for (int i = 0; i <= stringLength - motifLengthMax; i++) {
					this.potentialMotifs.add(s.substring(i, i + motifLengthMax));
				}
			}
		}
	}*/
	
//	private void recursiveGenerateMotif(Set<String> ngrams, String output, int l) {
//		if (l <= 0) {
//			if (output.length() >= motifLengthMin) {
//				if (!potentialMotifs.containsKey(output.length())) {
//					potentialMotifs.put(output.length(), new HashSet<String>());
//				}
//				potentialMotifs.get(output.length()).add(output);
//			}
//			return;
//		}
//		for (String ngram : ngrams) {
//			recursiveGenerateMotif(ngrams, output.concat(ngram), l - 1);
//		}
//	}

	public static IntStream convertListToStream(List<String> list) {
		List<Integer> integerArray = new ArrayList<>();
		// copy elements from object array to integer array
		for (int i = 0; i < list.size(); i++) {
			integerArray.add(Integer.parseInt(list.get(i)));
		}
		
	    return integerArray.stream().flatMapToInt(IntStream::of);
	}
	
	public static Supplier<Stream<List<Integer>>> convertListToStream2(List<String> list) {
		List<Integer> integerArray = new ArrayList<>();
		// copy elements from object array to integer array
		for (int i = 0; i < list.size(); i++) {
			integerArray.add(Integer.parseInt(list.get(i)));
		}
		
		Supplier<Stream<List<Integer>>> streamSupplier 
		  = () -> Stream.of(integerArray);
		return streamSupplier;
		
	}
}

