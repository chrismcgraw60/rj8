package folderManager;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Defines API for persistent Folder data.
 */
public interface IFolderData {
	
	/**
	 * Get a Folder object from storage that represents a given File System path. 
	 * @param folder The path for the required {@link Folder}.
	 * @return The {@link Folder} that represents the given {@link Path}.
	 */
	public Folder getFolder(Path folder);
	
	/**
	 * Get all tracked Folders from the application's watch folder. 
	 * @return A {@link Stream} of {@link Folder}s representing the app's tracked folders.
	 */
	public Stream<Folder> getAllFolders();
	
	/**
	 * Get a Folder object from storage that represents a given File System path and optionally create the 
	 * Folder data in storage if it doesn't exist.
	 * @param folder The path for the required {@link Folder}.
	 * @param createIfAbsent Indicates that Folder data should be created for the given path if it doesn't already
	 * exist.
	 * @return The {@link Folder} that represents the given {@link Path}.
	 */
	public Folder getFolder(Path folder, Boolean createIfAbsent);
	
	/**
	 * Creates a Folder in storage that represents the given path. 
	 * @param path The {@link Path} of the Folder to be created.
	 * @return {@link Folder} object that represents the created Folder.
	 */
	public Folder createFolder(Path path);

}
