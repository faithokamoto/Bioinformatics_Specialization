package phylogeny;

// used to create a matrix
import java.util.HashMap;

/**
 * A specialized distance matrix for UPGMA, with merge & closest override
 * @see phylogeny.DistanceMatrix
 * @author faith
 */
public class DistanceMatrixUPGMA extends DistanceMatrix {	
	/**
	 * Constructor
	 * <br>
	 * Initializes matrix and maxNode
	 * @param arrMatrix a distance matrix array
	 */
	public DistanceMatrixUPGMA(double[][] arrMatrix) {
		super(arrMatrix);
	}
	
	/**
	 * Calculates a weighted average between two columns at on row
	 * @param one the first column
	 * @param two the second column
	 * @param row the row to find a weighted average in
	 * @return the weighted average
	 */
	private double weightedAverage(int one, int two, int row) {
		// weights each row, col value by the number of nodes in the col
		return (get(one, row) * getNumNodes(one) + get(two, row) * getNumNodes(two)) 
				/ (getNumNodes(one) + getNumNodes(two));
	}
	
	/**
	 * Merge two rows into one
	 * <br>
	 * Calculates the new values (weighted averages), then merges
	 * @param one one node
	 * @param two another node
	 */
	@Override
	public void merge(int one, int two) {
		HashMap<Integer, Double> mergedRow = new HashMap<Integer, Double>();
		for (Integer row : getNodes()) {
			if (row != one && row != two)
				mergedRow.put(row, weightedAverage(one, two, row));
		}
		super.merge(one, two, mergedRow);
	}
	
	/**
	 * Finds the two nodes closest to each other
	 * <br>
	 * Loops over all node-pairs to find the minimum values, saving ID#s
	 * @return the ID#s of the closest nodes
	 */
	public int[] closest() {
		// initialize return variable
		int[] coords = new int[2];
		// initialize the minimum weight between nodes to the maximum possible
		double min = Integer.MAX_VALUE;
		
		// loop over all node-pairs
		for (Integer row : getNodes()) for (Integer col : getNodes())
			// if this node-pair is closer than the current best
			if (row != col && get(row, col) < min) {
				// set all values to reflect that
				min = get(row, col);
				coords[0] = row;
				coords[1] = col;
			}

		return coords;
	}
}