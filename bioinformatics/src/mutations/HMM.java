package mutations;

/**
 * A Hidden Markov Model, which can calculate probabilities and most-likely-hidden-states
 * given the proper data
 * @author faith
 */
public class HMM {
	/**
	 * the symbols that can be emitted, sorted
	 */
	private char[] alph;
	/**
	 * the states that the HMM can be in, sorted
	 */
	private LabeledChar[] states;
	/**
	 * a table of transition probabilities, with [i][j] being
	 * the probability of transitions from states[i] to states[j]
	 */
	private double[][] trans;
	/**
	 * a table of emission probabilities, with [i][j] being
	 * the probability of emitting alph[j] from states[i]
	 */
	private double[][] emiss;
	
	/**
	 * Initializes all instance variables
	 * @param alph the emission alphabet
	 * @param states the possible states
	 * @param trans a transition probability table
	 * @param emiss an emission probability table
	 */
	public HMM(char[] alph, LabeledChar[] states, double[][] trans, double[][] emiss) {
		this.alph = alph;
		this.states = states;
		this.trans = trans;
		this.emiss = emiss;
	}
	
	/**
	 * Basic binary search for a char array
	 * @param arr the array to search through
	 * @param look the char to look for
	 * @return the index of the char in the array, or -1 if not found
	 */
	public static int indexOf(char[] arr, char look) {
		// include all indices in the search
		int left = 0;
		int right = arr.length - 1;
		
		// while some indices need to be searched
		while (left <= right) {
			// calculate the middle index
			int mid = (left + right) / 2;
			
			// if this index has the char, return it
			if (arr[mid] == look) return mid;
			// or if this index comes before the char, discount all to left
			else if (arr[mid] < look) left = mid + 1;
			// same for discounting all to right
			else right = mid - 1;
		}
		
		// if not found, return -1
		return -1;
	}
	
	/**
	 * Calculates the probability of a certain hidden path
	 * @param path a hidden path
	 * @return the probability of the hidden path
	 */
	public double pathProb(char[] path) {
		// there is an equal probability to start in any state
		double prob = 1 / states.length;
		// loop over all state-pairs
		for (int i = 0; i < path.length - 1; ++i)
			// multiply prob by this transition probability
			prob *= trans[path[i]][path[i + 1]];
		
		return prob;
	}
	
	/**
	 * Calculates the likelihood of a certain outcome given a hidden path
	 * @param outcome an outcome
	 * @param path a hidden path
	 * @return the likelihood of this outcome assuming this hidden path
	 */
	public double outcomeGivenPathProb(char[] outcome, LabeledChar[] path) {
		// initialize probability to 1
		double prob = 1;
		// loop over all emitted chars
		for (int i = 0; i < outcome.length; ++i)
			// multiple probability by the emission probability of this char
			prob *= emiss[LabeledChar.indexOf(states, path[i])][indexOf(alph, outcome[i])];
		
		return prob;
	}
	
	/**
	 * Backtracks through a backtrack matrix given a row to start at
	 * @param backtrack a backtrack matrix
	 * @param startRow the row to start at
	 * @return the hidden path given by the backtrack matrix
	 */
	private String backtrackPath(int[][] backtrack, int startRow) {
		// start the path with the state given by startRow
		String path = states[startRow].toString();
		// initialize curRow to the starting row
		int curRow = startRow;
		// loop over all columns backwards through the matrix
		for (int i = backtrack[0].length - 1; i >= 0; --i) {
			// update the current row by backtracking
			curRow = backtrack[curRow][i];
			// add this new state to the front of the string
			path = states[curRow].toString() + path;
		}
		
		return path;
	}
	
	/**
	 * Finds the Viterbi path given an outcome
	 * @param outcome the outcome to find a path for
	 * @return the optimal hidden path for this outcome
	 */
	public String viterbiPath(char[] outcome) {
		// initialize the probability matrix (1 cell for each node)
		double[][] probs = new double[states.length][outcome.length];
		// initialize the backtrack matrix (1 less column because the first column doesn't have a backtrack
		int[][] backtrack = new int[states.length][outcome.length - 1];
		
		// loop over all rows in the first column
		for (int i = 0; i < states.length; ++i)
			// initialize probability assuming equal chance for all initial states
			probs[i][0] = Math.log((1.0 / states.length) * emiss[i][indexOf(alph, outcome[0])]);
		
		// loop over all other columns, and all nodes in that column
		for (int i = 1; i < outcome.length; ++i) for (int j = 0; j < states.length; ++j) {
			// set this node to a very small number
			probs[j][i] = (double) Integer.MIN_VALUE;
			
			// loop over all nodes in the preceding column
			for (int k = 0; k < states.length; ++k) {
				// calculate probability if path from this node is used
				double tryProb = probs[k][i - 1] + Math.log(trans[k][j] * emiss[j][indexOf(alph, outcome[i])]);
				// if it beats the current probability
				if (tryProb > probs[j][i]) {
					// set it as the probability, and the node's row as a backtrack
					probs[j][i] = tryProb;
					backtrack[j][i - 1] = k;
				}
			}
		}
		
		// assume that the maximum node in the last column is in row 0
		int maxRow = 0;
		// loop over all other rows in the last column
		for (int i = 1; i < states.length; ++i)
			// if this node has a better probability, set its row as the maximum
			if (probs[i][outcome.length - 1] > probs[maxRow][outcome.length - 1]) maxRow = i;
		
		// return the backtracked path starting at the max node in the last column
		return backtrackPath(backtrack, maxRow);
	}
	
	/**
	 * Calculate the likelihood of an outcome
	 * @param outcome the outcome to calculate likelihood of
	 * @return the sum of all multiplicative paths through the Viterbi graph
	 */
	public double outcomeProb(char[] outcome) {
		// initialize a probability matrix (1 cell for each node 
		double[][] probs = new double[states.length][outcome.length];
		
		
		// loop over all rows in the first column
		for (int i = 0; i < states.length; ++i)
			// initialize probability assuming equal chance for all initial states
			probs[i][0] = Math.log((1.0 / states.length) * emiss[i][indexOf(alph, outcome[0])]);
		
		// loop over all other columns
		for (int i = 1; i < outcome.length; ++i)
			// loop over all nodes in that column
			for (int j = 0; j < states.length; ++j)
				// loop over all nodes in the last column
				for (int k = 0; k < states.length; ++k)
					// add the probability of using this path in to this node's probability
					probs[j][i] += probs[k][i - 1] * trans[k][j] *
							emiss[j][indexOf(alph, outcome[i])];
		
		// initialize the probability of this outcome to 0
		double outcomeProb = 0;
		// loop over all nodes in the last column
		for (int i = 0; i < states.length; ++i)
			// add this node's probability to the overall probability
			outcomeProb += probs[i][outcome.length - 1];
		
		return outcomeProb;
	}
	
	public String toString() {
		// initialize return variable
		String ret = "";
		
		// first, write out the transition matrix
		
		// write out each state as a header
		for (LabeledChar state : states) ret += "\t" + state.toString();
		// loop over all states
		for (int i = 0; i < states.length; ++i) {
			// newline for the new row, write out the state as a side-label
			ret += "\n" + states[i].toString();
			// loop over all states to transition to, write out probability
			for (int j = 0; j < states.length; ++j) ret += "\t" + trans[i][j];
		}
		
		// spacer
		ret += "\n--------\n";
		
		// now, write out the emission matrix
		
		// write out each alphabet character as a header
		for (char let : alph) ret += "\t" + let;
		// loop over all states
		for (int i = 0; i < states.length; ++i) {
			// newline for the new row, write out the state as a side-label
			ret += "\n" + states[i].toString();
			// loop over all alphabet characters to emit, write out probability
			for (int j = 0; j < alph.length; ++j) ret += "\t" + emiss[i][j];
		}
		
		return ret;
	}
}