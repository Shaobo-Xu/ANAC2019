package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import genius.cli.Runner;
import genius.core.exceptions.InstantiateException;

public class mediatortest {

	@Test
	public void test1() throws IOException, NotEqualException, JAXBException, InstantiateException {
		File output = File.createTempFile("test", ".csv");

		Runner.main(new String[] { "src/test/resources/test/mediatortest.xml",
				output.getCanonicalPath().replaceFirst("[.][^.]+$", "") });
		equal(new File("src/test/resources/test/mediatortestexpected.txt"), output);
	}

	/**
	 * Check the output log files line by line.
	 * 
	 * @param expectedFile
	 *            the file containing the expected log
	 * @param actualFile
	 *            the file containing the actual log
	 * @return
	 * @throws IOException
	 * @throws NotEqualException
	 */
	private void equal(File expectedFile, File actualFile) throws IOException, NotEqualException {
		BufferedReader expectedFileReader = new BufferedReader(new FileReader(expectedFile));
		BufferedReader actualFileReader = new BufferedReader(new FileReader(actualFile));

		int linenr = 1;
		String expectedLine, actualLine;
		while ((expectedLine = expectedFileReader.readLine()) != null) {
			actualLine = actualFileReader.readLine();
			equal(linenr, expectedLine, actualLine);
			linenr++;
		}

		expectedFileReader.close();
		actualFileReader.close();
	}

	/**
	 * ignore run time(s), agent1, agent2, agent3, agent4. We ignore the agents
	 * because they contain memory address
	 */
	List<Integer> indicesToSkip = Arrays.asList(0, 12, 13, 14, 15);

	/**
	 * Check that given lines are equal. ";" is separator character.
	 * 
	 * @param linenr
	 * @param expected
	 *            the expected line in the log
	 * @param actual
	 *            the actual line in the log
	 * @throws NotEqualException
	 */

	private void equal(int linenr, String expected, String actual) throws NotEqualException {
		String[] expectedElems = expected.split(";");
		String[] actualElems = actual.split(";");
		if (expectedElems.length != actualElems.length) {
			throw new NotEqualException("Lines do not contain the same number of results", linenr,
					"" + expectedElems.length, "" + actualElems.length);
		}
		for (int i = 0; i < expectedElems.length; i++) {
			if (indicesToSkip.contains(i))
				continue;
			String expectedElem = expectedElems[i];
			String actualElem = actualElems[i];
			boolean equals = false;
			try {
				equals = Math.abs(Double.parseDouble(expectedElem) - Double.parseDouble(actualElem)) < 0.000001;
			} catch (NumberFormatException e) {
				equals = expectedElem.equals(actualElem);
			}
			if (!equals) {
				throw new NotEqualException("element " + (i + 1) + " is not equal", linenr, expectedElem, actualElem);
			}
		}
	}
}
