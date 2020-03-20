package alignment;

// for data structures
import java.util.ArrayList;
import java.util.Collections;

/**
 * <h1>Path through a Graph</h1>
 * Fairly basic data structure about a path - its weight and its nodes in order
 * @author faith
 */
public class Path {
	// weight of this path
	private int weight;
	// nodes of this path in order
	private ArrayList<Node> nodes;
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes instance variables
	 */
	public Path() {
		weight = 0;
		nodes = new ArrayList<Node>();
	}
	
	/**
	 * <h1>Backtracks from the sink node to reconstruct the path</h1>
	 * Starting at the sink node, adds a Node and then backtracks to the next node.
	 * Once the source node is reached, add it and leave.
	 * @param source the source node (beginning of Path)
	 * @param sink the sink node (end of Path)
	 */
	public void backtrack(Node source, Node sink) {
		// start at the sink node
		Node curNode = sink;
		// weight is the sink node's weight
		weight = curNode.getWeight();
		
		// while not yet reaching source
		while (curNode != source) {
			// add the current node
			nodes.add(curNode);
			// backtrack curNode from the current node
			curNode = curNode.getBacktrack();
		}
		
		// finish by adding the source node & reversing
		nodes.add(source);
		Collections.reverse(nodes);
	}
	
	// setter

	public void setWeight(int weight) {this.weight = weight;}
	
	// nodes should node be changed directly, but starting bits can be removed
	
	public void removeNode() {this.nodes.remove(0);}
	
	// getters
	
	public int getWeight() {return this.weight;}
	
	public ArrayList<Node> getNodes() {return this.nodes;}
	
	/**
	 * <h1>Overridden toString</h1>
	 * Builds a String to return with the weight of the path and each Node's ID in order, with arrows in between.
	 * @return information about the path - weight and what it is
	 */
	@Override
	public String toString() {
		// information about weight
		String ret = weight + "\n";
		// information about sequence of nodes
		for (Node node : nodes) ret += node.getID() + "->";
		// delete the last ->
		return ret.substring(0, ret.length() - 2);
	}
}
