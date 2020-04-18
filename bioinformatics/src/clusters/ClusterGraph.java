package clusters;

/**
 * A graph that can cluster points using some algorithm
 * @author faith
 */
public abstract class ClusterGraph {
	/**
	 * the points that must be clustered,
	 * guaranteed to never be null or empty (or have null/empty inner arrays)
	 */
	private float[][] points;
	/**
	 * the centers of the clusters,
	 * guaranteed to be the same dimension as points and not empty if non-null
	 */
	private float[][] centers;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes the points of the graph
	 * @param points the points to cluster
	 */
	public ClusterGraph(float[][] points) {
		// validate these points
		validatePoints(points);
		
		this.points = points;
	}

	/**
	 * An algorithm for clustering points and choosing centers
	 * <br>
	 * To be implemented by subclasses
	 * @param num the number of clusters to create
	 */
	public abstract void cluster(int num);
	
	/**
	 * Validates some points for acceptability
	 * @param points the points to validate
	 */
	protected static void validatePoints(float[][] points) {
		// first check is there are points at all
		if (points == null || points.length == 0)
			throw new IllegalArgumentException("No points");
		// check that the first point exists
		if (points[0] == null || points[0].length == 0)
			throw new IllegalArgumentException("Points must have coordinates");
		
		// then loop through all points
		for (int i = 0; i < points.length; i++) {
			// check that this point exists
			if (points[i] == null)
				throw new IllegalArgumentException("Points must have coordinates");
			// check that this point has the dimensions of a reference point
			if (points[i].length != points[0].length)
				throw new IllegalArgumentException("Points must have same dimensions");
		}
	}
	
	/**
	 * Calculates the distance between two points
	 * @param a one points
	 * @param b another point
	 * @return the distance
	 */
	protected static double dist(float[] a, float[] b) {
		// check if the points are in the same dimension
		if (a.length != b.length)
			throw new IllegalArgumentException("Points not in the same dimension");
		
		// initialize return variable
		double dist = 0;
		// add the squared distance for each dimension
		for (int i = 0; i < a.length; i++)
			dist += (a[i] - b[i]) * (a[i] - b[i]);
		
		// square-root the total
		return Math.sqrt(dist);
	}
	
	/**
	 * Calculates the minimum distance from a point to a center
	 * @param point the point to find distance from
	 * @param curCenters the number of centers that can be used
	 * @return the minimum distance from point to an allowed center
	 */
	protected double minDist(float[] point, int curCenters) {
		// make sure the number of centers is acceptable
		if (noCenters() || curCenters <= 0)
			throw new IllegalArgumentException("No centers to find distance with");
		if (curCenters > centers.length)
			throw new IllegalArgumentException("Cannot use more centers than exist");
		// ensure point has acceptable dimensions
		if (point.length != centers[0].length)
			throw new IllegalArgumentException("Point is not in the same dimension as centers");
		
		// initialize return variable to distance to the first center
		double minDist = dist(point, centers[0]);
		// loop through all other centers
		for (int i = 1; i < curCenters; i++) {
			// calculate distance for this center
			double curDist = dist(point, centers[i]);
			// if it beats the current minimum, use it instead
			if (curDist < minDist) minDist = curDist;
		}
		
		return minDist;
	}
	
	/**
	 * Calculates the minimum distance from a point to a center
	 * <br>
	 * Calls minDist and uses the maximum number of centers
	 * @param point the point to calculate distance from
	 * @return the minimum distance from the point to any center
	 */
	protected double minDist(float[] point) {
		return minDist(point, numCenters());
	}
	
	/**
	 * Calculates the distortion between points and centers
	 * @return the value of the distortion for this set of centers on these points
	 */
	public double distortion() {
		// check that there are centers
		if (noCenters())
			throw new IllegalStateException("No centers to find distortion from");
		
		// initialize return variable
		double tort = 0.0;
		// loop through all points
		for (float[] point : points)
			// square the minimum distance from this point to a center
			tort += Math.pow(minDist(point), 2);
		
		// adjust distortion for number of points
		return tort / points.length;
	}
	
	// getters
	
	/**
	 * Getter for the number of points
	 * @return the length of the points array
	 */
	public int numPoints() {return points.length;}
	
	/**
	 * Getter for the number of centers
	 * @return the length of the centers array
	 */
	public int numCenters() {return centers.length;}
	
	/**
	 * Create a fresh array of centers
	 * @param num the number of centers to create
	 */
	public void newCenters(int num) {
		// check that the number of centers is valid
		if (points.length < num)
			throw new IllegalArgumentException("Can't have more cluster centers than points");
		if (num <= 0)
			throw new IllegalArgumentException("Have to have some centers");
		
		centers = new float[num][points[0].length];
	}
	
	/**
	 * Check is there are no centers
	 * @return whether the center array is null
	 */
	public boolean noCenters() {return centers == null;}
	
	/**
	 * Getter for a certain center
	 * @param num the index of the center to get it
	 * @return a copy of the given center's coordinate array
	 */
	public float[] getCenter(int num) {
		// ensure there are centers to get
		if (noCenters())
			throw new IllegalStateException("Centers do not exist");
		// ensure that the center to get is valid
		if (num >= numCenters())
			throw new IllegalArgumentException("Invalid center # (>= centers.length)");
		if (num < 0)
			throw new IllegalArgumentException("Invalid center # (< 0)");
		
		return centers[num].clone();
	}
	
	/**
	 * Getter for a certain point
	 * @param num the index of the point to get it
	 * @return a copy of the given point's coordinate array
	 */
	public float[] getPoint(int num) {
		// ensure the point to get is valid
		if (num >= points.length)
			throw new IllegalArgumentException("Invalid point # (>= points.length)");
		if (num < 0)
			throw new IllegalArgumentException("Invalid point # (< 0)");
		
		return points[num].clone();
	}
	
	/**
	 * Getter for a copy of the centers array
	 * @return a deep-copied center array at this point
	 */
	public float[][] centerCopy() {
		// check that there are centers to copy
		if (noCenters())
			throw new IllegalStateException("Centers do not exist");
		
		// initialize return variable
		float[][] copy = new float[centers.length][centers[0].length];
		// copy each center into array
		for (int i = 0; i < centers.length; i++)
			copy[i] = centers[i].clone();
		
		return copy;
	}
	
	// setter
	
	/**
	 * Setter for a certain center
	 * @param num the index of the center to set
	 * @param point the point to use as a new center
	 */
	public void setCenter(int num, float[] point) {
		if (noCenters())
			throw new IllegalArgumentException("Centers have not been initialized");
		if (num >= centers.length)
			throw new IllegalArgumentException("Invalid point # (>= centers.length)");
		if (num < 0)
			throw new IllegalArgumentException("Invalid point # (< 0)");
		if (point.length != points[0].length)
			throw new IllegalArgumentException("Center cannot be in different dimension than the points");
		
		centers[num] = point;
	}
	
	/**
	 * Prints out the centers
	 */
	public void printCenters() {
		// check that there are centers to print
		if (noCenters())
			throw new IllegalStateException("Centers do not exist");
		
		// loop over all centers
		for (float[] point : centers) {
			// loop over all coordinates values
			for (float coord : point)
				// print out coordinate with 3 decimal places
				System.out.println(String.format("%.3f", coord) + " ");
			// newline before next line
			System.out.println();
		}
	}
}