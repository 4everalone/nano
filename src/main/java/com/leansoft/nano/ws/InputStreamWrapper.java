package com.leansoft.nano.ws;

import java.io.IOException;
import java.io.InputStream;

import java.io.ByteArrayOutputStream;

public class InputStreamWrapper extends InputStream
{
   private InputStream is;
   private boolean useBuffer;
   
   private ByteArrayOutputStream bos = new ByteArrayOutputStream();
   
   public InputStreamWrapper(InputStream is, boolean useBuffer)
   {
      this.useBuffer = useBuffer;
      this.is = is;
   }
   
   @Override
   public int read() throws IOException
   {
      int ch = is.read();
      if (ch != -1 && useBuffer)
      {
         bos.write(ch);
      }
      return ch;
   }
   
   public String toString(String charsetName) throws java.io.UnsupportedEncodingException
   {
      return bos.toString(charsetName);
   }

}
