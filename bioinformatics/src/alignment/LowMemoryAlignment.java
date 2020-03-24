package alignment;

// for data structures
import java.util.HashMap;

/**
 * <h1>Memory Efficient Protein Alignment</h1>
 * Similar to Protein Alignment, but uses (quite sparse) linear memory and only quadratic runtime.
 * @author faith
 */
public class LowMemoryAlignment {
	/**
	 * The penalty for vertical/horizontal moves
	 */
	private static final int INDEL_PENALTY = -5;
	/**
	 * A string of all dashes ("-"), used to get a string of a certain length
	 */
	private static final String DASHES = "------------------------------";
	/**
	 * The strings being aligned (shipped from align to toMid)
	 */
	private static String side, top;
	/**
	 * Backtracking pointers (shipped from toMid to findMiddleEdge)
	 */
	private static byte[] backtrack;
	/**
	 * The score of the optimal alignment
	 */
	public static Integer score;
	
	/**
	 * </h1>Calculates the values of nodes in the middle column</h1>
	 * Loops over all columns up to midCol, storing the optimal values in curCol
	 * and the optimal backtracks in backtrack. Transfers curCol to lastCol each loop.
	 * @param fromSource whether to build a scoring graph from the upper left (or from the lower right)
	 * @param up the upper bound of the overall alignment graph to consider, noting that coordinates start at 0
	 * @param down the lower bound ( > up)
	 * @param left the left bound
	 * @param right the right bound ( > left)
	 * @param scoring a scoring matrix for diagonal moves
	 * @return an int[] array, where index i is the maximum value of the node at row i in column mid
	 */
	private static int[] toMid(boolean fromSource, int up, int down, int left, int right, 
			HashMap<Character, HashMap<Character, Integer>> scoring) {
		// calculate the # of the middle column, assuming fromSource
		int midCol = (left + right) / 2 - left;
		// update assumption if necessary
		if (!fromSource) midCol = (left + right + 1) / 2 - left;
		
		
		// saves the last column calculated
		int[] lastCol = new int[down - up + 1];
		// saves the current column being calculated
		int[] curCol = new int[lastCol.length];
		// for the first column, just use INDEL_PENALTY (only vertical moves possible)
		for (int row = 1; row < curCol.length; row++) curCol[row] = row * INDEL_PENALTY;

		// initialize backtrack
		backtrack = new byte[lastCol.length - 1];
		// loop over all columns until the middle one
		for (int col = 1; col <= midCol; col++) {
			// save the last column calculated
			lastCol = curCol.clone();
			
			// the first row of curCol has vertical moves possible
			curCol[0] = lastCol[0] + INDEL_PENALTY;
			// loop over all rows except the first
			for (int row = 1; row < lastCol.length; row++) {
				// initialize this node, and its backtrack, as if a diagonal move occurred
				if (fromSource) curCol[row] = lastCol[row - 1] +
							scoring.get(side.charAt(row + up - 1)).get(top.charAt(left + col - 1));
				// the chars have to be calculated differently if toSink (vs fromSource)
				else curCol[row] = lastCol[row - 1] +
						scoring.get(side.charAt(down - row)).get(top.charAt(right - col));
				backtrack[row - 1] = 0;
				
				// check if a horizontal move would be better, set that if so
				if (lastCol[row] + INDEL_PENALTY > curCol[row]) {
					curCol[row] = lastCol[row] + INDEL_PENALTY;
					backtrack[row - 1] = 1;
				}
				
				// same for a vertical move
				if (curCol[row - 1] + INDEL_PENALTY > curCol[row]) {
					curCol[row] = curCol[row - 1] + INDEL_PENALTY;
					backtrack[row - 1] = 2;
				}
			}
		}
		
		// the last column calculated will have been the middle column
		return curCol;
	}
	
	/**
	 * <h1>Finds a middle path through an alignment matrix</h1>
	 * Gets the middle columns from the source and to the sink, finds the max nodes between them,
	 * then calculates the points on the path using the backtrack array.
	 * @param up the upper bound of the overall alignment graph to consider, noting that coordinates start at 0
	 * @param down the lower bound ( > up)
	 * @param left the left bound
	 * @param right the right bound ( > left)
	 * @param scoring a scoring matrix for diagonal moves
	 * @return two points that make up the optimal middle path, in the format of a length-4 <code>int[]</code> array,
	 * with {x1, y1, x2, y2}
	 */
	private static int[] findMiddleEdge(int up, int down, int left, int right,
			HashMap<Character, HashMap<Character, Integer>> scoring) {
		// calculate the # of the middle column
		int midCol = (left + right) / 2;
		
		// get middle columns from the source and from the sink
		int[] fromSource = toMid(true, up, down, left, right, scoring);
		int[] toSink = toMid(false, up, down, left, right, scoring);

		// the row in the midCol which maximizes fromSource & toSink
		int maxRow = 0;
		// the value of the node at (midCol, row)
		int maxNode = fromSource[0] + toSink[toSink.length - 1];
		// loop over the rest of the values in the midCols
		for (int i = 1; i < fromSource.length; i++) {
			// if the fromSource & toSink is the best so far, save it as so
			if (fromSource[i] + toSink[toSink.length - i - 1] > maxNode) {
				maxRow = i;
				maxNode = fromSource[i] + toSink[toSink.length - i - 1];
			}
		}
		// a maximum score will be set the first time
		if (score == null) score = maxNode;
		// shift the maxRow accounting for the offset down from up 
		maxRow += up;
		
		// if the row is the bottom one or a horizontal backtrack is noted
		if (maxRow == down || backtrack[backtrack.length - maxRow - 1 + up] == 1)
			return new int[] {midCol, maxRow, midCol + 1, maxRow};
	
		// if a diagonal backtrack is noted
		else if (backtrack[backtrack.length - maxRow - 1 + up] == 0) 
			return new int[] {midCol, maxRow, midCol + 1, maxRow + 1};
		
		// otherwise, assume vertical backtrack
		else return new int[] {midCol, maxRow, midCol, maxRow + 1};
	}

	/**
	 * <h1>Recursive, low memory alignment</h1>
	 * Calculates the middle edge of the given bounds of the alignment graph, then calls itself for the parts
	 * before and after said edge. Using the edge and those alignments, figures out the overall alignment.
	 * Trivial, recursion-ending cases: only vertical or horizontal edges left
	 * @param up the upper bound of the overall alignment graph to consider, noting that coordinates start at 0
	 * @param down the lower bound ( > up)
	 * @param left the left bound
	 * @param right the right bound ( > left)
	 * @param scoring a scoring matrix
	 * @return an optimal alignment of two strings
	 */
	private static String[] align(int up, int down, int left, int right,
			HashMap<Character, HashMap<Character, Integer>> scoring) {
		// if the part left is a vertical line, return Strings reflecting that alignment
		if (left == right) 
			return new String[] {side.substring(up, down), DASHES.substring(0, down - up)};
		// same if the part left is a horizontal line
		else if (up == down) 
			return new String[] {DASHES.substring(0, right - left), top.substring(left, right)};
		// for regular cases in the recursion
		else {
			// find the middle edge of the current bounds (split graph in half)
			int[] midEdge = findMiddleEdge(up, down, left, right, scoring);
			// calculate the alignment of the part before the middle edge
			String[] fromSource = align(up, midEdge[1], left, (left + right) / 2, scoring);
			// calculate the alignment of the part after the middle edge
			String[] toSink = align(midEdge[3], down, midEdge[2], right, scoring);
			
			// if the middle edge was vertical, add that to fromSource
			if (midEdge[0] == midEdge[2]) {
				fromSource[0] += side.charAt(midEdge[1]);
				fromSource[1] += "-";
			}
			// same for if it was horizontal
			else if (midEdge[1] == midEdge[3]) {
				fromSource[0] += "-";
				fromSource[1] += top.charAt(midEdge[0]);
			}
			// same for being diagonal
			else {
				fromSource[0] += side.charAt(midEdge[1]);
				fromSource[1] += top.charAt(midEdge[0]);
			}
			
			// combine the alignments of before the middle edge, the middle edge, and after the middle edge
			return new String[] {fromSource[0] + toSink[0], fromSource[1] + toSink[1]};
		}
	}
	
	/**
	 * <h1>Public alignment call</h1>
	 * Sets the strings to align, then starts the recursion with proper parameters
	 * @param side the first string to align
	 * @param top the second string to align
	 * @param scoring a scoring matrix
	 * @return an optimal alignment of the two strings
	 */
	public static String[] align(String side, String top, HashMap<Character, HashMap<Character, Integer>> scoring) {
		// setting strings
		LowMemoryAlignment.side = side;
		LowMemoryAlignment.top = top;
		// calling private method
		return align(0, side.length(), 0, top.length(), scoring);
	}
}