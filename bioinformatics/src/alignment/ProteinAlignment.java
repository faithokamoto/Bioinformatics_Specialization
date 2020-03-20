package alignment;

// for data structures
import java.util.HashMap;

/**
 * <h1>Specialized Graph for Protein Alignment</h1>
 * Specialized kind of a Grid that uses an amino acid scoring matrix to compute path weights.
 * @author faith
 */
public class ProteinAlignment extends Grid {
	/**
	 * This class should never be initialized without strings and a scoring matrix,
	 * that defeats the purpose of PROTEIN and ALIGNMENT
	 */
	private ProteinAlignment() {super(0, 0, 0, null, false, false);}
	
	/**
	 * <h1>Local Alignment Constructor</h1>
	 * Less detailed alignment specification.
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param scoring the scoring matrix to weight paths with
	 * @param local whether to allow local alignment or not
	 * 
	 */
	public ProteinAlignment(String one, String two,
			HashMap<Character, HashMap< Character, Integer>> scoring, boolean local) {
		// calling master constructor with proper params
		this(one, two, scoring, local, local);
	}
	
	/**
	 * <h1>Constructor</h1>
	 * Allows for the highest degree of specificity with alignment.
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param scoring the scoring matrix to weight paths with
	 * @param vertTaxi whether to allow skipping over string one
	 * @param horizTaxi whether to allow skipping over string two
	 */
	public ProteinAlignment(String one, String two,
			HashMap<Character, HashMap<Character, Integer>> scoring, boolean vertTaxi, boolean horizTaxi) {
		// initialize path
		super(one.length() + 1, two.length() + 1, -5,
				new StringAlignmentPath(one, two, vertTaxi || horizTaxi), vertTaxi, horizTaxi);
		// initialize nodes
		initializeNodes(scoring, 1, -5, one, two);
	}

}