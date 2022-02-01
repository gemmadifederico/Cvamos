package dtu.qpms.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

public class qPMSPM<T> {
	
	private int motifLengthMin;
	private int motifLengthMax;
	private double motifMaxDistance;
	private Set<Integer> ngramLengths;
	private double quorum;
	private int threads;
	private Map<Integer, HashSet<String>> potentialMotifs;
	private Set<String> verifiedMotifs;
	private Set<String> strings;
	private CostMapping<T> costs;
	private Map<Character, T> charsToValues;
	private Map<T, Character> valuesToChars;

	public qPMSPM(int lMin, int lMax, double d, Set<Integer> n, double q, int threads, CostMapping<T> costs) {
		this.motifLengthMin = lMin;
		this.motifLengthMax = lMax;
		this.motifMaxDistance = d;
		this.ngramLengths = n;
		this.quorum = q;
		this.threads = threads;
		this.potentialMotifs = new HashMap<Integer, HashSet<String>>();
		this.verifiedMotifs = new HashSet<String>();
		this.strings = new HashSet<String>();
		this.costs = costs;
		charsToValues = new HashMap<Character, T>();
		valuesToChars = new HashMap<T, Character>();
	}
	
	public Set<String> getCandidateMotifs() {
		Set<String> pm = new HashSet<String>();
		for (HashSet<String> m : potentialMotifs.values()) {
			pm.addAll(m);
		}
		return pm;
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
		@SuppressWarnings("unchecked")
		HashMap<Integer, List<String>>[] partitionedSets = new HashMap[this.threads+1];
		for (Integer motifsSize : potentialMotifs.keySet()) {
			int i = 0;
			System.out.println("Size: " + motifsSize + " - tot candidate " + potentialMotifs.get(motifsSize).size() + ", partition size: " + (potentialMotifs.get(motifsSize).size() / this.threads));
			for (List<String> strs : Iterables.partition(potentialMotifs.get(motifsSize), potentialMotifs.get(motifsSize).size() / this.threads)) {
				if (partitionedSets[i] == null) {
					partitionedSets[i] = new HashMap<Integer, List<String>>();
				}
				System.out.println("Adding " + strs.size() + " candidates with length " + motifsSize + " to element " + i);
				partitionedSets[i].put(motifsSize, strs);
				i++;
			}
		}
		
		for (int i = 0; i < partitionedSets.length; i++) {
			if (partitionedSets[i] != null) {
				System.out.println("Creating thread for " + partitionedSets[i].keySet().size() + " points");
				MotifsVerifierExecutor<T> e = new MotifsVerifierExecutor<T>(partitionedSets[i], strings, motifMaxDistance, quorum, costs, charsToValues);
				threads.add(e);
				e.start();
				System.out.println("Starting thread");
			}
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
		this.potentialMotifs = new HashMap<Integer, HashSet<String>>();
		
		for (int ngramLength : ngramLengths) {
			// generate all possible n-grams
			Set<String> ngrams = new HashSet<String>();
			for (String s : strings) {
				int stringLength = s.length();
				if (stringLength >= ngramLength) {
					for (int i = 0; i <= stringLength - ngramLength; i++) {
						ngrams.add(s.substring(i, i + ngramLength));
					}
				}
			}
			
			// generate all motifs
			int motifLength = getMinMotifLength();
			while(motifLength <= getMaxMotifLength()) {
				// generate motifs
				recursiveGenerateMotif(ngrams, "", motifLength/ngramLength);
				motifLength += ngramLength;
			}
		}
	}
	
	private void recursiveGenerateMotif(Set<String> ngrams, String output, int l) {
		if (l <= 0) {
			if (output.length() >= motifLengthMin) {
				if (!potentialMotifs.containsKey(output.length())) {
					potentialMotifs.put(output.length(), new HashSet<String>());
				}
				potentialMotifs.get(output.length()).add(output);
			}
			return;
		}
		for (String ngram : ngrams) {
			recursiveGenerateMotif(ngrams, output.concat(ngram), l - 1);
		}
	}
}
