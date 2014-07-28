package folderManager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import javax.sql.DataSource;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

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
	 * JDBC data source for Folder storage
	 */
	private final DataSource ds;
	
	/**
	 * @param ds JDBC data source for persistent storage.
	 */
	public JdbcFolderData(DataSource ds) {
		Preconditions.checkNotNull(ds, "ds must not be null.");
		
		this.ds = ds;
		this.folderCache = CacheBuilder.newBuilder().build(new FolderCacheLoader(ds));
	}
	
	@Override
	public Folder getFolder(Path folder) {
		return getFolder(folder, false);
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
	public synchronized Folder createFolder(Path path) {
		Preconditions.checkNotNull(path, "path must not be null.");
				
		boolean alreadyExistsInDB = false;
		
		/*
		 * Attempt INSERT of folder data. 
		 */
		try ( Connection conn = ds.getConnection();
				PreparedStatement insertFolderStmt = conn.prepareStatement(insertFolderSQL) ){
			
			final DateTime now = DateTime.now();
			final Folder f = new Folder(null, path, Folder.Status.Created, now, now);
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
				 * This is OK. We'll use the existing stored folder data for the Path instead.
				 */
				alreadyExistsInDB = true;
			}
		}

		if (!alreadyExistsInDB) {
			/*
			 * The path wasn't represented in storage so we know we have just created it and now need to load 
			 * it into cache for subsequent reads. However, the Cache may have previously loaded an Optional.Empty 
			 * for the required folder so we need to flush that out or the real data won't be loaded.
			 */
			folderCache.invalidate(path);
		}
		
		try {
			/*
			 * Load in the value that we know to be present in storage.
			 */
			return folderCache.get(path).orElseThrow( this::folderCreationFailure );
		}
		catch(Exception ex) {
			Throwables.propagate(ex);
		}
		
		/*
		 * Should not be possible to reach here.
		 */
		throw new RuntimeException();
	}
	
	private RuntimeException folderCreationFailure() {
		return new RuntimeException("Creating folder failed. We were able to create it in the DB but couldn't get it from cache.");
	}
	
	private RuntimeException noFolderFailure(Path folderPath) {
		return new RuntimeException("Could not retrieve the requested folder: " + folderPath);
	}
}
