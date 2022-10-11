package dtu.qpms;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import dtu.qpms.model.CostMapping;
import dtu.qpms.model.MotifsFilterExecutor;
import dtu.qpms.model.Sequence;
import dtu.qpms.utils.XLogHelper;

public class MotifsFilter {

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.err.println("Use: java -jar FILE.jar input.xes motifs.xes output.xes costs-map.json distance label");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String motifsFile = args[1];
		String outputFile = args[2];
		String mapFile = args[3];
		int maxDistance = Integer.parseInt(args[4]);
		
		String ACTIVITY_NAME_ABSTRACTED_ACTIVITY = args[5];
		
		CostMapping<String> c = new CostMapping<String>();
		c.read(mapFile);
		
		System.out.println("qPMS-PM - Motifs Filter");
		System.out.println("-----------------------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        motifs file: " + motifsFile);
		System.out.println("        output file: " + outputFile);
		System.out.println("     costs map file: " + mapFile);
		System.out.println("motifs max distance: " + maxDistance);
		System.out.println("");
		
		Set<Sequence<String>> motifs = new HashSet<Sequence<String>>();
		MotifsFilterExecutor<String, XAttributeMap> mf = new MotifsFilterExecutor<String, XAttributeMap>(c);
		
		XParser parser = new XesXmlParser();
		
		long time = System.currentTimeMillis();
		System.out.print("1. Parsing log... ");
		XLog logStrings = parser.parse(new File(inputFile)).get(0);
		
		for (XTrace t : logStrings) {
			Sequence<String> s = new Sequence<String>();
			Sequence<XAttributeMap> attributes = new Sequence<XAttributeMap>();
			for (XEvent e : t) {
				s.add(XConceptExtension.instance().extractName(e));
				attributes.add(e.getAttributes());
			}
			mf.addString(s, attributes);
		}
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		
		time = System.currentTimeMillis();
		System.out.print("2. Parsing motifs... ");
		XLog logMotifs = parser.parse(new File(motifsFile)).get(0);
		
		for (XTrace t : logMotifs) {
			Sequence<String> s = new Sequence<String>();
			for (XEvent e : t) {
				s.add(XConceptExtension.instance().extractName(e));
			}
			motifs.add(s);
			mf.addMotif(s);
		}
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("3. Filtering motifs... ");
		Set<Pair<Sequence<String>, Sequence<XAttributeMap>>> replaced = mf.filter(maxDistance, ACTIVITY_NAME_ABSTRACTED_ACTIVITY);
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("4. Saving motifs... ");
		int motifCounter = 1;
		XLog logFiltered = XLogHelper.generateNewXLog("filtered");
		for(Pair<Sequence<String>, Sequence<XAttributeMap>> seq : replaced) {
			XTrace t = XLogHelper.createTrace("case_" + motifCounter);
			for (int i = 0; i < seq.getLeft().size(); i++) {
				XEvent e = XLogHelper.xesFactory.createEvent();
				e.setAttributes(seq.getRight().get(i));
				XConceptExtension.instance().assignName(e, seq.getLeft().get(i));
				t.add(e);
			}
			logFiltered.add(t);
			motifCounter++;
		}
		XSerializer serializer = new XesXmlSerializer();
		serializer.serialize(logFiltered, new FileOutputStream(outputFile));
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
	}
}
