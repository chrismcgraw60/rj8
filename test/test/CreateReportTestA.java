package test;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Generates various Types of Junit result to provide data for import.
 */
public class CreateReportTestA {

	@Test
	public void testFailNotImplemented_A() {
		fail("Not yet implemented");
	}

	@Test
	public void testPassAssertionNoComment_A() {
		assertTrue(true);
	}
	
	@Test
	public void testPassAssertionWithComment_A() {
		assertTrue("This should pass", true);
	}

	@Test
	public void testFailAssertionNosComment_A() {
		int expected = 1;
		int actual = 2;
		assertEquals(expected,  actual);
	}
	
	@Test
	public void testFailAssertionWithComment_A() {
		int expected = 1;
		int actual = 2;
		assertEquals("Numbers should be equal.", expected,  actual);
	}
	
	@Test
	public void testException_A() {
		doSomethingWrong();
	}
	
	private void doSomethingWrong() {
		throw new RuntimeException("Error in Test helper method.");
	}
}
