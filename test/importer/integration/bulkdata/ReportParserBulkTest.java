package importer.integration.bulkdata;

import static org.junit.Assert.assertEquals;
import folderManager.IFolderData;
import folderManager.JdbcFolderData;
import importer.IBatchImporter;
import importer.ImportSource;
import importer.ReportParser;
import importer.integration.bulkdata.BulkTestReportGenerator.BulkDataInfo;
import importer.jdbc.BatchJdbcImporter;

import java.nio.file.Path;
import java.nio.file.Paths;
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
		H2DataSource.clear(DS);
		bulkTestData = BulkTestReportGenerator.initialiseTestReportStructure();
	}
	
	@AfterClass
	public static void tearDown() {
		DS.close();
	}

	@Test
	public void testParallelParse() throws Exception {
		
		final IFolderData folderData = new JdbcFolderData(DS);
		final IBatchImporter batchImporter = new BatchJdbcImporter(DS, folderData, 1000);
		
		Integer actualImportedTestResultCount = new ForkJoinPool().submit( () -> {
				Integer importedRowCount = 
					bulkTestFilePaths()
					.parallel()
					.map(filePath -> new ReportParser().parse(filePath))
					.map(testResults -> batchImporter.doImport(testResults))
					.collect(Collectors.summingInt(i -> i));
				
				return importedRowCount;
			}
		).get();
		
		Integer expectedTestCount = 
				(bulkTestData.subFolderCount * bulkTestData.reportFilesPerSubfolderCount * bulkTestData.testResultsPerTestReportCount);
		assertEquals("Expected number of test result records where imported.", expectedTestCount, actualImportedTestResultCount);
	}
	
	/*
	 * Compute stream of file paths that represent Test Report sample data. 
	 */
	private Stream<Path> bulkTestFilePaths() {
		ImportSource is = new ImportSource(bulkTestData.rootFolder.getAbsolutePath());
		return is.computePaths().map(s -> Paths.get(s));
	}
}
