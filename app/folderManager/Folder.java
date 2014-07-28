package folderManager;

import java.nio.file.Path;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

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
		StringBuilder sb = new StringBuilder();
		
		sb.append("Folder: {\n")
			.append("\t path: ").append(path).append("\n")
			.append("\t id: ").append(id).append("\n")
			.append("\t status: ").append(status).append("\n")
			.append("\t created: ").append(created).append("\n")
			.append("\t updated: ").append(updated).append("\n")
			.append("}");
		
		return sb.toString();
	}

	/**
	 * Enumerates the various states that a Folder can be in from the perspective of this application.
	 * We currently only track that a Folder was created. 
	 */
	public enum Status {
		Created
	}
}
