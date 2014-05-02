package importer;

import com.google.common.base.Preconditions;

/**
 * Represents a Test Result as per the Junit report element: <b>testcase</b>.
 */
public class ReportedTestResultEntry extends ReportedTestElement {
	
	private Long id = new Long(-1);
	private String testMethodName;

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
}
