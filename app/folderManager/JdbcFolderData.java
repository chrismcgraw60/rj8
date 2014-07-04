package folderManager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;

import javax.sql.DataSource;

import org.joda.time.DateTime;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/*
 * CREATE TABLE Folder (
  id			bigint SERIAL not null,
  name			varchar(1024),
  parent_Id		bigint,
  status		varchar(32),
  createdOn		timestamp,
  updatedOn		timestamp,
  
  CONSTRAINT folderPK PRIMARY KEY (id),
  CONSTRAINT parent_folder_integrity CHECK (parent_Id IS NOT NULL),
  CONSTRAINT folderParentRef FOREIGN KEY (parent_Id)
  	REFERENCES Folder (id)
  	ON DELETE CASCADE
);
 */
public class JdbcFolderData implements IFolderData {
	
	private static final String insertFolderSQL = 
		"insert into folder (path, status, createdOn, updatedOn) values (?, ?, ?, ?)";
	
	static final String loadAllFoldersSQL = "select * from folder";
	
	static final String loadFolderSQL = "select * from folder where path = '%s'";

	private final LoadingCache<Path, Optional<Folder>> folderCache;
	private final DataSource ds;
	
	public JdbcFolderData(DataSource ds) {
		this.ds = ds;
		this.folderCache = CacheBuilder.newBuilder().build(new FolderCacheLoader(ds));
	}
	
	@Override
	public Folder getFolder(Path folder) {
		return getFolder(folder, false);
	}
	
	@Override
	public Folder getFolder(Path folderPath, Boolean createIfAbsent) {
		
		final Optional<Folder> folder = folderCache.getUnchecked(folderPath);
		
		return createIfAbsent ? 
			folder.orElse(createFolder(folderPath)) : 
				folder.orElseThrow( () -> noFolderFailure(folderPath) );
	}
	
	@Override
	public Folder createFolder(Path path) {
				
		try ( Connection conn = ds.getConnection();
				PreparedStatement insertFolderStmt = conn.prepareStatement(insertFolderSQL) ){
			
			final DateTime now = DateTime.now();
			final Folder f = new Folder(null, path, Folder.Status.Created, now, now);
			final Timestamp timeStamp = new Timestamp(now.getMillis());
			
			insertFolderStmt.setString		(1, f.getPath().toString());
			insertFolderStmt.setString		(2, f.getStatus().name());
			insertFolderStmt.setTimestamp	(3, timeStamp); //createdOn
			insertFolderStmt.setTimestamp	(4, timeStamp); //updatedOn
			
			/*
			 * TODO: Add uniquness constraint on path and catch concurrent creation exceptions here.
			 */
			int affectedRows = insertFolderStmt.executeUpdate();
			
			if (affectedRows == 0) {
			    throw new RuntimeException("Creating folder row failed, no rows affected.");
			}
			
			folderCache.invalidate(path);
			
			return folderCache.get(path).orElseThrow( this::folderCreationFailure );
			
//			return folderCache.get(path).orElseThrow( this::folderCreationFailure );
		}
		catch(Exception ex) {
			Throwables.propagate(ex);
		}
		
		return null;
	}
	
	RuntimeException folderCreationFailure() {
		return new RuntimeException("Creating folder failed. We were able to create it in the DB but couldn't get it from cache.");
	}
	
	RuntimeException noFolderFailure(Path folderPath) {
		return new RuntimeException("Could not retrieve the requested folder: " + folderPath);
	}
}
