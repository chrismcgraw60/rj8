package importer;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import importer.ReportedTestResultEntry.FailureInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import testdata.TestDataInfo;

public class ReportParserTest {
	
	@Test
	public void testParse() throws Exception {
		ImportSource is = TestDataInfo.getImportSource();
		String testReportPath = is.computePaths().sorted().findFirst().get();
		Stream<ReportedTestElement> parsedElements = ReportParser.parse(testReportPath);
		
		Map<Class<? extends ReportedTestElement>, List<ReportedTestElement>> elemsByClass = 
				parsedElements.collect(Collectors.groupingBy(rte -> rte.getClass()));
		
		List<ReportedTestElement> testSuiteElements = elemsByClass.get(ReportedTestSuiteEntry.class);
		List<ReportedTestElement> testResultElements = elemsByClass.get(ReportedTestResultEntry.class);
		
		/*
		 * Check Suite Entry
		 */
		assertEquals("Expect 1 Test Suite Element", 1, testSuiteElements.size());
		ReportedTestSuiteEntry suiteEntry = (ReportedTestSuiteEntry)testSuiteElements.get(0);
		assertEquals("Suite has expected File Name.",  TestDataInfo.testDataFile, suiteEntry.getContainingFile());
		assertEquals("Suite has expected Folder Name.",  TestDataInfo.testDataFolderName, suiteEntry.getContainingFolder());
		assertEquals("Suite has expected Time Stamp.",  TestDataInfo.testDataSuiteTimeStamp, suiteEntry.getTimestamp());
		assertEquals("Suite has expected qualified Name.", "testdata.AllTests", suiteEntry.getQualifiedName());
		assertEquals("Suite has expected Local Name.", "AllTests", suiteEntry.getLocalTestCaseName());
		assertEquals("Suite has expected Package Name.", "testdata", suiteEntry.getPackageName());
		assertNotNull("Suite has a well formed UUID storage ID.", suiteEntry.getStorageId());
		assertNotNull("Suite has a non-null Time value.", suiteEntry.getTime());
		assertEquals("Suite has expected #tests run.", new Long(18), suiteEntry.getTestsRun());
		assertEquals("Suite has expected #errors.", new Long(3), suiteEntry.getTotalErrors());
		assertEquals("Suite has expected #failures.", new Long(9), suiteEntry.getTotalFailures());
		assertEquals("Suite has expected #skipped.", new Long(0), suiteEntry.getTotalSkipped());
		
		assertEquals("Expect 1 Test Suite Element", 18, testResultElements.size());
		List<String> testResultNames = 
				testResultElements.stream()
					.map(tre -> (ReportedTestResultEntry)tre)
					.map(tre -> tre.getQualifiedName() + "." + tre.getMethodName())
					.sorted().collect(toList());
		
		/*
		 * Check Result Entries.
		 * Sample the list.
		 */
		assertEquals("1st ReportedTestResultEntry as expected.", "testdata.CreateReportTestA.testException_A", testResultNames.get(0));
		assertEquals("7th ReportedTestResultEntry as expected.", "testdata.CreateReportTestB.testException_B", testResultNames.get(6));
		assertEquals("13th ReportedTestResultEntry as expected.", "testdata.CreateReportTestC.testException_C", testResultNames.get(12));
		
		/*
		 * A sample test pass is reported correctly.
		 */
		ReportedTestResultEntry resultEntry = (ReportedTestResultEntry)testResultElements.get(4);
		assertEquals("Result has expected qualified Name.", "testdata.CreateReportTestA", resultEntry.getQualifiedName());
		assertEquals("Result has expected Local Name.", "CreateReportTestA", resultEntry.getLocalTestCaseName());
		assertEquals("Result has expected Package Name.", "testdata", resultEntry.getPackageName());
		assertEquals("Result has expected Method Name.", "testPassAssertionWithComment_A", resultEntry.getMethodName());
		assertNotNull("Result has a well formed UUID storage ID.", resultEntry.getStorageId());
		assertNotNull("Result has a non-null Time value.", resultEntry.getTime());
		assertEquals("Result has expected status string.", resultEntry.getStatus(), ReportedTestResultEntry.STATUS_PASS);
		
		/*
		 * A sample fail is reported correctly.
		 *   
		 	<testcase classname="testdata.CreateReportTestB" name="testFailAssertionWithComment_B" time="0.001">
    			<failure 
    				message="Numbers should be equal. expected:&lt;1&gt; but was:&lt;2&gt;" 
    				type="junit.framework.AssertionFailedError">
    				
    				junit.framework.AssertionFailedError: Numbers should be equal. expected:&lt;1&gt; but was:&lt;2&gt;
					at testdata.CreateReportTestB.testFailAssertionWithComment_B(CreateReportTestB.java:38)
				</failure>
  			</testcase>
		 */
		ReportedTestResultEntry resultFailureEntry = (ReportedTestResultEntry)testResultElements.get(10);
		assertEquals("Result has expected qualified Name.", "testdata.CreateReportTestB", resultFailureEntry.getQualifiedName());
		assertEquals("Result has expected Local Name.", "CreateReportTestB", resultFailureEntry.getLocalTestCaseName());
		assertEquals("Result has expected Package Name.", "testdata", resultFailureEntry.getPackageName());
		assertEquals("Result has expected Method Name.", "testFailAssertionWithComment_B", resultFailureEntry.getMethodName());
		assertNotNull("Result has a well formed UUID storage ID.", resultFailureEntry.getStorageId());
		assertNotNull("Result has a non-null Time value.", resultFailureEntry.getTime());
		assertNotNull("Result has failure info object.", resultFailureEntry.getFailureInfo());
		assertEquals("Result has expected status string.", ReportedTestResultEntry.STATUS_FAIL, resultFailureEntry.getStatus());
		
		FailureInfo failInfo = resultFailureEntry.getFailureInfo();
		assertEquals("FailureInfo has expected type.", "junit.framework.AssertionFailedError", failInfo.getExceptionName());
		assertEquals("FailureInfo has expected message.", "Numbers should be equal. expected:<1> but was:<2>", failInfo.getMessage());
		assertTrue("FailureInfo has expected details 1.", failInfo.getDetails().contains("junit.framework.AssertionFailedError: Numbers should be equal. expected:<1> but was:<2>"));
		assertTrue("FailureInfo has expected details 2.", failInfo.getDetails().contains("at testdata.CreateReportTestB.testFailAssertionWithComment_B(CreateReportTestB.java:38)"));
		assertEquals("FailureInfo has type 'failure'", failInfo.getFailureType(), FailureInfo.Type.failure);
		
		/*
		 * A sample error is reported correctly.
		 *   
			<testcase classname="testdata.CreateReportTestC" name="testException_C" time="0.001">
    			<error message="Error in Test helper method." type="java.lang.RuntimeException">java.lang.RuntimeException: Error in Test helper method.
					at testdata.CreateReportTestC.doSomethingWrong(CreateReportTestC.java:47)
					at testdata.CreateReportTestC.testException_C(CreateReportTestC.java:43)
				</error>
  			</testcase>
		 */
		ReportedTestResultEntry resultErrorEntry = (ReportedTestResultEntry)testResultElements.get(15);
		assertEquals("Result has expected qualified Name.", "testdata.CreateReportTestC", resultErrorEntry.getQualifiedName());
		assertEquals("Result has expected Local Name.", "CreateReportTestC", resultErrorEntry.getLocalTestCaseName());
		assertEquals("Result has expected Package Name.", "testdata", resultErrorEntry.getPackageName());
		assertEquals("Result has expected Method Name.", "testException_C", resultErrorEntry.getMethodName());
		assertNotNull("Result has a well formed UUID storage ID.", resultErrorEntry.getStorageId());
		assertNotNull("Result has a non-null Time value.", resultErrorEntry.getTime());
		assertNotNull("Result has failure info object.", resultErrorEntry.getFailureInfo());
		assertEquals("Result has expected status string.", ReportedTestResultEntry.STATUS_ERROR, resultErrorEntry.getStatus());
		
		FailureInfo errorInfo = resultErrorEntry.getFailureInfo();
		assertEquals("FailureInfo has expected type.", "java.lang.RuntimeException", errorInfo.getExceptionName());
		assertEquals("FailureInfo has expected message.", "Error in Test helper method.", errorInfo.getMessage());
		assertTrue("FailureInfo has expected details 1.", errorInfo.getDetails().contains("java.lang.RuntimeException: Error in Test helper method."));
		assertTrue("FailureInfo has expected details 2.", errorInfo.getDetails().contains("at testdata.CreateReportTestC.doSomethingWrong(CreateReportTestC.java:47)"));
		assertTrue("FailureInfo has expected details 3.", errorInfo.getDetails().contains("at testdata.CreateReportTestC.testException_C(CreateReportTestC.java:43)"));
		assertEquals("FailureInfo has type 'failure'", errorInfo.getFailureType(), FailureInfo.Type.error);
	}
}
