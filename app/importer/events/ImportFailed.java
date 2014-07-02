package importer.events;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

/**
 * Emitted when a file import fails.
 */
public class ImportFailed extends ImportEvent {

	private final Kind<Path> eventType;
	
	/**
	 * @param path The file for which the import failed.
	 * @param eventType The event that failed
	 */
	public ImportFailed(Path path, final Kind<Path> eventType) {
		super(path);
		
		this.eventType = eventType;
	}

	public Kind<Path> getEventType() {
		return eventType;
	}

}
