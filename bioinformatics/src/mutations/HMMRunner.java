package mutations;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * Runs some code from the HMM or HMMBuilder classes
 * @author faith
 */
public class HMMRunner {
	/**
	 * the number of iterations BW should run for
	 */
	public static int iterations;
	/**
	 * the emitted outcome/alphabet of emitted symbols
	 */
	public static char[] outcome, alph;
	/**
	 * the possible hidden states
	 */
	public static Character[] states;
	/**
	 * transition/emission matrices
	 */
	public static double[][] trans, emiss;
	
	/**
	 * Reads in data from a file, storing in public static variables
	 * @param filename the file to read from
	 */
	public static void readData(String filename) {
		// try to read data
		try {
			// point a Scanner at the file
			Scanner reader = new Scanner(new File(filename));
			
			// read the number of iterations
			iterations = reader.nextInt();
			// move to next item
			reader.nextLine();
			reader.nextLine();
			
			// read the emitted outcome
			outcome = reader.nextLine().toCharArray();
			// move to next item
			reader.nextLine();
			
			// read the alphabet of emitted symbols as strings
			String[] alphStr = reader.nextLine().replace('\t', ' ').split(" ");
			// initialize char array to proper length
			alph = new char[alphStr.length];
			// copy the 1-char strings into the char array
			for (int i = 0; i < alph.length; ++i) alph[i] = alphStr[i].charAt(0);
			// move to next item
			reader.nextLine();
			
			// read the possible hidden states as strings
			String[] statesStr = reader.nextLine().replace('\t', ' ').split(" ");
			// initialize char array to proper length
			states = new Character[statesStr.length];
			// copy the 1-char strings into the char array
			for (int i = 0; i < states.length; ++i) states[i] = statesStr[i].charAt(0);
			// move to next item
			reader.nextLine();
			reader.nextLine();
			
			// initialize transition matrix
			trans = new double[states.length][states.length];
			// loop over all from-states
			for (int i = 0; i < states.length; ++i) {
				// ignore the label
				reader.next();
				// loop over all to-states
				for (int j = 0; j < states.length; ++j)
					// read in the probability
					trans[i][j] = reader.nextDouble();
			}
			// move to next item
			reader.nextLine();
			reader.nextLine();
			reader.nextLine();
			
			// initialize emission matrix
			emiss = new double[states.length][alph.length];
			// loop over all states
			for (int i = 0; i < states.length; ++i) {
				// ignore the label
				reader.next();
				// loop over all possible emissions
				for (int j = 0; j < alph.length; ++j)
					// read in the probability
					emiss[i][j] = reader.nextDouble();
			}
			// clean up
			reader.close();
		}
		// if something happens, print it out
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs some test code, currently the BW learning algorithm
	 * @param args not used
	 */
	public static void main(String[] args) {
		// read in data
		readData("src/mutations/data.txt");
		// run algorithm, print out result
		System.out.println(HMMBuilder.buildBW(alph, states, outcome, trans, emiss, iterations));
	}
}