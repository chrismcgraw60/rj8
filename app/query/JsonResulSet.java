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
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Wraps a JDBC {@link ResultSet} as JSON Strings. There are 2 components:
 * <ol>
 * <li> The ResultSet metadata that gives Column name / type information.
 * <li> The ResultSet data itself.
 * </ol>
 */
public class JsonResulSet {
	
	private static class ColumnMetaData {
		
		ColumnMetaData(String columnName, String columnType) {
			this.columnName = columnName;
			this.columnType = columnType;
		}
		
		final String columnName;
		final  String columnType;
	}
	
	final ResultSet jdbcResultSet;
	final Map<String, ColumnMetaData> columnMetadata;
	
	/**
	 * Private constructor. Instances created via {@link JsonResulSet#initialiseFrom(ResultSet)}.
	 * @param jdbcResultSet The JDBC {@link ResultSet} to be wrapped.
	 * @param columnMetadata A Map encapsulating the result's Column Name / Type pairs.
	 */
	private JsonResulSet(ResultSet jdbcResultSet, Map<String, ColumnMetaData> columnMetadata) {
		this.jdbcResultSet = jdbcResultSet;
		this.columnMetadata = columnMetadata;
	}
	
	/**
	 * Factory method to create a {@link JsonResulSet} from a given JDBC {@link ResultSet}. 
	 * @param rs The {@link ResultSet} to be wrapped.
	 * @return The new wrapper {@link JsonResulSet}.
	 */
	public static JsonResulSet initialiseFrom(ResultSet rs) {
		Map<String, ColumnMetaData> columnMap = buildColumnMetadataMap(rs);
		return new JsonResulSet(rs, columnMap);
	}
	
	/**
	 * @return the adapted ResultSet Rows as a stream of JSON Strings.
	 * e.g {row: ["val1", "val2", "val3"]}
	 */
	public Stream<String> rowsAsStream() {
		Stream<String> json = 
			SQL.stream(this.jdbcResultSet, Unchecked.function(row -> {
				final StringWriter sw = new StringWriter();
				final JsonGenerator jg = new JsonFactory().createGenerator(sw).useDefaultPrettyPrinter();
				jg.writeStartObject();
					jg.writeArrayFieldStart("row");
						this.columnMetadata.forEach(Unchecked.biConsumer((k, v) -> jg.writeString(row.getString(k))));
					jg.writeEndArray();
				jg.writeEndObject();
				jg.close();
				return sw.toString();
			}));
		
		return json;
	}
	
	/**
	 * Returns the ResultSet metadata as a JSON String.
	 * @return <p>Json representation of column metadata.</p> 
	 * e.g { "metadata" : { columns : [ {"id": "bigint"}. {"name": "varchar"}. {"timestamp": "timestamp"} ] }}  
	 */
	public String getMetadata() {		
		try {
			final StringWriter sw = new StringWriter();
			final JsonGenerator jg = new JsonFactory().createGenerator(sw).useDefaultPrettyPrinter();
			jg.writeStartObject();
				jg.writeObjectFieldStart("metadata");
					jg.writeArrayFieldStart("columns");
						this.columnMetadata.forEach(Unchecked.biConsumer((k, v) -> writeColumnMetaData(jg, v)));
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
	
	/*
	 * Write out a ColumnMetadata object to a given JSON generator.
	 */
	private static void writeColumnMetaData(JsonGenerator jg, ColumnMetaData mdt) throws JsonGenerationException, IOException {
		jg.writeStartObject();
		jg.writeStringField("name", mdt.columnName);
		jg.writeStringField("type", mdt.columnType);
		jg.writeEndObject();;
	}
	
	/*
	 * Build the Column Name / Type map from the ResultSet object.
	 */
	private static Map<String, ColumnMetaData> buildColumnMetadataMap(ResultSet rs) {
		/*
		 * LinkedHashmap to preserve key traversal order.
		 * Important as we output rows as arrays of values where the index position in
		 * the row implicitly corresponds to the entry order of the column map.
		 * Reason for this is performance. 
		 */
		Map<String, ColumnMetaData> columnMap = Maps.newLinkedHashMap();
		try {
			ResultSetMetaData rmd = rs.getMetaData();
			for (int i=1; i<= rmd.getColumnCount(); i++) {
				ColumnMetaData cmd = 
					new ColumnMetaData(rmd.getColumnName(i), rmd.getColumnTypeName(i));
				columnMap.put(cmd.columnName, cmd);
			}
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
		return columnMap;
	}
}
