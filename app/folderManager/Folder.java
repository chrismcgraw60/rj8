package folderManager;

import java.nio.file.Path;

import org.joda.time.DateTime;

public class Folder {
	
	private final Long id;
	private final Path path;
	private final Status status;
	private final DateTime created;
	private final DateTime updated;
	
	public Folder(final Long id, final Path path, final Status status, final DateTime created, final DateTime updated) {
		this.id = id;
		this.path = path;
		this.status = status;
		this.created = created;
		this.updated = updated;
	}
	
	public Long getId() {
		return id;
	}
	
	public Path getPath() {
		return path;
	}
	
	public Status getStatus() {
		return status;
	}

	public DateTime getCreated() {
		return created;
	}

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

	public enum Status {
		Created
	}
}
