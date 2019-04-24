package com.leansoft.nano.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import com.leansoft.nano.NanoBaseUnitTest;

public class Base64Test extends NanoBaseUnitTest 
{
   public void testBase64encodeWithOffset()
   {
      String whole = "1234567890"; 
      String str = Base64.encode(whole.getBytes(), 6, 4);
      assertFalse(str.isEmpty());
      String decoded = new String(Base64.decode(str));
      assertEquals("7890", decoded);
   }
   
   public void testBase64EncodeWithLengthMoreThanWeHave()
   {
      String whole = "1234567890";
      boolean thereWasException = false;
      try
      {
         Base64.encode(whole.getBytes(), 6, 10);
      }
      catch (ArrayIndexOutOfBoundsException ex)
      {
         thereWasException = true;
      }
      assertEquals(true, thereWasException);
   }

   public void testBase64encodeWithOffsetToStream() throws IOException
   {
      String whole = "1234567890";
      ByteArrayOutputStream os = new ByteArrayOutputStream(10);
      Base64.encode(whole.getBytes(), 6, 4, os);
      assertNotSame(0, os.size());
      String decoded = new String(Base64.decode(os.toString()));
      assertEquals("7890", decoded);
   }
   
   public void testBase64encodeWithOffsetToWriter() throws IOException
   {
      String whole = "1234567890";
      StringWriter wr = new StringWriter();
      
      Base64.encode(whole.getBytes(), 6, 4, wr);
            
      assertFalse(wr.getBuffer().toString().isEmpty());
      String decoded = new String(Base64.decode(wr.getBuffer().toString()));
      assertEquals("7890", decoded);
   }
   

}
