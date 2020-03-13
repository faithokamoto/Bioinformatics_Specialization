package reconstruction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * <h1>Contig Generator</h1>
 * Contains the requisite methods to produce all contigs given k-mers,
 * and also to output intermediate steps such as eulerian adjacency lists.
 * <br>
 * Some important terms:
 * <ul>
 * 		<li><strong>k-mer</strong>: string of length k (AATA has 3-mers AAT and ATA)</li>
 * 		<li><strong>prefix</strong>: a string, excluding the last char</li>
 * 		<li><strong>suffix</strong>: a string, excluding the first char</li>
 * 		<li><strong>adjacency list</strong>: a map with a node (k-mer) associated
 * 			with a list of k-mers that represent outgoing nodes</li>
 * 		<li><strong>Eulerian/DeBrujin adjacency list</strong>: adjacency list where the paths are k-mers
 * 			and the nodes on either end are the prefix and suffix of the path</li>
 * 		<li><strong>path</strong>: a list of k-mers in order that represents a longer string</li>
 * 		<li><strong>contig</strong>: a valid path/cycle through <strong>some</strong> k-mers in an array</li>
 * 		<li><strong>in-and-out node</strong>: a node that has one incoming and one outgoing path</li>
 * 		<li><strong>maximal non-branching path</strong>: a path with maximum length through an adjacency list where
 * 			only the ending nodes are allowed to be non-in-and-out</li>
 * </ul>
 * @author faith
 */
public class contigs {
	/**
	 * <h1>Constructs a genome given a path</h1>
	 * Takes the prefix of the first k-mer and then adds the last char of each k-mer.
	 * <br>
	 * precondition: kmer's substrings are all the same length
	 * @param kmers array of k-mers int order of appearance
	 * @return the genome
	 */
	public static String pathToGenome(ArrayList<String> kmers) {
		// initialize return variable to prefix of the first k-mer
		String genome = kmers.get(0).substring(0, kmers.get(0).length() - 1);
		
		// for each k-mer, add the last char to genome (only new bit)
		for (String k : kmers) genome += k.charAt(kmers.get(0).length() - 1);
		
		return genome;
	}
	
	/**
	 * <h1>Creates a HashMap mapping k-mers to whether they represent in-and-out nodes</h1>
	 * Loops over the adjacency list and notes number of incoming and outgoing nodes in
	 * a HashMap. Then loops over that HashMap to check if each node has 1 of each, if so,
	 * then that key's value in inAndOut is true.
	 * precondition: adjList contains no duplicate or empty keys
	 * @param adjList an adjacency list
	 * @return a HashMap with k-mers mapped to in-and-out-ness
	 */
	public static Map<String, Boolean> inAndOut(Map<String, ArrayList<String>> adjList) {
		// initialize return variable
		Map<String, Boolean> inAndOut = new HashMap<String, Boolean>();
		// tracks how many incoming and outgoing paths a node has
		Map<String, int[]> connections = new HashMap<String, int[]>();
		// used as a placeholder in the loop
		ArrayList<String> outs;
		
		// loop over all keys in the adjacency list
		for (String key : adjList.keySet()) {
			// save the outgoing nodes from this key
			outs = adjList.get(key);
			
			// initialize spot in connections for key if not there
			if (!connections.containsKey(key)) connections.put(key, new int[2]);
			// save, in the first index, how many outgoing paths this node has
			connections.get(key)[0] = outs.size();
			// loop over all outgoing nodes
			for (String o : outs) {
				// initialize spot in connections for key if not there
				if (!connections.containsKey(o)) connections.put(o, new int[2]);
				// add one, int the second index, to how many incoming paths this node has
				connections.get(o)[1]++;
			}
		}
		
		// loop over all keys in connections
		for (String key : connections.keySet()) {
			// if this node has one incoming and one outgoing node, it is true for inAndOut
			if (connections.get(key)[0] == 1 && connections.get(key)[1] == 1) inAndOut.put(key, true);
			// otherwise it isn't
			else inAndOut.put(key, false);
		}
		
		return inAndOut;
	}
	
	
	/**
	 * <h1>Creates a DeBrujin adjacency list given k-mers</h1>
	 * Loop through each k-mer, adding its prefix as a key (if not already there),
	 * then adding its suffix to the value's <code>ArrayList</code>.
	 * <br>
	 * precondition: all strings in kmers are the same length and not empty
	 * @param kmers an array of k-mers
	 * @return a deBrujin adjacency list
	 */
	public static Map<String, ArrayList<String>> deBru(String[] kmers) {
		// initialize return variable
		Map<String, ArrayList<String>> deBru = new LinkedHashMap<String, ArrayList<String>>();
		
		// used to track situation during the loop
		String prefix;
		// loop through all strings in kmers
		for (String key : kmers) {
			// save this string's prefix
			prefix = key.substring(0, key.length() - 1);
			
			// if this prefix isn't a key yet, initialize it
			if (!(deBru.get(prefix) != null)) deBru.put(prefix, new ArrayList<String>());
			
			// add the suffix as a after-kmer
			deBru.get(prefix).add(key.substring(1));
		}
		
		// initialize an iterator for the adjacency list
	    Iterator<Entry<String, ArrayList<String>>> iterator = deBru.entrySet().iterator();

	    // loop through the iterator, removing keys if they have no values
	    while (iterator.hasNext()) if (iterator.next().getValue().size() == 0) iterator.remove();
	    
		return deBru;
	}
	
	/**
	 * <h1>Finds all maximal non-branching paths given a eulerian adjacency list</h1>
	 * Initializes some helper maps, then first finds all contigs that start with a non-in-and-out
	 * node, and extends them until a like one is reached. Then finds all cycles of in-and-out nodes.
	 * <br>
	 * preconditions: adjList contains no duplicate or empty strings
	 * <br>
	 * calls: inAndOut
	 * @param adjList an eulerian adjacency list
	 * @return an <code>ArrayList</code> of contig paths (each stored in an <code>ArrayList</code>
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList<String>> maxNonBranch(Map<String, ArrayList<String>> adjList) {
		// initialize return varaible
		ArrayList<ArrayList<String>> contigs = new ArrayList<ArrayList<String>>();
		// get the in-and-out status of each node
		Map<String, Boolean> inAndOut = inAndOut(adjList);
		// will hold all of the nodes that are in-and-out that have not been used yet...
		ArrayList<String> unusedIAO = new ArrayList<String>();
		// initialized to all in-and-out nodes
		for (String key : inAndOut.keySet()) if (inAndOut.get(key)) unusedIAO.add(key);
		
		// used as placeholders in the loop
		ArrayList<String> contig = new ArrayList<String>();
		String next;
		// loop over all keys in adjList
		for (String node : adjList.keySet()) {
			// if this node is not an in-and-out node
			if (!inAndOut.get(node)) {
				// for each outgoing node
				for (String out : adjList.get(node)) {
					// start a contig with original node and the outgoing node
					contig.add(node);
					contig.add(out);
					next = out;
					
					// while the next node is an in-and-out node
					while (inAndOut.get(next)) {
						// remove it from the list of unused in-and-out nodes
						unusedIAO.remove(unusedIAO.indexOf(next));
						// grab its next node (there will only be one)
						next = adjList.get(next).get(0);
						// add next node to the contig
						contig.add(next);
					}
					
					// add this contig to the master list of contigs, get ride of it
					contigs.add((ArrayList<String>) contig.clone());
					contig.clear();
				}
			}
		}
		
		// while some in-and-out nodes are unused (must be in a loop with each other
		while (!unusedIAO.isEmpty()) {
			// get one and add it to a contig
			next = unusedIAO.remove(0);
			contig.add(next);
			
			// get the next node in this loop
			next = adjList.get(next).get(0);
			// while this loop hasn't been closed yet
			while (!contig.contains(next)) {
				// remove this node and add it to the contig
				unusedIAO.remove(unusedIAO.indexOf(next));
				contig.add(next);
				// get the next node
				next = adjList.get(next).get(0);
				
			}
			
			// close the loop
			contig.add(contig.get(0));
			// add contig to master list of contigs and get rid of it
			contigs.add((ArrayList<String>) contig.clone());
			contig.clear();
		}
		
		return contigs;
	}

	/**
	 * <h1>Generates all contigs present in some k-mers</h1>
	 * Creates an adjacency list, then finds all maximal non-branching paths in it.
	 * <br>
	 * precondition: kmers are all the same length and not empty
	 * <br>
	 * calls: deBru, maxNonBranch
	 * @param kmers an array of k-mers
	 * @return all contigs found in kmers
	 */
	public static ArrayList<ArrayList<String>> contigGenerator(String[] kmers) {
		// run maxNonBranch on the adjacency list of these k-mers
		return maxNonBranch(deBru(kmers));
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
	 * <h1>Writes contigs to a file</h1>
	 * Points a writer at a file, then finds each contigs's path and writes it.
	 * Adds a newline after each contig.
	 * <br>
	 * precondition: contigs contains no empty <code>ArrayList</code>s
	 * <br>
	 * calls: pathToGenome
	 * @param filename the path/name of the file to write to
	 * @param paths an <code>ArrayList</code> of paths
	 * @throws IOException if filename is not valid
	 */
	public static void writeContigsToFile(String filename, ArrayList<ArrayList<String>> contigs) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// for each contig
			for (ArrayList<String> path : contigs) {
				// write its path
				writer.write(pathToGenome(path));
				writer.write("\n");
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
	
	public static void main(String[] args) {
		String[] data = readFileAsString("src/reconstruction/data.txt").split("\n");
		writeContigsToFile("src/reconstruction/output.txt", contigGenerator(data));
	}
}