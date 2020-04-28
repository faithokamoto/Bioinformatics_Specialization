package mutations;

// for data structures
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A generalized node which holds directed paths.
 * <br>
 * Provides methods to add paths, get all paths, get the number of paths,
 * access the Node at the end of a given path, and check if a certain path exists
 * @author faith
 * @param <L> the kind of labels that paths will use
 * @param <N> the type of Node at then end of paths, should always be a subclass of the subclass
 */
public class Node<L, N extends Node<L, N>> {
	/**
	 * the ID# of this Node, used for identification
	 */
	private final int id;
	/**
	 * all paths coming out of this Node, accessible by their label
	 */
	private final HashMap<L, N> paths;
	
	/**
	 * Initializes ID# and paths (to empty)
	 * @param id the ID# of this Node
	 */
	public Node(int id) {
		this.id = id;
		paths = new HashMap<L, N>();
	}
	
	/**
	 * Adds a path with the specified label which ends at the specified Node.
	 * DOES NOT add a path if one with the same label already exists
	 * @param label the label of this path
	 * @param node the Node at the end of the path
	 * @return whether the path was actually added
	 */
	public boolean addPath(L label, N node) {return paths.putIfAbsent(label, node) == null;}
	
	/**
	 * Checks for the existence of a path with a certain label
	 * @param label the label to check for
	 * @return whether a path with this label exists for this Node
	 */
	public boolean hasPath(L label) {return paths.containsKey(label);}
	
	/**
	 * Gets the Node at the end of a path with a specified label
	 * @param label the label to use as access
	 * @return the Node associated with this label
	 */
	public N pathEnd(L label) {return paths.get(label);}
	
	/**
	 * Gets all labels for all paths coming out of this Node
	 * @return all the keys in the paths HashSet
	 */
	public ArrayList<L> pathLabels() {return new ArrayList<L>(paths.keySet());}
	
	/**
	 * Gets all Nodes at the end of all paths coming out of this Node.
	 * CAN NOT be used to access the corresponding labels
	 * @return all the values in the paths HashSet
	 */
	public ArrayList<N> pathEnds() {return new ArrayList<N>(paths.values());}
	
	/**
	 * Replaces the label of a path, keeping the same Node at the end
	 * @param oldLabel the old label to replace
	 * @param newLabel the new label to have on the path
	 */
	public void replacePath(L oldLabel, L newLabel) {paths.put(newLabel, paths.remove(oldLabel));}
	
	/**
	 * Gets the number of paths coming out of this Node
	 * @return the size of the paths HashSet
	 */
	public int numPaths() {return paths.size();}
	
	/**
	 * Gets the ID# of this Node
	 * @return the value of this.id
	 */
	public int getID() {return id;}
	
	public String toString() {
		// start return string with ID#
		String ret = "#" + id + ", paths out=";
		// for each path out, add label and end ID#
		for (L label : pathLabels()) ret += "(" + label + "=" + pathEnd(label).getID() + ")";
		
		return ret;
	}
}