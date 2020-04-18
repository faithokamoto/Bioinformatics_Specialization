package clusters;

// for arrays that can change size
import java.util.ArrayList;

/**
 * A clustering graph that uses the Lloyd algorithm
 * @author faith
 */
public class LloydGraph extends ClusterGraph {
	/**
	 * the clusters created,
	 * guaranteed to never be null
	 */
	private ArrayList<ArrayList<float[]>> clusters;
	
	/**
	 * Constructor
	 * @param points the points to cluster
	 */
	public LloydGraph(float[][] points) {
		// use superclass constructor
		super(points);
		// initialize clusters
		clusters = new ArrayList<ArrayList<float[]>>();
	}
	
	/**
	 * Finds the closest center to a point
	 * @param point the point to use
	 * @return the index of the closest center
	 */
	private int closestCenter(float[] point) {
		// check that the point exists
		if (point == null)
			throw new IllegalArgumentException("Point is null");
		// check that centers to find distance from exist
		if (noCenters())
			throw new IllegalStateException("Centers do not exist");
		// check dimensions of point
		if (point.length != getCenter(0).length)
			throw new IllegalArgumentException("Point has differnt dimensions than centers");
		
		// initialize return variable to first center
		int closestCenter = 0;
		// initialize distance as to first center
		double minDist = dist(point, getCenter(0));
		
		// loop through all centers after first
		for (int i = 1; i < numCenters(); i++) {
			// calculate distance to this center
			double curDist = dist(point, getCenter(i));
			// if it beats the current minimum
			if (curDist < minDist) {
				// set this center as the closest, use its distance for minimum
				minDist = curDist;
				closestCenter = i;
			}
		}
		
		return closestCenter;
	}
	
	/**
	 * Finds the center of gravity of a cluster
	 * @param cluster the cluster to find the center of gravity of
	 * @return a point at the center of gravity
	 */
	private static float[] centerOfGravity(ArrayList<float[]> cluster) {
		// check that the cluster is not null or empty
		if (cluster == null || cluster.size() == 0)
			throw new IllegalArgumentException("Cannot calculate with empty cluster");
		// check that the first point in cluster is not null
		if (cluster.get(0) == null || cluster.get(0).length == 0)
			throw new IllegalArgumentException("Cannot have empty points in cluster");
		
		// initialize array for conversion of cluster into 2-D array
		float[][] validate = new float[cluster.size()][cluster.get(0).length];
		// add each array in cluster into the 2-D array
		for (int i = 0; i < cluster.size(); i++) validate[i] = cluster.get(i);
		// validate the points in the cluster
		validatePoints(validate);

		// initialize return variable
		float[] center = new float[cluster.get(0).length];
		// loop through all points in the cluster
		for (float[] point : cluster)
			// add up the points' coordinates
			for (int i = 0; i < point.length; i++) center[i] += point[i];
		
		// adjust each coordinate for the number of points added in
		for (int i = 0; i < center.length; i++) center[i] /= cluster.size();
		
		return center;
	}
	
	/**
	 * Check if two points are close by 0.001
	 * @param a one point
	 * @param b another point
	 * @return if the points differ by less than 0.001
	 */
	private static boolean closeByThous(float[] a, float[] b) {
		// check that the points exist
		if (a == null || b == null)
			throw new IllegalArgumentException("Can't look at null points");
		// check that the points are in the same dimensions
		if (a.length != a.length)
			throw new IllegalArgumentException("Points in differnt dimensions");
		
		// check each coordinate for closeness
		for (int i = 0; i < a.length; i++)
			if (Math.abs(a[i] - b[i]) > 0.001) return false;
		
		return true;
	}

	/**
	 * Check if two sets of points differ by less than 0.001
	 * @param a one set of points
	 * @param b another set of points
	 * @return whether the two sets contains points which are close by 0.001
	 */
	private static boolean closeByThous(float[][] a, float[][] b) {
		// make sure both sets of points are valid by themselves
		validatePoints(a);
		validatePoints(b);
		// check that the point arrays are the same number
		if (a.length != b.length)
			throw new IllegalArgumentException("Different number of points");
		// check that the points are in the same dimension
		if (a[0].length != b[0].length)
			throw new IllegalArgumentException("Points in differnt dimensions");
		
		// initialize a set of unmatched points from b
		ArrayList<float[]> unused = new ArrayList<float[]>(b.length);
		for (float[] point : b) unused.add(point.clone());
		
		// loop over all points in a
		for (float[] point : a) {
			// initially, no close match has been found
			boolean close = false;
			
			// loop over all unmatched points
			for (int i = 0; i < unused.size(); i++)
				// if the unmatched point is close to the current point
				if (closeByThous(point, unused.get(i))) {
					// remove the unmatched point since it has a match
					unused.remove(i);
					// note that a match was found
					close = true;
					// stop looking
					break;
				}
			
			// if no match was found, not that
			if (!close) return false;
		}
		
		// if all points had matches, the sets are close
		return true;
	}
	
	/**
	 * Initializes the centers and clusters
	 * @param num the number of clusters to create
	 */
	private void initializeClusters(int num) {
		// initialize the centers array to the right size
		newCenters(num);
		// initialize clusters arraylist to the right size
		clusters = new ArrayList<ArrayList<float[]>>(num);
		// use the first point as the first center
		setCenter(0, getPoint(0));
		// add in the first cluster
		clusters.add(new ArrayList<float[]>());
		
		// loop over all other clusters that must be made
		for (int i = 1; i < num; i++) {
			// initialize the probability array for the points
			float[] probs = new float[numPoints()];
			// the total probability starts at 0
			double total = 0.0;
			
			// loop over each point
			for (int j = 0; j < numPoints(); j++) {
				// probability of a point is the minimum distance to a center^2
				probs[j] = (float) Math.pow(minDist(getPoint(j), i), 2);
				// add this probability to the total probability
				total += probs[j];
			}
			
			// assume that the first point is being used
			int point = 0;
			// the subtotal probability so far is 0
			double subtotal = 0.0;
			// get a random number 0 <= rand < total
			double rand = Math.random() * total;
			// while the subtotal is less than the random number
			while (subtotal < rand) {
				// add the current probability to the subtotal
				subtotal += probs[point];
				// move the point being considered forward
				point++;
			}
			// set a random point as the center
			setCenter(i, getPoint(point - 1));
			// add in a cluster for this point
			clusters.add(new ArrayList<float[]>());
		}
	}
	
	/**
	 * Cluster the points using the Lloyd algorithm
	 */
	public void cluster(int num) {
		// initialize the clusters
		initializeClusters(num);
		// storage for the old centers
		float[][] oldCenters;
		
		do {
			// get the current centers
			oldCenters = centerCopy();
			// clear the old clusters
			for (int i = 0; i < clusters.size(); i++) clusters.get(i).clear();
			
			// loop over all points
			for (int i = 0; i < numPoints(); i++)
				// add this point to its closest center's cluster
				clusters.get(closestCenter(getPoint(i))).add(getPoint(i));
			
			// loop over all clusters
			for (int i = 0; i < clusters.size(); i++)
				// set each center's cluster to its center of gravity
				setCenter(i, centerOfGravity(clusters.get(i)));
		}
		// keep going if there were significant adjustments
		while (!closeByThous(oldCenters, centerCopy()));
	}
}