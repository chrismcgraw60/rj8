package importer.jdbc;

import importer.ReportedTestElement;
import importer.ReportedTestResultEntry;
import importer.ReportedTestResultEntry.FailureInfo;
import importer.ReportedTestSuiteEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Encapsulates bulk DB Import logic for TestEntries.
 * SAMPLE QUERY:
 * SELECT TESTENTRY.CLASSNAME, TESTENTRY.METHODNAME,TESTSUITE.CLASSNAME 
 * FROM TESTENTRY INNER JOIN TESTSUITE ON TESTENTRY.SUITE_ID = TESTSUITE.ID  
 * WHERE TESTENTRY.SUITE_ID = 3
 */
public class BatchJdbcImporter {

	private static final String insertTestCaseSQL = 
			"insert into testEntry (uuid, className, methodName, time, status, failexception, failmessage, faildetail, suite_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String insertTestSuiteSQL = 
			"insert into testSuite (uuid, className, time, folder, file, tests, failures, errors, skipped, timestamp) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 * Import a given {@link Stream} of {@link ReportedTestElement}s to a database.
	 * @param testCaseEntries The {@link ReportedTestElement}s to be imported. Must not be null.
	 * @param ds The target {@link DataSource}s for the import. Must not be null.
	 * @param batchSize Controls the size of the INSERT batch that is sent to the database. Must be > 0.
	 * @return The # of imported elements.
	 */
	public static int doImport(Stream<ReportedTestElement> testCaseEntries, DataSource ds, int batchSize) {
		Preconditions.checkNotNull(testCaseEntries, "testCaseEntries must not be null.");
		Preconditions.checkNotNull(ds, "ds must not be null.");
		Preconditions.checkArgument(batchSize > 0, "batchSize must be greater than 0.");
		
		Integer count = 0;
				
		try {
			Connection conn = ds.getConnection();
			PreparedStatement insertTestCaseStmt = conn.prepareStatement(insertTestCaseSQL);
			PreparedStatement insertTestSuiteStmt = conn.prepareStatement(insertTestSuiteSQL);
			try {
				
				Long currentSuiteId = null;
				
				for (ReportedTestElement te: testCaseEntries.collect(Collectors.toList())) {
					
					if (te instanceof ReportedTestSuiteEntry) {
						/*
						 * If we come across a Test Suite Entry, we immediately insert it and
						 * use the returned key for the following Test Result Entries.
						 */
						currentSuiteId = insertTestSuiteEntryAndReturnID(insertTestSuiteStmt, te);
					}
					
					if (te instanceof ReportedTestResultEntry) {
						/*
						 * Test Results are inserted with a FK to their containing Test Suites.
						 */
						addTestResultToInsertBatch(te, insertTestCaseStmt, currentSuiteId);
						/*
						 * Send bulk insert to DB if we hit the batch limit.
						 */
						if(++count % batchSize == 0) {
					    	insertTestCaseStmt.executeBatch();
					    }
					}
				}
				/*
				 * Insert any remaining Test Entries.
				 */
				insertTestCaseStmt.executeBatch();
			}
			finally {
				insertTestCaseStmt.close();
				insertTestSuiteStmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		
		return count;
	}

	private static void addTestResultToInsertBatch(ReportedTestElement te, PreparedStatement insertTestCase, Long keyOfLastCreatedSuite)
			throws SQLException {
		ReportedTestResultEntry tre = (ReportedTestResultEntry)te;
		insertTestCase.setString(1, tre.getStorageId().toString());
		insertTestCase.setString(2, tre.getQualifiedName());
		insertTestCase.setString(3, tre.getMethodName());
		insertTestCase.setString(4, tre.getTime());
		
		insertTestCase.setString(5, tre.getStatus());
		
		FailureInfo fi = tre.getFailureInfo();
		
		insertTestCase.setString(6, fi == null ? null : fi.getExceptionName());
		insertTestCase.setString(7, fi == null ? null : fi.getMessage());
		insertTestCase.setString(8, fi == null ? null : fi.getDetails());
		
		insertTestCase.setLong(9, keyOfLastCreatedSuite);
		insertTestCase.addBatch();
	}

	private static Long insertTestSuiteEntryAndReturnID(PreparedStatement insertTestSuite, ReportedTestElement te)
			throws SQLException {
		ReportedTestSuiteEntry tse = (ReportedTestSuiteEntry)te;
		insertTestSuite.setString(1, tse.getStorageId().toString());
		insertTestSuite.setString(2, tse.getQualifiedName());
		insertTestSuite.setString(3, tse.getTime());
		insertTestSuite.setString(4, tse.getContainingFolder());
		insertTestSuite.setString(5, tse.getContainingFile());
		
		
		insertTestSuite.setLong(6, tse.getTestsRun());
		insertTestSuite.setLong(7, tse.getTotalFailures());
		insertTestSuite.setLong(8, tse.getTotalErrors());
		insertTestSuite.setLong(9, tse.getTotalSkipped());
		
		insertTestSuite.setTimestamp(10,  new Timestamp(tse.getTimestamp().getMillis()));
		
		Long idOfInsertedRecord = null;
		
		int affectedRows = insertTestSuite.executeUpdate();
		if (affectedRows == 0) {
		    throw new RuntimeException("Creating test suite entry failed, no rows affected.");
		}
		
		ResultSet generatedKeys = insertTestSuite.getGeneratedKeys();
		
		if (generatedKeys.next()) {
			idOfInsertedRecord = generatedKeys.getLong(1);
		} else {
		    throw new RuntimeException("Creating test suite entry failed, no generated key obtained.");
		}
		
		return idOfInsertedRecord;
	}
}
