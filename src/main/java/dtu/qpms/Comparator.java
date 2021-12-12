package dtu.qpms;

import dtu.qpms.model.Sequence;

public class Comparator {

	public static void main(String[] args) {
		int l = 3;
		int d = 0;
		int n = 3;
		double q = 0.5;

		
		PMPMS p = new PMPMS(l, d, n, q);
		p.getStrings().add(Sequence.str("ABBBACCCA"));
		p.getStrings().add(Sequence.str("DBBBDCCCD"));
		p.getStrings().add(Sequence.str("AAAAA"));
		p.generateCandidateMotifs();
		System.out.println(p.getPotentialMotifs());
		p.verifyMotifs();
		System.out.println(p.getMotifs());
		
		
		
		
//		PMPMS p = new PMPMS(3, 0, 4, 1);
//		p.getStrings().add(Sequence.str("ABBBACCCA"));
//		p.getStrings().add(Sequence.str("DBBBDCCCD"));
////		p.generateCandidateMotifs();
////		p.verifyMotifs();
////		Set<String> motifs = new HashSet<String>();
//		motifs.add("CCC");
//		motifs.add("BBB");
//		System.out.println(p.getPotentialMotifs());
//		Set<String> filtered = p.filterOutMotifs(motifs, p.strings, p.d, "1");
//		System.out.println("NEW TRACES");
//		System.out.println(filtered);
		
//		p.getStrings().add("AHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNNMNLLJJLLMMLLMNNMNJPKKLLMNOOJKLLJKQEIHHIHHDDABBDDIH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNNLLJJLLMMLLJKLLMNOOJPKLMMKJKLLPKKQEHHCAHHCAADDH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNKKLLMMLLJKLLKPPMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNLLKJKKLLMOOMNMKJKKLLKPJKJEHHCAAIHHIABBDDIH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNNLLKJKLLMMLLMNNMNKKOOMNKKLLKJKKLLKPJKJEHHCAAIHHIABBDDACBDDIH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNLLJJLLMMLLJKKMNOOMNNMNKKLLMJKLLJKQEIHHIHHCBBDDCCADDIH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNLLJJLLKLLMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNLLKJKKLLMNNMNKKMNOOJKKQPJKLLPJKQEIHHCBBAIHHCCADDIH");
//		p.getStrings().add("BAHHIEJKLLMNOOMNLLMMLLPJKLLJPKLLMMLLJKLLMNOOJPKLMMKJKLLPKKQEHHCAHHCAADDH");
		
//		System.out.println(1);
//		p.generateCandidateMotifs();
//		System.out.println(2);
//		p.verifyMotifs();
//		System.out.println("MOTIFS");
//		System.out.println(p.getMotifs());
//		Set<String> filtered = p.filterOutMotifs(p.getMotifs(), p.getStrings(), p.getMaxDistance(), "1");
//		System.out.println("NEW TRACES");
//		System.out.println(filtered);
//		
//		System.out.println("--------------------");
//		
//		p.l = 18;
//		p.d = 3;
//		p.strings = filtered;
//		p.generateCandidateMotifs();
//		p.verifyMotifs();
//		System.out.println("MOTIFS");
//		System.out.println(p.verifiedMotifs);
//		filtered = p.filterOutMotifs(p.verifiedMotifs, p.strings, p.d, "2");
//		System.out.println("NEW TRACES");
//		System.out.println(filtered);
//		
//		System.out.println("--------------------");
//		
//		p.l = 12;
//		p.d = 5;
//		p.strings = filtered;
//		p.generateCandidateMotifs();
//		p.verifyMotifs();
//		System.out.println("MOTIFS");
//		System.out.println(p.verifiedMotifs);
//		filtered = p.filterOutMotifs(p.verifiedMotifs, p.strings, p.d, "3");
//		System.out.println("NEW TRACES");
//		System.out.println(filtered);
//		
//		System.out.println("--------------------");
//		
//		p.l = 11;
//		p.d = 5;
//		p.strings = filtered;
//		p.generateCandidateMotifs();
//		p.verifyMotifs();
//		System.out.println("MOTIFS");
//		System.out.println(p.verifiedMotifs);
//		filtered = p.filterOutMotifs(p.verifiedMotifs, p.strings, p.d, "4");
//		System.out.println("NEW TRACES");
//		System.out.println(filtered);
		
		
//		p.strings.add("BAHHIEJKLLMNOOMNKKMNNMNNMNNMNNMNLLJJLLKLLMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNLLKJKKLLMNNMNKKMNOOJKKQPJKLLPJKQEIHHCBBAIHHCCADDIH");
//		Set<String> m1 = new HashSet<String>(); m1.add("AHHIEJKLLMNOOMNKKMNN");
//		Set<String> filtered = p.filterOutMotifs(m1, p.strings, 4, "1");
//		System.out.println(filtered);
//		
//		p.strings = filtered;
//		p.strings.add("1NMNNMNNMNNMNLLJJLLKLLMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNNMNLLKJKKLLMNNMNKKMNOOJKKQPJKLLPJKQEIHHCBBAIHHCCADDIH");
//		Set<String> m2 = new HashSet<String>(); m2.add("LLKJKLLMML");
//		Set<String> filtered2 = p.filterOutMotifs(m2, p.strings, 3, "2");
//		System.out.println(filtered2);
	}
}
