import importer.IBatchImporter;
import importer.jdbc.BatchJdbcImporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.sql.DataSource;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.db.DB;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import filewatch.ImportFileWatcher;
import folderManager.FolderManager;
import folderManager.IFolderData;
import folderManager.JdbcFolderData;

public class Global extends GlobalSettings {

	private Injector injector;
	private FolderManager folderManager;
	
	@Override
	public void onStart(Application application) {
		
		final ImportFileWatcher fileWatcher = initialiseFolderWatcher();
		final IFolderData folderData = new JdbcFolderData(DB.getDataSource());
		folderManager = new FolderManager(fileWatcher, folderData);
		
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(FolderManager.class).toInstance(folderManager);
			}
		});
	}
	
	@Override
	public void onStop(Application app) {
		super.onStop(app);
		folderManager.shutDown();
	}

	@Override
	public <T> T getControllerInstance(Class<T> aClass) throws Exception {
		return injector.getInstance(aClass);
	}
	
	/*
	 * Initialize and start the background file watcher.
	 * If we can't start the file watcher due to any Exceptions then we re-throw.
	 * This will prevent the entire application from starting which I think is what we want.
	 */	
	private ImportFileWatcher initialiseFolderWatcher() {
		final Path watchFolder = getWatchFolderPath();
		final DataSource ds =  DB.getDataSource();
		final IFolderData fd = new JdbcFolderData(ds);
		final IBatchImporter importer = new BatchJdbcImporter(ds, fd, 1000);
		final ImportFileWatcher watcher = new ImportFileWatcher(watchFolder, importer);

		try { watcher.start(); } 
		catch (IOException e) { Throwables.propagate(e); }
		
		return watcher;
	}
	
	private Path getWatchFolderPath() {
		File targetFolder = new File(Play.application().configuration().getString("watchFolder"));
		
		if (!targetFolder.exists()) {  targetFolder.mkdir();  }
		
		return targetFolder.toPath();
	}
}
