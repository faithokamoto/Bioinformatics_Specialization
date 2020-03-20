package alignment;

/**
 * <h1>Basic runner for the String Alignment class</h1>
 * @author faith
 */
public class StringAlignmentRunner extends Runner {
	// run the StringAlignment code
	public static void main(String[] args) {
		// grab the strings
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		// instantiate the aligner
		StringAlignment run = new StringAlignment(data[0], data[1], false, true);
		// find the alignment
		run.findPath();
	}
}
