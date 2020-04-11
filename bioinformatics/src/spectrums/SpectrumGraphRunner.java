package spectrums;

// for data structures
import java.util.ArrayList;

// for reading from files
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A basic runner for SpectrumGraph & VectorGraph
 * @author faith
 */
public class SpectrumGraphRunner {
	/**
	 * Reads a list of integers from a file
	 * <br>
	 * Points a Scanner at the file and then reads integers until none remain
	 * @param filename the file to read from
	 * @return the integers in order of appearance
	 */
	public static ArrayList<Integer> readInts(String filename) {
		// initialize return variable
		ArrayList<Integer> spectrum = new ArrayList<Integer>();
		try {
			// point a scanner at the file
			Scanner reader = new Scanner(new File(filename));
			// while integers remain to read, read the next one in
			while (reader.hasNextInt()) spectrum.add(reader.nextInt());
			// clean up
			reader.close();
		}
		// if something fishy occurs, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
		return spectrum;
	}
	
	public static byte[] readByteArray(String data) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		Scanner reader = new Scanner(data);
		while (reader.hasNextByte()) list.add(reader.nextByte());
		reader.close();
		byte[] arr = new byte[list.size()];
		for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
		return arr;
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
	
	public static ArrayList<String> thresholdPeptides(String filename) {
		String[] data = readFileAsString(filename).split("\n");
		String protenome = data[data.length - 2];
		int threshold = Integer.parseInt(data[data.length - 1]);
		VectorGraph run = new VectorGraph(readByteArray(data[0]));
		ArrayList<String> goodPeptides = new ArrayList<String>();
		int vectorNum = 1;
		do {
			String peptide = run.findPeptide(protenome, threshold);
			if (peptide != null) goodPeptides.add(peptide);
			run.setVector(readByteArray(data[vectorNum]));
			vectorNum++;
		}
		while (vectorNum < data.length - 1);
		return goodPeptides;
	}
	
	/**
	 * Runs code to test it
	 * @param args not used
	 */
	public static void main(String[] args) {
		String[] data = readFileAsString("src/spectrums/data.txt").split("\n");
		PostTransModGraph run = new PostTransModGraph(data[0], readByteArray(data[1]), Integer.parseInt(data[2]));
		long start = System.nanoTime();
		System.out.println(run.findPeptide());
		long end = System.nanoTime();
		System.out.println("took " + (end - start) + "ns");
	}
}