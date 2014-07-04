package folderManager;

import java.nio.file.Path;


public interface IFolderData {
	
	public Folder getFolder(Path folder);
	
	public Folder getFolder(Path folder, Boolean createIfAbsent);
	
	public Folder createFolder(Path path);

}
