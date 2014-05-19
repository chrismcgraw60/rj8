package testdata;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Generates various Types of Junit result to provide data for import.
 */
@Ignore
public class CreateReportTestD_Ignored {

	@Test
	public void testFailNotImplemented_D() {
		fail("Not yet implemented");
	}

	@Test
	public void testPassAssertionNoComment_D() {
		assertTrue(true);
	}
	
	@Test
	public void testPassAssertionWithComment_D() {
		assertTrue("This should pass", true);
	}

	@Test
	public void testFailAssertionNosComment_D() {
		int expected = 1;
		int actual = 2;
		assertEquals(expected,  actual);
	}
	
	@Test
	public void testFailAssertionWithComment_D() {
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
