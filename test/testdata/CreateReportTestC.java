package testdata;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Generates various Types of Junit result to provide data for import.
 */
public class CreateReportTestC {

	@Test
	public void testFailNotImplemented_C() {
		fail("Not yet implemented");
	}

	@Test
	public void testPassAssertionNoComment_C() {
		assertTrue(true);
	}
	
	@Test
	public void testPassAssertionWithComment_C() {
		assertTrue("This should pass", true);
	}

	@Test
	public void testFailAssertionNosComment_C() {
		int expected = 1;
		int actual = 2;
		assertEquals(expected,  actual);
	}
	
	@Test
	public void testFailAssertionWithComment_C() {
		int expected = 1;
		int actual = 2;
		assertEquals("Numbers should be equal.", expected,  actual);
	}
	
	@Test
	public void testException_C() {
		doSomethingWrong();
	}
	
	private void doSomethingWrong() {
		throw new RuntimeException("Error in Test helper method.");
	}
}
