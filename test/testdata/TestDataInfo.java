package testdata;

import java.io.File;
import java.net.URL;

import org.joda.time.DateTime;

import importer.ImportSource;

/**
 * Helper class that encapsulates access to sample test report data files.
 * These data files are intended to be used as a basis for the unit level testing
 * where required.
 */
public class TestDataInfo {
	public static final String testDataFolderName = "ant-test-results";
	public static final String malformedDataFolderName = "malformed-data";
	public static final String testDataFolderPath = "/test/";
	public static final String testDataFile = "TEST-testdata.AllTests.xml";
	public static final String malformedDataFile = "malformed-import-file.txt";
	public static final DateTime testDataSuiteTimeStamp = DateTime.parse("2014-05-19T08:45:56");
	
	/**
	 * @return The path to the Test Report File's parent folder.
	 */
	public static String getTestReportFolderPath() {
		URL here = ClassLoader.getSystemResource(".");
		File fHere = new File(here.getPath());
		String path = fHere.getParentFile().getAbsoluteFile() + testDataFolderPath + testDataFolderName;
		return path;
	}
	
	/**
	 * @return The path to the Test Report File.
	 */
	public static String getTestReportFilePath() {
		return getTestReportFolderPath() + "/" + testDataFile;
	}
	
	/**
	 * @return The path to a file that will through an Exception in the report parser.
	 */
	public static String getMalformedReportFilePath() {
		URL here = ClassLoader.getSystemResource(".");
		File fHere = new File(here.getPath());
		String path = fHere.getParentFile().getAbsoluteFile() + testDataFolderPath + malformedDataFolderName + "/" + malformedDataFile;
		return path;
	}
	
	/**
	 * @return An {@link ImportSource} object that has been configured with the test data folder.
	 */
	public static ImportSource getImportSource() {
		return new ImportSource(getTestReportFolderPath());
	}
}
