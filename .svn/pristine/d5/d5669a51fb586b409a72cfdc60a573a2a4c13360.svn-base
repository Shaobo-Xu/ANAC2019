package test;

@SuppressWarnings("serial")
public class NotEqualException extends Exception {

	/**
	 * 
	 * @param message
	 *            detail message on what differs.
	 * @param linenr
	 *            the line where contents were not equal. first line =1.
	 * @param expected
	 *            the expected contents
	 * @param actual
	 *            the actual contents
	 */
	public NotEqualException(String message, int linenr, String expected,
			String actual) {

		super("Line " + linenr + ":" + message + ". Expected " + expected
				+ " but found " + actual);
	}

}
