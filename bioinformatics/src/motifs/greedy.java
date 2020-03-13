package motifs;

// for all kinds of lists
import java.util.*;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class greedy {
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
	
	// creates a consensus string from a probability matrix
	// consensus string: string with highest probability
	// PRECONDITION: profile[]'s are of equal length, all values in profile are on [0, 1], profile's length = 4
	public static String consensus(float[][] profile) {
		// initialize return variable
		String con = "";
		// maximum probability set to 0
		float max = 0;
		// used to keep track of which char to add to con in the loop
		char add = 'A';
		
		// loop through the rows (probabilities for one base)
		for (int i = 0, n = profile[0].length; i < n; i++) {
			// loop through the columns (probabilities for each base at an index)
			for (int j = 0, m = profile.length; j < m; j++) {
				// if this base's probability is higher than the column's max
				if (profile[j][i] > max) {
					// set column's max to this probability
					max = profile[j][i];
					
					// set the char to add to the row's corresponding char
					add = BASES.get(j).charAt(0);
				}
			}
			
			// add proper char to the consensus string
			con += add;
			// reset max probability for the column
			max = 0;
		}
		
		return con;
	}
	
	// creates a probability matrix with frequency of each base's appearance at each index in an arraylist of strings
	// PRECONDITION: dna's strings are the same length, which is > 0
	public static float[][] createProfile(ArrayList<String> dna) {
		// initialize return variable
		float[][] profile = new float[4][dna.get(0).length()];
		// used to track situation in the loop
		int total = 1;
		
		// for each index in the strings
		for (int i = 0, n = profile[0].length; i < n; i++) {
			// loop over each base
			for (String base : BASES) {
				// loop over each DNA string
				for (String genome : dna) {
					// if THIS string's char at THIS index equals THIS base, increment counter
					if ((genome.charAt(i) + "").equals(base)) total++;
				}
				
				//calculate the frequency of this base's appearance
				profile[BASES.indexOf(base)][i] = (float) total / (dna.size() + 4);
				// reset counter
				total = 1;
			}
		}
		
		return profile;
	}
	
	// calculates the score of a DNA string based on a probability matrix
	// PRECONDITION: pattern is not empty, pattern's length = profile[]'s lengths, profile's length = 4
	// PRECONDITION: all values in profile are on [0, 1]
	public static double score(String pattern, float[][] profile) {
		// initialize return variable (probability starts at 1)
		double prob = 1;
		
		// loop through DNA string
		for (int i = 0, n = pattern.length(); i < n; i++) {
			// multiply total probability by appropiate probability from profile
			prob *= profile[BASES.indexOf(pattern.charAt(i) + "")][i];
		}
		
		return prob;
	}
	
	// calculates the score of an array of strings against a probability matrix
	// PRECONDITION: strings in motifs are the same length, which is the length of all profile[]s and > 0
	// CALLS: consensus
	public static int score(ArrayList<String> motifs, float[][] profile) {
		// initialize return variable
		int score = 0;
		// find the consensus string for this profile
		String consensus = consensus(profile);
		
		// for each string, increment score by how different it is from the consensus
		for (String motif : motifs) score += hammingDistance(motif, consensus);
		
		return score;
	}
	
	// finds the k-mer in a string that is the most probable given a probability matrix for each position and base
	// PRECONDITION: genome is not empty, genome's length >= profile[]'s lengths
	// CALLS: score
	public static String profProbKmer(String genome, float[][] profile) {
		// calculate how long the k-mer should be
		int k = profile[0].length;
		// initialize return variable to first k-mer
		String mostProb = genome.substring(0, k);
		// maximum probability so far is nothing
		double maxProb = 0;
		// used to track the situation inside the loop
		double prob = 0;
		
		// loop through every k-mer in the string
		for (int i = 0, n = genome.length() - k; i <= n; i++) {
			// calculate the probability of this k-mer
			prob = score(genome.substring(i, i + k), profile);
			
			// if the probability exceeds the maximum probability so far
			if (prob > maxProb) {
				// set maximum probability to this probability, and most likely k-mer to this k-mer
				maxProb = prob;
				mostProb = genome.substring(i, i + k);
			}
		}
		return mostProb;
	}
	
	// tries to find an array of k-mers, from an array of strings, whcih are the most similar to each other
	// PRECONDTION: all strings in dna are longer than k, k > 0
	// CALLS: createProfile, profProbKmer
	public static String[] greedyMotifSearch(String[] dna, int k) {
		// initialize return variable
		String[] bestMotifs = new String[dna.length];
		// used to hold current motifs, must grow and shrink
		ArrayList<String> motifs = new ArrayList<String>();
		// minimum score set to maximum (every character of every string is different)
		int minScore = k * dna.length;
		// used to track values in the loop
		float[][] profile = new float[4][dna[0].length()];
		int score;
		
		// loop over every k-mer in the first string
		for (int i = 0, n = dna[0].length() - k; i <= n; i++) {
			// set motifs to just the k-mer
			motifs.clear();
			motifs.add(dna[0].substring(i, i + k));
			
			// loop over all other strings
			for (int j = 1, m = dna.length; j < m; j++) {
				// create a profile for the current k-mers
				profile = createProfile(motifs);
				
				// add the best k-mer from the current string to motifs
				motifs.add(profProbKmer(dna[j], profile));
			}
			
			// score the set of k-mers found
			score = score(motifs, profile);
			
			// if this score is less than the minimum score so far
			if (score < minScore) {
				// set minimum score to this score
				minScore = score;
				// set the best-scoring motifs to these motifs
				for (int j = 0, m = motifs.size(); j < m; j++) {
					bestMotifs[j] = motifs.get(j);
				}
			}
		}
		
		return bestMotifs;
	}
	
	// reads a file in as a string, getting rid of line breaks
	// PRECONDITION: fileName is a valid location
	public static String readFileAsString(String fileName) {
		// i got no forking idea how this works
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(fileName)));
			text = text.replace("\r", "").replace("*", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
	
	// where I call methods to test them
	public static void main(String[] args) {
		String[] data = readFileAsString("src/greedy/data.txt").split("\n");
		String[] motifs = greedyMotifSearch(data, 12);
		for (String motif : motifs) System.out.println(motif);
	}
}