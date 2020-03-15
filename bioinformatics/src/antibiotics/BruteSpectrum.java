package antibiotics;

//for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

//for all kinds of lists
import java.util.*;

/**
 * <h1>Brute Cyclopeptide Sequencing</h1>
 * Contains all the necessary methods to generate the sequence of a peptide from its ideal mass spectrum,
 * as well as some helper methods that calculate ideal spectrum of given peptides and the number of
 * peptides with a given total mass.
 * <br>
 * Some important terms:
 * <ul>
 * 		<li><strong>peptide</strong>: a chain of amino acids, here represented by an <code>ArrayList</code>
 * 									of integer mass values, in order</li>
 * 		<li><strong>subpeptide</strong>: a contiguous fragment of a peptide (does not necessarily appear in
 * 									the linear overall peptide as may wrap around from behind</li>
 * </ul>
 * @author faith
 *
 */
public class BruteSpectrum {
	/**
	 * stores all the possible masses of an amino acid (excluding duplicates) for easy access
	 */
	public static final int[] masses = new int[] {57, 71, 87, 97, 99, 101, 103, 113, 114, 115,
			128, 129, 131, 137, 147, 156, 163, 186};
	/**
	 * stores all masses that already have a number of peptides calculated
	 * @see peptidesWithMass
	 */
	public static Map<Integer, Long> massPoss = new HashMap<Integer, Long>();

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
	public static ArrayList<Integer> idealSpectrum(ArrayList<Integer> peptide, boolean cyclo) {
		// initialize return variable
		ArrayList<Integer> spec = new ArrayList<Integer>();
		// initialize array of prefix masses (masses up to this index in peptide)
		int[] prefixMass = new int[peptide.size() + 1];
		
		// put proper values in each index of prefixMass
		for (int i = 1; i <= peptide.size(); i++)
			// mass = mass of the previous stuff + mass of the amino acid at this point
			prefixMass[i] = prefixMass[i - 1] + peptide.get(i - 1);
		
		// store total mass of the entire peptide
		int totalMass = prefixMass[peptide.size()];
		// loop over each starting index in peptide
		for (int i = 0; i < peptide.size(); i++) {
			// loop over each index after i in peptide (including over-length)
			for (int j = i + 1; j <= peptide.size(); j++) {
				// add mass of this bit (prefix mass of stuff after - prefix mass of stuff before)
				spec.add(prefixMass[j] - prefixMass[i]);
				// if some indexes on each end could be used (checking for cyclic sub-peptides)
				if (cyclo && i > 0 && j < peptide.size())
					// add mass of that bit (total mass - mass of the insides)
					spec.add(totalMass - (prefixMass[j] - prefixMass[i]));
			}
		}
		// add 0 (if nothing was left)
		spec.add(0);
		// sort the whole thing
		Collections.sort(spec);
		
		return spec;
	}
	
	/**
	 * <h1>Calculates how many peptides have the given mass</h1>
	 * (accounting for order, but not for peptides with the same mass)
	 * <br>
	 * Recurvise function. End cases where mass is 0 (return 1), mass is less than smallest amino acid's
	 * mass (return 0), and mass has already been calculated (return that). Otherwise, adds up the number
	 * of peptides with masses equal to current mass minus each possible amino acid mass, and enter this
	 * value into the Map of already calculated masses.
	 * <br>
	 * precondition: mass >= 0
	 * <br>
	 * calls: itself
	 * @param mass the mass the peptide should have
	 * @return the number of peptides with this mass
	 * @see massPoss
	 */
	public static long peptidesWithMass(int mass) {
		// if the mass is 0, there is one possible peptide (empty amino acid)
		if (mass == 0) return 1;
		// if the mass is > 0 but < 57, no peptides are possible (no possible amino acids)
		else if (mass < 57) return 0;
		// if the number of peptides has already been calculated for this mass, use that
		else if (massPoss.containsKey(mass)) return massPoss.get(mass);
		
		// initialize counter
		long count = 0;
		// loop over all amino acid masses, calcuate number of peptides if each is used as a start
		for (int m : masses) count += peptidesWithMass(mass - m);
		// put newly calcuated number in the map
		massPoss.put(mass, count);
		
		return count;
	}
	
	/**
	 * <h1>Adds all possible masses to the end of given peptide</h1>
	 * Loops through all peptides, and for each loops though all masses in only, adding every combination of
	 * peptide + mass to expaneded, then returning it
	 * @param peptides the peptides to add to
	 * @param only an array of allowable amino acids to add
	 * @return all the expanded peptides
	 */
	@SuppressWarnings("unchecked")
	public static Set<ArrayList<Integer>> expand(Set<ArrayList<Integer>> peptides, int[] only) {
		// initialize return variable
		Set<ArrayList<Integer>> expanded = new HashSet<ArrayList<Integer>>();
		
		// used as placeholders in the loop
		int len;
		ArrayList<Integer> temp;
		// loop over each peptide
		for (ArrayList<Integer> peptide : peptides) {
			// calcuate its size (that's the index to add to)
			len = peptide.size();
			// copy it
			temp = (ArrayList<Integer>) peptide.clone();
			// add a placeholder for a new mass
			temp.add(0);
			// loop over all masses to add
			for (int mass : only) {
				// set the last index to this mass, add to expanded
				temp.set(len, mass);
				expanded.add((ArrayList<Integer>) temp.clone());
			}
		}
		
		return expanded;
	}
		
	/**
	 * <h1>Calculates the total mass of a peptide</h1>
	 * Loops through the peptide, adding each amino acid's mass up.
	 * @param peptide the peptide to calcuate the mass of (an <code>ArrayList</code> of masses)
	 * @return the total mass of the peptide
	 */
	public static int totalMass(ArrayList<Integer> peptide) {
		// initialize return variable
		int total = 0;
		// add each mass to total
		for (Integer amino : peptide) total += amino;
		
		return total;
	}
	
	/**
	 * <h1>Puts the first element of each <code>ArrayList</code> in a <code>Set</code> into an array</h1>
	 * Loops through all of the <code>ArrayLists</code>, adding their first elements to an array.
	 * <br>
	 * precondition: starts contains no empty <code>ArrayLists</code>
	 * @param starts the collections to pull data from
	 * @return an int[] array with the first element of... see above
	 */
	public static int[] getFirsts(Set<ArrayList<Integer>> starts) {
		// initialize return variable
		int[] firsts = new int[starts.size()];
		
		// index starts at 1
		int i = 0;
		// loop over all arraylists
		for (ArrayList<Integer> start : starts) {
			// add the first element to the array
			firsts[i] = start.get(0);
			// move the index forward
			i++;
		}
		
		return firsts;
	}
	
	/**
	 * <h1>Finds all peptides that match the given mass spectrum</h1>
	 * Maintains two <code>Set</code>s: peptides that match the spectrum and peptides currently being considered.
	 * From the latter, iteratively expands by all allowable masses (initially all amino acid masses), then adds
	 * to the former if cyclic specturms match. If not, then checks if linear specturms are consistent and
	 * throws out if not. After the first iteration updates allowable masses to be all current masses.
	 * <br>
	 * precondition: spectrum contains only combinations of actual amino acid masses
	 * <br>
	 * calls: expand, getFirsts, idealSpectrum, totalMass
	 * @param spectrum the spectrum that peptides should match
	 * @return all peptides with spectrums that match
	 */
	public static Set<ArrayList<Integer>> spectrumSequencing(ArrayList<Integer> spectrum) {
		// initialize return variable
		Set<ArrayList<Integer>> goodPeptides = new HashSet<ArrayList<Integer>>();
		// holds peptides that aren't quite good enough yet, initialized to the empty peptide
		Set<ArrayList<Integer>> canadiates = new HashSet<ArrayList<Integer>>();
		canadiates.add(new ArrayList<Integer>());
		
		// placeholders for the loop
		
		// the total mass of the final peptide
		int parentMass = spectrum.get(spectrum.size() - 1);
		// whether this is the first run or not
		boolean firstRun = true;
		// masses that may be added to the end of candidate peptides
		int[] only = masses.clone();
		// while still considering some peptides
		while (!canadiates.isEmpty()) {
			// expand all candidates by all allowable masses
			canadiates = expand(canadiates, only);
			
			// loop through all peptides
			for (Iterator<ArrayList<Integer>> i = canadiates.iterator(); i.hasNext();) {
				// save the current peptide
			    ArrayList<Integer> peptide = i.next();
			    
			    // if this peptide's mass equals the ideal mass
			    if (totalMass(peptide) == parentMass) {
			    	// if the cyclic spectrum of the peptide matches
					if (idealSpectrum(peptide, true).equals(spectrum)
					// and it hasn't been saved as good yet
							&& !goodPeptides.contains(peptide)) {
						// add it to return variable
						goodPeptides.add(peptide);
					}
					// remove current peptide from consideration
					i.remove();
				}
			    // or if this peptide's mass is too great
				else if (totalMass(peptide) > parentMass ||
				// or its linear spectrum is inconsistent with the given one
						!spectrum.containsAll(idealSpectrum(peptide, false)))
					// remove current peptide from consideration
					i.remove();
			}
			
			// if this is the first iteration
			if (firstRun) {
				// the only masses that can be added from now on are the current ones
				only = getFirsts(canadiates);
				// this is no longer the first iteration
				firstRun = false;
			}
		}
		
		return goodPeptides;
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
	 * <h1>Reads a mass spectrum from a string into an <code>ArrayList</code>
	 * Jumps through the data by spaces until none is left, parsing the integers between the spaces and
	 * adding them to the return variable.
	 * <br>
	 * precondition: data has only integers separated by spaces
	 * @param data the spectrum to read in
	 * @return the spectrum represented by an <code>ArrayList</code>
	 */
	public static ArrayList<Integer> readSpectrum(String data) {
		// initialize return varaible
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
	
	/**
	 * <h1>Prints many peptides (represented as an <code>ArrayList</code> of masses) to the console</h1>
	 * Loops over each peptide and prints it out with formatting.
	 * <br>
	 * precondition: peptides contains no empty <code>ArrayLists</code>
	 * @param peptides the peptides to print out
	 */
	public static void printPeptides(Collection<ArrayList<Integer>> peptides) {
		// loop over all peptides
		for (ArrayList<Integer> peptide : peptides) {
			// for all masses but the last, print out mass followed by "-"
			for (int i = 0; i < peptide.size() - 1; i++)
				System.out.print(peptide.get(i) + "-");
			// for last mass, print out mass followed by " 
			System.out.print(peptide.get(peptide.size() - 1) + " ");
		}
	}
	
	// where I call methods to test them
	public static void main(String[] args) {
	}
}