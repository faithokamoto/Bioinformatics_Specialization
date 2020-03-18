package alignment;

// for data structures
import java.util.HashMap;
import java.util.HashSet;

/**
 * <h1>Node on a Directed Weighted Graph</h1>
 * The basic data structure of a DWG, knowing its maximal weight, the backtrack to get there, and all the paths
 * leading in (and all the nodes leading out). Basically a contructor with getters and setters.
 * @author faith
 */
public class Node {
	// the name and maximal weight of this node
	private int name, weight;
	// the optimal node to backtrack to
	private Node backtrack;
	// the incoming paths (node, value of path)
	private HashMap<Node, Integer> ins;
	// the outgoing nodes
	private HashSet<Node> outs;
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes all instance variables to defaults except name, which accepts a value.
	 * @param name the name of this node
	 */
	public Node(int name) {
		// set name
		this.name = name;
		// all other variables have not been calculated yet
		weight = 0;
		backtrack = null;
		ins = new HashMap<Node, Integer>();
		outs = new HashSet<Node>();
	}
	
	// getters
	
	public int getName() {return name;}

	public int getWeight() {return weight;}

	public Node getBacktrack() {return backtrack;}
	
	public HashMap<Node, Integer> getIns() {return ins;}
	
	public HashSet<Node> getOuts() {return outs;}

	// setters
	
	public void setWeight(int weight) {this.weight = weight;}

	public void setBacktrack(Node backtrack) {this.backtrack = backtrack;}

	// ins and outs cannot be directly set, only added to
	
	public void addIn(Node in, Integer val) {this.ins.put(in, val);}
	
	public void addOut(Node out) {this.outs.add(out);}
	
	/**
	 * <h1>Overriden toString</h1>
	 * Gives information about the node and all of its incoming/outgoing nodes
	 */
	@Override
	public String toString() {
		// information about just this node
		String ret = "\nNode " + name + " has weight " + weight + ", and backtracks " + backtrack;
		
		// information about incoming nodes
		ret += "\nIt has incoming nodes (node, path value): ";
		for (Node in : ins.keySet()) ret += "(" + in.getName() + ", " + ins.get(in) + ") ";
		
		// information about outgoing nodes
		ret += "\nIt has outgoing nodes: ";
		for (Node out : outs) ret += out.getName() + " ";
		
		return ret;
	}
}
