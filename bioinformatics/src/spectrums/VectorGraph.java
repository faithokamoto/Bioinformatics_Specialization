package spectrums;

// for data tables
import java.util.HashMap;

// for reading from files
import java.io.File;
import java.util.Scanner;

/**
 * Can calculate the optimally scoring peptide for an experimental spectrum,
 * given a spectra vector as a byte[]
 * @author faith
 */
public class VectorGraph {
	/**
	 * amino acid abbreviation->mass lookup table
	 */
	public static final HashMap<Character, Short> REVERSE_MASS_TABLE = reverseMassTable();
	/**
	 * mass->amino acid abbreviation lookup table
	 */
	public static final HashMap<Short, Character> MASS_TABLE = readMassTable();
	/**
	 * the minimum weight of any amino acid
	 */
	public static final int MIN_WEIGHT = 57;
	/**
	 * the vector spectrum ([i] is weight i's value in the spectrum)
	 */
	private byte[] nodeWeights;
	
	/**
	 * Initializes the reverse mass table
	 * <br>
	 * As long as more entries are left, reads in the next entry from the amino
	 * acid file into a HashMap
	 * @return the completed reverse mass table
	 */
	private static HashMap<Character, Short> reverseMassTable() {
		// initialize return variable
		HashMap<Character, Short> rmt = new HashMap<Character, Short>(27);
		// try to read from the amino acid file
		try {
			// point a reader at the file
			Scanner reader = new Scanner(new File("src/spectrums/amino_acids.txt"));
			// while more entries are left to read
			while (reader.hasNext())
				// read in the next entry
				rmt.put(reader.next().charAt(0), reader.nextShort());
			// clean up
			reader.close();
		}
		// if anything fishy happens, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return rmt;
	}
	
	/**
	 * Initializes the mass table
	 * <br>
	 * For each amino acid in the reverse mass table, enters in the mass
	 * as a key and acid as the value. Overwrites twice for I/L && K/Q
	 * @return the completed mass table
	 */
	private static HashMap<Short, Character> readMassTable() {
		// initialize return variable
		HashMap<Short, Character> mt = new HashMap<Short, Character>(24);
		// loop over reverse mass table, swap key-value order and enter
		for (Character amino : REVERSE_MASS_TABLE.keySet())
			mt.put(REVERSE_MASS_TABLE.get(amino), amino);
		
		return mt;
	}
	
	/**
	 * Constructor
	 * <br>
	 * Initialize nodeWeights
	 * @param nodeWeights the weights of the various spectrum spots
	 */
	public VectorGraph(byte[] nodeWeights) {
		setVector(nodeWeights);
	}
	
	public void setVector(byte[] newWeights) {
		nodeWeights = newWeights;
	}
	
	/**
	 * Calculates a node's optimal score and string
	 * <br>
	 * Try all paths with jump-length of an amino acid mass, and if the 
	 * score beats the current best then set score and string to that.
	 * @param node the node to calculate
	 * @param maxScores the previously computed scores
	 * @param maxStrings the previously computed strings
	 */
	private void calcNode(int node, int[] maxScores, String[] maxStrings) {
		// initialize this node's score to a minimum value
		maxScores[node] = Integer.MIN_VALUE + 1000;
		// initialize this node's string to empty
		maxStrings[node] = "";
		
		// loop over all possible masses to add
		for (Short mass : MASS_TABLE.keySet()) {
			// calculate the end node if this mass's path was used
			int endNode = node + mass;
			// only calculate if the end node is in an acceptable range
			if ((endNode == nodeWeights.length - 1 || endNode < nodeWeights.length - MIN_WEIGHT)
					// if score is higher using this node
				&& (maxScores[endNode] + nodeWeights[endNode] > maxScores[node])) 
				// set score and string to use that path
				maxScores[node] = maxScores[endNode] + nodeWeights[endNode];
				maxStrings[node] = MASS_TABLE.get(mass) + maxStrings[endNode];
		}
	}

	/**
	 * Finds the ideal peptide with the current weights
	 * <br>
	 * Initializes the dynamic programming arrays, runs calcNode on all necessary
	 * nodes, then pulls the optimal string for the sink node (node #0)
	 * @return
	 */
	public String findPeptide() {
		// initialize some dynamic programming arrays
		int[] maxScores = new int[nodeWeights.length];
		String[] maxStrings = new String[nodeWeights.length];
		// the sink node has a blank string
		maxStrings[maxStrings.length - 1] = "";
		// loop over all needed nodes
		for (int node = nodeWeights.length - MIN_WEIGHT - 1; node >= 0; node--)
			// calculate score & string
			calcNode(node, maxScores, maxStrings);
		// return the source node's string
		return maxStrings[0];
	}
	
	/**
	 * Calculates the weight of a peptide
	 * <br>
	 * Adds the weight of each amino acid from the rmt
	 * @param peptide the peptide to find weight of
	 * @return the total weight of each amino acid in the peptide
	 */
	private static int totalWeight(String peptide) {
		// initialize return variable
		int weight = 0;
		// loop over all chars in peptide
		for (int i = 0; i < peptide.length(); i++)
			// add weight of this char
			weight += REVERSE_MASS_TABLE.get(peptide.charAt(i));
		
		return weight;
	}
	
	/**
	 * Scores a peptide against the vector
	 * <br>
	 * Loops through all amino acids, moving the node# up
	 * and adding up the scores
	 * @param peptide the peptide to score
	 * @return the score of the peptide
	 */
	private int scorePeptide(String peptide) {
		// initialize return variable
		int score = 0;
		// initialize the node to -1 (0 weight)
		int node = -1;
		// loop over all chars in peptide
		for (int i = 0; i < peptide.length(); i++) {
			// add this aminno acid's weight to node
			node += REVERSE_MASS_TABLE.get(peptide.charAt(i));
			// add the score of this node to the score
			score += nodeWeights[node];
		}
		
		return score;
	}
	
	/**
	 * Finds the highest-scoring peptide against a protenome
	 * <br>
	 * Starting with start index and end index at 0, substrings
	 * protenome and calcualtes the peptide's weight. If wrong, shifts
	 * indexes, and if right calculates score. Peptides that score above
	 * all previous ones are saved.
	 * @param protenome a protenome to search through
	 * @return the highest scoring amino acid substring of protenome
	 */
	public String findPeptide(String protenome) {
		// initially, the best peptide
		String bestPeptide = "";
		// the best score so far is the minimum possible
		int bestScore = Integer.MIN_VALUE, score;
		// the [start index, end index) of the peptide
		int startIndex = 0, endIndex = 0;
		
		// while endIndex is valid
		while (endIndex <= protenome.length()) {
			// set peptide to the proper substring
			String peptide = protenome.substring(startIndex, endIndex);
			// calculate its weight
			int weight = totalWeight(peptide);
			
			// if it weighs too little, add a char by moving endIndex up
			if (weight < nodeWeights.length) endIndex++;
			// if it weights too much, delete a char by moving startIndex up
			else if (weight > nodeWeights.length) startIndex++;
			// or if it weighs the right amount
			else {
				// score it
				score = scorePeptide(peptide);
				// if the score is the best so far
				if (score > bestScore) {
					// set bestScore and bestPeptide
					bestScore = score;
					bestPeptide = peptide;
				}
				// shift the indexes forward
				startIndex++;
				endIndex++;
			}
		}
		
		return bestPeptide;
	}
	
	/**
	 * Finds a peptide in a protenome that scores above threshold
	 * <br>
	 * Gets the best peptide, only returning it if its score
	 * is high enough.
	 * @param protenome the protenome to search
	 * @param threshold the minimum acceptable score
	 * @return the best peptide if score >= threshold, otherwise null
	 */
	public String findPeptide(String protenome, int threshold) {
		String peptide = findPeptide(protenome);
		if (scorePeptide(peptide) >= threshold) return peptide;
		else return null;
	}
	
	/**
	 * Calculates the probability of a dictionary
	 * <br>
	 * Loops over all possible cases of masses (amino acids) to
	 * have as the last one, calculating the new node and target
	 * score for each. Adds up all probabilities for all cases.
	 * @param end the end of the subset of nodes to use
	 * @param target the target score for the peptides
	 * @param preCalc a dynamic array to hold precalculated values
	 */
	private void calcDictProb(int end, int target, Double[][] preCalc) {
		// initialize this spot to 0
		preCalc[end][target] = 0.0;
		// loop over all possible masses to remove
		for (short mass : REVERSE_MASS_TABLE.values()) {
			// calculate the new end and target
			int newEnd = end - mass;
			int newTarget = target - nodeWeights[end];
			// initialize the probability
			double prob = 0;
			
			// now to find the probability of this case
			
			// if any boundary conditions are violated
			if (newTarget < 0 || newEnd < -1 ||
					newTarget >= preCalc[0].length)
				// probability stays 0
				prob = 0;
			// if the new end would be the 0-th node
			else if (newEnd == -1) {
				// if the target is 0 score, then probability is 1/20
				if (newTarget == 0) prob = 1 / 20.0;
				// any other target is not possible
				else prob = 0;
			}
			// if this probability has already been calculated
			else if (preCalc[newEnd][newTarget] != null)
				// just use that
				prob = preCalc[newEnd][newTarget] / 20;
			// or if this probability has to be calculated
			else {
				//calculate it, and use it
				calcDictProb(newEnd, newTarget, preCalc);
				prob = preCalc[newEnd][newTarget] / 20;
			}
			
			// add this case's probability
			preCalc[end][target] += prob;
		}
	}
	
	/**
	 * Calculates the probability of a dictionary
	 * <br>
	 * Calls calcDicProb with end node as the last node and score
	 * as every score on [threshold, maxScore], adding up all
	 * @param threshold the minimum score to accept
	 * @param maxScore the maximum score to accept
	 * @return the probability of a dictionary on this vector
	 */
	public double dictProb(int threshold, int maxScore) {
		// initialize return variable
		double total = 0;
		// initialize the dynamic array
		Double[][] preCalc = new Double[nodeWeights.length][maxScore + 1];
		
		// loop over all allowed scores
		for (int score = threshold; score <= maxScore; score++) {
			// calculate the probability over the whole vector for each
			calcDictProb(nodeWeights.length - 1, score, preCalc);
			// add this probability
			total += preCalc[nodeWeights.length - 1][score];
		}
		return total;
	}
	
	/**
	 * Converts a peptide to a binary vector
	 * @param peptide the peptide to convert (as amino acids)
	 * @return the binary vector for this peptide
	 */
	public static byte[] peptideToVector(String peptide) {
		// initialize array of prefix masses (masses up to this index in peptide)
		int[] prefixMass = new int[peptide.length() + 1];
		
		// put proper values in each index of prefixMass
		for (int i = 1; i <= peptide.length(); i++)
			// mass = mass of the previous stuff + mass of the amino acid at this point
			prefixMass[i] = prefixMass[i - 1] + REVERSE_MASS_TABLE.get(peptide.charAt(i - 1));
		
		// initialize vector to the proper size
		byte[] vector = new byte[prefixMass[prefixMass.length - 1]];
		// add in 1s where necessary
		for (int i = 1; i < prefixMass.length; i++) vector[prefixMass[i] - 1] = 1;
		
		return vector;
	}
}