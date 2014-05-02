package importer.integration.bulkdata;

import static org.junit.Assert.assertEquals;
import importer.ImportSource;
import importer.integration.bulkdata.BulkTestReportGenerator.BulkDataInfo;

import org.junit.Test;

import utils.TestProperties;

public class ImportSourceBulkTest {
	
	private static final String testRootFolder = TestProperties.INSTANCE.getBulkDataRootFolder(); 
	
	/**
	 * Uses {@link BulkTestReportGenerator} to build out a data set for use with tests.
	 * @throws Exception
	 */
	@Test
	public void testConstruction_multipleFolders() throws Exception{
		int NUM_SUBFOLDERS = 2;
		int TEST_REPORTS_PER_SUBFOLDEr = 2;
		int expectedFiles = NUM_SUBFOLDERS * TEST_REPORTS_PER_SUBFOLDEr;
		BulkDataInfo info = 
			BulkTestReportGenerator.initialiseTestReportStructure(testRootFolder, NUM_SUBFOLDERS, TEST_REPORTS_PER_SUBFOLDEr);
		ImportSource is = new ImportSource(info.rootFolder.getPath());
		assertEquals("ImportSource detected correct # of files.", expectedFiles,  is.computePaths().count());		
	}
}
