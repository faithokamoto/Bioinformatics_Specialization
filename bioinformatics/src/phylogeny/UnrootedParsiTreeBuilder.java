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
public class UnrootedParsiTreeBuilder extends TreeBuilder{
	/**
	 * the TreeBuilder which actually builds
	 */
	private SmallParsiTreeBuilder builder;
	/**
	 * the nodes ID#s on the edge split to add a root
	 */
	private int[] splitEdge;
	/**
	 * whether the adjacency list has been rooted
	 */
	private boolean rooted;
	
	/**
	 * Constructor
	 * <br>
	 * Roots the tree passed in, then initializes the SPTB reference
	 * @param adjList the (undirected, unrooted) adjacency list of this tree
	 * @param labelMap a mapping of all leaves to labels
	 */
	public UnrootedParsiTreeBuilder(HashMap<Integer, ArrayList<Integer>> adjList,
			HashMap<Integer, char[]> labelMap) {
		super(labelMap.size());
		rooted = false;
		rootTree(adjList, adjList.size());
		builder = new SmallParsiTreeBuilder(adjList, labelMap);
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
		// initialize splitEdge to the highest node & a connection split by new node
		splitEdge = new int[] {rootID - 1, rootID, adjList.get(rootID - 1).get(0)};
		
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
				int node = processing.get(i);
				// loop over all nodes coming out of the current one
				for (int j = adjList.get(node).size() - 1; j >= 0; j--) {
					int out = adjList.get(node).get(j);
					// if this out has nodes coming out of it
					if (adjList.containsKey(out)) {
						// remove path from out-node back to current node
						adjList.get(out).remove((Integer) node);
						// add out-node to the processing queue
						processing.add(out);
					}
				}
				
				// this node has been processed, get rid of it
				processing.remove((Integer) node);
			}
		}
		
		// the tree is now rooted
		rooted = true;
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
		rooted = false;
	}
	
	/**
	 * Builds the tree using the builder's Small Parsimony
	 */
	public void buildTree() {
		builder.buildTree();
	}
	
	/**
	 * Writes the adjacency list of this tree to a file
	 * <br>
	 * Unroots the tree's adjacency list, then uses the builder's method
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		if (rooted) unrootTree();
		builder.writeAdjList(filename);
	}
	
	/**
	 * Converts the adjacency list into a unrooted, undirected one
	 * <br>
	 * Unroots the tree (if necessary), then copies all paths in an opposite way
	 * @return this tree's unrooted, undirected adjacency list
	 */
	public HashMap<Integer, ArrayList<Integer>> getAdjList() {
		// check if tree needs to be unrooted
		if (rooted) unrootTree();
		
		// loop over all starts of a path
		for (Integer start : builder.getAdjList().keySet())
			// loop over all ends
			for (Integer end : builder.getPaths(start))
				// add end->start as a path if is doesn't exist yet
				if (!builder.getPaths(end).contains(start))
					builder.addPath(end, start);
			
		// tree is no longer rooted
		rooted = false;
		
		return builder.getAdjList();
	}
	
	/**
	 * Getter for score of this tree
	 * @return builder.getScore()
	 */
	public int getScore() {return builder.getScore();}
}