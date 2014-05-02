package importer.integration.bulkdata;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import utils.TestProperties;

import com.google.common.io.Files;

/**
 * A helper class for generating large amounts of Test Report Data.
 * The main purpose of this class is to provide data for performance profiling.
 */
public class BulkTestReportGenerator {

	/**
	 * The number of Test results in the sample bulk Test Report.
	 */
	private static final int sourceTestReport_numTests = 1740;
	private static final String sourceTestReportFile = "TEST-aa1.xml";
	
	private static final String rootFolderName = TestProperties.INSTANCE.getBulkDataRootFolder();
	private static final Integer subFolderCount = TestProperties.INSTANCE.getBulkNumSubFolders();
	private static final Integer testReportCount = TestProperties.INSTANCE.getBulkNumTestReports();
	
	/**
	 * Initializes bulk test data from test.properties file.
	 * @see BulkTestReportGenerator#initialiseTestReportStructure(String, int, int)
	 */
	public static BulkDataInfo initialiseTestReportStructure() throws Exception{
		return initialiseTestReportStructure(rootFolderName, subFolderCount, testReportCount);
	}
	
	/**
	 * <p>
	 * Creates a folder structure containing simulated test result data. All of the Test data originates from the
	 * sample Test ReportFile : "TEST-aa1.xml". This file contains a Test-Suite Report with 1740 tests. The parameters
	 * control how many sub-folders and copies of this file will be created.
	 * </p>
	 * @param rootFolderName The path to the folder which will act as the root of the test folder structure. If it does not
	 * exist it will be created. If it exists then no further action will be taken.  
	 * @param subFolderCount The number of sub-folders that will be created under the root. 
	 * @param testRunsPerSubFolder The number of copies of the Test Report file that will be copied under each sub-folder.
	 * @return A {@link BulkDataInfo} object representing the result of the data initialization..
	 * @throws Exception
	 */
	public static BulkDataInfo initialiseTestReportStructure(String rootFolderName, int subFolderCount, int testRunsPerSubFolder) throws Exception{
		/*
		 * Location of sample results file that we want to replicate.
		 */
		URL sourceFileUrl = BulkTestReportGenerator.class.getResource(sourceTestReportFile);
		String sourceFilePath = sourceFileUrl.getFile();
		File sourceFile = new File(sourceFilePath);
		
		/*
		 * Root folder of target folder structure.
		 * If the folder already exists then we assume the test set is good and we will use it.
		 */
		URL workingFolderUrl = BulkTestReportGenerator.class.getResource(".");
		File rootTargetFolder = new File(workingFolderUrl.getFile().concat(rootFolderName));
		if (!rootTargetFolder.exists()) { 
			rootTargetFolder.mkdir(); 
			simulateTestReportData(sourceFile, rootTargetFolder, subFolderCount, testRunsPerSubFolder);	
		}
		
		BulkDataInfo info = new BulkDataInfo();
		info.rootFolder = rootTargetFolder;
		info.subFolderCount = subFolderCount;
		info.reportFilesPerSubfolderCount = testRunsPerSubFolder;
		
		return info;
	}

	private  static void simulateTestReportData(File sourceFile, File rootTargetFolder, int numFolders, int numCopiedFiles) throws IOException {
		for (int folderCounter=1; folderCounter<=numFolders; folderCounter++) {
			String targetFolderPath = targetFolderPath(rootTargetFolder, folderCounter);
			File targetFolder = makeDir(targetFolderPath);
			copyFileIntoTargetFolder(sourceFile, targetFolder, numCopiedFiles, folderCounter);
		}
	}

	private static File makeDir(String targetFolderPath) {
		File newFolder = new File(targetFolderPath);
		newFolder.mkdir();
		return newFolder;
	}

	private static void copyFileIntoTargetFolder(File sourceFile, File targetFolder, int numCopiedFiles, int folderNumber) throws IOException {
		for (int fileCounter=1; fileCounter <= numCopiedFiles; fileCounter++) {
			String filePath = targetFilePath(targetFolder, folderNumber, fileCounter);
			Files.copy(sourceFile, new File(filePath));
		}
	}

	private static String targetFolderPath(File rootTargetFolder, int folderCounter) {
		return 
			new StringBuilder()
				.append(rootTargetFolder.toPath())
				.append(File.separator)
				.append("developer_")
				.append(folderCounter)
				.toString();
	}
	
	private static String targetFilePath(File targetFolder, int folderNumber, int fileCounter) {
		return new StringBuilder()
			.append(targetFolder.getPath())
			.append(File.separator)
			.append("TEST-developer")
			.append(folderNumber)
			.append("_testRun_")
			.append(fileCounter)
			.append(".xml")
			.toString();
	}
	
	/**
	 * Encapsulates interesting metrics about the test data generated from a call to 
	 * {@link BulkTestReportGenerator#initialiseTestReportStructure()}
	 */
	public static class BulkDataInfo {
		public File rootFolder;
		public Integer subFolderCount;
		public Integer reportFilesPerSubfolderCount;
		public Integer testResultsPerTestReportCount = sourceTestReport_numTests;
	}
}
