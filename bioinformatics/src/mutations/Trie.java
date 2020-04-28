package mutations;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Trie {
	private final TrieNode root = new TrieNode(0);
	private int maxID = 0;
	
	public Trie(ArrayList<String> patterns) {
		addPatterns(patterns);
	}
	
	public void addPatterns(ArrayList<String> patterns) {
		for (String pattern : patterns) addPattern(pattern);
	}
	
	private void addPattern(String pattern) {
		TrieNode cur = root;
		for (int i = 0; i < pattern.length(); ++i) {
			char need = pattern.charAt(i);
			if (cur.hasPath(need)) {
				cur = cur.pathEnd(need);
			}
			else {
				for (; i < pattern.length(); ++i) {
					need = pattern.charAt(i);
					cur.addPath(new TrieNode(++maxID), need);
					cur = cur.pathEnd(need);
				}
			}
		}
		cur.makeEnd();
	}
	
	public ArrayList<Integer> findMatches(String text) {
		ArrayList<Integer> matches = new ArrayList<Integer>();
		for (int i = 0; i < text.length(); ++i) {
			TrieNode cur = root;
			for (int j = i; j < text.length(); ++j) {
				char need = text.charAt(j);
				if (!cur.hasPath(need)) break;
				cur = cur.pathEnd(need);
				if (cur.isEnd()) {
					matches.add(i);
					break;
				}
			}
		}
		return matches;
	}
	
	private static void writeAllPaths(TrieNode root, BufferedWriter writer) throws IOException {
		for (Character label : root.pathLabels()) {
			writer.write(root.getID() + "->" + root.pathEnd(label).getID() + ":" + label + "\n");
			writeAllPaths(root.pathEnd(label), writer);
		}
	}
	
	public void writeAdjList(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writeAllPaths(root, writer);
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}