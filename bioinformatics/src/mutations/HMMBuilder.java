package mutations;

/**
 * Fully-static class meant for guessing HMMs when parameters are not fully known
 * @author faith
 */
public final class HMMBuilder {
	/**
	 * Builds a most-likely HMM given an emitted outcome and a hidden path
	 * @param alph the alphabet of emitted symbols
	 * @param states the states which are used
	 * @param outcome an emitted outcome
	 * @param path the known hidden path which corresponds to the given outcome
	 * @return an HMM with optimal transition and emission matrices
	 */
	public static HMM<Character> buildViterbi(char[] alph, Character[] states, char[] outcome, char[] path) {
		// calculate new transition & emission matrices
		double[][] trans = guessTransProbsViterbi(states, path);
		double[][] emiss = guessEmissProbsViterbi(alph, states, outcome, path);
		
		// build, and return, an HMM
		return new HMM<Character>(alph, states, trans, emiss);
	}
	
	/**
	 * Viterbi-guess a most-likely HMM given an emitted outcome
	 * @param alph the alphabet of emitted symbols
	 * @param states the states which are used
	 * @param outcome an emitted outcome
	 * @param transGuess an initial guess for a transition matrix
	 * @param emissGuess an initial guess for an emission matrix
	 * @param iterations the number of iterations to run for
	 * @return a HMM with the best guess of transition and emission matrices
	 */
	public static HMM<Character> buildViterbi(char[] alph, Character[] states, char[] outcome,
			double[][] transGuess, double[][] emissGuess, int iterations) {
		// build an HMM using the guessed matrices
		HMM<Character> guess = new HMM<Character>(alph, states, transGuess, emissGuess);
		// if there are no more iterations, return the guess
		if (iterations == 0) return guess;
		
		// if there are more iterations to go
		else {
			// get a most-likely path from the guessed HMM
			char[] path = guess.viterbiPath(outcome).toCharArray();
			// re-guess transition and emission matrices
			transGuess = guessTransProbsViterbi(states, path);
			emissGuess = guessEmissProbsViterbi(alph, states, outcome, path);
			// call self with new transition and emission matrices, with one less iteration to go
			return buildViterbi(alph, states, outcome, transGuess, emissGuess, iterations - 1);
		}
	}
	
	/**
	 * Barum-Welch-guess a most-likely HMM given an emitted outcome
	 * @param alph the alphabet of emitted symbols
	 * @param states the states which are used
	 * @param outcome an emitted outcome
	 * @param transGuess an initial guess for a transition matrix
	 * @param emissGuess an initial guess for an emission matrix
	 * @param iterations the number of iterations to run for
	 * @return a HMM with the best guess of transition and emission matrices
	 */
	public static HMM<Character> buildBW(char[] alph, Character[] states, char[] outcome,
			double[][] transGuess, double[][] emissGuess, int iterations) {
		// build an HMM using the guessed matrices
		HMM<Character> guess = new HMM<Character>(alph, states, transGuess, emissGuess);
		// if there are no more iterations, return the guess
		if (iterations == 0) return guess;
		
		// if there are more iterations to go
		else {
			// compute forwards and backwards arrays
			double[][] forwards = guess.getForwards(outcome);
			double[][] backwards = guess.getBackwards(outcome);
			// compute conditional state & path probabilities
			double[][] stateProbs = guess.calcCondStateProbs(outcome, forwards, backwards);
			double[][] pathProbs = guess.calcCondPathProbs(outcome, forwards, backwards);
			// re-guess transition and emission matrices
			emissGuess = guessEmissProbsBW(stateProbs, outcome, alph);
			transGuess = guessTransProbsBW(pathProbs, states.length);
			
			// call self with new trans & emiss guesses and one less iteration to go
			return buildBW(alph, states, outcome, transGuess, emissGuess, iterations - 1);
		}
	}
	
	/**
	 * Basic binary search for a Character array
	 * @param arr the array to search through
	 * @param look the char to look for
	 * @return the index of the char in the array, or -1 if not found
	 */
	public static int indexOf(Character[] arr, char look) {
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
	 * Guesses the values of a transition matrix given a hidden path
	 * @param states all possible, sorted hidden states
	 * @param path a hidden path
	 * @return the guessed transition matrix
	 */
	public static double[][] guessTransProbsViterbi(Character[] states, char[] path) {
		// initialize return variable
		double[][] trans = new double[states.length][states.length];
		// initialize row-counter (will hold the total count of a row)
		double[] rowCount = new double[states.length];
		
		// loop over all adjacent states in path
		for (int i = 0; i < path.length - 1; ++i) {
			// grab the index of the first state
			int firstIndex = indexOf(states, path[i]);
			// increment this state's row-counter
			++rowCount[firstIndex];
			// increment the transition from this state to the next one
			++trans[firstIndex][indexOf(states, path[i + 1])];
		}
		
		// loop over all rows of trans
		for (int i = 0; i < trans.length; ++i)
			// loop over all values in this row
			for (int j = 0; j < trans[0].length; ++j) {
				// if this row has some transitions then 
				if (rowCount[i] > 0) 
					// probability is value / rowCount
					trans[i][j] /= rowCount[i];
				// otherwise, each value is the same
				else trans[i][j] = 1.0 / states.length;
			}
		
		return trans;
	}
	
	/**
	 * Guesses the values of an emission matrix given a hidden path
	 * @param alph the sorted alphabet of emitted chars
	 * @param states all possible, sorted hidden states
	 * @param outcome an emission produced by the HM
	 * @param path a hidden path
	 * @return the completed emission matrix
	 */
	public static double[][] guessEmissProbsViterbi(char[] alph, Character[] states, char[] outcome, char[] path) {
		// initialize return variable
		double[][] emiss = new double[states.length][alph.length];
		// initialize row-counter (will hold the total count of a row)
		int[] rowCount = new int[states.length];
		
		// loop over all states in path
		for (int i = 0; i < path.length; ++i) {
			// save the index of this state
			int stateIndex = indexOf(states, path[i]);
			// increment this state's row-counter
			++rowCount[stateIndex];
			// increment this state's emission of this char's index
			++emiss[stateIndex][indexOf(alph, outcome[i])];
		}
		
		// loop over all rows of emiss
		for (int i = 0; i < emiss.length; ++i)
			// loop over all values in this row
			for (int j = 0; j < emiss[0].length; ++j) {
				// if this row has some emissions then 
				if (rowCount[i] > 0) 
					// probability is value / rowCount
					emiss[i][j] /= rowCount[i];
				// otherwise, each value is the same
				else emiss[i][j] = 1.0 / alph.length;
			}
		
		return emiss;
	}

	/**
	 * Guesses the values of a transition matrix given conditional probabilities of paths
	 * @param pathProbs conditional path probabilities in a Viterbi graph
	 * @param numStates the number of different hidden states
	 * @return the completed transition matrix
	 */
	public static double[][] guessTransProbsBW(double[][] pathProbs, int numStates) {
		// initialize return variable
		double[][] trans = new double[numStates][numStates];
		// initialize a row-counter (total value of each row)
		double[] rowCount = new double[numStates];
		
		// loop over all cells in pathProbs
		for (int row = 0; row < pathProbs.length; ++row)
			for (int col = 0; col < pathProbs[0].length; ++col) {
				// increment the proper transition probability
				trans[row / numStates][row % numStates] += pathProbs[row][col];
				// increment the proper state's transitions
				rowCount[row / numStates] += pathProbs[row][col];
			}
		
		// loop over all cells in trans
		for (int row = 0; row < trans.length; ++row)
			for (int col = 0; col < trans[0].length; ++col)
				// divide by the total value of the row
				trans[row][col] /= rowCount[row];
		
		return trans;
	}

	/**
	 * Guesses the values of an emission matrix given conditional probabilities of states
	 * @param stateProbs conditional state probabilities in a Viterbi graph
	 * @param outcome the Viterbi graph's emission
	 * @param alph the alphabet of possible emitted chars
	 * @return the completed emission matrix
	 */
	public static double[][] guessEmissProbsBW(double[][] stateProbs, char[] outcome, char[] alph) {
		// initialize return variable
		double[][] emiss = new double[stateProbs.length][alph.length];
		// initialize row-counter (total value of that row)
		double[] rowCount = new double[stateProbs.length];
		
		// loop over all cells in stateProbs
		for (int row = 0; row < stateProbs.length; ++row)
			for (int col = 0; col < stateProbs[0].length; ++col) {
				// increment the proper emission probability
				emiss[row][indexOf(alph, outcome[col])] += stateProbs[row][col];
				// increment the proper state's emissions
				rowCount[row] += stateProbs[row][col];
			}
		
		// loop over all cells of emiss
		for (int row = 0; row < emiss.length; ++row)
			for (int col = 0; col < emiss[0].length; ++col)
				// divide by the total value for that row
				emiss[row][col] /= rowCount[row];
		
		return emiss;
	}
}