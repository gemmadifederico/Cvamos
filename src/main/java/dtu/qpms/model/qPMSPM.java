package dtu.qpms.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

public class qPMSPM<T> {
	
	private int motifLength;
	private double motifMaxDistance;
	private int ngramLength;
	private double quorum;
	private int threads;
	private Set<String> potentialMotifs;
	private Set<String> verifiedMotifs;
	private Set<String> strings;
	private CostMapping<T> costs;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;

	public qPMSPM(int l, double d, int n, double q, int threads, CostMapping<T> costs) {
		this.motifLength = l;
		this.motifMaxDistance = d;
		this.ngramLength = n;
		this.quorum = q;
		this.threads = threads;
		this.potentialMotifs = new HashSet<String>();
		this.verifiedMotifs = new HashSet<String>();
		this.strings = new HashSet<String>();
		this.costs = costs;
		charsToValues = new HashMap<Character, T>();
		valuesToChars = new HashMap<T, Character>();
	}
	
	public Set<String> getCandidateMotifs() {
		return potentialMotifs;
	}
	
	public Set<String> getStringMotifs() {
		return verifiedMotifs;
	}
	
	public Set<Sequence<T>> getMotifs() {
		Set<Sequence<T>> motifs = new HashSet<Sequence<T>>();
		for (String s : verifiedMotifs) {
			Sequence<T> seq = new Sequence<T>();
			for (int i = 0; i < s.length(); i++) {
				seq.add(charsToValues.get(s.charAt(i)));
			}
			motifs.add(seq);
		}
		return motifs;
	}

	public boolean addString(Sequence<T> string) {
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
		return strings.add(s);
	}
	
	public int getMotifLength() {
		return motifLength;
	}
	
	public double getMaxDistance() {
		return motifMaxDistance;
	}
	
	public int getNgramLength() {
		return ngramLength;
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
		
		for(List<String> s : Iterables.partition(potentialMotifs, potentialMotifs.size() / this.threads)) {
			MotifsVerifierExecutor<T> e = new MotifsVerifierExecutor<T>(s, strings, motifMaxDistance, quorum, costs, charsToValues);
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
		
		this.verifiedMotifs = new HashSet<String>();
		for (MotifsVerifierExecutor<T> t : threads) {
			verifiedMotifs.addAll(t.getVerifiedMotifs());
		}
		
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
	public void generateCandidateMotifs() {
		this.potentialMotifs = new HashSet<String>();
		// generate all n-grams
		Set<String> ngrams = new HashSet<String>();
		for (String s : strings) {
			int stringLength = s.length();
			if (stringLength >= ngramLength) {
				for (int i = 0; i <= stringLength - ngramLength; i++) {
					ngrams.add(s.substring(i, i + ngramLength));
				}
			}
		}
		// generate motifs
		recursiveGenerateMotif(ngrams, "", motifLength/ngramLength);
	}
	
	private void recursiveGenerateMotif(Set<String> ngrams, String output, int l) {
		if (l <= 0) {
			this.potentialMotifs.add(output);
			return;
		}
		for (String ngram : ngrams) {
			recursiveGenerateMotif(ngrams, output.concat(ngram), l - 1);
		}
	}
	
//	public static <T> Set<Sequence<T>> filterOutMotifs(Set<Sequence<T>> motifs, Set<Sequence<T>> strings, double maxDistance, Sequence<T> replace) {
//		if (motifs.isEmpty()) {
//			return strings;
//		}
//		Set<String> toReturn = new HashSet<String>();
//		int motifLength = motifs.iterator().next().length();
//		for (String s : strings) {
//			List<Integer> indexesWithMotifs = new ArrayList<Integer>();
//			for (Sequence<T> m : motifs) {
//				int stringLength = s.length();
//				if (stringLength >= motifLength) {
//					for (int i = 0; i <= stringLength - motifLength; i++) {
//						if (hammingDistance(s.substring(i, i + motifLength), m) <= maxDistance) {
//							indexesWithMotifs.add(i);
//							i += motifLength - 1;
//						}
//					}
//				}
//			}
//			Collections.sort(indexesWithMotifs);
//			
//			List<Integer> indexesWithMotifsToReplace = new ArrayList<Integer>();
//			for (int i = 0; i < indexesWithMotifs.size(); i++) {
//				if (!(i > 0 && indexesWithMotifs.get(i) < indexesWithMotifs.get(i - 1) + motifLength)) {
//					indexesWithMotifsToReplace.add(indexesWithMotifs.get(i));
//				}
//			}
//			
//			String replacement = "";
//			for (int i = 0; i < indexesWithMotifsToReplace.size(); i++) {
//				if (i==0) {
//					replacement += s.substring(0, indexesWithMotifsToReplace.get(i));
//				} else {
//					replacement += s.substring(indexesWithMotifsToReplace.get(i - 1) + motifLength, indexesWithMotifsToReplace.get(i));
//				}
//				replacement += replace;
//				if (i + 1 == indexesWithMotifsToReplace.size()) {
//					replacement += s.substring(indexesWithMotifsToReplace.get(i) + motifLength);
//				}
//			}
//			toReturn.add(replacement);
//		}
//		return toReturn;
//	}
}
