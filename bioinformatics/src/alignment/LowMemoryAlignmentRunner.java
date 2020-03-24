package alignment;

/**
 * <h1>Basic runner for the <code>LowMemoryAlignment</code> class</h1>
 * @author faith
 *
 */
public class LowMemoryAlignmentRunner extends ProteinAlignmentRunner {
	// run the LowMemoryAlignment code
	public static void main(String[] args) {
		// read in the strings to align
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		// read in the scoring matrix to use
		String blossom = readFileAsString("src/alignment/blossom.txt");
		// get the alignment of the strings
		String[] alignment = LowMemoryAlignment.align(data[0], data[1], readScoringMatrix(blossom));
		// print out the score and the alignment
		System.out.println(LowMemoryAlignment.score);
		for (String a : alignment) System.out.println(a);
	}
}
