package query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.google.common.base.Throwables;

public class JdbcQueryService {

	public ResultSet runQuery(DataSource ds, String sql) {
		ResultSet rs = null;
		
		try (Connection conn = ds.getConnection()){
			rs = conn.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		
		return rs;
	}
}