package kmers;

// for lists that grow as needed
import java.util.ArrayList;

//for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class FrequencyArray {
	// makes it easier than declaring all the bases multiple times
	@SuppressWarnings("serial")
	private static final ArrayList<String> BASES = new ArrayList<String>() {
		{
			add("A");
			add("C");
			add("G");
			add("T");
		}
	};

	// converts a DNA string to a number
	// PRECONDITION: pattern is not empty
	public static int patternToNumber(String pattern) {
		// initialize return variable
		int num = 0;

		// for each base in the string
		for (int i = 0, n = pattern.length(); i < n; i++) {
			// add the proper number to the total index-number
			num += BASES.indexOf(pattern.charAt(i) + "") * Math.pow(4, n - 1 - i);
		}

		return num;
	}

	// converts a number to a DNA string of length k
	// PRECONDITION: num >= 0, k > 0
	public static String numberToPattern(int num, int k) {
		// initialize return variable
		String pattern = "";
		// used to hold the remainder each time through the loop
		int rem;
		
		// loop k times
		for (int i = 0; i < k; i++) {
			// find the remainder when the num is divided by 4
			rem = num % 4;
			// add the proper nucleotide to the pattern
			pattern = BASES.get(rem) + pattern;
			
			// integer division of num by 4
			num /= 4;
		}
		
		return pattern;
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
		
	// creates a frequency array for how often each k-mer appears in a longer string
	// PRECONDITION: genome is not empty, genome's length >= k, k > 0
	// CALLS: patternToNumber
	public static int[] computingFrequencies(String genome, int k) {
		// initialize return variable, with enough room for every possible k-mer
		int[] frequencies = new int[(int) Math.pow(4, k)];
		
		// loop over every k-mer in the longer string
		for (int i = 0, n = genome.length(); i <= n - k; i++) {
			// add its count to the frequency array
			frequencies[patternToNumber(genome.substring(i, i + k))]++;
		}
		
		return frequencies;
	}

	// finds all most-frequent k-mers in a longer string that appear at least t times
	// PRECONDITION: genome is not empty, genome's length > k + t, k > 0, t >= 0
	// CALLS: computingFrequencies, numberToPattern
	public static ArrayList<String> frequentWords(String genome, int k, int t) {
		// initialize return variable
		ArrayList<String> frequent = new ArrayList<String>();
		
		// create the frequency array for this longer string
		int[] freqArray = computingFrequencies(genome, k);
		
		// loop over the frequency array
		for (int i = 0, n = freqArray.length; i < n; i++) {
			// if the corresponding k-mer appeared at least t times, add it to the list of frequent ones
			if (freqArray[i] >= t) frequent.add(numberToPattern(i, k));
		}
		
		return frequent;
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