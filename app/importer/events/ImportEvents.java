package importer.events;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

/**
 * Helper class for creating {@link ImportEvent}s.
 */
public class ImportEvents {

	/**
	 * @param path
	 * @return A new {@link ImportStarted} event object, configured with the
	 * given path.
	 */
	public static ImportStarted started(final Path path) {
		final ImportStarted ev = new ImportStarted(path);
		return ev;
	}
	
	/**
	 * @param path
	 * @return A new {@link ImportSuccessful} event object, configured with the
	 * given path.
	 */
	public static ImportSuccessful successful(final Path path) {
		final ImportSuccessful ev = new ImportSuccessful(path);
		return ev;
	}
	
	/**
	 * @param path
	 * @return A new {@link ImportFailed} event object, configured with the
	 * given path.
	 */
	public static ImportFailed failed(final Path path, final Kind<Path> eventType) {
		final ImportFailed ev = new ImportFailed(path, eventType);
		return ev;
	}
}
