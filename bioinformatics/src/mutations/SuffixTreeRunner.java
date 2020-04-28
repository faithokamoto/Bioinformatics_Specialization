package mutations;

// for reading from files
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Basic runner for the SuffixTree class
 * @author faith
 */
public class SuffixTreeRunner {
	/**
	 * Currently, reads in two Strings and finds the shortest unique sequence from the first
	 * @param args not used
	 * @throws FileNotFoundException if there is no data file
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// point a scanner at the file
		Scanner reader = new Scanner(new File("src/mutations/data.txt"));
		// read in both strings
		SuffixTree run = new SuffixTree(reader.next(), reader.next());
		// clean up
		reader.close();
		
		// for debugging purposes
		//run.writeAdjList("src/mutations/output.txt");
		// print out the shortest unique substring from the first
		System.out.println(run.shortestUnique());
	}
}