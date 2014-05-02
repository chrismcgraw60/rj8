package importer;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

/**
 * Models a TestSuite element from a Junit Report.
 */
public class ReportedTestSuiteEntry extends ReportedTestElement {
	
	private Long testsRun;
	private String fileName;
	private String folderName;
	private DateTime timestamp;

	/**
	 * @return # of tests run in the Test suite.
	 */
	public Long getTestsRun() {
		return this.testsRun;
	}
	
	/**
	 * @param testsRun # of tests run in the Test suite.
	 * @return This object.
	 */
	public ReportedTestSuiteEntry setTestsRun(Long testsRun) {
		Preconditions.checkNotNull(testsRun, "testsRun must not be null.");
		this.testsRun = testsRun;
		return this;
	}
	
	/**
	 * The name of the Junit Report file where the Suite entry originated.
	 * @return This object.
	 */
	public String getContainingFile() {
		return this.fileName;
	}
	
	/**
	 * @param fileName The name of the Junit Report file where the Suite entry originated.
	 * @return This object.
	 */
	public ReportedTestSuiteEntry setContainingFile(String fileName) {
		Preconditions.checkNotNull(fileName, "fileName must not be null.");
		this.fileName = fileName;
		return this;
	}
	
	/**
	 * The name of the Folder containing this Suite's corresponding Junit Report file.
	 * @return This object.
	 */
	public String getContainingFolder() {
		return this.folderName;
	}
	
	/**
	 * @param folderName The name of the Folder containing this Suite's corresponding Junit Report file.
	 * @return This object.
	 */
	public ReportedTestSuiteEntry setContainingFolder(String folderName) {
		Preconditions.checkNotNull(folderName, "folderName must not be null.");
		this.folderName = folderName;
		return this;
	}

	/**
	 * @return The timestamp as per the Junit Report.
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp as per the Junit Report.
	 * @return This object.
	 */
	public ReportedTestSuiteEntry setTimestamp(DateTime timestamp) {
		Preconditions.checkNotNull(timestamp, "timestamp must not be null.");
		this.timestamp = timestamp;
		return this;
	}

	/* (non-Javadoc)
	 * @see importer.ReportedTestElement#validateState()
	 */
	@Override
	public void validateState() {
		super.validateState();
		checkStateNotNull(this.testsRun, "testsRun was not set.");	
		checkStateNotNull(this.fileName, "fileName was not set.");
		checkStateNotNull(this.folderName, "folderName was not set.");
		checkStateNotNull(this.timestamp, "timestamp was not set.");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TestSuite {className=").append(this.qualifiedName).append("}");
		return sb.toString();
	}
}
