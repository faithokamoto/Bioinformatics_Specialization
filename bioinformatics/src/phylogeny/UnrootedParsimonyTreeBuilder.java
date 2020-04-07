package phylogeny;

// for various data structures
import java.util.HashMap;
import java.util.ArrayList;

/**
 * A helper to Parsimony Tree Builder, able to transform unrooted trees into rooted ones
 * and then run Small Parsimony on it. Contains the same public methods as Parsimony
 * Tree Builder (one of which is a reference), but runs background methods to supplement
 * @author faith
 */
public class UnrootedParsimonyTreeBuilder {
	/**
	 * the TreeBuilder which actually builds
	 */
	private ParsimonyTreeBuilder builder;
	/**
	 * the nodes ID#s on the edge split to add a root
	 */
	private int[] splitEdge;
	
	/**
	 * Constructor
	 * <br>
	 * Roots the tree passed in, then initializes the PTB reference
	 * @param adjList the (undirected, unrooted) adjacency list of this tree
	 * @param labelMap a mapping of (at least) all leaves to labels
	 */
	public UnrootedParsimonyTreeBuilder(HashMap<Integer, ArrayList<Integer>> adjList,
			HashMap<Integer, char[]> labelMap) {
		rootTree(adjList, labelMap.size() + adjList.size());
		builder = new ParsimonyTreeBuilder(adjList, labelMap);
	}
	
	/**
	 * Roots the tree and makes all edges directional
	 * <br>
	 * Finds the nodes on either edge of the edge to split, then removes the edge between
	 * them and adds edges to the new root node. Moving interactively out from these edge 
	 * nodes, deletes all edges flowing backwards until the leaves are reached.
	 * @param adjList the (undirected, unrooted) adjacency list of this tree
	 * @param rootID the ID of the root node to add
	 */
	private void rootTree(HashMap<Integer, ArrayList<Integer>> adjList, int rootID) {
		// initialize splitEdge to the 2 highest nodes split by the new highest node
		splitEdge = new int[] {rootID - 1, rootID, rootID - 2};
		
		// remove the old edge from the adjacency list
		adjList.get(splitEdge[0]).remove((Integer) splitEdge[2]);
		adjList.get(splitEdge[2]).remove((Integer) splitEdge[0]);
		// add the new edges to the adjacency lsit
		adjList.put(splitEdge[1], new ArrayList<Integer>(2));
		adjList.get(splitEdge[1]).add(splitEdge[0]);
		adjList.get(splitEdge[1]).add(splitEdge[2]);
		
		// initialize the nodes being processed to just the ones on the edge
		ArrayList<Integer> processing = new ArrayList<Integer>();
		processing.add(splitEdge[0]);
		processing.add(splitEdge[2]);
		// while some nodes still need to be processed
		while (!processing.isEmpty()) {
			// loop over processing from the back (to make removal easier)
			for (int i = processing.size() - 1; i >= 0; i--) {
				// loop over all nodes coming out of the current one
				for (Integer out : adjList.get(processing.get(i))) {
					// if this out has nodes coming out of it
					if (adjList.containsKey(out)) {
						// remove path from out-node back to current node
						adjList.get(out).remove(processing.get(i));
						// add out-node to the processing queue
						processing.add(out);
					}
				}
				
				// this node has been processed, get rid of it
				processing.remove(i);
			}
		}
	}
	
	/**
	 * Unroots the tree
	 * <br>
	 * Deletes the edges added to the root and adds the removed edge back in
	 */
	private void unrootTree() {
		builder.removePath(splitEdge[1], splitEdge[0]);
		builder.removePath(splitEdge[1], splitEdge[2]);
		builder.addPath(splitEdge[0], splitEdge[2]);
	}
	
	/**
	 * Builds the tree using the builder's Small Parsimony
	 */
	public void buildTree() {
		builder.buildTree();
	}
	
	/**
	 * Writes the adjaceny list of this tree to a file
	 * <br>
	 * Unroots the tree's adjacency list, then uses the builder's method
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		unrootTree();
		builder.writeAdjList(filename);
	}
}