package spectrums;

// for data structures
import java.util.HashMap;
import java.util.ArrayList;
// for lazy gal's sorting
import java.util.Collections;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Can calculate the peptide corresponding to an ideal spectrum
 * @author faith
 */
public class SpectrumGraph {
	/**
	 * a mass->amino acid abbreviation lookup table
	 */
	private static final HashMap<Short, Character> MASS_TABLE = VectorGraph.MASS_TABLE;
	/**
	 * the adjacency list of this graph
	 */
	private HashMap<Integer, ArrayList<Integer>> adjList;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes adjList
	 * @param spectrum the ideal spectrum to sequence from
	 */
	public SpectrumGraph(ArrayList<Integer> spectrum) {
		initializeAdjList(spectrum);
	}
	
	/**
	 * Initialize adjList from a spectrum
	 * <br>
	 * Using all masses as the small mass, and all masses greater than the current
	 * small mass as the larger mass, adds path small->large if the difference
	 * corresponds to an amino acid mass
	 * @param spectrum a sorted ideal spectrum
	 */
	private void initializeAdjList(ArrayList<Integer> spectrum) {
		// initialize adjList to empty
		adjList = new HashMap<Integer, ArrayList<Integer>>();
		
		// loop over all indexes to use as the smaller value
		for (int small = 0; small < spectrum.size(); small++) {
			// loop over all indexes greater than small to use as the larger value
			for (int large = small + 1; large < spectrum.size(); large++) {
				// check if this difference corresponds to an amino acid
				if (MASS_TABLE.containsKey((short) (spectrum.get(large) - spectrum.get(small)))) {
					// if so, add a path between small and large
					adjList.putIfAbsent(spectrum.get(small), new ArrayList<Integer>());
					adjList.get(spectrum.get(small)).add(spectrum.get(large));
				}
			}
		}
	}
	
	/**
	 * Clones an ArrayList of ArrayLists
	 * <br>
	 * Loops through each ArrayList, copying it into a large clone
	 * @param old the ArrayList to clone
	 * @return a deep copy of old
	 */
	private ArrayList<ArrayList<Integer>> clone(ArrayList<ArrayList<Integer>> old) {
		// initialize return variable to the exact right size
		ArrayList<ArrayList<Integer>> clone = new ArrayList<ArrayList<Integer>>(old.size());
		// loop over each inner list, cloning it
		for (ArrayList<Integer> list : old) clone.add(new ArrayList<Integer>(list));
		
		return clone;
	}

	/**
	 * Finds all paths to sink starting from a given node
	 * <br>
	 * Going over all nodes coming out of the current one, calculates all paths
	 * and adds the current node to the beginning. Then saves the calculation
	 * in a dynamic programming variable so that it isn't computed again.
	 * @param start the prefix-label-weight of the node to start from
	 * @param pathsComputed all previously computed path sets
	 * @return all paths (ordered weights) to sink from this node
	 */
	private ArrayList<ArrayList<Integer>> allPaths(int start,
			HashMap<Integer, ArrayList<ArrayList<Integer>>> pathsComputed) {
		// if this node has already been computed, just return that
		if (pathsComputed.containsKey(start)) return pathsComputed.get(start);
		
		// initialize return variable
		ArrayList<ArrayList<Integer>> paths = new ArrayList<ArrayList<Integer>>();
		// if this is the last node
		if (!adjList.containsKey(start)) {
			// return a path containing only it
			paths.add(new ArrayList<Integer>(1));
			paths.get(0).add(start);
		}
		// otherwise
		else {
			// for each node after, get all paths
			for (Integer out : adjList.get(start))
				paths.addAll(allPaths(out, pathsComputed));
			
			// add this node to the beginning of all those paths
			for (ArrayList<Integer> path : paths) path.add(0, start);
		}
		
		// save this node's computation for reference
		pathsComputed.put(start, clone(paths));
		
		return paths;
	}
	
	/**
	 * Finds all paths from source to sink
	 * <br>
	 * Calls other allPaths on the source node and a new dynamic programming list
	 * @return  all paths (ordered weights) from source to sink
	 */
	private ArrayList<ArrayList<Integer>> allPaths() {
		return allPaths(0, new HashMap<Integer, ArrayList<ArrayList<Integer>>>());
	}
	
	/**
	 * Converts a path to a peptide
	 * <br>
	 * Loops path, adding all internal distance to a list
	 * @param path the path (ordered prefix weights) to convert
	 * @return the peptide (ordered amino acid weights)
	 */
	private ArrayList<Short> toPeptide(ArrayList<Integer> path) {
		// initialize return variable
		ArrayList<Short> peptide = new ArrayList<Short>(path.size() - 1);
		// add each difference in as a weight
		for (int i = 0; i < path.size() - 1; i++)
			peptide.add((short) (path.get(i + 1) - path.get(i)));
		
		return peptide;
	}
	
	/**
	 * Generates an ideal mass spectrum for a given peptide
	 * <br>
	 * First calculates the prefix mass for each index in peptide (mass up to that point). Then loop over each
	 * subpeptide (indexes i = 0 & j = i + 1) and adds its mass, as calculated by the prefix masses.
	 * <br>
	 * @param peptide the peptide to generate a spectrum for
	 * @return a sorted list of each molecular mass in the ideal spectrum
	 */
	private ArrayList<Integer> idealSpectrum(ArrayList<Short> peptide) {
		// initialize return variable
		ArrayList<Integer> spec = new ArrayList<Integer>();
		// initialize array of prefix masses (masses up to this index in peptide)
		int[] prefixMass = new int[peptide.size() + 1];
		
		// put proper values in each index of prefixMass
		for (int i = 1; i <= peptide.size(); i++)
			// mass = mass of the previous stuff + mass of the amino acid at this point
			prefixMass[i] = prefixMass[i - 1] + peptide.get(i - 1);
		
		// loop over each starting index in peptide
		for (int i = 0; i < peptide.size(); i++) {
			// loop over each index after i in peptide (including over-length)
			for (int j = i + 1; j <= peptide.size(); j++) {
				// add mass of this bit (prefix mass of stuff after - prefix mass of stuff before)
				spec.add(prefixMass[j] - prefixMass[i]);
			}
		}
		// add 0 (if nothing was left)
		spec.add(0);
		// sort the whole thing
		Collections.sort(spec);
		
		return spec;
	}
	
	/**
	 * Checks if a spectrum is consistent with the adjacency list
	 * <br>
	 * Loops over the keys of adjList, checking if the spectrum contains each
	 * @param spectrum the spectrum to check
	 * @return whether the spectrum could have produced the adjList
	 */
	private boolean isConsistant(ArrayList<Integer> spectrum) {
		// loop over all adjList values
		for (Integer node : adjList.keySet()) 
			// if not in spectrum, is not consistent
			if (!spectrum.contains(node)) return false;
		
		return true;
	}
	
	/**
	 * Converts a weight-peptide to an amino-peptide
	 * <br>
	 * Loops over the peptide, consulting the mass table for
	 * the amino acid to use in the weight's place
	 * @param peptide the weights of the amino acids
	 * @return the peptide as amino acids
	 */
	private String toAmino(ArrayList<Short> peptide) {
		// initailize return variable
		String amino = "";
		// loop over all masses, checking against mass table
		for (Short mass : peptide) amino += MASS_TABLE.get(mass);
		
		return amino;
	}

	/**
	 * Finds a peptide consistent with the adjacency list given
	 * <br>
	 * Considers all paths from source to sink, then returns the amino
	 * acid form if consistent with the spectrum/adjacency list
	 * @return a peptide with the proper ideal spectrum
	 */
	public String findPeptide() {
		// loop over all paths from source to sink
		for (ArrayList<Integer> path : allPaths()) {
			// convert this path to a peptide
			ArrayList<Short> peptide = toPeptide(path);
			// if this peptide is consistent, return its amino acid for
			if (isConsistant(idealSpectrum(peptide))) return toAmino(peptide);
		}
		
		// if no paths were good, throw an exception
		throw new IllegalStateException("No peptide consistant with this spectrum found");
	}
	
	/**
	 * Writes the adjacency list to a file
	 * <br>
	 * Loops over each path-start, and then each end for the current
	 * path-start, writing the whole path to a file
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		// try to write to the file
		try {
			// point a writer at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			// loop over all path starts, and all ends to those starts
			for (Integer start : adjList.keySet()) for (Integer end : adjList.get(start))
				// write this path out
				writer.write(start + "->" + end + ":" + MASS_TABLE.get((short) (end - start)) + "\n");
				
			// clean up
			writer.close();
		}
		// if something fishy occurs, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}