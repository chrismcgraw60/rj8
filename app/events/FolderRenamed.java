package events;

import java.nio.file.Path;

import filewatch.ImportFileWatcher;

/**
 * Emitted by {@link ImportFileWatcher} when a Folder is deleted.
 */
public class FolderRenamed extends FolderEvent {

	/**
	 * @param path The deleted Folder.
	 */
	public FolderRenamed(Path path) {
		super(path);
	}
}
