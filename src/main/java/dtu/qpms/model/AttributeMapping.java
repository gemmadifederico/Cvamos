package dtu.qpms.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class AttributeMapping<T> {
private static final double DEFAULT_COST = 1;

public enum AttribOperation {
    MIN, MAX, MEAN, EQUALS 
}
	
	private Map<T, HashMap<T, T>> attribMap;
	
	public AttributeMapping() {
		attribMap = new HashMap<T, HashMap<T, T>>();
	}
	
	public AttribOperation getOperation(String t) {
		if (attribMap.containsKey(t)) {
			//System.out.println("the attribmap of attr " +t+ "is:"+ attribMap.get(t));
			return (AttribOperation) attribMap.get(t).get("operation");
		}
		return AttribOperation.MEAN;
	}
	
	public double getTolerance(T t) {
		if (attribMap.containsKey(t)) {
			return ((double) attribMap.get(t).get("tolerance"));
		}
		return 0.0;
	}
	
	public boolean contains(Object sensor) {
		if(attribMap.containsKey(sensor)) {
			return true;
		}
		return false;
	}
	
	public void read(String file) throws FileNotFoundException {
		@SuppressWarnings("rawtypes")
		ArrayList<Map> costs = load(file);
		generateMap(costs);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<Map> load(String file) throws FileNotFoundException {
		ArrayList<Map> costs = new ArrayList<Map>();
		Reader reader = new FileReader(file);
		Gson gson = new Gson();
		costs = gson.fromJson(reader, ArrayList.class);
		return costs;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateMap(List<Map> attribs) {
		for(Map a : attribs) {
			T name = (T) a.get("name");
			T tolerance = (T) a.get("tolerance");
			
			HashMap temp = new HashMap<>();
			temp.put("tolerance", tolerance);
			
			if(a.get("operation") != null) {
				 String op = a.get("operation").toString();
				 AttribOperation operation = AttribOperation.valueOf(op);
				 temp.put("operation", operation);
			}
			attribMap.put(name, temp);
		}
	}
}
