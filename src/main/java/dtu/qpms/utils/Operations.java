package dtu.qpms.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Operations {
	public static double calculateAverage(List <String> marks) {
	    return marks.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .average()
	                .orElse(0.0);
	}
	
	public static double calculateMedian(List<String> arr) {
		int n = arr.size();
		List<Double> a = new ArrayList<>();
		for (int i = 0; i < arr.size(); i++) {
			a.add(Double.parseDouble(arr.get(i)));
		}
		Collections.sort(a);
		// check for even case
		if (n % 2 != 0) return (double)a.get(n/2);
		return (double)(a.get((n - 1) / 2) + a.get(n / 2)) / 2.0;
	}
	
	public static double calculateMin(List <String> marks) {
	    return marks.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .min()
	                .orElse(0.0);
	}
	
	public static double calculateMax(List <String> marks) {
	    return marks.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .max()
	                .orElse(0.0);
	}
	
	public static DoubleStream convertListToStream(List<String> list) {
		List<Double> doubleArray = new ArrayList<>();
		// copy elements from object array to integer array
		for (int i = 0; i < list.size(); i++) {
			doubleArray.add(Double.parseDouble(list.get(i)));
		}
		
	    return doubleArray.stream().flatMapToDouble(DoubleStream::of);
	}
}
