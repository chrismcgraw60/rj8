package query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.H2DataSource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
	public void testQueryOnTestEntries() throws Exception {
		String sql = "SELECT * FROM TESTENTRY";

		ResultSet rs = new JdbcQueryService(DS).runQuery(sql);
		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
		
		String metadataJson = jsonRs.getMetadata();
		List<String> rowJson = jsonRs.rowsAsStream().collect(Collectors.toList());
		
		assertNotNull(metadataJson);
		assertNotNull(rowJson);
	}
	
	@Test
	public void testQueryOnTestSuites() throws Exception {
		String sql = "SELECT * FROM TESTSUITE order by FILE";
		ResultSet rs = new JdbcQueryService(DS).runQuery(sql);
		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
		
		String metadataJson = jsonRs.getMetadata();
		assertNotNull(metadataJson);
		
		Map<String, Object> columnNameToType = mapJsonToNameType(metadataJson);
		
		assertEquals("Suite Table has expected # columns.", 11, columnNameToType.size());
		assertColNameAndType(columnNameToType, "ID", "BIGINT");
		assertColNameAndType(columnNameToType, "UUID", "VARCHAR");
		assertColNameAndType(columnNameToType, "CLASSNAME", "VARCHAR");
		assertColNameAndType(columnNameToType, "TIME", "VARCHAR");
		assertColNameAndType(columnNameToType, "FOLDER", "VARCHAR");
		assertColNameAndType(columnNameToType, "FILE", "VARCHAR");
		assertColNameAndType(columnNameToType, "TESTS", "BIGINT");
		assertColNameAndType(columnNameToType, "FAILURES", "BIGINT");
		assertColNameAndType(columnNameToType, "ERRORS", "BIGINT");
		assertColNameAndType(columnNameToType, "SKIPPED", "BIGINT");
		assertColNameAndType(columnNameToType, "TIMESTAMP", "TIMESTAMP");
		
		List<String> rowJson = jsonRs.rowsAsStream().collect(Collectors.toList());
		assertNotNull(rowJson);
		assertEquals("Expected 2 Rows in Suites Table", 2,  rowJson.size());
		
		List<String> valueList = extractFirstRowAsList(rowJson);
		assertEquals("TestEntry Table has expected # values.", 11, valueList.size());
		assertNotNull("ID not null.", valueList.get(0));
		assertNotNull("UUID not null.", valueList.get(1));
		assertEquals("Expected CLASSNAME value.", "com.ibm.rdm.client.api.tests.AllAutomatedTests", valueList.get(2));
		assertEquals("Expected TIME value.", "14429.64", valueList.get(3));
		assertEquals("Expected FOLDER value.", "developer_1", valueList.get(4));
		assertEquals("Expected FILE value.", "TEST-developer1_testRun_1.xml", valueList.get(5));
		
		assertEquals("Expected TESTS value.", 1740, Long.parseLong(valueList.get(6)));
		assertEquals("Expected FAILURES value.", 6, Long.parseLong(valueList.get(7)));
		assertEquals("Expected ERRORS value.", 1, Long.parseLong(valueList.get(8)));
		assertEquals("Expected SKIPPED value.", 0, Long.parseLong(valueList.get(9)));
		assertNotNull("TIMESTAMP not null.", valueList.get(10));
		
		System.out.println(valueList);
		
	}

	@SuppressWarnings("unchecked")
	private List<String> extractFirstRowAsList(List<String> rowJson) throws Exception {
		String row1 = rowJson.get(0);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> rowData = mapper.readValue(row1, Map.class); 
		List<String> valueList = (List<String>) rowData.get("row");
		return valueList;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mapJsonToNameType(String metadataJson) throws Exception{
		/*
		 * We use JAckson to parse JSON into gnarly untyped maps.
		 * This code gets to the data we want and turns it into a map of ColumnName -> Column Type (ie ID -> BIGINT).
		 */
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> columnMetadata = mapper.readValue(metadataJson, Map.class); 
		List<Map<String, Object>> columns = (List<Map<String, Object>>) ((Map<String, Object>)columnMetadata.get("metadata")).get("columns");
		
		Map<String, Object> columnNameToType = Maps.newHashMap();
		for (Map<String, Object> map : columns) {
			String name = map.get("name").toString();
			String type = map.get("type").toString();
			columnNameToType.put(name, type);
		}
		return columnNameToType;
	}

	private void assertColNameAndType(Map<String, Object> columnNameToType, String expectedColName, String expectedColType) {
		assertTrue("Expected Column ID", columnNameToType.containsKey(expectedColName));
		assertEquals("Column " + expectedColName + " has type: " + expectedColType, expectedColType, columnNameToType.get(expectedColName));
	}
}
