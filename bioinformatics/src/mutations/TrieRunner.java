package mutations;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class TrieRunner {
	public static ArrayList<String> readPatterns(String filename) {
		ArrayList<String> patterns = new ArrayList<String>();
		try {
			Scanner reader = new Scanner(new File(filename));
			while (reader.hasNext()) patterns.add(reader.next());
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return patterns;
	}
	
	public static void main(String[] args) {
		ArrayList<String> patterns = readPatterns("src/mutations/data.txt");
		String text = patterns.remove(0);
		Trie run = new Trie(patterns);
		for (Integer match : run.findMatches(text)) System.out.print(match + " ");
	}
}