package filewatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import importer.events.ImportFailed;
import importer.events.ImportStarted;
import importer.events.ImportSuccessful;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import utils.FolderWatchUtils;
import utils.H2DataSource;

import com.google.common.collect.Sets;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * Covers usage of {@link ImportFileWatcher} class. Currently 2 cases:
 * <ul>
 * <li> Verify all events are emitted for in bulk creation of a folder tree into the watch folder.
 * <li> Verify all events are emitted and service continues after creation of malformed file within the watch folder.
 * <ul>
 */
public class ImportFilewatcherTest {
		
	private static BoneCPDataSource DS = null;
	
	private static FolderWatchUtils folderUtils = null;
	
	@BeforeClass
	public static void setUp() throws Exception {
		DS = H2DataSource.create();
		H2DataSource.clear(DS);
		
		folderUtils = new FolderWatchUtils(DS);
	}
	
	/**
	 * Covers successful import of a folder sub-tree under the watch folder.
	 * All files in the sub  tree should be successfully imported.
	 */
	@Test
	public void testBulkFileCreation() throws Exception {

		final ImportFileWatcher watcher = folderUtils.initialiseWatcher();
		
		/*
		 *  Watcher runs async so we'll use the latch to control the test execution
		 *  - If expected conditions are met we'll count-down the latch and continue the test.
		 *  - If expected conditions are not met in a specified amount of time, the latch will
		 *  time out and we'll fail the test.
		 */
		final CountDownLatch latch = new CountDownLatch(1);
		/*
		 * Counts the number of times that the watcher notifies an import. The expected # import
		 * events is the same as the # files that are copied into the watch folder.
		 */
		final AtomicInteger eventCounter = new AtomicInteger(1);
		/*
		 * Tracks the file paths that the watcher tells us were imported successfully.
		 */
		final Set<Path> importSuccessfulOnPaths = Sets.newHashSet();
		/*
		 * Tracks the file paths that the watcher tells us imports were started.
		 */
		final Set<Path> importStartedOnPaths = Sets.newHashSet();
		
		/*
		 * Set up a subscriber on the watcher to verify the expected events are being triggered.
		 */
		final int SUB_FOLDER_COUNT = 10;
		final int TEST_FILE_COUNT = 20;
		
		watcher.events()
			.ofType(ImportStarted.class)
			.subscribe( ev -> importStartedOnPaths.add(ev.getPath()));
		
		watcher.events()
			.ofType(ImportSuccessful.class)
			.subscribe( ev -> {
					importSuccessfulOnPaths.add(ev.getPath());
					if (eventCounter.getAndIncrement() == (SUB_FOLDER_COUNT * TEST_FILE_COUNT)) latch.countDown(); 
				}
			);
				
		/*
		 * Start the watcher and create the test folder structure under the watch root.
		 */
		watcher.start();	
		final Set<Path> expectedFiles = 
			folderUtils.bulkCreationCopyTestFilesTo(watcher.getWatchFolder(), 1, SUB_FOLDER_COUNT, TEST_FILE_COUNT);
		
		assertTrue("Should finish in 10s.", latch.await(10, TimeUnit.SECONDS));
		assertEquals(expectedFiles, importSuccessfulOnPaths);
		assertEquals(importSuccessfulOnPaths, importStartedOnPaths);
		
		watcher.stop();
	}
	
	/**
	 * Covers case of importing an unrecognized file format and verifies that the watcher emits a failure 
	 * event but continues to process valid files.
	 * 
	 * We'll import 1 bad file followed by 2 good files. So we should get 3 corresponding events.
	 */
	@Test
	public void testFileCreation_UnknownFormat() throws Exception {
		
		final ImportFileWatcher watcher = folderUtils.initialiseWatcher();
		/*
		 * We expect 3 events before continuing the test's thread.
		 */
		final CountDownLatch latch = new CountDownLatch(3);
		/*
		 * Tracks the file paths that the watcher tells us were imported successfully.
		 */
		final Set<Path> pathsWhereImportSuccessful = Sets.newHashSet();
		/*
		 * Tracks the file paths that the watcher tells us a failure occurred.
		 */
		final Set<Path> pathsWhereImportFailed = Sets.newHashSet();
		/*
		 * Tracks the file paths that the watcher tells us imports were started.
		 */
		final Set<Path> pathsWhereImportStarted = Sets.newHashSet();
		
		watcher.events()
			.ofType(ImportStarted.class)
			.subscribe( ev -> pathsWhereImportStarted.add(ev.getPath()));
		
		watcher.events()
			.ofType(ImportFailed.class)
			.subscribe(
				ev -> { 
					pathsWhereImportSuccessful.add(ev.getPath());
					latch.countDown();
				}
			);
		
		watcher.events()
			.ofType(ImportSuccessful.class)
			.subscribe( ev -> {
					pathsWhereImportFailed.add(ev.getPath());
					latch.countDown(); 
				}
			);
		
		watcher.start();
		
		final Set<Path> expectedFails = folderUtils.copyMalformedTestFileTo(watcher.getWatchFolder());
		final Set<Path> expectedSucesses = folderUtils.bulkCreationCopyTestFilesTo(watcher.getWatchFolder(), 1, 1, 2);
		
		assertTrue("Should finish in 5s.", latch.await(5, TimeUnit.SECONDS));
		assertEquals(expectedFails, pathsWhereImportSuccessful);
		assertEquals(expectedSucesses, pathsWhereImportFailed);
		// We should have got an event for every import that started, regardless of the outcome.
		final Set<Path> pathsWhereImportCompleted = Sets.union(pathsWhereImportSuccessful, pathsWhereImportFailed);
		assertEquals(pathsWhereImportCompleted, pathsWhereImportStarted);
		
		watcher.stop();
	}
	
	/**
	 * Supporting nested folders complicates things. Leave it out for this iteration
	 */
//	@Test
//	public void testImportNestedFolders() throws Exception {
//		final ImportFileWatcher watcher = initialiseWatcher();
//		final CountDownLatch latch = new CountDownLatch(1);
//		final Set<Path> importedFiles = Sets.newHashSet();
//		final Set<Path> expectedFiles = Sets.newHashSet();
//		
//		watcher.events()
//		.ofType(ImportSuccessful.class)
//		.subscribe( ev -> {
//				importedFiles.add(ev.getPath());
//				latch.countDown(); 
//			}
//		);
//		
//		watcher.start();
//		
//		Path folder = watcher.getWatchFolder().resolve("Folder1/Folder2/Folder3");
//		Files.createDirectories(folder);
//		
//		Path subFolderTestFile = folder.resolve("test.txt");
//		copyTestFileTo(subFolderTestFile);
//		
//		assertTrue("Should finish in 5s.", latch.await(5, TimeUnit.SECONDS));
//		assertEquals(expectedFiles, importedFiles);
//		
//		watcher.stop();
//	}

}
