package mutations;

/**
 * Has all kinds of methods to deal with suffix arrays/Burrows-Wheeler (BW) transforms.
 * <br>
 * Public methods:
 * <ul>
 * 	<li>convert a string to a suffix array</li>
 * 	<li>convert a string to its BW transform</li>
 * 	<li>convert a BW transform to the original string</li>
 * 	<li>generate a last-to-first pointer array from a BW transform</li>
 * 	<li>find the number of times a pattern (or patterns) appear in a string using its BW transform</li>
 * </ul>
 * In the O notations, n = arr.length & k = bw.length() / str.length()
 * @author faith
 */
public class SuffixArray {
	/**
	 * Shifts a section of an array up an index. O(end - start)
	 * @param arr the array to shift
	 * @param start the first index to shift left
	 * @param end the last index to shift left
	 */
	private static void shiftLeft(String[] arr, int start, int end) {
		// check to make sure the arguments are valid
		if (arr == null)
			throw new IllegalArgumentException("Can't shift a null array");
		if (start < 0 || end >= arr.length)
			throw new ArrayIndexOutOfBoundsException("Invalid start/end indices ("
					+ start + "->" + end + ") on an array length " + arr.length);
		
		// shift end down if needed
		if (end == arr.length - 1) end--;
		// loop over end->start, setting index one up to equal current index
		for (int i = end; i >= start; --i) arr[i + 1] = arr[i];
	}
	
	/**
	 * Shifts a section of an array up an index. O(end - start)
	 * @param arr the array to shift
	 * @param start the first index to shift left
	 * @param end the last index to shift left
	 */
	private static void shiftLeft(int[] arr, int start, int end) {
		// check to make sure the arguments are valid
		if (arr == null)
			throw new IllegalArgumentException("Can't shift a null array");
		if (start < 0 || end >= arr.length)
			throw new ArrayIndexOutOfBoundsException("Invalid start/end indices ("
					+ start + "->" + end + ") on an array length " + arr.length);
		
		// shift end down if needed
		if (end == arr.length - 1) end--;
		// loop over end->start, setting index one up to equal current index
		for (int i = end; i >= start; --i) arr[i + 1] = arr[i];
	}
	
	/**
	 * Shifts a section of an array up an index. O(end - start)
	 * @param arr the array to shift
	 * @param start the first index to shift left
	 * @param end the last index to shift left
	 */
	private static void shiftLeft(char[] arr, int start, int end) {
		// check to make sure the arguments are valid
		if (arr == null)
			throw new IllegalArgumentException("Can't shift a null array");
		if (start < 0 || end >= arr.length)
			throw new ArrayIndexOutOfBoundsException("Invalid start/end indices ("
					+ start + "->" + end + ") on an array length " + arr.length);
		
		// shift end down if needed
		if (end == arr.length - 1) end--;
		// loop over end->start, setting index one up to equal current index
		for (int i = end; i >= start; --i) arr[i + 1] = arr[i];
	}
	
	/**
	 * Flips (reverses) an array. O(n)
	 * @param arr the array to flip
	 */
	private static void flip(char[] arr) {
		// check to make sure the argument is valid
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
	 * Finds the index of a char in a sorted array. O(floor(log2(end))) in worst case
	 * @param arr the array to search through
	 * @param check the char to look for
	 * @param end the highest index to check
	 * @return the index of the char, or -1 if it is not in the range given
	 */
	private static int indexOf(char[] arr, char check, int end) {
		// check to make sure that the arguments are valid
		if (arr == null)
			throw new IllegalArgumentException("Can't search through a null array");
		if (end >= arr.length)
			throw new ArrayIndexOutOfBoundsException("Can't use end index " + end
					 + " in an array of length " + arr.length);
		
		// the leftmost index to search starts at 0
		int left = 0;
		// the rightmost index to search starts at end
		int right = end;
		
		// while there are still indices left to search
		while (left <= right) {
			// calculate the middle index
			int mid = (right + left) / 2;
			
			// if the middle index has the correct char, index has been found!
			if (arr[mid] == check) return mid;
			// or if the middle index's char comes before the correct one,
			// shift left index to ignore all chars before & including mid
			else if (arr[mid] < check) left = mid + 1;
			// similar logic if the middle index's char comes after
			else right = mid - 1;
		}
		
		// if the char wasn't found, return -1
		return -1;
	}
		
	/**
	 * Fills up several important arrays.
	 * O(k(floor(log2(k))) + floor(log2(k)) - k + k^2) in worst case.
	 * In a normal case (4 chars appearing randomly and 1 appearing once) O(k^2)
	 * @param bw a BW transform
	 * @param chars an array with the chars that appear in bw, sorted
	 * @param numEach a parallel array to chars with the frequency of each char in bw
	 * @param charLabels labels bw's chars with the frequency before each position
	 */
	private static void generateCharLabels(String bw, char[] chars, int[] numEach, int[] charLabels) {
		// check to make sure that arguments are valid
		if (bw == null)
			throw new IllegalArgumentException("Can't label a null string");
		if (chars == null || numEach == null || charLabels == null)
			throw new IllegalArgumentException("Can't fill up null arrays");
		if (charLabels.length != bw.length())
			throw new IllegalArgumentException("Improper number of label spots ("
					+ charLabels.length + " spots for a  " + bw.length() + "-long string");
		if (chars.length < bw.length())
			throw new IllegalArgumentException("Might run out of room in chars array ("
					+ chars.length + " spots for a  " + bw.length() + "-long string");
		if (numEach.length < bw.length())
			throw new IllegalArgumentException("Might run out of room in numEach array ("
					+ numEach.length + " spots for a  " + bw.length() + "-long string");
		
		// initialize return variable
		int numChars = 0;
		// loop over each char in bw
		for (int i = 0; i < bw.length(); ++i) {
			// save the current char
			char cur = bw.charAt(i);
			// try to find the index of this char in chars
			int index = indexOf(chars, cur, numChars - 1);
			
			// if it was found, label the char appropriately
			if (index != -1) charLabels[i] = numEach[index]++;
			
			// otherwise
			else {
				// label it as the first char
				charLabels[i] = 0;
				// assume that it will be added as the last char
				int addIndex = numChars++;
				// while the char before in chars should come after, move index backwards
				while (addIndex > 0 && chars[addIndex - 1] > cur) --addIndex;
				// shift both numEach and chars to accommodate the new char
				shiftLeft(numEach, addIndex, numChars - 1);
				shiftLeft(chars, addIndex, numChars - 1);
				// add the proper values to both arrays
				numEach[addIndex] = 1;
				chars[addIndex] = cur;
			}
		}
	}
	
	/**
	 * Converts a string to a suffix array. O(k^2) in worst case
	 * @param str the string to convert
	 * @return the starting positions of all suffixes, sorted
	 */
	public static int[] toSuffixArray(String str) {
		// check to make sure that argument is valid
		if (str == null)
			throw new IllegalArgumentException("Can't make suffix array of a null string");
		
		// initialize return variable
		int[] starts = new int[str.length()];
		// initialize the array of sorted suffixes
		String[] suffixes = new String[starts.length];
		// loop over all starting spots in str
		for (int i = 0; i < str.length(); ++i) {
			// assume that the current suffix will be sorted last
			int addIndex = i;
			// look-char to check for difference as the first one in this suffix
			int j = i;
			// while there are more suffixes & chars to search through
			while (addIndex > 0 && j < str.length()) {
				// if check-char is the same as sorting-char, move that look-char right
				if (suffixes[addIndex - 1].charAt(j - i) == str.charAt(j)) ++j;
				// or if check-char comes after sorting-char
				else if (suffixes[addIndex - 1].charAt(j - i) > str.charAt(j)) {
					// move suffix being considered left
					--addIndex;
					// reset look-char
					j = i;
				}
				// or if check-char comes before sorting-char, addIndex is correct
				else break;
			}
			
			// shift the arrays left to accommodate new value
			shiftLeft(starts, addIndex, i);
			shiftLeft(suffixes, addIndex, i);
			// add new value in
			suffixes[addIndex] = str.substring(i);
			starts[addIndex] = i;
		}
		return starts;
	}
	
	/**
	 * Converts a string to its BW transform. O(k^2 + k) in worst case
	 * @param str the string to convert
	 * @return the BW transform of the string
	 */
	public static String toBWTransform(String str) {
		if (str == null)
			throw new IllegalArgumentException("Can't transform a null string");
		
		// initialize return variable
		// makes a string by building up a char array for memory purposes
		char[] bw = new char[str.length()];
		// get the sorted-suffix array
		int[] suffixArray = toSuffixArray(str);
		
		// loop through suffix array
		for (int i = 0; i < suffixArray.length; ++i) {
			// save the index of the char from str that goes in bw here
			int charIndex = suffixArray[i] - 1;
			// loop around if necessary
			if (charIndex == -1) charIndex = str.length() - 1;
			// save into bw
			bw[i] = str.charAt(charIndex);
		}
		
		return new String(bw);
	}
	
	/**
	 * Generates a last-to-first pointer array from a BW transform.
	 * O(k(floor(log2(k))) + floor(log2(k)) + k^2) in worst case.
	 * O(k^2 + k) in normal case
	 * @param bw a BW transform
	 * @return a last-to-first pointer array
	 */
	public static int[] getLastToFirst(String bw) {
		if (bw == null)
			throw new IllegalArgumentException("Can't find last-to-first of a null transform");
		
		// initialize return variable
		int[] ltf = new int[bw.length()];
		
		// initialize all the helper arrays
		char[] chars = new char[ltf.length];
		int[] numEach = new int[ltf.length];
		int[] charLabels = new int[ltf.length];
		// fill up the helper arrays
		generateCharLabels(bw, chars, numEach, charLabels);
		
		// loop over all rows in bw
		for (int i = 0; i < bw.length(); ++i) {
			// save the current char
			char cur = bw.charAt(i);
			// assume that this row will point to row #1 in the first column
			int firstRow = 0;
			// save which char is being considered (starts at the first char)
			int charNum = 0;
			// while the current char is still before the one needed
			while (chars[charNum] != cur) {
				// shift firstRow down to skip past all of the current char
				firstRow += numEach[charNum];
				// move to the next char
				++charNum;
			}
			
			// this last-row points to the calculated first-row
			// offset by the number of this char that appears before (char label) in bw
			ltf[i] = firstRow + charLabels[i];
		}
		
		return ltf;
	}
	
	/**
	 * Convert a BW transform back to the original.
	 * O(k(floor(log2(k))) + floor(log2(k)) + k + k^2) in worst case.
	 * O(k^2 + k) in normal case
	 * @param bw a BW transform
	 * @return the original string
	 */
	public static String fromBWTransform(String bw) {
		if (bw == null) 
			throw new IllegalArgumentException("Can't un-transform a null transform");
		
		// initialize return variable
		// build up a char array instead of a string for memory purposes
		char[] str = new char[bw.length()];
		// get a last-to-first array
		int[] ltf = getLastToFirst(bw);
		// save the current row (start with the last row)
		int curRow = bw.indexOf('@');
		// loop over all indexes needed
		for (int i = 0; i < str.length; ++i) {
			// save the current char
			str[i] = bw.charAt(curRow);
			// move curRow backwards, using ltf
			curRow = ltf[curRow];
		}
		//rotateLeft(str);
		// flip the string (since it was build up backwards)
		flip(str);
		
		return new String(str);
	}
	
	public static int countMatches(String bw, String pattern) {
		return countMatches(bw, pattern, getLastToFirst(bw));
	}
	
	/**
	 * Count the number of times a pattern appears in a string by its BW transform
	 * @param bw a BW transform
	 * @param pattern the pattern to search for
	 * @param lastToFirst a last-to-first array
	 * @return the number of matches in the original string
	 */
	public static int countMatches(String bw, String pattern, int[] lastToFirst) {
		// checking for argument validity
		if (bw == null)
			throw new IllegalArgumentException("Can't look with a null transform");
		if (lastToFirst == null)
			throw new IllegalArgumentException("Can't use a null last-to-first array");
		if (lastToFirst.length != bw.length())
			throw new IllegalArgumentException("Last-to-first array has the wrong number of pointers");
		
		// set initial bounds with the assumption that all rows match
		int top = 0;
		int bottom = bw.length() - 1;
		
		// loop until what is needed is found
		while (true) {
			// loop over all chars of pattern from the back (since ltf moves backwards)
			for (int i = pattern.length() - 1; i >= 0; --i) {
				// save the current look-char
				char cur = pattern.charAt(i);
				// initialize top & bottom rows with next char
				int lastTop = -1;
				int lastBottom = -1;
				
				// loop over all rows being considered, and if this row's char matches
				for (int j = top; j <= bottom; ++j) if (bw.charAt(j) == cur) {
					// if a top hasn't been found, set top and bottom to this spot
					if (lastTop == -1) {
						lastTop = j;
						lastBottom = j;
					}
					// otherwise just update bottom
					else lastBottom = j;
				}
				
				// if nothing was found, return 0
				if (lastBottom == -1) return 0;
				// otherwise convert to new bounds for top and bottom
				else {
					top = lastToFirst[lastTop];
					bottom = lastToFirst[lastBottom];
				}
			}
			return bottom - top + 1;
		}
	}
	
	/**
	 * Counts the number of matches for some patterns using the BW transform.
	 * 
	 * @param bw a BW transform
	 * @param patterns the patterns to look for
	 * @return the number of times each pattern appear in a parallel array
	 */
	public static int[] countMatches(String bw, String[] patterns) {
		// initialize return variable
		int[] matches = new int[patterns.length];
		// calculate the last-to-first array
		int[] ltf = getLastToFirst(bw);
		// count matches for each pattern
		for (int i = 0; i < patterns.length; ++i) matches[i] = countMatches(bw, patterns[i], ltf);
		
		return matches;
	}
}