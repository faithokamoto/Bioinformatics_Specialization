package mutations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SuffixTree {
	private final TreeNode root = new TreeNode(0);
	private int maxID = 0;
	
	public SuffixTree(String text) {
		construct(text + "#");
	}
	
	public SuffixTree(String text1, String text2) {
		construct(text1 + "#" + text2 + "?");
	}
	
 	public void construct(String text) {
		SuffixTrie trie = new SuffixTrie(text);
		compress(trie, trie.getRoot(), root);
	}
	
	private class TreeNode extends Node<String, TreeNode> {
		public static final char NOT_SET = 'n';
		public static final char PURPLE = 'p';
		public static final char BLUE = 'b';
		public static final char RED = 'r';
		private char color;
		
		public TreeNode(int id) {
			super(id);
			color = NOT_SET;
		}
		
		public void setColor(char color) {this.color = color;}
		
		public char getColor() {return color;}
		
		public void replaceFirst(char add, String old) {
			replacePath(old, add + old.substring(1));
		}
	}
	
	private class SuffixTrieNode extends Node<Character, SuffixTrieNode> {
		public SuffixTrieNode(int id) {
			super(id);
		}
	}
	
	private class SuffixTrie {
		private final SuffixTrieNode root = new SuffixTrieNode(0);
		private int maxID = 0;
		
		public SuffixTrie(String text) {
			construct(text);
		}
		
		public void construct(String text) {
			for (int i = 0; i < text.length(); ++i) {
				SuffixTrieNode cur = root;
				for (int j = i; j < text.length(); ++j) {
					char need = text.charAt(j);
					if (cur.hasPath(need)) {
						cur = cur.pathEnd(need);
					}
					else {
						for (; j < text.length(); ++j) {
							need = text.charAt(j);
							cur.addPath(new SuffixTrieNode(++maxID/*, j*/), need);
							cur = cur.pathEnd(need);
						}
						//cur.setLabel(i);
					}
				}
			}
		}
		
		public SuffixTrieNode getRoot() {return root;}
	}
	
	private String compress(SuffixTrie trie, SuffixTrieNode theirRoot, TreeNode myRoot) {
		if (theirRoot.pathLabels().size() > 1) {
			for (Character label : theirRoot.pathLabels()) {
				if (theirRoot.pathEnd(label).pathLabels().size() > 1) {
					TreeNode newRoot = new TreeNode(++maxID);
					myRoot.addPath(newRoot, label + "");
					compress(trie, theirRoot.pathEnd(label), newRoot);
				}
				else {
					String shortPath = compress(trie, theirRoot.pathEnd(label), myRoot);
					if (shortPath.length() > 1) myRoot.replaceFirst(label, shortPath);
					
				}
			}
		}
		else if (theirRoot.pathLabels().size() == 1) {
			SuffixTrieNode cur = theirRoot;
			String pathLabel = "+";
			while (cur.pathLabels().size() == 1) {
				char label = cur.pathLabels().iterator().next();
				pathLabel += label;
				cur = cur.pathEnd(label);
			}
			TreeNode newRoot = new TreeNode(++maxID);
			myRoot.addPath(newRoot, pathLabel);
			if (pathLabel.contains("#")) newRoot.setColor(TreeNode.BLUE);
			else if (pathLabel.charAt(pathLabel.length() - 1) == '?') newRoot.setColor(TreeNode.RED);
			compress(trie, cur, newRoot);
			return pathLabel;
		}
		return "+";
	}
	
	private static String longestRepeat(TreeNode start) {
		String longest = "";
		for (String label : start.pathLabels()) {
			if (start.pathEnd(label).pathLabels().size() > 1) {
				String cur = label + longestRepeat(start.pathEnd(label));
				if (cur.length() > longest.length()) longest = cur;
			}
		}
		return longest;
	}
	
	public String longestRepeat() {
		colorAll();
		return longestRepeat(root);
	}
	
	private static void color(TreeNode root) {
		for (TreeNode next : root.pathEnds()) {
			if (next.getColor() == TreeNode.NOT_SET) color(next);
			if (root.getColor() == TreeNode.NOT_SET) root.setColor(next.getColor());
			else if (root.getColor() != next.getColor()) root.setColor(TreeNode.PURPLE);
		}
	}
	
	private void colorAll() {
		color(root);
	}
	
	private static String longestShared(TreeNode root) {
		String longest = "";
		for (String label : root.pathLabels()) {
			if (root.pathEnd(label).getColor() == TreeNode.PURPLE) {
				String cur = label + longestShared(root.pathEnd(label));
				if (cur.length() > longest.length()) longest = cur;
			}
		}
		return longest;
	}
	
	public String longestShared() {
		colorAll();
		return longestShared(root);
	}
	
	private static String shortestUnique(TreeNode root) {
		if (root.getColor() == TreeNode.BLUE) return "";
		else if (root.getColor() == TreeNode.RED) return null;
		else {
			String shortest = null;
			for (String label : root.pathLabels()) {
				String cur = shortestUnique(root.pathEnd(label));
				if (cur != null) {
					cur = label + cur;
					if (cur.contains("#")) cur = cur.substring(0, label.indexOf('#'));
					if (shortest == null || cur.length() < shortest.length()) shortest = cur;
				}
			}
			return shortest;
		}
	}
	
	public String shortestUnique() {
		colorAll();
		writeAllPathLabels("src/mutations/output.txt");
		return shortestUnique(root);
	}
	
	private static void writeAllPathLabels(TreeNode root, BufferedWriter writer) throws IOException {
		for (String label : root.pathLabels()) {
			TreeNode end = root.pathEnd(label);
			writer.write(root.getID() + "->" + end.getID() + ":" + label + ":" + end.getColor() + "\n");
			writeAllPathLabels(end, writer);
		}
	}
	
	public void writeAllPathLabels(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writeAllPathLabels(root, writer);
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private TreeNode getRoot() {return root;}
}