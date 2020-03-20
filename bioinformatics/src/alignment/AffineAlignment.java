package alignment;

import java.util.HashMap;

public class AffineAlignment extends Graph {
	int total, cols;
	HashMap<Integer, Node> refer;
	
	/**
	 * This class should never be instantiated without strings to align
	 */
	private AffineAlignment() {super(null);}
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes all instance variables
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param scoring a scoring matrix
	 * @param gapOpenPenalty the penalty for starting a gap
	 * @param gapExtPenalty the penalty for extending a gap (< gOP)
	 */
	public AffineAlignment(String one, String two, HashMap<Character, HashMap<Character, Integer>> scoring,
			int gapOpenPenalty, int gapExtPenalty) {
		// initialize path
		super(new StringAlignmentPath(one, two, false));
		
		// calculate cols and total
		this.cols = two.length() + 1;
		this.total = this.cols * (one.length() + 1);
		
		// initialize Nodes
		this.refer = new HashMap<Integer, Node>();
		initializeNodes(scoring, gapOpenPenalty, gapExtPenalty, one, two);
	}
	
	/**
	 * <h1>Initializes all Nodes, with in-paths specified</h1>
	 * @param scoring a scoring matrix
	 * @param gapOpenPenalty the penalty for starting a gap
	 * @param gapExtPenalty the penalty for extending a gap by 1 char
	 * @param one the first string to align
	 * @param two the second string to align
	 */
	public void initializeNodes(HashMap<Character, HashMap<Character, Integer>> scoring, 
			int gapOpenPenalty, int gapExtPenalty, String one, String two) {
		// loop over all IDs that must be added
		for (int i = 0; i < total; i++) {
			// add a Node to the down matrix
			refer.put(i + total, new Node(i + total));
			// if this Node is not in the first row
			if (i >= cols) {
				// and not in the second row, add a gap-extending path up
				if (i > cols) refer.get(i + total).addIn(refer.get(i + total - cols), gapExtPenalty);
				// either way, add a gap-opening path to the diagonal matrix
				refer.get(i + total).addIn(refer.get(i - cols), gapOpenPenalty);
			}
			
			// similar logic for the right matrix
			refer.put(i + total * 2, new Node(i + total * 2));
			if (i % cols != 0) {
				if (i % cols != 1) refer.get(i + total * 2).addIn(refer.get(i + total * 2 - 1), gapExtPenalty);
				refer.get(i + total * 2).addIn(refer.get(i - 1), gapOpenPenalty);
			}
			
			// add a Node to the diagonal matrix
			refer.put(i, new Node(i));
			// add gap-ending paths to the parallel spots in the other matrices
			refer.get(i).addIn(refer.get(i + total), 0);
			refer.get(i).addIn(refer.get(i + total * 2), 0);
			// if this Node is not in the first row or column
			if (i > cols && i % cols != 0)
				// add a diagonal path with weight dictated by scoring matrix
				refer.get(i).addIn(refer.get(i - cols - 1), 
						scoring.get(one.charAt((i / cols) - 1)).get(two.charAt((i % cols) - 1)));
		}
		
		// set the source and sink nodes to the proper spots in the diagonal matrix
		setSource(refer.get(0));
		setSink(refer.get(total - 1));
	}
	
	/**
	 * <h1>Calculates, for each node, the optimal weight and backtrack</h1>
	 * Loops over all nodes, calculating in forced topographical order.
	 */
	@Override
	public void calculateNodes() {
		// loop over all id values
		for (int i = 0; i < total; i++) {
			// the right & down matrices have to be calculated before the
			// diagonal one, as it relies on them
			maximizeNode(refer.get(i + total));
			maximizeNode(refer.get(i + total * 2));
			maximizeNode(refer.get(i));
		}
	}
}