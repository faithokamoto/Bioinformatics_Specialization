package alignment;

/**
 * <h1>Specialized Path for String Alignment</h1>
 * In this Path, there are two extra instance variables (the strings being aligned by a Graph),
 * and they are referred to in the toString, where parts are used instead of Node IDs.
 * @author faith
 */
public class StringAlignmentPath extends Path {
	// the strings being aligned
	private String one, two;
	// whether local alignment was allowed
	private boolean local;
	
	/**
	 * <h1>Constructor</h1>
	 * Does the superclass constructor and then initializes its instance variables.
	 * @param one the first string being aligned
	 * @param two the second string being aligned
	 * @param local whether local alignment was allowed
	 */
	public StringAlignmentPath(String one, String two, boolean local) {
		super();
		this.one = one;
		this.two = two;
		this.local = local;
	}

	/**
	 * <h1>Overridden toString</h1>
	 * Loops through each node, using the difference between IDs to determine what kind of
	 * path was taken and add the appropriate chars to the overall String.
	 * @return the path's weight and the strings' alignment
	 */
	@Override
	public String toString() {
		// initialize return variable
		String[] ret = {"", ""};
		
		// index to start at
		int index = 0;
		// while the beginning jump is detected as a taxi, remove the node there
		while (local && super.getNodes().get(index + 1).getWeight() == 0) super.removeNode();
		
		// the first "lastID" (ID of node at the start of the mini-path) is the source's ID
		int lastID = super.getNodes().get(0).getID();
		// placeholder to make calculations clearer
		int cols = two.length() + 1;
		int total = (one.length() + 1) * cols;
		// loop over all nodes after the first
		for (int i = 1; i < super.getNodes().size(); i++) {
			// save the "nextID" (ID of node at the end of the mini-path)
			int nextID = super.getNodes().get(i).getID();
			
			// it the difference was 1, this is a horizontal move
			if ((nextID - lastID) % total == 1) {
				// no chars from one were used
				ret[0] += '-';
				// calculate which char from two was used
				ret[1] += two.charAt((lastID  % total) % cols);
			}
			// if the difference was a column's worth, this is a vertical move
			else if ((nextID - lastID) % total == cols) {
				// calculate which char from one was used
				ret[0] += one.charAt((lastID % total) / cols);
				// no chars from two were used
				ret[1] += '-';
			}
			// if the difference was a column + 1's worth, this is a diagonal move
			else if ((nextID - lastID - 1) % total == cols){
				// calculate the chars from one & two which were used
				ret[0] += one.charAt((lastID % total) / cols);
				ret[1] += two.charAt((lastID % total) % cols);
			}
			// the end node is the start node of the next path
			lastID = nextID;
		}
		
		return super.getWeight() + "\n" + ret[0] + "\n" + ret[1];
	}
}
