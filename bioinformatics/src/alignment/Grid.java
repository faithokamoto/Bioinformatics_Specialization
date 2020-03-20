package alignment;

// for data structures
import java.util.HashMap;

/**
 * <h1>String Grid</h1>
 * A special type of directed graph which has rows and columns, and optimized initializeNodes()
 * and calculateNodes() that take advantage of the structure.
 * @author faith
 */
public abstract class Grid extends Graph {
	// the penalty for insertions/deletions, the number of rows/columns
	private int indelPenalty, rows, cols;
	// whether to look for local alignments or not
	private boolean startVertTaxi, endVertTaxi, startHorizTaxi, endHorizTaxi;
	// a reference map to make getting a Node easier
	private HashMap<Integer, Node> refer;

	
	public Grid(int rows, int cols, int indelPenalty, Path pathType, boolean local) {
		this(rows, cols, indelPenalty, pathType, local, local, local, local);
	}
	
	public Grid(int rows, int cols, int indelPenalty, Path pathType, boolean vertTaxi, boolean horizTaxi) {
		this(rows, cols, indelPenalty, pathType, vertTaxi, vertTaxi, horizTaxi, horizTaxi);
	}
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes all instance variables (except nodes)
	 * @param rows the # of rows (counting by nodes)
	 * @param cols the # of columns
	 * @param indelPenalty the penalty for insertions/deletions
	 * @param pathType allows for a specialized Path
	 * @param local wheteher to look for local alignments or not
	 */
	public Grid(int rows, int cols, int indelPenalty, Path pathType, 
			boolean startVertTaxi, boolean endVertTaxi, boolean startHorizTaxi, boolean endHorizTaxi) {
		// initialize the Path
		super(pathType);
		
		// initialize other instance variables
		this.rows = rows;
		this.cols = cols;
		this.indelPenalty = indelPenalty;
		this.startVertTaxi = startVertTaxi;
		this.endVertTaxi = endVertTaxi;
		this.startHorizTaxi = startHorizTaxi;
		this.endHorizTaxi = endHorizTaxi;
		this.refer = new HashMap<Integer, Node>();
	}
	
	public void initializeNodes(HashMap<Character, HashMap<Character, Integer>> scoring,
			int match, int mismatch, String one, String two) {
		// loop over all ID#s required for the grid
		for (int i = 0; i < rows * cols; i++) {
			// create a Node with this ID in the reference map
			refer.put(i, new Node(i));
			
			// if this Node is not in the first column
			if (i % cols != 0) {
				// add a horizontal path between it and the one to the left
				refer.get(i).addIn(refer.get(i - 1), indelPenalty);
				
				// if this Node is not in the first row as well
				if (i >= cols) {
					// add a diagonal path between it and the one diagonally to the upper left
					
					// if a specialized scoring matrix was given
					if (scoring != null)
						// put the proper value and use it as the path's weight
						refer.get(i).addIn(refer.get(i - cols - 1), 
								scoring.get(one.charAt((i / cols) - 1)).get(two.charAt((i % cols) - 1)));
					// or not, if the corresponding chars match
					else if (one.charAt((i / cols) - 1) == two.charAt((i % cols) - 1))
						// use the match value as the path's weight
						refer.get(i).addIn(refer.get(i - cols - 1), match);
					// or if the chars don't match, used the mismatch value
					else refer.get(i).addIn(refer.get(i - cols - 1), mismatch);
				}
			}
			
			// if this Node is not in the first row
			if (i >= cols) {
				// add a vertical path between it and the one above
				refer.get(i).addIn(refer.get(i - cols), indelPenalty);
			}
		}
		
		// set the source (Node #0)
		setSource(refer.get(0));
		// set the sink (last Node)
		setSink(refer.get(rows * cols - 1));
	}
	
	/**
	 * <h1>Overridden, specifically optimized calculateNodes()</h1>
	 * First calculates the (very simple) weights and backtracks for the first row & column,
	 * then loops through all inner nodes, finding the maximal path and saving the weight & backtrack.
	 */
	@Override
	public void calculateNodes() {
		// loop over all Nodes in first column (excluding source)
		for (int row = 1; row < rows; row++) {
			// if vertical jumps from source are allowed & indels are penalized
			if (startVertTaxi && indelPenalty < 0) {
				// the optimal strategy is to taxi from the source
				refer.get(row * cols).setWeight(0);
				refer.get(row * cols).setBacktrack(getSource());
			}
			// or if looking for only global alignment
			else {
				// this Node's weight is the number of paths * the IP
				refer.get(row * cols).setWeight(row * indelPenalty);
				// this Node's only possible backtrack is the one above it
				refer.get(row * cols).setBacktrack(refer.get((row - 1) * cols));
			}
		}
		
		// similar loop for the first row
		for (int col = 1; col < cols; col++) {
			if (startHorizTaxi && indelPenalty < 0) {
				refer.get(col).setWeight(0);
				refer.get(col).setBacktrack(getSource());
			}
			else {
				refer.get(col).setWeight(col * indelPenalty);
				refer.get(col).setBacktrack(refer.get(col - 1));
			}
		}
		
		// used to track the maximal weighted Node (which will taxi to the sink)
		Node bestNode = null;
		int maxWeight = 0;
		// loop over all rows (excluding first)
		for (int row = 1; row < rows; row++) {
			// loop over all columns (excluding first)
			for (int col = 1; col < cols; col++) {
				// grab the current Node
				Node curNode = refer.get(row * cols + col);
				// maximize (by paths) the weight and backtrack of this Node
				maximizeNode(curNode);
				
				// if taxi to source is allowed and it would be beneficial
				if(startVertTaxi && startHorizTaxi && curNode.getWeight() < 0) {
					// taxi back to source
					curNode.setWeight(0);
					curNode.setBacktrack(getSource());
				}
				
				// if taxi to sink is allowed (or it is not required in one direction)
				// and the current Node has the best weight so far
				if ((endHorizTaxi || (endVertTaxi && col == cols - 1)) && 
						(endVertTaxi || (endHorizTaxi && row == rows - 1)) && curNode.getWeight() > maxWeight) {
					// set the best-Node stats to this Node
					maxWeight = curNode.getWeight();
					bestNode = curNode;
				}
			}
		}
		
		// if taxi helped
		if (bestNode != null && bestNode != getSink()) {
			// taxi from bestNode to the sink
			getSink().setWeight(maxWeight);
			getSink().setBacktrack(bestNode);
		}
	}
	
	// various getters
	
	public Node getNode(int id) {return this.refer.get(id);}
	
	// refer should not be accessed directly, only added to
	
	public void addNode(int id) {this.refer.put(id, new Node(id));}
}