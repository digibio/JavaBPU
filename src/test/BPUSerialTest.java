/**
 * 
 */
package test;
/*
 * This has been completely unmaintained so far
 */
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bio.digi.bpucontrol.BPUControlSerial;


/**
 * @author xor
 *
 */
public class BPUSerialTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link BPUControl.BPUSerial#four()}.
	 */
	@Test
	public void testStaticListComports() {
		String[] comports = null;
		try {
			comports = BPUControlSerial.listAvailablePorts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		assertEquals("please verify 1 usb device has been plugged in", 1,  comports.length);
	}

}
