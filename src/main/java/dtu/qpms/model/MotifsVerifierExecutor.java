package dtu.qpms.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MotifsVerifierExecutor<T> extends Thread {

	private Collection<String> potentialMotifs;
	private Set<String> verifiedMotifs;
	private Collection<String> strings;
	private double motifMaxDistance;
	private double quorum;
	private CostMapping<T> costs;
	private Map<Character, T> map;
	
	public MotifsVerifierExecutor(
			Collection<String> potentialMotifs,
			Collection<String> strings,
			double motifMaxDistance,
			double quorum,
			CostMapping<T> costs,
			Map<Character, T> map) {
		this.potentialMotifs = potentialMotifs;
		this.verifiedMotifs = new HashSet<String>();
		this.strings = strings;
		this.motifMaxDistance = motifMaxDistance;
		this.quorum = quorum;
		this.costs = costs;
		this.map = map;
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
	
	private boolean verifyMotifInString(String string, String motif, double maxDistance) {
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
	
	private int hammingDistance(String str1, String str2) {
		int count = 0;
		for (int i = 0; i < str1.length(); i++) {
			char c1 = str1.charAt(i);
			char c2 = str2.charAt(i);
			if (c1 != c2) {
				count += costs.getCost(map.get(c1), map.get(c2));
			}
		}
		return count;
	}

}
