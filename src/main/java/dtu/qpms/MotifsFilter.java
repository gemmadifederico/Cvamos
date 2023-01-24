package dtu.qpms;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import dtu.qpms.model.AttributeMapping;
import dtu.qpms.model.CostMapping;
import dtu.qpms.model.MotifsFilterExecutor;
import dtu.qpms.model.Sequence;
import dtu.qpms.utils.XLogHelper;

public class MotifsFilter {

	public static void main(String[] args) throws Exception {
		if (args.length != 7) {
			System.err.println("Use: java -jar FILE.jar input.xes motifs.xes output.xes costs-map.json attrib-map.json distance label");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String motifsFile = args[1];
		String outputFile = args[2];
		String mapFile = args[3];
		String attrFile = args[4];
		int maxDistance = Integer.parseInt(args[5]);
		
		String ACTIVITY_NAME_ABSTRACTED_ACTIVITY = args[6];
		
		CostMapping<String> c = new CostMapping<String>();
		c.read(mapFile);
		
		AttributeMapping<String> a = new AttributeMapping<String>();
		a.read(attrFile);
		
		System.out.println("qPMS-PM - Motifs Filter");
		System.out.println("-----------------------");
		System.out.println("         input file: " + inputFile);
		System.out.println("        motifs file: " + motifsFile);
		System.out.println("        output file: " + outputFile);
		System.out.println("     costs map file: " + mapFile);
		System.out.println("     attrib map file: " + attrFile);
		System.out.println("motifs max distance: " + maxDistance);
		System.out.println("");
		
		//Set<Sequence<String>> motifs = new HashSet<Sequence<String>>();
		//Map<Sequence<String>, HashMap<Character, List<String>>> motifs = new HashMap<>();
		
		//Map<Sequence<String>, List<HashMap<String, Object>>> motifs = new HashMap<>();
		List<Map<Sequence<String>, HashMap<String, Object>>> motifs = new ArrayList();
		MotifsFilterExecutor<String, XAttributeMap> mf = new MotifsFilterExecutor<String, XAttributeMap>(c, a);
		
		XParser parser = new XesXmlParser();
		
		long time = System.currentTimeMillis();
		System.out.print("1. Parsing log... ");
		XLog logStrings = parser.parse(new File(inputFile)).get(0);
		
		// I have to parse the log in strings completely, since I'm not dividing it in motifs now
		for (XTrace t : logStrings) {
			Sequence<String> s = new Sequence<String>();
			Sequence<XAttributeMap> attributes = new Sequence<XAttributeMap>();
			for (XEvent e : t) {
				s.add(XConceptExtension.instance().extractName(e));
				attributes.add(e.getAttributes());
			}
			mf.addString(s, attributes);
			mf.addTrace(t);
		}
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
				
		time = System.currentTimeMillis();
		System.out.print("2. Parsing motifs... ");
		XLog logMotifs = parser.parse(new File(motifsFile)).get(0);
		for (XTrace t : logMotifs) {
			Sequence<String> s = new Sequence<String>();
			HashMap<String, Object> attr = new HashMap();
			for (XEvent e : t) {  
				String envname = XConceptExtension.instance().extractName(e);
				if(a.contains(envname)) {
					// this is an env variable
					attr.put(envname, e.getAttributes().get("value"));
				} else {
					s.add(envname);
				}
			}
			// at the end of the trace I have to add the motif and the attributes
			HashMap newmotif = new HashMap();
			newmotif.put(s, attr);
			//motifs.add(newmotif);
			mf.addMotif(s, attr);
		}
		
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("3. Filtering motifs... ");
		Set<Triple<XAttribute, Sequence<String>, Sequence<XAttributeMap>>> replaced = mf.filter(maxDistance, ACTIVITY_NAME_ABSTRACTED_ACTIVITY);
		System.out.println("Done! - " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		System.out.print("4. Saving motifs... ");
		int motifCounter = 1;
		XLog logFiltered = XLogHelper.generateNewXLog("filtered");
		for(Triple<XAttribute, Sequence<String>, Sequence<XAttributeMap>> seq : replaced) {
			XTrace t = XLogHelper.createTrace(seq.getLeft().toString());
			for (int i = 0; i < seq.getMiddle().size(); i++) {
				XEvent e = XLogHelper.xesFactory.createEvent();
				if(seq.getRight().get(i)!= null) {
					e.setAttributes(seq.getRight().get(i));
				}
				XConceptExtension.instance().assignName(e, seq.getMiddle().get(i));
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
