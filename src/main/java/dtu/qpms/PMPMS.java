package dtu.qpms;

import java.util.HashSet;
import java.util.Set;

import dtu.qpms.model.Sequence;

public class PMPMS<T> {
	
	private int l;
	private double d;
	private int n;
	private double q;
	private Set<Sequence<T>> potentialMotifs;
	private Set<Sequence<T>> verifiedMotifs;
	private Set<Sequence<T>> strings;

	public PMPMS(int l, double d, int n, double q) {
		this.l = l;
		this.d = d;
		this.n = n;
		this.q = q;
		this.potentialMotifs = new HashSet<Sequence<T>>();
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		this.strings = new HashSet<Sequence<T>>();
	}
	
	public Set<Sequence<T>> getPotentialMotifs() {
		return potentialMotifs;
	}
	
	public Set<Sequence<T>> getMotifs() {
		return verifiedMotifs;
	}

	public Set<Sequence<T>> getStrings() {
		return strings;
	}
	
	public int getMotifLength() {
		return l;
	}
	
	public double getMaxDistance() {
		return d;
	}
	
	public int getNgramLength() {
		return n;
	}
	
	public double getQuorum() {
		return q;
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
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		for (Sequence<T> m : potentialMotifs) {
			double stringsWithMotif = 0;
			for (Sequence<T> s : strings) {
				if (verifyMotifInString(s, m, d)) {
					stringsWithMotif++;
					
				} else {
					if (q == 1) {
						break;
					}
				}
			}
			if (stringsWithMotif / strings.size() >= q) {
				verifiedMotifs.add(m);
			}
		}
	}
	
	/**
	 * Generate only motifs from the given string
	 */
	public void generateCandidateMotifs() {
		this.potentialMotifs = new HashSet<Sequence<T>>();
		// generate all n-grams
		Set<Sequence<T>> ngrams = new HashSet<Sequence<T>>();
		for (Sequence<T> s : strings) {
			int stringLength = s.size();
			if (stringLength >= n) {
				for (int i = 0; i <= stringLength - n; i++) {
					ngrams.add(s.substring(i, i + n));
				}
			}
		}
		// generate motifs
		recursiveGenerateMotif(ngrams, new Sequence<T>(), l/n);
	}
	
	private void recursiveGenerateMotif(Set<Sequence<T>> ngrams, Sequence<T> output, int l) {
		if (l <= 0) {
			this.potentialMotifs.add(output);
			return;
		}
		for (Sequence<T> ngram : ngrams) {
			recursiveGenerateMotif(ngrams, Sequence.concat(output, ngram), l - 1);
		}
	}
	
//	public static Set<String> filterOutMotifs(Set<Sequence<T>> motifs, Set<Sequence<T>> strings, double maxDistance, Sequence<T> replace) {
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
	
	private static <T> boolean verifyMotifInString(Sequence<T> string, Sequence<T> motif, double maxDistance) {
		int motifLength = motif.size();
		int stringLength = string.size();
		
		if (stringLength >= motifLength) {
			for (int i = 0; i <= stringLength - motifLength; i++) {
				if (hammingDistance(string.substring(i, i + motifLength), motif) <= maxDistance) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static <T> int hammingDistance(Sequence<T> str1, Sequence<T>str2) {
		int i = 0, count = 0;
		while (i < str1.size()) {
			if (!str1.get(i).equals(str2.get(i))) {
				count++;
			}
			i++;
		}
		return count;
	}
}
