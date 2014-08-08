package folderManager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableCollection;

/**
 * Implementation of {@link IFolderData} that uses a JDBC data source for Folder
 * storage and retrieval.
 * 
 * {@link JdbcFolderData} also maintains an in-memory cache of Folder data using a 
 * {@link LoadingCache}.
 */
public class JdbcFolderData implements IFolderData {
	
	/*
	 * Error code indicating that uniqueness constraint has been broken.
	 * We check for this when we get an SQL Exception after attempting insertion of
	 * a new folder row (path should be unique).
	 * TODO: Need to make sure this code is the thing we should be checking.
	 */
	private static final String uniqueConstraintErrorCode = "23505";
	
	/*
	 * Folder Insertion SQL
	 */
	private static final String insertFolderSQL = 
		"insert into folder (path, status, createdOn, updatedOn) values (?, ?, ?, ?)";
	
	private static final String updateFolderSQL = 
			"update folder set path=?, status=?, updatedOn=? where id=?";
	
	/*
	 * Select all Folders SQL
	 */
	static final String loadAllFoldersSQL = "select * from folder";
	
	/*
	 * Select Folder by Path SQL
	 */
	static public final String loadFolderSQL = "select * from folder where path = '%s'";

	/*
	 * In-memory Folder cache.
	 */
	private final LoadingCache<Path, Optional<Folder>> folderCache;
	
	/*
	 * CacheLoader for LoadingCache. We hold an explicit reference as we
	 * sometimes want to use its functionality directly (i.e for directly
	 * loading all Folders from DB without knowing the paths).
	 */
	private final FolderCacheLoader folderDataLoader;
	
	/*
	 * JDBC data source for Folder storage
	 */
	private final DataSource ds;
	
	/**
	 * @param ds JDBC data source for persistent storage.
	 */
	public JdbcFolderData(DataSource ds) {
		Preconditions.checkNotNull(ds, "ds must not be null.");
		
		this.ds = ds;
		this.folderDataLoader = new FolderCacheLoader(ds);
		this.folderCache = CacheBuilder.newBuilder().build(this.folderDataLoader);
	}
	
	@Override
	public Folder getFolder(Path folder) {
		return getFolder(folder, false);
	}
	
	@Override
	public List<Folder> getAllFolders() {
		return allFoldersFromCache()
			.stream()
			.filter(folderOpt -> folderOpt.isPresent())
			.map(folderOpt -> folderOpt.get())
			.collect(Collectors.toList());
	}
	
	@Override
	public Folder getFolder(Path folderPath, Boolean createIfAbsent) {
		Preconditions.checkNotNull(folderPath, "folderPath must not be null.");
		Preconditions.checkNotNull(createIfAbsent, "createIfAbsent must not be null.");
		
		final Optional<Folder> folder = folderCache.getUnchecked(folderPath);
		
		return createIfAbsent ? 
			folder.orElse(createFolder(folderPath)) : 
				folder.orElseThrow( () -> noFolderFailure(folderPath) );
	}
	
	
	@Override
	public Folder createFolder(Path path) {
		Preconditions.checkNotNull(path, "path must not be null.");
				
		/*
		 * Attempt INSERT of folder data. 
		 */
		try ( Connection conn = ds.getConnection();
				PreparedStatement insertFolderStmt = conn.prepareStatement(insertFolderSQL) ){
			
			final DateTime now = DateTime.now();
			final Folder f = new Folder(null, path, Folder.Status.Active, now, now);
			final Timestamp timeStamp = new Timestamp(now.getMillis());
			
			insertFolderStmt.setString		(1, f.getPath().toString());
			insertFolderStmt.setString		(2, f.getStatus().name());
			insertFolderStmt.setTimestamp	(3, timeStamp); //createdOn
			insertFolderStmt.setTimestamp	(4, timeStamp); //updatedOn
			
			int affectedRows = insertFolderStmt.executeUpdate();
			
			if (affectedRows == 0) throw new RuntimeException("Creating folder row failed, no rows affected.");
		}
		catch (SQLException sqlEx) {
			if (sqlEx.getSQLState().equals(uniqueConstraintErrorCode)) { 
				/* 
				 * A Row with the given path already exists so the INSERT has failed the DB uniqueness constraint on "path".
				 * This is OK. We'll continue on to use the existing stored folder data for the Path instead.
				 */
			}
		}
		/*
		 * We may have an Empty cache value for the given path. In that case we fall back to
		 * refreshing the cache before returning the cache value.
		 */
		return folderCache.getUnchecked(path).orElseGet(() -> refreshCacheAndRetrieve(path));
	}
	
	/* (non-Javadoc)
	 * @see folderManager.IFolderData#updateFolder(folderManager.Folder)
	 */
	@Override
	public Folder updateFolder(Folder folder) {
		Preconditions.checkNotNull(folder, "folder must not be null.");
		
		final DateTime now = DateTime.now();
		final Timestamp timeStamp = new Timestamp(now.getMillis());
		
		/*
		 * Attempt UPDATE of folder data. 
		 */
		try ( Connection conn = ds.getConnection();
				PreparedStatement updateFolderStmt = conn.prepareStatement(updateFolderSQL) ){
			
			updateFolderStmt.setString		(1, folder.getPath().toString());
			updateFolderStmt.setString		(2, folder.getStatus().name());
			updateFolderStmt.setTimestamp	(3, timeStamp);
			updateFolderStmt.setLong		(4, folder.getId());
			
			int affectedRows = updateFolderStmt.executeUpdate();
			
			if (affectedRows == 0) throw new RuntimeException("Updating folder row failed, no rows affected.");
			
			folderCache.invalidate(folder.getPath());
		}
		catch (Exception ex) {
			Throwables.propagate(ex);
		}
		
		return folder.updateTimestamp(now);
	}

	/*
	 * Refresh and return cache value. If we don't have a value in the Optional after
	 * refresh, then we throw an Exception.
	 * NOTE: Synchronization is required here to prevent concurrent threads reading the Empty
	 * value for the path while the cache is in the process of refreshing the path from storage.
	 */
	private synchronized Folder refreshCacheAndRetrieve(Path path) {
		folderCache.refresh(path); 
		return folderCache.getUnchecked(path).orElseThrow(this::folderCreationFailure);
	}
	
	/*
	 * Helper to take checked ExecutionException handing out of main flow. 
	 */
	private ImmutableCollection<Optional<Folder>> allFoldersFromCache() {
		final Map<Path, Optional<Folder>> loadedFolders = folderDataLoader.loadAll();
		folderCache.putAll(loadedFolders);
		
		try { return folderCache.getAll(loadedFolders.keySet()).values(); } 
		catch (ExecutionException e) { Throwables.propagate(e); }
		
		return null;
	}
	
	private RuntimeException folderCreationFailure() {
		return new RuntimeException("Creating folder failed. We were able to create it in the DB but couldn't get it from cache.");
	}
	
	private RuntimeException noFolderFailure(Path folderPath) {
		return new RuntimeException("Could not retrieve the requested folder: " + folderPath);
	}
}
