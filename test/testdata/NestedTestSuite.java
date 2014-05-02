package testdata;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	CreateReportTestC.class
})

/**
 * Is intended to be run as part of the AllTests suite to generate a small set of Test Report data.
 */
public class NestedTestSuite {}
