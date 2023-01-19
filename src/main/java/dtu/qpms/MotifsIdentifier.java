package dtu.qpms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;

import dtu.qpms.model.AttributeMapping;
import dtu.qpms.model.CostMapping;
import dtu.qpms.model.Sequence;
import dtu.qpms.model.qPMSPM;
import dtu.qpms.utils.XLogHelper;

public class MotifsIdentifier {

	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
//		CostMapping<String> c1 = new CostMapping<String>();
////		c1.read(args[2]);
//		qPMSPM<String> p1 = new qPMSPM<String>(
//				2, // motif length
//				1, // distance
//				1, // quorum
//				1, // threads
//				c1 // costs
//				);
////		
//		p1.addString(Sequence.str("AABBCC"));
//		p1.addString(Sequence.str("BBCCDD"));
//		
//		time = System.currentTimeMillis();
//		System.out.print("2. Generating candidate motifs... ");
//		p1.generateCandidateMotifs();		
//		System.out.println("Done! - " + p1.getCandidateMotifs().size() + " motifs identified in " + (System.currentTimeMillis() - time) + " ms");
////		System.out.println("Candidate motifs:" +  "\n" + p1.getCandidateMotifs() + "\n");
//		
//		time = System.currentTimeMillis();
//		System.out.print("3. Verifying motifs... ");
//		p1.verifyMotifs();
//		System.out.println("Done! - " + +p1.getMotifs().size()  + " motif verified in " + (System.currentTimeMillis() - time) + " ms");
////		System.out.println("Verified motifs: \n"+ p1.getMotifs());
//		System.exit(0);
		
		if (args.length != 8) {
			System.err.println("Use: java -jar FILE.jar input.xes output.xes costs-map.json attrib-map.json motifLength distance quorum");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String outputFile = args[1];
		String mapFile = args[2];
		String attrFile = args[3];
		
		int motifsLength = Integer.parseInt(args[4]);
		int maxDistance = Integer.parseInt(args[5]);
		double quorum = Double.parseDouble(args[6]);
		int threads = Integer.parseInt(args[7]);
		
		AttributeMapping<String> a = new AttributeMapping<String>();
		a.read(attrFile);
		CostMapping<String> c = new CostMapping<String>();
		c.read(mapFile);

		
		qPMSPM<String> p = new qPMSPM<String>(motifsLength, maxDistance, quorum, threads, c, a);
		
		System.out.println("qPMS-PM");
		System.out.println("-------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        outmput file: " + outputFile);
		System.out.println("     costs map file: " + mapFile);
		System.out.println("     attrib map file: " + attrFile);
		System.out.println("  	  motifs length: " + motifsLength);
		System.out.println("motifs max distance: " + maxDistance);
		System.out.println("             quorum: " + quorum);
		System.out.println("            threads: " + threads);
		System.out.println("");
		
		long initialTime = System.currentTimeMillis();
		System.out.print("1. Parsing log and generating candidate motifs... ");
		XParser parser = new XesXmlParser();
		XLog log = parser.parse(new File(inputFile)).get(0);
		
		for (XTrace t : log) {
			/*List<Map.Entry<XAttribute, XAttribute>> newstrings = new ArrayList<>();
			Sequence<String> s = new Sequence<String>();
			for (XEvent e : t) {
				newstrings.add(new AbstractMap.SimpleEntry<>(e.getAttributes().get("concept:name"), 
						e.getAttributes().get("time:timestamp")));
				Timestamp timest = Timestamp.from(Instant.parse(e.getAttributes().get("time:timestamp").toString()));
				SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
				s.add(XConceptExtension.instance().extractName(e));
			}*/
			p.addString(t);
			//p.addTrace(t);
		}
		p.aggregateAttributes();
		/*System.out.println("Candidate motifs: ");
		for (Sequence<String> b: p.getCandidateMotifs()) {
			System.out.println(b);
		}*/
		System.out.println("Done! - " + p.getCandidateMotifs().size() + " motifs identified in " + (System.currentTimeMillis() - time) + "ms");

		/*time = System.currentTimeMillis();
		System.out.print("2. Generating candidate motifs... ");
		p.generateCandidateMotifs();
		System.out.println("Done! - " + p.getCandidateMotifs().size() + " motifs identified in " + (System.currentTimeMillis() - time) + "ms");
		*/
		time = System.currentTimeMillis();
		System.out.print("3. Verifying motifs... ");
		p.verifyMotifs();
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("4. Saving motifs... ");
		int motifCounter = 1;
		XLog logMotifs = XLogHelper.generateNewXLog("motifs");
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		for(Entry<Sequence<String>, Map<String, String>> seq : p.getMotifs().entrySet()) {
			XTrace t = XLogHelper.createTrace("case_" + motifCounter);
			// inserting the events
			for (String s : seq.getKey()) {
				XEvent ev = XLogHelper.insertEvent(t, s);
				XLogHelper.decorateElement(ev, "value", 0.0);
				XLogHelper.setTimestamp(ev, new Date());
			}
			// inserting the attribs as events, and decorating with the value
			for (Entry<String, String> el : seq.getValue().entrySet()) {		
				XEvent ev = XLogHelper.insertEvent(t, el.getKey());
				XLogHelper.decorateElement(ev, "value", el.getValue());
				XLogHelper.setTimestamp(ev, new Date());
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
            writer.writeNext(new String[]{inputFile, ""+motifsLength, ""+maxDistance, ""+quorum, ""+p.getMotifs().size(), ""+(System.currentTimeMillis() - initialTime)});
        }
	}
}
