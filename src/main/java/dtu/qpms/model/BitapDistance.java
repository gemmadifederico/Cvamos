package dtu.qpms.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BitapDistance<T> {
	
	private CostMapping<T> costs;
	private Map<Character, T> map;
	private AttributeMapping<T> attribs;
	
	public BitapDistance(CostMapping<T> costs, AttributeMapping<T> attribs, Map<Character, T> map) {
		this.costs = costs;
		this.attribs = attribs;
		this.map = map;
	}

	public int distance(String str1, String str2, double maxDist) {
		
		// we convert the String text to Character Array for
        // easy indexing
        char[] text = str1.toCharArray();
 
        // we convert the String pattern to Character Array
        // to access each letter in the String easily.
        char[] pattern = str2.toCharArray();
 
        // Index shows the function bitap search if they are
        // equal at a particular index or not
        int index = SearchString(text, pattern, maxDist);
 
        // If the pattern is not equal to the text of the
        // string approximately Then we tend to return -1 If
        // index is -1 Then we print there is No match
        if (index == -1) {
//            System.out.println("\nNo Match\n");
            return (int) (maxDist+1);
        }
 
        else {
 
            // Else if there is a match
            // Then we print the position of the index at
            // where the pattern and the text matches.
//            System.out.println(
//                "\nPattern found at index: \n" + index);
//        	System.out.println("Pattern " + str2 + " found in " + str1 + " at index " + index);
            return 0;
        }
	}
	
	private int bitap_search(char[] text, char[] pattern)
    {
 
        // Here the len variable is taken
        // This variable accepts the pattern length as its
        // value
        int len = pattern.length;
 
        // This is an array of pattern_mask of all
        // character values in it.
 
        long pattern_mask[]
            = new long[Character.MAX_VALUE + 1];
 
        // Here the variable of being long type is
        // complemented with 1;
 
        long R = ~1;
 
        // Now if the length of the pattern is 0
        // we would return -1
        if (len == 0) {
            return -1;
        }
 
        // Or if the length of the pattern exceeds the
        // length of the character array Then we would
        // declare that the pattern is too long. We would
        // return -1
       
        if (len > 63) {
 
            System.out.println("Pattern too long!");
            return -1;
        }
 
        // Now filling the values in the pattern mask
        // We would run th eloop until the max value of
        // character Initially all the values of Character
        // are put up inside the pattern mask array And
        // initially they are complemented with zero
       
        for (int i = 0; i <= Character.MAX_VALUE; ++i)
 
            pattern_mask[i] = ~0;
 
        // Now len being the variable of pattern length ,
        // the loop is set  till there Now the pattern being
        // the index of the pattern_mask 1L means the long
        // integer is shifted to left by i times The result
        // of that is now being complemented the result of
        // the above is now being used as an and operator We
        // and the pattern_mask and the result of it
       
        for (int i = 0; i < len; ++i)
            pattern_mask[pattern[i]] &= ~(1L << i);
 
       
        // Now the loop is made to run until the text length
        // Now what we do this the R array is used
        // as an Or function with pattern_mask at index of
        // text of i
 
        for (int i = 0; i < text.length; ++i) {
 
            R |= pattern_mask[text[i]];
 
            // Now  result of the r after the above
            // operation
            // we shift it to left side by 1 time
 
            R <<= 1;
 
            // If the 1L long integer if shifted left of the
            // len And the result is used to and the result
            // and R array
            // If that result is equal to 0
            // We return the index value
            // Index=i-len+1
           
            if ((R & (1L << len)) == 0)
 
                return i - len + 1;
        }
 
        // if the index is not matched
        // then we return it as -1
        // stating no match found.
       
        return -1;
    }
	
	public static int SearchString(char[] text, char[] pattern, double k)
	{
		int result = -1;
		int m = pattern.length;
		int[] R;
		int[] patternMask = new int[128];
		int i, d;
		if(m == 0) return 0;
		if (m > 31) return -1; //Error: The pattern is too long!

		R = new int[(int) ((k + 1) * Integer.BYTES)];
		for (i = 0; i <= k; ++i)
			R[i] = ~1;

		for (i = 0; i <= 127; ++i)
			patternMask[i] = ~0;

		for (i = 0; i < m; ++i)
			patternMask[pattern[i]] &= ~(1 << i);

		for (i = 0; i < text.length; ++i)
		{
			int oldRd1 = R[0];

			R[0] |= patternMask[text[i]];
			R[0] <<= 1;

			for (d = 1; d <= k; ++d)
			{
				int tmp = R[d];

				R[d] = (oldRd1 & (R[d] | patternMask[text[i]])) << 1;
				oldRd1 = tmp;
			}

			if (0 == (R[(int) k] & (1 << m)))
			{
				result = (i - m) + 1;
				break;
			}
		}

		return result;
	}
	
	public double distanceAttributes(HashMap<Character,  List<String>> stringAttribs, HashMap<Character, List<String>> motifAttribs) {
		// the motif and the substring matches, now we have to check the attributes
		// a motif can have different lists of attributes
		// given each pair motif+list of attributes, we compare it with the actual substring+list of attributes
		// in particular, we have to check if the attributes in the motif are contained in the list of attributes of the substring
		// and that their values are more or less similar.
		double totDistance = 0;
		for(Entry<Character, List<String>> motifAttr : motifAttribs.entrySet()) {
			if(stringAttribs.containsKey(motifAttr.getKey())) {
				// the substring contains the given motif attrib
				double tolerance = attribs.getTolerance(map.get(motifAttr.getKey()));
				/* System.out.println("comparing: " + Double.parseDouble(motifAttr.getValue().get(0)) + 
						" with: " + Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))
						+ "tolerance: " + tolerance); */
				if((Double.parseDouble(motifAttr.getValue().get(0)) - tolerance) <= (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))) &&
						(Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0))) <= (Double.parseDouble(motifAttr.getValue().get(0)) + tolerance)) {
						totDistance += 0;
				} else {
					double a = (Double.parseDouble(motifAttr.getValue().get(0)) + tolerance) - (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0)));
					double b = (Double.parseDouble(motifAttr.getValue().get(0)) - tolerance) - (Double.parseDouble(stringAttribs.get(motifAttr.getKey()).get(0)));
					totDistance += Math.min(Math.abs(a), Math.abs(b));
				}
			} else {
				totDistance += 200;
			}
		}
		// System.err.println(totDistance);
		return totDistance;
	}
}
