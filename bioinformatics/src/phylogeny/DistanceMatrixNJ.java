package phylogeny;

import java.util.HashMap;

/**
 * A specialized distance matrix for the Neighbor Joining Algorithm, with merge & closest override
 * @see phylogeny.DistanceMatrix
 * @author faith
 */
public class DistanceMatrixNJ extends DistanceMatrix {
	/**
	 * Constructor
	 * <br>
	 * Initializes the backing matrix
	 * @param arrMatrix a distance matrix array
	 */
	public DistanceMatrixNJ(double[][] arrMatrix) {
		super(arrMatrix);
	}
	
	/**
	 * Calculates the total distance from one node to all others
	 * <br>
	 * Loops over each distance from the passed-in-node, adding together
	 * @param id the ID# in the matrix of the target node
	 * @return the sum of all values in the HashMap associated with that Node
	 */
	public double totalDist(int id) {
		// initialize return variable
		double total = 0;
		// add all values up
		for (Integer col : getNodes()) total += get(id, col);
		
		return total;
	}
	
	/**
	 * Merge two rows into one
	 * <br>
	 * Calculates the new values (limb lengths), then merges
	 * @param one one node
	 * @param two another node
	 */
	@Override
	public void merge(int one, int two) {
		HashMap<Integer, Double> mergedRow = new HashMap<Integer, Double>();
		for (Integer row : getNodes()) {
			if (row != one && row != two)
				mergedRow.put(row, 0.5 * (get(row, one) + get(row, two) - get(one, two)));
		}
		super.merge(one, two, mergedRow);
	}
	
	/**
	 * Finds the two nodes closest to each other
	 * <br>
	 * Loops over all node-pairs to find the minimum values in the NJ matrix, saving ID#s
	 * @return the ID#s of the closest nodes
	 */
	@Override
	public int[] closest() {
		// initialize return variable
		int[] coords = new int[2];
		// initialize the minimum distance to the maximum value
		double min = Integer.MAX_VALUE;
		// initialize a lookup map for total distances
		HashMap<Integer, Double> totals = new HashMap<Integer, Double>();
		// fill the total distance map
		for (Integer row : getNodes()) totals.put(row, totalDist(row));
		
		// loop over all node-pairs
		for (Integer row : getNodes()) for (Integer col : get(row).keySet()) 
			// check that these are two separate nodes
			if (row != col) {
				// calculate the value in the neighbor-joining matrix
				double val = (size() - 2) * get(row, col) - totals.get(row) - totals.get(col);
				// if it is better than the current minimum
				if (val < min) {
					// set values to reflect that
					min = val;
					coords[0] = row;
					coords[1] = col;
				}
			}
		
		return coords;
	}
}
