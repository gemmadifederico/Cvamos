package dtu.qpms.model;

import java.util.ArrayList;

public class Sequence<T> extends ArrayList<T> {

	private static final long serialVersionUID = -2649942823028260419L;

	public Sequence() {	}
	
	public Sequence(Sequence<T> copy) {
		for(T i : copy) {
			add(i);
		}
	}
	
	public Sequence(T ...items) {
		for (T i : items) {
			add(i);
		}
	}
	
	public Sequence<T> substring(int beginIndex, int endIndex) {
		if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = size() - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
		Sequence<T> toRet = new Sequence<T>();
		for (int i = beginIndex; i < endIndex; i++) {
			toRet.add(get(i));
		}
		return toRet;
	}
	
	public static <T> Sequence<T> of(T ...items) {
		return new Sequence<T>(items);
	}
	
	public static Sequence<String> str(String s) {
		return new Sequence<String>(s.split("(?!^)"));
	}
	
	public static <T> Sequence<T> concat(Sequence<T> s1, Sequence<T> s2) {
		Sequence<T> s = new Sequence<T>(s1);
		s.addAll(s2);
		return s;
	}
	
	@Override
	public String toString() {
		String s = "[";
		for (T i : this) {
			s += i + ",";
		}
		s = s.substring(0, s.length() - 1) + "]";
		return s;
	}
	
}
