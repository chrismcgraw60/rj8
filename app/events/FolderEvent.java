package events;

import java.nio.file.Path;

import folderManager.FileSystemEvent;

/**
 * Base class for all Folder Events.
 */
public class FolderEvent extends FileSystemEvent {

	/**
	 * @param folderPath All Folder events are centered around a specific Folder.
	 */
	public FolderEvent(Path folderPath) {
		super(folderPath);
	}

}
