package reversals;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

// for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

// for lists that can change size
import java.util.ArrayList;
import java.util.HashMap;

public class Genomes {	
	/**
	 * <h1>Converts a sequence of blocks to ordered nodes</h1>
	 * Loops over the blocks, using sign and value to input nodes.
	 * @param chrom the sequence of blocks (+/- for direction)
	 * @return ordered nodes corresponding to that sequence of blocks
	 */
	public static int[] chromToCycle(int[] chrom) {
		// initialize return variable
		int[] nodes = new int[chrom.length * 2];
		// loop over all blocks in chrom
		for (int i = 0; i < chrom.length; i++) {
			// save the current block
			int block = chrom[i];
			
			// if it is positive
			if (block > 0) {
				// add the appropriate nodes
				nodes[2 * i] = 2 * block - 1;
				nodes[2 * i + 1] = 2 * block;
			}
			// if it is negative
			else {
				// add the appropriate nodes
				nodes[2 * i] = -2 * block;
				nodes[2 * i + 1] = -2 * block - 1;
			}
		}
		
		return nodes;
	}
	
	/**
	 * <h1>Generates the colored edges between a sequence of blocks</h1>
	 * Cyclizes the chromosomes, then loops over and adds each middle edge and the loop-around edge.
	 * @param genome a number of sequences of blocks
	 * @return the colored edges in the graph, as ordered pairs of nodes
	 */
	public static ArrayList<int[]> coloredEdges(ArrayList<int[]> genome) {
		// initialize return variable
		ArrayList<int[]> ce = new ArrayList<int[]>();
		// loop over all chromosomes
		for (int[] chrom : genome) {
			// cyclize this chromosome
			int[] nodes = chromToCycle(chrom);
			// loop over the chromosome, adding middle edges
			for (int i = 1; i < nodes.length - 1; i += 2)
				ce.add(new int[] {nodes[i], nodes[i + 1]});
			// add a wrap-around edge
			ce.add(new int[] {nodes[nodes.length - 1], nodes[0]});
		}
		
		return ce;
	}
		
	/**
	 * <h1>Converts a graph to a sequence of blocks</h1>
	 * Loops over each edge-pair in the graph, checking if the one after is connected by an edge.
	 * If so, adds the edge, if not, adds an edge to the first pair of the current chromosome and
	 * then adds a new chromosome. Converts the <code>ArrayList</code>s of <code>Integer</code>s 
	 * into <code>int[]</code>s.
	 * @param graph colored edges on a graph (pairs of connected nodes)
	 * @return multiple sequences of blocks (chromosomes) making up a genome
	 */
	public static ArrayList<int[]> graphToGenome(ArrayList<int[]> graph) {
		// initialize ArrayList version of return variable
		ArrayList<ArrayList<Integer>> genome = new ArrayList<ArrayList<Integer>>();
		// add an empty chromosome to the genome
		genome.add(new ArrayList<Integer>());

		// the index in graph where the cycle starts
		int startCycle = 0;
		// loop over all pairs in graph
		for (int i = 0; i < graph.size(); i++) {
			// if the current pair is connected to the next by a backwards edge, add it
			if (i != graph.size() - 1 && graph.get(i)[1] - graph.get(i + 1)[0] == 1) 
				genome.get(genome.size() - 1).add(-graph.get(i)[1] / 2);
			// if the current pair is connected to the enxt by a forwards edge
			else if (i != graph.size() - 1 && graph.get(i)[1] - graph.get(i + 1)[0] == -1) 
				genome.get(genome.size() - 1).add(graph.get(i + 1)[0] / 2);
			
			// if the current pair is the end of this chromosome
			else {
				// if the current pair is connected by a backwards edge to the first pair
				if (graph.get(i)[1] - graph.get(startCycle)[0] == 1) 
					genome.get(genome.size() - 1).add(0, -graph.get(i)[1] / 2);
				// if the current pair is connected by a forward edge to the first pair
				else genome.get(genome.size() - 1).add(0, graph.get(startCycle)[0] / 2);
				
				// add a new empty chromosome
				genome.add(new ArrayList<Integer>());
				// the next chromosome will start at the next index
				startCycle = i + 1;
			}
		}
		
		// get rid of the last empty chromosome
		genome.remove(genome.size() - 1);
		// converting genome into ArrayList<int[]
		ArrayList<int[]> arrGenome = new ArrayList<int[]>();
		for (ArrayList<Integer> chrom : genome) {
			// i have no idea how stream works
			arrGenome.add(chrom.stream().mapToInt(Integer::intValue).toArray());
		}
		
		return arrGenome;
	}
	
	/**
	 * <h1>Checks if two length-2 <code>int[]</code> arrays contain the same values</h1>
	 * @param a one length-2 arrary
	 * @param b another length-2 array
	 * @return whether the values match
	 */
	public static boolean pairEquals(int[] a, int[] b) {
		// check for either orientation
		return (a[0] == b[0] && a[1] == b[1]) || (a[1] == b[0] && a[0] == b[1]);
	}
	
	/**
	 * <h1>Calculates the number of 2-breaks necessary to transform one genome into another</h1>
	 * Gets all colored edges, then loops through them to find connections, incrementing cycles and
	 * reseting the check-index back to 0 for each edge connection found and removed. Then
	 * calculates blocks - cycles.
	 * @param genome1 one group of sequences of blocks
	 * @param genome2 another group of sequences of blocks
	 * @return the number of 2-breaks needed for transformation
	 */
	public static int count2Break(ArrayList<int[]> genome1, ArrayList<int[]> genome2) {
		// get all colored edges from the two genomes
		ArrayList<int[]> ce = coloredEdges(genome1);
		ce.addAll(coloredEdges(genome2));
		
		// calculate the number of blocks involved
		int blocks = ce.size() / 2;
		// grab the first edge to use
		int[] edge = ce.remove(0);
		// grab the node from that edge that should connect to another
		int connection = edge[1];
		
		// the index being checked starts at the beginning
		int i = 0;
		// the number of cycles found starts at 0
		int cycles = 0;
		// while some edges are left to check
		while (!ce.isEmpty()) {
			// if the edges ran out (means the edge needed was removed already)
			if (i == ce.size()) {
				// a cycle has been completed
				cycles++;
				// reset i, edge, connection
				i = 0;
				edge = ce.remove(i);
				connection = edge[1];
			}
			
			// if the current edge makes a trivial cycle
			else if (pairEquals(edge, ce.get(i))) {
				// a cycle has been completed
				cycles++;
				// remove the edge and reset i
				ce.remove(i);
				i = 0;
				// as long as this wasn't the alst edge
				if (!ce.isEmpty()) {
					// reset edge and connection
					edge = ce.remove(i);
					connection = edge[1];
				}
			}
			
			// if a connection was made with the first node's first edge
			else if (connection == ce.get(i)[0]) {
				// reset edge to the next edge in the cycle, connection, i
				edge = ce.remove(i);
				connection = edge[1];
				i = 0;
				// if this was the last edge, a cycle has been completed
				if (ce.isEmpty()) cycles++;
			}
			
			// similar for the first node's second edge
			else if (connection == ce.get(i)[1]) {
				edge = ce.remove(i);
				connection = edge[0];
				i = 0;
				if (ce.isEmpty()) cycles++;
			}
			
			// if no checks went through, move checking index forward
			else i++;
		}
		
		// the number of 2-breaks is blocks - cycles
		return blocks - cycles;
	}
	
	/**
	 * <h1>Checks if two numbers are connected by a black edge</h1>
	 * Black edges have an even larger number, so checks for that.
	 * @param a one number
	 * @param b another number
	 * @return if these numbers could "come after" each other in an edge graph
	 */
	public static boolean comesAfter(int a, int b) {
		// check both cases (a > b, b > a)
		return (a - b == 1 && b % 2 == 1) || (b - a == 1 && a % 2 == 1);
	}
	
	/**
	 * <h1>Performs a 2-break on a set of edges</h1>
	 * Loops through edges, checking if the current edge comes after the last one sorted.
	 * If so, adds it to the list of sorted edges, continuing until all edges have been
	 * sorted. Removes (a, b) & (c, d), adding (a, c) and (b, d)
	 * @param edges a list of edges (node1, node2)
	 * @param a a positive number
	 * @param b a number that forms an existing colored edge with a (a, b)
	 * @param c a positive number
	 * @param d a number that forms an existing colored edge with c (c, d)
	 */
	public static void graph2Break(ArrayList<int[]> edges, int a, int b, int c, int d) {
		// initialize a storage place for edges, sorted
		ArrayList<int[]> sorted = new ArrayList<int[]>(edges.size());
		
		// add one of the new edges as the beginning point of sorted
		sorted.add(new int[] {a, c});
		// add the other new edge to the unsorted edges
		edges.add(new int[] {b, d});
		// while some edges have to be sorted
		while (!edges.isEmpty()) {
			// nothing has happened so far this time through the loop
			boolean stuck = true;
			
			// loop over edges from front (since it is mostly sorted in that direction)
			for (int i = 0; i < edges.size(); i++) {
				// save the last node used
				int last = sorted.get(sorted.size() - 1)[1];
				
				// if this edge is supposed to be removed
				if (pairEquals(edges.get(i), new int[] {a, b}) ||
						pairEquals(edges.get(i), new int[] {c, d})) {
					// remove it
					edges.remove(i);
					i--;
				}
				// if it comes after the last node used
				else if (comesAfter(edges.get(i)[0], last)) {
					// add it to sorted (it is in order)
					sorted.add(edges.remove(i));
					i--;
					// some change came to sorted this loop through
					stuck = false;
				}
				// similar logic if its reversed form comes after the last node used
				else if (comesAfter(edges.get(i)[1], last)) {
					sorted.add(new int[] {edges.get(i)[1], edges.get(i)[0]});
					edges.remove(i);
					i--;
					stuck = false;
				}
			}
			
			// if nothing happen last time through the loop, jump-start new chromosome
			if (stuck) sorted.add(edges.remove(edges.size() - 1));
		}
		
		// edges is empty, set it to sorted
		edges.addAll(sorted);
	}
	
	/**
	 * <h1>Performs a 2-break on a sequence of blocks</h1>
	 * Converts the genome into edges, 2-breaks those edges, then converts back to a genome.
	 * @param genome a sequence of blocks (+/- for direction)
	 * @param a a positive number
	 * @param b a number that forms an existing colored edge with a (a, b)
	 * @param c a positive number
	 * @param d a number that forms an existing colored edge with c (c, d)
	 */
	public static void genome2Break(ArrayList<int[]> genome, int a, int b, int c, int d) {
		// create the list of colored edges
		ArrayList<int[]> graph = coloredEdges(genome);
		// 2-break those edges
		graph2Break(graph, a, b, c, d);
		// convert the new set of edges back to a genome
		genome.clear();
		genome.addAll(graphToGenome(graph));
	}
	
	/**
	 * <h1>Converts one genome into another using 2-breaks</h1>
	 * Creates the edge-lists for the two genomes, deleting the ones that match from one set,
	 * and iteratively 2-breaks edges from the first set that flank non-trivial edges from
	 * the second, deleting any trivial edges from the second as the first changes. Each step
	 * of this process is output in genome form.
	 * @param red the first genome (which will be modified)
	 * @param blue the second genome (the target)
	 */
	public static void shortestRearrangement(ArrayList<int[]> red, ArrayList<int[]> blue) {
		// output the initial state of the red genome
		writeGenomeToFile("src/reversals/output.txt", red);
		// calculate the red and blue edges from their respective genomes
		ArrayList<int[]> redEdges = coloredEdges(red);
		ArrayList<int[]> blueEdges = coloredEdges(blue);
		
		// loop through every red edge
		for (int[] edge : redEdges) {
			// loop through every blue edge
			for (int i = 0; i < blueEdges.size(); i++) {
				// if this blue edge forms a trivial cycle with the red edge
				if (pairEquals(blueEdges.get(i), edge)) {
					// remove it, and break for a new red edge
					blueEdges.remove(i);
					break;
				}
			}
		}
		
		// while some blue edges remain in non-trivial cycles
		while (!blueEdges.isEmpty()) {
			// pull an edge from blueEdges
			int[] mid = blueEdges.remove(blueEdges.size() - 1);
			// initialize the nodes to 0 (which doesn't exist)
			int a = 0, b = 0, c = 0, d = 0;
			
			// loop over all edges in redEdges
			for (int[] edge : redEdges) {
				// if this edge, reversed, comes before mid
				if (edge[0] == mid[0]) {
					// set a and b to the proper nodes
					a = edge[1];
					b = edge[0];
				}
				// if this edge comes before mid
				else if (edge[1] == mid[0]) {
					a = edge[0];
					b = edge[1];
				}
				// if this edge comes after mid
				else if (edge[0] == mid[1]) {
					c = edge[1];
					d = edge[0];
				}
				// if this edge, reversed, comes after mid
				else if (edge[1] == mid[1]) {
					c = edge[0];
					d = edge[1];
				}
				
				// if edges before and after mid have been found, stop looking
				if (a != 0 && c != 0) break;
			}
			
			// perform the 2-break with proper nodes 
			graph2Break(redEdges, a, b, c, d);
			// print out the new permutation of red
			writeGenomeToFile("src/reversals/output.txt", graphToGenome(redEdges));
			
			// loop over all blue edges
			for (int i = 0; i < blueEdges.size(); i++) {
				// check if this edge forms a trivial cycle with the other
				// edge created by the 2-break: (b, d) on purpose, (a, c) ot
				if (pairEquals(blueEdges.get(i), new int[] {a, c})) {
					// if so, remove it and stop looking
					blueEdges.remove(i);
					break;
				}
			}
		}
	}
	
	/**
	 * <h1>Find the reverse complement of a DNA string</h1>
	 * Loops through the string from the back, building up a char
	 * array and then converting into a string.
	 * @param forward the string to find a reverse complement of
	 * @return the reverse complement of forward
	 */
	public static String reverseComplement(String forward) {
		// initialize the char array (for memory efficiency)
		char[] reverse = new char[forward.length()];
		
		// loop over all chars in forward
		for (int n = forward.length(), i = n - 1; i >= 0; i--) {
			// save the char at this spot
			char c = forward.charAt(i);
			// add the appropriate base to the appropriate index,
			// depending on what c is
			if (c == 'A') reverse[n - i - 1] = 'T';
			else if (c == 'T') reverse[n - i - 1] = 'A';
			else if (c == 'G') reverse[n - i - 1] = 'C';
			else if (c == 'C') reverse[n - i - 1] = 'G';
		}
		
		// convert the char array into a string
		return new String(reverse);
	}
	
	/**
	 * <h1>Counts the number of matching k-mers (plus reverse complements) in two strings</h1>
	 * Loops over all k-mers in one, adding them and their reverse complements to a count
	 * map that has string-number of times that string appears in one. Then loops over two,
	 * checking each k-mer to see if it appears in the count map, adding the nubmer of times
	 * it does if so.
	 * @param one the first string
	 * @param two the second string
	 * @param k the length of substrings to compare
	 * @return the matching k-mers/reverse-complement-kmers that match
	 */
	public static int countKmerMatches(String one, String two, int k) {
		// initialize return variable
		int count = 0;
		// initialize the count map
		HashMap<String, Integer> oneKmers = new HashMap<String, Integer>();
		
		// loop through all k-mers in one
		for (int i = 0; i <= one.length() - k; i++) {
			// save the current k-mer
			String kmer = one.substring(i, i + k);
			// if this k-mer is not in the count map, initialize its count to 1
			if (!oneKmers.containsKey(kmer)) oneKmers.put(kmer, 1);
			// otherwise increment its count
			else oneKmers.put(kmer, oneKmers.get(kmer) + 1);
			
			// similar logic after converting the string to its reverse complement
			kmer = reverseComplement(kmer);
			if (!oneKmers.containsKey(kmer)) oneKmers.put(kmer, 1);
			else oneKmers.put(kmer, oneKmers.get(kmer) + 1);
		}
		
		// loop over all k-mers in two
		for (int i = 0; i <= two.length() - k; i++) {
			// save the current k-mer
			String kmer = two.substring(i, i + k);
			// check if this k-mer matches one from one (it's a joke. please laugh)
			if (oneKmers.containsKey(kmer)) {
				// if the kmer is its own reverse complement, then it was double-counted, so halve the count
				if (kmer.equals(reverseComplement(kmer))) count += oneKmers.get(kmer) / 2;
				// otherwise just add the count of this k-mer from one to the total number of matches
				else count += oneKmers.get(kmer);
			}
		}
		
		return count;
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
	 * <h1>Reads <code>int</code> groupings int lists of <code>int[]</code>s</h1>
	 * Formats the string, splits into groups using split, then reads each group into an array.
	 * @param data a properly formatted representation of the groupings
	 * @param split what characters split the groupings
	 * @return <code>ArrayList</code> of <code>int[]</code> groupings
	 */
	public static ArrayList<int[]> readGroups(String data, String split) {
		// initialize return variable
		ArrayList<int[]> groups = new ArrayList<int[]>();
		// reformat data for better use
		data = data.substring(1, data.length() - 1).replace("+", "").replace(split, "S").replace(",", "");
		// split the data into groups
		String[] minis = data.split("S");
		
		// loop over each group
		for (String mini : minis) {
			// split this group into the ints
			String[] seqStr = mini.split(" ");
			
			// read the ints into an array
			int[] seq = new int[seqStr.length];
			for (int i = 0; i < seqStr.length; i++) seq[i] = Integer.parseInt(seqStr[i]);
			// add this group to groups
			groups.add(seq);
		}
		
		return groups;
	}
	
	/**
	 * <h1>Writes a permutation to a file</h1>
	 * Points a writer at a file, then writes each section in order with the proper format
	 * <br>
	 * @param filename the path/name of the file to write to
	 * @param perm an <code>int[]</code> array of numbers in order, representing blocks in a genome
	 * @throws IOException if filename is not valid
	 */
	public static void writePermToFile(String filename, int[] perm) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			
			// loop over ever number but the last
			for (int i = 0; i < perm.length - 1; i++) {
				// if positive add a plus sign
				if (perm[i] > 0) writer.write("+");
				// write number with space afterwards
				writer.write(perm[i] + " ");
			}
			// if last number is positive, add a plus sign
			if (perm[perm.length - 1] > 0) writer.write("+");
			// write last number with a newline after it
			writer.write(perm[perm.length - 1] + "\n");
			
	        // clean up
		    writer.flush();
		    writer.close();  
		}
		
		// if that didn't work, explain why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**<h1>Writes a genome to a file</h1>
	 * Points a writer at a file, then writes each section in order with the proper format
	 * <br>
	 * @param filename the path/name of the file to write to
	 * @param genome an <code>ArrayList</h1> of <code>int[]</code> representing chromosomes,
	 * with numbered, directional (+/-) blocks
	 * @throws IOException if filename is not valid
	 */
	public static void writeGenomeToFile(String filename, ArrayList<int[]> genome) {
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			
			for (int[] chrom : genome) {
				writer.write("(");
				// loop over ever number but the last
				for (int i = 0; i < chrom.length - 1; i++) {
					// if positive add a plus sign
					if (chrom[i] > 0) writer.write("+");
					// write number with space afterwards
					writer.write(chrom[i] + " ");
				}
				// if last number is positive, add a plus sign
				if (chrom[chrom.length - 1] > 0) writer.write("+");
				// write last number with a newline after it
				writer.write(chrom[chrom.length - 1] + ")");
			}
			writer.write("\n");
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
		
		System.out.println(countKmerMatches(readFileAsString("src/reversals/E_coli.txt"),
				readFileAsString("src/reversals/S_enterica.txt"), 30));
		
	}
}