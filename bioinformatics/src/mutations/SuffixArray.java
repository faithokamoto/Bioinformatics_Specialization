package mutations;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Has all kinds of methods for pattern matching with Burrow-Wheeler transforms
 * <br>
 * Public methods:
 * <ul>
 * 	<li>convert a string to a suffix array</li>
 * 	<li>convert a string to its BW transform</li>
 * 	<li>convert a BW transform to the original string</li>
 * 	<li>generate a last-to-first pointer array from a BW transform</li>
 * 	<li>find the number of times a pattern (or patterns) appear in a string
 * 		using its BW transform</li>
 *  <li>find the number of times a pattern (or patterns) appear in a string
 *  	using its BW transform with some mismatches allowed</li>
 *  <li>find the starting indices of a pattern (or patterns) in a string using
 *  	its BW transform</li>
 *  <li>find the starting indices of a pattern (or patterns) in a string using
 *  	its BW transform with some mismatches allowed</li>
 *  <li>determine which reads are close enough to some point in a genome</h1>
 * </ul>
 * Definitions/abbreviations:
 * <ul>
 * 	<li><strong>Suffix array (sa):</strong> all suffixes of a string (perhaps
 * 		with cyclic prefixes after) sorted, here represented as an array of
 * 		starting indices, in the order that their corresponding suffix appears
 * 		in the array</li>
 * 	<li><strong>Burrow-Wheeler transform (BW transform, bw):</strong> the last
 * 		column of a sorted cyclic suffix array, here represented as a string</li>
 * 	<li><strong>Last-to-first pointer array (ltf):</strong> pointers indicating
 * 		which row in the first column of a suffix array (all chars, sorted) has
 * 		the corresponding char to a row in the last column (bw), here represented
 * 		as an array of row numbers, with the [i]th element being the pointer from
 * 		the ith char in bw<li>
 * 	<li><strong>Char-label array (cl):</strong> how many identical chars appear
 * 		before a certain char in the bw, here represented as an array of char
 * 		labels, with the [i]th element being the label for the ith char in bw.</li>
 * 	<li><string>First-Occurrence array (fo):</strong> the first row index of a char
 * 		in the first column of a suffix array (all chars, sorted), here represented
 * 		as a parallel array to DNA</li>
 * </li>
 * </ul>
 * @author faith
 */
public class SuffixArray {
	/**
	 * the acceptable characters in a DNA string, sorted
	 */
	public static final char[] DNA = {'@', 'A', 'C', 'G', 'T'};

	/**
	 * Shifts a section of an array up an index.
	 * @param arr the array to shift
	 * @param start the first index to shift left
	 * @param end the last index to shift left
	 */
	private static void shiftLeft(int[] arr, int start, int end) {
		// check for argument validity
		if (arr == null)
			throw new IllegalArgumentException("Can't shift a null array");
		if (start < 0 || end >= arr.length || start > end)
			throw new ArrayIndexOutOfBoundsException("Invalid start/end indices ("
					+ start + "->" + end + ") on an array length " + arr.length);
		
		// shift end down if needed
		if (end == arr.length - 1) end--;
		// loop over end->start, setting index one up to equal current index
		for (int i = end; i >= start; --i) arr[i + 1] = arr[i];
	}
	
	/**
	 * Flips (reverses) an array.
	 * @param arr the array to flip
	 */
	private static void flip(char[] arr) {
		// check for argument validity
		if (arr == null)
			throw new IllegalArgumentException("Can't flip a null array");
		
		// loop over the first half of the indices
		for (int i = 0; i < arr.length / 2; ++i) {
			// swap the values in opposite indices
			char temp = arr[i];
			arr[i] = arr[arr.length - i - 1];
			arr[arr.length - i - 1] = temp;
		}
	}
	
	/**
	 * Fills up several important arrays.
	 * @param bw a BW transform
	 * @param fO a first-occurrence array for bw
	 * @param cL a char-label array for bw
	 */
	private static void generateCharLabels(String bw, int[] fo, int[] cl) {
		// check for argument validity
		if (bw == null)
			throw new IllegalArgumentException("Can't label a null string");
		if (fo == null || cl == null)
			throw new IllegalArgumentException("Can't fill up null arrays");
		if (cl.length != bw.length())
			throw new IllegalArgumentException("Improper number of label spots ("
					+ cl + " spots for a  " + bw.length() + "-long string");
		
		// loop over each char in bw
		for (int i = 0; i < bw.length(); ++i) {
			// grab the index in DNA of the current char
			int index = Arrays.binarySearch(DNA, bw.charAt(i));
			
			// if this is an invalid char, throw an error
			if (index == -1)
				throw new IllegalArgumentException("Requires @ACGT DNA sequences");
			
			// otherwise, label by the number before, then increment the number seen
			else cl[i] = fo[index]++;
				
		}
		
		// there will have been 1 of the @ char, so its firstO will be 0 and so-far will be 1
		int soFar = fo[0]--;
		// loop over all other chars
		for (int i = 1; i < fo.length; ++i) {
			// add the amount that this char appeared to so-far
			soFar += fo[i];
			// firstO of this char was the old soFar
			fo[i] = soFar - fo[i];
		}
	}
	
	/**
	 * Converts a string to a suffix array.
	 * @param str the string to convert
	 * @return a suffix array
	 */
	public static int[] toSuffixArray(String str) {
		// check for argument validity
		if (str == null)
			throw new IllegalArgumentException("Can't make suffix array of a null string");

		// initialize return variable
		int[] starts = new int[str.length()];
		
		// the last suffix will always be sorted first
		starts[0] = str.length() - 1;
		// initialize an array holding jump spots: the last suffix with a certain two-base prefix
		int[] jump = new int[(DNA.length - 1) * (DNA.length - 1)];
		// since one suffix is already filled in, everything jumps to 1
		Arrays.fill(jump, 1);
		
		// loop over all starting spots in str
		for (int i = 0; i < str.length() - 1; ++i) {
			// grab the first spot safe to jump to
			int jumpSpot = (Arrays.binarySearch(DNA, str.charAt(i)) - 1) * 4
					+ Arrays.binarySearch(DNA, str.charAt(i + 1)) - 1;
			// assume that this spot is the index to add
			int addIndex = jump[jumpSpot];
			
			// look-char to check for difference as the first one in this suffix
			int j = i;
			// while there are more suffixes & chars to search through
			while (addIndex > 0 && j < str.length()) {
				// if check-char is the same as sorting-char, move that look-char right
				if (str.charAt(starts[addIndex - 1] - i + j) == str.charAt(j)) ++j;
				
				// or if check-char comes after sorting-char
				else if (str.charAt(starts[addIndex - 1] - i + j) > str.charAt(j)) {
					// move suffix being considered left
					--addIndex;
					// reset look-char
					j = i;
				}
				
				// or if check-char comes before sorting-char, addIndex is correct
				else break;
			}
			
			// shift the arrays left to accommodate new value
			shiftLeft(starts, addIndex, i + 1);
			// add new value in
			starts[addIndex] = i;
			// increment this suffix's & all suffixes' after jump-spots
			for (j = jumpSpot; j < jump.length; ++j) jump[j] = jump[j] + 1;
		}
		
		return starts;
	}
	
	/**
	 * Converts a string to its BW transform.
	 * @param str the string to convert
	 * @return the BW transform of the string
	 */
	public static String toBWTransform(String str) {
		return toBWTransform(str, toSuffixArray(str));
	}
	
	/**
	 * Converts a string to its BW transform.
	 * @param str the string to convert
	 * @param sA a pre-computed suffix array
	 * @return the BW transform of the string
	 */
	private static String toBWTransform(String str, int[] sa) {
		// check for argument validity
		if (str == null)
			throw new IllegalArgumentException("Can't transform a null string");
		if (sa == null)
			throw new IllegalArgumentException("Can't use a null suffix array");
		if (sa.length != str.length())
			throw new IllegalArgumentException("Suffix array is the wrong length ("
					+ sa.length + ") for a string length " + str.length());
		
		// initialize return variable
		// makes a string by building up a char array for memory purposes
		char[] bw = new char[str.length()];
		
		// loop through suffix array
		for (int i = 0; i < sa.length; ++i) {
			// save the index of the char from str that goes in bw here
			int charIndex = sa[i] - 1;
			// loop around if necessary
			if (charIndex == -1) charIndex = str.length() - 1;
			// save into bw
			bw[i] = str.charAt(charIndex);
		}
		
		return new String(bw);
	}
	
	/**
	 * Generates a last-to-first pointer array from a BW transform.
	 * @param bw a BW transform
	 * @return a last-to-first pointer array
	 */
	public static int[] getLastToFirst(String bw) {
		// check for argument validity
		if (bw == null)
			throw new IllegalArgumentException("Can't find last-to-first of a null transform");
		
		// initialize return variable
		int[] ltf = new int[bw.length()];
		
		// initialize the helper arrays
		int[] fo = new int[DNA.length];
		int[] cl = new int[ltf.length];
		// fill up the helper arrays
		generateCharLabels(bw, fo, cl);
		
		// loop over all rows in bw
		for (int i = 0; i < bw.length(); ++i) {
			// save the current char
			char cur = bw.charAt(i);
			
			// this last-row points to the calculated first-row
			// offset by the number of this char that appears before (char label) in bw
			ltf[i] = fo[Arrays.binarySearch(DNA, cur)] + cl[i];
		}
		
		return ltf;
	}
	
	/**
	 * Convert a BW transform back to the original.
	 * @param bw a BW transform
	 * @return the original string
	 */
	public static String fromBWTransform(String bw) {
		// check for argument validity
		if (bw == null) 
			throw new IllegalArgumentException("Can't un-transform a null transform");
		
		// initialize return variable
		// build up a char array instead of a string for memory purposes
		char[] str = new char[bw.length()];
		// get a last-to-first array
		int[] ltf = getLastToFirst(bw);
		// save the current row (start with the last row)
		int curRow = bw.indexOf('@');
		
		// loop over all indices needed
		for (int i = 0; i < str.length; ++i) {
			// save the current char
			str[i] = bw.charAt(curRow);
			// move curRow backwards, using ltf
			curRow = ltf[curRow];
		}

		// flip the string (since it was build up backwards)
		flip(str);
		
		return new String(str);
	}
	
	/**
	 * Counts the number of matches for a pattern in a string
	 * @param str the string to search through
	 * @param pattern the pattern to look for
	 * @return the number of times the pattern appears in the string
	 */
	public static int countMatches(String str, String pattern) {
		// compute the suffix array for this string
		int[] sa = toSuffixArray(str);
		// convert the string to its BW transform
		str = toBWTransform(str, sa);
		
		// the number of matches is the number of starts
		return findStarts(str, pattern, getLastToFirst(str), sa, 0,
				str.length() - 1, pattern.length() - 1).size();
	}

	/**
	 * Counts the number of matches for some patterns in a string.
	 * @param str a string
	 * @param patterns the patterns to look for
	 * @return the number of times each pattern appear in a parallel array
	 */
	public static int[] countMatches(String str, String[] patterns) {
		// initialize return variable
		int[] matches = new int[patterns.length];
		
		// calculate the suffix array of this string
		int[] sa = toSuffixArray(str);
		// convert the string to its BW transform
		str = toBWTransform(str, sa);
		// calculate the last-to-first array
		int[] ltf = getLastToFirst(str);
		// count matches for each pattern
		for (int i = 0; i < patterns.length; ++i)
			// the number of matches is the number of starts
			matches[i] = findStarts(str, patterns[i], ltf, sa, 0,
					str.length() - 1, patterns[i].length() - 1).size();
		
		return matches;
	}
	
	/**
	 * Counts the number of matches for a pattern in a string
	 * @param str the string to search through
	 * @param pattern the pattern to look for
	 * @param d the number of differences to allow
	 * @return the number of times the pattern appears in the string
	 */
	public static int countNearMatches(String str, String pattern, int d) {
		// compute the suffix array for this string
		int[] sa = toSuffixArray(str);
		// convert the string to its BW transform
		str = toBWTransform(str, sa);
		
		// the number of matches is the number of starts
		return findStarts(str, pattern, getLastToFirst(str), sa, d, 0,
				str.length() - 1, pattern.length() - 1).size();
	}
	
	 /**
	  * Counts the number of matches for some patterns in a string.
	  * @param str a string
	  * @param patterns the patterns to look for
	  * @param d the number of mismatches to allow
	  * @return the number of times each pattern appear in a parallel array
	 */
	public static int[] countNearMatches(String str, String[] patterns, int d) {
		// initialize return variable
		int[] matches = new int[patterns.length];
		
		// calculate the suffix array of this string
		int[] sa = toSuffixArray(str);
		// convert the string to its BW transform
		str = toBWTransform(str, sa);
		// calculate the last-to-first array
		int[] ltf = getLastToFirst(str);
		// count matches for each pattern
		for (int i = 0; i < patterns.length; ++i)
			// the number of matches is the number of starts
			matches[i] = findStarts(str, patterns[i], ltf, sa, d, 0,
					str.length() - 1, patterns[i].length() - 1).size();
		
		return matches;
	}
	
	/**
	 * Finds all starting indices of a pattern in a string
	 * @param str the string to search through
	 * @param pattern the pattern to search for
	 * @return all (sorted) starting indices of the pattern
	 */
	public static ArrayList<Integer> findStarts(String str, String pattern) {
		// get the suffix array for this string
		int[] sa = toSuffixArray(str);
		// convert this string to its BW transform
		str = toBWTransform(str);
		
		return findStarts(str, pattern, getLastToFirst(str), sa, 0, str.length() - 1, pattern.length() - 1);
	}
		
	/**
	 * Finds all starting indices of a pattern in a string using its BW transform
	 * @param bw a BW transform
	 * @param pattern the pattern to look for
	 * @param ltf a last-to-first pointer array
	 * @param sa a suffix array
	 * @param top the index of the top row to search through
	 * @param bottom the index of the bottom row to search through
	 * @param index the last index of pattern to use
	 * @return all (sorted) starting indices of the pattern
	 */
	private static ArrayList<Integer> findStarts(String bw, String pattern, int[] ltf, int[] sa,
			int top, int bottom, int index) {
		// check for argument validity
		if (bw == null)
			throw new IllegalArgumentException("Can't look with a null transform");
		if (ltf == null || sa == null)
			throw new IllegalArgumentException("Can't use null helper arrays (ltf/sa)");
		if (ltf.length != bw.length())
			throw new IllegalArgumentException("Last-to-first array has the wrong number of pointers"
					+ " (" + ltf.length + " for a " + bw.length() + "-length string)");
		if (sa.length != bw.length())
			throw new IllegalArgumentException("Suffix array has the wrong number of starts ("
					+ sa.length + " for a " + bw.length() + "-length string)");
		if (index < 0 || index >= pattern.length())
			throw new StringIndexOutOfBoundsException("Cannot use index " + index + " in a "
					+ pattern.length() + "-length pattern");
		if (top < 0 || bottom >= bw.length() || top > bottom)
			throw new IllegalArgumentException("Top & bottom row indices don't work "
					+ "(" + top + "->" + bottom + ")");
		
		// loop until what is needed is found
		while (true) {
			// loop over all chars of pattern from the back (since ltf moves backwards)
			for (int i = index; i >= 0; --i) {
				// save the current look-char
				char cur = pattern.charAt(i);
				// initialize top & bottom rows with next char
				int lastTop = -1;
				int lastBottom = -1;
				
				// loop over all rows being considered
				for (int j = top; j <= bottom; ++j)
					// if this row's char matches
					if (bw.charAt(j) == cur) {
						// note the top-row and break
						lastTop = j;
						break;
					}
				
				// if nothing was found, return that
				if (lastTop == -1) return new ArrayList<Integer>(0);
				
				// something was found! loop over all rows again, this time from back
				for (int j = bottom; j >= top; --j)
					// if this row's char matches
					if (bw.charAt(j) == cur) {
						// not the bottom-row and break
						lastBottom = j;
						break;
					}
				
				// convert to new bounds for top and bottom
				top = ltf[lastTop];
				bottom = ltf[lastBottom];
				
			}
			
			// initialize return variable to exact size
			ArrayList<Integer> starts = new ArrayList<Integer>(bottom - top + 1);
			// for each row under consideration, add its start-index from the suffix array
			for (int i = top; i <= bottom; ++i) starts.add(sa[i]);
			
			return starts;
		}
	}

	/**
	 * Finds all starting indices of some patterns in a string.
	 * NOTE: indices may be repeated if multiple patterns start at the same index
	 * @param str the string to search through
	 * @param patterns the patterns to search for
	 * @return all (sorted) starting indices of the patterns
	 */
	public static ArrayList<Integer> findStarts(String str, String[] patterns) {
		// initialize return variable
		ArrayList<Integer> starts = new ArrayList<Integer>();
		
		// get this string's suffix array
		int[] sa = toSuffixArray(str);
		// convert this string to its BW transform
		str = toBWTransform(str);
		// get this string's last-to-first pointer array
		int[] ltf = getLastToFirst(str);
		
		// add all starts of each pattern to the overall start list
		for (String pattern : patterns) starts.addAll(findStarts(str, pattern, ltf, sa, 0,
				str.length() - 1, pattern.length() - 1));
		
		// simple insertion sort of the starts list
		for (int i = 1; i < starts.size(); ++i) {
			int val = starts.remove(i);
			int addIndex = i;
			while (addIndex > 0 && starts.get(addIndex - 1) > val) --addIndex;
			starts.add(addIndex, val);
		}
		
		return starts;
	}
	
	/**
	 * Finds all starting indices of a pattern in a string with mismatches.
	 * @param str the string to search through
	 * @param pattern the pattern to search for
	 * @param d the maximum number of mismatches
	 * @return all (sorted) starting indices of the patterns
	 */
	public static ArrayList<Integer> findStarts(String str, String pattern, int d) {
		// get the suffix array of this string
		int[] sA = toSuffixArray(str);
		// convert this string to its BW transform
		str = toBWTransform(str);
		
		return findStarts(str, pattern, getLastToFirst(str), sA, d, 0,
				str.length() - 1, pattern.length() - 1);
	}
	
	/**
	 * Finds all starting indices of a pattern in a string with mismatches using its BW transform
	 * @param bw a BW transform
	 * @param pattern the pattern to search for
	 * @param ltf a last-to-first pointer array
	 * @param sa a suffix array
	 * @param d the maximum number of mismatches
	 * @param top the index of the top row to search through
	 * @param bottom the index of the bottom row to search through
	 * @param index the last index of pattern to use
	 * @return all (sorted) starting indices of pattern, with at most d mismatches, in the string
	 */
	private static ArrayList<Integer> findStarts(String bw, String pattern, int[] ltf, int[] sa,
			int d, int top, int bottom, int index) {
		// check for argument validity
		if (bw == null)
			throw new IllegalArgumentException("Can't look with a null transform");
		if (ltf == null || sa == null)
			throw new IllegalArgumentException("Can't use null helper arrays (ltf/sa)");
		if (ltf.length != bw.length())
			throw new IllegalArgumentException("Last-to-first array has the wrong number of pointers"
					+ " (" + ltf.length + " for a " + bw.length() + "-length string)");
		if (sa.length != bw.length())
			throw new IllegalArgumentException("Suffix array has the wrong number of starts ("
					+ sa.length + " for a " + bw.length() + "-length string)");
		if (index < 0 || index >= pattern.length())
			throw new StringIndexOutOfBoundsException("Cannot use index " + index + " in a "
					+ pattern.length() + "-length pattern");
		if (top < 0 || bottom >= bw.length() || top > bottom)
			throw new IllegalArgumentException("Top & bottom row indices don't work "
					+ "(" + top + "->" + bottom + ")");
		
		// if no differences are allowed, just use the regular findStarts
		if (d == 0) return findStarts(bw, pattern, ltf, sa, top, bottom, index);
		
		// initialize return variable
		ArrayList<Integer> starts = new ArrayList<Integer>();
		
		// if this is the last index (and d > 0), all chars are valid
		if (index == 0) {
			// loop over all valid rows
			for (int i = top; i <= bottom; ++i)
				// if this pattern does not wrap around
				if (sa[ltf[i]] + pattern.length() < bw.length())
					// add it in
					starts.add(sa[ltf[i]]);
			
			return starts;
		}
		
		// set up a bounds array ([top, bottom] for each char)
		int[][] bounds = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
		// save the number of chars encountered so far
		byte chars = 0;
		// save the index in DNA of the correct char, decrease since the current index will no be used again
		int correctIndex = Arrays.binarySearch(DNA, pattern.charAt(index--)) - 1;
		
		// loop over all rows from the top
		for (int i = top; i <= bottom; ++i) {
			// save the index of this row's char
			int charIndex = Arrays.binarySearch(DNA, bw.charAt(i)) - 1;
			// if it is a valid index and this char doesn't have a top-index yet
			if (charIndex >= 0 && bounds[charIndex][0] == -1) {
				// set this char's top-index
				bounds[charIndex][0] = i;
				// increment the number of chars found
				chars++;
				// if the maximum number of chars have been found, break
				if (chars == DNA.length - 1) break;
			}
		}
		
		// loop over all rows from the bottom
		for (int i = bottom; i >= top; --i) {
			// save the index of this row's char
			int charIndex = Arrays.binarySearch(DNA, bw.charAt(i)) - 1;
			// if it is a valid index and this char doesn't have a bottom-index yet
			if (charIndex >= 0 && bounds[charIndex][1] == -1) {
				// set this char's bottom-index
				bounds[charIndex][1] = i;
				
				// calculate converted top & bottom indices
				int newTop = ltf[bounds[charIndex][0]];
				int newBottom = ltf[bounds[charIndex][1]];
				
				// now going to add all starts of the pattern before this index, assuming this char
				
				// if this char is the matching char, don't decrease d
				if (charIndex == correctIndex)
					starts.addAll(findStarts(bw, pattern, ltf, sa, d, newTop, newBottom, index));
				// otherwise, decrease d, since one less difference is allowed
				else
					starts.addAll(findStarts(bw, pattern, ltf, sa, d - 1, newTop, newBottom, index));
				
				// decrement the amount of chars that needs to been seen again
				chars--;
				// if all chars have been seen again, break
				if (chars == 0) break;
			}
		}
		
		// convert to HashSet to get rid of duplicates, then convert back
		return new ArrayList<Integer>(new HashSet<Integer>(starts));
	}
	
	/**
	 * Finds all starting indices of some patterns in a string with mismatches
	 * NOTE: indices may be repeated if multiple patterns start at the same index
	 * @param str the string to search through
	 * @param patterns the patterns to search for
	 * @param d the maximum number of mismatches to allow
	 * @return all (sorted) starting indices of the patterns in the string
	 */
	public static ArrayList<Integer> findStarts(String str, String[] patterns, int d) {
		// initialize return variable
		ArrayList<Integer> starts = new ArrayList<Integer>();
		
		// get the suffix array of this string
		int[] sA = toSuffixArray(str);
		// convert this string to a BW transform
		str = toBWTransform(str);
		// get the last-to-first array of this string
		int[] ltf = getLastToFirst(str);
		
		// add all starts for each pattern
		for (String pattern : patterns)
			starts.addAll(findStarts(str, pattern, ltf, sA, d, 0, str.length() - 1, pattern.length() - 1));
		
		// simple insertion sort of starts
		for (int i = 1; i < starts.size(); ++i) {
			int val = starts.remove(i);
			int addIndex = i;
			while (addIndex > 0 && starts.get(addIndex - 1) > val) --addIndex;
			starts.add(addIndex, val);
		}
		
		return starts;
	}
	
	/**
	 * Determine which reads are close-enough to a string
	 * @param str the string to compare to
	 * @param reads the reads to compare
	 * @param d the maximum number of mismatches to allow
	 * @return all reads which are close enough to some spot in the string
	 */
	public static ArrayList<String> closeReads(String str, String[] reads, int d) {
		// initialize return variable
		ArrayList<String> close = new ArrayList<String>();
		
		// get the suffix array for this string
		int[] sA = toSuffixArray(str);
		// convert this string to the BW transform
		str = toBWTransform(str, sA);
		// get the last-to-first array for this string
		int[] ltf = getLastToFirst(str);
		
		// loop over all reads
		for (String read : reads)
			// if this read has at least one start, add it to the close-enough list
			if (!findStarts(str, read, ltf, sA, d, 0,
					str.length() - 1, read.length() - 1).isEmpty()) close.add(read);
		
		return close;
	}
}