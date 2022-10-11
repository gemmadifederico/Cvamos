package dtu.qpms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import com.opencsv.CSVWriter;

import dtu.qpms.model.CostMapping;
import dtu.qpms.model.Sequence;
import dtu.qpms.model.qPMSPM;
import dtu.qpms.utils.XLogHelper;

public class MotifsIdentifier {

	public static void main(String[] args) throws Exception {
		
//		CostMapping<String> c1 = new CostMapping<String>();
////		c1.read(args[2]);s
//		qPMSPM<String> p1 = new qPMSPM<String>(
//				4, // min length
//				4, // max length
//				0, // distance
//				1, // ngram size
//				1, // quorum
//				9, // threads
//				c1);
//		
//		p1.addString(Sequence.str("ABBBACCCA"));
//		p1.addString(Sequence.str("ABBBDCCCD"));
////		p.addString(Sequence.str("AAAAA"));
//		
//		p1.generateCandidateMotifs();
//		System.out.println(p1.getCandidateMotifs());
//		System.out.println(p1.getCandidateMotifs().size() + " candidate motifs");
//		p1.verifyMotifs();
//		System.out.println(p1.getMotifs());
//		System.exit(0);
		
		if (args.length != 8) {
			System.err.println("Use: java -jar FILE.jar input.xes output.xes costs-map.json minLength maxLength distance ngram quorum");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String outputFile = args[1];
		String mapFile = args[2];
		
		int motifsMinLength = Integer.parseInt(args[3]);
		int motifsMaxLength = Integer.parseInt(args[4]);
		int maxDistance = Integer.parseInt(args[5]);
		Set<Integer> ngramSizes = new HashSet<Integer>(Arrays.asList(Integer.parseInt(args[6])));
		double quorum = Double.parseDouble(args[7]);
		int threads = 5;
		
		CostMapping<String> c = new CostMapping<String>();
		c.read(mapFile);
		qPMSPM<String> p = new qPMSPM<String>(motifsMinLength, motifsMaxLength, maxDistance, ngramSizes, quorum, threads, c);
		
		System.out.println("qPMS-PM");
		System.out.println("-------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        output file: " + outputFile);
		System.out.println("     costs map file: " + mapFile);
		System.out.println("  motifs min length: " + motifsMinLength);
		System.out.println("  motifs max length: " + motifsMaxLength);
		System.out.println("motifs max distance: " + maxDistance);
		System.out.println("     ngrams lengths: " + ngramSizes);
		System.out.println("             quorum: " + quorum);
		System.out.println("            threads: " + threads);
		System.out.println("");
		
		long time = System.currentTimeMillis();
		System.out.print("1. Parsing log... ");
		XParser parser = new XesXmlParser();
		XLog log = parser.parse(new File(inputFile)).get(0);
		
		for (XTrace t : log) {
			Sequence<String> s = new Sequence<String>();
			for (XEvent e : t) {
				s.add(XConceptExtension.instance().extractName(e));
			}
			p.addString(s);
		}
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("2. Generating candidate motifs... ");
		p.generateCandidateMotifs();
		System.out.println("Done! - " + p.getCandidateMotifs().size() + " motifs identified in " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("3. Verifying motifs... ");
		p.verifyMotifs();
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("4. Saving motifs... ");
		int motifCounter = 1;
		XLog logMotifs = XLogHelper.generateNewXLog("motifs");
		for(Sequence<String> seq : p.getMotifs()) {
			XTrace t = XLogHelper.createTrace("case_" + motifCounter);
			for (String s : seq) {
				XLogHelper.insertEvent(t, s);
			}
			logMotifs.add(t);
			motifCounter++;
		}
		XSerializer serializer = new XesXmlSerializer();
		serializer.serialize(logMotifs, new FileOutputStream(outputFile));
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		System.out.println("");
		System.out.println(p.getMotifs().size() + " motifs identified.");
		System.out.println("Total time: " + (System.currentTimeMillis() - initialTime));
		
		try (CSVWriter writer = new CSVWriter(new FileWriter("output.csv", true))) {
            writer.writeNext(new String[]{inputFile, ""+ngramSizes.toString(), ""+motifsMinLength, ""+motifsMaxLength, ""+maxDistance, ""+quorum, ""+p.getMotifs().size(), ""+(System.currentTimeMillis() - initialTime)});
        }
	}
}
