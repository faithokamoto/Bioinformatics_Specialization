package clusters;

// for arrays that can change size
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * A simple runner for the ClusterGraphs
 * @author faith
 */
public class GraphRunner {
	/**
	 * the number of clusters to make
	 */
	public static int k;
	
	/**
	 * Reads points from a file
	 * @param filename
	 * @return the points in a float[#points][#coords]
	 */
	public static float[][] readPoints(String filename) {
		// initialize return variable
		float[][] points = null;
		// try to read from the file
		try {
			// point a Scanner at the file
			Scanner reader = new Scanner(new File(filename));
			// initialize an arraylist of points read
			ArrayList<float[]> pointsList = new ArrayList<float[]>();
			
			// read how many clusters to make
			k = reader.nextInt();
			// read the dimensions of the points
			int m = reader.nextInt();
			
			// while there are more points to read
			while (reader.hasNext()) {
				// initialize a point
				float[] newPoint = new float[m];
				// for each coordinate required
				for (int i = 0; i < m; i++) {
					// skip all non-float tags
					while (!reader.hasNextFloat() && reader.hasNext()) reader.next();
					// read in the coordinate
					newPoint[i] = reader.nextFloat();
				}
				
				// add the new point in
				pointsList.add(newPoint);
			}
			
			// clean up
			reader.close();
			// initialize a 2-D array for the points
			points = new float[pointsList.size()][m];
			// convert the arraylist into a 2-D array
			for (int i = 0; i < points.length; i++)
				points[i] = pointsList.get(i);
		}
		// if something fishy occurs, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
		return points;
	}
	
	// write method to read in the data into some arrays of points and centers
	
	public static void main(String[] args) {
	}
}