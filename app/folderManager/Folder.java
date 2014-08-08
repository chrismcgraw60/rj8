package folderManager;

import java.io.StringWriter;
import java.nio.file.Path;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * POJO to model a folder. The folder in this context will be a parent folder for a given JUnit report.
 */
public class Folder {
	
	private final Long id;
	private final Path path;
	private final Status status;
	private final DateTime created;
	private final DateTime updated;
	
	/**
	 * Public constructor.
	 * @param id  May be null (in case where Folder is not yet persisted in storage).
	 * @param path Must not be null.
	 * @param status Must not be null.
	 * @param created Must not be null.
	 * @param updated Must not be null.
	 */
	public Folder(final Long id, final Path path, final Status status, final DateTime created, final DateTime updated) {
		
		Preconditions.checkNotNull(path, "path must not be null.");
		Preconditions.checkNotNull(status, "status must not be null.");
		Preconditions.checkNotNull(created, "created must not be null.");
		Preconditions.checkNotNull(updated, "updated must not be null.");
		
		this.id = id;
		this.path = path;
		this.status = status;
		this.created = created;
		this.updated = updated;
	}
	
	/**
	 * Helper method that will return a new {@link Folder} instance that is a copy of this Folder with
	 * the exception of its status field (which will be set to the given status). 
	 * @param newStatus The new status for the Folder.
	 * @return A copy of this folder with updated status.
	 */
	public Folder updateStatus(Status newStatus) {
		return new Folder(this.getId(), this.getPath(), newStatus, this.getCreated(), this.getUpdated());
	}
	
	/**
	 * Helper method that will return a new {@link Folder} instance that is a copy of this Folder with
	 * the exception of its updated field (which will be set to the given status). 
	 * @param newStatus The new updated time stamp for the Folder.
	 * @return A copy of this folder with updated time stamp.
	 */
	public Folder updateTimestamp(DateTime timestamp) {
		return new Folder(this.getId(), this.getPath(), this.getStatus(), this.getCreated(), timestamp);
	}
	
	/**
	 * @return Unique identifier for the folder. This is used in persistence which in this case means the 
	 * the id will map to an RDBMS column.
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * @return The file system {@link Path} that describes the location of this Folder.
	 */
	public Path getPath() {
		return path;
	}
	
	/**
	 * @return The {@link Status} value for this Folder.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Timestamp when this Folder was created.
	 */
	public DateTime getCreated() {
		return created;
	}

	/**
	 * @return Timestamp when this Folder was updated.
	 */
	public DateTime getUpdated() {
		return updated;
	}
	
	@Override
	public String toString() {
		return toJSON(this);
	}
	
	public static String toJSON(Folder folder) {
		final StringWriter sw = new StringWriter();
		
		try {
		
			final JsonGenerator jg = new JsonFactory().createGenerator(sw).useDefaultPrettyPrinter();
			jg.writeStartObject();
			
				jg.writeStringField("id", folder.getId().toString());
				jg.writeStringField("path", folder.getPath().toString());
				jg.writeStringField("status", folder.getStatus().toString());
				jg.writeStringField("created", folder.getCreated().toString());
				jg.writeStringField("updated", folder.getUpdated().toString());
				
			jg.writeEndObject();
			jg.close();
		}
		catch(Exception ex) {
			Throwables.propagate(ex);
		}
		
		return sw.toString();
	}

	/**
	 * Enumerates the various states that a Folder can be in from the perspective of this application.
	 * We currently only track that a Folder was created. 
	 */
	public enum Status {
		Active, ActiveWithErrors, Importing
	}
}
