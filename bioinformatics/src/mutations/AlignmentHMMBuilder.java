package mutations;

/**
 * A fully-static class for building string-alignment HMMs
 * @author faith
 */
public final class AlignmentHMMBuilder {
	/**
	 * the space character
	 */
	public static final char SPACE = '-';
	
	/**
	 * This is a static class; it should not be instantiated
	 */
	private AlignmentHMMBuilder() {}
	
	/**
	 * Builds an HMM modeling the alignment given
	 * @param threshold a column-removal threshold (any columns with a higher percentage of spaces are removed)
	 * @param alph the alphabet which the aligned strings are constructed from
	 * @param alignment the alignment to build an HMM from (with space symbols making all strings the same length)
	 * @param pseudo a pseudo count to add to all trans/emiss probabilities
	 * @return the completed HMM
	 */
	public static AlignmentHMM build(double threshold, char[] alph, char[][] alignment, double pseudo) {
		// set a special ordering of chars
		LabeledChar.specialOrder = new char[] {'M', 'D', 'I'};
		// get which columns need to be removed
		int[] removedCols = removedCols(threshold, alignment);
		// calculate the number of states which will be needed
		int numStates = (alignment[0].length - removedCols.length + 1) * 3;
		// classify each spot in alignment with a state
		Integer[][] classify = classifyAlignment(alignment, removedCols);
		// calculate transition and emission probabilities off the classification
		double[][] trans = calcTransProbs(classify, numStates, pseudo);
		double[][] emiss = calcEmissProbs(alignment, alph, classify, numStates, pseudo);
		// build & return the HMM
		return new AlignmentHMM(alph, getStates(numStates), trans, emiss);
	}

	/**
	 * Calculates the index of a given state
	 * @param state the state to calculate index for
	 * @return the index of that state in the sorted states array
	 */
	public static final int stateToIndex(LabeledChar state) {
		// no way to know index for the end state without knowing the total number of states
		if (state.getLabel() == LabeledChar.END)
			throw new IllegalArgumentException("No way to know for the end state");
		// the start state is the first index
		else if (state.getLabel() == LabeledChar.START) return 0;
		
		// the letter of the state shows its offset from 3 * label
		else if (state.getLetter() == 'I') return 3 * state.getLabel() + 1;
		else if (state.getLetter() == 'M') return 3 * state.getLabel() - 1;
		else if (state.getLetter() == 'D') return 3 * state.getLabel();
		
		// if none of the above came through, this state is not a recognized one
		throw new IllegalArgumentException("Invalid state");
	}
	
	/**
	 * Determine whether a sorted int[] contains a certain value
	 * @param arr a sorted int[] array
	 * @param look the value to look for
	 * @return whether this value is in the array
	 */
	public static boolean contains(int[] arr, int look) {
		// set bounds to the edges of the array
		int left = 0;
		int right = arr.length - 1;
		
		// while there are more values to consider
		while (left <= right) {
			// calculate the middle index
			int mid = (left + right) / 2;
			// if this index has the value, it is here
			if (arr[mid] == look) return true;
			// if this index comes before the value, discount it and all before
			else if (arr[mid] < look) left = mid + 1;
			// if this index comes after the value, discount it and all after
			else right = mid - 1;
		}
		
		// if not found, return false
		return false;
	}

	/**
	 * Determines the indexes of columns to remove from an alignment
	 * @param threshold the minimum percentage of spaces in a column to warrant removal
	 * @param alignment the alignment to remove columns from
	 * @return the indexes of columns to remove, sorted
	 */
	public static int[] removedCols(double threshold, char[][] alignment) {
		// tracks the number of columns which must be removed
		int cols = 0;
		// tracks whether each column should be removed
		boolean[] remove = new boolean[alignment[0].length];
		
		// loop over all columns
		for (int i = 0; i < alignment[0].length; ++i) {
			// initialize number of deletions (spaces) in this column to 0
			int dels = 0;
			// loop over all rows in this column
			for (int j = 0; j < alignment.length; ++j)
				// if this spot in alignment is a space, increment dels
				if (alignment[j][i] == '-') ++dels;
			
			// if the deletions exceed/meet the threshold
			if ((double) dels / alignment.length >= threshold) {
				// not that
				remove[i] = true;
				++cols;
			}
		}
		
		// initialize the columns removed to the proper length
		int[] removedCols = new int[cols];
		// the index of removedCols to add to starts at 0
		int index = 0;
		// loop over remove, and if this column is to be removed add it into removedCols
		for (int i = 0; i < remove.length; ++i) if (remove[i]) removedCols[index++] = i;
		
		return removedCols;
	}
	
	/**
	 * Classifies all chars in an alignment by their state's index
	 * @param alignment the alignment to classify
	 * @param removedCols the columns which are removed
	 * @return a completed classification matrix corresponding to the alignment
	 */
	private static Integer[][] classifyAlignment(char[][] alignment, int[] removedCols) {
		// initialize return variable
		Integer[][] classify = new Integer[alignment.length][alignment[0].length];
		// for each row, will track the number of removed columns before this index
		int[] before = new int[alignment[0].length];
		
		// loop over all columns
		for (int col = 0; col < alignment[0].length; ++col) {
			// if this is the first column, no columns before are removed
			if (col == 0) before[col] = 0;
			// if the column before was removed, set before to one more than previous
			else if (contains(removedCols, col - 1)) before[col] = before[col - 1] + 1;
			// otherwise just set to previous
			else before[col] = before[col - 1];
			
			// loop over all rows of the alignment
			for (int row = 0; row < alignment.length; ++row) {
				// if this column is removed
				if (contains(removedCols, col)) {
					// classify spaces as null states
					if (alignment[row][col] == SPACE) classify[row][col] = null;
					// classify non-spaces as insertions
					else classify[row][col] = 
							stateToIndex(new LabeledChar('I', col - before[col]));
				}
				// if this column is not removed
				else {
					// classify spaces as deletions
					if (alignment[row][col] == SPACE) classify[row][col] = 
							stateToIndex(new LabeledChar('D', col - before[col] + 1));
					// classify non-spaces as matches
					else classify[row][col] = 
							stateToIndex(new LabeledChar('M', col - before[col] + 1));
				}
			}
		}
		
		return classify;
	}
	
	/**
	 * Calculates transition probabilities
	 * @param classify a classification matrix
	 * @param numStates the total number of possible states
	 * @param pseudo a pseduocount to add to all probabilities
	 * @return a completed transition matrix
	 */
	public static double[][] calcTransProbs(Integer[][] classify, int numStates, double pseudo) {
		// initialize return variable
		double[][] trans = new double[numStates][numStates];
		// initialize row-counter (total value of that row)
		int[] rowCount = new int[numStates];
		
		// loop over all rows of the classification matrix (one row = one string)
		for (Integer[] row : classify) {
			// index of the state transitioning from
			int from = 0;
			// while the from-column has no state, move from-column forward
			while (row[from] == null) ++from;
			// increment transition from S to first state
			++trans[0][row[from]];
			// increment transitions from S
			++rowCount[0];
			
			// while from is small enough
			while (from < row.length - 1) {
				// assume that the to-state will be the next
				int to = from + 1;
				// while the to-column has no state and is valid, move to-column forward
				while (to < row.length && row[to] == null) ++to;
				// if the to-column is invalid don't worry about transition
				if (to == row.length) break;
				// increment transition from from to to
				++trans[row[from]][row[to]];
				// increment transitions from from
				++rowCount[row[from]];
				// move from-state to to-state
				from = to;
			}
			
			// increment transition from last state to E
			++trans[row[from]][numStates - 1];
			// increment transitions from last state
			++rowCount[row[from]];
		}
		
		// loop over all rows of the transition matrix
		for (int row = 0; row < trans.length; ++row) {
			// calculate the column where possible transitions start
			int startCol = ((row + 1) / 3) * 3 + 1;
			// loop over all possible transition areas
			for (int col = startCol; col < startCol + 3 && col < trans.length; ++col) 
				// if this spot is nonzero, divide by the total count for this row
				if (trans[row][col] != 0)
					trans[row][col] /= rowCount[row];
		}
		
		// if pseudocounts are in use, loop over all rows of the transition matrix again
		if (pseudo != 0) for (int row = 0; row < trans.length; ++row) {
			// calculate the column where possible transitions start
			int startCol = ((row + 1) / 3) * 3 + 1;
			// loop over all possible transition areas
			for (int col = startCol; col < startCol + 3 && col < trans.length; ++col) {
				// assume that the total for the row is 3 * pseudo (the added pseudocounts)
				double divide = 3 * pseudo;
				// if this row had some transitions recorded, add in 1 for already-there probability
				if (rowCount[row] != 0) ++divide;
				// if this row has only 2 transitions, remove one assumed pseudo
				if (startCol + 3 < trans.length) divide -= pseudo;
				// adjust probability for pseudocounts
				trans[row][col] = (trans[row][col] + pseudo) / divide;
			}
		}
		
		return trans;
	}
	
	/**
	 * Calculates emission probabilities given
	 * @param alignment an alignment
	 * @param acids the alphabet the alignment is constructed from
	 * @param classify a classification matrix
	 * @param numStates the total number of possible states
	 * @param pseudo a pseduocount to add to all probabilities
	 * @return a completed emission matrix
	 */
	public static double[][] calcEmissProbs(char[][] alignment, char[] alph,
			Integer[][] classify, int numStates, double pseudo) {
		// initialize return variable
		double[][] emiss = new double[numStates][alph.length];
		// initialize row-counter (total count for this row
		double[] rowCount = new double[numStates];
		
		// loop over all rows of the alignment
		for (int row = 0; row < alignment.length; ++row)
			// loop over all columns in this row
			for (int col = 0; col < alignment[0].length; ++col)
				// if this spot is valid and emits a char
				if (classify[row][col] != null && alignment[row][col] != SPACE) {
					// increment the emission for this state and this char
					++emiss[classify[row][col]][HMMBuilder.indexOf(alph, alignment[row][col])];
					// increment the emissions for this state
					++rowCount[classify[row][col]];
				}

		// loop over all rows of the emission matrix
		for (int i = 0; i < emiss.length; ++i)
			// loop over all cells in this row
			for (int j = 0; j < emiss[0].length; ++j)
				// if this cell is nonzero
				if (emiss[i][j] != 0)
					// divide by total count of this row to normalize
					emiss[i][j] /= rowCount[i];


		// if pseudocounts are used, loop over all rows except start and end
		if (pseudo != 0) for (int row = 1; row < emiss.length - 1; ++row) {
			// if this row is not a D (it emits a char)
			if (row % 3 != 0) {
				// calculate the factor to divide by
				double divide = alph.length * pseudo;
				if (rowCount[row] > 0) ++divide;
				// loop over all cells of this row
				for (int col = 0; col < emiss[0].length; ++col)
					// update probability for pseudocounts
					emiss[row][col] = (emiss[row][col] + pseudo) / divide;
			}
		}
	
		return emiss;
	}
	
	/**
	 * Generates all states needed for an alignment HMM
	 * @param num the number of states needed
	 * @return the completed, sorted list of states
	 */
	public static LabeledChar[] getStates(int num) {
		// initialize return variable
		LabeledChar[] states = new LabeledChar[num];
		// set the start-state
		states[0] = new LabeledChar('S', LabeledChar.START);
		// set the I0 state
		states[1] = new LabeledChar('I', 0);
		// loop over all columns in the alignment (essentially)
		for (int i = 3; i < num - 1; i += 3) {
			// set M, I, and D for this label
			states[i - 1] = new LabeledChar('M', i / 3);
			states[i] = new LabeledChar('D', i / 3);
			states[i + 1] = new LabeledChar('I', i / 3);
		}
		// set the end-state
		states[states.length - 1] = new LabeledChar('E', LabeledChar.END);
		
		return states;
	}
}