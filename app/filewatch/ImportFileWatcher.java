package filewatch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import folderManager.FileSystemEvent;
import importer.IBatchImporter;
import importer.ReportParser;
import importer.ReportedTestElement;
import importer.events.ImportEvents;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Monitors a defined folder and its sub-tree for changes that would trigger changes in
 * the data store:
 * <ul>
 * <li> Creation of a new Test Report file will trigger a batch import of the file's content into the data store.
 * <li> Deletion of a Test Report will flag the corresponding data in the data store as having no corresponding report.
 * <li> Modification of a Test Report will trigger a deletion of existing data corresponding to the file and a 
 * fresh batch import.
 * </ul>
 * 
 * TODO: This is work in progress. The only currently implemented function is triggering an import when a new file is 
 * created in the watch folder. Future work will evolve the other functions as we need better data management over the 
 * DB and probably another table to track each folder and its status. 
 */
public class ImportFileWatcher {

	private final static Logger logger = LoggerFactory.getLogger(ImportFileWatcher.class);
	private final static String WATCH_THREAD_ID = "file-watch-worker-%d";
		
	private final IBatchImporter importer;
	private final Path rootFolder;
	
	private final ListeningExecutorService watcherThreadExec = initialiseWatcherThreadExecutor();
	private final WatchService watchService  = initializeWatchService();
	private final Map<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();
	private final List<Subscriber<? super FileSystemEvent>> subscribers = Lists.newCopyOnWriteArrayList();	
	
	/**
	 * Creates a new ImportFileWatcher instance.
	 * @param rootFolder The root folder that will be watched. All sub folders and their contents will 
	 * be monitored.
	 * @param importer Used to import new files into the data store.
	 */
	public ImportFileWatcher(final Path rootFolder, final IBatchImporter importer) {
		Preconditions.checkNotNull(rootFolder, "rootFolder must not be null.");
		Preconditions.checkNotNull(importer, "importer");
		
		this.rootFolder = rootFolder;
		this.importer = importer;
	}
	
	/**
	 * @return An {@link Observable} that can be used to watch and process events that
	 * are emitted by this ImportFileWatcher object.
	 */
	public Observable<FileSystemEvent> events() {
		return Observable.create( 
				new OnSubscribe<FileSystemEvent>() {
					@Override
					public void call(Subscriber<? super FileSystemEvent> subs) {
						subscribers.add(subs);
					}
				});
	}
	
	/**
	 * Start the Watcher and any associated worker threads.. 
	 * @throws IOException
	 */
	public void start() throws IOException {
		logger.info("Starting Import File Watcher...");
		
		initialiseRootFolder();
		
		final ListenableFuture<?> watcherResult = watcherThreadExec.submit(watcherTask());
		
		/*
		 * We have a custom callback to make sure any Exceptions thrown in the
		 * watcher thread are communicated.
		 * We don't expect a success as the thread is expected to run until the Executor 
		 * shuts down.
		 */
		Futures.addCallback(watcherResult, new ThrowOnFutureFailureLogOnReturn());
	}

	/**
	 * Shut down the watcher and any associated worker Threads.
	 */
	public void stop(){
		logger.info("Stopping Import File Watcher...");
		
		watcherThreadExec.shutdown();
		closeWatchService();
		
		logger.info("Stopped Import File Watcher.");
	}
	
	/**
	 * @return The root Folder for this ImportFileWatcher object. All sub folders and files under
	 * this location will be monitored.
	 */
	public Path getWatchFolder() {
		return this.rootFolder;
	}
	
	/*
	 * Construct the task that will run the watcher thread logic.
	 * This will be submitted to the Executor to run as the watcher thread.
	 */
	private Callable<Object> watcherTask() {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				registerPathWithFileWatcher(rootFolder);
				logger.info("Started Import File Watcher. Root Folder: " + rootFolder);
				while(true) {
					try {
						processWatchKey(watchService.take());
					}
					catch(ClosedWatchServiceException ex) {
						logger.info("ClosedWatchServiceException in Watch Loop - Must have still been attempting to take() after FileWatcher was closed. Returning from Watch Loop.");
						return null;
					}
				}
			}
		};
	};
	
	private void processWatchKey(final WatchKey key) {
		key.pollEvents()
			.stream()
			.map(ev -> castToPathEvent(ev))
			.forEach(pathEvent -> processEvent(pathEvent, key));
	}
	
	private void processEvent(final WatchEvent<Path> event, final WatchKey key) {
								
		final Path path = watchKeys.get(key);
		final Kind<Path> eventType = event.kind();
		final Path srcFileOrFolder = path.resolve(event.context());
		try {
			if (eventType == ENTRY_CREATE) { handleCreated(srcFileOrFolder); }
			else if (eventType == ENTRY_MODIFY) { /* TODO */ logger.debug("Processing Modified Folder: " + srcFileOrFolder); }
			else if (eventType == ENTRY_DELETE) { /* TODO */ logger.debug("Deleted Folder: " + srcFileOrFolder); }
			else { unhandledWatchEvent(eventType, srcFileOrFolder); }
		}
		catch(Exception ex) {
			String errMsg = String.format("Error processing event: %s. \nMonitored Path: %s. \nEvent Source: %s.", eventType, path, srcFileOrFolder);
			logger.error(errMsg, ex);
			subscribers.forEach(s -> s.onNext(ImportEvents.failed(srcFileOrFolder, eventType)));
		}
		finally {
			if (!key.reset()) { watchKeys.remove(key); }
		}
	}
	
	/*
	 * Provide unchecked wrapper for register.
	 * Store newly created watch key for easy access to its associated Path.
	 */
	private void registerPathWithFileWatcher(final Path toWatch) {
		final WatchKey key;
		try {
			key = toWatch.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
			watchKeys.put(key, toWatch);
		} catch (IOException e) { Throwables.propagate(e); } 
	}
	
	/*
	 * Create Single ThreadExecutor to manage the Watch Thread.
	 */
	private ListeningExecutorService initialiseWatcherThreadExecutor() {
		final ThreadFactory watchThreads = new ThreadFactoryBuilder().setNameFormat(WATCH_THREAD_ID).build();
		return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(watchThreads));
	}
	
	/*
	 * Unchecked wrapper for WatchService initialization.
	 */
	private WatchService initializeWatchService() {
		WatchService ws = null;
		
		try { ws = FileSystems.getDefault().newWatchService(); }
		catch(Exception ioex) { Throwables.propagate(ioex); }
		
		return ws;
	}
	
	private void initialiseRootFolder() {
		final File watchRoot = this.rootFolder.toFile();
		if (!watchRoot.exists()) watchRoot.mkdir();
	}

	private void unhandledWatchEvent(final Kind<Path> eventType, final Path srcFileOrFolder) {
		logger.warn("Unhandled event: [" + eventType.toString() + "] on path: " + srcFileOrFolder);
	}

	private void unhandledCreation(final Path srcFileOrFolder) {
		logger.warn("Unhandled Creation event for " + srcFileOrFolder);
	}
	
	private void handleCreated(final Path srcFileOrFolder) {
		if (Files.isDirectory(srcFileOrFolder)) { handleCreatedFolder(srcFileOrFolder); }
		else if (Files.isRegularFile(srcFileOrFolder)) { handleCreatedFile(srcFileOrFolder); }
		else { unhandledCreation(srcFileOrFolder); }
	}

	private void handleCreatedFile(final Path file) {
		logger.debug("Detected new File: " + file);
		/*
		 * TODO: File system event for file creation
		 */
		importFromFile(file);
	}

	private void handleCreatedFolder(final Path folder) {
		logger.debug("New Folder Added to Import tree: " + folder);
		/*
		 * TODO: File system event for file creation
		 */
		registerPathWithFileWatcher(folder);
		traverseFolder(folder).forEach(fileInFolder -> importFromFile(fileInFolder));
	}
	
	private void importFromFile(final Path filePath) {
		/*
		 * We may get a folder passed in as a result of recursive folder traversal.
		 * Don't try to parse it. 
		 */
		if (Files.isDirectory(filePath)) return;
		
		subscribers.forEach(s -> s.onNext(ImportEvents.started(filePath)));
		final Stream<ReportedTestElement> testCaseEntries = new ReportParser().parse(filePath);
		final int importedEntryCount = importer.doImport(testCaseEntries);
		logger.debug("Imported " + importedEntryCount + " entries from file: " + filePath);
		subscribers.forEach(s -> s.onNext(ImportEvents.successful(filePath)));
	}
	
	/*
	 * Unchecked wrapper for recursive folder traversal.
	 */
	private Stream<Path> traverseFolder(final Path folderPath) {
		try { return Files.walk(folderPath); } 
		catch (IOException e) { Throwables.propagate(e); }
		
		return null;
	}
	
	/*
	 * Close watch service but don't re-throw any Exceptions. Just log them and let shut down proceed.  
	 */
	private void closeWatchService() {
		try { watchService.close(); } 
		catch (IOException e) { logger.error("Error while stopping WatchService: ", e); }
	}
	
	@SuppressWarnings("unchecked")
	private static WatchEvent<Path> castToPathEvent(WatchEvent<?> event) {
		return (WatchEvent<Path>) event;
	}
	
	/**
	 * Future callback that re-throws any exceptions. Currently, the only time we will 
	 * be notified of success is when the loop has returned a null due to a ClosedWatchServiceException. 
	 * When this happens we log a message.
	 */
	private class ThrowOnFutureFailureLogOnReturn implements FutureCallback<Object> {

		@Override
		public void onFailure(Throwable th) {
			logger.error("Future callback - Unhandled error, stopping ImportFileWatcher.", th);
			stop();
		}

		@Override
		public void onSuccess(Object arg0) { 
			logger.info("Future callback - File Watcher was closed");
		}
		
	}
}
