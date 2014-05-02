package importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ReportedTestResultEntryTest {

	@Rule public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * Storage ID must be set for state to be considered valid.
	 */
	@Test
	public void uninitialisedStorageIdInvalid() {
		
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("storageId was not set.");
		
		new ReportedTestResultEntry()
			.setId(new Long(22))
			.setQualifiedName("")
			.setTime("0.00")
			.validateState();
	}
	
	@Test
	public void testCaseEntryQualifiedNameParsed_hasPackagePrefix() {
		
		ReportedTestResultEntry tce = new ReportedTestResultEntry();
		
		tce.setId(new Long(22))
			.setQualifiedName("com.chris.TestFoo") .setTime("2.22").setStorageId(UUID.randomUUID());
		
		assertEquals("id is set.", new Long(22), tce.getId());
		assertEquals("time is set.", "2.22", tce.getTime());
		assertEquals("QualifiedName is set..", "com.chris.TestFoo", tce.getQualifiedName());
		assertEquals("Package Name is correctly computed.", "com.chris", tce.getPackageName());
		assertEquals("Local Name is correctly computed.", "TestFoo", tce.getLocalTestCaseName());
		assertNotNull("storageID not null", tce.getStorageId());
	}
	
	@Test
	public void testCaseEntryQualifiedNameParsed_NoPackagePrefix() {
		
		ReportedTestResultEntry tce = new ReportedTestResultEntry();
		
		tce.setId(new Long(22)).setQualifiedName("TestFoo").setTime("2.22").setStorageId(UUID.randomUUID());
		
		assertEquals("id is set.", new Long(22), tce.getId());
		assertEquals("time is set.", "2.22", tce.getTime());
		assertEquals("QualifiedName is set..", "TestFoo", tce.getQualifiedName());
		assertEquals("Package Name is correctly computed.", "", tce.getPackageName());
		assertEquals("Local Name is correctly computed.", "TestFoo", tce.getLocalTestCaseName());
		assertNotNull("storageID not null", tce.getStorageId());
	}
}
