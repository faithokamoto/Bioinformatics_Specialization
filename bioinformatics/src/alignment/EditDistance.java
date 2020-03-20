package alignment;

/**
 * <h1>Edit Distance Calculator</h1>
 * A special kind of Grid which can be used to calculate the edit distance (# of changes
 * required to turn one into another) between two strings.
 * @author faith
 */
public class EditDistance extends Grid {
	/**
	 * This class should never be initialized without strings,
	 * that defeats the purpose of finding the edit difference
	 */
	private EditDistance() {super(0, 0, 0, null, false, false);}
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes all instance variables.
	 * @param one the string to start with
	 * @param two the string to edit one into
	 */
	public EditDistance(String one, String two) {
		// initialize most instance variables
		super(one.length() + 1, two.length() + 1, -1, new Path(), false, false);
		// Initialize nodes
		initializeNodes(null, 0, -1, one, two);
	}
	
	/**
	 * <h1>Calculate the edit distance between the strings</h1>
	 * First runs through the values of all nodes, then finds
	 * the value of the sink node (the end, all edits).
	 */
	public void calcEdits() {
		calculateNodes();
		System.out.println(-getSink().getWeight());
	}
}
