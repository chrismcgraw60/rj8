package query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.google.common.base.Throwables;

/**
 * Simple class that wraps the JDBC boiler plate for running a SELECT query and 
 * handling any checked Exceptions.
 */
public class JdbcQueryService {
	
	private final DataSource ds;
	
	/**
	 * @param ds The {@link DataSource} to be used in any queries.
	 */
	public JdbcQueryService(DataSource ds) {
		this.ds = ds;
	}

	/**
	 * Runs a query against this instance's configured data source.
	 * @param sql The SQL query to be run.
	 * @return {@link ResultSet} containing the query results. 
	 */
	public ResultSet runQuery(String sql) {
		ResultSet rs = null;
		
		try (Connection conn = ds.getConnection()){
			rs = conn.prepareStatement(sql).executeQuery();
			return rs;
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		
		return rs;
	}
}