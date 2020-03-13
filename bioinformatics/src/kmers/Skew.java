package kmers;

// for lists that grow as needed
import java.util.ArrayList;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class Skew {
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

	// finds indexes that minimize #G-#C in a genome
	// PRECONDTION: genome is not empty
	public static ArrayList<Integer> minimumSkew(String genome) {
		// initialize return variable
		ArrayList<Integer> minI = new ArrayList<Integer>();
		// keep track of total skew
		int skew = 0;
		// keep track on minimum skew recorded
		int min = 0;

		// loop over the whole string
		for (int i = 0, n = genome.length(); i < n; i++) {
			// if this index is G, G-C increases, if C, it decreases
			if (genome.charAt(i) == 'G') skew++;
			else if (genome.charAt(i) == 'C') skew--;
			
			// if the current skew is less than ever recorded
			if (skew < min) {
				// minimum recorded skew set to current skew
				min = skew;
				
				// indexes-that-minimize-skew set to just current index
				minI.clear();
				minI.add(i + 1);
			}
			// or if it just the same as minimum, add to indexes list
			else if (skew == min) minI.add(i + 1);
		}
		
		return minI;
	}

	// calculates the number of differing positions in two strings
	// PRECONDITION: strings are equal length
	public static int hammingDistance(String a, String b) {
		// initialize return variable
		int dist = 0;
		
		// loop over each index, incrementing counter if chars are different
		for (int i = 0, n = a.length(); i < n; i++)
			if (a.charAt(i) != b.charAt(i)) dist++;
		
		return dist;
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
		
	// lists all the indexes where k-mers of a longer string have at most d differences from a passed-in string
	// PRECONDITION: pattern is not empty, genome's length >= pattern's, d <= pattern's length, d >= 0
	// CALLS: hammingDistance
	public static ArrayList<Integer> approximatePatternMatching(String genome, String pattern, int d) {
		// initialize return variable
		ArrayList<Integer> starts = new ArrayList<Integer>();

		// loop over every k-mer in the longer string
		for (int i = 0, n = genome.length() - pattern.length(); i <= n; i++) {
			// if this k-mer has at most d differences from passed-in string
			// add its beginning index to return list
			if (hammingDistance(pattern, genome.substring(i, i + pattern.length())) <= d) starts.add(i);
		}
		
		return starts;
	}

	// counts how many k-mers of a longer string have at most d differences from a passed-in string
	// PRECONDITION: pattern is not empty, genome's length >= pattern's, d <= pattern's length, d >= 0
	// CALLS: hammingDistance
	public static int approximatePatternCount(String genome, String pattern, int d) {
		// initialize return variable
		int count = 0;

		// loop over every k-mer in the longer string
		for (int i = 0, n = genome.length() - pattern.length(); i <= n; i++) {
			// if this k-mer has at most d differences from passed-in string
			// increment the counter
			if (hammingDistance(pattern, genome.substring(i, i + pattern.length())) <= d) count++;
		}
		
		return count;
	}

	// lists all the d-neighbors of a string
	// d-neighbors: strings of equal length that differ by as many as d chars
	// first ArrayList is strings less than d differences from pattern, second is exactly d differences
	// PRECONDITION: pattern is not empty, d <= pattern.length, d >= 0
	// CALLS: hammingDistance
	public static ArrayList<ArrayList<String>> neighbors(String pattern, int d) {
		// initialize return variable to have two ArrayLists of strings
		@SuppressWarnings("serial")
		ArrayList<ArrayList<String>> neighborhood = new ArrayList<ArrayList<String>>(){
			{
			add(new ArrayList<String>());
			add(new ArrayList<String>());
			}
		};
		
		// first arraylist = not yet at d, second arraylist = at d

		// if no differences are allowed, simply return the string intact in at-d list
		if (d == 0) neighborhood.get(1).add(pattern);
		
		// if the string is only one long
		else if (pattern.length() == 1) {
			// if d > 1 then stick all bases in not-yet-at-d list
			if (d > 1) neighborhood.get(0).addAll(BASES);
			
			// else (d == 1)
			else {
				for (String base : BASES) {
					// stick the base equal to pattern in not-yet-at-d-list
					if (base.equals(pattern)) neighborhood.get(0).add(base);
					
					// stick others in at-d list (one difference & d = 1)
					else neighborhood.get(1).add(base);
				}
			}
		}
		
		// moving into the non-special cases
		else {
			// in the else statement to make special cases run faster
			// compute d-neighbors for the string minus the first char
			ArrayList<ArrayList<String>> suffixNeighbors = neighbors(pattern.substring(1), d);
			// track the new pattern being tested in the first forEach loop
			String newPattern = "";
			
			// loop over the not-yet-at-d list
			for (String text : suffixNeighbors.get(0)) {
				for (String base : BASES) {
					// add each base to text in succession
					newPattern = base + text;
					
					// if that results in d maxing out, add new pattern to the at-d list
					if (hammingDistance(pattern, newPattern) == d) neighborhood.get(1).add(newPattern);
					// or if it doesn't add new pattern to the not-yet-at-d list
					else neighborhood.get(0).add(newPattern);
				}
			}
			
			// loop over the at-d list
			for (String text : suffixNeighbors.get(1))
				// add the first char of pattern to text (prevent any more differences)
				neighborhood.get(1).add(pattern.charAt(0) + text);
		}

		return neighborhood;
	}

	// creates a frequency array with how often a d-neighbor of each k-mer appears in a longer string
	// d-neighbors: strings of equal length that differ by as many as d chars 
	// PRECONDITION: genome is not empty, genome is longer than k, k >= d, d >= 0, k > 0
	// CALLS: neighbors, patternToNumber
	public static int[] compFreqWMismatches(String genome, int k, int d) {
		// initialize return variable, with enough room for every possible k-mer
		int[] frequencies = new int[(int) Math.pow(4, k)];
		
		// used to keep track of what is happening in the loop
		ArrayList<ArrayList<String>> bothNeighbors = new ArrayList<ArrayList<String>>();
		ArrayList<String> neighbors = new ArrayList<String>();

		// loop over every k-mer in the longer string
		for (int i = 0, n = genome.length(); i <= n - k; i++) {
			// find all the d-neighbors of this k-mer
			bothNeighbors = neighbors(genome.substring(i, i + k), d);
			neighbors.addAll(bothNeighbors.get(0));
			neighbors.addAll(bothNeighbors.get(1));
			
			// add each neighbor's count to the frequency array
			for (String neighbor : neighbors) frequencies[patternToNumber(neighbor)]++;
		}
		
		return frequencies;
	}

	// finds all most-frequent k-mers with at most d mismatches from them or their reverse complement in a longer string
	// PRECONDITION: genome is not empty, genome's length >= k, k >= d, k > 0, d >= 0
	// CALLS: compFreqWMismatches, numberToPattern, reverseComplement, patternToNumber
	public static ArrayList<String> freqWordsWMismatchesRC(String genome, int k, int d) {
		// initialize return variable
		ArrayList<String> frequent = new ArrayList<String>();
		// get the frequency array for the longer string
		int[] freqArray = compFreqWMismatches(genome, k, d);
		// maximum occurrences is set to 0
		int max = 0;
		
		// used to keep track of values in the loop
		String pattern, reverse;
		int total;
		
		// loop over the frequency array
		for (int i = 0, n = freqArray.length; i < n; i++) {
			// find the DNA string corresponding to this number...
			pattern = numberToPattern(i, k);
			// ... and its reverse complement
			reverse = reverseComplement(pattern);
			
			// calculate the total number of times that either appears with d-mismatches in the longer string
			total = freqArray[i] + freqArray[patternToNumber(reverse)];
			
			// if this total is higher than the maximum recorded
			if (total > max) {
				// set the maximum recorded to the total
				max = total;
				// return array set to just this string
				frequent.clear();
				frequent.add(pattern);
			}
			// or if the total IS the max recorded, add string to return array
			else if (total == max) frequent.add(pattern);
		}
		
		return frequent;
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