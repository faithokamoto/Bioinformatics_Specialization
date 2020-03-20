package alignment;

// for reading from files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <h1>Essential Runner Functions class</h1>
 * Many Runners need to read files, so by extending this class they can without copying code in
 * @author faith
 */
public abstract class Runner {
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
}
