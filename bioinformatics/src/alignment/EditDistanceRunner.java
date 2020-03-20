package alignment;

/**
 * <h1>Basic runner for the <code>EditDistance</code> class</h1>
 * @author faith
 */
public class EditDistanceRunner extends Runner {
	// run the EditDistance code
	public static void main(String[] args) {
		// read in the strings
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		// initialize the EditDistance
		EditDistance run = new EditDistance(data[0], data[1]);
		// calculate the number of edits
		run.calcEdits();
	}
}
