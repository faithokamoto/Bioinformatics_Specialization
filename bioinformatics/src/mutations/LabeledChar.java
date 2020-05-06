package mutations;

public class LabeledChar {
	private final char letter;
	private final int label;
	
	public static final int NO_LABEL = Integer.MIN_VALUE + 1;
	public static final int START = Integer.MIN_VALUE;
	public static final int END = Integer.MAX_VALUE;
	
	public static char[] specialOrder = null;
	
	public LabeledChar(char letter) {
		this(letter, NO_LABEL);
	}
	
	public LabeledChar(char letter, int label) {
		this.letter = letter;
		this.label = label;
	}
	
	public static LabeledChar[] convert(char[] arr) {
		LabeledChar[] converted = new LabeledChar[arr.length];
		for (int i = 0; i < arr.length; ++i) converted[i] = new LabeledChar(arr[i]);
		return converted;
	}
	
	/**
	 * Basic binary search for a LabeledChar array
	 * @param arr the array to search through
	 * @param look the LabeledChar to look for
	 * @return the index of the LabeledChar, or -1 if no found
	 */
	public static int indexOf(LabeledChar[] arr, LabeledChar look) {
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
	
	public char getLetter() {return letter;}
	
	public int getLabel() {return label;}
	
	public int compareTo(LabeledChar other) {
		if (label < other.getLabel()) return -1;
		else if (label > other.getLabel()) return 1;
		if (specialOrder != null) {
			int i1 = HMM.indexOf(specialOrder, letter);
			int i2 = HMM.indexOf(specialOrder, other.getLetter());
			if (i1 < i2) return -1;
			else if (i1 > i2) return 1;
		}
		if (letter < other.getLetter()) return -1;
		else if (letter > other.getLetter()) return 1;
		return 0;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof LabeledChar)) return false;
		LabeledChar other = (LabeledChar) obj;
		return other.getLetter() == getLetter() &&
				other.getLabel() == getLabel();
	}
	
	public String toString() {
		if (label == NO_LABEL) return letter + "";
		else return letter + "" + label;
	}
}