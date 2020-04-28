package mutations;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class SuffixTreeRunner {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner reader = new Scanner(new File("src/mutations/data.txt"));
		SuffixTree run = new SuffixTree(reader.next(), reader.next());
		reader.close();
		run.writeAllPathLabels("src/mutations/output.txt");
		System.out.println(run.shortestUnique());
	}
}