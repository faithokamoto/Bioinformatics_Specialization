package mutations;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A Suffix Tree which is built from text given
 * @author faith
 */
public class SuffixTree {
	/**
	 * only the root node needs to have a reference
	 */
	private final TreeNode root = new TreeNode(0);
	/**
	 * the maximal ID# of any node in the tree
	 */
	private int maxID = 0;
	
	/**
	 * Constructs the Tree from a single text
	 * @param text the text to construct the tree from
	 */
	public SuffixTree(String text) {
		construct(text + "#");
	}
	
	/**
	 * Constructs the Tree from two texts
	 * @param text1 the first text
	 * @param text2 the second text
	 */
	public SuffixTree(String text1, String text2) {
		construct(text1 + "#" + text2 + "?");
	}
	
	/**
	 * Constructs a suffix tree from a text
	 * @param text the text to use
	 */
 	private void construct(String text) {
		// compress a newly constructed suffix tree
		compress((new SuffixTrie(text)).getRoot(), root);
	}
	
 	/**
 	 * A kind of Node that can have a color, and labels its paths with Strings
 	 * @author faith
 	 */
	private class TreeNode extends Node<String, TreeNode> {
		/**
		 * the available colors
		 */
		public static final char NOT_SET = 'n',
			BOTH = 'b', FIRST = 'f', SECOND = 's';
		/**
		 * the color of this Node
		 */
		private char color;
		
		/**
		 * Initializes ID#, and the color to not set
		 * @param id the ID# of this node
		 */
		public TreeNode(int id) {
			super(id);
			color = NOT_SET;
		}
		
		/**
		 * Sets the color of this Node as long as it is valid
		 * @param color the new color
		 */
		public void setColor(char color) {
			if (color != BOTH && color != FIRST && color != SECOND)
				throw new IllegalArgumentException("Invalid color");
			this.color = color;
		}
		
		/**
		 * Gets the color of this Node
		 * @return the value of this.color
		 */
		public char getColor() {return color;}
		
		/**
		 * Replaces the first char of a label
		 * @param add the new char
		 * @param old the old label
		 */
		public void replaceFirst(char add, String old) {
			replacePath(old, add + old.substring(1));
		}
	}
	
	/**
	 * A kind of Node that uses Characters for path labels
	 * @author faith
	 */
	private class SuffixTrieNode extends Node<Character, SuffixTrieNode> {
		public SuffixTrieNode(int id) {
			super(id);
		}
	}
	
	/**
	 * A Trie constructed from the suffixes of a text
	 * @author faith
	 */
	private class SuffixTrie {
		/**
		 * only the root node needs to have a stored reference
		 */
		private final SuffixTrieNode root = new SuffixTrieNode(0);
		/**
		 * the maximal ID# of any Node in the Trie
		 */
		private int maxID = 0;
		
		/**
		 * Constructs a suffix trie from the text
		 * @param text the text to use suffixes from
		 */
		public SuffixTrie(String text) {
			construct(text);
		}
		
		/**
		 * Constructs a suffix trie from the text
		 * @param text the text to use suffixes from
		 */
		private void construct(String text) {
			// loop over all start indexes
			for (int i = 0; i < text.length(); ++i) {
				// the start node is the root
				SuffixTrieNode cur = root;
				// loop over all chars in the substring i
				for (int j = i; j < text.length(); ++j) {
					// save the current char
					char need = text.charAt(j);
					// if there is already a path, move the current node down it
					if (cur.hasPath(need)) cur = cur.pathEnd(need);
					// otherwise a new leaf is needed, loop over remaining chars
					else for (; j < text.length(); ++j) {
						// save the current char
						need = text.charAt(j);
						// create a new Node with the next ID#
						SuffixTrieNode newNode = new SuffixTrieNode(++maxID);
						// add a path from the current node
						// labeled by the appropriate char, to the new one
						cur.addPath(need, newNode);
						// move the current node down the path
						cur = newNode;
						// if the end of the first string was reached, leave
						if (need == '#') break;
					}
					if (need == '#') break;
				}
			}
		}
		
		/**
		 * Gets the root of the Trie
		 * @return the value of this.root
		 */
		public SuffixTrieNode getRoot() {return root;}
	}
	
	private TreeNode newNode() {return new TreeNode(++maxID);}
	
	/**
	 * Compresses a suffix trie subtree into a suffix tree subtree
	 * @param theirRoot the root of the trie's subtree
	 * @param myRoot the root of the tree's subtree
	 * @return the path label of a non-branching path compressed into a single path (if applicable)
	 */
	private String compress(SuffixTrieNode theirRoot, TreeNode myRoot) {
		// if this trie root has multiple children
		if (theirRoot.pathLabels().size() > 1) {
			// loop over all paths
			for (Character label : theirRoot.pathLabels()) {
				// if the node at the end of this path has multiple children
				if (theirRoot.pathEnd(label).pathLabels().size() > 1) {
					// get a new node
					TreeNode newRoot = newNode();
					// add a path with the same label to this new node
					myRoot.addPath(label + "", newRoot);
					// compress the new node's subtree
					compress(theirRoot.pathEnd(label), newRoot);
				}
				
				// or if the node at the end of this path has only one child
				else {
					// compress the subtree leading down this path, and grab the label of the new path
					String shortPath = compress(theirRoot.pathEnd(label), myRoot);
					// adjust the first character of the path to replace the placeholder
					if (shortPath.length() > 1) myRoot.replaceFirst(label, shortPath);
				}
			}
		}
		
		// or if this trie root has only one child
		else if (theirRoot.pathLabels().size() == 1) {
			// save the current node of the trie being used
			SuffixTrieNode cur = theirRoot;
			// start the label with a placeholder character
			String pathLabel = "+";
			
			// while the current node is non-branching
			while (cur.pathLabels().size() == 1) {
				// grab its (single) label
				char label = cur.pathLabels().iterator().next();
				// add it to the growing label
				pathLabel += label;
				// move the current node to the end of the path
				cur = cur.pathEnd(label);
			}
			
			// create a new node for the root
			TreeNode newRoot = newNode();
			// attach the new node by the build-up path to the root
			myRoot.addPath(pathLabel, newRoot);
			// grab the last char
			char end = pathLabel.charAt(pathLabel.length() - 1);
			// if it's #, then this is the first text
			if (end == '#') newRoot.setColor(TreeNode.FIRST);
			// if it's ?, then this is the second text
			else if (end == '?') newRoot.setColor(TreeNode.SECOND);
			
			// compress the subtree under the new root
			compress(cur, newRoot);
			// return the built-up path
			return pathLabel;
		}
		
		// else if this is the end of a path, return just the placeholder symbol
		return "+";
	}
	
	/**
	 * Finds the longest repeat in a subtree
	 * @param root the root of the subtree
	 * @return the longest repeated text found in that subtree
	 */
	private static String longestRepeat(TreeNode root) {
		// initialize return variable to empty (short)
		String longest = "";
		// loop over all path labels
		for (String label : root.pathLabels()) {
			// if the node on the end of this path has multiple children
			if (root.pathEnd(label).pathLabels().size() > 1) {
				// then this path is repeated, try path label + longest repeat of child's subtree
				String cur = label + longestRepeat(root.pathEnd(label));
				// if it's long enough, set it as the longest
				if (cur.length() > longest.length()) longest = cur;
			}
		}
		
		return longest;
	}
	
	/**
	 * Finds the longest repeat in the text given
	 * @return the longest repeat in the subtree under the root
	 */
	public String longestRepeat() {return longestRepeat(root);}
	
	/**
	 * Colors all Nodes in a subtree
	 * @param root the root of the subtree to color
	 */
	private static void color(TreeNode root) {
		// loop over all child nodes
		for (TreeNode next : root.pathEnds()) {
			// if this child node isn't colored, color it
			if (next.getColor() == TreeNode.NOT_SET) color(next);
			// if this node doesn't have a color, set it to the child's color
			if (root.getColor() == TreeNode.NOT_SET) root.setColor(next.getColor());
			// or if it has a color which isn't the child's color, set to to the both-color
			else if (root.getColor() != next.getColor()) root.setColor(TreeNode.BOTH);
		}
	}
	
	/**
	 * Colors all Nodes in the subtree defined by THE root
	 */
	private void colorAll() {color(root);}
	
	/**
	 * Finds the longest shared substring in a subtree
	 * @param root the root of the subtree to search through
	 * @return the longest shared (BOTH) substring found
	 */
	private static String longestShared(TreeNode root) {
		// initialize return variable to empty (short)
		String longest = "";
		// loop over all paths out
		for (String label : root.pathLabels())
			// if the node at the end of the path is also a both-colored node
			if (root.pathEnd(label).getColor() == TreeNode.BOTH) {
				// this path is shared, try it + longestShared of the out-node
				String cur = label + longestShared(root.pathEnd(label));
				// if this is longest than the current longest, use it as the longest
				if (cur.length() > longest.length()) longest = cur;
			}
		
		return longest;
	}
	
	/**
	 * Finds the longest shared substring between two texts
	 * @return the longest path consisting entirely of BOTH nodes
	 */
	public String longestShared() {
		// coloring is important to identify dependencies
		colorAll();
		return longestShared(root);
	}
	
	/**
	 * Finds the SUS (see public method) in a subtree
	 * @param root the root of the subtree
	 * @return the SUS in this subtree
	 */
	private static String shortestUnique(TreeNode root) {
		// if this root is an only-first node, return empty
		if (root.getColor() == TreeNode.FIRST) return "";
		// if this root is an only-second node, return null
		else if (root.getColor() == TreeNode.SECOND) return null;
		
		// otherwise, this is a two-color node
		else {
			// no shortest string found yet
			String shortest = null;
			// loop over all paths out
			for (String label : root.pathLabels()) {
				// try to get the SUS for this root's subtree
				String cur = shortestUnique(root.pathEnd(label));
				
				// if there was a valid SUS
				if (cur != null) {
					// add the label on in front
					cur = label + cur;
					// if shortest isn't set or if this beats shortest's length, set it as shortest
					if (shortest == null || cur.length() < shortest.length()) shortest = cur;
				}
			}
			
			return shortest;
		}
	}
	
	/**
	 * Find the shortest unique substring for the first text
	 * @return the shortest unique substring for the first text
	 */
	public String shortestUnique() {
		// coloring is important to identify identities
		colorAll();
		return shortestUnique(root);
	}
	
	/**
	 * Writes all paths out for a subtree
	 * @param root the root of the subtree
	 * @param writer the writer to use
	 * @throws IOException it the writer doesn't work
	 */
	private static void writeAllPaths(TreeNode root, BufferedWriter writer) throws IOException {
		// loop over all paths out
		for (String label : root.pathLabels()) {
			// grab the end of this path
			TreeNode end = root.pathEnd(label);
			// write out the path, format "startID->endID:pathLabel:endColor", then newline
			writer.write(root.getID() + "->" + end.getID() + ":" + label + ":" + end.getColor() + "\n");
			// write all the paths of the end's subtree
			writeAllPaths(end, writer);
		}
	}
	
	/**
	 * Writes all paths out for the suffix tree
	 * @param filename the file to write to
	 */
	public void writeAdjList(String filename) {
		// try writing to the file
		try {
			// point a writer at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			// writes all the paths under the root
			writeAllPaths(root, writer);
			// clean up
			writer.close();
		}
		// if something weird happens, print it out
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}