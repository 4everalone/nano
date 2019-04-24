package com.leansoft.nano.transform;

import java.util.TimeZone;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.transform.TimeZoneTransform;

public class TimeZoneTransformTest extends NanoBaseUnitTest {
	public void testTimeZone() throws Exception {
		TimeZone zone = TimeZone.getTimeZone("GMT");
	    TimeZoneTransform format = new TimeZoneTransform();
	    String value = format.write(zone);
	    TimeZone copy = format.read(value);
	      
	    assertEquals(zone, copy);
	}
}
