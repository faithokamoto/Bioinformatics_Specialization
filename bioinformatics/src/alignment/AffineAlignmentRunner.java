package alignment;

import java.util.HashMap;

public class AffineAlignmentRunner extends ProteinAlignmentRunner {
	public static void main(String[] args) {
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		String blossom = readFileAsString("src/alignment/blossom.txt");
		HashMap<Character, HashMap<Character, Integer>> scoring = readScoringMatrix(blossom);
		AffineAlignment run = new AffineAlignment(data[0], data[1], scoring, -11, -1);
		run.findPath();
	}
}
