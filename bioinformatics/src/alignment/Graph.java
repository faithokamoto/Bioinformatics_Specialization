package alignment;

// for data structures
import java.util.HashMap;
import java.util.HashSet;

/**
 * <h1>Directed Weight Graph</h1>
 * Stores a directed weighted graph in the form of Nodes which each know their directions and path weights,
 * and has the necessary methods to initialize those nodes from String data and find an optimal Path through
 * itself. One instance of this class can only change its nodes through initializeNodes(), called in the constructor.
 * @author faith
 */
public class Graph {
	// the best-weighted path through this graph
	private Path dag;
	// the starting and end nodes of this graph
	private Node source, sink;
	
	/**
	 * This class should never be initialized without a information,
	 * what's a Graph bereft of Nodes/Paths?
	 */
	@SuppressWarnings("unused")
	private Graph() {}
	
	/**
	 * <h1>Constructor for subclasses</h1>
	 * Allows for specialized Paths to be used.
	 * @param pathType the specialized Path
	 */
	public Graph(Path pathType) {
		dag = pathType;
	}
	
	/**
	 * <h1>Constructor</h1>
	 * Initializes all instance variables
	 * @param data a string of properly formatted paths
	 * @param source the starting node
	 * @param sink the ending node
	 */
	public Graph(String data, int source, int sink) {
		// initialize the Path
		dag = new Path();
		// initialize the nodes
		initializeNodes(data, source, sink);
	}
	
	/**
	 * <h1>Initializes the Nodes of this graph given proper data to do so</h1>
	 * Using a lookup table to ensure that no node is created more than once, loops through
	 * all given paths in data, creating nodes and adding incoming and outgoing paths.
	 * @param data a string of properly formatted paths
	 * @param source the starting node
	 * @param sink the ending node
	 */
	public void initializeNodes(String data, int source, int sink) {
		// used to make sure only one node of each name is created
		HashMap<Integer, Node> refer = new HashMap<Integer, Node>();
		
		// clean up the data, split it into individual paths
		data = data.trim();
		String[] paths = data.split("\n");
		
		// placeholders for the loop
		int start, end, val;
		// loop over all paths in data
		for (String path : paths) {
			// pull the start node, end node, and value of this path
			start = Integer.parseInt(path.split("-")[0]);
			end = Integer.parseInt(path.split(">")[1].split(":")[0]);
			val = Integer.parseInt(path.split(":")[1]);
			
			// if the start node hasn't yet been created, initialize it
			if (!refer.containsKey(start)) refer.put(start, new Node(start));
			
			// same for the end node
			if (!refer.containsKey(end)) refer.put(end, new Node(end));
			
			// add the end node as one of the start node's outgoing nodes
			refer.get(start).addOut(refer.get(end));
			// add the start node as one of the end node's incoming nodes
			refer.get(end).addIn(refer.get(start), val);
			
			// check if the nodes should be source/sink
			if (start == source) this.source = refer.get(start);
			else if (end == sink) this.sink = refer.get(end);
		}
	}

	/**
	 * <h1>Finds, for each node, the maximal weight and the backtrack to get there</h1>
	 * Starting with the source Node, loop over all Nodes just considered, loop over all of their outgoing Nodes,
	 * and loop over (for each outgoing Node) all of the incoming paths, and find which one gives the best weight
	 * if followed. Saves that weight as the Node's weight, and the incoming Node as the outgoing Node's backtrack,
	 * then repeats the whole process with all nodes that have just been calculated (moves from nodes one out from
	 * source, to nodes 2 out from source, and so on).
	 */
	public void calculateNodes() {
		// set of Nodes to look at this time through the loop
		HashSet<Node> considering = new HashSet<Node>();
		// at first, only consider source
		considering.add(source);
		
		// while there are some nodes that are being considered
		while(!considering.isEmpty()) {
			// initialized a Set of Nodes that have already been calculated this time around
			HashSet<Node> justCalculated = new HashSet<Node>();
			
			// loop over all Nodes in considering as starting nodes
			for (Node start : considering) {
				// loop over all of this Node's outgoing Nodes
				for (Node end : start.getOuts()) {
					// as long as this Node hasn't been calculated yet
					if (!justCalculated.contains(end)) {
						maximizeNode(end);
						
						// this node has just been calculated
						justCalculated.add(end);
					}
				}
			}
			
			// clear the considering list (already been considered)
			considering.clear();
			// add the nodes just calculated (expand out the network)
			considering.addAll(justCalculated);
		}
	}
	
	/**
	 * <h1>Finds the ideal path to use to maximize the weight of a Node</h1>
	 * First makes sure that there are paths to check, then loops over all paths
	 * to maximize weight, setting end's weight and backtrack in the process.
	 * @param end the Node to maximize weight
	 */
	public void maximizeNode(Node end) {
		// as long as this node has some paths in
		if (end.getIns().size() != 0) {
			// initialize weight to min
			end.setWeight(Integer.MIN_VALUE);
			
			// loop over all start nodes of incoming paths
			for (Node start : end.getIns().keySet()) {
				// if using this path is better than the current weight
				if (start.getWeight() + end.getIns().get(start) > end.getWeight()) {
					// use it
					end.setBacktrack(start);
					end.setWeight(start.getWeight() + end.getIns().get(start));
				}
			}
		}
	}

	/**
	 * <h1>For outside-this-class users, give the path</h1>
	 * First calculates weight and backtrack for all nodes, then finds the path by backtracking and prints it.
	 */
	public void findPath() {
		calculateNodes();
		dag.backtrack(source, sink);
		// for convenience
		System.out.println(dag);
	}

	// getters
	
	public Node getSource() {return source;}
	
	public Node getSink() {return sink;}
	
	// setters
	
	public void setSource(Node source) {this.source = source;}
	
	public void setSink(Node sink) {this.sink = sink;}
}