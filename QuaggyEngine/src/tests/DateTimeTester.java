package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import core.DateTime;

public class DateTimeTester {

	@Test
	public void test() {
		DateTime t1, t2, t3;
		
		t1 = new DateTime("2015-12-01 00:00:00");
		t2 = new DateTime("0000-01-00 00:00:00");
		t3 = t1.add(t2);
		assertTrue(t3.toString().equals("2016-1-1 0:0:0"));
		
		t1 = new DateTime("2015-04-23 14:53:16");
		t2 = new DateTime("0001-12-05 24:08:12");
		t3 = t1.add(t2);
		assertTrue(t3.toString().equals("2017-4-29 15:1:28"));
		
		t1 = new DateTime("2015-12-27 01:16:54");
		t3 = t1.toUTC();
		assertTrue(t3.toString().equals("2015-12-27 6:16:54"));
		
		t1 = new DateTime("2016-01-01 02:30:30");
		t3 = t1.toEastern();
		assertTrue(t3.toString().equals("2015-12-31 21:30:30"));
		
		t1 = new DateTime("2012-11-05 00:00:00");
		t2 = new DateTime("2013-11-05 00:00:00");
		t3 = new DateTime("2015-12-26 18:36:17");
		assertFalse(t3.inRange(t1, t2));
		
		//Test figuring out difference, in days
		t1 = new DateTime("2016-01-05 00:00:00");
		t2 = new DateTime("2015-12-30 00:00:00");
		assertEquals(6, DateTime.daysBetween(t2, t1));
		
		t1 = new DateTime("2016-01-02 00:00:00");
		t2 = new DateTime("2016-01-02 00:00:00");
		assertEquals(0, DateTime.daysBetween(t2, t1));
	}

}
