package alignment;

// for data structures
import java.util.ArrayList;

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
	
	// setter

	public void setWeight(int weight) {this.weight = weight;}
	
	// nodes cannot be directly set, only added to from front

	public void addNode(Node node) {this.nodes.add(0, node);}
	
	/**
	 * <h1>Overriden toString</h1>
	 * Gives information about the path - weight and what it is
	 */
	@Override
	public String toString() {
		// information about weight
		String ret = weight + "\n";
		// information about sequence of nodes
		for (Node node : nodes) ret += node.getName() + "->";
		// delete the last ->
		return ret.substring(0, ret.length() - 2);
	}
}
