package mutations;

// for arrays that can change size
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * Runs code from AlignmentHMM or AlignmentHMMBuilder classes
 * @author faith
 *
 */
public class AlignmentHMMRunner {
	/**
	 * the string to align
	 */
	public static char[] align;
	/**
	 * the threshold for column removal/pseudocount to add to all probabilities
	 */
	public static double threshold, pseudo;
	/**
	 * the possible string chars
	 */
	public static char[] alph;
	/**
	 * the strings to align against
	 */
	public static char[][] alignment;
	
	/**
	 * Read data into public static variables
	 * @param filename the file to read from
	 */
	public static void readData(String filename) {
		// try to read data
		try {
			// point a Scanner at the file
			Scanner reader = new Scanner(new File(filename));
			
			// read the string to align
			align = reader.next().toCharArray();
			// move to next item
			reader.nextLine();
			reader.nextLine();
			
			// read the column removal threshold
			threshold = reader.nextDouble();
			// read the probability pseudocount
			pseudo = reader.nextDouble();
			// move to next item
			reader.nextLine();
			reader.nextLine();
			
			// read the letters as strings
			String[] alphStr = reader.nextLine().replace("\t", " ").split(" ");
			// initialize char array to right size
			alph = new char[alphStr.length];
			// copy each 1-char string into the char array
			for (int i = 0; i < alph.length; ++i) alph[i] = alphStr[i].charAt(0);
			// move to next item
			reader.nextLine();
			
			// hold an unknown number of strings in an alignment
			ArrayList<char[]> strings = new ArrayList<char[]>();
			// as long as there are strings, add them
			while (reader.hasNext()) strings.add(reader.next().toCharArray());
			// clean up
			reader.close();
			// initialize alignment array to right size
			alignment = new char[strings.size()][strings.get(0).length];
			// copy each string from ArrayList into array
			for (int i = 0; i < strings.size(); i++) alignment[i] = strings.get(i);
		}
		// if something happens, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs some Alignment code, right now finding the Viterbi path of an HMM build from data
	 * @param args not used
	 */
	public static void main(String[] args) {
		// read data
		readData("src/mutations/data.txt");
		// print out the path produced by building an HMM from the alignment and then running the string through it
		System.out.println(AlignmentHMMBuilder.build(threshold, alph, alignment, pseudo).viterbiPath(align));
	}
}