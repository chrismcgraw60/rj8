package testdata;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	CreateReportTestA.class,
	CreateReportTestB.class,
	NestedTestSuite.class
})

/**
 * The root test suite to be run by gen-testData.sh for creating small test report data set. 
 */
public class AllTests {}
