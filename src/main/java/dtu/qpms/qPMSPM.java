package dtu.qpms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;

import dtu.qpms.model.MotifsVerifierExecutor;
import dtu.qpms.model.Sequence;
import dtu.qpms.model.SequenceItemPrinter;

public class qPMSPM<T> {
	
	private int motifLength;
	private double motifMaxDistance;
	private int ngramLength;
	private double quorum;
	private Set<Sequence<T>> potentialMotifs;
	private Set<Sequence<T>> verifiedMotifs;
	private Set<Sequence<T>> strings;
	private SequenceItemPrinter<T> printer = null;

	public qPMSPM(int l, double d, int n, double q) {
		this.motifLength = l;
		this.motifMaxDistance = d;
		this.ngramLength = n;
		this.quorum = q;
		this.potentialMotifs = new HashSet<Sequence<T>>();
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		this.strings = new HashSet<Sequence<T>>();
	}
	
	public Set<Sequence<T>> getCandidateMotifs() {
		return potentialMotifs;
	}
	
	public Set<Sequence<T>> getMotifs() {
		return verifiedMotifs;
	}

	public boolean addString(Sequence<T> string) {
		if (printer == null) {
			printer = string.getPrinter();
		}
		return strings.add(string);
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
		
		for(List<Sequence<T>> s : Iterables.partition(potentialMotifs, potentialMotifs.size() / 3)) {
			MotifsVerifierExecutor<T> e = new MotifsVerifierExecutor<T>(potentialMotifs, s, motifMaxDistance, quorum);
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
		
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		for (MotifsVerifierExecutor<T> t : threads) {
			verifiedMotifs.addAll(t.getVerifiedMotifs());
		}
		
//		this.verifiedMotifs = new HashSet<Sequence<T>>();
//		
//		
//		
//		for (Sequence<T> m : potentialMotifs) {
//			double stringsWithMotif = 0;
//			for (Sequence<T> s : strings) {
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
		this.potentialMotifs = new HashSet<Sequence<T>>();
		// generate all n-grams
		Set<Sequence<T>> ngrams = new HashSet<Sequence<T>>();
		for (Sequence<T> s : strings) {
			int stringLength = s.size();
			if (stringLength >= ngramLength) {
				for (int i = 0; i <= stringLength - ngramLength; i++) {
					ngrams.add(s.substring(i, i + ngramLength));
				}
			}
		}
		// generate motifs
		recursiveGenerateMotif(ngrams, new Sequence<T>(printer), motifLength/ngramLength);
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
}
