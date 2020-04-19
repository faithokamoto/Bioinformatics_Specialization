package clusters;

// for reusing UPGMA code
import phylogeny.Node;
import phylogeny.TreeBuilder;

/**
 * A clustering algorithm that builds an UPGMA tree
 * @author faith
 */
public class TreeClustering extends TreeBuilder {
	/**
	 * a resizable distance matrix
	 */
	private DistanceMatrixTracker dists;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes the distance matrix and Tree
	 * @param dists a distance matrix
	 */
	public TreeClustering(double[][] dists) {
		super(dists.length);
		// use special distance matrix that is a tracker
		this.dists = new DistanceMatrixTracker(dists);
	}
	
	/**
	 * Builds a tree with the UPGMA algorithm
	 * <br>
	 * While more than one active node exists, merges the two closest nodes
	 * and adds paths as necessary
	 */
	@Override
	public void buildTree() {
		// add all of the leaf nodes in with age 0
		for (int i = 0; i < dists.size(); i++) getTree().addNode(i, 0);
		
		// while more than one cluster remains
		while (dists.size() > 1) {
			// find the closest nodes
			int[] closest = dists.closest();
			// calculate the ID# of the new node to add
			int newId = dists.getMaxNode() + 1;
			// create this new node, with the proper age
			Node newNode = getTree().addNode(newId, dists.get(closest[0], closest[1]) / 2);
			
			// add paths to the down-nodes based on age difference
			getTree().addPath(newId, closest[0],
					getTree().getAge(newNode) - getTree().getAge(closest[0]));
			getTree().addPath(newId, closest[1],
					getTree().getAge(newNode) - getTree().getAge(closest[1]));
			// merge the closest nodes together in the distance matrix
			dists.merge(closest[0], closest[1]);
		}
	}
}