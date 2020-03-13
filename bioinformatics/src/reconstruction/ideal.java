package reconstruction;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;

// for all kinds of lists
import java.util.*;
import java.util.Map.Entry;

// general PRECONDTION for all methods: strings only contain ACGT in various combinations
// k-mer: string of length k
// prefix/suffix : a string, excluding last/first char
class ideal {
	// constructs a genome given its path (list of k-mers in order of appearance)
	// PRECONDTION: kmers's strings are all the same length
	public static String pathToGenome(ArrayList<String> kmers) {
		// initialize return variable to prefix of the first k-mer
		String genome = kmers.get(0).substring(0, kmers.get(0).length() - 1);
		
		// for each k-mer, add the last char to genome (only new bit)
		for (String k : kmers) genome += k.charAt(kmers.get(0).length() - 1);
		
		return genome;
	}
	
	// creates an deBru adjacency list between k-mers in an array
	// deBru adjacency list: map with key related to a bunch of strings that could come after in a genome,
	// 						 with each key a prefix only able to relate to suffixes within a string
	// PRECONDITION: kmers contains no empty strings
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
	
	// check if unused edges remain in an eulerian adjacency list
	// PRECONDITION: nothing
	public static boolean hasMoreEdges(Map<String, ArrayList<String>> map) {
		// iterating over map
        for (String value : map.keySet())  {
        	// if this key's arraylist is not empty, return true
        	if (!map.get(value).isEmpty()) return true;
        }
        // if all arraylists were empty, no unsused edges remain
        return false;
	}
	
	// finds an eulerian cycle given a start and a map of unused edges in an adjacency list
	// PRECONDITION: unused is a valid eulerian adjacency list
	public static ArrayList<String> newCycle(String start, Map<String, ArrayList<String>> unused,
			ArrayList<String> unusedCycle) {
		// initialize return variable
		ArrayList<String> cycle = new ArrayList<String>();
		
		// if start is empty (indicates no current cycle exists)
		if (start.length() == 0)
			// stick the first available key into the cycle
			cycle.add(unused.entrySet().stream().findFirst().get().getKey());
		// else the start of cycle should just be start
		else cycle.add(start);
		
		// used as placeholders in the loop
		String next, latest;
		// just keep swimming :)
		while (true) {
			// save the last string added to cycle (to be used as a key)
			latest = cycle.get(cycle.size() - 1);
			
			// if this string has no values left ("stuck")
			if (unused.get(latest).size() != 0) {
				// grab a value from the arraylist of possible next strings, remove it since it's been used
				next = unused.get(latest).remove(0);
				
				// if the string used as a key still has values, and is not listed as unusedCycle, add it
				if (unused.get(latest).size() != 0 && !unusedCycle.contains(latest)) unusedCycle.add(latest);
				// if the string used as a key no longer has values, and is listed as unsusedCycle, delete it
				else if (unusedCycle.contains(latest) && unused.get(latest).size() == 0) unusedCycle.remove(latest);
				
				// add the next string to the cycle
				cycle.add(next);
			}
			
			// if stuck, leave!
			else break;
		}
		
		// if there was a start already in place, remove this one's start
		if (start != "") cycle.remove(0);
		
		return cycle;
	}
	
	// rotate an eulerian cycle to start and end at a certain, contained, string
	// PRECONDTION: newStart is contained in cycle, cycle starts and ends with the same value
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
	
	// finds an eulerian cycle given an adjacency list
	// PRECONDITION: all keys in euler have non-empty arraylists,
	// 				 euler represents a eulerian cycle's adjacency list
	// CALLS: newCycle, hasMoreEdges, moveCycle
	public static ArrayList<String> eulerToCycle(Map<String, ArrayList<String>> euler) {
		// initialize return variable
		ArrayList<String> cycle = new ArrayList<String>();
		
		// not that all edges are unused
		Map<String, ArrayList<String>> unused = new LinkedHashMap<String, ArrayList<String>>(euler);
		// there are no keys in cycle with unused values (since there are no keys in cycle)
		ArrayList<String> unusedCycle = new ArrayList<String>();
		
		// get an initial cycle 
		cycle.addAll(newCycle("", unused, unusedCycle));
		
		// while unused edges remain
		while (hasMoreEdges(unused)) {
			// rotate the cycle so that it starts/ends on a key with unused values
			moveCycle(cycle, unusedCycle.get(0));
			// extend the cycle
			cycle.addAll(newCycle(unusedCycle.get(0), unused, unusedCycle));
		}
		
		return cycle;
	}
	
	// finds an eulerian path given an adjacency list
	// PRECONDITION: all keys in path have non-empty arraylists,
	// 				 path represents a eulerian path's adjacency list
	// CALLS: eulerToCycle, moveCycle
	public static ArrayList<String> eulerToPath(Map<String, ArrayList<String>> path) {
		// first, convert the path to a cycle by connecting the end to the start
		
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
        		if (path.get(out) == null) end = out;
        		
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
        
        // add an edge going from end to start in path
        if (!(path.get(end) != null)) path.put(end, new ArrayList<String>());
        path.get(end).add(start);
        
        // finally, make a cycle using the now-cyclic path
        ArrayList<String> cycle = eulerToCycle(path);
        // rotate the cycle so it starts at start
        moveCycle(cycle, start);
        // remove last node (it will be start, this gets rid of the added edge)
        cycle.remove(cycle.size() - 1);
        
        
		return cycle;
	}
	
	// reconstructs a long string given many k-mers
	// PRECONDTION: kmers contains no empty strings
	// CALLS: deBru, eulerToPath, pathToGenome
	public static String genomeReconstruction(String[] kmers) {
		// create an adjacency list based on kmers
		Map<String, ArrayList<String>> adjList = deBru(kmers);
		// find a path through the kmers based on the adjacency list
		ArrayList<String> path = eulerToPath(adjList);
		// reconstruct the genome based on the path
		return pathToGenome(path);
	}

	// convert a decimal number into a binary k-mer
	// PRECONDITION: d > 0, k > 0, d as a binary number has <= k digits
	public static String decimalToBinary(int d, int k) {
		// initialize return variable
		String b = "";
		
		// add 1s or 0s based on dividing by 2
		while (d > 0) {
			b = (d % 2) + b;
			d /= 2;
		}
		
		// fill extra space at the beginning with 0s
		while (b.length() < k) b = "0" + b;
		
		return b;
	}
	
	// lists all binary k-mers
	// PRECONDITION: k > 0
	// CALLS: decimalToBinary
	public static String[] allBinaryKmers(int k) {
		// initialize return variable
		String[] all = new String[(int) Math.pow(2, k)];
		// add binary number corresponding to index for all of all
		for (int i = 0; i < all.length; i++) all[i] = decimalToBinary(i, k);
		
		return all;
	}
	
	// finds a cyclic binary string that contains all binary k-mers
	// PRECONDTION: k > 0
	// CALLS: deBru, eulerToCycle, pathToGenome
	public static String kUniversal(int k) {
		// get all k-mers
		String[] kmers = allBinaryKmers(k);
		// create an adjacency list based on kmers
		Map<String, ArrayList<String>> adjList = deBru(kmers);
		// find a cycle based on the adjacency list
		ArrayList<String> cycle = eulerToCycle(adjList);
		// construct a k-universal string based on the cycle
		String universe = pathToGenome(cycle);
		// remove the last k-1 chars before returning
		return universe.substring(0, universe.length() - k + 1);
	}
	
	// reads a file in as a string, getting rid of line breaks
	// PRECONDITION: filename is a valid location
	public static String readFileAsString(String filename) {
		// initialize return variable
		String text = "";
		
		try {
			// read the file into text
			text = new String(Files.readAllBytes(Paths.get(filename)));
			// remove one kind of newline (other kind used to split)
			text = text.replace("\r", "");
		}
		
		// if that didn't work, explain why
		catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}

	// writes a path to a file with proper formatting
	// PRECONDITION: filename is a valid location, map is not empty
	public static void writePathToFile(String filename, ArrayList<String> path){
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// counter variable
			int j = 0;
			// loop through path
			for (int i = 0; i < path.size(); i++) {
				// write the next step
				writer.write(path.get(i));
				// as long as this step isn't the last one, write an arrow
				if (i != path.size() - 1) writer.write("->");
				
				// increment counter
				j++;
				// if 200 steps have been written since the last newline
				if (j == 200) {
					// add two newlines (for easy finding) and reset counter
					writer.write("\n\n");
					j -= 200;
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
		System.out.println(genomeReconstruction(readFileAsString("src/reconstruction/data.txt").split("\n")));
	}
}