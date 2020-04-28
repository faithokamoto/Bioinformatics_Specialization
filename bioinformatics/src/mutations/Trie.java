package mutations;

// for data structures
import java.util.ArrayList;

// for writing to files
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A Trie derived from patterns, which can be run against a genome for matches
 * @author faith
 */
public class Trie {
	/**
	 * it is only necessary to save a reference to the root node
	 */
	private final TrieNode root = new TrieNode(0);
	/**
	 * the maximal ID# used so far
	 */
	private int maxID = 0;
	
	/**
	 * Initializes a pattern Trie
	 * @param patterns the patterns to initialize the Trie with
	 */
	public Trie(ArrayList<String> patterns) {
		addPatterns(patterns);
	}
	
	/**
	 * Adds a bunch of patterns to the Trie
	 * @param patterns the patterns to add to the Trie
	 */
	public void addPatterns(ArrayList<String> patterns) {
		// add each pattern separately
		for (String pattern : patterns) addPattern(pattern);
	}
	
	/**
	 * Adds a single pattern to the Trie
	 * @param pattern the pattern to add
	 */
	private void addPattern(String pattern) {
		// start with the current node being the root
		TrieNode cur = root;
		// loop through all characters in the pattern
		for (int i = 0; i < pattern.length(); ++i) {
			// save the current character
			char need = pattern.charAt(i);
			// if there is a path with this character, move the current node down the path
			if (cur.hasPath(need)) cur = cur.pathEnd(need);
			
			// otherwise, going to have to make a new path
			// loop through all characters left
			else for (; i < pattern.length(); ++i) {
				// save the current character
				need = pattern.charAt(i);
				// generate a new node with a new ID#
				TrieNode newNode = new TrieNode(++maxID);
				// add a path with this character to the new node
				cur.addPath(need, newNode);
				// move the current node to the new node
				cur = newNode;
			}
		}
		
		// note that the node at the end of this path is and end of a pattern
		cur.makeEnd();
	}
	
	/**
	 * A specialized Node that can store whether it is the end of a path.
	 * and whose paths are labeled with Characters
	 * @author faith
	 */
	private class TrieNode extends Node<Character, TrieNode>{
		/**
		 * whether this Node is the end of a path
		 */
		private boolean isEnd;
		
		/**
		 * Initializes ID# to given and isEnd to false
		 * @param id the ID# of this Node
		 */
		public TrieNode(int id) {
			super(id);
			isEnd = false;
		}
		
		/**
		 * Makes this Node an end
		 */
		public void makeEnd() {isEnd = true;}
		
		/**
		 * Checks whether this Node is an end
		 * @return the value of this.isEnd
		 */
		public boolean isEnd() {return isEnd;}
	}
	
	/**
	 * Finds all indexes in a text where a pattern in the Trie begins
	 * @param text the text to search through
	 * @return the indexes which start patterns
	 */
	public ArrayList<Integer> findMatches(String text) {
		// initialize return variable
		ArrayList<Integer> matches = new ArrayList<Integer>();
		// loop over all indexes in text
		for (int i = 0; i < text.length(); ++i) {
			// start at the root node
			TrieNode cur = root;
			// loop through all indexes in the substring of i
			for (int j = i; j < text.length(); ++j) {
				// save the current character
				char need = text.charAt(j);
				// if there is no path with the char needed, leave
				if (!cur.hasPath(need)) break;
				// otherwise, move the current node down the path
				cur = cur.pathEnd(need);
				// if the node is an end of a path
				if (cur.isEnd()) {
					// this start index is the start of a pattern!
					matches.add(i);
					// leave, not needed any more
					break;
				}
			}
		}
		
		return matches;
	}
	
	/**
	 * Write all paths in the subtree defined by a certain root
	 * @param root the root of the subtree
	 * @param writer a BufferedWriter to write to a a file
	 * @throws IOException if the writer has a problem
	 */
	private static void writeAllPaths(TrieNode root, BufferedWriter writer) throws IOException {
		// loop through all paths out
		for (Character label : root.pathLabels()) {
			// write this path with proper notation, then newline
			writer.write(root.getID() + "->" + root.pathEnd(label).getID() + ":" + label + "\n");
			// write all paths of the subtree with the out-node as a root
			writeAllPaths(root.pathEnd(label), writer);
		}
	}

	/**
	 * Writes the adjacency list of the Trie to a file
	 * @param filename the file to write the adj list to
	 */
	public void writeAdjList(String filename) {
		// try to write to the file
		try {
			// point a writer at the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			// write the subtree under the root
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