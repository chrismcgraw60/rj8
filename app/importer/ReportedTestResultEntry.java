package importer;

import importer.ReportedTestResultEntry.FailureInfo.Type;

import com.google.common.base.Preconditions;

/**
 * Represents a Test Result as per the Junit report element: <b>testcase</b>.
 */
public class ReportedTestResultEntry extends ReportedTestElement {
	
	public static final String STATUS_PASS = "PASS";
	public static final String STATUS_FAIL = "FAIL";
	public static final String STATUS_ERROR = "ERROR";
	
	private Long id = new Long(-1);
	private String testMethodName;
	private FailureInfo failInfo;

	public String getStatus() {
		if (getFailureInfo() != null) {
			return failInfo.getFailureType() == Type.failure ? STATUS_FAIL : STATUS_ERROR;
		}
		return STATUS_PASS;
	}
	
	public FailureInfo getFailureInfo() {
		return failInfo;
	}

	public void setFailInfo(FailureInfo failInfo) {
		this.failInfo = failInfo;
	}

	public Long getId() {
		return this.id;
	}
	
	public ReportedTestResultEntry setId(Long id) {
		Preconditions.checkNotNull(id, "id must not be null.");
		this.id = id;
		return this;
	}
	
	public String getMethodName() {
		return this.testMethodName;
	}
	
	public ReportedTestResultEntry setMethodName(String testMethodName) {
		Preconditions.checkNotNull(testMethodName, "testMethodName must not be null.");
		this.testMethodName = testMethodName;
		return this;
	}

	@Override
	public void validateState() {
		super.validateState();
		checkStateNotNull(this.id, "id was not set.");	
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TestCase {qName=").append(this.qualifiedName).append(", methodName=" + this.testMethodName + "}");
		return sb.toString();
	}
	
	/**
	 * Encapsulates JUnit failure / exception.
	 */
	/*
	 *   <testcase classname="testdata.CreateReportTestB" name="testFailAssertionWithComment_B" time="0.001">
	 *   	<failure message="Numbers should be equal. expected:&lt;1&gt; but was:&lt;2&gt;" type="junit.framework.AssertionFailedError">
	 *   		junit.framework.AssertionFailedError: Numbers should be equal. expected:&lt;1&gt; but was:&lt;2&gt;
	 *   		at testdata.CreateReportTestB.testFailAssertionWithComment_B(CreateReportTestB.java:38)
	 *   	</failure>
	 *   </testcase>
	 */
	public static class FailureInfo {
		
		public static enum Type { error, failure };
		
		final String message;
		final String exceptionName;
		final String details;
		final Type failureType;
		
		public FailureInfo(String message, String execptionName, String details, Type failureType) {
			this.message = message;
			this.exceptionName = execptionName;
			this.details = details;
			this.failureType = failureType;
		}

		public String getMessage() {
			return message;
		}

		public String getExceptionName() {
			return exceptionName;
		}

		public String getDetails() {
			return details;
		}

		public Type getFailureType() {
			return failureType;
		}		
	}
}
