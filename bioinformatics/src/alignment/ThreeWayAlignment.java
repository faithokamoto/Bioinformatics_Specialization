package alignment;

/**
 * Finds 3-way alignments of strings using an absudly slow and memory-inefficient brute force method
 * @author faith
 */
public class ThreeWayAlignment extends Runner{
	/**
	 * <h1>Reverses a string</h1>
	 * Loops through all chars, adding to front.
	 * @param s the string to reverse
	 * @return the string with chars in reverse order
	 */
	private static String reverse(String s) {
		// r = reverse, r = return :)
		String r = "";
		// add all chars to front
		for (char c : s.toCharArray()) r = c + r;
		
		return r;
	}
	
	public static void main(String[] args) {
		// read in the strings to align
		String[] data = readFileAsString("src/alignment/data.txt").split("\n");
		// initialize 3-D alignment matrix
		int[][][] nodes = new int[data[0].length() + 1][data[1].length() + 1][data[2].length() + 1];
		// initialize 3-D backtrack pointer matrix
		byte[][][] backtrack = new byte[data[0].length()][data[1].length()][data[2].length()];
		
		// loop over all nodes corresponding to the first string's chars
		for (int x = 1; x < nodes.length; x++) {
			// same for the second string
			for (int y = 1; y < nodes[0].length; y++) {
				// and the third string
				for (int z = 1; z < nodes[0][0].length; z++) {
					// assume a true-diagonal path backwards first
					nodes[x][y][z] = nodes[x - 1][y - 1][z - 1];
					// if all chars match, this path is weighted 1
					if (data[0].charAt(x - 1) == data[1].charAt(y - 1) &&
							data[0].charAt(x - 1) == data[2].charAt(z - 1))
						nodes[x][y][z]++;
					// backtrack pointer for diagonal path
					backtrack[x - 1][y - 1][z - 1] = 0;
					
					// check if a path that backtracks only along the 3rd string is better
					if (nodes[x][y][z - 1] > nodes[x][y][z]) {
						// if so, use that as this node's path
						nodes[x][y][z] = nodes[x][y][z - 1];
						// save the proper backtrack
						backtrack[x - 1][y - 1][z - 1] = 1;
					}
					
					// same idea, for paths that backtrack along only the 2nd string
					if (nodes[x][y - 1][z] > nodes[x][y][z]) {
						nodes[x][y][z] = nodes[x][y - 1][z];
						backtrack[x - 1][y - 1][z - 1] = 2;
					}
					
					// same idea, for paths that backtrack along only the first string
					if (nodes[x - 1][y][z] > nodes[x][y][z]) {
						nodes[x][y][z] = nodes[x - 1][y][z];
						backtrack[x - 1][y - 1][z - 1] = 3;
					}
					
					// and again
					if (nodes[x][y - 1][z - 1] > nodes[x][y][z]) {
						nodes[x][y][z] = nodes[x][y - 1][z - 1];
						backtrack[x - 1][y - 1][z - 1] = 4;
					}
					
					// and again
					if (nodes[x - 1][y][z - 1] > nodes[x][y][z]) {
						nodes[x][y][z] = nodes[x - 1][y][z - 1];
						backtrack[x - 1][y - 1][z - 1] = 5;
					}
					
					// and again
					if (nodes[x - 1][y - 1][z] > nodes[x][y][z]) {
						nodes[x][y][z] = nodes[x - 1][y - 1][z];
						backtrack[x - 1][y - 1][z - 1] = 6;
					}
				}
			}
		}
		
		// start at the sink node
		int x = data[0].length();
		int y = data[1].length();
		int z = data[2].length();
		// start the alignment
		String[] alignment = new String[] {"", "", ""};
		// print out the score
		System.out.println(nodes[x][y][z]);
		// while not going off the edge
		while(x > 0 && y > 0 && z > 0) {
			// grab the backtrack of the curret node
			int bt = backtrack[x - 1][y - 1][z - 1];
			// if it says true diagonal
			if (bt == 0) {
				// add the proper chars
				alignment[0] += data[0].charAt(x - 1);
				alignment[1] += data[1].charAt(y - 1);
				alignment[2] += data[2].charAt(z - 1);
				// decrement all values (back in all directions)
				x--;
				y--;
				z--;
			}
			
			// so on
			else if (bt == 1) {
				alignment[0] += "-";
				alignment[1] += "-";
				alignment[2] += data[2].charAt(z - 1);
				z--;
			}
			
			else if (bt == 2) {
				alignment[0] += "-";
				alignment[1] += data[1].charAt(y - 1);
				alignment[2] += "-";
				y--;
			}
			
			else if (bt == 3) {
				alignment[0] += data[0].charAt(x - 1);
				alignment[1] += "-";
				alignment[2] += "-";
				x--;
			}
			
			else if (bt == 4) {
				alignment[0] += "-";
				alignment[1] += data[1].charAt(y - 1);
				alignment[2] += data[2].charAt(z - 1);
				y--;
				z--;
			}
			
			else if (bt == 5) {
				alignment[0] += data[0].charAt(x - 1);
				alignment[1] += "-";
				alignment[2] += data[2].charAt(z - 1);
				x--;
				z--;
			}
			
			else if (bt == 6) {
				alignment[0] += data[0].charAt(x - 1);
				alignment[1] += data[1].charAt(y - 1);
				x--;
				y--;
			}
		}
		
		// dashes to allow a proper amount taken
		String dashes = "-------------------------";
		
		// build up the final strings, then print
		alignment[0] = data[0].substring(0, x) + reverse(alignment[0]);
		alignment[1] = data[1].substring(0, y) + reverse(alignment[1]);
		alignment[2] = data[2].substring(0, z) + reverse(alignment[2]);
		int maxLen = Math.max(Math.max(alignment[0].length(), alignment[1].length()), alignment[2].length());
		
		for (String align : alignment) 
			System.out.println(dashes.substring(align.length(), maxLen) + align);
		
	}
}
