package importer.events;

import java.nio.file.Path;

import filewatch.ImportFileWatcher;

/**
 * Emitted by {@link ImportFileWatcher} when a File import is started.
 */
public class ImportStarted extends ImportEvent {

	/**
	 * @param path The file being imported.
	 */
	public ImportStarted(Path path) {
		super(path);
	}
}
