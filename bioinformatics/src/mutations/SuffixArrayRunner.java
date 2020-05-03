package mutations;

// for reading from files
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

// for arrays of indeterminate size
import java.util.ArrayList;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Basic runner for the SuffixArray class
 * @author faith
 */
public class SuffixArrayRunner {
	/**
	 * Runs some SuffixArray methods.
	 * Currently reads in the myc genome and some reads, and finds all close-enough reads
	 * @param args not used
	 * @throws IOException if the reading/writing goes wrong
	 */
	public static void main(String[] args) throws IOException {
		// point a reader at the reads file
		Scanner reader = new Scanner(new File("src/mutations/myc_reads.txt"));
		// initialize a List of reads
		ArrayList<String> readsList = new ArrayList<String>();
		
		// add each read in
		while (reader.hasNext()) readsList.add(reader.next());
		
		// clean up
		reader.close();
		
		// initialize an array for List -> array conversion
		String[] readsArr = new String[readsList.size()];
		// copy all values through
		for (int i = 0; i < readsArr.length; ++i) readsArr[i] = readsList.get(i);
		
		// point a reader at the genome file
		reader = new Scanner(new File("src/mutations/myc_bwt.txt"));
		// point a writer at the output file
		BufferedWriter writer = new BufferedWriter(new FileWriter("src/mutations/output.txt"));
		
		// for each read that is close enough
		for (String close : SuffixArray.closeReads(
				SuffixArray.fromBWTransform(reader.next()), readsArr, 1))
			// write it out
			writer.write(close + "\n");
		
		// clean up
		writer.close();
		reader.close();
	}
}