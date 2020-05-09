package mutations;

/**
 * A char with a number label
 * @author faith
 */
public class LabeledChar implements Comparable<LabeledChar> {
	/**
	 * the char being labeled
	 */
	private final char letter;
	/**
	 * the label for the char
	 */
	private final int label;
	
	/**
	 * a label to use when there is no label known (a plain char, basically)
	 */
	public static final int NO_LABEL = Integer.MIN_VALUE + 1;
	/**
	 * a label to distinguish a start-state, which always comes first
	 */
	public static final int START = Integer.MIN_VALUE;
	/**
	 * a label to distinguish an end-state, which always comes last
	 */
	public static final int END = Integer.MAX_VALUE;
	
	/**
	 * a special ordering to use during comparisons; should be set by
	 * classes which use LabeledChar and require custom "alphabetizing"
	 */
	public static char[] specialOrder = null;
	
	/**
	 * Creates a LabeledChar with no label
	 * @param letter the char to use
	 */
	public LabeledChar(char letter) {
		this(letter, NO_LABEL);
	}
	
	/**
	 * Creates a LabeledChar with a label
	 * @param letter the char to label
	 * @param label the label for the char
	 */
	public LabeledChar(char letter, int label) {
		this.letter = letter;
		this.label = label;
	}
	
	/**
	 * Converts an array of plain chars to an equivalent array of LabeledChars
	 * @param arr the array to convert
	 * @return a LabeledChar array with the same ordering and letters
	 */
	public static LabeledChar[] convert(char[] arr) {
		// initialize return variable
		LabeledChar[] converted = new LabeledChar[arr.length];
		// loop over all spots, copying the char into a LabeledChar
		for (int i = 0; i < arr.length; ++i)
			converted[i] = new LabeledChar(arr[i]);
		
		return converted;
	}
	
	/**
	 * Basic binary search for a LabeledChar array
	 * @param arr the array to search through
	 * @param look the LabeledChar to look for
	 * @return the index of the LabeledChar, or -1 if no found
	 */
	public static final int indexOf(LabeledChar[] arr, LabeledChar look) {
		// include all indices in the search
		int left = 0;
		int right = arr.length - 1;
		
		// while some indices need to be searched
		while (left <= right) {
			// calculate the middle index
			int mid = (left + right) / 2;
			
			// if this index has the char, return it
			if (arr[mid].equals(look)) return mid;
			// or if this index comes before the char, discount all to left
			else if (arr[mid].compareTo(look) < 0) left = mid + 1;
			// same for discounting all to right
			else right = mid - 1;
		}
		
		// if not found, return -1
		return -1;
	}
	
	/**
	 * Getter for the char with a label
	 * @return the value of letter
	 */
	public final char getLetter() {return letter;}
	
	/**
	 * Getter for the label
	 * @return the value of label
	 */
	public final int getLabel() {return label;}
	
	public int compareTo(LabeledChar other) {
		if (other == null)
			throw new NullPointerException("Cannot compareTo a null");
		// first check the labels, lower labels come first
		
		if (label < other.getLabel()) return -1;
		else if (label > other.getLabel()) return 1;
		
		// now check the letters
		
		// if there is a special order to be considered
		if (specialOrder != null) {
			// grab the indexes of both letters
			int i1 = HMMBuilder.indexOf(specialOrder, letter);
			int i2 = HMMBuilder.indexOf(specialOrder, other.getLetter());
			
			// if they both are in the special order
			if (i1 != -1 && i2 != -1) {
				// earlier indices in special order come first
				if (i1 < i2) return -1;
				else if (i1 > i2) return 1;
			}
		}
		
		// use just regular character ordering (ASCII) if no special order
		if (letter < other.getLetter()) return -1;
		else if (letter > other.getLetter()) return 1;
		
		// if all of that was equal, then these are equal
		return 0;
	}
	
	public boolean equals(Object obj) {
		// check for class validity
		if (!(obj instanceof LabeledChar)) return false;
		
		// cast obj to LabeledChar, check letter & label
		LabeledChar other = (LabeledChar) obj;
		return other.getLetter() == getLetter() &&
				other.getLabel() == getLabel();
	}
	
	public String toString() {
		// if the label is a special case, don't print it
		if (label == NO_LABEL || label == START || label == END) return letter + "";
		// otherwise print letter and label
		else return letter + "" + label;
	}
}