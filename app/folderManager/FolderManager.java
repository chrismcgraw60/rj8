package folderManager;

import java.util.stream.Stream;

import filewatch.ImportFileWatcher;

/**
 * Facade service for Folder related functionality that is required to be accessible from the web app 
 * layer.
 */
public class FolderManager {

	private final ImportFileWatcher fileWatcher;
	private final IFolderData folderData;
	
	public FolderManager(ImportFileWatcher fileWatcher, IFolderData folderData) {
		this.fileWatcher = fileWatcher;
		this.folderData = folderData;
	}
	
	public Stream<Folder> folders() {
		return folderData.getAllFolders();
	}
	
	public void shutDown() {
		this.fileWatcher.stop();
	}
}
