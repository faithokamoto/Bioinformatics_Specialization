package phylogeny;

// for various data structures
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;

/**
 * Represents a node in a phylogenetic tree. Has an ID# (for reference), a Map
 * of paths with weights (distances) between it and other Nodes, and methods that
 * deal with those paths. Paths can be added or edited, and Nodes can find the
 * minimum distance between themselves and other Nodes, as well as paths between
 * Nodes.
 * @author faith
 *
 */
public class Node {
	/**
	 * the ID# of this Node, used to refer to each other
	 */
	private int id;
	private double age;
	/**
	 * all paths leading out of this Node, by Node and weight
	 */
	private HashMap<Node, Double> paths;
	/**
	 * stores Node ID#s already visited for the use of some methods
	 */
	private static HashSet<Integer> visited;
	/**
	 * the maximal path length for any Node
	 */
	private static double maxPath = 0;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes paths and id
	 * @param id the ID# of this Node
	 */
	public Node(int id) {
		this.id = id;
		paths = new HashMap<Node, Double>();
	}
	
	/**
	 * Age Constructor
	 * <br>
	 * Initializes id, age, and path
	 * @param id the ID# of this Node
	 * @param age the age of this Node
	 */
	public Node(int id, double age) {
		this(id);
		this.age = age;
	}
	
	/**
	 * Resets visited for one of the methods that needs it
	 */
	private static void resetVisited() {
		visited = new HashSet<Integer>();
	}
	
	/**
	 * Adds a path to paths, updating maxPath if necessary
	 * @param end the Node on the other end of this path
	 * @param weight the weight of the new path
	 * @throws IllegalArgumentException if weight is negative
	 */
	public void addPath(Node end, double weight) {
		// check if weight was illegal
		if (weight < 0)
			throw new IllegalArgumentException("Path from " + id + "->" + end.getId() + " can't have"
				+ " negative weight of " + weight);
		paths.put(end, weight);
		// update maxPath if necessary
		if (weight > maxPath) maxPath = weight;
	}
	
	/**
	 * Edits an existing path to a new weight and end
	 * <br>
	 * Removes the old path and adds a new one
	 * @param oldEnd the Node at the end of the old path
	 * @param newEnd the Node at the end of the new path
	 * @param newWeight the new weight of the path
	 */
	public void editPath(Node oldEnd, Node newEnd, int newWeight) {
		paths.remove(oldEnd);
		addPath(newEnd, newWeight);
	}

	/**
	 * Calculates the distance from this Node to another
	 * <br>
	 * Recursive with base cases of the node looked for being the current Node
	 * or on a path from the current node. If not, then loops over all adjacent Nodes
	 * and minimizes their distance from the target Node
	 * @param node the Node to calculate distance from
	 * @param good used to differentiate this method from the public one
	 * @return the minimum distance from this Node to the target Node
	 */
	private double getDistFromNode(Node node, boolean good) {
		// check if the Node being looked for is this one, dist of 0 if so
		if (node == this) return 0;
		// check if this Node has a path to the looked-for Node, dist is weight if so
		if (paths.containsKey(node)) return getWeight(node);
		
		// note that this Node has been visited
		visited.add(this.id);
		// set the minimum distance to the max minus the max path (to avoid going over)
		double min = Double.MAX_VALUE - maxPath;
		
		// loop over all adjacent nodes
		for (Node next : getAdjNodes())
			// as long as this node hasn't been visited yet
			if (!visited.contains(next.getId()))
				// check if its distance + dist to get there is better than current min
				min = Math.min(min, next.getDistFromNode(node, true) + paths.get(next));
			
		return min;
	}
	
	/**
	 * Calculates the distance from this Node to another
	 * <br>
	 * Resets visited and calls getDistFromNode(Node, boolean)
	 * @param node the Node to calculate distance from
	 * @return the minimum distance from this Node to the target Node
	 */
	public double getDistFromNode(Node node) {
		resetVisited();
		return getDistFromNode(node, true);
	}
		
	/**
	 * Finds a path between this Node and a passed-in one
	 * <br>
	 * Maintaining a list of all paths under consideration (which begins with just the current Node),
	 * iteratively pulls one path, adds on all adjacent Nodes, checking each time if the path has
	 * reached its destination. If so creates the proper array and returns it.
	 * @param node the Node to find a path to
	 * @param good used to differentiate this method from the public one
	 * @throws runtimeException if no path could be found
	 * @return an int array with ID#s on the path, including endpoints, in order
	 */
	private int[] pathTo(Node node, boolean good) {
		// a path to itself is just this Node's ID#
		if (this == node) return new int[] {this.id};
		
		// initialize a holder for all paths being considered
		ArrayList<ArrayList<Node>> possiblePaths = new ArrayList<ArrayList<Node>>(paths.size());
		// the paths start with just this Node
		possiblePaths.add(new ArrayList<Node>());
		possiblePaths.get(0).add(this);
		// only the current node has been visited
		resetVisited();
		visited.add(this.getId());
		
		// while some paths are under consideration
		while (possiblePaths.size() != 0) {
			// loop over the paths from the back (to allow for removal)
			for (int i = possiblePaths.size() - 1; i >= 0; i--) {
				// grab the current path
				ArrayList<Node> curPath = possiblePaths.remove(i);
				
				// loop over all of the adjacent Nodes to the last Node in this path
				for (Node out : curPath.get(curPath.size() - 1).getAdjNodes()) {
					// as long as this Node has not yet been visited
					if (!visited.contains(out.getId())) {
						// add it to the current path
						curPath.add(out);
						
						// if it is the Node that is being looked for
						if (out == node) {
							// get an array with the ID#s in order and return it
							int[] path = new int[curPath.size()];
							for (int j = 0; j < path.length; j++) path[j] = curPath.get(j).getId();
							
							return path;
						}
						
						// otherwise, add it back in
						possiblePaths.add(new ArrayList<Node>(curPath));
						// reset curPath
						curPath.remove(curPath.size() - 1);
						// note that this Node has been visited
						visited.add(out.getId());
					}
				}
			}
		}
		
		// the return statement should have triggered by now
		throw new RuntimeException("No path could be found connecting the Nodes " + id + " and " + node.getId());
	}
	
	/**
	 * Finds a path between this Node and a passed-in one
	 * <br>
	 * Resets visited and calls pathTo(Node, boolean)
	 * @param node
	 * @return
	 */
	public int[] pathTo(Node node) {
		resetVisited();
		return pathTo(node, true);
	}
	
	/**
	 * Gets the weight of the path connecting this Node and another
	 * @param node the Node on the other end of the connecting path
	 * @throws IllegalArgumentException if this Node doesn't have a path to the passed-in one
	 * @return the weight of the path
	 */
	public double getWeight(Node node) {
		if (paths.containsKey(node)) return paths.get(node);
		throw new IllegalArgumentException("This Node (#" + this.id + ") has no path to " + node.getId());
	}
	
	/**
	 * Gets the weight of the path connecting this Node and another
	 * @param id the ID# of the Node on the other end of the connecting path
	 * @throws IllegalArgumentException if this Node doesn't have a path to the passed-in one
	 * @return the weight of the path
	 */
	public double getWeight(int id) {
		for (Node n : paths.keySet()) if (n.getId() == id) return paths.get(n);
		throw new IllegalArgumentException("This Node (#" + this.id + ") has no path to " + id);
	}
	
	public void setAge(double age) {this.age = age;}
	
	// getters
	
	public int getId() {return this.id;}
	
	public Set<Node> getAdjNodes() {return this.paths.keySet();}
	
	public double getAge() {return this.age;}
	
	public String toString() {
		String ret = "#" + id + ": ";
		for (Node curNode : getAdjNodes()) {
			ret += "(" + curNode.getId() + ", " + getWeight(curNode) + ") ";
		}
		return ret;
	}
}