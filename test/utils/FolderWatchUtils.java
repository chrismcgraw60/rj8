package utils;

import importer.IBatchImporter;
import importer.ImportSource;
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

import testdata.TestDataInfo;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.jolbox.bonecp.BoneCPDataSource;

import filewatch.ImportFileWatcher;
import folderManager.IFolderData;
import folderManager.JdbcFolderData;

/**
 * Utility class for manipulating the Watch Folder Tree content in tests.
 */
public class FolderWatchUtils {
	
	private static final String rootFolderName = TestProperties.INSTANCE.getFileWatchRootFolder();
	private BoneCPDataSource DS = null;
	
	/**
	 * @param DS Used for DB interaction.
	 */
	public FolderWatchUtils(BoneCPDataSource DS) {
		this.DS = DS;
	}

	/**
	 * @return A new ImportFileWatcher instance for testing.
	 * @throws IOException
	 */
	public ImportFileWatcher initialiseWatcher() throws IOException {
		final Path watchFolder = initialiseWatchFolder();
		final IFolderData fd = new JdbcFolderData(DS);
		final IBatchImporter importer = new BatchJdbcImporter(DS, fd, 1000);
		final ImportFileWatcher watcher = new ImportFileWatcher(watchFolder, importer);
		return watcher;
	}
	
	/**
	 * @param importer {@link IBatchImporter} to be used for initialization.
	 * @return A new ImportFileWatcher instance for testing.
	 * @throws IOException
	 */
	public ImportFileWatcher initialiseWatcher(IBatchImporter importer) throws IOException {
		final Path watchFolder = initialiseWatchFolder();
		final ImportFileWatcher watcher = new ImportFileWatcher(watchFolder, importer);
		return watcher;
	}
	
	/**
	 * Performs a creation of a folder structure under the watch folder root.
	 * Intended to simulate a bulk copy or creation that is expected to be 
	 * successfully imported.
	 * 
	 * @param watchFolderPath
	 * @param folderStartNum
	 * @param folderCount
	 * @param filesPerFolder
	 * @return A Set of all of the import File paths to be used for verification.
	 * @throws IOException
	 */
	public Set<Path> bulkCreationCopyTestFilesTo(Path watchFolderPath, int folderStartNum, int folderCount, int filesPerFolder) throws IOException {
		final Set<Path> paths = Sets.newHashSet();
		for (int i=folderStartNum; i<=folderCount; i++) {
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
	
	/**
	 * Creates a malformed file under the watch folder to allow testing of error handling.
	 * @param watchFolderPath
	 * @return A Set of all of the import File paths to be used for verification.
	 * @throws IOException
	 */
	public Set<Path> copyMalformedTestFileTo(Path watchFolderPath) throws IOException {
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
	 * Prepares the watch folder for use in a given test.
	 */
	private Path initialiseWatchFolder() throws IOException {
		final Path watchFolder = getWatchFolderPath();	
		Files.list(watchFolder).forEach(fileOrFolderToDelete -> removeRecursive(fileOrFolderToDelete));
		return watchFolder;
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
