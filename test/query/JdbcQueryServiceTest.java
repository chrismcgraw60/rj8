package query;

import static org.junit.Assert.assertNotNull;

import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.H2DataSource;

import com.jolbox.bonecp.BoneCPDataSource;

public class JdbcQueryServiceTest {
	
	private static BoneCPDataSource DS = null;
	
	@BeforeClass
	public static void setUp() throws Exception {
		DS = H2DataSource.create();
	}
	
	@AfterClass
	public static void tearDown() {
		DS.close();
	}
	@Test
	public void testQuery() throws Exception {
		String sql = "SELECT * FROM TESTENTRY";

		ResultSet rs = new JdbcQueryService(DS).runQuery(sql);
		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
		
		String metadataJson = jsonRs.getMetadata();
		List<String> rowJson = jsonRs.rowsAsStream().collect(Collectors.toList());
		
		assertNotNull(metadataJson);
		assertNotNull(rowJson);
		
	}
}
