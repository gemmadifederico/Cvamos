package dtu.qpms.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MotifsVerifierExecutor<T> extends Thread {

	private Map<Integer, List<String>> potentialMotifs;
	private Set<String> verifiedMotifs;
	private Collection<String> strings;
	private double motifMaxDistance;
	private double quorum;
	private HammingDistance<T> hamming;
	
	public MotifsVerifierExecutor(
			Map<Integer, List<String>> potentialMotifs,
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
		this.hamming = new HammingDistance<T>(costs, map);
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
		int i = 0;
		List<Integer> lengths = new ArrayList<Integer>(potentialMotifs.keySet());
		Collections.sort(lengths);
		for (Integer len : lengths) {
//			int motifsFound = 0;
			for (String m : potentialMotifs.get(len)) {
				double stringsWithMotif = 0;
				i++;
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
//					motifsFound++;
				}
			}
//			if (motifsFound == 0) {
//				break;
//			}
		}
		System.out.println(i + " motifs verified");
	}
	
	private boolean verifyMotifInString(String string, String motif, double maxDistance) {
		int motifLength = motif.length();
		int stringLength = string.length();
		
		if (stringLength >= motifLength) {
			for (int i = 0; i <= stringLength - motifLength; i++) {
				if (hamming.distance(string.substring(i, i + motifLength), motif) <= maxDistance) {
					return true;
				}
			}
		}
		return false;
	}
}
