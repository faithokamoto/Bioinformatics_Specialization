package alignment;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <h1>Basic runner for the <code>Graph</code> class</h1>
 * @author faith
 */
public class GraphRunner {
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
	
	// run the Graph code
	public static void main(String[] args) {
		// read the data
		String[] data = readFileAsString("src/alignment/data.txt").split("@");
		
		// find the source & sink of the graph
		String[] endpoints = data[0].split("\n");
		int source = Integer.parseInt(endpoints[0]);
		int sink = Integer.parseInt(endpoints[1]);
		
		// create a Graph, run it
		Graph run = new Graph(data[1], source, sink);
		run.findPath();
	}
}
