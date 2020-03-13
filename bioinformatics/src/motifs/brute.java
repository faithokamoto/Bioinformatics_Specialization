package motifs;

// for all kinds of lists
import java.util.*;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class brute {
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

	// brute-force algorithm to find most common k-mer in every string in dna with at most d differences
	// PRECONDITION: strings in dna are the same length, which is >= k, d <= k, k > 0, d >= 0
	// CALLS: neighbors, hammingDistance
	public static ArrayList<String> motifEnumeration(String[] dna, int k, int d) {
		// initialize return variable
		ArrayList<String> patterns = new ArrayList<String>();
		// used to track what's going on in the loops
		ArrayList<ArrayList<String>> bothNeighbors = new ArrayList<ArrayList<String>>();
		ArrayList<String> neighbors = new ArrayList<String>();
		int matches = 0;
		
		// loop over EVERY k-mer in the first string
		for (int i = 0, n = dna[0].length() - k; i <= n; i++) {
			// find all the d-neighbors of this k-mer
			bothNeighbors = neighbors(dna[0].substring(i, i + k), d);
			neighbors.addAll(bothNeighbors.get(0));
			neighbors.addAll(bothNeighbors.get(1));
			
			// loop over EVERY string in the neighborhood
			for (String neighbor : neighbors) {
				// loop over EVERY string in dna
				for (String text : dna) {
					// loop over EVER k-mer in the string
					for (int j = 0, m = text.length() - k; j <= m; j++) {
						// if a string is found close enough the to motif
						if (hammingDistance(text.substring(j, j + k), neighbor) <= d) {
							// indicate a match was found
							matches++;
							
							// jump out of the "k-mer in the string" (uses j) loop
							break;
						}
					}
				}
				
				// after going over every string in dna, if a match was found for each
				// add this neighbor to the return list
				if (matches == dna.length) patterns.add(neighbor);
				// reset the counter
				matches = 0;
			}
		}
		
		// removes duplicates from the motif list by sticking everything in a no-duplicate data structure
		// then sticking it back into the original
		Set<String> remove = new LinkedHashSet<String>();
		remove.addAll(patterns);
		patterns.clear();
		patterns.addAll(remove);
		
		return patterns;
	}
	
	// calculates the minimum differences between a string and an array of longer strings
	// a substring of the proper length is taken from each longer string for this purpose
	// PRECONDITION: strings in dna are the same length, which is >= the length of the string
	// CALLS: hammingDistance
	public static int distBtwnPatsStrs(String pattern, String[] dna) {
		// initialize return variable
		int distance = 0;
		// used to keep track of what's going on
		int partialD, checkD;
		// save length of the string that will be pulled
		int k = pattern.length();
		
		// for each string in the dna array (rhymes!)
		for (String genome : dna) {
			// set minimum to maximum difference: the entire string being different
			partialD = k;
			
			// loop over every k-mer in this current string
			for (int i = 0, n = genome.length() - k; i <= n; i++) {
				// calculate difference between this k-mer and the passed-in string
				checkD = hammingDistance(genome.substring(i, i+ k), pattern);
				
				// if it's less than the minimum so far, set the minimum to this difference
				if (partialD > checkD) partialD = checkD;
			}
			
			// add the minimum difference for this string to the total difference
			distance += partialD;
		}
		
		return distance;
	}
	
	// finds a consensus motif of length k
	// "consensus motif" is a string that minimizes total differences between a string and an array of longer strings
	// a substring of the proper length is taken from each longer string for this purpose
	// PRECONDITION: strings in dna are the same length, which is >= k, k > 0
	// CALLS: numberToPattern, distBtwnPatsStrs
	public static String medianString(String[] dna, int k) {
		// initialize return variable to the first k-mer in the first string
		String median = dna[0].substring(0, k);
		// set minimum to maximum difference: the entire string, for every string being different
		int min = k * dna.length;
		// used to track of values during the loop
		String pattern = "";
		int dist = 0;
		
		// for each number representing a DNA k-mer
		for (int i = 0, n = (int) Math.pow(4, k); i < n; i++) {
			// set the new string based on the current number
			pattern = numberToPattern(i, k);
			// calculate the new distance if this pattern is used
			dist = distBtwnPatsStrs(pattern, dna);
			
			// if the distance is less than the current minimum
			if (dist < min) {
				// set min to the distance, and set the consensus to the string
				min = dist;
				median = pattern;
				System.out.println(pattern);
			}
			else if (dist == min) {
				System.out.println(pattern + " " + dist);
			}
		}
		
		return median;
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
		String[] data = new String[] {"CTCGATGAGTAGGAAAGTAGTTTCACTGGGCGAACCACCCCGGCGCTAATCCTAGTGCCC",
				"GCAATCCTACCCGAGGCCACATATCAGTAGGAACTAGAACCACCACGGGTGGCTAGTTTC",
				"GGTGTTGAACCACGGGGTTAGTTTCATCTATTGTAGGAATCGGCTTCAAATCCTACACAG"
		};
		
		System.out.println(medianString(data, 7));
	}
}