package phylogeny;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * Basic Runner for TreeBuilder
 * @author faith
 */
public class TreeBuilderRunner {
	/**
	 * Reads a distance matrix from a file
	 * @param filename the file to read from
	 * @return the completed distance matrix
	 */
	public static int[][] readDistMatrix(String filename) {
		// initialize the return variable
		int[][] distMatrix = null;
		// attempt to point a scanner at a file
		try {
			Scanner reader = new Scanner(new File(filename));
			// read the dimensions of the matrix
			int n = reader.nextInt();
			// initialize to those dimensions
			distMatrix = new int[n][n];
			
			// read all ints into the proper place in the matrix
			for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) 
					distMatrix[i][j] = reader.nextInt();
			// clean up
			reader.close();
		}
		// if something weird happened
		catch (Exception e) {
			// print it out
			e.printStackTrace();
		}
		
		return distMatrix;
	}
	
	/**
	 * Makes a TreeBuilder and runs it
	 * @param args not used
	 */
	public static void main(String args[]) {
		// initialize the TreeBuilder with a distance matrix
		TreeBuilder run = new TreeBuilder(readDistMatrix("src/phylogeny/data.txt"));
		// build the tree
		run.buildTree();
		// print the tree's adjacency list
		run.writeAdjList("src/phylogeny/output.txt");
	}
}
