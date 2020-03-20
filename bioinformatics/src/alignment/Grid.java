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
				refer.get(i - 1).addOut(refer.get(i));
				
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
					
					// adding this node as an out to the other
					refer.get(i - cols - 1).addOut(refer.get(i));
				}
			}
			
			// if this Node is not in the first row
			if (i >= cols) {
				// add a vertical path between it and the one above
				refer.get(i).addIn(refer.get(i - cols), indelPenalty);
				refer.get(i - 1).addOut(refer.get(i));
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
			// if vertical jumps are allowed & indels are penalized
			if (startVertTaxi && indelPenalty < 0) {
				// the optimal strategy is to taxi from the source
				refer.get(row * cols).setWeight(0);
				refer.get(row * cols).setBacktrack(super.getSource());
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
				refer.get(col).setBacktrack(super.getSource());
			}
			else {
				refer.get(col).setWeight(col * indelPenalty);
				refer.get(col).setBacktrack(refer.get(col - 1));
			}
		}
		
		// used to track the maximal weighted Node (which will taxi to the sink)
		Node bestNode = super.getSource();
		int maxWeight = 0;
		// loop over all rows (excluding first)
		for (int row = 1; row < rows; row++) {
			// loop over all columns (excluding first)
			for (int col = 1; col < cols; col++) {
				// grab the current Node
				Node curNode = refer.get(row * cols + col);
				
				// parallel arrays to hold all possible paths in
				int index = 0;
				int[] pathVals = new int[3];
				Node[] orderedNodes = new Node[3];
				// loop over all paths in
				for (Node in : curNode.getIns().keySet()) {
					// note the weight if this path is used (weight of node + weight of path)
					pathVals[index] = in.getWeight() + curNode.getIns().get(in);
					// note, in the parallel array, which node corresponds to this path
					orderedNodes[index] = in;
					// move the index being added to forward
					index++;
				}
				
				// find the index of the maximum value in pathVals
				index = 0;
				for (int i = 1; i < 3; i++) if (pathVals[i] > pathVals[index]) index = i;
				
				if((endHorizTaxi || (endVertTaxi && col == cols - 1)) && 
						(endVertTaxi || (endHorizTaxi && row == rows - 1)) && pathVals[index] < 0) {
					curNode.setWeight(0);
					curNode.setBacktrack(super.getSource());
				} else {
					// set this node's optimal weight and backtrack
					curNode.setWeight(pathVals[index]);
					curNode.setBacktrack(orderedNodes[index]);
				}
				
				if ((endHorizTaxi || (endVertTaxi && col == cols - 1)) && 
						(endVertTaxi || (endHorizTaxi && row == rows - 1))
						&& curNode.getWeight() > maxWeight) {
					maxWeight = curNode.getWeight();
					bestNode = curNode;
				}
			}
		}
		if ((endHorizTaxi || endVertTaxi) && super.getSink() != bestNode) {
			super.getSink().setWeight(maxWeight);
			super.getSink().setBacktrack(bestNode);
		}
	}

	public int getIndelPenalty() {return this.indelPenalty;}
	
	public Node getNode(int id) {return this.refer.get(id);}
	
	public void addNode(int id) {this.refer.put(id, new Node(id));}
	
	public int getCols() {return this.cols;}
	
	public int getRows() {return this.rows;}
}