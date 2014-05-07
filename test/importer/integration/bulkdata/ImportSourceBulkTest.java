package importer.integration.bulkdata;

import static org.junit.Assert.assertEquals;
import importer.ImportSource;
import importer.integration.bulkdata.BulkTestReportGenerator.BulkDataInfo;

import org.junit.Test;

public class ImportSourceBulkTest {
		
	/**
	 * Uses {@link BulkTestReportGenerator} to build out a data set for use with tests.
	 * @throws Exception
	 */
	@Test
	public void testConstruction_multipleFolders() throws Exception{
		BulkDataInfo info = BulkTestReportGenerator.initialiseTestReportStructure();
		int expectedFiles = info.subFolderCount * info.reportFilesPerSubfolderCount;
		ImportSource is = new ImportSource(info.rootFolder.getPath());
		assertEquals("ImportSource detected correct # of files.", expectedFiles,  is.computePaths().count());		
	}
}
