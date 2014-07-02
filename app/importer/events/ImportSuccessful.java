package importer.events;

import java.nio.file.Path;

import filewatch.ImportFileWatcher;

/**
 * Emitted by {@link ImportFileWatcher} when a File import is successful.
 */
public class ImportSuccessful extends ImportEvent {

	/**
	 * @param path The file that was successfully imported.
	 */
	ImportSuccessful(Path path) {
		super(path);
	}

}
