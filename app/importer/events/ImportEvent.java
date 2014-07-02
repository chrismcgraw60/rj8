package importer.events;

import java.nio.file.Path;

import folderManager.FileSystemEvent;

/**
 * Base class for all Import Events.
 */
public class ImportEvent extends FileSystemEvent {

	/**
	 * @param srcFilePath All Import events are centered around the source file 
	 * that was imported.
	 */
	public ImportEvent(Path srcFilePath) {
		super(srcFilePath);
	}

}
