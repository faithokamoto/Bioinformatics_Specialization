package alignment;

/**
 * <h1>Basic runner for the <code>Graph</code> class</h1>
 * @author faith
 */
public class GraphRunner extends Runner {
	
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
