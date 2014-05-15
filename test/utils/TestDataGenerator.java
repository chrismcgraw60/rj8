package utils;

import importer.ReportedTestElement;
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
	private static final String qualifiedName = "com.chris.AllTests";
	private static final String fileTemplate = "TEST-dev_{$id}.xml";
	
	private static final int testSuiteCount = 1000;
	private static final int maxTestsInSuite = 10000;
	private static final int minTestsInSuite = 10;
	
	
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
		
		DateTime timestamp = DateTime.now().minusDays(maxTestsInSuite);
		
		List<ReportedTestElement> suiteEntries = Lists.newArrayList();
		
		for (int i = 0; i<testSuiteCount; i++) {
			
			long testsInSuite = i + minTestsInSuite;
			ReportedTestSuiteEntry tse = new ReportedTestSuiteEntry();
			tse.setContainingFile(fileTemplate.replace("{$id}", String.valueOf(i)));
			tse.setContainingFolder(folder);
			tse.setQualifiedName(qualifiedName);
			tse.setStorageId(UUID.randomUUID());
			tse.setTime(randomTimeDuration(testsInSuite));
			tse.setTimestamp(timestamp);
			tse.setTestsRun(testsInSuite);
			tse.setTotalErrors(randomNumTestsInRange(i, 2));
			tse.setTotalFailures(randomNumTestsInRange(i, 5));
			tse.setTotalSkipped(randomNumTestsInRange(i, 1));
			
			suiteEntries.add(tse);
			
			timestamp = 
				timestamp
					.plusDays(1)
					.withTime(Rnd.nextInt(12), Rnd.nextInt(60), Rnd.nextInt(60), Rnd.nextInt(1000));
		}
		
		BatchJdbcImporter.doImport(suiteEntries.stream(), DS, 500);
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
