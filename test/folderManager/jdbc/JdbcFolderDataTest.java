package folderManager.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import utils.H2DataSource;

import com.jolbox.bonecp.BoneCPDataSource;

import folderManager.Folder;
import folderManager.IFolderData;
import folderManager.JdbcFolderData;

public class JdbcFolderDataTest {
	
	private final BoneCPDataSource DS;
	
	public JdbcFolderDataTest() {
		DS = H2DataSource.create();
	}
	
	@Before
	public void setUp() throws Exception {
		H2DataSource.clear(DS);
	}
	
	@Test
	public void testCreateFolder() throws Exception {
		IFolderData folderData = new JdbcFolderData(DS);
		
		URL workingFolderUrl = this.getClass().getResource(".");
		Path path = Paths.get(workingFolderUrl.toURI());
		
		Folder newF = folderData.createFolder(path);
		assertNotNull(newF);
		assertNotNull(newF.getId());
		assertEquals(path, newF.getPath());
		
		Folder fetchedF = folderData.getFolder(path, false);
		assertNotNull(fetchedF);
		
		Path subFolder = path.resolve("sub1");
		Folder subF = folderData.getFolder(subFolder, true);
		
		Path subFolder2 = path.resolve("sub2");
		Folder subF2 = folderData.getFolder(subFolder2, false);

	}
	
	/**
	 * TEST CONCURRENT CREATES
	 */

}
