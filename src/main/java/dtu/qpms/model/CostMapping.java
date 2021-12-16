package dtu.qpms.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class CostMapping<T> {

	private static final double DEFAULT_COST = 1;
	
	private Map<T, HashMap<T, Double>> costsMap;
	
	public CostMapping() {
		costsMap = new HashMap<T, HashMap<T, Double>>();
	}
	
	public double getCost(T act1, T act2) {
		if (costsMap.containsKey(act1)) {
			if (costsMap.get(act1).containsKey(act2)) {
				return costsMap.get(act1).get(act2);
			}
		}
		return DEFAULT_COST;
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
	private void generateMap(List<Map> costs) {
		for(Map c : costs) {
			T act1 = (T) c.get("act1");
			T act2 = (T) c.get("act2");
			double cost = (double) c.get("cost");
			if (!costsMap.containsKey(act1)) {
				costsMap.put(act1, new HashMap<T, Double>());
			}
			costsMap.get(act1).put(act2, cost);
		}
	}
}
