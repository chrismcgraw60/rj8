package importer;

import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ReportedTestSuiteEntryTest {

	@Rule public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * #Tests must be set for state to be considered valid.
	 */
	@Test
	public void uninitialisedFieldsInvalid() {
		
		/*
		 * Tests Run
		 */
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("testsRun was not set.");
		new ReportedTestSuiteEntry()
			.setContainingFile("FILE")
			.setContainingFolder(Paths.get("FOLDER"))
			.setStorageId(UUID.randomUUID())
			.setQualifiedName("")
			.setTime("0.00")
			.validateState();
		
		/*
		 * FileName
		 */
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("fileName was not set.");
		new ReportedTestSuiteEntry()
			.setTestsRun(new Long(1))
			.setStorageId(UUID.randomUUID())
			.setQualifiedName("")
			.setTime("0.00")
			.validateState();
		
		/*
		 * FolderName
		 */
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("folderName was not set.");
		new ReportedTestSuiteEntry()
			.setTestsRun(new Long(1))
			.setContainingFile("FILE")
			.setStorageId(UUID.randomUUID())
			.setQualifiedName("")
			.setTime("0.00")
			.validateState();
		
		/*
		 * Timestamp
		 */
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("timestamp was not set.");
		new ReportedTestSuiteEntry()
			.setTestsRun(new Long(1))
			.setContainingFile("FILE")
			.setContainingFolder(Paths.get("FOLDER"))
			.setStorageId(UUID.randomUUID())
			.setQualifiedName("")
			.setTime("0.00")
			.validateState();
	}
}
