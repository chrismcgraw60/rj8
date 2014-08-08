package events;

import importer.events.ImportEvent;

import java.nio.file.Path;

/**
 * Helper class for creating {@link ImportEvent}s.
 */
public class FolderEvents {

	/**
	 * @param path
	 * @return A new {@link FolderCreated} event object, configured with the
	 * given path.
	 */
	public static FolderCreated created(final Path path) {
		final FolderCreated ev = new FolderCreated(path);
		return ev;
	}
	
}
