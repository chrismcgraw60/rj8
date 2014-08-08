package folderManager;

import java.nio.file.Path;
import java.util.List;

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
	 * @return A {@link List} of {@link Folder}s representing the app's tracked folders.
	 */
	public List<Folder> getAllFolders();
	
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
	
	/**
	 * Updates a given {@link Folder} in storage. The Folder's 'updated' time stamp
	 * field is updated to reflect the time of the update.
	 * @param folder The Folder to be updated.
	 * @return The Folder with up-to-date 'updated' time stamp field.
	 * @see {@link Folder#getUpdated()}
	 */
	public Folder updateFolder(Folder folder);

}
