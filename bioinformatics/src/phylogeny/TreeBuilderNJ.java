package phylogeny;


/**
 * A specialized TreeBuilder with an overridden buildTree() that uses the Neighbor Joining algorithm.
 * Also has a special, resizable DistanceMatrix
 * @see phylogeny.AdditiveTreeBuilder
 * @author faith
 */
public class TreeBuilderNJ extends TreeBuilder {
	/**
	 * a resizeable distance matrix
	 */
	private DistanceMatrixNJ dists;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes the distance matrix and Tree
	 * @param dists a distance matrix array
	 */
	public TreeBuilderNJ(double[][] dists) {
		super(dists.length);
		this.dists = new DistanceMatrixNJ(dists);
	}
	
	/**
	 * Builds the tree with the Neighbor Joining algorithm
	 */
	public void buildTree() {
		// if only 2 leaves are left
		if (dists.size() == 2) {
			// get those nodes
			int[] nodes = dists.getNodes();
			// add a path between them
			getTree().addNodeWithPath(nodes[0], nodes[1], dists.get(nodes[0], nodes[1]));
		}
		
		// otherwise
		else {
			// grab the closest nodes
			int[] closest = dists.closest();
			// calculate limb length for both
			double tri = (dists.totalDist(closest[0]) - dists.totalDist(closest[1])) / (dists.size() - 2),
					limb1 = 0.5 * (dists.get(closest[0], closest[1]) + tri),
					limb2 = 0.5 * (dists.get(closest[0], closest[1]) - tri);
			// merge those nodes together in the distance matrix
			dists.merge(closest[0], closest[1]);
			// calculate the ID# of the node that connects the two nodes
			int newId = dists.getMaxNode();
			
			// build the tree up to this point
			buildTree();
			// using the previously calculated values, add limbs for the closest nodes
			getTree().addPath(newId, closest[0], limb1);
			getTree().addPath(newId, closest[1], limb2);
		}
	}
}