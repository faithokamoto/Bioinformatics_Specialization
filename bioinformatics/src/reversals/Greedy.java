package reversals;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

// for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

// for lists that can change size
import java.util.ArrayList;

public class Greedy {
	/**
	 * <h1>Reverses part of a permutation so the [k-1] has |k| in it</h1>
	 * Finds the endpoints of the block to reverse, then loops to the middle and swaps numbers.
	 * @param perm the permutation to reverse
	 * @param k the index to sort
	 */
	public static void kReversal(int[] perm, int k) {
		// assume the reversal will start at the end
		int endReversal = k - 1;
		// move end along until k is reached
		while(endReversal < perm.length && Math.abs(perm[endReversal]) != k) endReversal++;
		
		// loop to the middle of the block being reversed
		for (int i = 0; i <= (endReversal - k + 1) / 2; i++) {
			// reverse numbers on the ends, negating all in the process
			int temp = perm[i + k - 1];
			perm[i + k - 1] = perm[endReversal - i] * -1;
			perm[endReversal - i] = temp * -1;
		}
	}
	
	/**
	 * <h1>Greedy sorting a a permutation</h1>
	 * Iteratively uses reversals to move sorted numbers to the front, writing each step into a file
	 * @param filename the file to write the reversal steps to
	 * @param perm the permutation to sort
	 */
	public static void greedySort(String filename, int[] perm) {
		// loop over all ks required
		for (int k = 1; k <= perm.length; k++) {
			// if this index is not sorted
			if (perm[k - 1] != k) {
				// try a k-reversal
				kReversal(perm, k);
				// write the current state
				writePermToFile(filename, perm);
				
				// if the k-reversal left it negative
				if (perm[k - 1] == -k) {
					// negate it & write the current state
					perm[k - 1] = k;
					writePermToFile(filename, perm);
				}
			}
		}
	}
	
	/**
	 * <h1>Counts the number of breakpoints in a permutation</h1>
	 * Loops over the permutation to check adjacent numbers, then checks the endpoints
	 * @param perm numbers representing an order of blocks in a genome
	 * @return the number of breakpoints (adjacent integers which have to be separated for sorting
	 */
	public static int numBreakpoints(int[] perm) {
		// initialize return variable
		int bp = 0;
		// check between all numbers for non- +1 moves, increment if so
		for (int i = 0; i < perm.length - 1; i++) if (perm[i] + 1 != perm[i + 1]) bp++;
		// check beginning and end
		if (perm[0] != 1) bp++;
		if (perm[perm.length - 1] != perm.length) bp++;
		
		return bp;
	}
	
	/**
	 * <h1>Reads a file into a string</h1>
	 * Tries to access the file to read, returns if possible
	 * @param filename the path/name of the file to read
	 * @return the String contents of the file
	 * @throws IOException (if the filename was not valid)
	 */
	public static String readFileAsString(String filename) {
		// initialize return variable
		String text = "";
		
		try {
			// read the file into text
			text = new String(Files.readAllBytes(Paths.get(filename)));
			text = text.replace("\r", "");
		}
		// if that didn't work, explain why
		catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
	
	/**
	 * <h1>Reads <code>int</code> groupings int lists of <code>int[]</code>s</h1>
	 * Formats the string, splits into groups using split, then reads each group into an array.
	 * @param data a properly formatted representation of the groupings
	 * @param split what characters split the groupings
	 * @return <code>ArrayList</code> of <code>int[]</code> groupings
	 */
	public static ArrayList<int[]> readGroups(String data, String split) {
		// initialize return variable
		ArrayList<int[]> groups = new ArrayList<int[]>();
		// reformat data for better use
		data = data.substring(1, data.length() - 1).replace("+", "").replace(split, "S").replace(",", "");
		// split the data into groups
		String[] minis = data.split("S");
		
		// loop over each group
		for (String mini : minis) {
			// split this group into the ints
			String[] seqStr = mini.split(" ");
			
			// read the ints into an array
			int[] seq = new int[seqStr.length];
			for (int i = 0; i < seqStr.length; i++) seq[i] = Integer.parseInt(seqStr[i]);
			// add this group to groups
			groups.add(seq);
		}
		
		return groups;
	}
	
	/**
	 * <h1>Writes a permutation to a file</h1>
	 * Points a writer at a file, then writes each section in order with the proper format
	 * <br>
	 * precondition: perm contains no 0s
	 * @param filename the path/name of the file to write to
	 * @param perm an <code>int[]</code> array of numbers in order, representing blocks in a genome
	 * @throws IOException if filename is not valid
	 */
	public static void writePermToFile(String filename, int[] perm) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			
			// loop over ever number but the last
			for (int i = 0; i < perm.length - 1; i++) {
				// if positive add a plus sign
				if (perm[i] > 0) writer.write("+");
				// write number with space afterwards
				writer.write(perm[i] + " ");
			}
			// if last number is positive, add a plus sign
			if (perm[perm.length - 1] > 0) writer.write("+");
			// write last number with a newline after it
			writer.write(perm[perm.length - 1] + "\n");
			
	        // clean up
		    writer.flush();
		    writer.close();  
		}
		
		// if that didn't work, explain why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeGenomeToFile(String filename, ArrayList<int[]> genome) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			
			for (int[] chrom : genome) {
				writer.write("(");
				// loop over ever number but the last
				for (int i = 0; i < chrom.length - 1; i++) {
					// if positive add a plus sign
					if (chrom[i] > 0) writer.write("+");
					// write number with space afterwards
					writer.write(chrom[i] + " ");
				}
				// if last number is positive, add a plus sign
				if (chrom[chrom.length - 1] > 0) writer.write("+");
				// write last number with a newline after it
				writer.write(chrom[chrom.length - 1] + ")");
			}
			writer.write("\n");
	        // clean up
		    writer.flush();
		    writer.close();  
		}
		
		// if that didn't work, explain why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
	}
}