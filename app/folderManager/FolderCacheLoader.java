package folderManager;

import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader;

/**
 *
 */
class FolderCacheLoader extends CacheLoader<Path, Optional<Folder>> {
	
	private final DataSource ds;
	
	public FolderCacheLoader(DataSource ds) {
		this.ds = ds;		
	}		
	
	@Override
	public Optional<Folder> load(Path path) {
		
		String query = String.format(JdbcFolderData.loadFolderSQL, path.toString());
		final ResultSet rs = new JdbcQueryService(this.ds).runQuery(query);
		
		if (!hasRows(rs)) { 
			return Optional.empty(); 
		}
		else { 
			Folder loadedFolder = fromRow(rs);
			return Optional.of(loadedFolder); 
		}
	}

	private boolean hasRows(final ResultSet rs) {
		boolean hasRows = false;
		
		try { hasRows = rs.first(); } 
		catch (SQLException e) { Throwables.propagate(e); }
		
		return hasRows; 
		
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
		
		final ResultSet rs = new JdbcQueryService(this.ds).runQuery(JdbcFolderData.loadAllFoldersSQL);
		
		Map<Path, Optional<Folder>> loadedMap = 
			SQL.stream(rs, Unchecked.function(row -> { return fromRow(row); }))
				.collect(Collectors.toMap(Folder::getPath , f -> Optional.of(f)));
		
		return loadedMap;
	}
	
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