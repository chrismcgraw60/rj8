package utils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Throwables;

/**
 * Singleton encapsulating test.properties.
 */
public class TestProperties {
	
	public static TestProperties INSTANCE = initialise();
	
	private static final String PROPERTIES_FILE = "test.properties";
	
	/*
	 * Property Names & default values
	 */
	private static final String bulkDataRootFolder = "bulk.dataRootFolder";
	private static final String bulkDataRootFolder_default = "tmp";
	
	private static final String bulkDataNumSubFolders = "bulk.numSubFolders";
	private static final String bulkDataNumSubFolders_default = "2";
	
	private static final String bulkDataNumTestReports = "bulk.numTestReports";
	private static final String bulkDataNumTestReports_default = "2";
	
	private final Properties props;
	
	/*
	 * Private constructor
	 */
	private TestProperties(Properties props) {
		this.props = props;
	}

	/**
	 * @return The file path of the root folder where bulk test report data should be generated.
	 */
	public String getBulkDataRootFolder() {
		return props.getProperty(bulkDataRootFolder, bulkDataRootFolder_default);
	}
	
	/**
	 * @return The number of sub folders to be created under the root test data folder.
	 * @see TestProperties#getBulkDataRootFolder() 
	 */
	public Integer getBulkNumSubFolders() {
		return Integer.parseInt(props.getProperty(bulkDataNumSubFolders, bulkDataNumSubFolders_default));
	}
	
	/**
	 * @return The number of test report files to be created under each test data subfolder.
	 * @see TestProperties#getBulkNumSubFolders()
	 */
	public Integer getBulkNumTestReports() {
		return Integer.parseInt(props.getProperty(bulkDataNumTestReports, bulkDataNumTestReports_default));
	}
	
	private static synchronized TestProperties initialise() {
		if (null == INSTANCE) {
			try {
				INSTANCE = loadProperties();
			} catch (Exception e) {
				e.printStackTrace();
				Throwables.propagate(e);
			}
		}
		return INSTANCE;
	}

	private static TestProperties loadProperties() throws Exception {
		Properties p = new Properties();
		URL testFileLocation = ClassLoader.getSystemResource(PROPERTIES_FILE);
		p.load(new FileInputStream(new File(testFileLocation.getPath())));
		return new TestProperties(p);
	}
}
