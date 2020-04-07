package phylogeny;

// for reading from files
import java.util.Scanner;
import java.io.File;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

// for arrays that change size
import java.util.ArrayList;

/**
 * Represents a phylogenetic tree, and can be initialized by nodes or not. Contains methods
 * to add nodes, add paths between nodes, add a node it the middle of a path, print
 * an adjacency list, get a distance matrix, and much more.
 * @author faith
 */
public class Tree {
	/**
	 * the dimensions of the distance matrix/the number of leaves present
	 */
	private int n;
	/**
	 * the highest node ID# in this tree
	 */
	private int highestNode;
	/**
	 * all of the nodes in this tree
	 */
	private ArrayList<Node> nodes;
	
	/**
	 * Blank constructor
	 * <br>
	 * Initializes nodes to empty, n to the passed-in value,
	 * and highestNode to the last leaf (n - 1)
	 * @param n the number of leaves this tree will have
	 */
	public Tree(int n) {
		this(n, new ArrayList<Node>());
		highestNode = n - 1;
	}
	
	/**
	 * Full constructor
	 * @param n the number of leaves this tree has
	 * @param nodes the nodes of this tree
	 */
	public Tree(int n, ArrayList<Node> nodes) {
		this.n = n;
		this.nodes = nodes;
	}
	
	/**
	 * File constructor
	 * <br>
	 * Initializes nodes and n from a file
	 * @param nodeFile a file with node information
	 */
	public Tree(String nodeFile) {
		// initialize nodes from this file
		initializeNodes(nodeFile);
	}
	
	/**
	 * Initializes nodes and n from a data file
	 * <br>
	 * Initializes nodes to an empty list, reads the number of leaves, then loops
	 * until no paths are left to read. Adds each path in (and end nodes, if necessary)
	 * @param dataFile the file to read node information from
	 */
	public void initializeNodes(String dataFile){
		// initialize nodes to empty
		nodes = new ArrayList<Node>();
		try {
			// open a Scanner on the file
			Scanner reader = new Scanner(new File(dataFile));
			// grab the number of leaves
			n = reader.nextInt();
			// move on
			reader.nextLine();
			
			// while paths are left to read
			while (reader.hasNextLine()) {
				// read in the next path
				String path = reader.nextLine();
				// grab the start node, end node, and weight of the path
				int start = Integer.parseInt(path.split("->")[0]);
				int end = Integer.parseInt(path.split("->")[1].split(":")[0]);
				int val = Integer.parseInt(path.split(":")[1]);
				
				// add the nodes (if necessary), add the path
				if (getNode(start) == null) addNodeWithPath(start, end, val);
				else if (getNode(end) == null) addNodeWithPath(end, start, val);
				else addPath(start, end, val);
			}
			// clean up
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the distance matrix of this Tree
	 * <br>
	 * Loops over all node pairs, calculating distance and setting the
	 * corresponding spots in the matrix
	 * @return a 0-indexed distance matrix for all leaves
	 */
	public double[][] getDistanceMatrix() {
		// initialize return variable
		double[][] dists = new double[n][n];
		
		// loop over all node pairs
		for (int from = 0; from < n; from++) for (int to = from + 1; to < n; to++) {
			// calculate the distance between these nodes
			double dist = nodes.get(from).getDistFromNode(nodes.get(to));
			// set the proper places in the matrix
			dists[from][to] = dist;
			dists[to][from] = dist;
		}
		
		return dists;
	}
	
	/**
	 * Insertion sort of Nodes by ID# in an ArrayList
	 * @param list the ArrayList to sort
	 */
	public void sortNodes(ArrayList<Node> list) {
		// loop over all unsorted Nodes
		for (int i = 1; i < list.size(); i++) {
			// grab current Node
			Node curNode = list.remove(i);
			// save the index before it
			int addIndex = i - 1;
			// while the Node before should come after, move index forward
			while (addIndex >= 0 && curNode.getId() < list.get(addIndex).getId()) addIndex--;
			// add Node back into the list at the correct index
			list.add(addIndex + 1, curNode);
		}
	}
	
	/**
	 * Writes the adjacency list of this Tree to a file
	 * <br>
	 * Points a writer at the file, then loops over all Nodes.
	 * For each Node, sorts the list of all adjacent Nodes and
	 * prints out each corresponding path.
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		// try to look at the file
		try {
			// point a writer at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			// initialize a placeholder ArrayList (so only 1 is created)
			ArrayList<Node> adj = new ArrayList<Node>();
			
			// loop over all Nodes
			for (int i = 0; i <= highestNode; i++) {
				// grab the current Node
				Node curNode = getNode(i);
				if (curNode.getAdjNodes() != null) {
					// grab its adjacent Nodes and sort by ID#
					adj.clear();
					adj.addAll(curNode.getAdjNodes());
					sortNodes(adj);
					
					// write each path corresponding to an adjacent Node
					for (Node out : adj) 
						writer.write(curNode.getId() + "->" + out.getId() + ":" + 
								String.format("%.3f", curNode.getWeight(out)) + "\n");
				}
			}
			
			writer.close();
		}
		// if that failed, print why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new Node to the master list
	 * <br>
	 * Calls addNode(int) and sets its age
	 * @param id the id# of the new node
	 * @param age the age of the new node
	 * @returns the Node that was added
	 */
	public Node addNode(int id, double age) {
		Node newNode = addNode(id);
		newNode.setAge(age);
		
		return newNode;
	}
	
	/**
	 * Adds a new Node to the master list
	 * <br>
	 * Calls addNode(int) and sets its age
	 * @param id the id# of the new node
	 * @param age the age of the new node
	 * @returns the Node that was added
	 */
	public Node addNode(int id, String label) {
		Node newNode = addNode(id);
		newNode.setLabel(label);
		
		return newNode;
	}
	
	/**
	 * Adds a new Node to the master list
	 * <br>
	 * Initializes a new Node with the specified ID#
	 * and age, updating highestNode if required
	 * @param id the id# of the new node
	 * @throws IllegalArgumentException if a Node with the specified ID already exists
	 * @returns the Node that was added
	 */
	public Node addNode(int id) {
		// check if a node with this ID exists
		if (getNode(id) != null)
			throw new IllegalArgumentException("Node with id#" + id + " already exists");
		Node newNode = new Node(id);
		nodes.add(newNode);
		// check if highestNode needs to be updated
		if (id > highestNode) highestNode = id;
		return newNode;
	}
	
	/**
	 * Attempts to grab a Node by ID
	 * <br>
	 * Loops through all Nodes, checking their ID#s
	 * @param id the ID# of the Node to get
	 * @return a Node that ID#, or null if none exist
	 */
	public Node getNode(int id) {
		for (Node n : nodes) if (n.getId() == id) return n;
		return null;
	}
	
	/**
	 * Adds a path between two Nodes
	 * @param start one Node on the path
	 * @param end the other Node on the path
	 * @param val the weight of the path
	 */
	public void addPath(Node start, Node end, double val) {
		start.addPath(end, val);
		end.addPath(start, val);
	}
	
	/**
	 * Adds a path between two Nodes
	 * @param start the ID# of one Node
	 * @param end the ID# of the other Node
	 * @param val the weight of the path
	 */
	public void addPath(int start, int end, double val) {
		if (getNode(end) == null) addNode(end);
		addPath(getNode(start), getNode(end), val);
	}
	
	/**
	 * Adds a new Node along with a path
	 * <br>
	 * Adds the Node, and the other end if necessary, and then the path
	 * @param newId the ID# of the new Node to add
	 * @param endId the ID# of the Node at the other of the path
	 * @param val the weight of the new path
	 * @return the Node added
	 */
	public Node addNodeWithPath(int newId, int endId, double val) {
		// add the Node
		Node newNode = addNode(newId);
		// check if the other Node must be added as well
		if (getNode(endId) == null) addNode(endId);
		// add the path
		addPath(newId, endId, val);
		
		return newNode;
	}
	
	/**
	 * Splits a path in the middle with a new Node
	 * <br>
	 * Deletes that path between the end node and start node and adds new paths
	 * between each and a new, middle, node.
	 * @param start the node on one end of the path
	 * @param middle the ID of the new node in the middle of the path
	 * @param end the node at the other end of the path
	 * @param startPath the weight of the new path between start and middle
	 * @param endPath the weight of the new path between end and middle
	 * @throws IllegalArgumentException if the new path values don't add up to the old one
	 * (between start and end)
	 */
	public void splitPath(Node start, int middle, Node end, int startPath, int endPath) {
		// check for valid path values
		if (startPath + endPath != start.getWeight(end))
			throw new IllegalArgumentException("New paths don't add up to the old one ("
					+ startPath + " + " + endPath + " != " + start.getWeight(end));
		// adds the paths to the node
		Node mid = addNodeWithPath(middle, start.getId(), startPath);
		mid.addPath(end, endPath);
		// edit the paths for start and end
		start.editPath(end, mid, startPath);
		end.editPath(start, mid, endPath);
	}
	
	/**
	 * Splits a path in the middle with a new Node
	 * <br>
	 * Converts start and end ID#s into Nodes, then calls splitPath(Node, int, Node, int, int)
	 * @param start the ID of the node on one end of the path
	 * @param middle the ID of the new node in the middle of the path
	 * @param end the ID of the node at the other end of the path
	 * @param startPath the weight of the new path between start and middle
	 * @param endPath the weight of the new path between end and middle
	 */
	public void splitPath(int start, int middle, int end, int startPath, int endPath) {
		splitPath(getNode(start), middle, getNode(end), startPath, endPath);
	}
	
	// getters
	
	public double getAge(Node node) {return node.getAge();}
	
	public double getAge(int id) {return getAge(getNode(id));}
	
	public int getN() {return n;}
	
	public int getHighestNode() {return highestNode;}
}