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
		this.nodeWeights = nodeWeights;
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