package reconstruction;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

// for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

// for all kinds of lists
import java.util.*;

/**
 * <h1>Gapped Genome Reconstruction</h1>
 * Contains the requisite methods to reconstruct a genome given kd-mers,
 * and also to output intermediate steps such as eulerian adjacency lists.
 * <br>
 * Some important terms:
 * <ul>
 * 		<li><strong>k-mer</strong>: string of length k (AATA has 3-mers AAT and ATA)</li>
 * 		<li><strong>kd-mer</strong>: a pair of k-mers separated by d intermediate chars
 * 			(ATAGTG has (2,1)-mers AT|GT and TA|TG)</li>
 * 		<li><strong>prefix</strong>: a string, excluding the last char</li>
 * 		<li><strong>suffix</strong>: a string, excluding the first char</li>
 * 		<li><strong>adjacency list</strong>: a map with a node (k-mer) associated
 * 			with a list of k-mers that represent outgoing nodes</li>
 * 		<li><strong>Eulerian/DeBrujin adjacency list</strong>: adjacency list where the paths are k-mers
 * 			and the nodes on either end are the prefix and suffix of the path</li>
 * 		<li><strong>path</strong>: a list of k-mers in order that represents a longer string</li>
 * </ul>
 * @author faith
 */
class gapped {
	/**
	 * <h1>Constructs a genome given a path</h1>
	 * Takes the prefix of the first k-mer and then adds the last char of each k-mer.
	 * <br>
	 * precondition: kmer's substrings are all the same length
	 * @param kmers array of k-mers int order of appearance
	 * @return the genome
	 */
	public static String pathToGenome(String[] kmers) {
		// initialize return variable to prefix of the first k-mer
		String genome = kmers[0].substring(0, kmers[0].length() - 1);
		
		// for each k-mer, add the last char to genome (only new bit)
		for (String k : kmers) genome += k.charAt(kmers[0].length() - 1);
		
		return genome;
	}
	
	/**
	 * <h1>Concatenates the prefixes of a concatenated kd-mer</h1>
	 * Calculates k, and then pulls the first (k-1) chars from each half of the string.
	 * <br>
	 * precondition: kdmer not empty, kdmer has an even length
	 * @param kdmer a kd-mer
	 * @return the prefixes of the k-mers in the kd-mer, concatenated
	 */
	public static String prefix(String kdmer) {
		// calculate k
		int k = kdmer.length() / 2;
		// pull the two prefixes, concatenate
		return kdmer.substring(0, k - 1) + kdmer.substring(k, k * 2 - 1);
	}
	
	/**
	 * <h1>Concatenates the suffixes of a concatenated kd-mer</h1>
	 * Calculates k, then pulls the last (k-1) chars from each half of the string.
	 * <br>
	 * precondition: kdmer not empty, kdmer has an even length
	 * @param kdmer a kd-mer
	 * @return the suffixes of the k-mers in the kd-mer, concatenated
	 */
	public static String suffix(String kdmer) {
		// calculate k
		int k = kdmer.length() / 2;
		// pull the two suffixes, concatenate
		return kdmer.substring(1, k) + kdmer.substring(k + 1);
	}
	
	/**
	 * <h1>Creates a DeBrujin adjacency list given kd-mers</h1>
	 * Loop through each kd-mer, adding its prefix as a key (if not already there),
	 * then adding its suffix to the value's <code>ArrayList</code>.
	 * <br>
	 * precondition: all strings in kdmers are the same length and not empty
	 * calls: prefix, suffix
	 * @param kdmers an array of kd-mers
	 * @return a deBrujin adjacency list
	 */
	public static Map<String, ArrayList<String>> createDeBruAdjList(String[] kdmers) {
		// initialize return variable
		Map<String, ArrayList<String>> deBru = new HashMap<String, ArrayList<String>>();
		
		// used as a placeholder in loop
		String prefix;
		// loop through all strings in kdmers
		for (String key : kdmers) {
			// save this kd-mer's prefix
			prefix = prefix(key);
			
			// if this prefix isn't a key yet, initialize it
			if (!(deBru.get(prefix) != null)) deBru.put(prefix, new ArrayList<String>());
			// add the suffix as a after-kd-mer
			deBru.get(prefix).add(suffix(key));
		}
	    
		return deBru;
	}
	
	/**
	 * <h1>Checks if unused edges remain in an adjacency list</h1>
	 * Iterates over each key and checks if that <code>ArrayList</code> is empty, returning true on
	 * the first non-empty <code>ArrayList</code> - or false if none return true.
	 * @param map an adjacency list
	 * @return if any nodes have unused edges (non-empty arraylists)
	 */
	public static boolean hasMoreEdges(Map<String, ArrayList<String>> map) {
		// loop over each key
        for (String key : map.keySet())
        	// if this key's arraylist is not empty, return true
        	if (!map.get(key).isEmpty()) return true;
        	
        // if all arraylists were empty, no unused edges remain
        return false;
	}
	
	/**
	 * <h1>Finds an eulerian cycle given a starting node and a map of unused edges</h1>
	 * Creates an <code>ArrayList</code> representing a cycle, beginning it with start. Then it loops, extending
	 * cycle by a random outgoing node and updating all relevant variables, until the current node
	 * has no outgoing nodes to add on. Finally, if extending is true, the first node is removed
	 * (since it will have been the last node of the pre-existing cycle).
	 * <br>
	 * preconditions: start is a key in unused, no strings are empty
	 * @param start a string to start the cycle at
	 * @param unused an adjacency list of unused edges
	 * @param unusedCycle an <code>ArrayList</code> of nodes with unused edges that are on the current cycle
	 * @param extending whether or not this new cycle is an extension of an existing one
	 * @return a eulerian cycle based off of unused
	 */
	public static ArrayList<String> newCycle(String start, Map<String, ArrayList<String>> unused,
			ArrayList<String> unusedCycle, boolean extending) {
		// initialize return variable
		ArrayList<String> cycle = new ArrayList<String>();
		// the start of cycle should just be start
		cycle.add(start);
		
		// used as placeholders in the loop
		String latest;
		
		// loop until stuck
		while (true) {
			// save the last kd-mer added to cycle (to be used as a key)
			latest = cycle.get(cycle.size() - 1);
			// if this string has values left (not "stuck")
			if (unused.get(latest).size() != 0) {
				// grab a value from the arraylist of possible next kd-mers, remove it since it's been used
				cycle.add(unused.get(latest).remove((int) (Math.random() * unused.get(latest).size())));
				
				// if the kd-mer used as a key still has values, and is not listed as unusedCycle, add it
				if (unused.get(latest).size() != 0 && !unusedCycle.contains(latest))
					unusedCycle.add(latest);
				// if the kd-mer used as a key no longer has values, and is listed as unsusedCycle, delete it
				else if (unusedCycle.contains(latest) && unused.get(latest).size() == 0)
					unusedCycle.remove(latest);
			}
			
			// if stuck, leave!
			else break;
		}
		
		if (extending) cycle.remove(0);
		
		return cycle;
	}
	
	/**
	 * <h1>Rotates an <code>ArrayList</code> to start and end at a certain value</h1>
	 * Takes out the first value, then removes each value before newStart and places it
	 * at the end of cycle. Finally, adds newStart to the end of the cycle.
	 * <br>
	 * precondition: newStart is contained in cycle
	 * @param cycle an <code>ArrayList</code> representing a cycle
	 * @param newStart the string to start the cycle at
	 */
	public static void moveCycle(ArrayList<String> cycle, String newStart) {
		// take out the first value in cycle (should be repeated on the end)
		cycle.remove(0);
		// find where newStart is in the string
		int curIndex = cycle.indexOf(newStart);
		
		// used as a placeholder in the loop
		String temp;
		// for each value before curIndex
		for (int i = 0; i < curIndex; i++) {
			// move first value in cycle around to the back
			temp = cycle.remove(0);
			cycle.add(temp);
		}
		
		// add the new starting value to the end
		cycle.add(newStart);
	}
	
	/**
	 * <h1>Deep-copies an adjacency list</h1>
	 * Iterates through original, adding each key along with a deep-copied <code>ArrayList</code>.
	 * @param original the map to copy
	 * @return a deep-copy of the map
	 */
	public static Map<String, ArrayList<String>> copy(Map<String, ArrayList<String>> original) {
		// initialize return variable
		Map<String, ArrayList<String>> copy = new HashMap<String, ArrayList<String>>();
		// loop through the original's keys
		for (String key : original.keySet())
			// put this key, and a copy of its value, into copy
			copy.put(key, new ArrayList<String>(original.get(key)));
		return copy;
	}
	
	/**
	 * <h1>Finds an eulerian cycle given an adjacency list.</h1>
	 * Initializes an empty cycle, an adjacency map of unused edges, and an <code>ArrayList</code> of
	 * nodes on cycle with unused edges. Then, as long as some edges are unused, extends the cycle
	 * and chooses a new starting node from a list of nodes on cycle with unused edges, rotates the cycle
	 * to begin on that node, and extends the cycle again.
	 * <br>
	 * preconditions: keys in euler have non-empty <code>ArrayList</code>s, euler has a eulerian cycle in it
	 * <br>
	 * calls: copy, hasMoreEdges, moveCycle, newCycle
	 * @param euler an adjacency list
	 * @param start a string to begin the cycle at (allows different starts to be forced)
	 * @return an <code>ArrayList</code> representing a eulerian cycle, with nodes in order of use
	 * @see newCycle
	 */
	public static ArrayList<String> eulerToCycle(Map<String, ArrayList<String>> euler, String start) {
		// initialize return variable
		ArrayList<String> cycle = new ArrayList<String>();
		// not that all edges are unused
		Map<String, ArrayList<String>> unused = copy(euler);
		// there are no keys in cycle with unused values (since there are no keys in cycle)
		ArrayList<String> unusedCycle = new ArrayList<String>();
		
		// get an initial cycle 
		cycle.addAll(newCycle(start, unused, unusedCycle, false));
		// used as a placeholder in the loop
		int newStart;
		// while unused edges remain
		while (hasMoreEdges(unused)) {
			// get a random new start from a key with unused values
			newStart = (int) (Math.random() * unusedCycle.size());
			// rotate the cycle so that it starts/ends on a key with unused values
			moveCycle(cycle, unusedCycle.get(newStart));
			// extend the cycle
			cycle.addAll(newCycle(unusedCycle.get(newStart), unused, unusedCycle, true));
		}
		
		return cycle;
	}
	
	/**
	 * <h1>Finds an eulerian path given an adjacency list</h1>
	 * Deep-copies the adjacency list, then gets a cycle, rotates so that it begins at start, and deletes
	 * the start node that will be on the end.
	 * <br>
	 * precondition: euler has already been processed into a cycle, start is a key in euler, all strings 
	 * are non-empty
	 * <br>
	 * calls: copy, eulerToCycle, moveCycle
	 * @param euler an adjacency list representing an eulerian cycle
	 * @param start the first node of the path
	 * @return an <code>ArrayList</code> representing a eulerian path, with nodes in order of use
	 */
	public static ArrayList<String> eulerToPath(Map<String, ArrayList<String>> euler, String start) {
		// deep-copy adjacency list
		Map<String, ArrayList<String>> path = copy(euler);

        // make a cycle with the cyclic path
        ArrayList<String> cycle = eulerToCycle(path, start);
        // rotate the cycle so it starts at start
        moveCycle(cycle, start);
        // remove last node (it will be start, this gets rid of the added edge)
        cycle.remove(cycle.size() - 1);

		return cycle;
	}
	
	/**
	 * <h1>Processes an adjacency list into a cycle</h1>
	 * Creates a count map, then loops over all keys and, to said map, adds 1 for each outgoing path
	 * and subtracts one for each incoming path for a node. Then checks the count matrix for the
	 * beginning and ending nodes, and adds a path from the end to the beginning.
	 * <br>
	 * precondition: all strings in path are non-empty
	 * @param path an adjacency list
	 * @return the String key of the first node in the path
	 */
	public static String makeCycle(Map<String, ArrayList<String>> path) {
		// used to save the beginning and ending nodes
		String start = "";
		String end = "";
		// for each node in path, tracks outgoing - incoming edges
		Map<String, Integer> count = new HashMap<String, Integer>();
		
		// loop over path's keys
        for (String key : path.keySet())  {
        	// add the number of outgoing paths to count at key
        	if (count.get(key) != null) count.put(key, count.get(key) + path.get(key).size());
        	else count.put(key, path.get(key).size());
        	
        	// loop over all outgoing paths from key
        	for (String out : path.get(key)) {
        		// if this outgoing path's node has not outgoing paths itself, it is the end node
        		if (!(path.get(out) != null)) end = out;
        		
        		// decrement this path's node
        		else if (count.get(out) != null) count.put(out, count.get(out) - 1);
        		else count.put(out, -1);	
        	}
        }
        
        // loop over count
        for (String key : count.keySet()) {
        	// if there is one extra outgoing path, this node is the start
        	if (count.get(key) == 1) start = key;
        	// if there is one extra incoming path, this node is the end
        	if (count.get(key) == -1) end = key;
        }
        
        // as long as this is a true path (end != start)
        if (!end.equals(start)) {
	        // add an edge going from end to start in path
	        if (!(path.get(end) != null)) path.put(end, new ArrayList<String>());
	        path.get(end).add(start);
        }
        
        return start;
	}
	
	/**
	 * <h1>Reconstructs a long string out of kd-mers</h1>
	 * Turns kdmers into an adjacency list, and processes that adjacency list into a path. Then loops,
	 * creating a possible new path and checking for validity until the path is valid.
	 * preconditions: kdmers contains no empty strings, all strings in kdmers are of equal length,
	 * kdmer's length > d, d > 0
	 * <br>
	 * calls: createDeBruAdjList, eulerToPath, gappedToGenome, makeCycle, prefix 
	 * @param kdmers String array of kd-mers to construct a string out of
	 * @param d the distance between each k-mer in the kd-mers
	 * @return a genome with kd-mer composition equal to kd-mers
	 */
	public static String genomeReconstruction(String[] kdmers, int d) {
		// create an adjacency list based on kmers
		Map<String, ArrayList<String>> adjList = createDeBruAdjList(kdmers);
		// turn that adjacency list into a path
		String start = makeCycle(adjList);
		
		// placeholder for the loop
		String genome;
		do {
			// find a path through the kdmers based on the adjacency list
			ArrayList<String> path = eulerToPath(adjList, start);
			// reconstruct the genome based on the path
			genome = gappedToGenome(path, d + 1);
		}
		// repeat if that path was not valid
		while(genome.equals("fail"));
		
		return genome;
	}

	/**
	 * <h1>Reconstructs a long string given many kd-mers</h1>
	 * Separates the kd-mers into two lists (first k-mer and second k-mer). Then constructs a 
	 * prefix and suffix given those lists, and compares the two to check validity.
	 * <br>
	 * precondition: kd-mers in kdmers are valid kd-mers, d > 0, kdmers's length > d
	 * <br>
	 * calls: pathToGenome
	 * @param kdmers <code>ArrayList</code> of kd-mers in order
	 * @param d distance between k-mers in kd-mers
	 * @return the genome is kd-mers is valid, "fail" if not
	 */
	public static String gappedToGenome(ArrayList<String> kdmers, int d) {
		// save the value of k
		int k = kdmers.get(0).length() / 2;
		
		// pull kdmers into two lists
		String[] firsts = new String[kdmers.size()];
		String[] lasts = new String[kdmers.size()];
		for (int i = 0; i < kdmers.size(); i++) {
			firsts[i] = kdmers.get(i).substring(0, k);
			lasts[i] = kdmers.get(i).substring(k);
		}
		
		// construct a prefix and suffix of the genome based on those lists
		String prefix = pathToGenome(firsts);
		String suffix = pathToGenome(lasts);
		// check through the chars that should overlap in prefix and suffix
		for (int i = k + d; i < prefix.length(); i++)
			// if any don't throw a warning
			if (prefix.charAt(i) != suffix.charAt(i - k - d)) return "fail";
		
		// if all overlap, return prefix + the end of suffix
		return prefix + suffix.substring(suffix.length() - k - d);
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
	 * <h1>Writes a genome to a file</h1>
	 * Points a writer at the file, then prints each char, with a newline after each 100.
	 * @param filename the path/name of a file to write to
	 * @param genome the genome to write to the file
	 */
	public static void writeGenomeToFile(String filename, String genome) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// tracks whether a newline should be added
			int line = 0;
			// loop over whole genome
			for (int i = 0; i < genome.length(); i++) {
				// write current char
				writer.write(genome.charAt(i));
				line++;
				
				// if 200 chars have been written, reset counter and write a newline
				if (line == 200) {
					writer.write("\n");
					line -= 200;
				}
			}
	        
	        // clean up
		    writer.flush();
		    writer.close();  
		}
		
		// if that didn't work, explain why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// where I call methods to test them
	public static void main(String[] args) {
		String data = readFileAsString("src/reconstruction/data.txt");
		String[] kdmers = data.replace(" ", "").replace("(", "").replace(")", "").replace("|", "").split("\n");
		writeGenomeToFile("src/reconstruction/output.txt", genomeReconstruction(kdmers, 1));
	}
}