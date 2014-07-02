package folderManager;

import java.nio.file.Path;

/**
 * Base class for all events that are emitted from the Folder Manager sub-system.
 */
public class FileSystemEvent {
	
	private final Path path;
	
	/**
	 * @param path All File System events are centered around a File System Path 
	 * on which something interesting happened.
	 */
	public FileSystemEvent (Path path) {
		this.path = path;
	}

	/**
	 * @return The File System Path associated with the event.
	 */
	public Path getPath() {
		return path;
	}
}
