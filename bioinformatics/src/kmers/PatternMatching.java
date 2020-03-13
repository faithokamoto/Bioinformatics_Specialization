package kmers;

//for lists that grow as needed
import java.util.ArrayList;

//for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class PatternMatching {
	
	// counts the number of times a string appears in a longer string
	// PRECONDITION: genome and pattern are not empty, genome's length >= pattern's
	public static int patternCount(String genome, String pattern) {
		// initialize return variable
		int count = 0;
		
		// jump through a longer string to the beginning indexes of the passed-in string
		for (int i = genome.indexOf(pattern), n = genome.length() - pattern.length(); i <= n && i != -1; i += 0) {
			// increment the counter, move to the next one
			count++;
			i = genome.indexOf(pattern, i + 1);
		}
		
		return count;
	}

	// finds the k-mers which appear most frequently, and at least t times, in a longer string
	// PRECONDITION: genome is not empty, genome's length >= k + t, k > 0, t >= 0
	// CALLS: patternCount
	public static ArrayList<String> frequentWords(String genome, int k, int t) {
		// initialize return variable
		ArrayList<String> frequent = new ArrayList<String>();
		
		// used to keep track of the situation through the loop
		String pattern = "";
		int count = 0;
		
		// maximum number of repeats set to the minimum required
		int maxCount = t;
		
		// loop through every k-mer in the text (with a chance of appearing t times after)
		for (int i = 0, n = genome.length() - k - t; i <= n; i++) {
			// get new k-mer
			pattern = genome.substring(i, i + k);
			
			// if it hasn't been found frequent yet
			if (!frequent.contains(pattern)) {
				// count the number of times it appears
				count = patternCount(genome.substring(i), pattern);
				
				// if it's more than the max so far
				if (count > maxCount) {
					// clear out the frequent list and stick it in
					frequent.clear();
					frequent.add(pattern);
					// set max so far to its count
					maxCount = count;
				}
				
				// if it's the max so far stick it in the frequent list
				else if (count == maxCount) frequent.add(pattern);
			}
		}
		
		// return all most frequent k-mers
		return frequent;
	}

	// finds the reverse complement of a DNA string
	// PRECONDITION: pattern is not empty
	public static String reverseComplement(String pattern) {
		// initialize return variable
		String reverse = "";
		
		// loop over passed-in string
		for (char c : pattern.toCharArray()) {
			// add complementary base to the beginning of reverse
			if (c == 'A') reverse = "T" + reverse;
			else if (c == 'C') reverse = "G" + reverse;
			else if (c == 'G') reverse = "C" + reverse;
			else if (c == 'T') reverse = "T" + reverse;
		}
		
		return reverse;
	}

	// lists all the indexes where a passed-in string begins in a longer string
	// PRECONDITION: genome and pattern are not empty, genome's length >= pattern's
	public static ArrayList<Integer> patternMatching(String genome, String pattern) {
		// initialize return variable
		ArrayList<Integer> starts = new ArrayList<Integer>();
		
		// jump through a longer string to the beginning indexes of the passed-in string
		for (int i = genome.indexOf(pattern), n = genome.length() - pattern.length(); i <= n && i != -1; i += 0) {
			// record the index, move to the next one
			starts.add(i);
			i = genome.indexOf(pattern, i + 1);
		}
		
		return starts;
	}

	// finds all the k-mers in a longer string that appear at least t times in a window of L length
	// PRECONDITON: genome is not empty, genome's length >= L, L >= k + t, k > 0, t > 0
	// CALLS: frequentWords, patternMatching
	public static ArrayList<String> clumpFinder(String genome, int k, int L, int t) {
		// initialize return variable
		ArrayList<String> clumps = new ArrayList<String>();
		
		// find all at least t-frequent k-mers spread out over the longer string
		ArrayList<String> frequent = frequentWords(genome, k, t);
		// used to track starts each time in the loop
		ArrayList<Integer> starts = new ArrayList<Integer>();
		
		// check each of these frequent k-mers
		for (String freq : frequent) {
			// find every index where the k-mer begins
			starts.clear();
			starts = patternMatching(genome, freq);
			
			// loop through the list of starts
			for (int j = 0; j <= starts.size() - t; j++) {
				// check t indexes ahead of the current index, if that k-mer is in the L-window
				if (starts.get(j) >= starts.get(j + t - 1) - L + k) {
					// add it to the list of clumpers
					clumps.add(freq);
					// stop checking for this k-mer
					break;
				}
			}
		}
		
		return clumps;
	}

	// reads a file in as a string, getting rid of line breaks
	// PRECONDITION: fileName is a valid location
	public static String readFileAsString(String fileName) {
		// i got no forking idea how this works
		String text = "";
		
		try {
			text = new String(Files.readAllBytes(Paths.get(fileName)));
			text = text.replace("\n", "").replace("\r", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}

	// where I call methods to test them
	public static void main(String[] args) {
		
	}
}