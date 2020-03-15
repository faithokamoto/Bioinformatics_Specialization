package antibiotics;

//for reading from files
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

//for all kinds of lists
import java.util.*;

/**
 * <h1>Encoded Peptide Finder</h1>
 * Contains all the necessary methods to find all sections of DNA which encode peptides, as
 * well as intermedaite steps (such as outputting lists of viable DNA strings to look for).
 * <br>
 * Some important terms:
 * <ul>
 * 		<li><strong>k-mer</strong>: string of length k (AATA has 3-mers AAT and ATA)</li>
 * 		<li><strong>codon</strong>: 3-letter block of DNA that encodes an amino acid</li>
 * 		<li><strong>peptide</strong>: a chain of amino acids, here represented by
 * 									concatenated amino acid abbreviations</li>
 * </ul>
 * @author faith
 */
public class EncodedPeptides {
	/**
	 * Stores a codon table (codon-amino acid) for easy access
	 */
	public static final Map<String, Character> CODON_TABLE = initializeCT();
	/**
	 * stores a reverse codon table (amino acid-all codons) for easy access
	 */
	public static final Map<Character, ArrayList<String>> REVERSE_CODON_TABLE = initializeRCT();
	
	/**
	 * <h1>Initializes the codon table from codons.txt</h1>
	 * Grabs the codon table from a file, then loops through its chars using the
	 * codons as keys and the 1-letter blocks as amino acids.
	 * <br>
	 * calls: readFileAsString
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
	 * <h1>Initializes the reverse codon table from CODON_TABLE</h1>
	 * Loops through all codons in CODON_TABLE, adding them to the arraylists of their amino acids,
	 * initializing said arraylists when necessary.
	 * <br>
	 * calls: rnaToDNA
	 * @return the finished codon table
	 */
	public static Map<Character, ArrayList<String>> initializeRCT() {
		// initialize return variable
		Map<Character, ArrayList<String>> rct = new HashMap<Character, ArrayList<String>>();
		
		// for each codon in codonTable
		for (String codon : CODON_TABLE.keySet()) {
			// if this codon's amino acid is not in rct yet, initialize it with an empty arraylist
			if (!rct.containsKey(CODON_TABLE.get(codon))) rct.put(CODON_TABLE.get(codon), new ArrayList<String>());
			// add this codon to it's amino acid's list of codons
			rct.get(CODON_TABLE.get(codon)).add(rnaToDNA(codon));
		}
		
		return rct;
	}

	/**
	 * <h1>Converts an RNA string into a DNA string</h1>
	 * Loops over rna's chars and adds them to a DNA string, changing U to T.
	 * <br>
	 * precondition: rna is a valid RNA string
	 * @param rna the RNA string to convert
	 * @return the DNA version of rna
	 */
	public static String rnaToDNA(String rna) {
		// initialize return variable
		String dna = "";
		
		// loop over rna's chars
		for (char c : rna.toCharArray()) {
			// change Us for Ts, just add other letters
			if (c == 'U') dna += "T";
			else dna += c;
		}
		
		return dna;
	}
	
	/**
	 * <h1>Creates the reverse complement of an DNA string</h1>
	 * Loops through dna's chars adding complementary bases to the beginning of a new string.
	 * <br>
	 * preconditions: dna is a vaild DNA string, dna is not empty
	 * @param dna the string to create a reverse complement of
	 * @return the reverse complement of DNA
	 */
	public static String reverseComplementDNA(String dna) {
		// initialize return variable
		String reverse = "";
		
		// loop over rna's chars
		for (char c : dna.toCharArray()) {
			// add complementary base to start of reversed string
			if (c == 'A') reverse = "T" + reverse;
			else if (c == 'T') reverse = "A" + reverse;
			else if (c == 'G') reverse = "C" + reverse;
			else if (c == 'C') reverse = "G" + reverse;
		}
		
		return reverse;
	}
	
	/**
	 * <h1>Finds all of the DNA strings that encode a peptide directly or by reverse complement</h1>
	 * After adding all of the possible first codons to an <code>ArrayList</code>, goes through each next
	 * amino acid in peptide. Removes each existing DNA string and then adds it back which each possible
	 * next codon appened to it. Finally adds all of the reverse complements as well.
	 * <br>
	 * preconditions: peptide is a valid peptide, peptide is not empty
	 * <br>
	 * calls: reverseComplementDNA
	 * @param peptide the peptide that DNA strings will be made from
	 * @return an <code>ArrayList</code> of DNA strings that encode the peptide
	 */
	public static ArrayList<String> peptideToDNAs(String peptide) {
		// initialize return variable to all codons that encode the first amino acid in peptide
		ArrayList<String> dnas = new ArrayList<String>(REVERSE_CODON_TABLE.get(peptide.charAt(0)));
		
		// used as a placeholder
		String rna;
		// for each amino acid in peptide after the first one
		for (int i = 1; i < peptide.length(); i++) {
			// for each existing DNA string (loop from back because will be adding on)
			for (int j = dnas.size() - 1; j >= 0 ; j--) {
				// remove, and save, the current DNA string
				rna = dnas.remove(j);
				// for each possible codon in for the current amino acid
				for (String codon : REVERSE_CODON_TABLE.get(peptide.charAt(i)))
					// add to dnas the DNA string plus the next codon
					dnas.add(rna + codon);
			}
		}
		
		// for each exiting DNA string (loop from back because adding on), add reverse complement
		for (int i = dnas.size() - 1; i >= 0; i--) dnas.add(reverseComplementDNA(dnas.get(i)));
		
		return dnas;
	}
	
	/**
	 * <h1>Finds all DNA strands in a long string that encode a peptide</h1>
	 * Gets a Set of all DNA strings that encode the peptide, then checks each k-mer in
	 * genome to see if it is contained in the Set, adding if so
	 * <br>
	 * preconditions: genome's length >= peptide's length * 3, no strings are empty
	 * <br>
	 * calls: peptideToDNAs
	 * @param genome the genome to check for coding segments
	 * @param peptide the peptide that should be encoded
	 * @return an <code>ArrayList</code> of k-mers in genome that encode peptide
	 */
	public static ArrayList<String> peptideEncoding(String genome, String peptide) {
		// initialize return variable
		ArrayList<String> dnas = new ArrayList<String>();
		// calculate how long the k-mers should be
		int k = peptide.length() * 3;
		// get Set of all k-mers that encode this peptide
		Set<String> valid = new HashSet<String>(peptideToDNAs(peptide));

		// loop over all k-mers in genome
		for (int i = 0; i <= genome.length() - k; i++) {
			// if this k-mer, or its reverse complement, encodes peptide
			if (valid.contains(genome.substring(i, i + k)))
				// add it to the list
				dnas.add(genome.substring(i, i + k));
		}
		return dnas;
	}
	
	/** <h1>Reads a file into a string</h1>
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
		 
	 }
}
