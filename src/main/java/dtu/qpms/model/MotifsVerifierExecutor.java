package dtu.qpms.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MotifsVerifierExecutor<T> extends Thread {

	private Set<Sequence<T>> potentialMotifs;
	private Set<Sequence<T>> verifiedMotifs;
	private Collection<Sequence<T>> strings;
	private double motifMaxDistance;
	private double quorum;
	
	public MotifsVerifierExecutor(Set<Sequence<T>> potentialMotifs, Collection<Sequence<T>> strings, double motifMaxDistance, double quorum) {
		this.potentialMotifs = potentialMotifs;
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		this.strings = strings;
		this.motifMaxDistance = motifMaxDistance;
		this.quorum = quorum;
	}
	
	public Set<Sequence<T>> getVerifiedMotifs() {
		return verifiedMotifs;
	}

	
	@Override
	public void run() {
		verifyMotifs();
	}
	
	public void verifyMotifs() {
		this.verifiedMotifs = new HashSet<Sequence<T>>();
		
		for (Sequence<T> m : potentialMotifs) {
			double stringsWithMotif = 0;
			for (Sequence<T> s : strings) {
				if (verifyMotifInString(s, m, motifMaxDistance)) {
					stringsWithMotif++;
				} else {
					if (quorum == 1) {
						break;
					}
				}
			}
			if (stringsWithMotif / strings.size() >= quorum) {
				verifiedMotifs.add(m);
			}
		}
	}
	
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
