package mutations;

/**
 * An HMM which can align strings, even with insertions and deletions
 * @author faith
 */
public class AlignmentHMM extends HMM<LabeledChar> {
	/**
	 * the total number of states (including a start and end state)
	 */
	public final int numStates;
	
	/**
	 * Initializes all instance variables and calculates the number of states
	 * @param alph the alphabet of characters in the emitted strings
	 * @param states the states (S, I0, M1, D1, I1.... In, E) which "hidden paths" are created out of
	 * @param trans a transition matrix
	 * @param emiss an emission matrix	
	 */
	public AlignmentHMM(char[] alph, LabeledChar[] states, double[][] trans, double[][] emiss) {
		super(alph, states, trans, emiss);
		numStates = states.length;
	}
	
	/**
	 * Calculates the prob/backtrack of state at (row, col) given it is an I state
	 * @param probs the probability Viterbi graph
	 * @param backtrack the backtrack matrix
	 * @param let the char emitted in this column
	 * @param row this state's row in the alignment Viterbi graph
	 * @param col this state's column in the alignment Viterbi graph
	 */
	private final void calcIProb(double[][] probs, int[][] backtrack, char let, int row, int col) {
		// calculate the index of this state (1 more than row since S doesn't have a row)
		int curState = row + 1;
		
		// if this state is not in the second column
		if (col > 1) {
			// set initial probability to the "sideways" path to same state
			probs[row][col] = probs[row][col - 1] +
					Math.log(transProb(curState, curState) * emissProb(curState, let));
			// note that it currently backtracks to the same row
			backtrack[row][col - 1] = row;
		}
		// if this state has a special "sideways" path the the source node
		else if (row == 0 && col == 1) {
			// set initial probability to this "sideways" path
			probs[row][col] = probs[row][col - 1] +
					Math.log(transProb(0, curState) * emissProb(curState, let));
			// not that it currently backtracks to the same row
			backtrack[row][col - 1] = row;
		}
		// or if none of that, just set to an obscenely low value
		else probs[row][col] = -Double.MAX_VALUE;
		
		// if this state is not in the first row
		if (row > 0) {
			// calculate probability for path backtracking up one
			double tryProb = probs[row - 1][col - 1] +
					Math.log(transProb(curState - 1, curState) * emissProb(curState, let));
			// if it turns out to be better than the current path
			if (tryProb > probs[row][col]) {
				// set probability and backtrack to this path
				probs[row][col] = tryProb;
				backtrack[row][col - 1] = row - 1;
			}
			
			// if this state is also not in the second column (if an inner state)
			if (col > 1) {
				// calculate probability for path backtracking up two
				tryProb = probs[row - 2][col - 1] + 
						Math.log(transProb(curState - 2, curState) * emissProb(curState, let));
				// if it turns out to be better than the current path
				if (tryProb > probs[row][col]) {
					// set probability and backtrack to this path
					probs[row][col] = tryProb;
					backtrack[row][col - 1] = row - 2;
				}
			}
		}
	}
	
	/**
	 * Calculates the prob/backtrack of state at (row, col) given it is an M state
	 * @param probs the probability Vieterbi graph
	 * @param backtrack the backtrack matrix
	 * @param let the char emitted in this column
	 * @param row this state's row in the alignment Viterbi graph
	 * @param col this state's column in the alignment Viterbi graph
	 */
	private final void calcMProb(double[][] probs, int[][] backtrack, char let, int row, int col) {
		// calculate the index of this state (1 more than row since S doesn't have a row)
		int curState = row + 1;
		
		// if this state is not in the second column
		if (col > 1) {
			// set its probability to the path backtracking up one
			probs[row][col] = probs[row - 1][col - 1] +
					Math.log(transProb(curState - 1, curState) * emissProb(curState, let));
			// and not that it backtracks up one
			backtrack[row][col - 1] = row - 1;
		}
		// if this state has a special "sideways" path to the source node 
		else if (col == 1 && row == 1) {
			// set its probability to that path
			probs[row][col] = probs[row - 1][col - 1] +
					Math.log(transProb(0, curState) * emissProb(curState, let));
			backtrack[row][col - 1] = row - 1;
		}
		// or if none of that, just set prob to an obscenely low number
		else probs[row][col] = -Double.MAX_VALUE;
		
		// if this state is not in the second row
		if (row > 1) {
			// calculate probability for the path backtracking up two
			double tryProb = probs[row - 2][col - 1] + 
					Math.log(transProb(curState - 2, curState) * emissProb(curState, let));
			// if it turns out to be better than the current path
			if (tryProb > probs[row][col]) {
				// set probability and backtrack to this path
				probs[row][col] = tryProb;
				backtrack[row][col - 1] = row - 2;
			}
			
			// if this state is also not in the second column (if an inner state)
			if (col > 1) {
				// calculate probability for the path backtracking up three
				tryProb = probs[row - 3][col - 1] +
						Math.log(transProb(curState - 3, curState) * emissProb(curState, let));
				// if it turns out to be better than the current path
				if (tryProb > probs[row][col]) {
					// set probability and backtrack to this path
					probs[row][col] = tryProb;
					backtrack[row][col - 1] = row - 3;
				}
			}
		}
	}
	
	/**
	 * Calculates the prob/backtrack of state at (row, col) given it is a D state
	 * @param probs the probability Vieterbi graph
	 * @param backtrack the backtrack matrix
	 * @param row this state's row in the alignment Viterbi graph
	 * @param col this state's column in the alignment Viterbi graph
	 */
	private final void calcDProb(double[][] probs, int[][] backtrack, int row, int col) {
		// calculate the index of this state (1 more than row since S doesn't have a row)
		int curState = row + 1;
		
		// assume a path backtracking up two
		probs[row][col] = probs[row - 2][col] +
				Math.log(transProb(curState - 2, curState));
		// set backtrack to up two
		backtrack[row][col - 1] = row - 2;
		
		// if this state is an inner state
		if (row > 2) {
			// calculate the probability of the path backtracking up three
			double tryProb = probs[row - 3][col] + Math.log(transProb(curState - 3, curState));
			// if it turns out to be better than the current path
			if (tryProb > probs[row][col]) {
				// set probability and backtrack to this path
				probs[row][col] = tryProb;
				backtrack[row][col - 1] = row - 3;
			}
			// calculate the probability of the path backtracking up four
			tryProb = probs[row - 4][col] + Math.log(transProb(curState - 4, curState));
			// if it turns out to be better than the current path
				if (tryProb > probs[row][col]) {
					// set probability and backtrack to this path
				probs[row][col] = tryProb;
				backtrack[row][col - 1] = row - 4;
			}
		}
	}

	/**
	 * Backtracks to find the Viterbi path
	 * @param backtrack a backtrack matrix
	 * @param startRow the row to start at
	 * @return
	 */
	private String backtrackPath(int[][] backtrack, int startRow) {
		// initialize return variable
		String path = "";
		// save the current row
		int curRow = startRow;
		// the current column is the last one
		int col = backtrack[0].length - 1;
		
		// while more columns are left to traverse
		while (col >= 0) {
			// add this current row's state to the path
			path = getState(curRow + 1) + " " + path;
			// calculate row to backtrack to
			int newRow = backtrack[curRow][col];
			// if this state was not a D, shift column back one
			if (curRow % 3 != 2) --col;
			// set the new current row
			curRow = newRow;
		}
		
		// if there are more rows to backtrack (leading Ds)
		for (; curRow > 0; curRow -= 3)
			// add each D to the path
			path = "D" + (curRow / 3 + 1) + " " + path;
		
		return path;
	}
	
	public String viterbiPath(char[] outcome) {
		// create the probability matrix (ignore the S and E states' rows, extra D column)
		double[][] probs = new double[numStates - 2][outcome.length + 1];
		// create the backtrack matrix (ignore backtracks for the first column)
		int[][] backtrack = new int[numStates - 2][outcome.length];
		
		// probability for D1
		probs[2][0] = Math.log(transProb(0, 3));
		// loop over all Ds in this first D column
		for (int i = 5; i < probs.length; i += 3)
			// calculate probability for the single path up
			probs[i][0] = probs[i - 3][0] + Math.log(transProb(i - 3 + 1, i + 1));
		
		// loop over all columns after the first
		for (int col = 1; col < probs[0].length; ++col)
			// loop over all rows in this column
			for (int row = 0; row < probs.length; ++row) {
				// determine the type of state at this node, then call proper calc-method
				if (row % 3 == 0) calcIProb(probs, backtrack, outcome[col - 1], row, col);
				else if (row % 3 == 1) calcMProb(probs, backtrack, outcome[col - 1], row, col);
				else calcDProb(probs, backtrack, row, col);
			}
		
		// assume that the start row is the 3rd-from-bottom
		int startRow = probs.length - 3;
		// loop over the two rows below it
		for (int row = probs.length - 2; row < probs.length; ++row)
			// if this row has a better probability, set it as the best row
			if (probs[row][probs[0].length - 1] > probs[startRow][probs[0].length - 1])
				startRow = row;
		
		// backtrack through the matrix
		return backtrackPath(backtrack, startRow);
	}
}