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

import dtu.qpms.model.Sequence;
import dtu.qpms.utils.XLogHelper;

public class Comparator {

	public static void main(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.err.println("Use: java -jar FILE.jar input.xes output.xes");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String outputfile = args[1];
		
		int l = 10;
		int d = 2;
		int n = 10;
		double q = 0.8;
		
		qPMSPM<String> p = new qPMSPM<String>(l, d, n, q);
		
		System.out.println("qPMS-PM");
		System.out.println("-------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        output file: " + outputfile);
		System.out.println("      motifs length: " + l);
		System.out.println("motifs max distance: " + d);
		System.out.println("      ngrams length: " + n);
		System.out.println("             quorum: " + q);
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
		serializer.serialize(logMotifs, new FileOutputStream(outputfile));
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		System.out.println("");
		System.out.println(p.getMotifs().size() + " motifs identified. Motifs saved as XES log at " + outputfile + ".");
		
//		p.addString(Sequence.str("ABBBACCCA"));
//		p.addString(Sequence.str("DBBBDCCCD"));
//		p.addString(Sequence.str("AAAAA"));
//		p.generateCandidateMotifs();
//		System.out.println(p.getCandidateMotifs());
//		p.verifyMotifs();
//		System.out.println(p.getMotifs());
	}
}
