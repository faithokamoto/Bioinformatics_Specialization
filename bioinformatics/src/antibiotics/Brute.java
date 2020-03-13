package antibiotics;

//for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;

//for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

//for all kinds of lists
import java.util.*;
 
public class Brute {
	// Stores a codon table (codon-amino acid) for easy access
	public static final Map<String, Character> codonTable = initializeCT();
	
	/**
	 * <h1>Initializes the codon table from codons.txt</h1>
	 * Grabs the codon table from a file, then loops through its chars using the 3-letter
	 * blocks as keys and the 1-letter blocks as amino acids.
	 * @return the finished codon table
	 */
	public static Map<String, Character> initializeCT() {
		// initialize return variable
		Map<String, Character> ct = new HashMap<String, Character>();
		// read in codon table
		String codons = readFileAsString("src/antibiotics/codons.txt");
		
		// used as placeholders in the loop
		String lastCodon = "";
		String read = "";
		// loop over all chars in file
		for (char c : codons.toCharArray()) {
			// if a letter, add it to the current read
			if (c != ' ' && c != '\n') read += c;
			// else, time to update ct!
			else {
				// if it was a 3-letter codon
				if (read.length() == 3) {
					// initialize its spot in ct with a space for amino acid
					ct.put(read, ' ');
					// note this is the last read codon
					lastCodon = read;
				}
				// if it was  1-letter amino acid abbreviation, add it for the latest codon
				else if(read.length() == 1) ct.put(lastCodon, read.charAt(0));
				
				// reset read
				read = "";
			}
		}
		
		return ct;
	}
	
	/**
	 * <h1>Converts an RNA string into a peptide</h1>
	 * Loops over 3-letter blocks in rna, checking them against the codon table.
	 * Breaks at the first stop codon found.
	 * @param rna an RNA string
	 * @return the peptide (concatenated amino acid abbreviations)
	 */
	public static String rnaToPeptide(String rna) {
		// initialize return variable
		String peptide = "";
		
		// used as a placeholder in the loop
		char aminoAcid;
		
		// loop over 3-letter blocks in rna
		for (int i = 0; i < rna.length(); i += 3) {
			// grab the latest amino acid
			aminoAcid = codonTable.get(rna.substring(i, i + 3));
			// if it's a stop codon, leave
			if (aminoAcid == ' ') break;
			// else add it to the peptide
			else peptide += aminoAcid;
		}
		
		return peptide;
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
	
	public static void main(String[] args) {
		System.out.println(rnaToPeptide(readFileAsString("src/antibiotics/data.txt")));
	}
}
