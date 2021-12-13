package dtu.qpms.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MotifsVerifierExecutor<T> extends Thread {

	private Collection<String> potentialMotifs;
	private Set<String> verifiedMotifs;
	private Collection<String> strings;
	private double motifMaxDistance;
	private double quorum;
	
	public MotifsVerifierExecutor(Collection<String> potentialMotifs, Collection<String> strings, double motifMaxDistance, double quorum) {
		this.potentialMotifs = potentialMotifs;
		this.verifiedMotifs = new HashSet<String>();
		this.strings = strings;
		this.motifMaxDistance = motifMaxDistance;
		this.quorum = quorum;
	}
	
	public Set<String> getVerifiedMotifs() {
		return verifiedMotifs;
	}

	
	@Override
	public void run() {
		verifyMotifs();
	}
	
	public void verifyMotifs() {
		this.verifiedMotifs = new HashSet<String>();
		
		for (String m : potentialMotifs) {
			double stringsWithMotif = 0;
			for (String s : strings) {
				if (verifyMotifInString(s, m, motifMaxDistance)) {
					stringsWithMotif++;
				} else {
					if (quorum == 1d) {
						break;
					}
				}
			}
			if (stringsWithMotif / strings.size() >= quorum) {
				verifiedMotifs.add(m);
			}
		}
	}
	
	private static <T> boolean verifyMotifInString(String string, String motif, double maxDistance) {
		int motifLength = motif.length();
		int stringLength = string.length();
		
		if (stringLength >= motifLength) {
			for (int i = 0; i <= stringLength - motifLength; i++) {
				if (hammingDistance(string.substring(i, i + motifLength), motif) <= maxDistance) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static <T> int hammingDistance(String str1, String str2) {
		int count = 0;
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) != str2.charAt(i)) {
				count++;
			}
		}
		return count;
	}

}
