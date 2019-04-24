package com.leansoft.nano.transform;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.transform.CalendarTransform;

public class CalendarTransformTest extends NanoBaseUnitTest {
	
	public void testCalendar() throws Exception {
		Calendar calendar = new GregorianCalendar();
		CalendarTransform transform = new CalendarTransform();
		
		calendar.setTime(new Date());
		
		String value = transform.write(calendar);
		Calendar copy = transform.read(value);
		
		assertEquals(calendar, copy);
	}

}
