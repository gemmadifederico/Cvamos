package dtu.qpms;

import java.io.File;
import java.io.FileOutputStream;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import dtu.qpms.model.CostMapping;
import dtu.qpms.model.Sequence;
import dtu.qpms.model.qPMSPM;
import dtu.qpms.utils.XLogHelper;

public class MotifsIdentifier {

	public static void main(String[] args) throws Exception {
		
		
//		int l = 4;
//		int d = 0;
//		int n = l;
//		double q = 0.8;
//		CostMapping<String> c = new CostMapping<String>();
//		c.read(args[2]);
//		qPMSPM<String> p = new qPMSPM<String>(l, d, n, q, 1, c);
//		
//		p.addString(Sequence.str("ABBBACCCA"));
//		p.addString(Sequence.str("DBBBDCCCD"));
////		p.addString(Sequence.str("AAAAA"));
//		
//		p.generateCandidateMotifs();
//		System.out.println(p.getCandidateMotifs());
//		p.verifyMotifs();
//		System.out.println(p.getMotifs());
		
		
		if (args.length != 3) {
			System.err.println("Use: java -jar FILE.jar input.xes output.xes costs-map.json");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String outputFile = args[1];
		String mapFile = args[2];
		
		int motifsLength = 3;
		int maxDistance = 0;
		int ngramSize = 1;
		double quorum = 1;
		int threads = 1;
		
		CostMapping<String> c = new CostMapping<String>();
		c.read(mapFile);
		qPMSPM<String> p = new qPMSPM<String>(motifsLength, maxDistance, ngramSize, quorum, threads, c);
		
		System.out.println("qPMS-PM");
		System.out.println("-------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        output file: " + outputFile);
		System.out.println("     costs map file: " + mapFile);
		System.out.println("      motifs length: " + motifsLength);
		System.out.println("motifs max distance: " + maxDistance);
		System.out.println("      ngrams length: " + ngramSize);
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
		System.out.println("3. Verifying motifs... ");
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
		System.out.println(p.getMotifs().size() + " motifs identified. Motifs saved as XES log at " + outputFile + ".");
	}
}
