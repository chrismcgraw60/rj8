package filewatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import importer.IBatchImporter;
import importer.ImportSource;
import importer.events.ImportFailed;
import importer.events.ImportStarted;
import importer.events.ImportSuccessful;
import importer.jdbc.BatchJdbcImporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import testdata.TestDataInfo;
import utils.H2DataSource;
import utils.TestProperties;

import com.google.common.base.Throwables;
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
	
	private static final String rootFolderName = TestProperties.INSTANCE.getFileWatchRootFolder();
	
	private static BoneCPDataSource DS = null;
	
	@BeforeClass
	public static void setUp() throws Exception {
		DS = H2DataSource.create();
		H2DataSource.clear(DS);
	}
	
	/**
	 * Covers successful import of a folder sub-tree under the watch folder.
	 * All files in the sub  tree should be successfully imported.
	 */
	@Test
	public void testBulkFileCreation() throws Exception {

		final ImportFileWatcher watcher = initialiseWatcher();
		
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
			bulkCreationCopyTestFilesTo(watcher.getWatchFolder(), SUB_FOLDER_COUNT, TEST_FILE_COUNT);
		
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
		
		final ImportFileWatcher watcher = initialiseWatcher();
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
		
		final Set<Path> expectedFails = copyMalformedTestFileTo(watcher.getWatchFolder());
		final Set<Path> expectedSucesses = bulkCreationCopyTestFilesTo(watcher.getWatchFolder(), 1, 2);
		
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

	/*
	 * Prepares a new ImportFileWatcher instance for testing.
	 */
	private ImportFileWatcher initialiseWatcher() throws IOException {
		final Path watchFolder = initialiseWatchFolder();
		final IBatchImporter importer = new BatchJdbcImporter(DS, 1000);
		final ImportFileWatcher watcher = new ImportFileWatcher(watchFolder, importer);
		return watcher;
	}
	
	/*
	 * Prepares the watch folder for use in a given test.
	 */
	private Path initialiseWatchFolder() throws IOException {
		final Path watchFolder = getWatchFolderPath();	
		Files.list(watchFolder).forEach(fileOrFolderToDelete -> removeRecursive(fileOrFolderToDelete));
		return watchFolder;
	}
	
	/*
	 * Performs a creation of a folder structure under the watch folder root.
	 * Intended to simulate a bulk copy or creation that is expected to be 
	 * successfully imported.
	 * 
	 * Returns a Set of all of the import File paths to be used for verification.
	 */
	private Set<Path> bulkCreationCopyTestFilesTo(Path watchFolderPath, int folderCount, int filesPerFolder) throws IOException {
		final Set<Path> paths = Sets.newHashSet();
		for (int i=1; i<=folderCount; i++) {
			Path subFolder = watchFolderPath.resolve(folderName(i));
			Files.createDirectory(subFolder);
			for (int j=1; j<=filesPerFolder; j++) {
				Path subFolderTestFile = subFolder.resolve(fileName(j));
				copyTestFileTo(subFolderTestFile);
				paths.add(subFolderTestFile);
			}
		}
		return paths;
	}
	
	private Set<Path> copyMalformedTestFileTo(Path watchFolderPath) throws IOException {
		final Set<Path> paths = Sets.newHashSet();
		Path subFolder = watchFolderPath.resolve("containsMalformedFile");
		Files.createDirectory(subFolder);
		Path subFolderTestFile = subFolder.resolve("malformedFile.xml");
		Path testReportFile = Paths.get(TestDataInfo.getMalformedReportFilePath());
		uncheckedFileCopy(subFolderTestFile, testReportFile);
		paths.add(subFolderTestFile);
		return paths;
	}

	/*
	 * Apply template fir test file names.
	 */
	private String fileName(int j) {
		return String.format("test-report-%05d.xml", j);
	}

	/*
	 * Apply template for test folder names.
	 */
	private String folderName(int i) {
		return String.format("Sub%05d", i);
	}

	/*
	 * Re-throw unchecked wrapper for IOException on file traversal
	 */
	private void removeRecursive(Path path) {
	    try {
			Files.walkFileTree(path, new Deleter());
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}
	
	/*
	 * Copy the test file template to a given target path.
	 * We use this copy to simulate creation of a new test report file. 
	 */
	private void copyTestFileTo(Path targetPath) {
		ImportSource is = TestDataInfo.getImportSource();
		
		is.computePaths().findFirst().ifPresent(s -> {
			Path testReportFile = Paths.get(s);
			uncheckedFileCopy(targetPath, testReportFile);
		});
	}

	/*
	 * Re-throw unchecked wrapper for IOException on file copy
	 */
	private void uncheckedFileCopy(Path targetPath, Path testReportFile) {
		try { Files.copy(testReportFile, targetPath); } 
		catch (IOException e) { Throwables.propagate(e); }
	}
	
	/*
	 * Compute and return the watch folder path to be used in the test.
	 */
	private Path getWatchFolderPath() {
		URL workingFolderUrl = ImportFileWatcher.class.getResource("../.");
		File workingFolder = new File(workingFolderUrl.getFile()).getParentFile().getAbsoluteFile();
		File targetFolder = new File(workingFolder.getAbsolutePath() + File.separatorChar + rootFolderName);
		
		if (!targetFolder.exists()) {  targetFolder.mkdir();  }
		
		return targetFolder.toPath();
	}
	
	/**
	 * Helper class to Delete contents of the Folder Tree under test.
	 */
	private static class Deleter extends SimpleFileVisitor<Path> {
		/* (non-Javadoc)
		 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
		 */
		@Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /* (non-Javadoc)
         * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
         */
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            /* Try to delete the file anyway, even if its attributes could not be read, since 
        	 * delete-only access is theoretically possible.
        	 */
        	try { Files.delete(file); }
        	catch(NoSuchFileException ex) { /* Ignore and continue. */ }
            return FileVisitResult.CONTINUE;
        }

        /* (non-Javadoc)
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ioex) throws IOException {
            if (ioex == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
            else { /* Directory iteration failed. Propagate exception. */ throw ioex; }
        }
	}

	
}
