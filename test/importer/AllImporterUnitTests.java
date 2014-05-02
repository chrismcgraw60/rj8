package importer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ReportedTestResultEntryTest.class,
	ReportedTestSuiteEntryTest.class,
	ReportParserTest.class
})

/**
 * Test Suite for integration tests. 
 * We take integration tests to be those which require external IO (ie with file system or DB).
 */
public class AllImporterUnitTests {}
