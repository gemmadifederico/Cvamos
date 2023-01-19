package dtu.qpms.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Operations {
	public static double calculateAverage(List <String> marks) {
	    return marks.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .average()
	                .orElse(0.0);
	}
	
	public static double calculateMedian(List<Object> arr) {
		List<String> strings = arr.stream()
				   .map(object -> Objects.toString(object, null))
				   .collect(Collectors.toList());
		List<Double> a = strings.stream()
				.map(object -> Double.parseDouble(object))
				.collect(Collectors.toList());
		
		int n = strings.size();
		Collections.sort(a);
		// check for even case
		if (n % 2 != 0) return (double)a.get(n/2);
		return (double)(a.get((n - 1) / 2) + a.get(n / 2)) / 2.0;
	}
	
	public static double calculateMedianString(List<String> strings) {
		List<Double> a = strings.stream()
				.map(object -> Double.parseDouble(object))
				.collect(Collectors.toList());
		
		int n = strings.size();
		Collections.sort(a);
		// check for even case
		if (n % 2 != 0) return (double)a.get(n/2);
		return (double)(a.get((n - 1) / 2) + a.get(n / 2)) / 2.0;
	}
	
	public static double calculateMin(List <Object> arr) {
		List<String> strings = arr.stream()
				   .map(object -> Objects.toString(object, null))
				   .collect(Collectors.toList());
	    return strings.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .min()
	                .orElse(0.0);
	}
	
	public static double calculateMinString(List <String> strings) {
	    return strings.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .min()
	                .orElse(0.0);
	}
	
	public static double calculateMax(List <Object> arr) {
		List<String> strings = arr.stream()
				   .map(object -> Objects.toString(object, null))
				   .collect(Collectors.toList());
	    return strings.stream()
	                .mapToDouble(d -> Double.parseDouble(d))
	                .max()
	                .orElse(0.0);
	}
	
	public static double calculateMaxString(List <String> strings) {
	    return strings.stream()
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
