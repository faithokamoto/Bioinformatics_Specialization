package phylogeny;

// used to create a matrix
import java.util.HashMap;

/**
 * Represents a distance matrix (like a 2D array), and is backed by a HashMap of HashMaps.
 * Initialized by a 2D array, and has methods that merge, delete, and find the closest
 * columns. And, of, course, has accessor methods for its matrix.
 * @author faith
 */
public abstract class DistanceMatrix {
	/**
	 * the backing distance matrix
	 */
	private HashMap<Integer, HashMap<Integer, Double>> matrix;
	/**
	 * holds the number of nodes associated with each ID#
	 */
	private HashMap<Integer, Integer> numNodes;
	/**
	 * holds the maximal node ID in the matrix
	 */
	private int maxNode;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes matrix and maxNode
	 * @param arrMatrix a distance matrix array
	 */
	public DistanceMatrix(double[][] arrMatrix) {
		maxNode = arrMatrix.length - 1;
		initializeMatrix(arrMatrix);
	}
	
	/**
	 * Initializes the backing distance matrix
	 * <br>
	 * Loops over the array, initializing numNodes to 1 and
	 * matrix to the values stored in it.
	 * @param arrMatrix a distance matrix array
	 */
	public void initializeMatrix(double[][] arrMatrix) {
		// initialize to empty
		matrix = new HashMap<Integer, HashMap<Integer, Double>>();
		numNodes = new HashMap<Integer, Integer>();
		
		// loop over all rows in the array
		for (int i = 0; i < arrMatrix.length; i++) {
			// initialize this row in matrix
			matrix.put(i, new HashMap<Integer, Double>());
			// this row in matrix starts with one node
			numNodes.put(i, 1);
			// loop over all values in this row, adding to matrix
			for (int j = 0; j < arrMatrix.length; j++)
				matrix.get(i).put(j, arrMatrix[i][j]);
		}
	}
	
	/**
	 * Deletes a node's information from the distance matrix
	 * <br>
	 * Deletes the row, then loops over all other rows to
	 * delete the column. Recalculates maxNode if necessary
	 * @param id the ID# of the node to delete
	 * @throws IllegalArgumentException if the ID# is not in the matrix
	 */
	public void delete(int id) {
		// check if the row was there in the first place
		if (!matrix.containsKey(id))
			throw new IllegalArgumentException("Node with ID#" + id + " is not in matrix");
		// remove this node's row
		matrix.remove(id);
		// remove this node from numNodes
		numNodes.remove(id);
		
		// loop over all other rows, removing this column
		for (Integer row : matrix.keySet())
			matrix.get(row).remove(id);
		// if this node was the maximum, recalculate the maximum
		if (id == maxNode) calcMaxNode();
	}
	
	/**
	 * Recalculates the maximum node ID#
	 * <br>
	 * Loops over all row labels with typical maximizing code
	 */
	private void calcMaxNode() {
		// initialize the current maximum to the absolute minimum
		int max = Integer.MIN_VALUE;
		// loop over keys, check if > than max, if so set max
		for (Integer row : matrix.keySet()) if (row > max) max = row;
		// set maxNode to the maximum observed
		maxNode = max;
	}
	
	/**
	 * Merge two rows into one
	 * <br>
	 * Subclasses should implement this, creating their mergedValues and then 
	 * calling the other merge
	 * @param one one node
	 * @param two another node
	 */
	public abstract void merge(int one, int two);
	
	/**
	 * Merge two rows into one
	 * <br>
	 * Adds a new row and column to matrix, using the passed-in values.
	 * Then deletes the old rows. Updates maxNode.
	 * @param one one node
	 * @param two another node
	 * @param mergedValues the new values for the row
	 * @throws IllegalArgumentException if the nodes being merged do not both exist
	 */
	protected void merge(int one, int two, HashMap<Integer, Double> mergedValues) {
		// check if the nodes exist
		if (matrix.get(one) == null || matrix.get(two) == null)
			throw new IllegalArgumentException("Cannot merge columns " + one + " & "
					+ two + ", at least one does not exist");
		// the maximum node is increased (new node ID# being added)
		maxNode++;
		
		// add this new row to the matrix
		matrix.put(maxNode, mergedValues);
		// loop over all rows
		for (Integer row : getNodes())
			// as long as this row isn't the ones being added/merged
			if (row != one && row != two && row != maxNode) 
				// add in value
				matrix.get(row).put(maxNode, mergedValues.get(row));
			
		
		// distance to self is always 0
		matrix.get(maxNode).put(maxNode, 0.0);
		// update numNodes
		numNodes.put(maxNode, numNodes.get(one) + numNodes.get(two));
		// delete
		delete(one);
		delete(two);
	}
	
	/**
	 * Finds the two nodes closest to each other
	 * <br>
	 * Loops over all node-pairs to find the closest ones
	 * @return the ID#s of the closest nodes
	 */
	public abstract int[] closest();
	
	// getters
	
	public double get(int one, int two) {return matrix.get(one).get(two);}
	
	public HashMap<Integer, Double> get(int id) {return matrix.get(id);}
	
	public int size() {return matrix.size();}
	
	public int getMaxNode() {return maxNode;}
	
	public int[] getNodes() {
		int[] nodes = new int[size()];
		int i = 0;
		for (Integer node : matrix.keySet()) {
			nodes[i] = node;
			i++;
		}
		return nodes;
	}
	
	public int getNumNodes(int id) {return numNodes.get(id);}
	
	/**
	 * Overridden toString
	 * <br>
	 * Just use the toString of the backing HashMap
	 */
	public String toString() {return matrix.toString();}
}