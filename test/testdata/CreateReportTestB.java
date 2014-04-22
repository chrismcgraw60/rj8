package testdata;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Generates various Types of Junit result to provide data for import.
 */
public class CreateReportTestB {

	@Test
	public void testFailNotImplemented_B() {
		fail("Not yet implemented");
	}

	@Test
	public void testPassAssertionNoComment_B() {
		assertTrue(true);
	}
	
	@Test
	public void testPassAssertionWithComment_B() {
		assertTrue("This should pass", true);
	}

	@Test
	public void testFailAssertionNosComment_B() {
		int expected = 1;
		int actual = 2;
		assertEquals(expected,  actual);
	}
	
	@Test
	public void testFailAssertionWithComment_B() {
		int expected = 1;
		int actual = 2;
		assertEquals("Numbers should be equal.", expected,  actual);
	}
	
	@Test
	public void testException_B() {
		doSomethingWrong();
	}
	
	private void doSomethingWrong() {
		throw new RuntimeException("Error in Test helper method.");
	}
}
