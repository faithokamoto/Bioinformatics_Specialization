package phylogeny;

// for various data structures
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A specialized TreeBuilder that has a greedy algorithm to find the best tree arrangement
 * and inner-node labels for a set of leaf-strings.
 * @author faith
 */
public class LargeParsiTreeBuilder extends TreeBuilder {
	/**
	 * the adjacency list of the current best tree
	 */
	private HashMap<Integer, ArrayList<Integer>> adjList;
	/**
	 * the labels of all of the leaves
	 */
	private HashMap<Integer, char[]> labelMap;

	/**
	 * String Constructor
	 * <br>
	 * Initializes labelMap and randomize structure
	 * @param leaves all strings meant for leaves
	 */
	public LargeParsiTreeBuilder(ArrayList<String> leaves) {
		super(leaves.size());
		labelMap = new HashMap<Integer, char[]>(leaves.size());
		// insert leaves into labelMap
		for (int leaf = 0; leaf < leaves.size(); leaf++)
			labelMap.put(leaf, leaves.get(leaf).toCharArray());
		// randomize structure
		randomTreeStructure();
	}
	
	/**
	 * Pre-Created Tree Constructor
	 * <br>
	 * Initializes all instance variables
	 * @param adjList an adjacency list of an undirected tree
	 * @param labelMap a mapping of all leaf #s to strings
	 */
	public LargeParsiTreeBuilder(HashMap<Integer, ArrayList<Integer>> adjList,
			HashMap<Integer, char[]> labelMap) {
		super(labelMap.size());
		this.adjList = adjList;
		this.labelMap = labelMap;
	}

	// various private methods to handle background processes of buildTree
	
	/**
	 * Adds an undirected path the the adjacency list
	 * <br>
	 * Adds entries in adjList if necessary, then the path
	 * @param a node ID# for one end of the path
	 * @param b node ID# for other end of the path
	 */
	private void addPath(int a, int b) {
		// add entries if necessary
		adjList.putIfAbsent(a, new ArrayList<Integer>(3));
		adjList.putIfAbsent(b, new ArrayList<Integer>(3));
		// add the path
		adjList.get(a).add(b);
		adjList.get(b).add(a);
	}
	
	/**
	 * Generates a random list with integers 0->(n-1)
	 * <br>
	 * Loops through all numbers to add, inserting at random indexes
	 * @param n the number of numbers to have in the list
	 * @return integers 0->(n-1), in a random order
	 */
	private ArrayList<Integer> randList(int n) {
		// initialize return variable to exact size needed
		ArrayList<Integer> rand = new ArrayList<Integer>(n);
		// loop over all numbers to add
		for (int i = 0; i < n; i++)
			// add to a random index
			rand.add((int) (Math.random() * rand.size()), i);
	
		return rand;
	}
	
	/**
	 * Removes and returns the last item in a list
	 * @param list the list to use
	 * @return the last item in the list
	 */
	private Integer last(ArrayList<Integer> list) {return list.remove(list.size() - 1);}
	
	/**
	 * Creates an adjacency list tree structure randomly
	 * <br>
	 * Randomizes the leaf order, then generates internal nodes and links
	 * leafs to them randomly in a "saturated lipid" line
	 */
	private void randomTreeStructure() {
		// initialize adjacency list to empty
		adjList = new HashMap<Integer, ArrayList<Integer>>();
		
		// randomize the order of the leaves
		ArrayList<Integer> rand = randList(labelMap.size());
		// the first last internal node used is the first non-leaf ID#
		int lastIN = labelMap.size();
		// add a path from one leaf to the first internal node
		addPath(last(rand), lastIN);
		
		// loop over all leaves except 1 used & 2 used after
		for (int leaf = 1; leaf < labelMap.size() - 2; leaf++) {
			// add a path from a random leaf to the last internal node
			addPath(last(rand), lastIN);
			// add a path from the last internal node to the next internal node
			addPath(lastIN, lastIN + 1);
			// change last internal node ot next internal node
			lastIN++;
		}
		
		// attach the last two leaves to the last internal node 
		addPath(rand.get(1), lastIN);
		addPath(rand.get(0), lastIN);
	}
	
	/**
	 * 2-breaks two paths
	 * <br>
	 * Removes the paths, then adds in the new connections
	 * @param start1 the start of the first path
	 * @param end1 the end of the first path
	 * @param start2 the start of the second path
	 * @param end2 the end of the second path
	 * @throws IllegalArgumentException if paths being removed don't exist
	 * 									or paths being added already do
	 */
	private void path2Break(Integer start1, Integer end1, Integer start2, Integer end2) {
		// remove the first path
		if (!adjList.get(start1).remove(end1) || !adjList.get(end1).remove(start1))
			throw new IllegalArgumentException(start1 + "->" + end1 + " doesn't exist");
		// remove the second path
		if(!adjList.get(start2).remove(end2) || !adjList.get(end2).remove(start2))
			throw new IllegalArgumentException(start2 + "->" + end2 + " doesn't exist");
		
		if (adjList.get(start1).contains(end2) || adjList.get(end2).contains(start1))
			throw new IllegalArgumentException(start1 + "->" + end2 + " already exists");
		// add a path from start1->end2
		addPath(start1, end2);
		
		
		if (adjList.get(start2).contains(end1) || adjList.get(end1).contains(start2))
			throw new IllegalArgumentException(start2 + "->" + end1 + " already exists");
		// add a path from start2->end1
		addPath(start2, end1);
	}
	
	/**
	 * Grabs a value from the adjacency list
	 * @param node the node to look at paths of
	 * @param index the index of the path to pull
	 * @return the node ID# in the specified location
	 */
	private int getAL(int node, int index) {return adjList.get(node).get(index);}
	
	/**
	 * Converts the adjacency list to that of the next neighbor
	 * @param a node ID# for one end of the edge
	 * @param b node ID# for other end of the edge
	 * @param second whether to pop two neighbors over
	 */
	private void nextNeighbor(int a, int b, boolean second) {
		// grab an outgoing node from a
		int aEdge = getAL(a, 0);
		// different node if going two neighbors over
		if (second) aEdge = getAL(a, 2);
		// if this path is the edge a->b, change the path
		if (aEdge == b) aEdge = getAL(a, 1);
		
		// grab an outgoing node from b
		int bEdge = getAL(b, 0);
		// if this path is the edge b->a, change the path
		if (bEdge == a) bEdge = getAL(b, 1);
		
		// two-break on the paths found
		path2Break(a, aEdge, b, bEdge);
	}
	
	/**
	 * Converts a neighbor back into the original adjacency list
	 * <br>
	 * Meant to be called after nextNeighbor
	 * @param a node ID# for one end of the edge
	 * @param b node ID# for the other end of the edge
	 */
	private void revert(int a, int b) {
		// grab the neighbor-edges
		int aEdge = getAL(a, 2);
		int bEdge = getAL(b, 2);
		
		// 2-break them away
		path2Break(a, aEdge, b, bEdge);
	}
	
	/**
	 * Clones the adjacency list
	 * <br>
	 * Loops through each key and adds a clone of its value to a new HashMap
	 * @return a deep copy of the adjacency list
	 */
	private HashMap<Integer, ArrayList<Integer>> alClone() {
		// initialize return variable to exact size needed
		HashMap<Integer, ArrayList<Integer>> clone =
				new HashMap<Integer, ArrayList<Integer>>(adjList.size());
		// loop through all keys, cloning values
		for (Integer key : adjList.keySet())
			clone.put(key, new ArrayList<Integer>(adjList.get(key)));
		
		return clone;
	}
	
	/**
	 * Clones the label map
	 * <br>
	 * Loops through each key and adds a clone of its value to a new HashMap
	 * @return a deep copy of the label map
	 */
	private HashMap<Integer, char[]> lmClone() {
		// initialize return variable to exact size needed
		HashMap<Integer, char[]> clone = 
				new HashMap<Integer, char[]>(labelMap.size());
		// loop through all keys, cloning values
		for (Integer key : labelMap.keySet())
			clone.put(key, labelMap.get(key).clone());
		
		return clone;
	}

	/**
	 * Finds all internal edges in the adjacency list
	 * <br>
	 * Starts processing the first non-leaf node, then iteratively checks over
	 * all out-nodes from each node being processed. Determines whether the
	 * out-path should be considered, and if so saves the edge and adds the
	 * out-node to be processed. Once a node has been processed it is moved to
	 * a list of previously processed nodes. Ends once all nodes are processed.
	 * @return all internal edges, as node ID# pairs
	 */
	private ArrayList<int[]> getAllInternalEdges() {
		// initialize return variable
		ArrayList<int[]> edges = new ArrayList<int[]>();
		// all the nodes currently being looked at
		ArrayList<Integer> processing = new ArrayList<Integer>();
		// all the nodes previously looked at
		HashSet<Integer> processed = new HashSet<Integer>();
		
		// calculate the number of leaves
		int numLeaves = labelMap.size();
		// add the first non-leaf node to be processed
		processing.add(numLeaves);
		
		// while some nodes need to be processed
		while (!processing.isEmpty()) {
			// loop over processing from the back to make removal easier
			for (int i = processing.size() - 1; i >= 0; i--) {
				// loop over all nodes coming out of this one
				for (Integer out : adjList.get(processing.get(i))) {
					// if this out-node is an internal node and not yet processed
					if (out >= numLeaves && !processed.contains(out)) {
						// add the edges between it and the in-node
						edges.add(new int[] {processing.get(i), out});
						// note that it needs to be processed
						processing.add(out);
					}
				}
				// move current node from processing to processed
				processed.add(processing.remove(i));
			}
		}
		
		return edges;
	}
	
	/**
	 * Builds up a tree using a greedy parsimony algorithm
	 * <br>
	 * Maintains 3 UPTBs: a current best, a best within each iteration, and a
	 * current one being considered. The current best starts with the current
	 * adjacency list, and its score it the current best score. As long as improvements
	 * in score are made, sets the best-overall to the best-in-last-iteration,then
	 * finds all nearest neighbors to the current best (for all internal edges). Once
	 * done, prints out the best tree's adjacency list.
	 * @param good differentiaties from public method
	 */
	private UnrootedParsiTreeBuilder buildTree(boolean good) {
		// the minimum score starts at the maximum value
		int minScore = Integer.MAX_VALUE;
		// the best tree structure starts as the current adjacency list
		UnrootedParsiTreeBuilder bestTree = new UnrootedParsiTreeBuilder(alClone(), lmClone());
		// build this tree up
		bestTree.buildTree();
		// get its score
		int score = bestTree.getScore();
		// the new-best tree starts as the best (and only) tree so far
		UnrootedParsiTreeBuilder newTree = bestTree;
		
		// while score keeps being improved
		while (score < minScore) {
			// set minScore, bestTree to the current best ones
			minScore = score;
			bestTree = newTree;
			// set the best adjacency list to the best tree's adjacency list
			adjList = bestTree.getAdjList();
			
			// loop over all internal edges
			for (int[] edge : getAllInternalEdges()) {
				// the is not the second neighbor yet
				boolean second = false;
				// loop for each neighbor
				for (int i = 0; i < 2; i++) {
					// shift the adjacency list to the next neighbor
					nextNeighbor(edge[0], edge[1], second);
					// initialize a tree with the new adjacency list
					UnrootedParsiTreeBuilder curTree =
							new UnrootedParsiTreeBuilder(alClone(), lmClone());
					// build the current tree up
					curTree.buildTree();
					
					// save its score
					int newScore = curTree.getScore();
					// if the score beats the best so far
					if (newScore < score) {
						// set it, and the current tree, as the best so far
						score = newScore;
						newTree = curTree;
					}
					
					// shift back the adjacency list from the neighbor
					revert(edge[0], edge[1]);
					// now looking for the second neighbor
					second = true;
				}
			}
		}
		
		return bestTree;
	}
	
	/**
	 * Builds an optimal tree with the leaves given
	 * <br>
	 * Runs buildTree many times, saving each time if better than the last
	 */
	public void buildTree() {
		// get an initial tree
		UnrootedParsiTreeBuilder best = buildTree(true);
		
		// loop many times
		for (int i = 0; i < 25; i++) {
			// print out a counter to indicate progress
			System.out.println(i);
			// restructure tree
			randomTreeStructure();
			// run buildTree again
			UnrootedParsiTreeBuilder cur = buildTree(true);
			// if the score beats current best, set best tree to this one
			if (cur.getScore() < best.getScore()) best = cur;
		}
		
		// write the best tree's adjacency list
		best.writeAdjList("src/phylogeny/output.txt");
	}
}