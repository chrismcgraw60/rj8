package importer;

import java.util.stream.Stream;

/**
 * Defines API for Test Import Mechanism.
 */
public interface IBatchImporter {

	/**
	 * Import a given {@link Stream} of {@link ReportedTestElement}s to a database.
	 * @param testCaseEntries The {@link ReportedTestElement}s to be imported. Must not be null.
	 * @return The # of imported elements.
	 */
	public abstract int doImport(Stream<ReportedTestElement> testCaseEntries);

}