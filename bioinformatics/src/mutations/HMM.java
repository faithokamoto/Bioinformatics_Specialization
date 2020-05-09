package mutations;

/**
 * A Hidden Markov Model, which can calculate probabilities and most-likely-hidden-states
 * given the proper data
 * @author faith
 * @param <S> the label for the HMM's states, such as Character or LabeledChar. Must implement Comparable.
 */
public class HMM<S extends Comparable<S>> {
	/**
	 * the symbols that can be emitted, sorted
	 */
	private final char[] alph;
	/**
	 * the states that the HMM can be in, sorted
	 */
	private final S[] states;
	/**
	 * a table of transition probabilities, with [i][j] being
	 * the probability of transitions from states[i] to states[j]
	 */
	private final double[][] trans;
	/**
	 * a table of emission probabilities, with [i][j] being
	 * the probability of emitting alph[j] from states[i]
	 */
	private final double[][] emiss;
	
	/**
	 * Initializes all instance variables
	 * @param alph the emission alphabet
	 * @param states the possible states
	 * @param trans a transition probability table
	 * @param emiss an emission probability table
	 */
	public HMM(char[] alph, S[] states, double[][] trans, double[][] emiss) {
		this.alph = alph;
		this.states = states;
		this.trans = trans;
		this.emiss = emiss;
	}
	
	/**
	 * Basic binary search for the states array
	 * @param arr the array to search through
	 * @param look the LabeledChar to look for
	 * @return the index of the LabeledChar, or -1 if no found
	 */
	public final int indexOf(S[] arr, S look) {
		// include all indices in the search
		int left = 0;
		int right = arr.length - 1;
		
		// while some indices need to be searched
		while (left <= right) {
			// calculate the middle index
			int mid = (left + right) / 2;
			
			// if this index has the char, return it
			if (arr[mid].equals(look)) return mid;
			// or if this index comes before the char, discount all to left
			else if (arr[mid].compareTo(look) < 0) left = mid + 1;
			// same for discounting all to right
			else right = mid - 1;
		}
		
		// if not found, return -1
		return -1;
	}
	
	// Getters for the arrays
	
	/**
	 * Getter for transition probabilities
	 * @param from the index of the state to transition from
	 * @param to the index of the state to transition to
	 * @return the likelihood of transitioning between the given states
	 */
	public final double transProb(int from, int to) {
		return trans[from][to];
	}
	
	/**
	 * Getter for transition probabilities
	 * @param from the state to transition from
	 * @param to the state to transition to
	 * @return the the likelihood of transitioning between the given states
	 */
	public final double transProb(S from, S to) {
		return trans[indexOf(states, from)][indexOf(states, to)];
	}
	
	/**
	 * Getter for emission probabilities
	 * @param state the index of the state emitted from
	 * @param emit the char emitted
	 * @return the likelihood of emitting emit from the given state
	 */
	public final double emissProb(int state, char emit) {
		return emiss[state][HMMBuilder.indexOf(alph, emit)];
	}
	
	/**
	 * Getter for emission probabilities
	 * @param state the state emitted from
	 * @param emit the char emitted
	 * @return the likelihood of emitting emit from the given state
	 */
	public final double emissProb(S state, char emit) {
		return emiss[indexOf(states, state)][HMMBuilder.indexOf(alph, emit)];
	}
	
	/**
	 * Getter for state values
	 * @param index the index of the state to get
	 * @return the value of states[index]
	 */
	public final S getState(int index) {
		return states[index];
	}
	
	/**
	 * Calculates the probability of a certain hidden path
	 * @param path a hidden path
	 * @return the probability of the hidden path
	 */
	public double pathProb(S[] path) {
		// there is an equal probability to start in any state
		double prob = 1 / states.length;
		// loop over all state-pairs
		for (int i = 0; i < path.length - 1; ++i)
			// multiply prob by this transition probability
			prob *= transProb(path[i], path[i + 1]);
		
		return prob;
	}
	
	/**
	 * Calculates the likelihood of a certain outcome given a hidden path
	 * @param outcome an outcome
	 * @param path a hidden path
	 * @return the likelihood of this outcome assuming this hidden path
	 */
	public double outcomeGivenPathProb(char[] outcome, S[] path) {
		// initialize probability to 1
		double prob = 1;
		// loop over all emitted chars
		for (int i = 0; i < outcome.length; ++i)
			// multiple probability by the emission probability of this char
			prob *= emissProb(indexOf(states, path[i]), outcome[i]);
		
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
	
	public double edgeProb(int from, int to, char emit) {
		return trans[from][to] * emissProb(to, emit);
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
		for (int row = 0; row < states.length; ++row)
			// initialize probability assuming equal chance for all initial states
			probs[row][0] = Math.log((1.0 / states.length) * emissProb(row, outcome[0]));
		
		// loop over all other columns, and all nodes in that column
		for (int col = 1; col < outcome.length; ++col) for (int row = 0; row < states.length; ++row) {
			// set this node to a very small number
			probs[row][col] = -Double.MAX_VALUE;
			
			// loop over all nodes in the preceding column
			for (int back = 0; back < states.length; ++back) {
				// calculate probability if path from this node is used
				double tryProb = probs[back][col - 1] + Math.log(edgeProb(back, row, outcome[col]));
				// if it beats the current probability
				if (tryProb > probs[row][col]) {
					// set it as the probability, and the node's row as a backtrack
					probs[row][col] = tryProb;
					backtrack[row][col - 1] = back;
				}
			}
		}
		
		// assume that the maximum node in the last column is in row 0
		int maxRow = 0;
		// loop over all other rows in the last column
		for (int row = 1; row < states.length; ++row)
			// if this node has a better probability, set its row as the maximum
			if (probs[row][outcome.length - 1] > probs[maxRow][outcome.length - 1]) maxRow = row;
		
		// return the backtracked path starting at the max node in the last column
		return backtrackPath(backtrack, maxRow);
	}
	
	/**
	 * Calculate the likelihood of an outcome
	 * @param outcome the outcome to calculate likelihood of
	 * @return the sum of all multiplicative paths through the Viterbi graph
	 */
	public double outcomeProb(char[] outcome) {
		// get all forward probabilities
		double[][] probs = getForwards(outcome);
		
		// initialize the probability of this outcome to 0
		double outcomeProb = 0;
		// loop over all nodes in the last column
		for (int i = 0; i < states.length; ++i)
			// add this node's probability to the overall probability
			outcomeProb += probs[i][outcome.length - 1];
		
		return outcomeProb;
	}
	
	/**
	 * Calculates conditional state probabilities in a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @return the completed conditional state probability matrix
	 */
	public double[][] calcCondStateProbs(char[] outcome) {
		return calcCondStateProbs(outcome, getForwards(outcome), getBackwards(outcome));
	}
	
	/**
	 * Calculates conditional state probabilities in a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @param forwards a pre-computed forward-node probability matrix
	 * @param backwards a pre-computed backward-node probability matrix
	 * @return the completed conditional state probability matrix
	 */
	public double[][] calcCondStateProbs(char[] outcome, double[][] forwards, double[][] backwards) {
		// initialize return variable
		double[][] conditional = new double[states.length][outcome.length];
		
		// initialize the probability of this outcome to 0
		double outcomeProb = 0;
		// loop over all nodes in the last column
		for (int i = 0; i < states.length; ++i)
			// add this node's probability to the overall probability
			outcomeProb += forwards[i][outcome.length - 1];
		
		// loop over all cells in the Viterbi graph
		for (int row = 0; row < states.length; ++row) for (int col = 0; col < outcome.length; ++col)
			// calculate conditional probability of using this state in this column
			conditional[row][col] = forwards[row][col] * backwards[row][col] / outcomeProb;
			
		
		return conditional;
	}
	
	/**
	 * Calculates conditional path probabilities in a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @return the completed conditional path probability matrix
	 */
	public double[][] calcCondPathProbs(char[] outcome) {
		return calcCondPathProbs(outcome, getForwards(outcome), getBackwards(outcome));
	}
	
	/**
	 * Calculates conditional path probabilities in a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @param forwards a pre-computed forward-node probability matrix
	 * @param backwards a pre-computed backward-node probability matrix
	 * @return the completed conditional path probability matrix
	 */
	public double[][] calcCondPathProbs(char[] outcome, double[][] forwards, double[][] backwards) {
		// initialize return variable
		double[][] conditional = new double[states.length * states.length][outcome.length - 1];
		
		// initialize the probability of this outcome to 0
		double outcomeProb = 0;
		// loop over all nodes in the last column
		for (int i = 0; i < states.length; ++i)
			// add this node's probability to the overall probability
			outcomeProb += forwards[i][outcome.length - 1];
		
		// loop over all columns of the graph to start from	
		for (int col = 0; col < outcome.length - 1; ++col)
			// loop over all states (row) to transition/go from and to
			for (int from = 0; from < states.length; ++from) for (int to = 0; to < states.length; ++to)
				// calculate conditional probability of path from (col, from) to (col + 1, to)
				conditional[from * states.length + to][col] = forwards[from][col] * backwards[to][col + 1] * 
					edgeProb(from, to, outcome[col + 1]) / outcomeProb;
		
		return conditional;
	}
	
	/**
	 * Calculates a forwards probability matrix for a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @return the completed forwards-node probability matrix
	 */
	public double[][] getForwards(char[] outcome) {
		// initialize return variable
		double[][] forwards = new double[states.length][outcome.length];
		
		// loop over all rows in the first column
		for (int i = 0; i < states.length; ++i)
			// initialize probability assuming equal chance for all initial states
			forwards[i][0] = (1.0 / states.length) * emissProb(i, outcome[0]);
		// loop over all other columns
		for (int col = 1; col < outcome.length; ++col)
			// loop over all nodes in that column
			for (int row = 0; row < states.length; ++row)
				// loop over all nodes in the last column
				for (int back = 0; back < states.length; ++back)
					// add the probability of using this path in to this node's probability
					forwards[row][col] += forwards[back][col - 1] * edgeProb(back, row, outcome[col]);

		return forwards;
	}
	
	/**
	 * Calculates a backwards probability matrix for a Viterbi graph
	 * @param outcome the outcome emitted by the Viterbi graph
	 * @return the completed backwards-node probability matrix
	 */
	public double[][] getBackwards(char[] outcome) {
		// initialize return variable
		double[][] backwards = new double[states.length][outcome.length];
		
		// loop over all rows in the first column, intializing probability
		for (int i = 0; i < states.length; ++i) backwards[i][outcome.length - 1] = 1;
		
		// loop over all other columns
		for (int col = outcome.length - 2; col >= 0; --col)
			// loop over all nodes in that column
			for (int row = 0; row < states.length; ++row)
				// loop over all nodes in the last column
				for (int back = 0; back < states.length; ++back)
					// add the probability of using this path in to this node's probability
					backwards[row][col] += backwards[back][col + 1] * edgeProb(row, back, outcome[col + 1]);
		
		return backwards;
	}
	
	public String toString() {
		// initialize return variable
		String ret = "";
		
		// first, write out the transition matrix
		
		// write out each state as a header
		for (S state : states) ret += "\t" + state.toString();
		// loop over all states
		for (int i = 0; i < trans.length; ++i) {
			// newline for the new row, write out the state as a side-label
			ret += "\n" + states[i].toString();
			// loop over all states to transition to
			for (int j = 0; j < trans[0].length; ++j) {
				// if this probability is nonzero, give 3 decimal place accuracy
				if (trans[i][j] != 0)
					ret += "\t" + String.format("%.3f", trans[i][j]);
				// or just write 0
				else ret += "\t0";
			}
		}
		
		// spacer
		ret += "\n--------\n";
		
		// now, write out the emission matrix
		
		// write out each alphabet character as a header
		for (char let : alph) ret += "\t" + let;
		// loop over all states
		for (int i = 0; i < emiss.length; ++i) {
			// newline for the new row, write out the state as a side-label
			ret += "\n" + states[i].toString();
			// loop over all alphabet characters to emit, write out probability
			for (int j = 0; j < emiss[0].length; ++j) {
				// if this probability is nonzero, give 3 decimal place accuracy
				if (emiss[i][j] != 0)
					ret += "\t" + String.format("%.3f", emiss[i][j]);
				// or just write 0
				else ret += "\t0";
			}
		}
		
		return ret;
	}
}