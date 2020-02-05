package com.leansoft.nano;

import java.util.Date;

import com.leansoft.nano.IReader;
import com.leansoft.nano.NanoFactory;
import com.leansoft.nano.annotation.Element;
import com.leansoft.nano.annotation.RootElement;

public class DateTest extends NanoBaseUnitTest {
	private static final String DATETIME_XML = "<?xml version=\"1.0\"?>\n" + 
                                               "<dateTime>\n" + 
			                                         "<time>2012-01-02T05:22:15.000Z</time>\n" +
                                               "</dateTime>\n";
	private static final String DATETIME_XML_TZ_OFFSET_MS= "<?xml version=\"1.0\"?>\n" + 
                                               "<dateTime>\n" + 
                                                 "<time>2020-01-31T12:06:04.334-05:00</time>\n" +
                                               "</dateTime>\n";
	private static final String DATETIME_XML_TZ_OFFSET= "<?xml version=\"1.0\"?>\n" + 
                                               "<dateTime>\n" + 
                                                 "<time>2020-01-31T12:06:04-05:00</time>\n" +
                                               "</dateTime>\n";
	@RootElement
	public static class DateTime {
		@Element
		private Date time;
		
		public Date getTime() {
			return time;
		}
		
		public void setTime(Date time) {
			this.time = time;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((time == null) ? 0 : time.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DateTime other = (DateTime) obj;
			if (time == null) {
				if (other.time != null)
					return false;
			} else if (!time.equals(other.time))
				return false;
			return true;
		}
	}
	
	public void testRead() throws Exception {
		IReader xmlReader = NanoFactory.getXMLReader();
		DateTime dateTime = xmlReader.read(DateTime.class, DATETIME_XML);
		assertEquals("2 Jan 2012 05:22:15 GMT", dateTime.getTime().toGMTString());
      
      DateTime dateTimeTzOffsetMs = xmlReader.read(DateTime.class, DATETIME_XML_TZ_OFFSET_MS);
		assertEquals("31 Jan 2020 17:06:04 GMT", dateTimeTzOffsetMs.getTime().toGMTString());
      
      DateTime dateTimeTzOffset = xmlReader.read(DateTime.class, DATETIME_XML_TZ_OFFSET);
		assertEquals("31 Jan 2020 17:06:04 GMT", dateTimeTzOffset.getTime().toGMTString());
	}
	
	public void testWriteXML() throws Exception {
		DateTime dateTime = new DateTime();
		dateTime.setTime(new Date(Date.UTC(112, 2, 3, 11, 20, 59)));
		String xmlStr = NanoFactory.getXMLWriter().write(dateTime);
		assertTrue(xmlStr.indexOf("<time>2012-03-03T11:20:59.000Z</time>") > 0);
	}
	
	public void testWriteJSON() throws Exception {
		DateTime dateTime = new DateTime();
		dateTime.setTime(new Date(Date.UTC(112, 2, 3, 11, 20, 59)));
		String jsonStr = NanoFactory.getJSONWriter().write(dateTime);
		assertTrue(jsonStr.indexOf("\"time\": \"2012-03-03T11:20:59.000Z\"") > 0);
	}
	
	public void testReadWriteXML() throws Exception {
		DateTime dateTime = new DateTime();
		dateTime.setTime(new Date(Date.UTC(111, 10, 10, 2, 3, 25)));
		String xmlStr = NanoFactory.getXMLWriter().write(dateTime);
		DateTime dateTimeNew = NanoFactory.getXMLReader().read(DateTime.class, xmlStr);
		assertEquals(dateTimeNew, dateTime);
	}
	
	public void testReadWriteJSON() throws Exception {
		DateTime dateTime = new DateTime();
		dateTime.setTime(new Date(Date.UTC(111, 10, 10, 2, 3, 25)));
		String jsonStr = NanoFactory.getJSONWriter().write(dateTime);
		DateTime dateTimeNew = NanoFactory.getJSONReader().read(DateTime.class, jsonStr);
		assertEquals(dateTimeNew, dateTime);
	}
}
