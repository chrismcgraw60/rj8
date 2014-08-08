package events;

import java.nio.file.Path;

import filewatch.ImportFileWatcher;

/**
 * Emitted by {@link ImportFileWatcher} when a File import is started.
 */
public class FolderCreated extends FolderEvent {

	/**
	 * @param path The newly created Folder.
	 */
	public FolderCreated(Path path) {
		super(path);
	}
}
