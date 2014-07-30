package folderManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.jooq.lambda.SQL;
import org.jooq.lambda.Unchecked;

import query.JdbcQueryService;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader;

/**
 * Extends {@link CacheLoader} to provide an RDBMS backed loader for the in-memory Folder cache. 
 */
class FolderCacheLoader extends CacheLoader<Path, Optional<Folder>> {
	
	private final DataSource ds;
	
	/**
	 * @param ds Provides access to the RDBMS.
	 */
	public FolderCacheLoader(DataSource ds) {
		Preconditions.checkNotNull(ds, "ds must not be null.");
		
		this.ds = ds;		
	}		
	
	/**
	 * Loads a Folder POJO representing the given {@link Path}.
	 * The Folder is loaded from information in the {@link DataSource}.
	 * 
	 * @return A Folder wrapped as {@link Optional} or {@link Optional#empty()} if there is no
	 * Folder data corresponding to the path in the database.
	 */
	public Optional<Folder> load(Path path) {
		Preconditions.checkNotNull(path, "path must not be null.");
		
		String query = String.format(JdbcFolderData.loadFolderSQL, path.toString());
		
		ResultSet rs = null;
		Optional<Folder> folderOpt = Optional.empty();
				
		try (Connection conn = ds.getConnection(); 
				PreparedStatement q = conn.prepareStatement(query)){
			
			rs = q.executeQuery();
			
			if (hasRows(rs)) { 
				Folder loadedFolder = fromRow(rs);
				folderOpt = Optional.of(loadedFolder); 
			}
			
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		
		return folderOpt;
		
	}
	
	/**
	 * NOTE: This override ignores the given paths and bulk loads all Folder data into the cache.
	 * 
	 * @param keys Ignored.
	 * 
	 * @see com.google.common.cache.CacheLoader#loadAll(java.lang.Iterable)
	 */
	@Override
	public Map<Path, Optional<Folder>> loadAll(Iterable<? extends Path> keys) {
		return loadAll();
	}

	/**
	 * @return All Folders in storage mapped by Path.
	 */
	public Map<Path, Optional<Folder>> loadAll() {
		final ResultSet rs = new JdbcQueryService(this.ds).runQuery(JdbcFolderData.loadAllFoldersSQL);
		
		Map<Path, Optional<Folder>> loadedMap = 
			SQL.stream(rs, Unchecked.function(row -> { return fromRow(row); }))
				.collect(Collectors.toMap(Folder::getPath , f -> Optional.of(f)));
		
		return loadedMap;
	}
	
	/*
	 * Returns true if the given ResultSet contains 1 or more rows. False otherwise.
	 */
	private boolean hasRows(final ResultSet rs) {
		boolean hasRows = false;
		
		try { hasRows = rs.first(); } 
		catch (SQLException e) { Throwables.propagate(e); }
		
		return hasRows; 
		
	}
	
	/*
	 * Populates a Folder POJO from a database row.
	 */
	private Folder fromRow(ResultSet row) {
		
		Folder f = null;
		
		try {
			Long id = row.getLong("id");
			Path path = Paths.get(row.getString("path"));
			Folder.Status status = Folder.Status.valueOf(row.getString("status"));
			DateTime created = new DateTime(row.getTimestamp("createdOn").getTime());
			DateTime updated = new DateTime(row.getTimestamp("updatedOn").getTime());
			f = new Folder(id, path, status, created, updated);
		}
		catch(SQLException sqlEx) {
			Throwables.propagate(sqlEx);
		}
		
		return f;
	}
}