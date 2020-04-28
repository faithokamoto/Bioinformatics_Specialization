package mutations;

// for arrays that can change size
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * Basic runner for the Trie class
 * @author faith
 */
public class TrieRunner {
	/**
	 * Reads all strings from a file
	 * @param filename the file to read from
	 * @return the Strings in the file
	 */
	public static ArrayList<String> readPatterns(String filename) {
		// initialize return variable
		ArrayList<String> patterns = new ArrayList<String>();
		// try to read from the file
		try {
			// point a reader at the file
			Scanner reader = new Scanner(new File(filename));
			// as long there is a string left, read it in
			while (reader.hasNext()) patterns.add(reader.next());
			// clean up
			reader.close();
		}
		// if that didn't work, print out what happened
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return patterns;
	}
	
	/**
	 * Currently finds all matches between patterns and a genome
	 * @param args not used
	 */
	public static void main(String[] args) {
		// get all the patterns
		ArrayList<String> patterns = readPatterns("src/mutations/data.txt");
		// save the first one as the genome
		String text = patterns.remove(0);
		// initialize the Trie
		Trie run = new Trie(patterns);
		// run findMatches, and print out all indexes
		for (Integer match : run.findMatches(text)) System.out.print(match + " ");
	}
}