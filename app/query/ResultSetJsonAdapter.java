package query;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

import org.jooq.lambda.SQL;
import org.jooq.lambda.Unchecked;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * TODO: WEB SOCKET
 */
public class ResultSetJsonAdapter {
	
	final ResultSet jdbcResultSet;
	final Map<String, String> columnMetadata;
	
	private ResultSetJsonAdapter(ResultSet jdbcResultSet, Map<String, String> columnMetadata) {
		this.jdbcResultSet = jdbcResultSet;
		this.columnMetadata = columnMetadata;
	}
	
	public static ResultSetJsonAdapter initialiseFrom(ResultSet rs) {
		Map<String, String> columnMap = buildColumnMetadataMap(rs);
		return new ResultSetJsonAdapter(rs, columnMap);
	}

	private static Map<String, String> buildColumnMetadataMap(ResultSet rs) {
		Map<String, String> columnMap = Maps.newLinkedHashMap();
		try {
			ResultSetMetaData rmd = rs.getMetaData();
			for (int i=1; i<= rmd.getColumnCount(); i++) {
				columnMap.put(rmd.getColumnName(i), rmd.getColumnTypeName(i));
			}
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		return columnMap;
	}
	
	public Stream<String> rowsAsStream() {
		Stream<String> json = 
			SQL.stream(this.jdbcResultSet, Unchecked.function(row -> {
				final StringWriter sw = new StringWriter();
				final JsonGenerator jg = new JsonFactory().createGenerator(sw).useDefaultPrettyPrinter();
				jg.writeStartObject();
					jg.writeArrayFieldStart("rows");
						this.columnMetadata.forEach(Unchecked.biConsumer((k, v) -> jg.writeString(row.getString(k))));
					jg.writeEndArray();
				jg.writeEndObject();
				jg.close();
				return sw.toString();
			}));
		
		return json;
	}
	
	/**
	 * @return <p>Json representation of column metadata.</p> 
	 * e.g { "result" : { columns : [ {"id": "bigint"}. {"name": "varchar"}. {"timestamp": "timestamp"} ] }}  
	 */
	public String getMetadata() {		
		try {
			final StringWriter sw = new StringWriter();
			final JsonGenerator jg = new JsonFactory().createGenerator(sw).useDefaultPrettyPrinter();
			jg.writeStartObject();
				jg.writeObjectFieldStart("result");
					jg.writeArrayFieldStart("columns");
						jg.writeStartObject();
							this.columnMetadata.forEach(Unchecked.biConsumer((k, v) -> jg.writeStringField(k, v)));
						jg.writeEndObject(); // End Column Type/Name pair
					jg.writeEndArray(); // End Columns Array
				// End object "result"
			jg.writeEndObject();
			jg.close();
			return sw.toString();
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		return null;
	}
}
