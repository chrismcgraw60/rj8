package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Throwables;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class H2DataSource {

	public static BoneCPDataSource create() {
		try {
			Class.forName("org.h2.Driver");
			BoneCPConfig config = new BoneCPConfig();
	//		config.setJdbcUrl("jdbc:h2:file:/media/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"); 
//			config.setJdbcUrl("jdbc:h2:tcp://localhost:9092/media/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL");
			//media/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL
			config.setJdbcUrl("jdbc:h2:tcp://localhost:9092/file:/home/chrismcgraw60/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL");
			config.setUsername("sa"); 
			config.setPassword("");
			config.setLazyInit(true);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			config.setConnectionTimeout(15, TimeUnit.SECONDS);
			return new BoneCPDataSource(config);
		} catch (ClassNotFoundException e) {
			Throwables.propagate(e);
		}
		return null;
	}
	
	public static void clear(BoneCPDataSource ds) {
		try (Connection conn = ds.getConnection()){
			conn.prepareStatement("DELETE FROM TESTSUITE").executeUpdate();
			conn.prepareStatement("DELETE FROM TESTENTRY").executeUpdate();
			conn.prepareStatement("DELETE FROM FOLDER").executeUpdate();
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
	}
}
