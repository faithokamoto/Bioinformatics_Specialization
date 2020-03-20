package alignment;

// for data structures
import java.util.HashMap;

/**
 * <h1>Basic runner for the <code>ProteinAlignmentGraph</code> class</h1>
 * @author faith
 */
public class ProteinAlignmentGraphRunner extends Runner {
	/**
	 * <h1>Reads data into a scoring matrix</h1>
	 * Splits the data into rows, using the header as a reference to figure out the coordinates,
	 * and then splits each row into int values and puts them in the proper part of the matrix.
	 * @param data a properly formatted scoring matrix
	 * @return the completed scoring matrix
	 */
	public static HashMap<Character, HashMap<Character, Integer>> readScoringMatrix(String data) {
		// initialize return variable
		HashMap<Character, HashMap<Character, Integer>> scoring = new HashMap<Character, HashMap<Character, Integer>>();
		
		// split data into rows
		String[] rows = data.split("\n");
		// use the first row to find the letters which corresponding to columns
		char[] letters = rows[0].replace(" ", "").toCharArray();
		// loop over all rows of data (excluding the header row)
		for (int i = 1; i < rows.length; i++) {
			// initialize this column's spot in scoring
			scoring.put(letters[i - 1], new HashMap<Character, Integer>());
			
			// pull the values of each matching letter from the row
			String[] vals = rows[i].replace("  ", " ").split(" ");
			// loop over those values
			for (int j = 1; j < vals.length; j++)
				// add them and their value to this column's spot
				scoring.get(letters[i - 1]).put(letters[j - 1], Integer.parseInt(vals[j]));
			
		}
		
		return scoring;
	}
	
	// run the Protein Alignment Graph code
	public static void main(String[] args) {
		// read in the data
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		// pull out the strings
		String one = data[0];
		String two = data[1];
		
		// read in the scoring matrix
		String pam = readFileAsString("src/alignment/pam.txt");
		// read the scoring matrix into a Map
		HashMap<Character, HashMap<Character, Integer>> scoring = readScoringMatrix(pam);
		
		// create a ProteinAlignmentGraph object
		ProteinAlignment run = new ProteinAlignment(one, two, scoring, true, true);
		// find its path
		run.findPath();
	}
}
