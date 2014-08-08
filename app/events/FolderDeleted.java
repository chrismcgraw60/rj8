package events;

import java.nio.file.Path;

import filewatch.ImportFileWatcher;

/**
 * Emitted by {@link ImportFileWatcher} when a Folder is deleted.
 */
public class FolderDeleted extends FolderEvent {

	/**
	 * @param path The deleted Folder.
	 */
	public FolderDeleted(Path path) {
		super(path);
	}
}
