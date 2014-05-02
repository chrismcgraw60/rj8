package utils;

import com.google.common.base.Throwables;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class H2DataSource {

	public static BoneCPDataSource create() {
		try {
			Class.forName("org.h2.Driver");
			BoneCPConfig config = new BoneCPConfig();
	//		config.setJdbcUrl("jdbc:h2:file:/media/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"); 
			config.setJdbcUrl("jdbc:h2:tcp://localhost:9092/media/d2/skunk/activator/activator-1.1.0_projects/rj8/db/test-data;MODE=PostgreSQL");
			config.setUsername("sa"); 
			config.setPassword("");
			config.setLazyInit(true);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			return new BoneCPDataSource(config);
		} catch (ClassNotFoundException e) {
			Throwables.propagate(e);
		}
		return null;
	}
}
