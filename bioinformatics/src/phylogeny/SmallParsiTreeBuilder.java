package phylogeny;

// for data structures
import java.util.HashMap;
import java.util.ArrayList;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * A specialized TreeBuilder that requires the directed downward-flowing adjacency list of a rooted
 * tree, and, using the Small Parsimony algorithm, can infer ancestral nodes' labels. Overrides
 * writeAdjList to write the adjacency list with the calculated labels.
 * @author faith
 */
public class SmallParsiTreeBuilder extends TreeBuilder {
	/**
	 *  all of the possible DNA bases
	 */
	private static final char[] BASES = {'A', 'C', 'G', 'T'}; 
	/**
	 * all of the possible amino acids
	 */
	private static final char[] ACIDS = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'};
	/**
	 *  the (directed) adjacency list of the tree
	 */
	private HashMap<Integer, ArrayList<Integer>> adjList;
	/**
	 *  a map for node#-label, stored in a char array for memory purposes
	 */
	private HashMap<Integer, char[]> labelMap;
	/**
	 * the minimum parsimony (sum of all edges) score of this tree
	 */
	private int totalScore;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes adjList, labelMap (adding in missing nodes
	 * from adjList if necessary), and totalScore
	 * @param adjList the (directed, downward) adjacency list of this tree
	 * @param labelMap a mapping of (at least) all leaves to labels
	 */
	public SmallParsiTreeBuilder(HashMap<Integer, ArrayList<Integer>> adjList,
			HashMap<Integer, char[]> labelMap) {
		super(labelMap.size());
		this.adjList = adjList;
		totalScore = 0;
		
		// updating labelMap if necessary
		for (Integer node : adjList.keySet())
			labelMap.putIfAbsent(node, new char[labelMap.get(0).length]);
		this.labelMap = labelMap;
	}
	
	// various private methods to handle background processes of the public ones
	
	/**
	 * Finds an ID# of a ripe (unprocessed with all children processed) node
	 * <br>
	 * Loops over all nodes in processed, and if they haven't been processed then checks
	 * all children. If all children have been processed, returns the current index.
	 * @param processed an array where [i] is whether the #i node has been processed
	 * @return the ID# of a ripe node, or null if none exists
	 * @throws IllegalArgumentException if processed is null
	 */
	private Integer findRipe(boolean[] processed) {
		// check for problems
		if (processed == null) throw new IllegalArgumentException("Can't search through a null array");
		
		// loop over all nodes
		for (int i = 0; i < processed.length; i++) {
			// if this node hasn't been processed yet 
			if (!processed[i]) {
				// if it has no children, it's ripe!
				if (!adjList.containsKey(i)) return i;
				
				// start by assuming all children have been processed
				boolean good = true;
				// loop over all children
				for (Integer out : adjList.get(i)) {
					// if this child hasn't been processed
					if (!processed[out]) {
						// note that it hasn't
						good = false;
						// leave, no point checking other children
						break;
					}
				}
				// if all children were processed, it's ripe!
				if (good) return i;
			}
		}
		
		// if no ripe nodes were found, return null
		return null;
	}
	
	/**
	 * Finds the minimum value in an array
	 * <br>
	 * Sets the minimum to the first value, then loops over all others, updating
	 * minimum if necessary.
	 * @param arr the array to find the minimum value in
	 * @return the minimum value in arr
	 * @throws IllegalArgumentException if arr is empty or null
	 */
	private int minArray(int[] arr) {
		// check for problems
		if (arr == null) throw new IllegalArgumentException("Can't minimize a null array");
		if (arr.length == 0) throw new IllegalArgumentException("Can't minimize a 0-length array");
		
		// initialize minimum to the first element
		int min = arr[0];
		// loop over all other elements
		for (int i = 1; i < arr.length; i++) if (arr[i] < min) min = arr[i];
		return min;
	}
	
	/**
	 * Calculates the number of different chars between to strings
	 * <br>
	 * Loops over the strings, incrementing a counter for each different char
	 * @param one one string
	 * @param two another string
	 * @return the number of differences
	 * @throws IllegalArgumentException if the strings are of a different length
	 */
	private int hammingDistance(String one, String two) {
		// check for problems
		if (one.length() != two.length())
			throw new IllegalArgumentException("Can't find differences if strings are different lengths");
		
		// initialize return variable
		int dist = 0;
		// loop over all chars
		for (int i = 0; i < one.length(); i++)
			// if different, increment
			if (one.charAt(i) != two.charAt(i)) dist++;
		
		return dist;
	}
	
	/**
	 * Reverses the adjacency list (maps values to keys)
	 * <br>
	 * Loops over all keys, and for each all values, inputting (value, key) into a return variable
	 * @return the adjacency list reversed
	 */
	private HashMap<Integer, Integer> getReverseAdjList() {
		// initialize return variable
		HashMap<Integer, Integer> rev = new HashMap<Integer, Integer>(labelMap.size());
		// loop over all keys
		for (Integer start : adjList.keySet()) 
			// loop over all values, and add (value, key) to return variable
			for (Integer end : adjList.get(start)) rev.put(end, start);
			
		return rev;
	}
	
	/**
	 * Labels one char-position in a tree using a score array
	 * <br>
	 * Looping over all nodes from root to leaves, finds all chars that would maximize score for that node.
	 * If more than one, prefers the parent node's char as a tiebreaker.
	 * @param score a score array with [i][j] being the parsimony score for the i-th node using the j-th base
	 * @param let the char-position to label in the tree
	 * @throws IllegalArgumentException if score is null, the wrong size, or let is an invalid index
	 */
	private void labelTree(int[][] score, int let) {
		// check for problems
		if (score == null) throw new IllegalArgumentException("Can't score using a null array");
		if (let < 0 || let >= labelMap.get(0).length) throw new IllegalArgumentException("Invalid index to label");
		if (score.length != labelMap.size()) throw new IllegalArgumentException("Improperly sized scoring array");
		
		// get the reverse adjacency list
		HashMap<Integer, Integer> reverseAdjList = getReverseAdjList();
		// initialize a placeholder list (save memory by only having 1)
		ArrayList<Character> best = new ArrayList<Character>(4);
		
		// loop over all nodes from the root down
		for (int node = labelMap.size() - 1; node >= 0; node--) {
			// clear the list of best chars
			best.clear();
			// initialize the minimum score to the maximum possible
			int min = Integer.MAX_VALUE;
			
			// loop over all bases
			for (int a = 0; a < ACIDS.length; a++) {
				// if this score beats the current best
				if (score[node][a] < min) {
					// set best to only this base
					best.clear();
					best.add(ACIDS[a]);
					// set the minimum to this score
					min = score[node][a];
				}
				// or if it just matches, add the current base
				else if (score[node][a] == min) best.add(ACIDS[a]);
			}
			
			// if only one char is the best, or if this is the root node (no way to break ties)
			if (best.size() == 1 || node == labelMap.size() - 1)
				// just grab that char
				labelMap.get(node)[let] = best.get(0);
			
			// or if a tiebreaker is needed
			else {
				// save the char of the parent node
				char parentChar = labelMap.get(reverseAdjList.get(node))[let];
				// if this is one of the chars under consideration, use it
				if (best.contains(parentChar)) labelMap.get(node)[let] = parentChar;
				// otherwise just grab the first candidate
				else labelMap.get(node)[let] = best.get(0);
			}
		}
	}
	
	// various protected methods to allow some classes to modify adjList
	
	/**
	 * Removes a path from the adjacency list
	 * <br>
	 * Removes for adjList, then checks if the entry should be deleted entirely
	 * @param start the start node ID# of the path to delete
	 * @param end the end node ID# of the path to delete
	 * @throws IllegalArgumentException if the path does not exist
	 */
	protected void removePath(int start, int end) {
		if(!adjList.get(start).remove((Integer) end))
			throw new IllegalArgumentException(
					"Can't remove path " + start +
					"->" + end + " that doesn't exist");
		if (adjList.get(start).isEmpty()) adjList.remove(start);
	}
	
	/**
	 * Adds a path to the adjacency list
	 * <br>
	 * Adds an entry if necessary, then adds the path
	 * @param start the start node ID# of the path to delete
	 * @param end the end node ID# of the path to delete
	 * @throws IllegalArgumentException if path already exists
	 */
	protected void addPath(int start, int end) {
		if (adjList.get(start).contains(end))
			throw new IllegalArgumentException("Can't add path "
					+ start + "->" + end + " that already exist");
		adjList.putIfAbsent(start, new ArrayList<Integer>());
		adjList.get(start).add(end);
	}
	
	/**
	 * Builds up the tree's labels using Small Parsimony
	 * <br>
	 * Loops one time for each char-position to label. For each, uses a processed-array
	 * ([i] is whether the i-th node has been processed) and a score array ([i][j] is
	 * the minimum parsimony score for the i-th node with the j-th base as a label).
	 * First builds up the score array for the leaf nodes, then continuously finds
	 * ripe nodes (not yet processed with all children processed) and calculates
	 * parsimony scores for them. Finally, with the finished score array, labels all
	 * nodes with the appropriate char, and increments totalScore by the minimum
	 * score for the root node.
	 */
	public void buildTree() {
		totalScore = 0;
		// loop for all chars-spots that need labeling
		for (int let = 0, len = labelMap.get(0).length; let < len; let++) {
			// note that no nodes have been processed (all indexes have false)
			boolean[] processed = new boolean[labelMap.size()];
			// initialize a parsimony score array
			int[][] score = new int[processed.length][ACIDS.length];
			
			// loop over all leaf nodes
			for (int node = 0; node < getTree().getN(); node++) {
				// note that this node has been processed
				processed[node] = true;
				// loop over all bases
				for (int a = 0; a < ACIDS.length; a++)
					// if the char in this position and the base do not match, score = 1
					if (labelMap.get(node)[let] != ACIDS[a]) score[node][a]++;	
			}
			
			// grab a ripe node
			Integer ripe = findRipe(processed);
			// while there are ripe nodes left
			while (ripe != null) {
				// note that this node has been processed
				processed[ripe] = true;
				// loop over each base, and for each base loop over each child
				for (int a = 0; a < ACIDS.length; a++) for (Integer child : adjList.get(ripe)) {
					// initialize the minimum score to the maximum possible
					int min = Integer.MAX_VALUE;
					
					// loop over each base for this child
					for (int ca = 0; ca < ACIDS.length; ca++) {
						// set the score at this point to the child's score
						int val = score[child][ca];
						// if the child-base and the parent-base don't match, increment the score
						if (ca != a) val++;
						// if the score beats the current minimum, set it as so
						if (val < min) min = val;
					}
					// add this child's sub-score to the overall score for this node
					score[ripe][a] += min;
				}
				
				// get another ripe node
				ripe = findRipe(processed);
			}
			
			// label the tree over this char-position using the score array
			labelTree(score, let);
			// increment the total score by the root's minimum
			totalScore += minArray(score[score.length - 1]);
		}
	}
	
	/**
	 * Writes the adjacency list of the labeled tree to a file
	 * <br>
	 * Loops over all start nodes, and then all end nodes of paths coming out,
	 * and calculates the weight (hamming distance) of the path. Writes both
	 * paths (start->end, end->start) with the proper weight, formatted.
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		// try to open a writer on the file
		try {
			// point a writer at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			// write the score on a line by itself
			writer.write(totalScore + "\n");
			
			// loop over all starts of a path
			for (Integer start : adjList.keySet()) {
				// find the String of this node
				String s = new String(labelMap.get(start));
				// loop over all ends of this node's paths
				for (Integer end : adjList.get(start)) {
					// find the String of this node
					String e = new String(labelMap.get(end)); 
					// calculate the weight of this path
					int d = hammingDistance(s, e);
					// write it both ways
					writer.write(s + "->" + e + ":" + d + "\n");
					writer.write(e + "->" + s + ":" + d + "\n");
				}
			}
			// clean up
			writer.close();
		}
		// if something weird happened, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clones the adjacency list
	 * <br>
	 * Loops over each node-key, cloning its value for a new HashMap
	 * @return a deep-copy of the adjacency list
	 */
	public HashMap<Integer, ArrayList<Integer>> getAdjList() {
		// initialize return variable to exact size needed
		HashMap<Integer, ArrayList<Integer>> copy =
				new HashMap<Integer, ArrayList<Integer>>(adjList.size());
		// loop over all keys, adding in a deep-copy of the value
		for (Integer key : adjList.keySet())
			copy.put(key, new ArrayList<Integer>(adjList.get(key)));
		
		return copy;
	}

	/**
	 * Gets all paths coming out of a node
	 * @param node the node to consider
	 * @return all node ID#s at the other end of the paths
	 */
	public ArrayList<Integer> getPaths(int node) {
		return new ArrayList<Integer>(adjList.get(node));
	}
	
	/**
	 * Getter for the score of this tree
	 * @return this.totalScore
	 */
	public int getScore() {return totalScore;}
}