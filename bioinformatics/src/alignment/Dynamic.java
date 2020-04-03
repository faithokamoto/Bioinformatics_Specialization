package alignment;

//for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * <h1>Dynamic Programming for DAGs</h1>
 * Contains the necessary methods to find the maximal weighted path through a grid-based
 * directed weighted graph, and some periphery methods such as finding the longest common
 * substring between two strings and the minimum number of coins to change money.
 * @author faith
 */
public class Dynamic {
	/**
	 * <h1>Shifts all the values in an array forward and adds a value to the back
	 * Loops through the array, setting each index equal to the one after it, then adds val in to the end.
	 * <br>
	 * precondition: arr is not empty
	 * @param arr the array to shift
	 * @param val the value to add to the back
	 */
	public static void shift(int[] arr, int val) {
		// shift values forward
		for (int i = 0; i < arr.length - 1; i++)
			arr[i] = arr[i + 1];
		// set value of the back
		arr[arr.length - 1] = val;
	}
	
	/**
	 * <h1>Calculates the minimum number of coins needed for change</h1>
	 * Loops through all numbers up to changeNeeded, trying each coin to minimize the number of coins
	 * required to reach the current number, by consulting a dynamic programming array.
	 * <br>
	 * preconditions: changeNeeded >= smallest value in coins, coins is not empty, all of coins' values > 0
	 * <br>
	 * calls: shift
	 * @param changeNeeded the money that must be changed
	 * @param coins the denominations available
	 * @return the... see above
	 */
	public static int leastChange(int changeNeeded, int[] coins) {
		// calculate the maximum denomination
		int maxDenom = coins[0];
		// initialize dynamic programming array
		int[] minCoins = new int[maxDenom];
		
		// loop over all numbers up to changeNeeded
		for (int i = 1; i <= changeNeeded; i++) {
			// initialize this value to maximum
			int min = Integer.MAX_VALUE;
			// loop over all denominations
			for (int coin : coins)
				// if this coin is small enough & the # of coins required is less than current min
				if (i >= coin && minCoins[maxDenom - coin] + 1 < min)
					// update the min with new minimum number of coins
					min = minCoins[maxDenom - coin] + 1;
			// shift the array forward, adding the new min to the back
			shift(minCoins, min);
		}
		
		// return the proper value in the array
		return minCoins[maxDenom - 1];
	}

	/**
	 * <h1>Finds the value of the most-weighted path through an only right-or-down graph</h1>
	 * Using a dynamic array where the value at [i][j] is the maximum weight for a path going
	 * to the node at the ith row and jth column. First calculates the value for the edge row
	 * and column, then the inner nodes are the max value of the path coming from above or left.
	 * <br>
	 * precondition: down is 1 longer than right, right[] is 1 longer than down[]
	 * @param down the weights of the down paths
	 * @param right the weights of the right paths
	 * @return the... see above
	 */
	public static int maxEdges(int[][] down, int[][] right) {
		// calculate the width and height of this grid
		int height = down.length;
		int width = down[0].length - 1;
		// initialize dynamic programming array
		int[][] maxNodes = new int[height + 1][width + 1];
		
		// loop over all nodes in the first column (excepting origin)
		for (int row = 1; row <= height; row++)
			// this node's value is the one above it + the down-path before it
			maxNodes[row][0] = maxNodes[row - 1][0] + down[row - 1][0];
		
		// loop over all nodes in the first row (excepting origin)
		for (int column = 1; column <= width; column++)
			// this node's value is the one to the left + the right-path before it
			maxNodes[0][column] = maxNodes[0][column - 1] + right[0][column - 1];
		
		// loop over all rows (excepting the first)
		for (int row = 1; row <= height; row++)
			// loop over all columns (excepting the first)
			for (int column = 1; column <= width; column++)
				// this node's value is the maximum of the one above it + the down-path before it
				maxNodes[row][column] = Math.max(maxNodes[row - 1][column] + down[row - 1][column],
											// and the one to the left + the right-path before it
												maxNodes[row][column - 1] + right[row][column - 1]);
			
		// return the proper value in the array
		return maxNodes[height][width];
	}
	
	/**
	 * <h1>Creates a back-tracking array holding which direction to go in an alignment matrix</h1>
	 * Uses a dynamic programming array, finds the maximum weight of each node (max of all incoming nodes,
	 * considering that matching chars leads to a +1 bonus on the diagonal path), and then saves the 
	 * direction gone at each node in a backtracking array. <strong>Directions are:</strong> 0 for diagonal,
	 * 1 for down, and 2 for right.
	 * @param first a string to align
	 * @param second another string to align it with
	 * @return an matrix with indicators of how to back up to get the maximum weighted path
	 */
	public static byte[][] lcsBacktrack(String first, String second) {
		// initialize return variable
		byte[][] backtrack = new byte[first.length() + 1][second.length() + 1];
		// initialize dynamic programming array
		int[][] maxNodes = new int[first.length() + 1][second.length() + 1];
		
		// loop from second to last row
		for (int row = 1; row <= first.length(); row++) {
			// loop from second to last column
			for (int column = 1; column <= second.length(); column++) {
				// determine if the corresponding chars - if so, the weight of the diagonal edge is 1
				int match = 0;
				if (first.charAt(row - 1) == second.charAt(column - 1)) match = 1;
				
				// the maximum weight of this node is the maximum of the nodes leading in to it
				maxNodes[row][column] = Math.max(maxNodes[row - 1][column], 
															// here accounting for weight of diag
						Math.max(maxNodes[row][column - 1], maxNodes[row - 1][column - 1] + match));
				
				// if the path used came from the top, this node backtracks up
				if (maxNodes[row][column] == maxNodes[row - 1][column])
					backtrack[row][column] = 1;
				// if the path used came from the left, this node backtracks right
				else if(maxNodes[row][column] == maxNodes[row][column - 1])
					backtrack[row][column] = 2;
			}
		}
		
		return backtrack;
	}
	
	/**
	 * <h1>Finds the longest common substring given a backtracking matrix</h1>
	 * Recursively calls itself, checking the direction to go and using that node,
	 * adding the proper char if a diagonal path was used.
	 * <br>
	 * preconditions: backtrack's length >= row, backtrack[]'s length >= column, ref's length >= row, column & row >= 0
	 * @param backtrack a backtracking matrix
	 * @param ref the string along the side
	 * @param row the row of the node being considered
	 * @param column the column of the node being considered
	 * @return the longest common substring between two strings
	 */
	public static String outputLCS(byte[][] backtrack, String ref, int row, int column) {
		// if on the edge, no diagonal paths exist, so end condition
		if (row == 0 || column == 0) return "";
		// if a diagonal path was used, return path for node diagonally back + char for this path
		else if (backtrack[row][column] == 0)
			return outputLCS(backtrack, ref, row - 1, column - 1) + ref.charAt(row - 1);
		// if a vertical path was used, return path for node above
		else if (backtrack[row][column] == 1)
			return outputLCS(backtrack, ref, row - 1, column);
		// else (a horizontal path was used) return path for node to the left
		else return outputLCS(backtrack, ref, row, column - 1);
	}
	
	/**
	 * <h1>Reads a file into a string</h1>
	 * Tries to access the file to read, returns if possible
	 * @param filename the path/name of the file to read
	 * @return the String contents of the file
	 * @throws IOException (if the filename was not valid)
	 */
	public static String readFileAsString(String filename) {
		// initialize return variable
		String text = "";
		
		try {
			// read the file into text
			text = new String(Files.readAllBytes(Paths.get(filename)));
			text = text.replace("\r", "");
		}
		
		// if that didn't work, explain why
		catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
	
	public static void main(String[] args) {
	}
}