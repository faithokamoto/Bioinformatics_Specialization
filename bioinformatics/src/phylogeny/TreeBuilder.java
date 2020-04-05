package phylogeny;

/**
 * Helper class that can build up a Tree from a distance matrix. Maintains a reference to the Tree,
 * as well as the distance matrix (both initialized in constructor). Public methods are to build up
 * the whole tree, or find the limb length of a leaf (perhaps on only a selection of leaves)
 * @author faith
 */
public class TreeBuilder {
	/**
	 * the Tree being built up
	 */
	private Tree myTree;
	/**
	 * the distance matrix being used for this Tree
	 */
	private int[][] dists;
	/**
	 * the last i and j Nodes used in limbLength
	 */
	private static int lastI, lastJ;
	
	/**
	 * N-Constructor
	 * <br>
	 * For subclasses that have their own distance matrices,
	 * and just need myTree initialized with n
	 * @param n the number of leaves in the eventual tree
	 */
	public TreeBuilder(int n) {
		myTree = new Tree(n);
	}
	
	/**
	 * Constructor
	 * <br>
	 * Initializes dists and myTree
	 * @param distMatrix a distance matrix
	 */
	public TreeBuilder(int[][] dists) {
		this.dists = dists;
		myTree = new Tree(dists.length);
	}
	
	/**
	 * Calculates the length of a leaf's limb using leaves up to maxLeaf
	 * <br>
	 * Loop over all leaf-pairs, calculating the limb length using them and
	 * updating values if it beats the current minimum.
	 * @param leaf the leaf to find a limb length of
	 * @param maxLeaf the maximum leaf value to use
	 * @return the minimum limb length using given part of the distance matrix
	 */
	public int limbLength(int leaf, int maxLeaf) {
		// initialize the minimum limb to the maximum value
		int minLength = Integer.MAX_VALUE;
		
		// loop over all leaf pairs
		for (int i = 0; i <= maxLeaf; i++) for (int j = i + 1; j <= maxLeaf; j++)
			// as long as the current leaf pair doesn't include the leaf under consideration
			if (leaf != i && leaf != j) {
				// calculate the limb length using this pair
				int curDist = (dists[i][leaf] + dists[j][leaf] - dists[i][j]) / 2;
				// if it's the best so far
				if (curDist < minLength) {
					// set minLength, lastI, and lastJ accordingly
					minLength = curDist;
					lastI = i;
					lastJ = j;
				}
			}
		
		return minLength;
	}
	
	/**
	 * Calculates the length of a leaf's limb using all leaves
	 * <br>
	 * Calls limbLength(int, int) with the maximum leaf
	 * @param leaf the leaf to find a limb length of
	 * @return the minimum limb length
	 */
	public int limbLength(int leaf) {
		return limbLength(leaf, dists.length - 1);
	}

	/**
	 * Adds a new Node to a Tree in a manner consistent with the distance matrix
	 * <br>
	 * Calculates the effective limb length, the length from start to newLeaf, and the path between
	 * the start and end Nodes. Looping over the path gives the open and close nodes (edges of
	 * the connection to split) and the distance after the close node, or a perfect Node to connect
	 * to without splitting a path. Then calculates all new path distances, splits the path,
	 * and adds the new leaf onto the new connecting Node
	 * @param newLeaf the ID# of the leaf Node to be added
	 * @param start the ID# of the optimal path-to-join's start Node w
	 * @param end the ID# of the end Node of ^^
	 */
	private void addPathAtDistance(int newLeaf, int start, int end) {
		int[] path = myTree.getNode(start).pathTo(myTree.getNode(end));
		// calculate the effective limb length for this leaf from the given path
		int effectiveLimb = (dists[start][newLeaf] + dists[end][newLeaf] - dists[start][end]) / 2;
		// calculate the length from start to the beginning of the limb
		int restLength = dists[start][newLeaf] - effectiveLimb;
		
		// by default, the path being split is the last connection in path
		int open = path[path.length - 2], close = path[path.length - 1];
		// distance traveled so far on the path, distance traveled after the close node on the path
		int distSoFar = 0, afterDist = 0;
		// loop over all connections in path
		for (int i = 0; i < path.length - 1; i++) {
			// if restLength has already been reached
			if (distSoFar > restLength)
				// add on to afterDist
				afterDist += myTree.getNode(path[i]).getWeight(path[i + 1]);
			// otherwise
			else {
				// add this connection to distSoFar
				distSoFar += myTree.getNode(path[i]).getWeight(path[i + 1]);
				
				// if a Node with the exact distance is found
				if (distSoFar == restLength) {
					// initialize the new Node with a path to perfect-node, and book it
					myTree.addNodeWithPath(newLeaf, path[i], dists[start][newLeaf] - restLength);
					break;
				}
				// or it distSoFar has overshot for the first time
				else if (distSoFar > restLength && afterDist == 0) {
					// set open an close to the ends of this connection
					open = myTree.getNode(path[i]).getId();
					close = myTree.getNode(path[i + 1]).getId();
				}
			}
		}
		
		// if a path needs to be split (perfect Node not found)
		if (myTree.getNode(newLeaf) == null) {
			// calculate the ID# of the internal Node that will be added
			int newId = myTree.getHighestNode() + 1;
			// calculate the length of the path from the new Node to the close Node
			int closePath = dists[end][newLeaf] - effectiveLimb - afterDist;
			// calculate the length of the path from the new Node to the open Node
			int openPath = (int) myTree.getNode(open).getWeight(close) - closePath;
			
			// split the path
			myTree.splitPath(open, newId, close, openPath, closePath);
			// add the new leaf node on
			myTree.addNodeWithPath(newLeaf, newId, effectiveLimb);
		}
	}
	
	/**
	 * Builds the tree using all leaves up to maxLeaf
	 * <br>
	 * Recursive with base condition of only two leaves left. Otherwise
	 * build the tree with one less leaves, then adds on a leaf to it
	 * @param maxLeaf the maximum leaf to consider
	 */
	private void buildTree(int maxLeaf) {
		// if only 2 leaves are in this tree, just add a path between them
		if (maxLeaf == 1) myTree.addNodeWithPath(0, 1, dists[0][1]);
		
		// otherwise
		else {
			// build the tree up to this point
			buildTree(maxLeaf - 1);
			
			// run limbLength to get lastI and lastJ
			limbLength(maxLeaf, maxLeaf);
			// add the current leaf into the tree
			addPathAtDistance(maxLeaf, lastI, lastJ);
		}
	}
	
	/**
	 * Builds the tree by running buildTree(int) on all leaves
	 */
	public void buildTree() {
		buildTree(dists.length - 1);
	}
	
	/**
	 * Writes the adjacency list of the Tree to a file
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		myTree.writeAdjList(filename);
	}
	
	// getter
	
	public Tree getTree() {return myTree;}
}