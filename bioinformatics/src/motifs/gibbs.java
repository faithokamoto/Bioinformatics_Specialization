package motifs;

// for all kinds of lists
import java.util.*;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// for getting preliminary motifs
import motifs.random;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
class gibbs {
	// makes it easier than declaring all the bases multiple times
	@SuppressWarnings("serial")
	private static final ArrayList<Character> BASES = new ArrayList<Character>() {
		{
			add('A');
			add('C');
			add('G');
			add('T');
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
					add = BASES.get(j);
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
	public static float[][] createProfile(String[] dna) {
		// initialize return variable
		float[][] profile = new float[4][dna[0].length()];
		// used to track situation in the loop
		int total = 1;
		
		// for each index in the strings
		for (int i = 0, n = profile[0].length; i < n; i++) {
			// loop over each base
			for (char base : BASES) {
				// loop over each DNA string
				for (String genome : dna) {
					// if THIS string's char at THIS index equals THIS base, increment counter
					if (genome.charAt(i) == base) total++;
				}
				
				//calculate the frequency of this base's appearance
				profile[BASES.indexOf(base)][i] = (float) total / (dna.length + 4);
				// reset counter
				total = 1;
			}
		}
		
		return profile;
	}
	
	// calculates the score of a DNA string based on a probability matrix
	// PRECONDITION: pattern is not empty, pattern's length = profile[]'s lengths, profile's length = 4
	// PRECONDITION: all values in profile are on [0, 1]
	public static double scoreMotif(String pattern, float[][] profile) {
		// initialize return variable (probability starts at 1)
		double prob = 1;
		
		// loop through DNA string
		for (int i = 0, n = pattern.length(); i < n; i++) {
			// multiply total probability by appropriate probability from profile
			prob *= profile[BASES.indexOf(pattern.charAt(i))][i];
		}
		
		return prob;
	}
	
	// calculates the score of an array of strings against a probability matrix
	// PRECONDITION: strings in motifs are the same length, which is the length of all profile[]s and > 0
	// CALLS: consensus
	public static int scoreMotifs(String[] motifs, float[][] profile) {
		// initialize return variable
		int score = 0;
		// find the consensus string for this profile
		String consensus = consensus(profile);
		
		// for each string, increment score by how different it is from the consensus
		for (String motif : motifs) score += hammingDistance(motif, consensus);
		
		return score;
	}
	
	// generate a (weighted random) k-mer based on a probability matrix
	// PRECONDITION: genome is not empty, genome's length >= profile[]'s lengths
	// CALLS: scoreMotif
	public static String weightedKmer(String genome, float[][] profile) {
		// calculate how long the k-mer should be
		int k = profile[0].length;
		String[] kmers = new String[genome.length() - k + 1];
		for (int i = 0, n = genome.length() - k; i <= n; i++) kmers[i] = genome.substring(i, i + k);
		return kmers[weightedRandom(motifProbs(kmers, profile))];
	}
	
	// generate a random number with weighted probabilities given in a list
	// PRECONDITION: probs's length > 0
	public static int weightedRandom(double[] probs) {
		// sum all the values in probs
		double sum = 0;
		for (double p : probs) sum += p;
		
		// divide each value by the sum (normalizes values to sum to 1)
		for (int i = 0, n = probs.length; i < n; i++) probs[i] /= sum;
		
		// generate a random number and reset sum
		double rand = Math.random();
		sum = 0;
		
		// check through the loop
		for (int i = 0, n = probs.length; i < n; i++) {
			// if the current number is higher than the random number, return index
			if (probs[i] + sum > rand) return i;
			// add current number to sum so compare-to-random number works
			sum += probs[i];
		}
		
		// just in case that didn't work, return the last value
		return probs.length - 1;
	}
	
	// remove string at a specified index from an array
	// PRECONDITION: 0 <= index < motifs's length
	public static String[] motifsExcept(String[] motifs, int index) {
		// initialize return variable
		String[] except = new String[motifs.length - 1];
		
		// add all strings before index
		for (int i = 0; i < index; i++) except[i] = motifs[i];
		// add all strings after index
		for (int i = index + 1, n = motifs.length; i < n; i++) except[i - 1] = motifs[i];
		
		return except;
	}
	
	// create an array of probabilities based on an array of strings and a probability matrix
	// PRECONDTITION: motifs's strings are the same length, which = profile[]'s length
	// CALLS: scoreMotif
	public static double[] motifProbs(String[] motifs, float[][] profile) {
		// initialize return variable
		double[] probs = new double[motifs.length];
		
		// calculate & save probability of each string
		for (int i = 0, n = motifs.length - 1; i < n; i++) probs[i] = scoreMotif(motifs[i], profile);
		
		return probs;
	}
	
	// tries to find an array of k-mers, from an array of strings, which are the most similar to each other
	// PRECONDITION: dna's strings are the same length, which is >= k, k > 0, n > 0
	// CALLS: bestRandomMotifs, createProfile, scoreMotifs, weightedRandom
	// CALLS: motifsExcpet, motifProbs, weightedKmer
	public static String[] gibbsSampler(String[] dna, int k, int n) {
		// initialize return variable to some good motifs
		String[] bestMotifs = random.bestRandomMotifs(dna, k, 20);
		// save those motifs in another variable
		String[] motifs = bestMotifs.clone();
		
		// initialize a probability matrix & lowest score to current motifs
		float[][] profile = createProfile(motifs);
		int minScore = scoreMotifs(motifs, profile);
		// used to track situation in the loop
		int score, index;
		
		// loop n times
		for (int i = 1; i < n; i++) {
			// get a random motif index
			index = (int) (Math.random() * motifs.length);
			// create a profile based on everything but that motif
			profile = createProfile(motifsExcept(motifs, index));
			
			// choose a weighted random k-mer based on that profile from the left-out string
			motifs[index] = weightedKmer(dna[index], profile);
			// calculate the new score
			score = scoreMotifs(motifs, createProfile(motifs));
			
			// if the new motifs are better than the current best ones
			if (score < minScore) {
				// update minimum score & best motifs
				minScore = score;
				bestMotifs = motifs.clone();
			}
		}
		
		return bestMotifs;
	}
	
	// tries to find an array of k-mers, from an array of strings, which are the most similar to each other
	// PRECONDITION: dna's strings are the same length, which is >= k, k > 0, t > 0, n > 0
	// CALLS: gibbsSampler, scoreMotifs, createProfile
	public static String[] bestGibbsMotifs(String[] dna, int k, int n, int t) {
		// initialize return variable to some good motifs
		String[] bestMotifs = gibbsSampler(dna, k, n);
		// save current motifs
		String[] motifs = bestMotifs.clone();
		// the minimum recorded score is score of those motifs
		int minScore = scoreMotifs(motifs, createProfile(motifs));
		// keep track of current score in the loop
		int score;
		
		// loop 1 - t times (already ran once)
		for (int i = 1; i < t; i++) {
			// get some new "good" motifs
			motifs = gibbsSampler(dna, k, n);
			// score these motifs
			score = scoreMotifs(motifs, createProfile(motifs));
			
			// if the new motifs are better than the current best ones
			if (score < minScore) {
				// update minimum score & best motifs
				minScore = score;
				bestMotifs = motifs.clone();
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
			text = text.replace("*", "").replace("\r", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
	
	// where I call methods to test them
	public static void main(String[] args) {
		String data = readFileAsString("src/motifs/dosr.txt");
		String[] motifs = bestGibbsMotifs(data.split("\n"), 21, 1000, 200);
		for (String motif : motifs) System.out.println(motif);
		System.out.println(scoreMotifs(motifs, createProfile(motifs)));
	}
}