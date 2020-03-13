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
class prep {
	// creates a list of all k-mers within a longer string
	// PRECONDTION: pattern is not empty, pattern's length >= k, k > 0
	public static String[] composition(String text, int k) {
		// initialize return variable (String array with proper length)
		String[] kmers = new String[text.length() - k + 1];
		
		// loop over each k-mer in text
		for (int i = 0; i <= text.length() - k; i++)
			// add k-mer to array
			kmers[i] = text.substring(i, i + k);
		
		return kmers;
	}
	
	// constructs a genome given its path (list of k-mers in order of appearance)
	// PRECONDTION: kmers's strings are all the same length
	public static String pathToGenome(ArrayList<String> kmers) {
		// initialize return variable to prefix of the first k-mer
		String genome = kmers.get(0).substring(0, kmers.get(0).length() - 1);
		
		// for each k-mer, add the last char to genome (only new bit)
		for (String k : kmers) genome += k.charAt(kmers.get(0).length() - 1);
		
		return genome;
	}
	
	// sorts an array of strings from a start index using insertion sort
	// PRECONDITION: arr contains no empty strings
	public static void sortStrings(String[] arr, int start) {
		// used as placeholders in the loop
		int check;
		String cur;
		
		// loop through elements from index 1
		for (int i = 1; i < arr.length; i++) {
			// save value at this index
			cur = arr[i];
			// the index that is being checked against starts as one before
			check = i - 1;
			
			// while check is in the array, and the element at check's start-specified substring
			// comes after cur's
			while (check >= 0 && cur.substring(start).compareTo(arr[check].substring(start)) < 0)
				// move the index being check backwards
				check--;
			
			// move elements up between i & check
			for (int j = i - 1; j > check; j--) arr[j + 1] = arr[j];
			// place cur in proper spot
			arr[check + 1] = cur;
		}
	}
	
	// creates an adjacency list between k-mers in an array
	// adjacency list: map with key related to a bunch of strings that could come after in a genome
	// PRECONDITION: kmers contains no empty strings
	// CALLS: sortStrings
	public static Map<String, ArrayList<String>> overlap(String[] kmers) {
		// initialize return variable
		Map<String, ArrayList<String>> overlap = new LinkedHashMap<String, ArrayList<String>>();
		
		// used to track situation during the loop
		String prefix, suffix;
		// loop through all strings in kmers as possible keys
		for (String key : kmers) {
			// add this key to the adjacency list
			overlap.put(key, new ArrayList<String>());
			// save its suffix
			suffix = key.substring(1);
			
			// loop through all strings in kmers as possible values
			for (String value : kmers) {
				// save its prefix
				prefix = value.substring(0, value.length() - 1);
				
				// if value could come after the key (prefix = suffix)
				// and the key and value are not the same string
				if (key != value && prefix.equals(suffix))
					// add this value to the key's value's arraylist
					overlap.get(key).add(value);
			}
		}
		
		// initialize an iterator for the adjacency list
	    Iterator<Entry<String, ArrayList<String>>> iterator = overlap.entrySet().iterator();

	    // loop through the iterator, removing keys if they have no values
	    while (iterator.hasNext()) if (iterator.next().getValue().size() == 0) iterator.remove();
	    
	    // sort the arraylists of the values
	    for (ArrayList<String> value : overlap.values()) Collections.sort(value);
	    
		return overlap;
	}
	
	// creates an adjacency list between k-mers in an string
	// deBru adjacency list: map with key related to a bunch of strings that could come after in a genome,
	// 						 with each key a prefix only able to relate to suffixes within a string
	// PRECONDITION: genome is not empty, genome's length >= k, k > 0
	public static Map<String, ArrayList<String>> deBru(String genome, int k) {
		// initialize return variable
		Map<String, ArrayList<String>> deBru= new LinkedHashMap<String, ArrayList<String>>();

		// used as a placeholder in the loop
		String kmer;
		
		// loop through k-mers in genome
		for (int i = 0; i < genome.length() - k; i++) {
			// save current k-mer
			kmer = genome.substring(i, i + k);
			
			// if this k-mer is not a key yet, initialize it as one
			if (!(deBru.get(kmer) != null)) deBru.put(kmer, new ArrayList<String>());
			
			// add k-mer after this one to the kmers-that-could-come-after list
			deBru.get(kmer).add(genome.substring(i + 1, i + k + 1));
		}
	    
		return deBru;
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
	
	// writes each element from an array into a file, separated by a newline
	// PRECONDITION: filename is a valid location, text is not empty
	public static void writeArrayToFile(String filename, String[] text){
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// write each element of text into the file
			for (int i = 0; i < text.length; i++) writer.write(text[i]+"\n");
			
			// clean up
			writer.flush();  
			writer.close();
		}
		
		// if that didn't work, explain why
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// writes an adjacency list to a file with proper formatting
	// PRECONDITION: filename is a valid location, map is not empty
	public static void writeAdjListToFile(String filename, Map<String, ArrayList<String>> map){
		try {
			// create a writer pointed at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// iterating over map
	        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet())  {
	        	// print out key and arrow
	        	writer.write(entry.getKey() + " -> ");
	        	
	        	// print out each string in the list in value
	            ArrayList<String> value = entry.getValue();
	            for (int i = 0; i < value.size(); i++) {
	            	// print out string
	            	writer.write(value.get(i));
	            	
	            	// formatting
	            	if (i != value.size() - 1) writer.write(", ");
	            	else writer.write("\n");
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
	}
}