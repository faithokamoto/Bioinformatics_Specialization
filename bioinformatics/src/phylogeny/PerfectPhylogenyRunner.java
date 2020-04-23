package phylogeny;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * Runner for PerfectPhylogeny
 * @author faith
 */
public class PerfectPhylogenyRunner {
	/**
	 * Reads an SNP matrix into a 2-D array
	 * @param filename the file to read the matrix from
	 * @return the SNP matrix as a 2-D array
	 */
	public static boolean[][] readSNPs(String filename) {
		// initialize return variable
		boolean[][] snps = null;
		// try to read from the file
		try {
			// point a Scanner at the file
			Scanner reader = new Scanner(new File(filename));
			// read the dimensions of the matrix
			int numSNPs = reader.nextInt();
			int numSamples = reader.nextInt();
			
			// initialize the matrix accordingly
			snps = new boolean[numSamples][numSNPs];
			// loop over all rows and columns
			for (int i = 0; i < numSamples; i++) for (int j = 0; j < numSNPs; j++) {
				// read the next number
				int val = reader.nextInt();
				// a 1 mean true, a 0 means false
				if (val == 1) snps[i][j] = true;
				else if (val == 0) snps[i][j] = false;
				
				// anything else means an error
				else {
					reader.close();
					throw new IllegalArgumentException("Matrix can only have 1 and 0");
				}
			}
			
			// clean up
			reader.close();
		}
		// if something weird happened, print it
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return snps;
	}
	
	/**
	 * Runs the PerfectPhylogeny code
	 * @param args not used
	 */
	public static void main(String[] args) {
		PerfectPhylogeny run = new PerfectPhylogeny(readSNPs("src/phylogeny/data.txt"));
		run.buildTree();
		run.printSNPs();
		System.out.println();
		run.printVectorMap();
		run.writeAdjList("src/phylogeny/output.txt", 0);
	}
}