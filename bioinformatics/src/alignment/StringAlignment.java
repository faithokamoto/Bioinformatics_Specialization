package alignment;

/**
 * <h1>String Aligner</h1>
 * Specialized kind of Grid that aligns strings according to criteria
 * @author faith
 */
public class StringAlignment extends Grid {
	/**
	 * <h1>Global Alignment Constructor</h1>
	 * If only strings are passed in, global alignment is assumed
	 * @param one the first string to align
	 * @param two the second string to align
	 */
	public StringAlignment(String one, String two) {
		// calling the master constructor with proper params
		this(one, two, false, false, false, false);
	}
	
	/**
	 * <h1>Local Alignment Constructor</h1>
	 * Allows for local alignment to to specified.
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param local whether to use local alignment or not
	 */
	public StringAlignment(String one, String two, boolean local) {
		// calling the master constructor with proper params
		this(one, two, local, local, local, local);
	}
	
	/**
	 * <h1>Specific Taxi Constructor</h1>
	 * Allows for taxi-ing only one way to be specified.
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param vertTaxi whether to allow skips over one
	 * @param horizTaxi whether to allow skips over two
	 */
	public StringAlignment(String one, String two, boolean vertTaxi, boolean horizTaxi) {
		// calling the master constructor with proper params
		this(one, two, vertTaxi, vertTaxi, horizTaxi, horizTaxi);
	}
	
	/**
	 * <h1>Constructor</h1>
	 * Allows for taxi-ing to be even more specific
	 * @param one the first string to align
	 * @param two the second string to align
	 * @param startVertTaxi whether to allow skips over the beginning of one
	 * @param endVertTaxi whether to allow skips over the end of one
	 * @param startHorizTaxi whether to allow skips over the beginning of two
	 * @param endHorizTaxi whether to allow skips over the end of two
	 */
	public StringAlignment(String one, String two, 
			boolean startVertTaxi, boolean endVertTaxi, boolean startHorizTaxi, boolean endHorizTaxi) {
		// initializing Path
		super(one.length() + 1, two.length() + 1, -2, 
				new StringAlignmentPath(one, two, startVertTaxi || startHorizTaxi),
				startVertTaxi, endVertTaxi, startHorizTaxi, endHorizTaxi);
		// initializing nodes
		initializeNodes(null, 1, -2, one, two);
	}
}
