package utils;

import importer.ReportedTestElement;
import importer.ReportedTestResultEntry;
import importer.ReportedTestResultEntry.FailureInfo;
import importer.ReportedTestSuiteEntry;
import importer.jdbc.BatchJdbcImporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * Injects test data directly into the DB.
 *
 */
public class TestDataGenerator {

	private static BoneCPDataSource DS = null;
	private static Random Rnd = new Random();
	private static final String deleteTestSuiteDataSQL = "delete from testSuite";
	
	private static final String folder = "/testFolder";
	private static final String qualifiedSuiteName = "com.chris.AllTests";
	private static final String fileTemplate = "TEST-dev_{$id}.xml";
	
	List<String> qNames = Lists.newArrayList(
			"com.foo.db.SqlTest", //4 
			"com.foo.db.JdbcTest",  //2
			"com.foo.db.ConfigTest", //3
			"com.foo.model.UserTest", //10
			"com.foo.model.AccountTest", //15
			"com.foo.model.ProductTest", //30
			"com.foo.model.orm.OrderDBTest",  //13
			"com.foo.view.AccountViewTest", // 6
			"com.foo.view.ProductWidgetTest", //10
			"com.foo.view.UserLoginTest"); //7
	
	private static final int testSuiteCount = 500;
	private static final int minTestsInSuite = 50;
	
	private static final String dummyErrorStack = 
			"java.lang.RuntimeException: Error in Test helper method\n." + 
			"at testdata.CreateReportTestC.doSomethingWrong(CreateReportTestC.java:49)\n" +
			"at testdata.CreateReportTestC.testException_C(CreateReportTestC.java:45)";
	
	private static final String dummyFailStack = 
			"junit.framework.AssertionFailedError: expected:&lt;1&gt; but was:&lt;2&gt;\n." + 
			"at testdata.CreateReportTestC.testFailAssertionNosComment_C(CreateReportTestC.java:33)";
	
	@BeforeClass
	public static void setUp() throws Exception {
		DS = H2DataSource.create();
	}
	
	@AfterClass
	public static void tearDown() {
		DS.close();
	}
	
	@Test
	public void createSuiteData() throws Exception{
		
		clearDB();
		
		DateTime timestamp = DateTime.now().minusDays(testSuiteCount);
		
		List<ReportedTestElement> testEntries = Lists.newArrayList();
		
		for (int i = 0; i<testSuiteCount; i++) {
			
			long testsInSuite = i + minTestsInSuite;
			ReportedTestSuiteEntry tse = new ReportedTestSuiteEntry();
			tse.setContainingFile(fileTemplate.replace("{$id}", String.valueOf(i)));
			tse.setContainingFolder(folder);
			tse.setQualifiedName(qualifiedSuiteName);
			tse.setStorageId(UUID.randomUUID());
			tse.setTime(randomTimeDuration(testsInSuite));
			tse.setTimestamp(timestamp);
			tse.setTestsRun(testsInSuite);
			tse.setTotalErrors(randomNumTestsInRange(i, 0));
			tse.setTotalFailures(randomNumTestsInRange(i, 1));
			tse.setTotalSkipped(randomNumTestsInRange(i, 0));
			
			testEntries.add(tse);
			
			timestamp = 
				timestamp
					.plusDays(1)
					.withTime(Rnd.nextInt(12), Rnd.nextInt(59), Rnd.nextInt(59), Rnd.nextInt(999));
			
			int createdErrorResults = 0;
			int createdFailureResults = 0;
			int createdSkippedTests = 0;
			
			for (int j = 0; j<testsInSuite; j++) {
				/*
				 * Need to build up from Test Results (but i knew that) ..
				 */
				ReportedTestResultEntry tre = new ReportedTestResultEntry();
				tre.setFailInfo(null);
				
				if (createdErrorResults < tse.getTotalErrors()) {
					FailureInfo fi = 
						new FailureInfo("Error in Test helper method.", "java.lang.RuntimeException", dummyErrorStack, FailureInfo.Type.error);
					tre.setFailInfo(fi);
					createdErrorResults++;
				} 
				else if (createdFailureResults < tse.getTotalFailures()) {
					FailureInfo fi = 
						new FailureInfo("Numbers should be equal. expected:foo but was:bar", "junit.framework.AssertionFailedError", dummyFailStack, FailureInfo.Type.failure);
					tre.setFailInfo(fi);
					createdFailureResults++;
				} 
				else if (createdSkippedTests < tse.getTotalSkipped()) {
					tre.setSkipped(true);
					createdSkippedTests++;
				}
				
				tre.setMethodName("testMethod_" + j);
				tre.setQualifiedName(randomQName());
				tre.setStorageId(UUID.randomUUID());
				tre.setTime("todo");
								
				// then need to add this to test suite and build up results.
				testEntries.add(tre);
			}
			
		}
		
		BatchJdbcImporter.doImport(testEntries.stream(), DS, 500);
	}
	
	private String randomQName() {
		int qNameIndex = Rnd.nextInt(10);
		return qNames.get(qNameIndex);
	}
	
	/*
	 * Return a number which is a random value that falls within the percentage given by percentageRange.
	 * e.g. if I have 1000 tests with %range = 5%, then I should get a number between 0 and 50.
	 */
	private long randomNumTestsInRange(int numTests, int percentageRange) {
		
		if (percentageRange >= numTests) return 0;
		
		int failRangeAsPecentageNumTests  = (int)Math.floor((numTests / 100) * percentageRange);
		
		if (failRangeAsPecentageNumTests <= 0) return 0;
		
//		System.out.println("numTests:" + numTests + ", %Range:" + percentageRange + ", failRange: " + failRangeAsPecentageNumTests);
		return (long)Rnd.nextInt(failRangeAsPecentageNumTests);
		
	}
	
	private String randomTimeDuration(long numTests) {
		double totalDuration = 0;
		for (int i=0; i<numTests; i++) {
			double rndDuration = Rnd.nextDouble() * 10;
			totalDuration += rndDuration;
		}
		return String.valueOf(totalDuration);
	}

	private void clearDB() throws SQLException {
		Connection conn = DS.getConnection();
		PreparedStatement deleteTestSuitesStmt = conn.prepareStatement(deleteTestSuiteDataSQL);
		deleteTestSuitesStmt.executeUpdate();
	}
}
