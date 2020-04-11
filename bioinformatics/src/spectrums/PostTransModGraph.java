package spectrums;

/**
 * Holds a Post-Translational Modification graph, and can traverse it to
 * find the optimal modifications to fit a peptide to a vector. Uses a
 * three-dimensional coordinate system (x, y, z) where x = spot in the
 * vector being used, y = amino acid of the peptide being used, and z =
 * number of modifications that have been made. All of x, y, and z are
 * 1-indexed.
 * @author faith
 */
public class PostTransModGraph {
	/**
	 * maximum scores for all of the nodes
	 */
	private short[][][] scores;
	/*
	 * optimal backtracks for all of the nodes
	 */
	private short[][][] backtrack;
	/**
	 * the vector used for scoring
	 */
	private byte[] vector;
	/**
	 * masses for each row's amino acid
	 */
	private short[] rowMass;
	/**
	 * the peptide being modified
	 */
	private char[] peptide;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes all instance variables
	 * @param peptide the peptide to modify
	 * @param vector the vector to score against
	 * @param mods the number of modifications to allow
	 */
	public PostTransModGraph(String peptide, byte[] vector, int mods) {
		this.peptide = peptide.toCharArray();
		this.vector = vector;
		// initialize all arrays to proper size
		scores = new short[vector.length + 1][peptide.length() + 1][mods + 1];
		backtrack = new short[vector.length + 1][peptide.length() + 1][mods + 1];
		rowMass = new short[peptide.length()];
		// special filling of scores and rowMass
		initializeScores();
		initializeRowMass();
	}
	
	/**
	 * Initializes the scores array
	 * <br>
	 * Loops through all values, setting to the smallest possible
	 * integer, then sets the source node (0, 0, 0) to score 0
	 */
	private void initializeScores() {
		// loop through all values to set to -inf
		for (short x = 0; x < scores.length; x++)
			for (byte y = 0; y < scores[x].length; y++)
				for (byte z = 0; z < scores[x][y].length; z++)
					scores[x][y][z] = Short.MIN_VALUE;
		
		// set source to 0
		scores[0][0][0] = 0;
	}
	
	/**
	 * Initializes the row mass array
	 * <br>
	 * Loops through all chars (amino acids) in peptide,
	 * using VectorGraph's mass lookup table to find mass
	 */
	private void initializeRowMass() {
		// loop through all amino acids in peptide
		for (int i = 0; i < peptide.length; i++)
			// set this row mass to the mass of the amino acid
			rowMass[i] = VectorGraph.REVERSE_MASS_TABLE.get(peptide[i]);
	}
	
	/**
	 * Fills in the score array
	 * <br>
	 * First fills in all legal spots in the first layer, then loops over all layers,
	 * all legal rows, and all legal nodes. For each finds the optimal backtrack - first
	 * checks for diagonals, then updates the best-in-non-diag-row spot and tries to use that
	 */
	private void fillScores() {
		// loop through all diagonal moves through the first layer
		for (short y = 1, x = 0; y < scores[0].length && x + rowMass[y - 1] < scores.length; y++) {
			// shift the x being used forward by rowMass of the row
			x += rowMass[y - 1];
			// set node score to diagonal node + score of this node by vector
			scores[x][y][0] = (short) (scores[x - rowMass[y - 1]][y - 1][0] + vector[x - 1]);
		}
		
		// loop through all layers after the first and all legal rows on that layer
		for (byte z = 1; z < scores[0][0].length; z++) for (byte y = z; y < scores[0].length; y++) {
			// initialize the best spot in the last upper row to the first legal one
			int[] maxLastRow = {y - 1, scores[y - 1][y - 1][z - 1]};
			// loop through all nodes in this row
			for (short x = y; x < scores.length; x++) {
				// check to see if a diagonal move is legal & not to a -inf
				if (x - rowMass[y - 1] >= y + z &&
						scores[x - rowMass[y - 1]][y - 1][z] != Short.MIN_VALUE)
					// if so, set the score of this node as if that was used
					scores[x][y][z] = (short) (scores[x - rowMass[y - 1]][y - 1][z] + vector[x - 1]);
				
				// if the spot non-diagonally just up beats the current best lastRow
				if (scores[x - 1][y - 1][z - 1] > maxLastRow[1]) {
					// set the spot accordingly
					maxLastRow[0] = x - 1;
					maxLastRow[1] = scores[x - 1][y - 1][z - 1];
				}
				
				// if the maximum best spot in the last upper row is legal & would beat the current value
				if (maxLastRow[1] != Short.MIN_VALUE && maxLastRow[1] + vector[x - 1] > scores[x][y][z]) {
					// use it as a path
					scores[x][y][z] = (short) (maxLastRow[1] + vector[x - 1]);
					backtrack[x][y][z] = (short) (x - maxLastRow[0]);
				}
			}
		}
	}
	
	/**
	 * Checks to see if a node is the source node
	 * <br>
	 * Checks for having 3 coordinates, and then that (x, y, z) == (0, 0, 0)
	 * @param node a node's coordiates (x, y, z)
	 * @return whether the node is the source node
	 */
	private boolean notSource(int[] node) {  
		return !(node.length == 3 && node[0] == 0 && node[1] == 0 && node[2] == 0);
	}
	
	/**
	 * Reconstructs the optimal modified peptide
	 * <br>
	 * Starting at the sink node, pulls the backtrack and adds
	 * to the peptide what it indicates, then adjusts the
	 * current node backwards
	 * @return the optimal modified peptide
	 */
	private String backtrack() {
		// initialize return variable
		String mod = "";
		
		// start with the sink node
		int[] curNode = {backtrack.length - 1, backtrack[0].length - 1, backtrack[0][0].length - 1};
		// while the current node is not the source
		while (notSource(curNode)) {
			// grab its backtrack
			int curBacktrack = backtrack[curNode[0]][curNode[1]][curNode[2]];
			// if the backtrack indicates a diagonal path, move diagonally
			if (curBacktrack == 0) curNode[0] -= rowMass[curNode[1] - 1];
			
			// otherwise
			else {
				// calculate the modification
				short change = (short) (curBacktrack - rowMass[curNode[1] - 1]); 
				// add in the modification with indications over direction
				if (change > 0) mod = "(+" + change + ")" + mod;
				else mod = "(" + change + ")" + mod;
				// move back to the indicated node
				curNode[0] -= curBacktrack;
				curNode[2]--;
			}
			
			// add the next char in
			mod = peptide[curNode[1] - 1] + mod;
			// move up a row
			curNode[1]--;
		}
		
		return mod;
	}
	
	/**
	 * Finds the optimal modified peptide
	 * <br>
	 * Scores all relevant nodes, then backtracks from the sink
	 * @return the optimal peptide with modifications noted
	 */
	public String findPeptide() {
		fillScores();
		System.out.println(scores[scores.length - 1][scores[0].length - 1][scores[0][0].length - 1]);
		return backtrack();
	}
}