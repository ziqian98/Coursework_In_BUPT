import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;

public class ProviderTest {
	
	private static Provider p = new Provider();
	
	@Test(timeout = 1000)
	public void testChangetariff() {
		p.changetariff("18", "elec_tariff.txt");
		assertEquals("18", p.currenttariffelec());
	}
	
	@Test
	public void testCheckidinput() {
		assertEquals(true, p.checkidinput("Tomcat"));
	}

	@Test(timeout = 1000)
	public void testSendemail() {
		p.sendemail("Tom6789", "allbill.txt");
	}

	@Test(timeout = 1000)
	public void testReadhistory() {
		p.readhistory("Tom6789", "2018-4-1", "2018-6-1");
	}

	@Test
	public void testCurrenttariffelec() {
		assertEquals("18", p.currenttariffelec());
	}

	@Test
	public void testCurrenttariffgas() {
		assertEquals("11", p.currenttariffgas());
	}

	@Test(timeout = 3000)
	public void testNewconsumer() {
		p.newconsumer("789000", "Jack", "333333");
	}

	@Test(timeout = 1000)
	public void testRemoveconsumer() {
		p.removeconsumer("Tom6789", "all.txt");
	}

	@Test(timeout = 1000)
	public void testRemoveconsumerfile() {
		p.removeconsumerfile("Tom6789");
	}

	@Test(timeout = 1000)
	public void testDoDeleteEmptyDir() {
		p.doDeleteEmptyDir("consumerinfo/Tom6789");
	}

	@Test
	public void testDeleteDir() {
		assertEquals(false, p.deleteDir(new File("Tom6789")));
	}

	@Test(timeout = 1000)
	public void testGetconsumer() {
		p.getconsumer();
	}

	@Test
	public void testCheckid() {
		assertEquals(true, p.checkid("123456"));
	}

}
