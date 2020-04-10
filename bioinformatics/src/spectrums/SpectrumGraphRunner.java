package spectrums;

// for data structures
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * A basic runner for SpectrumGraph & VectorGraph
 * @author faith
 */
public class SpectrumGraphRunner {
	/**
	 * Reads a list of integers from a file
	 * <br>
	 * Points a Scanner at the file and then reads integers until none remain
	 * @param filename the file to read from
	 * @return the integers in order of appearance
	 */
	public static ArrayList<Integer> readInts(String filename) {
		// initialize return variable
		ArrayList<Integer> spectrum = new ArrayList<Integer>();
		try {
			// point a scanner at the file
			Scanner reader = new Scanner(new File(filename));
			// while integers remain to read, read the next one in
			while (reader.hasNextInt()) spectrum.add(reader.nextInt());
			// clean up
			reader.close();
		}
		// if something fishy occurs, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
		return spectrum;
	}
	
	/**
	 * Runs code to test it
	 * @param args not used
	 */
	public static void main(String[] args) {
	}
}