package antibiotics;

//for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

//for all kinds of lists
import java.util.*;

/**
 * <h1>Scoring-based Cyclopeptide Sequencing</h1>
 * Contains all the necessary methods to generate the sequence of a peptide from an experimental mass
 * spectrum based on score (# of similar elements), as well as some helper methods that calculate ideal
 * specta of given peptides and the score of a peptide against a spectrum
 * @author faith
 */
public class Scoring {
	/**
	 * stores all the possible masses of an amino acid (excluding duplicates) for easy access
	 */
	public static final int[] masses = new int[] {57, 71, 87, 97, 99, 101, 103, 113, 114, 115,
			128, 129, 131, 137, 147, 156, 163, 186};
	/**
	 * stores an amino acid mass table (abbreviation-integer mass)
	 */
	public static final Map<Character, Integer> AMINO_ACID_MASS = initializeAAM();
	
	/**
	 * <h1>Initializes the amino acid mass table from amino_acids.txt</h1>
	 * Grabs the amino acid masses, then loop through each line, pulling the char and integer out for the table.
	 * <br>
	 * calls: readFileAsString
	 * @return the completed amino acid mass table
	 */
	public static Map<Character, Integer> initializeAAM() {
		// initialize return variable
		Map<Character, Integer> aam = new HashMap<Character, Integer>();
		// read in masses
		String masses = readFileAsString("src/antibiotics/amino_acids.txt");
		
		// while some of masses is left
		while (!masses.isBlank()) {
			// put in first char (amino acid abbreviation) as key
			aam.put(masses.charAt(0),
			// and integer (after space, before newline) as value
					Integer.parseInt(masses.substring(masses.indexOf(' ') + 1, masses.indexOf('\n'))));
			// move masses to next line
			masses = masses.substring(masses.indexOf('\n') + 1);
		}
		
		return aam;
	}
	
	/**
	 * <h1>Puts a value in an array and shifts remaining values left</h1>
	 * Makes a new array, adding values before index, value at index, and then values after index.
	 * @param arr the array to modify
	 * @param index the index to add an element to
	 * @param value the value to add
	 * @return the array, modified as above
	 */
	public static int[] set(int[] arr, int index, int value) {
		// if the index isn't valid just return the array unchanged
		if (index >= arr.length || index < 0) return arr;
		
		// initialize return variable
		int[] changed = new int[arr.length];
		
		// add all values before the changed index
		for (int i = 0; i < index; i++) changed[i] = arr[i];
		// set the changed index
		changed[index] = value;
		// add all but last value after the changed index
		for (int i = index + 1; i < arr.length; i++) changed[i] = arr[i - 1];
		
		return changed;
	}
	
	/**
	 * <h1>Calculates the total mass of a peptide</h1>
	 * Loops through the peptide, adding each amino acid's mass up.
	 * @param peptide the peptide to calcuate the mass of (an <code>ArrayList</code> of masses)
	 * @return the total mass of the peptide
	 */
	public static int totalMass(int[] peptide) {
		// initialize return variable
		int total = 0;
		// add each mass to total
		for (int amino : peptide) total += amino;
		
		return total;
	}
	
	/**
	 * <h1>Generates an ideal mass spectrum for a given peptide</h1>
	 * First calculates the prefix mass for each index in peptide (mass up to that point). Then loop over each
	 * subpeptide (indexes i = 0 & j = i + 1) and adds its mass, as calculated by the prefix masses. Also checks
	 * (as long as cyclo is true) for possible cyclic subpeptides of the current indexes and adds  if so.
	 * <br>
	 * preconditions: peptide is a valid peptide, peptide is not empty
	 * @param peptide the peptide to generate a spectrum for
	 * @param cyclo whether to calculate a cylospectrum (true) or linear spectrum (false)
	 * @return a sorted <cod>ArrayList</code> of Integers for each molecular mass in the ideal spectrum
	 */
	public static ArrayList<Integer> idealSpectrum(int[] peptide, boolean cyclo) {
		// initialize return variable
		ArrayList<Integer> spec = new ArrayList<Integer>();
		// initialize array of prefix masses (masses up to this index in peptide)
		int[] prefixMass = new int[peptide.length + 1];
		
		// put proper values in each index of prefixMass
		for (int i = 1; i <= peptide.length; i++)
			// mass = mass of the previous stuff + mass of the amino acid at this point
			prefixMass[i] = prefixMass[i - 1] + peptide[i - 1];
		
		// loop over each starting index in peptide
		for (int i = 0; i < peptide.length; i++) {
			// loop over each index after i in peptide (including over-length)
			for (int j = i + 1; j <= peptide.length; j++) {
				// add mass of this bit (prefix mass of stuff after - prefix mass of stuff before)
				spec.add(prefixMass[j] - prefixMass[i]);
				// if some indexes on each end could be used (checking for cyclic sub-peptides)
				if (cyclo && i > 0 && j < peptide.length) {
					// add mass of that bit (total mass - mass of the insides)
					spec.add(prefixMass[peptide.length] - (prefixMass[j] - prefixMass[i]));
				}
			}
		}
		// add 0 (if nothing was left)
		spec.add(0);
		
		return spec;
	}
	
	/**
	 * <h1>Find the m most common mass differences (accounting for ties) in spectrum that may correspond to amino acids</h1>
	 * First creates a count map for all the differences, then uses that map to insertion-sort the differences
	 * by count. Then, finds the first element after the mth that doesn't share the mth count, and dumps
	 * all elements before that into an array for returning
	 * <br>
	 * precondition: all values in spectrum are >= 0, m > 1
	 * @param spectrum the spectrum to find mass differences in
	 * @param m the minimum number of differences to save
	 * @return the... see above
	 */
	public static int[] topDiffs(ArrayList<Integer> spectrum, int m) {
		// initialize count map & arraylist of differences
		Map<Integer, Integer> diffsMap = new HashMap<Integer, Integer>();
		ArrayList<Integer> diffs = new ArrayList<Integer>();
		
		// loop over all masses in spectrum
		for (int i = 1; i < spectrum.size(); i++) {
			// loop over all masses before i mass in spectrum
			for (int j = i - 1; j >= 0; j--) {
				// save this difference
				int diff = Math.abs(spectrum.get(i) - spectrum.get(j));
				// if it falls in the range of amino acid masses
				if (diff > 56 && diff < 201) {
					// if it hasn't been put into the diffsMap yet
					if (!diffsMap.containsKey(diff)) {
						// initialize it in diffMap and diffs
						diffsMap.put(diff, 1);
						diffs.add(diff);
					}
					// if it has been put in, increment its count
					else diffsMap.put(diff, diffsMap.get(diff) + 1);
				}
			}
		}
		
		// insertion sort: loop over all elements
		for (int i = 1; i < diffs.size(); i++) {
			// save current number
			Integer current = diffs.get(i);
			// index that this element should come after
			int newIndex = i - 1;
			// while it's checking elements that exist and the score of the checked element is less than the
			// one being sorted
			while(newIndex >= 0 && diffsMap.get(diffs.get(newIndex)) < diffsMap.get(current))
				// decrement the index
				newIndex--;
			
			// move current element to new, sorted index
			diffs.remove(i);
			diffs.add(newIndex + 1, current);
		}
		
		// save the count of the mth element
		int mCount = diffsMap.get(diffs.get(m - 1));
		// first index to remove from
		int removeIndex = m;
		// while the index to remove from has the same count as mth, move index to remove from back
		while (diffsMap.get(diffs.get(removeIndex)) == mCount) removeIndex++;
		
		// initialize return variable
		int[] diffsArray = new int[removeIndex - 1];
		// add in all proper elements
		for (int i = 0; i < diffsArray.length; i++) diffsArray[i] = diffs.get(i);
		
		return diffsArray;
	}
		
	/**
	 * <h1>Scores a peptide against a spectrum</h1>
	 * Creates the theoretical spectrum for peptide, then loops over the experimental spectrum,
	 * incrementing a counter for each match found. Each match is removed from the theoretical
	 * spectrum to account for multiples.
	 * <br>
	 * preconditions: peptide is a valid peptide, exSpec is a valid mass spectrum
	 * <br>
	 * calls: idealSpectrum
	 * @param peptide the peptide to score
	 * @param exSpec the spectrum to score against (experimentalSpectrum)
	 * @param whether to use a linear or cyclic spectrum
	 * @return the number of values (considering multipliticy) that match between the spectrums
	 */
	public static int score(int[] peptide, ArrayList<Integer> exSpec, boolean cyclo) {
		// initialize return variable
		int score = 0;
		
		// create the theoretical spectrum for this peptide
		ArrayList<Integer> theoSpec = idealSpectrum(peptide, cyclo);
		// loop over each value in exSpec
		for (Integer ex : exSpec) {
			// if this value is in theoSpec
			if (theoSpec.contains(ex)) {
				// add to score (match!)
				score++;
				// remove it from theoSpec (to prevent double-counting)
				theoSpec.remove(ex);
			}
		}
		
		return score;
	}
	
	/**
	 * <h1>Adds all possible masses to the end of given peptide</h1>
	 * Loops through all peptides, and for each loops though all masses, adding every combination of
	 * peptide + mass to expanded, then returning it.
	 * <br>
	 * calls: set
	 * @param peptides the peptides to add to
	 * @param only the masses to expand with
	 * @return all the expanded peptides
	 */
	public static Set<int[]> expand(Set<int[]> peptides, int[] only) {
		// initialize return variable
		Set<int[]> expanded = new HashSet<int[]>();
		
		// loop over each peptide
		for (int[] peptide : peptides) {
			// copy it
			int[] temp = new int[peptide.length + 1];
			for (int i = 0; i < peptide.length; i++) temp[i] = peptide[i];
			// loop over all masses to add
			for (int mass : only) {
				// set the last index to this mass, add to expanded
				expanded.add(set(temp, peptide.length, mass));
			}
		}
		
		return expanded;
	}
	
	/**
	 * <h1>Trimms a <code>Set</code> of peptides to n with the highest score (allowing for ties)</h1>
	 * First checks if trimming is needed, just returns leaderboard unchanged if not. If so, creates a
	 * score map (score-peptides with score), and loop from the higher score until enough peptides
	 * have been added to the return variable.
	 * <br>
	 * preconditions: leaderboard & exSpec are not empty, n > 1
	 * <br>
	 * calls: score
	 * @param leaderboard the peptides to trim
	 * @param exSpec the spectrum to score against
	 * @param n the minimum number of peptides to keep
	 * @return the... see above
	 */
	public static Set<int[]> trim(Set<int[]> leaderboard, ArrayList<Integer> exSpec, int n) {
		// only go through the trouble of trimming if there's a chance it throws out values
		if (n < leaderboard.size()) {
			// create a score map (score-pepetides with score)
			Map<Integer, Set<int[]>> scores = new TreeMap<Integer, Set<int[]>>(Collections.reverseOrder());
			
			// loop over all peptides
			for (int[] peptide : leaderboard) {
				// save this peptide's score
				int score = score(peptide, exSpec, false);
				// if it's not a key in scores yet, initialize it
				if (!scores.containsKey(score)) scores.put(score, new HashSet<int[]>());
				// add this peptide to its score
				scores.get(score).add(peptide);
			}
			
			// tracks how many peptides have been counted
			int sum = 0;
			// initialize return variable
			Set<int[]> trimmed = new HashSet<int[]>();
			// loop over all scores, from high to low
			for (Integer score : scores.keySet()) {
				// add to sum the number of peptides with this score
				sum += scores.get(score).size();
				// add said peptides to return variable
				trimmed.addAll(scores.get(score));
				
				// if the peptides counted is >= the minimum number to keep, leave
				if (sum >= n) break;
			}
			
			// return all the peptides added
			return trimmed;
		}
		
		// if n < leaderboard.size(), then nothing is trimmed
		else return leaderboard;
	}
	
	/**
	 * <h1>Finds a peptide that best matches the given mass specturm</h1>
	 * Maintains a <code>Set</code> of at least n best-scoring peptides (there may be more if ties),
	 * iteratively expanding it and then checking each one. If the total mass matches, and the score
	 * is better than the current top score, then it is saved as the leader. If it is too big it is
	 * deleted. This continues until no peptides are left under consideration
	 * <br>
	 * precondition: spectrum contains only combinations of actual amino acid masses, n > 1
	 * <br>
	 * calls: expand, getFirsts, score, topDiffs, totalMass, trim
	 * @param spectrum the spectrum that peptides are compared to
	 * @return all peptides with spectrums that match
	 */
	public static int[] scoringSequencing(ArrayList<Integer> exSpec, int m, int n) {
		// initialize return variable
		int[] leader = null;
		// holds peptides that are currently being tried, initialized to the empty peptide
		Set<int[]> leaderboard = new HashSet<int[]>();
		leaderboard.add(new int[0]);
		
		// placeholders for the loop
		// the total mass of the final peptide
		int parentMass = Collections.max(exSpec);
		// the top score achieved
		int topScore = 0;
		int[] onlyMasses = topDiffs(exSpec, m);

		// while still considering some peptides
		while (!leaderboard.isEmpty()) {
			// expand all candidates by all masses
			leaderboard = expand(leaderboard, onlyMasses);
			// loop through all peptides
			for (Iterator<int[]> i = leaderboard.iterator(); i.hasNext();) {
				// save the current peptide
			    int[] peptide = i.next();
			    
			    // if this peptide's mass equals the ideal mass
			    if (totalMass(peptide) == parentMass) {
			    	// save its score
			    	int score = score(peptide, exSpec, true);
			    	// if it beats the top score
					if (score > topScore) {
						// set it as the best, and its score as the top
						leader = peptide.clone();
						topScore = score;
						System.out.println(topScore);
					}
					i.remove();
				}
			    // or if this peptide's mass is too great
				else if (totalMass(peptide) > parentMass)
					// remove current peptide from consideration
					i.remove();
			}
			
			// trim leaderboard to at least n canadiates
			leaderboard = trim(leaderboard, exSpec, n);
		}
		return leader;
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
	
	/**<h1>Reads a mass spectrum from a string into an <code>ArrayList</code>
	 * Jumps through the data by spaces until none is left, parsing the integers between the spaces and
	 * adding them to the return variable.
	 * <br>
	 * precondition: data has only integers separated by spaces
	 * @param data the spectrum to read in
	 * @return the spectrum represented by an <code>ArrayList</code>
	 */
	public static ArrayList<Integer> readSpectrum(String data) {
		// initialize return variable
		ArrayList<Integer> spec = new ArrayList<Integer>();
		
		// to make readable
		data += " ";
		// while some data is left to read
		while (!data.isBlank()) {
			// pull the next int out of the string
			spec.add(Integer.parseInt(data.substring(0, data.indexOf(' '))));
			// substring pass the int
			data = data.substring(data.indexOf(' ') + 1);
		}
		
		return spec;
	}
	
	public static void main(String[] args) {
		ArrayList<Integer> spec = readSpectrum(readFileAsString("src/antibiotics/data.txt"));
		System.out.println(spec);
		int[] peptide = {97, 129, 129, 97};
		System.out.println(score(peptide, spec, false));
	}
}