package importer.integration.bulkdata;

import static org.junit.Assert.assertEquals;
import importer.ImportSource;
import importer.ReportParser;
import importer.integration.bulkdata.BulkTestReportGenerator.BulkDataInfo;
import importer.jdbc.BatchJdbcImporter;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.H2DataSource;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * This test is currently doing too much as it imports straight to DB.
 * Keeping it here just now for reference.
 */
public class ReportParserBulkTest {
	
	private static BulkDataInfo bulkTestData = null;
	
	private static BoneCPDataSource DS = null;
		
	@BeforeClass
	public static void setUp() throws Exception {
		DS = H2DataSource.create();
		bulkTestData = BulkTestReportGenerator.initialiseTestReportStructure();
	}
	
	@AfterClass
	public static void tearDown() {
		DS.close();
	}

	@Test
	public void testParallelParse() throws Exception {
		
		Integer actualImportedTestResultCount = new ForkJoinPool().submit( () -> {
				Integer c = 
					bulkTestFilePaths()
					.parallel()
					.map(fp -> ReportParser.parse(fp))
					.map(tce -> BatchJdbcImporter.doImport(tce, DS, 1000))
					.collect(Collectors.summingInt(i -> i));
				
				return c;
			}
		).get();
		
		Integer expectedTestCount = 
				(bulkTestData.subFolderCount * bulkTestData.reportFilesPerSubfolderCount * bulkTestData.testResultsPerTestReportCount);
		assertEquals("Expected number of test result records where imported.", expectedTestCount, actualImportedTestResultCount);
	}
	
	/*
	 * Compute stream of file paths that represent Test Report sample data. 
	 */
	private Stream<String> bulkTestFilePaths() {
		ImportSource is = new ImportSource(bulkTestData.rootFolder.getAbsolutePath());
		return is.computePaths();
	}
}
