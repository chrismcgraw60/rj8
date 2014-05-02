package query;

import java.sql.ResultSet;

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
		
		ResultSet rs = new JdbcQueryService().runQuery(DS, sql);
		ResultSetJsonAdapter result = ResultSetJsonAdapter.initialiseFrom(rs);
		
		System.out.println(result.getMetadata());
		
		result.rowsAsStream().forEach(System.out::println);
	}
}
