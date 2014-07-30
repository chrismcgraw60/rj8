package folderManager.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import utils.H2DataSource;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
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
		assertEquals(newF.getId(), fetchedF.getId());
		assertEquals(newF.getPath(), fetchedF.getPath());
		
		Path subFolderPath = path.resolve("sub1");
		Folder subF = folderData.getFolder(subFolderPath, true);
		assertNotNull(subF);
		assertNotNull(subF.getId());
		assertEquals(subFolderPath, subF.getPath());
	}
	
	/**
	 * Verify that tying to get a non-existent folder without createIfAbsent
	 * will throw.
	 */
	@Test(expected = RuntimeException.class) 
	public void test_expected_failure() throws Exception {
		IFolderData folderData = new JdbcFolderData(DS);
		URL workingFolderUrl = this.getClass().getResource(".");
		Path path = Paths.get(workingFolderUrl.toURI());
		
		Path doesntExist = path.resolve("wontFindeMe");
		folderData.getFolder(doesntExist, false);
	}
	
	/**
	 * TEST CONCURRENT CREATES
	 */
	@Test
	public void concurrentGetsShouldCreateOnlyOnFolder() throws Exception {
		
		IFolderData folderData = new JdbcFolderData(DS);
		
		URL workingFolderUrl = this.getClass().getResource(".");
		Path path = Paths.get(workingFolderUrl.toURI());

		final Path folderPath = path.resolve("folder");	
		ExecutorService exec = Executors.newFixedThreadPool(500);
		
		List<Callable<Folder>> folderGets = Lists.newArrayList();
		for (int i=0; i<100000; i++) { 
			folderGets.add(() -> {return folderData.getFolder(folderPath, true);});
		}
						
		exec.invokeAll(folderGets)
			.stream()
			.map(folderFuture -> folderFrom(folderFuture))
			.forEach(fetchedFolder -> assertEquals(folderPath, fetchedFolder.getPath()));
		
		assertSingleFolderInFolderData(folderPath);
		
		exec.shutdown();
	}
	
	private void assertSingleFolderInFolderData(Path folderPath) {
		
		String query = String.format(JdbcFolderData.loadFolderSQL, folderPath.toString());
		
		ResultSet rs = null;
				
		try (Connection conn = DS.getConnection(); 
				PreparedStatement q = conn.prepareStatement(query)){
			
			rs = q.executeQuery();
			assertTrue("Should contain 1 result", rs.first());
			assertFalse("Should contain only 1 result.", rs.next());
			
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
	}

	private Folder folderFrom(Future<Folder> folderFuture) {
		
		Folder folder  = null;
		
		try { folder = folderFuture.get(); } 
		catch (Exception e) { Throwables.propagate(e); }
		
		return folder;
		
	}
}
