package clusters;

/**
 * A clustering graph that uses the K-means algorithm
 * @author faith
 */
public class FFTGraph extends ClusterGraph {
	/**
	 * Constructor
	 * @param points the points to cluster
	 */
	public FFTGraph(float[][] points) {
		super(points);
	}
	
	/**
	 * Finds the farthest point from available centers
	 * @param curCenters the number of centers to use
	 * @return the farthest point available from any center
	 */
	private float[] farthestPoint(int curCenters) {
		// check that there are centers to use
		if (noCenters() || curCenters <= 0)
			throw new IllegalArgumentException("No centers to calculate distance with");
		// check that no more than the maximum number of centers are used
		if (curCenters > numCenters())
			throw new IllegalArgumentException("Invalid curCenters (>= centers.length)");
			
		// initialize the farthest point to the first one
		float[] farthestPoint = getPoint(0);
		// initialize maximum distance to this point's minimum distance
		double maxDist = minDist(farthestPoint, curCenters);
		
		// loop through all points after the first
		for (int i = 1; i < numPoints(); i++) {
			// save the current point
			float[] curPoint = getPoint(i);
			// calculate the current point's distance
			double curDist = minDist(curPoint, curCenters);
			// if it beats the current maximum distance
			if (curDist > maxDist) {
				// use it as the farthest point and its distance as the maximum
				maxDist = curDist;
				farthestPoint = curPoint;
			}
		}
		
		return farthestPoint;
	}
	
	/**
	 * Clusters points using the K-means algorithm
	 */
	public void cluster(int num) {
		// check that the number of clusters is valid
		if (num <= 0)
			throw new IllegalArgumentException("Have to make a some clusters");
		if (num > numPoints())
			throw new IllegalArgumentException("Can't make more clusters than points");
		// initialize the centers array to the proper size
		newCenters(num);
		// set the first center to the first point
		setCenter(0, getPoint(0));
		// there is currently one center
		int curCenter = 1;
		
		// while more centers are needed
		while (curCenter < num) {
			// use the farthest point as the next center
			setCenter(curCenter, farthestPoint(curCenter));
			// there is one more center
			curCenter++;
		}
	}
}