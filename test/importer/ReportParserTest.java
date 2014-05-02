package importer;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
		assertEquals("Suite has expected #tests run.", suiteEntry.getTestsRun(), new Long(18));
		
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
	}
}
