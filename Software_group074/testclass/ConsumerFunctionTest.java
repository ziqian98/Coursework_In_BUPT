import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;

public class ConsumerFunctionTest {

	private static ConsumerFunction cf = new ConsumerFunction();
	@Test(timeout = 1000)
	public void testReadmeter() {
		cf.readmeter("123456", "gas.txt", 6);
	}

	@Test
	public void testReadtariff() {
		assertEquals("18.0", String.valueOf(cf.readtariff("elec_tariff.txt")));
	}

	@Test
	public void testReadbudget() {
		assertEquals(670, cf.readbudget("123456", "elecbudget.txt"));
	}

	@Test(timeout = 1000)
	public void testSetresultpageelec() {
		cf.setresultpageelec("123456", "2018-4-1", "2018-6-30");
	}

	@Test(timeout = 1000)
	public void testSetresultpagegas() {
		cf.setresultpagegas("123456", "2018-4-1", "2018-6-30");
	}

	@Test
	public void testGetelectariff() {
		assertEquals("18", cf.getelectariff());
	}

	@Test(timeout = 1000)
	public void testGetgastariff() {
		assertEquals("11", cf.getgastariff());
	}

	@Test
	public void testChangeuserbudget() {
		cf.changeuserbudget("123456", "670", "elecbudget.txt");
		assertEquals(670, cf.readbudget("123456", "elecbudget.txt"));
	}

}
