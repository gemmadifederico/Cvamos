package dtu.qpms.model;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
import java.util.concurrent.ConcurrentHashMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import dtu.qpms.model.AttributeMapping.AttribOperation;
import dtu.qpms.utils.Operations;

public class MotifsVerifierExecutor<T> extends Thread{

	//private Set<String> potentialMotifs;
	private List<Map<String, HashMap<Character, String>>> potentialMotifs;
	private Map<String, HashMap<Character, String>> verifiedMotifs;
	private Map<String, List<HashMap<Character, Object>>> tempVerifiedMotifs;
	private Collection<String> strings;
	private double motifMaxDistance;
	private double quorum;
	private HammingDistance<T> hamming;
	private BitapDistance<T> bitap;
	private AttributeMapping<T> cvalues;
	private Collection<XTrace> traces;
	private Map<T, Character> valuesToChar;
	
	public MotifsVerifierExecutor(
			List<Map<String, HashMap<Character, String>>> newPotentialMotifs,
			//Collection<String> strings,
			Collection<XTrace> traces,
			double motifMaxDistance,
			double quorum,
			CostMapping<T> costs,
			Map<Character, T> map,
			Map<T, Character> valuesToChar,
			AttributeMapping<T> cvalues) {
		this.potentialMotifs = newPotentialMotifs;
		this.verifiedMotifs = new HashMap<>();
		this.tempVerifiedMotifs = new HashMap();
		//this.strings = strings;
		this.traces = traces;
		this.motifMaxDistance = motifMaxDistance;
		this.quorum = quorum;
		this.cvalues = cvalues;
		this.hamming = new HammingDistance<T>(costs, cvalues, map);
		this.bitap = new BitapDistance<T>(costs,cvalues, map);
		this.valuesToChar = valuesToChar;
	}
	
	public Map<String, HashMap<Character, String>> getVerifiedMotifs() {
		return verifiedMotifs;
	}

	@Override
	public void run() {
		verifyMotifs();
	}
	
	public void verifyMotifs() {
		this.verifiedMotifs = new HashMap<>();
//			int motifsFound = 0;
		for (Map<String, HashMap<Character, String>> l : potentialMotifs) {
			for(Entry<String, HashMap<Character, String>> m : l.entrySet()) {
				double stringsWithMotif = 0;
				for(XTrace s: traces) {
				//for (String s : strings) {
					if (verifyMotifInStringOld(s, m, motifMaxDistance)) {
						stringsWithMotif++;
					} else {
						if (quorum == 1d) {
							break;
						}
					}
				}
				if (stringsWithMotif / traces.size() >= quorum) {
					verifiedMotifs.put(m.getKey(), m.getValue());
	//				motifsFound++;
				}
			}
		}
//			if (motifsFound == 0) {
//				break;
//			}
	}
	
	private boolean verifyMotifInStringOld(XTrace string, Entry<String,HashMap<Character, String>> m, double maxDistance) {
		int motifLength = m.getKey().length();
		int stringLength = string.size();
		//System.err.println(m);
		// generate all the substrings of the strings
		String traceToString = "";
		if (stringLength >= motifLength) {
			for (int i = 0; i < stringLength; i++) {
				HashMap<String, List<String>> attrib = new HashMap<>();
				String str = "";
				int counter = 0;
				// loop through the string until I reach the desired motif length
				// So I have to go through all the elements from i on, until to reach the motif length
				// Hence, I use the loop to go through the trace, starting from position i, i.e. position i+0, i+1, ..., i+n
				for (int j=0; j<=stringLength; j++) {
					if(i+j>=stringLength) {
						break;
					}
					if(counter>motifLength) {
						break;
					}
					XEvent e = string.get(j+i);
					String ename = XConceptExtension.instance().extractName(e);
					char echar = valuesToChar.get(ename);
					if (cvalues.contains(ename)) {
						// this is an env attribute
						//attrib.put(echar, e.getAttributes().get("value"));
						if(attrib.get(ename) != null) {
							attrib.get(ename).add(e.getAttributes().get("value").toString());
						} else {
							//attrib.put(echar, Arrays.asList(e.getAttributes().get("value")));
							attrib.put(ename, new ArrayList<>(Arrays.asList(e.getAttributes().get("value").toString())));
						}
					} else {
						str += echar;
						counter += 1;
					}
					if(counter == motifLength) {
						// the motif is ready 
						// now we have to compare the motif first, and the attributes after
						// let's start with the motif
						if (hamming.distance(str, m.getKey()) <= maxDistance) {
// uncomment to switch to bitap	if(bitap.distance(str, m.getKey(), maxDistance) <= maxDistance) {
							// there is a motif match, but we have to check the attributes
							// the distance is lower than the maxDistance, hence I have to check the attributes
							// now the list of attributes is complex, indeed attrib is a list of elements that needs to be aggregated before to be compared to the motif attribs
							HashMap<Character, String> aggAttrib = aggregateAttributes(attrib);
							// If the set of attributes of the compared string is empty, there is a match anyway
							//System.out.println(aggAttrib + " ---- " + m.getValue().toString());
							if (aggAttrib.isEmpty() | hamming.distanceAttributes(aggAttrib, m.getValue()) <= maxDistance) {
								return true;
							} else {
								//System.err.println("there is a match but not in attributes");
								break;
							}
						} else {
							break;
						}
					}
				}
			}
		}
		return false;
	}
	
	private HashMap<Character, String> aggregateAttributes(HashMap<String, List<String>> attrib) {
		HashMap aggAttribs = new HashMap<>();
		for(Entry<String, List<String>> attr : attrib.entrySet()) {
			Character attrChar = valuesToChar.get(attr.getKey());
			AttribOperation operation = cvalues.getOperation(attr.getKey().toString());
			if(operation.equals(AttribOperation.MEAN)) {
				DoubleStream intStream = Operations.convertListToStream(attr.getValue());
				aggAttribs.put(attrChar, (String.valueOf(Operations.calculateMedianString(attr.getValue()))));
			}
			if(operation.equals(AttribOperation.MAX)) {
				//IntStream intStream = convertListToStream(attr.getValue());
				//aggAttribs.put(attrChar, Arrays.asList(intStream.max()));
				aggAttribs.put(attrChar,(String.valueOf(Operations.calculateMaxString(attr.getValue()))));
			}
			if(operation.equals(AttribOperation.MIN)) {
				// IntStream intStream = convertListToStream(attr.getValue());
				// aggAttribs.put(attrChar, Arrays.asList(intStream.min()));
				aggAttribs.put(attrChar, (String.valueOf(Operations.calculateMinString(attr.getValue()))));
			}
			if(operation.equals(AttribOperation.EQUALS)) {
				aggAttribs.put(attrChar, attr.getValue());
				// in this case I return a list of possible values, i.e. I do nothing
			}
		}
		return aggAttribs;
	}

	private boolean verifyMotifInString(String string, String motif, double maxDistance) {
		int motifLength = motif.length();
		int stringLength = string.length();
		int rtol = (int)maxDistance;
//		System.out.println(string + "---"+motif);
		boolean found = false;
		
		if (stringLength >= motifLength) {
//			First of all we need to know the size of the substrings, that is always the distance +1
			for(int j=motifLength; j>=0; j-=(maxDistance+1)) {
				String motifSub;
//				Check if the remaining part of the motif is of the expected size
				int motifRemainingPos = motif.length()-(motif.length()-j);
				int stringSize = (int) (maxDistance+1);
				if(motifRemainingPos == rtol && found==true) {
//					System.out.println("	And the remaining part is shorter then the distance");
					return true;
				}
				if(motifRemainingPos >=((int)maxDistance +1)){
					motifSub = motif.substring(j - ((int)maxDistance +1), j);
				} else {
					motifSub = motif.substring(j - motifRemainingPos, j);
					stringSize = motifRemainingPos;
				}
				 
//				System.out.println("The submotif is: "+motifSub);
				for(int i=stringLength; i>=stringSize; i--) {
					String stringSub = string.substring(i - stringSize, i);
//					System.out.println("	The substring is: " + stringSub);
					int dist = hamming.distance(motifSub, stringSub);
//					System.out.println("	The distance between them is: " + dist);
					if(dist <= maxDistance) {
//						in this case there is a match, so we need to go the other part of the motif
						found = true;
						rtol -= dist;
//						System.out.println("	The distance is lower then maxDist");
						break;
					} else {
//						System.out.println("	The distance is greather than maxDistance, so go next");
						found = false;
						continue;
					}
				}	
				if(found == true) {
					continue;
				}

			}		
		}
		return false;
	}
	

	private boolean verifyMotifInString2(String string, String motif, double maxDistance) {
		int motifLength = motif.length();
		int stringLength = string.length();
		System.out.println("The string is: " + string);
		System.out.println("The motif is: " + motif);
		
		if (stringLength >= motifLength) {
//			Consider the motif from the end, and divide it in substrings of length maxDistance +1
			String msub = motif.substring(motif.length() - ((int)maxDistance +1));
			System.out.println("The first submotif is: "+msub);
		
			for(int i=string.length(); i>=((int)maxDistance +1)*2; i--) {
				int rtol = (int)maxDistance;
				String ssub = string.substring(i - ((int)maxDistance +1), i);
				System.out.println("The first substring is: " + ssub);
				int dist = hamming.distance(msub, ssub);
				System.out.println("The distance between them is: " + dist);
				if(dist <= maxDistance) {
					System.out.println("Since the distance is lower or equal than maxdistance, I check the rest of the motif and the string.");
					rtol -= dist;
					String msubsub;
					String ssubsub;
					if((motif.length() - msub.length())< ((int)maxDistance +1)) {
						System.out.println("this is the case");
						 msubsub = motif.substring(0, (motif.length() - ((int)maxDistance +1)));
						 ssubsub = string.substring((i - ((int)maxDistance +1) - msub.length()), i - ((int)maxDistance +1));
					}else {
						 msubsub = motif.substring( (motif.length() - ((int)maxDistance +1) -  ((int)maxDistance +1)), motif.length() - ((int)maxDistance +1) );
						 ssubsub = string.substring((i - ((int)maxDistance +1) -  ((int)maxDistance +1)), i - ((int)maxDistance +1));
					}
					
					System.out.println("The leftsubstring is: " + ssubsub);
					System.out.println("The left motif is: " + msubsub);
					if(ssubsub.length() <= rtol) {
						System.out.println("Optimal case, we don't have to check");
						return true;
					} else {
						int dist2 = hamming.distance(msubsub, ssubsub);
						if(dist2<=rtol) {
							return true;
						} else {
							System.out.println("No the left parts are not matching");
							return false;
						}
						
					}
				} else {
					System.out.println("The distance is greather than maxDistance, so go next");
					continue;
				}
			}
			
//			Consider the string from the end, and get sliding substrings of length maxDistance+1
//			Set a counter of the remaining tolerance, equals to maxDistance
//			Check the hamming dist between submotif and substring
//			If > maxDistance, move to the next sliding substring
//			If <= maxDistance, tolerance counter -= dist, the substring is a candidate now, so let's move into verifying the left part of the motif
//			if length of left part of the motif <= tolerance counter, the motif is already verified
//			otherwise verify again as before
//			if dist > remaining tolerance, then discard it and go back to the right part of the motif.

		}
		return false;
	}
	
}
