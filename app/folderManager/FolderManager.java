package folderManager;

import importer.events.ImportEvent;
import importer.events.ImportStarted;
import importer.events.ImportSuccessful;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.jna.platform.mac.MacFileUtils.FileManager;

import events.FolderCreated;
import filewatch.ImportFileWatcher;
import folderManager.Folder.Status;

/**
 * Facade service for Folder related functionality that is required to be accessible from the web app 
 * layer.
 */
public class FolderManager {

	private final static Logger logger = LoggerFactory.getLogger(FolderManager.class);
	
	private final ImportFileWatcher fileWatcher;
	private final IFolderData folderData;
	
	/**
	 * @param fileWatcher Monitors file system for incoming folders / files. Must not be null.
	 * @param folderData Access to DB storage. Must not be null.
	 */
	public FolderManager(ImportFileWatcher fileWatcher, IFolderData folderData) {
		
		Preconditions.checkNotNull(fileWatcher, "fileWatcher must not be null");
		Preconditions.checkNotNull(folderData, "folderData must not be null");
		
		this.fileWatcher = fileWatcher;
		this.folderData = folderData;
	}

	/**
	 * The {@link Observable} returned from this method is composed of data from 3 sources.
	 * <ol>
	 * <li>First, the Folder representing the root watch Folder is put onto the stream. The 
	 * protocol is that the watch folder will always be first, so callers need to be aware of
	 * this if they want to know what the root folder is.
	 * <li>Next, known Folders and their states from the DB Storage are put onto the stream. 
	 * These represent the Folder states at the time of invocation (as recorded in the DB).</li>
	 * <li>Subsequently, any changing Folder states detected by the FileWatcher will be added
	 * to the stream as and when they occur. 
	 * </ol>
	 * @return An {@link Observable} that emits {@link Folder} objects, each emitted object
	 * representing either the most up-to-date state of the Folder on the File System.
	 */
	public Observable<Folder> folderEventStream() {
		
		/*
		 * File system events emitted by the FileWatcher. We transform them to Folder objects.
		 */
		Observable<Folder> folderStreamObservable =  
			this.fileWatcher.events().map(this::processEventAndReturnAssociatedFolder);
		
		/*
		 * Before we return the transformed Observable from the File watcher, we prepend the
		 * stream with the root folder state, followed by all folder states in the DB.
		 */
		Folder rootWatchFolder = folderData.getFolder(fileWatcher.getWatchFolder(), true);
		List<Folder> initialFolderStates = Lists.newArrayList(rootWatchFolder);
		initialFolderStates.addAll(folderData.getAllFolders());
		return folderStreamObservable.startWith(initialFolderStates);
	}
	
	/**
	 * Shuts down the {@link FileManager} and any associated services (ie {@link ImportFileWatcher}).
	 */
	public void shutDown() {
		this.fileWatcher.stop();
	}
	
	private Folder processEventAndReturnAssociatedFolder(FileSystemEvent ev) {
		
		Class<? extends FileSystemEvent> eventType = ev.getClass();
		
		Folder srcFolder;
		
		if (eventType.equals(FolderCreated.class)) {
			srcFolder = folderData.getFolder(ev.getPath(), true);
		}
		else if (ImportEvent.class.isAssignableFrom(eventType)) {
			srcFolder = handleImportEvent(ev);
		}
		else {
			logger.warn("Unhandled event: " + ev.toString());
			srcFolder = folderData.getFolder(ev.getPath());
		}
		
		return srcFolder;
	}

	private Folder handleImportEvent(FileSystemEvent ev) {
		final Folder f = folderData.getFolder(ev.getPath().getParent());
				
		if (ev.getClass().equals(ImportStarted.class)) {
			return folderData.updateFolder(f.updateStatus(Status.Importing));
		}
		else if (ev.getClass().equals(ImportSuccessful.class)) {
			return folderData.updateFolder(f.updateStatus(Status.Active));
		}
		else {/* failure */
			return folderData.updateFolder(f.updateStatus(Status.ActiveWithErrors));
		}
	}
}





