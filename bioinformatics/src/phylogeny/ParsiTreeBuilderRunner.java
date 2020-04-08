package phylogeny;

// for data structures
import java.util.HashMap;
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;

/**
 * A basic runner for Large Parsimony trees
 * @author faith
 */
public class ParsiTreeBuilderRunner {
	/**
	 * Checks if a string holds an integer
	 * @param strNum a string
	 * @return whether strNum represents an int
	 */
	public static boolean isInt(String strNum) {
		// try to parse it
	    try {Integer.parseInt(strNum);}
	    // if fail, not an integer
	    catch (NumberFormatException nfe) {return false;}
	    // otherwise is an integer
	    return true;
	}

	/**
	 * Reads an adjacency list of an undirected, unrooted tree
	 * <br>
	 * Loops through all the paths, reading them into the adjacency list (and label map, if appropriate)
	 * properly with the help of an ArrayList that holds all previously reaf leaves.
	 * @param filename the file to read from
	 * @param labelMap a label map to input data into
	 * @return the finished adjacency list
	 */
	public static HashMap<Integer, ArrayList<Integer>> readAdjList(String filename,
			HashMap<Integer, char[]> labelMap) {
		// initialize return variable
		HashMap<Integer, ArrayList<Integer>> adjList = new HashMap<Integer, ArrayList<Integer>>();
		
		// try to read from the file
		try {
			// point a scanner at the file
			Scanner reader = new Scanner(new File(filename));
			
			// a list of all leaves read, with (i) as the i-th leaf
			ArrayList<String> leaves = new ArrayList<String>(reader.nextInt());
			// move to the goodies!
			reader.nextLine();
			
			// while there are more paths to read
			while (reader.hasNextLine()) {
				// read the line into a variable
				String[] line = reader.nextLine().split("->");
				
				// if the first value is non-numeric
				if (!isInt(line[0])) {
					// if this leaf has not been read yet
					if (!leaves.contains(line[0])) {
						// add the path into the adjacency list
						adjList.put(leaves.size(), new ArrayList<Integer>(1));
						adjList.get(leaves.size()).add(Integer.parseInt(line[1]));
						
						// add it into leaves and labelMap
						labelMap.put(leaves.size(), line[0].toCharArray());
						leaves.add(line[0]);
					}
					
					// or if it has been read
					else {
						// just add the path in
						adjList.put(leaves.indexOf(line[0]), new ArrayList<Integer>(1));
						adjList.get(leaves.indexOf(line[0])).add(Integer.parseInt(line[1]));
					}
				}
				
				// else if the second value is non-numeric
				else if (!isInt(line[1])) {
					// save the starting node ID#
					int start = Integer.parseInt(line[0]);
					// add the start node into the adjacency list
					adjList.putIfAbsent(start, new ArrayList<Integer>(3));
					
					// if this leaf hasn't been read before
					if (!leaves.contains(line[1])) {
						// add the path
						labelMap.put(leaves.size(), line[1].toCharArray());
						
						adjList.get(start).add(leaves.size());
						// add it into leaves and labelMap
						leaves.add(line[1]);
					}
					
					// or if it has been read just add the path
					else adjList.get(start).add(leaves.indexOf(line[1]));
				}
				
				// or if this is just internal nodes
				else {
					// save the starting node ID#
					int start = Integer.parseInt(line[0]);
					// add the path
					adjList.putIfAbsent(start, new ArrayList<Integer>(3));
					adjList.get(start).add(Integer.parseInt(line[1]));
				}
			}
			
			// clean up
			reader.close();
		}
		// if something weird happens, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return adjList;
	}
	
	/**
	 * Read a FASTA file into a list of sequences
	 * @param filename the file to read
	 * @return all the sequences in the file
	 */
	public static ArrayList<String> readFASTA(String filename) {
		// initialize return variable
		ArrayList<String> leaves = new ArrayList<String>();
		
		// try to read from the file
		try {
			// point a reader at the file
			Scanner reader = new Scanner(new File(filename));
			// if there are more lines to read
			while (reader.hasNextLine()) {
				// read in the line
				String line = reader.nextLine();
				// as long as it's a sequence, add to list
				if (line.charAt(0) != '>') leaves.add(line);
			}
			// clean up
			reader.close();
		}
		// if something weird happens, print it
		catch (Exception e) {
			e.printStackTrace();
		}
		return leaves;
	}
	
	/**
	 * Reads in data and runs Large Parsimony on it
	 * @param args not used
	 */
	public static void main(String[] args) {
		// read in the adjacency list
		ArrayList<String> leaves = readFASTA("src/phylogeny/hiv.txt");
		
		// initialize the tree
		LargeParsiTreeBuilder run = new LargeParsiTreeBuilder(leaves);
		// build it
		run.buildTree();
	}
}