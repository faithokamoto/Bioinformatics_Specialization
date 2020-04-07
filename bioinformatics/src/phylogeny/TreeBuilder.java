package phylogeny;

/**
 * Helper class that can build up a Tree from a distance matrix. Maintains a reference to the Tree,
 * and must implement buildTree() to build the Tree using some algorithm
 * @author faith
 */
public abstract class TreeBuilder {
	/**
	 * the Tree being built up
	 */
	private Tree myTree;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes myTree
	 * @param n the number of leaves in the eventual tree
	 */
	public TreeBuilder(int n) {
		myTree = new Tree(n);
	}
	
	/**
	 * Builds the tree with some algorithm
	 */
	public abstract void buildTree();
	
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