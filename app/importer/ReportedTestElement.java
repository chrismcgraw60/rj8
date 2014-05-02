package importer;

import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * Base class for Reportable Test elements.
 * Reportable Test elements are distinct types of entity that will be persisted
 * for query. We currently have 2 distinct types:
 * - TestSuites
 * - TestCases
 */
public abstract class ReportedTestElement {
	
	protected UUID storageId; 
	protected String qualifiedName;
	protected String packageName;
	protected String localName;
	protected String time;
	
	public UUID getStorageId() {
		return this.storageId;
	}
	
	public ReportedTestElement setStorageId(UUID storageId) {
		Preconditions.checkNotNull(storageId, "storageId must not ne null");
		this.storageId = storageId;
		return this;
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public ReportedTestElement setQualifiedName(String qualifiedName) {
		Preconditions.checkNotNull(qualifiedName, "qualifiedName must not ne null");
		
		this.qualifiedName = qualifiedName;
		/*
		 * Split name into package / local name
		 */
		int lastDotIndex = this.qualifiedName.lastIndexOf(".");
		this.packageName = lastDotIndex >= 0 ? this.qualifiedName.substring(0, lastDotIndex) : "";
		this.localName = this.qualifiedName.substring(lastDotIndex+1);
		
		return this;
	}

	/**
	 * @return The time taken by the Test, as recorded by the junit report "time" attribute.
	 */
	public String getTime() {
		return time;
	}
	
	/**
	 * @param time The time taken by the Test, as recorded by the junit report "time" attribute.
	 * @return This object.
	 */
	public ReportedTestElement setTime(String time) {
		Preconditions.checkNotNull(time, "time must not ne null");
		this.time = time;
		return this;
	}
	
	public String getPackageName() {
		return this.packageName;
	}

	public String getLocalTestCaseName() {
		return localName;
	}
	
	public void validateState() {
		checkStateNotNull(this.qualifiedName, "qualifiedName was not set.");
		checkStateNotNull(this.packageName, "packageName was not set.");
		checkStateNotNull(this.storageId, "storageId was not set.");
		checkStateNotNull(this.time, "time was not set.");
	}
	
	protected static void checkStateNotNull(Object ref, String errorMessage) {
		if (ref == null) {
			throw new IllegalStateException(errorMessage);
		}
	}
}
