package phylogeny;

// for data structures
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A specialized TreeBuilder for a "perfect" SNP matrix
 * @author faith
 */
public class PerfectPhylogeny extends TreeBuilder {
	/**
	 * the SNP matrix the tree is to be build from
	 */
	private boolean[][] snps;
	/**
	 * a mapping of all node ID#s to vectors
	 */
	private HashMap<Integer, boolean[]> vectorMap;
	
	/**
	 * Constructor
	 * <br>
	 * Sorts then sets SNPs, initializes vectorMap
	 * @param snps
	 */
	public PerfectPhylogeny(boolean[][] snps) {
		super(snps.length);
		this.snps = sortSNPs(snps);
		vectorMap = new HashMap<Integer, boolean[]>();
	}
	
	/**
	 * Sorts the SNPs in lexicographic order
	 * @param unsorted a matrix of SNPs, unsorted
	 * @return the same matrix with columns sorted
	 */
	private static boolean[][] sortSNPs(boolean[][] unsorted) {
		// the number of "true"s in each column
		ArrayList<Integer> numTrue = new ArrayList<Integer>(unsorted[0].length);
		// the columns in order
		ArrayList<boolean[]> cols = new ArrayList<boolean[]>(unsorted[0].length);
		
		// loop over all columns
		for (int i = 0; i < unsorted[0].length; i++) {
			// initialize trues to 0
			int trues = 0;
			// initialize the current column
			boolean[] curCol = new boolean[unsorted.length];
			// loop over all cells/rows
			for (int j = 0; j < unsorted.length; j++) {
				// update curCol and trues
				curCol[j] = unsorted[j][i];
				if (unsorted[j][i]) trues++;
			}
			
			// initialize the spot to add this column at the end
			int addIndex = i;
			// while the column before has less trues, move addIndex back
			while (addIndex > 0 && numTrue.get(addIndex - 1) < trues) addIndex--;
			// add trues and curCol to proper spots
			numTrue.add(addIndex, trues);
			cols.add(addIndex, curCol);
		}
		
		// initialize return variable
		boolean[][] sorted = new boolean[unsorted.length][unsorted[0].length];
		// loop over all rows
		for (int i = 0; i < unsorted.length; i++)
			// loop over all columns
			for (int j = 0; j < unsorted[0].length; j++)
				// copy proper value from cols into this cell of sorted
				sorted[i][j] = cols.get(j)[i];
			
		return sorted;
	}
	
	/**
	 * Builds the specified subtree
	 * @param rootID the ID# of the root of the subtree
	 * @param col the column of the SNP matrix to consider
	 * @param resp a responsibility map
	 */
	private void buildSubtree(int rootID, int col, HashMap<Integer, ArrayList<Integer>> resp) {
		// if no columns found a difference between these rows
		if (col == snps[0].length)
			// the nodes are the same, attach all
			for (Integer leafID : resp.get(rootID))
				getTree().addNodeWithPath(leafID, rootID, 0);
			
		// otherwise if the current nodes can be split into subgroups
		else {
			// calculate node ID#s for child nodes
			int trueChild = getTree().getHighestNode() + 1;
			int falseChild = trueChild + 2;
			// initialize spots in resp
			resp.put(trueChild, new ArrayList<Integer>());
			resp.put(falseChild, new ArrayList<Integer>());
			
			// loop over all leaf nodes that this root has
			for (Integer leaf : resp.get(rootID)) {
				// if the leaf node has a true in this spot, move to trueChild's list
				if (snps[leaf][col]) resp.get(trueChild).add(leaf);
				// otherwise move to falseChild's list
				else resp.get(falseChild).add(leaf);
			}
			// get rid of rootID, all of its nodes have been divided up
			resp.remove(rootID);
			
			// if all leaves were moved to the falseChild list
			if (resp.get(trueChild).isEmpty()) {
				// move falseChild back to root
				resp.put(rootID, resp.remove(falseChild));
				// delete trueChild's (empty) listing
				resp.remove(trueChild);
				
				// build subtree from same root using next column
				buildSubtree(rootID, col + 1, resp);
			}
			
			// else if all leaves were moved to the trueChild list
			else if (resp.get(falseChild).isEmpty()) {
				// move trueChild back to root
				resp.put(rootID, resp.remove(trueChild));
				// delete false Child's (empty) listing
				resp.remove(falseChild);
				
				// as long as the root isn't the overall root, change it to be "true" at this spot
				if (rootID != snps.length) {
					// grab the root Node by ID#
					Node rootNode = getTree().getNode(rootID);
					// edit weight of the rootNode's backwards path up one
					rootNode.editWeight(rootNode.getAdjNodes().iterator().next(), 1);
					// edit the vector entry to have a "true" in the proper spot
					vectorMap.get(rootID)[col] = true;
				}
				// build subtree from same root using next column
				buildSubtree(rootID, col + 1, resp);
			}
			
			// if here, then nodes in both subtrees
			else {
				// if falseChild has only one leaf, just attach leaf directly to root
				if (resp.get(falseChild).size() ==  1) {
					// add the leaf's path to the root
					getTree().addNodeWithPath(resp.get(falseChild).get(0), rootID, 0);
					// get rid of falseChild's entry
					resp.remove(falseChild);
				}
				// otherwise falseChild is the root of a bona fide subtree
				else {
					// add the inner node's path to the root
					getTree().addNodeWithPath(falseChild, rootID, 0);
					// add an entry for this inner node in the vector map
					vectorMap.put(falseChild, vectorMap.get(rootID).clone());
					// build the subtree from falseChild root using next column
					buildSubtree(falseChild, col + 1, resp);
				}
				
				// if trueChild has only one leaf, just attach leaf directly to root
				if (resp.get(trueChild).size() == 1) {
					// add the leaf's path to the root
					getTree().addNodeWithPath(resp.get(trueChild).get(0), rootID, 1);
					// get rid of trueChild's entry
					resp.remove(trueChild);
				}
				// otherwise trueChild is the root of a bona fide subtree
				else {
					// add the inner node's path to the root
					getTree().addNodeWithPath(trueChild, rootID, 1);
					// add an entry for this inner node in the vector map
					vectorMap.put(trueChild, vectorMap.get(rootID).clone());
					// change proper SNP to true!
					vectorMap.get(trueChild)[col] = true;
					// build the subtree from trueChild root using next column
					buildSubtree(trueChild, col + 1, resp);
				}
			}
		}
	}
	
	/**
	 * Builds the tree with the perfect phylogeny algorithm
	 */
	public void buildTree() {
		// initialize a responsibility map (internal node ID# -> leaf ID#s in subtree beneath)
		HashMap<Integer, ArrayList<Integer>> resp = new HashMap<Integer, ArrayList<Integer>>();
		// the root of the tree will be N (#leaves)
		int rootID = getTree().getN();
		// add the root to the responsibility map
		resp.put(rootID, new ArrayList<Integer>());
		// add each leaf under the root and its vector
		for (int i = 0; i < snps.length; i++) {
			resp.get(rootID).add(i);
			vectorMap.put(i, snps[i]);
		}
		// add the root to the tree
		getTree().addNode(rootID);
		// the root has vector of all 0
		vectorMap.put(rootID, new boolean[snps[0].length]);
		
		// build the tree, starting with the root and the first column
		buildSubtree(rootID, 0, resp);
		// normalize all node ID#s
		getTree().normalizeNodes();
		normalizeVectorMap();
	}
	
	/**
	 * Finds the max Integer key
	 * @param map the map to seach through
	 * @return the value of the maximal key
	 */
	private static int maxKey(HashMap<Integer, boolean[]> map) {
		// initialize max to lowest value
		int max = Integer.MIN_VALUE;
		// loop over all keys, updating max once new max found
		for (Integer key : map.keySet()) if (key > max) max = key;
		
		return max;
	}
	
	/**
	 * Normalizes the node ID#s in vectorMap so they go unbroken from 0->max
	 */
	private void normalizeVectorMap() {
		// grab the max node ID#
		int maxKey = maxKey(vectorMap);
		// loop from back (so moving down is easier)
		for (int i = maxKey - 1; i >= 0; i--)
			// if this ID# needs a node
			if (vectorMap.get(i) == null) {
				// loop over all ID#s above it
				for (int j = i + 1; j <= maxKey; i=j++) {
					vectorMap.put(j - 1, vectorMap.remove(j));
				}
				// adjust highestNode
				maxKey--;
			}
	}
	
	/**
	 * Prints out the SNP matrix
	 */
	public void printSNPs() {
		// loop over all rows
		for (int i = 0; i < snps.length; i++) {
			// print out the sample number
			System.out.print("#" + i);
			// loop over all cells/columns
			for (int j = 0; j < snps[0].length; j++) {
				System.out.print(" ");
				// if this SNP is true, print 1
				if (snps[i][j]) System.out.print(1);
				// if this SNP is false, print 0
				else System.out.print(0);
				// space before next number
			}
			// newline before next sample
			System.out.println();
		}
	}
	
	/**
	 * Prints out the vector map
	 */
	public void printVectorMap() {
		for (int i = 0; i <= maxKey(vectorMap); i++) {
			// print out the node ID#
			System.out.print("#" + i);
			// loop over all cells/columns
			for (int j = 0; j < snps[0].length; j++) {
				System.out.print(" ");
				// if this SNP is true, print 1
				if (vectorMap.get(i)[j]) System.out.print(1);
				// if this SNP is false, print 0
				else System.out.print(0);
				// space before next number
			}
			// newline before next node
			System.out.println();
		}
	}
}