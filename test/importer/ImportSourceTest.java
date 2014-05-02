package importer;

import static org.junit.Assert.assertEquals;
import importer.ImportSource;

import org.junit.Test;

import testdata.TestDataInfo;

public class ImportSourceTest {
	
	/**
	 * NOTE: This test requires the folder ant-test-results to be generated. 
	 * This can be done by running gen-testData.sh (TODO: run ant from code to generate the test data). 
	 */
	@Test
	public void testConstruction() {
		/*
		 * From File reference.
		 */
		ImportSource is = new ImportSource(TestDataInfo.getTestReportFilePath());
		assertEquals("ImportSource constructed from file. ", TestDataInfo.getTestReportFilePath(), is.computePaths().findFirst().get());
		/*
		 * From Folder reference.
		 */
		is = new ImportSource(TestDataInfo.getTestReportFolderPath());
		assertEquals("ImportSource constructed from folder. ", TestDataInfo.getTestReportFilePath(), is.computePaths().findFirst().get());
	}
}
